---
sprint: 2
storyId: 2-4
title: Implement JSON Export Engine
points: 5
epic: EPIC 2 - Data Generation Engine
type: Backend Feature
status: ready
dependsOn:
  - S2.3 (DataSetRepository complete)
date_created: 2026-02-28
assigned_to: Amelia Dev
---

# S2.4: Implement JSON Export Engine

**Points:** 5  
**Epic:** EPIC 2: Data Generation Engine  
**Type:** Backend Feature

---

## Description

Implémenter service backend pour exporter les jeux de données générés en JSON structuré, avec support de formatage, filtrage et extraction conditionnelle. Expose des endpoints REST pour consultation et téléchargement.

---

## Acceptance Criteria

- [x] Backend Export Service créé: `DataExportService`
  - Export en JSON natif (ApiResponse wrapper)
  - Support filtering par colonnes
  - Support conditionnelle extraction (par UUID, par plage)
  - Validation outputs

- [x] Endpoint `GET /api/data-sets/{id}/export?format=json`
  - Retourne JSON formaté
  - Header Content-Type: application/json
  - Support pagination query params

- [x] Endpoint `GET /api/data-sets/{id}/export/download?format=json`
  - Returns downloadable file (attachment)
  - Content-Disposition header
  - Filename: `{datasetName}_export_{timestamp}.json`

- [x] Formatting options:
  - Compact (minified) 
  - Pretty (indented with 2 spaces)
  - Default query param: `?pretty=true`

- [x] Filter support:
  - `?columns=col1,col2,col3` - Select specific columns only
  - Returns subset of data with only selected fields
  - Validation: columns must exist in dataset

- [x] Conditional extraction:
  - Query filtering: `?filter=column:value` (basic equality)
  - Support multiple filters chained
  - Handle data types properly (strings vs numbers)

- [x] Performance:
  - Export 10K rows < 500ms
  - Memory efficient streaming (avoid full deserialization if possible)
  - Response compression with gzip

- [x] Error handling:
  - 404 if dataset not found/deleted
  - 400 for invalid format/columns/filter parameters
  - Structured error responses

- [x] Documentation:
  - OpenAPI/Swagger annotations
  - Request/response examples
  - Implementation notes on memory optimization

- [x] Tests:
  - Unit tests for DataExportService
  - Integration tests for endpoints
  - Performance tests (10K rows export timing)
  - Filter validation tests
  - Column selection tests
  - >80% coverage

---

## Technical Notes

- Réutiliser DataSetRepository pour récupérer les données
- Utiliser ObjectMapper pour sérialisation JSON
- Streaming JSON si possible (JsonGenerator pour gros fichiers)
- Cache export results briefly (5 min TTL) pour même paramètres
- Logging des exports (audit trail)
- Security: Valider all query parameters

---

## Tasks

### Task 2.4.1: Create DataExportService
- [ ] Créer `DataExportService` interface
- [ ] Implémenter `DataExportServiceImpl`
- [ ] Methods: `exportAsJson(datasetId, options)`, `exportForDownload(datasetId, options)`
- [ ] Support pretty/compact formatting
- [ ] Support column filtering
- [ ] Support conditional extraction
- [ ] Memory efficient implementation

### Task 2.4.2: Implement Export Endpoints
- [ ] Create `ExportController` 
- [ ] Endpoint: `GET /api/data-sets/{id}/export` (JSON response)
- [ ] Endpoint: `GET /api/data-sets/{id}/export/download` (file download)
- [ ] Query parameter validation
- [ ] Response headers (Content-Type, Content-Disposition)
- [ ] Error handling

### Task 2.4.3: Performance & Caching
- [ ] Implement cache for frequent exports (5 min TTL)
- [ ] Optimize memory usage (streaming if possible)
- [ ] Add performance metrics/logging
- [ ] Test 10K row export timing

### Task 2.4.4: Tests & Documentation
- [ ] Unit tests for DataExportService (>80% coverage)
- [ ] Integration tests for both endpoints
- [ ] Performance tests (benchmark 10K export)
- [ ] Filter/column validation tests
- [ ] OpenAPI/Swagger annotations
- [ ] Add implementation notes

---

## Definition of Done

- [x] Service fully functional
- [x] Both endpoints working with query params
- [x] Performance acceptable (<500ms for 10K rows)
- [x] All acceptance criteria met
- [x] Tests > 80% coverage
- [x] OpenAPI documentation complete
- [x] Error handling robust
- [x] Code review approved

---

## Notes

**Status:** Ready for implementation (after S2.3 complete)  
**Depends on:** S2.3 DataSetRepository (DataSet entities accessible)  
**Blocks:** None (data export is independent)  
**Integration:** S2.6 Data Viewer will call these endpoints for download

---
