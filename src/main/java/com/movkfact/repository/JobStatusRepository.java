package com.movkfact.repository;

import com.movkfact.entity.JobStatus;
import com.movkfact.entity.JobStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for persistent batch job status (S3.3).
 */
@Repository
public interface JobStatusRepository extends JpaRepository<JobStatus, Long> {

    List<JobStatus> findByStatus(JobStatusType status);
}
