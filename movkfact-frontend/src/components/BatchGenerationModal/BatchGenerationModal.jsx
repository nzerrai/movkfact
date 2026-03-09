import React, { useState, useCallback } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Checkbox,
  Chip,
  Collapse,
  TextField,
  Button,
  CircularProgress,
  Alert,
  Box,
  Typography,
  Divider,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import { useSnackbar } from 'notistack';
import { submitBatch, getDomainColumnConfig } from '../../services/batchService';
import { getDatasetsByDomain } from '../../services/domainService';
import { useBatchJobs } from '../../context/BatchJobsContext';

const DATASET_NAME_REGEX = /^[a-zA-Z0-9_\-\s]+$/;
const NEW_DATASET_VALUE = '__new__';

/**
 * Modal de génération batch.
 * Étape 1 : sélectionner un domaine.
 * Étape 2 : choisir un dataset existant (pré-remplit le nom) ou saisir un nouveau nom.
 */
const BatchGenerationModal = ({ open, onClose, domains = [] }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { trackJob } = useBatchJobs();

  // Set of selected domain IDs
  const [selectedDomains, setSelectedDomains] = useState(new Set());
  // Map domainId → { hasConfigurations, columns, columnsCount }
  const [domainConfigs, setDomainConfigs] = useState(new Map());
  // Map domainId → Dataset[]
  const [domainDatasets, setDomainDatasets] = useState(new Map());
  // Map domainId → { selectedDatasetId: string, name: string, count: string }
  const [datasetForms, setDatasetForms] = useState(new Map());
  // Set of domain IDs currently loading
  const [loadingDomains, setLoadingDomains] = useState(new Set());
  // Global submit loading
  const [submitting, setSubmitting] = useState(false);
  // Submit error message
  const [submitError, setSubmitError] = useState(null);

  const handleClose = useCallback(() => {
    setSelectedDomains(new Set());
    setDomainConfigs(new Map());
    setDomainDatasets(new Map());
    setDatasetForms(new Map());
    setLoadingDomains(new Set());
    setSubmitting(false);
    setSubmitError(null);
    onClose();
  }, [onClose]);

  const handleDomainToggle = useCallback(async (domain) => {
    const id = domain.id;
    const next = new Set(selectedDomains);

    if (next.has(id)) {
      next.delete(id);
      setSelectedDomains(next);
      return;
    }

    next.add(id);
    setSelectedDomains(next);

    // Initialize form for this domain if not already set
    if (!datasetForms.has(id)) {
      setDatasetForms((prev) => {
        const m = new Map(prev);
        m.set(id, { selectedDatasetId: NEW_DATASET_VALUE, name: `${domain.name}-batch`, count: '100' });
        return m;
      });
    }

    // Load column config + existing datasets if not already loaded
    if (!domainConfigs.has(id)) {
      setLoadingDomains((prev) => new Set(prev).add(id));
      try {
        const [config, datasets] = await Promise.all([
          getDomainColumnConfig(id),
          getDatasetsByDomain(id),
        ]);
        setDomainConfigs((prev) => {
          const m = new Map(prev);
          m.set(id, config);
          return m;
        });
        setDomainDatasets((prev) => {
          const m = new Map(prev);
          m.set(id, datasets || []);
          return m;
        });
      } catch (_) {
        setDomainConfigs((prev) => {
          const m = new Map(prev);
          m.set(id, { hasConfigurations: false, columns: [] });
          return m;
        });
        setDomainDatasets((prev) => {
          const m = new Map(prev);
          m.set(id, []);
          return m;
        });
      } finally {
        setLoadingDomains((prev) => {
          const s = new Set(prev);
          s.delete(id);
          return s;
        });
      }
    }
  }, [selectedDomains, datasetForms, domainConfigs]);

  const handleDatasetSelect = useCallback((domainId, datasetId) => {
    setDatasetForms((prev) => {
      const m = new Map(prev);
      const current = m.get(domainId) || {};
      if (datasetId === NEW_DATASET_VALUE) {
        m.set(domainId, { ...current, selectedDatasetId: NEW_DATASET_VALUE });
      } else {
        const datasets = domainDatasets.get(domainId) || [];
        const ds = datasets.find((d) => String(d.id) === String(datasetId));
        m.set(domainId, {
          ...current,
          selectedDatasetId: datasetId,
          name: ds ? ds.name : current.name,
        });
      }
      return m;
    });
  }, [domainDatasets]);

  const handleFormChange = useCallback((domainId, field, value) => {
    setDatasetForms((prev) => {
      const m = new Map(prev);
      m.set(domainId, { ...m.get(domainId), [field]: value });
      return m;
    });
  }, []);

  const getFormError = useCallback((domainId) => {
    const form = datasetForms.get(domainId);
    if (!form) return null;
    if (!form.name || form.name.trim().length < 3) return 'Nom trop court (min 3 caractères)';
    if (form.name.trim().length > 50) return 'Nom trop long (max 50 caractères)';
    if (!DATASET_NAME_REGEX.test(form.name.trim())) return 'Caractères non autorisés';
    const count = parseInt(form.count, 10);
    if (isNaN(count) || count < 1 || count > 10000) return 'Nombre de lignes invalide (1–10000)';
    return null;
  }, [datasetForms]);

  const isSubmitDisabled = useCallback(() => {
    if (selectedDomains.size === 0) return true;
    if (submitting) return true;
    for (const id of selectedDomains) {
      const config = domainConfigs.get(id);
      if (!config || !config.hasConfigurations) return true;
      if (getFormError(id) !== null) return true;
    }
    return false;
  }, [selectedDomains, domainConfigs, submitting, getFormError]);

  const handleSubmit = useCallback(async () => {
    setSubmitError(null);
    setSubmitting(true);

    try {
      const dataSetConfigs = [];
      for (const id of selectedDomains) {
        const config = domainConfigs.get(id);
        const form = datasetForms.get(id);
        const columns = config.columns.map((col) => {
          const dto = {
            name: col.name,
            columnType: col.type,
            format: null,
            minValue: null,
            maxValue: null,
            nullable: false,
          };
          if (col.additionalConfig) {
            try { dto.constraints = JSON.parse(col.additionalConfig); } catch (_) {}
          }
          return dto;
        });
        dataSetConfigs.push({
          domainId: id,
          datasetName: form.name.trim(),
          columns,
          count: parseInt(form.count, 10),
        });
      }

      const result = await submitBatch(dataSetConfigs);
      trackJob(String(result.jobId), result.totalDatasets);
      enqueueSnackbar(
        `Batch soumis — ${result.totalDatasets} dataset(s) en cours`,
        { variant: 'info', autoHideDuration: 4000 }
      );
      handleClose();
    } catch (err) {
      const msg = err?.response?.data?.message || err.message || 'Erreur lors de la soumission du batch';
      setSubmitError(msg);
      enqueueSnackbar(`❌ ${msg}`, { variant: 'error', autoHideDuration: 5000 });
    } finally {
      setSubmitting(false);
    }
  }, [selectedDomains, domainConfigs, datasetForms, trackJob, enqueueSnackbar, handleClose]);

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>
        Génération Batch
        <Typography variant="body2" color="text.secondary">
          Sélectionnez les domaines puis le dataset à générer
        </Typography>
      </DialogTitle>

      <DialogContent dividers>
        {submitError && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {submitError}
          </Alert>
        )}

        {domains.length === 0 && (
          <Alert severity="warning">Aucun domaine disponible. Créez d'abord un domaine.</Alert>
        )}

        <List disablePadding>
          {domains.map((domain, idx) => {
            const id = domain.id;
            const isSelected = selectedDomains.has(id);
            const isLoadingConfig = loadingDomains.has(id);
            const config = domainConfigs.get(id);
            const hasConfig = config?.hasConfigurations === true;
            const datasets = domainDatasets.get(id) || [];
            const form = datasetForms.get(id);
            const formError = isSelected ? getFormError(id) : null;

            return (
              <React.Fragment key={id}>
                {idx > 0 && <Divider component="li" />}
                <ListItem
                  alignItems="flex-start"
                  sx={{ flexDirection: 'column', py: 1 }}
                  disableGutters
                >
                  {/* ── Ligne domaine ── */}
                  <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                    <ListItemIcon sx={{ minWidth: 40 }}>
                      <Checkbox
                        edge="start"
                        checked={isSelected}
                        onChange={() => handleDomainToggle(domain)}
                        disabled={config !== undefined && !hasConfig}
                        inputProps={{ 'aria-label': `select-domain-${id}` }}
                      />
                    </ListItemIcon>
                    <ListItemText
                      primary={domain.name}
                      secondary={domain.description || null}
                    />
                    {isLoadingConfig && <CircularProgress size={20} sx={{ ml: 1 }} />}
                    {!isLoadingConfig && config && hasConfig && (
                      <Chip
                        icon={<CheckCircleOutlineIcon />}
                        label={`${config.columnsCount} col.`}
                        color="success"
                        size="small"
                        variant="outlined"
                      />
                    )}
                    {!isLoadingConfig && config && !hasConfig && (
                      <Chip
                        icon={<WarningAmberIcon />}
                        label="Aucune configuration"
                        color="warning"
                        size="small"
                      />
                    )}
                  </Box>

                  {/* ── Formulaire dataset (étape 2) ── */}
                  <Collapse in={isSelected && hasConfig} timeout="auto" unmountOnExit sx={{ width: '100%', pl: 5 }}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, mt: 1, mb: 1 }}>

                      {/* Sélecteur de dataset existant */}
                      <FormControl fullWidth size="small">
                        <InputLabel>Dataset</InputLabel>
                        <Select
                          value={form?.selectedDatasetId ?? NEW_DATASET_VALUE}
                          label="Dataset"
                          onChange={(e) => handleDatasetSelect(id, e.target.value)}
                        >
                          {datasets.map((ds) => (
                            <MenuItem key={ds.id} value={String(ds.id)}>
                              {ds.name}
                              {ds.rowCount != null && (
                                <Typography variant="caption" color="text.secondary" sx={{ ml: 1 }}>
                                  ({ds.rowCount} lignes)
                                </Typography>
                              )}
                            </MenuItem>
                          ))}
                          <MenuItem value={NEW_DATASET_VALUE}>
                            <em>— Nouveau dataset —</em>
                          </MenuItem>
                        </Select>
                      </FormControl>

                      {/* Nom + nombre de lignes */}
                      <Box sx={{ display: 'flex', gap: 2 }}>
                        <TextField
                          label="Nom du dataset"
                          size="small"
                          value={form?.name ?? ''}
                          onChange={(e) => handleFormChange(id, 'name', e.target.value)}
                          error={!!formError && formError.startsWith('Nom')}
                          helperText={
                            formError && formError.startsWith('Nom')
                              ? formError
                              : form?.selectedDatasetId !== NEW_DATASET_VALUE
                                ? 'Le nom sera suffixé automatiquement si déjà utilisé'
                                : ''
                          }
                          inputProps={{ 'data-testid': `dataset-name-${id}` }}
                          sx={{ flex: 2 }}
                        />
                        <TextField
                          label="Nombre de lignes"
                          size="small"
                          type="number"
                          value={form?.count ?? '100'}
                          onChange={(e) => handleFormChange(id, 'count', e.target.value)}
                          error={!!formError && formError.startsWith('Nombre')}
                          helperText={formError && formError.startsWith('Nombre') ? formError : '1–10 000'}
                          inputProps={{ min: 1, max: 10000, 'data-testid': `dataset-count-${id}` }}
                          sx={{ flex: 1 }}
                        />
                      </Box>

                    </Box>
                  </Collapse>
                </ListItem>
              </React.Fragment>
            );
          })}
        </List>
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button onClick={handleClose} disabled={submitting}>
          Annuler
        </Button>
        <Button
          variant="contained"
          onClick={handleSubmit}
          disabled={isSubmitDisabled()}
          startIcon={submitting ? <CircularProgress size={18} color="inherit" /> : null}
          data-testid="submit-batch-button"
        >
          {submitting ? 'Envoi…' : `Lancer le batch (${selectedDomains.size})`}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default BatchGenerationModal;
