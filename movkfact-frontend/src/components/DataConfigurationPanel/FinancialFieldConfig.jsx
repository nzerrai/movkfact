import { Box, FormControl, InputLabel, MenuItem, Select, TextField, Typography } from '@mui/material';
import React from 'react';

/**
 * S2.6: FinancialFieldConfig Component
 * Configuration form for Financial data types (Amount, AccountNumber, Currency)
 */
const FinancialFieldConfig = ({ columnName, columnType, config, onChange }) => {
  const handleChange = (field, value) => {
    onChange({
      ...config,
      [field]: value
    });
  };

  const renderFieldConfig = () => {
    switch (columnType) {
      case 'AMOUNT':
        return (
          <Box>
            <FormControl fullWidth margin="normal" size="small">
              <InputLabel>Currency</InputLabel>
              <Select
                value={config.currency || 'EUR'}
                onChange={(e) => handleChange('currency', e.target.value)}
                label="Currency"
              >
                <MenuItem value="EUR">EUR (€)</MenuItem>
                <MenuItem value="USD">USD ($)</MenuItem>
                <MenuItem value="GBP">GBP (£)</MenuItem>
                <MenuItem value="JPY">JPY (¥)</MenuItem>
                <MenuItem value="CHF">CHF (CHF)</MenuItem>
              </Select>
            </FormControl>
            <TextField
              fullWidth
              label="Montant min"
              type="number"
              value={config.minValue ?? 0}
              onChange={(e) => handleChange('minValue', parseFloat(e.target.value))}
              margin="normal"
              size="small"
              inputProps={{ step: '0.01' }}
            />
            <TextField
              fullWidth
              label="Montant max"
              type="number"
              value={config.maxValue ?? 1000000}
              onChange={(e) => handleChange('maxValue', parseFloat(e.target.value))}
              margin="normal"
              size="small"
              inputProps={{ step: '0.01' }}
            />
          </Box>
        );

      case 'ACCOUNT_NUMBER':
        return (
          <FormControl fullWidth margin="normal" size="small">
            <InputLabel>Format</InputLabel>
            <Select
              value={config.format || 'IBAN'}
              onChange={(e) => handleChange('format', e.target.value)}
              label="Format"
            >
              <MenuItem value="IBAN">IBAN (International)</MenuItem>
              <MenuItem value="BBAN">BBAN (Basic)</MenuItem>
              <MenuItem value="MASKED">Masked Account (**** **** 1234)</MenuItem>
              <MenuItem value="GENERIC">Generic Format (16 digits)</MenuItem>
            </Select>
          </FormControl>
        );

      case 'CURRENCY':
        return (
          <Typography variant="body2" sx={{ mt: 2, p: 1, bgcolor: '#f5f5f5', borderRadius: 1 }}>
            Currency code will be auto-populated. No additional configuration needed.
          </Typography>
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

export default FinancialFieldConfig;
