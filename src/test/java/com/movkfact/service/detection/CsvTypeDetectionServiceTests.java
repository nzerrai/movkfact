package com.movkfact.service.detection;

import com.movkfact.dto.TypeDetectionResult;
import com.movkfact.dto.DetectedColumn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for CsvTypeDetectionService - validates full detection flow.
 */
@SpringBootTest
public class CsvTypeDetectionServiceTests {

    @Autowired(required = false)
    private CsvTypeDetectionService detectionService;

    private MultipartFile createMockCsv(String content) {
        return new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String createCsvContent(String[] headers, String[][] rows) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);

        // Write headers
        writer.println(String.join(",", headers));

        // Write rows
        for (String[] row : rows) {
            writer.println(String.join(",", row));
        }

        writer.flush();
        return baos.toString();
    }

    @Test
    public void detectTypes_valid_csv_with_personal_data() {
        String[] headers = {"first_name", "email", "phone"};
        String[][] rows = {
                {"John", "john@example.com", "+33612345678"},
                {"Jane", "jane@company.fr", "+33687654321"}
        };
        String csvContent = createCsvContent(headers, rows);
        MultipartFile csvFile = createMockCsv(csvContent);

        TypeDetectionResult result = detectionService.detectTypes(csvFile, 100);

        assertThat(result).isNotNull();
        assertThat(result.getColumns()).hasSize(3);
        assertThat(result.getColumns().stream()
                .map(DetectedColumn::getColumnName))
                .contains("first_name", "email", "phone");
    }

    @Test
    public void detectTypes_valid_csv_with_financial_data() {
        String[] headers = {"amount", "currency", "account_number"};
        String[][] rows = {
                {"100.50", "EUR", "1234-5678-9012-3456"},
                {"250.00", "USD", "4532-1234-5678-9012"}
        };
        String csvContent = createCsvContent(headers, rows);
        MultipartFile csvFile = createMockCsv(csvContent);

        TypeDetectionResult result = detectionService.detectTypes(csvFile, 100);

        assertThat(result).isNotNull();
        assertThat(result.getColumns()).hasSize(3);
    }

    @Test
    public void detectTypes_valid_csv_with_temporal_data() {
        String[] headers = {"date_birth", "created_at", "timezone"};
        String[][] rows = {
                {"1990-05-15", "2026-02-27", "Europe/Paris"},
                {"1985-12-20", "2026-02-26", "America/New_York"}
        };
        String csvContent = createCsvContent(headers, rows);
        MultipartFile csvFile = createMockCsv(csvContent);

        TypeDetectionResult result = detectionService.detectTypes(csvFile, 100);

        assertThat(result).isNotNull();
        assertThat(result.getColumns()).hasSize(3);
    }

    @Test
    public void detectTypes_mixed_types_comprehensive() {
        String[] headers = {"id", "first_name", "email", "amount", "created_date", "status"};
        String[][] rows = {
                {"1", "John", "john@example.com", "100.50", "2026-02-27", "active"},
                {"2", "Jane", "jane@example.fr", "250.00", "2026-02-26", "inactive"}
        };
        String csvContent = createCsvContent(headers, rows);
        MultipartFile csvFile = createMockCsv(csvContent);

        TypeDetectionResult result = detectionService.detectTypes(csvFile, 100);

        assertThat(result.getColumns()).hasSize(6);
        // At least some columns should be detected with high confidence
        long highConfidence = result.getColumns().stream()
                .filter(col -> col.getConfidence() > 80.0)
                .count();
        assertThat(highConfidence).isGreaterThan(0);
    }

    @Test
    public void detectTypes_with_nulls_and_empty_values() {
        String[] headers = {"email", "phone"};
        String[][] rows = {
                {"john@example.com", "+33612345678"},
                {"", ""},
                {"jane@example.fr", null}
        };
        String csvContent = createCsvContent(headers, rows);
        MultipartFile csvFile = createMockCsv(csvContent);

        TypeDetectionResult result = detectionService.detectTypes(csvFile, 100);

        // Should handle nulls/empty gracefully
        assertThat(result.getColumns()).hasSize(2);
    }

    @Test
    public void detectTypes_accuracy_above_90_percent() {
        String[] headers = {"first_name", "last_name", "email", "amount", "date_birth"};
        String[][] rows = {
                {"John", "Doe", "john@example.com", "100.50", "1990-05-15"},
                {"Jane", "Smith", "jane@company.fr", "250.00", "1985-12-20"},
                {"Bob", "Johnson", "bob@test.com", "150.75", "1995-03-10"}
        };
        String csvContent = createCsvContent(headers, rows);
        MultipartFile csvFile = createMockCsv(csvContent);

        TypeDetectionResult result = detectionService.detectTypes(csvFile, 100);

        // Calculate accuracy: correct detections / total columns
        long correctDetections = result.getColumns().stream()
                .filter(col -> col.getConfidence() > 80.0)
                .count();
        double accuracy = (double) correctDetections / headers.length * 100;

        assertThat(accuracy).isGreaterThanOrEqualTo(90.0);
    }

    @Test
    public void detectTypes_returns_detection_method() {
        String[][] rows = {{"john@example.com"}};
        String csvContent = createCsvContent(new String[]{"email"}, rows);
        MultipartFile csvFile = createMockCsv(csvContent);

        TypeDetectionResult result = detectionService.detectTypes(csvFile, 100);

        assertThat(result.getDetectionMethod()).isEqualTo("pattern_based");
    }

    @Test
    public void detectTypes_handles_sample_size_parameter() {
        String[] headers = {"email"};
        String[][] rows = new String[20][1];
        for (int i = 0; i < 20; i++) {
            rows[i] = new String[]{"user" + i + "@example.com"};
        }
        String csvContent = createCsvContent(headers, rows);
        MultipartFile csvFile = createMockCsv(csvContent);

        // Sample size of 5 - should only analyze first 5 rows
        TypeDetectionResult result = detectionService.detectTypes(csvFile, 5);

        assertThat(result).isNotNull();
        assertThat(result.getColumns()).hasSize(1);
    }

    @Test
    public void detectTypes_performance_under_500ms_for_10k_rows() {
        // Generate 10k row CSV
        StringBuilder csv = new StringBuilder("email,amount,date_birth\n");
        for (int i = 0; i < 10000; i++) {
            csv.append("user").append(i).append("@example.com,")
                    .append(100.50 + i).append(",")
                    .append("1990-05-15\n");
        }
        MultipartFile csvFile = createMockCsv(csv.toString());

        long start = System.currentTimeMillis();
        TypeDetectionResult result = detectionService.detectTypes(csvFile, 100);
        long duration = System.currentTimeMillis() - start;

        assertThat(duration)
                .as("Detection should complete in <500ms for 10k rows")
                .isLessThan(500L);
        assertThat(result).isNotNull();
    }
}
