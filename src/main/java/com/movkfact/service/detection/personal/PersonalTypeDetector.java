package com.movkfact.service.detection.personal;

import com.movkfact.enums.ColumnType;
import com.movkfact.service.detection.ColumnValueAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PersonalTypeDetector - Orchestrator for 6 personal column types.
 * 
 * <b>Supported Column Types:</b>
 * <ul>
 *   <li>FIRST_NAME - Personal first names with typical length 3-9 chars</li>
 *   <li>LAST_NAME - Surnames, often longer (6-15 chars) or hyphenated</li>
 *   <li>EMAIL - Email addresses (reuses ColumnValueAnalyzer)</li>
 *   <li>GENDER - Gender codes/words (M/F/Male/Female/Homme/Femme)</li>
 *   <li>PHONE - Phone numbers (international formats supported)</li>
 *   <li>ADDRESS - Physical addresses with keywords and postal codes</li>
 * </ul>
 * 
 * <b>Detection Strategy:</b>
 * <ol>
 *   <li>Invokes each specialized validator independently</li>
 *   <li>Collects confidence scores from all validators</li>
 *   <li>Finds best match (highest confidence)</li>
 *   <li>Applies conflict resolution for ambiguous types (FIRST_NAME vs LAST_NAME)</li>
 *   <li>Applies minimum confidence threshold (75%)</li>
 * </ol>
 * 
 * <b>Conflict Resolution:</b>
 * When FIRST_NAME and LAST_NAME scores are similar (within 5%):
 * - Checks column name for hints ("firstname" → FIRST_NAME, "lastname" → LAST_NAME)
 * - Otherwise selects type with highest confidence
 * 
 * <b>Confidence Threshold:</b>
 * Minimum 75% required for detection. Lower scores result in null return (inconclusive).
 * 
 * @since S2.2.2 Task 2.2.2
 */
@Service
public class PersonalTypeDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonalTypeDetector.class);
    
    @Autowired
    private FirstNameValidator firstNameValidator;
    
    @Autowired
    private LastNameValidator lastNameValidator;
    
    @Autowired
    private GenderValidator genderValidator;
    
    @Autowired
    private PhoneValidator phoneValidator;
    
    @Autowired
    private AddressValidator addressValidator;
    
    @Autowired
    private ColumnValueAnalyzer valueAnalyzer; // For EMAIL detection
    
    /**
     * Detect personal column type from sample data.
     * 
     * Orchestrates multiple specialized validators to identify column purpose.
     * Handles conflicts between similar types (FIRST_NAME vs LAST_NAME) using
     * heuristics like column name and length statistics.
     * 
     * <b>Process:</b>
     * 1. Invoke each validator (FirstName, LastName, Email, Gender, Phone, Address)
     * 2. Collect confidence scores in scoreMap
     * 3. Find highest-scoring type (best match)
     * 4. Apply tie-breaking logic if FIRST_NAME or LAST_NAME candidates:
     *    - If scores within 5%, check column name for hints
     *    - "lastname" in column name → prefer LAST_NAME
     *    - "firstname" in column name → prefer FIRST_NAME
     *    - Otherwise → pick type with highest confidence
     * 5. Verify minimum 75% confidence threshold
     * 6. Return ColumnType or null if inconclusive
     * 
     * @param columnName The column name (used for context hints in tie-breaking)
     * @param sampleValues Sample data from column (typically 5-20 values)
     * @return Detected {@link ColumnType}, one of: FIRST_NAME, LAST_NAME, EMAIL,
     *         GENDER, PHONE, ADDRESS, or null if inconclusive (&lt; 75% confidence)
     * 
     * @see FirstNameValidator#validate(List) FirstName detection logic
     * @see LastNameValidator#validate(List) LastName detection logic
     * @see GenderValidator#validate(List) Gender detection logic
     * @see PhoneValidator#validate(List) Phone detection logic
     * @see AddressValidator#validate(List) Address detection logic
     * @see ColumnValueAnalyzer#analyzeValues(String, List) Email detection logic
     */
    public ColumnType detect(String columnName, List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            logger.debug("PersonalTypeDetector: No sample values for column '{}'", columnName);
            return null;
        }
        
        // Collect confidence scores from each validator
        Map<ColumnType, Double> scores = new HashMap<>();
        
        // FIRST_NAME validation
        double firstNameScore = firstNameValidator.validate(sampleValues);
        scores.put(ColumnType.FIRST_NAME, firstNameScore);
        
        // LAST_NAME validation
        double lastNameScore = lastNameValidator.validate(sampleValues);
        scores.put(ColumnType.LAST_NAME, lastNameScore);
        
        // EMAIL validation (reuse ColumnValueAnalyzer)
        ColumnType emailDetected = valueAnalyzer.analyzeValues(columnName, sampleValues);
        double emailScore = (emailDetected == ColumnType.EMAIL) ? 85.0 : 0.0;
        scores.put(ColumnType.EMAIL, emailScore);
        
        // GENDER validation
        double genderScore = genderValidator.validate(sampleValues);
        scores.put(ColumnType.GENDER, genderScore);
        
        // PHONE validation
        double phoneScore = phoneValidator.validate(sampleValues);
        scores.put(ColumnType.PHONE, phoneScore);
        
        // ADDRESS validation
        double addressScore = addressValidator.validate(sampleValues);
        scores.put(ColumnType.ADDRESS, addressScore);
        
        // Find best match (highest confidence)
        ColumnType bestType = scores.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
        
        double bestScore = scores.getOrDefault(bestType, 0.0);
        
        // Handle FIRST_NAME vs LAST_NAME conflict: prefer higher score, or if equal, pick by confidence details
        if ((bestType == ColumnType.LAST_NAME || bestType == ColumnType.FIRST_NAME)) {
            double firstScore = scores.get(ColumnType.FIRST_NAME);
            double lastScore = scores.get(ColumnType.LAST_NAME);
            
            // If both have very similar scores (within 5%), pick the one with longer avg length (likely last name)
            if (Math.abs(firstScore - lastScore) < 5) {
                // Use column name or other context to distinguish
                // For now: if names_column contains "last", prefer LAST_NAME
                // Otherwise just pick highest (which will be from the max() calculation)
                if (columnName != null && columnName.toLowerCase().contains("last")) {
                    bestType = ColumnType.LAST_NAME;
                    bestScore = lastScore;
                } else if (columnName != null && columnName.toLowerCase().contains("first")) {
                    bestType = ColumnType.FIRST_NAME;
                    bestScore = firstScore;
                } else if (lastScore > firstScore) {
                    bestType = ColumnType.LAST_NAME;
                    bestScore = lastScore;
                } else {
                    bestType = ColumnType.FIRST_NAME;
                    bestScore = firstScore;
                }
            } else if (lastScore > firstScore) {
                bestType = ColumnType.LAST_NAME;
                bestScore = lastScore;
            } else {
                bestType = ColumnType.FIRST_NAME;
                bestScore = firstScore;
            }
        }
        
        // Require minimum 75% confidence for personal types (slightly higher than general 80%)
        if (bestScore >= 75.0) {
            logger.debug("PersonalTypeDetector: Column '{}' detected as {} with confidence {}%",
                    columnName, bestType.name(), Math.round(bestScore));
            return bestType;
        }
        
        logger.debug("PersonalTypeDetector: Column '{}' inconclusive (best: {} at {}%)",
                columnName, bestType, Math.round(bestScore));
        return null;
    }
}
