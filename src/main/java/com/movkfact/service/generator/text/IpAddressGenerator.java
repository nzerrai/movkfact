package com.movkfact.service.generator.text;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;

import java.util.Random;

/**
 * Générateur d'adresses IPv4 aléatoires (format: xxx.xxx.xxx.xxx).
 */
public class IpAddressGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public IpAddressGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        return (random.nextInt(223) + 1) + "."
             + random.nextInt(256) + "."
             + random.nextInt(256) + "."
             + (random.nextInt(254) + 1);
    }
}
