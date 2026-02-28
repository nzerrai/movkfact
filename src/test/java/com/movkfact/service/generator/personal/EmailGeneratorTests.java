package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour EmailGenerator.
 */
class EmailGeneratorTests {

    @Test
    void testEmailGeneratorCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO("email", ColumnType.EMAIL);
        EmailGenerator generator = new EmailGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testGenerateSingleEmail() {
        ColumnConfigDTO config = new ColumnConfigDTO("email", ColumnType.EMAIL);
        EmailGenerator generator = new EmailGenerator(config);
        
        Object result = generator.generate();
        assertNotNull(result);
        assertInstanceOf(String.class, result);
        String email = (String) result;
        assertTrue(email.contains("@"), "Email should contain @");
        assertTrue(email.contains("."), "Email should contain .");
    }

    @Test
    void testEmailFormat() {
        ColumnConfigDTO config = new ColumnConfigDTO("email", ColumnType.EMAIL);
        EmailGenerator generator = new EmailGenerator(config);
        
        for (int i = 0; i < 50; i++) {
            String email = (String) generator.generate();
            // Should match pattern: alphanumeric@domain.ext
            assertTrue(email.matches("^[a-z0-9]+@[a-z0-9.-]+$"), "Email should match pattern: " + email);
        }
    }

    @Test
    void testGenerateMultipleEmails() {
        ColumnConfigDTO config = new ColumnConfigDTO("email", ColumnType.EMAIL);
        EmailGenerator generator = new EmailGenerator(config);
        
        java.util.List<Object> results = generator.generateBatch(100);
        assertEquals(100, results.size());
        results.forEach(r -> assertInstanceOf(String.class, r));
    }

    @Test
    void testEmailVariety() {
        ColumnConfigDTO config = new ColumnConfigDTO("email", ColumnType.EMAIL);
        EmailGenerator generator = new EmailGenerator(config);
        
        java.util.Set<String> uniqueEmails = new java.util.HashSet<>();
        for (int i = 0; i < 100; i++) {
            uniqueEmails.add((String) generator.generate());
        }
        
        // Should generate many unique emails (due to random username)
        assertTrue(uniqueEmails.size() > 50, "Should generate variety of unique emails");
    }
}
