package com.movkfact.service.detection.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for LAST_NAME column type detection.
 * 
 * Identifies columns containing last names through pattern matching and heuristic analysis.
 * Distinguishes last names from first names using average length and special patterns.
 * 
 * <b>Detection Criteria:</b>
 * <ul>
 *   <li>Single-word or hyphenated values with letter characters (plus accents)</li>
 *   <li>Typical length range: 6-15 characters (typical last name range)</li>
 *   <li>Common patterns: hyphenated names (e.g., Smith-Jones) are boosted</li>
 *   <li>Rejects: full names (contains spaces), emails, numeric values</li>
 * </ul>
 * 
 * <b>Confidence Calculation:</b>
 * Base confidence = (valid_names / total_non_null_values) * 100
 * - Boost by 1.15x if average length is 6-15 chars (typical last name range)
 * - Reduce by 0.35x if average length &lt; 6 chars (more typical of first names)
 * - Additional boost by 1.1x if hyphenated names detected (25%+ of values)
 * 
 * <b>Typical Values:</b>
 * "Smith", "Johnson", "Martin", "Dupont", "Garcia", "Smith-Jones" (French/English last names)
 * 
 * @since S2.2.2 Task 2.2.2
 */
@Service
public class LastNameValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(LastNameValidator.class);
    
    // Pattern: single or hyphenated words, starts with letter, allows accents
    private static final String LAST_NAME_PATTERN = "^[A-Za-zÀ-ÿ]([A-Za-zÀ-ÿ\\-']*[A-Za-zÀ-ÿ])?$";
    private static final Pattern LAST_NAME_REGEX = Pattern.compile(LAST_NAME_PATTERN);
    
    // Reject patterns: numbers, emails, too short (likely not a name)
    private static final String EMAIL_PATTERN = ".*@.*";
    private static final Pattern EMAIL_REGEX = Pattern.compile(EMAIL_PATTERN);
    
    private static final String NUMERIC_PATTERN = ".*\\d.*";
    private static final Pattern NUMERIC_REGEX = Pattern.compile(NUMERIC_PATTERN);
    
    /**
     * Validate if a list of values represents last names.
     * 
     * Performs comprehensive checks on the sample values:
     * 1. Pattern matching: single or hyphenated names with letters/accents
     * 2. Length analysis: checks if average length indicates last name (6+ chars)
     * 3. Content filtering: rejects emails, numbers, full names
     * 4. Hyphenation detection: boosts confidence if hyphenated names present
     * 5. Statistical analysis: calculates confidence based on patterns
     * 
     * @param sampleValues List of column values to analyze (typically 5-20 samples)
     * @return Confidence score 0-100. Score >= 75% is considered HIGH confidence match.
     *         - 0%: No values match last name pattern, or list is empty
     *         - 30-70%: Mixed results or borderline data (ambiguous with first names)
     *         - 85-100%: Strong indication of last name column (longer names, hyphens)
     * @see LastNameValidator#LAST_NAME_REGEX Pattern definition
     */
    public double validate(List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        int validNames = 0;
        int totalNonNull = 0;
        double avgLength = 0;
        int hyphenatedCount = 0;
        
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
            
            // Check name pattern (single word or hyphenated)
            if (LAST_NAME_REGEX.matcher(trimmed).matches() && trimmed.length() >= 2) {
                validNames++;
                if (trimmed.contains("-")) {
                    hyphenatedCount++;
                }
            }
        }
        
        if (totalNonNull == 0) {
            return 0.0;
        }
        
        avgLength = avgLength / totalNonNull;
        double confidence = (validNames * 100.0) / totalNonNull;
        
        // Last names typically 6-15 chars, penalize if very short (first name range: 3-6)
        if (avgLength >= 6 && avgLength <= 15) {
            confidence = Math.min(100, confidence * 1.15);  // Boost for typical last name range
        } else if (avgLength < 6) {
            confidence = confidence * 0.35;  // Penalize for names in first name range (too short)
        }
        
        // Hyphenated names are very common in last names - boost confidence
        if (hyphenatedCount > 0) {
            confidence = Math.min(100, confidence * 1.1);
        }
        
        logger.debug("LastNameValidator: {} of {} values match last name pattern, avg length {}, hyphenated {}. Final confidence: {}%",
                validNames, totalNonNull, Math.round(avgLength), hyphenatedCount, Math.round(confidence));
        
        return confidence;
    }
}
