package com.movkfact.service.generator.financial;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour les générateurs financiers (Amount, Currency, AccountNumber).
 */
class FinancialGeneratorTests {

    @Test
    void testAmountGeneratorCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO("amount", ColumnType.AMOUNT);
        AmountGenerator generator = new AmountGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testGenerateSingleAmount() {
        ColumnConfigDTO config = new ColumnConfigDTO("amount", ColumnType.AMOUNT);
        AmountGenerator generator = new AmountGenerator(config);
        
        Object result = generator.generate();
        assertNotNull(result);
        assertInstanceOf(BigDecimal.class, result);
        BigDecimal amount = (BigDecimal) result;
        assertTrue(amount.doubleValue() > 0);
    }

    @Test
    void testAmountWithCustomRange() {
        ColumnConfigDTO config = new ColumnConfigDTO("amount", ColumnType.AMOUNT);
        config.setMinValue(100);
        config.setMaxValue(1000);
        AmountGenerator generator = new AmountGenerator(config);
        
        for (int i = 0; i < 20; i++) {
            BigDecimal amount = (BigDecimal) generator.generate();
            assertTrue(amount.doubleValue() >= 100);
            assertTrue(amount.doubleValue() <= 1000);
        }
    }

    @Test
    void testCurrencyGeneratorCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO("currency", ColumnType.CURRENCY);
        CurrencyGenerator generator = new CurrencyGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testGenerateSingleCurrency() {
        ColumnConfigDTO config = new ColumnConfigDTO("currency", ColumnType.CURRENCY);
        CurrencyGenerator generator = new CurrencyGenerator(config);
        
        Object result = generator.generate();
        assertNotNull(result);
        assertInstanceOf(String.class, result);
        String currency = (String) result;
        assertEquals(3, currency.length());  // ISO 4217 codes are 3 letters
    }

    @Test
    void testCurrencyFormats() {
        ColumnConfigDTO config = new ColumnConfigDTO("currency", ColumnType.CURRENCY);
        CurrencyGenerator generator = new CurrencyGenerator(config);
        
        for (int i = 0; i < 50; i++) {
            String currency = (String) generator.generate();
            assertTrue(currency.matches("^[A-Z]{3}$"), "Currency should be 3 uppercase letters");
        }
    }

    @Test
    void testAccountNumberGeneratorCreation() {
        ColumnConfigDTO config = new ColumnConfigDTO("account", ColumnType.ACCOUNT_NUMBER);
        AccountNumberGenerator generator = new AccountNumberGenerator(config);
        assertNotNull(generator);
    }

    @Test
    void testGenerateSingleAccountNumber() {
        ColumnConfigDTO config = new ColumnConfigDTO("account", ColumnType.ACCOUNT_NUMBER);
        AccountNumberGenerator generator = new AccountNumberGenerator(config);
        
        Object result = generator.generate();
        assertNotNull(result);
        assertInstanceOf(String.class, result);
        String account = (String) result;
        assertTrue(account.startsWith("****"));
        assertEquals(8, account.length());  // ****XXXX
    }

    @Test
    void testAccountNumberMasking() {
        ColumnConfigDTO config = new ColumnConfigDTO("account", ColumnType.ACCOUNT_NUMBER);
        AccountNumberGenerator generator = new AccountNumberGenerator(config);
        
        for (int i = 0; i < 50; i++) {
            String account = (String) generator.generate();
            assertTrue(account.matches("^\\*{4}\\d{4}$"), "Account should be ****XXXX");
        }
    }
}
