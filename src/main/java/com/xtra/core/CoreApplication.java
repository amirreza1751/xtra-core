package com.xtra.core;

import com.xtra.core.model.StreamInfo;
import com.xtra.core.repository.LineActivityRepository;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.repository.ProgressInfoRepository;
import com.xtra.core.repository.StreamInfoRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;

@EnableScheduling
@SpringBootApplication
public class CoreApplication {
    final ProcessRepository processRepository;
    final ProgressInfoRepository progressInfoRepository;
    final StreamInfoRepository streamInfoRepository;
    final LineActivityRepository lineActivityRepository;

    public CoreApplication(ProcessRepository processRepository, ProgressInfoRepository progressInfoRepository, StreamInfoRepository streamInfoRepository, LineActivityRepository lineActivityRepository) {
        this.processRepository = processRepository;
        this.streamInfoRepository = streamInfoRepository;
        this.progressInfoRepository = progressInfoRepository;
        this.lineActivityRepository = lineActivityRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }

    @PostConstruct
    private void init() {
        processRepository.deleteAll();
        progressInfoRepository.deleteAll();
        streamInfoRepository.deleteAll();
        lineActivityRepository.deleteAll();
    }
}
