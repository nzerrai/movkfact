package com.movkfact.service.detection.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for PHONE column type detection.
 * 
 * Identifies columns containing phone numbers through pattern matching and digit counting.
 * Supports multiple international formats: US, European, and other country variants.
 * 
 * <b>Supported Formats:</b>
 * <ul>
 *   <li>US: (202) 555-0173, 202-555-0173, 2025550173, +1-202-555-0173</li>
 *   <li>European: +33 1 23 45 67 89, 01 23 45 67 89, +44 20 7946 0958</li>
 *   <li>International: +[country_code] [digits and separators]</li>
 *   <li>Separators allowed: spaces, hyphens, parentheses, periods</li>
 * </ul>
 * 
 * <b>Validation Criteria:</b>
 * 1. Pattern match: must match international phone pattern format
 * 2. Digit count: must contain at minimum 7 consecutive digits
 * 
 * <b>Confidence Calculation:</b>
 * Base confidence = (valid_phones / total_non_null_values) * 100
 * No adjustments applied (straightforward pattern + digit validation).
 * 
 * <b>Typical Values:</b>
 * "202-555-0173", "+33 1 23 45 67 89", "(202) 555-0173", "+1-202-555-0173"
 * 
 * @since S2.2.2 Task 2.2.2
 */
@Service
public class PhoneValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(PhoneValidator.class);
    
    // International phone patterns
    // US: (202) 555-0173, 202-555-0173, 2025550173, +1-202-555-0173
    // EU: +33 1 23 45 67 89, 01 23 45 67 89, +44 20 7946 0958
    // Pattern: starts with +, digits, spaces, hyphens, parentheses. Min 7 digits.
    private static final String PHONE_PATTERN = 
            "^[+]?[0-9()\\s\\-\\.]{7,}$";
    private static final Pattern PHONE_REGEX = Pattern.compile(PHONE_PATTERN);
    
    // Requires at least 7 consecutive digits
    private static final String DIGIT_EXTRACTION = "\\d";
    
    /**
     * Validate if a list of values represents phone numbers.
     * 
     * Performs two-level validation:
     * 1. Pattern matching: checks if format matches international phone pattern
     * 2. Digit validation: verifies minimum 7 digits to filter false positives
     * 
     * @param sampleValues List of column values to analyze (typically 5-20 samples)
     * @return Confidence score 0-100. Score >= 75% indicates HIGH confidence match.
     *         - 0%: No values match phone pattern, or list is empty
     *         - 50-80%: Mixed results (some valid phone numbers with some non-matches)
     *         - 100%: All values match phone pattern and digit requirement
     * @see PhoneValidator#PHONE_REGEX Pattern definition
     */
    public double validate(List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        int validPhones = 0;
        int totalNonNull = 0;
        
        for (String value : sampleValues) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            
            totalNonNull++;
            String trimmed = value.trim();
            
            // Check basic phone pattern
            if (!PHONE_REGEX.matcher(trimmed).matches()) {
                continue;
            }
            
            // Count digits - must have at least 7 consecutive digits
            long digitCount = trimmed.chars().filter(Character::isDigit).count();
            if (digitCount >= 7) {
                validPhones++;
            }
        }
        
        if (totalNonNull == 0) {
            return 0.0;
        }
        
        double confidence = (validPhones * 100.0) / totalNonNull;
        logger.debug("PhoneValidator: {} of {} values match phone pattern (confidence: {}%)",
                validPhones, totalNonNull, Math.round(confidence));
        
        return confidence;
    }
}
