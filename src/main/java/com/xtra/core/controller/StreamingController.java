package com.xtra.core.controller;

import com.xtra.core.model.ProgressInfo;
import com.xtra.core.model.Subtitle;
import com.xtra.core.repository.ProgressInfoRepository;
import com.xtra.core.service.LineService;
import com.xtra.core.service.ProcessService;
import com.xtra.core.service.VodService;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class StreamingController {
    private final LineService lineService;
    private final ProgressInfoRepository progressInfoRepository;

    @Value("${nginx.port}")
    private String localServerPort;
    @Value("${nginx.address}")
    private String serverAddress;

    @Autowired
    public StreamingController(LineService lineService, ProgressInfoRepository progressInfoRepository) {
        this.lineService = lineService;
        this.progressInfoRepository = progressInfoRepository;
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

    @GetMapping("vod/{line_id}/{stream_id}")
    public @ResponseBody
    ResponseEntity<String> getVodPlaylist(@PathVariable String line_id, @PathVariable String stream_id) throws IOException {
        ResponseEntity<String> lineResponse = lineService.authorizeLine(Integer.parseInt(stream_id), Integer.parseInt(line_id));
        HttpHeaders responseHeaders = new HttpHeaders();
        if (lineResponse.getStatusCode() != HttpStatus.ACCEPTED)
            return lineResponse;
        else
            {
            URL url = new URL("http://"+serverAddress + localServerPort+"/hls/" + stream_id + ".json/master.m3u8");
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
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        response = ResponseEntity.ok()
                .headers(responseHeaders).contentType(MediaType.APPLICATION_JSON)
                .headers(responseHeaders).contentLength(Long.parseLong(String.valueOf(json_file.length())))
                .headers(responseHeaders).cacheControl(CacheControl.noCache())
                .headers(responseHeaders).cacheControl(CacheControl.noStore())
                .header("Content-Disposition", "inline; filename=" + "\"" + file.getName() + "\"")
                .body(json_file);
        return  response;
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

    @GetMapping("vod/encode")
    public String encode(){
        ProcessService p = new ProcessService();
        VodService v = new VodService(p);
        String result = v.encode("/home/amirak/web/subtitletest/DallasTrim.mkv");
        System.out.println(result);
        return result;
    }

    @GetMapping("vod/add_sub")
    public String add_sub() throws IOException {
        ProcessService p = new ProcessService();
        VodService v = new VodService(p);
        List<Subtitle> subs = new ArrayList<>();
        Subtitle sub1 = new Subtitle();
        sub1.setId(1l); sub1.setLanguage("eng"); sub1.setLocation("/home/amirak/web/subtitletest/eng.srt");
        Subtitle sub2 = new Subtitle();
        sub2.setId(2l); sub2.setLanguage("dan"); sub2.setLocation("/home/amirak/web/subtitletest/danish.srt");
        Subtitle sub3 = new Subtitle();
        sub3.setId(3l); sub3.setLanguage("fr"); sub3.setLocation("/home/amirak/web/subtitletest/fr.srt");
        subs.add(sub1);
        subs.add(sub2);
        subs.add(sub3);
        String result = v.add_subtitle("/home/amirak/web/subtitletest/DallasTrim_encoded.mp4", subs);
        System.out.println(result);
        return result;
    }
}