package com.example.sonardemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class SonarPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(SonarPocApplication.class, args);
		Logger logger = LoggerFactory.getLogger(SonarPocApplication.class);
		logger.info("Hello, SonarQube!");

	}

}
