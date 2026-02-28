package com.movkfact.util;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT Utility class for token generation, validation, and claim extraction.
 * 
 * Provides methods to:
 * - Generate JWT tokens with custom subjects
 * - Validate JWT tokens and check expiration
 * - Extract subject (username/ID) from tokens
 * 
 * All tokens are signed using HS256 algorithm with the configured JWT secret key.
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

    private final Key jwtSecretKey;

    @Autowired
    public JwtUtil(@Qualifier("jwtSecretKey") Key jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
    }

    /**
     * Generates a JWT token for the given subject.
     * 
     * @param subject typically the username or user ID
     * @return a signed JWT token string
     */
    public String generateToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates the given JWT token.
     * 
     * Checks if the token is properly signed and not expired.
     * 
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise (null, malformed, expired, invalid signature)
     */
    public boolean validateToken(String token) {
        // Handle null or empty token
        if (token == null || token.isEmpty()) {
            logger.warn("Null or empty JWT token provided for validation");
            return false;
        }
        
        try {
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) jwtSecretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extracts the subject (typically username or user ID) from the given JWT token.
     * 
     * <strong>IMPORTANT:</strong> This method returns {@code null} if the token is invalid.
     * Callers MUST check for null before using the returned value.
     * 
     * Token must be valid before calling this method. Invalid tokens (expired, malformed,
     * with invalid signature) will return {@code null} instead of throwing exceptions.
     * 
     * <code>
     * Example usage:
     * String subject = jwtUtil.extractSubject(token);
     * if (subject != null) {
     *     // Process subject
     *     user = getUserById(subject);
     * } else {
     *     // Handle invalid token
     *     throw new InvalidTokenException();
     * }
     * </code>
     * 
     * @param token the JWT token to extract subject from
     * @return the subject string from the token, or {@code null} if token is invalid
     */
    public String extractSubject(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) jwtSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("Failed to extract subject - invalid signature: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("Failed to extract subject from token: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error extracting subject: {}", e.getMessage());
        }
        return null;
    }
}
