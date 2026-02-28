package com.movkfact.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Wrapper for error API responses.
 * 
 * Format: { "error": "error message", "timestamp": "ISO8601Z", "status": HTTP_CODE, "path": "/api/..." }
 * Used for all error responses (400, 404, 409, 500, etc.)
 */
public class ApiErrorResponse {
    
    private String error;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime timestamp;
    
    private Integer status;
    
    private String path;
    
    /**
     * Constructor with all fields
     */
    public ApiErrorResponse(String error, LocalDateTime timestamp, Integer status, String path) {
        this.error = error;
        this.timestamp = timestamp;
        this.status = status;
        this.path = path;
    }
    
    /**
     * Constructor without timestamp (auto-populated)
     */
    public ApiErrorResponse(String error, Integer status, String path) {
        this.error = error;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now(ZoneId.of("UTC"));
    }
    
    /**
     * No-arg constructor for JSON deserialization
     */
    public ApiErrorResponse() {
    }
    
    /**
     * Factory method for clean instantiation with auto timestamp
     */
    public static ApiErrorResponse of(String error, int status, String path) {
        return new ApiErrorResponse(error, status, path);
    }
    
    // Getters and Setters
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
}
