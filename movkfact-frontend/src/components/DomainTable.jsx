import React from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Card,
  CardContent,
  Box,
  Button,
  Chip,
  Grid,
  Skeleton,
  Typography,
  useMediaQuery,
  IconButton,
  Tooltip,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import StorageIcon from '@mui/icons-material/Storage';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import StatusBadge from './StatusBadge';
import { formatRowCount } from '../utils/formatters';

/**
 * DomainTable component - displays domains as table on desktop, cards on mobile
 */
export const DomainTable = ({
  domains = [],
  onEdit,
  onDelete,
  onUpload,
  onViewDatasets,
  onCreateDataset,
  loading = false,
  searchText = '',
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  // Filter domains based on search text
  const filteredDomains = domains.filter(domain =>
    domain.name.toLowerCase().includes(searchText.toLowerCase())
  );

  // Format date utility
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (loading) {
    return (
      <TableContainer component={Card}>
        <Table>
          <TableHead>
            <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
              <TableCell><strong>Nom</strong></TableCell>
              <TableCell><strong>Description</strong></TableCell>
              <TableCell><strong>Datasets</strong></TableCell>
              {!isMobile && <TableCell><strong>Lignes totales</strong></TableCell>}
              <TableCell><strong>Statuts</strong></TableCell>
              <TableCell align="center"><strong>Actions</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {[1, 2, 3].map((i) => (
              <TableRow key={i}>
                <TableCell><Skeleton variant="text" width={120} /></TableCell>
                <TableCell><Skeleton variant="text" width={180} /></TableCell>
                <TableCell><Skeleton variant="rectangular" width={40} height={24} /></TableCell>
                {!isMobile && <TableCell><Skeleton variant="text" width={60} /></TableCell>}
                <TableCell><Skeleton variant="rectangular" width={80} height={24} /></TableCell>
                <TableCell align="center"><Skeleton variant="rectangular" width={120} height={32} /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    );
  }

  if (filteredDomains.length === 0) {
    return (
      <Box sx={{ textAlign: 'center', py: 4 }}>
        <Typography variant="body2" color="text.secondary">
          {domains.length === 0 
            ? 'No domains yet. Create one to get started.' 
            : 'No domains match your search.'}
        </Typography>
      </Box>
    );
  }

  // Mobile Card View
  if (isMobile) {
    return (
      <Grid container spacing={2}>
        {filteredDomains.map(domain => (
          <Grid item xs={12} key={domain.id}>
            <Card>
              <CardContent>
                <Typography variant="h6" component="div" gutterBottom>
                  {domain.name}
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  {domain.description || 'No description'}
                </Typography>
                <Typography variant="caption" display="block" color="text.secondary">
                  Created: {formatDate(domain.createdAt)}
                </Typography>
                <Typography variant="caption" display="block" color="text.secondary" gutterBottom>
                  Updated: {formatDate(domain.updatedAt)}
                </Typography>
                <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
                  <Tooltip title="View Uploaded Datasets">
                    <Button
                      size="small"
                      variant="outlined"
                      startIcon={<StorageIcon />}
                      onClick={() => onViewDatasets?.(domain)}
                      fullWidth
                    >
                      Datasets
                    </Button>
                  </Tooltip>
                  <Button
                    size="small"
                    variant="outlined"
                    startIcon={<AddCircleOutlineIcon />}
                    onClick={() => onCreateDataset?.(domain)}
                    fullWidth
                    sx={{ color: 'success.main', borderColor: 'success.main' }}
                  >
                    Créer
                  </Button>
                  <Button
                    size="small"
                    variant="outlined"
                    startIcon={<CloudUploadIcon />}
                    onClick={() => onUpload?.(domain)}
                    fullWidth
                  >
                    Upload CSV
                  </Button>
                  <Button
                    size="small"
                    variant="outlined"
                    startIcon={<EditIcon />}
                    onClick={() => onEdit?.(domain)}
                    fullWidth
                  >
                    Edit
                  </Button>
                  <IconButton
                    size="small"
                    color="error"
                    onClick={() => onDelete?.(domain)}
                    sx={{ flex: 0 }}
                  >
                    <DeleteIcon />
                  </IconButton>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    );
  }

  // Desktop Table View
  return (
    <TableContainer component={Card}>
      <Table>
        <TableHead>
          <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
            <TableCell><strong>Nom</strong></TableCell>
            <TableCell><strong>Description</strong></TableCell>
            <TableCell><strong>Datasets</strong></TableCell>
            {!isMobile && <TableCell><strong>Lignes totales</strong></TableCell>}
            <TableCell><strong>Statuts</strong></TableCell>
            <TableCell align="center"><strong>Actions</strong></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {filteredDomains.map(domain => (
            <TableRow key={domain.id} hover>
              <TableCell>{domain.name}</TableCell>
              <TableCell>
                {domain.description ? domain.description.substring(0, 50) + '...' : 'N/A'}
              </TableCell>
              <TableCell>
                <Chip
                  label={domain.datasetCount ?? 0}
                  size="small"
                  color="primary"
                  variant="outlined"
                />
              </TableCell>
              {!isMobile && (
                <TableCell>{formatRowCount(domain.totalRows)}</TableCell>
              )}
              <TableCell>
                <StatusBadge
                  downloaded={domain.statuses?.downloaded ?? false}
                  modified={domain.statuses?.modified ?? false}
                  viewed={domain.statuses?.viewed ?? false}
                />
              </TableCell>
              <TableCell align="center">
                <Tooltip title="Créer un dataset">
                  <IconButton
                    size="small"
                    onClick={() => onCreateDataset?.(domain)}
                    sx={{ color: 'success.main' }}
                  >
                    <AddCircleOutlineIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="View Uploaded Datasets">
                  <IconButton
                    size="small"
                    onClick={() => onViewDatasets?.(domain)}
                    sx={{ color: 'primary.main' }}
                  >
                    <StorageIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Upload CSV">
                  <IconButton
                    size="small"
                    onClick={() => onUpload?.(domain)}
                    sx={{ color: 'info.main' }}
                  >
                    <CloudUploadIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Edit">
                  <IconButton
                    size="small"
                    onClick={() => onEdit?.(domain)}
                  >
                    <EditIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Delete">
                  <IconButton
                    size="small"
                    color="error"
                    onClick={() => onDelete?.(domain)}
                  >
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default DomainTable;
