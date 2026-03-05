package com.movkfact.dto;

/**
 * Configuration d'anonymisation pour une colonne.
 * columnType doit correspondre à un ColumnType valide ou être null (colonne non anonymisée).
 */
public class AnonymizationColumnConfig {
    private String columnName;
    private String columnType; // ColumnType.name() ou null = colonne conservée telle quelle
    private boolean anonymize;

    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public String getColumnType() { return columnType; }
    public void setColumnType(String columnType) { this.columnType = columnType; }

    public boolean isAnonymize() { return anonymize; }
    public void setAnonymize(boolean anonymize) { this.anonymize = anonymize; }
}
