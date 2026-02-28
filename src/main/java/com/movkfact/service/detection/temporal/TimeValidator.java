package com.movkfact.service.detection.temporal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for TIME column type detection.
 * 
 * Identifies columns containing time values through pattern matching and time parsing.
 * Supports multiple time formats (HH:MM:SS, HH:MM, etc.).
 * 
 * <b>Detection Criteria:</b>
 * <ul>
 *   <li>Time formats: HH:MM:SS, HH:MM, HH:MM:ss.SSS (with milliseconds)</li>
 *   <li>24-hour format: Hours 0-23, Minutes 0-59, Seconds 0-59</li>
 *   <li>Rejects: dates, text, numbers, invalid time objects</li>
 * </ul>
 * 
 * <b>Confidence Calculation:</b>
 * Base confidence = (valid_times / total_non_null_values) * 100
 * - Boost by 1.15x if all times are HH:MM:SS format (standardized)
 * - Boost by 1.10x if includes seconds component (more specific)
 * 
 * <b>Typical Values:</b>
 * "10:30:00", "14:45:30", "23:59:59", "00:00:00", "10:30"
 * 
 * @since S2.2.4 Task 2.2.4
 */
@Service
public class TimeValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(TimeValidator.class);
    
    // Common time patterns
    private static final DateTimeFormatter[] TIME_FORMATTERS = {
        DateTimeFormatter.ISO_TIME,                      // HH:MM:SS or HH:MM:SS.SSS
        DateTimeFormatter.ofPattern("HH:mm:ss"),         // HH:MM:SS (24-hour)
        DateTimeFormatter.ofPattern("HH:mm"),            // HH:MM
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS"),     // HH:MM:SS.sss (with millis)
        DateTimeFormatter.ofPattern("hh:mm:ss a"),       // 12-hour format (rare)
        DateTimeFormatter.ofPattern("hh:mm a"),          // 12-hour format HH:MM (rare)
    };
    
    // Time pattern: HH:MM or HH:MM:SS or HH:MM:SS.sss
    private static final String TIME_LIKE_PATTERN = "^\\d{1,2}:\\d{2}(:\\d{2})?(\\.\\d{1,3})?$";
    private static final Pattern TIME_REGEX = Pattern.compile(TIME_LIKE_PATTERN);
    
    /**
     * Validate if a list of values represents time values.
     * 
     * Performs comprehensive checks on the sample values:
     * 1. Pattern matching: time-like format detection (HH:MM:SS)
     * 2. Time parsing: tries multiple common time formats
     * 3. Range validation: valid hour/minute/second values
     * 4. Format consistency analysis
     * 5. Statistical analysis: calculates confidence
     * 
     * @param sampleValues List of column values to analyze (typically 5-20 samples)
     * @return Confidence score 0-100. Score >= 75% is considered HIGH confidence match.
     *         - 0%: No values parse as times, or list is empty
     *         - 30-70%: Mixed results or ambiguous times
     *         - 85-100%: Strong indication of time column
     */
    public double validate(List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        int validTimes = 0;
        int totalNonNull = 0;
        int fullFormatCount = 0;  // HH:MM:SS
        int secondsCount = 0;      // With seconds component
        
        for (String value : sampleValues) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            
            totalNonNull++;
            String trimmed = value.trim();
            
            // Quick pattern check: must look like a time
            if (!TIME_REGEX.matcher(trimmed).find()) {
                continue;
            }
            
            // Try to parse with each formatter
            LocalTime parsedTime = tryParseTime(trimmed);
            if (parsedTime != null) {
                validTimes++;
                
                // Check if full HH:MM:SS format
                if (trimmed.matches("^\\d{2}:\\d{2}:\\d{2}.*")) {
                    fullFormatCount++;
                    secondsCount++;
                } else if (trimmed.contains(":") && trimmed.split(":").length >= 2) {
                    // Has at least hours and minutes
                    if (trimmed.split(":").length >= 3) {
                        secondsCount++;
                    }
                }
            }
        }
        
        if (totalNonNull == 0) {
            return 0.0;
        }
        
        double confidence = (validTimes * 100.0) / totalNonNull;
        
        // Boost if full HH:MM:SS format (standardized)
        if (fullFormatCount >= validTimes * 0.8 && validTimes > 0) {
            confidence = Math.min(100, confidence * 1.15);
        }
        
        // Boost if includes seconds (more specific)
        if (secondsCount >= validTimes * 0.7 && validTimes > 0) {
            confidence = Math.min(100, confidence * 1.10);
        }
        
        logger.debug("TimeValidator: {} of {} values parse as times, {} full format, {} with seconds. Final confidence: {}%",
                validTimes, totalNonNull, fullFormatCount, secondsCount, Math.round(confidence));
        
        return confidence;
    }
    
    /**
     * Try to parse a string as a time using multiple formatters.
     * 
     * @param timeString String to parse
     * @return Parsed LocalTime, or null if parsing fails with all formatters
     */
    private LocalTime tryParseTime(String timeString) {
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalTime.parse(timeString, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        return null;
    }
}
