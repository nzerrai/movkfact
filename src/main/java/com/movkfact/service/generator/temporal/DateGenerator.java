package com.movkfact.service.generator.temporal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.time.LocalDate;
import java.util.Random;

/**
 * Générateur de dates génériques aléatoires.
 */
public class DateGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public DateGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    /**
     * Génère une date aléatoire au format yyyy-MM-dd.
     * @return String - Date générée (ex: "2026-02-15")
     */
    @Override
    public Object generate() {
        // Generate a date between -10 years and today
        long daysAgo = random.nextLong(3650); // ~10 years
        LocalDate date = LocalDate.now().minusDays(daysAgo);
        
        String format = columnConfig.getFormat() != null ? columnConfig.getFormat() : "yyyy-MM-dd";
        return date.toString();
    }
}
