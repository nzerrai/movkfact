package com.movkfact.service.generator.numeric;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;

import java.util.Map;
import java.util.Random;

/**
 * Générateur d'entiers aléatoires avec contraintes min/max optionnelles.
 * Plage par défaut : [0, 1000]. Supporte constraints.min / constraints.max.
 */
public class IntegerGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public IntegerGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        int min = 0;
        int max = 1000;

        Map<String, Object> constraints = columnConfig.getConstraints();
        if (constraints != null) {
            if (constraints.get("min") != null) {
                min = ((Number) constraints.get("min")).intValue();
            }
            if (constraints.get("max") != null) {
                max = ((Number) constraints.get("max")).intValue();
            }
        }

        return min + random.nextInt(max - min + 1);
    }
}
