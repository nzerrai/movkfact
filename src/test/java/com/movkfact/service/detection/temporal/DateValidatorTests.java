package com.movkfact.service.detection.temporal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for DateValidator - validates general date patterns
 */
@SpringBootTest
public class DateValidatorTests {
    
    @Autowired
    private DateValidator validator;
    
    @Test
    public void validator_detects_iso_dates() {
        List<String> dates = Arrays.asList(
                "2025-01-15",
                "2024-12-25",
                "2023-06-10",
                "2022-03-01",
                "2021-09-30"
        );
        double confidence = validator.validate(dates);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
    
    @Test
    public void validator_detects_iso_timestamps() {
        List<String> timestamps = Arrays.asList(
                "2025-01-15T10:30:00",
                "2024-12-25T14:45:30",
                "2023-06-10T08:15:00",
                "2022-03-01T23:59:59",
                "2021-09-30T00:00:00"
        );
        double confidence = validator.validate(timestamps);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
    
    @Test
    public void validator_detects_datetime_with_spaces() {
        List<String> datetimes = Arrays.asList(
                "2025-01-15 10:30:00",
                "2024-12-25 14:45:30",
                "2023-06-10 08:15:00",
                "2022-03-01 23:59:59",
                "2021-09-30 00:00:00"
        );
        double confidence = validator.validate(datetimes);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_european_format_dates() {
        List<String> dates = Arrays.asList(
                "15/01/2025",
                "25/12/2024",
                "10/06/2023",
                "01/03/2022",
                "30/09/2021"
        );
        double confidence = validator.validate(dates);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_american_format_dates() {
        List<String> dates = Arrays.asList(
                "01-15-2025",
                "12-25-2024",
                "06-10-2023",
                "03-01-2022",
                "09-30-2021"
        );
        double confidence = validator.validate(dates);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
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
                "2025-01-15",
                null,
                "2024-12-25T10:30:00",
                "",
                "15/01/2023"
        );
        double confidence = validator.validate(withNulls);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
}
