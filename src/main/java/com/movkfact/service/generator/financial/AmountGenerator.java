package com.movkfact.service.generator.financial;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Générateur de montants financiers aléatoires avec 2 décimales.
 * Plage: [0.01, 9999.99]. Utilise locale US pour format décimal.
 * Supporte les contraintes wizard: constraints.min / constraints.max (priorité sur minValue/maxValue).
 */
public class AmountGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public AmountGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        double min = 1.0;
        double max = 1000000.0;

        Map<String, Object> constraints = columnConfig.getConstraints();
        if (constraints != null) {
            if (constraints.get("min") != null) {
                min = ((Number) constraints.get("min")).doubleValue();
            }
            if (constraints.get("max") != null) {
                max = ((Number) constraints.get("max")).doubleValue();
            }
        } else {
            if (columnConfig.getMinValue() != null) {
                min = columnConfig.getMinValue();
            }
            if (columnConfig.getMaxValue() != null) {
                max = columnConfig.getMaxValue();
            }
        }

        double amount = min + (max - min) * random.nextDouble();
        String formattedAmount = String.format(Locale.US, "%.2f", amount);
        return new BigDecimal(formattedAmount);
    }
}
