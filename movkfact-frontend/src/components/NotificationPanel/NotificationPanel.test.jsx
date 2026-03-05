/**
 * Tests for NotificationPanel component — S3.3
 *
 * Covers:
 * - Hidden when no jobs exist
 * - Renders header with job count badge
 * - Renders job cards with progress bar
 * - Shows estimated remaining time for RUNNING jobs
 * - Shows rows generated for COMPLETED jobs
 * - Shows error count for jobs with errors
 * - Collapse / expand on header click
 * - Remove button appears for completed jobs
 * - Connection status badge for RECONNECTING / DISCONNECTED
 */

import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { SnackbarProvider } from 'notistack';
import NotificationPanel from './NotificationPanel';
import * as BatchJobsContextModule from '../../context/BatchJobsContext';

// ─── Mock useBatchJobs ────────────────────────────────────────────────────────

jest.mock('../../context/BatchJobsContext', () => ({
  ...jest.requireActual('../../context/BatchJobsContext'),
  useBatchJobs: jest.fn(),
}));

const mockRemoveJob = jest.fn();

function renderWithState(jobs = {}, connectionStatus = 'CONNECTED') {
  BatchJobsContextModule.useBatchJobs.mockReturnValue({
    state: { jobs, connectionStatus },
    removeJob: mockRemoveJob,
  });
  return render(
    <SnackbarProvider>
      <NotificationPanel />
    </SnackbarProvider>
  );
}

const RUNNING_JOB = {
  jobId: 1,
  status: 'RUNNING',
  dataSetCount: 5,
  completed: 2,
  total: 5,
  percentage: 40,
  currentDataSet: 'Dataset #2',
  estimatedRemainingSeconds: 30,
  averageTimePerDataSet: 3.5,
  errors: [],
  startedAt: new Date().toISOString(),
  completedAt: null,
  dataSetIds: [],
};

const COMPLETED_JOB = {
  ...RUNNING_JOB,
  jobId: 2,
  status: 'COMPLETED',
  completed: 5,
  percentage: 100,
  estimatedRemainingSeconds: 0,
  rowsGenerated: 500,
  duration: 15,
  completedAt: new Date().toISOString(),
};

const ERROR_JOB = {
  ...RUNNING_JOB,
  jobId: 3,
  status: 'PARTIAL',
  errors: [
    { message: 'Dataset #3 failed', affectedDataSet: 'Dataset #3', retryAttempts: 3 },
  ],
};

// ─── Tests ────────────────────────────────────────────────────────────────────

describe('NotificationPanel', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders nothing when no jobs and CONNECTED', () => {
    const { container } = renderWithState({}, 'CONNECTED');
    expect(container.firstChild).toBeNull();
  });

  test('renders nothing when no jobs and DISCONNECTED', () => {
    const { container } = renderWithState({}, 'DISCONNECTED');
    expect(container.firstChild).toBeNull();
  });

  test('renders panel when there are active jobs', () => {
    renderWithState({ 1: RUNNING_JOB });
    expect(screen.getByText('Batch Jobs')).toBeInTheDocument();
  });

  test('shows job count badge with active running jobs', () => {
    renderWithState({ 1: RUNNING_JOB });
    // The badge "1" for 1 active job
    expect(screen.getByText('1')).toBeInTheDocument();
  });

  test('renders job ID in each card', () => {
    renderWithState({ 1: RUNNING_JOB });
    expect(screen.getByText(/Job #1/)).toBeInTheDocument();
  });

  test('shows RUNNING status chip', () => {
    renderWithState({ 1: RUNNING_JOB });
    expect(screen.getByText('RUNNING')).toBeInTheDocument();
  });

  test('shows progress percentage', () => {
    renderWithState({ 1: RUNNING_JOB });
    expect(screen.getByText(/40%/)).toBeInTheDocument();
  });

  test('shows completed/total datasets', () => {
    renderWithState({ 1: RUNNING_JOB });
    expect(screen.getByText(/2\/5/)).toBeInTheDocument();
  });

  test('shows current dataset name for RUNNING job', () => {
    renderWithState({ 1: RUNNING_JOB });
    expect(screen.getByText(/Dataset #2/)).toBeInTheDocument();
  });

  test('shows estimated remaining time for RUNNING job', () => {
    renderWithState({ 1: RUNNING_JOB });
    expect(screen.getByText(/30s restant/)).toBeInTheDocument();
  });

  test('shows rows generated for COMPLETED job', () => {
    renderWithState({ 2: COMPLETED_JOB });
    expect(screen.getByText(/500/)).toBeInTheDocument();
    expect(screen.getByText(/lignes générées/)).toBeInTheDocument();
  });

  test('shows COMPLETED status chip', () => {
    renderWithState({ 2: COMPLETED_JOB });
    expect(screen.getByText('COMPLETED')).toBeInTheDocument();
  });

  test('shows error count for job with errors', () => {
    renderWithState({ 3: ERROR_JOB });
    expect(screen.getByText(/1 erreur/)).toBeInTheDocument();
  });

  test('shows remove button for completed jobs', () => {
    renderWithState({ 2: COMPLETED_JOB });
    // Close icon button present
    const closeButtons = screen.getAllByRole('button');
    expect(closeButtons.length).toBeGreaterThan(1); // header toggle + close
  });

  test('remove button calls removeJob', () => {
    renderWithState({ 2: COMPLETED_JOB });
    // Find close button (last small button)
    const buttons = screen.getAllByRole('button');
    const closeBtn = buttons[buttons.length - 1];
    fireEvent.click(closeBtn);
    expect(mockRemoveJob).toHaveBeenCalledWith(2);
  });

  test('clicking header collapses the panel', () => {
    renderWithState({ 1: RUNNING_JOB });
    const header = screen.getByText('Batch Jobs').closest('[style], [class]');
    // After first click the panel collapses (job info disappears)
    fireEvent.click(header || screen.getByText('Batch Jobs'));
    // After collapse, dataset text should not be visible
    expect(screen.queryByText('Dataset #2')).not.toBeInTheDocument();
  });

  test('shows Reconnecting badge when connection is RECONNECTING', () => {
    renderWithState({ 1: RUNNING_JOB }, 'RECONNECTING');
    expect(screen.getByText(/Reconnexion/)).toBeInTheDocument();
  });

  test('shows Déconnecté badge when connection is DISCONNECTED with jobs', () => {
    renderWithState({ 1: RUNNING_JOB }, 'DISCONNECTED');
    expect(screen.getByText(/Déconnecté/)).toBeInTheDocument();
  });

  test('renders multiple jobs', () => {
    renderWithState({ 1: RUNNING_JOB, 2: COMPLETED_JOB });
    expect(screen.getByText(/Job #1/)).toBeInTheDocument();
    expect(screen.getByText(/Job #2/)).toBeInTheDocument();
  });
});
