package com.movkfact.service.detection.temporal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for BIRTH_DATE column type detection.
 * 
 * Identifies columns containing birth dates through pattern matching and date parsing.
 * Supports multiple date formats commonly used for birth dates.
 * 
 * <b>Detection Criteria:</b>
 * <ul>
 *   <li>Date formats: ISO 8601 (YYYY-MM-DD), DD/MM/YYYY, MM-DD-YYYY, DD-MM-YYYY</li>
 *   <li>Typical range: past dates (not future dates - suggests historical data)</li>
 *   <li>Age range validation: 0-150 years old (realistic for living/deceased persons)</li>
 *   <li>Rejects: future dates, text, numbers, invalid date objects</li>
 * </ul>
 * 
 * <b>Confidence Calculation:</b>
 * Base confidence = (valid_dates / total_non_null_values) * 100
 * - Boost by 1.12x if all dates are in past (historical data)
 * - Boost by 1.08x if date range 0-120 years (typical life span)
 * - Penalize if future dates detected (0.7x)
 * 
 * <b>Typical Values:</b>
 * "1980-05-15", "15/05/1980", "05-15-1980", "1995-12-31"
 * 
 * @since S2.2.4 Task 2.2.4
 */
@Service
public class BirthDateValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(BirthDateValidator.class);
    
    // Common date patterns for birth dates
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_DATE,                    // YYYY-MM-DD
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),     // DD/MM/YYYY
        DateTimeFormatter.ofPattern("MM-dd-yyyy"),     // MM-DD-YYYY
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),     // DD-MM-YYYY
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),     // YYYY/MM/DD (variant)
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),     // DD.MM.YYYY (European)
        DateTimeFormatter.ofPattern("dd-MMM-yyyy"),    // 01-Jan-1980
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), // With time (ISO)
    };
    
    private static final String DATE_LIKE_PATTERN = "^\\d{1,4}[-/.]\\d{1,2}[-/.]\\d{1,4}";
    private static final Pattern DATE_REGEX = Pattern.compile(DATE_LIKE_PATTERN);
    
    /**
     * Validate if a list of values represents birth dates.
     * 
     * Performs comprehensive checks on the sample values:
     * 1. Pattern matching: date-like format detection
     * 2. Date parsing: tries multiple common date formats
     * 3. Range validation: ages between 0-150 years (realistic lifespan)
     * 4. Historical validation: birth dates typically in the past
     * 5. Statistical analysis: calculates confidence based on age distribution
     * 
     * @param sampleValues List of column values to analyze (typically 5-20 samples)
     * @return Confidence score 0-100. Score >= 75% is considered HIGH confidence match.
     *         - 0%: No values parse as dates, or list is empty
     *         - 30-70%: Mixed results or ambiguous dates
     *         - 85-100%: Strong indication of birth date column
     */
    public double validate(List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        int validDates = 0;
        int totalNonNull = 0;
        int pastDates = 0;
        int reasonableAgeDates = 0;
        LocalDate now = LocalDate.now();
        LocalDate minReasonableDate = now.minusYears(150); // 150 years ago
        LocalDate maxReasonableDate = now.minusYears(0);   // Today (no future births)
        
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
                validDates++;
                
                // Check if date is in past (typical for birth dates)
                if (parsedDate.isBefore(now)) {
                    pastDates++;
                }
                
                // Check if age is reasonable (0-150 years)
                if (parsedDate.isAfter(minReasonableDate) && parsedDate.isBefore(maxReasonableDate.plusDays(1))) {
                    reasonableAgeDates++;
                }
            }
        }
        
        if (totalNonNull == 0) {
            return 0.0;
        }
        
        double confidence = (validDates * 100.0) / totalNonNull;
        
        // Penalize heavily if future dates detected (unrealistic for birth dates)
        int futureDates = validDates - pastDates;
        if (futureDates > 0) {
            double futureRatio = (futureDates * 1.0) / validDates;
            confidence = confidence * (1.0 - futureRatio * 0.9);  // Strong penalty for future dates
        }
        
        // Boost if all dates are past (typical for birth dates)
        if (pastDates == validDates && validDates > 0) {
            confidence = Math.min(100, confidence * 1.12);
        }
        
        // Boost if ages are in reasonable range (0-120 years)
        if (reasonableAgeDates >= validDates * 0.8 && validDates > 0) {
            confidence = Math.min(100, confidence * 1.08);
        }
        
        logger.debug("BirthDateValidator: {} of {} values parse as dates, {} in past, {} reasonable age. Final confidence: {}%",
                validDates, totalNonNull, pastDates, reasonableAgeDates, Math.round(confidence));
        
        return confidence;
    }
    
    /**
     * Try to parse a string as a date using multiple formatters.
     * 
     * @param dateString String to parse
     * @return Parsed LocalDate, or null if parsing fails with all formatters
     */
    private LocalDate tryParseDate(String dateString) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                // Try to parse as LocalDate
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        return null;
    }
}
