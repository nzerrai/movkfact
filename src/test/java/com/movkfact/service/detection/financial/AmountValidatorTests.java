package com.movkfact.service.detection.financial;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for AmountValidator - validates monetary amount patterns
 */
@SpringBootTest
public class AmountValidatorTests {
    
    @Autowired
    private AmountValidator validator;
    
    @Test
    public void validator_detects_simple_integers() {
        List<String> amounts = Arrays.asList(
                "1000",
                "500",
                "100",
                "2500",
                "750"
        );
        double confidence = validator.validate(amounts);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
    
    @Test
    public void validator_detects_decimal_amounts() {
        List<String> amounts = Arrays.asList(
                "1000.50",
                "500.25",
                "100.99",
                "2500.00",
                "750.75"
        );
        double confidence = validator.validate(amounts);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
    
    @Test
    public void validator_detects_currency_symbols() {
        List<String> amounts = Arrays.asList(
                "$1000.50",
                "€500.25",
                "£100.99",
                "$2500",
                "€750.75"
        );
        double confidence = validator.validate(amounts);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_thousands_separators() {
        List<String> amounts = Arrays.asList(
                "1,000.50",
                "500,000.25",
                "1.234,56",  // European format
                "2,500.00",
                "750.75"
        );
        double confidence = validator.validate(amounts);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_rejects_non_numeric_data() {
        List<String> nonAmounts = Arrays.asList(
                "John",
                "john@example.com",
                "12/01/2025",
                "France",
                "Male"
        );
        double confidence = validator.validate(nonAmounts);
        assertThat(confidence).isLessThan(30.0);
    }
    
    @Test
    public void validator_handles_mixed_amount_data() {
        List<String> mixed = Arrays.asList(
                "1000.50",
                "John",
                "€500",
                "invalid",
                "2,500.00"
        );
        double confidence = validator.validate(mixed);
        assertThat(confidence).isBetween(50.0, 75.0);
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
                "1000.50",
                null,
                "500.25",
                "",
                "2500"
        );
        double confidence = validator.validate(withNulls);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
}
