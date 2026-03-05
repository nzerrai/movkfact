import { Box, TextField, Typography } from '@mui/material';

/**
 * Configuration min/max for numeric column types (INTEGER, DECIMAL, PERCENTAGE).
 * Sends minValue / maxValue matching ColumnConfigDTO field names.
 */
const NumericFieldConfig = ({ columnName, columnType, config, onChange }) => {
  const handleChange = (field, value) => {
    onChange({ ...config, [field]: value });
  };

  const step = columnType === 'INTEGER' ? '1' : '0.01';

  return (
    <Box sx={{ p: 2, border: '1px solid #e0e0e0', borderRadius: 1, mb: 2 }}>
      <Typography variant="subtitle2" sx={{ mb: 2, fontWeight: 600 }}>
        {columnName} ({columnType})
      </Typography>
      <Box sx={{ display: 'flex', gap: 2 }}>
        <TextField
          label="Valeur min"
          type="number"
          value={config.minValue ?? ''}
          onChange={(e) => handleChange('minValue', e.target.value === '' ? undefined : Number(e.target.value))}
          size="small"
          inputProps={{ step }}
          sx={{ width: 150 }}
        />
        <TextField
          label="Valeur max"
          type="number"
          value={config.maxValue ?? ''}
          onChange={(e) => handleChange('maxValue', e.target.value === '' ? undefined : Number(e.target.value))}
          size="small"
          inputProps={{ step }}
          sx={{ width: 150 }}
        />
      </Box>
    </Box>
  );
};

export default NumericFieldConfig;
