package com.movkfact.controller;

import com.movkfact.response.ApiResponse;
import com.movkfact.service.DataExportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.movkfact.event.DatasetActivityEvent;
import com.movkfact.entity.ActivityActionType;
import org.springframework.context.ApplicationEventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * S2.4: Data Export Controller
 * REST endpoints for exporting generated DataSet data as JSON.
 * 
 * Features:
 * - Pretty/Compact formatting
 * - Column filtering
 * - Conditional extraction
 * - File download with proper headers
 * 
 * @author Amelia Dev
 */
@RestController
@RequestMapping("/api/data-sets")
@CrossOrigin(origins = "*")
@Tag(name = "Data Export", description = "Export generated data in JSON format")
public class DataExportController {

    @Autowired
    private DataExportService exportService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Export DataSet as JSON (API response).
     * 
     * AC1: Endpoint GET /api/data-sets/{id}/export?format=json responsive (<500ms)
     * 
     * GET /api/data-sets/{id}/export
     * Query Parameters:
     *   - pretty=true/false (default: true)
     *   - columns=col1,col2,col3 (optional column filtering)
     *   - filter=columnName:value (optional conditional extraction)
     * 
     * @param datasetId Dataset ID
     * @param pretty Pretty formatting (default: true)
     * @param columns Column selection filter (optional)
     * @param filter Conditional filter (optional)
     * @return JSON formatted data
     */
    @GetMapping("/{id}/export")
    @Operation(summary = "Export DataSet as JSON response", description = "Returns generated data formatted as JSON with optional filtering")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "JSON export successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "DataSet not found")
    })
    public ResponseEntity<ApiResponse<String>> exportAsJson(
            @PathVariable("id") Long datasetId,
            @Parameter(description = "Pretty print output (true/false)", example = "true")
            @RequestParam(value = "pretty", defaultValue = "true") boolean pretty,
            @Parameter(description = "Column selection filter: col1,col2,col3", required = false)
            @RequestParam(value = "columns", required = false) String columns,
            @Parameter(description = "Condition filter: columnName:value", required = false)
            @RequestParam(value = "filter", required = false) String filter) {
        
        try {
            // Validate parameters
            exportService.validateExportParameters(datasetId, columns, filter);
            
            // Export data
            String json = exportService.exportAsJson(datasetId, pretty, columns, filter);
            
            return ResponseEntity.ok(
                new ApiResponse<>(json, "DataSet exported successfully")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(null, "Invalid parameters: " + e.getMessage())
            );
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>(null, "Export processing failed: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>(null, "Unexpected error: " + e.getMessage())
            );
        }
    }

    /**
     * Export DataSet for download (file download).
     * 
     * AC2: Endpoint GET /api/data-sets/{id}/export/download?format=json returns downloadable file
     * Content-Disposition header: attachment; filename=...
     * 
     * GET /api/data-sets/{id}/export/download
     * Query Parameters:
     *   - pretty=true/false (default: true)
     *   - columns=col1,col2,col3 (optional)
     *   - filter=columnName:value (optional)
     * 
     * @param datasetId Dataset ID
     * @param pretty Pretty formatting
     * @param columns Column filter
     * @param filter Condition filter
     * @return File download response
     */
    @GetMapping("/{id}/export/download")
    @Operation(summary = "Download DataSet as JSON file", description = "Returns data as downloadable JSON file with metadata")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File download ready"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "DataSet not found")
    })
    public ResponseEntity<?> exportForDownload(
            @PathVariable("id") Long datasetId,
            @Parameter(description = "Pretty print output", example = "true")
            @RequestParam(value = "pretty", defaultValue = "true") boolean pretty,
            @Parameter(description = "Column selection filter", required = false)
            @RequestParam(value = "columns", required = false) String columns,
            @Parameter(description = "Condition filter", required = false)
            @RequestParam(value = "filter", required = false) String filter) {
        
        try {
            // Validate parameters
            exportService.validateExportParameters(datasetId, columns, filter);
            
            // Export with metadata
            Map<String, Object> exportData = exportService.exportForDownload(datasetId, pretty, columns, filter);
            String filename = (String) ((Map<String, Object>) exportData.get("metadata")).get("filename");
            
            // Publish activity event
            eventPublisher.publishEvent(new DatasetActivityEvent(datasetId, ActivityActionType.DOWNLOADED, "system"));
            
            // Build response with download headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", filename);
            headers.set("X-Export-Filename", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>(null, "Invalid parameters: " + e.getMessage())
            );
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>(null, "Export processing failed: " + e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ApiResponse<>(null, "Export failed: " + e.getMessage())
            );
        }
    }
}
