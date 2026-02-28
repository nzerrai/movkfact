package com.movkfact.entity;

import com.movkfact.enums.ColumnType;
import jakarta.persistence.*;

/**
 * Entité représentant la configuration d'une colonne pour la génération de données.
 * Stocke les paramètres spécifiques au type de colonne.
 */
@Entity
@Table(name = "column_configs")
public class ColumnConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom de la colonne (obligatoire) */
    @Column(nullable = false)
    private String name;

    /** Type énuméré de colonne déterminant le générateur à utiliser (FIRST_NAME, EMAIL, BIRTH_DATE, etc.) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ColumnType columnType;

    /** Format de la colonne (ex: "full"/"street"/"city" pour ADDRESS, "yyyy-MM-dd" pour DATE) */
    @Column(columnDefinition = "TEXT")
    private String format;

    /** Valeur minimale pour générateurs numériques (ex: age min, montant min) */
    @Column
    private Integer minValue;

    /** Valeur maximale pour générateurs numériques (ex: age max, montant max) */
    @Column
    private Integer maxValue;

    /** Indique si les valeurs null sont acceptées pour cette colonne */
    @Column
    private Boolean nullable = false;

    /** Configuration additionnelle en JSON (ex: {"country": "FR", "ageCategory": "ADULT_LIVING"}) */
    @Column(columnDefinition = "TEXT")
    private String additionalConfig;

    /** Référence au dataset parent contenant cette configuration */
    @Column(nullable = false)
    private Long datasetId;

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public String getAdditionalConfig() {
        return additionalConfig;
    }

    public void setAdditionalConfig(String additionalConfig) {
        this.additionalConfig = additionalConfig;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }
}
