import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
  Chip
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import DeleteIcon from '@mui/icons-material/Delete';
import ViewIcon from '@mui/icons-material/Visibility';

/**
 * S2.5: UploadedDatasetsList Component
 * Display list of uploaded datasets for a domain
 */
const UploadedDatasetsList = ({ domainId, onViewDataset, onDeleteDataset, showActions = true }) => {
  const [datasets, setDatasets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (domainId) {
      loadDatasets();
    }
  }, [domainId]);

  const loadDatasets = async () => {
    setLoading(true);
    setError(null);

    try {
      const apiUrl = process.env.NODE_ENV === 'production'
        ? `/api/domains/${domainId}/data-sets`
        : `http://localhost:8080/api/domains/${domainId}/data-sets`;

      const response = await fetch(apiUrl, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`Failed to load datasets: ${response.status}`);
      }

      const result = await response.json();
      const datasetsList = result.data || result.datasets || [];
      setDatasets(datasetsList);
    } catch (err) {
      console.error('Error loading datasets:', err);
      setError(err.message || 'Failed to load uploaded datasets');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (datasetId) => {
    if (!window.confirm('Are you sure you want to delete this dataset?')) {
      return;
    }

    try {
      const apiUrl = process.env.NODE_ENV === 'production'
        ? `/api/data-sets/${datasetId}`
        : `http://localhost:8080/api/data-sets/${datasetId}`;

      const response = await fetch(apiUrl, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`Failed to delete dataset: ${response.status}`);
      }

      // Reload list after deletion
      loadDatasets();
      
      if (onDeleteDataset) {
        onDeleteDataset(datasetId);
      }
    } catch (err) {
      console.error('Error deleting dataset:', err);
      setError(err.message || 'Failed to delete dataset');
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Unknown';
    return new Date(dateString).toLocaleString();
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  };

  // Loading State
  if (loading) {
    return (
      <Box sx={{ textAlign: 'center', padding: 3 }}>
        <CircularProgress size={40} />
        <Typography sx={{ mt: 2 }}>Loading datasets...</Typography>
      </Box>
    );
  }

  // Error State
  if (error) {
    return (
      <Alert severity="error" sx={{ mb: 2 }}>
        <strong>Error:</strong> {error}
        <Box sx={{ mt: 1 }}>
          <Button size="small" onClick={loadDatasets}>Retry</Button>
        </Box>
      </Alert>
    );
  }

  // Empty State
  if (datasets.length === 0) {
    return (
      <Alert severity="info" sx={{ mb: 2 }}>
        No datasets uploaded yet for this domain
      </Alert>
    );
  }

  // Datasets List
  return (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6">📊 Uploaded Datasets ({datasets.length})</Typography>
          <Button
            size="small"
            startIcon={<RefreshIcon />}
            onClick={loadDatasets}
            disabled={loading}
          >
            Refresh
          </Button>
        </Box>

        <Table size="small">
          <TableHead>
            <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
              <TableCell><strong>Dataset Name</strong></TableCell>
              <TableCell align="right"><strong>Rows</strong></TableCell>
              <TableCell align="right"><strong>Columns</strong></TableCell>
              <TableCell><strong>Status</strong></TableCell>
              <TableCell><strong>Created</strong></TableCell>
              {showActions && <TableCell align="center"><strong>Actions</strong></TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {datasets.map((dataset) => (
              <TableRow key={dataset.id} hover>
                <TableCell>
                  <Typography variant="body2" sx={{ fontWeight: 500 }}>
                    {dataset.fileName || dataset.name || `Dataset #${dataset.id}`}
                  </Typography>
                  {dataset.fileSize && (
                    <Typography variant="caption" color="textSecondary">
                      {formatFileSize(dataset.fileSize)}
                    </Typography>
                  )}
                </TableCell>
                <TableCell align="right">
                  {dataset.rowCount || dataset.totalRows || dataset.numberOfRows || 'N/A'}
                </TableCell>
                <TableCell align="right">
                  {dataset.columnCount || dataset.totalColumns || '-'}
                </TableCell>
                <TableCell>
                  <Chip
                    label={
                      typeof dataset.status === 'object' && dataset.status !== null
                        ? dataset.status.modified ? 'Modifié' : dataset.status.downloaded ? 'Téléchargé' : 'Actif'
                        : dataset.status || 'Active'
                    }
                    size="small"
                    color="success"
                    variant="outlined"
                  />
                </TableCell>
                <TableCell>
                  <Typography variant="caption">
                    {formatDate(dataset.createdAt || dataset.uploadedAt || dataset.generatedAt)}
                  </Typography>
                </TableCell>
                {showActions && (
                  <TableCell align="center">
                    <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center' }}>
                      {onViewDataset && (
                        <Button
                          size="small"
                          variant="outlined"
                          startIcon={<ViewIcon />}
                          onClick={() => onViewDataset(dataset)}
                        >
                          View
                        </Button>
                      )}
                      {onDeleteDataset && (
                        <Button
                          size="small"
                          variant="outlined"
                          color="error"
                          startIcon={<DeleteIcon />}
                          onClick={() => handleDelete(dataset.id)}
                        >
                          Delete
                        </Button>
                      )}
                    </Box>
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
};

export default UploadedDatasetsList;
