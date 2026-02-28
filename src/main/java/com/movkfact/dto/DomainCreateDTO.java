package com.movkfact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for Domain entity creation/update requests.
 * 
 * Represents the input contract for POST /api/domains and PUT /api/domains/{id} endpoints.
 * Contains only fields that clients can modify (excludes id, timestamps, version for security).
 * 
 * Validation:
 * - name: Required non-blank string
 * - description: Optional, max 2000 characters (matches entity limit)
 */
public class DomainCreateDTO {
    
    @NotBlank(message = "Domain name is required")
    @Size(min = 1, max = 255, message = "Domain name must be between 1 and 255 characters")
    private String name;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    /**
     * No-arg constructor for JSON deserialization
     */
    public DomainCreateDTO() {
    }
    
    /**
     * Constructor with all fields
     */
    public DomainCreateDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    /**
     * Constructor with name only
     */
    public DomainCreateDTO(String name) {
        this.name = name;
        this.description = null;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
