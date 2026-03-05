package com.movkfact.service.generator.numeric;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;

import java.util.Random;

/**
 * Générateur de valeurs booléennes aléatoires (true/false).
 */
public class BooleanGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public BooleanGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        return random.nextBoolean();
    }
}
