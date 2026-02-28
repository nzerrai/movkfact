package com.movkfact.repository;

import com.movkfact.entity.DataSet;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
