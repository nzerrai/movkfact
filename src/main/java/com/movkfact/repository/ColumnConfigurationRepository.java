package com.movkfact.repository;

import com.movkfact.entity.ColumnConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ColumnConfiguration entities
 */
@Repository
public interface ColumnConfigurationRepository extends JpaRepository<ColumnConfiguration, Long> {
    
    /**
     * Find all column configurations for a specific domain
     * @param domainId the domain ID
     * @return list of column configurations
     */
    List<ColumnConfiguration> findByDomainId(Long domainId);

    /**
     * Delete all column configurations for a specific domain
     * @param domainId the domain ID
     */
    void deleteByDomainId(Long domainId);

    /**
     * Check if domain has any configurations
     * @param domainId the domain ID
     * @return true if configurations exist
     */
    boolean existsByDomainId(Long domainId);
}
