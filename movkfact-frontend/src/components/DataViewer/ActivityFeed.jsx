import React, { useEffect, useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  List,
  ListItem,
  ListItemText,
  Chip,
  CircularProgress,
  Alert,
  Divider,
} from '@mui/material';
import HistoryIcon from '@mui/icons-material/History';
import { getDatasetActivity } from '../../services/dataSetService';

/**
 * ActivityFeed - Affiche l'historique des activités d'un dataset
 */
const ActivityFeed = ({ datasetId }) => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchActivities = async () => {
      try {
        setLoading(true);
        setError(null);
        const activityData = await getDatasetActivity(datasetId, null, 0, 10); // Dernières 10 activités
        setActivities(activityData);
      } catch (err) {
        console.error('Error loading activities:', err);
        setError('Impossible de charger l\'historique des activités');
      } finally {
        setLoading(false);
      }
    };

    if (datasetId) {
      fetchActivities();
    }
  }, [datasetId]);

  const getActionColor = (action) => {
    switch (action) {
      case 'CREATED': return 'success';
      case 'VIEWED': return 'info';
      case 'DOWNLOADED': return 'primary';
      case 'MODIFIED': return 'warning';
      case 'RESET': return 'secondary';
      case 'DELETED': return 'error';
      default: return 'default';
    }
  };

  const getActionLabel = (action) => {
    switch (action) {
      case 'CREATED': return 'Créé';
      case 'VIEWED': return 'Consulté';
      case 'DOWNLOADED': return 'Téléchargé';
      case 'MODIFIED': return 'Modifié';
      case 'RESET': return 'Réinitialisé';
      case 'DELETED': return 'Supprimé';
      default: return action;
    }
  };

  const formatTimestamp = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleString('fr-FR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <HistoryIcon color="action" />
          <Typography variant="h6">Historique des activités</Typography>
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
          <CircularProgress size={24} />
        </Box>
      </Paper>
    );
  }

  if (error) {
    return (
      <Paper sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <HistoryIcon color="action" />
          <Typography variant="h6">Historique des activités</Typography>
        </Box>
        <Alert severity="warning" sx={{ mt: 1 }}>
          {error}
        </Alert>
      </Paper>
    );
  }

  return (
    <Paper sx={{ p: 2, mb: 2 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
        <HistoryIcon color="action" />
        <Typography variant="h6">Historique des activités</Typography>
        <Chip
          label={`${activities.length} activité${activities.length > 1 ? 's' : ''}`}
          size="small"
          variant="outlined"
        />
      </Box>

      {activities.length === 0 ? (
        <Typography variant="body2" color="textSecondary" sx={{ textAlign: 'center', py: 2 }}>
          Aucune activité enregistrée pour ce dataset
        </Typography>
      ) : (
        <List sx={{ maxHeight: 300, overflow: 'auto' }}>
          {activities.map((activity, index) => (
            <React.Fragment key={activity.id || index}>
              <ListItem sx={{ px: 0 }}>
                <ListItemText
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                      <Chip
                        label={getActionLabel(activity.action)}
                        color={getActionColor(activity.action)}
                        size="small"
                        variant="outlined"
                      />
                      {activity.userName && (
                        <Typography variant="body2" color="textSecondary">
                          par {activity.userName}
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
    </Paper>
  );
};

export default ActivityFeed;