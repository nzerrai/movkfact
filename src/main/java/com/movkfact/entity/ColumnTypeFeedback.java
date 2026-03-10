package com.movkfact.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Corpus d'apprentissage adaptatif (S10.1).
 *
 * <p>Enregistre les mappings validés par l'utilisateur :
 * nom de colonne normalisé → type choisi. Le champ {@code count} est incrémenté
 * à chaque confirmation du même mapping.</p>
 *
 * <p>Un mapping devient "fiable" (Niveau 0) lorsque {@code count} atteint
 * le seuil configuré ({@code detection.learning.min-count}, défaut 3).</p>
 */
@Entity
@Table(name = "column_type_feedback",
       indexes = @Index(name = "idx_feedback_lookup", columnList = "column_name_normalized, domain_id"))
public class ColumnTypeFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "column_name_normalized", nullable = false, length = 100)
    private String columnNameNormalized;

    @Column(name = "validated_type", nullable = false, length = 50)
    private String validatedType;

    /** Null = mapping global (cross-domain) */
    @Column(name = "domain_id")
    private Long domainId;

    @Column(nullable = false)
    private int count = 1;

    @Column(name = "last_seen", nullable = false)
    private LocalDateTime lastSeen = LocalDateTime.now();

    protected ColumnTypeFeedback() {}

    public ColumnTypeFeedback(String columnNameNormalized, String validatedType, Long domainId) {
        this.columnNameNormalized = columnNameNormalized;
        this.validatedType = validatedType;
        this.domainId = domainId;
    }

    public Long getId()                          { return id; }
    public String getColumnNameNormalized()      { return columnNameNormalized; }
    public String getValidatedType()             { return validatedType; }
    public Long getDomainId()                    { return domainId; }
    public int getCount()                        { return count; }
    public LocalDateTime getLastSeen()           { return lastSeen; }

    public void incrementCount() {
        this.count++;
        this.lastSeen = LocalDateTime.now();
    }
}
