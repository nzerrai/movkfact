package com.movkfact.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.entity.SystemConfiguration;
import com.movkfact.service.ConfigurationService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * RestAssured tests for SystemSettingsController.
 * Tests CRUD operations for system configuration parameters.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SystemSettingsControllerTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private ConfigurationService configurationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        
        // Ensure test data exists
        configurationService.saveConfiguration("max_columns_per_dataset", "50", "Max columns", "INTEGER");
    }
    
    @Test
    void testGET_SpecificSetting_Success() {
        given()
        .when()
            .get("/api/settings/max_columns_per_dataset")
        .then()
            .statusCode(200)
            .body("data.configKey", equalTo("max_columns_per_dataset"))
            .body("data.configValue", equalTo("50"));
    }
    
    @Test
    void testGET_NonexistentSetting_404() {
        given()
        .when()
            .get("/api/settings/nonexistent_key")
        .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }
    
    @Test
    void testPUT_UpdateSetting_Success() {
        SystemConfiguration update = new SystemConfiguration(
            "max_columns_per_dataset", "100", "Updated max columns", "INTEGER"
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(update)
        .when()
            .put("/api/settings/max_columns_per_dataset")
        .then()
            .statusCode(200)
            .body("data.configValue", equalTo("100"));
    }
    
    @Test
    void testPUT_MismatchedKey_400() {
        SystemConfiguration update = new SystemConfiguration(
            "wrong_key", "100", "Mismatched key", "INTEGER"
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(update)
        .when()
            .put("/api/settings/max_columns_per_dataset")
        .then()
            .statusCode(400)
            .body("error", containsString("must match"));
    }
    
    @Test
    void testPOST_CreateNewSetting_Success() {
        SystemConfiguration newConfig = new SystemConfiguration(
            "new_setting", "test_value", "New test setting", "STRING"
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(newConfig)
        .when()
            .post("/api/settings")
        .then()
            .statusCode(201)
            .body("data.configKey", equalTo("new_setting"));
    }
    
    @Test
    void testPOST_DuplicateSetting_409() {
        SystemConfiguration duplicate = new SystemConfiguration(
            "max_columns_per_dataset", "75", "Duplicate", "INTEGER"
        );
        
        given()
            .contentType(ContentType.JSON)
            .body(duplicate)
        .when()
            .post("/api/settings")
        .then()
            .statusCode(409)
            .body("error", containsString("already exists"));
    }
}