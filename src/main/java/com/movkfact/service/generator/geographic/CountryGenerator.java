package com.movkfact.service.generator.geographic;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;

import java.util.List;
import java.util.Random;

/**
 * Générateur de noms de pays aléatoires.
 */
public class CountryGenerator extends DataTypeGenerator {
    private static final Random random = new Random();
    private static final List<String> COUNTRIES = List.of(
        "France", "Allemagne", "Espagne", "Italie", "Royaume-Uni", "Belgique",
        "Pays-Bas", "Suisse", "Portugal", "Suède", "Norvège", "Danemark",
        "Pologne", "Autriche", "Grèce", "États-Unis", "Canada", "Mexique",
        "Brésil", "Argentine", "Chili", "Colombie", "Japon", "Corée du Sud",
        "Chine", "Inde", "Australie", "Nouvelle-Zélande", "Maroc", "Tunisie",
        "Algérie", "Sénégal", "Côte d'Ivoire", "Cameroun", "Afrique du Sud"
    );

    public CountryGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        return COUNTRIES.get(random.nextInt(COUNTRIES.size()));
    }
}
