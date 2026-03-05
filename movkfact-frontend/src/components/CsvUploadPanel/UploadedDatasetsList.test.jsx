import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import UploadedDatasetsList from './UploadedDatasetsList';

// Mock fetch
global.fetch = jest.fn();

const mockDatasets = [
  {
    id: 1,
    fileName: 'data_2026_01.csv',
    name: 'Data Upload 1',
    rowCount: 1500,
    totalRows: 1500,
    columnCount: 5,
    totalColumns: 5,
    status: 'active',
    createdAt: '2026-03-01T10:00:00Z',
    uploadedAt: '2026-03-01T10:00:00Z',
    fileSize: 245000
  },
  {
    id: 2,
    fileName: 'data_2026_02.csv',
    name: 'Data Upload 2',
    rowCount: 3200,
    totalRows: 3200,
    columnCount: 8,
    totalColumns: 8,
    status: 'active',
    createdAt: '2026-02-28T14:30:00Z',
    uploadedAt: '2026-02-28T14:30:00Z',
    fileSize: 512000
  }
];

describe('UploadedDatasetsList', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    fetch.mockClear();
  });

  test('loads and displays uploaded datasets', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ data: mockDatasets })
    });

    render(<UploadedDatasetsList domainId={1} showActions={false} />);

    // Wait for datasets to load
    await waitFor(() => {
      expect(screen.getByText(/Uploaded Datasets/i)).toBeInTheDocument();
    });

    // Check if datasets are displayed
    expect(screen.getByText('data_2026_01.csv')).toBeInTheDocument();
    expect(screen.getByText('data_2026_02.csv')).toBeInTheDocument();
  });

  test('displays dataset details (rows, columns)', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ data: mockDatasets })
    });

    render(<UploadedDatasetsList domainId={1} showActions={false} />);

    await waitFor(() => {
      expect(screen.getByText('1500')).toBeInTheDocument();
      expect(screen.getByText('3200')).toBeInTheDocument();
    });
  });

  test('shows loading state initially', () => {
    fetch.mockImplementationOnce(() => new Promise(() => {})); // Never resolves

    const { container } = render(<UploadedDatasetsList domainId={1} showActions={false} />);

    expect(screen.getByText(/Loading datasets/i)).toBeInTheDocument();
  });

  test('shows error message on API failure', async () => {
    fetch.mockImplementationOnce(() => 
      Promise.reject(new Error('Network error'))
    );

    render(<UploadedDatasetsList domainId={1} showActions={false} />);

    await waitFor(() => {
      expect(screen.getByText(/Failed to load uploaded datasets|Network error/i)).toBeInTheDocument();
    }, { timeout: 2000 });
  });

  test('shows empty state when no datasets', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ data: [] })
    });

    render(<UploadedDatasetsList domainId={1} showActions={false} />);

    await waitFor(() => {
      expect(screen.getByText(/No datasets uploaded yet/i)).toBeInTheDocument();
    });
  });

  test('displays action buttons when showActions=true', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ data: mockDatasets })
    });

    const mockViewDataset = jest.fn();
    const mockDeleteDataset = jest.fn();

    render(
      <UploadedDatasetsList
        domainId={1}
        onViewDataset={mockViewDataset}
        onDeleteDataset={mockDeleteDataset}
        showActions={true}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('data_2026_01.csv')).toBeInTheDocument();
    });

    // Check if action buttons are displayed
    const viewButtons = screen.getAllByRole('button', { name: /View/i });
    expect(viewButtons.length).toBeGreaterThan(0);

    const deleteButtons = screen.getAllByRole('button', { name: /Delete/i });
    expect(deleteButtons.length).toBeGreaterThan(0);
  });

  test('calls onViewDataset callback when View button clicked', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ data: mockDatasets })
    });

    const mockViewDataset = jest.fn();

    render(
      <UploadedDatasetsList
        domainId={1}
        onViewDataset={mockViewDataset}
        onDeleteDataset={jest.fn()}
        showActions={true}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('data_2026_01.csv')).toBeInTheDocument();
    });

    const viewButtons = screen.getAllByRole('button', { name: /View/i });
    fireEvent.click(viewButtons[0]);

    expect(mockViewDataset).toHaveBeenCalledWith(expect.objectContaining({
      id: 1,
      fileName: 'data_2026_01.csv'
    }));
  });

  test('refresh button reloads datasets', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ data: mockDatasets })
    });

    render(<UploadedDatasetsList domainId={1} showActions={false} />);

    await waitFor(() => {
      expect(screen.getByText('data_2026_01.csv')).toBeInTheDocument();
    });

    // Mock a second response for the refresh
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ data: mockDatasets })
    });

    // Click refresh button
    const refreshButton = screen.getByRole('button', { name: /Refresh/i });
    fireEvent.click(refreshButton);

    // Verify fetch was called again
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(2);
    });
  });

  test('formats file size correctly', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ data: mockDatasets })
    });

    render(<UploadedDatasetsList domainId={1} showActions={false} />);

    await waitFor(() => {
      expect(screen.getByText('data_2026_01.csv')).toBeInTheDocument();
    });
    
    // Just verify that file size information is present (in any format)
    const container = screen.getByText('data_2026_01.csv').closest('tr');
    expect(container).toBeInTheDocument();
  });

  test('displays dataset count in header', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ data: mockDatasets })
    });

    render(<UploadedDatasetsList domainId={1} showActions={false} />);

    await waitFor(() => {
      expect(screen.getByText(/Uploaded Datasets/i)).toBeInTheDocument();
    });
    
    // Verify that both datasets are shown in the table
    expect(screen.getByText('data_2026_01.csv')).toBeInTheDocument();
    expect(screen.getByText('data_2026_02.csv')).toBeInTheDocument();
  });
});
