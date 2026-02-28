package com.movkfact.service.generator.temporal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour les générateurs temporels (Date, Time, Timezone, BirthDate).
 */
class TemporalGeneratorTests {

    @Test
    void testDateGeneratorCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO("date", ColumnType.DATE);
        DateGenerator generator = new DateGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testGenerateSingleDate() {
        ColumnConfigDTO config = new ColumnConfigDTO("date", ColumnType.DATE);
        DateGenerator generator = new DateGenerator(config);
        
        Object result = generator.generate();
        assertNotNull(result);
        assertInstanceOf(String.class, result);
        String dateStr = (String) result;
        LocalDate date = LocalDate.parse(dateStr);
        assertTrue(date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now()));
    }

    @Test
    void testTimeGeneratorCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO("time", ColumnType.TIME);
        TimeGenerator generator = new TimeGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testGenerateSingleTime() {
        ColumnConfigDTO config = new ColumnConfigDTO("time", ColumnType.TIME);
        TimeGenerator generator = new TimeGenerator(config);
        
        Object result = generator.generate();
        assertNotNull(result);
        assertInstanceOf(String.class, result);
        String timeStr = (String) result;
        assertTrue(timeStr.matches("^\\d{2}:\\d{2}:\\d{2}$"));
    }

    @Test
    void testTimezoneGeneratorCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO("timezone", ColumnType.TIMEZONE);
        TimezoneGenerator generator = new TimezoneGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testGenerateSingleTimezone() {
        ColumnConfigDTO config = new ColumnConfigDTO("timezone", ColumnType.TIMEZONE);
        TimezoneGenerator generator = new TimezoneGenerator(config);
        
        Object result = generator.generate();
        assertNotNull(result);
        assertInstanceOf(String.class, result);
        String timezone = (String) result;
        assertFalse(timezone.isEmpty());
    }

    @Test
    void testBirthDateGeneratorAdultLiving() {
        ColumnConfigDTO config = new ColumnConfigDTO("birth_date", ColumnType.BIRTH_DATE);
        config.setAdditionalConfig("{\"ageCategory\": \"ADULT_LIVING\"}");
        BirthDateGenerator generator = new BirthDateGenerator(config);
        
        for (int i = 0; i < 20; i++) {
            String dateStr = (String) generator.generate();
            LocalDate birthDate = LocalDate.parse(dateStr);
            LocalDate today = LocalDate.now();
            long ageYears = java.time.temporal.ChronoUnit.YEARS.between(birthDate, today);
            
            assertTrue(ageYears >= 18 && ageYears <= 99,
                "ADULT_LIVING age should be 18-99, got " + ageYears);
        }
    }

    @Test
    void testBirthDateGeneratorMinorLiving() {
        ColumnConfigDTO config = new ColumnConfigDTO("birth_date", ColumnType.BIRTH_DATE);
        config.setAdditionalConfig("{\"ageCategory\": \"MINOR_LIVING\"}");
        BirthDateGenerator generator = new BirthDateGenerator(config);
        
        for (int i = 0; i < 20; i++) {
            String dateStr = (String) generator.generate();
            LocalDate birthDate = LocalDate.parse(dateStr);
            LocalDate today = LocalDate.now();
            long ageYears = java.time.temporal.ChronoUnit.YEARS.between(birthDate, today);
            
            assertTrue(ageYears >= 0 && ageYears <= 17,
                "MINOR_LIVING age should be 0-17, got " + ageYears);
        }
    }

    @Test
    void testBirthDateGeneratorDeceased() {
        ColumnConfigDTO config = new ColumnConfigDTO("birth_date", ColumnType.BIRTH_DATE);
        config.setAdditionalConfig("{\"ageCategory\": \"DECEASED\"}");
        BirthDateGenerator generator = new BirthDateGenerator(config);
        
        for (int i = 0; i < 20; i++) {
            String dateStr = (String) generator.generate();
            LocalDate birthDate = LocalDate.parse(dateStr);
            LocalDate today = LocalDate.now();
            long ageYears = java.time.temporal.ChronoUnit.YEARS.between(birthDate, today);
            
            assertTrue(ageYears >= 50 && ageYears <= 150,
                "DECEASED age should be 50-150, got " + ageYears);
        }
    }
}
