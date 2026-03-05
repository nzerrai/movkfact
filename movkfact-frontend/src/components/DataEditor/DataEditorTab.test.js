import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import DataEditorTab from './DataEditorTab';

jest.mock('../../services/dataSetService', () => ({
  getRows: jest.fn(),
  updateRow: jest.fn(),
  deleteRow: jest.fn(),
}));

// Stable enqueueSnackbar reference — prevents useCallback from recreating loadRows on every render
// Must be prefixed with "mock" for jest.mock() hoisting to allow the reference
const mockEnqueueSnackbar = jest.fn();
jest.mock('notistack', () => ({
  useSnackbar: () => ({ enqueueSnackbar: mockEnqueueSnackbar }),
  SnackbarProvider: ({ children }) => children,
}));

describe('DataEditorTab', () => {
  const defaultProps = {
    datasetId: 1,
    rowCount: 2,
    onModifiedCountChange: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    const service = require('../../services/dataSetService');
    service.getRows.mockResolvedValue({
      rows: [
        { rowIndex: 0, data: { firstName: 'Alice', email: 'alice@test.com' } },
        { rowIndex: 1, data: { firstName: 'Bob', email: 'bob@test.com' } },
      ],
      totalRows: 2,
      page: 0,
      size: 50,
    });
    service.updateRow.mockResolvedValue({ rowIndex: 0, data: { firstName: 'Charlie', email: 'alice@test.com' } });
    service.deleteRow.mockResolvedValue(undefined);
  });

  test('affiche le spinner pendant le chargement puis les données', async () => {
    render(<DataEditorTab {...defaultProps} />);
    await waitFor(() => expect(screen.getByText('Alice')).toBeInTheDocument(), { timeout: 2000 });
    expect(screen.getByText('Bob')).toBeInTheDocument();
  });

  test('appelle getRows au montage', async () => {
    const { getRows } = require('../../services/dataSetService');
    await act(async () => {
      render(<DataEditorTab {...defaultProps} />);
    });
    expect(getRows).toHaveBeenCalledWith(1, 0, 50);
  });

  test('affiche le guard Alert si rowCount > 50000', async () => {
    await act(async () => {
      render(<DataEditorTab {...defaultProps} rowCount={60000} />);
    });
    expect(screen.getByText(/50 000 lignes/i)).toBeInTheDocument();
  });

  test('ne montre PAS le guard si rowCount <= 50000', async () => {
    await act(async () => {
      render(<DataEditorTab {...defaultProps} />);
    });
    expect(screen.queryByText(/50 000 lignes/i)).not.toBeInTheDocument();
  });

  test('ouvre le DeleteRowDialog au clic sur Supprimer', async () => {
    render(<DataEditorTab {...defaultProps} />);
    const deleteButtons = await waitFor(() => screen.getAllByRole('button', { name: /Supprimer/i }), { timeout: 2000 });
    fireEvent.click(deleteButtons[0]);
    await waitFor(() => expect(screen.getByText(/Supprimer la ligne/i)).toBeInTheDocument(), { timeout: 2000 });
  });

  test('annuler le dialog ferme sans appel API', async () => {
    const { deleteRow } = require('../../services/dataSetService');
    render(<DataEditorTab {...defaultProps} />);
    const deleteButtons = await waitFor(() => screen.getAllByRole('button', { name: /Supprimer/i }), { timeout: 2000 });
    fireEvent.click(deleteButtons[0]);
    const annulerBtn = await waitFor(() => screen.getByRole('button', { name: /Annuler/i }), { timeout: 2000 });
    fireEvent.click(annulerBtn);
    expect(deleteRow).not.toHaveBeenCalled();
  });
});
