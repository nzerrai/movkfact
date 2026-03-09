package com.movkfact.entity;

import jakarta.persistence.*;

/**
 * Entité JPA pour un pattern de détection de type de colonne (S10.2).
 *
 * <p>Chaque ligne représente une expression régulière associée à un {@code ColumnType}.
 * Le cache {@code PatternCache} charge toutes les entrées au démarrage et à chaque reload.</p>
 */
@Entity
@Table(name = "detection_pattern",
       indexes = @Index(name = "idx_detection_pattern_type", columnList = "column_type"))
public class DetectionPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "column_type", nullable = false, length = 50)
    private String columnType;

    @Column(nullable = false, length = 255)
    private String pattern;

    @Column(length = 255)
    private String description;

    protected DetectionPattern() {}

    public DetectionPattern(String columnType, String pattern, String description) {
        this.columnType = columnType;
        this.pattern = pattern;
        this.description = description;
    }

    public Long getId()             { return id; }
    public String getColumnType()   { return columnType; }
    public String getPattern()      { return pattern; }
    public String getDescription()  { return description; }

    public void setColumnType(String columnType)   { this.columnType = columnType; }
    public void setPattern(String pattern)         { this.pattern = pattern; }
    public void setDescription(String description) { this.description = description; }
}
