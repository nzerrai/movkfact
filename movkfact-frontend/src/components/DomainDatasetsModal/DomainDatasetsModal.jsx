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
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import RefreshIcon from '@mui/icons-material/Refresh';
import UploadedDatasetsList from '../CsvUploadPanel/UploadedDatasetsList';

/**
 * Modal pour voir les datasets uploadés d'un Domain
 * - Affiche la liste des datasets
 * - Permet de view/delete les datasets
 * - Accès rapide sans passer par l'upload workflow
 */
const DomainDatasetsModal = ({
  open,
  domainId,
  domainName,
  onClose,
}) => {
  const navigate = useNavigate();
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [isLoadingCount, setIsLoadingCount] = useState(false);
  const [datasetCount, setDatasetCount] = useState(null);

  // Memoized function to load dataset count
  const loadDatasetCount = useCallback(async () => {
    setIsLoadingCount(true);
    try {
      const result = await fetch(
        `http://localhost:8080/api/domains/${domainId}/data-sets`,
        { method: 'GET' }
      );
      const data = await result.json();
      setDatasetCount(Array.isArray(data.data) ? data.data.length : 0);
    } catch (err) {
      console.error('Failed to load dataset count:', err);
      setDatasetCount(0);
    } finally {
      setIsLoadingCount(false);
    }
  }, [domainId]);

  // Charger le nombre de datasets au démarrage
  useEffect(() => {
    if (open && domainId) {
      loadDatasetCount();
    }
  }, [open, domainId, loadDatasetCount]);

  const handleRefresh = () => {
    setRefreshTrigger(prev => prev + 1);
    loadDatasetCount();
  };

  const handleViewDataset = (dataset) => {
    if (dataset && dataset.id) {
      navigate(`/data-viewer/${dataset.id}`);
      onClose();
    }
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 2,
          boxShadow: 3,
        },
      }}
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
            📊 Uploaded Datasets
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            Domain: <strong>{domainName}</strong>
            {isLoadingCount ? (
              <CircularProgress size={16} sx={{ ml: 1 }} />
            ) : (
              ` (${datasetCount ?? 0} dataset${datasetCount !== 1 ? 's' : ''})`
            )}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Tooltip title="Refresh">
            <IconButton
              onClick={handleRefresh}
              size="small"
              sx={{
                '&:hover': { backgroundColor: '#e0e0e0' },
              }}
            >
              <RefreshIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <IconButton
            onClick={onClose}
            size="small"
            sx={{
              '&:hover': { backgroundColor: '#e0e0e0' },
            }}
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        </Box>
      </DialogTitle>

      {/* Content */}
      <DialogContent sx={{ p: 3 }}>
        <Box sx={{ mb: 2 }}>
          <Alert severity="info" sx={{ mb: 2 }}>
            📁 Tous les fichiers CSV uploadés pour le domain <strong>{domainName}</strong>
          </Alert>
        </Box>

        {domainId && (
          <UploadedDatasetsList
            key={refreshTrigger}
            domainId={domainId}
            onViewDataset={handleViewDataset}
            onDeleteDataset={(datasetId) => {
              // Refresh the list after deletion
              handleRefresh();
            }}
            showActions={true}
          />
        )}
      </DialogContent>

      {/* Footer */}
      <DialogActions sx={{ p: 2, borderTop: '1px solid #e0e0e0' }}>
        <Button onClick={onClose} sx={{ px: 3 }}>
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DomainDatasetsModal;
