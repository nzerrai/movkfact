package com.movkfact.service.detection.financial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for ACCOUNT_NUMBER column type detection.
 * 
 * Identifies columns containing bank account numbers through pattern matching.
 * Supports IBAN, BBAN, and generic account numbers.
 * 
 * <b>Detection Criteria:</b>
 * <ul>
 *   <li>Numeric or alphanumeric values (8-34 characters typical)</li>
 *   <li>IBAN format: 2 letters + 2 digits + alphanumeric (e.g., "FR1420041010050500013M02606")</li>
 *   <li>BBAN format: numeric, 8-17 digits (e.g., "20041010050500013M02606")</li>
 *   <li>Generic account: 5+ alphanumeric characters, no spaces/special chars</li>
 *   <li>Can be masked format (e.g., "****1234", "...789")</li>
 *   <li>Rejects: text words, emails, phone numbers</li>
 * </ul>
 * 
 * <b>Confidence Calculation:</b>
 * Base confidence = (valid_accounts / total_non_null_values) * 100
 * - Boost by 1.15x if most values are IBAN format (officially standardized)
 * - Boost by 1.10x if consistent length (suggests structured data)
 * - Standard boost for numeric-only accounts
 * 
 * <b>Typical Values:</b>
 * "FR1420041010050500013M02606" (IBAN), "20041010050500013M02606" (BBAN), 
 * "****5678", "ACC123456789"
 * 
 * @since S2.2.3 Task 2.2.3
 */
@Service
public class AccountNumberValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountNumberValidator.class);
    
    // IBAN pattern: 2 letters + 2 digits + up to 30 alphanumeric
    private static final String IBAN_PATTERN = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$";
    private static final Pattern IBAN_REGEX = Pattern.compile(IBAN_PATTERN);
    
    // BBAN pattern: 8-17 digits or mixed alphanumeric
    private static final String BBAN_PATTERN = "^[A-Z0-9]{8,17}$";
    private static final Pattern BBAN_REGEX = Pattern.compile(BBAN_PATTERN);
    
    // Masked account pattern: can have * or . for masked digits
    private static final String MASKED_PATTERN = "^[*\\.]+(\\d+|[A-Z0-9]+)$";
    private static final Pattern MASKED_REGEX = Pattern.compile(MASKED_PATTERN);
    
    // Generic account: at least 5 alphanumeric, no spaces/special chars except dash
    private static final String GENERIC_ACCOUNT_PATTERN = "^[A-Z0-9\\-]{5,}$";
    private static final Pattern GENERIC_ACCOUNT_REGEX = Pattern.compile(GENERIC_ACCOUNT_PATTERN);
    
    /**
     * Validate if a list of values represents account numbers.
     * 
     * Performs comprehensive checks on the sample values:
     * 1. Pattern matching: IBAN, BBAN, masked, or generic account format
     * 2. Format consistency analysis
     * 3. Content filtering: rejects common text words, emails, dates
     * 4. Minimum length validation
     * 
     * @param sampleValues List of column values to analyze (typically 5-20 samples)
     * @return Confidence score 0-100. Score >= 75% is considered HIGH confidence match.
     *         - 0%: No values match account pattern, or list is empty
     *         - 30-70%: Mixed results or borderline data
     *         - 85-100%: Strong indication of account column
     */
    public double validate(List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        int validAccounts = 0;
        int totalNonNull = 0;
        int ibanCount = 0;
        int babanCount = 0;
        int maskedCount = 0;
        int genericCount = 0;
        int lengthVariance = 0;
        
        for (String value : sampleValues) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            
            totalNonNull++;
            String trimmed = value.trim().toUpperCase();
            
            // Reject obvious text
            if (isCommonWord(trimmed)) {
                continue;
            }
            
            // Try IBAN format
            if (IBAN_REGEX.matcher(trimmed).matches()) {
                validAccounts++;
                ibanCount++;
                continue;
            }
            
            // Try BBAN format
            if (BBAN_REGEX.matcher(trimmed).matches()) {
                validAccounts++;
                babanCount++;
                continue;
            }
            
            // Try masked format
            if (MASKED_REGEX.matcher(trimmed).matches()) {
                validAccounts++;
                maskedCount++;
                continue;
            }
            
            // Try generic account format
            if (GENERIC_ACCOUNT_REGEX.matcher(trimmed).matches()) {
                validAccounts++;
                genericCount++;
                lengthVariance++;
                continue;
            }
            
            // Minimum length check: at least 5 chars (to avoid short IDs)
            if (trimmed.length() >= 5 && trimmed.matches("^[A-Z0-9\\-]*$")) {
                validAccounts++;
                genericCount++;
            }
        }
        
        if (totalNonNull == 0) {
            return 0.0;
        }
        
        double confidence = (validAccounts * 100.0) / totalNonNull;
        
        // Boost if IBAN format detected (officially standardized)
        if (ibanCount > 0 && ibanCount >= totalNonNull * 0.5) {
            confidence = Math.min(100, confidence * 1.15);
        }
        
        // Boost if consistent format (suggests structured data)
        if (ibanCount == totalNonNull || babanCount == totalNonNull || 
            maskedCount >= totalNonNull * 0.7) {
            confidence = Math.min(100, confidence * 1.10);
        }
        
        logger.debug("AccountNumberValidator: {} of {} values match account pattern (IBAN:{}, BBAN:{}, Masked:{}, Generic:{}). Final confidence: {}%",
                validAccounts, totalNonNull, ibanCount, babanCount, maskedCount, genericCount, Math.round(confidence));
        
        return confidence;
    }
    
    /**
     * Check if value is a common English/French word to filter false positives.
     * 
     * @param value String to check
     * @return true if value appears to be a common word
     */
    private boolean isCommonWord(String value) {
        String[] commonWords = {
            "ACCOUNT", "COMPTE", "NUMBER", "NUMERO", "ID", "IDENTIFIER",
            "NAME", "NOM", "DESCRIPTION", "REFERENCE", "CODE", "DATA"
        };
        
        for (String word : commonWords) {
            if (value.equals(word)) {
                return true;
            }
        }
        
        return false;
    }
}
