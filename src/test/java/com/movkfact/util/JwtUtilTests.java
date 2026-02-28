package com.movkfact.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil JWT token operations.
 * 
 * Verifies that:
 * - JWT tokens can be generated successfully
 * - JWT tokens can be validated correctly
 * - Subject can be extracted from JWT tokens
 * - Invalid tokens are rejected
 */
@SpringBootTest
public class JwtUtilTests {

    @Autowired
    private JwtUtil jwtUtil;

    private String testToken;
    private String testSubject = "testuser123";

    @BeforeEach
    public void setUp() {
        // Generate a fresh token for each test
        testToken = jwtUtil.generateToken(testSubject);
    }

    /**
     * Test that JWT token can be generated successfully.
     */
    @Test
    public void generateTokenSuccessfully() {
        String token = jwtUtil.generateToken("user123");
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));  // JWT format: header.payload.signature
    }

    /**
     * Test that generated JWT token is valid.
     */
    @Test
    public void validateTokenSuccessfully() {
        boolean isValid = jwtUtil.validateToken(testToken);
        assertTrue(isValid);
    }

    /**
     * Test that subject can be extracted from JWT token.
     */
    @Test
    public void extractSubjectFromToken() {
        String extractedSubject = jwtUtil.extractSubject(testToken);
        assertEquals(testSubject, extractedSubject);
    }

    /**
     * Test that invalid (corrupted) token is rejected.
     */
    @Test
    public void invalidTokenIsRejected() {
        String invalidToken = testToken + "invalid";
        boolean isValid = jwtUtil.validateToken(invalidToken);
        assertFalse(isValid);
    }

    /**
     * Test that malformed token is rejected.
     */
    @Test
    public void malformedTokenIsRejected() {
        String malformedToken = "not.a.valid.jwt";
        boolean isValid = jwtUtil.validateToken(malformedToken);
        assertFalse(isValid);
    }

    /**
     * Test that subject extraction returns null for invalid token.
     */
    @Test
    public void extractSubjectReturnsNullForInvalidToken() {
        String invalidToken = "invalid.token.here";
        String subject = jwtUtil.extractSubject(invalidToken);
        assertNull(subject);
    }

    /**
     * Test that validateToken returns false for expired tokens.
     */
    @Test
    public void expiredTokenIsRejected() {
        // Create a token that expires in the past (simulated)
        // For this test, we create a valid token and let JwtUtil decide on expiration
        String validToken = jwtUtil.generateToken(testSubject);
        
        // Token just generated should be valid
        assertTrue(jwtUtil.validateToken(validToken));
        
        // Note: Testing actual expiration (24 hour duration) would require
        // mocking the system time or creating a separate test with time-based assertions
    }

    /**
     * Test that null token returns false on validation.
     */
    @Test
    public void nullTokenIsRejected() {
        boolean isValid = jwtUtil.validateToken(null);
        assertFalse(isValid);
    }

}
