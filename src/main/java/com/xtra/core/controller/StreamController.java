package com.xtra.core.controller;

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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class StreamController {

    @GetMapping("/streams")
    public @ResponseBody
    ResponseEntity<String> GetPlaylist(@RequestParam String user_id, @RequestParam String stream_id
            , @RequestParam String extension) throws IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        File file = ResourceUtils.getFile(System.getProperty("user.home") + "/streams/" + stream_id + "_." + extension);
        String playlist = new String(Files.readAllBytes(file.toPath()));

        Pattern pattern = Pattern.compile("(.*)\\.ts");
        Matcher match = pattern.matcher(playlist);

        while (match.find()) {
            playlist = playlist.replace(match.group(0), String.format("/hls/%s/", user_id) + match.group(0));
        }

        return ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.valueOf("application/x-mpegurl"))
                .headers(responseHeaders).contentLength(Long.parseLong(String.valueOf(playlist.length())))
                .headers(responseHeaders).cacheControl(CacheControl.noCache())
                .headers(responseHeaders).cacheControl(CacheControl.noStore())
                .header("Content-Disposition", "inline; filename=" + "\"" + file.getName() + "\"")
                .body(playlist);
    }

    @GetMapping("segment")
    public @ResponseBody
    ResponseEntity<byte[]> PlaySegment(@RequestParam String user_id, @RequestParam String stream_id
            , @RequestParam String extension, @RequestParam String segment_id) throws IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        return ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.valueOf("video/mp2t"))
                .body(IOUtils.toByteArray(FileUtils.openInputStream(new File(System.getProperty("user.home") + "/streams/" + stream_id + "_" + segment_id + "." + extension))));
    }

}