package com.xtra.core.controller.api;
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

    @GetMapping("/start/{streamId}")
    public boolean startStream(@PathVariable Long streamId){
        return streamService.startStream(streamId);
    }

    @GetMapping("/stop/{streamId}")
    public boolean stopStream(@PathVariable Long streamId){return streamService.stopStream(streamId);}

    @GetMapping("/restart/{streamId}")
    public boolean restartStream(@PathVariable Long streamId){
        return streamService.restartStream(streamId);
    }
}
