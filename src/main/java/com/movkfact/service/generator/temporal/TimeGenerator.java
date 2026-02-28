package com.movkfact.service.generator.temporal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.time.LocalTime;
import java.util.Random;

/**
 * Générateur d'heures aléatoires au format HH:mm:ss.
 * Plage: 00:00:00 à 23:59:59.
 */
public class TimeGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public TimeGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    /**
     * Génère une heure aléatoire au format HH:mm:ss.
     * @return String - Heure générée (ex: "14:30:45")
     */
    @Override
    public Object generate() {
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        
        LocalTime time = LocalTime.of(hour, minute, second);
        return time.toString();
    }
}
