import React, { useState, useEffect } from 'react';
import {
  Box, Select, MenuItem, ListSubheader, IconButton,
  FormControl, InputLabel, Autocomplete, TextField,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import DragIndicatorIcon from '@mui/icons-material/DragIndicator';
import AutoFixHighIcon from '@mui/icons-material/AutoFixHigh';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import axios from 'axios';
import DynamicConstraintsPanel from './DynamicConstraintsPanel';
import { COLUMN_TYPE_GROUPS } from '../../constants/columnTypes';
import { BANKING_LEXICON as STATIC_LEXICON } from '../../constants/bankingLexicon';

// Cache module-level pour éviter de recharger à chaque montage de colonne
let _cachedLexicon = null;

/**
 * Ligne de colonne draggable dans l'étape 2 du wizard (S7.2 AC3).
 * Le nom de colonne utilise un Autocomplete avec le lexique bancaire chargé depuis l'API.
 * Quand une suggestion est choisie, le type est pré-rempli automatiquement.
 */
const ColumnRow = ({ column, onChange, onRemove, canRemove }) => {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({ id: column.id });
  const style = { transform: CSS.Transform.toString(transform), transition };

  const [lexicon, setLexicon] = useState(_cachedLexicon ?? STATIC_LEXICON);

  useEffect(() => {
    if (_cachedLexicon) return; // déjà chargé
    axios.get('/api/lexicon/banking')
      .then(r => {
        const data = r.data?.data ?? r.data ?? [];
        if (data.length > 0) {
          _cachedLexicon = data;
          setLexicon(data);
        }
      })
      .catch(() => { /* conserver le fallback statique */ });
  }, []);

  const handleSuggestionSelect = (_, selected) => {
    if (!selected) return;
    const label = typeof selected === 'string' ? selected : selected.label;
    const entry = typeof selected === 'string'
      ? lexicon.find(e => e.label.toLowerCase() === selected.toLowerCase())
      : selected;
    onChange({
      ...column,
      name: label,
      type: entry?.type ?? column.type,
      constraints: entry?.type && entry.type !== column.type ? {} : column.constraints,
    });
  };

  const handleInputChange = (_, value) => {
    onChange({ ...column, name: value });
  };

  return (
    <Box
      ref={setNodeRef}
      style={style}
      sx={{ display: 'flex', gap: 1, mb: 1, alignItems: 'flex-start', flexWrap: 'wrap' }}
    >
      <Box
        {...attributes}
        {...listeners}
        sx={{ cursor: 'grab', color: 'text.secondary', pt: 1 }}
        aria-label="drag-handle"
      >
        <DragIndicatorIcon />
      </Box>

      <Autocomplete
        freeSolo
        options={lexicon}
        groupBy={option => option.group}
        getOptionLabel={option => (typeof option === 'string' ? option : option.label)}
        inputValue={column.name}
        onInputChange={handleInputChange}
        onChange={handleSuggestionSelect}
        sx={{ width: 220 }}
        size="small"
        renderInput={params => (
          <TextField
            {...params}
            label="Nom de la colonne"
            inputProps={{
              ...params.inputProps,
              'data-testid': 'column-name-input',
            }}
          />
        )}
        renderOption={(props, option) => (
          <li {...props} key={option.label}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', width: '100%', gap: 1 }}>
              <span>{option.label}</span>
              {option.type && (
                <Box
                  component="span"
                  sx={{ fontSize: '0.7rem', color: 'text.secondary', display: 'flex', alignItems: 'center', gap: 0.5 }}
                >
                  <AutoFixHighIcon sx={{ fontSize: '0.85rem' }} />
                  {option.type}
                </Box>
              )}
            </Box>
          </li>
        )}
      />

      <FormControl size="small" sx={{ width: 160 }}>
        <InputLabel>Type</InputLabel>
        <Select
          value={column.type}
          label="Type"
          onChange={e => onChange({ ...column, type: e.target.value, constraints: {} })}
          inputProps={{ 'data-testid': 'column-type-select' }}
        >
          {COLUMN_TYPE_GROUPS.flatMap(group => [
            <ListSubheader key={group.label}>{group.label}</ListSubheader>,
            ...group.types.map(t => (
              <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>
            )),
          ])}
        </Select>
      </FormControl>

      <DynamicConstraintsPanel column={column} onChange={onChange} />

      <IconButton
        size="small"
        color="error"
        onClick={onRemove}
        disabled={!canRemove}
        aria-label="Supprimer la colonne"
        sx={{ mt: 0.5 }}
      >
        <DeleteIcon fontSize="small" />
      </IconButton>
    </Box>
  );
};

export default ColumnRow;
