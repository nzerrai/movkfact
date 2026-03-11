package com.movkfact.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movkfact.dto.AnonymizationColumnConfig;
import com.movkfact.dto.DetectedColumn;
import com.movkfact.dto.TypeDetectionResult;
import com.movkfact.entity.DataSet;
import com.movkfact.repository.DataSetRepository;
import com.movkfact.service.AnonymizationService;
import com.movkfact.service.detection.CsvTypeDetectionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Endpoints d'anonymisation RGPD.
 *
 * POST /api/anonymize/inspect  — retourne les colonnes détectées (CSV ou JSON)
 * POST /api/anonymize/process  — retourne le fichier anonymisé en streaming
 *
 * Aucune donnée n'est persistée côté serveur.
 */
@RestController
@RequestMapping("/api/anonymize")
public class AnonymizationController {

    private final AnonymizationService service;
    private final DataSetRepository dataSetRepository;
    private final CsvTypeDetectionService detectionService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AnonymizationController(AnonymizationService service,
                                   DataSetRepository dataSetRepository,
                                   CsvTypeDetectionService detectionService) {
        this.service = service;
        this.dataSetRepository = dataSetRepository;
        this.detectionService = detectionService;
    }

    /**
     * Détecte les colonnes du fichier uploadé avec leurs types RGPD.
     * Pour CSV : utilise le pipeline de détection complet (patterns + data-based).
     * Pour JSON : retourne uniquement les noms de colonnes (inspection des en-têtes).
     */
    @PostMapping("/inspect")
    public ResponseEntity<Map<String, Object>> inspect(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "format", defaultValue = "csv") String format) throws IOException {

        Map<String, String> detectedTypes = new HashMap<>();
        List<String> columns;

        if ("json".equalsIgnoreCase(format)) {
            columns = service.inspectColumns(file, format);
        } else {
            TypeDetectionResult detection = detectionService.detectTypes(file, 100, false);
            columns = detection.getColumns().stream()
                    .map(DetectedColumn::getColumnName)
                    .collect(Collectors.toList());
            detection.getColumns().forEach(col -> {
                if (col.getDetectedType() != null) {
                    detectedTypes.put(col.getColumnName(), col.getDetectedType().name());
                }
            });
        }

        Map<String, Object> response = new HashMap<>();
        response.put("columns", columns);
        response.put("detectedTypes", detectedTypes);
        response.put("format", format);
        response.put("filename", file.getOriginalFilename());
        return ResponseEntity.ok(response);
    }

    /**
     * Anonymise le fichier et le retourne directement en téléchargement.
     * Le fichier original n'est jamais stocké sur le serveur.
     */
    @PostMapping("/process")
    public void process(
            @RequestParam("file") MultipartFile file,
            @RequestParam("config") String configJson,
            @RequestParam(value = "format", defaultValue = "csv") String format,
            HttpServletResponse response) throws IOException {

        List<AnonymizationColumnConfig> config = mapper.readValue(
            configJson, new TypeReference<>() {});

        String originalName = file.getOriginalFilename() != null
            ? file.getOriginalFilename().replaceFirst("\\.[^.]+$", "")
            : "fichier";

        if ("json".equalsIgnoreCase(format)) {
            response.setContentType("application/json");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + originalName + "_anonymise.json\"");
            service.anonymizeJson(file, config, response.getOutputStream());
        } else {
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + originalName + "_anonymise.csv\"");
            service.anonymizeCsv(file, config, response.getOutputStream());
        }
    }

    /**
     * Anonymise le fichier et sauvegarde le résultat comme un dataset MockFact dans un domaine.
     * Retourne les métadonnées du dataset créé (id, name).
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> save(
            @RequestParam("file") MultipartFile file,
            @RequestParam("config") String configJson,
            @RequestParam(value = "format", defaultValue = "csv") String format,
            @RequestParam("domainId") Long domainId,
            @RequestParam("datasetName") String datasetName) throws IOException {

        List<AnonymizationColumnConfig> config = mapper.readValue(
            configJson, new TypeReference<>() {});

        long start = System.currentTimeMillis();
        List<Map<String, Object>> rows = "json".equalsIgnoreCase(format)
            ? service.anonymizeJsonToRows(file, config)
            : service.anonymizeCsvToRows(file, config);
        long elapsed = System.currentTimeMillis() - start;

        String dataJson = mapper.writeValueAsString(rows);
        int columnCount = rows.isEmpty() ? 0 : rows.get(0).size();

        DataSet ds = new DataSet();
        ds.setDomainId(domainId);
        ds.setName(datasetName);
        ds.setRowCount(rows.size());
        ds.setColumnCount(columnCount);
        ds.setGenerationTimeMs(elapsed);
        ds.setDataJson(dataJson);
        ds.setOriginalData(dataJson);
        ds.setVersion(0);
        DataSet saved = dataSetRepository.save(ds);

        return ResponseEntity.ok(Map.of(
            "id", saved.getId(),
            "name", saved.getName(),
            "domainId", saved.getDomainId(),
            "rowCount", saved.getRowCount(),
            "columnCount", saved.getColumnCount()
        ));
    }
}
