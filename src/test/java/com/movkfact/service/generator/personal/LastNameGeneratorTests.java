package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour LastNameGenerator.
 */
class LastNameGeneratorTests {

    @Test
    void testLastNameGeneratorCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO("lastname", ColumnType.LAST_NAME);
        LastNameGenerator generator = new LastNameGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testGenerateSingleLastName() {
        ColumnConfigDTO config = new ColumnConfigDTO("lastname", ColumnType.LAST_NAME);
        LastNameGenerator generator = new LastNameGenerator(config);
        
        Object result = generator.generate();
        assertNotNull(result);
        assertInstanceOf(String.class, result);
        assertTrue(((String) result).length() > 0);
    }

    @Test
    void testGenerateMultipleLastNames() {
        ColumnConfigDTO config = new ColumnConfigDTO("lastname", ColumnType.LAST_NAME);
        LastNameGenerator generator = new LastNameGenerator(config);
        
        java.util.List<Object> results = generator.generateBatch(50);
        assertEquals(50, results.size());
        results.forEach(r -> assertInstanceOf(String.class, r));
    }

    @Test
    void testLastNameNotEmpty() {
        ColumnConfigDTO config = new ColumnConfigDTO("lastname", ColumnType.LAST_NAME);
        LastNameGenerator generator = new LastNameGenerator(config);
        
        for (int i = 0; i < 100; i++) {
            String lastName = (String) generator.generate();
            assertFalse(lastName.isEmpty());
            assertFalse(lastName.contains(" "));  // Single word
        }
    }
}
