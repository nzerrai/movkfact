# S2.7: Data Viewer UI - Design Specification

**Story Points**: 6  
**Sprint**: Sprint 2  
**Status**: 🔶 DESIGN PHASE  
**Date**: 01 Mars 2026  
**Dependencies**: S2.3 ✅, S2.4 ✅, S2.6 ✅ (all completed)

---

## User Story Overview

As a **data analyst**, I want to **view and explore generated datasets** with **sorting, filtering, and pagination** so that I can **validate data quality and extract specific records**.

---

## Acceptance Criteria

### AC 2.7.1: Display Generated Dataset (Must Have)
- [ ] Load dataset from S2.3 API response via ConfigurationPanel
- [ ] Display data in interactive table format (MUI Table)
- [ ] Show first 100 rows by default (pagination)
- [ ] Display row count: "Showing X-Y of Z rows total"
- [ ] Column headers match dataset column names
- [ ] Cell values truncated to 50 chars (tooltip on hover)

### AC 2.7.2: Sorting & Filtering (Must Have)
- [ ] Click column header to sort (ascending/descending)
- [ ] Sort indicator (↑/↓) on active column
- [ ] Filter by column with text input (case-insensitive contains)
- [ ] Filter by column type (dropdown: DATE, AMOUNT, TEXT, etc.)
- [ ] Clear all filters button
- [ ] Preserve filter/sort state during pagination

### AC 2.7.3: Pagination & Virtual Scrolling (Should Have)
- [ ] Rows per page selector (25, 50, 100, 250, 500)
- [ ] Previous/Next/Jump to page buttons
- [ ] Jump to page input (1-based)
- [ ] Display "Page X of Y"
- [ ] Virtual scrolling for >1000 rows (react-window, already installed)
- [ ] Performance: <200ms pagination for 10k rows

### AC 2.7.4: Export & Share (Must Have)
- [ ] "Export Filtered Results" button
  - Exports current filter/sort state (not full dataset)
  - Calls S2.4 API with column filter + sort params
  - Download as CSV
- [ ] "Download Full Dataset" button
  - Downloads all rows (calls S2.4 without filters)
- [ ] "Share as JSON" button
  - Copy filtered results as JSON to clipboard
- [ ] Disable buttons during async operations (show loading)

### AC 2.7.5: Data Quality Indicators (Nice to Have)
- [ ] Show "null" count per column (in column header tooltip)
- [ ] Highlight null/empty cells with light gray background
- [ ] Show data type icon per column (📅 DATE, 💰 AMOUNT, etc.)
- [ ] Row count accuracy: verify counts match H2 database

### AC 2.7.6: Responsive Design (Must Have)
- [ ] Desktop: Full table with all features
- [ ] Tablet (768px+): Horizontal scroll, smaller font
- [ ] Mobile (<768px): Card view (one row per card, vertical)
- [ ] No content shift on scroll

### AC 2.7.7: Error Handling (Must Have)
- [ ] Display error alert if dataset load fails (404, timeout, etc.)
- [ ] Retry button on error
- [ ] Graceful handling of malformed data (show "[Invalid Data]")
- [ ] Time out after 10 seconds if data doesn't load

### AC 2.7.8: Test Coverage (Must Have)
- [ ] >80% Jest test coverage
- [ ] Unit tests for all components
- [ ] Integration tests with mocked data
- [ ] Performance tests (sort/filter <50ms for 1000 rows)
- [ ] Accessibility tests (keyboard nav, screen readers)

---

## Architecture Design

### Component Hierarchy

```
DataViewerContainer (Orchestrator)
├── DatasetStats (statistics card)
│   ├── "X rows, Y columns"
│   ├── "Generated: 125ms ago"
│   └── Data type distribution
├── FilterBar (search & filtering controls)
│   ├── ColumnSelect (dropdown)
│   ├── FilterInput (text search)
│   ├── FilterTypeSelect (DATA_TYPE filter)
│   └── ClearFilters button
├── SortingOptions (hidden, applied on header click)
├── DataTable (main display)
│   ├── TableHead
│   │   ├── Column headers (click to sort)
│   │   └── Type icons
│   ├── TableBody (virtualized if >1000 rows)
│   │   └── TableRow × N
│   └── TablePagination (MUI pagination)
└── ActionBar (bottom controls)
    ├── ExportFiltered (CSV from S2.4 API)
    ├── DownloadFull (CSV all rows)
    ├── ShareJSON (clipboard)
    └── DataQualityReport (expand/collapse)
```

