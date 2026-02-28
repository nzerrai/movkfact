package com.movkfact.service.generator.temporal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Générateur de fuseaux horaires aléatoires (format IANA).
 */
public class TimezoneGenerator extends DataTypeGenerator {
    private static final List<String> TIMEZONES = Arrays.asList(
        "UTC",
        "Europe/Paris",
        "Europe/London",
        "Europe/Berlin",
        "America/New_York",
        "America/Chicago",
        "America/Denver",
        "America/Los_Angeles",
        "Asia/Tokyo",
        "Asia/Shanghai",
        "Asia/Hong_Kong",
        "Asia/Singapore",
        "Australia/Sydney",
        "Australia/Melbourne",
        "Pacific/Auckland",
        "Africa/Cairo",
        "Africa/Johannesburg"
    );

    private static final Random random = new Random();

    public TimezoneGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    /**
     * Génère un identifiant de fuseau horaire IANA aléatoire.
     * @return String - Fuseau horaire (ex: "Europe/Paris", "America/New_York")
     */
    @Override
    public Object generate() {
        return TIMEZONES.get(random.nextInt(TIMEZONES.size()));
    }
}
