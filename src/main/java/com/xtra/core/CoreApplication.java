package com.xtra.core;

import com.xtra.core.repository.ConnectionRepository;
import com.xtra.core.repository.ProcessRepository;
import com.xtra.core.repository.ProgressInfoRepository;
import com.xtra.core.repository.StreamInfoRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@EnableScheduling
@SpringBootApplication
public class CoreApplication {
    final ProcessRepository processRepository;
    final ProgressInfoRepository progressInfoRepository;
    final StreamInfoRepository streamInfoRepository;
    final ConnectionRepository connectionRepository;

    public CoreApplication(ProcessRepository processRepository, ProgressInfoRepository progressInfoRepository, StreamInfoRepository streamInfoRepository, ConnectionRepository connectionRepository) {
        this.processRepository = processRepository;
        this.streamInfoRepository = streamInfoRepository;
        this.progressInfoRepository = progressInfoRepository;
        this.connectionRepository = connectionRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }

    @PostConstruct
    private void init() {
        processRepository.deleteAll();
        progressInfoRepository.deleteAll();
        streamInfoRepository.deleteAll();
        connectionRepository.deleteAll();
    }
}
