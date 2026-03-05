package com.movkfact.batch.dto;

/**
 * Réponse immédiate après soumission d'un batch job.
 */
public class BatchJobResponseDTO {

    private Long jobId;
    private String status;
    private int totalDatasets;
    private String message;

    public BatchJobResponseDTO() {}

    public BatchJobResponseDTO(Long jobId, String status, int totalDatasets, String message) {
        this.jobId = jobId;
        this.status = status;
        this.totalDatasets = totalDatasets;
        this.message = message;
    }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalDatasets() { return totalDatasets; }
    public void setTotalDatasets(int totalDatasets) { this.totalDatasets = totalDatasets; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
