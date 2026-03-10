package com.movkfact.service.detection;

import com.movkfact.enums.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Service for value-based type detection.
 * Analyzes sample values to confirm or detect column types.
 * Validates against known formats (email, numeric, dates, timezones, etc.).
 */
@Service
public class ColumnValueAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(ColumnValueAnalyzer.class);
    
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String NUMERIC_REGEX = "^-?\\d+(\\.\\d+)?$";
    private static final String ACCOUNT_NUMBER_REGEX = "^\\d{4}[-]?\\d{4}[-]?\\d{4}[-]?\\d{4}$";
    private static final String TIMEZONE_PATTERN = "^[A-Za-z_]+(/[A-Za-z_]+)?$|^[A-Z]{3,4}$";
    private static final String ISO_8601_DATE = "^\\d{4}-\\d{2}-\\d{2}";
    private static final String EU_DATE = "^\\d{2}/\\d{2}/\\d{4}$";
    private static final String US_DATE = "^\\d{2}-\\d{2}-\\d{4}$";
    
    /**
     * Analyze sample values to detect column type.
     * Validates format patterns and returns highest confidence type.
     * 
     * @param columnName The column header name (for logging)
     * @param sampleValues Sample data values from the column
     * @return Detected ColumnType, or null if confidence is too low
     */
    public ColumnType analyzeValues(String columnName, List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            logger.debug("ColumnValueAnalyzer: No sample values for column '{}'", columnName);
            return null;
        }
        
        // Filter out nulls and empty strings for analysis
        List<String> validValues = new ArrayList<>();
        for (String value : sampleValues) {
            if (value != null && !value.trim().isEmpty()) {
                validValues.add(value.trim());
            }
        }
        
        if (validValues.isEmpty()) {
            logger.debug("ColumnValueAnalyzer: No valid values found for column '{}'", columnName);
            return null;
        }
        
        // Test against different patterns
        double emailScore = testEmailFormat(validValues);
        double numericScore = testNumericFormat(validValues);
        double accountScore = testAccountNumberFormat(validValues);
        double dateScore = testDateFormat(validValues);
        double timezoneScore = testTimezoneFormat(validValues);
        
        // Find highest score
        Map<ColumnType, Double> scores = new HashMap<>();
        scores.put(ColumnType.EMAIL, emailScore);
        scores.put(ColumnType.AMOUNT, numericScore);
        scores.put(ColumnType.ACCOUNT_NUMBER, accountScore);
        
        // For dates: check column name to distinguish DATE vs BIRTH_DATE
        if (dateScore > 0) {
            String col = columnName == null ? "" : columnName.toLowerCase();
            if (col.contains("birth") || col.contains("naiss") || col.contains("dob")) {
                scores.put(ColumnType.BIRTH_DATE, dateScore);
            } else {
                scores.put(ColumnType.DATE, dateScore);
            }
        }
        
        scores.put(ColumnType.TIMEZONE, timezoneScore);
        
        ColumnType bestType = scores.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
        
        double bestScore = scores.getOrDefault(bestType, 0.0);
        
        if (bestScore >= 80.0) {
            logger.debug("ColumnValueAnalyzer: Column '{}' detected as {} with confidence {}%",
                    columnName, bestType, Math.round(bestScore));
            return bestType;
        }
        
        logger.debug("ColumnValueAnalyzer: Column '{}' inconclusive (best: {} at {}%)",
                columnName, bestType, Math.round(bestScore));
        return null;
    }
    
    private double testEmailFormat(List<String> values) {
        Pattern emailPattern = Pattern.compile(EMAIL_REGEX);
        long matches = values.stream()
                .filter(v -> emailPattern.matcher(v).matches())
                .count();
        return (matches * 100.0) / values.size();
    }
    
    private double testNumericFormat(List<String> values) {
        Pattern numericPattern = Pattern.compile(NUMERIC_REGEX);
        long matches = values.stream()
                .filter(v -> numericPattern.matcher(v).matches())
                .count();
        return (matches * 100.0) / values.size();
    }
    
    private double testAccountNumberFormat(List<String> values) {
        Pattern accountPattern = Pattern.compile(ACCOUNT_NUMBER_REGEX);
        long matches = values.stream()
                .filter(v -> accountPattern.matcher(v).matches())
                .count();
        return (matches * 100.0) / values.size();
    }
    
    private double testDateFormat(List<String> values) {
        Pattern iso8601 = Pattern.compile(ISO_8601_DATE);
        Pattern euDate = Pattern.compile(EU_DATE);
        Pattern usDate = Pattern.compile(US_DATE);
        
        long matches = values.stream()
                .filter(v -> iso8601.matcher(v).matches() || 
                           euDate.matcher(v).matches() || 
                           usDate.matcher(v).matches())
                .count();
        return (matches * 100.0) / values.size();
    }
    
    private double testTimezoneFormat(List<String> values) {
        Pattern tzPattern = Pattern.compile(TIMEZONE_PATTERN);
        long matches = values.stream()
                .filter(v -> tzPattern.matcher(v).matches())
                .count();
        return (matches * 100.0) / values.size();
    }
}
