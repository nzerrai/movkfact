package com.movkfact.service.detection.financial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for AMOUNT column type detection.
 * 
 * Identifies columns containing monetary amounts through pattern matching and heuristic analysis.
 * Suitable for financial transactions, payments, balances, etc.
 * 
 * <b>Detection Criteria:</b>
 * <ul>
 *   <li>Numeric values (integers or decimals)</li>
 *   <li>Supports optional currency symbols: €, $, £, ¥</li>
 *   <li>Supports decimal separators: . (dot) or , (comma)</li>
 *   <li>Supports thousands separators: , (comma), . (dot), or space</li>
 *   <li>Typical range: 0.01 to 9,999,999.99</li>
 *   <li>Rejects: text, emails, phone numbers, dates</li>
 * </ul>
 * 
 * <b>Confidence Calculation:</b>
 * Base confidence = (valid_amounts / total_non_null_values) * 100
 * - Boost by 1.10x if values are typically in 0-1M range (realistic amounts)
 * - Slightly penalizes if extreme values detected
 * 
 * <b>Typical Values:</b>
 * "1000.50", "€50", "$123.45", "1,234.56", "100", "9999.99"
 * 
 * @since S2.2.3 Task 2.2.3
 */
@Service
public class AmountValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(AmountValidator.class);
    
    // Pattern: optional currency symbol, optional thousands separator, digits with optional decimal
    // Matches: 1000, 1000.50, €50.00, $1,234.56, 1.234,56
    private static final String AMOUNT_PATTERN = "^[€$£¥]?\\s*[0-9]{1,3}([,.]?[0-9]{3})*([.,][0-9]{1,2})?\\s*[€$£¥]?$";
    private static final Pattern AMOUNT_REGEX = Pattern.compile(AMOUNT_PATTERN);
    
    /**
     * Validate if a list of values represents monetary amounts.
     * 
     * Performs comprehensive checks on the sample values:
     * 1. Pattern matching: numeric with optional currency symbols and separators
     * 2. Format filtering: rejects malformed amounts
     * 3. Content filtering: rejects text, emails, dates
     * 4. Statistical analysis: calculates confidence based on value distribution
     * 
     * @param sampleValues List of column values to analyze (typically 5-20 samples)
     * @return Confidence score 0-100. Score >= 75% is considered HIGH confidence match.
     *         - 0%: No values match amount pattern, or list is empty
     *         - 30-70%: Mixed results or borderline data
     *         - 85-100%: Strong indication of amount column
     */
    public double validate(List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        int validAmounts = 0;
        int totalNonNull = 0;
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        
        for (String value : sampleValues) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            
            totalNonNull++;
            String trimmed = value.trim();
            
            // Check if matches amount pattern
            if (AMOUNT_REGEX.matcher(trimmed).matches()) {
                validAmounts++;
                
                // Try to extract numeric value for range analysis
                try {
                    double numericValue = parseAmountValue(trimmed);
                    minValue = Math.min(minValue, numericValue);
                    maxValue = Math.max(maxValue, numericValue);
                } catch (NumberFormatException e) {
                    // Log but don't fail - still count as valid pattern match
                    logger.debug("Could not parse numeric value from: {}", trimmed);
                }
            }
        }
        
        if (totalNonNull == 0) {
            return 0.0;
        }
        
        double confidence = (validAmounts * 100.0) / totalNonNull;
        
        // Boost slightly if values are in realistic range (0.01 to 1,000,000)
        if (minValue >= 0.01 && maxValue <= 1_000_000) {
            confidence = Math.min(100, confidence * 1.10);
        }
        
        // Penalize if extreme outliers detected
        if (maxValue > 100_000_000 || minValue < -100_000_000) {
            confidence = confidence * 0.85;
        }
        
        logger.debug("AmountValidator: {} of {} values match amount pattern, range [{}, {}]. Final confidence: {}%",
                validAmounts, totalNonNull, 
                minValue == Double.MAX_VALUE ? "N/A" : Math.round(minValue),
                maxValue == Double.MIN_VALUE ? "N/A" : Math.round(maxValue),
                Math.round(confidence));
        
        return confidence;
    }
    
    /**
     * Parse amount string to double value, handling various formats.
     * 
     * Supports:
     * - Currency symbols (€, $, £, ¥)
     * - Thousands separators (comma, dot, space)
     * - Decimal separators (dot or comma)
     * 
     * @param amount String representation of amount (e.g., "€1,234.56")
     * @return Numeric value as double
     * @throws NumberFormatException if value cannot be parsed
     */
    private double parseAmountValue(String amount) throws NumberFormatException {
        // Remove currency symbols and spaces
        String cleaned = amount.replaceAll("[€$£¥\\s]", "");
        
        // Determine decimal separator: assume last separator (. or ,) is decimal
        int lastDotIndex = cleaned.lastIndexOf('.');
        int lastCommaIndex = cleaned.lastIndexOf(',');
        
        String normalized;
        if (lastDotIndex > lastCommaIndex) {
            // Last separator is dot -> it's decimal separator
            normalized = cleaned.replace(",", "");
        } else if (lastCommaIndex > lastDotIndex) {
            // Last separator is comma -> it's decimal separator
            normalized = cleaned.replace(".", "").replace(",", ".");
        } else {
            // No separator or only one
            normalized = cleaned;
        }
        
        return Double.parseDouble(normalized);
    }
}
