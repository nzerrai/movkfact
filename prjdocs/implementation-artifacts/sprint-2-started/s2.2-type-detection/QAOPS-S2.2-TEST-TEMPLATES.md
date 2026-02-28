---
title: "S2.2 - QA TEST AUTOMATION CODE TEMPLATES"
date: 2026-02-28
owner: "Quinn (QA Engineer)"
purpose: "JUnit 5 test patterns & code templates for S2.2 implementation"
---

# 🧪 S2.2 Test Automation - Code Templates for Quinn

**For:** Quinn (QA Engineer)  
**Purpose:** Pre-built test patterns to accelerate automation  
**Usage:** Copy-paste & customize for each detector  
**Target:** 85%+ coverage, fast TDD cycle with Amelia

---

## 📐 Template 1: Unit Test - Single Detector

```java
package com.movkfact.service.detection;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("EmailTypeDetector Unit Tests")
class EmailTypeDetectorTests {
    
    private EmailTypeDetector detector;
    private static final double CONFIDENCE_THRESHOLD = 0.80;
    
    @BeforeEach
    void setUp() {
        detector = new EmailTypeDetector();
    }
    
    // ========== HEADER MATCHING TESTS ==========
    
    @Test
    @DisplayName("Exact header match - 'email' should give high confidence")
    void testExactHeaderMatch() {
        String header = "email";
        int score = detector.matchHeaderPatterns(header);
        assert Score >= 95 : "Exact match should score >=95%";
    }
    
    @Test
    @DisplayName("Partial header match - 'emailaddress' should match")
    void testPartialHeaderMatch() {
        String header = "emailaddress";
        int score = detector.matchHeaderPatterns(header);
        assertTrue(score > 50, "Partial match should score >50%");
    }
    
    @Test
    @DisplayName("No header match - 'xyz' should give low score")
    void testNoHeaderMatch() {
        String header = "xyz";
        int score = detector.matchHeaderPatterns(header);
        assertEquals(0, score, "No match should score 0%");
    }
    
    @Test
    @DisplayName("Case insensitivity - 'EMAIL' should match like 'email'")
    void testCaseInsensitivity() {
        int score1 = detector.matchHeaderPatterns("email");
        int score2 = detector.matchHeaderPatterns("EMAIL");
        assertEquals(score1, score2, "Scoring should be case-insensitive");
    }
    
    // ========== VALUE MATCHING TESTS ==========
    
    @Test
    @DisplayName("All valid emails - should score 100%")
    void testAllValidEmails() {
        List<String> values = List.of(
            "user@example.com",
            "john.doe@company.co.uk",
            "info+tag@service.org"
        );
        
        int score = detector.analyzeValuePatterns(values);
        assertEquals(100, score, "All valid emails should score 100%");
    }
    
    @Test
    @DisplayName("Partial valid emails - should score >50% but <100%")
    void testPartialValidEmails() {
        List<String> values = List.of(
            "user@example.com",      // valid
            "invalid-no-at",         // invalid
            "john@domain.com"        // valid
        );
        
        int score = detector.analyzeValuePatterns(values);
        assertTrue(score > 50 && score < 100, 
                   "Mixed valid/invalid should score between 50-100%, got: " + score);
    }
    
    @Test
    @DisplayName("No valid emails - should score 0%")
    void testNoValidEmails() {
        List<String> values = List.of(
            "not-an-email",
            "no-at-symbol",
            "@nodomain"
        );
        
        int score = detector.analyzeValuePatterns(values);
        assertEquals(0, score, "No valid emails should score 0%");
    }
    
    @ParameterizedTest
    @CsvSource({
        "user@example.com, true",
        "john.doe+tag@company.co.uk, true",
        "@example.com, false",
        "user@, false",
        "plaintext, false",
        "user @example.com, false"
    })
    @DisplayName("Email validation - parameterized test")
    void testEmailValidation(String email, boolean expected) {
        boolean result = detector.isValidEmail(email);
        assertEquals(expected, result, "Email validation failed for: " + email);
    }
    
    // ========== NULL/EMPTY HANDLING TESTS ==========
    
    @Test
    @DisplayName("Empty value list - should handle gracefully")
    void testEmptyValueList() {
        List<String> values = Collections.emptyList();
        int score = detector.analyzeValuePatterns(values);
        assertEquals(0, score, "Empty list should score 0%, not crash");
    }
    
    @Test
    @DisplayName("All null values - should handle gracefully")
    void testAllNullValues() {
        List<String> values = List.of(null, null, null);
        assertDoesNotThrow(() -> detector.analyzeValuePatterns(values),
                          "Null values should not throw exception");
    }
    
    @Test
    @DisplayName("Mixed valid and null - should score based on non-null only")
    void testMixedValidAndNull() {
        List<String> values = List.of(
            null,
            "user@example.com",
            null,
            "john@domain.com"
        );
        
        int score = detector.analyzeValuePatterns(values);
        assertTrue(score >= 90, "Should only score on non-null values");
    }
    
    // ========== COMBINED SCORE TEST ==========
    
    @Test
    @DisplayName("Combined confidence = (header × 0.4) + (value × 0.6)")
    void testCombinedScoring() {
        // Header score: 98%, Value score: 100%
        // Expected: (98 × 0.4) + (100 × 0.6) = 39.2 + 60 = 99.2%
        
        String header = "email";
        List<String> values = List.of("user@example.com", "john@domain.com");
        
        DetectionResult result = detector.detect(header, values);
        
        assertTrue(result.getConfidence() >= 0.95,
                   "Combined score should be ~99%, got: " + result.getConfidence());
    }
    
    // ========== EDGE CASES ==========
    
    @Test
    @DisplayName("International characters - utf-8 encoded emails")
    void testInternationalCharacters() {
        List<String> values = List.of(
            "françois@example.com",
            "müller@domain.de",
            "garcía@españa.es"
        );
        
        int score = detector.analyzeValuePatterns(values);
        assertTrue(score > 80, "Should handle UTF-8 international characters");
    }
    
    @Test
    @DisplayName("Whitespace handling - should trim values")
    void testWhitespaceHandling() {
        List<String> values = List.of(
            "  user@example.com  ",
            "john@domain.com\n",
            "\tjohn2@test.com"
        );
        
        int score = detector.analyzeValuePatterns(values);
        assertTrue(score >= 80, "Should handle whitespace gracefully");
    }
    
    // ========== PERFORMANCE TESTS ==========
    
    @Test
    @DisplayName("Performance - 1000 emails should process in <50ms")
    void testPerformance() {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            values.add("user" + i + "@example.com");
        }
        
        long startTime = System.currentTimeMillis();
        int score = detector.analyzeValuePatterns(values);
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue(duration < 50, 
                   "Should process 1000 values in <50ms, took: " + duration + "ms");
        assertEquals(100, score);
    }
}
```

