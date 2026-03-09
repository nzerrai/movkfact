package com.movkfact.repository;

import com.movkfact.entity.DetectionPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour les patterns de détection de types (S10.2).
 */
@Repository
public interface DetectionPatternRepository extends JpaRepository<DetectionPattern, Long> {

    List<DetectionPattern> findByColumnType(String columnType);
}
