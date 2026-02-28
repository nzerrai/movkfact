package com.movkfact.repository;

import com.movkfact.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Domain entity.
 *
 * Provides typed CRUD operations and custom finder methods for Domain entities.
 * All repository methods integrate H2 database access with automatic transaction management.
 * Leverages Spring Data JPA's query derivation to auto-implement methods based on naming convention.
 *
 * Query Methods:
 * - Standard CRUD: save(), findById(), findAll(), delete(), count()
 * - Custom finders: existsByName(), findByNameIgnoreCase(), findByDeletedAtIsNull()
 * - Soft delete aware: findByIdAndDeletedAtIsNull(), findByNameIgnoreCaseAndDeletedAtIsNull()
 *
 * Performance Notes:
 * - Queries on name field use index idx_domain_name for O(log N) performance
 * - Queries filtering by deleted_at use index idx_domain_deleted_at
 *
 * Usage Example:
 * <pre>
 *   {@code @Autowired}
 *   private DomainRepository domainRepository;
 *
 *   // Find active domain by name (case-insensitive)
 *   Optional&lt;Domain&gt; domain = domainRepository.findByNameIgnoreCaseAndDeletedAtIsNull("MyDomain");
 *
 *   // List all active domains
 *   List&lt;Domain&gt; activeDomains = domainRepository.findByDeletedAtIsNull();
 * </pre>
 * 
 * @author Amelia (Developer Agent)
 * @version 1.0
 * @see com.movkfact.entity.Domain
 */
@Repository
public interface DomainRepository extends JpaRepository<Domain, Long> {

    /**
     * Checks if a domain with the given name exists in the database (active or deleted).
     * Uses database index on name column for fast lookup.
     *
     * @param name the domain name to check (case-sensitive)
     * @return true if a domain with this exact name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Checks if an active (non-deleted) domain with the given name exists.
     * Filters by both name match and soft-delete status.
     *
     * @param name the domain name to check (case-sensitive)
     * @return true if an active domain with this name exists, false otherwise
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Finds a domain by name with case-insensitive matching across all domains (including deleted).
     * Uses database index on name column.
     * Useful for admin/recovery operations on archived domains.
     *
     * @param name the domain name (case-insensitive, uses DB LOWER function)
     * @return Optional containing the domain if found (regardless of deleted status), empty otherwise
     */
    Optional<Domain> findByNameIgnoreCase(String name);

    /**
     * Finds an active (non-deleted) domain by name with case-insensitive matching.
     * Primary method for user-facing domain lookups.
     * Returns only if deletedAt IS NULL.
     *
     * @param name the domain name (case-insensitive)
     * @return Optional containing the domain if found and not deleted, empty otherwise
     */
    Optional<Domain> findByNameIgnoreCaseAndDeletedAtIsNull(String name);

    /**
     * Retrieves all active (non-deleted) domains.
     * Filters by deleted_at IS NULL condition at the database level.
     * Uses index idx_domain_deleted_at for efficient filtering.
     * Standard method for listing all user-visible domains.
     *
     * @return List of active Domain entities; empty list if no active domains exist
     */
    List<Domain> findByDeletedAtIsNull();

    /**
     * Retrieves a domain by ID only if it is active (not soft-deleted).
     * Combined filtering for ID and soft-delete status.
     * Stronger than findById() as it enforces soft-delete filtering.
     *
     * @param id the domain ID
     * @return Optional containing the active domain if found, empty otherwise (or if deleted)
     */
    Optional<Domain> findByIdAndDeletedAtIsNull(Long id);
}
