package com.movkfact.websocket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

/**
 * STOMP message sent when a batch job finishes (success or partial).
 * Topic: /topic/batch/{jobId}
 */
public class JobCompletedMessage {

    private final String type = "job_completed";
    private Long jobId;
    private String status;
    private int rowsGenerated;
    private long duration;
    private List<Long> dataSetIds;
    private int skippedCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;

    public JobCompletedMessage() {}

    public JobCompletedMessage(Long jobId, String status, int rowsGenerated,
                                long durationMs, List<Long> dataSetIds, int skippedCount) {
        this.jobId = jobId;
        this.status = status;
        this.rowsGenerated = rowsGenerated;
        this.duration = durationMs / 1000;
        this.dataSetIds = dataSetIds;
        this.skippedCount = skippedCount;
        this.timestamp = LocalDateTime.now();
    }

    public String getType() { return type; }
    public Long getJobId() { return jobId; }
    public String getStatus() { return status; }
    public int getRowsGenerated() { return rowsGenerated; }
    public long getDuration() { return duration; }
    public List<Long> getDataSetIds() { return dataSetIds; }
    public int getSkippedCount() { return skippedCount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
