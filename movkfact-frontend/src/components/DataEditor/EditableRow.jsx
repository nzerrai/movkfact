import React from 'react';
import { TableRow, TableCell, IconButton, Tooltip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import EditableCell from './EditableCell';

/**
 * EditableRow — ligne de tableau avec cellules éditables et boutons Modifier/Supprimer.
 */
const EditableRow = ({
  row,
  columns,
  editingCell,
  onCellDoubleClick,
  onChange, // accumule les changements
  onSaveRow, // sauvegarde la ligne entière
  onCellCancel,
  onEditClick,
  onDeleteClick,
  onNavigateNext,
  onNavigatePrev,
  disabled
}) => {
  const { rowIndex, data } = row;

  return (
    <TableRow hover>
      {/* rowIndex — non éditable */}
      <TableCell sx={{ color: 'text.disabled', bgcolor: 'grey.50', fontWeight: 'bold', minWidth: 60 }}>
        {rowIndex}
      </TableCell>

      {/* Cellules éditables */}
      {columns.map((colName) => (
        <EditableCell
          key={colName}
          value={data[colName]}
          rowIndex={rowIndex}
          colName={colName}
          isEditing={editingCell?.rowIndex === rowIndex && editingCell?.colName === colName}
          onDoubleClick={() => onCellDoubleClick(rowIndex, colName, data[colName])}
          onChange={onChange}
          onSaveRow={onSaveRow}
          onCancel={onCellCancel}
          onNavigateNext={onNavigateNext}
          onNavigatePrev={onNavigatePrev}
          disabled={disabled}
        />
      ))}

      {/* Actions */}
      <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>
        <Tooltip title="Modifier">
          <span>
            <IconButton size="small" aria-label="Modifier" onClick={() => onEditClick(rowIndex)} disabled={disabled}>
              <EditIcon fontSize="small" />
            </IconButton>
          </span>
        </Tooltip>
        <Tooltip title="Supprimer">
          <span>
            <IconButton size="small" aria-label="Supprimer" color="error" onClick={() => onDeleteClick(rowIndex)} disabled={disabled}>
              <DeleteIcon fontSize="small" />
            </IconButton>
          </span>
        </Tooltip>
      </TableCell>
    </TableRow>
  );
};

export default EditableRow;
