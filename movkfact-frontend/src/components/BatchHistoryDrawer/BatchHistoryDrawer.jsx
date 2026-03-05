import React from 'react';
import {
  Drawer, Box, Typography, Divider, IconButton,
  List, ListItem, Chip, Button, Tooltip,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import DeleteSweepIcon from '@mui/icons-material/DeleteSweep';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { useBatchJobs } from '../../context/BatchJobsContext';

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('fr-FR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

function StatusIcon({ status }) {
  if (status === 'COMPLETED') return <CheckCircleIcon sx={{ fontSize: 16, color: 'success.main' }} />;
  if (status === 'PARTIAL') return <WarningAmberIcon sx={{ fontSize: 16, color: 'warning.main' }} />;
  return <ErrorIcon sx={{ fontSize: 16, color: 'error.main' }} />;
}

function statusColor(status) {
  if (status === 'COMPLETED') return 'success';
  if (status === 'PARTIAL') return 'warning';
  return 'error';
}

export default function BatchHistoryDrawer({ open, onClose }) {
  const { state, clearHistory } = useBatchJobs();
  const history = state.history || [];

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      PaperProps={{ sx: { width: 380 } }}
    >
      {/* Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', px: 2, py: 1.5, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Typography variant="h6" sx={{ flex: 1 }}>
          Historique des batchs
        </Typography>
        {history.length > 0 && (
          <Tooltip title="Effacer l'historique">
            <IconButton size="small" onClick={clearHistory} sx={{ mr: 0.5 }}>
              <DeleteSweepIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        )}
        <IconButton size="small" onClick={onClose}>
          <CloseIcon fontSize="small" />
        </IconButton>
      </Box>

      {/* Body */}
      <Box sx={{ overflowY: 'auto', flex: 1 }}>
        {history.length === 0 ? (
          <Box sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Aucun job dans l'historique.
            </Typography>
          </Box>
        ) : (
          <List disablePadding>
            {history.map((job, idx) => (
              <React.Fragment key={`${job.jobId}-${idx}`}>
                <ListItem alignItems="flex-start" sx={{ flexDirection: 'column', py: 1.5, px: 2 }}>
                  {/* Row 1: ID + status */}
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, width: '100%', mb: 0.5 }}>
                    <StatusIcon status={job.status} />
                    <Typography variant="body2" fontWeight={600} sx={{ flex: 1 }}>
                      Job #{job.jobId}
                    </Typography>
                    <Chip
                      label={job.status}
                      color={statusColor(job.status)}
                      size="small"
                      sx={{ height: 20, fontSize: '0.65rem' }}
                    />
                  </Box>

                  {/* Row 2: datasets + rows */}
                  <Typography variant="caption" color="text.secondary">
                    {job.completed ?? job.dataSetCount ?? '?'}/{job.total ?? job.dataSetCount ?? '?'} datasets
                    {job.rowsGenerated != null && ` · ${job.rowsGenerated.toLocaleString()} lignes`}
                    {job.duration != null && ` · ${job.duration}s`}
                  </Typography>

                  {/* Row 3: dates */}
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                    Démarré : {formatDate(job.startedAt)}
                  </Typography>
                  {job.completedAt && (
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                      Terminé : {formatDate(job.completedAt)}
                    </Typography>
                  )}

                  {/* Errors */}
                  {job.errors && job.errors.length > 0 && (
                    <Typography variant="caption" color="error.main" sx={{ display: 'block', mt: 0.25 }}>
                      {job.errors.length} erreur(s)
                    </Typography>
                  )}
                </ListItem>
                {idx < history.length - 1 && <Divider component="li" />}
              </React.Fragment>
            ))}
          </List>
        )}
      </Box>
    </Drawer>
  );
}
