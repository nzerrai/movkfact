package com.movkfact.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a column configuration for CSV uploads
 * Maps detected CSV columns to data types
 */
@Entity
@Table(name = "column_configurations")
public class ColumnConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long domainId;

    @Column(nullable = false)
    private String columnName;

    @Column(nullable = false)
    private String detectedType;

    @Column(nullable = false)
    private Double confidence;

    @Column(columnDefinition = "TEXT")
    private String detector;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public ColumnConfiguration() {
    }

    public ColumnConfiguration(Long domainId, String columnName, String detectedType, 
                               Double confidence, String detector) {
        this.domainId = domainId;
        this.columnName = columnName;
        this.detectedType = detectedType;
        this.confidence = confidence;
        this.detector = detector;
    }

    // Getters and Setters
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

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDetectedType() {
        return detectedType;
    }

    public void setDetectedType(String detectedType) {
        this.detectedType = detectedType;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getDetector() {
        return detector;
    }

    public void setDetector(String detector) {
        this.detector = detector;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
