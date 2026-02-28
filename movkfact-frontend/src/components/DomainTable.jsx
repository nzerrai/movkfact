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
  Grid,
  CircularProgress,
  Typography,
  useMediaQuery,
  IconButton,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';

/**
 * DomainTable component - displays domains as table on desktop, cards on mobile
 */
export const DomainTable = ({
  domains = [],
  onEdit,
  onDelete,
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
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
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
            <TableCell><strong>Name</strong></TableCell>
            <TableCell><strong>Description</strong></TableCell>
            <TableCell><strong>Created</strong></TableCell>
            <TableCell><strong>Updated</strong></TableCell>
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
              <TableCell>{formatDate(domain.createdAt)}</TableCell>
              <TableCell>{formatDate(domain.updatedAt)}</TableCell>
              <TableCell align="center">
                <IconButton
                  size="small"
                  onClick={() => onEdit?.(domain)}
                  title="Edit"
                >
                  <EditIcon fontSize="small" />
                </IconButton>
                <IconButton
                  size="small"
                  color="error"
                  onClick={() => onDelete?.(domain)}
                  title="Delete"
                >
                  <DeleteIcon fontSize="small" />
                </IconButton>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default DomainTable;
