package com.movkfact.repository;

import com.movkfact.entity.DataSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DataSet entity.
 * Provides CRUD operations and custom queries for soft-deleted dataset tracking.
 */
@Repository
public interface DataSetRepository extends JpaRepository<DataSet, Long> {
    
    /**
     * Find all datasets for a domain that are not soft-deleted.
     * 
     * @param domainId The domain ID
     * @return List of active datasets for the domain
     */
    List<DataSet> findByDomainIdAndDeletedAtIsNull(Long domainId);
    
    /**
     * Find a single dataset by ID that is not soft-deleted.
     * 
     * @param id The dataset ID
     * @return Optional containing the dataset if found and not deleted
     */
    Optional<DataSet> findByIdAndDeletedAtIsNull(Long id);
    
    /**
     * Check if a dataset exists by ID and is not soft-deleted.
     * 
     * @param id The dataset ID
     * @return true if dataset exists and not deleted, false otherwise
     */
    boolean existsByIdAndDeletedAtIsNull(Long id);
    
    /**
     * Check if a dataset with the given name exists for a domain and is not soft-deleted.
     * 
     * @param domainId The domain ID
     * @param name The dataset name
     * @return true if dataset exists and not deleted, false otherwise
     */
    @Query(value = "SELECT CASE WHEN EXISTS (SELECT 1 FROM datasets d WHERE d.domain_id = :domainId AND d.dataset_name = :name AND d.deleted_at IS NULL) THEN true ELSE false END", nativeQuery = true)
    boolean existsByDomainIdAndNameAndDeletedAtIsNull(@Param("domainId") Long domainId, @Param("name") String name);
    
    /**
     * Count all datasets that are not soft-deleted.
     *
     * @return Total count of active datasets
     */
    @Query("SELECT COUNT(d) FROM DataSet d WHERE d.deletedAt IS NULL")
    long countByDeletedAtIsNull();

    /**
     * Returns only the IDs of non-deleted datasets.
     * Used by BatchJobExecutionListener.afterJob to avoid loading full entities.
     *
     * @return List of active dataset IDs
     */
    @Query("SELECT d.id FROM DataSet d WHERE d.deletedAt IS NULL")
    List<Long> findIdsByDeletedAtIsNull();

    /**
     * Find all active datasets for a list of domain IDs (anti-N+1).
     * Used by DomainService.getDomainsWithStats() to load datasets for multiple domains in one query.
     *
     * @param domainIds list of domain IDs
     * @return list of active datasets belonging to any of the given domains
     */
    List<DataSet> findByDomainIdInAndDeletedAtIsNull(List<Long> domainIds);
}
