import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Collapse,
  Container,
  Stack,
  Tab,
  Tabs,
  Typography
} from '@mui/material';
import HistoryIcon from '@mui/icons-material/History';
import DatasetStats from './DatasetStats';
import FilterBar from './FilterBar';
import DataTable from './DataTable';
import ActionBar from './ActionBar';
import QualityReportPanel from './QualityReportPanel';
import ActivityFeed from './ActivityFeed';

/**
 * S2.7 DataViewerContainer Component
 * Main orchestrator for dataset viewing, filtering, sorting, pagination
 * 
 * Props:
 * - dataset: { id, data: [], rowCount, columnCount, generationTimeMs }
 * - domainId: number
 * - onBack: () => void
 */
const DataViewerContainer = ({ dataset, domainId, onBack }) => {
  const [stats, setStats] = useState(null);
  const [filters, setFilters] = useState({});
  const [sortConfig, setSortConfig] = useState(null); // { columns: [{ column, direction }, ...] }
  const [pageIndex, setPageIndex] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(100);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [tab, setTab] = useState(0);
  const [showActivity, setShowActivity] = useState(false);

  // Load dataset stats from new API endpoint
  useEffect(() => {
    if (!dataset?.id) return;

    const fetchStats = async () => {
      try {
        setLoading(true);
        const response = await fetch(
          `http://localhost:8080/api/data-sets/${dataset.id}/stats`
        );
        if (!response.ok) throw new Error(`Stats fetch failed: ${response.status}`);
        const data = await response.json();
        setStats(data);
      } catch (err) {
        console.error('Stats error:', err);
        // Fallback to basic stats from dataset
        setStats({
          totalRows: dataset.rowCount,
          totalColumns: dataset.columnCount,
          nullCounts: {},
          columnTypes: {}
        });
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, [dataset?.id]);

  // Apply filters with operator support (Phase 2 enhancement)
  const applyFilter = (value, operator, filterValue) => {
    if (!filterValue) return true;
    
    const val = String(value || '').toLowerCase();
    const compareVal = String(filterValue || '').toLowerCase();

    switch (operator) {
      case 'equals':
        return val === compareVal;
      case 'contains':
        return val.includes(compareVal);
      case 'startsWith':
        return val.startsWith(compareVal);
      case 'endsWith':
        return val.endsWith(compareVal);
      case '>':
        return !isNaN(value) && !isNaN(filterValue) && Number(value) > Number(filterValue);
      case '<':
        return !isNaN(value) && !isNaN(filterValue) && Number(value) < Number(filterValue);
      case '>=':
        return !isNaN(value) && !isNaN(filterValue) && Number(value) >= Number(filterValue);
      case '<=':
        return !isNaN(value) && !isNaN(filterValue) && Number(value) <= Number(filterValue);
      default:
        return val.includes(compareVal);
    }
  };

  // Filter dataset with advanced operators (Phase 2)
  const dataArray = Array.isArray(dataset?.data) ? dataset.data : [];
  const filteredData = dataArray.filter(row => {
    return Object.entries(filters).every(([filterKey, filterData]) => {
      const rowValue = row[filterData.column];
      return applyFilter(rowValue, filterData.operator, filterData.value);
    });
  }) || [];

  // Apply sorting with multi-column support (Phase 2 enhancement)
  const sortedData = sortConfig && sortConfig.columns && sortConfig.columns.length > 0
    ? [...filteredData].sort((a, b) => {
        for (const sort of sortConfig.columns) {
          const aVal = a[sort.column] || '';
          const bVal = b[sort.column] || '';
          const comparison = String(aVal).localeCompare(String(bVal));
          if (comparison !== 0) {
            return sort.direction === 'asc' ? comparison : -comparison;
          }
        }
        return 0; // All sorts are equal
      })
    : filteredData;

  // Paginate sorted data
  const startIdx = pageIndex * rowsPerPage;
  const endIdx = startIdx + rowsPerPage;
  const visibleRows = sortedData.slice(startIdx, endIdx);
  const totalPages = Math.ceil(sortedData.length / rowsPerPage);

  // Reset to first page when filters change
  useEffect(() => {
    setPageIndex(0);
  }, [filters, sortConfig]);

  const handleClearFilters = () => {
    setFilters({});
    setSortConfig(null);
  };

  // Handle sorting with multi-column support (shift+click for secondary sort)
  const handleSort = (column, isShiftClick = false) => {
    let newSortConfig = { columns: [] };

    if (isShiftClick && sortConfig && sortConfig.columns && sortConfig.columns.length > 0) {
      // Add as secondary sort
      newSortConfig.columns = [...sortConfig.columns];
      const existingIndex = newSortConfig.columns.findIndex(s => s.column === column);
      
      if (existingIndex >= 0) {
        // Toggle direction if already in sort list
        const currentDir = newSortConfig.columns[existingIndex].direction;
        if (currentDir === 'asc') {
          newSortConfig.columns[existingIndex].direction = 'desc';
        } else {
          // Remove from sort if toggling to next state
          newSortConfig.columns.splice(existingIndex, 1);
        }
      } else {
        // Add new column to sort
        newSortConfig.columns.push({ column, direction: 'asc' });
      }
    } else {
      // Single column sort (replace existing)
      if (sortConfig?.columns?.[0]?.column === column) {
        // Toggle direction for same column
        const currentDir = sortConfig.columns[0].direction;
        newSortConfig.columns = [{ column, direction: currentDir === 'asc' ? 'desc' : 'asc' }];
      } else {
        newSortConfig.columns = [{ column, direction: 'asc' }];
      }
    }

    setSortConfig(newSortConfig);
  };

  const handleRetry = () => {
    setError(null);
    // Re-fetch stats
    window.location.reload();
  };

  if (!dataset) {
    return (
      <Container maxWidth="lg" sx={{ py: 3 }}>
        <Alert severity="error">No dataset provided</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 3 }}>
      <Stack spacing={3}>
        {/* Header */}
        <Box>
          <Typography variant="h5" sx={{ mb: 1 }}>
            📊 Dataset Viewer
          </Typography>
          <Typography variant="body2" color="textSecondary">
            Domain ID: {domainId} | Dataset ID: {dataset.id}
          </Typography>
        </Box>

        {/* Stats Card */}
        {loading && <CircularProgress size={32} />}
        {stats && !loading && <DatasetStats stats={stats} />}
        {error && (
          <Alert severity="error" onClose={handleRetry}>
            {error} <strong>Click to retry</strong>
          </Alert>
        )}

        {/* Activity Feed */}
        <Box>
          <Button
            size="small"
            variant="outlined"
            startIcon={<HistoryIcon />}
            onClick={() => setShowActivity(prev => !prev)}
          >
            {showActivity ? 'Masquer l\'historique' : 'Historique des activités'}
          </Button>
          <Collapse in={showActivity} timeout="auto" unmountOnExit>
            <Box sx={{ mt: 1 }}>
              <ActivityFeed datasetId={dataset.id} />
            </Box>
          </Collapse>
        </Box>

        {/* Tabs */}
        <Card>
          <Tabs value={tab} onChange={(e, v) => setTab(v)}>
            <Tab label={`Data (${sortedData.length} rows)`} />
            <Tab label="Quality Report" />
          </Tabs>

          {tab === 0 && (
            <CardContent>
              {/* Filter Bar */}
              <FilterBar
                columns={dataset.data?.[0] ? Object.keys(dataset.data[0]) : []}
                filters={filters}
                onFilterChange={setFilters}
                onClearFilters={handleClearFilters}
              />

              {/* Data Table */}
              <Box sx={{ mt: 3, mb: 3 }}>
                <DataTable
                  data={visibleRows}
                  columns={dataset.data?.[0] ? Object.keys(dataset.data[0]) : []}
                  sortConfig={sortConfig}
                  onSort={handleSort}
                  pageIndex={pageIndex}
                  rowsPerPage={rowsPerPage}
                  totalRows={sortedData.length}
                  onPageChange={setPageIndex}
                  onRowsPerPageChange={(newRows) => {
                    setRowsPerPage(newRows);
                    setPageIndex(0);
                  }}
                  totalPages={totalPages}
                />
              </Box>
            </CardContent>
          )}

          {tab === 1 && (
            <CardContent>
              <QualityReportPanel
                stats={stats}
                data={dataset.data || []}
                columns={dataset.data?.[0] ? Object.keys(dataset.data[0]) : []}
              />
            </CardContent>
          )}
        </Card>

        {/* Action Bar */}
        <ActionBar
          datasetId={dataset.id}
          filteredData={sortedData}
          allData={dataset.data || []}
          filters={filters}
          sortConfig={sortConfig}
          onQualityReportToggle={() => setTab(1)}
        />

        {/* Back Button */}
        {onBack && (
          <Box sx={{ mt: 2, textAlign: 'center' }}>
            <button
              onClick={onBack}
              style={{
                padding: '10px 20px',
                backgroundColor: '#1976d2',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '1rem'
              }}
            >
              ← Back to Configuration
            </button>
          </Box>
        )}
      </Stack>
    </Container>
  );
};

export default DataViewerContainer;
