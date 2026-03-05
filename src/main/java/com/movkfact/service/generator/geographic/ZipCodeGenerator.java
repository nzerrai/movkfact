package com.movkfact.service.generator.geographic;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;

import java.util.Random;

/**
 * Générateur de codes postaux français (5 chiffres).
 */
public class ZipCodeGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public ZipCodeGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        return String.format("%05d", random.nextInt(96000) + 1000);
    }
}
