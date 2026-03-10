package com.movkfact.service.generator.numeric;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;

/**
 * Générateur de nombres décimaux aléatoires avec contraintes min/max optionnelles.
 * Plage par défaut : [0.0, 1000.0]. 2 décimales.
 */
public class DecimalGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public DecimalGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        double min = 0.0;
        double max = 1000.0;

        if (columnConfig.getMinValue() != null) min = columnConfig.getMinValue().doubleValue();
        if (columnConfig.getMaxValue() != null) max = columnConfig.getMaxValue().doubleValue();

        Map<String, Object> constraints = columnConfig.getConstraints();
        if (constraints != null) {
            if (constraints.get("min") != null) min = ((Number) constraints.get("min")).doubleValue();
            if (constraints.get("max") != null) max = ((Number) constraints.get("max")).doubleValue();
        }

        if (max < min) max = min;

        double value = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
