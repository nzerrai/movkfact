package com.movkfact.websocket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * STOMP message sent for each dataset completion (real-time progress).
 * Topic: /topic/batch/{jobId}
 */
public class JobProgressMessage {

    private final String type = "job_progress_update";
    private Long jobId;
    private int completed;
    private int total;
    private int percentage;
    private String currentDataSet;
    private double averageTimePerDataSet;
    private long estimatedRemainingSeconds;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;

    public JobProgressMessage() {}

    public JobProgressMessage(Long jobId, int completed, int total,
                               String currentDataSet, long elapsedMs) {
        this.jobId = jobId;
        this.completed = completed;
        this.total = total;
        this.percentage = total > 0 ? (int) ((completed * 100.0) / total) : 0;
        this.currentDataSet = currentDataSet;
        this.timestamp = LocalDateTime.now();

        if (completed > 0) {
            this.averageTimePerDataSet = elapsedMs / 1000.0 / completed;
            long remaining = total - completed;
            this.estimatedRemainingSeconds = (long) (averageTimePerDataSet * remaining);
        }
    }

    public String getType() { return type; }
    public Long getJobId() { return jobId; }
    public int getCompleted() { return completed; }
    public int getTotal() { return total; }
    public int getPercentage() { return percentage; }
    public String getCurrentDataSet() { return currentDataSet; }
    public double getAverageTimePerDataSet() { return averageTimePerDataSet; }
    public long getEstimatedRemainingSeconds() { return estimatedRemainingSeconds; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
