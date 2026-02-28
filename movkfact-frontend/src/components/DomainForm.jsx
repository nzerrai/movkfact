import React, { useState, useEffect } from 'react';
import {
  Box,
  TextField,
  Button,
  CircularProgress,
  Typography,
  Alert,
} from '@mui/material';

/**
 * DomainForm component - reusable form for creating/editing domains
 */
export const DomainForm = ({
  initialData = null,
  onSubmit,
  onCancel,
  loading = false,
  error = null,
}) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const isEditMode = !!initialData;

  // Populate form with initial data
  useEffect(() => {
    if (initialData) {
      setFormData({
        name: initialData.name || '',
        description: initialData.description || '',
      });
    } else {
      setFormData({
        name: '',
        description: '',
      });
    }
    setFieldErrors({});
  }, [initialData]);

  // Parse error response to extract field-level errors (400 status)
  useEffect(() => {
    if (error && error.status === 400) {
      // Extract field-specific errors from backend response if available
      if (error.data && error.data.fieldErrors) {
        setFieldErrors(error.data.fieldErrors);
      } else {
        // Fallback to generic error message
        setFieldErrors({ _global: error.message || 'Validation failed' });
      }
    } else if (error && error.status === 409) {
      // Conflict error typically about duplicate - will be shown as toast
      setFieldErrors({});
    }
  }, [error]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
    // Clear field error when user starts typing
    if (fieldErrors[name]) {
      setFieldErrors(prev => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    // Basic client-side validation
    const errors = {};
    if (!formData.name.trim()) {
      errors.name = 'Name is required';
    }
    if (formData.description.length > 2000) {
      errors.description = 'Description must be less than 2000 characters';
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    // Clear field errors and submit
    setFieldErrors({});
    onSubmit?.(formData);
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      {/* Global error message */}
      {error && error.status !== 409 && error.status !== 400 && (
        <Alert severity="error">
          {error.message}
        </Alert>
      )}

      {/* Name field */}
      <TextField
        label="Domain Name"
        name="name"
        value={formData.name}
        onChange={handleInputChange}
        error={!!fieldErrors.name}
        helperText={fieldErrors.name}
        required
        placeholder="e.g., Production Database"
        disabled={loading}
      />

      {/* Description field */}
      <TextField
        label="Description"
        name="description"
        value={formData.description}
        onChange={handleInputChange}
        multiline
        rows={4}
        placeholder="Describe this domain..."
        helperText={`${formData.description.length}/2000 characters`}
        error={!!fieldErrors.description}
        disabled={loading}
        inputProps={{ maxLength: 2000 }}
      />

      {/* Character counter for description */}
      {formData.description.length > 1900 && (
        <Typography variant="caption" color="warning.main">
          Getting close to character limit ({formData.description.length}/2000)
        </Typography>
      )}

      {/* Submit and Cancel buttons */}
      <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
        <Button
          variant="outlined"
          onClick={onCancel}
          disabled={loading}
        >
          Cancel
        </Button>
        <Button
          type="submit"
          variant="contained"
          disabled={loading}
          startIcon={loading ? <CircularProgress size={20} /> : null}
        >
          {loading ? 'Loading...' : isEditMode ? 'Update Domain' : 'Create Domain'}
        </Button>
      </Box>
    </Box>
  );
};

export default DomainForm;
