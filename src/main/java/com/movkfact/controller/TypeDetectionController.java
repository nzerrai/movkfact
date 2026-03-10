package com.movkfact.controller;

import com.movkfact.context.DetectionContext;
import com.movkfact.dto.TypeDetectionResult;
import com.movkfact.service.detection.CsvTypeDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST Controller for CSV Type Detection API.
 * Endpoint: POST /api/domains/{domainId}/detect-types
 * Accepts CSV file and returns detected column types with confidence scores.
 */
@RestController
@RequestMapping("/api/domains")
@Tag(name = "Type Detection", description = "CSV column type detection API")
public class TypeDetectionController {
    
    private static final Logger logger = LoggerFactory.getLogger(TypeDetectionController.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    @Autowired
    private CsvTypeDetectionService detectionService;

    @Autowired
    private DetectionContext detectionContext;
    
    /**
     * Detect column types from uploaded CSV file.
     * 
     * @param domainId The domain ID (for context/auditing)
     * @param file The CSV file to analyze
     * @param sampleSize Optional number of rows to sample (default: 100, max: 10000)
     * @return TypeDetectionResult with detected column types and confidence scores
     */
    @PostMapping("/{domainId}/detect-types")
    @Operation(
            summary = "Detect column types from CSV",
            description = "Analyzes CSV file headers and sample data to detect column types " +
                    "with confidence scores",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Detection successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid file format or missing required fields"),
                    @ApiResponse(responseCode = "413", description = "File too large (max 10MB)"),
                    @ApiResponse(responseCode = "415", description = "Unsupported media type - CSV expected"),
                    @ApiResponse(responseCode = "500", description = "Internal server error during detection")
            }
    )
    public ResponseEntity<TypeDetectionResult> detectTypes(
            @PathVariable Long domainId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "sampleSize", required = false, defaultValue = "100")
            @Parameter(description = "Number of rows to sample (1-10000)") Integer sampleSize,
            @RequestParam(value = "noHeader", required = false, defaultValue = "false")
            @Parameter(description = "True if the CSV has no header row") boolean noHeader) {
        
        try {
            // Validate file exists and is not empty
            if (file == null || file.isEmpty()) {
                logger.warn("Empty or missing file for domain {}", domainId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "File is required and cannot be empty");
            }
            
            logger.info("TypeDetectionController: POST /api/domains/{}/detect-types - File: {}",
                    domainId, file.getOriginalFilename());
            
            // Validate file extension is CSV
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
                logger.warn("Invalid file extension {} for domain {}", filename, domainId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "File must be a CSV file (.csv)");
            }
            
            // Validate file size
            if (file.getSize() > MAX_FILE_SIZE) {
                logger.warn("File size {} exceeds maximum allowed {} for domain {}",
                        file.getSize(), MAX_FILE_SIZE, domainId);
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "File size exceeds maximum allowed size (10MB)");
            }
            
            // Validate MIME type (check content type)
            String contentType = file.getContentType();
            if (contentType != null && !contentType.contains("text/csv") &&
                    !contentType.contains("application/vnd.ms-excel") &&
                    !contentType.contains("text/plain")) {
                logger.warn("Invalid MIME type {} for domain {}", contentType, domainId);
                // Note: Not throwing 415 here - some systems may not send proper MIME types
                // We'll let the service validate the CSV format
            }
            
            // Validate sample size
            if (sampleSize < 1 || sampleSize > 10000) {
                sampleSize = 100;
            }
            
            // Alimenter le contexte de détection avec le domaine courant (S10.2)
            detectionContext.setDomainId(domainId);

            // Call detection service
            TypeDetectionResult result = detectionService.detectTypes(file, sampleSize, noHeader);
            
            logger.info("TypeDetectionController: Detection successful for domain {} - {} columns detected",
                    domainId, result.getColumns().size());
            
            return ResponseEntity.ok(result);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error for domain {}: {}", domainId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Error detecting types for domain {}", domainId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to detect types: " + e.getMessage());
        }
    }
}
