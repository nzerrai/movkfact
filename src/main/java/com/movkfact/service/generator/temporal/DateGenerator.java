package com.movkfact.service.generator.temporal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;

/**
 * Générateur de dates génériques aléatoires.
 * Supporte les contraintes wizard: constraints.dateFrom / constraints.dateTo.
 */
public class DateGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public DateGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    /**
     * Génère une date aléatoire au format yyyy-MM-dd.
     * Si constraints.dateFrom et constraints.dateTo sont présents, génère dans la plage.
     * Sinon, génère entre -10 ans et aujourd'hui.
     * @return String - Date générée (ex: "2026-02-15")
     */
    @Override
    public Object generate() {
        Map<String, Object> constraints = columnConfig.getConstraints();
        if (constraints != null
                && constraints.get("dateFrom") != null
                && constraints.get("dateTo") != null) {
            LocalDate from = LocalDate.parse((String) constraints.get("dateFrom"));
            LocalDate to   = LocalDate.parse((String) constraints.get("dateTo"));
            long days = ChronoUnit.DAYS.between(from, to);
            return from.plusDays(days <= 0 ? 0 : (long) (random.nextDouble() * days)).toString();
        }

        long daysAgo = (long) (random.nextDouble() * 3650);
        return LocalDate.now().minusDays(daysAgo).toString();
    }
}
