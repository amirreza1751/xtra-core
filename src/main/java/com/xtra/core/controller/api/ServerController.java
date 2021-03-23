package com.xtra.core.controller.api;

import com.xtra.core.model.Resource;
import com.xtra.core.service.ServerService;
import com.xtra.core.service.StreamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("")
public class ServerController {
    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping("/resources")
    public Resource getResourceUsage(@RequestParam String interfaceName) {
        return serverService.getResourceUsage(interfaceName);
    }
}
