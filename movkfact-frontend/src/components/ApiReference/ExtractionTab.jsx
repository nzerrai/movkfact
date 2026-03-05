import React, { useState, useMemo } from 'react';
import {
  Box, Typography, FormControl, InputLabel, Select, MenuItem,
  RadioGroup, FormControlLabel, Radio, TextField, Button,
  CircularProgress, Alert, Table, TableHead, TableRow,
  TableCell, TableBody, TableContainer, Paper, FormLabel,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import PreviewIcon from '@mui/icons-material/Preview';
import axios from 'axios';

export default function ExtractionTab({ datasets }) {
  const [datasetId, setDatasetId] = useState('');
  const [mode, setMode] = useState('full');
  const [format, setFormat] = useState('JSON');
  const [rowIds, setRowIds] = useState('');
  const [count, setCount] = useState(50);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [previewRows, setPreviewRows] = useState(null);

  const builtUrl = useMemo(() => {
    if (!datasetId) return 'GET /api/data-sets/{id}/export?…';
    let url = `/api/data-sets/${datasetId}/export?format=${format}&mode=${mode}`;
    if (mode === 'filtered' && rowIds) url += `&rowIds=${rowIds}`;
    if (mode === 'sample') url += `&count=${count}`;
    return `GET ${url}`;
  }, [datasetId, format, mode, rowIds, count]);

  const buildPath = () => {
    let url = `/api/data-sets/${datasetId}/export?format=${format}&mode=${mode}`;
    if (mode === 'filtered' && rowIds) url += `&rowIds=${rowIds}`;
    if (mode === 'sample') url += `&count=${count}`;
    return url;
  };

  const handleDownload = async () => {
    if (!datasetId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await axios.get(buildPath(), { responseType: 'blob' });
      const ext = format === 'CSV' ? 'csv' : 'json';
      const blob = new Blob([res.data]);
      const href = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = href;
      a.download = `dataset-${datasetId}.${ext}`;
      a.click();
      URL.revokeObjectURL(href);
    } catch (e) {
      setError(e.response?.data?.message || e.message);
    } finally {
      setLoading(false);
    }
  };

  const handlePreview = async () => {
    if (!datasetId) return;
    setLoading(true);
    setError(null);
    setPreviewRows(null);
    try {
      const res = await axios.get(`/api/data-sets/${datasetId}/rows?page=0&size=50`);
      const data = res.data;
      const rows = Array.isArray(data) ? data : (data.rows ?? data.content ?? []);
      setPreviewRows(rows.slice(0, 50));
    } catch (e) {
      setError(e.response?.data?.message || e.message);
    } finally {
      setLoading(false);
    }
  };

  const previewColumns = previewRows && previewRows.length > 0
    ? Object.keys(previewRows[0].data ?? previewRows[0])
    : [];

  return (
    <Box>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Configurez et téléchargez un export, ou prévisualisez les 50 premières lignes.
      </Typography>

      {/* Dataset selector */}
      <FormControl fullWidth size="small" sx={{ mb: 2 }}>
        <InputLabel id="extraction-dataset-label">Dataset</InputLabel>
        <Select
          labelId="extraction-dataset-label"
          inputProps={{ id: 'extraction-dataset-select', 'aria-label': 'Dataset' }}
          value={datasetId}
          onChange={e => setDatasetId(e.target.value)}
          label="Dataset"
        >
          {datasets.length === 0 && (
            <MenuItem disabled value="">Aucun dataset disponible</MenuItem>
          )}
          {datasets.map(ds => (
            <MenuItem key={ds.id} value={ds.id}>{ds.name} (id: {ds.id})</MenuItem>
          ))}
        </Select>
      </FormControl>

      {/* Mode */}
      <FormControl sx={{ mb: 2 }}>
        <FormLabel>Mode</FormLabel>
        <RadioGroup row value={mode} onChange={e => setMode(e.target.value)}>
          <FormControlLabel value="full" control={<Radio size="small" />} label="full" />
          <FormControlLabel value="filtered" control={<Radio size="small" />} label="filtered" />
          <FormControlLabel value="sample" control={<Radio size="small" />} label="sample" />
        </RadioGroup>
      </FormControl>

      {mode === 'filtered' && (
        <TextField
          label="rowIds (ex: 0,5,10)"
          value={rowIds}
          onChange={e => setRowIds(e.target.value)}
          size="small"
          sx={{ mb: 2, mr: 2 }}
        />
      )}
      {mode === 'sample' && (
        <TextField
          label="count"
          type="number"
          value={count}
          onChange={e => setCount(Number(e.target.value))}
          size="small"
          inputProps={{ min: 1, max: 10000 }}
          sx={{ mb: 2, mr: 2 }}
        />
      )}

      {/* Format */}
      <FormControl size="small" sx={{ mb: 2, ml: mode !== 'full' ? 0 : 0, display: 'block' }}>
        <InputLabel>Format</InputLabel>
        <Select value={format} onChange={e => setFormat(e.target.value)} label="Format" sx={{ minWidth: 120 }}>
          <MenuItem value="JSON">JSON</MenuItem>
          <MenuItem value="CSV">CSV</MenuItem>
        </Select>
      </FormControl>

      {/* Live URL */}
      <Box
        component="code"
        sx={{
          display: 'block',
          mb: 2,
          p: 1,
          bgcolor: '#1e1e1e',
          color: '#9cdcfe',
          borderRadius: 1,
          fontSize: '0.8rem',
          fontFamily: 'monospace',
          wordBreak: 'break-all',
        }}
      >
        {builtUrl}
      </Box>

      {/* Actions */}
      <Box sx={{ display: 'flex', gap: 1.5, mb: 2 }}>
        <Button
          variant="contained"
          startIcon={loading ? <CircularProgress size={14} color="inherit" /> : <DownloadIcon />}
          onClick={handleDownload}
          disabled={!datasetId || loading}
        >
          Télécharger
        </Button>
        <Button
          variant="outlined"
          startIcon={loading ? <CircularProgress size={14} /> : <PreviewIcon />}
          onClick={handlePreview}
          disabled={!datasetId || loading}
        >
          Prévisualiser (50 lignes)
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* Preview table */}
      {previewRows && (
        <TableContainer component={Paper} variant="outlined" sx={{ maxHeight: 400 }}>
          <Table size="small" stickyHeader>
            <TableHead>
              <TableRow>
                {previewColumns.map(col => (
                  <TableCell key={col}>{col}</TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {previewRows.map((row, idx) => {
                const data = row.data ?? row;
                return (
                  <TableRow key={idx}>
                    {previewColumns.map(col => (
                      <TableCell key={col}>{String(data[col] ?? '')}</TableCell>
                    ))}
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}
