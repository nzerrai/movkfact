package com.movkfact.service.detection.temporal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validator for TIMEZONE column type detection.
 * 
 * Identifies columns containing timezone identifiers through pattern matching and
 * IANA timezone validation.
 * 
 * <b>Detection Criteria:</b>
 * <ul>
 *   <li>IANA timezone codes: Europe/Paris, America/New_York, Asia/Tokyo, etc.</li>
 *   <li>Short abbreviations: EST, UTC, GMT, PST, IST, CST, etc.</li>
 *   <li>UTC offsets: UTC+1, GMT-5, +05:00, -08:00</li>
 *   <li>Rejects: text, numbers, dates, invalid timezone identifiers</li>
 * </ul>
 * 
 * <b>Confidence Calculation:</b>
 * Base confidence = (valid_timezones / total_non_null_values) * 100
 * - Boost by 1.15x if all are IANA format (officially standardized)
 * - Boost by 1.08x if consistent and recognized by Java ZoneId
 * 
 * <b>Typical Values:</b>
 * "Europe/Paris", "America/New_York", "UTC", "EST", "GMT", "UTC+1", "+05:30"
 * 
 * @since S2.2.4 Task 2.2.4
 */
@Service
public class TimezoneValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(TimezoneValidator.class);
    
    // Cache of all available IANA timezone IDs (loaded once)
    private static final Set<String> VALID_ZONE_IDS = new HashSet<>();
    static {
        // Add all available zone IDs from Java
        VALID_ZONE_IDS.addAll(ZoneId.getAvailableZoneIds());
    }
    
    // Common short timezone abbreviations (not IANA standard but common)
    private static final Set<String> SHORT_ABBREVIATIONS = new HashSet<>();
    static {
        // UTC/GMT family
        SHORT_ABBREVIATIONS.add("UTC");
        SHORT_ABBREVIATIONS.add("GMT");
        SHORT_ABBREVIATIONS.add("UT");
        SHORT_ABBREVIATIONS.add("Z");
        
        // North American
        SHORT_ABBREVIATIONS.add("EST");
        SHORT_ABBREVIATIONS.add("EDT");
        SHORT_ABBREVIATIONS.add("CST");
        SHORT_ABBREVIATIONS.add("CDT");
        SHORT_ABBREVIATIONS.add("MST");
        SHORT_ABBREVIATIONS.add("MDT");
        SHORT_ABBREVIATIONS.add("PST");
        SHORT_ABBREVIATIONS.add("PDT");
        
        // European
        SHORT_ABBREVIATIONS.add("WET");
        SHORT_ABBREVIATIONS.add("CET");
        SHORT_ABBREVIATIONS.add("EET");
        SHORT_ABBREVIATIONS.add("IST");
        SHORT_ABBREVIATIONS.add("BST");
        SHORT_ABBREVIATIONS.add("WEST");
        SHORT_ABBREVIATIONS.add("CEST");
        SHORT_ABBREVIATIONS.add("EEST");
        
        // Asian
        SHORT_ABBREVIATIONS.add("JST");
        SHORT_ABBREVIATIONS.add("IST");
        SHORT_ABBREVIATIONS.add("SGT");
        SHORT_ABBREVIATIONS.add("HKT");
        SHORT_ABBREVIATIONS.add("AEST");
        SHORT_ABBREVIATIONS.add("ACST");
        SHORT_ABBREVIATIONS.add("AWST");
    }
    
    // UTC offset pattern: UTC±HH:MM, UTC±HHMM, ±HH:MM, ±HHMM, Z
    private static final String UTC_OFFSET_PATTERN = "^(UTC|GMT)([+-]\\d{1,2}:?\\d{0,2})?$";
    private static final Pattern UTC_OFFSET_REGEX = Pattern.compile(UTC_OFFSET_PATTERN, Pattern.CASE_INSENSITIVE);
    
    private static final String SIGNED_OFFSET_PATTERN = "^[+-]\\d{2}:?\\d{2}$";
    private static final Pattern SIGNED_OFFSET_REGEX = Pattern.compile(SIGNED_OFFSET_PATTERN);
    
    /**
     * Validate if a list of values represents timezone identifiers.
     * 
     * Performs comprehensive checks on the sample values:
     * 1. IANA timezone ID validation (official Java ZoneId list)
     * 2. Short abbreviation matching (common timezone codes)
     * 3. UTC offset format validation (UTC+1, -05:00, etc.)
     * 4. Format consistency analysis
     * 5. Statistical analysis: calculates confidence
     * 
     * @param sampleValues List of column values to analyze (typically 5-20 samples)
     * @return Confidence score 0-100. Score >= 75% is considered HIGH confidence match.
     *         - 0%: No values match timezone pattern, or list is empty
     *         - 30-70%: Mixed results or borderline data
     *         - 85-100%: Strong indication of timezone column
     */
    public double validate(List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        int validTimezones = 0;
        int totalNonNull = 0;
        int ianaFormatCount = 0;
        int offsetFormatCount = 0;
        
        for (String value : sampleValues) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            
            totalNonNull++;
            String trimmed = value.trim();
            
            // Try IANA timezone ID
            if (VALID_ZONE_IDS.contains(trimmed)) {
                validTimezones++;
                ianaFormatCount++;
                continue;
            }
            
            // Try short abbreviation (case-insensitive)
            String upperTrimmed = trimmed.toUpperCase();
            if (SHORT_ABBREVIATIONS.contains(upperTrimmed)) {
                validTimezones++;
                continue;
            }
            
            // Try UTC offset format
            if (UTC_OFFSET_REGEX.matcher(trimmed).matches()) {
                validTimezones++;
                offsetFormatCount++;
                continue;
            }
            
            // Try signed offset format
            if (SIGNED_OFFSET_REGEX.matcher(trimmed).matches()) {
                validTimezones++;
                offsetFormatCount++;
            }
        }
        
        if (totalNonNull == 0) {
            return 0.0;
        }
        
        double confidence = (validTimezones * 100.0) / totalNonNull;
        
        // Boost if IANA format (officially standardized)
        if (ianaFormatCount >= validTimezones * 0.6 && validTimezones > 0) {
            confidence = Math.min(100, confidence * 1.15);
        }
        
        // Boost if consistent format (suggests structured data)
        if (offsetFormatCount >= validTimezones * 0.7 || ianaFormatCount >= validTimezones * 0.7) {
            confidence = Math.min(100, confidence * 1.08);
        }
        
        logger.debug("TimezoneValidator: {} of {} values match timezone pattern (IANA:{}, Offset:{}). Final confidence: {}%",
                validTimezones, totalNonNull, ianaFormatCount, offsetFormatCount, Math.round(confidence));
        
        return confidence;
    }
}
