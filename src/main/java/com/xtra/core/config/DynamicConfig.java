package com.xtra.core.config;

import com.xtra.core.repository.ConfigurationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy
public class DynamicConfig {
    private final ConfigurationRepository repository;

    public DynamicConfig(ConfigurationRepository configurationRepository) {
        this.repository = configurationRepository;
    }

    @Bean
    public String getToken() {
        var token = repository.findById("token");
        return token.map(com.xtra.core.model.Configuration::getValue).orElse(null);
    }
}
