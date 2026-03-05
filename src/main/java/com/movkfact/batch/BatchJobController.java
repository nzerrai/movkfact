package com.movkfact.batch;

import com.movkfact.batch.dto.*;
import com.movkfact.entity.JobStatus;
import com.movkfact.repository.JobStatusRepository;
import com.movkfact.response.ApiErrorResponse;
import com.movkfact.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for Spring Batch Generation operations.
 *
 * Endpoints:
 * - POST /api/batch/generate   → Lance un batch job (async)
 * - GET  /api/batch/{jobId}    → Statut du job
 * - GET  /api/batch/{jobId}/progress → Progression temps réel
 */
@RestController
@RequestMapping("/api/batch")
@Tag(name = "Batch Generation", description = "APIs for parallel dataset batch generation")
public class BatchJobController {

    @Autowired
    @Qualifier("asyncJobLauncher")
    private TaskExecutorJobLauncher asyncJobLauncher;

    @Autowired
    @Qualifier("batchGenerationJob")
    private Job batchGenerationJob;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private BatchJobConfigStore configStore;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    // ============================================================================
    // POST /api/batch/generate
    // ============================================================================

    @PostMapping("/generate")
    @Operation(
        summary = "Launch batch dataset generation",
        description = "Submits a batch job to generate multiple datasets in parallel (4 workers). " +
                      "Returns immediately with a jobId to track progress."
    )
    public ResponseEntity<?> generateBatch(@Valid @RequestBody BatchGenerationRequestDTO request) {
        try {
            List<BatchDataSetConfigDTO> configs = request.getDataSetConfigs();
            String configKey = UUID.randomUUID().toString();

            // Store configs for the reader to retrieve during step execution
            configStore.storeConfigs(configKey, configs);

            // Build JobParameters
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("configKey", configKey)
                .addLong("timestamp", System.currentTimeMillis())  // ensures unique job instance
                .toJobParameters();

            // Launch async (returns immediately)
            JobExecution execution = asyncJobLauncher.run(batchGenerationJob, jobParameters);

            // Store total count for progress calculations
            configStore.storeTotalCount(execution.getId(), configs.size());

            BatchJobResponseDTO response = new BatchJobResponseDTO(
                execution.getId(),
                execution.getStatus().name(),
                configs.size(),
                "Batch job submitted. Use GET /api/batch/" + execution.getId() + " to track status."
            );

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                ApiResponse.success(response, "Batch job launched")
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("Failed to launch batch job: " + e.getMessage(), 500, "/api/batch/generate"));
        }
    }

    // ============================================================================
    // GET /api/batch/{jobId}
    // ============================================================================

    @GetMapping("/{jobId}")
    @Operation(
        summary = "Get batch job status",
        description = "Returns the current status of a batch job including completed/total counts."
    )
    public ResponseEntity<?> getJobStatus(
            @Parameter(description = "The batch job execution ID") @PathVariable Long jobId) {

        JobExecution execution = jobExplorer.getJobExecution(jobId);
        if (execution == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("Batch job not found with id: " + jobId, 404, "/api/batch/" + jobId));
        }

        BatchJobStatusDTO status = buildJobStatus(execution);
        return ResponseEntity.ok(ApiResponse.success(status, "Job status retrieved"));
    }

    // ============================================================================
    // GET /api/batch/{jobId}/progress
    // ============================================================================

    @GetMapping("/{jobId}/progress")
    @Operation(
        summary = "Get batch job progress",
        description = "Returns real-time progress of a batch job with estimated remaining time."
    )
    public ResponseEntity<?> getJobProgress(
            @Parameter(description = "The batch job execution ID") @PathVariable Long jobId) {

        JobExecution execution = jobExplorer.getJobExecution(jobId);
        if (execution == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("Batch job not found with id: " + jobId, 404, "/api/batch/" + jobId + "/progress"));
        }

        BatchJobProgressDTO progress = buildJobProgress(execution);
        return ResponseEntity.ok(ApiResponse.success(progress, "Job progress retrieved"));
    }

    // ============================================================================
    // GET /api/batch/{jobId}/full-status — Persistent state (for WS reconnection)
    // ============================================================================

    @GetMapping("/{jobId}/full-status")
    @Operation(
        summary = "Get persistent batch job status",
        description = "Returns the persisted job status from DB. Used by frontend to recover state after WebSocket reconnection."
    )
    public ResponseEntity<?> getFullStatus(
            @Parameter(description = "The batch job execution ID") @PathVariable Long jobId) {

        return jobStatusRepository.findById(jobId)
            .<ResponseEntity<?>>map(status -> ResponseEntity.ok(ApiResponse.success(status, "Full status retrieved")))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("Batch job not found with id: " + jobId, 404,
                        "/api/batch/" + jobId + "/full-status")));
    }

    // ============================================================================
    // Helpers
    // ============================================================================

    private BatchJobStatusDTO buildJobStatus(JobExecution execution) {
        int completed = getWriteCount(execution);
        int skipped = getSkipCount(execution);
        int total = configStore.getTotalCount(execution.getId());
        int percentage = total > 0 ? (int) ((completed * 100.0) / total) : 0;

        List<String> errors = new ArrayList<>();
        execution.getAllFailureExceptions().forEach(e -> errors.add(e.getMessage()));

        return new BatchJobStatusDTO(
            execution.getId(),
            execution.getStatus().name(),
            completed,
            total,
            percentage,
            skipped,
            errors
        );
    }

    private BatchJobProgressDTO buildJobProgress(JobExecution execution) {
        int completed = getWriteCount(execution);
        int total = configStore.getTotalCount(execution.getId());
        int percentage = total > 0 ? (int) ((completed * 100.0) / total) : 0;

        long elapsedMs = execution.getStartTime() != null
            ? System.currentTimeMillis() - execution.getStartTime().toInstant(
                java.time.ZoneOffset.UTC).toEpochMilli()
            : 0;

        long estimatedRemaining = 0;
        if (completed > 0 && completed < total) {
            long msPerItem = elapsedMs / completed;
            estimatedRemaining = (msPerItem * (total - completed)) / 1000;
        }

        return new BatchJobProgressDTO(
            execution.getId(),
            execution.getStatus().name(),
            completed,
            total,
            percentage,
            estimatedRemaining
        );
    }

    private int getWriteCount(JobExecution execution) {
        return (int) execution.getStepExecutions().stream()
            .mapToLong(StepExecution::getWriteCount)
            .sum();
    }

    private int getSkipCount(JobExecution execution) {
        return (int) execution.getStepExecutions().stream()
            .mapToLong(StepExecution::getProcessSkipCount)
            .sum();
    }
}
