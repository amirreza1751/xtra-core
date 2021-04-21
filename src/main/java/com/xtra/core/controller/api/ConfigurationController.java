package com.xtra.core.controller.api;

import com.xtra.core.model.Configuration;
import com.xtra.core.service.ConfigurationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
public class ConfigurationController {
    final private ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostMapping("")
    public ResponseEntity<Boolean> startStream(@RequestBody Configuration configuration) {
        return ResponseEntity.ok(configurationService.saveConfig(configuration));
    }
}
