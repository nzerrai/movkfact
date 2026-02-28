package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.util.Random;

/**
 * Générateur de genre (Masculin/Féminin/Autre).
 */
public class GenderGenerator extends DataTypeGenerator {
    private static final String[] GENDERS = {"M", "F", "X"};
    private static final Random random = new Random();

    public GenderGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        return GENDERS[random.nextInt(GENDERS.length)];
    }
}
