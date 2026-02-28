package com.movkfact.service.generator.financial;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Générateur de codes de devises ISO 4217 aléatoires.
 * Sélectionne parmi 16 devises majeures (EUR, USD, GBP, JPY, etc.).
 */
public class CurrencyGenerator extends DataTypeGenerator {
    private static final List<String> CURRENCIES = Arrays.asList(
        "EUR", "USD", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD",
        "CNY", "INR", "KRW", "SGD", "HKD", "NOK", "SEK", "DKK"
    );

    private static final Random random = new Random();

    public CurrencyGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    /**
     * Génère un code de devise ISO 4217 aléatoire.
     * @return String - Code devise (ex: "EUR", "USD", "GBP")
     */
    @Override
    public Object generate() {
        return CURRENCIES.get(random.nextInt(CURRENCIES.size()));
    }
}
