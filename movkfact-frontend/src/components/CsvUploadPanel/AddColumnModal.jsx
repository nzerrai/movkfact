/**
 * AddColumnModal - Component for adding extra columns during CSV upload
 * Provides form for column name, type, and type-specific constraints
 */

import React, { useState, useMemo } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Box,
  Button,
  Alert,
  Typography,
  Paper,
} from '@mui/material';
import { useForm, Controller } from 'react-hook-form';

const COLUMN_TYPES = [
  'TEXT', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'DATE', 'EMAIL', 
  'PHONE', 'ENUM', 'UUID', 'URL', 'PERCENTAGE', 'AMOUNT'
];

const AddColumnModal = ({ open, onAdd, onClose, existingNames = [] }) => {
  const [error, setError] = useState(null);
  const { control, handleSubmit, watch, reset, formState: { errors } } = useForm({
    defaultValues: {
      name: '',
      columnType: 'TEXT',
      minValue: '',
      maxValue: '',
      enumValues: '',
    }
  });

  const selectedType = watch('columnType');

  // Reset form when modal closes
  const handleClose = () => {
    reset();
    setError(null);
    onClose();
  };

  // Validate and submit
  const onSubmit = (data) => {
    // Validate name
    if (!data.name || data.name.trim().length === 0) {
      setError('Column name is required');
      return;
    }

    // Validate uniqueness
    if (existingNames.includes(data.name)) {
      setError(`Column "${data.name}" already exists`);
      return;
    }

    // Validate name format (alphanumeric, underscore, hyphen only)
    if (!/^[a-zA-Z0-9_-]+$/.test(data.name)) {
      setError('Column name can only contain alphanumeric characters, underscores, and hyphens');
      return;
    }

    // Build constraints based on type
    const constraints = {};
    
    if (selectedType === 'INTEGER' || selectedType === 'DECIMAL') {
      if (data.minValue !== '') constraints.min = parseInt(data.minValue);
      if (data.maxValue !== '') constraints.max = parseInt(data.maxValue);
      
      if (constraints.min && constraints.max && constraints.min > constraints.max) {
        setError('Min value cannot be greater than max value');
        return;
      }
    }

    if (selectedType === 'ENUM') {
      if (!data.enumValues || data.enumValues.trim().length === 0) {
        setError('Enum values are required for ENUM type (comma-separated)');
        return;
      }
      constraints.values = data.enumValues
        .split(',')
        .map(v => v.trim())
        .filter(v => v.length > 0);
        
      if (constraints.values.length === 0) {
        setError('At least one enum value is required');
        return;
      }
    }

    // Call parent handler
    onAdd({
      name: data.name.trim(),
      columnType: selectedType,
      constraints: Object.keys(constraints).length > 0 ? constraints : null
    });

    // Reset and close
    handleClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Ajouter une colonne supplémentaire</DialogTitle>

      <DialogContent sx={{ pt: 2 }}>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {/* Column Name */}
          <Controller
            name="name"
            control={control}
            rules={{
              required: 'Column name is required',
              minLength: { value: 1, message: 'Name must not be empty' },
              maxLength: { value: 50, message: 'Name must be less than 50 characters' }
            }}
            render={({ field }) => (
              <TextField
                {...field}
                label="Nom de la colonne"
                placeholder="e.g., status, created_date"
                fullWidth
                size="small"
                error={!!errors.name}
                helperText={errors.name?.message || 'Alphanumeric, underscore, hyphen only'}
              />
            )}
          />

          {/* Column Type */}
          <Controller
            name="columnType"
            control={control}
            render={({ field }) => (
              <FormControl fullWidth size="small">
                <InputLabel>Type</InputLabel>
                <Select
                  {...field}
                  label="Type"
                >
                  {COLUMN_TYPES.map(type => (
                    <MenuItem key={type} value={type}>{type}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}
          />

          {/* Type-Specific Fields */}
          {(selectedType === 'INTEGER' || selectedType === 'DECIMAL') && (
            <Paper sx={{ p: 2, backgroundColor: '#f5f5f5' }}>
              <Typography variant="caption" display="block" sx={{ mb: 1 }}>
                Constraints
              </Typography>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Controller
                  name="minValue"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Min"
                      type="number"
                      size="small"
                      sx={{ flex: 1 }}
                      error={!!errors.minValue}
                      helperText={errors.minValue?.message}
                    />
                  )}
                />
                <Controller
                  name="maxValue"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Max"
                      type="number"
                      size="small"
                      sx={{ flex: 1 }}
                      error={!!errors.maxValue}
                      helperText={errors.maxValue?.message}
                    />
                  )}
                />
              </Box>
            </Paper>
          )}

          {selectedType === 'ENUM' && (
            <Paper sx={{ p: 2, backgroundColor: '#f5f5f5' }}>
              <Controller
                name="enumValues"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Enum Values"
                    placeholder="value1, value2, value3"
                    multiline
                    rows={3}
                    fullWidth
                    size="small"
                    helperText="Comma-separated list of possible values"
                    error={!!errors.enumValues}
                  />
                )}
              />
            </Paper>
          )}
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose}>Annuler</Button>
        <Button 
          onClick={handleSubmit(onSubmit)} 
          variant="contained" 
          color="primary"
        >
          Ajouter colonne
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddColumnModal;