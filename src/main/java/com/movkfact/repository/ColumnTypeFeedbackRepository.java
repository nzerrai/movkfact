package com.movkfact.repository;

import com.movkfact.entity.ColumnTypeFeedback;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour le corpus d'apprentissage adaptatif (S10.1).
 */
@Repository
public interface ColumnTypeFeedbackRepository extends JpaRepository<ColumnTypeFeedback, Long> {

    /**
     * Trouve le meilleur mapping pour un nom normalisé dans un domaine spécifique.
     * Retourne l'entrée avec le count le plus élevé.
     */
    @Query("SELECT f FROM ColumnTypeFeedback f " +
           "WHERE f.columnNameNormalized = :name AND f.domainId = :domainId " +
           "ORDER BY f.count DESC")
    List<ColumnTypeFeedback> findByNameAndDomain(@Param("name") String name,
                                                  @Param("domainId") Long domainId);

    /**
     * Trouve le meilleur mapping global (domain_id IS NULL).
     */
    @Query("SELECT f FROM ColumnTypeFeedback f " +
           "WHERE f.columnNameNormalized = :name AND f.domainId IS NULL " +
           "ORDER BY f.count DESC")
    List<ColumnTypeFeedback> findGlobal(@Param("name") String name);

    /**
     * Trouve une entrée exacte pour upsert, avec verrou pessimiste pour éviter les races.
     * Le domainId peut être null (mapping global) — géré via IS NULL explicite.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM ColumnTypeFeedback f " +
           "WHERE f.columnNameNormalized = :name " +
           "AND f.validatedType = :type " +
           "AND ((:domainId IS NULL AND f.domainId IS NULL) OR f.domainId = :domainId)")
    Optional<ColumnTypeFeedback> findByColumnNameNormalizedAndValidatedTypeAndDomainId(
            @Param("name") String name,
            @Param("type") String type,
            @Param("domainId") Long domainId);
}
