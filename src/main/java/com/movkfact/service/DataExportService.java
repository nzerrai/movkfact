package com.movkfact.service;

import com.movkfact.entity.DataSet;
import com.movkfact.repository.DataSetRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * S2.4: Data Export Service
 * Handles JSON export for GeneratedDataSet objects.
 * 
 * Features:
 * - Pretty/Compact formatting
 * - Column filtering/selection
 * - Conditional extraction (basic equality filters)
 * - Memory efficient (streaming-ready architecture)
 * - Performance: <500ms for 10K rows
 * 
 * @author Amelia Dev
 */
@Service
public class DataExportService {

    @Autowired
    private DataSetRepository dataSetRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final int PERFORMANCE_THRESHOLD_MS = 500;
    private static final int LARGE_DATASET_THRESHOLD = 5000; // rows

    /**
     * Export DataSet as JSON (API consumption).
     * 
     * AC: Export en JSON natif avec formatage (pretty par défaut)
     * Performance: <500ms pour 10K rows
     * 
     * @param datasetId Dataset ID to export
     * @param pretty true for indented JSON, false for compact
     * @param columnsFilter Optional comma-separated column names to include (null = all)
     * @param filterCondition Optional filter: "columnName:value" (null = no filtering)
     * @return Formatted JSON string
     * @throws IllegalArgumentException if dataset not found or parameters invalid
     */
    public String exportAsJson(Long datasetId, boolean pretty, String columnsFilter, String filterCondition) 
            throws JsonProcessingException {
        
        long startTime = System.currentTimeMillis();
        
        DataSet dataSet = dataSetRepository.findById(datasetId)
            .orElseThrow(() -> new IllegalArgumentException("DataSet not found: " + datasetId));
        
        if (dataSet.getDataJson() == null || dataSet.getDataJson().isEmpty()) {
            return pretty ? "[]" : "[]";
        }
        
        // Parse stored JSON data
        List<Map<String, Object>> data = objectMapper.readValue(
            dataSet.getDataJson(), 
            objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
        );
        
        // Apply column filtering if specified
        if (columnsFilter != null && !columnsFilter.isEmpty()) {
            Set<String> selectedColumns = new HashSet<>(Arrays.asList(columnsFilter.split(",")));
            data = filterColumns(data, selectedColumns);
        }
        
        // Apply conditional extraction if specified
        if (filterCondition != null && !filterCondition.isEmpty()) {
            data = filterByCondition(data, filterCondition);
        }
        
        // Format output
        if (pretty) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        } else {
            objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        }
        
        String result = objectMapper.writeValueAsString(data);
        
        // Performance logging
        long duration = System.currentTimeMillis() - startTime;
        if (duration > PERFORMANCE_THRESHOLD_MS) {
            System.err.println(String.format(
                "PERF WARNING: DataSet export took %dms (threshold: %dms) for %d rows",
                duration, PERFORMANCE_THRESHOLD_MS, data.size()
            ));
        }
        
        return result;
    }

    /**
     * Export DataSet with metadata for download.
     * 
     * AC: Exporte avec headers Content-Disposition pour téléchargement
     * 
     * @param datasetId Dataset ID
     * @param pretty Formatting option
     * @param columnsFilter Optional column selection
     * @param filterCondition Optional filter
     * @return JSON wrapper with metadata and data
     * @throws JsonProcessingException if serialization fails
     */
    public Map<String, Object> exportForDownload(Long datasetId, boolean pretty, String columnsFilter, String filterCondition) 
            throws JsonProcessingException {
        
        DataSet dataSet = dataSetRepository.findById(datasetId)
            .orElseThrow(() -> new IllegalArgumentException("DataSet not found: " + datasetId));
        
        String jsonData = exportAsJson(datasetId, pretty, columnsFilter, filterCondition);
        List<Map<String, Object>> parsedData = objectMapper.readValue(
            jsonData,
            objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
        );
        
        // Build download metadata
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("exportDate", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        metadata.put("datasetId", datasetId);
        metadata.put("datasetName", dataSet.getName());
        metadata.put("rowCount", parsedData.size());
        metadata.put("totalRowsGenerated", dataSet.getRowCount());
        metadata.put("generatedAt", dataSet.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        metadata.put("format", "JSON");
        metadata.put("filename", generateFilename(dataSet.getName()));
        
        result.put("metadata", metadata);
        result.put("data", parsedData);
        
        return result;
    }

    /**
     * Validate export parameters before execution.
     * 
     * @param datasetId Dataset ID
     * @param columnsFilter Column filter (optional)
     * @param filterCondition Condition filter (optional)
     * @throws IllegalArgumentException if validation fails
     */
    public void validateExportParameters(Long datasetId, String columnsFilter, String filterCondition) {
        // Validate dataset exists
        if (!dataSetRepository.existsById(datasetId)) {
            throw new IllegalArgumentException("DataSet not found: " + datasetId);
        }
        
        // Validate columns filter format
        if (columnsFilter != null && !columnsFilter.isEmpty()) {
            String[] columns = columnsFilter.split(",");
            for (String col : columns) {
                if (col.trim().isEmpty()) {
                    throw new IllegalArgumentException("Invalid column name (empty)");
                }
            }
        }
        
        // Validate filter condition format
        if (filterCondition != null && !filterCondition.isEmpty()) {
            if (!filterCondition.contains(":")) {
                throw new IllegalArgumentException("Filter condition must be format 'columnName:value'");
            }
            String[] parts = filterCondition.split(":", 2);
            if (parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid filter condition format");
            }
        }
    }

    /**
     * Filter data to include only specified columns.
     * 
     * @param data Raw data list
     * @param selectedColumns Column names to keep
     * @return Filtered data with only selected columns
     */
    private List<Map<String, Object>> filterColumns(List<Map<String, Object>> data, Set<String> selectedColumns) {
        return data.stream()
            .map(row -> {
                Map<String, Object> filtered = new HashMap<>();
                selectedColumns.forEach(col -> {
                    if (row.containsKey(col)) {
                        filtered.put(col, row.get(col));
                    }
                });
                return filtered;
            })
            .collect(Collectors.toList());
    }

    /**
     * Filter data by condition (basic equality).
     * Format: "columnName:value"
     * 
     * @param data Raw data
     * @param filterCondition Filter in format "col:value"
     * @return Filtered data matching condition
     */
    private List<Map<String, Object>> filterByCondition(List<Map<String, Object>> data, String filterCondition) {
        String[] parts = filterCondition.split(":", 2);
        String columnName = parts[0].trim();
        String value = parts[1].trim();
        
        return data.stream()
            .filter(row -> {
                Object rowValue = row.get(columnName);
                return rowValue != null && rowValue.toString().equals(value);
            })
            .collect(Collectors.toList());
    }

    /**
     * Generate filename for download.
     * Format: {datasetName}_export_{timestamp}.json
     * 
     * @param datasetName Name of dataset
     * @return Filename safe for download
     */
    private String generateFilename(String datasetName) {
        String safeName = datasetName.replaceAll("[^a-zA-Z0-9_-]", "_");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_export_%s.json", safeName, timestamp);
    }
}
