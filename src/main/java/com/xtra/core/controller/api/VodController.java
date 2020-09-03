package com.xtra.core.controller.api;

import com.xtra.core.model.EncodingStatus;
import com.xtra.core.model.MediaInfo;
import com.xtra.core.model.Vod;
import com.xtra.core.service.VodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/vod")
public class VodController {
    private final VodService vodService;

    @Autowired
    public VodController(VodService vodService) {
        this.vodService = vodService;
    }

    @PostMapping("/encode")
    public EncodingStatus encode(@RequestBody Vod vod) throws IOException {
        return vodService.encode(vod);
    }

    @PostMapping("/info")
    public MediaInfo getMediaInfo(@RequestBody Vod vod) {
        return vodService.getMediaInfo(vod);
    }
}
