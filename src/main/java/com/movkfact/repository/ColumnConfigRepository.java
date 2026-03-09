package com.movkfact.repository;

import com.movkfact.entity.ColumnConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnConfigRepository extends JpaRepository<ColumnConfig, Long> {

    List<ColumnConfig> findByDatasetId(Long datasetId);
}
