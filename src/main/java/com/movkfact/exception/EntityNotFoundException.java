package com.movkfact.exception;

/**
 * Custom exception for resource not found scenarios.
 * 
 * Thrown by repository finders when entity doesn't exist (or is soft-deleted).
 * Caught by GlobalExceptionHandler and mapped to 404 Not Found response.
 */
public class EntityNotFoundException extends RuntimeException {
    
    /**
     * Constructor with message
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
