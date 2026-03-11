package com.movkfact.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.dto.DataSetDTO;
import com.movkfact.dto.GenerationRequestDTO;
import com.movkfact.entity.DataSet;
import com.movkfact.entity.Domain;
import com.movkfact.enums.ColumnType;
import com.movkfact.repository.DataSetRepository;
import com.movkfact.repository.DomainRepository;
import com.movkfact.service.DataGeneratorService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * RestAssured tests for DataGenerationController.
 * Tests all 5 endpoints for data generation API (POST, GET list, GET metadata, GET data, DELETE).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DataGenerationControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private DataGeneratorService dataGeneratorService;

    @Autowired
    private ObjectMapper objectMapper;

    private Domain testDomain;
    private Long testDomainId;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        // Clean up
        dataSetRepository.deleteAll();
        domainRepository.deleteAll();

        // Create test domain
        testDomain = new Domain("Test Domain for S2.3", "Domain for generator tests");
        Domain saved = domainRepository.save(testDomain);
        testDomainId = saved.getId();
    }

    // ============================================================================
    // Task 2.3.2: POST /api/domains/{domainId}/data-sets
    // ============================================================================

    @Test
    void testPOST_CreateDataset_Success201() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(10);
        request.setDatasetName("Test_Dataset");
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        request.setColumns(columns);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(201)
            .header("Location", notNullValue())
            .body("data.id", notNullValue())
            .body("data.domainId", equalTo(testDomainId.intValue()))
            .body("data.name", equalTo("Test_Dataset"))
            .body("data.rowCount", equalTo(10))
            .body("data.generationTimeMs", greaterThanOrEqualTo(0))
            .body("message", notNullValue());
    }

    @Test
    void testPOST_CreateDataset_InvalidDomain404() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(10);
        request.setDatasetName("Test_Dataset");
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        request.setColumns(columns);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/domains/{domainId}/data-sets", 999L)
        .then()
            .statusCode(404);
    }

    @Test
    void testPOST_CreateDataset_NegativeRows400() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(-5);
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        request.setColumns(columns);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(400);
    }

    @Test
    void testPOST_CreateDataset_ZeroRows400() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(0);
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        request.setColumns(columns);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(400);
    }

    @Test
    void testPOST_CreateDataset_EmptyColumns400() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(10);
        request.setColumns(new ArrayList<>());

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(400);
    }

    @Test
    void testPOST_CreateDataset_MultipleColumns201() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(50);
        request.setDatasetName("Multi_Column_Dataset");
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        columns.add(new ColumnConfigDTO("email", ColumnType.EMAIL));
        columns.add(new ColumnConfigDTO("amount", ColumnType.AMOUNT));
        request.setColumns(columns);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(201)
            .body("data.name", equalTo("Multi_Column_Dataset"))
            .body("data.rowCount", equalTo(50))
            .body("message", notNullValue());
    }

    @Test
    void testPOST_CreateDataset_MissingName400() {
        // datasetName is @NotBlank — omitting it must return 400
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(10);
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        request.setColumns(columns);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(400);
    }

    @Test
    void testPOST_CreateDataset_DataPersisted() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(5);
        request.setDatasetName("Persisted_Dataset");
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        request.setColumns(columns);

        String response = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(201)
            .extract()
            .asString();

        // Verify dataset is in database
        long count = dataSetRepository.count();
        assertTrue(count > 0, "Dataset should be persisted in database");
    }

    @Test
    void testPOST_CreateDataset_LargeDataset() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(1000);
        request.setDatasetName("Large_Dataset");
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        columns.add(new ColumnConfigDTO("email", ColumnType.EMAIL));
        request.setColumns(columns);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(201)
            .body("data.rowCount", equalTo(1000))
            .body("message", notNullValue());
    }

    // ============================================================================
    // Task 2.3.3: GET /api/domains/{domainId}/data-sets (List)
    // ============================================================================

    @Test
    void testGET_ListDatasetsByDomain_Success() {
        // Create a dataset directly via repository
        DataSet dataset = new DataSet();
        dataset.setDomainId(testDomainId);
        dataset.setName("Dataset for List");
        dataset.setRowCount(10);
        dataset.setGenerationTimeMs(100L);
        dataset.setDataJson("[{\"firstname\": \"John\"}]");
        dataSetRepository.save(dataset);

        // Then list datasets
        given()
        .when()
            .get("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("data.size()", greaterThanOrEqualTo(1))
            .body("data[0].id", notNullValue())
            .body("message", notNullValue());
    }

    @Test
    void testGET_ListDatasetsByDomain_EmptyList() {
        given()
        .when()
            .get("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("data.size()", equalTo(0));
    }

    @Test
    void testGET_ListDatasetsByDomain_InvalidDomain404() {
        given()
        .when()
            .get("/api/domains/{domainId}/data-sets", 999L)
        .then()
            .statusCode(404);
    }

    // ============================================================================
    // GET /api/data-sets/{id} (Metadata)
    // ============================================================================

    @Test
    void testGET_DatasetMetadata_Success() {
        // Create a dataset directly
        DataSet dataset = new DataSet();
        dataset.setDomainId(testDomainId);
        dataset.setName("Metadata Test");
        dataset.setRowCount(10);
        dataset.setGenerationTimeMs(100L);
        dataset.setDataJson("[{\"firstname\": \"John\"}]");
        DataSet saved = dataSetRepository.save(dataset);

        // Get metadata
        given()
        .when()
            .get("/api/data-sets/{id}", saved.getId())
        .then()
            .statusCode(200)
            .body("data.id", notNullValue())
            .body("data.name", equalTo("Metadata Test"))
            .body("data.rowCount", equalTo(10))
            .body("data.createdAt", notNullValue());
    }

    @Test
    void testGET_DatasetMetadata_InvalidId404() {
        given()
        .when()
            .get("/api/data-sets/{id}", 999L)
        .then()
            .statusCode(404);
    }

    // ============================================================================
    // GET /api/data-sets/{id}/data (Paginated Data)
    // ============================================================================

    @Test
    void testGET_DatasetData_Success() {
        // Create a dataset with 50 rows
        DataSet dataset = new DataSet();
        dataset.setDomainId(testDomainId);
        dataset.setName("Data Pagination Test");
        dataset.setRowCount(50);
        dataset.setGenerationTimeMs(500L);
        // Create JSON with 50 rows
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < 50; i++) {
            if (i > 0) json.append(",");
            json.append("{\"firstname\": \"Name").append(i).append("\"}");
        }
        json.append("]");
        dataset.setDataJson(json.toString());
        DataSet saved = dataSetRepository.save(dataset);

        // Get data with default pagination
        given()
        .when()
            .get("/api/data-sets/{id}/data", saved.getId())
        .then()
            .statusCode(200)
            .body("data.totalRows", equalTo(50))
            .body("data.pageNumber", equalTo(0))
            .body("data.pageSize", equalTo(50))
            .body("data.totalPages", equalTo(1))
            .body("data.rows", notNullValue())
            .body("data.rows.size()", equalTo(50));
    }

    @Test
    void testGET_DatasetData_WithPagination() {
        // Create a dataset with 100 rows
        DataSet dataset = new DataSet();
        dataset.setDomainId(testDomainId);
        dataset.setName("Pagination Test");
        dataset.setRowCount(100);
        dataset.setGenerationTimeMs(1000L);
        // Create JSON with 100 rows
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < 100; i++) {
            if (i > 0) json.append(",");
            json.append("{\"firstname\": \"Name").append(i).append("\"}");
        }
        json.append("]");
        dataset.setDataJson(json.toString());
        DataSet saved = dataSetRepository.save(dataset);

        // Get second page (page 1, size 30)
        given()
        .when()
            .get("/api/data-sets/{id}/data?page=1&size=30", saved.getId())
        .then()
            .statusCode(200)
            .body("data.totalRows", equalTo(100))
            .body("data.pageNumber", equalTo(1))
            .body("data.pageSize", equalTo(30))
            .body("data.totalPages", equalTo(4))
            .body("data.rows.size()", equalTo(30));
    }

    @Test
    void testGET_DatasetData_InvalidPagination400() {
        // Create a dataset
        DataSet dataset = new DataSet();
        dataset.setDomainId(testDomainId);
        dataset.setName("Pagination Validation");
        dataset.setRowCount(10);
        dataset.setGenerationTimeMs(100L);
        dataset.setDataJson("[{\"firstname\": \"John\"}]");
        DataSet saved = dataSetRepository.save(dataset);

        // Test invalid page size (> 100)
        given()
        .when()
            .get("/api/data-sets/{id}/data?page=0&size=101", saved.getId())
        .then()
            .statusCode(400);
    }

    @Test
    void testGET_DatasetData_InvalidDatasetId404() {
        given()
        .when()
            .get("/api/data-sets/{id}/data", 999L)
        .then()
            .statusCode(404);
    }

    // ============================================================================
    // Task 2.3.4: DELETE /api/data-sets/{id}
    // ============================================================================

    @Test
    void testDELETE_DatasetSoftDelete_Success() {
        // Create a dataset
        DataSet dataset = new DataSet();
        dataset.setDomainId(testDomainId);
        dataset.setName("Delete Test");
        dataset.setRowCount(10);
        dataset.setGenerationTimeMs(100L);
        dataset.setDataJson("[{\"firstname\": \"John\"}]");
        DataSet saved = dataSetRepository.save(dataset);

        // Delete it
        given()
        .when()
            .delete("/api/data-sets/{id}", saved.getId())
        .then()
            .statusCode(204);
    }

    @Test
    void testDELETE_Dataset_NotFoundAfterDelete() {
        // Create a dataset
        DataSet dataset = new DataSet();
        dataset.setDomainId(testDomainId);
        dataset.setName("Soft Delete Test");
        dataset.setRowCount(10);
        dataset.setGenerationTimeMs(100L);
        dataset.setDataJson("[{\"firstname\": \"John\"}]");
        DataSet saved = dataSetRepository.save(dataset);
        Long datasetId = saved.getId();

        // Delete it
        given()
        .when()
            .delete("/api/data-sets/{id}", datasetId)
        .then()
            .statusCode(204);

        // Try to get it - should be 404
        given()
        .when()
            .get("/api/data-sets/{id}", datasetId)
        .then()
            .statusCode(404);
    }

    @Test
    void testGET_CheckName_Available() {
        given()
        .when()
            .get("/api/domains/{domainId}/datasets/check-name?name={name}", testDomainId, "new_dataset_name")
        .then()
            .statusCode(200)
            .body("data.available", equalTo(true))
            .body("message", containsString("available"));
    }

    @Test
    void testGET_CheckName_Exists() {
        // Create a dataset first
        DataSet existing = new DataSet();
        existing.setDomainId(testDomainId);
        existing.setName("existing_name");
        existing.setRowCount(10);
        existing.setColumnCount(2);
        existing.setGenerationTimeMs(100L);
        existing.setDataJson("[]");
        dataSetRepository.save(existing);

        given()
        .when()
            .get("/api/domains/{domainId}/datasets/check-name?name={name}", testDomainId, "existing_name")
        .then()
            .statusCode(200)
            .body("data.available", equalTo(false))
            .body("message", containsString("taken"));
    }

    @Test
    void testGET_CheckName_InvalidFormat() {
        given()
        .when()
            .get("/api/domains/{domainId}/datasets/check-name?name={name}", testDomainId, "invalid@name")
        .then()
            .statusCode(400)
            .body("error", containsString("contain alphanumeric"));
    }

    @Test
    void testGET_CheckName_TooShort() {
        given()
        .when()
            .get("/api/domains/{domainId}/datasets/check-name?name={name}", testDomainId, "ab")
        .then()
            .statusCode(400)
            .body("error", containsString("between 3 and 50"));
    }

    @Test
    void testGET_CheckName_Empty() {
        given()
        .when()
            .get("/api/domains/{domainId}/datasets/check-name?name=", testDomainId)
        .then()
            .statusCode(400)
            .body("error", containsString("required"));
    }

    @Test
    void testDELETE_InvalidDatasetId404() {
        given()
        .when()
            .delete("/api/data-sets/{id}", 999L)
        .then()
            .statusCode(404);
    }

    // ============================================================================
    // Extra Columns Tests (Epic 12)
    // ============================================================================

    @Test
    void testPOST_CreateDataset_WithExtraColumns_Success() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(10);
        request.setDatasetName("Test_With_Extra_Columns");
        
        // Add detected columns
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        columns.add(new ColumnConfigDTO("email", ColumnType.EMAIL));
        request.setColumns(columns);
        
        // Add extra columns
        List<ColumnConfigDTO> extraColumns = new ArrayList<>();
        ColumnConfigDTO extraCol = new ColumnConfigDTO("status", ColumnType.ENUM);
        extraColumns.add(extraCol);
        request.setExtraColumns(extraColumns);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(201)
            .body("data.name", equalTo("Test_With_Extra_Columns"))
            .body("data.rowCount", equalTo(10));
    }

    @Test
    void testPOST_CreateDataset_ExceedMaxColumns_Returns400() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(10);
        request.setDatasetName("Test_Too_Many_Columns");
        
        // Add detected columns to reach near max (assuming max is 50)
        List<ColumnConfigDTO> columns = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            columns.add(new ColumnConfigDTO("col_" + i, ColumnType.TEXT));
        }
        request.setColumns(columns);
        
        // Add extra columns that will exceed the limit
        List<ColumnConfigDTO> extraColumns = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            extraColumns.add(new ColumnConfigDTO("extra_col_" + i, ColumnType.TEXT));
        }
        request.setExtraColumns(extraColumns);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(400)
            .body("error", containsString("column"))
            .body("error", containsString("exceed|maximum|limit"));
    }

    @Test
    void testPOST_CreateDataset_WithDuplicateExtraColumn_Returns400() {
        GenerationRequestDTO request = new GenerationRequestDTO();
        request.setNumberOfRows(10);
        request.setDatasetName("Test_Duplicate_Extra_Column");
        
        // Add detected columns
        List<ColumnConfigDTO> columns = new ArrayList<>();
        columns.add(new ColumnConfigDTO("firstname", ColumnType.FIRST_NAME));
        request.setColumns(columns);
        
        // Add extra column with same name as detected column
        List<ColumnConfigDTO> extraColumns = new ArrayList<>();
        extraColumns.add(new ColumnConfigDTO("firstname", ColumnType.ENUM));
        request.setExtraColumns(extraColumns);

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/domains/{domainId}/data-sets", testDomainId)
        .then()
            .statusCode(400)
            .body("error", containsString("duplicate|already exists|conflict"));
    }
}

