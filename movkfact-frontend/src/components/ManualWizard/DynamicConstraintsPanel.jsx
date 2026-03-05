import React from 'react';
import { Box, TextField, Typography } from '@mui/material';

/**
 * Panel conditionnel affichant les contraintes selon le type de colonne (S7.2 AC3).
 * Types avec contraintes : AMOUNT (min/max), DATE / BIRTH_DATE (dateFrom/dateTo).
 * Autres types : pas de contraintes affichées.
 */
const DynamicConstraintsPanel = ({ column, onChange }) => {
  const constraints = column.constraints || {};

  const handleChange = (key, value) => {
    onChange({ ...column, constraints: { ...constraints, [key]: value } });
  };

  if (column.type === 'INTEGER' || column.type === 'DECIMAL' || column.type === 'PERCENTAGE' || column.type === 'AMOUNT') {
    return (
      <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
        <TextField
          label="Min"
          type="number"
          size="small"
          value={constraints.min ?? ''}
          onChange={(e) => handleChange('min', e.target.value === '' ? undefined : Number(e.target.value))}
          sx={{ width: 100 }}
          inputProps={{ 'data-testid': 'constraint-min' }}
        />
        <TextField
          label="Max"
          type="number"
          size="small"
          value={constraints.max ?? ''}
          onChange={(e) => handleChange('max', e.target.value === '' ? undefined : Number(e.target.value))}
          sx={{ width: 100 }}
          inputProps={{ 'data-testid': 'constraint-max' }}
        />
      </Box>
    );
  }

  if (column.type === 'ENUM') {
    const values = constraints.values ?? [];
    const raw = values.join(', ');
    return (
      <Box sx={{ mt: 1 }}>
        <TextField
          label="Valeurs (séparées par des virgules)"
          size="small"
          value={raw}
          onChange={(e) => {
            const list = e.target.value.split(',').map(v => v.trimStart()).filter(v => v !== '');
            handleChange('values', list);
          }}
          sx={{ width: 280 }}
          placeholder="Ex: Actif, Inactif, Suspendu"
          inputProps={{ 'data-testid': 'constraint-enum-values' }}
        />
      </Box>
    );
  }

  if (column.type === 'TEXT') {
    return (
      <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
        <TextField
          label="Longueur max"
          type="number"
          size="small"
          value={constraints.maxLength ?? ''}
          onChange={(e) => handleChange('maxLength', e.target.value === '' ? undefined : Number(e.target.value))}
          sx={{ width: 130 }}
          inputProps={{ 'data-testid': 'constraint-maxLength' }}
        />
      </Box>
    );
  }

  if (column.type === 'DATE' || column.type === 'BIRTH_DATE') {
    return (
      <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
        <TextField
          label="Date début"
          type="date"
          size="small"
          value={constraints.dateFrom ?? ''}
          onChange={(e) => handleChange('dateFrom', e.target.value || undefined)}
          InputLabelProps={{ shrink: true }}
          sx={{ width: 150 }}
          inputProps={{ 'data-testid': 'constraint-dateFrom' }}
        />
        <TextField
          label="Date fin"
          type="date"
          size="small"
          value={constraints.dateTo ?? ''}
          onChange={(e) => handleChange('dateTo', e.target.value || undefined)}
          InputLabelProps={{ shrink: true }}
          sx={{ width: 150 }}
          inputProps={{ 'data-testid': 'constraint-dateTo' }}
        />
      </Box>
    );
  }

  return null;
};

export default DynamicConstraintsPanel;
