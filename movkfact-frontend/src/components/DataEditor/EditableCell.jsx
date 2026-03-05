import React, { useState, useEffect } from 'react';
import { TableCell, TextField } from '@mui/material';

/**
 * EditableCell — double-clic pour éditer, Entrée pour sauvegarder la ligne,
 * Tab pour passer à la colonne suivante SANS sauvegarder, Échap pour annuler.
 */
const EditableCell = ({
  value,
  rowIndex,
  colName,
  isEditing,
  onDoubleClick,
  onChange, // pour accumuler les changements (au lieu de onSubmit immédiat)
  onSaveRow, // sauvegarde LA LIGNE ENTIÈRE
  onCancel,
  onNavigateNext, // pour Tab
  onNavigatePrev, // pour Shift+Tab
  disabled
}) => {
  const [localValue, setLocalValue] = useState(value);

  useEffect(() => {
    setLocalValue(value);
  }, [value, isEditing]);

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      // Sauvegarde la ligne entière
      onSaveRow(rowIndex);
    } else if (e.key === 'Tab') {
      e.preventDefault();
      // Valider le changement en local et passer à la colonne suivante
      onChange(rowIndex, colName, localValue);
      if (e.shiftKey) {
        onNavigatePrev?.(rowIndex, colName);
      } else {
        onNavigateNext?.(rowIndex, colName);
      }
    } else if (e.key === 'Escape') {
      onCancel();
    }
  };

  if (isEditing) {
    return (
      <TableCell sx={{ border: '2px solid orange', p: 0.5 }}>
        <TextField
          size="small"
          autoFocus
          value={localValue ?? ''}
          onChange={(e) => {
            const newVal = e.target.value;
            setLocalValue(newVal);
            // Accumule les changements EN TEMPS RÉEL
            onChange(rowIndex, colName, newVal);
          }}
          onKeyDown={handleKeyDown}
          variant="outlined"
          fullWidth
        />
      </TableCell>
    );
  }

  return (
    <TableCell
      onDoubleClick={disabled ? undefined : onDoubleClick}
      sx={{
        cursor: disabled ? 'default' : 'pointer',
        '&:hover': { bgcolor: disabled ? 'inherit' : 'action.hover' }
      }}
    >
      {value !== null && value !== undefined ? String(value) : ''}
    </TableCell>
  );
};

export default EditableCell;
