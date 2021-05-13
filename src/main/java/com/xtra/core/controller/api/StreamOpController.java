package com.xtra.core.controller.api;

import com.xtra.core.projection.catchup.CatchupRecordView;
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

    @GetMapping("/start")
    public boolean startAllStream() {
        return streamService.startAllStreams();
    }

    @GetMapping("/stop")
    public boolean stopAllStreams() {
        return streamService.stopAllStreams();
    }

    @GetMapping("/restart")
    public boolean restartAllStreams() {
        return streamService.restartAllStreams();
    }

    @PostMapping("/batch-stop")
    public boolean batchStopStreams(@RequestBody List<Long> streamIds){
        return streamService.batchStopStreams(streamIds);
    }

    //catch-up
    @PostMapping("{id}/catch-up/record")
    public ResponseEntity<Boolean> record(@PathVariable Long id, @RequestBody CatchupRecordView catchupRecordView){
        return ResponseEntity.ok(streamService.record(id, catchupRecordView));
    }
}