### Component Files (New - 5 files)

| Component | Purpose | LOC Est. |
|-----------|---------|----------|
| `DataViewerContainer.jsx` | Main orchestrator, state management | 180 |
| `DatasetStats.jsx` | Display metadata & statistics | 80 |
| `FilterBar.jsx` | Search/filter controls | 120 |
| `DataTable.jsx` | Table view with sorting (no virtual scroll yet) | 150 |
| `DataTableVirtual.jsx` | Virtual scrolling for >1k rows | 100 |

**Total**: ~630 LOC (similar to S2.6)

---

## Data Flow

### 1. Entry Point (from S2.6 ResultViewer)
```
ResultViewer → "View Full Dataset" button
  ↓
DataViewerContainer (mounted with dataset ID)
  ↓
Fetch dataset from S2.3 stored in H2 DB
  ↓
Display in DataTable with pagination/filtering
```

### 2. State Management (React.useState)
```javascript
// DataViewerContainer
const [dataset, setDataset] = useState(null);         // Full dataset
const [filters, setFilters] = useState({});           // Column-specific filters
const [sortConfig, setSortConfig] = useState(null);   // { column, direction }
const [pageIndex, setPageIndex] = useState(0);        // 0-based
const [rowsPerPage, setRowsPerPage] = useState(100);  // rows to display
const [loading, setLoading] = useState(false);
const [error, setError] = useState(null);
```

### 3. Filtering Logic
```javascript
// Apply filters to dataset
const filteredData = dataset.filter(row => {
  return Object.entries(filters).every(([col, value]) => {
    if (!value) return true;  // No filter on this column
    return String(row[col]).toLowerCase().includes(value.toLowerCase());
  });
});
```

### 4. Sorting Logic
```javascript
// Sort filtered data
const sortedData = [...filteredData].sort((a, b) => {
  const aVal = a[sortConfig.column] || '';
  const bVal = b[sortConfig.column] || '';
  return sortConfig.direction === 'asc' ? 
    String(aVal).localeCompare(String(bVal)) :
    String(bVal).localeCompare(String(aVal));
});
```

### 5. Pagination (Frontend Side)
```javascript
// Paginate sorted data
const startIdx = pageIndex * rowsPerPage;
const endIdx = startIdx + rowsPerPage;
const visibleRows = sortedData.slice(startIdx, endIdx);
const totalPages = Math.ceil(sortedData.length / rowsPerPage);
```

### 6. Export Options

#### Option A: Export Filtered Results (S2.4 API)
```javascript
const handleExportFiltered = async () => {
  // Build filter string for S2.4 API
  const filterStr = Object.entries(filters)
    .map(([col, val]) => `${col}:${val}`)
    .join('&');
  
  const url = `/api/data-sets/${datasetId}/export/download?format=csv&filter=${filterStr}&sort=${sortConfig.column}`;
  const response = await fetch(url);
  const blob = await response.blob();
  saveAs(blob, `dataset_filtered_${Date.now()}.csv`);
};
```

#### Option B: Download Full Dataset (No Filters)
```javascript
const handleDownloadFull = async () => {
  const url = `/api/data-sets/${datasetId}/export/download?format=csv`;
  const response = await fetch(url);
  const blob = await response.blob();
  saveAs(blob, `dataset_full_${Date.now()}.csv`);
};
```

---

## API Integration Points

### 1. S2.3 API (Already Used by S2.6)
**Purpose**: Get dataset from ConfigurationPanel  
**Source**: `generationResult` passed as prop to DataViewer

### 2. S2.4 API (CSV Export)
**Endpoint**: `GET /api/data-sets/{datasetId}/export/download`  
**Query Params** (new):
- `format`: 'csv' | 'json'
- `filter`: URL-encoded filter string (optional)
  - Format: `column1=value&column2=value`
  - Backend applies as: `WHERE column1 LIKE '%value%' AND column2 LIKE '%value%'`
