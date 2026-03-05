package com.movkfact.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO pour la réponse de prévisualisation de génération de données (S7.1).
 */
public class PreviewResponseDTO {

    private List<Map<String, Object>> previewRows;
    private int columnCount;

    public PreviewResponseDTO() {
    }

    public PreviewResponseDTO(List<Map<String, Object>> previewRows, int columnCount) {
        this.previewRows = previewRows;
        this.columnCount = columnCount;
    }

    public List<Map<String, Object>> getPreviewRows() {
        return previewRows;
    }

    public void setPreviewRows(List<Map<String, Object>> previewRows) {
        this.previewRows = previewRows;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }
}
