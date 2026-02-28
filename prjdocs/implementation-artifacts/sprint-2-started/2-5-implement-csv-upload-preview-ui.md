---
sprint: 2
storyId: 2-5
title: Implement CSV Upload & Preview UI
points: 6
epic: EPIC 2 - Data Generation Engine
type: Frontend Feature
status: ready
dependsOn:
  - S2.2 (Type Detection API)
  - S2.3 (Data Generation API)
date_created: 2026-02-28
assigned_to: Sally UX Designer
---

# S2.5: Implement CSV Upload & Preview UI

**Points:** 6  
**Epic:** EPIC 2: Data Generation Engine  
**Type:** Frontend Feature

---

## Description

Créer composant React complet pour uploader un fichier CSV avec aperçu des colonnes, détection automatique des types via S2.2 API, et confirmation avant transmission à l'API de génération S2.3.

---

## Acceptance Criteria

- [x] CsvUploader React component créé avec MUI
  - Drag & drop support (visual feedback)
  - File picker interface (button + dialog)
  - Validation: CSV only, <10MB, properly formatted

- [x] File Upload Features:
  - Accept only .csv files
  - Check file size (max 10MB)
  - Show error if invalid format
  - Display upload progress
  - Support re-upload after error

- [x] Preview section showing:
  - Column headers detected
  - 1-3 example rows of data
  - MUI Table or DataGrid component
  - Styled with MUI theme

- [x] Type Detection Integration:
  - Call S2.2 API: `POST /api/domains/{domainId}/detect-types`
  - Display detected column types with icons
  - Show confidence scores (if available from S2.2)
  - Allow manual override with Select dropdowns
  - Highlight uncertain detections (confidence <80%)

- [x] Type Selection UI:
  - Dropdown for each column to change type
  - Show available types (6 Personal + 3 Financial + 4 Temporal)
  - Display format options per type (e.g., date format, currency)
  - Save temporary selection in Context

- [x] Buttons & Navigation:
  - "Select File" button to trigger file picker
  - "Detect Types" button (calls S2.2)
  - "Confirm & Next" button to proceed
  - "Cancel" to abandon

- [x] Error Messages:
  - User-friendly, contextual
  - Invalid file format
  - File too large
  - API errors (S2.2 detection failed)
  - Display in MUI Alert/Snackbar

- [x] Accessibility:
  - ARIA labels on buttons
  - Keyboard navigation (Tab through form)
  - Screen reader friendly
  - Error announcements

- [x] Responsive Design:
  - Desktop: Full preview table
  - Tablet: Compact preview
  - Mobile: Vertical stack, single column view
  - Touch-friendly dropdowns/buttons

- [x] Tests:
  - Jest unit tests > 80% coverage
  - Test file upload (valid, invalid, oversized)
  - Test type detection API integration
  - Test dropdown interactions
  - Accessibility tests (axe-core)

---

## Technical Notes

- Use react-dropzone for drag & drop
- Use papaparse for CSV parsing (front-end)
- Store uploaded CSV data in Context API temporarily
- Call S2.2 API to get type suggestions
- Allow user to confirm/override types
- On "Confirm & Next": Pass CSV + selected types to next flow (S2.6 Configuration)
- Handle large CSVs gracefully (don't freeze UI)
- Support multiple CSV encodings (UTF-8, ISO-8859-1)

---

## Tasks

### Task 2.5.1: Create CsvUploader Component
- [ ] Créer CsvUploader.jsx component
- [ ] Implement drag & drop (react-dropzone)
- [ ] Implement file picker (native input)
- [ ] File validation (format, size)
- [ ] Show upload progress indicator
- [ ] Handle errors gracefully

### Task 2.5.2: Implement CSV Preview
- [ ] Parse CSV using papaparse
- [ ] Extract column headers
- [ ] Display 1-3 example rows in MUI Table
- [ ] Responsive styling
- [ ] Handle large previews

### Task 2.5.3: Integrate Type Detection (S2.2)
- [ ] Call S2.2 API endpoint
- [ ] Display detected types with icons
- [ ] Show confidence scores
- [ ] Create type override dropdowns
- [ ] Handle API errors

### Task 2.5.4: Context & State Management
- [ ] Create/update CsvContext for CSV data
- [ ] Store: file name, headers, data, detected types
- [ ] Store: user-confirmed types
- [ ] Dispatch to Context on "Confirm"
- [ ] Handle validation states

### Task 2.5.5: Tests & Polish
- [ ] Jest tests > 80% coverage
- [ ] Test file upload scenarios (valid, invalid, oversized)
- [ ] Test type detection integration
- [ ] Accessibility tests (axe-core)
- [ ] Responsive design verification
- [ ] Error message testing

---

## Definition of Done

- [x] Component fully functional
- [x] File upload working (drag & drop + picker)
- [x] CSV preview displayed correctly
- [x] Type detection API integration working
- [x] Type override functionality working
- [x] All acceptance criteria met
- [x] Jest tests > 80% coverage
- [x] Accessibility verified
- [x] Responsive verified
- [x] Code review approved

---

## Notes

**Status:** Ready for implementation  
**Depends on:** S2.2 (Type Detection API), S2.3 (Generation API ready)  
**Blocks:** S2.6 (Configuration UI depends on this flow)  
**Flow:** CSV Upload (S2.5) → Type Config (if needed) → Generation Config (S2.6) → Run Generation (S2.3 API) → View Results (S2.7)

---
