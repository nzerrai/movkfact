package com.movkfact.service.detection.financial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validator for CURRENCY column type detection.
 * 
 * Identifies columns containing ISO 4217 currency codes or currency symbols.
 * Supports 3-letter currency codes (e.g., EUR, USD, GBP) and common symbols.
 * 
 * <b>Detection Criteria:</b>
 * <ul>
 *   <li>ISO 4217 3-letter currency codes: EUR, USD, GBP, JPY, CHF, CAD, AUD, etc.</li>
 *   <li>Currency symbols: €, $, £, ¥, ₹, ₽, ₩, ₪, ₨</li>
 *   <li>Lowercase variations: eur, usd, gbp</li>
 *   <li>Rejects: text words, numbers, email addresses</li>
 * </ul>
 * 
 * <b>Confidence Calculation:</b>
 * Base confidence = (valid_currencies / total_non_null_values) * 100
 * - Boost by 1.15x if all values are valid ISO 4217 codes (highly standardized)
 * - Boost by 1.10x if mixed symbols and codes (proper currency data)
 * 
 * <b>Typical Values:</b>
 * "EUR", "USD", "GBP", "JPY", "€", "$", "£", "eur", "usd"
 * 
 * @since S2.2.3 Task 2.2.3
 */
@Service
public class CurrencyValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(CurrencyValidator.class);
    
    // ISO 4217 currency codes (common ones - subset of ~180 total codes)
    private static final Set<String> ISO_CURRENCIES = new HashSet<>();
    static {
        // Major currencies
        ISO_CURRENCIES.add("EUR"); ISO_CURRENCIES.add("USD"); ISO_CURRENCIES.add("GBP"); ISO_CURRENCIES.add("JPY");
        ISO_CURRENCIES.add("CHF"); ISO_CURRENCIES.add("CAD"); ISO_CURRENCIES.add("AUD"); ISO_CURRENCIES.add("NZD");
        ISO_CURRENCIES.add("MXN"); ISO_CURRENCIES.add("SGD"); ISO_CURRENCIES.add("HKD"); ISO_CURRENCIES.add("NOK");
        ISO_CURRENCIES.add("SEK"); ISO_CURRENCIES.add("DKK"); ISO_CURRENCIES.add("PLN"); ISO_CURRENCIES.add("CZK");
        ISO_CURRENCIES.add("HUF"); ISO_CURRENCIES.add("RON"); ISO_CURRENCIES.add("BGN"); ISO_CURRENCIES.add("HRK");
        ISO_CURRENCIES.add("RUB"); ISO_CURRENCIES.add("INR"); ISO_CURRENCIES.add("BRL"); ISO_CURRENCIES.add("ZAR");
        ISO_CURRENCIES.add("KRW"); ISO_CURRENCIES.add("THB"); ISO_CURRENCIES.add("MYR"); ISO_CURRENCIES.add("PHP");
        ISO_CURRENCIES.add("IDR"); ISO_CURRENCIES.add("VND"); ISO_CURRENCIES.add("TRY"); ISO_CURRENCIES.add("AED");
        ISO_CURRENCIES.add("SAR"); ISO_CURRENCIES.add("QAR"); ISO_CURRENCIES.add("ILS"); ISO_CURRENCIES.add("EGP");
        ISO_CURRENCIES.add("PKR"); ISO_CURRENCIES.add("NGN"); ISO_CURRENCIES.add("KES"); ISO_CURRENCIES.add("CLP");
        ISO_CURRENCIES.add("PEN"); ISO_CURRENCIES.add("COP"); ISO_CURRENCIES.add("ARS"); ISO_CURRENCIES.add("XAU");
        ISO_CURRENCIES.add("XAG");
    }
    
    // Currency symbols pattern
    private static final String SYMBOL_PATTERN = "^[€\\$£¥₹₽₩₪₨₱₡₴₵]$";
    private static final Pattern SYMBOL_REGEX = Pattern.compile(SYMBOL_PATTERN);
    
    // ISO code pattern: exactly 3 letters
    private static final String ISO_CODE_PATTERN = "^[A-Z]{3}$";
    private static final Pattern ISO_CODE_REGEX = Pattern.compile(ISO_CODE_PATTERN);
    
    /**
     * Validate if a list of values represents currency codes or symbols.
     * 
     * Performs comprehensive checks on the sample values:
     * 1. Symbol matching: common currency symbols
     * 2. ISO 4217 code matching: 3-letter currency codes
     * 3. Format consistency
     * 4. Content filtering: rejects text, numbers, dates
     * 
     * @param sampleValues List of column values to analyze (typically 5-20 samples)
     * @return Confidence score 0-100. Score >= 75% is considered HIGH confidence match.
     *         - 0%: No values match currency pattern, or list is empty
     *         - 30-70%: Mixed results or borderline data
     *         - 85-100%: Strong indication of currency column
     */
    public double validate(List<String> sampleValues) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        int validCurrencies = 0;
        int totalNonNull = 0;
        int isoCodeCount = 0;
        int symbolCount = 0;
        
        for (String value : sampleValues) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            
            totalNonNull++;
            String trimmed = value.trim();
            
            // Check if it's a currency symbol
            if (SYMBOL_REGEX.matcher(trimmed).matches()) {
                validCurrencies++;
                symbolCount++;
                continue;
            }
            
            // Check if it's an ISO 4217 code (3 uppercase letters)
            String uppercase = trimmed.toUpperCase();
            if (ISO_CODE_REGEX.matcher(uppercase).matches()) {
                if (ISO_CURRENCIES.contains(uppercase)) {
                    validCurrencies++;
                    isoCodeCount++;
                    continue;
                }
            }
            
            // Try lowercase ISO code
            if (trimmed.length() == 3) {
                String upperTrimmed = trimmed.toUpperCase();
                if (ISO_CURRENCIES.contains(upperTrimmed)) {
                    validCurrencies++;
                    isoCodeCount++;
                }
            }
        }
        
        if (totalNonNull == 0) {
            return 0.0;
        }
        
        double confidence = (validCurrencies * 100.0) / totalNonNull;
        
        // Boost if all valid ISO codes (highly standardized)
        if (isoCodeCount == totalNonNull && isoCodeCount > 0) {
            confidence = Math.min(100, confidence * 1.15);
        }
        
        // Boost if mixed symbols and codes (proper currency variation)
        if (symbolCount > 0 && isoCodeCount > 0) {
            confidence = Math.min(100, confidence * 1.10);
        }
        
        logger.debug("CurrencyValidator: {} of {} values match currency pattern (ISO:{}, Symbol:{}). Final confidence: {}%",
                validCurrencies, totalNonNull, isoCodeCount, symbolCount, Math.round(confidence));
        
        return confidence;
    }
}
