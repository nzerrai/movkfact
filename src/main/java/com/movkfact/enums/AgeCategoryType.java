package com.movkfact.enums;

/**
 * Énumération des catégories d'âge pour la génération de dates de naissance.
 * Définit les plages d'âge acceptables pour chaque catégorie.
 */
public enum AgeCategoryType {
    /**
     * Adultes vivants : 18-99 ans
     * Génère des dates entre aujourd'hui - 18 ans et aujourd'hui - 99 ans
     */
    ADULT_LIVING(18, 99, "Adultes vivants"),

    /**
     * Mineurs vivants : 0-17 ans
     * Génère des dates entre aujourd'hui et aujourd'hui - 17 ans
     */
    MINOR_LIVING(0, 17, "Mineurs vivants"),

    /**
     * Personnes décédées : 50-150 ans
     * Génère des dates 50-150 ans avant aujourd'hui
     */
    DECEASED(50, 150, "Personnes décédées");

    private final int minAge;
    private final int maxAge;
    private final String description;

    AgeCategoryType(int minAge, int maxAge, String description) {
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.description = description;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public String getDescription() {
        return description;
    }
}
