package com.xtra.core.controller.api;

import com.xtra.core.model.Resource;
import com.xtra.core.schedule.CoreTaskScheduler;
import com.xtra.core.service.ServerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/servers")
public class ServerController {
    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping("/resources")
    public Resource getResourceUsage(@RequestParam String interfaceName) throws InterruptedException {
        return serverService.getResourceUsage(interfaceName);
    }

}
