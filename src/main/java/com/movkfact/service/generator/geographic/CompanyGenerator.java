package com.movkfact.service.generator.geographic;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;

import java.util.List;
import java.util.Random;

/**
 * Générateur de noms d'entreprises aléatoires.
 */
public class CompanyGenerator extends DataTypeGenerator {
    private static final Random random = new Random();
    private static final List<String> PREFIXES = List.of(
        "Acme", "Global", "Alpha", "Omega", "Tech", "Smart", "Digital", "Eco",
        "Nord", "Sud", "Est", "Ouest", "Prime", "Elite", "Pro", "Nexus", "Apex"
    );
    private static final List<String> SUFFIXES = List.of(
        "Solutions", "Systems", "Group", "Corp", "SAS", "SARL", "SA", "Ltd",
        "Services", "Industries", "Consulting", "Partners", "Labs", "Studio",
        "Technologies", "Innovations", "Ventures", "Capital"
    );

    public CompanyGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        return PREFIXES.get(random.nextInt(PREFIXES.size()))
             + " "
             + SUFFIXES.get(random.nextInt(SUFFIXES.size()));
    }
}
