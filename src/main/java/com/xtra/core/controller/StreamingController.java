package com.xtra.core.controller;

import com.xtra.core.model.LineStatus;
import com.xtra.core.projection.LineAuth;
import com.xtra.core.service.LineService;
import com.xtra.core.service.ProgressInfoService;
import com.xtra.core.service.StreamService;
import com.xtra.core.service.VodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RestController
public class StreamingController {
    private final LineService lineService;
    private final StreamService streamService;
    private final ProgressInfoService progressInfoService;
    private final VodService vodService;

    @Autowired
    public StreamingController(LineService lineService, StreamService streamService, ProgressInfoService progressInfoService, VodService vodService) {
        this.lineService = lineService;
        this.streamService = streamService;
        this.progressInfoService = progressInfoService;
        this.vodService = vodService;
    }


    @GetMapping("/streams")
    public @ResponseBody
    ResponseEntity<String> getPlaylist(@RequestParam("line_token") String lineToken, @RequestParam("stream_token") String streamToken
            , @RequestParam String extension, @RequestHeader(value = "HTTP_USER_AGENT", defaultValue = "") String userAgent, HttpServletRequest request) throws IOException {
        //@todo decrypt stream_id and user_id
        Map<String, String> data = streamService.getPlaylist(lineToken, streamToken, extension, userAgent, request.getRemoteAddr());
        HttpHeaders responseHeaders = new HttpHeaders();
        return ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.valueOf("application/x-mpegurl"))
                .headers(responseHeaders).contentLength(Long.parseLong(String.valueOf(data.get("playlist").length())))
                .headers(responseHeaders).cacheControl(CacheControl.noCache())
                .headers(responseHeaders).cacheControl(CacheControl.noStore())
                .header("Content-Disposition", "inline; filename=" + "\"" + data.get("fileName") + "\"")
                .body(data.get("playlist"));

    }

    @GetMapping("segment/{line_token}/{stream_token}/{segment}/{extension}")
    public @ResponseBody
    ResponseEntity<byte[]> getSegment(@PathVariable("line_token") String lineToken, @PathVariable("stream_token") String streamToken
            , @PathVariable String extension, @PathVariable String segment, @RequestHeader(value = "User-Agent", defaultValue = "") String userAgent, HttpServletRequest request) throws IOException {
        byte[] movie_segment = streamService.getSegment(lineToken, streamToken, extension, segment, userAgent, request.getRemoteAddr());
        HttpHeaders responseHeaders = new HttpHeaders();
        return ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.valueOf("video/mp2t"))
                .body(movie_segment);

    }

    //@todo allow only from localhost
    @PostMapping("update")
    public void updateProgress(@RequestParam("stream_id") Long streamId, InputStream dataStream) {
        progressInfoService.updateProgressInfo(streamId, dataStream);
    }

    @GetMapping("vod/{line_token}/{vod_token}")
    public @ResponseBody
    ResponseEntity<String> getVodPlaylist(@PathVariable("line_token") String lineToken, @PathVariable("vod_token") String vodToken, @RequestHeader(value = "HTTP_USER_AGENT", defaultValue = "") String userAgent, HttpServletRequest request) throws IOException {
        String content = vodService.getVodPlaylist(lineToken, vodToken, request.getRemoteAddr(), userAgent);
        HttpHeaders responseHeaders = new HttpHeaders();
        return ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.valueOf("application/x-mpegurl"))
                .headers(responseHeaders).contentLength(Long.parseLong(String.valueOf(content.length())))
                .headers(responseHeaders).cacheControl(CacheControl.noCache())
                .headers(responseHeaders).cacheControl(CacheControl.noStore())
                .header("Content-Disposition", "inline; filename=" + "\"" + vodToken + ".m3u8" + "\"")
                .body(content);
    }

    @GetMapping("vod/json_handler/hls/{vod_token}")
    public ResponseEntity<String> jsonHandler(@PathVariable String vod_token) {
        String jsonString = vodService.jsonHandler(vod_token);
        HttpHeaders responseHeaders = new HttpHeaders();
        return ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.APPLICATION_JSON)
                .headers(responseHeaders).cacheControl(CacheControl.noCache())
                .headers(responseHeaders).cacheControl(CacheControl.noStore())
                .body(jsonString);
    }

    @GetMapping("vod/auth")
    public ResponseEntity<String> vodAuth(@RequestParam String lineToken, @RequestParam String vodToken, @RequestHeader(value = "HTTP_USER_AGENT", defaultValue = "") String userAgent, HttpServletRequest request) {
        LineStatus lineStatus = lineService.authorizeLineForVod(new LineAuth(lineToken, vodToken, request.getRemoteAddr(), userAgent));
        if (lineStatus != LineStatus.OK)
            return new ResponseEntity<>("forbidden", HttpStatus.FORBIDDEN);
        else {
            return new ResponseEntity<>("Play", HttpStatus.OK);
        }
    }
}