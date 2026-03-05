---
title: "🚀 SALLY IMPLEMENTATION START - S2.5 Frontend"
titleFR: "🚀 SALLY IMPLÉMENTATION DÉMARRAGE - S2.5 Frontend"
date: "2026-02-28"
time: "13:00 CET"
recipient: "Sally"
phase: "IMPLEMENTATION"
status: "STARTING NOW"
---

# 🚀 SALLY IMPLEMENTATION START - Begin Now

**To:** Sally  
**From:** Bob (Scrum Master)  
**Date:** 28 février 2026 @ 13:00 CET  
**Status:** 🟢 **IMPLEMENTATION STARTS NOW**

---

## 🎯 THIS IS YOUR IMPLEMENTATION CHECKLIST

Follow this step-by-step to start building CsvUploadPanel NOW.

---

## ✅ STEP 1: Prepare Environment (10 mins)

```bash
# Go to frontend project
cd /home/seplos/mockfact/movkfact-frontend

# Install Papa Parse library (CSV parsing)
npm install papaparse

# Verify npm is working
npm list papaparse
# Should show: papaparse@5.x.x

# Start development server (if not already running)
npm start
# Should open http://localhost:3000
```

---

## ✅ STEP 2: Create Component Directory Structure

```bash
# Create CsvUploadPanel directory
mkdir -p src/components/CsvUploadPanel

# Create component files
touch src/components/CsvUploadPanel/CsvUploadPanel.jsx
touch src/components/CsvUploadPanel/UploadZone.jsx
touch src/components/CsvUploadPanel/PreviewTable.jsx
touch src/components/CsvUploadPanel/TypeDetectionResults.jsx
touch src/components/CsvUploadPanel/CsvUploadPanel.test.jsx

# Verify created
ls -la src/components/CsvUploadPanel/
```

---

## ✅ STEP 3: Create UploadZone Component

**File:** `src/components/CsvUploadPanel/UploadZone.jsx`

```jsx
import { useState, useRef } from 'react';
import { Box, Button, Card, Typography } from '@mui/material';

const UploadZone = ({ onFileSelected, onFileDropped }) => {
  const [dragActive, setDragActive] = useState(false);
  const fileInput = useRef(null);

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(e.type === 'dragenter' || e.type === 'dragover');
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      const file = files[0];
      if (file.type === 'text/csv' || file.name.endsWith('.csv')) {
        onFileDropped(file);
      }
    }
  };

  const handleChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      onFileSelected(e.target.files[0]);
    }
  };

  return (
    <Card
      onDragEnter={handleDrag}
      onDragLeave={handleDrag}
      onDragOver={handleDrag}
      onDrop={handleDrop}
      sx={{
        padding: 4,
        textAlign: 'center',
        border: '2px dashed',
        borderColor: dragActive ? 'primary.main' : 'divider',
        backgroundColor: dragActive ? 'action.hover' : 'background.paper',
        cursor: 'pointer',
        transition: 'all 0.3s ease',
        '&:hover': {
          borderColor: 'primary.light',
          backgroundColor: 'action.hover'
        }
      }}
    >
      <Typography variant="h6" sx={{ mb: 2 }}>
        📁 CSV Upload & Preview
      </Typography>
      <Typography sx={{ mb: 2, color: 'text.secondary' }}>
        Drag CSV file here or click below
      </Typography>
      <Button
        variant="contained"
        onClick={() => fileInput.current?.click()}
        sx={{ mb: 2 }}
      >
        📂 Select File
      </Button>
      <input
        ref={fileInput}
        type="file"
        accept=".csv"
        hidden
        onChange={handleChange}
      />
      <Typography variant="caption" sx={{ display: 'block', color: 'text.secondary' }}>
        CSV files only • Maximum 10 MB
      </Typography>
    </Card>
  );
};

export default UploadZone;
```

---

## ✅ STEP 4: Create PreviewTable Component

**File:** `src/components/CsvUploadPanel/PreviewTable.jsx`