---

## 📐 Template 2: Integration Test - API Controller

```java
package com.movkfact.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Type Detection API Integration Tests")
class TypeDetectionControllerTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    private static final String ENDPOINT = "/api/type-detection";
    
    // ========== HAPPY PATH TESTS ==========
    
    @Test
    @DisplayName("POST /api/type-detection with valid CSV - returns 200 OK")
    void testDetectTypesWithValidCSV() throws Exception {
        String csvContent = "email,name,amount\n" 
                          + "user@example.com,John Doe,1000.50\n"
                          + "jane@test.com,Jane Smith,2000.00";
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            csvContent.getBytes()
        );
        
        mockMvc.perform(multipart(ENDPOINT)
                .file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.columns").isArray())
            .andExpect(jsonPath("$.columns.length()").value(3))
            .andExpect(jsonPath("$.columns[0].type").value("EMAIL"))
            .andExpect(jsonPath("$.columns[0].confidence").value(greaterThan(0.9)))
            .andExpect(jsonPath("$.columns[1].type").value("FIRST_NAME"))
            .andExpect(jsonPath("$.columns[2].type").value("AMOUNT"));
    }
    
    @Test
    @DisplayName("POST /api/type-detection with CSV text body - returns 200 OK")
    void testDetectTypesWithTextBody() throws Exception {
        String csvContent = "first_name,last_name,phone\n"
                          + "John,Doe,+1-555-0100\n"
                          + "Jane,Smith,+1-555-0101";
        
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"data\": \"" + csvContent.replace("\n", "\\n") + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.columns[0].type").value("FIRST_NAME"))
            .andExpect(jsonPath("$.columns[1].type").value("LAST_NAME"))
            .andExpect(jsonPath("$.columns[2].type").value("PHONE"));
    }
    
    // ========== ERROR HANDLING TESTS ==========
    
    @Test
    @DisplayName("POST /api/type-detection without file - returns 400 Bad Request")
    void testMissingFile() throws Exception {
        mockMvc.perform(multipart(ENDPOINT))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }
    
    @Test
    @DisplayName("POST /api/type-detection with empty csv - returns 400 Bad Request")
    void testEmptyCSV() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.csv",
            "text/csv",
            "".getBytes()
        );
        
        mockMvc.perform(multipart(ENDPOINT)
                .file(file))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/type-detection with malformed csv - returns 422")
    void testMalformedCSV() throws Exception {
        String malformedCSV = "header1,header2\n"
                            + "\"unclosed quote,value2";
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "malformed.csv",
            "text/csv",
            malformedCSV.getBytes()
        );
        
        mockMvc.perform(multipart(ENDPOINT)
                .file(file))
            .andExpect(status().isUnprocessableEntity());
    }
    
    // ========== EDGE CASES ==========
    
    @Test
    @DisplayName("CSV with header only (no data rows) - returns UNKNOWN")
    void testHeaderOnlyCSV() throws Exception {
        String csvContent = "email,phone,address";
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "header-only.csv",
            "text/csv",
            csvContent.getBytes()
        );
        
        mockMvc.perform(multipart(ENDPOINT)
                .file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.columns[0].type").value("UNKNOWN"))
            .andExpect(jsonPath("$.columns[0].confidence").value(lessThan(0.5)));
    }
    
    @Test
    @DisplayName("Large CSV with 10K rows - returns <500ms")
    void testLargeCSVPerformance() throws Exception {
        StringBuilder csv = new StringBuilder("email,name,amount\n");
        for (int i = 0; i < 10000; i++) {
            csv.append("user").append(i).append("@example.com,")
               .append("User ").append(i).append(",")
               .append(1000 + i).append("\n");
        }
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.csv",
            "text/csv",
            csv.toString().getBytes()
        );
        
        long startTime = System.currentTimeMillis();
        mockMvc.perform(multipart(ENDPOINT)
                .file(file))
            .andExpect(status().isOk());
        long duration = System.currentTimeMillis() - startTime;
        
        assertTrue(duration < 500, "Should process 10K rows in <500ms, took: " + duration + "ms");
    }
}
```

