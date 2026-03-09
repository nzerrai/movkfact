package com.movkfact.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO representing a dataset summary with enriched status information.
 * Used by GET /api/domains/{domainId}/data-sets (FR-003).
 */
public class DataSetSummaryDTO {

    private Long id;
    private String datasetName;
    private Integer rowCount;
    private Integer columnCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;

    private DomainStatusDTO status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime lastActivity;

    public DataSetSummaryDTO() {}

    public DataSetSummaryDTO(Long id, String datasetName, Integer rowCount, Integer columnCount,
                              LocalDateTime createdAt, LocalDateTime updatedAt,
                              DomainStatusDTO status, LocalDateTime lastActivity) {
        this.id = id;
        this.datasetName = datasetName;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.lastActivity = lastActivity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }

    public Integer getRowCount() { return rowCount; }
    public void setRowCount(Integer rowCount) { this.rowCount = rowCount; }

    public Integer getColumnCount() { return columnCount; }
    public void setColumnCount(Integer columnCount) { this.columnCount = columnCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public DomainStatusDTO getStatus() { return status; }
    public void setStatus(DomainStatusDTO status) { this.status = status; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
}
