package com.xtra.core.controller.api;

import com.xtra.core.dto.ChannelStart;
import com.xtra.core.dto.catchup.CatchupRecordView;
import com.xtra.core.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("start")
    public ResponseEntity<?> startStream(@RequestBody ChannelStart channelStart) {
        streamService.startStream(channelStart, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("restart")
    public ResponseEntity<?> restartStream(@RequestBody ChannelStart channelStart) {
        streamService.startStream(channelStart, true);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{id}/stop")
    public ResponseEntity<?> stopStream(@PathVariable Long id) {
        streamService.stopStream(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{id}/restart")
    public ResponseEntity<?> restartStream(@PathVariable Long id) {
        streamService.restartStream(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/start")
    public ResponseEntity<?> startAllStream(List<ChannelStart> channelStarts) {
        streamService.startAllStreams(channelStarts);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stop")
    public ResponseEntity<?> stopAllStreams() {
        streamService.stopAllStreams();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/restart")
    public ResponseEntity<?> restartAllStreams() {
        streamService.restartAllStreams();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch-stop")
    public ResponseEntity<?> batchStopStreams(@RequestBody List<Long> streamIds) {
        streamService.batchStopStreams(streamIds);
        return ResponseEntity.ok().build();
    }

    //catch-up
    @PostMapping("{id}/catch-up/record")
    public ResponseEntity<Boolean> record(@PathVariable Long id, @RequestBody CatchupRecordView catchupRecordView) {
        return ResponseEntity.ok(streamService.record(id, catchupRecordView));
    }
}
