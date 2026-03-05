package com.movkfact.batch.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * Requête pour lancer une génération batch de plusieurs datasets.
 */
public class BatchGenerationRequestDTO {

    @NotNull(message = "dataSetConfigs must not be null")
    @NotEmpty(message = "At least one dataset config is required")
    @Size(max = 20, message = "Maximum 20 datasets per batch request")
    @Valid
    private List<BatchDataSetConfigDTO> dataSetConfigs;

    public BatchGenerationRequestDTO() {}

    public BatchGenerationRequestDTO(List<BatchDataSetConfigDTO> dataSetConfigs) {
        this.dataSetConfigs = dataSetConfigs;
    }

    public List<BatchDataSetConfigDTO> getDataSetConfigs() { return dataSetConfigs; }
    public void setDataSetConfigs(List<BatchDataSetConfigDTO> dataSetConfigs) {
        this.dataSetConfigs = dataSetConfigs;
    }
}