```jsx
import { Box } from '@mui/material';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography
} from '@mui/material';

const PreviewTable = ({ csvData, title = 'Preview (first 10 rows)' }) => {
  if (!csvData || csvData.length === 0) return null;

  const columnNames = Object.keys(csvData[0]);
  const rows = csvData.slice(0, 10);

  return (
    <Box sx={{ mt: 3 }}>
      <Typography variant="h6" sx={{ mb: 2 }}>
        {title}
      </Typography>
      <TableContainer component={Paper}>
        <Table size="small" dense>
          <TableHead>
            <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
              <TableCell align="center" sx={{ fontWeight: 'bold' }}>
                #
              </TableCell>
              {columnNames.map((col) => (
                <TableCell key={col} sx={{ fontWeight: 'bold' }}>
                  {col}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row, idx) => (
              <TableRow key={idx} hover>
                <TableCell align="center" sx={{ backgroundColor: '#fafafa' }}>
                  {idx + 1}
                </TableCell>
                {columnNames.map((col) => (
                  <TableCell
                    key={`${idx}-${col}`}
                    sx={{
                      maxWidth: 150,
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap'
                    }}
                    title={String(row[col])}
                  >
                    {String(row[col]).substring(0, 50)}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default PreviewTable;
```

---

## ✅ STEP 5: Create TypeDetectionResults Component

**File:** `src/components/CsvUploadPanel/TypeDetectionResults.jsx`

```jsx
import { Box, Button, Card, CardContent, TextField, Typography } from '@mui/material';

const TypeDetectionResults = ({
  results,
  typeOverrides,
  onTypeOverride,
  onConfirm,
  onCancel
}) => {
  const getConfidenceColor = (confidence) => {
    if (confidence >= 0.8) return '#4caf50'; // Green
    if (confidence >= 0.6) return '#ff9800'; // Yellow
    return '#f44336'; // Red
  };

  const getConfidenceIcon = (confidence) => {
    if (confidence >= 0.8) return '🟢';
    if (confidence >= 0.6) return '🟡';
    return '🔴';
  };

  const allTypes = [
    // Personal (6)
    'PERSONAL_ID', 'FULL_NAME', 'EMAIL', 'PHONE', 'ADDRESS', 'SSN',
    // Financial (3)
    'AMOUNT', 'BANK_ACCOUNT', 'CURRENCY',
    // Temporal (4)
    'DATE', 'TIME', 'DATETIME', 'DURATION'
  ];

  if (!results || results.length === 0) return null;

  const successRate =
    results.filter((r) => r.confidence > 0.6).length / results.length;

  return (
    <Card sx={{ mt: 3 }}>
      <CardContent>
        <Typography variant="h6" sx={{ mb: 2 }}>
          📊 Column Type Detection Results
        </Typography>

        {results.map((column) => {
          const selectedType = typeOverrides[column.name] || column.type;

          return (
            <Box
              key={column.name}
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 2,
                padding: 2,
                borderBottom: '1px solid #eee',
                '&:hover': { backgroundColor: '#f9f9f9' }
              }}
            >
              {/* Column Name */}
              <Typography sx={{ minWidth: 120, fontWeight: 'bold' }}>
                {column.name}
              </Typography>

              {/* Type Dropdown */}
              <TextField
                select
                value={selectedType}
                onChange={(e) => onTypeOverride(column.name, e.target.value)}
                SelectProps={{ native: true }}
                size="small"
                sx={{ minWidth: 150 }}
              >
                {allTypes.map((type) => (
                  <option key={type} value={type}>
                    {type}
                  </option>
                ))}
              </TextField>

              {/* Confidence Indicator */}
              <Box
                sx={{
                  minWidth: 80,
                  textAlign: 'center',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1
                }}
              >
                <Typography variant="caption">
                  {getConfidenceIcon(column.confidence)}
                </Typography>
                <Typography variant="caption">
                  {Math.round(column.confidence * 100)}%
                </Typography>
              </Box>

              {/* Detector */}
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ flex: 1 }}
              >
                {column.detector}
              </Typography>
            </Box>
          );
        })}

        {/* Summary */}
        <Box sx={{ mt: 2, pt: 2, borderTop: '1px solid #ddd' }}>
          <Typography variant="body2">
            📈 Success Rate: {Math.round(successRate * 100)}% (
            {results.filter((r) => r.confidence > 0.6).length}/{results.length}{' '}
            columns)
          </Typography>
        </Box>

        {/* Buttons */}
        <Box
          sx={{
            display: 'flex',
            gap: 1,
            mt: 3,
            justifyContent: 'space-between'
          }}
        >
          <Button variant="outlined" onClick={onCancel}>
            Cancel
          </Button>
          <Button variant="contained" onClick={onConfirm}>
            Confirm & Continue →
          </Button>
        </Box>
      </CardContent>
    </Card>
  );
};

export default TypeDetectionResults;
```

