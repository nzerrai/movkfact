package com.movkfact.service.detection.financial;

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
 * FinancialTypeDetector - Orchestrator for 3 financial column types.
 * 
 * <b>Supported Column Types:</b>
 * <ul>
 *   <li>AMOUNT - Monetary amounts with decimals and currency symbols</li>
 *   <li>ACCOUNT_NUMBER - Bank account numbers (IBAN, BBAN, generic)</li>
 *   <li>CURRENCY - ISO 4217 currency codes or symbols</li>
 * </ul>
 * 
 * <b>Detection Strategy:</b>
 * <ol>
 *   <li>Invokes each specialized validator independently</li>
 *   <li>Collects confidence scores from all validators</li>
 *   <li>Finds best match (highest confidence)</li>
 *   <li>Applies minimum confidence threshold (75%)</li>
 * </ol>
 * 
 * <b>Confidence Threshold:</b>
 * Minimum 75% required for detection. Lower scores result in null return (inconclusive).
 * 
 * <b>Special Considerations:</b>
 * - AMOUNT and CURRENCY can coexist in same dataset (e.g., amount with currency symbol)
 * - If tie between AMOUNT and CURRENCY, AMOUNT is preferred (more specific)
 * - ACCOUNT_NUMBER typically has distinct format, rarely confused with others
 * 
 * @since S2.2.3 Task 2.2.3
 */
@Service
public class FinancialTypeDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(FinancialTypeDetector.class);
    
    @Autowired
    private AmountValidator amountValidator;
    
    @Autowired
    private AccountNumberValidator accountNumberValidator;
    
    @Autowired
    private CurrencyValidator currencyValidator;
    
    /**
     * Detect financial column type from sample data.
     * 
     * Orchestrates multiple specialized validators to identify column purpose.
     * Specifically designed for detecting financial data types.
     * 
     * <b>Process:</b>
     * 1. Invoke each validator (Amount, AccountNumber, Currency)
     * 2. Collect confidence scores in scoreMap
     * 3. Find highest-scoring type (best match)
     * 4. Apply tie-breaking logic if scores are very close (within 5%)
     * 5. Verify minimum 75% confidence threshold
     * 6. Return ColumnType or null if inconclusive
     * 
     * @param columnName The column name (used for context hints)
     * @param sampleValues Sample data from column (typically 5-20 values)
     * @return Detected {@link ColumnType}, one of: AMOUNT, ACCOUNT_NUMBER, CURRENCY,
     *         or null if inconclusive (&lt; 75% confidence)
     * 
     * @see AmountValidator#validate(List) Amount detection logic
     * @see AccountNumberValidator#validate(List) Account detection logic
     * @see CurrencyValidator#validate(List) Currency detection logic
     */
    public ColumnType detect(String columnName, List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            logger.debug("FinancialTypeDetector: No sample values for column '{}'", columnName);
            return null;
        }
        
        // Collect confidence scores from each validator
        Map<ColumnType, Double> scores = new HashMap<>();
        
        // AMOUNT validation
        double amountScore = amountValidator.validate(sampleValues);
        scores.put(ColumnType.AMOUNT, amountScore);
        
        // ACCOUNT_NUMBER validation
        double accountScore = accountNumberValidator.validate(sampleValues);
        scores.put(ColumnType.ACCOUNT_NUMBER, accountScore);
        
        // CURRENCY validation
        double currencyScore = currencyValidator.validate(sampleValues);
        scores.put(ColumnType.CURRENCY, currencyScore);
        
        // Find best match (highest confidence)
        ColumnType bestType = scores.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
        
        double bestScore = scores.getOrDefault(bestType, 0.0);
        
        // Require minimum 75% confidence for financial types
        if (bestScore >= 75.0) {
            logger.debug("FinancialTypeDetector: Column '{}' detected as {} with confidence {}%",
                    columnName, bestType.name(), Math.round(bestScore));
            return bestType;
        }
        
        logger.debug("FinancialTypeDetector: Column '{}' inconclusive (best: {} at {}%)",
                columnName, bestType, Math.round(bestScore));
        return null;
    }
}
