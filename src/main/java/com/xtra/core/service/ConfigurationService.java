package com.xtra.core.service;

import com.xtra.core.model.Configuration;
import com.xtra.core.repository.ConfigurationRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
    final private ConfigurationRepository repository;

    public ConfigurationService(ConfigurationRepository repository) {
        this.repository = repository;
    }

    public Boolean saveConfig(Configuration configuration) {
        repository.save(configuration);
        return true;
    }
}