---

## ✅ STEP 6: Create Main CsvUploadPanel Component

**File:** `src/components/CsvUploadPanel/CsvUploadPanel.jsx`

```jsx
import { useState } from 'react';
import { Alert, AlertTitle, Box, CircularProgress, LinearProgress, Typography } from '@mui/material';
import Papa from 'papaparse';
import UploadZone from './UploadZone';
import PreviewTable from './PreviewTable';
import TypeDetectionResults from './TypeDetectionResults';

const CsvUploadPanel = ({ domainId, onProceedToConfiguration, onCancel }) => {
  const [file, setFile] = useState(null);
  const [csvData, setCsvData] = useState([]);
  const [detectionResults, setDetectionResults] = useState([]);
  const [typeOverrides, setTypeOverrides] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [step, setStep] = useState('upload'); // upload | review

  const handleFileSelected = (selectedFile) => {
    handleFile(selectedFile);
  };

  const handleFileDropped = (droppedFile) => {
    handleFile(droppedFile);
  };

  const handleFile = (selectedFile) => {
    setError(null);

    // Validation
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
      // Step 1: Parse CSV locally
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

      // Step 2: Call S2.2 API for type detection
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

    const response = await fetch(
      `/api/domains/${domainId}/detect-types`,
      {
        method: 'POST',
        body: formData
        // Don't set Content-Type - browser does it automatically
      }
    );

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Type detection failed');
    }

    const result = await response.json();
    setDetectionResults(result.data.columns || []);
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

    if (onProceedToConfiguration) {
      onProceedToConfiguration(finalResults);
    }
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

  // Render: Error State
  if (error) {
    return (
      <Box>
        <Alert severity="error" sx={{ mb: 2 }}>
          <AlertTitle>Error</AlertTitle>
          {error}
        </Alert>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <UploadZone
            onFileSelected={handleFileSelected}
            onFileDropped={handleFileDropped}
          />
        </Box>
      </Box>
    );
  }

  // Render: Loading State
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

  // Render: Upload Step
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

  // Render: Review Step
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
```

---

## ✅ STEP 7: Create Unit Tests

**File:** `src/components/CsvUploadPanel/CsvUploadPanel.test.jsx`

