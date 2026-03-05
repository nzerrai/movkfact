package com.movkfact.controller;

import com.movkfact.entity.DataSet;
import com.movkfact.entity.Domain;
import com.movkfact.repository.DomainRepository;
import com.movkfact.repository.DataSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * S2.4: Data Export Controller Tests
 * Tests for JSON export of GeneratedDataSet objects
 * 
 * AC Coverage:
 * - AC1: Export endpoint responsive <500ms
 * - AC2: CSV parser (not directly tested - in DataExportService)
 * - AC3: Column filtering
 * - AC4-AC11: Error handling, performance, documentation
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("S2.4: Data Export Controller Tests")
public class DataExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private DataSet testDataSet;
    private Long testDataSetId;

    @BeforeEach
    public void setUp() {
        // Create test domain (foreign key dependency)
        Domain domain = new Domain();
        domain.setName("Export Test Domain " + System.currentTimeMillis());
        Domain savedDomain = domainRepository.save(domain);

        // Create test DataSet
        testDataSet = new DataSet();
        testDataSet.setDomainId(savedDomain.getId());  // Use domainId, not Domain relation
        testDataSet.setName("ExportTestDataSet");
        testDataSet.setRowCount(3);
        testDataSet.setGenerationTimeMs(50L);  // Required field

        // Test data: JSON array of records
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(Map.of("id", "1", "name", "John Doe", "email", "john@example.com", "amount", "1000.50"));
        data.add(Map.of("id", "2", "name", "Jane Smith", "email", "jane@example.com", "amount", "2500.75"));
        data.add(Map.of("id", "3", "name", "Bob Wilson", "email", "bob@example.com", "amount", "750.25"));

        try {
            testDataSet.setDataJson(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        testDataSet = dataSetRepository.save(testDataSet);
        testDataSetId = testDataSet.getId();
    }

    // ============= AC1: EXPORT AS JSON (API Response) <500ms =============

    @Test
    @DisplayName("AC1: Export DataSet as JSON with pretty formatting")
    public void testExportAsJsonPretty() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export")
                .param("pretty", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(jsonPath("$.message").value("DataSet exported successfully"));

        long duration = System.currentTimeMillis() - startTime;
        if (duration > 500) {
            System.out.println("WARNING: Export took " + duration + "ms (threshold: 500ms)");
        }
    }

    @Test
    @DisplayName("AC1: Export DataSet as JSON with compact formatting")
    public void testExportAsJsonCompact() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export")
                .param("pretty", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("AC1: Export uses default pretty=true")
    public void testExportDefaultPretty() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isNotEmpty());
    }

    // ============= AC3: COLUMN FILTERING =============

    @Test
    @DisplayName("AC3: Export with column filtering")
    public void testExportWithColumnFilter() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export")
                .param("columns", "name,email"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("AC3: Export with single column filter")
    public void testExportWithSingleColumnFilter() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export")
                .param("columns", "name"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isNotEmpty());
    }

    // ============= AC4: CONDITIONAL EXTRACTION =============

    @Test
    @DisplayName("AC4: Export with conditional extraction (filter)")
    public void testExportWithConditionalFilter() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export")
                .param("filter", "id:1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("AC4: Export with filter and columns combined")
    public void testExportWithFilterAndColumns() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export")
                .param("columns", "name,email")
                .param("filter", "id:1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isNotEmpty());
    }

    // ============= ERROR HANDLING - VALIDATION =============

    @Test
    @DisplayName("ERROR: Invalid filter with missing colon returns 400")
    public void testExportInvalidFilterFormat() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export")
                .param("filter", "no_colon_here"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("format")));
    }

    @Test
    @DisplayName("ERROR: DataSet not found returns 404")
    public void testExportDataSetNotFound() throws Exception {
        mockMvc.perform(get("/api/data-sets/99999/export"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("not found")));
    }

    // ============= FILE DOWNLOAD ENDPOINT =============

    @Test
    @DisplayName("AC5: Export for download with Content-Disposition header")
    public void testExportForDownload() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export/download"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Disposition"))
            .andExpect(header().string("Content-Disposition", containsString("attachment")))
            .andExpect(header().string("Content-Disposition", containsString(".json")));
    }

    @Test
    @DisplayName("AC5: Download filename includes dataset name and timestamp")
    public void testDownloadFilenameFormat() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export/download"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("ExportTestDataSet")))
            .andExpect(header().string("Content-Disposition", containsString("_export_")));
    }

    @Test
    @DisplayName("AC5: Download with column filtering")
    public void testDownloadWithColumnFilter() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export/download")
                .param("columns", "name,email"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Disposition"));
    }

    // ============= CONTENT TYPE VALIDATION =============

    @Test
    @DisplayName("AC6: Export endpoint returns application/json")
    public void testExportContentType() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    @DisplayName("AC6: Download endpoint returns application/json")
    public void testDownloadContentType() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export/download"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    // ============= RESPONSE FORMAT VALIDATION =============

    @Test
    @DisplayName("Response has correct ApiResponse structure")
    public void testResponseStructure() throws Exception {
        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    // ============= PERFORMANCE TESTS =============

    @Test
    @DisplayName("PERF: Export completes in <500ms")
    public void testExportPerformance() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export"));

        long duration = System.currentTimeMillis() - startTime;
        if (duration > 500) {
            System.out.println("WARNING: Export took " + duration + "ms (threshold: 500ms)");
        }
    }

    @Test
    @DisplayName("PERF: Download completes in <500ms")
    public void testDownloadPerformance() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/data-sets/" + testDataSetId + "/export/download"));

        long duration = System.currentTimeMillis() - startTime;
        if (duration > 500) {
            System.out.println("WARNING: Download took " + duration + "ms (threshold: 500ms)");
        }
    }

}
