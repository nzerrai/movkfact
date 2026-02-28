package com.movkfact.controller;

import com.movkfact.dto.DomainCreateDTO;
import com.movkfact.dto.DomainResponseDTO;
import com.movkfact.entity.Domain;
import com.movkfact.exception.EntityNotFoundException;
import com.movkfact.repository.DomainRepository;
import com.movkfact.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Domain entity CRUD operations.
 * 
 * Endpoints:
 * - POST /api/domains → Create domain (201 Created)
 * - GET /api/domains → List all active domains (200 OK)
 * - GET /api/domains/{id} → Get single domain (200 OK or 404)
 * - PUT /api/domains/{id} → Update domain (200 OK or 404)
 * - DELETE /api/domains/{id} → Soft delete domain (204 No Content)
 * 
 * All GET requests exclude soft-deleted domains (deletedAt IS NULL).
 * All responses wrapped in ApiResponse with standardized format.
 * Validations applied via @Valid on DTOs.
 * Exceptions handled by GlobalExceptionHandler.
 * 
 * FUTURE: Extract business logic to DomainService when adding:
 * - Authorization checks
 * - Business rule validation
 * - Notifications
 * - Audit logging
 */
@RestController
@RequestMapping("/api/domains")
public class DomainController {
    
    @Autowired
    private DomainRepository domainRepository;
    
    /**
     * POST /api/domains - Create new domain
     * 
     * @param dto DomainCreateDTO with name and optional description
     * @return 201 Created with Location header and created domain
     * @throws DataIntegrityViolationException if name already exists
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DomainResponseDTO>> createDomain(
            @Valid @RequestBody DomainCreateDTO dto) {
        
        // Check for duplicate name
        if (domainRepository.existsByNameAndDeletedAtIsNull(dto.getName())) {
            throw new DataIntegrityViolationException("Domain name already exists");
        }
        
        // Map DTO to entity
        Domain domain = new Domain(dto.getName(), dto.getDescription());
        
        // Save to database
        Domain saved = domainRepository.save(domain);
        
        // Map entity to response DTO
        DomainResponseDTO responseDTO = mapToResponseDTO(saved);
        
        // Return 201 Created with Location header
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(saved.getId())
                        .toUri())
                .body(ApiResponse.success(responseDTO, "Domain created successfully"));
    }
    
    /**
     * GET /api/domains - List all active (non-deleted) domains
     * 
     * Optional pagination via query parameters:
     * @param offset starting index (default 0)
     * @param limit maximum items to return (default 100)
     * @return 200 OK with list of domains
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DomainResponseDTO>>> getAllDomains(
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "100") Integer limit) {
        
        // Fetch all active domains
        List<Domain> allDomains = domainRepository.findByDeletedAtIsNull();
        
        // Apply simple pagination
        int end = Math.min(offset + limit, allDomains.size());
        List<Domain> paginated = allDomains.subList(
                Math.min(offset, allDomains.size()),
                end
        );
        
        // Map to response DTOs
        List<DomainResponseDTO> dtos = paginated.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(
                ApiResponse.success(dtos, "Domains retrieved successfully")
        );
    }
    
    /**
     * GET /api/domains/{id} - Get single domain by ID
     * 
     * @param id domain ID
     * @return 200 OK with domain, or 404 Not Found if not exists or deleted
     * @throws EntityNotFoundException if domain not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DomainResponseDTO>> getDomainById(
            @PathVariable Long id) {
        
        Domain domain = domainRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Domain not found with ID: " + id));
        
        DomainResponseDTO dto = mapToResponseDTO(domain);
        
        return ResponseEntity.ok(
                ApiResponse.success(dto, "Domain retrieved successfully")
        );
    }
    
    /**
     * PUT /api/domains/{id} - Update domain
     * 
     * @param id domain ID to update
     * @param dto DomainCreateDTO with updated name and/or description
     * @return 200 OK with updated domain, or 404 Not Found if not exists
     * @throws EntityNotFoundException if domain not found or deleted
     * @throws DataIntegrityViolationException if new name conflicts with another domain
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DomainResponseDTO>> updateDomain(
            @PathVariable Long id,
            @Valid @RequestBody DomainCreateDTO dto) {
        
        // Find existing domain (active only)
        Domain domain = domainRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Domain not found with ID: " + id));
        
        // Check for duplicate name only if name is being changed
        if (!domain.getName().equals(dto.getName()) &&
            domainRepository.existsByNameAndDeletedAtIsNull(dto.getName())) {
            throw new DataIntegrityViolationException("Domain name already exists");
        }
        
        // Update fields
        domain.setName(dto.getName());
        domain.setDescription(dto.getDescription());
        
        // Save updated entity (JPA @Version auto-increments for optimistic locking)
        Domain updated = domainRepository.save(domain);
        
        // Map to response DTO
        DomainResponseDTO responseDTO = mapToResponseDTO(updated);
        
        return ResponseEntity.ok(
                ApiResponse.success(responseDTO, "Domain updated successfully")
        );
    }
    
    /**
     * DELETE /api/domains/{id} - Soft delete domain
     * 
     * Marks domain as deleted via deletedAt timestamp but doesn't remove from DB (soft delete).
     * Deleted domains excluded from all GET requests.
     * 
     * @param id domain ID to delete
     * @return 204 No Content, or 404 Not Found if not exists
     * @throws EntityNotFoundException if domain not found or already deleted
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDomain(@PathVariable Long id) {
        
        // Find existing domain (active only)
        Domain domain = domainRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Domain not found with ID: " + id));
        
        // Soft delete: set deletedAt timestamp
        domain.softDelete();
        
        // Save
        domainRepository.save(domain);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Helper method: Map Domain entity to DomainResponseDTO
     */
    private DomainResponseDTO mapToResponseDTO(Domain domain) {
        return new DomainResponseDTO(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getDeletedAt()
        );
    }
}
