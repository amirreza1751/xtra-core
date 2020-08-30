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

    public String setSubtitles(Vod vod) throws IOException {
        String video_path = vod.getLocation();
        List<Subtitle> subtitles = vod.getSubtitles();
        Path path = Paths.get(video_path);
        String file_directory = path.getParent().toString();
        String file_name_without_extension = FilenameUtils.removeExtension(String.valueOf(path.getFileName()));
        String output_video = file_directory + "/" + file_name_without_extension + System.currentTimeMillis() + ".mp4";

        subtitles.removeIf(subtitle -> {
            try {
                return this.getFileEncoding(subtitle).equals("unknown");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        });
        ArrayList<String> args = new ArrayList<>(Arrays.asList(
                "ffmpeg",
                "-i",
                video_path,
                "-c:v",
                "copy",
                "-c:a",
                "copy",
                "-c:s",
                "mov_text",
                output_video,
                "-y"
        ));
        ArrayList<String> sub_info = new ArrayList<>();
        ArrayList<String> map_option = new ArrayList<>();
        ArrayList<String> sub_label = new ArrayList<>();
//        System.out.println("final subs = " + subtitles.toString());
        String encoding;
        for (int i = 0; i < subtitles.size(); i++) {
            encoding = this.getFileEncoding(subtitles.get(i));
//            System.out.println("i = " + i);
            sub_info.addAll(Arrays.asList("-sub_charenc", "\"" + encoding + "\"", "-i", subtitles.get(i).getLocation()));
            map_option.addAll(Arrays.asList("-map", Integer.toString(i)));
            sub_label.addAll(Arrays.asList("-metadata:s:s:" + i, "language=" + subtitles.get(i).getLanguage()));

        }
        map_option.addAll(Arrays.asList("-map", Integer.toString(subtitles.size())));
        args.addAll(args.indexOf("-c:v"), sub_info);
        args.addAll(args.indexOf("-c:v"), map_option);
        args.addAll(args.indexOf(output_video), sub_label);
        Process proc;
        try {
            proc = new ProcessBuilder(args).start();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            return "Add subtitles failed.";
        }
        File file_to_delete = new File(video_path);
        file_to_delete.delete();
        return output_video;
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

    public String setAudios(Vod vod) {
        String video_path = vod.getLocation();
        List<Audio> audios = vod.getAudios();
        Path path = Paths.get(video_path);
        String file_directory = path.getParent().toString();
        String file_name_without_extension = FilenameUtils.removeExtension(String.valueOf(path.getFileName()));
        String output_video = file_directory + "/" + file_name_without_extension + System.currentTimeMillis() + ".mp4";
        ArrayList<String> args = new ArrayList<>(Arrays.asList(
                "ffmpeg",
                "-i",
                video_path,
                //input audios
                "-c",
                "copy",
                "-c:s",
                "mov_text",
                // map option
                "mov_text",
                output_video,
                "-y"
        ));

        ArrayList<String> audio_info = new ArrayList<>();
        ArrayList<String> map_option = new ArrayList<>();
        for (int i = 0; i < audios.size(); i++) {
            audio_info.addAll(Arrays.asList("-i", audios.get(i).getLocation()));
            map_option.addAll(Arrays.asList("-map", Integer.toString(i)));
        }
        map_option.addAll(Arrays.asList("-map", Integer.toString(audios.size())));
        args.addAll(args.indexOf("-c"), audio_info);
        args.addAll(args.indexOf("mov_text"), map_option);

        Process proc;
        try {
            proc = new ProcessBuilder(args).start();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            return "Add audios failed.";
        }
        File file_to_delete = new File(video_path);
        file_to_delete.delete();
        return output_video;
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



    /** Streaming methods */
    /** Streaming methods */
    /** Streaming methods */
    /** Streaming methods */
    /** Streaming methods */
    /** Streaming methods */
    /** Streaming methods */
    /** Streaming methods */
    /** Streaming methods */

    public ResponseEntity<?> getVodPlaylist(String lineToken, String vodToken) throws IOException {
        LineStatus lineStatus = lineService.authorizeLineForVod(lineToken, vodToken);
        HttpHeaders responseHeaders = new HttpHeaders();
        if (lineStatus != LineStatus.OK)
            return new ResponseEntity<>("forbidden", HttpStatus.FORBIDDEN);
        else {
            URL url = new URL(serverAddress + ":1234" + "/hls/" + vodToken + ".json/master.m3u8");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            int status = con.getResponseCode();
            if (status == 500) {
                return ResponseEntity.status(500).body("Internal Server Error");
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine).append("\n");
            }
            in.close();

            return ResponseEntity.ok()
                    .headers(responseHeaders).contentType(MediaType.valueOf("application/x-mpegurl"))
                    .headers(responseHeaders).contentLength(Long.parseLong(String.valueOf(content.length())))
                    .headers(responseHeaders).cacheControl(CacheControl.noCache())
                    .headers(responseHeaders).cacheControl(CacheControl.noStore())
                    .header("Content-Disposition", "inline; filename=" + "\"" + vodToken + ".m3u8" + "\"")
                    .body(content.toString());
        }

    }


    public ResponseEntity<?> jsonHandler(String vod_token) {
        HttpHeaders responseHeaders = new HttpHeaders();
        ResponseEntity<String> response;
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX = " + vod_token.replace(".json", ""));
        var vodId = this.getVodId(vod_token.replace(".json", ""));
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX vodId = " + vodId);
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
        System.out.println(jsonString);

        response = ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.APPLICATION_JSON)
                .headers(responseHeaders).cacheControl(CacheControl.noCache())
                .headers(responseHeaders).cacheControl(CacheControl.noStore())
                .body(jsonString);
        return response;
    }

        /** Streaming methods */
        /** Streaming methods */
        /** Streaming methods */
        /** Streaming methods */
        /** Streaming methods */
        /** Streaming methods */
        /** Streaming methods */
}
