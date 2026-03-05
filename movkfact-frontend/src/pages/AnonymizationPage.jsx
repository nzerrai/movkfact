import React, { useState, useRef, useEffect } from 'react';
import {
  Box, Typography, Button, Stepper, Step, StepLabel,
  Paper, Checkbox, Select, MenuItem,
  FormControl, InputLabel, Alert, CircularProgress,
  Table, TableHead, TableRow, TableCell, TableBody,
  Chip, Divider, TextField,
} from '@mui/material';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import ShieldIcon from '@mui/icons-material/Shield';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { ALL_COLUMN_TYPES } from '../constants/columnTypes';
import { getDomains } from '../services/domainService';

const STEPS = ['Charger le fichier', 'Configurer les colonnes', 'Enregistré'];

/** Suggestion de type basée sur le nom de la colonne */
function guessType(columnName) {
  const lower = columnName.toLowerCase();
  if (/pr[eé]nom/.test(lower))           return 'FIRST_NAME';
  if (/\bnom\b/.test(lower))             return 'LAST_NAME';
  if (/mail|email/.test(lower))          return 'EMAIL';
  if (/t[eé]l[eé]|phone|mobile/.test(lower)) return 'PHONE';
  if (/adresse|address/.test(lower))    return 'ADDRESS';
  if (/naissance|birthdate/.test(lower)) return 'BIRTH_DATE';
  if (/iban|compte|account/.test(lower)) return 'ACCOUNT_NUMBER';
  if (/date/.test(lower))               return 'DATE';
  if (/uuid|id/.test(lower))            return 'UUID';
  if (/ip/.test(lower))                 return 'IP_ADDRESS';
  if (/montant|amount|solde/.test(lower)) return 'AMOUNT';
  if (/soci[eé]t[eé]|entreprise|company/.test(lower)) return 'COMPANY';
  if (/ville|city/.test(lower))         return 'CITY';
  if (/pays|country/.test(lower))       return 'COUNTRY';
  if (/taux|rate|pourcentage/.test(lower)) return 'PERCENTAGE';
  return 'TEXT';
}