- `sort`: column name (optional)
- `sortDir`: 'asc' | 'desc' (optional)

**Important**: S2.4 may need to be enhanced with filter params support  
(Check if DataExportService already supports this)

### 3. New API Endpoint (Optional - Deferred to S3)
```
GET /api/data-sets/{id}/stats

Response:
{
  "totalRows": 1000,
  "totalColumns": 5,
  "nullCounts": {
    "firstName": 12,
    "email": 5,
    "amount": 0
  },
  "columnTypes": {
    "firstName": "FIRST_NAME",
    "email": "EMAIL",
    "amount": "AMOUNT"
  }
}
```

**Status**: Deferred (use basic stats for MVP)

---

## Technology Stack

### Frontend Libraries
```javascript
// Already installed:
import { Table, TableHead, TableBody, TableRow, TableCell, Pagination } from '@mui/material';
import { Alert, Box, Button, Card, Checkbox, CircularProgress } from '@mui/material';

// Already installed (for virtual scroll):
import { FixedSizeList } from 'react-window';  // From react-window

// File download:
import { saveAs } from 'file-saver';

// Optional (if needed):
// npm install react-table  // For complex sorting/filtering
// npm install react-query   // For server-side caching
```

### Performance Optimizations
- Memoization: `React.memo()` for FilterBar, DataTable components
- Virtual scrolling: Switch to `DataTableVirtual` if >1000 rows
- Pagination: Limit rendered rows to current page only
- Debounce filter input: 300ms delay before filtering

---

## Design Decisions to Validate

### Decision 1: Pagination Strategy
**Options**:
1. **Client-side pagination** (recommended for MVP)
   - All data in memory
   - Fast sort/filter
   - Limit: 10k rows max
   
2. **Server-side pagination** (future)
   - Lazy load from DB
   - No memory limit
   - Slower sort/filter

**Recommendation**: Option 1 (client-side) for S2.7 MVP

### Decision 2: Virtual Scrolling Activation
**Options**:
1. **Disabled** (MVP)
   - Simpler implementation
   - Good enough for <5k rows
   
2. **Auto-enable** for >1000 rows
   - Better performance
   - More complex

**Recommendation**: Option 1 (disabled MVP, enable in S3)

### Decision 3: Filter Scope
**Options**:
1. **Filter on displayed page only** (simple)
   - Fast but confusing UX
   
2. **Filter entire dataset** (recommended)
   - "Showing 5 of 100 rows after filter"
   - Slow for large datasets (need virtual scroll)

**Recommendation**: Option 2 (filter full dataset)

### Decision 4: Export Strategy
**Options**:
1. **Frontend-only** (CSV conversion in React)
   - No backend changes needed
   - Slow for 10k rows
   
2. **Use S2.4 API** (recommended)
   - Consistent with S2.6
   - Backend optimized
   - Need to enhance S2.4 with filter support

**Recommendation**: Option 2 (use S2.4 API)

### Decision 5: Mobile Responsiveness
**Options**:
1. **Horizontal scroll** (simple)
   - Table stays same
   
2. **Card view** (recommended)
   - Better UX on mobile
   - More complex

**Recommendation**: Option 2 (card view for mobile)

---

## Test Strategy

### Unit Tests (per component)

#### DatasetStats.test.jsx (3 tests)
- Renders stats card correctly
- Displays correct row/column counts
- Shows generated timestamp

#### FilterBar.test.jsx (6 tests)
- Renders filter controls
- Updates filter state on input change
- Clears all filters
- Filters by column type
- Disables controls while loading

#### DataTable.test.jsx (8 tests)
- Renders table with data
- Column header click triggers sort
- Sort indicator displays (↑/↓)
- Pagination controls work
- Rows per page selector works
- Navigate between pages
- Handle empty dataset
- Truncate long values

#### DataTableVirtual.test.jsx (4 tests)
- Renders virtualized list
- Scroll performance >60fps (mock test)
- Load/unload rows as visible
- Maintains sort/filter on scroll

