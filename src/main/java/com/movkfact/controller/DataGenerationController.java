package com.movkfact.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.dto.DataSetDTO;
import com.movkfact.dto.GenerationRequestDTO;
import com.movkfact.dto.GenerationResponseDTO;
import com.movkfact.dto.PaginatedDataResponseDTO;
import com.movkfact.entity.DataSet;
import com.movkfact.exception.EntityNotFoundException;
import com.movkfact.repository.DataSetRepository;
import com.movkfact.repository.DomainRepository;
import com.movkfact.response.ApiErrorResponse;
import com.movkfact.response.ApiResponse;
import com.movkfact.event.DatasetActivityEvent;
import com.movkfact.entity.ActivityActionType;
import com.movkfact.entity.Activity;
import com.movkfact.dto.DataSetSummaryDTO;
import com.movkfact.service.ActivityService;
import com.movkfact.service.ColumnConfigurationService;
import com.movkfact.service.DataGeneratorService;
import com.movkfact.service.DomainService;
import org.springframework.context.ApplicationEventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Data Generation operations.
 * 
 * Endpoints:
 * - POST /api/domains/{domainId}/data-sets → Create dataset
 * - GET /api/domains/{domainId}/data-sets → List datasets by domain
 * - GET /api/data-sets/{id} → Get dataset metadata
 * - GET /api/data-sets/{id}/data → Get paginated data
 * - DELETE /api/data-sets/{id} → Soft delete dataset
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Data Generation", description = "APIs for generating and managing datasets")
public class DataGenerationController {
    
    @Autowired
    private DataGeneratorService dataGeneratorService;
    
    @Autowired
    private DataSetRepository dataSetRepository;
    
    @Autowired
    private DomainRepository domainRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ColumnConfigurationService columnConfigurationService;

    @Autowired
    private DomainService domainService;

    // ============================================================================
    // POST /api/domains/{domainId}/data-sets - Create new dataset
    // ============================================================================
    
