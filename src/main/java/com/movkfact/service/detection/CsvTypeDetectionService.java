package com.movkfact.service.detection;

import com.movkfact.dto.InferenceResult;
import com.movkfact.dto.PiiResult;
import com.movkfact.dto.TypeDetectionResult;
import com.movkfact.dto.DetectedColumn;
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
    
    private static final int DEFAULT_SAMPLE_SIZE = 100;
    
    @Autowired
    private ColumnTypeInferenceService inferenceService;

    @Autowired
    private PiiDetectionService piiDetectionService;
    
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
    public TypeDetectionResult detectTypes(MultipartFile csvFile, Integer sampleSize, boolean noHeader) {
        logger.info("CsvTypeDetectionService: Starting detection for file: {} (noHeader={})",
                csvFile.getOriginalFilename(), noHeader);

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

            if (noHeader) {
                // No-header mode: parse as arrays, generate col_1, col_2, … names
                try (CSVParser csvParser = CSVFormat.DEFAULT.parse(new StringReader(csvContent))) {
                    List<CSVRecord> records = csvParser.getRecords();
                    if (records.isEmpty()) throw new IllegalArgumentException("CSV file is empty");
                    int colCount = records.get(0).size();
                    List<String> headers = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) headers.add("col_" + i);
                    logger.info("CsvTypeDetectionService: No-header mode — {} columns", colCount);

                    Map<String, List<String>> columnValues = new HashMap<>();
                    for (String h : headers) columnValues.put(h, new ArrayList<>());

                    int rowCount = 0;
                    for (CSVRecord record : records) {
                        if (rowCount >= sampleSize) break;
                        for (int i = 0; i < headers.size(); i++) {
                            columnValues.get(headers.get(i)).add(record.get(i));
                        }
                        rowCount++;
                    }

                    for (String header : headers) {
                        List<String> columnData = columnValues.get(header);
                        InferenceResult inference = inferenceService.infer(header, columnData);
                        PiiResult pii = piiDetectionService.detect(header, columnData);
                        DetectedColumn detected = new DetectedColumn(
                                header, inference.getType(), inference.getConfidence(),
                                new ArrayList<>(),
                                inference.getType() != null ? Arrays.asList(header) : new ArrayList<>()
                        );
                        detected.setInferenceLevel(inference.getLevel());
                        detected.setLearnedCount(inference.getLearnedCount());
                        detected.setPII(pii.isPii());
                        detected.setPiiCategory(pii.getCategory());
                        detectedColumns.add(detected);
                    }
                }
            } else {
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

                // Detect type for each column using ColumnTypeInferenceService (S9.1)
                for (String header : headers) {
                    List<String> columnData = columnValues.get(header);

                    InferenceResult inference = inferenceService.infer(header, columnData);
                    PiiResult pii = piiDetectionService.detect(header, columnData);

                    DetectedColumn detected = new DetectedColumn(
                            header,
                            inference.getType(),
                            inference.getConfidence(),
                            new ArrayList<>(),
                            inference.getType() != null ? Arrays.asList(header) : new ArrayList<>()
                    );
                    detected.setInferenceLevel(inference.getLevel());
                    detected.setLearnedCount(inference.getLearnedCount());
                    detected.setPII(pii.isPii());
                    detected.setPiiCategory(pii.getCategory());
                    detectedColumns.add(detected);

                    if (inference.getType() != null) {
                        logger.debug("CsvTypeDetectionService: Column '{}' → {} ({}, conf={}%)",
                                header, inference.getType().name(), inference.getLevel(),
                                Math.round(inference.getConfidence()));
                    } else {
                        logger.debug("CsvTypeDetectionService: Column '{}' → UNKNOWN", header);
                    }
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
