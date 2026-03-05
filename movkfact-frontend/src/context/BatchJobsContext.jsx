import React, { createContext, useContext, useCallback, useReducer, useEffect, useRef } from 'react';
import { useSnackbar } from 'notistack';
import WebSocketService from '../services/WebSocketService';
import { getJobStatus } from '../services/batchService';

const STORAGE_KEY = 'movkfact_active_jobs';

// ─── State shape ─────────────────────────────────────────────────────────────
// jobs: { [jobId]: { jobId, status, dataSetCount, completed, total, percentage,
//                    currentDataSet, estimatedRemainingSeconds, averageTimePerDataSet,
//                    errors, startedAt, completedAt, dataSetIds } }

const HISTORY_KEY = 'movkfact_job_history';

const initialState = {
  jobs: {},
  history: [],              // completed/failed jobs, persisted across sessions
  connectionStatus: 'DISCONNECTED', // DISCONNECTED | CONNECTING | CONNECTED | RECONNECTING
};

function reducer(state, action) {
  switch (action.type) {
    case 'JOB_STARTED':
      return {
        ...state,
        jobs: {
          ...state.jobs,
          [action.payload.jobId]: {
            jobId: action.payload.jobId,
            status: 'RUNNING',
            dataSetCount: action.payload.dataSetCount,
            completed: 0,
            total: action.payload.dataSetCount,
            percentage: 0,
            currentDataSet: null,
            estimatedRemainingSeconds: action.payload.estimatedDuration,
            averageTimePerDataSet: 0,
            errors: [],
            startedAt: action.payload.timestamp,
            completedAt: null,
            dataSetIds: [],
          },
        },
      };

    case 'JOB_PROGRESS':
      return {
        ...state,
        jobs: {
          ...state.jobs,
          [action.payload.jobId]: {
            ...state.jobs[action.payload.jobId],
            completed: action.payload.completed,
            total: action.payload.total,
            percentage: action.payload.percentage,
            currentDataSet: action.payload.currentDataSet,
            estimatedRemainingSeconds: action.payload.estimatedRemainingSeconds,
            averageTimePerDataSet: action.payload.averageTimePerDataSet,
            status: 'RUNNING',
          },
        },
      };

    case 'JOB_COMPLETED':
      return {
        ...state,
        jobs: {
          ...state.jobs,
          [action.payload.jobId]: {
            ...state.jobs[action.payload.jobId],
            status: action.payload.status === 'SUCCESS' ? 'COMPLETED' : 'PARTIAL',
            percentage: 100,
            completedAt: action.payload.timestamp,
            dataSetIds: action.payload.dataSetIds || [],
            rowsGenerated: action.payload.rowsGenerated,
            duration: action.payload.duration,
          },
        },
      };

    case 'JOB_ERROR':
      return {
        ...state,
        jobs: {
          ...state.jobs,
          [action.payload.jobId]: {
            ...state.jobs[action.payload.jobId],
            errors: [
              ...(state.jobs[action.payload.jobId]?.errors || []),
              {
                message: action.payload.errorMessage,
                affectedDataSet: action.payload.affectedDataSet,
                retryAttempts: action.payload.retryAttempts,
                timestamp: action.payload.timestamp,
              },
            ],
          },
        },
      };

    case 'CONNECTION_STATUS':
      return { ...state, connectionStatus: action.status };

    case 'RESTORE_JOBS':
      return { ...state, jobs: { ...action.jobs, ...state.jobs } };

    case 'MOVE_TO_HISTORY': {
      const job = state.jobs[action.jobId];
      const { [action.jobId]: _, ...remaining } = state.jobs;
      const historyEntry = job ? { ...job, dismissedAt: new Date().toISOString() } : null;
      return {
        ...state,
        jobs: remaining,
        history: historyEntry
          ? [historyEntry, ...state.history].slice(0, 50) // keep last 50
          : state.history,
      };
    }

    case 'RESTORE_HISTORY':
      return { ...state, history: action.history };

    case 'CLEAR_HISTORY':
      return { ...state, history: [] };

    default:
      return state;
  }
}

// ─── Context ─────────────────────────────────────────────────────────────────

const BatchJobsContext = createContext(null);

