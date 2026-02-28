---
sprint: 2
storyId: 2-7
title: Implement Data Viewer & Management UI
points: 6
epic: EPIC 2 - Data Generation Engine
type: Frontend Feature
status: ready
dependsOn:
  - Sprint 1 Complete (Frontend base)
  - S2.3 (Data API endpoints)
  - S2.6 (Generation workflow)
date_created: 2026-02-28
assigned_to: Sally UX Designer
---

# S2.7: Implement Data Viewer & Management UI

**Points:** 6  
**Epic:** EPIC 2: Data Generation Engine  
**Type:** Frontend Feature

---

## Description

Créer interface pour visualiser, gérer et exporter les jeux de données générés. Support complet pour consultation, pagination, filtrage et suppression avec statistiques détaillées.

---

## Acceptance Criteria

- [x] DataViewer React component créé avec MUI DataGrid
  - Affiche toutes les colonnes du jeu
  - Pagination avec configurable rows per page
  - Sorting sur chaque colonne
  - Responsive table (mobile: compact view)

- [x] DatasetManagement page affichant:
  - Liste tous les datasets du domaine
  - Statut, date création, nombre lignes
  - Activity status (GENERATED, VIEWED, DOWNLOADED, MODIFIED)
  - Actions: View, Download, Delete, Regenerate

- [x] Viewer features:
  - Export data as CSV (utiliser react-csv)
  - Export as JSON (call S2.4 export API if available)
  - Copy table to clipboard
  - Search/filtrage par valeur
  - Row count stats
  - Generation time display

- [x] Delete confirmation dialog
  - Show dataset name
  - Warn user about permanent deletion
  - Confirm before deleting

- [x] Dataset Details View:
  - Column information (name, type, count unique values)
  - Original statistics (min, max, avg if numeric)
  - Activity metadata (views, downloads, last accessed)
  - Version history link (if S2.9 implemented)
  - Export options

- [x] Pagination & Performance:
  - Handle large datasets efficiently (virtualization)
  - Max rows displayed: configurable (default 100)
  - Lazy load data if needed
  - Performance acceptable for 10K+ rows

- [x] Accessible:
  - ARIA labels
  - Keyboard navigation
  - Screen reader friendly
  - High contrast mode support

- [x] Responsive:
  - Desktop: Full DataGrid
  - Tablet: Compact view
  - Mobile: List view with expandable details

- [x] Tests:
  - Jest tests > 80% coverage
  - Test dataset list display
  - Test pagination
  - Test export functionality
  - Accessibility tests

---

## Technical Notes

- Use MUI DataGrid for performance with large datasets
- react-csv for CSV export
- FileSaver.js for download JSON (call S2.4 export API)
- Context API for dataset list
- Virtualization for large datasets (react-virtualized)
- Lazy loading of data if needed
- Handle streaming large files
- Call S2.3 GET endpoints for data retrieval

---

## Tasks

### Task 2.7.1: Create DataViewer Component
- [ ] Créer DataViewer.jsx component
- [ ] Implement MUI DataGrid or react-table
- [ ] Support pagination, sorting, filtering
- [ ] Display column data properly typed
- [ ] Handle large datasets with virtualization
- [ ] Responsive design

### Task 2.7.2: Create DatasetManagement Page
- [ ] Créer DatasetManagement.jsx page
- [ ] Afficher liste datasets du domaine (via S2.3 API)
- [ ] Display activity status
- [ ] Implement View/Download/Delete/Regenerate actions
- [ ] Add search/filter capability
- [ ] Display statistics

### Task 2.7.3: Implement Export & Actions
- [ ] Implement CSV export (react-csv)
- [ ] Implement JSON export (call S2.4 API)
- [ ] Implement delete action with confirmation
- [ ] Implement regenerate (restart flow from S2.5)
- [ ] Display activity metadata if S2.8 available

### Task 2.7.4: Tests & Polish
- [ ] Jest tests > 80% coverage
- [ ] Test list display
- [ ] Test export functionality
- [ ] Test delete confirmation
- [ ] Accessibility tests (axe-core)
- [ ] Responsive design verification
- [ ] Performance tests with large datasets

---

## Definition of Done

- [x] Components fully functional
- [x] DataGrid displaying data correctly
- [x] Pagination working
- [x] Export working (CSV + JSON if S2.4 available)
- [x] Delete working with confirmation
- [x] All acceptance criteria met
- [x] Jest tests > 80% coverage
- [x] Accessibility verified
- [x] Responsive verified
- [x] Code review approved

---

## Notes

**Status:** Ready for implementation  
**Depends on:** S2.3 (Data API), S2.4 (Export API - optional), S2.8 (Activity - optional), S2.6 (Config UI)  
**Blocks:** None  
**Integration:** Shows generated datasets, calls S2.3 APIs for data, calls S2.4 for exports if available

---
