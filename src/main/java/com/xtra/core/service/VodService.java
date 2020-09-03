package com.xtra.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtra.core.model.*;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static com.xtra.core.utility.Util.removeQuotations;

@Service
public class VodService {

    @Value("${main.apiPath}")
    private String mainApiPath;
    @Value("${nginx.address}")
    private String serverAddress;
    private final ProcessService processService;
    private final LineService lineService;

    public VodService(ProcessService processService, LineService lineService) {
        this.processService = processService;
        this.lineService = lineService;
    }

    public EncodingStatus encode(Vod vod) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        executor.submit(() -> {
            String video_path = vod.getLocation();
            Path path = Paths.get(video_path);
            String file_directory = path.getParent().toString();
            String file_name_without_extension = FilenameUtils.removeExtension(String.valueOf(path.getFileName()));
            String output_video = file_directory + File.separator + file_name_without_extension + System.currentTimeMillis() + ".mp4";
            Map<String, String> data = new HashMap<>();
            String[] args = new String[]{
                    "ffmpeg",
                    "-i",
                    video_path,
                    "-vcodec",
                    "copy",
    //                "-preset",
    //                "veryfast",
    //                 "libx264",
                    "-acodec",
                    "copy",
    //                 "aac",
                    output_video,
                    "-y"
            };

            Process proc;
            try {
                proc = new ProcessBuilder(args).start();
                proc.waitFor();
            } catch (IOException | InterruptedException e) {
                data.put("encodeStatus", EncodingStatus.NOT_ENCODED.toString());
                this.updateVodStatus(vod.getId(), data);
            }
                Path mp4_path = Paths.get(file_directory + File.separator + file_name_without_extension + ".mp4");
                try {
                    Files.deleteIfExists(mp4_path); //if old files with same name exists
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
                data.put("location", mp4_path.toString());
                data.put("encodeStatus", EncodingStatus.ENCODED.toString());
                this.updateVodStatus(vod.getId(), data);
        });
        return EncodingStatus.ENCODING;
    }
    public void updateVodStatus(Long id, Map<String, String> data){
        try {
            new RestTemplate().patchForObject(mainApiPath + "/vod/" + id, data, String.class);
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


    public Vod getVod(String vodId) {
        RestTemplate restTemplate = new RestTemplate();
        Vod vod = restTemplate.getForObject(mainApiPath + "/movies/" + vodId, Vod.class);
        return vod;
    }


    public Long getVodId(String vodToken) {
        try {
            return new RestTemplate().getForObject(mainApiPath + "/movies/get_id/" + vodToken, Long.class);
        } catch (RestClientException e) {
            //@todo log exception
            System.out.println(e.getMessage());
            return null;
        }
    }

    public MediaInfo getMediaInfo(Vod vod) {
        String result = processService.getMediaInfo(vod.getLocation());
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
        } catch (JsonProcessingException e) {
            return null;
        }
        return info;
    }




    public String getVodPlaylist(String lineToken, String vodToken) throws IOException {
        LineStatus lineStatus = lineService.authorizeLineForVod(lineToken, vodToken);
        if (lineStatus != LineStatus.OK)
//            return new ResponseEntity<>("forbidden", HttpStatus.FORBIDDEN);
            throw new RuntimeException("Forbidden " + HttpStatus.FORBIDDEN);
        else {
            URL url = new URL(serverAddress + ":1234" + "/hls/" + vodToken + ".json/master.m3u8");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            int status = con.getResponseCode();
            if (status == 500) {
//                return ResponseEntity.status(500).body("Internal Server Error");
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


    public String jsonHandler(String vod_token) {
        var vodId = this.getVodId(vod_token.replace(".json", ""));
        Vod vod = this.getVod(vodId.toString());
        JSONArray sequences = new JSONArray();
        for (Subtitle subtitle : vod.getSubtitles()){
            JSONObject clips_object = new JSONObject();
            clips_object.put("language", subtitle.getLanguage());
            clips_object.put("clips", new JSONArray()
                    .put(new JSONObject()
                            .put("type","source")
                            .put("path", subtitle.getLocation())));

            sequences.put(clips_object);
        }
        sequences.put(new JSONObject()
                .put("clips", new JSONArray()
                        .put(new JSONObject()
                                .put("type","source")
                                .put("path", vod.getLocation()))));

        String jsonString = new JSONObject()
                .put("sequences", sequences)
                .toString();
        return jsonString;
    }

}
