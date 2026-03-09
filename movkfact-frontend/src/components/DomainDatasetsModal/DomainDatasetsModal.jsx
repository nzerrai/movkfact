import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  CircularProgress,
  Alert,
  Typography,
  IconButton,
  Tooltip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import RefreshIcon from '@mui/icons-material/Refresh';
import StatusBadge from '../StatusBadge';
import { getDatasetsByDomain } from '../../services/domainService';
import { formatDateTime } from '../../utils/formatters';

/**
 * Modal enrichie — affiche les datasets d'un domaine avec statuts (FR-003).
 * - Remplace le fetch hardcodé par domainService.getDatasetsByDomain()
 * - StatusBadge par dataset
 * - Tri par updatedAt DESC par défaut
 * - Filtre par statut : Tous / Modifiés / Téléchargés
 */
const DomainDatasetsModal = ({
  open,
  domainId,
  domainName,
  onClose,
}) => {
  const navigate = useNavigate();
  const [datasets, setDatasets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filterStatus, setFilterStatus] = useState('all');

  const loadDatasets = useCallback(async () => {
    if (!domainId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await getDatasetsByDomain(domainId);
      // Tri updatedAt DESC (déjà fait côté backend, on s'assure)
      const sorted = [...data].sort((a, b) => {
        if (!a.updatedAt) return 1;
        if (!b.updatedAt) return -1;
        return new Date(b.updatedAt) - new Date(a.updatedAt);
      });
      setDatasets(sorted);
    } catch (err) {
      console.error('Failed to load datasets:', err);
      setError('Impossible de charger les datasets.');
      setDatasets([]);
    } finally {
      setLoading(false);
    }
  }, [domainId]);

  useEffect(() => {
    if (open && domainId) {
      loadDatasets();
    }
  }, [open, domainId, loadDatasets]);

  const handleRefresh = () => {
    loadDatasets();
  };

  const handleViewDataset = (dataset) => {
    if (dataset && dataset.id) {
      navigate(`/data-viewer/${dataset.id}`);
      onClose();
    }
  };

  const filteredDatasets = datasets.filter((ds) => {
    if (filterStatus === 'modified') return ds.status?.modified;
    if (filterStatus === 'downloaded') return ds.status?.downloaded;
    return true;
  });

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{ sx: { borderRadius: 2, boxShadow: 3 } }}
    >
      {/* Header */}
      <DialogTitle
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          borderBottom: '1px solid #e0e0e0',
          backgroundColor: '#f5f5f5',
          pb: 2,
        }}
      >
        <Box>
          <Typography variant="h6" sx={{ fontWeight: 600, mb: 0.5 }}>
            Datasets du domaine
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            Domain: <strong>{domainName}</strong>
            {loading ? (
              <CircularProgress size={16} sx={{ ml: 1 }} />
            ) : (
              ` (${datasets.length} dataset${datasets.length !== 1 ? 's' : ''})`
            )}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Tooltip title="Refresh">
            <IconButton
              onClick={handleRefresh}
              size="small"
              sx={{ '&:hover': { backgroundColor: '#e0e0e0' } }}
            >
              <RefreshIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <IconButton
            onClick={onClose}
            size="small"
            sx={{ '&:hover': { backgroundColor: '#e0e0e0' } }}
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        </Box>
      </DialogTitle>

      {/* Content */}
      <DialogContent sx={{ p: 3 }}>
        {/* Filtre par statut */}
        <Box sx={{ mb: 2, display: 'flex', gap: 2, alignItems: 'center' }}>
          <Alert severity="info" sx={{ flex: 1 }}>
            Tous les fichiers CSV uploadés pour le domain <strong>{domainName}</strong>
          </Alert>
          <FormControl size="small" sx={{ minWidth: 160 }}>
            <InputLabel>Filtre statut</InputLabel>
            <Select
              value={filterStatus}
              label="Filtre statut"
              onChange={(e) => setFilterStatus(e.target.value)}
            >
              <MenuItem value="all">Tous</MenuItem>
              <MenuItem value="modified">Modifiés</MenuItem>
              <MenuItem value="downloaded">Téléchargés</MenuItem>
            </Select>
          </FormControl>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        ) : filteredDatasets.length === 0 ? (
          <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
            Aucun dataset{filterStatus !== 'all' ? ' pour ce filtre' : ''}.
          </Typography>
        ) : (
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                  <TableCell><strong>Nom</strong></TableCell>
                  <TableCell><strong>Lignes</strong></TableCell>
                  <TableCell><strong>Colonnes</strong></TableCell>
                  <TableCell><strong>Statut</strong></TableCell>
                  <TableCell><strong>Dernière modif.</strong></TableCell>
                  <TableCell align="center"><strong>Action</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredDatasets.map((ds) => (
                  <TableRow key={ds.id} hover>
                    <TableCell>{ds.datasetName || ds.name || '—'}</TableCell>
                    <TableCell>{ds.rowCount ?? '—'}</TableCell>
                    <TableCell>{ds.columnCount ?? '—'}</TableCell>
                    <TableCell>
                      <StatusBadge
                        downloaded={ds.status?.downloaded ?? false}
                        modified={ds.status?.modified ?? false}
                        viewed={ds.status?.viewed ?? false}
                      />
                    </TableCell>
                    <TableCell>{formatDateTime(ds.updatedAt)}</TableCell>
                    <TableCell align="center">
                      <Button
                        size="small"
                        variant="outlined"
                        onClick={() => handleViewDataset(ds)}
                      >
                        Voir
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </DialogContent>

      {/* Footer */}
      <DialogActions sx={{ p: 2, borderTop: '1px solid #e0e0e0' }}>
        <Button onClick={onClose} sx={{ px: 3 }}>
          Fermer
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DomainDatasetsModal;
