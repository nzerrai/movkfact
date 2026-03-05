package com.movkfact.batch.dto;

/**
 * Progression temps réel d'un batch job.
 */
public class BatchJobProgressDTO {

    private Long jobId;
    private String status;
    private int completed;
    private int total;
    private int percentage;
    private long estimatedRemainingSeconds;

    public BatchJobProgressDTO() {}

    public BatchJobProgressDTO(Long jobId, String status, int completed, int total,
                                int percentage, long estimatedRemainingSeconds) {
        this.jobId = jobId;
        this.status = status;
        this.completed = completed;
        this.total = total;
        this.percentage = percentage;
        this.estimatedRemainingSeconds = estimatedRemainingSeconds;
    }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCompleted() { return completed; }
    public void setCompleted(int completed) { this.completed = completed; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }

    public long getEstimatedRemainingSeconds() { return estimatedRemainingSeconds; }
    public void setEstimatedRemainingSeconds(long estimatedRemainingSeconds) {
        this.estimatedRemainingSeconds = estimatedRemainingSeconds;
    }
}
