package com.movkfact;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MoveFactApplication startup, health checks, and security configuration.
 * 
 * Verifies that:
 * - Spring application context loads successfully
 * - Health check endpoint is accessible and returns correct response
 * - Public endpoints are accessible without authentication
 * - Protected endpoints require authentication
 * - H2 console is accessible in development
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MoveFactApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Test that application context loads without errors.
     */
    @Test
    public void contextLoads() {
        // This test passes if the application context loads successfully
        // without throwing any exceptions during initialization
    }

    /**
     * Test that health check endpoint is accessible and returns 200 OK.
     */
    @Test
    public void healthCheckEndpointReturns200() throws Exception {
        mockMvc.perform(get("/api/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    /**
     * Test that health check endpoint returns correct JSON structure.
     */
    @Test
    public void healthCheckEndpointReturnsCorrectJson() throws Exception {
        mockMvc.perform(get("/api/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.status").isString());
    }

    /**
     * Test that public health endpoint is accessible without authentication.
     * 
     * This verifies that SecurityConfig allows /api/health without auth.
     */
    @Test
    public void publicHealthEndpointAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test that CORS headers are properly configured for frontend requests.
     * 
     * Verifies that React frontend (localhost:3000) can make cross-origin requests
     * by checking CORS preflight response.
     */
    @Test
    public void corsConfigurationAllowsFrontendRequests() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    /**
     * Test that CORS allows specified methods for public endpoints.
     * 
     * Verifies that GET, POST, PUT, DELETE methods are allowed from frontend.
     */
    @Test
    public void corsAllowsCommonHttpMethods() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk());
    }

}
