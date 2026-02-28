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
import com.movkfact.response.ApiResponse;
import com.movkfact.service.DataGeneratorService;
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
    public ResponseEntity<ApiResponse<DataSetDTO>> generateDataset(
            @Parameter(description = "The domain ID", required = true)
            @PathVariable Long domainId,
            @Parameter(description = "Generation request with columns and row count")
            @Valid @RequestBody GenerationRequestDTO request) {
        
        // Validate domain exists
        domainRepository.findByIdAndDeletedAtIsNull(domainId)
            .orElseThrow(() -> new EntityNotFoundException("Domain not found with id " + domainId));
        
        // Set domain ID in request
        request.setDomainId(domainId);
        
        // Generate data using DataGeneratorService
        GenerationResponseDTO response = dataGeneratorService.generate(request);
        
        // Create DataSet entity
        DataSet dataset = new DataSet();
        dataset.setDomainId(domainId);
        dataset.setName(request.getDatasetName() != null ? 
            request.getDatasetName() : 
            "Dataset_" + System.currentTimeMillis());
        dataset.setRowCount(response.getNumberOfRows());
        dataset.setGenerationTimeMs(response.getGenerationTimeMs());
        
        // Serialize data to JSON
        try {
            String dataJson = objectMapper.writeValueAsString(response.getData());
            dataset.setDataJson(dataJson);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize dataset: " + e.getMessage(), e);
        }
        
        // Persist to database
        DataSet saved = dataSetRepository.save(dataset);
        
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
    public ResponseEntity<ApiResponse<List<DataSetDTO>>> listDatasetsByDomain(
            @Parameter(description = "The domain ID", required = true)
            @PathVariable Long domainId) {
        
        // Validate domain exists
        domainRepository.findByIdAndDeletedAtIsNull(domainId)
            .orElseThrow(() -> new EntityNotFoundException("Domain not found with id " + domainId));
        
        // Query datasets
        List<DataSet> datasets = dataSetRepository.findByDomainIdAndDeletedAtIsNull(domainId);
        List<DataSetDTO> dtos = datasets.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
        
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
        
        return ResponseEntity.noContent().build();
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
}
