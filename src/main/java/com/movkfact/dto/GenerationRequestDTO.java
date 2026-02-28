package com.movkfact.dto;

import jakarta.validation.constraints.Min;
 import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO représentant la requête de génération de données.
 */
public class GenerationRequestDTO {
    private Long domainId;
    private String datasetName;
    
    @NotNull(message = "Number of rows must not be null")
    @Min(value = 1, message = "Number of rows must be at least 1")
    private Integer numberOfRows;
    
    @NotNull(message = "Columns must not be null")
    @NotEmpty(message = "At least one column must be provided")
    private List<ColumnConfigDTO> columns;

    // Constructeurs
    public GenerationRequestDTO() {
    }

    public GenerationRequestDTO(Integer numberOfRows, List<ColumnConfigDTO> columns) {
        this.numberOfRows = numberOfRows;
        this.columns = columns;
    }

    // Getters & Setters
    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public Integer getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(Integer numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public List<ColumnConfigDTO> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfigDTO> columns) {
        this.columns = columns;
    }
}
