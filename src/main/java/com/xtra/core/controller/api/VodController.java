package com.xtra.core.controller.api;

import com.xtra.core.model.Audio;
import com.xtra.core.model.Subtitle;
import com.xtra.core.service.VodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/vod")
public class VodController {
    private final VodService vodService;

    @Autowired
    public VodController(VodService vodService) {
        this.vodService = vodService;
    }

    @GetMapping("/encode")
    public String encode(@RequestParam String video_path){
        return vodService.encode(video_path);
    }

    @GetMapping("/addAudios")
    public String addAudios(@RequestParam String video_path, @RequestParam List<Audio> audios) {
        return vodService.addAudios(video_path, audios);
    }

    @GetMapping("/addSubtitles")
    public String addSubtitles(@RequestParam String video_path, @RequestParam List<Subtitle> subtitles) throws IOException {
        return vodService.addSubtitles(video_path, subtitles);
    }
}
