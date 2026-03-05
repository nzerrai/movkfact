package com.movkfact.batch;

import com.movkfact.batch.dto.BatchDataSetConfigDTO;
import com.movkfact.entity.DataSet;
import com.movkfact.entity.JobStatus;
import com.movkfact.entity.JobStatusType;
import com.movkfact.repository.DataSetRepository;
import com.movkfact.repository.JobStatusRepository;
import com.movkfact.websocket.BatchJobNotificationService;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Batch listener integrating WebSocket notifications with persistent job status (S3.3).
 *
 * Implements:
 * - JobExecutionListener: job start/end notifications + DB persistence
 * - ChunkListener: per-dataset progress notifications
 * - SkipListener: error/retry notifications when a dataset is skipped after retries
 */
@Component
public class BatchJobExecutionListener
        implements JobExecutionListener,
                   ChunkListener,
                   SkipListener<BatchDataSetConfigDTO, DataSet> {

    @Autowired
    private BatchJobNotificationService notificationService;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private BatchJobConfigStore configStore;

    /**
     * ThreadLocal storing the current job ID during chunk/skip processing.
     * Set in beforeChunk() so onSkipInProcess/onSkipInWrite can send notifications
     * without requiring StepExecution injection (which is unavailable in SkipListener).
     */
    private final ThreadLocal<Long> currentJobId = new ThreadLocal<>();

    // ============================================================================
    // JobExecutionListener
    // ============================================================================

    @Override
    public void beforeJob(JobExecution jobExecution) {
        Long jobId = jobExecution.getId();

        // FIX C1: derive total from configKey (stored before asyncJobLauncher.run())
        // instead of getTotalCount(jobId) which may not be set yet due to race condition.
        String configKey = jobExecution.getJobParameters().getString("configKey");
        List<BatchDataSetConfigDTO> configs = configStore.retrieveConfigs(configKey);
        int total = configs.size();

        // Make total available for afterChunk and controller queries
        configStore.storeTotalCount(jobId, total);

        // Persist initial status
        JobStatus status = new JobStatus(jobId, total);
        jobStatusRepository.save(status);

        // Notify clients
        notificationService.notifyJobStarted(jobId, total);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Long jobId = jobExecution.getId();

        JobStatus status = jobStatusRepository.findById(jobId).orElse(null);
        if (status == null) return;

        // Determine terminal status
        String batchStatus = jobExecution.getStatus().name();
        boolean isSuccess = "COMPLETED".equals(batchStatus);
        JobStatusType finalStatus = isSuccess ? JobStatusType.COMPLETED : JobStatusType.FAILED;

        status.setStatus(finalStatus);
        status.setCompletedAt(LocalDateTime.now());

        // Count written datasets and skips
        int written = (int) jobExecution.getStepExecutions().stream()
            .mapToLong(se -> se.getWriteCount()).sum();
        int skipped = (int) jobExecution.getStepExecutions().stream()
            .mapToLong(se -> se.getProcessSkipCount()).sum();
        status.setCompleted(written);
        status.setProgress(status.getTotal() > 0
            ? (int) ((written * 100.0) / status.getTotal()) : 100);

        jobStatusRepository.save(status);

        // Collect generated dataset IDs (only non-deleted)
        List<Long> dataSetIds = dataSetRepository.findIdsByDeletedAtIsNull();

        // FIX M1: compute actual rows from configs (sum of count per dataset config)
        String configKey = jobExecution.getJobParameters().getString("configKey");
        int actualRowsGenerated = configStore.retrieveConfigs(configKey).stream()
            .mapToInt(BatchDataSetConfigDTO::getCount)
            .sum();

        long durationMs = jobExecution.getStartTime() != null && jobExecution.getEndTime() != null
            ? java.time.Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis()
            : 0;

        notificationService.notifyJobCompleted(
            jobId,
            isSuccess ? "SUCCESS" : "PARTIAL_FAILURE",
            actualRowsGenerated,
            durationMs,
            dataSetIds,
            skipped
        );
    }

    // ============================================================================
    // ChunkListener — called after each chunk (1 dataset = 1 chunk)
    // ============================================================================

    @Override
    public void beforeChunk(ChunkContext context) {
        // FIX C2: capture jobId in ThreadLocal so SkipListener methods can use it
        Long jobId = context.getStepContext().getStepExecution().getJobExecution().getId();
        currentJobId.set(jobId);
    }

    @Override
    public void afterChunk(ChunkContext context) {
        var stepExecution = context.getStepContext().getStepExecution();
        var jobExecution = stepExecution.getJobExecution();
        Long jobId = jobExecution.getId();

        int completed = (int) stepExecution.getWriteCount();
        int total = configStore.getTotalCount(jobId);

        if (total <= 0) return;

        // Update persistent status
        jobStatusRepository.findById(jobId).ifPresent(status -> {
            status.setCompleted(completed);
            status.setProgress((int) ((completed * 100.0) / total));
            jobStatusRepository.save(status);
        });

        // Calculate elapsed time for ETA
        long elapsedMs = jobExecution.getStartTime() != null
            ? System.currentTimeMillis() - jobExecution.getStartTime()
                .toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
            : 0;

        notificationService.notifyProgress(
            jobId, completed, total,
            "Dataset #" + completed,
            elapsedMs
        );
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        currentJobId.remove(); // clean up ThreadLocal on chunk error
    }

    // ============================================================================
    // SkipListener — called when retry limit exhausted and item is skipped
    // FIX C2: notify clients via WebSocket + persist error in JobStatus
    // ============================================================================

    @Override
    public void onSkipInProcess(BatchDataSetConfigDTO item, Throwable t) {
        Long jobId = currentJobId.get();
        if (jobId == null) return;

        String dataSetName = item != null ? item.getDatasetName() : "Unknown";
        String errorMsg = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();

        notificationService.notifyError(jobId, errorMsg, dataSetName, 3);

        jobStatusRepository.findById(jobId).ifPresent(status -> {
            status.setErrorCount((status.getErrorCount() == null ? 0 : status.getErrorCount()) + 1);
            status.setLastError(errorMsg);
            jobStatusRepository.save(status);
        });
    }

    @Override
    public void onSkipInWrite(DataSet item, Throwable t) {
        Long jobId = currentJobId.get();
        if (jobId == null) return;

        String dataSetName = item != null ? item.getName() : "Unknown";
        String errorMsg = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();

        notificationService.notifyError(jobId, errorMsg, dataSetName, 3);

        jobStatusRepository.findById(jobId).ifPresent(status -> {
            status.setErrorCount((status.getErrorCount() == null ? 0 : status.getErrorCount()) + 1);
            status.setLastError(errorMsg);
            jobStatusRepository.save(status);
        });
    }

    @Override
    public void onSkipInRead(Throwable t) {
        // Not expected in this reader (ConcurrentLinkedQueue never throws)
    }
}
