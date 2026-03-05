package com.movkfact.batch.dto;

import com.movkfact.dto.ColumnConfigDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Configuration d'un dataset individuel dans une requête batch.
 */
public class BatchDataSetConfigDTO {

    @NotNull(message = "Domain ID is required")
    private Long domainId;

    @NotBlank(message = "Dataset name is required")
    @Size(min = 3, max = 50, message = "Dataset name must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_\\-\\s]+$",
             message = "Dataset name can only contain alphanumeric characters, underscores, hyphens, and spaces")
    private String datasetName;

    @NotNull(message = "Columns must not be null")
    @NotEmpty(message = "At least one column must be provided")
    @Valid
    private List<ColumnConfigDTO> columns;

    @NotNull(message = "Row count is required")
    @Min(value = 1, message = "Row count must be at least 1")
    @Max(value = 10000, message = "Row count cannot exceed 10000 per dataset")
    private Integer count;

    public BatchDataSetConfigDTO() {}

    public BatchDataSetConfigDTO(Long domainId, String datasetName,
                                  List<ColumnConfigDTO> columns, Integer count) {
        this.domainId = domainId;
        this.datasetName = datasetName;
        this.columns = columns;
        this.count = count;
    }

    public Long getDomainId() { return domainId; }
    public void setDomainId(Long domainId) { this.domainId = domainId; }

    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }

    public List<ColumnConfigDTO> getColumns() { return columns; }
    public void setColumns(List<ColumnConfigDTO> columns) { this.columns = columns; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
