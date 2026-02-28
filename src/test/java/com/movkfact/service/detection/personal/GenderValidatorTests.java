package com.movkfact.service.detection.personal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for GenderValidator - validates gender code and text patterns
 */
@SpringBootTest
public class GenderValidatorTests {
    
    @Autowired
    private GenderValidator validator;
    
    @Test
    public void validator_detects_gender_abbreviations() {
        List<String> genderCodes = Arrays.asList(
                "M",
                "F",
                "M",
                "F",
                "M"
        );
        double confidence = validator.validate(genderCodes);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
    
    @Test
    public void validator_detects_gender_words_english() {
        List<String> genderWords = Arrays.asList(
                "Male",
                "Female",
                "M",
                "F",
                "Male"
        );
        double confidence = validator.validate(genderWords);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_gender_words_french() {
        List<String> genderWords = Arrays.asList(
                "Homme",
                "Femme",
                "M",
                "F",
                "Masculin"
        );
        double confidence = validator.validate(genderWords);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_rejects_non_gender_data() {
        List<String> nonGender = Arrays.asList(
                "John",
                "Smith",
                "john@example.com",
                "12345",
                "France"
        );
        double confidence = validator.validate(nonGender);
        assertThat(confidence).isLessThan(30.0);
    }
    
    @Test
    public void validator_handles_mixed_data() {
        List<String> mixed = Arrays.asList(
                "M",
                "Female",
                "John",
                "F",
                "Unknown"
        );
        double confidence = validator.validate(mixed);
        assertThat(confidence).isBetween(60.0, 80.0);
    }
    
    @Test
    public void validator_handles_empty_list() {
        List<String> empty = Arrays.asList();
        double confidence = validator.validate(empty);
        assertThat(confidence).isEqualTo(0.0);
    }
}
