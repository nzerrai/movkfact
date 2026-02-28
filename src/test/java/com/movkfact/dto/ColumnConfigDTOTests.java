package com.movkfact.dto;

import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ColumnConfigDTO.
 */
class ColumnConfigDTOTests {

    @Test
    void testColumnConfigDTOCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO();
        assertNotNull(config);
    }

    @Test
    void testColumnConfigDTOWithConstructor() {
        ColumnConfigDTO config = new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME);
        
        assertEquals("firstname", config.getName());
        assertEquals(ColumnType.FIRST_NAME, config.getColumnType());
        assertEquals(false, config.getNullable());
    }

    @Test
    void testColumnConfigDTOSettersGetters() {
        ColumnConfigDTO config = new ColumnConfigDTO();
        config.setName("email");
        config.setColumnType(ColumnType.EMAIL);
        config.setFormat("email@example.com");
        config.setNullable(true);
        
        assertEquals("email", config.getName());
        assertEquals(ColumnType.EMAIL, config.getColumnType());
        assertEquals("email@example.com", config.getFormat());
        assertEquals(true, config.getNullable());
    }

    @Test
    void testColumnConfigDTOWithAdditionalConfig() {
        ColumnConfigDTO config = new ColumnConfigDTO("birthdate", ColumnType.BIRTH_DATE);
        config.setAdditionalConfig("{\"ageCategory\": \"ADULT_LIVING\"}");
        
        assertEquals("{\"ageCategory\": \"ADULT_LIVING\"}", config.getAdditionalConfig());
    }
}
