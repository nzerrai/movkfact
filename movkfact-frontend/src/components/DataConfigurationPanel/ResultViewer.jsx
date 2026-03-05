import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Divider,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography
} from '@mui/material';
import { saveAs } from 'file-saver';
import React, { useState } from 'react';

/**
 * S2.6: ResultViewer Component
 * Displays generated data with export options (JSON, CSV)
 * 
 * Features:
 * - JSON preview with syntax highlighting
 * - Statistics (row count, generation time)
 * - Download JSON button (immediate)
 * - Download CSV button (via S2.4 API)
 * - Copy to clipboard
 */
const ResultViewer = ({ data, domainId, onConfigureMore }) => {
  const [loadingCsv, setLoadingCsv] = useState(false);
  const [error, setError] = useState(null);
  const [copiedToClipboard, setCopiedToClipboard] = useState(false);

  const handleDownloadJson = () => {
    try {
      const jsonString = JSON.stringify(data.data || data, null, 2);
      const blob = new Blob([jsonString], { type: 'application/json' });
      const filename = `dataset_${new Date().toISOString().split('T')[0]}.json`;
      saveAs(blob, filename);
    } catch (err) {
      setError('Failed to download JSON');
    }
  };

  const handleDownloadCsv = async () => {
    setError(null);
    setLoadingCsv(true);

    try {
      // Call S2.4 API to export as CSV
      const apiUrl = `http://localhost:8080/api/data-sets/${data.id}/export/download?format=csv`;

      const response = await fetch(apiUrl, {
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`Export failed: ${response.status}`);
      }

      // Get filename from Content-Disposition header
      const contentDisposition = response.headers.get('Content-Disposition');
      let filename = 'dataset.csv';
      if (contentDisposition) {
        const match = contentDisposition.match(/filename="?(.+?)"?$/);
        if (match) filename = match[1].replace('.json', '.csv');
      }

      const blob = await response.blob();
      saveAs(blob, filename);
    } catch (err) {
      setError(`CSV export failed: ${err.message}`);
    } finally {
      setLoadingCsv(false);
    }
  };

  const handleCopyToClipboard = () => {
    try {
      const jsonString = JSON.stringify(data.data || data, null, 2);
      navigator.clipboard.writeText(jsonString);
      setCopiedToClipboard(true);
      setTimeout(() => setCopiedToClipboard(false), 2000);
    } catch (err) {
      setError('Failed to copy to clipboard');
    }
  };

  // Parse generation time from data
  const generationTimeMs = data.generationTimeMs || null;
  const rowCount = data.rowCount || (Array.isArray(data.data) ? data.data.length : 0);

  // Get first 10 rows for preview
  const previewData = Array.isArray(data.data) ? data.data.slice(0, 10) : [];
  const columns = previewData.length > 0 ? Object.keys(previewData[0]) : [];

  return (
    <Box>
      <Typography variant="h6" sx={{ mb: 2, mt: 2 }}>
        Generation Results ✅
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {copiedToClipboard && (
        <Alert severity="success" sx={{ mb: 2 }}>
          Copied to clipboard!
        </Alert>
      )}

      {/* Statistics Card */}
      <Paper sx={{ p: 2, mb: 3, bgcolor: '#f5f5f5' }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3}>
          <Box>
            <Typography variant="body2" color="textSecondary">
              Total Rows Generated
            </Typography>
            <Typography variant="h5" color="primary">
              {rowCount.toLocaleString()}
            </Typography>
          </Box>
          {generationTimeMs && (
            <Box>
              <Typography variant="body2" color="textSecondary">
                Generation Time
              </Typography>
              <Typography variant="h5" color="primary">
                {generationTimeMs}ms
              </Typography>
            </Box>
          )}
          <Box>
            <Typography variant="body2" color="textSecondary">
              Columns
            </Typography>
            <Typography variant="h5" color="primary">
              {columns.length}
            </Typography>
          </Box>
        </Stack>
      </Paper>

      {/* Data Preview */}
      <Typography variant="subtitle2" sx={{ mb: 1, mt: 2 }}>
        Preview (first 10 rows)
      </Typography>

      {previewData.length > 0 ? (
        <Box sx={{ overflowX: 'auto', mb: 3 }}>
          <Table size="small" stickyHeader>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                {columns.map((col) => (
                  <TableCell key={col} sx={{ fontWeight: 600 }}>
                    {col}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {previewData.map((row, index) => (
                <TableRow key={index} sx={{ '&:hover': { backgroundColor: '#fafafa' } }}>
                  {columns.map((col) => (
                    <TableCell key={`${index}-${col}`}>
                      {String(row[col] || '').slice(0, 50)}
                    </TableCell>
                  ))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Box>
      ) : (
        <Alert severity="warning">No data to display</Alert>
      )}

      <Divider sx={{ my: 3 }} />

      {/* Export Options */}
      <Typography variant="subtitle2" sx={{ mb: 2 }}>
        Export & Share
      </Typography>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 3 }}>
        <Button
          variant="contained"
          color="primary"
          onClick={handleDownloadJson}
        >
          📥 Download JSON
        </Button>

        <Button
          variant="contained"
          color="secondary"
          onClick={handleDownloadCsv}
          disabled={loadingCsv}
          startIcon={loadingCsv ? <CircularProgress size={20} /> : null}
        >
          {loadingCsv ? 'Exporting...' : '📥 Download CSV'}
        </Button>

        <Button
          variant="outlined"
          color="primary"
          onClick={handleCopyToClipboard}
        >
          📋 Copy to Clipboard
        </Button>
      </Stack>

      <Divider sx={{ my: 3 }} />

      {/* Navigation */}
      <Stack direction="row" spacing={2}>
        <Button
          variant="outlined"
          color="primary"
          onClick={onConfigureMore}
        >
          ← Configure More Data
        </Button>
        <Button
          variant="contained"
          color="success"
          onClick={() => window.location.href = '/'}
        >
          Finish & Back to Home
        </Button>
      </Stack>
    </Box>
  );
};

export default ResultViewer;
