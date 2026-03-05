import { Box, FormControl, InputLabel, MenuItem, Select, TextField, Typography } from '@mui/material';
import React from 'react';

/**
 * S2.6: PersonalFieldConfig Component
 * Configuration form for Personal data types (FirstName, LastName, Email, Gender, Phone, Address)
 */
const PersonalFieldConfig = ({ columnName, columnType, config, onChange }) => {
  const handleChange = (field, value) => {
    onChange({
      ...config,
      [field]: value
    });
  };

  const renderFieldConfig = () => {
    switch (columnType) {
      case 'FIRST_NAME':
      case 'LAST_NAME':
        return (
          <FormControl fullWidth margin="normal" size="small">
            <InputLabel>Locale</InputLabel>
            <Select
              value={config.locale || 'FR'}
              onChange={(e) => handleChange('locale', e.target.value)}
              label="Locale"
            >
              <MenuItem value="FR">Français</MenuItem>
              <MenuItem value="US">English (US)</MenuItem>
              <MenuItem value="DE">Deutsch</MenuItem>
              <MenuItem value="ES">Español</MenuItem>
            </Select>
          </FormControl>
        );

      case 'EMAIL':
        return (
          <TextField
            fullWidth
            label="Domain Pattern (optional)"
            placeholder="e.g., @example.com"
            value={config.domainPattern || ''}
            onChange={(e) => handleChange('domainPattern', e.target.value)}
            margin="normal"
            size="small"
          />
        );

      case 'GENDER':
        return (
          <FormControl fullWidth margin="normal" size="small">
            <InputLabel>Format</InputLabel>
            <Select
              value={config.format || 'FULL'}
              onChange={(e) => handleChange('format', e.target.value)}
              label="Format"
            >
              <MenuItem value="FULL">Full (Male/Female)</MenuItem>
              <MenuItem value="SHORT">Short (M/F)</MenuItem>
              <MenuItem value="CODES">Codes (M/F/X)</MenuItem>
              <MenuItem value="FR">Français (Homme/Femme)</MenuItem>
            </Select>
          </FormControl>
        );

      case 'PHONE':
        return (
          <FormControl fullWidth margin="normal" size="small">
            <InputLabel>Country Code</InputLabel>
            <Select
              value={config.countryCode || 'FR'}
              onChange={(e) => handleChange('countryCode', e.target.value)}
              label="Country Code"
            >
              <MenuItem value="FR">+33 (France)</MenuItem>
              <MenuItem value="US">+1 (USA)</MenuItem>
              <MenuItem value="DE">+49 (Germany)</MenuItem>
              <MenuItem value="ES">+34 (Spain)</MenuItem>
              <MenuItem value="GB">+44 (UK)</MenuItem>
            </Select>
          </FormControl>
        );

      case 'ADDRESS':
        return (
          <FormControl fullWidth margin="normal" size="small">
            <InputLabel>Country</InputLabel>
            <Select
              value={config.country || 'FR'}
              onChange={(e) => handleChange('country', e.target.value)}
              label="Country"
            >
              <MenuItem value="FR">France</MenuItem>
              <MenuItem value="US">United States</MenuItem>
              <MenuItem value="DE">Germany</MenuItem>
              <MenuItem value="ES">Spain</MenuItem>
              <MenuItem value="IT">Italy</MenuItem>
            </Select>
          </FormControl>
        );

      default:
        return null;
    }
  };

  return (
    <Box sx={{ p: 2, border: '1px solid #e0e0e0', borderRadius: 1, mb: 2 }}>
      <Typography variant="subtitle2" sx={{ mb: 2, fontWeight: 600 }}>
        {columnName} ({columnType})
      </Typography>
      {renderFieldConfig()}
    </Box>
  );
};

export default PersonalFieldConfig;
