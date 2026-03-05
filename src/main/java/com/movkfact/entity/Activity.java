package com.movkfact.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant une activité sur un dataset.
 * Enregistre les actions effectuées sur les jeux de données.
 */
@Entity
@Table(name = "activity",
    indexes = {
        @Index(name = "idx_activity_dataset", columnList = "dataset_id"),
        @Index(name = "idx_activity_timestamp", columnList = "timestamp")
    }
)
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_id", nullable = false)
    private Long dataSetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ActivityActionType action;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "row_index")
    private Integer rowIndex;

    @Column(name = "modified_columns", columnDefinition = "TEXT")
    private String modifiedColumns;

    @Column(name = "previous_value", columnDefinition = "TEXT")
    private String previousValue;

    // Constructors
    public Activity() {
    }

    public Activity(Long dataSetId, ActivityActionType action, String userName) {
        this.dataSetId = dataSetId;
        this.action = action;
        this.userName = userName;
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public Activity(Long dataSetId, ActivityActionType action, String userName,
                    Integer rowIndex, String modifiedColumns, String previousValue) {
        this(dataSetId, action, userName);
        this.rowIndex = rowIndex;
        this.modifiedColumns = modifiedColumns;
        this.previousValue = previousValue;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(Long dataSetId) {
        this.dataSetId = dataSetId;
    }

    public ActivityActionType getAction() {
        return action;
    }

    public void setAction(ActivityActionType action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(Integer rowIndex) {
        this.rowIndex = rowIndex;
    }

    public String getModifiedColumns() {
        return modifiedColumns;
    }

    public void setModifiedColumns(String modifiedColumns) {
        this.modifiedColumns = modifiedColumns;
    }

    public String getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(String previousValue) {
        this.previousValue = previousValue;
    }
}