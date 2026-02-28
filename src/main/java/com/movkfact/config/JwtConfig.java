package com.movkfact.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * JWT Configuration for authentication and authorization.
 * 
 * Provides a bean for the JWT secret key used to sign and verify JWT tokens.
 * The secret is read from the JWT_SECRET environment variable, with a default
 * value for development.
 */
@Configuration
public class JwtConfig {

    /**
     * Provides the JWT secret key as a Spring Bean.
     * 
     * Validates that the JWT secret is strong enough (minimum 32 characters for HS256).
     * Throws IllegalArgumentException if secret is too weak.
     * 
     * @param jwtSecret the JWT secret from environment or application properties
     * @return Key instance for JWT operations
     * @throws IllegalArgumentException if secret length is less than 32 characters
     */
    @Bean("jwtSecretKey")
    public Key jwtSecretKey(
            @Value("${jwt.secret:movkfact-dev-secret-key-change-in-production-immediately}") String jwtSecret) {
        
        // Validate JWT secret strength for HS256 algorithm
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            throw new IllegalArgumentException(
                "JWT_SECRET environment variable is not set. Please set JWT_SECRET to a string of at least 32 characters."
            );
        }
        
        if (jwtSecret.length() < 32) {
            throw new IllegalArgumentException(
                String.format(
                    "JWT_SECRET must be at least 32 characters for HS256 security. Current length: %d. " +
                    "Please set a stronger JWT_SECRET in environment variables.",
                    jwtSecret.length()
                )
            );
        }
        
        return new SecretKeySpec(
                jwtSecret.getBytes(),
                0,
                jwtSecret.getBytes().length,
                "HmacSHA256"
        );
    }

}
