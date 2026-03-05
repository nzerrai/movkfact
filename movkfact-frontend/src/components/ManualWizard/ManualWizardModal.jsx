import React, { useState } from 'react';
import {
  Dialog, DialogTitle, DialogContent,
  Stepper, Step, StepLabel, IconButton, Box,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import WizardStep1_NameAndRows from './WizardStep1_NameAndRows';
import WizardStep2_ColumnConfig from './WizardStep2_ColumnConfig';
import WizardStep3_Preview from './WizardStep3_Preview';
import WizardStep4_Confirm from './WizardStep4_Confirm';

const STEPS = ['Nom & lignes', 'Colonnes', 'Aperçu', 'Confirmation'];

/**
 * Modal wizard 4 étapes pour la création manuelle de dataset (S7.2).
 * Centralise tout l'état du wizard.
 */
const ManualWizardModal = ({ open, domainId, onClose, onSuccess }) => {
  const [step, setStep] = useState(0);
  const [datasetName, setDatasetName] = useState('');
  const [rowCount, setRowCount] = useState(1000);
  const [columns, setColumns] = useState([]);
  const [previewRows, setPreviewRows] = useState([]);

  const handleReset = () => {
    setStep(0);
    setDatasetName('');
    setRowCount(1000);
    setColumns([]);
    setPreviewRows([]);
  };

  const handleClose = () => {
    handleReset();
    onClose();
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="lg"
      fullWidth
      data-testid="manual-wizard-modal"
    >
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        Créer un dataset — Étape {step + 1}/{STEPS.length}
        <IconButton onClick={handleClose} aria-label="Fermer" size="small">
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <Box sx={{ px: 3, pt: 1 }}>
        <Stepper activeStep={step}>
          {STEPS.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>
      </Box>

      <DialogContent>
        {step === 0 && (
          <WizardStep1_NameAndRows
            datasetName={datasetName}
            rowCount={rowCount}
            onNext={(name, count) => {
              setDatasetName(name);
              setRowCount(count);
              setStep(1);
            }}
          />
        )}
        {step === 1 && (
          <WizardStep2_ColumnConfig
            columns={columns}
            onColumnsChange={setColumns}
            onBack={() => setStep(0)}
            onPreview={(cols) => {
              setColumns(cols);
              setPreviewRows([]);
              setStep(2);
            }}
          />
        )}
        {step === 2 && (
          <WizardStep3_Preview
            columns={columns}
            previewRows={previewRows}
            onPreviewLoaded={setPreviewRows}
            onBack={() => setStep(1)}
            onConfirm={() => setStep(3)}
          />
        )}
        {step === 3 && (
          <WizardStep4_Confirm
            datasetName={datasetName}
            domainId={domainId}
            rowCount={rowCount}
            columns={columns}
            onBack={() => setStep(2)}
            onSuccess={() => {
              handleReset();
              onSuccess(datasetName, rowCount);
            }}
          />
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ManualWizardModal;