---

## 📐 Template 3: End-to-End Test - Real CSV Files

```java
package com.movkfact.service.detection;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.file.*;

@DisplayName("Type Detection E2E - Real CSV Files")
class TypeDetectionE2ETests {
    
    private TypeDetectionService service = new TypeDetectionService();
    
    // ========== ACCURACY VALIDATION ==========
    
    @ParameterizedTest
    @ValueSource(strings = {
        "src/test/resources/s2.2-samples/set-1-easy/personal-basic.csv",
        "src/test/resources/s2.2-samples/set-1-easy/financial-basic.csv",
        "src/test/resources/s2.2-samples/set-1-easy/temporal-basic.csv"
    })
    @DisplayName("Easy test samples - expect 95%+ accuracy")
    void testEasySampleAccuracy(String csvFilePath) throws Exception {
        Path path = Paths.get(csvFilePath);
        assertTrue(Files.exists(path), "Test file not found: " + csvFilePath);
        
        TypeDetectionResponse response = service.detectFromFile(path);
        
        // Load expected results
        GroundTruth expected = GroundTruthLoader.load(
            csvFilePath.replace(".csv", ".ground-truth.json")
        );
        
        // Calculate accuracy
        double accuracy = calculateAccuracy(response, expected);
        
        assertEquals(expected.getExpectedAccuracy(), accuracy, 0.05,
                     "Accuracy mismatch for: " + csvFilePath);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "src/test/resources/s2.2-samples/set-2-medium/ambiguous-name.csv",
        "src/test/resources/s2.2-samples/set-2-medium/ambiguous-date.csv"
    })
    @DisplayName("Medium test samples - expect 75-85% accuracy")
    void testMediumSampleAccuracy(String csvFilePath) throws Exception {
        Path path = Paths.get(csvFilePath);
        
        TypeDetectionResponse response = service.detectFromFile(path);
        GroundTruth expected = GroundTruthLoader.load(
            csvFilePath.replace(".csv", ".ground-truth.json")
        );
        
        double accuracy = calculateAccuracy(response, expected);
        
        assertTrue(accuracy >= 0.75 && accuracy <= 0.85,
                   "Medium accuracy should be 75-85%, got: " + accuracy);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "src/test/resources/s2.2-samples/set-3-hard/ambiguous-headers.csv",
        "src/test/resources/s2.2-samples/set-4-robustness/missing-values.csv"
    })
    @DisplayName("Hard test samples - expect 60%+ accuracy")
    void testHardSampleAccuracy(String csvFilePath) throws Exception {
        Path path = Paths.get(csvFilePath);
        
        TypeDetectionResponse response = service.detectFromFile(path);
        GroundTruth expected = GroundTruthLoader.load(
            csvFilePath.replace(".csv", ".ground-truth.json")
        );
        
        double accuracy = calculateAccuracy(response, expected);
        
        assertTrue(accuracy >= 0.60,
                   "Hard accuracy should be 60%+, got: " + accuracy);
    }
    
    // ========== ROBUSTNESS TESTS ==========
    
    @Test
    @DisplayName("Robustness - CSV with 35% null values should not crash")
    void testNullHandling() throws Exception {
        Path path = Paths.get("src/test/resources/s2.2-samples/set-3-edge/missing-values.csv");
        
        assertDoesNotThrow(() -> {
            TypeDetectionResponse response = service.detectFromFile(path);
            assertNotNull(response);
            assertNotNull(response.getColumns());
        });
    }
    
    @Test
    @DisplayName("Robustness - UTF-8 encoded file with accents")
    void testUTF8Encoding() throws Exception {
        Path path = Paths.get("src/test/resources/s2.2-samples/set-4-encoding/utf-8-accents.csv");
        
        TypeDetectionResponse response = service.detectFromFile(path);
        
        // Verify no garbled output
        response.getColumns().forEach(col -> {
            assertFalse(col.getType().contains("?"),
                       "Column type contains garbled characters");
        });
    }
    
    @Test
    @DisplayName("Robustness - Large file 10K rows should complete <500ms")
    void testLargeFilePerformance() throws Exception {
        Path path = Paths.get("src/test/resources/s2.2-samples/set-5-performance/10k-rows.csv");
        
        long startTime = System.currentTimeMillis();
        TypeDetectionResponse response = service.detectFromFile(path);
        long duration = System.currentTimeMillis() - startTime;
        
        assertNotNull(response);
        assertTrue(duration < 500,
                   "Should process 10K rows in <500ms, took: " + duration + "ms");
    }
    
    // ========== HELPER METHODS ==========
    
    private double calculateAccuracy(TypeDetectionResponse actual, GroundTruth expected) {
        int correct = 0;
        int total = Math.min(actual.getColumns().size(), expected.getExpectedTypes().size());
        
        for (int i = 0; i < total; i++) {
            String actualType = actual.getColumns().get(i).getType();
            String expectedType = expected.getExpectedTypes().get(i);
            
            if (actualType.equals(expectedType)) {
                correct++;
            } else if (actual.getColumns().get(i).getAlternatives().contains(expectedType)) {
                // Partial credit for correct alternative
                correct += 0.5;
            }
        }
        
        return correct / (double) total;
    }
}
```

