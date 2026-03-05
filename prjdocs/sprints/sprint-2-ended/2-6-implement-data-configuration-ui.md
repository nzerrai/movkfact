---
sprint: 2
storyId: 2-6
title: Implement Data Configuration Interface
points: 6
epic: EPIC 2 - Data Generation Engine
type: Frontend Feature
status: ready
dependsOn:
  - S2.5 (CSV Upload UI)
  - S2.3 (Generation API)
date_created: 2026-02-28
assigned_to: Sally UX Designer
---

# S2.6: Implement Data Configuration Interface

**Points:** 6  
**Epic:** EPIC 2: Data Generation Engine  
**Type:** Frontend Feature

---

## Description

Créer interface de configuration permettant de customizer les colonnes (types, formats, plages, contraintes) et lancer la génération de données. Affichage des résultats générés avec options d'export/download.

---

## Acceptance Criteria

- [x] ConfigurationPanel React component créé avec MUI
  - Affiche colonnes avec contrôles de configuration
  - Support pour chaque type (Personal, Financial, Temporal)
  - Input de nombre lignes à générer
  - Validation des inputs

- [x] Configuration forms par type:
  - **Personal:** Format name, email domain pattern, phone format, address format
  - **Financial:** Currency, amount range, account masking
  - **Temporal:** Date range, timezone, format

- [x] Generation Controls:
  - Input field: nombre de lignes (min 1, max 100,000)
  - Validation: positive integer, reasonable max
  - "Generate" button
  - Loading indicator during generation

- [x] Results Display:
  - JSON data preview with syntax highlighting
  - Row count statistics
  - Generation time metrics
  - Sample data display (first 10 rows by default)

- [x] Export Options:
  - Download JSON file button
  - Download CSV file button (if S2.4 export available)
  - Copy data to clipboard
  - Show file size estimate

- [x] Error Handling:
  - Display validation errors for invalid config
  - Show API errors from S2.3 generation endpoint
  - User-friendly error messages
  - Retry button on failure

- [x] State Management:
  - Store config in Context API
  - Persist config between page navigation
  - Clear config on new upload

- [x] Responsive Design:
  - Desktop: Full form + results side-by-side
  - Tablet: Stacked layout
  - Mobile: Single column, simplified controls

- [x] Accessibility:
  - ARIA labels on all inputs
  - Keyboard navigation through form
  - Screen reader friendly
  - Error announcements

- [x] Tests:
  - Jest tests > 80% coverage
  - Test configuration form validation
  - Test generation API integration
  - Test result display
  - Accessibility tests

---

## Technical Notes

- Use react-hook-form or Formik for form handling
- Store config in CsvContext
- Call S2.3 POST /api/domains/{domainId}/data-sets endpoint
- Display results with react-json-view or SyntaxHighlighter
- Use FileSaver.js for downloads
- Handle large results efficiently (virtualization if needed)
- Validation: client-side + server-side

---

## Tasks

### Task 2.6.1: Create Configuration Form Components
- [ ] Créer ConfigurationPanel component
- [ ] Créer PersonalFieldConfig sub-component
- [ ] Créer FinancialFieldConfig sub-component
- [ ] Créer TemporalFieldConfig sub-component
- [ ] Implement form validation
- [ ] MUI styling and layout

### Task 2.6.2: Implement Generation Logic
- [ ] Connect to S2.3 API endpoint: POST /api/domains/{domainId}/data-sets
- [ ] Send configuration + row count
- [ ] Handle loading/error states
- [ ] Update Context with results
- [ ] Display generation metrics

### Task 2.6.3: Results Display & Export
- [ ] Create ResultViewer component
- [ ] Display JSON data with syntax highlighting
- [ ] Show statistics (generation time, row count)
- [ ] Implement download JSON button
- [ ] Implement download CSV button (if export available)
- [ ] Implement copy to clipboard

### Task 2.6.4: Tests & Polish
- [ ] Jest tests > 80% coverage
- [ ] Test form validation
- [ ] Test API integration
- [ ] Accessibility tests (axe-core)
- [ ] Responsive design verification

---

## Definition of Done

- [x] Component fully functional
- [x] Form working with all field types
- [x] Generation integration working
- [x] Results display correct
- [x] Export/download working
- [x] All acceptance criteria met
- [x] Jest tests > 80% coverage
- [x] Accessibility verified
- [x] Responsive verified
- [x] Code review approved

---

## Notes

**Status:** Ready for implementation  
**Depends on:** S2.5 (CSV Upload), S2.3 (Generation API)  
**Blocks:** S2.7 (Data Viewer - can view generated data)  
**Flow:** CSV Upload (S2.5) → Configuration (S2.6) → Generation → Results Display (S2.6) → View/Export (S2.7)

---
