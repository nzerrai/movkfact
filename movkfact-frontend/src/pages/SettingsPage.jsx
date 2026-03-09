import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import {
  Box, Typography, Divider, Paper, List, ListItem, ListItemText,
  Tabs, Tab,
  Accordion, AccordionSummary, AccordionDetails,
  IconButton, Tooltip, Button,
  Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions,
  TextField, MenuItem, Select, FormControl, InputLabel,
  Table, TableBody, TableRow, TableCell,
  Snackbar, Alert,
  CircularProgress,
} from '@mui/material';
import SettingsIcon from '@mui/icons-material/Settings';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import RefreshIcon from '@mui/icons-material/Refresh';

const API = 'http://localhost:8080/api/settings/patterns';

// ── Onglet Général ────────────────────────────────────────────────────────────
const GeneralTab = () => (
  <Box>
    <Paper variant="outlined" sx={{ mb: 2 }}>
      <List disablePadding>
        <ListItem divider>
          <ListItemText primary="Version de l'application" secondary="movkfact v1.0.0 — Sprint 10" />
        </ListItem>
        <ListItem divider>
          <ListItemText primary="Backend" secondary="http://localhost:8080" />
        </ListItem>
        <ListItem>
          <ListItemText
            primary="Fonctionnalités à venir"
            secondary="Authentification JWT, thème sombre, gestion des utilisateurs"
          />
        </ListItem>
      </List>
    </Paper>
    <Typography variant="caption" color="text.secondary">
      Les paramètres avancés seront disponibles dans une prochaine version.
    </Typography>
  </Box>
);

