package com.movkfact.service.generator.text;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;

/**
 * Générateur d'identifiants UUID v4 aléatoires.
 */
public class UuidGenerator extends DataTypeGenerator {

    public UuidGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        return java.util.UUID.randomUUID().toString();
    }
}
