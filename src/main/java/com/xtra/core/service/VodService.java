package com.xtra.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtra.core.config.DynamicConfig;
import com.xtra.core.dto.VodStatusView;
import com.xtra.core.model.*;
import com.xtra.core.dto.LineAuth;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.io.File;
import java.lang.Process;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.web.client.RestClientException;

import static com.xtra.core.utility.Util.removeQuotations;

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

    public VodService(ProcessService processService, LineService lineService, ApiService apiService, DynamicConfig config) {
        this.processService = processService;
        this.lineService = lineService;
        this.apiService = apiService;
        this.config = config;
    }

    public void encode(Vod vod) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(() -> {
            String video_path = vodPathPrefix + File.separator + vod.getLocation();
            Path path = Paths.get(video_path);
            String file_directory = path.getParent().toString();
            String file_name_without_extension = FilenameUtils.removeExtension(String.valueOf(path.getFileName()));
            String output_video = file_directory + File.separator + file_name_without_extension + System.currentTimeMillis() + ".mp4";
            VodStatusView status = new VodStatusView();
            String[] args;
            if (encodePreProcessor(video_path)) { // Codecs must be changed.
                args = new String[]{
                        "ffmpeg",
                        "-i",
                        video_path,
                        "-vcodec",
                        "libx264",
                        "-preset",
                        "veryfast",
                        "-acodec",
                        "aac",
                        output_video,
                        "-y"
                };
            } else // Default Condition ( No Change )
            {
                args = new String[]{
                        "ffmpeg",
                        "-i",
                        video_path,
                        "-vcodec",
                        "copy",
                        "-acodec",
                        "copy",
                        output_video,
                        "-y"
                };
            }
            Process proc;
            try {
                proc = new ProcessBuilder(args).start();
                proc.waitFor();
            } catch (IOException | InterruptedException e) {
                status.setStatus(EncodeStatus.ENCODING_FAILED);
                this.updateVodStatus(vod.getId(), status);
            }
            Path mp4_path = Paths.get(file_directory + File.separator + file_name_without_extension + ".mp4");
            try {
//                Files.deleteIfExists(mp4_path); //if old files with same name exists
                Files.deleteIfExists(path); //input mkv file must be deleted
            } catch (IOException e) {
                e.printStackTrace();
            }
            Path output = Paths.get(output_video);
            try {
                Files.move(output, mp4_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            status.setLocation(mp4_path.toString().replace(vodPathPrefix + File.separator, ""));
            status.setStatus(EncodeStatus.ENCODED);
            this.updateVodStatus(vod.getId(), status);
        });
    }

    public void updateVodStatus(Long id, VodStatusView statusView) {
        try {
            apiService.sendPatchRequest("/system/videos/" + id, statusView);
        } catch (RestClientException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getFileEncoding(Subtitle subtitle) throws IOException {
        UniversalDetector detector = new UniversalDetector(null);
        FileInputStream fis;
        byte[] buf = new byte[4096];
        int nread;
        String encoding = "";
        fis = new FileInputStream(subtitle.getLocation());
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        detector.dataEnd();
        encoding = detector.getDetectedCharset();
        detector.reset();
        if (encoding != null) {
//            System.out.println("Detected encoding = " + encoding);
            return encoding;
        } else {
//            System.out.println("No encoding detected.");
            return "unknown";
        }

    }

    public Vod getVodByToken(String vodToken) {
        try {
            return apiService.sendGetRequest("/system/videos/" + vodToken, Vod.class);
        } catch (RestClientException e) {
            //@todo log exception
            System.out.println(e.getMessage());
            return null;
        }
    }

    public List<MediaInfo> getMediaInfo(List<Vod> vodList) {
        List<MediaInfo> mediaInfoList = new ArrayList<>();
        for (Vod vod : vodList) {
            String result = processService.getMediaInfo(vodPathPrefix + vod.getLocation());
            MediaInfo info = new MediaInfo();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                var root = objectMapper.readTree(result);
                var video = root.get("streams").get(0);
                info.setVideoCodec(removeQuotations(video.get("codec_name").toString()));
                info.setResolution(video.get("width") + "x" + video.get("height"));

                var audio = root.get("streams").get(1);
                info.setAudioCodec(removeQuotations(audio.get("codec_name").toString()));

                var duration = root.get("format").get("duration").toString();
                info.setDuration(Duration.ofSeconds((int) Float.parseFloat(removeQuotations(duration))));
            } catch (JsonProcessingException | NullPointerException e) {
                mediaInfoList.add(new MediaInfo("", "", "", Duration.ZERO));
                continue;
            }
            mediaInfoList.add(info);
        }
        return mediaInfoList;
    }

    public boolean encodePreProcessor(String location) {
        String result = processService.getMediaInfo(location);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            var root = objectMapper.readTree(result);
            var videoCodec = removeQuotations(root.get("streams").get(0).get("codec_name").toString());

            var audioCodec = removeQuotations(root.get("streams").get(1).get("codec_name").toString());
            return !videoCodec.equals("h264") || !audioCodec.equals("aac");

        } catch (JsonProcessingException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public String getVodPlaylist(String lineToken, String vodToken, String ipAddress, String userAgent) throws IOException {
        LineStatus lineStatus = lineService.authorizeLineForVod(new LineAuth(lineToken, vodToken, ipAddress, userAgent, config.getServerToken()));
        if (lineStatus != LineStatus.OK)
            throw new RuntimeException("Forbidden " + HttpStatus.FORBIDDEN);
        else {
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

    public String jsonHandler(String token, String ipAddress, String userAgent) {
        String[] tokens = token.split("_");
        //tokens[0] => line token
        //tokens[1] => vod token
        //tokens[2] => ip address
        LineStatus lineStatus = lineService.authorizeLineForVod(new LineAuth(tokens[0], tokens[1], tokens[2].replace(".json", ""), userAgent, config.getServerToken()));
        if (lineStatus != LineStatus.OK)
            throw new RuntimeException("Forbidden " + HttpStatus.FORBIDDEN);
        else {
            Vod vod = this.getVodByToken(tokens[1].replace(".json", ""));
            JSONArray sequences = new JSONArray();
            if (vod.getSubtitles() != null) {
                for (Subtitle subtitle : vod.getSubtitles()) {
                    JSONObject clips_object = new JSONObject();
                    clips_object.put("language", subtitle.getLanguage());
                    clips_object.put("clips", new JSONArray()
                            .put(new JSONObject()
                                    .put("type", "source")
                                    .put("path", vodRootPath + File.separator + subtitle.getLocation())));

                    sequences.put(clips_object);
                }
            }
            sequences.put(new JSONObject()
                    .put("clips", new JSONArray()
                            .put(new JSONObject()
                                    .put("type", "source")
                                    .put("path", vodRootPath + File.separator + vod.getLocation()))));
            System.out.println(new JSONObject()
                    .put("sequences", sequences)
                    .toString());
            return new JSONObject()
                    .put("sequences", sequences)
                    .toString();
        }
    }

}