export default function AnonymizationPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [file, setFile] = useState(null);
  const [format, setFormat] = useState('csv');
  const [columns, setColumns] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [domains, setDomains] = useState([]);
  const [domainId, setDomainId] = useState('');
  const [datasetName, setDatasetName] = useState('');
  const [savedDataset, setSavedDataset] = useState(null);
  const fileRef = useRef();

  useEffect(() => {
    getDomains().then(setDomains).catch(() => {});
  }, []);

  // ── Étape 1 : upload + inspection ────────────────────────────────────────────
  const handleFileChange = (e) => {
    const f = e.target.files[0];
    if (!f) return;
    setFile(f);
    setError(null);
    const ext = f.name.split('.').pop().toLowerCase();
    setFormat(ext === 'json' ? 'json' : 'csv');
    // Pre-fill dataset name from filename
    const baseName = f.name.replace(/\.[^.]+$/, '');
    setDatasetName(baseName + '_anonymise');
  };

  const handleInspect = async () => {
    setLoading(true);
    setError(null);
    try {
      const fd = new FormData();
      fd.append('file', file);
      fd.append('format', format);
      const res = await axios.post('/api/anonymize/inspect', fd);
      const cols = res.data.columns.map(col => ({
        columnName: col,
        columnType: guessType(col),
        anonymize: true,
      }));
      setColumns(cols);
      setStep(1);
    } catch (e) {
      setError("Impossible de lire le fichier. Vérifiez le format.");
    } finally {
      setLoading(false);
    }
  };

  // ── Étape 2 : configuration ───────────────────────────────────────────────────
  const toggleAnonymize = (idx) => {
    setColumns(prev => prev.map((c, i) =>
      i === idx ? { ...c, anonymize: !c.anonymize } : c));
  };

  const setType = (idx, type) => {
    setColumns(prev => prev.map((c, i) =>
      i === idx ? { ...c, columnType: type } : c));
  };

  // ── Étape 3 : traitement + sauvegarde dans un dataset ────────────────────────
  const handleSave = async () => {
    if (!domainId) { setError("Veuillez sélectionner un domaine."); return; }
    if (!datasetName.trim()) { setError("Veuillez saisir un nom de dataset."); return; }
    setLoading(true);
    setError(null);
    try {
      const fd = new FormData();
      fd.append('file', file);
      fd.append('format', format);
      fd.append('config', JSON.stringify(columns));
      fd.append('domainId', domainId);
      fd.append('datasetName', datasetName.trim());

      const res = await axios.post('/api/anonymize/save', fd);
      setSavedDataset(res.data);
      setStep(2);
    } catch (e) {
      const data = e.response?.data;
      const msg = typeof data === 'string' ? data
        : data?.message || data?.error || null;
      setError(msg || "Erreur lors de l'anonymisation. Vérifiez la configuration.");
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setStep(0); setFile(null); setColumns([]);
    setError(null); setSavedDataset(null);
    setDatasetName(''); setDomainId('');
    if (fileRef.current) fileRef.current.value = '';
  };

  const anonymizedCount = columns.filter(c => c.anonymize).length;

  return (
    <Box sx={{ p: 3, maxWidth: 900, mx: 'auto' }}>
      {/* En-tête */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
        <ShieldIcon color="primary" sx={{ fontSize: 32 }} />
        <Box>
          <Typography variant="h5" fontWeight={700}>Anonymisation RGPD</Typography>
          <Typography variant="body2" color="text.secondary">
            Anonymisation irréversible — le résultat est enregistré comme dataset dans un domaine
          </Typography>
        </Box>
      </Box>

      <Stepper activeStep={step} sx={{ mb: 4 }}>
        {STEPS.map(label => (
          <Step key={label}><StepLabel>{label}</StepLabel></Step>
        ))}
      </Stepper>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* ── Étape 0 : Upload ── */}
      {step === 0 && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="subtitle1" fontWeight={600} gutterBottom>
            Sélectionner un fichier CSV ou JSON
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap', mt: 2 }}>
            <Button
              variant="outlined"
              startIcon={<UploadFileIcon />}
              component="label"
            >
              Parcourir
              <input
                ref={fileRef}
                type="file"
                accept=".csv,.json"
                hidden
                onChange={handleFileChange}
              />
            </Button>
            {file && (
              <Typography variant="body2" color="text.secondary">
                {file.name} ({(file.size / 1024).toFixed(1)} Ko)
              </Typography>
            )}
          </Box>
          <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
            <Button
              variant="contained"
              disabled={!file || loading}
              onClick={handleInspect}
              startIcon={loading ? <CircularProgress size={16} color="inherit" /> : null}
            >
              Analyser les colonnes
            </Button>
          </Box>
        </Paper>
      )}

      {/* ── Étape 1 : Configuration des colonnes ── */}
      {step === 1 && (
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="subtitle1" fontWeight={600}>
              Configuration des colonnes
            </Typography>
            <Chip
              label={`${anonymizedCount} / ${columns.length} colonnes à anonymiser`}
              color={anonymizedCount > 0 ? 'primary' : 'default'}
              size="small"
            />
          </Box>

          <Alert severity="info" sx={{ mb: 2 }}>
            Cochez les colonnes à anonymiser. Le type détermine la stratégie RGPD appliquée.
            Les colonnes non cochées sont conservées telles quelles.
          </Alert>

          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox">Anonymiser</TableCell>
                <TableCell>Colonne</TableCell>
                <TableCell>Type de données</TableCell>
                <TableCell>Stratégie</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {columns.map((col, idx) => (
                <TableRow key={col.columnName} hover>
                  <TableCell padding="checkbox">
                    <Checkbox
                      checked={col.anonymize}
                      onChange={() => toggleAnonymize(idx)}
                      color="primary"
                    />
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" fontFamily="monospace">
                      {col.columnName}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <FormControl size="small" sx={{ minWidth: 180 }} disabled={!col.anonymize}>
                      <Select
                        value={col.columnType}
                        onChange={e => setType(idx, e.target.value)}
                      >
                        {ALL_COLUMN_TYPES.map(t => (
                          <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </TableCell>
                  <TableCell>
                    <Typography variant="caption" color={col.anonymize ? 'primary' : 'text.disabled'}>
                      {col.anonymize ? strategyLabel(col.columnType) : '— conservé —'}
                    </Typography>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          <Divider sx={{ my: 3 }} />

          {/* Destination dataset */}
          <Typography variant="subtitle2" fontWeight={600} sx={{ mb: 2 }}>
            Destination du dataset anonymisé
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 3 }}>
            <FormControl size="small" sx={{ minWidth: 220 }}>
              <InputLabel>Domaine</InputLabel>
              <Select
                value={domainId}
                label="Domaine"
                onChange={e => setDomainId(e.target.value)}
              >
                {domains.map(d => (
                  <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              size="small"
              label="Nom du dataset"
              value={datasetName}
              onChange={e => setDatasetName(e.target.value)}
              sx={{ minWidth: 280 }}
            />
          </Box>

          <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
            <Button onClick={() => setStep(0)}>Retour</Button>
            <Button
              variant="contained"
              color="primary"
              disabled={anonymizedCount === 0 || !domainId || !datasetName.trim() || loading}
              onClick={handleSave}
              startIcon={loading ? <CircularProgress size={16} color="inherit" /> : <ShieldIcon />}
            >
              Anonymiser et enregistrer
            </Button>
          </Box>
        </Paper>
      )}

      {/* ── Étape 2 : Terminé ── */}
      {step === 2 && savedDataset && (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <ShieldIcon color="success" sx={{ fontSize: 56, mb: 2 }} />
          <Typography variant="h6" fontWeight={700} gutterBottom>
            Dataset anonymisé enregistré
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            <strong>{savedDataset.name}</strong> — {savedDataset.rowCount} lignes, {savedDataset.columnCount} colonnes
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Aucune donnée originale n'a été conservée sur le serveur.
          </Typography>
          <Alert severity="success" sx={{ mb: 3, textAlign: 'left' }}>
            <strong>Conformité RGPD :</strong> Les transformations appliquées sont irréversibles.
            Même en cas de fuite du fichier résultant, les données d'origine ne peuvent pas être reconstituées.
          </Alert>
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap' }}>
            <Button
              variant="contained"
              startIcon={<OpenInNewIcon />}
              onClick={() => navigate(`/data-viewer/${savedDataset.id}`)}
            >
              Voir le dataset
            </Button>
            <Button variant="outlined" startIcon={<UploadFileIcon />} onClick={handleReset}>
              Anonymiser un autre fichier
            </Button>
          </Box>
        </Paper>
      )}
    </Box>
  );
}

function strategyLabel(type) {
  const map = {
    FIRST_NAME: 'Prénom synthétique',
    LAST_NAME: 'Nom synthétique',
    EMAIL: 'Email fictif régénéré',
    PHONE: 'Numéro synthétique',
    ADDRESS: 'Adresse synthétique',
    BIRTH_DATE: 'Année uniquement',
    DATE: 'Décalage ±180 jours',
    ACCOUNT_NUMBER: 'IBAN fictif régénéré',
    UUID: 'UUID v4 aléatoire',
    IP_ADDRESS: 'IP régénérée aléatoirement',
    TEXT: 'Texte aléatoire',
    AMOUNT: 'Montant aléatoire',
    INTEGER: 'Entier aléatoire',
    DECIMAL: 'Décimal aléatoire',
    PERCENTAGE: 'Pourcentage aléatoire',
    COMPANY: 'Entreprise synthétique',
    CITY: 'Ville synthétique',
    COUNTRY: 'Pays synthétique',
    ZIP_CODE: 'Code postal aléatoire',
    ENUM: 'Valeur aléatoire (liste)',
    URL: 'URL fictive régénérée',
  };
  return map[type] || 'Hash SHA-256';
}
