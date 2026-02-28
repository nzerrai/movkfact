package com.movkfact.service.detection.temporal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for TimezoneValidator - validates timezone identifier patterns
 */
@SpringBootTest
public class TimezoneValidatorTests {
    
    @Autowired
    private TimezoneValidator validator;
    
    @Test
    public void validator_detects_iana_timezone_codes() {
        List<String> timezones = Arrays.asList(
                "Europe/Paris",
                "America/New_York",
                "Asia/Tokyo",
                "UTC",
                "GMT"
        );
        double confidence = validator.validate(timezones);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
    
    @Test
    public void validator_detects_short_abbreviations() {
        List<String> timezones = Arrays.asList(
                "EST",
                "PST",
                "UTC",
                "GMT",
                "IST"
        );
        double confidence = validator.validate(timezones);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_utc_offset_formats() {
        List<String> timezones = Arrays.asList(
                "UTC+1",
                "UTC-5",
                "+05:30",
                "-08:00",
                "GMT+0"
        );
        double confidence = validator.validate(timezones);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_mixed_timezone_formats() {
        List<String> timezones = Arrays.asList(
                "Europe/Paris",
                "EST",
                "UTC+1",
                "GMT",
                "+02:00"
        );
        double confidence = validator.validate(timezones);
        assertThat(confidence).isGreaterThanOrEqualTo(75.0);
    }
    
    @Test
    public void validator_detects_additional_iana_zones() {
        List<String> timezones = Arrays.asList(
                "Australia/Sydney",
                "Asia/Singapore",
                "Europe/London",
                "America/Los_Angeles",
                "Africa/Cairo"
        );
        double confidence = validator.validate(timezones);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
    
    @Test
    public void validator_rejects_non_timezone_data() {
        List<String> nonTimezones = Arrays.asList(
                "John Smith",
                "john@example.com",
                "2025-01-15",
                "10:30:00",
                "USD"
        );
        double confidence = validator.validate(nonTimezones);
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
                "Europe/Paris",
                null,
                "EST",
                "",
                "UTC+1"
        );
        double confidence = validator.validate(withNulls);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
}
