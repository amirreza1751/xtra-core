package com.xtra.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtra.core.config.DynamicConfig;
import com.xtra.core.dto.*;
import com.xtra.core.model.*;
import com.xtra.core.model.exception.EntityNotFoundException;
import com.xtra.core.repository.VideoRepository;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import static org.springframework.beans.BeanUtils.copyProperties;
import java.io.*;
import java.io.File;
import java.lang.Process;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.web.client.RestClientException;

import static com.xtra.core.utility.Util.removeQuotations;
@Log4j2
@Service
public class VodService {

    @Value("${main.apiPath}")
    private String mainApiPath;
    @Value("${nginx.address}")
    private String serverAddress;
    @Value("${vod.root.path}")
    private String vodRootPath;
    @Value("${vod.path.prefix}")
    private String vodPathPrefix;
    private final ProcessService processService;
    private final LineService lineService;
    private final ApiService apiService;
    private final DynamicConfig config;
    private final VideoRepository videoRepository;

    public VodService(ProcessService processService, LineService lineService, ApiService apiService, DynamicConfig config, VideoRepository videoRepository) {
        this.processService = processService;
        this.lineService = lineService;
        this.apiService = apiService;
        this.config = config;
        this.videoRepository = videoRepository;
    }

    public void encode(EncodeRequest encodeRequest) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(() -> {
            String directory = Paths.get(vodPathPrefix + File.separator + encodeRequest.getSourceLocation()).getParent().toString();
            File outputDirectory = new File(
                    directory + File.separator + encodeRequest.getVideoId() + "_output"
            );
            if (!outputDirectory.exists()) {
                var result = outputDirectory.mkdirs();
                if (!result) {
                    throw new RuntimeException("Could not create directory");
                }
            }
            EncodeResponse encodeResponse = new EncodeResponse();
             List<String> args = new ArrayList<String>(Arrays.asList(
                     //Add base options of FFMPEG here
                    "ffmpeg",
                    "-v",
                    "quiet",
                    "-y",
                    "-i",
                    vodPathPrefix + File.separator + encodeRequest.getSourceLocation(), //Input Video
                    "-c:v",
                    encodeRequest.getTargetVideoCodec().getCodec(), // Output Video Codec
                    "-c:a",
                    encodeRequest.getTargetAudioCodec().getCodec() // Output Audio Codec
                     ));

             List<String> targetVideoPathList = new ArrayList<>();
            for (Resolution resolution : encodeRequest.getTargetResolutions()){
                //Add options here for each video output
                args.addAll(Arrays.asList("-vf", "scale=" + resolution.getWidth() + ":-2", outputDirectory + File.separator + resolution.getText() + ".mp4"));
                var relativeDirectory = Paths.get(encodeRequest.getSourceLocation()).getParent() == null ? "" : Paths.get(encodeRequest.getSourceLocation()).getParent() + File.separator;
                var relativeVideoPath = relativeDirectory + encodeRequest.getVideoId() + "_output" + File.separator + resolution.getText() + ".mp4";
                targetVideoPathList.add(relativeVideoPath);
            }
            log.info(args.toString());
            Process proc;
            try {
                proc = new ProcessBuilder(args).start();
                proc.waitFor();
            } catch (IOException | InterruptedException e) {
                log.error("Video " + encodeRequest.getVideoId() + " " + encodeRequest.getSourceLocation() + " encoding failed.");
                encodeResponse.setEncodeStatus(EncodeStatus.ENCODING_FAILED);
                this.updateVodStatus(encodeRequest.getVideoId(), encodeResponse);
            }
            log.info("Video " + encodeRequest.getVideoId() + " " + encodeRequest.getSourceLocation() + " successfully encoded.");
            encodeResponse.setEncodeStatus(EncodeStatus.ENCODED);
            encodeResponse.setTargetVideoInfos(getBatchMediaInfo(targetVideoPathList));
            this.updateVodStatus(encodeRequest.getVideoId(), encodeResponse);
        });
    }

    public void updateVodStatus(Long id, EncodeResponse encodeResponse) {
        try {
            apiService.sendPatchRequest("/system/videos/" + id + "/encode_status", String.class,encodeResponse);
        } catch (RestClientException e) {
            System.out.println(e.getMessage());
        }
    }

    public Video getVideoByToken(String vodToken) {
        try {
            return apiService.sendGetRequest("/system/videos/" + vodToken, Video.class);
        } catch (RestClientException e) {
            //@todo log exception
            System.out.println(e.getMessage());
            return null;
        }
    }

    public VideoInfoView getMediaInfo(String location) {
            location = URLDecoder.decode(location, StandardCharsets.UTF_8);
            VideoInfoView info = new VideoInfoView();
            String result = processService.getMediaInfo(vodPathPrefix + File.separator + location);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                var root = objectMapper.readTree(result);
                var video = root.get("streams").get(0);
                info.setVideoCodec(VideoCodec.findByText(removeQuotations(video.get("codec_name").toString())));
                info.setResolution(Resolution.findByWidth(removeQuotations(video.get("width").toString())));

                var audio = root.get("streams").get(1);
                info.setAudioCodec(AudioCodec.findByText(removeQuotations(audio.get("codec_name").toString())));

                var duration = root.get("format").get("duration").toString();
                info.setDuration(Duration.ofSeconds((int) Float.parseFloat(removeQuotations(duration))));

                var fileSize = root.get("format").get("size").toString();
                info.setFileSize(removeQuotations(fileSize));
            } catch (JsonProcessingException | NullPointerException e) {
                log.error("Get video info failed.");
            }
            log.info("Video Info retrieved Successfully.");
        return info;
    }

    public List<VideoInfoView> getBatchMediaInfo(List<String> paths){
        List<VideoInfoView> videoInfoViewList = new ArrayList<>();
        for (String path : paths){
            videoInfoViewList.add(getMediaInfo(path));
        }
        return videoInfoViewList;
    }

    public String getVodPlaylist(String lineToken, String vodToken, String ipAddress, String userAgent) throws IOException {
        LineStatus lineStatus = lineService.authorizeLineForVod(new LineAuth(lineToken, vodToken, ipAddress, userAgent, config.getServerToken()));
        if (lineStatus != LineStatus.OK)
            throw new RuntimeException("Forbidden " + HttpStatus.FORBIDDEN);
        else {
            var video = updateVideoFromMainServer(vodToken);
            log.info("Video saved: " + video.getToken() + " " + video.getSourceLocation());
            URL url = new URL(serverAddress + ":1234" + "/hls/" + lineToken + "_" + vodToken + "_" + ipAddress + ".json/master.m3u8");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            int status = con.getResponseCode();
            if (status == 500) {
                throw new RuntimeException("Internal Server Error");
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine).append("\n");
            }
            in.close();

            return content.toString();
        }

    }
    private Video updateVideoFromMainServer(String vodToken){
        Video video = this.getVideoByToken(vodToken);
        if (video == null)
            log.error("video by token not found. vod token: " + vodToken);
        var vid = videoRepository.findByToken(vodToken);
        if (vid.isPresent()){
            Video existingVideo = vid.get();
            existingVideo.getSourceSubtitles().clear();
            existingVideo.getSourceAudios().clear();
            existingVideo.getTargetResolutions().clear();
            existingVideo.getSourceSubtitles().addAll(video.getSourceSubtitles());
            existingVideo.getSourceAudios().addAll(video.getSourceAudios());
            existingVideo.getTargetResolutions().addAll(video.getTargetResolutions());
            existingVideo.setVideoId(video.getId());
            video = videoRepository.save(existingVideo);
        } else{
            video.setVideoId(video.getId());
            video = videoRepository.save(video);
        }
        return  video;
    }

    public String jsonHandler(String token, String ipAddress, String userAgent) {
        String[] tokens = token.split("_");
        //tokens[0] => line token
        //tokens[1] => vod token
        //tokens[2] => ip address
        LineStatus lineStatus = lineService.authorizeLineForVod(new LineAuth(tokens[0], tokens[1], tokens[2].replace(".json", ""), userAgent, config.getServerToken()));
        if (lineStatus != LineStatus.OK){
            throw new RuntimeException("Forbidden " + HttpStatus.FORBIDDEN);
        }
        else {
            Video video = videoRepository.findByToken(tokens[1].replace(".json", "")).orElseThrow();
            var relativeDirectory = Paths.get(video.getSourceLocation()).getParent() == null ? "" : Paths.get(video.getSourceLocation()).getParent() + File.separator;
            JSONArray sequences = new JSONArray();
            if (video.getSourceSubtitles() != null) {
                for (Subtitle subtitle : video.getSourceSubtitles()) {
                    JSONObject clips_object = new JSONObject();
                    clips_object.put("language", subtitle.getLanguage());
                    clips_object.put("clips", new JSONArray()
                            .put(new JSONObject()
                                    .put("type", "source")
                                    .put("path", vodRootPath + File.separator + subtitle.getLocation())));

                    sequences.put(clips_object);
                }
            }
            for (Resolution resolution : video.getTargetResolutions()){
                sequences.put(new JSONObject()
                        .put("clips", new JSONArray()
                                .put(new JSONObject()
                                        .put("type", "source")
                                        .put("path", vodRootPath + File.separator + relativeDirectory + video.getVideoId() + "_output" + File.separator + resolution.getText() + ".mp4"))));
            }
            log.info(new JSONObject()
                    .put("sequences", sequences)
                    .toString());
            return new JSONObject()
                    .put("sequences", sequences)
                    .toString();
        }
    }

}
