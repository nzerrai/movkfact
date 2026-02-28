package com.movkfact.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.dto.TypeDetectionResult;
import com.movkfact.dto.DetectedColumn;
import com.movkfact.service.detection.CsvTypeDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API tests for TypeDetectionController - validates REST endpoint behavior.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TypeDetectionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired(required = false)
    private CsvTypeDetectionService detectionService;

    private MockMultipartFile createMockCsv(String content) {
        return new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    public void detectTypes_valid_csv_returns_200() throws Exception {
        String csvContent = "first_name,email\nJohn,john@example.com\n";
        MockMultipartFile file = createMockCsv(csvContent);

        mockMvc.perform(multipart("/api/domains/1/detect-types")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns", hasSize(greaterThan(0))));
    }

    @Test
    public void detectTypes_invalid_format_returns_400() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "not a csv".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/domains/1/detect-types")
                .file(invalidFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void detectTypes_oversized_file_returns_413() throws Exception {
        // Create a file larger than 10MB (the DetectionConfig limit)
        byte[] largeContent = new byte[11 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.csv",
                "text/csv",
                largeContent
        );

        mockMvc.perform(multipart("/api/domains/1/detect-types")
                .file(largeFile))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    public void detectTypes_response_contains_columns() throws Exception {
        String csvContent = "first_name,email,amount\nJohn,john@example.com,100.50\n";
        MockMultipartFile file = createMockCsv(csvContent);

        mockMvc.perform(multipart("/api/domains/1/detect-types")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns", hasSize(3)))
                .andExpect(jsonPath("$.columns[*].columnName", hasItems(
                        "first_name", "email", "amount")));
    }

    @Test
    public void detectTypes_response_includes_confidence() throws Exception {
        String csvContent = "email\njohn@example.com\njane@example.fr\n";
        MockMultipartFile file = createMockCsv(csvContent);

        mockMvc.perform(multipart("/api/domains/1/detect-types")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns[0].confidence", greaterThan(0.0)))
                .andExpect(jsonPath("$.columns[0].detectedType", notNullValue()));
    }

    @Test
    public void detectTypes_response_includes_detection_method() throws Exception {
        String csvContent = "email\njohn@example.com\n";
        MockMultipartFile file = createMockCsv(csvContent);

        mockMvc.perform(multipart("/api/domains/1/detect-types")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detectionMethod", equalTo("pattern_based")));
    }

    @Test
    public void detectTypes_with_sample_size_parameter() throws Exception {
        String csvContent = "email\njohn@example.com\njane@example.com\nbob@example.com\n";
        MockMultipartFile file = createMockCsv(csvContent);

        mockMvc.perform(multipart("/api/domains/1/detect-types")
                .file(file)
                .param("sampleSize", "2"))
                .andExpect(status().isOk());
    }

    @Test
    public void detectTypes_with_nulls_and_empty_values() throws Exception {
        String csvContent = "email,phone\njohn@example.com,+33612345678\n,\njane@example.fr,\n";
        MockMultipartFile file = createMockCsv(csvContent);

        mockMvc.perform(multipart("/api/domains/1/detect-types")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns", notNullValue()));
    }

    @Test
    public void detectTypes_with_headers_only_no_data_rows() throws Exception {
        // Edge case: CSV with headers but no data rows
        String csvContent = "first_name,email,amount\n";
        MockMultipartFile file = createMockCsv(csvContent);

        mockMvc.perform(multipart("/api/domains/1/detect-types")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns", notNullValue()));
    }

    @Test
    public void detectTypes_response_time_under_500ms() throws Exception {
        // Generate 5k row CSV
        StringBuilder csv = new StringBuilder("email,amount,date_birth\n");
        for (int i = 0; i < 5000; i++) {
            csv.append("user").append(i).append("@example.com,")
                    .append(100.50 + i).append(",")
                    .append("1990-05-15\n");
        }
        MockMultipartFile file = createMockCsv(csv.toString());

        long start = System.currentTimeMillis();
        mockMvc.perform(multipart("/api/domains/1/detect-types")
                        .file(file))
                .andExpect(status().isOk());
        long duration = System.currentTimeMillis() - start;

        // Response time should be under 500ms
        org.junit.jupiter.api.Assertions.assertTrue(duration < 500,
                "API response time should be under 500ms, but was " + duration + "ms");
    }

    @Test
    public void detectTypes_missing_file_returns_400() throws Exception {
        mockMvc.perform(multipart("/api/domains/1/detect-types"))
                .andExpect(status().isBadRequest());
    }
}
