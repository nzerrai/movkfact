package com.movkfact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application entry point for movkfact backend.
 * 
 * This class initializes the Spring application context and starts the embedded
 * Tomcat server on localhost:8080 by default.
 */
@SpringBootApplication
public class MoveFactApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoveFactApplication.class, args);
    }
}
