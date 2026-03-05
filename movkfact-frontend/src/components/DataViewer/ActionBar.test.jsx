import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SnackbarProvider } from 'notistack';
import ActionBar from './ActionBar';

// Mock fetch and file-saver
global.fetch = jest.fn();
jest.mock('file-saver', () => ({
  saveAs: jest.fn()
}));

// Mock clipboard API
Object.assign(navigator, {
  clipboard: {
    writeText: jest.fn(() => Promise.resolve())
  }
});

const mockFilteredData = [
  { firstName: 'John', lastName: 'Doe', age: '30' },
  { firstName: 'Jane', lastName: 'Smith', age: '28' }
];

const mockAllData = Array.from({ length: 10 }, (_, i) => ({
  firstName: `User${i}`,
  lastName: `Last${i}`,
  age: String(20 + i)
}));

const mockOnQualityReportToggle = jest.fn();

const Wrapper = ({ children }) => (
  <SnackbarProvider maxSnack={3}>
    {children}
  </SnackbarProvider>
);

// Suppress React act() warnings for MUI components
const originalError = console.error;
beforeAll(() => {
  console.error = (...args) => {
    if (
      typeof args[0] === 'string' &&
      (args[0].includes('act(') || args[0].includes('ReactDOMTestUtils'))
    ) {
      return;
    }
    originalError.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
});

describe('ActionBar', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    fetch.mockClear();
    navigator.clipboard.writeText.mockClear();
  });

  test('renders action buttons', () => {
    render(
      <ActionBar
        datasetId={1}
        filteredData={mockFilteredData}
        allData={mockAllData}
        filters={{}}
        sortConfig={null}
        onQualityReportToggle={mockOnQualityReportToggle}
      />,
      { wrapper: Wrapper }
    );

    expect(screen.getByRole('button', { name: /download full dataset/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /copy.*clipboard.*json/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /data quality metrics/i })).toBeInTheDocument();
  });

  test('shows export filtered button when filters applied', () => {
    render(
      <ActionBar
        datasetId={1}
        filteredData={mockFilteredData}
        allData={mockAllData}
        filters={{ firstName: 'John' }}
        sortConfig={null}
        onQualityReportToggle={mockOnQualityReportToggle}
      />,
      { wrapper: Wrapper }
    );

    expect(screen.getByText(/export filtered/i)).toBeInTheDocument();
  });

  test('handles quality report toggle', async () => {
    render(
      <ActionBar
        datasetId={1}
        filteredData={mockFilteredData}
        allData={mockAllData}
        filters={{}}
        sortConfig={null}
        onQualityReportToggle={mockOnQualityReportToggle}
      />,
      { wrapper: Wrapper }
    );

    const reportButton = screen.getByRole('button', { name: /data quality metrics/i });
    await userEvent.click(reportButton);

    expect(mockOnQualityReportToggle).toHaveBeenCalled();
  });

  test('disables share button when no data', () => {
    render(
      <ActionBar
        datasetId={1}
        filteredData={[]}
        allData={[]}
        filters={{}}
        sortConfig={null}
        onQualityReportToggle={mockOnQualityReportToggle}
      />,
      { wrapper: Wrapper }
    );

    const shareButton = screen.getByRole('button', { name: /copy.*json/i });
    expect(shareButton).toBeDisabled();
  });
});
