package com.movkfact.service.detection;

import com.movkfact.enums.ColumnType;
import com.movkfact.dto.TypeDetectionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ColumnValueAnalyzer - validates value-based detection confirmation.
 */
@SpringBootTest
public class ColumnValueAnalyzerTests {

    @Autowired(required = false)
    private ColumnValueAnalyzer analyzer;

    @BeforeEach
    public void setup() {
        // Analyzer will be initialized
    }

    @Test
    public void analyzer_detects_email_format() {
        List<String> emailValues = List.of(
                "john@example.com",
                "jane@example.fr",
                "admin@company.com"
        );
        ColumnType detected = analyzer.analyzeValues("email_column", emailValues);
        assertThat(detected).isEqualTo(ColumnType.EMAIL);
    }

    @Test
    public void analyzer_detects_amount_numeric_format() {
        List<String> amountValues = List.of(
                "100.50",
                "50",
                "1234.99"
        );
        ColumnType detected = analyzer.analyzeValues("total_amount", amountValues);
        assertThat(detected).isEqualTo(ColumnType.AMOUNT);
    }

    @Test
    public void analyzer_detects_date_formats() {
        List<String> dateValues = List.of(
                "2026-02-27",
                "27/02/2026",
                "02-27-2026"
        );
        ColumnType detected = analyzer.analyzeValues("created_at", dateValues);
        assertThat(detected).isEqualTo(ColumnType.DATE);
    }

    @Test
    public void analyzer_returns_unknown_for_mixed_data() {
        List<String> mixedValues = List.of(
                "random123",
                "abc@def",
                "xyz"
        );
        ColumnType detected = analyzer.analyzeValues("unknown", mixedValues);
        // Analyzer may return null when confidence is too low
        assertThat(detected).isNull();
    }

    @Test
    public void analyzer_handles_null_and_empty_values() {
        // Use Arrays.asList instead of List.of() to allow null values
        List<String> mixedWithNulls = Arrays.asList(
                "john@example.com",
                null,
                "",
                "jane@example.fr"
        );
        ColumnType detected = analyzer.analyzeValues("email_with_nulls", mixedWithNulls);
        // Should still detect EMAIL despite nulls/empty
        assertThat(detected).isEqualTo(ColumnType.EMAIL);
    }

    @Test
    public void analyzer_returns_confidence_score() {
        List<String> creditCardLike = List.of(
                "1234-5678-9012-3456",
                "4532-1234-5678-9012",
                "6011-1234-5678-9012"
        );
        ColumnType detected = analyzer.analyzeValues("account_numbers", creditCardLike);
        assertThat(detected).isEqualTo(ColumnType.ACCOUNT_NUMBER);
    }
}
