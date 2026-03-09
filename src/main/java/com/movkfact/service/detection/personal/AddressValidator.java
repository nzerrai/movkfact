package com.movkfact.service.detection.personal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for ADDRESS column type detection.
 * 
 * Identifies columns containing address information through keyword matching,
 * postal code detection, and structural analysis. Supports multiple languages
 * and international address formats.
 * 
 * <b>Detection Strategy:</b>
 * Uses keyword scoring system:
 * <ul>
 *   <li>Address keywords: +3 points (street, avenue, rue, boulevard, etc.)</li>
 *   <li>Comma separators: +2 points per comma (typical address structure)</li>
 *   <li>Postal/ZIP codes: +3 points (US, Canadian, EU formats detected)</li>
 *   <li>Length requirement: +1 point if >= 10 characters</li>
 * </ul>
 * Threshold: Score >= 3 indicates valid address element.
 * 
 * <b>Supported Languages:</b>
 * <ul>
 *   <li>English: street, avenue, boulevard, road, lane, drive, court, circle</li>
 *   <li>French: rue, avenue, boulevard, allée, voie, place, quai, cour</li>
 *   <li>German: strasse, weg, platz, straße</li>
 * </ul>
 * 
 * <b>Postal Code Formats:</b>
 * <ul>
 *   <li>US ZIP: 5 digits or 5+4 format (e.g., 10001 or 10001-0123)</li>
 *   <li>Canadian: A1A 1A1 format</li>
 *   <li>EU: 5-digit postal codes</li>
 * </ul>
 * 
 * <b>Typical Values:</b>
 * "123 Main Street, New York, NY 10001", "123 rue de la Paix, Paris, 75000"
 * 
 * @since S2.2.2 Task 2.2.2
 */
@Service
public class AddressValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(AddressValidator.class);
    
    // Address keywords in multiple languages
    private static final String[] ADDRESS_KEYWORDS = {
            "street", "avenue", "boulevard", "road", "lane", "drive", "court", "circle",
            "rue", "avenue", "boulevard", "allée", "voie", "place", "quai", "cour",
            "strasse", "weg", "platz", "straße"
    };
    
    // County/state/city/postal code patterns
    private static final String ZIP_PATTERN = "\\d{5}(-\\d{4})?"; // US zip code
    private static final String POSTAL_PATTERN = "[A-Z]\\d[A-Z] ?\\d[A-Z]\\d"; // Canadian postal
    private static final String EU_POSTAL = "\\d{5}"; // EU 5-digit postal
    
    private static final Pattern ZIP_REGEX = Pattern.compile(ZIP_PATTERN);
    private static final Pattern POSTAL_REGEX = Pattern.compile(POSTAL_PATTERN);
    private static final Pattern EU_POSTAL_REGEX = Pattern.compile(EU_POSTAL);
    
    private static final String COMMA_PATTERN = ","; // Most addresses have commas separating parts
    
    /**
     * Validate if a list of values represents addresses.
     * 
     * Performs multi-level analysis using keyword matching, postal code detection,
     * and structural pattern recognition. Designed to be flexible and support
     * international address formats.
     * 
     * Scoring system (per value):
     * - Keyword match (street, avenue, rue, etc.): +3 points
     * - Comma separator: +2 points (indicates address structure)
     * - Postal code detected: +3 points
     * - Length >= 10 chars: +1 point
     * - Threshold: Score >= 3 = valid address element
     * 
     * @param sampleValues List of column values to analyze (typically 5-20 samples)
     * @return Confidence score 0-100. Score >= 75% indicates HIGH confidence match.
     *         - 0%: No values contain address elements, or list is empty
     *         - 20-50%: Partial address data (some elements present)
     *         - 75-100%: Strong indication of address column (keywords/postal codes present)
     * @see AddressValidator#ADDRESS_KEYWORDS Keywords definition
     * @see AddressValidator#ZIP_REGEX Postal code patterns
     */
    public double validate(List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        int validAddresses = 0;
        int totalNonNull = 0;
        
        for (String value : sampleValues) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            
            totalNonNull++;
            String trimmed = value.trim();
            String lowerCase = trimmed.toLowerCase();
            
            int addressScore = 0;
            
            // Check for address keywords
            for (String keyword : ADDRESS_KEYWORDS) {
                if (lowerCase.contains(keyword)) {
                    addressScore += 3;
                    break; // Only count once
                }
            }
            
            // Check for comma-separated parts
            if (trimmed.contains(",")) {
                addressScore += 2;
            }
            
            // Check for postal/zip codes — skip purely numeric values (amounts, IDs) to avoid false positives
            boolean hasPotentialAddressContent = lowerCase.matches(".*[a-zA-Z,].*");
            if (hasPotentialAddressContent && (ZIP_REGEX.matcher(trimmed).find() ||
                POSTAL_REGEX.matcher(trimmed).find() ||
                EU_POSTAL_REGEX.matcher(trimmed).find())) {
                addressScore += 3;
            }
            
            // Check minimum length (addresses are typically >10 chars)
            if (trimmed.length() > 10) {
                addressScore += 1;
            }
            
            // Score >= 3 suggests likely address
            if (addressScore >= 3) {
                validAddresses++;
            }
        }
        
        if (totalNonNull == 0) {
            return 0.0;
        }
        
        double confidence = (validAddresses * 100.0) / totalNonNull;
        logger.debug("AddressValidator: {} of {} values match address pattern (confidence: {}%)",
                validAddresses, totalNonNull, Math.round(confidence));
        
        return confidence;
    }
}
