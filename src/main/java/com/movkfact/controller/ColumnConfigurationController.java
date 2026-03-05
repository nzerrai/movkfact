package com.movkfact.controller;

import com.movkfact.service.ColumnConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Column Configuration API
 * Endpoints for saving and retrieving column configurations from CSV uploads
 */
@RestController
@RequestMapping("/api/domains/{domainId}/columns")
@Tag(name = "Column Configuration", description = "Column configuration management API")
@CrossOrigin(origins = "*")
public class ColumnConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(ColumnConfigurationController.class);

    @Autowired
    private ColumnConfigurationService columnConfigurationService;

    /**
     * Save column configurations for a domain
     * Replaces existing configurations
     *
     * @param domainId the domain ID
     * @param columns list of column configuration objects
     * @return success response with saved configurations
     */
    @PostMapping("/save-configuration")
    @Operation(summary = "Save column configurations", description = "Save detected column types and configurations for a domain")
    public ResponseEntity<Map<String, Object>> saveColumnConfigurations(
            @PathVariable Long domainId,
            @RequestBody List<Map<String, Object>> columns) {

        logger.info("Received request to save {} column configurations for domain {}", columns.size(), domainId);

        try {
            columnConfigurationService.saveColumnConfigurations(domainId, columns);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Column configurations saved successfully");
            response.put("domainId", domainId);
            response.put("columnsCount", columns.size());

            logger.info("Successfully saved column configurations for domain {}", domainId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error saving column configurations for domain {}: {}", domainId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to save configurations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get column configurations for a domain
     *
     * @param domainId the domain ID
     * @return list of column configurations
     */
    @GetMapping("/configuration")
    @Operation(summary = "Get column configurations", description = "Retrieve saved column configurations for a domain")
    public ResponseEntity<Map<String, Object>> getColumnConfigurations(@PathVariable Long domainId) {

        logger.debug("Fetching column configurations for domain {}", domainId);

        try {
            List<Map<String, ? extends Object>> configurations = columnConfigurationService.getColumnConfigurationsAsMap(domainId);
            boolean hasConfigurations = !configurations.isEmpty();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("domainId", domainId);
            response.put("hasConfigurations", hasConfigurations);
            response.put("columnsCount", configurations.size());
            response.put("columns", configurations);

            logger.debug("Retrieved {} column configurations for domain {}", configurations.size(), domainId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving column configurations for domain {}: {}", domainId, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve configurations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Check if domain has configurations
     *
     * @param domainId the domain ID
     * @return status object
     */
    @GetMapping("/has-configuration")
    @Operation(summary = "Check if domain has configurations", description = "Check if a domain has saved column configurations")
    public ResponseEntity<Map<String, Object>> hasConfigurations(@PathVariable Long domainId) {

        try {
            boolean hasConfigurations = columnConfigurationService.hasConfigurations(domainId);

            Map<String, Object> response = new HashMap<>();
            response.put("domainId", domainId);
            response.put("hasConfigurations", hasConfigurations);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error checking configurations for domain {}: {}", domainId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to check configurations");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete column configurations for a domain
     *
     * @param domainId the domain ID
     * @return success response
     */
    @DeleteMapping("/configuration")
    @Operation(summary = "Delete column configurations", description = "Delete all column configurations for a domain")
    public ResponseEntity<Map<String, Object>> deleteColumnConfigurations(@PathVariable Long domainId) {

        logger.info("Received request to delete column configurations for domain {}", domainId);

        try {
            columnConfigurationService.deleteColumnConfigurations(domainId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Column configurations deleted successfully");
            response.put("domainId", domainId);

            logger.info("Successfully deleted column configurations for domain {}", domainId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting column configurations for domain {}: {}", domainId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete configurations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
