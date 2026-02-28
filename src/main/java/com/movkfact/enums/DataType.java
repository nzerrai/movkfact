package com.movkfact.enums;

/**
 * Énumération des typologies de données que le service peut générer.
 */
public enum DataType {
    /**
     * Données personnelles : noms, prénoms, emails, genres, téléphones, adresses
     */
    PERSONAL("Personal Data"),

    /**
     * Données financières : montants, devises, numéros de compte
     */
    FINANCIAL("Financial Data"),

    /**
     * Données temporelles : dates, heures, fuseaux horaires, dates de naissance
     */
    TEMPORAL("Temporal Data");

    private final String description;

    DataType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
