import React, { useState, useEffect } from 'react';
import { Box, Typography, Button, Grid } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import StorageIcon from '@mui/icons-material/Storage';
import PersonIcon from '@mui/icons-material/Person';
import StatCard from '../components/StatCard';
import { useDomainContext } from '../context/DomainContext';

const Dashboard = () => {
  const navigate = useNavigate();
  const { state } = useDomainContext();
  const [totalDatasets, setTotalDatasets] = useState(0);

  useEffect(() => {
    const fetchTotalDatasets = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/data-sets/count');
        if (response.ok) {
          const data = await response.json();
          setTotalDatasets(data.data || 0);
        }
      } catch (error) {
        console.error('Failed to fetch total datasets:', error);
      }
    };

    fetchTotalDatasets();
  }, []);

  return (
    <Box>
      <Typography variant="h2" sx={{ mb: 2 }}>
        Bienvenue Movkfact
      </Typography>
      <Typography variant="body1" sx={{ mb: 4, color: 'textSecondary' }}>
        Plateforme de génération de données pour tests
      </Typography>

      <Grid container spacing={2} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard 
            title="Total Domains" 
            value={state.domains.length.toString()} 
            icon={<StorageIcon sx={{ color: 'primary.main' }} />} 
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <StatCard title="Total Datasets" value={totalDatasets.toString()} icon={<PersonIcon sx={{ color: 'secondary.main' }} />} />
        </Grid>
      </Grid>

      <Box sx={{ mt: 4 }}>
        <Button
          variant="contained"
          color="primary"
          onClick={() => navigate('/domains')}
          size="large"
        >
          Create New Domain
        </Button>
      </Box>
    </Box>
  );
};

export default Dashboard;
