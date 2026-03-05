package com.movkfact.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.batch.dto.BatchDataSetConfigDTO;
import com.movkfact.batch.dto.BatchGenerationRequestDTO;
import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.entity.Domain;
import com.movkfact.enums.ColumnType;
import com.movkfact.repository.DataSetRepository;
import com.movkfact.repository.DomainRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for BatchJobController (S3.2).
 *
 * Covers:
 * - POST /api/batch/generate → 202 + jobId
 * - GET  /api/batch/{jobId} → status tracking
 * - GET  /api/batch/{jobId}/progress → progress DTO
 * - Validation: invalid request → 400
 * - Not found: unknown jobId → 404
 * - Performance: 10 datasets × 100 rows < 10 seconds
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BatchJobControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testDomainId;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        dataSetRepository.deleteAll();
        domainRepository.deleteAll();

        Domain domain = domainRepository.save(new Domain("Batch Test Domain", "Domain for batch tests"));
        testDomainId = domain.getId();
    }

    // ============================================================================
    // POST /api/batch/generate
    // ============================================================================

    @Test
    @Order(1)
    void testPOST_Generate_Returns202WithJobId() throws Exception {
        BatchGenerationRequestDTO request = buildRequest(1, testDomainId, 10);

        given()
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(request))
        .when()
            .post("/api/batch/generate")
        .then()
            .statusCode(202)
            .body("data.jobId", notNullValue())
            .body("data.status", notNullValue())
            .body("data.totalDatasets", equalTo(1))
            .body("data.message", containsString("Use GET /api/batch/"));
    }

    @Test
    @Order(2)
    void testPOST_Generate_MultipleDatasets_Returns202() throws Exception {
        BatchGenerationRequestDTO request = buildRequest(3, testDomainId, 10);

        given()
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(request))
        .when()
            .post("/api/batch/generate")
        .then()
            .statusCode(202)
            .body("data.totalDatasets", equalTo(3));
    }

    @Test
    @Order(3)
    void testPOST_Generate_EmptyConfigs_Returns400() throws Exception {
        String body = "{\"dataSetConfigs\": []}";

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/batch/generate")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(4)
    void testPOST_Generate_MissingDatasetName_Returns400() throws Exception {
        String body = """
            {
              "dataSetConfigs": [{
                "domainId": %d,
                "columns": [{"name": "firstname", "type": "FIRST_NAME"}],
                "count": 10
              }]
            }
            """.formatted(testDomainId);

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/batch/generate")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(5)
    void testPOST_Generate_CountExceedsMax_Returns400() throws Exception {
        String body = """
            {
              "dataSetConfigs": [{
                "domainId": %d,
                "datasetName": "Too_Big",
                "columns": [{"name": "firstname", "type": "FIRST_NAME"}],
                "count": 99999
              }]
            }
            """.formatted(testDomainId);

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/batch/generate")
        .then()
            .statusCode(400);
    }

    // ============================================================================
    // GET /api/batch/{jobId}
    // ============================================================================

    @Test
    @Order(6)
    void testGET_JobStatus_ValidJobId_Returns200() throws Exception {
        long jobId = launchBatchJob(1, testDomainId, 5);

        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/batch/" + jobId)
        .then()
            .statusCode(200)
            .body("data.jobId", equalTo((int)(long) jobId))
            .body("data.status", notNullValue())
            .body("data.totalCount", equalTo(1))
            .body("data.percentage", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(7)
    void testGET_JobStatus_UnknownJobId_Returns404() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/batch/999999")
        .then()
            .statusCode(404)
            .body("error", notNullValue());
    }

    @Test
    @Order(8)
    void testGET_JobStatus_CompletedJob_ShowsCorrectCounts() throws Exception {
        int numDatasets = 2;
        long jobId = launchBatchJob(numDatasets, testDomainId, 5);

        // Wait for job to complete (max 10s)
        waitForCompletion(jobId, 10);

        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/batch/" + jobId)
        .then()
            .statusCode(200)
            .body("data.status", equalTo("COMPLETED"))
            .body("data.completedCount", equalTo(numDatasets))
            .body("data.totalCount", equalTo(numDatasets))
            .body("data.percentage", equalTo(100));
    }

    // ============================================================================
    // GET /api/batch/{jobId}/progress
    // ============================================================================

    @Test
    @Order(9)
    void testGET_Progress_ValidJobId_Returns200() throws Exception {
        long jobId = launchBatchJob(1, testDomainId, 5);

        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/batch/" + jobId + "/progress")
        .then()
            .statusCode(200)
            .body("data.jobId", equalTo((int)(long) jobId))
            .body("data.status", notNullValue())
            .body("data.total", equalTo(1))
            .body("data.percentage", greaterThanOrEqualTo(0))
            .body("data.estimatedRemainingSeconds", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(10)
    void testGET_Progress_UnknownJobId_Returns404() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/batch/888888/progress")
        .then()
            .statusCode(404)
            .body("error", notNullValue());
    }

    // ============================================================================
    // Performance test: 10 × 100 rows < 10 seconds
    // ============================================================================

    @Test
    @Order(11)
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testPerformance_10Datasets_100Rows_Under10Seconds() throws Exception {
        BatchGenerationRequestDTO request = buildRequest(10, testDomainId, 100);

        long start = System.currentTimeMillis();

        long jobId = given()
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(request))
        .when()
            .post("/api/batch/generate")
        .then()
            .statusCode(202)
            .extract()
            .jsonPath().getLong("data.jobId");

        // Wait for completion
        waitForCompletion(jobId, 10);

        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 10_000L,
            "Expected 10 datasets × 100 rows to complete in < 10s, but took " + elapsed + "ms");
    }

    // ============================================================================
    // Graceful failure: unknown domain skipped, others complete
    // ============================================================================

    @Test
    @Order(12)
    void testGracefulFailure_UnknownDomain_JobStillCompletes() throws Exception {
        // Mix: 1 valid + 1 invalid domain
        BatchDataSetConfigDTO validConfig = new BatchDataSetConfigDTO(
            testDomainId, "Valid_Dataset",
            List.of(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME)),
            5
        );
        BatchDataSetConfigDTO invalidConfig = new BatchDataSetConfigDTO(
            99999L, "Invalid_Dataset",
            List.of(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME)),
            5
        );

        BatchGenerationRequestDTO request = new BatchGenerationRequestDTO();
        request.setDataSetConfigs(List.of(validConfig, invalidConfig));

        long jobId = given()
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(request))
        .when()
            .post("/api/batch/generate")
        .then()
            .statusCode(202)
            .extract()
            .jsonPath().getLong("data.jobId");

        // Wait max 10s
        waitForCompletion(jobId, 10);

        // Job finishes (COMPLETED or FAILED_WITH_SKIPS)
        String finalStatus = given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/batch/" + jobId)
        .then()
            .statusCode(200)
            .extract()
            .jsonPath().getString("data.status");

        // Must not be STARTED/STARTING (job must have terminated)
        assertNotEquals("STARTING", finalStatus);
        assertNotEquals("STARTED", finalStatus);
    }

    // ============================================================================
    // Helpers
    // ============================================================================

    private BatchGenerationRequestDTO buildRequest(int count, Long domainId, int rowsPerDataset) {
        BatchGenerationRequestDTO request = new BatchGenerationRequestDTO();
        List<BatchDataSetConfigDTO> configs = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            configs.add(new BatchDataSetConfigDTO(
                domainId,
                "Batch_Dataset_" + i,
                List.of(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME)),
                rowsPerDataset
            ));
        }
        request.setDataSetConfigs(configs);
        return request;
    }

    private long launchBatchJob(int numDatasets, Long domainId, int rows) throws Exception {
        BatchGenerationRequestDTO request = buildRequest(numDatasets, domainId, rows);
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

    /**
     * Polls GET /api/batch/{jobId} until status is terminal (COMPLETED/FAILED/STOPPED)
     * or timeout reached.
     */
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
            Thread.sleep(200);
        }
    }
}
