package com.xtra.core.controller.api;

import com.xtra.core.schedule.CoreTaskScheduler;
import com.xtra.core.service.LineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lines")
public class LineController {
    LineService lineService;

    @Autowired
    public LineController(LineService lineService) {
        this.lineService = lineService;
    }

    @GetMapping("/kill_connections/{line_id}")
    public ResponseEntity<String> killConnection(@PathVariable("line_id") Long lineId) {
        if (lineService.killAllConnections(lineId)) {
            return ResponseEntity.ok("connection killed");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Connection Not Found");
        }
    }

}
