import React, { useState, useCallback } from 'react';
import {
  Box,
  Paper,
  Typography,
  LinearProgress,
  IconButton,
  Collapse,
  Chip,
  Divider,
  Tooltip,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import CloseIcon from '@mui/icons-material/Close';
import NotificationsIcon from '@mui/icons-material/Notifications';
import WifiOffIcon from '@mui/icons-material/WifiOff';
import SyncIcon from '@mui/icons-material/Sync';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import { useBatchJobs } from '../../context/BatchJobsContext';

// ─── Helpers ─────────────────────────────────────────────────────────────────

function formatSeconds(seconds) {
  if (!seconds || seconds <= 0) return null;
  if (seconds < 60) return `~${Math.round(seconds)}s`;
  const m = Math.floor(seconds / 60);
  const s = Math.round(seconds % 60);
  return `~${m}m ${s}s`;
}

function formatElapsed(startedAt) {
  if (!startedAt) return null;
  const elapsedMs = Date.now() - new Date(startedAt).getTime();
  const s = Math.floor(elapsedMs / 1000);
  if (s < 60) return `${s}s`;
  return `${Math.floor(s / 60)}m ${s % 60}s`;
}

// ─── Job status color/icon ────────────────────────────────────────────────────

function getJobColor(status) {
  if (status === 'COMPLETED') return 'success';
  if (status === 'PARTIAL') return 'warning';
  if (status === 'FAILED') return 'error';
  return 'primary'; // RUNNING
}

function getProgressColor(status) {
  if (status === 'COMPLETED') return 'success';
  if (status === 'PARTIAL') return 'warning';
  if (status === 'FAILED') return 'error';
  return 'primary';
}

// ─── Single job card ──────────────────────────────────────────────────────────

function JobCard({ job, onRemove }) {
  const isDone = job.status === 'COMPLETED' || job.status === 'PARTIAL' || job.status === 'FAILED';
  const isStuck = job.status === 'RUNNING' && job.startedAt
    && (Date.now() - new Date(job.startedAt).getTime()) > 5 * 60 * 1000; // > 5 min
  const remaining = formatSeconds(job.estimatedRemainingSeconds);
  const elapsed = formatElapsed(job.startedAt);

  return (
    <Box sx={{ mb: 1.5 }}>
      {/* Header row */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 0.5 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
          {job.status === 'COMPLETED' && (
            <CheckCircleIcon sx={{ fontSize: 16, color: 'success.main' }} />
          )}
          {(job.status === 'PARTIAL' || job.status === 'FAILED') && (
            <ErrorIcon sx={{ fontSize: 16, color: 'error.main' }} />
          )}
          <Typography variant="caption" fontWeight={600}>
            Job #{job.jobId}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
          <Chip
            label={job.status}
            color={getJobColor(job.status)}
            size="small"
            sx={{ height: 18, fontSize: '0.6rem' }}
          />
          {(isDone || isStuck) && (
            <Tooltip title={isStuck && !isDone ? 'Job bloqué — forcer la fermeture' : 'Fermer'}>
              <IconButton size="small" onClick={() => onRemove(job.jobId)} sx={{ p: 0.25 }}>
                <CloseIcon sx={{ fontSize: 14 }} />
              </IconButton>
            </Tooltip>
          )}
        </Box>
      </Box>

      {/* Progress bar */}
      <LinearProgress
        variant="determinate"
        value={job.percentage || 0}
        color={getProgressColor(job.status)}
        sx={{ borderRadius: 1, height: 6, mb: 0.5 }}
      />

      {/* Stats row */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
        <Typography variant="caption" color="text.secondary">
          {job.completed ?? 0}/{job.total ?? job.dataSetCount ?? '?'} datasets
          {job.percentage != null ? ` · ${job.percentage}%` : ''}
        </Typography>
        {!isDone && remaining && (
          <Typography variant="caption" color="text.secondary">
            {remaining} restant
          </Typography>
        )}
        {isDone && elapsed && (
          <Typography variant="caption" color="text.secondary">
            {elapsed} total
          </Typography>
        )}
      </Box>

      {/* Current dataset */}
      {job.currentDataSet && !isDone && (
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.25 }}>
          ↳ {job.currentDataSet}
        </Typography>
      )}

      {/* Completion info */}
      {job.status === 'COMPLETED' && job.rowsGenerated != null && (
        <Typography variant="caption" color="success.main" sx={{ display: 'block', mt: 0.25 }}>
          ✓ {job.rowsGenerated.toLocaleString()} lignes générées en {job.duration}s
        </Typography>
      )}

      {/* Errors */}
      {job.errors && job.errors.length > 0 && (
        <Typography variant="caption" color="error.main" sx={{ display: 'block', mt: 0.25 }}>
          {job.errors.length} erreur(s)
        </Typography>
      )}
    </Box>
  );
}

// ─── Connection status badge ──────────────────────────────────────────────────

function ConnectionBadge({ status }) {
  if (status === 'CONNECTED') return null;

  const icon = status === 'RECONNECTING'
    ? <SyncIcon sx={{ fontSize: 14, animation: 'spin 1s linear infinite', '@keyframes spin': { from: { transform: 'rotate(0deg)' }, to: { transform: 'rotate(360deg)' } } }} />
    : <WifiOffIcon sx={{ fontSize: 14 }} />;

  const label = status === 'RECONNECTING' ? 'Reconnexion…' : 'Déconnecté';
  const color = status === 'RECONNECTING' ? 'warning' : 'error';

  return (
    <Chip
      icon={icon}
      label={label}
      color={color}
      size="small"
      sx={{ height: 18, fontSize: '0.6rem', ml: 0.5 }}
    />
  );
}

// ─── Main NotificationPanel ───────────────────────────────────────────────────

export default function NotificationPanel() {
  const { state, removeJob } = useBatchJobs();
  const [expanded, setExpanded] = useState(true);

  const jobs = Object.values(state.jobs);
  const activeCount = jobs.filter(j => j.status === 'RUNNING').length;
  const totalCount = jobs.length;

  const handleRemove = useCallback((jobId) => {
    removeJob(jobId);
  }, [removeJob]);

  if (totalCount === 0 && state.connectionStatus === 'DISCONNECTED') return null;
  if (totalCount === 0 && state.connectionStatus === 'CONNECTED') return null;

  return (
    <Paper
      elevation={4}
      sx={{
        position: 'fixed',
        bottom: 20,
        right: 20,
        width: 350,
        zIndex: 1300,
        borderRadius: 2,
        overflow: 'hidden',
        maxHeight: expanded ? 480 : 48,
        transition: 'max-height 0.25s ease',
      }}
    >
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          px: 1.5,
          py: 0.75,
          backgroundColor: 'primary.main',
          color: 'primary.contrastText',
          cursor: 'pointer',
          userSelect: 'none',
          minHeight: 48,
        }}
        onClick={() => setExpanded(prev => !prev)}
      >
        <NotificationsIcon sx={{ fontSize: 18, mr: 0.75 }} />
        <Box sx={{ flex: 1, display: 'flex', alignItems: 'center' }}>
          <Typography variant="body2" fontWeight={600} component="span">
            Batch Jobs
          </Typography>
          {activeCount > 0 && (
            <Chip
              label={activeCount}
              size="small"
              sx={{
                ml: 1,
                height: 18,
                fontSize: '0.65rem',
                backgroundColor: 'rgba(255,255,255,0.25)',
                color: 'inherit',
              }}
            />
          )}
        </Box>
        <ConnectionBadge status={state.connectionStatus} />
        <Tooltip title={expanded ? 'Réduire' : 'Agrandir'}>
          <IconButton size="small" sx={{ color: 'inherit', ml: 0.5 }}>
            {expanded ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
          </IconButton>
        </Tooltip>
      </Box>

      {/* Body */}
      <Collapse in={expanded}>
        <Box sx={{ px: 1.5, py: 1, maxHeight: 432, overflowY: 'auto' }}>
          {jobs.length === 0 ? (
            <Typography variant="caption" color="text.secondary">
              Aucun job actif.
            </Typography>
          ) : (
            jobs.map((job, idx) => (
              <React.Fragment key={job.jobId}>
                <JobCard job={job} onRemove={handleRemove} />
                {idx < jobs.length - 1 && <Divider sx={{ mb: 1.5 }} />}
              </React.Fragment>
            ))
          )}
        </Box>
      </Collapse>
    </Paper>
  );
}
