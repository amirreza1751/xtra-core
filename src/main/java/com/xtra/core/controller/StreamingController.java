package com.xtra.core.controller;

import com.xtra.core.model.LineActivity;
import com.xtra.core.model.LineStatus;
import com.xtra.core.model.ProgressInfo;
import com.xtra.core.repository.LineActivityRepository;
import com.xtra.core.repository.ProgressInfoRepository;
import com.xtra.core.service.LineService;
import com.xtra.core.service.StreamService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class StreamingController {
    private final LineService lineService;
    private final StreamService streamService;
    private final ProgressInfoRepository progressInfoRepository;
    private final LineActivityRepository lineActivityRepository;

    @Value("${nginx.port}")
    private String localServerPort;
    @Value("${nginx.address}")
    private String serverAddress;

    @Autowired
    public StreamingController(LineService lineService, ProgressInfoRepository progressInfoRepository, LineActivityRepository lineActivityRepository, StreamService streamService) {
        this.lineService = lineService;
        this.progressInfoRepository = progressInfoRepository;
        this.lineActivityRepository = lineActivityRepository;
        this.streamService = streamService;
    }

    @GetMapping("/streams")
    public @ResponseBody
    ResponseEntity<String> GetPlaylist(@RequestParam("line_token") String lineToken, @RequestParam("stream_token") String streamToken
            , @RequestParam String extension, HttpServletRequest request) throws IOException {
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
            Long streamId = streamService.getStreamId(lineToken);
            if (lineId == null) {
                return new ResponseEntity<>("Unknown Error", HttpStatus.FORBIDDEN);
            }

            LineActivity activity = new LineActivity();
            activity.setLineId(lineId);
            activity.setStreamId(streamId);
            activity.setStartDate(LocalDateTime.now());
            activity.setUserIp(request.getRemoteAddr());
            lineActivityRepository.save(activity);

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
    ResponseEntity<byte[]> getSegment(@RequestParam("line_id") String lineToken, @RequestParam("stream_id") String streamToken
            , @RequestParam String extension, @RequestParam String segment) throws IOException {
        LineStatus status = lineService.authorizeLineForStream(lineToken, streamToken);
        Long streamId = streamService.getStreamId(streamToken);
        if (status == LineStatus.OK) {
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
    public void updateProgress(@RequestParam Long stream_id, InputStream dataStream) {
        Scanner s = new Scanner(dataStream).useDelimiter("\\s");
        ProgressInfo progressInfo = new ProgressInfo(stream_id);
        while (s.hasNextLine()) {
            var property = s.nextLine();
            var splited = property.split("=");
            switch (splited[0]) {
                case "fps":
                    progressInfo.setFrameRate(splited[1]);
                    progressInfoRepository.save(progressInfo);
                    break;
                case "bitrate":
                    progressInfo.setBitrate(splited[1]);
                    break;
                case "speed":
                    progressInfo.setSpeed(splited[1]);
                    break;

            }
        }
    }

    @GetMapping("vod/{line_token}/{stream_token}")
    public @ResponseBody
    ResponseEntity<String> getVodPlaylist(@PathVariable("line_token") String lineToken, @PathVariable("stream_token") String streamToken) throws IOException {
        LineStatus lineStatus = lineService.authorizeLineForVod(lineToken, streamToken);
        HttpHeaders responseHeaders = new HttpHeaders();
        if (lineStatus != LineStatus.OK)
            return new ResponseEntity<>("forbidden", HttpStatus.FORBIDDEN);
        else {
            Long streamId = streamService.getStreamId(streamToken);
            URL url = new URL("http://" + serverAddress + localServerPort + "/hls/" + streamId + ".json/master.m3u8");
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

    @GetMapping("vod/json_handler/hls/{file_name}")
    public ResponseEntity<String> jsonHandler(@PathVariable String file_name) throws IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        ResponseEntity<String> response;
        File file = new File("");
        String json_file = "";
        try {
            file = ResourceUtils.getFile(System.getProperty("user.home") + "/vod/" + file_name);
            json_file = new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        response = ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.APPLICATION_JSON)
                .headers(responseHeaders).contentLength(Long.parseLong(String.valueOf(json_file.length())))
                .headers(responseHeaders).cacheControl(CacheControl.noCache())
                .headers(responseHeaders).cacheControl(CacheControl.noStore())
                .header("Content-Disposition", "inline; filename=" + "\"" + file.getName() + "\"")
                .body(json_file);
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