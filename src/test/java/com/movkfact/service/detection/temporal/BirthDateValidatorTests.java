package com.movkfact.service.detection.temporal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for BirthDateValidator - validates historical birth date patterns
 */
@SpringBootTest
public class BirthDateValidatorTests {
    
    @Autowired
    private BirthDateValidator validator;
    
    @Test
    public void validator_detects_iso_birth_dates() {
        List<String> dates = Arrays.asList(
                "1980-05-15",
                "1995-12-31",
                "1970-01-01",
                "1985-06-15",
                "1990-03-20"
        );
        double confidence = validator.validate(dates);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_european_format_birth_dates() {
        List<String> dates = Arrays.asList(
                "15/05/1980",
                "31/12/1995",
                "01/01/1970",
                "15/06/1985",
                "20/03/1990"
        );
        double confidence = validator.validate(dates);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_american_format_birth_dates() {
        List<String> dates = Arrays.asList(
                "05-15-1980",
                "12-31-1995",
                "01-01-1970",
                "06-15-1985",
                "03-20-1990"
        );
        double confidence = validator.validate(dates);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_mixed_date_formats() {
        List<String> dates = Arrays.asList(
                "1980-05-15",
                "15/05/1980",
                "05-15-1980",
                "1985-06-15",
                "15.06.1985"
        );
        double confidence = validator.validate(dates);
        assertThat(confidence).isGreaterThanOrEqualTo(75.0);
    }
    
    @Test
    public void validator_rejects_future_dates() {
        List<String> futureDates = Arrays.asList(
                "2050-05-15",
                "2099-12-31",
                "2100-01-01",
                "2075-06-15",
                "2080-03-20"
        );
        double confidence = validator.validate(futureDates);
        assertThat(confidence).isLessThan(75.0);
    }
    
    @Test
    public void validator_rejects_non_date_data() {
        List<String> nonDates = Arrays.asList(
                "John Smith",
                "john@example.com",
                "10:30:00",
                "Europe/Paris",
                "USD"
        );
        double confidence = validator.validate(nonDates);
        assertThat(confidence).isLessThan(30.0);
    }
    
    @Test
    public void validator_handles_empty_list() {
        List<String> empty = Arrays.asList();
        double confidence = validator.validate(empty);
        assertThat(confidence).isEqualTo(0.0);
    }
    
    @Test
    public void validator_handles_null_and_empty_values() {
        List<String> withNulls = Arrays.asList(
                "1980-05-15",
                null,
                "1995-12-31",
                "",
                "1985-06-15"
        );
        double confidence = validator.validate(withNulls);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
}
