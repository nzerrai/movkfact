package com.movkfact.service.detection.financial;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for AccountNumberValidator - validates account number patterns (IBAN, BBAN, generic)
 */
@SpringBootTest
public class AccountNumberValidatorTests {
    
    @Autowired
    private AccountNumberValidator validator;
    
    @Test
    public void validator_detects_iban_format() {
        List<String> ibans = Arrays.asList(
                "FR1420041010050500013M02606",
                "DE89370400440532013000",
                "GB82WEST12345698765432",
                "IT60X0542811101000000123456",
                "ES9121140418450200051332"
        );
        double confidence = validator.validate(ibans);
        assertThat(confidence).isGreaterThanOrEqualTo(85.0);
    }
    
    @Test
    public void validator_detects_bban_format() {
        List<String> bbans = Arrays.asList(
                "20041010050500013M02606",
                "370400440532013000",
                "540400123456789012",
                "12345678901234567890",
                "98765432109876543210"
        );
        double confidence = validator.validate(bbans);
        assertThat(confidence).isGreaterThanOrEqualTo(75.0);
    }
    
    @Test
    public void validator_detects_masked_accounts() {
        List<String> masked = Arrays.asList(
                "****5678",
                "****1234",
                "****9999",
                "....5678",
                "****2222"
        );
        double confidence = validator.validate(masked);
        assertThat(confidence).isGreaterThanOrEqualTo(75.0);
    }
    
    @Test
    public void validator_detects_generic_account_numbers() {
        List<String> generic = Arrays.asList(
                "ACC123456789",
                "ACC987654321",
                "ACC111222333",
                "ACC555666777",
                "ACC999000111"
        );
        double confidence = validator.validate(generic);
        assertThat(confidence).isGreaterThanOrEqualTo(75.0);
    }
    
    @Test
    public void validator_rejects_non_account_data() {
        List<String> nonAccounts = Arrays.asList(
                "John Smith",
                "john@example.com",
                "12/01/2025",
                "France",
                "Male"
        );
        double confidence = validator.validate(nonAccounts);
        assertThat(confidence).isLessThan(30.0);
    }
    
    @Test
    public void validator_handles_mixed_account_data() {
        List<String> mixed = Arrays.asList(
                "FR1420041010050500013M02606",
                "John Smith",
                "****1234",
                "invalid",
                "ACC123456"
        );
        double confidence = validator.validate(mixed);
        assertThat(confidence).isBetween(50.0, 85.0);
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
                "FR1420041010050500013M02606",
                null,
                "DE89370400440532013000",
                "",
                "****5678"
        );
        double confidence = validator.validate(withNulls);
        assertThat(confidence).isGreaterThanOrEqualTo(75.0);
    }
}
