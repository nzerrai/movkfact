package com.movkfact.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for DataSet response.
 * Contains metadata about a generated dataset without the actual data.
 */
public class DataSetDTO implements Serializable {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("domainId")
    private Long domainId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("rowCount")
    private Integer rowCount;
    
    @JsonProperty("generationTimeMs")
    private Long generationTimeMs;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    // Constructors
    public DataSetDTO() {}

    public DataSetDTO(Long id, Long domainId, String name, Integer rowCount, 
                     Long generationTimeMs, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.domainId = domainId;
        this.name = name;
        this.rowCount = rowCount;
        this.generationTimeMs = generationTimeMs;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public Long getGenerationTimeMs() {
        return generationTimeMs;
    }

    public void setGenerationTimeMs(Long generationTimeMs) {
        this.generationTimeMs = generationTimeMs;
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
