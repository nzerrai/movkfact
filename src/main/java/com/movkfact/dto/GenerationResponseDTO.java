package com.movkfact.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for data generation responses
 * Contains generated data and metadata
 */
public class GenerationResponseDTO implements Serializable {
    
    @JsonProperty("datasetId")
    private Long datasetId;
    
    @JsonProperty("numberOfRows")
    private Integer numberOfRows;
    
    @JsonProperty("generationTimeMs")
    private Long generationTimeMs;
    
    @JsonProperty("data")
    private List<Map<String, Object>> data;  // [{col1: val1, col2: val2}, ...]

    // Constructors
    public GenerationResponseDTO() {}

    public GenerationResponseDTO(Long datasetId, Integer numberOfRows, Long generationTimeMs, List<Map<String, Object>> data) {
        this.datasetId = datasetId;
        this.numberOfRows = numberOfRows;
        this.generationTimeMs = generationTimeMs;
        this.data = data;
    }

    // Getters & Setters
    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public Integer getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(Integer numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public Long getGenerationTimeMs() {
        return generationTimeMs;
    }

    public void setGenerationTimeMs(Long generationTimeMs) {
        this.generationTimeMs = generationTimeMs;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}
