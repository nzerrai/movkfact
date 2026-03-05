import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Box, Alert, CircularProgress, Button, Typography } from '@mui/material';
import HistoryIcon from '@mui/icons-material/History';
import { useSnackbar } from 'notistack';
import DataEditorTable from './DataEditorTable';
import DeleteRowDialog from './DeleteRowDialog';
import ActivityPanel from './ActivityPanel';
import { getRows, updateRow, deleteRow } from '../../services/dataSetService';

/**
 * DataEditorTab — Onglet principal de l'éditeur de données inline (S6.2).
 * Orchestre la pagination, l'édition cellule, la suppression et l'historique.
 */
const DataEditorTab = ({ datasetId, rowCount, onModifiedCountChange }) => {
  const { enqueueSnackbar } = useSnackbar();

  const [rows, setRows] = useState([]);
  const [totalRows, setTotalRows] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize] = useState(50);
  const [loading, setLoading] = useState(false);
  const [editingCell, setEditingCell] = useState(null); // { rowIndex, colName }
  const [pendingChanges, setPendingChanges] = useState({}); // { rowIndex: { colName: newValue, ... }, ... }
  const [modifiedCount, setModifiedCount] = useState(0);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [pendingDeleteRowIndex, setPendingDeleteRowIndex] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [activityOpen, setActivityOpen] = useState(false);
  const [sortColumn, setSortColumn] = useState(null);
  const [sortDirection, setSortDirection] = useState('asc');

  const pendingChangesRef = useRef(pendingChanges);
  useEffect(() => {
    pendingChangesRef.current = pendingChanges;
  }, [pendingChanges]);

  const isLargeDataset = rowCount > 50000;

  const loadRows = useCallback(async (targetPage) => {
    if (!datasetId) return;
    setLoading(true);
    try {
      const result = await getRows(datasetId, targetPage, pageSize);
      setRows(result.rows || []);
      setTotalRows(result.totalRows || 0);
    } catch (err) {
      enqueueSnackbar('Erreur lors du chargement des lignes', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  }, [datasetId, pageSize, enqueueSnackbar]);

  // Charger les rows au montage et au changement de page
  useEffect(() => {
    loadRows(page);
  }, [loadRows, page]);

  useEffect(() => {
    onModifiedCountChange?.(modifiedCount);
  }, [modifiedCount, onModifiedCountChange]);

  // ─── Édition cellule ───────────────────────────────────────────────────────

  const handleCellDoubleClick = (rowIndex, colName, currentValue) => {
    setEditingCell({ rowIndex, colName });
  };

  const handleEditClick = (rowIndex) => {
    const row = rows.find(r => r.rowIndex === rowIndex);
    if (!row) return;
    const firstCol = Object.keys(row.data)[0];
    if (firstCol) setEditingCell({ rowIndex, colName: firstCol });
  };

  // Accumule les modifications sans les sauvegarder immédiatement
  const handleCellChange = (rowIndex, colName, newValue) => {
    setPendingChanges(prev => ({
      ...prev,
      [rowIndex]: {
        ...prev[rowIndex],
        [colName]: newValue
      }
    }));
  };

  // Sauvegarde TOUS les changements de la ligne quand on appuie sur Enter
  const handleSaveRow = async (rowIndex) => {
    const changes = pendingChangesRef.current[rowIndex];
    if (!changes || Object.keys(changes).length === 0) {
      setEditingCell(null);
      return;
    }

    try {
      await updateRow(datasetId, rowIndex, changes);
      // Mise à jour locale
      setRows(prev => prev.map(r =>
        r.rowIndex === rowIndex ? { ...r, data: { ...r.data, ...changes } } : r
      ));
      setPendingChanges(prev => {
        const updated = { ...prev };
        delete updated[rowIndex];
        return updated;
      });
      setEditingCell(null);
      setModifiedCount(prev => prev + 1);
      enqueueSnackbar(`Ligne ${rowIndex} modifiée avec succès`, { variant: 'success' });
    } catch (err) {
      enqueueSnackbar(err.response?.data?.message || 'Erreur lors de la modification', { variant: 'error' });
    }
  };

  const handleCellCancel = () => {
    // Annule : restaure l'état original et quitte l'édition
    const rowIndex = editingCell?.rowIndex;
    if (rowIndex !== null && rowIndex !== undefined) {
      setPendingChanges(prev => {
        const updated = { ...prev };
        delete updated[rowIndex];
        return updated;
      });
    }
    setEditingCell(null);
  };

  // Navigation vers la colonne suivante (Tab)
  const handleNavigateNext = (rowIndex, currentColName) => {
    const row = rows.find(r => r.rowIndex === rowIndex);
    if (!row) return;
    const cols = Object.keys(row.data);
    const currentIndex = cols.indexOf(currentColName);
    if (currentIndex >= 0 && currentIndex < cols.length - 1) {
      setEditingCell({ rowIndex, colName: cols[currentIndex + 1] });
    }
  };

  // Navigation vers la colonne précédente (Shift+Tab)
  const handleNavigatePrev = (rowIndex, currentColName) => {
    const row = rows.find(r => r.rowIndex === rowIndex);
    if (!row) return;
    const cols = Object.keys(row.data);
    const currentIndex = cols.indexOf(currentColName);
    if (currentIndex > 0) {
      setEditingCell({ rowIndex, colName: cols[currentIndex - 1] });
    }
  };

  // ─── Suppression ───────────────────────────────────────────────────────────

  const handleDeleteClick = (rowIndex) => {
    setPendingDeleteRowIndex(rowIndex);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    setDeleteLoading(true);
    try {
      await deleteRow(datasetId, pendingDeleteRowIndex);
      await loadRows(page);
      setTotalRows(prev => prev - 1);
      setModifiedCount(prev => prev + 1);
      enqueueSnackbar(`Ligne ${pendingDeleteRowIndex} supprimée`, { variant: 'info' });
      setDeleteDialogOpen(false);
      setPendingDeleteRowIndex(null);
    } catch (err) {
      enqueueSnackbar(err.response?.data?.message || 'Erreur lors de la suppression', { variant: 'error' });
    } finally {
      setDeleteLoading(false);
    }
  };

  // ─── Tri ───────────────────────────────────────────────────────────────────

  const handleSortChange = (col) => {
    if (sortColumn === col) {
      setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
    } else {
      setSortColumn(col);
      setSortDirection('asc');
    }
  };

  return (
    <Box sx={{ display: 'flex', gap: 2 }}>
      <Box sx={{ flex: 1 }}>
        {/* Guard 50k lignes */}
        {isLargeDataset && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            Ce dataset dépasse 50 000 lignes. L'édition inline est désactivée.
            Utilisez le reset ou régénérez.
          </Alert>
        )}

        {/* Barre d'outils */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="body2" color="textSecondary">
            {totalRows} ligne{totalRows > 1 ? 's' : ''} au total
          </Typography>
          <Button
            size="small"
            startIcon={<HistoryIcon />}
            onClick={() => setActivityOpen(prev => !prev)}
            variant={activityOpen ? 'contained' : 'outlined'}
          >
            Historique
          </Button>
        </Box>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <DataEditorTable
            rows={rows}
            totalRows={totalRows}
            page={page}
            pageSize={pageSize}
            onPageChange={setPage}
            sortColumn={sortColumn}
            sortDirection={sortDirection}
            onSortChange={handleSortChange}
            editingCell={editingCell}
            onCellDoubleClick={handleCellDoubleClick}
            onCellChange={handleCellChange}
            onSaveRow={handleSaveRow}
            onCellCancel={handleCellCancel}
            onEditClick={handleEditClick}
            onDeleteClick={handleDeleteClick}
            onNavigateNext={handleNavigateNext}
            onNavigatePrev={handleNavigatePrev}
            disabled={isLargeDataset}
          />
        )}

        <DeleteRowDialog
          open={deleteDialogOpen}
          rowIndex={pendingDeleteRowIndex}
          onConfirm={handleDeleteConfirm}
          onCancel={() => { setDeleteDialogOpen(false); setPendingDeleteRowIndex(null); }}
          loading={deleteLoading}
        />
      </Box>

      <ActivityPanel
        datasetId={datasetId}
        open={activityOpen}
        onClose={() => setActivityOpen(false)}
      />
    </Box>
  );
};

export default DataEditorTab;
