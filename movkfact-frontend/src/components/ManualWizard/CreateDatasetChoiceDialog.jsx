import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import AutoFixHighIcon from '@mui/icons-material/AutoFixHigh';

/**
 * Dialog proposant deux modes de création de dataset : Upload CSV ou Création manuelle (S7.2 AC1).
 */
const CreateDatasetChoiceDialog = ({ open, onChooseCSV, onChooseManual, onClose }) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Créer un dataset</DialogTitle>
      <DialogContent>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Choisissez votre méthode de création :
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            fullWidth
            startIcon={<CloudUploadIcon />}
            onClick={onChooseCSV}
            sx={{ py: 3, flexDirection: 'column', gap: 1 }}
            data-testid="choice-csv-button"
          >
            <span>Upload CSV</span>
            <Typography variant="caption" color="text.secondary">
              Importer depuis un fichier CSV existant
            </Typography>
          </Button>
          <Button
            variant="contained"
            fullWidth
            startIcon={<AutoFixHighIcon />}
            onClick={onChooseManual}
            sx={{ py: 3, flexDirection: 'column', gap: 1 }}
            data-testid="choice-manual-button"
          >
            <span>Création manuelle</span>
            <Typography variant="caption" sx={{ color: 'inherit', opacity: 0.8 }}>
              Configurer les colonnes manuellement
            </Typography>
          </Button>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Annuler</Button>
      </DialogActions>
    </Dialog>
  );
};

export default CreateDatasetChoiceDialog;
