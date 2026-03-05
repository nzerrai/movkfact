import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  CircularProgress,
} from '@mui/material';

/**
 * DeleteRowDialog — Dialog de confirmation pour la suppression d'une ligne.
 * Pattern identique à DeleteConfirmDialog.jsx.
 */
const DeleteRowDialog = ({ open, rowIndex, onConfirm, onCancel, loading = false }) => {
  return (
    <Dialog open={open} onClose={onCancel}>
      <DialogTitle>Confirmer la suppression</DialogTitle>
      <DialogContent>
        <DialogContentText>
          Supprimer la ligne {rowIndex} ? Cette action est irréversible dans l'éditeur.
          Le reset complet du dataset reste disponible.
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel} disabled={loading}>
          Annuler
        </Button>
        <Button
          onClick={onConfirm}
          variant="contained"
          color="error"
          disabled={loading}
          startIcon={loading ? <CircularProgress size={20} /> : null}
        >
          {loading ? 'Suppression...' : 'Supprimer'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DeleteRowDialog;
