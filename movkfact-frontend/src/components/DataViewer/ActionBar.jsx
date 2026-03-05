import React from 'react';
import {
  Box,
  Button,
  Stack,
  Tooltip,
  CircularProgress
} from '@mui/material';
import FileDownloadIcon from '@mui/icons-material/FileDownload';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import AssignmentIcon from '@mui/icons-material/Assignment';
import { useSnackbar } from 'notistack';
import { saveAs } from 'file-saver';

/**
 * S2.7 ActionBar Component
 * Action buttons for export, sharing, and quality report
 */
const ActionBar = ({
  datasetId,
  filteredData,
  allData,
  filters = {},
  sortConfig = null,
  onQualityReportToggle,
  isExporting = false
}) => {
  const { enqueueSnackbar } = useSnackbar();
  const [isExportingFiltered, setIsExportingFiltered] = React.useState(false);
  const [isExportingFull, setIsExportingFull] = React.useState(false);

  /**
   * Export filtered dataset as CSV
   * Calls S2.4 API with filter parameters
   */
  const handleExportFiltered = async () => {
    if (!filteredData || filteredData.length === 0) {
      enqueueSnackbar('No data to export', { variant: 'warning' });
      return;
    }

    setIsExportingFiltered(true);
    try {
      // Build query params from filters
      const filterParams = new URLSearchParams();
      filterParams.append('datasetId', datasetId);

      // Add filter string (format: col1:val1,col2:val2)
      const filterStr = Object.entries(filters)
        .map(([col, val]) => `${col}:${val}`)
        .join(',');
      if (filterStr) {
        filterParams.append('filter', filterStr);
      }

      // Add sort config
      if (sortConfig) {
        filterParams.append('sortColumn', sortConfig.column);
        filterParams.append('sortDir', sortConfig.direction);
      }

      // Call S2.4 API endpoint
      const response = await fetch(
        `/api/data-sets/${datasetId}/export?${filterParams.toString()}`,
        { method: 'GET' }
      );

      if (!response.ok) {
        throw new Error(`Export failed: ${response.statusText}`);
      }

      // Get CSV blob and trigger download
      const blob = await response.blob();
      saveAs(blob, `dataset-${datasetId}-filtered.csv`);
      enqueueSnackbar('Filtered data exported successfully', { variant: 'success' });
    } catch (err) {
      console.error('Export error:', err);
      enqueueSnackbar(`Export failed: ${err.message}`, { variant: 'error' });
    } finally {
      setIsExportingFiltered(false);
    }
  };

  /**
   * Export full dataset (no filters)
   * Calls S2.4 API without filter parameters
   */
  const handleExportFull = async () => {
    if (!allData || allData.length === 0) {
      enqueueSnackbar('No data to export', { variant: 'warning' });
      return;
    }

    setIsExportingFull(true);
    try {
      const response = await fetch(
        `/api/data-sets/${datasetId}/export`,
        { method: 'GET' }
      );

      if (!response.ok) {
        throw new Error(`Export failed: ${response.statusText}`);
      }

      const blob = await response.blob();
      saveAs(blob, `dataset-${datasetId}-full.csv`);
      enqueueSnackbar('Full dataset exported successfully', { variant: 'success' });
    } catch (err) {
      console.error('Export error:', err);
      enqueueSnackbar(`Export failed: ${err.message}`, { variant: 'error' });
    } finally {
      setIsExportingFull(false);
    }
  };

  /**
   * Copy filtered dataset as JSON to clipboard
   * For sharing and quick access
   */
  const handleShareJSON = async () => {
    if (!filteredData || filteredData.length === 0) {
      enqueueSnackbar('No data to share', { variant: 'warning' });
      return;
    }

    try {
      const json = JSON.stringify(filteredData, null, 2);
      await navigator.clipboard.writeText(json);
      enqueueSnackbar('Dataset copied to clipboard (JSON format)', { variant: 'success' });
    } catch (err) {
      enqueueSnackbar('Failed to copy to clipboard', { variant: 'error' });
    }
  };

  /**
   * Toggle quality report visibility
   * Shows data quality metrics and anomalies
   */
  const handleQualityReport = () => {
    if (onQualityReportToggle) {
      onQualityReportToggle();
    }
  };

  const hasFilters = Object.keys(filters).length > 0;
  const filteredCount = filteredData?.length || 0;
  const totalCount = allData?.length || 0;

  return (
    <Box sx={{ p: 2, backgroundColor: '#fafafa', borderTop: '1px solid #e0e0e0' }}>
      <Stack direction="row" spacing={2} sx={{ flexWrap: 'wrap', gap: 1 }}>
        {/* Export filtered data (only visible if filters applied) */}
        {hasFilters && (
          <Tooltip title={`Export ${filteredCount} filtered rows as CSV`}>
            <span>
              <Button
                variant="contained"
                color="primary"
                size="small"
                startIcon={isExportingFiltered ? <CircularProgress size={20} /> : <FileDownloadIcon />}
                onClick={handleExportFiltered}
                disabled={isExportingFiltered || filteredCount === 0}
              >
                Export Filtered ({filteredCount})
              </Button>
            </span>
          </Tooltip>
        )}

        {/* Export full dataset */}
        <Tooltip title={`Export all ${totalCount} rows as CSV`}>
          <span>
            <Button
              variant="outlined"
              color="primary"
              size="small"
              startIcon={isExportingFull ? <CircularProgress size={20} /> : <FileDownloadIcon />}
              onClick={handleExportFull}
              disabled={isExportingFull || totalCount === 0}
            >
              Download Full Dataset
            </Button>
          </span>
        </Tooltip>

        {/* Share as JSON */}
        <Tooltip title="Copy filtered dataset to clipboard as JSON">
          <Button
            variant="outlined"
            color="secondary"
            size="small"
            startIcon={<ContentCopyIcon />}
            onClick={handleShareJSON}
            disabled={filteredCount === 0}
          >
            Share as JSON
          </Button>
        </Tooltip>

        {/* Quality report toggle */}
        <Tooltip title="View data quality metrics and anomaly report">
          <Button
            variant="outlined"
            color="info"
            size="small"
            startIcon={<AssignmentIcon />}
            onClick={handleQualityReport}
          >
            Quality Report
          </Button>
        </Tooltip>
      </Stack>
    </Box>
  );
};

export default ActionBar;