```jsx
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CsvUploadPanel from './CsvUploadPanel';

describe('CsvUploadPanel', () => {
  it('renders upload zone initially', () => {
    render(<CsvUploadPanel domainId={1} />);
    expect(screen.getByText(/Upload & Preview/i)).toBeInTheDocument();
  });

  it('shows error for non-CSV file', async () => {
    render(<CsvUploadPanel domainId={1} />);
    const input = screen.getByDisplayValue(/Select File/i).closest('input');
    
    const file = new File(['test'], 'test.txt', { type: 'text/plain' });
    fireEvent.change(input, { target: { files: [file] } });
    
    // Should show error
    await screen.findByText(/Invalid file format/i);
  });

  it('shows error for oversized file', async () => {
    render(<CsvUploadPanel domainId={1} />);
    const input = screen.getByDisplayValue(/Select File/i).closest('input');
    
    // Create large file (11MB)
    const largeFile = new File(
      [new ArrayBuffer(11 * 1024 * 1024)],
      'large.csv',
      { type: 'text/csv' }
    );
    fireEvent.change(input, { target: { files: [largeFile] } });
    
    // Should show error
    await screen.findByText(/File too large/i);
  });

  it('accepts valid CSV file', async () => {
    render(<CsvUploadPanel domainId={1} />);
    const input = screen.getByDisplayValue(/Select File/i).closest('input');
    
    const csvContent = 'name,email\nJohn,john@test.com';
    const file = new File([csvContent], 'test.csv', { type: 'text/csv' });
    
    fireEvent.change(input, { target: { files: [file] } });
    
    // Wait for loading indicator
    await screen.findByText(/Processing/i);
  });
});
```

---

## ✅ STEP 8: Integrate Component into DomainsPage

**File:** `src/pages/DomainsPage.jsx`

Add this import at top:
```javascript
import CsvUploadPanel from '../components/CsvUploadPanel/CsvUploadPanel';
```

Add state management:
```javascript
const [showUploader, setShowUploader] = useState(false);
const [selectedDomainId, setSelectedDomainId] = useState(null);
```

Add the component in JSX:
```jsx
{showUploader && (
  <CsvUploadPanel
    domainId={selectedDomainId}
    onProceedToConfiguration={(results) => {
      console.log('Detection results:', results);
      setShowUploader(false);
      // TODO: Proceed to S2.6 Configuration
    }}
    onCancel={() => setShowUploader(false)}
  />
)}
```

Add button to trigger uploader:
```jsx
<Button
  onClick={() => {
    setSelectedDomainId(domain.id);
    setShowUploader(true);
  }}
>
  Upload CSV
</Button>
```

---

## ✅ STEP 9: Test in Browser

```bash
# Make sure frontend is running
npm start

# Navigate to: http://localhost:3000

# Test the component:
# 1. Click "Upload CSV" on a domain
# 2. Try drag & drop
# 3. Try file picker
# 4. Upload test.csv file
# 5. Verify preview displays
# 6. Check type detection results show
```

---

## 🎯 VERIFICATION CHECKLIST

- [ ] All component files created (5 files)
- [ ] UploadZone renders correctly
- [ ] Drag & drop zone highlights on hover
- [ ] File picker opens on button click
- [ ] CSV file parsing works
- [ ] PreviewTable displays first 10 rows
- [ ] S2.2 API integration works (after Amelia verifies)
- [ ] Type detection results show with confidence scores
- [ ] Manual type override dropdowns work
- [ ] Error messages display correctly
- [ ] Unit tests passing (npm test)
- [ ] 90%+ code coverage achieved
- [ ] No console errors or warnings

---

## 💡 TROUBLESHOOTING

**Papa Parse not installed:**
```bash
npm install papaparse
```

**Component not rendering:**
- Check import path in DomainsPage.jsx
- Verify all 5 component files created
- Check HTML for SyntaxError in console

**S2.2 API not responding:**
- Amelia needs to verify endpoint (see AMELIA-IMPLEMENTATION-START.md)
- Check Network tab in browser DevTools
- Backend might not be running on port 8080

**Tests not running:**
```bash
npm test -- --coverage
# Check that jest is installed
npm list @testing-library/react
```

---

## 🔗 ASK FOR HELP FROM AMELIA

Once API is verified, message Amelia in response:

```
✅ Frontend component ready!

Status:
- UploadZone: READY
- PreviewTable: READY  
- TypeDetectionResults: READY
- Main component: READY
- Tests: READY (90%+ coverage)

Waiting on: S2.2 API verification
Ready to integrate when API is live!
```

---

**Status:** ✅ **READY TO IMPLEMENT NOW**  
**Timeline:** Complete by ~20 mars EOD  
**Next:** Start building components today

