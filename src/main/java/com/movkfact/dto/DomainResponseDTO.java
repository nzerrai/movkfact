package com.movkfact.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * DTO for Domain entity response in success responses.
 *
 * Represents the output contract for GET and POST/PUT responses.
 * Includes all domain fields: id, timestamps, soft-delete status.
 * Timestamps serialized as ISO8601 with Z timezone.
 *
 * Aggregate stats (datasetCount, totalRows, statuses) are populated by DomainService
 * for the enriched GET /api/domains endpoint (FR-002). They are null for single-domain responses.
 *
 * Response format: Within {@link com.movkfact.response.ApiResponse}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainResponseDTO {

    private Long id;
    private String name;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime deletedAt;

    // Aggregate stats (FR-002) — populated by DomainService.getDomainsWithStats()
    private Integer datasetCount;
    private Long totalRows;
    private DomainStatusDTO statuses;

    public DomainResponseDTO() {}

    public DomainResponseDTO(Long id, String name, String description,
                             LocalDateTime createdAt, LocalDateTime updatedAt,
                             LocalDateTime deletedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public Integer getDatasetCount() { return datasetCount; }
    public void setDatasetCount(Integer datasetCount) { this.datasetCount = datasetCount; }

    public Long getTotalRows() { return totalRows; }
    public void setTotalRows(Long totalRows) { this.totalRows = totalRows; }

    public DomainStatusDTO getStatuses() { return statuses; }
    public void setStatuses(DomainStatusDTO statuses) { this.statuses = statuses; }
}
