package com.xtra.core.controller.api;

import com.xtra.core.dto.EncodeRequest;
import com.xtra.core.dto.VideoInfoView;
import com.xtra.core.model.EncodeStatus;
import com.xtra.core.model.MediaInfo;
import com.xtra.core.model.Vod;
import com.xtra.core.service.VodService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/encode")
    public ResponseEntity<?> encode(@RequestBody EncodeRequest encodeRequest) {
        vodService.encode(encodeRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/info")
    public ResponseEntity<VideoInfoView> getMediaInfo(@RequestParam String path) {
        return ResponseEntity.ok(vodService.getMediaInfo(path));
    }
}
