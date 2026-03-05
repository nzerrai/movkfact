package com.movkfact.batch.dto;

import java.util.List;

/**
 * Statut complet d'un batch job.
 */
public class BatchJobStatusDTO {

    private Long jobId;
    private String status;
    private int completedCount;
    private int totalCount;
    private int percentage;
    private int skippedCount;
    private List<String> errors;

    public BatchJobStatusDTO() {}

    public BatchJobStatusDTO(Long jobId, String status, int completedCount,
                              int totalCount, int percentage, int skippedCount,
                              List<String> errors) {
        this.jobId = jobId;
        this.status = status;
        this.completedCount = completedCount;
        this.totalCount = totalCount;
        this.percentage = percentage;
        this.skippedCount = skippedCount;
        this.errors = errors;
    }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }

    public int getSkippedCount() { return skippedCount; }
    public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}
