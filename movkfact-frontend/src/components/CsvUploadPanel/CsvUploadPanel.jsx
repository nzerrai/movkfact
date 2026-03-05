import { useState } from 'react';
import { Alert, AlertTitle, Box, Button, Card, CardContent, CircularProgress, LinearProgress, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material';
import Papa from 'papaparse';
import UploadZone from './UploadZone';
import PreviewTable from './PreviewTable';
import TypeDetectionResults from './TypeDetectionResults';
import UploadedDatasetsList from './UploadedDatasetsList';

/**
 * S2.5: CsvUploadPanel Component
 * Main component for CSV upload with type detection preview
 */
const CsvUploadPanel = ({ domainId, onProceedToConfiguration, onCancel }) => {
  const [file, setFile] = useState(null);
  const [csvData, setCsvData] = useState([]);
  const [detectionResults, setDetectionResults] = useState([]);
  const [typeOverrides, setTypeOverrides] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [step, setStep] = useState('upload'); // upload | review | confirmed
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleFileSelected = (selectedFile) => {
    handleFile(selectedFile);
  };

  const handleFileDropped = (droppedFile) => {
    handleFile(droppedFile);
  };

  const handleFile = (selectedFile) => {
    setError(null);

    if (!selectedFile.name.endsWith('.csv')) {
      setError('Invalid file format. Please upload a CSV file.');
      return;
    }

    if (selectedFile.size > 10 * 1024 * 1024) {
      setError('File too large. Maximum size: 10 MB');
      return;
    }

    setFile(selectedFile);
    processFile(selectedFile);
  };

  const processFile = async (selectedFile) => {
    setIsLoading(true);
    setError(null);

    try {
      // Parse CSV locally
      const csvData = await new Promise((resolve, reject) => {
        Papa.parse(selectedFile, {
          header: true,
          skipEmptyLines: true,
          complete: (results) => resolve(results.data),
          error: (error) => reject(error)
        });
      });

      if (csvData.length === 0) {
        throw new Error('CSV file is empty');
      }

      setCsvData(csvData);

      // Call S2.2 API for type detection
      await callTypeDetectionAPI(csvData, selectedFile);
      setStep('review');

    } catch (err) {
      setError(err.message || 'Error processing file');
    } finally {
      setIsLoading(false);
    }
  };

  const callTypeDetectionAPI = async (csvData, selectedFile) => {
    const formData = new FormData();
    formData.append('file', selectedFile);

    // Determine API URL - use backend directly or proxy
    const apiUrl = process.env.NODE_ENV === 'production' 
      ? `/api/domains/${domainId}/detect-types`
      : `http://localhost:8080/api/domains/${domainId}/detect-types`;

    const response = await fetch(apiUrl, {
      method: 'POST',
      body: formData,
      headers: {
        'Accept': 'application/json'
      }
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      console.error('API Error Response:', errorData);
      throw new Error(errorData.message || 'Type detection failed');
    }

    const result = await response.json();
    console.log('Detection Results (raw):', result);
    
    // Handle both formats: wrapped in data or direct columns array
    let columns = result.data?.columns || result.columns || [];
    
    // Normalize column properties from backend format to component format
    columns = columns.map(col => ({
      name: col.columnName || col.name,
      type: col.detectedType || col.type,
      confidence: col.confidence || 0,
      detector: col.detector || (col.matchedPatterns?.join(', ') || 'unknown'),
      alternatives: col.alternatives || [],
      matchedPatterns: col.matchedPatterns || []
    }));
    
    console.log('Detection Results (normalized):', columns);
    setDetectionResults(columns);
  };

  const handleTypeOverride = (columnName, newType) => {
    setTypeOverrides((prev) => ({
      ...prev,
      [columnName]: newType
    }));
  };

  const handleConfirm = () => {
    const finalResults = detectionResults.map((col) => ({
      ...col,
      type: typeOverrides[col.name] || col.type
    }));
    
    // Store final results and move to confirmed step
    setDetectionResults(finalResults);
    setStep('confirmed');
  };

  const handleCancel = () => {
    setFile(null);
    setCsvData([]);
    setDetectionResults([]);
    setTypeOverrides({});
    setError(null);
    setStep('upload');
    if (onCancel) onCancel();
  };

  // Error State
  if (error) {
    return (
      <Box>
        <Alert severity="error" sx={{ mb: 2 }}>
          <AlertTitle>Error</AlertTitle>
          {error}
        </Alert>
        <UploadZone
          onFileSelected={handleFileSelected}
          onFileDropped={handleFileDropped}
        />
      </Box>
    );
  }

  // Loading State
  if (isLoading) {
    return (
      <Box sx={{ textAlign: 'center', padding: 3 }}>
        <CircularProgress />
        <Typography sx={{ mt: 2 }}>Processing your file...</Typography>
        <LinearProgress variant="determinate" value={45} sx={{ mt: 1 }} />
        <Typography variant="caption">
          {file?.name || 'Your file'} ({csvData.length} rows detected)
        </Typography>
      </Box>
    );
  }

  // Upload Step
  if (step === 'upload') {
    return (
      <Box>
        <UploadZone
          onFileSelected={handleFileSelected}
          onFileDropped={handleFileDropped}
        />
      </Box>
    );
  }

  // Confirmed Step - Show summary
  if (step === 'confirmed') {
    const successRate = detectionResults.filter((r) => r.confidence > 60).length / detectionResults.length;
    
    return (
      <Box>
        <Alert severity="success" sx={{ mb: 2 }}>
          <AlertTitle>✅ Configuration Complete!</AlertTitle>
          CSV file uploaded successfully with type detection. Ready to proceed with data configuration.
        </Alert>
        
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>📋 Upload Summary</Typography>
            <Box sx={{ mb: 2 }}>
              <Typography variant="body2"><strong>File:</strong> {file?.name}</Typography>
              <Typography variant="body2"><strong>Rows:</strong> {csvData.length}</Typography>
              <Typography variant="body2"><strong>Columns:</strong> {detectionResults.length}</Typography>
              <Typography variant="body2"><strong>Detection Success Rate:</strong> {Math.round(successRate * 100)}%</Typography>
            </Box>
            
            <Typography variant="subtitle2" sx={{ mb: 1 }}>Detected Columns:</Typography>
            <Table size="small">
              <TableHead>
                <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                  <TableCell><strong>Column</strong></TableCell>
                  <TableCell><strong>Type</strong></TableCell>
                  <TableCell><strong>Confidence</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {detectionResults.map((col) => (
                  <TableRow key={col.name}>
                    <TableCell>{col.name}</TableCell>
                    <TableCell>{col.type}</TableCell>
                    <TableCell>{Math.round(col.confidence)}%</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
        
        <Box sx={{ display: 'flex', gap: 1, justifyContent: 'space-between', mb: 3 }}>
          <Button variant="outlined" onClick={() => { setStep('review'); }}>← Back to Review</Button>
          <Button variant="contained" color="success" onClick={() => {
            if (onProceedToConfiguration) {
              // Pass both csvData and detectionResults for ConfigurationPanel
              onProceedToConfiguration({
                csvData: csvData,
                detectionResults: detectionResults
              });
            }
            // Trigger refresh of uploaded datasets list
            setRefreshTrigger(prev => prev + 1);
          }}>
            Proceed to Configuration →
          </Button>
        </Box>

        {/* Display uploaded datasets list */}
        {domainId && (
          <UploadedDatasetsList
            key={refreshTrigger}
            domainId={domainId}
            onViewDataset={(dataset) => {
              console.log('View dataset:', dataset);
              // Could navigate to data viewer here
            }}
            onDeleteDataset={(datasetId) => {
              setRefreshTrigger(prev => prev + 1);
            }}
            showActions={true}
          />
        )}
      </Box>
    );
  }

  // Review Step
  return (
    <Box>
      <PreviewTable csvData={csvData} title={`Preview: ${file?.name}`} />
      <TypeDetectionResults
        results={detectionResults}
        typeOverrides={typeOverrides}
        onTypeOverride={handleTypeOverride}
        onConfirm={handleConfirm}
        onCancel={handleCancel}
      />
    </Box>
  );
};

export default CsvUploadPanel;
