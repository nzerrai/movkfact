import React from 'react';
import { render, screen, fireEvent, act, waitFor } from '@testing-library/react';
import ActivityPanel from './ActivityPanel';

jest.mock('../../services/dataSetService', () => ({
  getDatasetActivity: jest.fn(),
}));

describe('ActivityPanel', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    const { getDatasetActivity } = require('../../services/dataSetService');
    getDatasetActivity.mockResolvedValue([
      { id: 1, action: 'ROW_MODIFIED', rowIndex: 2, timestamp: '2026-03-04T10:00:00' },
      { id: 2, action: 'ROW_DELETED', rowIndex: 5, timestamp: '2026-03-04T11:00:00' },
    ]);
  });

  test('ne rend rien quand open=false', () => {
    const { container } = render(<ActivityPanel datasetId={1} open={false} />);
    expect(container.firstChild).toBeNull();
  });

  test('affiche les activités chargées', async () => {
    render(<ActivityPanel datasetId={1} open={true} />);
    await waitFor(() => expect(screen.getByText(/Ligne modifiée/i)).toBeInTheDocument(), { timeout: 2000 });
    expect(screen.getByText(/Ligne supprimée/i)).toBeInTheDocument();
  });

  test('affiche le rowIndex pour les activités row-level', async () => {
    render(<ActivityPanel datasetId={1} open={true} />);
    await waitFor(() => expect(screen.getByText(/Ligne 2/i)).toBeInTheDocument(), { timeout: 2000 });
    expect(screen.getByText(/Ligne 5/i)).toBeInTheDocument();
  });

  test('bouton Actualiser déclenche un re-fetch', async () => {
    const { getDatasetActivity } = require('../../services/dataSetService');
    await act(async () => {
      render(<ActivityPanel datasetId={1} open={true} />);
    });
    await act(async () => {
      fireEvent.click(screen.getByText(/Actualiser/i));
    });
    expect(getDatasetActivity).toHaveBeenCalledTimes(2);
  });

  test('filtre ROW_MODIFIED appelle l\'API avec le bon filtre', async () => {
    const { getDatasetActivity } = require('../../services/dataSetService');
    await act(async () => {
      render(<ActivityPanel datasetId={1} open={true} />);
    });
    await act(async () => {
      fireEvent.click(screen.getByText(/Modif\./i));
    });
    expect(getDatasetActivity).toHaveBeenCalledWith(1, 'ROW_MODIFIED', 0, 100);
  });
});
