package com.movkfact.dto;

import java.util.List;

/**
 * DTO for paginated rows response (GET /api/data-sets/{id}/rows).
 */
public class PagedRowsResponseDTO {

    private List<RowResponseDTO> rows;
    private int totalRows;
    private int page;
    private int size;

    public PagedRowsResponseDTO() {
    }

    public PagedRowsResponseDTO(List<RowResponseDTO> rows, int totalRows, int page, int size) {
        this.rows = rows;
        this.totalRows = totalRows;
        this.page = page;
        this.size = size;
    }

    public List<RowResponseDTO> getRows() {
        return rows;
    }

    public void setRows(List<RowResponseDTO> rows) {
        this.rows = rows;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