#### DataViewerContainer.test.jsx (8 tests)
- Load dataset on mount
- Handle API error gracefully
- Retry button works
- Export filtered results (S2.4 API call)
- Download full dataset
- Share as JSON to clipboard
- Pagination + filter + sort together
- State persistence across tabs

### Integration Tests (2 tests)
- Full flow: Load → Filter → Sort → Export
- S2.6 ResultViewer → S2.7 DataViewer navigation

### Performance Tests (2 tests)
- Sort 1000 rows: <50ms
- Filter 1000 rows: <50ms

### Accessibility Tests (3 tests)
- Keyboard navigation (Tab through controls)
- Screen reader support (aria labels)
- Color contrast (WCAG AA)

**Total**: 36 tests, target >80% coverage

---

## Implementation Roadmap

### Phase 1: Core Display (Day 1)
- [ ] DataViewerContainer state setup
- [ ] DatasetStats component
- [ ] DataTable basic rendering
- [ ] MUI Pagination
- [ ] 8 unit tests

### Phase 2: Filtering & Sorting (Day 1-2)
- [ ] FilterBar component
- [ ] Sort on column header click
- [ ] Client-side filter logic
- [ ] 12 unit tests

### Phase 3: Export & Navigation (Day 2)
- [ ] Export filtered results (S2.4 API)
- [ ] Download full dataset
- [ ] Share as JSON (clipboard)
- [ ] 8 unit tests

### Phase 4: Polish & Performance (Day 2-3)
- [ ] Responsive design (mobile card view)
- [ ] Error handling & retry
- [ ] Virtual scrolling (if needed)
- [ ] Performance optimization
- [ ] 8 unit tests + 3 integration tests

### Phase 5: Testing & Documentation (Day 3)
- [ ] Jest test suite (36 tests)
- [ ] Accessibility audit
- [ ] Performance benchmarks
- [ ] Security review
- [ ] Documentation

---

## Dependencies & Pre-requisites

### ✅ Already Satisfied
- S2.3 DataGenerationController (test data available)
- S2.4 DataExportService (export endpoint ready)
- S2.6 ConfigurationPanel (navigation ready)
- react-table installed ❌ (use MUI Table instead)
- react-window installed ✅ (for future virtual scroll)
- file-saver installed ✅ (for downloads)

### ⚠️ Potential Blockers
- S2.4 filter parameter support: Need to verify/enhance `DataExportService.java`
- Performance on 10k rows: May need virtual scrolling sooner than S3

### 📋 Configuration Needed
- CORS: Ensure frontend can call S2.4 export endpoint
- API timeouts: Increase if exporting >10k rows

---

## Success Criteria

| Criteria | Target | Status |
|----------|--------|--------|
| Component creation | 5 JSX files, ~630 LOC | ⏳ Ready |
| Test coverage | >80% | ⏳ Target: 36 tests |
| Performance | <200ms page load | ⏳ Target |
| Sorting | <50ms for 1k rows | ⏳ Target |
| Filtering | <50ms for 1k rows | ⏳ Target |
| Mobile responsive | Works on mobile | ⏳ Target |
| Accessibility | WCAG AA compliant | ⏳ Target |
| Documentation | Complete AC + code comments | ⏳ Ready |

---

## Open Questions for Nouredine

1. **Virtual Scrolling**: Enable for MVP or defer to S3?
2. **Filter Scope**: Filter full dataset (slower) or visible page only (confusing)?
3. **Mobile View**: Card view or keep horizontal scroll?
4. **Data Stats Endpoint**: Create `/api/data-sets/{id}/stats`or use frontend aggregation?
5. **Export Enhancement**: Should S2.4 support filter parameters or handle in frontend?
6. **Performance Baseline**: Any specific row count expectations (<1k, 1-10k, >10k)?

---

## Ready to Start?

✅ **Design Specification Complete**  
✅ **5 Components Designed**  
✅ **36 Tests Planned**  
✅ **API Integration Mapped**  
✅ **Edge Cases Considered**

**Next Step**: Confirm design decisions above, then proceed to Phase 1 implementation.

**Estimated Timeline**: 2-3 days (similar to S2.6)

