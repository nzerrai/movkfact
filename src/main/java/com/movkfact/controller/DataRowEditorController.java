package com.movkfact.controller;

import com.movkfact.dto.PagedRowsResponseDTO;
import com.movkfact.dto.RowResponseDTO;
import com.movkfact.dto.RowUpdateRequestDTO;
import com.movkfact.response.ApiResponse;
import com.movkfact.service.DataRowEditorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for row-level dataset editing (S6.1).
 * Provides CRUD operations on individual rows of a dataset.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Row Editor", description = "Row-level dataset editing operations")
public class DataRowEditorController {

    @Autowired
    private DataRowEditorService dataRowEditorService;

    @Operation(summary = "Get paginated rows from a dataset")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rows retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dataset not found")
    })
    @GetMapping("/data-sets/{id}/rows")
    public ResponseEntity<ApiResponse<PagedRowsResponseDTO>> getRows(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PagedRowsResponseDTO result = dataRowEditorService.getRows(id, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, "Rows retrieved"));
    }

    @Operation(summary = "Get a single row by index")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Row retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dataset or row not found")
    })
    @GetMapping("/data-sets/{id}/rows/{rowIndex}")
    public ResponseEntity<ApiResponse<RowResponseDTO>> getRow(
            @PathVariable Long id,
            @PathVariable int rowIndex) {
        RowResponseDTO result = dataRowEditorService.getRow(id, rowIndex);
        return ResponseEntity.ok(ApiResponse.success(result, "Row retrieved"));
    }

    @Operation(summary = "Partially update a row (merge — only provided columns are replaced)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Row updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Columns empty or null"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dataset or row not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "Dataset too large for row editing")
    })
    @PutMapping("/data-sets/{id}/rows/{rowIndex}")
    public ResponseEntity<ApiResponse<RowResponseDTO>> updateRow(
            @PathVariable Long id,
            @PathVariable int rowIndex,
            @Valid @RequestBody RowUpdateRequestDTO request) {
        RowResponseDTO result = dataRowEditorService.updateRow(id, rowIndex, request.getColumns(), null);
        return ResponseEntity.ok(ApiResponse.success(result, "Row updated"));
    }

    @Operation(summary = "Delete a row — subsequent rows are re-indexed")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Row deleted"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Dataset or row not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "Dataset too large for row editing")
    })
    @DeleteMapping("/data-sets/{id}/rows/{rowIndex}")
    public ResponseEntity<Void> deleteRow(
            @PathVariable Long id,
            @PathVariable int rowIndex) {
        dataRowEditorService.deleteRow(id, rowIndex, null);
        return ResponseEntity.noContent().build();
    }
}
