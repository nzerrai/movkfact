import React, { useState } from 'react';
import { Box, Button, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { DndContext, closestCenter } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy, arrayMove } from '@dnd-kit/sortable';
import ColumnRow from './ColumnRow';

/**
 * Étape 2 du wizard : configuration des colonnes avec drag-and-drop (S7.2 AC3).
 */
const WizardStep2_ColumnConfig = ({ columns, onColumnsChange, onBack, onPreview }) => {
  const [localColumns, setLocalColumns] = useState(
    columns.length > 0
      ? columns
      : [{ id: `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`, name: 'colonne_1', type: 'FIRST_NAME', constraints: {} }]
  );

  const addColumn = () => {
    setLocalColumns((prev) => [
      ...prev,
      {
        id: `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`,
        name: `colonne_${prev.length + 1}`,
        type: 'FIRST_NAME',
        constraints: {},
      },
    ]);
  };

  const removeColumn = (id) => {
    setLocalColumns((prev) => prev.filter((c) => c.id !== id));
  };

  const updateColumn = (id, updated) => {
    setLocalColumns((prev) => prev.map((c) => (c.id === id ? updated : c)));
  };

  const handleDragEnd = (event) => {
    const { active, over } = event;
    if (active.id !== over?.id) {
      setLocalColumns((cols) => {
        const oldIndex = cols.findIndex((c) => c.id === active.id);
        const newIndex = cols.findIndex((c) => c.id === over.id);
        return arrayMove(cols, oldIndex, newIndex);
      });
    }
  };

  const isValid = localColumns.length > 0 && localColumns.every((c) => c.name.trim().length > 0);

  const handlePreview = () => {
    onColumnsChange(localColumns);
    onPreview(localColumns);
  };

  return (
    <Box sx={{ pt: 1 }}>
      <DndContext collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
        <SortableContext
          items={localColumns.map((c) => c.id)}
          strategy={verticalListSortingStrategy}
        >
          {localColumns.map((col) => (
            <ColumnRow
              key={col.id}
              column={col}
              onChange={(updated) => updateColumn(col.id, updated)}
              onRemove={() => removeColumn(col.id)}
              canRemove={localColumns.length > 1}
            />
          ))}
        </SortableContext>
      </DndContext>

      <Button
        startIcon={<AddIcon />}
        onClick={addColumn}
        sx={{ mt: 1, mb: 3 }}
        data-testid="add-column-button"
      >
        + Ajouter une colonne
      </Button>

      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
        <Button onClick={onBack} data-testid="step2-back-button">← Retour</Button>
        <Button
          variant="contained"
          onClick={handlePreview}
          disabled={!isValid}
          data-testid="step2-preview-button"
        >
          Prévisualiser →
        </Button>
      </Box>
    </Box>
  );
};

export default WizardStep2_ColumnConfig;
