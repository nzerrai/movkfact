package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour PhoneGenerator.
 */
class PhoneGeneratorTests {

    @Test
    void testPhoneGeneratorCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO("phone", ColumnType.PHONE);
        PhoneGenerator generator = new PhoneGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testGenerateSinglePhone() {
        ColumnConfigDTO config = new ColumnConfigDTO("phone", ColumnType.PHONE);
        PhoneGenerator generator = new PhoneGenerator(config);
        
        Object result = generator.generate();
        assertNotNull(result);
        assertInstanceOf(String.class, result);
        String phone = (String) result;
        assertTrue(phone.startsWith("+33"));
    }

    @Test
    void testPhoneFormatCorrect() {
        ColumnConfigDTO config = new ColumnConfigDTO("phone", ColumnType.PHONE);
        PhoneGenerator generator = new PhoneGenerator(config);
        
        for (int i = 0; i < 50; i++) {
            String phone = (String) generator.generate();
            // Should match pattern: +33 X XX XX XX XX
            assertTrue(phone.matches("^\\+33 [0-9] \\d{2} \\d{2} \\d{2} \\d{2}$"), 
                "Phone should match French format: " + phone);
        }
    }

    @Test
    void testPhoneStartsWithPlus33() {
        ColumnConfigDTO config = new ColumnConfigDTO("phone", ColumnType.PHONE);
        PhoneGenerator generator = new PhoneGenerator(config);
        
        for (int i = 0; i < 100; i++) {
            String phone = (String) generator.generate();
            assertTrue(phone.startsWith("+33 "));
        }
    }

    @Test
    void testGenerateMultiplePhones() {
        ColumnConfigDTO config = new ColumnConfigDTO("phone", ColumnType.PHONE);
        PhoneGenerator generator = new PhoneGenerator(config);
        
        java.util.List<Object> results = generator.generateBatch(50);
        assertEquals(50, results.size());
        results.forEach(r -> assertInstanceOf(String.class, r));
    }
}
