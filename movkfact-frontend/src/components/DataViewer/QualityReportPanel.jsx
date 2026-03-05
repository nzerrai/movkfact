import React from 'react';
import {
  Box,
  Card,
  CardContent,
  CardHeader,
  Grid,
  LinearProgress,
  Paper,
  Stack,
  Typography,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Alert
} from '@mui/material';
import WarningIcon from '@mui/icons-material/Warning';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

/**
 * S2.7 QualityReportPanel Component (Phase 4)
 * Display data quality metrics and anomaly indicators
 * 
 * Props:
 * - stats: { totalRows, totalColumns, nullCounts, columnTypes }
 * - data: raw dataset rows
 * - column: column name to analyze (optional)
 */
const QualityReportPanel = ({ stats, data = [], columns = [] }) => {
  if (!stats || !data || data.length === 0) {
    return (
      <Card sx={{ mt: 2 }}>
        <CardContent>
          <Alert severity="info">No data available for quality analysis</Alert>
        </CardContent>
      </Card>
    );
  }

  // Calculate quality metrics
  const totalRows = stats.totalRows || data.length;
  const totalColumns = stats.totalColumns || columns.length;
  const nullCounts = stats.nullCounts || {};
  const columnTypes = stats.columnTypes || {};

  // Calculate completeness percentage
  const totalCells = totalRows * totalColumns;
  const totalNulls = Object.values(nullCounts).reduce((a, b) => a + (b || 0), 0);
  const completenessPercent = totalCells > 0 ? Math.round(((totalCells - totalNulls) / totalCells) * 100) : 0;

  // Quality score (0-100)
  const qualityScore = Math.max(0, completenessPercent - (Object.keys(nullCounts).length * 2));

  // Identify problematic columns
  const problematicColumns = Object.entries(nullCounts)
    .filter(([col, count]) => count > (totalRows * 0.1)) // >10% nulls
    .sort(([, a], [, b]) => b - a)
    .slice(0, 5);

  // Data type distribution
  const typeDistribution = {};
  Object.values(columnTypes).forEach(type => {
    typeDistribution[type] = (typeDistribution[type] || 0) + 1;
  });

  return (
    <Box sx={{ mt: 3 }}>
      {/* Quality Score Card */}
      <Card sx={{ mb: 2 }}>
        <CardHeader title="Data Quality Score" />
        <CardContent>
          <Stack spacing={2}>
            {/* Main quality score with progress */}
            <Box>
              <Stack direction="row" justifyContent="space-between" mb={1}>
                <Typography variant="body2">Overall Quality</Typography>
                <Chip
                  label={`${qualityScore}/100`}
                  color={qualityScore >= 80 ? 'success' : qualityScore >= 60 ? 'warning' : 'error'}
                  icon={qualityScore >= 80 ? <CheckCircleIcon /> : <WarningIcon />}
                  size="small"
                />
              </Stack>
              <LinearProgress
                variant="determinate"
                value={qualityScore}
                sx={{
                  height: 8,
                  backgroundColor: '#e0e0e0',
                  '& .MuiLinearProgress-bar': {
                    backgroundColor: qualityScore >= 80 ? '#4caf50' : qualityScore >= 60 ? '#ff9800' : '#f44336'
                  }
                }}
              />
            </Box>

            {/* Completeness */}
            <Box>
              <Stack direction="row" justifyContent="space-between" mb={1}>
                <Typography variant="body2">Data Completeness</Typography>
                <Typography variant="caption">{completenessPercent}%</Typography>
              </Stack>
              <LinearProgress variant="determinate" value={completenessPercent} />
              <Typography variant="caption" color="textSecondary" sx={{ mt: 0.5 }}>
                {totalCells - totalNulls} of {totalCells} cells populated
              </Typography>
            </Box>
          </Stack>
        </CardContent>
      </Card>

      {/* Dataset Overview */}
      <Grid container spacing={2} mb={2}>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 2, textAlign: 'center' }}>
            <Typography variant="h6">{totalRows}</Typography>
            <Typography variant="caption" color="textSecondary">Total Rows</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 2, textAlign: 'center' }}>
            <Typography variant="h6">{totalColumns}</Typography>
            <Typography variant="caption" color="textSecondary">Total Columns</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 2, textAlign: 'center' }}>
            <Typography variant="h6">{totalNulls}</Typography>
            <Typography variant="caption" color="textSecondary">Null Values</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper sx={{ p: 2, textAlign: 'center' }}>
            <Typography variant="h6">{Object.keys(columnTypes).length}</Typography>
            <Typography variant="caption" color="textSecondary">Data Types</Typography>
          </Paper>
        </Grid>
      </Grid>

      {/* Data Type Distribution */}
      {Object.keys(typeDistribution).length > 0 && (
        <Card sx={{ mb: 2 }}>
          <CardHeader title="Data Type Distribution" />
          <CardContent>
            <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
              {Object.entries(typeDistribution).map(([type, count]) => (
                <Chip
                  key={type}
                  label={`${type}: ${count}`}
                  variant="outlined"
                  size="small"
                  sx={{ mb: 1 }}
                />
              ))}
            </Stack>
          </CardContent>
        </Card>
      )}

      {/* Problematic Columns Alert */}
      {problematicColumns.length > 0 && (
        <Card sx={{ mb: 2, backgroundColor: '#fff3e0' }}>
          <CardHeader title="Columns with High Null Percentage" />
          <CardContent>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Column</TableCell>
                  <TableCell align="right">Null Count</TableCell>
                  <TableCell align="right">Percentage</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {problematicColumns.map(([col, count]) => {
                  const percentage = Math.round((count / totalRows) * 100);
                  const isVeryProblematic = percentage > 30;
                  return (
                    <TableRow key={col}>
                      <TableCell>{col}</TableCell>
                      <TableCell align="right">{count}</TableCell>
                      <TableCell align="right">{percentage}%</TableCell>
                      <TableCell>
                        <Chip
                          label={isVeryProblematic ? 'Critical' : 'Warning'}
                          size="small"
                          color={isVeryProblematic ? 'error' : 'warning'}
                          variant="filled"
                        />
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {/* Column-by-Column Analysis */}
      <Card>
        <CardHeader title="Column Null Counts" />
        <CardContent sx={{ maxHeight: 400, overflow: 'auto' }}>
          <Table size="small">
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell>Column</TableCell>
                <TableCell align="right">Null Count</TableCell>
                <TableCell align="right">Percentage</TableCell>
                <TableCell>Data Type</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {columns.map(col => {
                const nullCount = nullCounts[col] || 0;
                const percentage = totalRows > 0 ? Math.round((nullCount / totalRows) * 100) : 0;
                const dataType = columnTypes[col] || 'Unknown';
                return (
                  <TableRow key={col}>
                    <TableCell>{col}</TableCell>
                    <TableCell align="right">{nullCount}</TableCell>
                    <TableCell align="right">{percentage}%</TableCell>
                    <TableCell>{dataType}</TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </Box>
  );
};

export default QualityReportPanel;
