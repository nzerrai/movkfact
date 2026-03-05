package com.movkfact.event;

import com.movkfact.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener pour enregistrer automatiquement les activités sur les datasets.
 */
@Component
public class DatasetActivityEventListener {

    @Autowired
    private ActivityService activityService;

    /**
     * Écoute les événements d'activité et enregistre dans la base de données.
     */
    @EventListener
    @Async
    public void handleDatasetActivity(DatasetActivityEvent event) {
        try {
            activityService.recordActivity(event.getDatasetId(), event.getAction(), event.getUserName());
        } catch (Exception e) {
            // Log l'erreur mais ne bloque pas l'opération principale
            System.err.println("Failed to record activity: " + e.getMessage());
        }
    }
}