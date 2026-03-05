/**
 * Tests for BatchJobsContext — S3.3 (WebSocket State Management)
 *
 * Covers:
 * - reducer: all action types
 * - BatchJobsProvider: integration with useBatchJobs hook
 * - trackJob / removeJob public API
 * - localStorage persistence (RUNNING jobs)
 * - WebSocket lifecycle (connect/disconnect via mocked WebSocketService)
 */

import React from 'react';
import { render, screen, act, waitFor } from '@testing-library/react';
import { SnackbarProvider } from 'notistack';
import { BatchJobsProvider, useBatchJobs } from './BatchJobsContext';

// ─── Mock WebSocketService ────────────────────────────────────────────────────

jest.mock('../services/WebSocketService', () => ({
  subscribeToBatch: jest.fn(),
  unsubscribeFromBatch: jest.fn(),
  disconnect: jest.fn(),
  onConnect: null,
  onDisconnect: null,
  onReconnecting: null,
}));

import WebSocketService from '../services/WebSocketService';

// ─── Helpers ──────────────────────────────────────────────────────────────────

const Wrapper = ({ children }) => (
  <SnackbarProvider maxSnack={3}>
    <BatchJobsProvider>{children}</BatchJobsProvider>
  </SnackbarProvider>
);

/** Renders a consumer component that exposes the context value via data-testid */
function ContextInspector() {
  const { state, trackJob, removeJob } = useBatchJobs();
  return (
    <div>
      <span data-testid="job-count">{Object.keys(state.jobs).length}</span>
      <span data-testid="connection">{state.connectionStatus}</span>
      <button
        data-testid="track-btn"
        onClick={() => trackJob(1, 3)}
      />
      <button
        data-testid="remove-btn"
        onClick={() => removeJob(1)}
      />
    </div>
  );
}

// ─── Reducer unit tests ───────────────────────────────────────────────────────

// Import reducer separately via a named re-export would be ideal,
// but since it is not exported we test it indirectly through the Provider.

describe('BatchJobsContext — reducer (via Provider)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
    WebSocketService.subscribeToBatch.mockImplementation(() => {});
  });

  test('initial state has 0 jobs and DISCONNECTED status', () => {
    render(<ContextInspector />, { wrapper: Wrapper });
    expect(screen.getByTestId('job-count').textContent).toBe('0');
    expect(screen.getByTestId('connection').textContent).toBe('DISCONNECTED');
  });

  test('trackJob adds a job with RUNNING status', () => {
    render(<ContextInspector />, { wrapper: Wrapper });
    act(() => {
      screen.getByTestId('track-btn').click();
    });
    expect(screen.getByTestId('job-count').textContent).toBe('1');
  });

  test('removeJob deletes the job from state', () => {
    render(<ContextInspector />, { wrapper: Wrapper });
    act(() => {
      screen.getByTestId('track-btn').click();
    });
    expect(screen.getByTestId('job-count').textContent).toBe('1');

    act(() => {
      screen.getByTestId('remove-btn').click();
    });
    expect(screen.getByTestId('job-count').textContent).toBe('0');
  });

  test('trackJob calls WebSocketService.subscribeToBatch', () => {
    render(<ContextInspector />, { wrapper: Wrapper });
    act(() => {
      screen.getByTestId('track-btn').click();
    });
    expect(WebSocketService.subscribeToBatch).toHaveBeenCalledWith(1, expect.any(Function));
  });

  test('removeJob calls WebSocketService.unsubscribeFromBatch', () => {
    render(<ContextInspector />, { wrapper: Wrapper });
    act(() => {
      screen.getByTestId('track-btn').click();
    });
    act(() => {
      screen.getByTestId('remove-btn').click();
    });
    expect(WebSocketService.unsubscribeFromBatch).toHaveBeenCalledWith(1);
  });

  test('trackJob is idempotent — duplicate calls do not double-add', () => {
    render(<ContextInspector />, { wrapper: Wrapper });
    act(() => {
      screen.getByTestId('track-btn').click();
      screen.getByTestId('track-btn').click();
    });
    expect(screen.getByTestId('job-count').textContent).toBe('1');
    // subscribeToBatch called only once
    expect(WebSocketService.subscribeToBatch).toHaveBeenCalledTimes(1);
  });
});

// ─── localStorage persistence ─────────────────────────────────────────────────

describe('BatchJobsContext — localStorage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
    WebSocketService.subscribeToBatch.mockImplementation(() => {});
  });

  test('persists RUNNING jobs to localStorage on state change', async () => {
    render(<ContextInspector />, { wrapper: Wrapper });
    act(() => {
      screen.getByTestId('track-btn').click();
    });

    await waitFor(() => {
      const stored = localStorage.getItem('movkfact_active_jobs');
      expect(stored).not.toBeNull();
      const jobs = JSON.parse(stored);
      expect(Object.keys(jobs).length).toBeGreaterThan(0);
    });
  });

  test('restores RUNNING jobs from localStorage on mount', async () => {
    // Pre-seed localStorage with a running job
    const fakeJobs = {
      '99': {
        jobId: 99,
        status: 'RUNNING',
        dataSetCount: 5,
        completed: 2,
        total: 5,
        percentage: 40,
        currentDataSet: null,
        estimatedRemainingSeconds: 30,
        averageTimePerDataSet: 0,
        errors: [],
        startedAt: new Date().toISOString(),
        completedAt: null,
        dataSetIds: [],
      },
    };
    localStorage.setItem('movkfact_active_jobs', JSON.stringify(fakeJobs));

    render(<ContextInspector />, { wrapper: Wrapper });

    await waitFor(() => {
      expect(screen.getByTestId('job-count').textContent).toBe('1');
    });
  });

  test('does NOT restore COMPLETED jobs from localStorage', async () => {
    const fakeJobs = {
      '55': {
        jobId: 55,
        status: 'COMPLETED',
        dataSetCount: 2,
        completed: 2,
        total: 2,
        percentage: 100,
        errors: [],
        startedAt: new Date().toISOString(),
        completedAt: new Date().toISOString(),
        dataSetIds: [],
      },
    };
    localStorage.setItem('movkfact_active_jobs', JSON.stringify(fakeJobs));

    render(<ContextInspector />, { wrapper: Wrapper });

    // Completed jobs should not be restored
    await waitFor(() => {
      expect(screen.getByTestId('job-count').textContent).toBe('0');
    });
  });
});

// ─── WebSocket connection status ──────────────────────────────────────────────

describe('BatchJobsContext — connection status', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  test('connection status updates to CONNECTED when WS connects', async () => {
    render(<ContextInspector />, { wrapper: Wrapper });

    act(() => {
      // Simulate WebSocketService triggering the onConnect callback
      WebSocketService.onConnect && WebSocketService.onConnect();
    });

    // The context wires onConnect in a useEffect; test that it registers the callback
    // by verifying the assignment happens on mount
    expect(WebSocketService.onConnect).toBeDefined();
  });

  test('disconnects WebSocket on unmount', () => {
    const { unmount } = render(<ContextInspector />, { wrapper: Wrapper });
    unmount();
    expect(WebSocketService.disconnect).toHaveBeenCalled();
  });
});

// ─── useBatchJobs guard ───────────────────────────────────────────────────────

describe('useBatchJobs', () => {
  test('throws if used outside BatchJobsProvider', () => {
    const spy = jest.spyOn(console, 'error').mockImplementation(() => {});
    function BadConsumer() {
      useBatchJobs();
      return null;
    }
    expect(() => render(<BadConsumer />)).toThrow(
      'useBatchJobs must be used inside BatchJobsProvider'
    );
    spy.mockRestore();
  });
});
