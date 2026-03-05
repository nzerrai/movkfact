package com.movkfact.entity;

/**
 * Lifecycle states for a batch job (S3.3 persistent tracking).
 */
public enum JobStatusType {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}
