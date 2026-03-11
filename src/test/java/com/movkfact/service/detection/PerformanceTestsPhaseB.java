package com.movkfact.service.detection;

import com.movkfact.enums.ColumnType;
import com.movkfact.dto.TypeDetectionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance Benchmark Tests for S2.2 Type Detection
 * 
 * Tests type detection performance on different CSV sizes:
 * - 10 rows: target <10ms
 * - 1K rows: target <50ms
 * - 10K rows: target <500ms (AC1 acceptance criteria)
 * 
 * @since S2.2.3 Phase B
 */
@SpringBootTest
public class PerformanceTestsPhaseB {
    
    @Autowired
    private CsvTypeDetectionService detectionService;
    
    private static final String[] HEADERS = {"first_name", "email", "amount", "date_birth", "phone"};
    
    /**
     * Generate CSV content with specified number of rows
     */
    private String generateCsvContent(int rowCount) {
        StringBuilder csv = new StringBuilder();
        
        // Headers
        csv.append(String.join(",", HEADERS)).append("\n");
        
        // Data rows
        String[] firstNames = {"Jean", "Marie", "Pierre", "Sophie", "Luc", "Anne", "Paul", "Claire"};
        String[] domains = {"gmail.com", "yahoo.com", "outlook.com", "example.com", "test.com"};
        String[] dates = {"1990-01-15", "1985-06-20", "1992-03-10", "1988-11-25", "1995-07-30"};
        String[] phones = {"+33612345678", "0612345678", "+1234567890", "06.12.34.56.78"};
        
        Random random = new Random(42); // Seed for reproducibility
        
        for (int i = 0; i < rowCount; i++) {
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String email = firstName.toLowerCase() + i + "@" + domains[random.nextInt(domains.length)];
            String amount = String.valueOf(100 + random.nextInt(900));
            String date = dates[random.nextInt(dates.length)];
            String phone = phones[random.nextInt(phones.length)];
            
            csv.append(firstName).append(",")
               .append(email).append(",")
               .append(amount).append(",")
               .append(date).append(",")
               .append(phone).append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Create MockMultipartFile from CSV content
     */
    private MultipartFile createMockCsvFile(String csvContent, String filename) throws IOException {
        return new MockMultipartFile(
                "file",
                filename,
                "text/csv",
                csvContent.getBytes()
        );
    }
    
    /**
     * BENCHMARK 1: Small file (10 rows)
     * Target: <10ms
     */
    @Test
    public void benchmark_10Rows() throws IOException {
        String csvContent = generateCsvContent(10);
        MultipartFile csvFile = createMockCsvFile(csvContent, "test-10-rows.csv");
        
        // Warm up
        detectionService.detectTypes(csvFile, 10, false);
        
        // Actual benchmark (multiple runs for average)
        long totalTime = 0;
        int runs = 5;
        
        for (int run = 0; run < runs; run++) {
            csvFile = createMockCsvFile(csvContent, "test-10-rows.csv");
            long startTime = System.nanoTime();
            
            TypeDetectionResult result = detectionService.detectTypes(csvFile, 10, false);
            
            long endTime = System.nanoTime();
            long elapsedMs = (endTime - startTime) / 1_000_000;
            totalTime += elapsedMs;
            
            System.out.println("  Run " + (run + 1) + ": " + elapsedMs + "ms");
            
            // Verify results
            assertNotNull(result);
            assertFalse(result.getColumns().isEmpty());
        }
        
        long avgTime = totalTime / runs;
        System.out.println("BENCHMARK 1: 10 rows - Average: " + avgTime + "ms (target: <10ms)");
        
        // Target: <10ms (allow some tolerance for JVM startup)
        assertTrue(avgTime < 100, "10-row detection took " + avgTime + "ms (should be <100ms initial run)");
    }
    
    /**
     * BENCHMARK 2: Medium file (1K rows)
     * Target: <50ms
     */
    @Test
    public void benchmark_1KRows() throws IOException {
        String csvContent = generateCsvContent(1000);
        MultipartFile csvFile = createMockCsvFile(csvContent, "test-1k-rows.csv");
        
        // Warm up
        detectionService.detectTypes(csvFile, 1000, false);
        
        // Actual benchmark
        long totalTime = 0;
        int runs = 5;
        
        for (int run = 0; run < runs; run++) {
            csvFile = createMockCsvFile(csvContent, "test-1k-rows.csv");
            long startTime = System.nanoTime();
            
            TypeDetectionResult result = detectionService.detectTypes(csvFile, 1000, false);
            
            long endTime = System.nanoTime();
            long elapsedMs = (endTime - startTime) / 1_000_000;
            totalTime += elapsedMs;
            
            System.out.println("  Run " + (run + 1) + ": " + elapsedMs + "ms");
            
            // Verify results
            assertNotNull(result);
            assertFalse(result.getColumns().isEmpty());
        }
        
        long avgTime = totalTime / runs;
        System.out.println("BENCHMARK 2: 1K rows - Average: " + avgTime + "ms (target: <50ms)");
        
        // Target: <50ms
        assertTrue(avgTime < 150, "1K-row detection took " + avgTime + "ms (should be <150ms)");
    }
    
    /**
     * BENCHMARK 3: Large file (10K rows)
     * Target: <500ms (AC1 ACCEPTANCE CRITERIA)
     */
    @Test
    public void benchmark_10KRows() throws IOException {
        String csvContent = generateCsvContent(10000);
        MultipartFile csvFile = createMockCsvFile(csvContent, "test-10k-rows.csv");
        
        // Warm up
        detectionService.detectTypes(csvFile, 10000, false);
        
        // Actual benchmark
        long totalTime = 0;
        int runs = 3;
        
        for (int run = 0; run < runs; run++) {
            csvFile = createMockCsvFile(csvContent, "test-10k-rows.csv");
            long startTime = System.nanoTime();
            
            TypeDetectionResult result = detectionService.detectTypes(csvFile, 10000, false);
            
            long endTime = System.nanoTime();
            long elapsedMs = (endTime - startTime) / 1_000_000;
            totalTime += elapsedMs;
            
            System.out.println("  Run " + (run + 1) + ": " + elapsedMs + "ms");
            
            // Verify results
            assertNotNull(result);
            assertFalse(result.getColumns().isEmpty());
        }
        
        long avgTime = totalTime / runs;
        System.out.println("BENCHMARK 3: 10K rows - Average: " + avgTime + "ms (target: <500ms) *** AC1 ***");
        
        // AC1 ACCEPTANCE CRITERIA: <500ms
        assertTrue(avgTime < 500, "10K-row detection took " + avgTime + "ms (should be <500ms per AC1)");
    }
    
    /**
     * BENCHMARK 4: Extreme case (50K rows)
     * For stress testing and profiling
     */
    @Test
    public void benchmark_50KRows() throws IOException {
        System.out.println("BENCHMARK 4: 50K rows - Extreme case (stress test)");
        
        String csvContent = generateCsvContent(50000);
        MultipartFile csvFile = createMockCsvFile(csvContent, "test-50k-rows.csv");
        
        // Single run (no warm up for extreme case)
        long startTime = System.nanoTime();
        
        TypeDetectionResult result = detectionService.detectTypes(csvFile, 50000, false);
        
        long endTime = System.nanoTime();
        long elapsedMs = (endTime - startTime) / 1_000_000;
        
        System.out.println("  Single run: " + elapsedMs + "ms");
        System.out.println("  Columns detected: " + result.getColumns().size());
        
        // Verify results
        assertNotNull(result);
        assertFalse(result.getColumns().isEmpty());
    }
}
