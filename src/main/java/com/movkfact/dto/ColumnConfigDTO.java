package com.movkfact.dto;

import com.movkfact.enums.ColumnType;

/**
 * DTO représentant la configuration d'une colonne pour la génération de données.
 */
public class ColumnConfigDTO {
    private String name;
    private ColumnType columnType;
    private String format;
    private Integer minValue;
    private Integer maxValue;
    private Boolean nullable;
    private String additionalConfig;

    // Constructeurs
    public ColumnConfigDTO() {
    }

    public ColumnConfigDTO(String name, ColumnType columnType) {
        this.name = name;
        this.columnType = columnType;
        this.nullable = false;
    }

    // Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public void setColumnType(ColumnType columnType) {
        this.columnType = columnType;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public Boolean getNullable() {
        return nullable;
    }

    /**
     * Set whether this column allows null values.
     * @param nullable true if column allows null values, false otherwise
     */
    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public String getAdditionalConfig() {
        return additionalConfig;
    }

    public void setAdditionalConfig(String additionalConfig) {
        this.additionalConfig = additionalConfig;
    }
}
