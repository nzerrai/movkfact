package com.movkfact.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.movkfact.enums.ColumnType;
import com.movkfact.response.ApiErrorResponse;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for application-wide error handling.
 * 
 * Centralizes exception handling to return standardized error responses
 * to clients instead of raw exceptions and stack traces.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle JWT-related exceptions (malformed tokens, invalid signatures, etc.)
     * 
     * @param ex the JwtException
     * @return 401 Unauthorized response with error details
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(JwtException ex) {
        logger.warn("JWT exception: {}", ex.getMessage());
        
        Map<String, Object> response = buildErrorResponse(
            "INVALID_TOKEN",
            "Invalid or expired JWT token",
            HttpStatus.UNAUTHORIZED.value()
        );
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle JWT signature validation failures
     * 
     * @param ex the SignatureException
     * @return 401 Unauthorized response
     */
    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Map<String, Object>> handleSignatureException(SignatureException ex) {
        logger.warn("JWT signature validation failed: {}", ex.getMessage());
        
        Map<String, Object> response = buildErrorResponse(
            "INVALID_SIGNATURE",
            "JWT signature validation failed",
            HttpStatus.UNAUTHORIZED.value()
        );
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle access denied exceptions (insufficient permissions)
     * 
     * @param ex the AccessDeniedException
     * @return 403 Forbidden response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        
        Map<String, Object> response = buildErrorResponse(
            "ACCESS_DENIED",
            "You do not have permission to access this resource",
            HttpStatus.FORBIDDEN.value()
        );
        
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle illegal argument exceptions (validation failures)
     * 
     * @param ex the IllegalArgumentException
     * @return 400 Bad Request response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Invalid argument: {}", ex.getMessage());
        
        Map<String, Object> response = buildErrorResponse(
            "INVALID_ARGUMENT",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle validation errors from @Valid annotation on DTOs
     * Extracts first validation error and returns ApiErrorResponse with 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        logger.warn("Validation error: {}", ex.getMessage());
        
        // Extract first field error message
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");
        
        ApiErrorResponse errorResponse = ApiErrorResponse.of(
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle database constraint violations (unique constraint, foreign key, etc.)
     * Returns 409 Conflict for data integrity violations
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            WebRequest request) {
        
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        // Extract constraint violation details if available
        String errorMessage = ex.getMostSpecificCause() != null 
                ? ex.getMostSpecificCause().getMessage()
                : "Data constraint violation: " + ex.getMessage();
        
        // Simplify error message for client (check for unique constraint)
        if (errorMessage.contains("UNIQUE constraint failed") || errorMessage.contains("unique")) {
            errorMessage = "Resource with this name already exists";
        }
        
        ApiErrorResponse errorResponse = ApiErrorResponse.of(
                errorMessage,
                HttpStatus.CONFLICT.value(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle EntityNotFoundException (resource not found)
     * Returns 404 Not Found for missing resources
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex,
            WebRequest request) {
        
        logger.warn("Entity not found: {}", ex.getMessage());
        
        ApiErrorResponse errorResponse = ApiErrorResponse.of(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle ResponseStatusException (HTTP status exceptions)
     * Re-throws to let Spring handle it with the correct status code
     * 
     * @param ex the ResponseStatusException
     * @return ResponseEntity with the exception's status code
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        logger.debug("Response status exception: {} - {}", ex.getStatusCode(), ex.getReason());
        
        Map<String, Object> response = buildErrorResponse(
            "HTTP_ERROR",
            ex.getReason() != null ? ex.getReason() : "HTTP Status " + ex.getStatusCode(),
            ex.getStatusCode().value()
        );
        
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    /**
     * Handle unreadable HTTP message body — notamment quand Jackson reçoit un ColumnType inconnu.
     * Retourne 400 avec message explicite: "Type inconnu : 'XYZ'. Types valides : [...]"
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        String message = "Requête invalide : corps JSON illisible";
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ife) {
            if (ife.getTargetType() != null
                    && ife.getTargetType().isEnum()
                    && ColumnType.class.isAssignableFrom(ife.getTargetType())) {
                message = "Type inconnu : '" + ife.getValue()
                        + "'. Types valides : " + Arrays.toString(ColumnType.values());
            }
        }

        logger.warn("HTTP message not readable: {}", ex.getMessage());
        ApiErrorResponse errorResponse = ApiErrorResponse.of(
                message,
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions (catch-all)
     * 
     * @param ex the Exception
     * @return 500 Internal Server Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = buildErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred. Please contact support.",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Build standardized error response
     * 
     * @param errorCode the error code (enum-like identifier)
     * @param message the user-friendly error message
     * @param status the HTTP status code
     * @return map containing error details
     */
    private Map<String, Object> buildErrorResponse(String errorCode, String message, int status) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", errorCode);
        response.put("message", message);
        response.put("status", status);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
