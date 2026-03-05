import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Box,
  Button,
  CircularProgress,
  Alert,
  Paper,
  Typography,
  Stack,
  Breadcrumbs,
  Link,
  Tabs,
  Tab,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import DataViewerContainer from '../components/DataViewer/DataViewerContainer';
import DataEditorTab from '../components/DataEditor/DataEditorTab';

/**
 * DataViewerPage - Affiche un dataset spécifique
 * Route: /data-viewer/:datasetId
 */
const DataViewerPage = () => {
  const { datasetId } = useParams();
  const navigate = useNavigate();
  const [dataset, setDataset] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState(0);
  const [editorModifiedCount, setEditorModifiedCount] = useState(0);

  // Fetch dataset details and data
  useEffect(() => {
    const fetchDataset = async () => {
      try {
        setLoading(true);
        setError(null);

        const apiUrl = process.env.NODE_ENV === 'production'
          ? `/api/data-sets/${datasetId}`
          : `http://localhost:8080/api/data-sets/${datasetId}`;

        const response = await fetch(apiUrl);
        if (!response.ok) {
          throw new Error(`Failed to load dataset: ${response.status}`);
        }

        const result = await response.json();
        const datasetData = result.data || result;
        
        // Fetch actual data if available
        let dataWithContent = { 
          ...datasetData, 
          data: [] // Initialize as empty array
        };
        
        if (datasetData.id) {
          try {
            const dataUrl = process.env.NODE_ENV === 'production'
              ? `/api/data-sets/${datasetData.id}/export?format=json`
              : `http://localhost:8080/api/data-sets/${datasetData.id}/export?format=json`;

            const dataResponse = await fetch(dataUrl);
            if (dataResponse.ok) {
              const dataContent = await dataResponse.json();
              // Parse the data - it might be a string or array
              let parsedData = [];
              
              if (dataContent.data) {
                // If data is a string, parse it
                if (typeof dataContent.data === 'string') {
                  try {
                    parsedData = JSON.parse(dataContent.data);
                  } catch (parseErr) {
                    console.warn('Failed to parse data string:', parseErr);
                    parsedData = [];
                  }
                } 
                // If it's already an array, use it
                else if (Array.isArray(dataContent.data)) {
                  parsedData = dataContent.data;
                }
              }
              
              // Ensure it's always an array
              dataWithContent.data = Array.isArray(parsedData) ? parsedData : [];
            }
          } catch (err) {
            console.warn('Could not fetch dataset content:', err);
            // Continue with empty data
          }
        }
        
        setDataset(dataWithContent);
      } catch (err) {
        console.error('Error loading dataset:', err);
        setError(err.message || 'Failed to load dataset');
      } finally {
        setLoading(false);
      }
    };

    if (datasetId) {
      fetchDataset();
    }
  }, [datasetId]);

  const handleBack = () => {
    navigate('/domains');
  };

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ py: 4, display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress />
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={handleBack}
          variant="outlined"
        >
          Back to Domains
        </Button>
      </Container>
    );
  }

  if (!dataset) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="info">
          Dataset not found
        </Alert>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={handleBack}
          variant="outlined"
          sx={{ mt: 2 }}
        >
          Back to Domains
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Breadcrumb */}
      <Box sx={{ mb: 3 }}>
        <Breadcrumbs>
          <Link
            component="button"
            variant="body2"
            onClick={handleBack}
            sx={{ cursor: 'pointer', color: 'primary.main' }}
          >
            Domains
          </Link>
          <Typography color="textPrimary">
            {dataset.name || `Dataset #${datasetId}`}
          </Typography>
        </Breadcrumbs>
      </Box>

      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <Typography variant="h4" sx={{ mb: 1 }}>
            📊 {dataset.name || `Dataset #${datasetId}`}
          </Typography>
          <Stack direction="row" spacing={3}>
            <Typography variant="body2" color="textSecondary">
              <strong>Rows:</strong> {dataset.rowCount || 0}
            </Typography>
            <Typography variant="body2" color="textSecondary">
              <strong>Columns:</strong> {dataset.columnCount || 0}
            </Typography>
            <Typography variant="body2" color="textSecondary">
              <strong>Generated:</strong> {dataset.generationTimeMs}ms
            </Typography>
          </Stack>
        </div>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={handleBack}
          variant="outlined"
        >
          Back
        </Button>
      </Box>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
        <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)}>
          <Tab label="Visualiseur" />
          <Tab
            label={editorModifiedCount > 0
              ? `Éditeur de données (${editorModifiedCount})`
              : 'Éditeur de données'}
          />
        </Tabs>
      </Box>

      {/* Visualiseur — display:none préserve le state (pagination/filtres) */}
      <Box sx={{ display: activeTab === 0 ? 'block' : 'none' }}>
        <Paper sx={{ p: 3 }}>
          <DataViewerContainer
            dataset={dataset}
            domainId={dataset.domainId}
            onBack={handleBack}
          />
        </Paper>
      </Box>

      {/* Éditeur de données */}
      <Box sx={{ display: activeTab === 1 ? 'block' : 'none' }}>
        <DataEditorTab
          datasetId={Number(datasetId)}
          rowCount={dataset?.rowCount}
          onModifiedCountChange={setEditorModifiedCount}
        />
      </Box>
    </Container>
  );
};

export default DataViewerPage;