---

## 📐 Template 4: Accuracy Validation Report Generator

```java
package com.movkfact.service.detection.qa;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.util.*;

@DisplayName("QA Accuracy Report Generator")
public class AccuracyReportGenerator {
    
    private TypeDetectionService service;
    private GroundTruthLoader loader;
    
    public void generateAccuracyReport(Path testDataDir, Path outputFile) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        
        // Test Set Metadata
        report.put("generatedAt", LocalDateTime.now().toString());
        report.put("testDataDir", testDataDir.toString());
        
        // Overall Statistics
        Map<String, Double> overallStats = new LinkedHashMap<>();
        List<Map<String, Object>> testSetResults = new ArrayList<>();
        
        // Process each test set
        for (String setName : List.of("set-1-easy", "set-2-medium", "set-3-hard", 
                                      "set-4-robustness", "set-5-performance")) {
            
            Path setDir = testDataDir.resolve(setName);
            if (!Files.exists(setDir)) continue;
            
            Map<String, Object> setResult = processTestSet(setName, setDir);
            testSetResults.add(setResult);
        }
        
        report.put("testSets", testSetResults);
        
        // Calculate overall accuracy
        double overallAccuracy = testSetResults.stream()
            .mapToDouble(r -> (Double) r.get("accuracy"))
            .average()
            .orElse(0.0);
        
        report.put("overallAccuracy", String.format("%.2f%%", overallAccuracy * 100));
        
        // Write to file
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(outputFile.toFile(), report);
        
        System.out.println("✅ Report generated: " + outputFile);
    }
    
    private Map<String, Object> processTestSet(String setName, Path setDir) throws Exception {
        Map<String, Object> setResult = new LinkedHashMap<>();
        setResult.put("name", setName);
        
        List<Path> csvFiles = Files.list(setDir)
            .filter(p -> p.toString().endsWith(".csv"))
            .sorted()
            .toList();
        
        List<Map<String, Object>> fileResults = new ArrayList<>();
        int totalCorrect = 0;
        int totalColumns = 0;
        
        for (Path csvFile : csvFiles) {
            TypeDetectionResponse response = service.detectFromFile(csvFile);
            GroundTruth expected = loader.load(
                csvFile.toString().replace(".csv", ".ground-truth.json")
            );
            
            int columnsCorrect = 0;
            for (int i = 0; i < response.getColumns().size(); i++) {
                if (response.getColumns().get(i).getType()
                        .equals(expected.getExpectedTypes().get(i))) {
                    columnsCorrect++;
                }
            }
            
            double fileAccuracy = columnsCorrect / (double) response.getColumns().size();
            
            Map<String, Object> fileResult = new LinkedHashMap<>();
            fileResult.put("file", csvFile.getFileName().toString());
            fileResult.put("accuracy", String.format("%.2f%%", fileAccuracy * 100));
            fileResult.put("correctColumns", columnsCorrect);
            fileResult.put("totalColumns", response.getColumns().size());
            
            fileResults.add(fileResult);
            totalCorrect += columnsCorrect;
            totalColumns += response.getColumns().size();
        }
        
        double setAccuracy = totalCorrect / (double) totalColumns;
        setResult.put("accuracy", setAccuracy);
        setResult.put("accuracyFormatted", String.format("%.2f%%", setAccuracy * 100));
        setResult.put("filesProcessed", csvFiles.size());
        setResult.put("fileResults", fileResults);
        
        return setResult;
    }
}
```

---

## 🎯 Template Usage Guide

**Step 1: Copy Template 1 to Quinn's test class**
```bash
cp src/test/java/com/movkfact/service/detection/EmailTypeDetectorTests.java
# Customize for each of 13 detectors
```

**Step 2: Adapt for each detector**
```java
// Change class name
class PhoneTypeDetectorTests { ... }

// Update test values
List<String> values = List.of(
    "+1-555-0100",
    "+33 1 23 45 67",
    "555.0100"
);

// Adjust scoring expectations
assertTrue(score >= 85, "Phone should score 85%+");
```

**Step 3: Run TDD cycle**
```bash
# Run test (fails initially)
mvn test -Dtest=EmailTypeDetectorTests

# Amelia implements
# Test passes ✅

# Repeat for all 13 detectors
```

**Step 4: Generate coverage**
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

---

**Last Updated:** 28 février 2026  
**Status:** ✅ Ready for Quinn to use

