---
sprint: 2
storyId: 2-9
title: Implement Dataset Versioning & Reset
points: 5
epic: EPIC 2 - Data Generation Engine
type: Backend Feature
status: ready
dependsOn:
  - S2.3 (DataSet entity complete)
date_created: 2026-02-28
assigned_to: Amelia Dev
---

# S2.9: Implement Dataset Versioning & Reset

**Points:** 5  
**Epic:** EPIC 2: Data Generation Engine  
**Type:** Backend Feature

---

## Description

Implémenter système de versioning pour conserver une copie originale de chaque jeu de données généré. Permettre à l'utilisateur de réinitialiser un dataset modifié et retrouver la version originale.

---

## Acceptance Criteria

- [x] DataSetVersion entity created:
  - `id` (PK)
  - `datasetId` (FK to DataSet)
  - `versionNumber` (Integer, starts at 0)
  - `dataJson` (LONGTEXT) - Full data snapshot
  - `metadata` (JSON) - Column config, generation params
  - `createdAt` (timestamp)
  - `isOriginal` (boolean) - Mark the original version

- [x] Versioning Logic:
  - Version 0 = Original generated data (immutable)
  - Version 1+ = Modifications/updates
  - Auto-increment on each modification
  - Soft deletions don't create versions

- [x] Original Data Preservation:
  - When dataset created: Save complete generated data as Version 0
  - Mark as `isOriginal = true`
  - Lock Version 0 from modifications

- [x] Endpoint: `GET /api/data-sets/{id}/versions`
  - List all versions of a dataset
  - Return: versionNumber, createdAt, size, isOriginal
  - Include metadata summary

- [x] Endpoint: `GET /api/data-sets/{id}/versions/{versionNumber}`
  - Retrieve specific version data
  - Full dataset content
  - Read-only (cannot modify specific version)

- [x] Endpoint: `POST /api/data-sets/{id}/reset`
  - Reset dataset to original (Version 0)
  - Copy original data back to current DataSet.dataJson
  - Create new version entry for the reset action
  - Return updated DataSetDTO with confirmation

- [x] Endpoint: `DELETE /api/data-sets/{id}/versions/{versionNumber}`
  - Delete specific version (except original)
  - Validate cannot delete Version 0
  - Return 403 if attempt to delete original

- [x] Reset Confirmation:
  - POST reset endpoint returns: `{data: {...}, message: "Dataset reset to original", resetAt: ...}`
  - Include original data size and modification count discarded

- [x] Performance:
  - Reset operation < 200ms
  - Version history storage efficient (up to 100 versions per dataset)
  - Query latest version < 50ms

- [x] Error Handling:
  - 404 if dataset or version not found
  - 403 if attempting to delete original version
  - 409 if resetting already-original dataset
  - Structured error responses

- [x] Tests:
  - Unit tests for versioning logic (>80% coverage)
  - Integration tests for all endpoints
  - Test reset functionality
  - Test version history pagination
  - Performance tests
  - Test edge cases (no modifications, many versions)

---

## Technical Notes

- Create DataSetVersionRepository extending JpaRepository
- Add cascade delete: when DataSet deleted, versions deleted
- Database indexes on: datasetId, versionNumber, createdAt
- Consider data compression for version storage (large datasets)
- Log reset actions for audit trail
- Version metadata: store column config, generation parameters

---

## Tasks

### Task 2.9.1: Create Versioning Infrastructure
- [ ] Create DataSetVersion entity
- [ ] Create DataSetVersionRepository
- [ ] Add indexes and constraints
- [ ] Create database migration

### Task 2.9.2: Implement Versioning Service
- [ ] Create VersioningService interface
- [ ] Implement auto-versioning on creation
- [ ] Implement getVersion(datasetId, versionNumber)
- [ ] Implement reset(datasetId) logic
- [ ] Implement deleteVersion(datasetId, versionNumber)

### Task 2.9.3: Expose Versioning Endpoints
- [ ] Create VersionController
- [ ] Endpoint: GET /api/data-sets/{id}/versions (list)
- [ ] Endpoint: GET /api/data-sets/{id}/versions/{versionNumber} (get)
- [ ] Endpoint: POST /api/data-sets/{id}/reset (reset)
- [ ] Endpoint: DELETE /api/data-sets/{id}/versions/{versionNumber} (delete version)
- [ ] Add OpenAPI documentation

### Task 2.9.4: Tests & Validation
- [ ] Unit tests for VersioningService (>80%)
- [ ] Integration tests for all endpoints
- [ ] Reset functionality tests
- [ ] Permission/validation tests (cannot delete original)
- [ ] Performance tests
- [ ] Add implementation Javadoc

---

## Definition of Done

- [x] Versioning system fully functional
- [x] All endpoints implemented and tested
- [x] Original data properly preserved
- [x] Reset functionality working
- [x] Performance acceptable
- [x] All acceptance criteria met
- [x] Tests > 80% coverage
- [x] OpenAPI documentation complete
- [x] Code review approved

---

## Notes

**Status:** Ready for implementation (after S2.3)  
**Depends on:** S2.3 (DataSet entity)  
**Blocks:** None (feature is additive)  
**Integration:** S2.8 (Activity Tracking) - can track reset actions  
**Future:** S2.7 (Data Viewer) could show version dropdown

---
