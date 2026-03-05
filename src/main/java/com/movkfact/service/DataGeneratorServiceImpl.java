package com.movkfact.service;

import com.movkfact.dto.GenerationRequestDTO;
import com.movkfact.dto.GenerationResponseDTO;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation du service de génération de données.
 * Orchestrate la génération sur les 3 typologies (Personal, Financial, Temporal).
 */
@Service
public class DataGeneratorServiceImpl implements DataGeneratorService {

    /**
     * Génère un ensemble de données selon les paramètres de requête.
     * Orchestre les générateurs spécialisés pour chaque colonne demandée.
     * Utilise le pattern Strategy avec une factory pour créer les générateurs appropriés.
     *
     * @param request Requête de génération contenant colonnes et nombre de lignes
     * @return Réponse contenant les données générées et métriques de performance
     * @throws IllegalArgumentException si la requête est invalide (colonnes/count nulls ou invalides)
     */
    @Override
    public GenerationResponseDTO generate(GenerationRequestDTO request) {
        if (request == null || request.getColumns() == null || request.getNumberOfRows() == null) {
            throw new IllegalArgumentException("Request must contain columns and numberOfRows");
        }

        if (request.getNumberOfRows() <= 0) {
            throw new IllegalArgumentException("numberOfRows must be greater than 0");
        }
        
        if (request.getColumns().isEmpty()) {
            throw new IllegalArgumentException("At least one column must be configured for generation");
        }

        long startTime = System.currentTimeMillis();
        
        // Créer les générateurs pour chaque colonne demandée (ordre préservé)
        Map<String, com.movkfact.service.generator.DataTypeGenerator> generators = new LinkedHashMap<>();
        for (com.movkfact.dto.ColumnConfigDTO column : request.getColumns()) {
            com.movkfact.service.generator.DataTypeGenerator generator =
                com.movkfact.service.generator.GeneratorFactory.createGenerator(column);
            generators.put(column.getName(), generator);
        }

        // Générer les données - une ligne à la fois
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < request.getNumberOfRows(); i++) {
            Map<String, Object> row = new LinkedHashMap<>();

            // Pour chaque colonne, générer une valeur avec son générateur spécialisé (ordre préservé)
            for (String columnName : generators.keySet()) {
                Object generatedValue = generators.get(columnName).generate();
                row.put(columnName, generatedValue);
            }
            
            data.add(row);
        }

        long endTime = System.currentTimeMillis();
        long generationTimeMs = endTime - startTime;

        return new GenerationResponseDTO(
            null,  // datasetId sera assigné par la base de données (via JPA @GeneratedValue)
            request.getNumberOfRows(),
            generationTimeMs,
            data
        );
    }
}
