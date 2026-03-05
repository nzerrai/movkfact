import React, { useEffect, useState, useCallback } from 'react';
import {
  Box,
  Typography,
  List,
  ListItem,
  ListItemText,
  Chip,
  CircularProgress,
  Alert,
  Divider,
  Button,
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import HistoryIcon from '@mui/icons-material/History';
import { getDatasetActivity } from '../../services/dataSetService';

const getActionColor = (action) => {
  switch (action) {
    case 'ROW_MODIFIED': return 'warning';
    case 'ROW_DELETED':  return 'error';
    case 'CREATED':      return 'success';
    case 'VIEWED':       return 'info';
    case 'DOWNLOADED':   return 'primary';
    case 'MODIFIED':     return 'warning';
    case 'RESET':        return 'secondary';
    case 'DELETED':      return 'error';
    default:             return 'default';
  }
};

const getActionLabel = (action) => {
  switch (action) {
    case 'ROW_MODIFIED': return 'Ligne modifiée';
    case 'ROW_DELETED':  return 'Ligne supprimée';
    case 'CREATED':      return 'Créé';
    case 'VIEWED':       return 'Consulté';
    case 'DOWNLOADED':   return 'Téléchargé';
    case 'MODIFIED':     return 'Modifié';
    case 'RESET':        return 'Réinitialisé';
    case 'DELETED':      return 'Supprimé';
    default:             return action;
  }
};

const formatTimestamp = (timestamp) => {
  if (!timestamp) return '';
  return new Date(timestamp).toLocaleString('fr-FR', {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
};

/**
 * ActivityPanel — Panneau latéral d'historique des modifications avec filtre par type.
 */
const ActivityPanel = ({ datasetId, open }) => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('ALL');

  const fetchActivities = useCallback(async () => {
    if (!datasetId) return;
    setLoading(true);
    setError(null);
    try {
      const actionFilter = filter === 'ALL' ? null : filter;
      const data = await getDatasetActivity(datasetId, actionFilter, 0, 100);
      setActivities(Array.isArray(data) ? data : []);
    } catch (err) {
      setError('Impossible de charger l\'historique');
    } finally {
      setLoading(false);
    }
  }, [datasetId, filter]);

  useEffect(() => {
    if (open) fetchActivities();
  }, [open, fetchActivities]);

  if (!open) return null;

  return (
    <Box sx={{ width: 320, borderLeft: 1, borderColor: 'divider', p: 2, bgcolor: 'background.paper' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
        <HistoryIcon color="action" />
        <Typography variant="h6" sx={{ flex: 1 }}>Historique</Typography>
        <Button size="small" startIcon={<RefreshIcon />} onClick={fetchActivities}>
          Actualiser
        </Button>
      </Box>

      <ToggleButtonGroup
        value={filter}
        exclusive
        onChange={(_, val) => { if (val) setFilter(val); }}
        size="small"
        sx={{ mb: 2, flexWrap: 'wrap' }}
      >
        <ToggleButton value="ALL">Tout</ToggleButton>
        <ToggleButton value="ROW_MODIFIED">Modif.</ToggleButton>
        <ToggleButton value="ROW_DELETED">Suppr.</ToggleButton>
      </ToggleButtonGroup>

      {loading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
          <CircularProgress size={24} />
        </Box>
      )}

      {error && <Alert severity="warning">{error}</Alert>}

      {!loading && !error && activities.length === 0 && (
        <Typography variant="body2" color="textSecondary" sx={{ textAlign: 'center', py: 2 }}>
          Aucune activité
        </Typography>
      )}

      {!loading && !error && (
        <List sx={{ maxHeight: 'calc(100vh - 300px)', overflow: 'auto' }}>
          {activities.map((activity, index) => (
            <React.Fragment key={activity.id || index}>
              <ListItem sx={{ px: 0, alignItems: 'flex-start' }}>
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                      <Chip
                        label={getActionLabel(activity.action)}
                        color={getActionColor(activity.action)}
                        size="small"
                        variant="outlined"
                      />
                      {activity.rowIndex !== null && activity.rowIndex !== undefined && (
                        <Typography variant="caption" color="textSecondary">
                          Ligne {activity.rowIndex}
                        </Typography>
                      )}
                    </Box>
                  }
                  secondary={formatTimestamp(activity.timestamp)}
                />
              </ListItem>
              {index < activities.length - 1 && <Divider />}
            </React.Fragment>
          ))}
        </List>
      )}
    </Box>
  );
};

export default ActivityPanel;
