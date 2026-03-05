package com.movkfact.websocket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * STOMP message sent when a dataset in a batch job fails (with retry info).
 * Topic: /topic/batch/{jobId}
 */
public class JobErrorMessage {

    private final String type = "job_error";
    private Long jobId;
    private String errorMessage;
    private String affectedDataSet;
    private int retryAttempts;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;

    public JobErrorMessage() {}

    public JobErrorMessage(Long jobId, String errorMessage,
                            String affectedDataSet, int retryAttempts) {
        this.jobId = jobId;
        this.errorMessage = errorMessage;
        this.affectedDataSet = affectedDataSet;
        this.retryAttempts = retryAttempts;
        this.timestamp = LocalDateTime.now();
    }

    public String getType() { return type; }
    public Long getJobId() { return jobId; }
    public String getErrorMessage() { return errorMessage; }
    public String getAffectedDataSet() { return affectedDataSet; }
    public int getRetryAttempts() { return retryAttempts; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
