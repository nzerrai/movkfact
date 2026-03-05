package com.movkfact.websocket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * STOMP message sent when a batch job starts.
 * Topic: /topic/batch/{jobId}
 */
public class JobStartedMessage {

    private final String type = "job_started";
    private Long jobId;
    private int dataSetCount;
    private int estimatedDuration;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;

    public JobStartedMessage() {}

    public JobStartedMessage(Long jobId, int dataSetCount) {
        this.jobId = jobId;
        this.dataSetCount = dataSetCount;
        this.estimatedDuration = dataSetCount * 5; // rough estimate: 5s per dataset
        this.timestamp = LocalDateTime.now();
    }

    public String getType() { return type; }
    public Long getJobId() { return jobId; }
    public int getDataSetCount() { return dataSetCount; }
    public int getEstimatedDuration() { return estimatedDuration; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
