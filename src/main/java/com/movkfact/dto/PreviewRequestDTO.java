package com.movkfact.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO pour la requête de prévisualisation de génération de données (S7.1).
 * Permet de générer N lignes (max 5) sans persistance.
 */
public class PreviewRequestDTO {

    @NotNull(message = "columns ne peut pas être null")
    @NotEmpty(message = "Au moins une colonne est requise")
    private List<ColumnConfigDTO> columns;

    private int count = 5;

    public PreviewRequestDTO() {
    }

    public PreviewRequestDTO(List<ColumnConfigDTO> columns, int count) {
        this.columns = columns;
        this.count = count;
    }

    public List<ColumnConfigDTO> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfigDTO> columns) {
        this.columns = columns;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
