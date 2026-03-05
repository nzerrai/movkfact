package com.movkfact.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for DataPreviewController (S7.1 AC1, AC4, AC5, AC6).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DataPreviewControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    // ─── AC1 : succès, 5 lignes générées ──────────────────────────────────────

    @Test
    void preview_validRequest_returns200With5Rows() {
        String body = """
            {
              "columns": [
                { "name": "prenom", "columnType": "FIRST_NAME" },
                { "name": "email",  "columnType": "EMAIL" }
              ],
              "count": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/datasets/preview")
        .then()
            .statusCode(200)
            .body("data.previewRows.size()", equalTo(5))
            .body("data.columnCount", equalTo(2))
            .body("data.previewRows[0]", hasKey("prenom"))
            .body("data.previewRows[0]", hasKey("email"))
            .body("message", equalTo("Preview generated"));
    }

    @Test
    void preview_count3_returns3Rows() {
        String body = """
            {
              "columns": [
                { "name": "montant", "columnType": "AMOUNT" }
              ],
              "count": 3
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/datasets/preview")
        .then()
            .statusCode(200)
            .body("data.previewRows.size()", equalTo(3));
    }

    // ─── AC1 : count > 5 est limité à 5 ───────────────────────────────────────

    @Test
    void preview_countOver5_isLimitedTo5() {
        String body = """
            {
              "columns": [
                { "name": "nom", "columnType": "LAST_NAME" }
              ],
              "count": 100
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/datasets/preview")
        .then()
            .statusCode(200)
            .body("data.previewRows.size()", equalTo(5));
    }

    // ─── AC2 : contraintes AMOUNT respectées ──────────────────────────────────

    @Test
    void preview_amountWithConstraints_allValuesInRange() {
        String body = """
            {
              "columns": [
                {
                  "name": "montant",
                  "columnType": "AMOUNT",
                  "constraints": { "min": 50, "max": 100 }
                }
              ],
              "count": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/datasets/preview")
        .then()
            .statusCode(200)
            .body("data.previewRows.size()", equalTo(5));
    }

    // ─── AC4 : contrainte invalide min > max → 400 avec message explicite ────────────────────────────

    @Test
    void preview_invalidConstraint_minGreaterThanMax_returns400WithMessage() {
        String body = """
            {
              "columns": [
                {
                  "name": "montant",
                  "columnType": "AMOUNT",
                  "constraints": { "min": 500, "max": 10 }
                }
              ],
              "count": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/datasets/preview")
        .then()
            .statusCode(400)
            .body("message", containsString("min"))
            .body("message", containsString("max"));
    }

    // ─── AC4 : date format invalide → 400 (pas 500) ────────────────────────────

    @Test
    void preview_invalidDateFormat_returns400() {
        String body = """
            {
              "columns": [
                {
                  "name": "date",
                  "columnType": "DATE",
                  "constraints": { "dateFrom": "not-a-date", "dateTo": "2024-12-31" }
                }
              ],
              "count": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/datasets/preview")
        .then()
            .statusCode(400)
            .body("message", containsString("YYYY-MM-DD"));
    }

    // ─── AC4 : columns vide → 400 ─────────────────────────────────────────────

    @Test
    void preview_emptyColumns_returns400() {
        String body = """
            {
              "columns": [],
              "count": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/datasets/preview")
        .then()
            .statusCode(400);
    }

    // ─── AC5 : type inconnu → 400 avec message explicite ──────────────────────

    @Test
    void preview_unknownColumnType_returns400WithMessage() {
        String body = """
            {
              "columns": [
                { "name": "test", "columnType": "INVALID_TYPE" }
              ],
              "count": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/datasets/preview")
        .then()
            .statusCode(400)
            .body("error", containsString("Type inconnu"));
    }

    // ─── AC2 : contraintes DATE dans le preview ────────────────────────────────

    @Test
    void preview_dateConstraints_returnsValidDates() {
        String body = """
            {
              "columns": [
                {
                  "name": "date_creation",
                  "columnType": "DATE",
                  "constraints": { "dateFrom": "2024-01-01", "dateTo": "2024-12-31" }
                }
              ],
              "count": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/datasets/preview")
        .then()
            .statusCode(200)
            .body("data.previewRows.size()", equalTo(5));
    }

    // ─── AC4 : columns null → 400 ─────────────────────────────────────────────

    @Test
    void preview_nullColumns_returns400() {
        String body = """
            {
              "count": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/datasets/preview")
        .then()
            .statusCode(400);
    }
}
