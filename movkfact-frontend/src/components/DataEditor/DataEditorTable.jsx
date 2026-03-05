import React from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TableSortLabel,
  Paper,
} from '@mui/material';
import EditableRow from './EditableRow';

/**
 * DataEditorTable — MUI Table avec pagination server-side, tri par colonne et lignes éditables.
 */
const DataEditorTable = ({
  rows,
  totalRows,
  page,
  pageSize,
  onPageChange,
  sortColumn,
  sortDirection,
  onSortChange,
  editingCell,
  onCellDoubleClick,
  onCellChange,
  onSaveRow,
  onCellCancel,
  onEditClick,
  onDeleteClick,
  onNavigateNext,
  onNavigatePrev,
  disabled,
}) => {
  // Extraire toutes les colonnes uniques de tous les rows
  const columnsSet = new Set();
  rows.forEach(row => {
    Object.keys(row.data).forEach(col => columnsSet.add(col));
  });
  const columns = Array.from(columnsSet);

  const sortedRows = [...rows].sort((a, b) => {
    if (!sortColumn) return 0;
    const valA = a.data[sortColumn];
    const valB = b.data[sortColumn];
    const cmp = String(valA ?? '').localeCompare(String(valB ?? ''), undefined, { numeric: true });
    return sortDirection === 'asc' ? cmp : -cmp;
  });

  return (
    <Paper>
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow sx={{ bgcolor: 'grey.100' }}>
              <TableCell sx={{ fontWeight: 'bold', color: 'text.disabled' }}>#</TableCell>
              {columns.map((col) => (
                <TableCell key={col}>
                  <TableSortLabel
                    active={sortColumn === col}
                    direction={sortColumn === col ? sortDirection : 'asc'}
                    onClick={() => onSortChange(col)}
                  >
                    <strong>{col}</strong>
                  </TableSortLabel>
                </TableCell>
              ))}
              <TableCell align="right"><strong>Actions</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {sortedRows.map((row) => (
              <EditableRow
                key={row.rowIndex}
                row={row}
                columns={columns}
                editingCell={editingCell}
                onCellDoubleClick={onCellDoubleClick}
                onChange={onCellChange}
                onSaveRow={onSaveRow}
                onCellCancel={onCellCancel}
                onEditClick={onEditClick}
                onDeleteClick={onDeleteClick}
                onNavigateNext={onNavigateNext}
                onNavigatePrev={onNavigatePrev}
                disabled={disabled}
              />
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        component="div"
        count={totalRows}
        page={page}
        rowsPerPage={pageSize}
        rowsPerPageOptions={[25, 50, 100]}
        onPageChange={(_, newPage) => onPageChange(newPage)}
        onRowsPerPageChange={() => {}}
        labelDisplayedRows={({ from, to, count }) => `${from}–${to} sur ${count}`}
      />
    </Paper>
  );
};

export default DataEditorTable;
