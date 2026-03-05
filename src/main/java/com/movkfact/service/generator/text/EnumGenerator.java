package com.movkfact.service.generator.text;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Générateur de valeurs aléatoires depuis une liste définie par l'utilisateur.
 * Contrainte attendue : "values" → liste de chaînes (ex: ["Actif","Inactif","Suspendu"]).
 * Si la liste est absente ou vide, retourne une chaîne vide.
 */
public class EnumGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public EnumGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object generate() {
        Map<String, Object> constraints = columnConfig.getConstraints();
        if (constraints == null) return "";

        Object raw = constraints.get("values");
        if (!(raw instanceof List)) return "";

        List<String> values = (List<String>) raw;
        if (values.isEmpty()) return "";

        return values.get(random.nextInt(values.size()));
    }
}
