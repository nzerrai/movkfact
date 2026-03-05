package com.movkfact.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.dto.PagedRowsResponseDTO;
import com.movkfact.dto.RowResponseDTO;
import com.movkfact.entity.Activity;
import com.movkfact.entity.ActivityActionType;
import com.movkfact.entity.DataSet;
import com.movkfact.exception.EntityNotFoundException;
import com.movkfact.repository.DataSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DataRowEditorServiceTest {

    @Mock
    private DataSetRepository dataSetRepository;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private DataRowEditorService dataRowEditorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject real ObjectMapper via reflection (field injection)
        try {
            var field = DataRowEditorService.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(dataRowEditorService, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private DataSet buildDataset(String json, int rowCount) {
        DataSet ds = new DataSet();
        ds.setDataJson(json);
        ds.setRowCount(rowCount);
        ds.setVersion(0);
        ds.setOriginalData(json); // originalData = même json initial
        return ds;
    }

    private String twoRowsJson() {
        return "[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25}]";
    }

    // ─── getRows ──────────────────────────────────────────────────────────────

    @Test
    void getRows_returnsCorrectPagination() throws Exception {
        DataSet ds = buildDataset(twoRowsJson(), 2);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));

        PagedRowsResponseDTO result = dataRowEditorService.getRows(1L, 0, 10);

        assertEquals(2, result.getTotalRows());
        assertEquals(2, result.getRows().size());
        assertEquals(0, result.getRows().get(0).getRowIndex());
        assertEquals(1, result.getRows().get(1).getRowIndex());
        assertEquals("Alice", result.getRows().get(0).getData().get("name"));
    }

    @Test
    void getRows_pageSizeRespected() {
        DataSet ds = buildDataset(twoRowsJson(), 2);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));

        PagedRowsResponseDTO result = dataRowEditorService.getRows(1L, 0, 1);

        assertEquals(2, result.getTotalRows());
        assertEquals(1, result.getRows().size());
        assertEquals(0, result.getRows().get(0).getRowIndex());
    }

    @Test
    void getRows_datasetNotFound_throws404() {
        when(dataSetRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> dataRowEditorService.getRows(99L, 0, 50));
    }

    // ─── getRow ───────────────────────────────────────────────────────────────

    @Test
    void getRow_returnsExactRow() {
        DataSet ds = buildDataset(twoRowsJson(), 2);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));

        RowResponseDTO result = dataRowEditorService.getRow(1L, 1);

        assertEquals(1, result.getRowIndex());
        assertEquals("Bob", result.getData().get("name"));
    }

    @Test
    void getRow_outOfBounds_throws404() {
        DataSet ds = buildDataset(twoRowsJson(), 2);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));

        assertThrows(EntityNotFoundException.class, () -> dataRowEditorService.getRow(1L, 5));
    }

    // ─── updateRow ────────────────────────────────────────────────────────────

    @Test
    void updateRow_partialMerge_onlyUpdatesProvidedColumns() {
        DataSet ds = buildDataset(twoRowsJson(), 2);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));
        when(dataSetRepository.save(any())).thenReturn(ds);
        when(activityService.recordRowActivity(anyLong(), any(), any(), anyInt(), any(), any()))
                .thenReturn(new Activity());

        RowResponseDTO result = dataRowEditorService.updateRow(1L, 0, Map.of("age", 99), null);

        assertEquals(99, result.getData().get("age"));
        assertEquals("Alice", result.getData().get("name")); // untouched
    }

    @Test
    void updateRow_versionIncremented() {
        DataSet ds = buildDataset(twoRowsJson(), 2);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));
        when(dataSetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(activityService.recordRowActivity(anyLong(), any(), any(), anyInt(), any(), any()))
                .thenReturn(new Activity());

        dataRowEditorService.updateRow(1L, 0, Map.of("age", 99), null);

        ArgumentCaptor<DataSet> captor = ArgumentCaptor.forClass(DataSet.class);
        verify(dataSetRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getVersion());
    }

    @Test
    void updateRow_activitySnapshotContainsOnlyChangedColumns() {
        DataSet ds = buildDataset(twoRowsJson(), 2);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));
        when(dataSetRepository.save(any())).thenReturn(ds);
        when(activityService.recordRowActivity(anyLong(), any(), any(), anyInt(), any(), any()))
                .thenReturn(new Activity());

        dataRowEditorService.updateRow(1L, 0, Map.of("age", 99), "tester");

        verify(activityService).recordRowActivity(
                eq(1L), eq(ActivityActionType.ROW_MODIFIED), eq("tester"),
                eq(0), contains("age"), contains("30") // previous value was 30
        );
    }

    @Test
    void updateRow_emptyColumns_throwsIllegalArgument() {
        DataSet ds = buildDataset(twoRowsJson(), 2);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));

        assertThrows(IllegalArgumentException.class,
                () -> dataRowEditorService.updateRow(1L, 0, Map.of(), null));
    }

    @Test
    void updateRow_originalDataUnchanged() {
        String original = twoRowsJson();
        DataSet ds = buildDataset(original, 2);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));
        when(dataSetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(activityService.recordRowActivity(anyLong(), any(), any(), anyInt(), any(), any()))
                .thenReturn(new Activity());

        dataRowEditorService.updateRow(1L, 0, Map.of("age", 99), null);

        assertEquals(original, ds.getOriginalData()); // NEVER touched
    }

    // ─── deleteRow ────────────────────────────────────────────────────────────

    @Test
    void deleteRow_removesRowAndCompacts() {
        DataSet ds = buildDataset(twoRowsJson(), 2);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));
        when(dataSetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(activityService.recordRowActivity(anyLong(), any(), any(), anyInt(), any(), any()))
                .thenReturn(new Activity());

        dataRowEditorService.deleteRow(1L, 0, null);

        ArgumentCaptor<DataSet> captor = ArgumentCaptor.forClass(DataSet.class);
        verify(dataSetRepository).save(captor.capture());

        DataSet saved = captor.getValue();
        assertEquals(1, saved.getRowCount());   // décrémenté
        assertEquals(1, saved.getVersion());    // incrémenté
        assertTrue(saved.getDataJson().contains("Bob")); // Bob est maintenant index 0
        assertFalse(saved.getDataJson().contains("Alice")); // Alice supprimée
    }

    @Test
    void deleteRow_activityRecordedWithFullSnapshot() {
        DataSet ds = buildDataset(twoRowsJson(), 2);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));
        when(dataSetRepository.save(any())).thenReturn(ds);
        when(activityService.recordRowActivity(anyLong(), any(), any(), anyInt(), any(), any()))
                .thenReturn(new Activity());

        dataRowEditorService.deleteRow(1L, 0, "admin");

        verify(activityService).recordRowActivity(
                eq(1L), eq(ActivityActionType.ROW_DELETED), eq("admin"),
                eq(0), isNull(), contains("Alice")
        );
    }

    @Test
    void deleteRow_outOfBounds_throws404() {
        DataSet ds = buildDataset(twoRowsJson(), 2);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));

        assertThrows(EntityNotFoundException.class, () -> dataRowEditorService.deleteRow(1L, 99, null));
    }

    // ─── Circuit breaker 413 ──────────────────────────────────────────────────

    @Test
    void updateRow_circuitBreaker_throws413WhenRowCountExceeds50k() {
        DataSet ds = buildDataset(twoRowsJson(), 50_001);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> dataRowEditorService.updateRow(1L, 0, Map.of("age", 1), null));
        assertEquals(413, ex.getStatusCode().value());
    }

    @Test
    void deleteRow_circuitBreaker_throws413WhenRowCountExceeds50k() {
        DataSet ds = buildDataset(twoRowsJson(), 50_001);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> dataRowEditorService.deleteRow(1L, 0, null));
        assertEquals(413, ex.getStatusCode().value());
    }

    @Test
    void getRows_notAffectedByCircuitBreaker() {
        DataSet ds = buildDataset(twoRowsJson(), 50_001);
        when(dataSetRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ds));

        // GET doit fonctionner même avec rowCount > 50k
        assertDoesNotThrow(() -> dataRowEditorService.getRows(1L, 0, 10));
    }
}
