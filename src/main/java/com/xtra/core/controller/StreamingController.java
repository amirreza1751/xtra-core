package com.xtra.core.controller;

import com.xtra.core.model.StreamInfo;
import com.xtra.core.service.LineService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class StreamingController {
    private final LineService lineService;

    @Value("${nginx.port}")
    private String localServerPort;
    @Value("${nginx.address}")
    private String serverAddress;

    @Autowired
    public StreamingController(LineService lineService) {
        this.lineService = lineService;
    }

    @GetMapping("/streams")
    public @ResponseBody
    ResponseEntity<String> GetPlaylist(@RequestParam String line_id, @RequestParam String stream_id
            , @RequestParam String extension) throws IOException {
        //@todo decrypt stream_id and user_id
        HttpHeaders responseHeaders = new HttpHeaders();
        ResponseEntity<String> response;
        ResponseEntity<String> lineResponse = lineService.authorizeLine(Integer.parseInt(stream_id), Integer.parseInt(line_id));
        if (false) {//@todo restore line auth
            return lineResponse;
        } else {
            File file = ResourceUtils.getFile(System.getProperty("user.home") + "/streams/" + stream_id + "_." + extension);
            String playlist = new String(Files.readAllBytes(file.toPath()));

            Pattern pattern = Pattern.compile("(.*)\\.ts");
            Matcher match = pattern.matcher(playlist);

            while (match.find()) {
                String link = match.group(0);
                playlist = playlist.replace(match.group(0), String.format(serverAddress + ":" + localServerPort + "/hls/%s/%s/%s", line_id, stream_id, link.split("_")[1]));
            }
            response = ResponseEntity.ok()
                    .headers(responseHeaders).contentType(MediaType.valueOf("application/x-mpegurl"))
                    .headers(responseHeaders).contentLength(Long.parseLong(String.valueOf(playlist.length())))
                    .headers(responseHeaders).cacheControl(CacheControl.noCache())
                    .headers(responseHeaders).cacheControl(CacheControl.noStore())
                    .header("Content-Disposition", "inline; filename=" + "\"" + file.getName() + "\"")
                    .body(playlist);
        }
        return response;
    }

    @GetMapping("segment")
    public @ResponseBody
    ResponseEntity<byte[]> PlaySegment(@RequestParam String line_id, @RequestParam String stream_id
            , @RequestParam String extension, @RequestParam String segment) throws IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        return ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.valueOf("video/mp2t"))
                .body(IOUtils.toByteArray(FileUtils.openInputStream(new File(System.getProperty("user.home") + "/streams/" + stream_id + "_" + segment + "." + extension))));
    }

    @PostMapping("update")
    public void updateProgress(@RequestParam Long stream_id, InputStream dataStream){
        Scanner s = new Scanner(dataStream).useDelimiter("\\s");
        StreamInfo streamInfo = new StreamInfo();
        while (s.hasNext()){
            var property = s.nextLine();
            var splited = property.split("=");
            switch (splited[0]){
                case "fps":
                    streamInfo.setFrameRate(splited[1]);
                    //save
                    break;
                case "bitrate":
                    streamInfo.setBitrate(splited[1]);
                    break;
                case "speed":
                    streamInfo.setSpeed(splited[1]);
                    break;

            }
        }
    }

    @GetMapping("vod")
    public @ResponseBody
    ResponseEntity<String> getVodPlaylist(@RequestParam String line_id, @RequestParam String stream_id) throws IOException {
        ResponseEntity<String> lineResponse = lineService.authorizeLine(Integer.parseInt(stream_id), Integer.parseInt(line_id));
        HttpHeaders responseHeaders = new HttpHeaders();
        if (lineResponse.getStatusCode() != HttpStatus.ACCEPTED)
            return lineResponse;
        else {
            URL url = new URL("http://vod.test/hls/" + stream_id + "/index.m3u8");
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
                    .header("Content-Disposition", "inline; filename=" + "\"" + "test.m3u8" + "\"")
                    .body(content.toString());
        }

    }

    @GetMapping("vod/auth")
    public ResponseEntity<String> vodAuth(@RequestParam String line_id, @RequestParam String stream_id) {
        ResponseEntity<String> lineResponse = lineService.authorizeLine(Integer.parseInt(stream_id), Integer.parseInt(line_id));
        if (lineResponse.getStatusCode() != HttpStatus.ACCEPTED)
            return lineResponse;
        else {
            return new ResponseEntity<>("Play", HttpStatus.OK);
        }
    }

}