package com.movkfact.service.detection.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for GENDER column type detection.
 * 
 * Identifies columns containing gender information through pattern matching.
 * Supports multiple formats: abbreviations (M/F, H/F), English words, and French words.
 * 
 * <b>Supported Formats:</b>
 * <ul>
 *   <li>Abbreviations: M, F, H (Homme)</li>
 *   <li>English: Male, Female, Man, Woman</li>
 *   <li>French: Homme, Femme, Masculin, Féminin</li>
 *   <li>Case-insensitive matching (handles "MALE", "male", "Male")</li>
 * </ul>
 * 
 * <b>Confidence Calculation:</b>
 * Base confidence = (matching_values / total_non_null_values) * 100
 * No adjustments applied (straightforward pattern matching).
 * 
 * <b>Typical Values:</b>
 * "M", "F", "Male", "Female", "Homme", "Femme", "H", "F"
 * 
 * @since S2.2.2 Task 2.2.2
 */
@Service
public class GenderValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(GenderValidator.class);
    
    // Common gender codes and words (case-insensitive)
    private static final String GENDER_PATTERN = 
            "^(M|F|Male|Female|Homme|Femme|Masculin|Féminin|Man|Woman|H|F)$";
    private static final Pattern GENDER_REGEX = Pattern.compile(GENDER_PATTERN, Pattern.CASE_INSENSITIVE);
    
    /**
     * Validate if a list of values represents gender information.
     * 
     * Matches values against gender pattern covering multiple formats and languages.
     * Uses case-insensitive matching to handle various capitalizations.
     * 
     * @param sampleValues List of column values to analyze (typically 5-20 samples)
     * @return Confidence score 0-100. Score >= 75% indicates HIGH confidence match.
     *         - 0%: No values match gender pattern, or list is empty
     *         - 50-70%: Mixed results with some non-gender values
     *         - 100%: All values match gender pattern (e.g., [M, F, M, F, M])
     * @see GenderValidator#GENDER_REGEX Pattern definition
     */
    public double validate(List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        int validGenders = 0;
        int totalNonNull = 0;
        
        for (String value : sampleValues) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            
            totalNonNull++;
            String trimmed = value.trim();
            
            // Check if value matches gender pattern
            if (GENDER_REGEX.matcher(trimmed).matches()) {
                validGenders++;
            }
        }
        
        if (totalNonNull == 0) {
            return 0.0;
        }
        
        double confidence = (validGenders * 100.0) / totalNonNull;
        logger.debug("GenderValidator: {} of {} values match gender pattern (confidence: {}%)",
                validGenders, totalNonNull, Math.round(confidence));
        
        return confidence;
    }
}
