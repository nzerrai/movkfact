package com.movkfact.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le AgeCategoryType enum.
 */
class AgeCategoryTypeTests {

    @Test
    void testAgeCategoryTypesExist() {
        assertNotNull(AgeCategoryType.ADULT_LIVING);
        assertNotNull(AgeCategoryType.MINOR_LIVING);
        assertNotNull(AgeCategoryType.DECEASED);
    }

    @Test
    void testAdultLivingAgeRange() {
        assertEquals(18, AgeCategoryType.ADULT_LIVING.getMinAge());
        assertEquals(99, AgeCategoryType.ADULT_LIVING.getMaxAge());
    }

    @Test
    void testMinorLivingAgeRange() {
        assertEquals(0, AgeCategoryType.MINOR_LIVING.getMinAge());
        assertEquals(17, AgeCategoryType.MINOR_LIVING.getMaxAge());
    }

    @Test
    void testDeceasedAgeRange() {
        assertEquals(50, AgeCategoryType.DECEASED.getMinAge());
        assertEquals(150, AgeCategoryType.DECEASED.getMaxAge());
    }

    @Test
    void testAgeCategoryDescriptions() {
        assertNotNull(AgeCategoryType.ADULT_LIVING.getDescription());
        assertNotNull(AgeCategoryType.MINOR_LIVING.getDescription());
        assertNotNull(AgeCategoryType.DECEASED.getDescription());
    }

    @Test
    void testAgeCategoryEnumCount() {
        assertEquals(3, AgeCategoryType.values().length);
    }
}
