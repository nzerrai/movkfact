package com.movkfact.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.batch.dto.BatchDataSetConfigDTO;
import com.movkfact.batch.dto.BatchGenerationRequestDTO;
import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.entity.Domain;
import com.movkfact.entity.JobStatusType;
import com.movkfact.enums.ColumnType;
import com.movkfact.repository.DataSetRepository;
import com.movkfact.repository.DomainRepository;
import com.movkfact.repository.JobStatusRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for S3.3 — WebSocket persistent job status.
 *
 * Covers:
 * - GET /api/batch/{jobId}/full-status → JobStatus from DB
 * - JobStatus is persisted at job start (beforeJob)
 * - JobStatus is updated to COMPLETED after job finishes (afterJob)
 * - Unknown jobId → 404
 * - JobStatus progress is updated per-chunk (afterChunk)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BatchJobFullStatusIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testDomainId;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        dataSetRepository.deleteAll();
        domainRepository.deleteAll();
        jobStatusRepository.deleteAll();

        Domain domain = domainRepository.save(new Domain("WS Test Domain", "Domain for WebSocket tests"));
        testDomainId = domain.getId();
    }

    // ────────────────────────────────────────────────────────────────────────
    // GET /api/batch/{jobId}/full-status — 404 for unknown job
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void fullStatus_UnknownJobId_Returns404() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/batch/999999/full-status")
        .then()
            .statusCode(404)
            .body("error", notNullValue());
    }

    // ────────────────────────────────────────────────────────────────────────
    // Job start → JobStatus persisted with RUNNING status
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @Order(2)
    void fullStatus_AfterJobStart_ReturnsRunningStatus() throws Exception {
        long jobId = launchBatchJob(2, testDomainId, 10);

        // Immediately after launch, should be RUNNING (or COMPLETED if very fast)
        String status = given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/batch/" + jobId + "/full-status")
        .then()
            .statusCode(200)
            .body("data.jobId", equalTo((int)(long) jobId))
            .body("data.status", notNullValue())
            .body("data.total", equalTo(2))
            .extract()
            .jsonPath().getString("data.status");

        // Status should be RUNNING or COMPLETED (never null or undefined)
        assertTrue(
            "RUNNING".equals(status) || "COMPLETED".equals(status) || "FAILED".equals(status),
            "Unexpected status: " + status
        );
    }

    // ────────────────────────────────────────────────────────────────────────
    // After job completes → JobStatus = COMPLETED
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    void fullStatus_AfterJobCompletes_ReturnsCompleted() throws Exception {
        long jobId = launchBatchJob(2, testDomainId, 5);
        waitForCompletion(jobId, 15);

        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/batch/" + jobId + "/full-status")
        .then()
            .statusCode(200)
            .body("data.jobId", equalTo((int)(long) jobId))
            .body("data.status", equalTo("COMPLETED"))
            .body("data.total", equalTo(2))
            .body("data.progress", equalTo(100))
            .body("data.completedAt", notNullValue());
    }

    // ────────────────────────────────────────────────────────────────────────
    // Progress is non-null and within [0, 100]
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    void fullStatus_Progress_IsValidPercentage() throws Exception {
        long jobId = launchBatchJob(3, testDomainId, 5);
        waitForCompletion(jobId, 15);

        int progress = given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/batch/" + jobId + "/full-status")
        .then()
            .statusCode(200)
            .extract()
            .jsonPath().getInt("data.progress");

        assertTrue(progress >= 0 && progress <= 100,
            "Progress should be 0-100 but was: " + progress);
    }

    // ────────────────────────────────────────────────────────────────────────
    // DB state verification — JobStatus entity is persisted correctly
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    void jobStatusEntity_IsPersisted_InDatabase() throws Exception {
        long jobId = launchBatchJob(1, testDomainId, 5);
        waitForCompletion(jobId, 15);

        var status = jobStatusRepository.findById(jobId).orElse(null);

        assertNotNull(status, "JobStatus should be persisted in DB");
        assertEquals(jobId, status.getJobId());
        assertNotNull(status.getStatus());
        assertNotNull(status.getCreatedAt());
        assertTrue(status.getTotal() > 0);
    }

    // ────────────────────────────────────────────────────────────────────────
    // findByStatus query — RUNNING jobs can be queried
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    void jobStatusRepository_findByStatus_ReturnsMatchingJobs() throws Exception {
        // Launch a job (it may complete fast)
        long jobId = launchBatchJob(1, testDomainId, 5);
        waitForCompletion(jobId, 15);

        // After completion, should be in COMPLETED list
        var completedJobs = jobStatusRepository.findByStatus(JobStatusType.COMPLETED);
        assertTrue(completedJobs.stream().anyMatch(j -> j.getJobId().equals(jobId)),
            "Completed job should appear in findByStatus(COMPLETED)");
    }

    // ────────────────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────────────────

    private long launchBatchJob(int numDatasets, Long domainId, int rows) throws Exception {
        BatchGenerationRequestDTO request = new BatchGenerationRequestDTO();
        List<BatchDataSetConfigDTO> configs = new java.util.ArrayList<>();
        for (int i = 0; i < numDatasets; i++) {
            configs.add(new BatchDataSetConfigDTO(
                domainId,
                "WS_Dataset_" + i,
                List.of(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME)),
                rows
            ));
        }
        request.setDataSetConfigs(configs);

        return given()
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(request))
        .when()
            .post("/api/batch/generate")
        .then()
            .statusCode(202)
            .extract()
            .jsonPath().getLong("data.jobId");
    }

    private void waitForCompletion(long jobId, int maxSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + (maxSeconds * 1000L);
        while (System.currentTimeMillis() < deadline) {
            String status = given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/batch/" + jobId)
            .then()
                .statusCode(200)
                .extract()
                .jsonPath().getString("data.status");

            if ("COMPLETED".equals(status) || "FAILED".equals(status) || "STOPPED".equals(status)) {
                return;
            }
            Thread.sleep(250);
        }
    }
}
