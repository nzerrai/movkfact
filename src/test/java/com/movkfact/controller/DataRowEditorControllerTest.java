package com.movkfact.controller;

import com.movkfact.entity.DataSet;
import com.movkfact.entity.Domain;
import com.movkfact.repository.ActivityRepository;
import com.movkfact.repository.DataSetRepository;
import com.movkfact.repository.DomainRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DataRowEditorController (S6.1).
 * Pattern: RestAssured + @SpringBootTest(RANDOM_PORT)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DataRowEditorControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private ActivityRepository activityRepository;

    private Long datasetId;
    private static final String TWO_ROWS_JSON =
            "[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25}]";

    @BeforeEach
    void setUp() throws Exception {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        activityRepository.deleteAll();
        dataSetRepository.deleteAll();
        domainRepository.deleteAll();

        Domain domain = domainRepository.save(new Domain("Test Domain", "for row editor tests"));

        // Créer un dataset directement en base (2 lignes)
        DataSet ds = new DataSet();
        ds.setDomainId(domain.getId());
        ds.setName("TestDS");
        ds.setRowCount(2);
        ds.setColumnCount(2);
        ds.setGenerationTimeMs(0L);
        ds.setDataJson(TWO_ROWS_JSON);
        ds.setOriginalData(TWO_ROWS_JSON);
        ds.setVersion(0);
        DataSet saved = dataSetRepository.save(ds);
        datasetId = saved.getId();
    }

    // ─── GET paginated rows ───────────────────────────────────────────────────

    @Test
    void GET_rows_200_correctStructure() {
        given()
            .when()
                .get("/api/data-sets/{id}/rows", datasetId)
            .then()
                .statusCode(200)
                .body("data.totalRows", equalTo(2))
                .body("data.rows", hasSize(2))
                .body("data.rows[0].rowIndex", equalTo(0))
                .body("data.rows[0].data.name", equalTo("Alice"))
                .body("data.rows[1].rowIndex", equalTo(1))
                .body("data.rows[1].data.name", equalTo("Bob"))
                .body("data.page", equalTo(0));
    }

    @Test
    void GET_rows_404_datasetNotFound() {
        given()
            .when()
                .get("/api/data-sets/{id}/rows", 999999L)
            .then()
                .statusCode(404);
    }

    @Test
    void GET_rows_pagination_page1() {
        given()
            .queryParam("page", 0)
            .queryParam("size", 1)
            .when()
                .get("/api/data-sets/{id}/rows", datasetId)
            .then()
                .statusCode(200)
                .body("data.rows", hasSize(1))
                .body("data.totalRows", equalTo(2))
                .body("data.rows[0].data.name", equalTo("Alice"));
    }

    // ─── GET single row ───────────────────────────────────────────────────────

    @Test
    void GET_row_200_exactRow() {
        given()
            .when()
                .get("/api/data-sets/{id}/rows/{rowIndex}", datasetId, 1)
            .then()
                .statusCode(200)
                .body("data.rowIndex", equalTo(1))
                .body("data.data.name", equalTo("Bob"));
    }

    @Test
    void GET_row_404_outOfBounds() {
        given()
            .when()
                .get("/api/data-sets/{id}/rows/{rowIndex}", datasetId, 99)
            .then()
                .statusCode(404);
    }

    // ─── PUT row ──────────────────────────────────────────────────────────────

    @Test
    void PUT_row_200_partialMerge() {
        Map<String, Object> body = Map.of("columns", Map.of("age", 99));

        given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
                .put("/api/data-sets/{id}/rows/{rowIndex}", datasetId, 0)
            .then()
                .statusCode(200)
                .body("data.data.age", equalTo(99))
                .body("data.data.name", equalTo("Alice")); // untouched
    }

    @Test
    void PUT_row_400_emptyColumns() {
        Map<String, Object> body = Map.of("columns", Map.of());

        given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
                .put("/api/data-sets/{id}/rows/{rowIndex}", datasetId, 0)
            .then()
                .statusCode(400);
    }

    @Test
    void PUT_row_404_datasetNotFound() {
        Map<String, Object> body = Map.of("columns", Map.of("age", 1));

        given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
                .put("/api/data-sets/{id}/rows/{rowIndex}", 999999L, 0)
            .then()
                .statusCode(404);
    }

    @Test
    void PUT_row_404_rowIndexNotFound() {
        Map<String, Object> body = Map.of("columns", Map.of("age", 1));

        given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
                .put("/api/data-sets/{id}/rows/{rowIndex}", datasetId, 99)
            .then()
                .statusCode(404);
    }

    // ─── DELETE row ───────────────────────────────────────────────────────────

    @Test
    void DELETE_row_204_andReindexed() {
        given()
            .when()
                .delete("/api/data-sets/{id}/rows/{rowIndex}", datasetId, 0)
            .then()
                .statusCode(204);

        // Vérifier que Bob est maintenant à l'index 0
        given()
            .when()
                .get("/api/data-sets/{id}/rows/0", datasetId)
            .then()
                .statusCode(200)
                .body("data.data.name", equalTo("Bob"));

        // rowCount décrémenté
        DataSet updated = dataSetRepository.findByIdAndDeletedAtIsNull(datasetId).orElseThrow();
        assertEquals(1, updated.getRowCount());
    }

    @Test
    void DELETE_row_404_notFound() {
        given()
            .when()
                .delete("/api/data-sets/{id}/rows/{rowIndex}", datasetId, 99)
            .then()
                .statusCode(404);
    }

    // ─── Régression originalData ───────────────────────────────────────────────

    @Test
    void PUT_thenReset_originalDataUnchanged() {
        // Modifier la ligne 0
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("columns", Map.of("name", "Charlie")))
            .when()
                .put("/api/data-sets/{id}/rows/0", datasetId)
            .then()
                .statusCode(200);

        // Vérifier que originalData est intacte en base
        DataSet ds = dataSetRepository.findByIdAndDeletedAtIsNull(datasetId).orElseThrow();
        assertEquals(TWO_ROWS_JSON, ds.getOriginalData());
        assertNotEquals(TWO_ROWS_JSON, ds.getDataJson()); // dataJson a changé
    }
}
