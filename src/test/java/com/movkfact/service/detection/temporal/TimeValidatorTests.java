package com.movkfact.service.detection.temporal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for TimeValidator - validates time value patterns
 */
@SpringBootTest
public class TimeValidatorTests {
    
    @Autowired
    private TimeValidator validator;
    
    @Test
    public void validator_detects_full_format_times() {
        List<String> times = Arrays.asList(
                "10:30:00",
                "14:45:30",
                "08:15:00",
                "23:59:59",
                "00:00:00"
        );
        double confidence = validator.validate(times);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
    
    @Test
    public void validator_detects_short_format_times() {
        List<String> times = Arrays.asList(
                "10:30",
                "14:45",
                "08:15",
                "23:59",
                "00:00"
        );
        double confidence = validator.validate(times);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_times_with_milliseconds() {
        List<String> times = Arrays.asList(
                "10:30:00.123",
                "14:45:30.456",
                "08:15:00.789",
                "23:59:59.999",
                "00:00:00.000"
        );
        double confidence = validator.validate(times);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_mixed_time_formats() {
        List<String> times = Arrays.asList(
                "10:30:00",
                "14:45",
                "08:15:00.123",
                "23:59:59",
                "00:00"
        );
        double confidence = validator.validate(times);
        assertThat(confidence).isGreaterThanOrEqualTo(75.0);
    }
    
    @Test
    public void validator_rejects_non_time_data() {
        List<String> nonTimes = Arrays.asList(
                "John Smith",
                "john@example.com",
                "2025-01-15",
                "Europe/Paris",
                "USD"
        );
        double confidence = validator.validate(nonTimes);
        assertThat(confidence).isLessThan(30.0);
    }
    
    @Test
    public void validator_rejects_invalid_time_values() {
        List<String> invalidTimes = Arrays.asList(
                "25:00:00",  // Invalid hour
                "12:60:00",  // Invalid minute
                "12:30:60",  // Invalid second
                "99:99:99",
                "invalid_time"
        );
        double confidence = validator.validate(invalidTimes);
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
                "10:30:00",
                null,
                "14:45:30",
                "",
                "08:15:00"
        );
        double confidence = validator.validate(withNulls);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
}
