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
        String playlist = new String(Files.readAllBytes(file.toPath()));
        Pattern pattern = Pattern.compile("(.*)\\.ts");
        Matcher match = pattern.matcher(playlist);

        System.out.println("Found value: " + match.group(0));

        return ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.valueOf("application/x-mpegurl"))
                .headers(responseHeaders).contentLength(Long.parseLong(String.valueOf(playlist.length())))
                .headers(responseHeaders).cacheControl(CacheControl.noCache())
                .headers(responseHeaders).cacheControl(CacheControl.noStore())
                .header("Content-Disposition", "inline; filename=" + "\"" + file.getName() + "\"")
                .body(playlist);
    }

}