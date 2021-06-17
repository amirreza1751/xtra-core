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

    //Start a new Stream
    @PostMapping("start")
    public ResponseEntity<?> startStream(@RequestBody ChannelStart channelStart) {
        streamService.startStream(channelStart);
        return ResponseEntity.ok().build();
    }

    // Stop a Stream
    @GetMapping("{id}/stop")
    public ResponseEntity<?> stopStream(@PathVariable Long id) {
        streamService.stopStream(id);
        return ResponseEntity.ok().build();
    }

    // Start multiple new Streams
    @PostMapping("batch-start")
    public ResponseEntity<?> startAllStream(List<ChannelStart> channelStarts) {
        streamService.startAllStreams(channelStarts);
        return ResponseEntity.ok().build();
    }

    // Stop multiple Streams
    @PostMapping("batch-stop")
    public ResponseEntity<?> batchStopStreams(@RequestBody List<Long> streamIds) {
        streamService.batchStopStreams(streamIds);
        return ResponseEntity.ok().build();
    }

    // Stop All Streams
    @GetMapping("stop")
    public ResponseEntity<?> stopAllStreams() {
        streamService.stopAllStreams();
        return ResponseEntity.ok().build();
    }

    //catch-up
    @PostMapping("{id}/catch-up/record")
    public ResponseEntity<Boolean> record(@PathVariable Long id, @RequestBody CatchupRecordView catchupRecordView) {
        return ResponseEntity.ok(streamService.record(id, catchupRecordView));
    }
}
