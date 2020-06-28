package com.xtra.core.controller.api;


import com.xtra.core.model.Stream;
import com.xtra.core.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/streams")
public class StreamOpController {
    private final StreamService streamService;

    @Autowired
    public StreamOpController(StreamService streamService) {
        this.streamService = streamService;
    }

    @PostMapping("/start")
    public String startStream(@RequestBody Stream stream){
        return streamService.StartStream(stream);
    }
}
