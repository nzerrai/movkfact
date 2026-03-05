package com.movkfact.dto;

import java.util.Map;

/**
 * DTO representing a single dataset row with its index.
 */
public class RowResponseDTO {

    private int rowIndex;
    private Map<String, Object> data;

    public RowResponseDTO() {
    }

    public RowResponseDTO(int rowIndex, Map<String, Object> data) {
        this.rowIndex = rowIndex;
        this.data = data;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
