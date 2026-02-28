package com.movkfact.service.detection.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for FIRST_NAME column type detection.
 * 
 * Identifies columns containing first names through pattern matching and heuristic analysis.
 * Distinguishes first names from last names using average length and detection patterns.
 * 
 * <b>Detection Criteria:</b>
 * <ul>
 *   <li>Single-word values with letter characters only (plus accents, hyphens, apostrophes)</li>
 *   <li>Minimum 2 characters (rejects single letters like M/F to avoid false positives with gender)</li>
 *   <li>Typical length range: 3-9 characters (typical first name range)</li>
 *   <li>Rejects: full names (contains spaces), emails, numeric values</li>
 * </ul>
 * 
 * <b>Confidence Calculation:</b>
 * Base confidence = (valid_names / total_non_null_values) * 100
 * - Boost by 1.08x if average length is 3-9 chars (typical first name range)
 * - Reduce by 0.8x if average length > 12 chars (unusually long for first names)
 * 
 * <b>Typical Values:</b>
 * "Jean", "Marie", "John", "Sarah", "Michael", "Pierre" (French/English first names)
 * 
 * @since S2.2.2 Task 2.2.2
 */
@Service
public class FirstNameValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(FirstNameValidator.class);
    
    // Pattern: single word, starts with letter, 2-30 chars, allows accents
    private static final String FIRST_NAME_PATTERN = "^[A-Za-zÀ-ÿ]([A-Za-zÀ-ÿ\\-']*[A-Za-zÀ-ÿ])?$";
    private static final Pattern FIRST_NAME_REGEX = Pattern.compile(FIRST_NAME_PATTERN);
    
    // Reject patterns: full names, numbers, emails, special formats
    private static final String FULL_NAME_PATTERN = "^[A-Za-zÀ-ÿ]+ [A-Za-zÀ-ÿ]+";
    private static final Pattern FULL_NAME_REGEX = Pattern.compile(FULL_NAME_PATTERN);
    
    private static final String EMAIL_PATTERN = ".*@.*";
    private static final Pattern EMAIL_REGEX = Pattern.compile(EMAIL_PATTERN);
    
    private static final String NUMERIC_PATTERN = ".*\\d.*";
    private static final Pattern NUMERIC_REGEX = Pattern.compile(NUMERIC_PATTERN);
    
    /**
     * Validate if a list of values represents first names.
     * 
     * Performs comprehensive checks on the sample values:
     * 1. Pattern matching: single-word names with letters/accents/hyphens/apostrophes
     * 2. Length filtering: minimum 2 chars to avoid false positives
     * 3. Content filtering: rejects emails, numbers, full names
     * 4. Statistical analysis: calculates confidence based on average length
     * 
     * @param sampleValues List of column values to analyze (typically 5-20 samples)
     * @return Confidence score 0-100. Score >= 75% is considered HIGH confidence match.
     *         - 0%: No values match first name pattern, or list is empty
     *         - 30-70%: Mixed results or borderline data
     *         - 85-100%: Strong indication of first name column
     * @see FirstNameValidator#FIRST_NAME_REGEX Pattern definition
     */
    public double validate(List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        int validNames = 0;
        int totalNonNull = 0;
        double avgLength = 0;
        
        for (String value : sampleValues) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            
            totalNonNull++;
            String trimmed = value.trim();
            avgLength += trimmed.length();
            
            // Reject if contains email pattern
            if (EMAIL_REGEX.matcher(trimmed).matches()) {
                continue;
            }
            
            // Reject if contains number
            if (NUMERIC_REGEX.matcher(trimmed).matches()) {
                continue;
            }
            
            // Reject if full name (contains space and multiple parts)
            if (FULL_NAME_REGEX.matcher(trimmed).matches()) {
                continue;
            }
            
            // Check single-word first name pattern (minimum 2 chars to distinguish from single letters like M/F)
            if (trimmed.length() >= 2 && FIRST_NAME_REGEX.matcher(trimmed).matches()) {
                validNames++;
            }
        }
        
        if (totalNonNull == 0) {
            return 0.0;
        }
        
        avgLength = avgLength / totalNonNull;
        double confidence = (validNames * 100.0) / totalNonNull;
        
        // First names: typically 3-9 chars
        // Boost moderately for typical range, penalize if too long
        if (avgLength >= 3 && avgLength <= 9) {
            confidence = Math.min(100, confidence * 1.08);  // Light boost for first name range
        } else if (avgLength > 12) {
            confidence = confidence * 0.8;  // Penalize if very long
        }
        
        logger.debug("FirstNameValidator: {} of {} values match first name pattern, avg length {}. Final confidence: {}%",
                validNames, totalNonNull, Math.round(avgLength), Math.round(confidence));
        
        return confidence;
    }
}
