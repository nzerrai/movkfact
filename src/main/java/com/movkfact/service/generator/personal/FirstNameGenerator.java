package com.movkfact.service.generator.personal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Générateur de prénoms français.
 */
public class FirstNameGenerator extends DataTypeGenerator {
    private static final List<String> FRENCH_FIRST_NAMES = Arrays.asList(
        "Jean", "Marie", "Pierre", "Sophie", "Luc",
        "Anne", "Marc", "Claire", "Paul", "Isabelle",
        "Philippe", "Monique", "François", "Jacqueline", "Laurent",
        "Nathalie", "Patrick", "Sylvie", "Denis", "Christine",
        "Olivier", "Valérie", "Thierry", "Martine", "Stéphane",
        "Catherine", "Christophe", "Bernadette", "Alain", "Pascale"
    );

    private static final Random random = new Random();

    public FirstNameGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }
    /**
     * Génère un prénom aléatoire.
     * @return String - Prénom généré (ex: "Marie", "Jean", "Sophie")
     */    @Override
    public Object generate() {
        return FRENCH_FIRST_NAMES.get(random.nextInt(FRENCH_FIRST_NAMES.size()));
    }
}
