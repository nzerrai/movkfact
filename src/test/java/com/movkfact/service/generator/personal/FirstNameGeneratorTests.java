package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour FirstNameGenerator.
 */
class FirstNameGeneratorTests {

    @Test
    void testFirstNameGeneratorCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME);
        FirstNameGenerator generator = new FirstNameGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testGenerateSingleFirstName() {
        ColumnConfigDTO config = new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME);
        FirstNameGenerator generator = new FirstNameGenerator(config);
        
        Object result = generator.generate();
        assertNotNull(result);
        assertInstanceOf(String.class, result);
        assertTrue(((String) result).length() > 0);
    }

    @Test
    void testGenerateMultipleFirstNames() {
        ColumnConfigDTO config = new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME);
        FirstNameGenerator generator = new FirstNameGenerator(config);
        
        java.util.List<Object> results = generator.generateBatch(10);
        assertEquals(10, results.size());
        results.forEach(r -> assertInstanceOf(String.class, r));
    }

    @Test
    void testGenerateVariety() {
        ColumnConfigDTO config = new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME);
        FirstNameGenerator generator = new FirstNameGenerator(config);
        
        java.util.Set<String> uniqueNames = new java.util.HashSet<>();
        for (int i = 0; i < 100; i++) {
            uniqueNames.add((String) generator.generate());
        }
        
        // Should generate at least 5 different names out of 100
        assertTrue(uniqueNames.size() >= 5, "Generator should produce variety");
    }
}
