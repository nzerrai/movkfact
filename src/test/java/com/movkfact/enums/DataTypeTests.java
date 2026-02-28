package com.movkfact.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le DataType enum.
 */
class DataTypeTests {

    @Test
    void testDataTypeEnumValuesExist() {
        assertNotNull(DataType.PERSONAL);
        assertNotNull(DataType.FINANCIAL);
        assertNotNull(DataType.TEMPORAL);
    }

    @Test
    void testDataTypeEnumDescriptions() {
        assertEquals("Personal Data", DataType.PERSONAL.getDescription());
        assertEquals("Financial Data", DataType.FINANCIAL.getDescription());
        assertEquals("Temporal Data", DataType.TEMPORAL.getDescription());
    }

    @Test
    void testDataTypeEnumCount() {
        assertEquals(3, DataType.values().length);
    }
}
