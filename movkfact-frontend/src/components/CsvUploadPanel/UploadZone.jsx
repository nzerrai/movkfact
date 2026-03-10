import { useState, useRef } from 'react';
import { Box, Button, Card, Checkbox, FormControlLabel, Typography } from '@mui/material';

/**
 * S2.5: UploadZone Component
 * Drag & drop CSV file upload interface with optional no-header mode.
 */
const UploadZone = ({ onFileSelected, onFileDropped, noHeader, onNoHeaderChange }) => {
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
    <Box>
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
          '&:hover': { borderColor: 'primary.light', backgroundColor: 'action.hover' }
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
        <Box sx={{ mt: 2 }} onClick={(e) => e.stopPropagation()}>
          <FormControlLabel
            control={
              <Checkbox
                checked={!!noHeader}
                onChange={(e) => onNoHeaderChange(e.target.checked)}
                size="small"
              />
            }
            label={
              <Typography variant="body2" color="text.secondary">
                Ce fichier n'a pas de ligne d'entête
              </Typography>
            }
          />
        </Box>
      </Card>
    </Box>
  );
};

export default UploadZone;