    /**
     * Generate a new dataset for a domain.
     * 
     * @param domainId The domain ID
     * @param request Generation request with columns and row count
     * @return 201 Created with dataset metadata
     */
    @PostMapping("/domains/{domainId}/data-sets")
    @Operation(
        summary = "Generate a new dataset",
        description = "Generate data for a domain and persist as a DataSet with metadata. " +
                      "Data is stored as JSON for easy retrieval and pagination."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Dataset successfully created",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request (negative rows, empty columns, invalid types)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Domain not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error during generation or serialization"
        )
    })
    public ResponseEntity<?> generateDataset(
            @Parameter(description = "The domain ID", required = true)
            @PathVariable Long domainId,
            @Parameter(description = "Generation request with columns and row count")
            @Valid @RequestBody GenerationRequestDTO request) {
        
        // Validate domain exists
        domainRepository.findByIdAndDeletedAtIsNull(domainId)
            .orElseThrow(() -> new EntityNotFoundException("Domain not found with id " + domainId));
        
        // Determine dataset name
        String datasetName = request.getDatasetName();
        
        // Check if dataset name already exists for this domain
        if (dataSetRepository.existsByDomainIdAndNameAndDeletedAtIsNull(domainId, datasetName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("Dataset with this name already exists in this domain", 409, "/api/domains/" + domainId + "/data-sets"));
        }
        
        // Set domain ID in request
        request.setDomainId(domainId);
        request.setDatasetName(datasetName); // Update request for service
        
        // Generate data using DataGeneratorService
        GenerationResponseDTO response = dataGeneratorService.generate(request);
        
        // Create DataSet entity
        DataSet dataset = new DataSet();
        dataset.setDomainId(domainId);
        dataset.setName(datasetName);
        dataset.setRowCount(response.getNumberOfRows());
        dataset.setColumnCount(request.getColumns().size());
        dataset.setGenerationTimeMs(response.getGenerationTimeMs());
        
        // Serialize data to JSON
        try {
            String dataJson = objectMapper.writeValueAsString(response.getData());
            dataset.setDataJson(dataJson);
            dataset.setOriginalData(dataJson); // Store original data for reset functionality
            dataset.setVersion(0); // Initial version
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize dataset: " + e.getMessage(), e);
        }
        
        // Persist to database
        DataSet saved = dataSetRepository.save(dataset);

        // Sync column configurations so batch generation can reuse this domain's schema.
        // additionalConfig stores constraints as JSON (e.g. ENUM values).
        List<Map<String, Object>> colConfigs = request.getColumns().stream()
            .map(col -> {
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("name", col.getName());
                m.put("type", col.getColumnType() != null ? col.getColumnType().name() : "TEXT");
                m.put("confidence", 1.0);
                m.put("detector", "manual");
                if (col.getConstraints() != null && !col.getConstraints().isEmpty()) {
                    try {
                        m.put("additionalConfig", objectMapper.writeValueAsString(col.getConstraints()));
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        // constraint serialization failure is non-fatal
                    }
                }
                return m;
            })
            .collect(Collectors.toList());
        columnConfigurationService.saveColumnConfigurations(domainId, colConfigs);

        // Publish activity event
        eventPublisher.publishEvent(new DatasetActivityEvent(saved.getId(), ActivityActionType.CREATED, "system"));

        // Return 201 Created with Location header
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(saved.getId())
            .toUri();
        
        DataSetDTO dto = mapToDTO(saved);
        
        return ResponseEntity
            .created(location)
            .body(ApiResponse.success(dto, "Dataset generated and saved successfully"));
    }

    // ============================================================================
    // GET /api/domains/{domainId}/datasets/check-name - Check dataset name availability
    // ============================================================================
    
    /**
     * Check if a dataset name is available for a domain.
     * 
     * @param domainId The domain ID
     * @param name The dataset name to check
     * @return 200 OK with availability status
     */
    @GetMapping("/domains/{domainId}/datasets/check-name")
    @Operation(
        summary = "Check dataset name availability",
        description = "Check if a dataset name is available for use in a specific domain."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Name availability checked successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid name format"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Domain not found"
        )
    })
    public ResponseEntity<?> checkDatasetName(
            @Parameter(description = "The domain ID", required = true)
            @PathVariable Long domainId,
            @Parameter(description = "The dataset name to check", required = true)
            @RequestParam String name) {
        
        // Validate domain exists
        domainRepository.findByIdAndDeletedAtIsNull(domainId)
            .orElseThrow(() -> new EntityNotFoundException("Domain not found with id " + domainId));
        
        // Validate name format
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("Dataset name is required", 400, "/api/domains/" + domainId + "/datasets/check-name"));
        }
        if (name.length() < 3 || name.length() > 50) {
            return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("Dataset name must be between 3 and 50 characters", 400, "/api/domains/" + domainId + "/datasets/check-name"));
        }
        if (!name.matches("^[a-zA-Z0-9_\\-\\s]+$")) {
            return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("Dataset name can only contain alphanumeric characters, underscores, hyphens, and spaces", 400, "/api/domains/" + domainId + "/datasets/check-name"));
        }
        
        // Check if name exists
        boolean exists = dataSetRepository.existsByDomainIdAndNameAndDeletedAtIsNull(domainId, name);
        
        Map<String, Object> result = Map.of(
            "name", name,
            "available", !exists
        );
        
        return ResponseEntity.ok(
            ApiResponse.success(result, exists ? "Name is already taken" : "Name is available")
        );
    }

    // ============================================================================
    // GET /api/domains/{domainId}/data-sets - List datasets by domain
    // ============================================================================
    
    /**
     * List all datasets for a domain (excludes soft-deleted).
     * 
     * @param domainId The domain ID
     * @return 200 OK with list of dataset metadata
     */
    @GetMapping("/domains/{domainId}/data-sets")
    @Operation(
        summary = "List datasets for a domain",
        description = "Retrieve all datasets for a specific domain. Excludes soft-deleted datasets."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "List of datasets retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Domain not found"
        )
    })
    public ResponseEntity<ApiResponse<List<DataSetSummaryDTO>>> listDatasetsByDomain(
            @Parameter(description = "The domain ID", required = true)
            @PathVariable Long domainId) {

        List<DataSetSummaryDTO> dtos = domainService.getDatasetsByDomainWithStats(domainId);
        return ResponseEntity.ok(
            ApiResponse.success(dtos, "Retrieved " + dtos.size() + " dataset(s)")
        );
    }

    // ============================================================================
    // GET /api/data-sets/{id} - Get dataset metadata
    // ============================================================================
    
    /**
     * Get metadata for a single dataset.
     * 
     * @param id The dataset ID
     * @return 200 OK with dataset metadata
     */
    @GetMapping("/data-sets/{id}")
    @Operation(
        summary = "Get dataset metadata",
        description = "Retrieve metadata for a specific dataset including row count, generation time, and timestamps."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Dataset metadata retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Dataset not found or has been deleted"
        )
    })
    public ResponseEntity<ApiResponse<DataSetDTO>> getDatasetMetadata(
            @Parameter(description = "The dataset ID", required = true)
            @PathVariable Long id) {
        
        DataSet dataset = dataSetRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new EntityNotFoundException("Dataset not found with id " + id));
        
        DataSetDTO dto = mapToDTO(dataset);
        
        // Publish activity event
        eventPublisher.publishEvent(new DatasetActivityEvent(id, ActivityActionType.VIEWED, "system"));
        
        return ResponseEntity.ok(
            ApiResponse.success(dto, "Dataset metadata retrieved")
        );
    }

    // ============================================================================
    // GET /api/data-sets/{id}/data - Get paginated data
    // ============================================================================
    
    /**
     * Get paginated data from a dataset.
     * 
     * @param id The dataset ID
     * @param page Page number (0-based, default 0)
     * @param size Page size (default 50, max 100)
     * @return 200 OK with paginated data
     */
    @GetMapping("/data-sets/{id}/data")
    @Operation(
        summary = "Get paginated data from a dataset",
        description = "Retrieve generated data with pagination support. " +
                      "Page size is limited to 100 rows maximum to prevent memory issues."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Data retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters (size > 100, page < 0)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Dataset not found or has been deleted"
        )
    })
    public ResponseEntity<ApiResponse<PaginatedDataResponseDTO>> getDatasetData(
            @Parameter(description = "The dataset ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1-100, default 50)")
            @RequestParam(defaultValue = "50") int size) {
        
        // Validate pagination parameters
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        
        // Get dataset
        DataSet dataset = dataSetRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new EntityNotFoundException("Dataset not found with id " + id));
        
        // Parse JSON data and paginate
        PaginatedDataResponseDTO paged = paginateData(dataset, page, size);
        
        // Publish activity event
        eventPublisher.publishEvent(new DatasetActivityEvent(id, ActivityActionType.VIEWED, "system"));
        
        return ResponseEntity.ok(
            ApiResponse.success(paged, "Data retrieved successfully")
        );
    }

    // ============================================================================
    // DELETE /api/data-sets/{id} - Soft delete dataset
    // ============================================================================
    
    /**
     * Soft delete a dataset.
     * 
     * @param id The dataset ID
     * @return 204 No Content
     */
    @DeleteMapping("/data-sets/{id}")
    @Operation(
        summary = "Delete a dataset",
        description = "Soft delete a dataset by marking it as deleted. " +
                      "The dataset remains in the database for audit trails but is excluded from queries."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Dataset successfully deleted"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Dataset not found or already deleted"
        )
    })
    public ResponseEntity<Void> deleteDataset(
            @Parameter(description = "The dataset ID", required = true)
            @PathVariable Long id) {
        
        DataSet dataset = dataSetRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new EntityNotFoundException("Dataset not found with id " + id));
        
        // Soft delete via timestamp
        dataset.setDeletedAt(LocalDateTime.now());
        dataSetRepository.save(dataset);
        
        // Publish activity event
        eventPublisher.publishEvent(new DatasetActivityEvent(id, ActivityActionType.DELETED, "system"));
        
        return ResponseEntity.noContent().build();
    }

    // ============================================================================
    // GET /api/data-sets/{id}/reset - Reset dataset to original version
    // ============================================================================
    
    /**
     * Reset a dataset to its original version.
     * 
     * @param id The dataset ID
     * @return 200 OK with reset dataset
     */
    @GetMapping("/data-sets/{id}/reset")
    @Operation(
        summary = "Reset dataset to original version",
        description = "Reset a dataset to its original generated data, discarding any modifications."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Dataset successfully reset"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Dataset not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Original data not available for reset"
        )
    })
    public ResponseEntity<ApiResponse<DataSetDTO>> resetDataset(
            @Parameter(description = "The dataset ID", required = true)
            @PathVariable Long id) {
        
        DataSet resetDataset = activityService.resetDataSet(id);
        DataSetDTO dto = mapToDTO(resetDataset);
        
        // Publish activity event
        eventPublisher.publishEvent(new DatasetActivityEvent(id, ActivityActionType.RESET, "system"));
        
        return ResponseEntity.ok(
            ApiResponse.success(dto, "Dataset reset to original version")
        );
    }

    // ============================================================================
    // GET /api/data-sets/{id}/activity - Get activity history
    // ============================================================================
    
    /**
     * Get activity history for a dataset.
     * 
     * @param id The dataset ID
     * @param action Optional action type filter
     * @param page Page number (0-based)
     * @param size Page size
     * @return 200 OK with activity history
     */
    @GetMapping("/data-sets/{id}/activity")
    @Operation(
        summary = "Get dataset activity history",
        description = "Retrieve the activity history for a dataset with optional filtering and pagination."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Activity history retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Dataset not found"
        )
    })
    public ResponseEntity<ApiResponse<List<Activity>>> getDatasetActivity(
            @Parameter(description = "The dataset ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Optional action type filter")
            @RequestParam(required = false) ActivityActionType action,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "50") int size) {
        
        // Validate dataset exists
        dataSetRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new EntityNotFoundException("Dataset not found with id " + id));
        
        List<Activity> activities;
        if (action != null) {
            activities = activityService.getActivityByType(id, action);
        } else {
            activities = activityService.getActivityHistory(id);
        }
        
        // Simple pagination
        int start = page * size;
        int end = Math.min(start + size, activities.size());
        List<Activity> pagedActivities = start < activities.size() ? activities.subList(start, end) : List.of();
        
        return ResponseEntity.ok(
            ApiResponse.success(pagedActivities, "Activity history retrieved")
        );
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================
    
    /**
     * Map DataSet entity to DataSetDTO.
     */
    private DataSetDTO mapToDTO(DataSet entity) {
        return new DataSetDTO(
            entity.getId(),
            entity.getDomainId(),
            entity.getName(),
            entity.getRowCount(),
            entity.getColumnCount(),
            entity.getGenerationTimeMs(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
    
    /**
     * Extract and paginate data from dataset JSON.
     * 
     * NOTE: Currently deserializes entire JSON blob into memory before pagination.
     * For MVP this is acceptable (max ~10K rows × 10 cols ≈ 5MB).
     * Performance optimization for Phase 2: Store data in streaming format (Parquet, DB rows, or line-delimited JSON)
     * to support pagination without full deserialization.
     */
    private PaginatedDataResponseDTO paginateData(DataSet dataset, int page, int size) {
        try {
            // Deserialize JSON data
            List<Map<String, Object>> allData = objectMapper.readValue(
                dataset.getDataJson(),
                objectMapper.getTypeFactory().constructCollectionType(
                    List.class,
                    objectMapper.getTypeFactory().constructMapType(
                        Map.class,
                        String.class,
                        Object.class
                    )
                )
            );
            
            // Calculate pagination
            int total = allData.size();
            int totalPages = (total + size - 1) / size;
            int start = Math.min(page * size, total);
            int end = Math.min(start + size, total);
            
            // Extract page slice
            List<Map<String, Object>> pageData = start < total ? 
                allData.subList(start, end) : 
                List.of();
            
            return new PaginatedDataResponseDTO(
                total,
                page,
                size,
                totalPages,
                pageData
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize dataset data", e);
        }
    }

    // ============================================================================
    // GET /api/datasets/count - Get total dataset count
    // ============================================================================
    
    /**
     * Get the total count of all datasets across all domains (excludes soft-deleted).
     * 
     * @return 200 OK with total dataset count
     */
    @GetMapping("/data-sets/count")
    @Operation(
        summary = "Get total dataset count",
        description = "Retrieve the total number of datasets across all domains. Excludes soft-deleted datasets."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Total dataset count retrieved successfully"
        )
    })
    public ResponseEntity<ApiResponse<Long>> getTotalDatasetCount() {
        long count = dataSetRepository.countByDeletedAtIsNull();
        
        return ResponseEntity.ok(
            ApiResponse.success(count, "Total dataset count: " + count)
        );
    }
}
