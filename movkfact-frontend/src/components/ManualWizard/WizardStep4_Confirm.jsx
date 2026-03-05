import React, { useState } from 'react';
import {
  Box, Button, Typography, Alert, CircularProgress,
  Table, TableBody, TableCell, TableContainer, TableRow, Paper,
} from '@mui/material';
import api from '../../services/api';

/**
 * Étape 4 du wizard : récapitulatif + lancement de la génération (S7.2 AC5).
 */
const WizardStep4_Confirm = ({ datasetName, domainId, rowCount, columns, onBack, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleGenerate = async () => {
    setLoading(true);
    setError(null);
    try {
      await api.post(`/api/domains/${domainId}/data-sets`, {
        datasetName: datasetName,
        domainId: domainId,
        numberOfRows: rowCount,
        columns: columns.map((col) => ({
          name: col.name,
          columnType: col.type,
          constraints: col.constraints && Object.keys(col.constraints).length > 0
            ? col.constraints
            : undefined,
        })),
      });
      onSuccess();
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Erreur lors de la génération');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ pt: 1 }}>
      <Typography variant="h6" sx={{ mb: 2 }}>Récapitulatif</Typography>

      <TableContainer component={Paper} sx={{ mb: 3 }}>
        <Table size="small">
          <TableBody>
            <TableRow>
              <TableCell><strong>Nom du dataset</strong></TableCell>
              <TableCell data-testid="summary-name">{datasetName}</TableCell>
            </TableRow>
            <TableRow>
              <TableCell><strong>Nombre de lignes</strong></TableCell>
              <TableCell data-testid="summary-rowcount">{rowCount.toLocaleString()}</TableCell>
            </TableRow>
            <TableRow>
              <TableCell><strong>Colonnes ({columns.length})</strong></TableCell>
              <TableCell data-testid="summary-columns">
                {columns.map((c) => `${c.name} (${c.type})`).join(', ')}
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </TableContainer>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} data-testid="confirm-error">
          {error}
        </Alert>
      )}

      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
        <Button onClick={onBack} disabled={loading} data-testid="step4-back-button">
          ← Retour
        </Button>
        <Button
          variant="contained"
          color="success"
          onClick={handleGenerate}
          disabled={loading}
          startIcon={loading ? <CircularProgress size={18} /> : null}
          data-testid="step4-generate-button"
        >
          {loading ? 'Génération...' : 'Lancer la génération'}
        </Button>
      </Box>
    </Box>
  );
};

export default WizardStep4_Confirm;
