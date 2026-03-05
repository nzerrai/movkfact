package com.movkfact.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Persistent job status for batch jobs (S3.3).
 *
 * Enables recovery after WebSocket reconnection: clients can query
 * GET /api/batch/{jobId}/full-status to restore their UI state.
 */
@Entity
@Table(name = "job_status")
public class JobStatus {

    @Id
    private Long jobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatusType status;

    @Column
    private Integer progress;   // 0-100

    @Column
    private Integer completed;  // datasets completed

    @Column
    private Integer total;      // total datasets

    @Column
    private Integer rowsGenerated;

    @Column
    private Integer errorCount;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    // Transient: populated from DataSetRepository on full-status query
    @Transient
    private List<Long> dataSetIds;

    public JobStatus() {}

    public JobStatus(Long jobId, int total) {
        this.jobId = jobId;
        this.status = JobStatusType.RUNNING;
        this.progress = 0;
        this.completed = 0;
        this.total = total;
        this.rowsGenerated = 0;
        this.errorCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public JobStatusType getStatus() { return status; }
    public void setStatus(JobStatusType status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Integer getCompleted() { return completed; }
    public void setCompleted(Integer completed) { this.completed = completed; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Integer getRowsGenerated() { return rowsGenerated; }
    public void setRowsGenerated(Integer rowsGenerated) { this.rowsGenerated = rowsGenerated; }

    public Integer getErrorCount() { return errorCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<Long> getDataSetIds() { return dataSetIds; }
    public void setDataSetIds(List<Long> dataSetIds) { this.dataSetIds = dataSetIds; }
}
