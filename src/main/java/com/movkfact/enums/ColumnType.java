package com.movkfact.enums;

/**
 * Énumération des types de colonnes que le service peut générer.
 * Chaque type appartient à une typologie DataType.
 */
public enum ColumnType {
    // Personal Data
    FIRST_NAME("personal", "Prénom"),
    LAST_NAME("personal", "Nom de famille"),
    EMAIL("personal", "Email"),
    PHONE("personal", "Numéro de téléphone"),
    GENDER("personal", "Genre"),
    ADDRESS("personal", "Adresse"),

    // Numeric Data
    INTEGER("numeric", "Nombre entier"),
    DECIMAL("numeric", "Nombre décimal"),
    PERCENTAGE("numeric", "Pourcentage"),
    BOOLEAN("numeric", "Booléen"),

    // Text Data
    ENUM("text", "Liste de valeurs"),
    TEXT("text", "Texte libre"),
    UUID("text", "UUID"),
    URL("text", "URL"),
    IP_ADDRESS("text", "Adresse IP"),

    // Geographic Data
    COUNTRY("geographic", "Pays"),
    CITY("geographic", "Ville"),
    COMPANY("geographic", "Entreprise"),
    ZIP_CODE("geographic", "Code postal"),

    // Financial Data
    AMOUNT("financial", "Montant"),
    CURRENCY("financial", "Devise"),
    ACCOUNT_NUMBER("financial", "Numéro de compte"),

    // Temporal Data
    DATE("temporal", "Date générique"),
    TIME("temporal", "Heure"),
    TIMEZONE("temporal", "Fuseau horaire"),
    BIRTH_DATE("temporal", "Date de naissance");

    private final String dataType;
    private final String description;

    ColumnType(String dataType, String description) {
        this.dataType = dataType;
        this.description = description;
    }

    /**
     * Get the data type classification for this column type.
     * @return the data type (e.g. "personal", "financial", "temporal")
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Get the human-readable description of this column type.
     * @return the description in French
     */
    public String getDescription() {
        return description;
    }
}
