package com.movkfact.service.detection.personal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for PhoneValidator - validates phone number formats (international)
 */
@SpringBootTest
public class PhoneValidatorTests {
    
    @Autowired
    private PhoneValidator validator;
    
    @Test
    public void validator_detects_us_phone_numbers() {
        List<String> usPhones = Arrays.asList(
                "202-555-0173",
                "(202) 555-0173",
                "202.555.0173",
                "2025550173",
                "+1-202-555-0173"
        );
        double confidence = validator.validate(usPhones);
        assertThat(confidence).isGreaterThanOrEqualTo(80.0);
    }
    
    @Test
    public void validator_detects_european_phone_numbers() {
        List<String> euPhones = Arrays.asList(
                "+33123456789",
                "01 23 45 67 89",
                "+44 20 7946 0958",
                "+49 30 12345678",
                "0033123456789"
        );
        double confidence = validator.validate(euPhones);
        assertThat(confidence).isGreaterThanOrEqualTo(75.0);
    }
    
    @Test
    public void validator_rejects_non_phone_data() {
        List<String> nonPhone = Arrays.asList(
                "John Smith",
                "john@example.com",
                "12345",
                "New York",
                "ABCDEF1234"
        );
        double confidence = validator.validate(nonPhone);
        assertThat(confidence).isLessThan(40.0);
    }
    
    @Test
    public void validator_handles_mixed_formats() {
        List<String> mixedPhones = Arrays.asList(
                "202-555-0173",
                "(202) 555-0173",
                "+1-202-555-0173",
                "invalid-phone",
                "2025550173"
        );
        double confidence = validator.validate(mixedPhones);
        assertThat(confidence).isGreaterThanOrEqualTo(70.0);
    }
    
    @Test
    public void validator_handles_short_numbers() {
        List<String> shortNumbers = Arrays.asList(
                "555",
                "123",
                "911",
                "112",
                "999"
        );
        double confidence = validator.validate(shortNumbers);
        // Short numbers may not be recognized as valid phones
        assertThat(confidence).isLessThan(60.0);
    }
    
    @Test
    public void validator_handles_empty_list() {
        List<String> empty = Arrays.asList();
        double confidence = validator.validate(empty);
        assertThat(confidence).isEqualTo(0.0);
    }
}
