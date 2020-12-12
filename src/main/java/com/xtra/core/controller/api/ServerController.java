package com.xtra.core.controller.api;

import com.xtra.core.model.Resource;
import com.xtra.core.service.ServerService;
import com.xtra.core.service.StreamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/servers")
public class ServerController {
    private final ServerService serverService;
    private final StreamService streamService;

    public ServerController(ServerService serverService, StreamService streamService) {
        this.serverService = serverService;
        this.streamService = streamService;
    }

    @GetMapping("/resources")
    public Resource getResourceUsage(@RequestParam String interfaceName) throws InterruptedException {
        return serverService.getResourceUsage(interfaceName);
    }

    @GetMapping("/streams/batch-start/")
    public boolean startStream(@RequestParam Long serverId, @RequestParam List<Long> streamIds) {
        return streamService.startStream(serverId, streamIds);
    }

    @GetMapping("/streams/batch-stop/")
    public boolean stopStream(@RequestParam List<Long> streamIds) {
        return streamService.stopStream(streamIds);
    }

    @GetMapping("/streams/batch-restart/")
    public boolean restartStream(@RequestParam Long serverId, @RequestParam List<Long> streamIds) {
        return streamService.restartStream(serverId, streamIds);
    }

}
