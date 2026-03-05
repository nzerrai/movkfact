package com.movkfact.service.generator.geographic;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.service.generator.DataTypeGenerator;

import java.util.List;
import java.util.Random;

/**
 * Générateur de noms de villes aléatoires.
 */
public class CityGenerator extends DataTypeGenerator {
    private static final Random random = new Random();
    private static final List<String> CITIES = List.of(
        "Paris", "Lyon", "Marseille", "Toulouse", "Bordeaux", "Nantes", "Strasbourg",
        "Lille", "Rennes", "Montpellier", "Nice", "Grenoble", "Dijon", "Angers",
        "Berlin", "Munich", "Hambourg", "Francfort", "Madrid", "Barcelone",
        "Rome", "Milan", "Londres", "Manchester", "Bruxelles", "Amsterdam",
        "Zurich", "Genève", "Lisbonne", "New York", "Los Angeles", "Chicago",
        "Toronto", "Montréal", "São Paulo", "Buenos Aires", "Tokyo", "Séoul",
        "Shanghai", "Mumbai", "Sydney", "Casablanca", "Tunis", "Alger", "Dakar"
    );

    public CityGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        return CITIES.get(random.nextInt(CITIES.size()));
    }
}
