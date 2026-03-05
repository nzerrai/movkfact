import React from 'react';
import { Box, Card, Grid, Stack, Tooltip, Typography } from '@mui/material';

/**
 * S2.7 DatasetStats Component
 * Display dataset metadata and statistics
 */
const DatasetStats = ({ stats }) => {
  return (
    <Card sx={{ p: 2, backgroundColor: '#f5f5f5' }}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3}>
        {/* Total Rows */}
        <Box>
          <Typography variant="body2" color="textSecondary">
            Total Rows
          </Typography>
          <Typography variant="h5" color="primary">
            {stats?.totalRows?.toLocaleString() || 0}
          </Typography>
        </Box>

        {/* Total Columns */}
        <Box>
          <Typography variant="body2" color="textSecondary">
            Total Columns
          </Typography>
          <Typography variant="h5" color="primary">
            {stats?.totalColumns || 0}
          </Typography>
        </Box>

        {/* Data Type Distribution */}
        <Box>
          <Typography variant="body2" color="textSecondary">
            Column Types
          </Typography>
          <Stack direction="row" spacing={1} sx={{ mt: 0.5 }}>
            <Tooltip title="Personal data types">
              <Box sx={{ px: 1, py: 0.5, backgroundColor: '#e3f2fd', borderRadius: 1 }}>
                <Typography variant="caption">Personal</Typography>
              </Box>
            </Tooltip>
            <Tooltip title="Financial data types">
              <Box sx={{ px: 1, py: 0.5, backgroundColor: '#f3e5f5', borderRadius: 1 }}>
                <Typography variant="caption">Financial</Typography>
              </Box>
            </Tooltip>
            <Tooltip title="Temporal data types">
              <Box sx={{ px: 1, py: 0.5, backgroundColor: '#fff3e0', borderRadius: 1 }}>
                <Typography variant="caption">Temporal</Typography>
              </Box>
            </Tooltip>
          </Stack>
        </Box>

        {/* Null/Empty Count */}
        {stats?.nullCounts && Object.keys(stats.nullCounts).length > 0 && (
          <Box>
            <Typography variant="body2" color="textSecondary">
              Columns with Nulls
            </Typography>
            <Typography variant="h5" color="warning.main">
              {Object.keys(stats.nullCounts).length}
            </Typography>
          </Box>
        )}
      </Stack>
    </Card>
  );
};

export default DatasetStats;
