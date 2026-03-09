package com.movkfact.controller;

import com.movkfact.dto.DomainCreateDTO;
import com.movkfact.dto.DomainResponseDTO;
import com.movkfact.entity.DataSet;
import com.movkfact.entity.Domain;
import com.movkfact.repository.ActivityRepository;
import com.movkfact.repository.DataSetRepository;
import com.movkfact.repository.DomainRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration test suite for DomainController.
 * 
 * Uses RestAssured for HTTP testing and Spring Boot test context.
 * Tests all 5 endpoints with success and error scenarios.
 * 
 * Test Coverage:
 * - CREATE (4 tests): valid, validation error, duplicate name
 * - READ (3 tests): list all, get by ID, not found
 * - UPDATE (3 tests): valid update, validation error, duplicate name, not found
 * - DELETE (2 tests): soft delete, not found
 * - SOFT DELETE AWARENESS: verify deleted domains excluded from list
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DomainControllerTest {
    
    @LocalServerPort
    int port;
    
    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        activityRepository.deleteAll();
        dataSetRepository.deleteAll();
        domainRepository.deleteAll();
    }
    
    // ========== CREATE Tests ==========
    
    @Test
    void testCreateDomainSuccess() {
        DomainCreateDTO dto = new DomainCreateDTO("Finance", "Financial data domain");
        
        Response response = given()
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post("/api/domains")
                .then()
                .statusCode(201)
                .assertThat()
                .body("message", org.hamcrest.Matchers.equalTo("Domain created successfully"))
                .extract()
                .response();
        
        // Verify domain created in database
        long domainCount = domainRepository.count();
        assertThat(domainCount).isEqualTo(1);
        
        // Verify Location header contains ID
        String locationHeader = response.header("Location");
        assertThat(locationHeader).contains("/api/domains/");
    }
    
    @Test
    void testCreateDomainValidationErrorMissingName() {
        DomainCreateDTO dto = new DomainCreateDTO("", "Description without name");
        
        given()
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post("/api/domains")
                .then()
                .statusCode(400)
                .assertThat()
                .body("status", org.hamcrest.Matchers.equalTo(400))
                .body("error", org.hamcrest.Matchers.containsString("name"));
    }
    
    @Test
    void testCreateDomainDuplicateName() {
        // Create first domain
        Domain domain1 = new Domain("Finance", "First domain");
        domainRepository.save(domain1);
        
        // Try to create second domain with same name
        DomainCreateDTO dto = new DomainCreateDTO("Finance", "Duplicate name");
        
        given()
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post("/api/domains")
                .then()
                .statusCode(409)
                .assertThat()
                .body("status", org.hamcrest.Matchers.equalTo(409));
    }
    
    // ========== READ Tests ==========
    
    @Test
    void testGetAllDomainsEmpty() {
        given()
                .when()
                .get("/api/domains")
                .then()
                .statusCode(200)
                .assertThat()
                .body("data", org.hamcrest.Matchers.hasSize(0));
    }
    
    @Test
    void testGetAllDomains() {
        // Create 3 domains
        domainRepository.save(new Domain("Finance", "Financial data"));
        domainRepository.save(new Domain("HR", "Human resources"));
        domainRepository.save(new Domain("Sales", "Sales data"));
        
        given()
                .when()
                .get("/api/domains")
                .then()
                .statusCode(200)
                .assertThat()
                .body("data", org.hamcrest.Matchers.hasSize(3))
                .body("message", org.hamcrest.Matchers.equalTo("Domains retrieved successfully"));
    }
    
    @Test
    void testGetDomainById() {
        // Create domain
        Domain domain = domainRepository.save(new Domain("Finance", "Financial data"));
        
        given()
                .when()
                .get("/api/domains/{id}", domain.getId())
                .then()
                .statusCode(200)
                .assertThat()
                .body("data.name", org.hamcrest.Matchers.equalTo("Finance"))
                .body("data.description", org.hamcrest.Matchers.equalTo("Financial data"));
    }
    
    @Test
    void testGetDomainByIdNotFound() {
        given()
                .when()
                .get("/api/domains/999")
                .then()
                .statusCode(404)
                .assertThat()
                .body("status", org.hamcrest.Matchers.equalTo(404));
    }
    
    // ========== UPDATE Tests ==========
    
    @Test
    void testUpdateDomainSuccess() {
        // Create domain
        Domain domain = domainRepository.save(new Domain("Finance", "Old description"));
        
        DomainCreateDTO updateDTO = new DomainCreateDTO("Finance Updated", "New description");
        
        given()
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .put("/api/domains/{id}", domain.getId())
                .then()
                .statusCode(200)
                .assertThat()
                .body("data.name", org.hamcrest.Matchers.equalTo("Finance Updated"))
                .body("data.description", org.hamcrest.Matchers.equalTo("New description"));
        
        // Verify version incremented in database
        Domain updated = domainRepository.findById(domain.getId()).orElseThrow();
        assertThat(updated.getVersion()).isEqualTo(1);
    }
    
    @Test
    void testUpdateDomainValidationError() {
        Domain domain = domainRepository.save(new Domain("Finance", "Description"));
        
        DomainCreateDTO updateDTO = new DomainCreateDTO("", "Invalid name");
        
        given()
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .put("/api/domains/{id}", domain.getId())
                .then()
                .statusCode(400)
                .assertThat()
                .body("status", org.hamcrest.Matchers.equalTo(400));
    }
    
    @Test
    void testUpdateDomainDuplicateName() {
        // Create two domains
        Domain domain1 = domainRepository.save(new Domain("Finance", "Description1"));
        Domain domain2 = domainRepository.save(new Domain("HR", "Description2"));
        
        // Try to update domain2 to have domain1's name
        DomainCreateDTO updateDTO = new DomainCreateDTO("Finance", "Updated");
        
        given()
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .put("/api/domains/{id}", domain2.getId())
                .then()
                .statusCode(409)
                .assertThat()
                .body("status", org.hamcrest.Matchers.equalTo(409));
    }
    
    @Test
    void testUpdateDomainNotFound() {
        DomainCreateDTO updateDTO = new DomainCreateDTO("Finance", "Description");
        
        given()
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .put("/api/domains/999")
                .then()
                .statusCode(404)
                .assertThat()
                .body("status", org.hamcrest.Matchers.equalTo(404));
    }
    
    @Test
    void testUpdateDomainWithSameName() {
        // Create domain
        Domain domain = domainRepository.save(new Domain("Finance", "Old desc"));
        
        // Update with same name but different description (should succeed)
        DomainCreateDTO updateDTO = new DomainCreateDTO("Finance", "New desc");
        
        given()
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .put("/api/domains/{id}", domain.getId())
                .then()
                .statusCode(200)
                .assertThat()
                .body("data.description", org.hamcrest.Matchers.equalTo("New desc"));
    }
    
    // ========== DELETE Tests ==========
    
    @Test
    void testDeleteDomainSuccess() {
        Domain domain = domainRepository.save(new Domain("Finance", "Description"));
        
        given()
                .when()
                .delete("/api/domains/{id}", domain.getId())
                .then()
                .statusCode(204);
        
        // Verify soft delete: domain still exists but deletedAt is set
        Domain deleted = domainRepository.findById(domain.getId()).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }
    
    @Test
    void testDeleteDomainNotFound() {
        given()
                .when()
                .delete("/api/domains/999")
                .then()
                .statusCode(404)
                .assertThat()
                .body("status", org.hamcrest.Matchers.equalTo(404));
    }
    
    // ========== Soft Delete Awareness Tests ==========
    
    @Test
    void testSoftDeleteExcludedFromList() {
        // Create 2 active domains and 1 deleted
        Domain active1 = domainRepository.save(new Domain("Finance", "Active"));
        Domain active2 = domainRepository.save(new Domain("HR", "Active"));
        Domain deleted = domainRepository.save(new Domain("Old", "Deleted"));
        deleted.softDelete();
        domainRepository.save(deleted);
        
        // List should return only 2 active domains
        given()
                .when()
                .get("/api/domains")
                .then()
                .statusCode(200)
                .assertThat()
                .body("data", org.hamcrest.Matchers.hasSize(2))
                .body("data.name", org.hamcrest.Matchers.containsInAnyOrder("Finance", "HR"));
    }
    
    // ========== Enriched stats Tests (FR-002) ==========

    @Test
    void testGetDomainsWithStats_noDatasets_returnsZeroStats() {
        domainRepository.save(new Domain("Finance", "Financial data"));

        given()
            .when().get("/api/domains")
            .then().statusCode(200)
            .body("data[0].datasetCount", equalTo(0))
            .body("data[0].totalRows",    equalTo(0))
            .body("data[0].statuses.downloaded", equalTo(false))
            .body("data[0].statuses.modified",   equalTo(false))
            .body("data[0].statuses.viewed",     equalTo(false));
    }

    @Test
    void testGetDomainsWithStats_twoDomains_threeDatasets_aggregatesCorrectly() {
        Domain d1 = domainRepository.save(new Domain("D1", "desc1"));
        Domain d2 = domainRepository.save(new Domain("D2", "desc2"));

        DataSet ds1 = new DataSet(); ds1.setDomainId(d1.getId()); ds1.setName("ds1");
        ds1.setRowCount(300); ds1.setGenerationTimeMs(10L); ds1.setDataJson("[]");
        dataSetRepository.save(ds1);

        DataSet ds2 = new DataSet(); ds2.setDomainId(d1.getId()); ds2.setName("ds2");
        ds2.setRowCount(700); ds2.setGenerationTimeMs(10L); ds2.setDataJson("[]");
        dataSetRepository.save(ds2);

        DataSet ds3 = new DataSet(); ds3.setDomainId(d2.getId()); ds3.setName("ds3");
        ds3.setRowCount(500); ds3.setGenerationTimeMs(10L); ds3.setDataJson("[]");
        dataSetRepository.save(ds3);

        given()
            .when().get("/api/domains")
            .then().statusCode(200)
            .body("data.size()", equalTo(2))
            // D1: 2 datasets, 1000 rows
            .body("data.find { it.name == 'D1' }.datasetCount", equalTo(2))
            .body("data.find { it.name == 'D1' }.totalRows",    equalTo(1000))
            // D2: 1 dataset, 500 rows
            .body("data.find { it.name == 'D2' }.datasetCount", equalTo(1))
            .body("data.find { it.name == 'D2' }.totalRows",    equalTo(500));
    }

    @Test
    void testGetDeletedDomainByIdNotFound() {
        // Create and soft delete domain
        Domain domain = domainRepository.save(new Domain("Finance", "Description"));
        domain.softDelete();
        domainRepository.save(domain);
        
        // Try to GET the deleted domain
        given()
                .when()
                .get("/api/domains/{id}", domain.getId())
                .then()
                .statusCode(404)
                .assertThat()
                .body("status", org.hamcrest.Matchers.equalTo(404));
    }
}
