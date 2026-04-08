package com.example.sonardemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class SonarPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(SonarPocApplication.class, args);
	

	}

	@PostConstruct
    public void onStartup() {
        log.info("Hello, SonarQube! - Application deployed to ECS!");
    }

}
