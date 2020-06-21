package com.xtra.core.controller;

import com.xtra.core.model.Line;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class StreamController {

    @GetMapping("/streams")
    public @ResponseBody
    ResponseEntity<String> GetPlaylist(@RequestParam String line_id, @RequestParam String stream_id
            , @RequestParam String extension) throws IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        ResponseEntity<String> response;
        RestTemplate restTemplate = new RestTemplate();
        try {
            Line line = restTemplate.getForObject("http://localhost:8082/api/lines/" + line_id, Line.class);
            if (line.getExpireDate().compareTo(LocalDateTime.now()) < 0) {
                response = ResponseEntity.status(403).body("Line Expired");
            } else if (line.isBlocked()) {
                response = ResponseEntity.status(403).body("Line is Blocked");
            } else if (line.isAdminBlocked()) {
                response = ResponseEntity.status(403).body("Line is Blocked By Admin");
            } else {
                File file = ResourceUtils.getFile(System.getProperty("user.home") + "/streams/" + stream_id + "_." + extension);
                String playlist = new String(Files.readAllBytes(file.toPath()));

                Pattern pattern = Pattern.compile("(.*)\\.ts");
                Matcher match = pattern.matcher(playlist);

                while (match.find()) {
                    String link = match.group(0);
                    playlist = playlist.replace(match.group(0), String.format("http://localhost:8081/hls/%s/%s/%s", line_id, stream_id, link.split("_")[1]));
                }
                response = ResponseEntity.ok()
                        .headers(responseHeaders).contentType(MediaType.valueOf("application/x-mpegurl"))
                        .headers(responseHeaders).contentLength(Long.parseLong(String.valueOf(playlist.length())))
                        .headers(responseHeaders).cacheControl(CacheControl.noCache())
                        .headers(responseHeaders).cacheControl(CacheControl.noStore())
                        .header("Content-Disposition", "inline; filename=" + "\"" + file.getName() + "\"")
                        .body(playlist);
            }
        } catch (HttpStatusCodeException exception) {
            int statusCode = exception.getStatusCode().value();
            if (statusCode == 404) {
                response = ResponseEntity.status(404).body("User Not Found");
            } else {
                response = ResponseEntity.status(500).body("Server Internal Error");
            }
        }

        return response;
    }

    @GetMapping("segment")
    public @ResponseBody
    ResponseEntity<byte[]> PlaySegment(@RequestParam String user_id, @RequestParam String stream_id
            , @RequestParam String extension, @RequestParam String segment) throws IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        return ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.valueOf("video/mp2t"))
                .body(IOUtils.toByteArray(FileUtils.openInputStream(new File(System.getProperty("user.home") + "/streams/" + stream_id + "_" + segment + "." + extension))));
    }

    @GetMapping("vod")
    public @ResponseBody
    ResponseEntity<String> vod(@RequestParam String user_id, @RequestParam String vod_id) throws IOException {

        /* Authentication for VOD */

        /* Authentication for VOD */


        /* Preparing Playlist for VOD Content */
        HttpHeaders responseHeaders = new HttpHeaders();
        URL url = new URL("http://vod.test/hls/" + vod_id + "/index.m3u8");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        int status = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine).append("\n");
        }
        in.close();
        /* Preparing Playlist for VOD Content **/


        return ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.valueOf("application/x-mpegurl"))
                .headers(responseHeaders).contentLength(Long.parseLong(String.valueOf(content.length())))
                .headers(responseHeaders).cacheControl(CacheControl.noCache())
                .headers(responseHeaders).cacheControl(CacheControl.noStore())
                .header("Content-Disposition", "inline; filename=" + "\"" + "test.m3u8" + "\"")
                .body(content.toString());
    }

    @GetMapping("vod/auth")
    public ResponseEntity<String> vodAuth()
    {
        return new ResponseEntity<String>("Hello World", HttpStatus.OK);
    }

}