// ── Dialog ajout / édition ────────────────────────────────────────────────────
const PatternDialog = ({ open, onClose, onSave, initial, columnTypes }) => {
  const isEdit = !!initial;
  const [form, setForm] = useState({ columnType: '', pattern: '', description: '' });
  const [error, setError] = useState('');

  useEffect(() => {
    if (open) {
      setForm(initial
        ? { columnType: initial.columnType, pattern: initial.pattern, description: initial.description || '' }
        : { columnType: '', pattern: '', description: '' });
      setError('');
    }
  }, [open, initial]);

  const handleSave = async () => {
    if (!form.pattern.trim()) { setError('Le pattern ne peut pas être vide'); return; }
    if (!form.columnType) { setError('Sélectionnez un type'); return; }
    setError('');
    await onSave(form, initial?.id);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Modifier le pattern' : 'Ajouter un pattern'}</DialogTitle>
      <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: '16px !important' }}>
        <FormControl fullWidth size="small">
          <InputLabel>Type de colonne</InputLabel>
          <Select
            value={form.columnType}
            label="Type de colonne"
            onChange={e => setForm(f => ({ ...f, columnType: e.target.value }))}
          >
            {columnTypes.map(t => <MenuItem key={t} value={t}>{t}</MenuItem>)}
          </Select>
        </FormControl>
        <TextField
          label="Regex (pattern)"
          value={form.pattern}
          onChange={e => setForm(f => ({ ...f, pattern: e.target.value }))}
          size="small"
          fullWidth
          placeholder="(?i)^mon_?champ$"
          inputProps={{ style: { fontFamily: 'monospace' } }}
        />
        <TextField
          label="Description (optionnelle)"
          value={form.description}
          onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
          size="small"
          fullWidth
        />
        {error && <Typography color="error" variant="caption">{error}</Typography>}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Annuler</Button>
        <Button onClick={handleSave} variant="contained">
          {isEdit ? 'Enregistrer' : 'Ajouter'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// ── Dialog confirmation suppression ──────────────────────────────────────────
const ConfirmDeleteDialog = ({ open, onClose, onConfirm, pattern }) => (
  <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
    <DialogTitle>Supprimer ce pattern ?</DialogTitle>
    <DialogContent>
      <DialogContentText>
        <code>{pattern?.pattern}</code> sera supprimé définitivement du type <strong>{pattern?.columnType}</strong>.
      </DialogContentText>
    </DialogContent>
    <DialogActions>
      <Button onClick={onClose}>Annuler</Button>
      <Button onClick={onConfirm} color="error" variant="contained">Supprimer</Button>
    </DialogActions>
  </Dialog>
);

// ── Onglet Patterns ───────────────────────────────────────────────────────────
const PatternsTab = () => {
  const [patterns, setPatterns] = useState([]);
  const [columnTypes, setColumnTypes] = useState([]);
  const [loading, setLoading] = useState(false);

  // Dialogs
  const [addOpen, setAddOpen] = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);

  // Snackbar
  const [snack, setSnack] = useState({ open: false, message: '', severity: 'success' });
  const notify = (message, severity = 'success') => setSnack({ open: true, message, severity });

  const loadPatterns = useCallback(async () => {
    setLoading(true);
    try {
      const [pRes, tRes] = await Promise.all([
        axios.get(API),
        axios.get(`${API}/types`),
      ]);
      setPatterns(pRes.data);
      setColumnTypes(tRes.data);
    } catch {
      notify('Erreur de chargement des patterns', 'error');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadPatterns(); }, [loadPatterns]);

  // Grouper par columnType
  const grouped = patterns.reduce((acc, p) => {
    if (!acc[p.columnType]) acc[p.columnType] = [];
    acc[p.columnType].push(p);
    return acc;
  }, {});
  const sortedTypes = Object.keys(grouped).sort();

  const handleSave = async (form, id) => {
    try {
      if (id) {
        await axios.put(`${API}/${id}`, form);
        notify('Pattern modifié');
      } else {
        await axios.post(API, form);
        notify('Pattern ajouté');
      }
      setAddOpen(false);
      setEditTarget(null);
      loadPatterns();
    } catch (err) {
      const msg = err.response?.data?.error || 'Erreur lors de la sauvegarde';
      notify(msg, 'error');
    }
  };

  const handleDelete = async () => {
    try {
      await axios.delete(`${API}/${deleteTarget.id}`);
      notify('Pattern supprimé');
      setDeleteTarget(null);
      loadPatterns();
    } catch {
      notify('Erreur lors de la suppression', 'error');
    }
  };

  const handleReload = async () => {
    try {
      const res = await axios.post(`${API}/reload`);
      notify(`Cache rechargé — ${res.data.types} types actifs`);
    } catch {
      notify('Erreur lors du rechargement', 'error');
    }
  };

  return (
    <Box>
      {/* Barre d'actions */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="body2" color="text.secondary">
          {patterns.length} pattern{patterns.length > 1 ? 's' : ''} — {sortedTypes.length} types
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            startIcon={<RefreshIcon />}
            variant="outlined"
            size="small"
            onClick={handleReload}
          >
            Recharger le cache
          </Button>
          <Button
            startIcon={<AddIcon />}
            variant="contained"
            size="small"
            onClick={() => setAddOpen(true)}
          >
            Ajouter un pattern
          </Button>
        </Box>
      </Box>

      {loading && <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}><CircularProgress size={32} /></Box>}

      {/* Accordéons par type */}
      {!loading && sortedTypes.map(type => (
        <Accordion key={type} disableGutters elevation={0} sx={{ border: '1px solid', borderColor: 'divider', mb: 0.5 }}>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, width: '100%' }}>
              <Typography variant="subtitle2" sx={{ fontFamily: 'monospace', minWidth: 160 }}>
                {type}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {grouped[type].length} pattern{grouped[type].length > 1 ? 's' : ''}
              </Typography>
              <Box sx={{ ml: 'auto' }}>
                <Tooltip title="Ajouter un pattern pour ce type">
                  <IconButton
                    size="small"
                    onClick={e => { e.stopPropagation(); setEditTarget(null); setAddOpen({ forType: type }); }}
                  >
                    <AddIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </Box>
            </Box>
          </AccordionSummary>
          <AccordionDetails sx={{ p: 0 }}>
            <Table size="small">
              <TableBody>
                {grouped[type].map(p => (
                  <TableRow key={p.id} hover>
                    <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.78rem', pl: 3 }}>
                      {p.pattern}
                    </TableCell>
                    <TableCell sx={{ color: 'text.secondary', fontSize: '0.75rem' }}>
                      {p.description || '—'}
                    </TableCell>
                    <TableCell align="right" sx={{ pr: 1, whiteSpace: 'nowrap' }}>
                      <Tooltip title="Modifier">
                        <IconButton size="small" onClick={() => setEditTarget(p)}>
                          <EditIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Supprimer">
                        <IconButton size="small" color="error" onClick={() => setDeleteTarget(p)}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </AccordionDetails>
        </Accordion>
      ))}

      {/* Dialog ajout */}
      <PatternDialog
        open={!!addOpen}
        onClose={() => setAddOpen(false)}
        onSave={handleSave}
        initial={addOpen?.forType ? { columnType: addOpen.forType, pattern: '', description: '' } : null}
        columnTypes={columnTypes}
      />

      {/* Dialog édition */}
      <PatternDialog
        open={!!editTarget}
        onClose={() => setEditTarget(null)}
        onSave={handleSave}
        initial={editTarget}
        columnTypes={columnTypes}
      />

      {/* Dialog suppression */}
      <ConfirmDeleteDialog
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        pattern={deleteTarget}
      />

      {/* Snackbar */}
      <Snackbar
        open={snack.open}
        autoHideDuration={3500}
        onClose={() => setSnack(s => ({ ...s, open: false }))}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity={snack.severity} onClose={() => setSnack(s => ({ ...s, open: false }))}>
          {snack.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

// ── Page principale Settings ──────────────────────────────────────────────────
const SettingsPage = () => {
  const [tab, setTab] = useState(0);

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
        <SettingsIcon color="action" />
        <Typography variant="h5">Settings</Typography>
      </Box>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label="Général" />
        <Tab label="Patterns de détection" />
      </Tabs>
      <Divider sx={{ mb: 3 }} />

      {tab === 0 && <GeneralTab />}
      {tab === 1 && <PatternsTab />}
    </Box>
  );
};

export default SettingsPage;
