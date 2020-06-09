package com.xtra.core.controller;

import org.springframework.http.*;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class StreamController {

    @RequestMapping(
            value = "/auth"
    )
    public @ResponseBody
    ResponseEntity<String> authenticateUser(@RequestParam String user_id, @RequestParam String stream_id, @RequestParam String extension) throws IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        String userHome = System.getProperty("user.home");
        File file = ResourceUtils.getFile(userHome + "/streams/" + stream_id + "_." + extension);
        File file = ResourceUtils.getFile("/home/amirak/xtreamcodes/iptv_xtream_codes/streams/" + allRequestParams.get("stream_unique_id") + "_." + allRequestParams.get("extension"));

        String playlist = new String(Files.readAllBytes(file.toPath()));


        Pattern pattern = Pattern.compile("(.*)\\.ts");
        Matcher match = pattern.matcher(playlist);

        while (match.find()) {
            playlist = playlist.replace(match.group(0), "/home/amirak/xtreamcodes/iptv_xtream_codes/streams/" + match.group(0));
        }
        System.out.println(playlist);
        return ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.valueOf("application/x-mpegurl"))
                .headers(responseHeaders).contentLength(Long.parseLong(String.valueOf(playlist.length())))
                .headers(responseHeaders).cacheControl(CacheControl.noCache())
                .headers(responseHeaders).cacheControl(CacheControl.noStore())
                .header("Content-Disposition", "inline; filename=" + "\"" + file.getName() + "\"")
                .body(playlist);
    }

}