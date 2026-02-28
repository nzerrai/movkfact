package com.movkfact.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Generic wrapper for successful API responses.
 * 
 * Format: { "data": T, "message": "success message" }
 * Used across all successful responses (200, 201, etc.)
 * 
 * Type parameter T can be: single entity, list of entities, or null (for 204 responses)
 * 
 * @param <T> the type of data being wrapped by this response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private T data;
    private String message;
    
    /**
     * Constructor with data and message
     */
    public ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }
    
    /**
     * No-arg constructor for JSON deserialization
     */
    public ApiResponse() {
    }
    
    /**
     * Factory method for clean instantiation
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message);
    }
    
    // Getters and Setters
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