export function BatchJobsProvider({ children }) {
  const [state, dispatch] = useReducer(reducer, initialState);
  const { enqueueSnackbar } = useSnackbar();
  const activeJobIds = useRef(new Set());

  // Restore active jobs from localStorage on mount, then verify their real status against the backend
  useEffect(() => {
    let runningIds = [];
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const jobs = JSON.parse(stored);
        const runningJobs = Object.fromEntries(
          Object.entries(jobs).filter(([, job]) => job.status === 'RUNNING')
        );
        runningIds = Object.keys(runningJobs);
        if (runningIds.length > 0) {
          dispatch({ type: 'RESTORE_JOBS', jobs: runningJobs });
        }
      }
    } catch (_) {}

    // Verify each restored RUNNING job against the backend to detect stuck jobs
    const TERMINAL_STATUSES = ['COMPLETED', 'FAILED', 'STOPPED', 'ABANDONED'];
    runningIds.forEach(async (jobId) => {
      try {
        const status = await getJobStatus(jobId);
        if (TERMINAL_STATUSES.includes(status.status)) {
          dispatch({
            type: 'JOB_COMPLETED',
            payload: {
              jobId,
              status: status.status === 'COMPLETED' ? 'SUCCESS' : 'FAILED',
              timestamp: new Date().toISOString(),
              dataSetIds: [],
              rowsGenerated: status.completedCount,
              duration: null,
            },
          });
          activeJobIds.current.delete(jobId);
          WebSocketService.unsubscribeFromBatch(jobId);
        }
      } catch (_) {
        // Backend unreachable — leave as RUNNING, user can force-dismiss
      }
    });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // Restore history from localStorage on mount
  useEffect(() => {
    try {
      const stored = localStorage.getItem(HISTORY_KEY);
      if (stored) {
        const history = JSON.parse(stored);
        if (Array.isArray(history) && history.length > 0) {
          dispatch({ type: 'RESTORE_HISTORY', history });
        }
      }
    } catch (_) {}
  }, []);

  // Persist active jobs to localStorage whenever state changes
  useEffect(() => {
    const running = Object.fromEntries(
      Object.entries(state.jobs).filter(([, job]) => job.status === 'RUNNING')
    );
    localStorage.setItem(STORAGE_KEY, JSON.stringify(running));
  }, [state.jobs]);

  // Persist history to localStorage whenever it changes
  useEffect(() => {
    localStorage.setItem(HISTORY_KEY, JSON.stringify(state.history));
  }, [state.history]);

  // WebSocket connection lifecycle
  useEffect(() => {
    WebSocketService.onConnect = () =>
      dispatch({ type: 'CONNECTION_STATUS', status: 'CONNECTED' });

    WebSocketService.onDisconnect = () =>
      dispatch({ type: 'CONNECTION_STATUS', status: 'DISCONNECTED' });

    WebSocketService.onReconnecting = (attempt) =>
      dispatch({ type: 'CONNECTION_STATUS', status: 'RECONNECTING' });

    return () => {
      WebSocketService.disconnect();
    };
  }, []);

  const handleMessage = useCallback((jobId, message) => {
    const { type } = message;

    switch (type) {
      case 'job_started':
        dispatch({ type: 'JOB_STARTED', payload: message });
        enqueueSnackbar(
          `Batch démarré avec ${message.dataSetCount} dataset(s)`,
          { variant: 'info', autoHideDuration: 4000 }
        );
        break;

      case 'job_progress_update':
        dispatch({ type: 'JOB_PROGRESS', payload: message });
        break;

      case 'job_completed':
        dispatch({ type: 'JOB_COMPLETED', payload: message });
        activeJobIds.current.delete(String(jobId));
        WebSocketService.unsubscribeFromBatch(String(jobId));
        if (message.status === 'SUCCESS') {
          enqueueSnackbar(
            `✅ Batch terminé ! ${message.rowsGenerated} lignes générées en ${message.duration}s`,
            { variant: 'success', autoHideDuration: 6000 }
          );
        } else {
          enqueueSnackbar(
            `⚠️ Batch partiellement terminé (${message.skippedCount} erreur(s))`,
            { variant: 'warning', autoHideDuration: 6000 }
          );
        }
        break;

      case 'job_error':
        dispatch({ type: 'JOB_ERROR', payload: message });
        enqueueSnackbar(
          `❌ Erreur dataset ${message.affectedDataSet}: ${message.errorMessage} (tentative ${message.retryAttempts}/3)`,
          { variant: 'error', autoHideDuration: 5000 }
        );
        break;

      default:
        break;
    }
  }, [enqueueSnackbar]);

  // Subscribe to new jobs as they appear in state.
  // handleMessage must be defined above before this useEffect.
  useEffect(() => {
    Object.keys(state.jobs).forEach((jobId) => {
      if (!activeJobIds.current.has(jobId)) {
        activeJobIds.current.add(jobId);
        WebSocketService.subscribeToBatch(jobId, (message) => handleMessage(jobId, message));
      }
    });
  }, [state.jobs, handleMessage]); // eslint-disable-line react-hooks/exhaustive-deps

  /**
   * Called by BatchGenerationPage after a job is submitted.
   * jobId is normalized to String throughout to avoid Long/String type mixing
   * between the backend (Long) and frontend state/subscriptions.
   */
  const trackJob = useCallback((jobId, totalDatasets) => {
    const jobIdStr = String(jobId);
    if (activeJobIds.current.has(jobIdStr)) return;

    activeJobIds.current.add(jobIdStr);
    dispatch({
      type: 'JOB_STARTED',
      payload: {
        jobId: jobIdStr,
        dataSetCount: totalDatasets,
        estimatedDuration: totalDatasets * 5,
        timestamp: new Date().toISOString(),
      },
    });

    WebSocketService.subscribeToBatch(jobIdStr, (message) => handleMessage(jobIdStr, message));

    enqueueSnackbar(
      `Batch job #${jobIdStr} soumis — ${totalDatasets} dataset(s) en cours`,
      { variant: 'info', autoHideDuration: 3000 }
    );
  }, [handleMessage, enqueueSnackbar]);

  const removeJob = useCallback((jobId) => {
    const jobIdStr = String(jobId);
    dispatch({ type: 'MOVE_TO_HISTORY', jobId: jobIdStr });
    WebSocketService.unsubscribeFromBatch(jobIdStr);
    activeJobIds.current.delete(jobIdStr);
  }, []);

  const clearHistory = useCallback(() => {
    dispatch({ type: 'CLEAR_HISTORY' });
  }, []);

  return (
    <BatchJobsContext.Provider value={{ state, trackJob, removeJob, clearHistory }}>
      {children}
    </BatchJobsContext.Provider>
  );
}

export function useBatchJobs() {
  const ctx = useContext(BatchJobsContext);
  if (!ctx) throw new Error('useBatchJobs must be used inside BatchJobsProvider');
  return ctx;
}
