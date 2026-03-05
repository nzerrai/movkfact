import { Box, FormControl, InputLabel, MenuItem, Select, TextField, Typography } from '@mui/material';
import React from 'react';

/**
 * S2.6: TemporalFieldConfig Component
 * Configuration form for Temporal data types (DateBirth, Date, Time, Timezone)
 */
const TemporalFieldConfig = ({ columnName, columnType, config, onChange }) => {
  const handleChange = (field, value) => {
    onChange({
      ...config,
      [field]: value
    });
  };

  const renderFieldConfig = () => {
    switch (columnType) {
      case 'DATE_BIRTH':
        return (
          <Box>
            <TextField
              fullWidth
              label="Birth Year Min"
              type="number"
              value={config.minYear || 1950}
              onChange={(e) => handleChange('minYear', parseInt(e.target.value))}
              margin="normal"
              size="small"
            />
            <TextField
              fullWidth
              label="Birth Year Max"
              type="number"
              value={config.maxYear || 2010}
              onChange={(e) => handleChange('maxYear', parseInt(e.target.value))}
              margin="normal"
              size="small"
            />
          </Box>
        );

      case 'DATE':
        return (
          <Box>
            <TextField
              fullWidth
              label="Date Range Start (YYYY-MM-DD)"
              type="text"
              value={config.startDate || '2020-01-01'}
              onChange={(e) => handleChange('startDate', e.target.value)}
              margin="normal"
              size="small"
              placeholder="2020-01-01"
            />
            <TextField
              fullWidth
              label="Date Range End (YYYY-MM-DD)"
              type="text"
              value={config.endDate || '2026-12-31'}
              onChange={(e) => handleChange('endDate', e.target.value)}
              margin="normal"
              size="small"
              placeholder="2026-12-31"
            />
            <FormControl fullWidth margin="normal" size="small">
              <InputLabel>Format</InputLabel>
              <Select
                value={config.format || 'ISO'}
                onChange={(e) => handleChange('format', e.target.value)}
                label="Format"
              >
                <MenuItem value="ISO">ISO (YYYY-MM-DD)</MenuItem>
                <MenuItem value="EU">EU (DD/MM/YYYY)</MenuItem>
                <MenuItem value="US">US (MM/DD/YYYY)</MenuItem>
                <MenuItem value="TIMESTAMP">Timestamp (milliseconds)</MenuItem>
              </Select>
            </FormControl>
          </Box>
        );

      case 'TIME':
        return (
          <FormControl fullWidth margin="normal" size="small">
            <InputLabel>Format</InputLabel>
            <Select
              value={config.format || '24H'}
              onChange={(e) => handleChange('format', e.target.value)}
              label="Format"
            >
              <MenuItem value="24H">24-hour (HH:MM:SS)</MenuItem>
              <MenuItem value="12H">12-hour (HH:MM:SS AM/PM)</MenuItem>
              <MenuItem value="MS">With Milliseconds (HH:MM:SS.sss)</MenuItem>
            </Select>
          </FormControl>
        );

      case 'TIMEZONE':
        return (
          <FormControl fullWidth margin="normal" size="small">
            <InputLabel>Timezone</InputLabel>
            <Select
              value={config.timezone || 'UTC'}
              onChange={(e) => handleChange('timezone', e.target.value)}
              label="Timezone"
            >
              <MenuItem value="UTC">UTC</MenuItem>
              <MenuItem value="Europe/Paris">Europe/Paris (CET/CEST)</MenuItem>
              <MenuItem value="America/New_York">America/New_York (EST/EDT)</MenuItem>
              <MenuItem value="Asia/Tokyo">Asia/Tokyo (JST)</MenuItem>
              <MenuItem value="Australia/Sydney">Australia/Sydney (AEDT/AEST)</MenuItem>
              <MenuItem value="Europe/London">Europe/London (GMT/BST)</MenuItem>
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

export default TemporalFieldConfig;
