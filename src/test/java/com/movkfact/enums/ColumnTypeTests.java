package com.movkfact.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le ColumnType enum.
 */
class ColumnTypeTests {

    @Test
    void testPersonalColumnTypesExist() {
        assertNotNull(ColumnType.FIRST_NAME);
        assertNotNull(ColumnType.LAST_NAME);
        assertNotNull(ColumnType.EMAIL);
        assertNotNull(ColumnType.PHONE);
        assertNotNull(ColumnType.GENDER);
        assertNotNull(ColumnType.ADDRESS);
    }

    @Test
    void testFinancialColumnTypesExist() {
        assertNotNull(ColumnType.AMOUNT);
        assertNotNull(ColumnType.CURRENCY);
        assertNotNull(ColumnType.ACCOUNT_NUMBER);
    }

    @Test
    void testTemporalColumnTypesExist() {
        assertNotNull(ColumnType.DATE);
        assertNotNull(ColumnType.TIME);
        assertNotNull(ColumnType.TIMEZONE);
        assertNotNull(ColumnType.BIRTH_DATE);
    }

    @Test
    void testColumnTypeDataTypes() {
        assertEquals("personal", ColumnType.FIRST_NAME.getDataType());
        assertEquals("financial", ColumnType.AMOUNT.getDataType());
        assertEquals("temporal", ColumnType.DATE.getDataType());
    }

    @Test
    void testColumnTypeDescriptions() {
        assertNotNull(ColumnType.FIRST_NAME.getDescription());
        assertNotNull(ColumnType.EMAIL.getDescription());
        assertNotNull(ColumnType.BIRTH_DATE.getDescription());
    }

    @Test
    void testColumnTypeEnumCount() {
        assertEquals(13, ColumnType.values().length);
    }
}
