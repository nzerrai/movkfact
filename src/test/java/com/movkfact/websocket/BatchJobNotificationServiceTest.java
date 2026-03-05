package com.movkfact.websocket;

import com.movkfact.websocket.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BatchJobNotificationService (S3.3 — WebSocket notifications).
 *
 * Verifies that the correct STOMP topic and message payload are sent
 * for each batch job lifecycle event.
 */
@ExtendWith(MockitoExtension.class)
class BatchJobNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BatchJobNotificationService notificationService;

    private static final Long JOB_ID = 42L;
    private static final String TOPIC_PREFIX = "/topic/batch/";

    // ────────────────────────────────────────────────────────────────────────
    // notifyJobStarted
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void notifyJobStarted_sendsToCorrectTopic() {
        notificationService.notifyJobStarted(JOB_ID, 5);

        verify(messagingTemplate).convertAndSend(
                eq(TOPIC_PREFIX + JOB_ID), any(JobStartedMessage.class));
    }

    @Test
    void notifyJobStarted_messageHasCorrectType() {
        ArgumentCaptor<JobStartedMessage> captor = ArgumentCaptor.forClass(JobStartedMessage.class);

        notificationService.notifyJobStarted(JOB_ID, 3);

        verify(messagingTemplate).convertAndSend(eq(TOPIC_PREFIX + JOB_ID), captor.capture());
        JobStartedMessage msg = captor.getValue();

        assertEquals("job_started", msg.getType());
        assertEquals(JOB_ID, msg.getJobId());
        assertEquals(3, msg.getDataSetCount());
        assertNotNull(msg.getTimestamp());
    }

    @Test
    void notifyJobStarted_estimatedDurationIsPositive() {
        ArgumentCaptor<JobStartedMessage> captor = ArgumentCaptor.forClass(JobStartedMessage.class);

        notificationService.notifyJobStarted(JOB_ID, 10);

        verify(messagingTemplate).convertAndSend(eq(TOPIC_PREFIX + JOB_ID), captor.capture());
        assertTrue(captor.getValue().getEstimatedDuration() > 0);
    }

    // ────────────────────────────────────────────────────────────────────────
    // notifyProgress
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void notifyProgress_sendsToCorrectTopic() {
        notificationService.notifyProgress(JOB_ID, 2, 5, "Dataset #2", 2000L);

        verify(messagingTemplate).convertAndSend(
                eq(TOPIC_PREFIX + JOB_ID), any(JobProgressMessage.class));
    }

    @Test
    void notifyProgress_messageHasCorrectFields() {
        ArgumentCaptor<JobProgressMessage> captor = ArgumentCaptor.forClass(JobProgressMessage.class);

        notificationService.notifyProgress(JOB_ID, 3, 5, "Dataset #3", 3000L);

        verify(messagingTemplate).convertAndSend(eq(TOPIC_PREFIX + JOB_ID), captor.capture());
        JobProgressMessage msg = captor.getValue();

        assertEquals("job_progress_update", msg.getType());
        assertEquals(JOB_ID, msg.getJobId());
        assertEquals(3, msg.getCompleted());
        assertEquals(5, msg.getTotal());
        assertEquals(60, msg.getPercentage()); // 3/5 * 100 = 60
        assertEquals("Dataset #3", msg.getCurrentDataSet());
        assertNotNull(msg.getTimestamp());
    }

    @Test
    void notifyProgress_zeroTotal_percentageIsZero() {
        ArgumentCaptor<JobProgressMessage> captor = ArgumentCaptor.forClass(JobProgressMessage.class);

        notificationService.notifyProgress(JOB_ID, 0, 0, null, 0L);

        verify(messagingTemplate).convertAndSend(eq(TOPIC_PREFIX + JOB_ID), captor.capture());
        assertEquals(0, captor.getValue().getPercentage());
    }

    // ────────────────────────────────────────────────────────────────────────
    // notifyError
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void notifyError_sendsToCorrectTopic() {
        notificationService.notifyError(JOB_ID, "Generation failed", "Dataset #2", 2);

        verify(messagingTemplate).convertAndSend(
                eq(TOPIC_PREFIX + JOB_ID), any(JobErrorMessage.class));
    }

    @Test
    void notifyError_messageHasCorrectFields() {
        ArgumentCaptor<JobErrorMessage> captor = ArgumentCaptor.forClass(JobErrorMessage.class);

        notificationService.notifyError(JOB_ID, "NullPointerException", "Dataset #1", 3);

        verify(messagingTemplate).convertAndSend(eq(TOPIC_PREFIX + JOB_ID), captor.capture());
        JobErrorMessage msg = captor.getValue();

        assertEquals("job_error", msg.getType());
        assertEquals(JOB_ID, msg.getJobId());
        assertEquals("NullPointerException", msg.getErrorMessage());
        assertEquals("Dataset #1", msg.getAffectedDataSet());
        assertEquals(3, msg.getRetryAttempts());
        assertNotNull(msg.getTimestamp());
    }

    // ────────────────────────────────────────────────────────────────────────
    // notifyJobCompleted
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void notifyJobCompleted_sendsToCorrectTopic() {
        notificationService.notifyJobCompleted(JOB_ID, "SUCCESS", 500, 3000L, List.of(1L, 2L, 3L), 0);

        verify(messagingTemplate).convertAndSend(
                eq(TOPIC_PREFIX + JOB_ID), any(JobCompletedMessage.class));
    }

    @Test
    void notifyJobCompleted_successMessage_hasCorrectFields() {
        ArgumentCaptor<JobCompletedMessage> captor = ArgumentCaptor.forClass(JobCompletedMessage.class);
        List<Long> ids = List.of(10L, 20L, 30L);

        notificationService.notifyJobCompleted(JOB_ID, "SUCCESS", 1500, 45000L, ids, 0);

        verify(messagingTemplate).convertAndSend(eq(TOPIC_PREFIX + JOB_ID), captor.capture());
        JobCompletedMessage msg = captor.getValue();

        assertEquals("job_completed", msg.getType());
        assertEquals(JOB_ID, msg.getJobId());
        assertEquals("SUCCESS", msg.getStatus());
        assertEquals(1500, msg.getRowsGenerated());
        assertEquals(45L, msg.getDuration()); // 45000ms → 45s
        assertEquals(ids, msg.getDataSetIds());
        assertEquals(0, msg.getSkippedCount());
        assertNotNull(msg.getTimestamp());
    }

    @Test
    void notifyJobCompleted_partialFailure_hasCorrectStatus() {
        ArgumentCaptor<JobCompletedMessage> captor = ArgumentCaptor.forClass(JobCompletedMessage.class);

        notificationService.notifyJobCompleted(JOB_ID, "PARTIAL_FAILURE", 200, 10000L, List.of(5L), 2);

        verify(messagingTemplate).convertAndSend(eq(TOPIC_PREFIX + JOB_ID), captor.capture());
        assertEquals("PARTIAL_FAILURE", captor.getValue().getStatus());
        assertEquals(2, captor.getValue().getSkippedCount());
    }

    @Test
    void notifyJobCompleted_neverInvokesOtherTopics() {
        notificationService.notifyJobCompleted(JOB_ID, "SUCCESS", 100, 1000L, List.of(), 0);

        // Only the exact topic for this jobId should be used
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), (Object) any());
        verify(messagingTemplate, never()).convertAndSend(
                eq(TOPIC_PREFIX + (JOB_ID + 1)), (Object) any());
    }
}
