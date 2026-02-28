package com.movkfact.service.generator.temporal;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.enums.AgeCategoryType;
import com.movkfact.service.generator.DataTypeGenerator;
import java.time.LocalDate;
import java.util.Random;

/**
 * Générateur de dates de naissance avec support des 3 catégories d'âge:
 * - ADULT_LIVING: 18-99 ans (majeurs vivants)
 * - MINOR_LIVING: 0-17 ans (mineurs vivants)
 * - DECEASED: 50-150 ans (décédés depuis 50 ans max)
 */
public class BirthDateGenerator extends DataTypeGenerator {
    private static final Random random = new Random();

    public BirthDateGenerator(ColumnConfigDTO columnConfig) {
        super(columnConfig);
    }

    @Override
    public Object generate() {
        AgeCategoryType ageCategory = getAgeCategory();
        int minAge = columnConfig.getMinValue() != null ? 
            columnConfig.getMinValue() : ageCategory.getMinAge();
        int maxAge = columnConfig.getMaxValue() != null ? 
            columnConfig.getMaxValue() : ageCategory.getMaxAge();
        
        int age = minAge + random.nextInt(maxAge - minAge + 1);
        LocalDate birthDate = LocalDate.now().minusYears(age);
        
        String format = columnConfig.getFormat() != null ? 
            columnConfig.getFormat() : "yyyy-MM-dd";
        return birthDate.toString();
    }

    /**
     * Extrait la catégorie d'âge depuis la configuration JSON.
     * Cherche le champ "ageCategory" avec valeurs valides: ADULT_LIVING, MINOR_LIVING, DECEASED.
     * En cas d'erreur de parsing, retourne ADULT_LIVING par défaut.
     * 
     * @return AgeCategoryType extraite ou ADULT_LIVING par défaut
     */
    private AgeCategoryType getAgeCategory() {
        if (columnConfig.getAdditionalConfig() != null && !columnConfig.getAdditionalConfig().isEmpty()) {
            try {
                // Utiliser Jackson pour parsing JSON robuste (disponible via spring-boot-starter-web)
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(columnConfig.getAdditionalConfig());
                
                if (node.has("ageCategory")) {
                    String categoryStr = node.get("ageCategory").asText();
                    try {
                        return AgeCategoryType.valueOf(categoryStr);
                    } catch (IllegalArgumentException e) {
                        // Si la valeur n'est pas reconnue, log warning et use default
                        System.err.println("Warning: Invalid ageCategory value '" + categoryStr + "', using default ADULT_LIVING");
                    }
                }
            } catch (Exception e) {
                // Si le JSON ne peut pas être parsé, log warning mais ne fail pas
                System.err.println("Warning: Failed to parse additionalConfig JSON for BirthDateGenerator: " + e.getMessage());
            }
        }
        // Default to ADULT_LIVING if no config or parsing fails
        return AgeCategoryType.ADULT_LIVING;
    }
}
