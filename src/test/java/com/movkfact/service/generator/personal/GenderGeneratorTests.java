package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour GenderGenerator.
 */
class GenderGeneratorTests {

    @Test
    void testGenderGeneratorCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO("gender", ColumnType.GENDER);
        GenderGenerator generator = new GenderGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testGenerateSingleGender() {
        ColumnConfigDTO config = new ColumnConfigDTO("gender", ColumnType.GENDER);
        GenderGenerator generator = new GenderGenerator(config);
        
        Object result = generator.generate();
        assertNotNull(result);
        assertInstanceOf(String.class, result);
        String gender = (String) result;
        assertTrue(gender.equals("M") || gender.equals("F") || gender.equals("X"));
    }

    @Test
    void testGenderValuesCorrect() {
        ColumnConfigDTO config = new ColumnConfigDTO("gender", ColumnType.GENDER);
        GenderGenerator generator = new GenderGenerator(config);
        
        java.util.Set<String> genders = new java.util.HashSet<>();
        for (int i = 0; i < 100; i++) {
            genders.add((String) generator.generate());
        }
        
        // Should eventually generate all three options
        assertTrue(genders.contains("M"));
        assertTrue(genders.contains("F"));
        assertTrue(genders.contains("X"));
    }

    @Test
    void testGenerateMultipleGenders() {
        ColumnConfigDTO config = new ColumnConfigDTO("gender", ColumnType.GENDER);
        GenderGenerator generator = new GenderGenerator(config);
        
        java.util.List<Object> results = generator.generateBatch(100);
        assertEquals(100, results.size());
        results.forEach(r -> assertInstanceOf(String.class, r));
    }
}
