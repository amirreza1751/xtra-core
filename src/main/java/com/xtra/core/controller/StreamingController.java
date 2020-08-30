package com.xtra.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtra.core.model.LineStatus;
import com.xtra.core.model.MediaInfo;
import com.xtra.core.model.Subtitle;
import com.xtra.core.model.Vod;
import com.xtra.core.service.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xtra.core.utility.Util.removeQuotations;

@RestController
public class StreamingController {
    private final LineService lineService;
    private final ProcessService processService;
    private final StreamService streamService;
    private final ProgressInfoService progressInfoService;
    private final LineActivityService lineActivityService;
    private final VodService vodService;

    @Value("${nginx.port}")
    private String localServerPort;
    @Value("${nginx.address}")
    private String serverAddress;

    @Autowired
    public StreamingController(LineService lineService, ProcessService processService, StreamService streamService, ProgressInfoService progressInfoService, LineActivityService lineActivityService, VodService vodService) {
        this.lineService = lineService;
        this.processService = processService;
        this.streamService = streamService;
        this.progressInfoService = progressInfoService;
        this.lineActivityService = lineActivityService;
        this.vodService = vodService;
    }


    @GetMapping("/streams")
    public @ResponseBody
    ResponseEntity<String> getPlaylist(@RequestParam("line_token") String lineToken, @RequestParam("stream_token") String streamToken
            , @RequestParam String extension, @RequestHeader(value = "HTTP_USER_AGENT", defaultValue = "") String userAgent, HttpServletRequest request) throws IOException {
        //@todo decrypt stream_id and user_id
        Map<String, String> data = streamService.getPlaylist(lineToken, streamToken, extension, userAgent, request);
        HttpHeaders responseHeaders = new HttpHeaders();
        return  ResponseEntity.ok()
                    .headers(responseHeaders).contentType(MediaType.valueOf("application/x-mpegurl"))
                    .headers(responseHeaders).contentLength(Long.parseLong(String.valueOf(data.get("playlist").length())))
                    .headers(responseHeaders).cacheControl(CacheControl.noCache())
                    .headers(responseHeaders).cacheControl(CacheControl.noStore())
                    .header("Content-Disposition", "inline; filename=" + "\"" + data.get("fileName") + "\"")
                    .body(data.get("playlist"));

    }

    @GetMapping("segment")
    public @ResponseBody
    ResponseEntity<byte[]> getSegment(@RequestParam("line_token") String lineToken, @RequestParam("stream_token") String streamToken
            , @RequestParam String extension, @RequestParam String segment, @RequestHeader(value = "HTTP_USER_AGENT", defaultValue = "") String userAgent, HttpServletRequest request) throws IOException {
        LineStatus status = lineService.authorizeLineForStream(lineToken, streamToken);
        Long streamId = streamService.getStreamId(streamToken);
        Long lineId = lineService.getLineId(lineToken);
        if (status == LineStatus.OK) {
            var result = lineActivityService.updateLineActivity(lineId, streamId, request.getRemoteAddr(), userAgent);
            if (!result) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            HttpHeaders responseHeaders = new HttpHeaders();
            return ResponseEntity.ok()
                    .headers(responseHeaders).contentType(MediaType.valueOf("video/mp2t"))
                    .body(IOUtils.toByteArray(FileUtils.openInputStream(new File(System.getProperty("user.home") + "/streams/" + streamId + "_" + segment + "." + extension))));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }


    }

    //@todo allow only from localhost
    @PostMapping("update")
    public void updateProgress(@RequestParam("stream_id") Long streamId, InputStream dataStream) {
        progressInfoService.updateProgressInfo(streamId, dataStream);
    }

    @GetMapping("vod/{line_token}/{vod_token}")
    public @ResponseBody
    ResponseEntity<String> getVodPlaylist(@PathVariable("line_token") String lineToken, @PathVariable("vod_token") String vodToken) throws IOException {
        String content = vodService.getVodPlaylist(lineToken, vodToken);
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
    public ResponseEntity<String> vodAuth(@RequestParam String lineToken, @RequestParam String streamToken) {
        LineStatus lineStatus = lineService.authorizeLineForVod(lineToken, streamToken);
        if (lineStatus != LineStatus.OK)
            return new ResponseEntity<>("forbidden", HttpStatus.FORBIDDEN);
        else {
            return new ResponseEntity<>("Play", HttpStatus.OK);
        }
    }
}