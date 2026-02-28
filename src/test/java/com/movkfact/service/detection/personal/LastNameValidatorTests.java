package com.movkfact.service.detection.personal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for LastNameValidator - validates last name patterns and formats
 */
@SpringBootTest
public class LastNameValidatorTests {
    
    @Autowired
    private LastNameValidator validator;
    
    @Test
    public void validator_detects_common_last_names() {
        List<String> lastNames = Arrays.asList(
                "Smith",
                "Johnson",
                "Martin",
                "Dupont",
                "Garcia"
        );
        double confidence = validator.validate(lastNames);
        assertThat(confidence).isGreaterThanOrEqualTo(80.0);
    }
    
    @Test
    public void validator_detects_last_names_with_accents() {
        List<String> lastNames = Arrays.asList(
                "Müller",
                "Björkman",
                "François",
                "Gonzáles",
                "Sørenssen"
        );
        double confidence = validator.validate(lastNames);
        assertThat(confidence).isGreaterThanOrEqualTo(75.0);
    }
    
    @Test
    public void validator_rejects_first_names() {
        List<String> firstNames = Arrays.asList(
                "Jean",
                "Marie",
                "John",
                "Sarah",
                "Michael"
        );
        double confidence = validator.validate(firstNames);
        assertThat(confidence).isLessThan(50.0);
    }
    
    @Test
    public void validator_rejects_full_names() {
        List<String> fullNames = Arrays.asList(
                "John Smith",
                "Marie Martin",
                "Jean-Claude Dupont"
        );
        double confidence = validator.validate(fullNames);
        assertThat(confidence).isLessThan(50.0);
    }
    
    @Test
    public void validator_handles_hyphenated_names() {
        List<String> hyphenatedNames = Arrays.asList(
                "Smith-Jones",
                "Martin-Garcia",
                "Dupont-Michel"
        );
        double confidence = validator.validate(hyphenatedNames);
        assertThat(confidence).isGreaterThanOrEqualTo(70.0);
    }
    
    @Test
    public void validator_handles_empty_list() {
        List<String> empty = Arrays.asList();
        double confidence = validator.validate(empty);
        assertThat(confidence).isEqualTo(0.0);
    }
}
