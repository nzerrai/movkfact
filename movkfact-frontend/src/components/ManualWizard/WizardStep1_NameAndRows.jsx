import React, { useState } from 'react';
import { Box, TextField, Typography, Slider, Button } from '@mui/material';

const NAME_REGEX = /^[a-zA-Z0-9 _-]{3,50}$/;

const MARKS = [
  { value: 100, label: '100' },
  { value: 1000, label: '1k' },
  { value: 10000, label: '10k' },
  { value: 100000, label: '100k' },
];

/**
 * Étape 1 du wizard : Nom du dataset + nombre de lignes (S7.2 AC2).
 */
const WizardStep1_NameAndRows = ({ datasetName, rowCount, onNext }) => {
  const [localName, setLocalName] = useState(datasetName || '');
  const [localCount, setLocalCount] = useState(rowCount || 1000);

  const isNameValid = NAME_REGEX.test(localName);
  const isCountValid = localCount >= 1 && localCount <= 100000;
  const isValid = isNameValid && isCountValid;

  const handleNameChange = (e) => {
    setLocalName(e.target.value);
  };

  const handleCountChange = (e) => {
    const val = parseInt(e.target.value, 10);
    if (!isNaN(val)) setLocalCount(val);
  };

  return (
    <Box sx={{ pt: 1 }}>
      <TextField
        label="Nom du dataset"
        value={localName}
        onChange={handleNameChange}
        fullWidth
        sx={{ mb: 3 }}
        error={localName.length > 0 && !isNameValid}
        helperText={
          localName.length > 0 && !isNameValid
            ? 'Nom invalide : 3-50 caractères, lettres, chiffres, espaces, _ ou -'
            : 'Exemple : "Clients_Test_2026"'
        }
        inputProps={{ 'data-testid': 'dataset-name-input' }}
      />

      <TextField
        label="Nombre de lignes"
        type="number"
        value={localCount}
        onChange={handleCountChange}
        fullWidth
        sx={{ mb: 2 }}
        error={!isCountValid}
        helperText={!isCountValid ? 'Valeur entre 1 et 100 000' : ''}
        inputProps={{ min: 1, max: 100000, 'data-testid': 'row-count-input' }}
      />

      <Typography gutterBottom variant="body2" color="text.secondary">
        Sélection rapide :
      </Typography>
      <Slider
        value={localCount}
        min={1}
        max={100000}
        marks={MARKS}
        onChange={(_, val) => setLocalCount(val)}
        valueLabelDisplay="auto"
        sx={{ mb: 4 }}
      />

      <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
        <Button
          variant="contained"
          onClick={() => onNext(localName, localCount)}
          disabled={!isValid}
          data-testid="step1-next-button"
        >
          Suivant →
        </Button>
      </Box>
    </Box>
  );
};

export default WizardStep1_NameAndRows;
