package com.movkfact.repository;

import com.movkfact.entity.Activity;
import com.movkfact.entity.ActivityActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour les entités Activity.
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /**
     * Trouve toutes les activités pour un dataset, triées par timestamp décroissant.
     */
    List<Activity> findByDataSetIdOrderByTimestampDesc(Long dataSetId);

    /**
     * Trouve toutes les activités d'un type spécifique pour un dataset.
     */
    List<Activity> findByDataSetIdAndActionOrderByTimestampDesc(Long dataSetId, ActivityActionType action);
}