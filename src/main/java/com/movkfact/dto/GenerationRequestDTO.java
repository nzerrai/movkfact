package com.movkfact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * DTO représentant la requête de génération de données.
 */
public class GenerationRequestDTO {
    private Long domainId;
    
    @NotBlank(message = "Dataset name must not be blank")
    @Size(min = 3, max = 50, message = "Dataset name must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_\\-\\s]+$", message = "Dataset name can only contain alphanumeric characters, underscores, hyphens, and spaces")
    private String datasetName;
    
    @NotNull(message = "Number of rows must not be null")
    @Min(value = 1, message = "Number of rows must be at least 1")
    private Integer numberOfRows;
    
    @NotNull(message = "Columns must not be null")
    @NotEmpty(message = "At least one column must be provided")
    private List<ColumnConfigDTO> columns;
    
    private List<ColumnConfigDTO> extraColumns;

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

    public List<ColumnConfigDTO> getExtraColumns() {
        return extraColumns;
    }

    public void setExtraColumns(List<ColumnConfigDTO> extraColumns) {
        this.extraColumns = extraColumns;
    }
}
