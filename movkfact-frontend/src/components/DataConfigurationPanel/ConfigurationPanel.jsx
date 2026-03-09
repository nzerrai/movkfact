import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Checkbox,
  Chip,
  CircularProgress,
  Divider,
  FormControl,
  FormControlLabel,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Tooltip,
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
  // S9.1: detectedTypes values may be { type, confidence, inferenceLevel } objects or plain strings
  // S9.2: also preserve isPII and piiCategory
  const resolveType = (val) => (val && typeof val === 'object' ? val.type : val);
  const resolveConfidence = (val) => (val && typeof val === 'object' ? val.confidence : null);
  const resolveLevel = (val) => (val && typeof val === 'object' ? val.inferenceLevel : null);
  const resolveLearnedCount = (val) => (val && typeof val === 'object' ? val.learnedCount || 0 : 0);
  const resolveIsPII = (val) => (val && typeof val === 'object' ? val.isPII || false : false);
  const resolvePiiCategory = (val) => (val && typeof val === 'object' ? val.piiCategory || null : null);

  // S9.2: anonymization state — pre-checked for PII columns, user can uncheck (with warning)
  const [anonymizeState, setAnonymizeState] = useState({});

  useEffect(() => {
    if (detectedTypes && Object.keys(detectedTypes).length > 0) {
      const initialConfigs = {};
      const initialAnonymize = {};
      Object.entries(detectedTypes).forEach(([colName, colVal]) => {
        initialConfigs[colName] = {
          type: resolveType(colVal),
          detectedType: resolveType(colVal), // S10.1 — type initial pour le feedback
          confidence: resolveConfidence(colVal),
          inferenceLevel: resolveLevel(colVal),
          learnedCount: resolveLearnedCount(colVal),
          isPII: resolveIsPII(colVal),
          piiCategory: resolvePiiCategory(colVal),
          params: {}
        };
        // Pre-check anonymization for PII columns (AC2)
        if (resolveIsPII(colVal)) {
          initialAnonymize[colName] = true;
        }
      });
      setColumnConfigs(initialConfigs);
      setAnonymizeState(initialAnonymize);
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

  // S10.1 / M4 — sélecteur de type : permet de corriger le type détecté
  const handleTypeChange = (columnName, newType) => {
    setColumnConfigs(prev => ({
      ...prev,
      [columnName]: {
        ...prev[columnName],
        type: newType,
        params: {} // reset des params spécifiques au type précédent
      }
    }));
  };

  // Tous les types disponibles (miroir de l'enum Java ColumnType)
  const ALL_COLUMN_TYPES = [
    { value: 'FIRST_NAME', label: 'Prénom' },
    { value: 'LAST_NAME', label: 'Nom de famille' },
    { value: 'EMAIL', label: 'Email' },
    { value: 'PHONE', label: 'Téléphone' },
    { value: 'GENDER', label: 'Genre' },
    { value: 'ADDRESS', label: 'Adresse' },
    { value: 'INTEGER', label: 'Nombre entier' },
    { value: 'DECIMAL', label: 'Nombre décimal' },
    { value: 'PERCENTAGE', label: 'Pourcentage' },
    { value: 'BOOLEAN', label: 'Booléen' },
    { value: 'ENUM', label: 'Liste de valeurs' },
    { value: 'TEXT', label: 'Texte libre' },
    { value: 'UUID', label: 'UUID' },
    { value: 'URL', label: 'URL' },
    { value: 'IP_ADDRESS', label: 'Adresse IP' },
    { value: 'COUNTRY', label: 'Pays' },
    { value: 'CITY', label: 'Ville' },
    { value: 'COMPANY', label: 'Entreprise' },
    { value: 'ZIP_CODE', label: 'Code postal' },
    { value: 'AMOUNT', label: 'Montant' },
    { value: 'CURRENCY', label: 'Devise' },
    { value: 'ACCOUNT_NUMBER', label: 'Numéro de compte' },
    { value: 'DATE', label: 'Date générique' },
    { value: 'TIME', label: 'Heure' },
    { value: 'TIMEZONE', label: 'Fuseau horaire' },
    { value: 'BIRTH_DATE', label: 'Date de naissance' },
  ];

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

        // ENUM: send values as constraints so EnumGenerator can pick a random value
        if (config.type === 'ENUM' && params.values?.length > 0) {
          colDto.constraints = { values: params.values };
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

      // S10.1 — envoyer le feedback de détection pour apprentissage adaptatif
      try {
        const feedbacks = Object.entries(columnConfigs).map(([colName, config]) => ({
          colName,
          detectedType: config.detectedType || null,
          validatedType: config.type || null,
        }));
        await fetch(`http://localhost:8080/api/domains/${domainId}/feedback`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(feedbacks),
        });
      } catch (feedbackErr) {
        // Non bloquant — le feedback est best-effort
        console.warn('Feedback submission failed (non-blocking):', feedbackErr);
      }

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

  // S9.1 + S10.1 — badge de confiance / apprentissage
  const ConfidenceBadge = ({ confidence, inferenceLevel, learnedCount }) => {
    if (confidence == null) return null;
    // S10.1 — badge "Appris" si LEARNED (AC5 — tooltip avec N fois)
    if (inferenceLevel === 'LEARNED') {
      const countLabel = learnedCount > 0 ? `${learnedCount} fois` : 'plusieurs fois';
      return (
        <Tooltip title={`Détecté via vos précédentes validations (${countLabel}) — confiance ${Math.round(confidence)}%`}>
          <Chip label="Appris" size="small" color="primary" sx={{ ml: 1 }} />
        </Tooltip>
      );
    }
    const label = confidence >= 85 ? 'Haute' : confidence >= 60 ? 'Moyenne' : 'Faible';
    const color = confidence >= 85 ? 'success' : confidence >= 60 ? 'warning' : 'default';
    const tip = inferenceLevel === 'NAME_BASED' ? 'Détecté via le nom de colonne' : 'Détecté via l\'analyse des données';
    return (
      <Tooltip title={`${tip} — confiance ${Math.round(confidence)}%`}>
        <Chip label={`Confiance ${label}`} size="small" color={color} sx={{ ml: 1 }} />
      </Tooltip>
    );
  };

  // S9.2 — badge PII avec catégorie RGPD
  const PiiBadge = ({ piiCategory }) => {
    if (!piiCategory) return null;
    const labels = { CONTACT: 'Contact', IDENTITY: 'Identité', LOCATION: 'Localisation' };
    return (
      <Tooltip title={`Donnée personnelle RGPD — catégorie : ${piiCategory}`}>
        <Chip label={`PII · ${labels[piiCategory] || piiCategory}`} size="small" color="error" sx={{ ml: 1 }} />
      </Tooltip>
    );
  };

  // S9.2 — case à cocher anonymisation + avertissement si décochée
  const AnonymizeCheckbox = ({ colName, isPII, piiCategory }) => {
    if (!isPII) return null;
    const checked = anonymizeState[colName] !== false;
    return (
      <Box sx={{ mt: 1 }}>
        <FormControlLabel
          control={
            <Checkbox
              checked={checked}
              size="small"
              onChange={(e) => setAnonymizeState(prev => ({ ...prev, [colName]: e.target.checked }))}
            />
          }
          label={<Typography variant="caption">Anonymiser (RGPD)</Typography>}
        />
        {!checked && (
          <Alert severity="warning" sx={{ mt: 0.5, py: 0.5 }}>
            Cette colonne contient des données personnelles ({piiCategory}). Désactiver l'anonymisation peut enfreindre le RGPD.
          </Alert>
        )}
      </Box>
    );
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
          const isPII = config.isPII || false;
          const piiCategory = config.piiCategory || null;

          // S10.1 / M4 — en-tête commun à toutes les colonnes : badge + sélecteur de type
          const ColumnHeader = () => (
            <Box sx={{ mb: 1, p: 1.5, border: '1px solid #e0e0e0', borderRadius: 1, backgroundColor: '#fafafa' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 0.5, mb: 1 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>{colName}</Typography>
                <ConfidenceBadge
                  confidence={config.confidence}
                  inferenceLevel={config.inferenceLevel}
                  learnedCount={config.learnedCount}
                />
                <PiiBadge piiCategory={piiCategory} />
              </Box>
              <FormControl size="small" sx={{ minWidth: 220 }}>
                <InputLabel>Type de colonne</InputLabel>
                <Select
                  value={colType || ''}
                  label="Type de colonne"
                  onChange={(e) => handleTypeChange(colName, e.target.value)}
                >
                  {ALL_COLUMN_TYPES.map(t => (
                    <MenuItem key={t.value} value={t.value}>{t.label} ({t.value})</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Box>
          );

          // Render appropriate config component based on type
          if (colType?.startsWith('FIRST_NAME') || colType?.startsWith('LAST_NAME') ||
              colType?.startsWith('EMAIL') || colType?.startsWith('GENDER') ||
              colType?.startsWith('PHONE') || colType?.startsWith('ADDRESS')) {
            return (
              <Box key={colName} sx={{ mb: 2 }}>
                <ColumnHeader />
                <PersonalFieldConfig
                  columnName={colName}
                  columnType={colType}
                  config={config.params}
                  onChange={(newParams) => handleConfigChange(colName, newParams)}
                  piiCategory={piiCategory}
                />
                <AnonymizeCheckbox colName={colName} isPII={isPII} piiCategory={piiCategory} />
              </Box>
            );
          }

          if (colType?.startsWith('AMOUNT') || colType?.startsWith('ACCOUNT_NUMBER') ||
              colType?.startsWith('CURRENCY')) {
            return (
              <Box key={colName} sx={{ mb: 2 }}>
                <ColumnHeader />
                <FinancialFieldConfig
                  columnName={colName}
                  columnType={colType}
                  config={config.params}
                  onChange={(newParams) => handleConfigChange(colName, newParams)}
                />
                <AnonymizeCheckbox colName={colName} isPII={isPII} piiCategory={piiCategory} />
              </Box>
            );
          }

          if (colType === 'INTEGER' || colType === 'DECIMAL' || colType === 'PERCENTAGE') {
            return (
              <Box key={colName} sx={{ mb: 2 }}>
                <ColumnHeader />
                <NumericFieldConfig
                  columnName={colName}
                  columnType={colType}
                  config={config.params}
                  onChange={(newParams) => handleConfigChange(colName, newParams)}
                />
                <AnonymizeCheckbox colName={colName} isPII={isPII} piiCategory={piiCategory} />
              </Box>
            );
          }

          if (colType?.startsWith('BIRTH_DATE') || colType?.startsWith('DATE') ||
              colType?.startsWith('TIME') || colType?.startsWith('TIMEZONE')) {
            return (
              <Box key={colName} sx={{ mb: 2 }}>
                <ColumnHeader />
                <TemporalFieldConfig
                  columnName={colName}
                  columnType={colType}
                  config={config.params}
                  onChange={(newParams) => handleConfigChange(colName, newParams)}
                />
                <AnonymizeCheckbox colName={colName} isPII={isPII} piiCategory={piiCategory} />
              </Box>
            );
          }

          if (colType === 'ENUM') {
            const values = config.params?.values ?? [];
            // rawEnumInput holds the live string while typing; falls back to joined values
            const raw = config.params?.rawEnumInput ?? values.join(', ');
            return (
              <Box key={colName} sx={{ mb: 2 }}>
                <ColumnHeader />
                <TextField
                  label="Valeurs (séparées par des virgules)"
                  size="small"
                  value={raw}
                  onChange={(e) => {
                    // Only update the raw display string — do not parse yet
                    handleConfigChange(colName, { ...config.params, rawEnumInput: e.target.value });
                  }}
                  onBlur={(e) => {
                    // Parse into array on blur
                    const list = e.target.value.split(',').map(v => v.trim()).filter(v => v !== '');
                    handleConfigChange(colName, { values: list, rawEnumInput: list.join(', ') });
                  }}
                  fullWidth
                  placeholder="Ex: Actif, Inactif, Suspendu"
                  helperText={values.length === 0 ? 'Saisissez au moins une valeur' : `${values.length} valeur(s) définies`}
                  error={values.length === 0}
                  sx={{ mt: 1 }}
                />
                <AnonymizeCheckbox colName={colName} isPII={isPII} piiCategory={piiCategory} />
              </Box>
            );
          }

          // Fallback for types without specific config (TEXT, COUNTRY, ZIP_CODE, CITY, etc.)
          return (
            <Box key={colName} sx={{ mb: 2 }}>
              <ColumnHeader />
              <Typography variant="caption" color="text.secondary" sx={{ pl: 0.5 }}>
                Aucune configuration spécifique requise pour ce type.
              </Typography>
              <AnonymizeCheckbox colName={colName} isPII={isPII} piiCategory={piiCategory} />
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
