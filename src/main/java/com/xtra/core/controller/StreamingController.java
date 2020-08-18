package com.xtra.core.controller;

import com.xtra.core.model.LineStatus;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class StreamingController {
    private final LineService lineService;
    private final StreamService streamService;
    private final ProgressInfoService progressInfoService;
    private final LineActivityService lineActivityService;
    private final VodService vodService;

    @Value("${nginx.port}")
    private String localServerPort;
    @Value("${nginx.address}")
    private String serverAddress;

    @Autowired
    public StreamingController(LineService lineService, StreamService streamService, ProgressInfoService progressInfoService, LineActivityService lineActivityService, VodService vodService) {
        this.lineService = lineService;
        this.streamService = streamService;
        this.progressInfoService = progressInfoService;
        this.lineActivityService = lineActivityService;
        this.vodService = vodService;
    }


    @GetMapping("/streams")
    public @ResponseBody
    ResponseEntity<String> GetPlaylist(@RequestParam("line_token") String lineToken, @RequestParam("stream_token") String streamToken
            , @RequestParam String extension, @RequestHeader(value = "HTTP_USER_AGENT", defaultValue = "") String userAgent, HttpServletRequest request) throws IOException {
        //@todo decrypt stream_id and user_id
        HttpHeaders responseHeaders = new HttpHeaders();
        ResponseEntity<String> response;
        LineStatus status = lineService.authorizeLineForStream(lineToken, streamToken);
        if (status != LineStatus.OK) {
            if (status == LineStatus.NOT_FOUND)
                response = new ResponseEntity<>("Line Not found", HttpStatus.NOT_FOUND);
            else if (status == LineStatus.BANNED)
                response = new ResponseEntity<>("Line is Banned", HttpStatus.FORBIDDEN);
            else if (status == LineStatus.BLOCKED)
                response = new ResponseEntity<>("Line is Blocked", HttpStatus.FORBIDDEN);
            else if (status == LineStatus.EXPIRED)
                response = new ResponseEntity<>("Line is Expired, Please Extend Your Line", HttpStatus.FORBIDDEN);
            else if (status == LineStatus.MAX_CONNECTION_REACHED)
                response = new ResponseEntity<>("You Have Used All of your connection capacity", HttpStatus.FORBIDDEN);
            else if (status == LineStatus.NO_ACCESS_TO_STREAM)
                response = new ResponseEntity<>("Cannot Access Stream", HttpStatus.FORBIDDEN);
            else
                response = new ResponseEntity<>("Unknown Error", HttpStatus.FORBIDDEN);
        } else {
            Long lineId = lineService.getLineId(lineToken);
            Long streamId = streamService.getStreamId(streamToken);
            if (lineId == null || streamId == null) {
                return new ResponseEntity<>("Unknown Error", HttpStatus.FORBIDDEN);
            }

            var result = lineActivityService.updateLineActivity(lineId, streamId, request.getRemoteAddr(), userAgent);

            if (!result) {
                return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
            }

            File file = ResourceUtils.getFile(System.getProperty("user.home") + "/streams/" + streamId + "_." + extension);
            String playlist = new String(Files.readAllBytes(file.toPath()));

            Pattern pattern = Pattern.compile("(.*)\\.ts");
            Matcher match = pattern.matcher(playlist);

            while (match.find()) {
                String link = match.group(0);
                playlist = playlist.replace(match.group(0), String.format(serverAddress + ":" + localServerPort + "/hls/%s/%s/%s", lineToken, streamToken, link.split("_")[1]));
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

    @GetMapping("vod/json_handler/hls/{vod_token}")
        public ResponseEntity<String> jsonHandler(@PathVariable String vod_token) {
            HttpHeaders responseHeaders = new HttpHeaders();
            ResponseEntity<String> response;
            var vodId = vodService.getVodId(vod_token.replace(".json", ""));
            Vod vod = vodService.getVod(vodId.toString());
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

            response = ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.APPLICATION_JSON)
                .headers(responseHeaders).cacheControl(CacheControl.noCache())
                .headers(responseHeaders).cacheControl(CacheControl.noStore())
                .body(jsonString);
        return response;
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