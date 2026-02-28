import React from 'react';
import { Card, CardContent, Box, Typography } from '@mui/material';

const StatCard = ({ title, value, icon }) => {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
          <Box sx={{ mr: 2, fontSize: '2rem' }}>{icon}</Box>
          <Typography color="textSecondary" gutterBottom>
            {title}
          </Typography>
        </Box>
        <Typography variant="h3">{value}</Typography>
      </CardContent>
    </Card>
  );
};

export default StatCard;
