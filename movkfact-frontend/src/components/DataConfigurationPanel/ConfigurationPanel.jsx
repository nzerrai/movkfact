import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Divider,
  Stack,
  TextField,
  Typography
} from '@mui/material';
import React, { useContext, useEffect, useState } from 'react';
import PersonalFieldConfig from './PersonalFieldConfig';
import FinancialFieldConfig from './FinancialFieldConfig';
import NumericFieldConfig from './NumericFieldConfig';
import TemporalFieldConfig from './TemporalFieldConfig';
import ResultViewer from './ResultViewer';

/**
 * S2.6: ConfigurationPanel Component
 * Main configuration interface for customizing columns and generating data
 * 
 * Flow:
 * 1. Display each detected column with type-specific configuration
 * 2. Set row count (1-10000)
 * 3. Click "Generate" to call S2.3 API
 * 4. Display results with export options
 */
const ConfigurationPanel = ({ 
  csvData, 
  detectedTypes, 
  domainId, 
  onGenerationComplete,
  CsvContext 
}) => {
  const [columnConfigs, setColumnConfigs] = useState({});
  const [rowCount, setRowCount] = useState(1000);
  const [isGenerating, setIsGenerating] = useState(false);
  const [generationResult, setGenerationResult] = useState(null);
  const [error, setError] = useState(null);
  const [step, setStep] = useState('configure'); // 'configure' | 'results'
  const [datasetName, setDatasetName] = useState('');
  const [nameError, setNameError] = useState('');
  const [nameValid, setNameValid] = useState(false);
  const [checkingName, setCheckingName] = useState(false);

  // Initialize column configs from detected types
  useEffect(() => {
    if (detectedTypes && Object.keys(detectedTypes).length > 0) {
      const initialConfigs = {};
      Object.entries(detectedTypes).forEach(([colName, colType]) => {
        initialConfigs[colName] = {
          type: colType,
          params: {}
        };
      });
      setColumnConfigs(initialConfigs);
    }
  }, [detectedTypes]);

  // Debounced name uniqueness check
  useEffect(() => {
    if (!datasetName || !nameValid) return;

    const timeoutId = setTimeout(async () => {
      setCheckingName(true);
      try {
        const response = await fetch(`http://localhost:8080/api/domains/${domainId}/datasets/check-name?name=${encodeURIComponent(datasetName)}`);
        const result = await response.json();
        if (result.data && !result.data.available) {
          setNameError('This name already exists in this domain');
          setNameValid(false);
        }
      } catch (error) {
        console.error('Name check failed:', error);
      } finally {
        setCheckingName(false);
      }
    }, 500);

    return () => clearTimeout(timeoutId);
  }, [datasetName, nameValid, domainId]);

  const validateDatasetName = (name) => {
    if (!name || name.trim() === '') {
      return { valid: false, error: 'Dataset name is required' };
    }
    if (name.length < 3) {
      return { valid: false, error: 'Minimum 3 characters required' };
    }
    if (name.length > 50) {
      return { valid: false, error: 'Maximum 50 characters allowed' };
    }
    const regex = /^[a-zA-Z0-9_\-\s]+$/;
    if (!regex.test(name)) {
      return { valid: false, error: 'Only alphanumeric, spaces, dashes, underscores allowed' };
    }
    return { valid: true, error: '' };
  };

  const handleConfigChange = (columnName, newConfig) => {
    setColumnConfigs({
      ...columnConfigs,
      [columnName]: {
        ...columnConfigs[columnName],
        params: newConfig
      }
    });
  };

  const handleNameChange = (e) => {
    const name = e.target.value;
    setDatasetName(name);
    const validation = validateDatasetName(name);
    setNameValid(validation.valid);
    setNameError(validation.error);
  };

  const validateInputs = () => {
    if (!nameValid) {
      setError('Please enter a valid dataset name');
      return false;
    }

    if (rowCount < 1 || rowCount > 10000) {
      setError('Row count must be between 1 and 10,000');
      return false;
    }

    if (!csvData || csvData.length === 0) {
      setError('No CSV data available');
      return false;
    }

    return true;
  };

  const handleGenerate = async () => {
    setError(null);

    if (!validateInputs()) {
      return;
    }

    setIsGenerating(true);

    try {
      // Build column configuration for API (S2.3)
      // Match ColumnConfigDTO structure: name, columnType, format, minValue, maxValue, nullable, additionalConfig
      const columns = Object.entries(columnConfigs).map(([colName, config]) => {
        const colDto = {
          name: colName,
          columnType: config.type // IMPORTANT: Backend expects 'columnType', not 'type'
        };
        
        // Add optional parameters based on type
        const params = config.params || {};
        
        // Map common parameters
        if (params.format) colDto.format = params.format;
        if (params.minValue !== undefined) colDto.minValue = params.minValue;
        if (params.maxValue !== undefined) colDto.maxValue = params.maxValue;
        if (params.nullable !== undefined) colDto.nullable = params.nullable;
        
        // Store type-specific parameters in additionalConfig as JSON
        if (Object.keys(params).length > 0) {
          colDto.additionalConfig = JSON.stringify(params);
        }
        
        return colDto;
      });

      const payload = {
        datasetName: datasetName,
        numberOfRows: rowCount,
        columns: columns
      };

      // Call S2.3 API: POST /api/domains/{domainId}/data-sets
      const response = await fetch(`http://localhost:8080/api/domains/${domainId}/data-sets`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Generation failed (${response.status})`);
      }

      const result = await response.json();
      setGenerationResult(result.data);
      setStep('results');

      if (onGenerationComplete) {
        onGenerationComplete(result.data);
      }

    } catch (err) {
      setError(err.message || 'Generation failed');
      console.error('Generation error:', err);
    } finally {
      setIsGenerating(false);
    }
  };

  const renderConfigForm = () => {
    if (!csvData || csvData.length === 0) {
      return (
        <Alert severity="warning">
          No CSV data available. Please upload a file first.
        </Alert>
      );
    }

    return (
      <Box>
        {/* Column Configurations */}
        <Typography variant="h6" sx={{ mb: 3, mt: 2 }}>
          Column Configuration
        </Typography>

        {Object.entries(columnConfigs).map(([colName, config]) => {
          const colType = config.type;

          // Render appropriate config component based on type
          if (colType?.startsWith('FIRST_NAME') || colType?.startsWith('LAST_NAME') || 
              colType?.startsWith('EMAIL') || colType?.startsWith('GENDER') || 
              colType?.startsWith('PHONE') || colType?.startsWith('ADDRESS')) {
            return (
              <PersonalFieldConfig
                key={colName}
                columnName={colName}
                columnType={colType}
                config={config.params}
                onChange={(newParams) => handleConfigChange(colName, newParams)}
              />
            );
          }

          if (colType?.startsWith('AMOUNT') || colType?.startsWith('ACCOUNT_NUMBER') || 
              colType?.startsWith('CURRENCY')) {
            return (
              <FinancialFieldConfig
                key={colName}
                columnName={colName}
                columnType={colType}
                config={config.params}
                onChange={(newParams) => handleConfigChange(colName, newParams)}
              />
            );
          }

          if (colType === 'INTEGER' || colType === 'DECIMAL' || colType === 'PERCENTAGE') {
            return (
              <NumericFieldConfig
                key={colName}
                columnName={colName}
                columnType={colType}
                config={config.params}
                onChange={(newParams) => handleConfigChange(colName, newParams)}
              />
            );
          }

          if (colType?.startsWith('BIRTH_DATE') || colType?.startsWith('DATE') ||
              colType?.startsWith('TIME') || colType?.startsWith('TIMEZONE')) {
            return (
              <TemporalFieldConfig
                key={colName}
                columnName={colName}
                columnType={colType}
                config={config.params}
                onChange={(newParams) => handleConfigChange(colName, newParams)}
              />
            );
          }

          // Fallback for types without specific config (INTEGER, DECIMAL, TEXT, COUNTRY, etc.)
          return (
            <Box key={colName} sx={{ mb: 2, p: 2, border: '1px solid #eee', borderRadius: 1 }}>
              <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>{colName}</Typography>
              <Typography variant="caption" color="text.secondary">Type: {colType} — no specific configuration required</Typography>
            </Box>
          );
        })}

        <Divider sx={{ my: 3 }} />

        {/* Dataset Name Input - S3.0 */}
        <Typography variant="h6" sx={{ mb: 2 }}>
          Dataset Name
        </Typography>

        <TextField
          fullWidth
          label="Dataset Name *"
          value={datasetName}
          onChange={handleNameChange}
          error={!!nameError}
          helperText={checkingName ? 'Checking availability...' : nameError || 'Enter a unique name for this dataset (3-50 characters)'}
          margin="normal"
          size="small"
          placeholder="e.g., customers_2026_01"
        />

        <Divider sx={{ my: 3 }} />

        {/* Generation Settings */}
        <Typography variant="h6" sx={{ mb: 2 }}>
          Generation Settings
        </Typography>

        <TextField
          fullWidth
          label="Number of Rows to Generate"
          type="number"
          value={rowCount}
          onChange={(e) => setRowCount(parseInt(e.target.value) || 1000)}
          margin="normal"
          size="small"
          inputProps={{ min: 1, max: 10000 }}
          helperText="Between 1 and 10,000 rows"
        />

        {/* Action Buttons */}
        <Stack direction="row" spacing={2} sx={{ mt: 3 }}>
          <Button
            variant="contained"
            color="primary"
            onClick={handleGenerate}
            disabled={isGenerating || !nameValid}
            startIcon={isGenerating ? <CircularProgress size={20} /> : null}
          >
            {isGenerating ? 'Generating...' : 'Generate Data'}
          </Button>
          <Button
            variant="outlined"
            color="secondary"
            onClick={() => setStep('configure')}
          >
            Reset
          </Button>
        </Stack>
      </Box>
    );
  };

  return (
    <Card sx={{ mt: 3 }}>
      <CardContent>
        <Typography variant="h5" sx={{ mb: 2 }}>
          Data Configuration
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {step === 'configure' && renderConfigForm()}

        {step === 'results' && generationResult && (
          <ResultViewer
            data={generationResult}
            domainId={domainId}
            onConfigureMore={() => {
              setStep('configure');
              setGenerationResult(null);
            }}
          />
        )}
      </CardContent>
    </Card>
  );
};

export default ConfigurationPanel;
