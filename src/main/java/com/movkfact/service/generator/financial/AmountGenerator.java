package com.movkfact.service.generator.financial;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Random;

/**
 * Générateur de montants financiers aléatoires avec 2 décimales.
 * Plage: [0.01, 9999.99]. Utilise locale US pour format décimal.
 */
public class AmountGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public AmountGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        int min = columnConfig.getMinValue() != null ? columnConfig.getMinValue() : 1;
        int max = columnConfig.getMaxValue() != null ? columnConfig.getMaxValue() : 1000000;
        
        double amount = min + (max - min) * random.nextDouble();
        // Use Locale.US to ensure "." decimal separator, not ","
        String formattedAmount = String.format(Locale.US, "%.2f", amount);
        return new BigDecimal(formattedAmount);
    }
}
