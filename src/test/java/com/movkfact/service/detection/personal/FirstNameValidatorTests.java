package com.movkfact.service.detection.personal;

import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for FirstNameValidator - validates first name patterns and formats
 */
@SpringBootTest
public class FirstNameValidatorTests {
    
    @Autowired
    private FirstNameValidator validator;
    
    @Test
    public void validator_detects_common_first_names() {
        List<String> firstNames = Arrays.asList(
                "Jean",
                "Marie",
                "John",
                "Sarah",
                "Michael"
        );
        double confidence = validator.validate(firstNames);
        assertThat(confidence).isGreaterThanOrEqualTo(80.0);
    }
    
    @Test
    public void validator_detects_first_names_with_accents() {
        List<String> firstNames = Arrays.asList(
                "José",
                "François",
                "Müller",
                "Søren",
                "André"
        );
        double confidence = validator.validate(firstNames);
        assertThat(confidence).isGreaterThanOrEqualTo(75.0);
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
    public void validator_rejects_emails() {
        List<String> emails = Arrays.asList(
                "john@example.com",
                "marie@test.fr",
                "test@domain.com"
        );
        double confidence = validator.validate(emails);
        assertThat(confidence).isLessThan(30.0);
    }
    
    @Test
    public void validator_handles_mixed_data() {
        List<String> mixed = Arrays.asList(
                "John",
                "Sarah",
                "123",
                "email@test.com",
                "Michael"
        );
        double confidence = validator.validate(mixed);
        // Should detect 3/5 names = 60% confidence
        assertThat(confidence).isBetween(50.0, 70.0);
    }
    
    @Test
    public void validator_handles_empty_list() {
        List<String> empty = Arrays.asList();
        double confidence = validator.validate(empty);
        assertThat(confidence).isEqualTo(0.0);
    }
}
