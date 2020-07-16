package com.xtra.core.controller.api;


import com.xtra.core.model.Stream;
import com.xtra.core.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/streams")
public class StreamOpController {
    private final StreamService streamService;

    @Autowired
    public StreamOpController(StreamService streamService) {
        this.streamService = streamService;
    }

    @PostMapping("/start")
    public long startStream(@RequestBody Stream stream){
        return streamService.startStream(stream);
    }

    @GetMapping("/stop/{streamId}")
    public boolean stopStream(@PathVariable Long streamId){return streamService.stopStream(streamId);}

    @PostMapping("/restart")
    public long restartStream(@RequestBody Stream stream){
        return streamService.restartStream(stream);
    }
}
