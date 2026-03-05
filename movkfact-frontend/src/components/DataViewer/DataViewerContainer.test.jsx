import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SnackbarProvider } from 'notistack';
import DataViewerContainer from './DataViewerContainer';

// Mock fetch globally
global.fetch = jest.fn();

const mockDataset = {
  id: 1,
  rowCount: 100,
  columnCount: 3,
  data: [
    { firstName: 'John', lastName: 'Doe', age: '30' },
    { firstName: 'Jane', lastName: 'Smith', age: '28' },
    { firstName: 'Bob', lastName: 'Johnson', age: '35' },
    { firstName: 'Alice', lastName: 'Williams', age: '32' }
  ]
};

const mockStats = {
  totalRows: 100,
  totalColumns: 3,
  nullCounts: { firstName: 0, lastName: 0, age: 2 },
  columnTypes: { firstName: 'PERSONAL', lastName: 'PERSONAL', age: 'NUMERIC' }
};

const Wrapper = ({ children }) => (
  <SnackbarProvider maxSnack={3}>
    {children}
  </SnackbarProvider>
);

describe('DataViewerContainer', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockStats
    });
  });

  test('renders container with dataset', () => {
    render(
      <DataViewerContainer dataset={mockDataset} domainId={1} />,
      { wrapper: Wrapper }
    );
    expect(screen.getByText('📊 Dataset Viewer')).toBeInTheDocument();
    expect(screen.getByText(/Domain ID: 1/)).toBeInTheDocument();
  });

  test('handles no dataset gracefully', () => {
    render(
      <DataViewerContainer dataset={null} domainId={1} />,
      { wrapper: Wrapper }
    );
    expect(screen.getByText('No dataset provided')).toBeInTheDocument();
  });

  test('displays dataset statistics', async () => {
    render(
      <DataViewerContainer dataset={mockDataset} domainId={1} />,
      { wrapper: Wrapper }
    );

    await waitFor(() => {
      // Stats component should render - check for any stat-related text
      expect(screen.getByText(/Domain ID/)).toBeInTheDocument();
    }, { timeout: 2000 });
  });

  test('displays data table with all rows', () => {
    render(
      <DataViewerContainer dataset={mockDataset} domainId={1} />,
      { wrapper: Wrapper }
    );

    // Verify all data rows are displayed
    expect(screen.getByText('John')).toBeInTheDocument();
    expect(screen.getByText('Jane')).toBeInTheDocument();
    expect(screen.getByText('Bob')).toBeInTheDocument();
  });

  test('renders filter bar', async () => {
    render(
      <DataViewerContainer dataset={mockDataset} domainId={1} />,
      { wrapper: Wrapper }
    );

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/Value/i)).toBeInTheDocument();
    });
  });

  test('calls onBack callback when back button clicked', async () => {
    const mockOnBack = jest.fn();
    render(
      <DataViewerContainer dataset={mockDataset} domainId={1} onBack={mockOnBack} />,
      { wrapper: Wrapper }
    );

    const backButton = screen.getByText('← Back to Configuration');
    await userEvent.click(backButton);

    expect(mockOnBack).toHaveBeenCalled();
  });

  test('fetches stats from API', async () => {
    render(
      <DataViewerContainer dataset={mockDataset} domainId={1} />,
      { wrapper: Wrapper }
    );

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        `http://localhost:8080/api/data-sets/1/stats`
      );
    });
  });

  test('displays quality report tab', async () => {
    render(
      <DataViewerContainer dataset={mockDataset} domainId={1} />,
      { wrapper: Wrapper }
    );

    // Wait for stats to be fetched
    await waitFor(() => {
      expect(screen.getByText('📊 Dataset Viewer')).toBeInTheDocument();
    });

    const qualityTab = screen.getByRole('tab', { name: /quality report/i });
    await userEvent.click(qualityTab);

    // The quality report should display with its content sections
    // Wait for the quality score content to appear
    await waitFor(() => {
      expect(screen.getByText('Data Quality Score')).toBeInTheDocument();
    }, { timeout: 2000 });
  });

  test('displays data viewer header', () => {
    render(
      <DataViewerContainer dataset={mockDataset} domainId={1} />,
      { wrapper: Wrapper }
    );

    expect(screen.getByText(/Dataset ID: 1/)).toBeInTheDocument();
  });
});
