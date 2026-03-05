package com.movkfact.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * DTO for row update requests (PUT /api/data-sets/{id}/rows/{rowIndex}).
 */
public class RowUpdateRequestDTO {

    @NotNull(message = "columns must not be null")
    @NotEmpty(message = "columns must not be empty")
    private Map<String, Object> columns;

    public Map<String, Object> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, Object> columns) {
        this.columns = columns;
    }
}
