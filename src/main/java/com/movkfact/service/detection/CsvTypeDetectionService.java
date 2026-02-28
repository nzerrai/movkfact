package com.movkfact.service.detection;

import com.movkfact.dto.TypeDetectionResult;
import com.movkfact.dto.DetectedColumn;
import com.movkfact.enums.ColumnType;
import com.movkfact.service.detection.personal.PersonalTypeDetector;
import com.movkfact.service.detection.financial.FinancialTypeDetector;
import com.movkfact.service.detection.temporal.TemporalTypeDetector;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service for CSV type detection.
 * Orchestrates pattern-based and value-based detection.
 * Returns TypeDetectionResult with confidence scores.
 * 
 * Pattern: Strategy pattern - combines ColumnPatternDetector + ColumnValueAnalyzer
 * Future: Ready to swap for ML-based detection (detectionMethod = "ml_based")
 */
@Service
public class CsvTypeDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvTypeDetectionService.class);
    
    private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int DEFAULT_SAMPLE_SIZE = 100;
    
    @Autowired
    private ColumnPatternDetector patternDetector;
    
    @Autowired
    private ColumnValueAnalyzer valueAnalyzer;
    
    @Autowired
    private PersonalTypeDetector personalTypeDetector;
    
    @Autowired
    private FinancialTypeDetector financialTypeDetector;
    
    @Autowired
    private TemporalTypeDetector temporalTypeDetector;
    
    @Value("${detection.max-file-size:#{10*1024*1024}}")
    private long maxFileSize;
    
    /**
     * Detect column types from CSV file.
     * Strategy:
     * 1. Parse CSV with charset detection (UTF-8, fallback to ISO-8859-1)
     * 2. For each column header:
     *    - Pattern matching on header name
     *    - Value analysis on sample rows
     *    - Merge confidence scores
     * 3. Return TypeDetectionResult with detected types and confidence
     * 
     * @param csvFile The uploaded CSV file
     * @param sampleSize Number of rows to analyze (default: 100)
     * @return TypeDetectionResult with detected columns and metadata
     */
    public TypeDetectionResult detectTypes(MultipartFile csvFile, Integer sampleSize) {
        logger.info("CsvTypeDetectionService: Starting detection for file: {}", 
                csvFile.getOriginalFilename());
        
        if (sampleSize == null || sampleSize <= 0) {
            sampleSize = DEFAULT_SAMPLE_SIZE;
        }
        
        try {
            // Validate file size
            if (csvFile.getSize() > maxFileSize) {
                logger.error("File size {} exceeds maximum allowed {}", 
                        csvFile.getSize(), maxFileSize);
                throw new IllegalArgumentException("File size exceeds maximum allowed size (10MB)");
            }
            
            // Read CSV content with charset detection
            byte[] fileContent = csvFile.getBytes();
            String csvContent = new String(fileContent, detectCharset(fileContent));
            
            // Parse CSV
            List<DetectedColumn> detectedColumns = new ArrayList<>();
            
            try (CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader()
                    .parse(new StringReader(csvContent))) {
                
                // Get headers
                List<String> headers = new ArrayList<>(csvParser.getHeaderMap().keySet());
                logger.info("CsvTypeDetectionService: Found {} columns", headers.size());
                
                // Get sample values for each column
                Map<String, List<String>> columnValues = new HashMap<>();
                for (String header : headers) {
                    columnValues.put(header, new ArrayList<>());
                }
                
                // Collect sample values
                int rowCount = 0;
                for (CSVRecord record : csvParser) {
                    if (rowCount >= sampleSize) {
                        break;
                    }
                    for (String header : headers) {
                        String value = record.get(header);
                        columnValues.get(header).add(value);
                    }
                    rowCount++;
                }
                
                // Detect type for each column
                for (String header : headers) {
                    List<String> columnData = columnValues.get(header);
                    
                    // Try specialized detectors in order of specificity
                    ColumnType detectedType = null;
                    double finalConfidence = 0.0;
                    
                    // 1. Try Personal Type Detection (6 types: first_name, last_name, email, gender, phone, address)
                    // Note: If detector returns non-null, it has already validated >= 75% confidence internally.
                    // Service confidence is set to 85.0 to indicate validator-approved match (higher than 75% threshold).
                    if (personalTypeDetector != null) {
                        detectedType = personalTypeDetector.detect(header, columnData);
                        if (detectedType != null) {
                            finalConfidence = 85.0; // Validator-approved: passed internal threshold check
                            logger.debug("CsvTypeDetectionService: Column '{}' → {} (via PersonalTypeDetector, validator-approved)",
                                    header, detectedType.name());
                        }
                    }
                    
                    // 2. If not personal, try Financial Type Detection (3 types: amount, account_number, currency)
                    if (detectedType == null && financialTypeDetector != null) {
                        detectedType = financialTypeDetector.detect(header, columnData);
                        if (detectedType != null) {
                            finalConfidence = 85.0; // Validator-approved: passed internal threshold check
                            logger.debug("CsvTypeDetectionService: Column '{}' → {} (via FinancialTypeDetector, validator-approved)",
                                    header, detectedType.name());
                        }
                    }
                    
                    // 3. If not financial, try Temporal Type Detection (4 types: birth_date, date, time, timezone)
                    if (detectedType == null && temporalTypeDetector != null) {
                        detectedType = temporalTypeDetector.detect(header, columnData);
                        if (detectedType != null) {
                            finalConfidence = 85.0; // Validator-approved: passed internal threshold check
                            logger.debug("CsvTypeDetectionService: Column '{}' → {} (via TemporalTypeDetector, validator-approved)",
                                    header, detectedType.name());
                        }
                    }
                    
                    // 4. Fallback to basic pattern detector if specialized detectors found nothing
                    if (detectedType == null) {
                        Map<ColumnType, Integer> patternMatches = patternDetector.matchPatterns(header);
                        if (!patternMatches.isEmpty()) {
                            detectedType = patternMatches.entrySet().stream()
                                    .max(Comparator.comparingInt(Map.Entry::getValue))
                                    .map(Map.Entry::getKey)
                                    .orElse(null);
                            finalConfidence = (double) patternMatches.getOrDefault(detectedType, 0);
                        }
                        
                        if (detectedType != null) {
                            logger.debug("CsvTypeDetectionService: Column '{}' → {} (via PatternDetector, confidence: {}%)",
                                    header, detectedType.name(), Math.round(finalConfidence));
                        }
                    }
                    
                    // Create DetectedColumn DTO
                    if (detectedType != null && finalConfidence >= 75.0) {
                        DetectedColumn detected = new DetectedColumn(
                                header,
                                detectedType,
                                finalConfidence,
                                new ArrayList<>(),  // alternatives
                                Arrays.asList(header)  // matchedPatterns
                        );
                        detectedColumns.add(detected);
                        logger.debug("CsvTypeDetectionService: Column '{}' → {} (confidence: {}%)",
                                header, detectedType.name(), Math.round(finalConfidence));
                    } else {
                        // Unknown or low confidence type
                        DetectedColumn detected = new DetectedColumn(
                                header,
                                null,
                                0.0,
                                new ArrayList<>(),
                                new ArrayList<>()
                        );
                        detectedColumns.add(detected);
                        logger.debug("CsvTypeDetectionService: Column '{}' → UNKNOWN (confidence too low)", header);
                    }
                }
            }
            
            TypeDetectionResult result = new TypeDetectionResult(
                    detectedColumns,
                    "pattern_based" // Current method: pattern-based. Future: can be "ml_based"
            );
            
            logger.info("CsvTypeDetectionService: Detection complete - {} columns detected", 
                    detectedColumns.size());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error detecting types from CSV", e);
            throw new RuntimeException("Failed to detect types: " + e.getMessage(), e);
        }
    }
    
    /**
     * Detect charset of byte array.
     * Tries UTF-8 first, then ISO-8859-1 as fallback.
     */
    private Charset detectCharset(byte[] fileContent) {
        // Check for UTF-8 BOM
        if (fileContent.length >= 3 &&
                fileContent[0] == (byte) 0xEF &&
                fileContent[1] == (byte) 0xBB &&
                fileContent[2] == (byte) 0xBF) {
            return StandardCharsets.UTF_8;
        }
        
        // Try UTF-8
        try {
            new String(fileContent, StandardCharsets.UTF_8);
            return StandardCharsets.UTF_8;
        } catch (Exception e) {
            logger.debug("Not valid UTF-8, trying ISO-8859-1");
        }
        
        // Fallback to ISO-8859-1
        return StandardCharsets.ISO_8859_1;
    }
}
