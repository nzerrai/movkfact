import { useState } from 'react';
import {
  Alert, AlertTitle, Box, Button, Card, CardContent,
  Chip, CircularProgress, LinearProgress, IconButton,
  Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography, Tooltip
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import Papa from 'papaparse';
import UploadZone from './UploadZone';
import PreviewTable from './PreviewTable';
import TypeDetectionResults from './TypeDetectionResults';
import UploadedDatasetsList from './UploadedDatasetsList';
import AddColumnModal from './AddColumnModal';

/**
 * Nom proposé par défaut selon le type détecté.
 */
const SUGGESTED_BY_TYPE = {
  FIRST_NAME: 'prénom', LAST_NAME: 'nom', EMAIL: 'email', PHONE: 'téléphone',
  GENDER: 'genre', ADDRESS: 'adresse', INTEGER: 'nombre', DECIMAL: 'montant',
  PERCENTAGE: 'taux', BOOLEAN: 'actif', ENUM: 'statut', TEXT: 'description',
  UUID: 'uuid', URL: 'url', IP_ADDRESS: 'ip_address', COUNTRY: 'pays',
  CITY: 'ville', COMPANY: 'entreprise', ZIP_CODE: 'code_postal',
  AMOUNT: 'montant', CURRENCY: 'devise', ACCOUNT_NUMBER: 'numero_compte',
  DATE: 'date', TIME: 'heure', TIMEZONE: 'fuseau_horaire', BIRTH_DATE: 'date_naissance',
};

/**
 * Construit map col_N → nom suggéré (dé-dupliqué avec suffixe _2, _3…).
 */
const buildSuggestedNames = (results) => {
  const used = {};
  const names = {};
  results.forEach((col) => {
    let base = SUGGESTED_BY_TYPE[col.type] || col.name;
    if (used[base]) {
      used[base]++;
      names[col.name] = `${base}_${used[base]}`;
    } else {
      used[base] = 1;
      names[col.name] = base;
    }
  });
  return names;
};

/**
 * S2.5: CsvUploadPanel Component
 * Supports CSV files without headers: détecte les types sur des noms génériques col_N,
 * puis propose des entêtes éditables basées sur les types détectés (step "naming").
 */
const CsvUploadPanel = ({ domainId, onProceedToConfiguration, onCancel }) => {
  const [file, setFile] = useState(null);
  const [csvData, setCsvData] = useState([]);
  const [detectionResults, setDetectionResults] = useState([]);
  const [typeOverrides, setTypeOverrides] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [step, setStep] = useState('upload'); // upload | review | naming | confirmed
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [extraColumns, setExtraColumns] = useState([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [columnNames, setColumnNames] = useState({});
  const [noHeader, setNoHeader] = useState(false);

  const handleFileSelected = (selectedFile) => handleFile(selectedFile);
  const handleFileDropped = (droppedFile) => handleFile(droppedFile);

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
      let parsedData;
      if (noHeader) {
        const raw = await new Promise((resolve, reject) => {
          Papa.parse(selectedFile, {
            header: false,
            skipEmptyLines: true,
            complete: (r) => resolve(r.data),
            error: (e) => reject(e),
          });
        });
        if (raw.length === 0) throw new Error('CSV file is empty');
        // Convert arrays to objects keyed col_1, col_2, …
        parsedData = raw.map((row) =>
          Object.fromEntries(row.map((val, i) => [`col_${i + 1}`, val]))
        );
      } else {
        parsedData = await new Promise((resolve, reject) => {
          Papa.parse(selectedFile, {
            header: true,
            skipEmptyLines: true,
            complete: (r) => resolve(r.data),
            error: (e) => reject(e),
          });
        });
        if (parsedData.length === 0) throw new Error('CSV file is empty');
      }
      setCsvData(parsedData);
      await callTypeDetectionAPI(selectedFile);
      setStep('review');
    } catch (err) {
      setError(err.message || 'Error processing file');
    } finally {
      setIsLoading(false);
    }
  };

  const callTypeDetectionAPI = async (selectedFile) => {
    const formData = new FormData();
    formData.append('file', selectedFile);
    const base = process.env.NODE_ENV === 'production' ? '' : 'http://localhost:8080';
    const url = `${base}/api/domains/${domainId}/detect-types?noHeader=${noHeader}`;
    const response = await fetch(url, {
      method: 'POST',
      body: formData,
      headers: { Accept: 'application/json' },
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Type detection failed');
    }
    const result = await response.json();
    let columns = result.data?.columns || result.columns || [];
    columns = columns.map((col) => ({
      name: col.columnName || col.name,
      type: col.detectedType || col.type,
      confidence: col.confidence || 0,
      detector: col.detector || col.matchedPatterns?.join(', ') || 'unknown',
      alternatives: col.alternatives || [],
      matchedPatterns: col.matchedPatterns || [],
    }));
    setDetectionResults(columns);
  };

  const handleTypeOverride = (columnName, newType) => {
    setTypeOverrides((prev) => ({ ...prev, [columnName]: newType }));
  };

  const handleAddColumn = (newColumn) => {
    setExtraColumns((prev) => [...prev, newColumn]);
    setShowAddModal(false);
  };

  const handleRemoveExtraColumn = (columnName) => {
    setExtraColumns((prev) => prev.filter((col) => col.name !== columnName));
  };

  const handleConfirm = () => {
    const finalResults = detectionResults.map((col) => ({
      ...col,
      type: typeOverrides[col.name] || col.type,
    }));
    setDetectionResults(finalResults);
    if (noHeader) {
      setColumnNames(buildSuggestedNames(finalResults));
      setStep('naming');
    } else {
      setStep('confirmed');
    }
  };

  const handleApplyNames = () => {
    const renamedCsv = csvData.map((row) => {
      const newRow = {};
      detectionResults.forEach((col) => {
        newRow[columnNames[col.name] || col.name] = row[col.name];
      });
      return newRow;
    });
    setCsvData(renamedCsv);
    const renamedResults = detectionResults.map((col) => ({
      ...col,
      name: columnNames[col.name] || col.name,
    }));
    setDetectionResults(renamedResults);
    setStep('confirmed');
  };

  const handleCancel = () => {
    setFile(null);
    setCsvData([]);
    setDetectionResults([]);
    setTypeOverrides({});
    setColumnNames({});
    setError(null);
    setNoHeader(false);
    setStep('upload');
    if (onCancel) onCancel();
  };

  // ── Error ──────────────────────────────────────────────────────────────────
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
          noHeader={noHeader}
          onNoHeaderChange={setNoHeader}
        />
      </Box>
    );
  }

  // ── Loading ────────────────────────────────────────────────────────────────
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

  // ── Upload step ────────────────────────────────────────────────────────────
  if (step === 'upload') {
    return (
      <UploadZone
        onFileSelected={handleFileSelected}
        onFileDropped={handleFileDropped}
        noHeader={noHeader}
        onNoHeaderChange={setNoHeader}
      />
    );
  }

  // ── Naming step (mode sans entête) ─────────────────────────────────────────
  if (step === 'naming') {
    return (
      <Box>
        <Alert severity="info" sx={{ mb: 2 }}>
          Le fichier ne contient pas d'entête. Voici les noms proposés d'après la détection
          automatique — modifiez-les si nécessaire.
        </Alert>
        <Card sx={{ mb: 2 }}>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>Proposer des entêtes de colonnes</Typography>
            <Table size="small">
              <TableHead>
                <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                  <TableCell><strong>Colonne</strong></TableCell>
                  <TableCell><strong>Type détecté</strong></TableCell>
                  <TableCell><strong>Nom proposé</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {detectionResults.map((col) => (
                  <TableRow key={col.name}>
                    <TableCell sx={{ color: 'text.secondary', fontFamily: 'monospace', fontSize: '0.8rem' }}>
                      {col.name}
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={col.type || '—'}
                        size="small"
                        color={col.confidence > 60 ? 'primary' : 'default'}
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>
                      <TextField
                        size="small"
                        value={columnNames[col.name] ?? col.name}
                        onChange={(e) =>
                          setColumnNames((prev) => ({ ...prev, [col.name]: e.target.value }))
                        }
                        sx={{ width: 220 }}
                        inputProps={{ style: { fontFamily: 'monospace' } }}
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
        <Box sx={{ display: 'flex', gap: 1, justifyContent: 'space-between' }}>
          <Button variant="outlined" onClick={() => setStep('review')}>← Retour</Button>
          <Button variant="contained" color="success" onClick={handleApplyNames}>
            Valider les entêtes →
          </Button>
        </Box>
      </Box>
    );
  }

  // ── Confirmed step ─────────────────────────────────────────────────────────
  if (step === 'confirmed') {
    const successRate =
      detectionResults.filter((r) => r.confidence > 60).length / detectionResults.length;
    const existingColumnNames = [
      ...detectionResults.map((r) => r.name),
      ...extraColumns.map((c) => c.name),
    ];

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
              <Typography variant="body2"><strong>Columns:</strong> {detectionResults.length + extraColumns.length}</Typography>
              <Typography variant="body2">
                <strong>Detection Success Rate:</strong> {Math.round(successRate * 100)}%
              </Typography>
            </Box>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>Detected Columns:</Typography>
            <Table size="small" sx={{ mb: 3 }}>
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

            {/* Extra Columns Display */}
            {extraColumns.length > 0 && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle2" sx={{ mb: 1 }}>Extra Columns Added:</Typography>
                <Table size="small" sx={{ mb: 2 }}>
                  <TableHead>
                    <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                      <TableCell><strong>Column</strong></TableCell>
                      <TableCell><strong>Type</strong></TableCell>
                      <TableCell><strong>Constraints</strong></TableCell>
                      <TableCell align="center"><strong>Action</strong></TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {extraColumns.map((col) => (
                      <TableRow key={col.name}>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            {col.name}
                            <Chip label="Ajoutée" size="small" variant="outlined" color="success" />
                          </Box>
                        </TableCell>
                        <TableCell>{col.columnType}</TableCell>
                        <TableCell>
                          {col.constraints ? (
                            <Typography variant="caption" component="div">
                              {Object.entries(col.constraints)
                                .map(([k, v]) => `${k}: ${Array.isArray(v) ? v.join(', ') : v}`)
                                .join(' | ')}
                            </Typography>
                          ) : (
                            '—'
                          )}
                        </TableCell>
                        <TableCell align="center">
                          <Tooltip title="Delete column">
                            <IconButton
                              size="small"
                              onClick={() => handleRemoveExtraColumn(col.name)}
                              color="error"
                            >
                              <DeleteIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </Box>
            )}
          </CardContent>
        </Card>

        <Button 
          variant="outlined" 
          onClick={() => setShowAddModal(true)} 
          sx={{ mb: 2 }}
          disabled={extraColumns.length >= 10} // Max 10 extra columns
        >
          + Ajouter colonne
        </Button>
        {extraColumns.length >= 10 && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            Maximum of 10 extra columns reached
          </Alert>
        )}

        <Box sx={{ display: 'flex', gap: 1, justifyContent: 'space-between', mb: 3 }}>
          <Button variant="outlined" onClick={() => setStep(noHeader ? 'naming' : 'review')}>
            ← Back to Review
          </Button>
          <Button
            variant="contained"
            color="success"
            onClick={() => {
              if (onProceedToConfiguration) {
                onProceedToConfiguration({ csvData, detectionResults, extraColumns });
              }
              setRefreshTrigger((prev) => prev + 1);
            }}
          >
            Proceed to Configuration →
          </Button>
        </Box>
        {domainId && (
          <UploadedDatasetsList
            key={refreshTrigger}
            domainId={domainId}
            onViewDataset={(dataset) => console.log('View dataset:', dataset)}
            onDeleteDataset={() => setRefreshTrigger((prev) => prev + 1)}
            showActions
          />
        )}

        <AddColumnModal
          open={showAddModal}
          onAdd={handleAddColumn}
          onClose={() => setShowAddModal(false)}
          existingNames={existingColumnNames}
        />
      </Box>
    );
  }

  // ── Review step ────────────────────────────────────────────────────────────
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
