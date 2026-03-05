package com.movkfact.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un ensemble de données générées.
 * Stocke les métadonnées et les résultats de génération en JSON.
 */
@Entity
@Table(name = "datasets", 
    indexes = {
        @Index(name = "idx_domain_id", columnList = "domainId"),
        @Index(name = "idx_deleted_at", columnList = "deletedAt"),
        @Index(name = "idx_domain_dataset_name", columnList = "domainId, dataset_name")
    },
    uniqueConstraints = @UniqueConstraint(columnNames = {"domainId", "dataset_name"})
)
public class DataSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long domainId;

    @Column(name = "dataset_name", nullable = false)
    private String datasetName;

    @Column(nullable = false)
    private Integer rowCount;

    @Column(nullable = true)
    private Integer columnCount;

    @Column(nullable = false)
    private Long generationTimeMs;

    @Column(columnDefinition = "LONGTEXT")
    private String dataJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private Integer version = 0;

    @Column(columnDefinition = "LONGTEXT")
    private String originalData;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    public String getName() {
        return datasetName;
    }

    public void setName(String name) {
        this.datasetName = name;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }

    public Long getGenerationTimeMs() {
        return generationTimeMs;
    }

    public void setGenerationTimeMs(Long generationTimeMs) {
        this.generationTimeMs = generationTimeMs;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getOriginalData() {
        return originalData;
    }

    public void setOriginalData(String originalData) {
        this.originalData = originalData;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
