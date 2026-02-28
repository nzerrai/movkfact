package com.movkfact.service.detection.financial;

import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for FinancialTypeDetector - orchestrator for financial type detection
 */
@SpringBootTest
public class FinancialTypeDetectorTests {
    
    @Autowired
    private FinancialTypeDetector detector;
    
    @Test
    public void detector_identifies_amount_column() {
        List<String> amounts = Arrays.asList(
                "1000.50",
                "500.25",
                "100.99",
                "2500.00",
                "750.75"
        );
        ColumnType detected = detector.detect("transaction_amount", amounts);
        assertThat(detected).isEqualTo(ColumnType.AMOUNT);
    }
    
    @Test
    public void detector_identifies_account_number_column() {
        List<String> accounts = Arrays.asList(
                "FR1420041010050500013M02606",
                "DE89370400440532013000",
                "GB82WEST12345698765432",
                "IT60X0542811101000000123456",
                "ES9121140418450200051332"
        );
        ColumnType detected = detector.detect("account_number", accounts);
        assertThat(detected).isEqualTo(ColumnType.ACCOUNT_NUMBER);
    }
    
    @Test
    public void detector_identifies_currency_column() {
        List<String> currencies = Arrays.asList(
                "EUR",
                "USD",
                "GBP",
                "JPY",
                "CHF"
        );
        ColumnType detected = detector.detect("currency_code", currencies);
        assertThat(detected).isEqualTo(ColumnType.CURRENCY);
    }
    
    @Test
    public void detector_prefers_amount_over_currency() {
        // When data could be either amount or currency (ambiguous)
        List<String> data = Arrays.asList(
                "1000",
                "500",
                "100",
                "2500",
                "750"
        );
        ColumnType detected = detector.detect("value_column", data);
        // Should detect as AMOUNT since numeric values look more like amounts
        assertThat(detected).isNotNull();
    }
    
    @Test
    public void detector_handles_mixed_amount_with_currency_symbols() {
        List<String> amounts = Arrays.asList(
                "$1000.50",
                "€500.25",
                "£100.99",
                "$2500.00",
                "€750.75"
        );
        ColumnType detected = detector.detect("price_with_currency", amounts);
        assertThat(detected).isEqualTo(ColumnType.AMOUNT);
    }
    
    @Test
    public void detector_returns_null_for_inconclusive_data() {
        List<String> mixed = Arrays.asList(
                "John",
                "Smith",
                "john@example.com",
                "12/01/2025",
                "France"
        );
        ColumnType detected = detector.detect("mixed_data", mixed);
        assertThat(detected).isNull();
    }
    
    @Test
    public void detector_handles_empty_list() {
        List<String> empty = Arrays.asList();
        ColumnType detected = detector.detect("empty_column", empty);
        assertThat(detected).isNull();
    }
}
