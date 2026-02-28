package com.movkfact.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * DTO for paginated data responses.
 * Used when returning data with pagination support.
 */
public class PaginatedDataResponseDTO implements Serializable {
    
    @JsonProperty("totalRows")
    private Integer totalRows;
    
    @JsonProperty("pageNumber")
    private Integer pageNumber;
    
    @JsonProperty("pageSize")
    private Integer pageSize;
    
    @JsonProperty("totalPages")
    private Integer totalPages;
    
    @JsonProperty("rows")
    private List<Map<String, Object>> rows;

    // Constructors
    public PaginatedDataResponseDTO() {}

    public PaginatedDataResponseDTO(Integer totalRows, Integer pageNumber, 
                                   Integer pageSize, Integer totalPages, 
                                   List<Map<String, Object>> rows) {
        this.totalRows = totalRows;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.rows = rows;
    }

    // Getters & Setters
    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }
}
