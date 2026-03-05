package com.movkfact.websocket;

import com.movkfact.websocket.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for broadcasting STOMP WebSocket notifications to clients
 * during batch job lifecycle events.
 *
 * Topics follow pattern: /topic/batch/{jobId}
 * Multiple clients can subscribe to the same jobId topic.
 */
@Service
public class BatchJobNotificationService {

    private static final String TOPIC_PREFIX = "/topic/batch/";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Notifies clients that a batch job has started.
     */
    public void notifyJobStarted(Long jobId, int dataSetCount) {
        JobStartedMessage msg = new JobStartedMessage(jobId, dataSetCount);
        messagingTemplate.convertAndSend(TOPIC_PREFIX + jobId, msg);
    }

    /**
     * Notifies clients of per-dataset progress with estimated remaining time.
     */
    public void notifyProgress(Long jobId, int completed, int total,
                                String currentDataSet, long elapsedMs) {
        JobProgressMessage msg = new JobProgressMessage(
            jobId, completed, total, currentDataSet, elapsedMs);
        messagingTemplate.convertAndSend(TOPIC_PREFIX + jobId, msg);
    }

    /**
     * Notifies clients that a dataset within the job failed (with retry context).
     */
    public void notifyError(Long jobId, String errorMessage,
                             String affectedDataSet, int retryAttempts) {
        JobErrorMessage msg = new JobErrorMessage(
            jobId, errorMessage, affectedDataSet, retryAttempts);
        messagingTemplate.convertAndSend(TOPIC_PREFIX + jobId, msg);
    }

    /**
     * Notifies clients that the batch job has completed (success or partial failure).
     */
    public void notifyJobCompleted(Long jobId, String status, int rowsGenerated,
                                    long durationMs, List<Long> dataSetIds, int skippedCount) {
        JobCompletedMessage msg = new JobCompletedMessage(
            jobId, status, rowsGenerated, durationMs, dataSetIds, skippedCount);
        messagingTemplate.convertAndSend(TOPIC_PREFIX + jobId, msg);
    }
}
