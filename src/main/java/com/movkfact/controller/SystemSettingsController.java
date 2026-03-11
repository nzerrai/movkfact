package com.movkfact.controller;

import com.movkfact.entity.SystemConfiguration;
import com.movkfact.response.ApiResponse;
import com.movkfact.response.ApiErrorResponse;
import com.movkfact.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * REST Controller for System Configuration management.
 * Allows admins to view and manage system-wide configuration parameters.
 * 
 * Endpoints:
 * - GET /api/settings/ → List all settings
 * - GET /api/settings/{key} → Get specific setting
 * - PUT /api/settings/{key} → Update setting
 * - POST /api/settings/ → Create new setting
 */
@RestController
@RequestMapping("/api/settings")
@Tag(name = "System Settings", description = "APIs for managing system configuration parameters")
public class SystemSettingsController {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemSettingsController.class);
    
    @Autowired
    private ConfigurationService configurationService;
    
    /**
     * Get all system configuration parameters.
     */
    @GetMapping
    @Operation(summary = "List all system settings", description = "Retrieves all configuration parameters")
    public ResponseEntity<?> getAllSettings() {
        logger.info("Fetching all system settings");
        var allSettings = configurationService.getAllConfigurations();
        return ResponseEntity.ok(ApiResponse.success(allSettings, "All settings retrieved successfully"));
    }
    
    /**
     * Get a specific configuration by key.
     */
    @GetMapping("/{configKey}")
    @Operation(summary = "Get a specific setting", description = "Retrieves a configuration parameter by its key")
    public ResponseEntity<?> getSetting(@PathVariable String configKey) {
        logger.info("Fetching setting: {}", configKey);
        
        var config = configurationService.getConfiguration(configKey);
        if (config.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("Configuration not found: " + configKey, 404, "/api/settings/" + configKey));
        }
        
        return ResponseEntity.ok(ApiResponse.success(config.get(), "Setting retrieved successfully"));
    }
    
    /**
     * Update or create a configuration parameter.
     * Restricted to ADMIN role (Phase 2: authentication required)
     */
    @PutMapping("/{configKey}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "Update a setting", description = "Updates or creates a configuration parameter")
    public ResponseEntity<?> updateSetting(
            @PathVariable String configKey,
            @Valid @RequestBody SystemConfiguration request) {
        
        logger.info("Updating setting: {} = {}", configKey, request.getConfigValue());
        
        if (!configKey.equals(request.getConfigKey())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("ConfigKey in URL must match request body", 400, "/api/settings/" + configKey));
        }
        
        SystemConfiguration updated = configurationService.saveConfiguration(
            configKey,
            request.getConfigValue(),
            request.getDescription(),
            request.getValueType()
        );
        
        return ResponseEntity.ok(ApiResponse.success(updated, "Setting updated successfully"));
    }
    
    /**
     * Create a new configuration parameter.
     * Restricted to ADMIN role (Phase 2: authentication required)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "Create a new setting", description = "Creates a new configuration parameter")
    public ResponseEntity<?> createSetting(@Valid @RequestBody SystemConfiguration request) {
        logger.info("Creating new setting: {}", request.getConfigKey());
        
        var existing = configurationService.getConfiguration(request.getConfigKey());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("Configuration already exists: " + request.getConfigKey(), 409, "/api/settings"));
        }
        
        SystemConfiguration created = configurationService.saveConfiguration(
            request.getConfigKey(),
            request.getConfigValue(),
            request.getDescription(),
            request.getValueType()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(created, "Setting created successfully"));
    }
}