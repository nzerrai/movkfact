package com.movkfact.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller for application monitoring.
 * 
 * Provides an endpoint to verify that the application is running and healthy.
 * This endpoint is accessible without authentication to support monitoring systems.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * Health check endpoint.
     * 
     * Returns the application status as JSON.
     * 
     * @return ResponseEntity with status "UP" and HTTP 200 OK
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
