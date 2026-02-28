package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Générateur de noms de famille aléatoires français.
 * Sélectionne aléatoirement parmi 30 noms de famille courants.
 */
public class LastNameGenerator extends DataTypeGenerator {
    private static final List<String> FRENCH_LAST_NAMES = Arrays.asList(
        "Martin", "Bernard", "Thomas", "Robert", "Richard",
        "Petit", "Durand", "Lefevre", "Michel", "Garcia",
        "David", "Bertrand", "Roux", "Vincent", "Fournier",
        "Morel", "Girard", "Andre", "Leroy", "Moreau",
        "Schmitt", "Mathieu", "Fontaine", "Chevalier", "Robin",
        "Fabre", "Nicolas", "Legrand", "Garnier", "Terr"
    );

    private static final Random random = new Random();

    public LastNameGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }
    /**
     * Génère un nom de famille aléatoire.
     * @return String - Nom de famille généré (ex: "Dupont", "Martin", "Bernard")
     */    @Override
    public Object generate() {
        return FRENCH_LAST_NAMES.get(random.nextInt(FRENCH_LAST_NAMES.size()));
    }
}
