import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  CircularProgress,
  Alert,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import StorageIcon from '@mui/icons-material/Storage';
import UploadedDatasetsList from '../components/CsvUploadPanel/UploadedDatasetsList';
import { getDomains } from '../services/domainService';

const DatasetsPage = () => {
  const navigate = useNavigate();
  const [domains, setDomains] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDomains = async () => {
      try {
        setLoading(true);
        const data = await getDomains(0, 100);
        setDomains(data);
      } catch (err) {
        setError(err.message || 'Failed to load domains');
      } finally {
        setLoading(false);
      }
    };
    fetchDomains();
  }, []);

  const handleViewDataset = (dataset) => {
    navigate(`/data-viewer/${dataset.id}`);
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  if (domains.length === 0) {
    return (
      <Box>
        <Typography variant="h4" sx={{ mb: 3 }}>Datasets</Typography>
        <Alert severity="info">
          No domains found. Create a domain first to see datasets.
        </Alert>
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
        <StorageIcon color="primary" />
        <Typography variant="h4">Datasets</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
          ({domains.length} domain{domains.length !== 1 ? 's' : ''})
        </Typography>
      </Box>

      {domains.map((domain) => (
        <Accordion key={domain.id} defaultExpanded={domains.length === 1}>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography fontWeight={600}>{domain.name}</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <UploadedDatasetsList
              domainId={domain.id}
              onViewDataset={handleViewDataset}
              showActions={true}
            />
          </AccordionDetails>
        </Accordion>
      ))}
    </Box>
  );
};

export default DatasetsPage;
