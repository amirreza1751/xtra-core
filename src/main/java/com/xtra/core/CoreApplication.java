package com.xtra.core;

import com.xtra.core.repository.ProcessRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@EnableScheduling
@SpringBootApplication
public class CoreApplication {
	final ProcessRepository processRepository;

	public CoreApplication(ProcessRepository processRepository) {
		this.processRepository = processRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(CoreApplication.class, args);
	}

	@PostConstruct
	private void init() {
		processRepository.deleteAll();
	}
}
