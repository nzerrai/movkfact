package com.movkfact.service.generator;

import com.movkfact.dto.ColumnConfigDTO;
import java.util.List;

/**
 * Abstract base class pour tous les générateurs de données.
 * Fornit l'interface commune pour générer des valeurs d'une colonne spécifique.
 */
public abstract class DataTypeGenerator {
    protected ColumnConfigDTO columnConfig;

    public DataTypeGenerator(ColumnConfigDTO columnConfig) {
        this.columnConfig = columnConfig;
    }

    /**
     * Génère une valeur pour cette colonne.
     *
     * @return Une valeur générée au hasard
     */
    public abstract Object generate();

    /**
     * Génère plusieurs valeurs pour cette colonne.
     *
     * @param count Nombre de valeurs à générer
     * @return Liste de valeurs générées
     */
    public List<Object> generateBatch(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> generate())
            .collect(java.util.stream.Collectors.toList());
    }

    protected String getConfigValue(String key, String defaultValue) {
        if (columnConfig.getAdditionalConfig() == null) {
            return defaultValue;
        }
        // Simple JSON parsing for configuration (would use Jackson in production)
        if (columnConfig.getAdditionalConfig().contains(key)) {
            return defaultValue;
        }
        return defaultValue;
    }
}
