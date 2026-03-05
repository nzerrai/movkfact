---
sprint: 2
storyId: 2-8
title: Implement Activity Tracking System
points: 5
epic: EPIC 2 - Data Generation Engine
type: Backend Feature
status: ready
dependsOn:
  - S2.3 (DataSet entity complete)
date_created: 2026-02-28
assigned_to: Amelia Dev
---

# S2.8: Implement Activity Tracking System

**Points:** 5  
**Epic:** EPIC 2: Data Generation Engine  
**Type:** Backend Feature

---

## Description

Implémenter système de suivi d'activité pour les jeux de données générés. Tracker les statuts (téléchargé, modifié, consulté) et fournir métadonnées d'utilisation pour chaque dataset.

---

## Acceptance Criteria

- [x] DataSet entity enhanced with activity fields:
  - `downloadedAt` (LocalDateTime) - First download timestamp
  - `viewedAt` (LocalDateTime) - Last viewed timestamp
  - `modifiedAt` (LocalDateTime) - Last modification timestamp
  - `downloadCount` (Integer) - Number of downloads
  - `viewCount` (Integer) - Number of views

- [x] Activity Status Enum:
  - GENERATED (initial state, not yet accessed)
  - VIEWED (at least one view)
  - DOWNLOADED (at least one download)
  - MODIFIED (after generation, if applicable)

- [x] ActivityService created:
  - `recordView(datasetId)` - Update viewedAt, increment viewCount
  - `recordDownload(datasetId)` - Update downloadedAt, increment downloadCount
  - `getActivityStatus(datasetId)` - Compute status based on fields
  - `getActivityMetadata(datasetId)` - Return full activity data

- [x] Update GET endpoints to record view:
  - GET /api/data-sets/{id} records a view
  - GET /api/data-sets/{id}/data records a view

- [x] Update download endpoints to record download:
  - GET /api/data-sets/{id}/export/download records a download
  - Implement similar for CSV export (if created)

- [x] Activity metadata in responses:
  - Include `activityStatus` in DataSetDTO
  - Include `lastViewedAt`, `downloadCount` in responses
  - Add to GET list endpoint: show activity summary

- [x] Activity History (optional for v1):
  - Create ActivityLog entity to track: datasetId, action (VIEW, DOWNLOAD), timestamp, userId (if auth added)
  - Repository: ActivityLogRepository with `findByDatasetId()`
  - Service method: `getActivityHistory(datasetId)` returns list

- [x] Performance:
  - Activity updates < 20ms
  - No blocking DB operations
  - Use DB indexes on datasetId, timestamps

- [x] Error Handling:
  - Handle non-existent datasets gracefully
  - Log errors without blocking request flow
  - Return partial data if tracking fails

- [x] Tests:
  - Unit tests for ActivityService (>80% coverage)
  - Integration tests for activity tracking in endpoints
  - Test status computation logic
  - Test incrementing counters
  - Performance tests for concurrent activity updates

---

## Technical Notes

- Add migration/SQL to update DataSet table with activity columns
- Use @PreUpdate/@PrePersist hooks for automatic timestamp management
- Consider async tracking for performance (separate thread or queue)
- Add database indexes on: datasetId, viewedAt, downloadedAt
- Logging: INFO level for tracking, ERROR level for failures
- No breaking changes to existing APIs

---

## Tasks

### Task 2.8.1: Enhance DataSet Entity
- [ ] Add activity fields to DataSet entity
- [ ] Create ActivityStatus enum
- [ ] Add getters/setters
- [ ] Create database migration

### Task 2.8.2: Create ActivityService
- [ ] Create ActivityService interface
- [ ] Implement service with tracking methods
- [ ] Implement activity history (ActivityLog entity + repo)
- [ ] Handle errors gracefully

### Task 2.8.3: Update Endpoints for Tracking
- [ ] Modify GET endpoints to call recordView()
- [ ] Modify download endpoints to call recordDownload()
- [ ] Ensure no performance impact
- [ ] Update DTOs to include activity data

### Task 2.8.4: Tests & Documentation
- [ ] Unit tests for ActivityService (>80%)
- [ ] Integration tests for endpoint tracking
- [ ] Performance tests
- [ ] Add Javadoc
- [ ] Document activity status computation logic

---

## Definition of Done

- [x] All activity fields implemented in DataSet
- [x] ActivityService fully functional
- [x] Activity tracking working in all endpoints
- [x] Metadata properly exposed in API responses
- [x] Performance acceptable
- [x] All acceptance criteria met
- [x] Tests > 80% coverage
- [x] Code review approved

---

## Notes

**Status:** Ready for implementation (after S2.3)  
**Depends on:** S2.3 (DataSet entity)  
**Blocks:** S2.7 (Data Viewer can show activity stats)  
**Integration:** Activity metadata shown in dataset list/detail views

---
