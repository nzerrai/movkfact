package com.movkfact.service.detection.financial;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for CurrencyValidator - validates ISO 4217 currency codes and symbols
 */
@SpringBootTest
public class CurrencyValidatorTests {
    
    @Autowired
    private CurrencyValidator validator;
    
    @Test
    public void validator_detects_iso_currency_codes() {
        List<String> codes = Arrays.asList(
                "EUR",
                "USD",
                "GBP",
                "JPY",
                "CHF"
        );
        double confidence = validator.validate(codes);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
    
    @Test
    public void validator_detects_lowercase_currency_codes() {
        List<String> codes = Arrays.asList(
                "eur",
                "usd",
                "gbp",
                "jpy",
                "chf"
        );
        double confidence = validator.validate(codes);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
    
    @Test
    public void validator_detects_currency_symbols() {
        List<String> symbols = Arrays.asList(
                "€",
                "$",
                "£",
                "¥",
                "€"
        );
        double confidence = validator.validate(symbols);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
    
    @Test
    public void validator_detects_mixed_currency_formats() {
        List<String> mixed = Arrays.asList(
                "EUR",
                "$",
                "GBP",
                "€",
                "USD"
        );
        double confidence = validator.validate(mixed);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_additional_iso_codes() {
        List<String> codes = Arrays.asList(
                "AUD",
                "CAD",
                "NZD",
                "SEK",
                "NOK"
        );
        double confidence = validator.validate(codes);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
    
    @Test
    public void validator_rejects_non_currency_data() {
        List<String> nonCurrency = Arrays.asList(
                "John",
                "john@example.com",
                "12/01/2025",
                "France",
                "Male"
        );
        double confidence = validator.validate(nonCurrency);
        assertThat(confidence).isLessThan(30.0);
    }
    
    @Test
    public void validator_handles_mixed_currency_data() {
        List<String> mixed = Arrays.asList(
                "EUR",
                "John",
                "€",
                "invalid",
                "USD"
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
                "EUR",
                null,
                "USD",
                "",
                "GBP"
        );
        double confidence = validator.validate(withNulls);
        assertThat(confidence).isGreaterThanOrEqualTo(90.0);
    }
}
