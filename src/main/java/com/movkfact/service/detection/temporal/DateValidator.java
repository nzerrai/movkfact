package com.movkfact.service.detection.temporal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for DATE column type detection.
 * 
 * Identifies columns containing general dates (created_at, modified_at, etc.)
 * through pattern matching and date parsing. Less restrictive than BirthDateValidator.
 * 
 * <b>Detection Criteria:</b>
 * <ul>
 *   <li>Date formats: ISO 8601, DD/MM/YYYY, MM-DD-YYYY, timestamps with time</li>
 *   <li>Timestamps: Can include time component (HH:MM:SS)</li>
 *   <li>Reasonable range: 1970-2099 (covers all modern data)</li>
 *   <li>Rejects: text, numbers, invalid date objects</li>
 * </ul>
 * 
 * <b>Confidence Calculation:</b>
 * Base confidence = (valid_dates / total_non_null_values) * 100
 * - Boost by 1.10x if all values are ISO format (standardized)
 * - Boost by 1.05x if include timestamp component
 * - Slight penalty if all future dates (unusual for typical created_at)
 * 
 * <b>Typical Values:</b>
 * "2025-01-15", "2025-01-15T10:30:00", "15/01/2025", "2024-12-25 14:30:15"
 * 
 * @since S2.2.4 Task 2.2.4
 */
@Service
public class DateValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(DateValidator.class);
    
    // Common date/datetime patterns
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ISO_DATE,                          // YYYY-MM-DD
        DateTimeFormatter.ISO_DATE_TIME,                     // YYYY-MM-DDTHH:MM:SS (ISO)
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),           // DD/MM/YYYY
        DateTimeFormatter.ofPattern("MM-dd-yyyy"),           // MM-DD-YYYY
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),           // DD-MM-YYYY
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),           // YYYY/MM/DD
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),           // DD.MM.YYYY
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),  // YYYY-MM-DD HH:MM:SS
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),  // DD/MM/YYYY HH:MM:SS
        DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"),  // MM-DD-YYYY HH:MM:SS
    };
    
    private static final String DATE_LIKE_PATTERN = "^\\d{1,4}[-/T.]\\d{1,2}[-/.]\\d{1,4}";
    private static final Pattern DATE_REGEX = Pattern.compile(DATE_LIKE_PATTERN, Pattern.CASE_INSENSITIVE);
    
    /**
     * Validate if a list of values represents general dates.
     * 
     * Performs comprehensive checks on the sample values:
     * 1. Pattern matching: date-like format detection
     * 2. Date parsing: tries multiple common date formats (with or without time)
     * 3. Range validation: dates between 1970-2099 (reasonable modern range)
     * 4. Format consistency analysis
     * 5. Statistical analysis: calculates confidence
     * 
     * @param sampleValues List of column values to analyze (typically 5-20 samples)
     * @return Confidence score 0-100. Score >= 75% is considered HIGH confidence match.
     *         - 0%: No values parse as dates, or list is empty
     *         - 30-70%: Mixed results or ambiguous dates
     *         - 85-100%: Strong indication of date column
     */
    public double validate(List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        int validDates = 0;
        int totalNonNull = 0;
        int isoFormatCount = 0;
        int timestampCount = 0;
        LocalDate minReasonable = LocalDate.of(1970, 1, 1);
        LocalDate maxReasonable = LocalDate.of(2099, 12, 31);
        
        for (String value : sampleValues) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            
            totalNonNull++;
            String trimmed = value.trim();
            
            // Quick pattern check: must look like a date
            if (!DATE_REGEX.matcher(trimmed).find()) {
                continue;
            }
            
            // Try to parse with each formatter
            LocalDate parsedDate = tryParseDate(trimmed);
            if (parsedDate != null) {
                // Check reasonable range (1970-2099)
                if (parsedDate.isAfter(minReasonable.minusDays(1)) && parsedDate.isBefore(maxReasonable.plusDays(1))) {
                    validDates++;
                    
                    // Check if ISO format (standardized)
                    if (trimmed.matches("^\\d{4}-\\d{2}-\\d{2}.*")) {
                        isoFormatCount++;
                    }
                    
                    // Check if includes timestamp (time component)
                    if (trimmed.contains(":") || trimmed.contains("T")) {
                        timestampCount++;
                    }
                }
            }
        }
        
        if (totalNonNull == 0) {
            return 0.0;
        }
        
        double confidence = (validDates * 100.0) / totalNonNull;
        
        // Boost if ISO format (standardized)
        if (isoFormatCount >= validDates * 0.7 && validDates > 0) {
            confidence = Math.min(100, confidence * 1.10);
        }
        
        // Boost if includes timestamps (typical for created_at/modified_at)
        if (timestampCount >= validDates * 0.5 && validDates > 0) {
            confidence = Math.min(100, confidence * 1.05);
        }
        
        logger.debug("DateValidator: {} of {} values parse as dates, {} ISO format, {} with timestamp. Final confidence: {}%",
                validDates, totalNonNull, isoFormatCount, timestampCount, Math.round(confidence));
        
        return confidence;
    }
    
    /**
     * Try to parse a string as a date or datetime using multiple formatters.
     * 
     * @param dateString String to parse
     * @return Parsed LocalDate, or null if parsing fails with all formatters
     */
    private LocalDate tryParseDate(String dateString) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                // Try to parse as LocalDateTime first (to handle timestamps)
                LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
                return dateTime.toLocalDate();
            } catch (DateTimeParseException e1) {
                try {
                    // If that fails, try as LocalDate
                    return LocalDate.parse(dateString, formatter);
                } catch (DateTimeParseException e2) {
                    // Try next formatter
                }
            }
        }
        return null;
    }
}
