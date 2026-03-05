package com.movkfact.service;

import com.movkfact.dto.ColumnConfigDTO;
import com.movkfact.dto.PreviewRequestDTO;
import com.movkfact.dto.PreviewResponseDTO;
import com.movkfact.enums.ColumnType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DataPreviewService (S7.1 AC1, AC2, AC4).
 */
class DataPreviewServiceTest {

    private DataPreviewService service;

    @BeforeEach
    void setUp() {
        service = new DataPreviewService();
    }

    // ─── AC1 : count limité à 5 ────────────────────────────────────────────────

    @Test
    void generatePreview_returns5RowsMaxRegardlessOfCount() {
        ColumnConfigDTO col = new ColumnConfigDTO("prenom", ColumnType.FIRST_NAME);
        PreviewRequestDTO request = new PreviewRequestDTO(List.of(col), 100);

        PreviewResponseDTO response = service.generatePreview(request);

        assertEquals(5, response.getPreviewRows().size());
        assertEquals(1, response.getColumnCount());
    }

    @Test
    void generatePreview_withCount3_returns3Rows() {
        ColumnConfigDTO col = new ColumnConfigDTO("email", ColumnType.EMAIL);
        PreviewRequestDTO request = new PreviewRequestDTO(List.of(col), 3);

        PreviewResponseDTO response = service.generatePreview(request);

        assertEquals(3, response.getPreviewRows().size());
    }

    @Test
    void generatePreview_withCount0_returns5Rows() {
        ColumnConfigDTO col = new ColumnConfigDTO("phone", ColumnType.PHONE);
        PreviewRequestDTO request = new PreviewRequestDTO(List.of(col), 0);

        PreviewResponseDTO response = service.generatePreview(request);

        assertEquals(5, response.getPreviewRows().size());
    }

    // ─── AC1 : colonnes présentes dans chaque ligne ────────────────────────────

    @Test
    void generatePreview_rowsContainExpectedColumns() {
        ColumnConfigDTO col1 = new ColumnConfigDTO("prenom", ColumnType.FIRST_NAME);
        ColumnConfigDTO col2 = new ColumnConfigDTO("email", ColumnType.EMAIL);
        PreviewRequestDTO request = new PreviewRequestDTO(List.of(col1, col2), 5);

        PreviewResponseDTO response = service.generatePreview(request);

        assertEquals(2, response.getColumnCount());
        for (Map<String, Object> row : response.getPreviewRows()) {
            assertTrue(row.containsKey("prenom"), "Row should contain 'prenom'");
            assertTrue(row.containsKey("email"), "Row should contain 'email'");
        }
    }

    // ─── AC2 : contraintes AMOUNT ──────────────────────────────────────────────

    @Test
    void generatePreview_amountWithConstraints_respects_minMax() {
        ColumnConfigDTO col = new ColumnConfigDTO("montant", ColumnType.AMOUNT);
        Map<String, Object> constraints = new HashMap<>();
        constraints.put("min", 100.0);
        constraints.put("max", 200.0);
        col.setConstraints(constraints);

        PreviewRequestDTO request = new PreviewRequestDTO(List.of(col), 5);
        PreviewResponseDTO response = service.generatePreview(request);

        for (Map<String, Object> row : response.getPreviewRows()) {
            Object val = row.get("montant");
            assertNotNull(val);
            double amount = Double.parseDouble(val.toString());
            assertTrue(amount >= 100.0 && amount <= 200.0,
                "Amount " + amount + " should be in [100, 200]");
        }
    }

    // ─── AC2 : contraintes DATE ────────────────────────────────────────────────

    @Test
    void generatePreview_dateWithConstraints_respects_dateRange() {
        ColumnConfigDTO col = new ColumnConfigDTO("date_naissance", ColumnType.DATE);
        Map<String, Object> constraints = new HashMap<>();
        constraints.put("dateFrom", "2020-01-01");
        constraints.put("dateTo", "2020-12-31");
        col.setConstraints(constraints);

        PreviewRequestDTO request = new PreviewRequestDTO(List.of(col), 5);
        PreviewResponseDTO response = service.generatePreview(request);

        for (Map<String, Object> row : response.getPreviewRows()) {
            String date = (String) row.get("date_naissance");
            assertNotNull(date);
            assertTrue(date.compareTo("2020-01-01") >= 0, "Date " + date + " should be >= 2020-01-01");
            assertTrue(date.compareTo("2020-12-31") <= 0, "Date " + date + " should be <= 2020-12-31");
        }
    }

    // ─── AC4 : validation contraintes invalides ────────────────────────────────

    @Test
    void generatePreview_invalidConstraint_minGreaterThanMax_throws400() {
        ColumnConfigDTO col = new ColumnConfigDTO("montant", ColumnType.AMOUNT);
        Map<String, Object> constraints = new HashMap<>();
        constraints.put("min", 500.0);
        constraints.put("max", 100.0);
        col.setConstraints(constraints);

        PreviewRequestDTO request = new PreviewRequestDTO(List.of(col), 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.generatePreview(request));
        assertTrue(ex.getMessage().contains("min"), "Error should mention 'min'");
        assertTrue(ex.getMessage().contains("max"), "Error should mention 'max'");
    }

    @Test
    void generatePreview_invalidConstraint_dateFromAfterDateTo_throws() {
        ColumnConfigDTO col = new ColumnConfigDTO("date", ColumnType.DATE);
        Map<String, Object> constraints = new HashMap<>();
        constraints.put("dateFrom", "2025-12-31");
        constraints.put("dateTo", "2020-01-01");
        col.setConstraints(constraints);

        PreviewRequestDTO request = new PreviewRequestDTO(List.of(col), 5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.generatePreview(request));
        assertTrue(ex.getMessage().contains("dateFrom"));
    }

    // ─── AC4 : colonnes multiples sans contraintes ─────────────────────────────

    @Test
    void generatePreview_multipleColumnsNoConstraints_succeeds() {
        List<ColumnConfigDTO> cols = List.of(
            new ColumnConfigDTO("prenom", ColumnType.FIRST_NAME),
            new ColumnConfigDTO("nom", ColumnType.LAST_NAME),
            new ColumnConfigDTO("email", ColumnType.EMAIL),
            new ColumnConfigDTO("montant", ColumnType.AMOUNT),
            new ColumnConfigDTO("date", ColumnType.DATE)
        );
        PreviewRequestDTO request = new PreviewRequestDTO(cols, 5);

        PreviewResponseDTO response = service.generatePreview(request);

        assertEquals(5, response.getPreviewRows().size());
        assertEquals(5, response.getColumnCount());
        response.getPreviewRows().forEach(row -> {
            assertEquals(5, row.size());
            cols.forEach(col -> assertNotNull(row.get(col.getName())));
        });
    }

    // ─── backward compat : AmountGenerator sans constraints ───────────────────

    @Test
    void generatePreview_amountWithMinMaxValue_backwardCompat() {
        ColumnConfigDTO col = new ColumnConfigDTO("montant", ColumnType.AMOUNT);
        col.setMinValue(10);
        col.setMaxValue(50);

        PreviewRequestDTO request = new PreviewRequestDTO(List.of(col), 3);
        PreviewResponseDTO response = service.generatePreview(request);

        assertEquals(3, response.getPreviewRows().size());
        for (Map<String, Object> row : response.getPreviewRows()) {
            double val = Double.parseDouble(row.get("montant").toString());
            assertTrue(val >= 10 && val <= 50, "Amount " + val + " should be in [10, 50]");
        }
    }
}
