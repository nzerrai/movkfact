package com.movkfact.service;

import com.movkfact.entity.Activity;
import com.movkfact.entity.ActivityActionType;
import com.movkfact.entity.DataSet;
import com.movkfact.repository.ActivityRepository;
import com.movkfact.repository.DataSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service pour gérer le tracking des activités sur les datasets.
 */
@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    /**
     * Enregistre une activité pour un dataset.
     */
    @Transactional
    public Activity recordActivity(Long dataSetId, ActivityActionType action, String userName) {
        Activity activity = new Activity(dataSetId, action, userName);
        return activityRepository.save(activity);
    }

    /**
     * Récupère l'historique complet des activités pour un dataset.
     */
    public List<Activity> getActivityHistory(Long dataSetId) {
        return activityRepository.findByDataSetIdOrderByTimestampDesc(dataSetId);
    }

    /**
     * Récupère les activités d'un type spécifique pour un dataset.
     */
    public List<Activity> getActivityByType(Long dataSetId, ActivityActionType action) {
        return activityRepository.findByDataSetIdAndActionOrderByTimestampDesc(dataSetId, action);
    }

    /**
     * Enregistre une activité row-level (ROW_MODIFIED ou ROW_DELETED).
     */
    @Transactional
    public Activity recordRowActivity(Long dataSetId, ActivityActionType action, String userName,
                                      Integer rowIndex, String modifiedColumns, String previousValue) {
        Activity activity = new Activity(dataSetId, action, userName, rowIndex, modifiedColumns, previousValue);
        return activityRepository.save(activity);
    }

    /**
     * Réinitialise un dataset à sa version originale.
     */
    @Transactional
    public DataSet resetDataSet(Long dataSetId) {
        DataSet dataset = dataSetRepository.findByIdAndDeletedAtIsNull(dataSetId)
            .orElseThrow(() -> new IllegalArgumentException("Dataset not found with id: " + dataSetId));

        if (dataset.getOriginalData() != null) {
            dataset.setDataJson(dataset.getOriginalData());
            dataset.setVersion(0);
            return dataSetRepository.save(dataset);
        }
        throw new IllegalStateException("Original data not found for dataset: " + dataSetId);
    }
}