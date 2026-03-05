package com.movkfact.event;

import com.movkfact.entity.ActivityActionType;

/**
 * Événement pour les activités sur les datasets.
 */
public class DatasetActivityEvent {
    private final Long datasetId;
    private final ActivityActionType action;
    private final String userName;

    public DatasetActivityEvent(Long datasetId, ActivityActionType action, String userName) {
        this.datasetId = datasetId;
        this.action = action;
        this.userName = userName;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public ActivityActionType getAction() {
        return action;
    }

    public String getUserName() {
        return userName;
    }
}