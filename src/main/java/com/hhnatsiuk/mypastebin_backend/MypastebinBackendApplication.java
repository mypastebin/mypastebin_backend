package com.hhnatsiuk.mypastebin_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MypastebinBackendApplication {

    private static final Logger logger = LogManager.getLogger(MypastebinBackendApplication.class);

    public static void main(String[] args) {
        logger.info("Starting application...");
        SpringApplication.run(MypastebinBackendApplication.class, args);
        logger.info("Application started successfully.");
    }

}
