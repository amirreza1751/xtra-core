package com.xtra.core.controller.api;

import com.xtra.core.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/streams")
public class StreamOpController {
    private final StreamService streamService;

    @Autowired
    public StreamOpController(StreamService streamService) {
        this.streamService = streamService;
    }

    @GetMapping("{id}/start")
    public boolean startStream(@PathVariable Long id) {
        return streamService.startStream(id);
    }

    @GetMapping("{id}/stop")
    public boolean stopStream(@PathVariable Long id) {
        return streamService.stopStream(id);
    }

    @GetMapping("{id}/restart")
    public boolean restartStream(@PathVariable Long id) {
        return streamService.restartStream(id);
    }

    @GetMapping("/streams/batch-start/")
    public boolean startStream(@RequestParam List<Long> streamIds) {
        return streamService.startStream(streamIds);
    }

    @GetMapping("/streams/batch-stop/")
    public boolean stopStream(@RequestParam List<Long> streamIds) {
        return streamService.stopStream(streamIds);
    }

    @GetMapping("/streams/batch-restart/")
    public boolean restartStream(@RequestParam List<Long> streamIds) {
        return streamService.restartStream(streamIds);
    }
}
