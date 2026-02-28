package com.movkfact.service.detection.temporal;

import com.movkfact.enums.ColumnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TemporalTypeDetector - Orchestrator for 4 temporal column types.
 * 
 * <b>Supported Column Types:</b>
 * <ul>
 *   <li>BIRTH_DATE - Birth dates with historical data characteristics</li>
 *   <li>DATE - General dates (created_at, modified_at, transaction dates, etc.)</li>
 *   <li>TIME - Time values (HH:MM:SS format)</li>
 *   <li>TIMEZONE - Timezone identifiers (IANA codes, abbreviations, UTC offsets)</li>
 * </ul>
 * 
 * <b>Detection Strategy:</b>
 * <ol>
 *   <li>Invokes each specialized validator independently</li>
 *   <li>Collects confidence scores from all validators</li>
 *   <li>Finds best match (highest confidence)</li>
 *   <li>Handles conflicts (e.g., DATE vs BIRTH_DATE)</li>
 *   <li>Applies minimum confidence threshold (75%)</li>
 * </ol>
 * 
 * <b>Confidence Threshold:</b>
 * Minimum 75% required for detection. Lower scores result in null return (inconclusive).
 * 
 * <b>Conflict Resolution:</b>
 * When DATE and BIRTH_DATE scores are similar (within 5%):
 * - Checks column name for hints ("birth" → BIRTH_DATE, "created"/"modified" → DATE)
 * - Otherwise selects type with highest confidence
 * 
 * @since S2.2.4 Task 2.2.4
 */
@Service
public class TemporalTypeDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(TemporalTypeDetector.class);
    
    @Autowired
    private BirthDateValidator birthDateValidator;
    
    @Autowired
    private DateValidator dateValidator;
    
    @Autowired
    private TimeValidator timeValidator;
    
    @Autowired
    private TimezoneValidator timezoneValidator;
    
    /**
     * Detect temporal column type from sample data.
     * 
     * Orchestrates multiple specialized validators to identify column purpose.
     * Specifically designed for detecting temporal data types.
     * 
     * <b>Process:</b>
     * 1. Invoke each validator (BirthDate, Date, Time, Timezone)
     * 2. Collect confidence scores in scoreMap
     * 3. Find highest-scoring type (best match)
     * 4. Apply tie-breaking logic if DATE and BIRTH_DATE are close
     * 5. Verify minimum 75% confidence threshold
     * 6. Return ColumnType or null if inconclusive
     * 
     * @param columnName The column name (used for context hints)
     * @param sampleValues Sample data from column (typically 5-20 values)
     * @return Detected {@link ColumnType}, one of: BIRTH_DATE, DATE, TIME, TIMEZONE,
     *         or null if inconclusive (&lt; 75% confidence)
     * 
     * @see BirthDateValidator#validate(List) BirthDate detection logic
     * @see DateValidator#validate(List) Date detection logic
     * @see TimeValidator#validate(List) Time detection logic
     * @see TimezoneValidator#validate(List) Timezone detection logic
     */
    public ColumnType detect(String columnName, List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            logger.debug("TemporalTypeDetector: No sample values for column '{}'", columnName);
            return null;
        }
        
        // Collect confidence scores from each validator
        Map<ColumnType, Double> scores = new HashMap<>();
        
        // BIRTH_DATE validation
        double birthDateScore = birthDateValidator.validate(sampleValues);
        scores.put(ColumnType.BIRTH_DATE, birthDateScore);
        
        // DATE validation
        double dateScore = dateValidator.validate(sampleValues);
        scores.put(ColumnType.DATE, dateScore);
        
        // TIME validation
        double timeScore = timeValidator.validate(sampleValues);
        scores.put(ColumnType.TIME, timeScore);
        
        // TIMEZONE validation
        double timezoneScore = timezoneValidator.validate(sampleValues);
        scores.put(ColumnType.TIMEZONE, timezoneScore);
        
        // Find best match (highest confidence)
        ColumnType bestType = scores.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
        
        double bestScore = scores.getOrDefault(bestType, 0.0);
        
        // Handle BIRTH_DATE vs DATE conflict: if scores are similar (within 5%), use column name hints
        if ((bestType == ColumnType.DATE || bestType == ColumnType.BIRTH_DATE)) {
            double dateScoreVal = scores.get(ColumnType.DATE);
            double birthDateScoreVal = scores.get(ColumnType.BIRTH_DATE);
            
            if (Math.abs(dateScoreVal - birthDateScoreVal) < 5) {
                // Scores are very similar, use column name to disambiguate
                if (columnName != null) {
                    String lowerName = columnName.toLowerCase();
                    if (lowerName.contains("birth") || lowerName.contains("naissance") || lowerName.contains("dob")) {
                        bestType = ColumnType.BIRTH_DATE;
                        bestScore = birthDateScoreVal;
                    } else if (lowerName.contains("created") || lowerName.contains("modified") || 
                               lowerName.contains("updated") || lowerName.contains("transaction")) {
                        bestType = ColumnType.DATE;
                        bestScore = dateScoreVal;
                    }
                }
                // If no clear hint, pick highest (already selected by max)
            } else if (birthDateScoreVal > dateScoreVal) {
                bestType = ColumnType.BIRTH_DATE;
                bestScore = birthDateScoreVal;
            } else {
                bestType = ColumnType.DATE;
                bestScore = dateScoreVal;
            }
        }
        
        // Require minimum 75% confidence for temporal types
        if (bestScore >= 75.0) {
            logger.debug("TemporalTypeDetector: Column '{}' detected as {} with confidence {}%",
                    columnName, bestType.name(), Math.round(bestScore));
            return bestType;
        }
        
        logger.debug("TemporalTypeDetector: Column '{}' inconclusive (best: {} at {}%)",
                columnName, bestType, Math.round(bestScore));
        return null;
    }
}
