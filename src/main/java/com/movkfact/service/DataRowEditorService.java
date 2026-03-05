package com.movkfact.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.dto.PagedRowsResponseDTO;
import com.movkfact.dto.RowResponseDTO;
import com.movkfact.entity.ActivityActionType;
import com.movkfact.entity.DataSet;
import com.movkfact.exception.EntityNotFoundException;
import com.movkfact.repository.DataSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for row-level dataset editing operations (S6.1).
 * Supports read, update, and delete of individual rows with activity tracking.
 */
@Service
public class DataRowEditorService {

    private static final int MAX_EDITABLE_ROW_COUNT = 50_000;

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Returns paginated rows from a dataset.
     *
     * @param datasetId dataset ID
     * @param page      zero-based page number
     * @param size      page size
     * @return paginated rows response
     */
    public PagedRowsResponseDTO getRows(Long datasetId, int page, int size) {
        DataSet dataset = findDatasetOrThrow(datasetId);
        List<Map<String, Object>> rows = parseRows(dataset.getDataJson());

        int total = rows.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<RowResponseDTO> pageRows = new ArrayList<>();
        for (int i = fromIndex; i < toIndex; i++) {
            pageRows.add(new RowResponseDTO(i, rows.get(i)));
        }

        return new PagedRowsResponseDTO(pageRows, total, page, size);
    }

    /**
     * Returns a single row by index.
     *
     * @param datasetId dataset ID
     * @param rowIndex  zero-based row index
     * @return row response
     */
    public RowResponseDTO getRow(Long datasetId, int rowIndex) {
        DataSet dataset = findDatasetOrThrow(datasetId);
        List<Map<String, Object>> rows = parseRows(dataset.getDataJson());

        if (rowIndex < 0 || rowIndex >= rows.size()) {
            throw new EntityNotFoundException("Row not found at index " + rowIndex);
        }
        return new RowResponseDTO(rowIndex, rows.get(rowIndex));
    }

    /**
     * Partially updates a row — only the provided columns are merged.
     * Increments version, records ROW_MODIFIED activity.
     *
     * @param datasetId      dataset ID
     * @param rowIndex       zero-based row index
     * @param updateColumns  columns to update
     * @param userName       actor user name (may be null)
     * @return updated row
     */
    @Transactional
    public RowResponseDTO updateRow(Long datasetId, int rowIndex, Map<String, Object> updateColumns, String userName) {
        if (updateColumns == null || updateColumns.isEmpty()) {
            throw new IllegalArgumentException("columns must not be null or empty");
        }

        DataSet dataset = findDatasetOrThrow(datasetId);
        checkSizeLimit(dataset);

        List<Map<String, Object>> rows = parseRows(dataset.getDataJson());
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            throw new EntityNotFoundException("Row not found at index " + rowIndex);
        }

        Map<String, Object> row = rows.get(rowIndex);

        // Snapshot only the columns being modified (before values)
        Map<String, Object> changedOnly = new HashMap<>();
        updateColumns.forEach((key, value) -> {
            changedOnly.put(key, row.get(key));
            row.put(key, value);
        });
        rows.set(rowIndex, row);

        // Persist
        dataset.setDataJson(serializeRows(rows));
        dataset.setVersion(dataset.getVersion() + 1);
        dataSetRepository.save(dataset); // @PreUpdate handles updatedAt

        // Activity
        String modifiedColumnsJson = serializeToJson(new ArrayList<>(updateColumns.keySet()));
        String previousValueJson = serializeToJson(changedOnly);
        activityService.recordRowActivity(datasetId, ActivityActionType.ROW_MODIFIED, userName,
                rowIndex, modifiedColumnsJson, previousValueJson);

        return new RowResponseDTO(rowIndex, row);
    }

    /**
     * Deletes a row by index. Subsequent rows are re-indexed (List.remove is O(n)).
     * Decrements rowCount, increments version, records ROW_DELETED activity.
     *
     * @param datasetId dataset ID
     * @param rowIndex  zero-based row index
     * @param userName  actor user name (may be null)
     */
    @Transactional
    public void deleteRow(Long datasetId, int rowIndex, String userName) {
        DataSet dataset = findDatasetOrThrow(datasetId);
        checkSizeLimit(dataset);

        List<Map<String, Object>> rows = parseRows(dataset.getDataJson());
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            throw new EntityNotFoundException("Row not found at index " + rowIndex);
        }

        Map<String, Object> snapshot = rows.remove(rowIndex); // indices suivants décrémentent automatiquement

        dataset.setDataJson(serializeRows(rows));
        dataset.setRowCount(dataset.getRowCount() - 1);
        dataset.setVersion(dataset.getVersion() + 1);
        dataSetRepository.save(dataset);

        // Activity — snapshot complet de la ligne supprimée
        String previousValueJson = serializeToJson(snapshot);
        activityService.recordRowActivity(datasetId, ActivityActionType.ROW_DELETED, userName,
                rowIndex, null, previousValueJson);
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private DataSet findDatasetOrThrow(Long datasetId) {
        return dataSetRepository.findByIdAndDeletedAtIsNull(datasetId)
                .orElseThrow(() -> new EntityNotFoundException("Dataset not found with id " + datasetId));
    }

    private void checkSizeLimit(DataSet dataset) {
        if (dataset.getRowCount() != null && dataset.getRowCount() > MAX_EDITABLE_ROW_COUNT) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Dataset trop volumineux pour édition ligne par ligne. Utilisez l'API bulk ou régénérez.");
        }
    }

    private List<Map<String, Object>> parseRows(String dataJson) {
        try {
            if (dataJson == null || dataJson.isBlank()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(dataJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to process dataset JSON: " + e.getMessage(), e);
        }
    }

    private String serializeRows(List<Map<String, Object>> rows) {
        try {
            return objectMapper.writeValueAsString(rows);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize dataset JSON: " + e.getMessage(), e);
        }
    }

    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
