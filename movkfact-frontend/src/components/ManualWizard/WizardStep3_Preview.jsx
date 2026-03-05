import React, { useEffect, useState } from 'react';
import {
  Box, Button, CircularProgress, Alert,
  Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Typography,
} from '@mui/material';
import { previewDataset } from '../../services/domainService';

/**
 * Étape 3 du wizard : prévisualisation des 5 lignes générées (S7.2 AC4).
 */
const WizardStep3_Preview = ({ columns, previewRows, onPreviewLoaded, onBack, onConfirm }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (previewRows && previewRows.length > 0) return;

    const fetchPreview = async () => {
      setLoading(true);
      setError(null);
      try {
        const apiColumns = columns.map((col) => ({
          name: col.name,
          columnType: col.type,
          constraints: col.constraints && Object.keys(col.constraints).length > 0
            ? col.constraints
            : undefined,
        }));
        const result = await previewDataset(apiColumns, 5);
        onPreviewLoaded(result.previewRows || result);
      } catch (err) {
        setError(err.response?.data?.message || err.response?.data?.error || err.message || 'Erreur lors de la prévisualisation');
      } finally {
        setLoading(false);
      }
    };
    fetchPreview();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const rows = previewRows || [];
  const colNames = rows.length > 0 ? Object.keys(rows[0]) : columns.map((c) => c.name);

  return (
    <Box sx={{ pt: 1 }}>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Aperçu de 5 lignes générées selon votre configuration :
      </Typography>

      {loading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress data-testid="preview-spinner" />
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} data-testid="preview-error">
          {error}
        </Alert>
      )}

      {!loading && !error && rows.length > 0 && (
        <TableContainer component={Paper} sx={{ mb: 3 }}>
          <Table size="small">
            <TableHead>
              <TableRow sx={{ bgcolor: 'grey.100' }}>
                {colNames.map((col) => (
                  <TableCell key={col}><strong>{col}</strong></TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((row, i) => (
                <TableRow key={i}>
                  {colNames.map((col) => (
                    <TableCell key={col}>{String(row[col] ?? '')}</TableCell>
                  ))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
        <Button onClick={onBack} data-testid="step3-back-button">← Modifier colonnes</Button>
        <Button
          variant="contained"
          onClick={onConfirm}
          disabled={loading || !!error || rows.length === 0}
          data-testid="step3-confirm-button"
        >
          Confirmer →
        </Button>
      </Box>
    </Box>
  );
};

export default WizardStep3_Preview;
