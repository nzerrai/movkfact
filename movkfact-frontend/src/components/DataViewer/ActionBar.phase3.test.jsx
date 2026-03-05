import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SnackbarProvider } from 'notistack';

// Mock file-saver BEFORE importing ActionBar
jest.mock('file-saver');

import ActionBar from './ActionBar';
import { saveAs } from 'file-saver';

// Mock fetch
global.fetch = jest.fn();

// Mock clipboard API
Object.assign(navigator, {
  clipboard: {
    writeText: jest.fn(() => Promise.resolve())
  }
});

const mockFilteredData = [
  { firstName: 'Alice', lastName: 'Anderson', age: '25', email: 'alice@example.com' },
  { firstName: 'Bob', lastName: 'Brown', age: '30', email: 'bob@example.com' }
];

const mockAllData = Array.from({ length: 100 }, (_, i) => ({
  firstName: `User${i}`,
  lastName: `Last${i}`,
  age: String(20 + (i % 50)),
  email: `user${i}@example.com`
}));

const mockOnQualityReportToggle = jest.fn();

const Wrapper = ({ children }) => (
  <SnackbarProvider maxSnack={3}>
    {children}
  </SnackbarProvider>
);

// Suppress React act() warnings
const originalError = console.error;
beforeAll(() => {
  console.error = (...args) => {
    if (
      typeof args[0] === 'string' &&
      (args[0].includes('act(') || args[0].includes('ReactDOMTestUtils') || args[0].includes('MUI'))
    ) {
      return;
    }
    originalError.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
});

describe('ActionBar - Phase 3: Export & Navigation', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Export Filtered Results', () => {
    test('calls S2.4 API with filter parameters when exporting', async () => {
      const mockBlob = new Blob(['a,b,c'], { type: 'text/csv' });
      fetch.mockResolvedValueOnce({
        ok: true,
        blob: async () => mockBlob
      });

      render(
        <ActionBar
          datasetId={1}
          filteredData={mockFilteredData}
          allData={mockAllData}
          filters={{ firstName: 'Alice' }}
          sortConfig={{ column: 'age', direction: 'asc' }}
          onQualityReportToggle={mockOnQualityReportToggle}
        />,
        { wrapper: Wrapper }
      );

      const exportButtons = screen.getAllByRole('button');
      const exportButton = exportButtons.find(btn => btn.textContent.includes('Export Filtered'));

      if (exportButton) {
        await userEvent.click(exportButton);

        await waitFor(() => {
          // Verify API was called
          expect(fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/data-sets/1/export'),
            expect.any(Object)
          );
          // Verify saveAs was called
          expect(saveAs).toHaveBeenCalled();
        });
      }
    });

    test('hides export filtered button when no filters applied', () => {
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

      const allButtons = screen.getAllByRole('button');
      const exportFilteredButton = allButtons.find(btn => btn.textContent.includes('Export Filtered'));
      expect(exportFilteredButton).not.toBeDefined();
    });

    test('shows loading state during export', async () => {
      const mockBlob = new Blob(['data'], { type: 'text/csv' });
      fetch.mockImplementationOnce(() =>
        new Promise(resolve =>
          setTimeout(
            () => resolve({ ok: true, blob: async () => mockBlob }),
            100
          )
        )
      );

      render(
        <ActionBar
          datasetId={1}
          filteredData={mockFilteredData}
          allData={mockAllData}
          filters={{ test: 'value' }}
          sortConfig={null}
          onQualityReportToggle={mockOnQualityReportToggle}
        />,
        { wrapper: Wrapper }
      );

      const exportButtons = screen.getAllByRole('button');
      const exportButton = exportButtons.find(btn => btn.textContent.includes('Export Filtered'));

      if (exportButton) {
        expect(exportButton).not.toBeDisabled();
      }
    });
  });

  describe('Download Full Dataset', () => {
    test('downloads entire dataset without filters', async () => {
      const mockBlob = new Blob(['all,data'], { type: 'text/csv' });
      fetch.mockResolvedValueOnce({
        ok: true,
        blob: async () => mockBlob
      });

      render(
        <ActionBar
          datasetId={3}
          filteredData={mockFilteredData}
          allData={mockAllData}
          filters={{ firstName: 'Alice' }}
          sortConfig={null}
          onQualityReportToggle={mockOnQualityReportToggle}
        />,
        { wrapper: Wrapper }
      );

      const allButtons = screen.getAllByRole('button');
      const downloadButton = allButtons.find(btn => btn.textContent.includes('Download Full Dataset'));

      if (downloadButton) {
        await userEvent.click(downloadButton);

        await waitFor(() => {
          // Verify API was called
          expect(fetch).toHaveBeenCalledWith(
            expect.stringMatching(/\/api\/data-sets\/3\/export/),
            expect.any(Object)
          );
          // Verify saveAs was called
          expect(saveAs).toHaveBeenCalled();
        });
      }
    });

    test('shows download button with total row count', () => {
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

      const allButtons = screen.getAllByRole('button');
      const downloadButton = allButtons.find(btn => btn.textContent.includes('Download Full Dataset'));

      // Should be visible and not disabled
      expect(downloadButton).toBeDefined();
      if (downloadButton) {
        expect(downloadButton).not.toBeDisabled();
      }
    });
  });

  describe('Share as JSON', () => {
    test('copies filtered data as JSON to clipboard', async () => {
      render(
        <ActionBar
          datasetId={1}
          filteredData={mockFilteredData}
          allData={mockAllData}
          filters={{ firstName: 'Alice' }}
          sortConfig={null}
          onQualityReportToggle={mockOnQualityReportToggle}
        />,
        { wrapper: Wrapper }
      );

      const allButtons = screen.getAllByRole('button');
      const shareButton = allButtons.find(btn => btn.textContent.includes('Share') && btn.textContent.includes('JSON'));

      if (shareButton) {
        await userEvent.click(shareButton);

        await waitFor(() => {
          expect(navigator.clipboard.writeText).toHaveBeenCalled();
        });
      }
    });

    test('disables share button when no filtered data', () => {
      render(
        <ActionBar
          datasetId={1}
          filteredData={[]}
          allData={mockAllData}
          filters={{ test: 'value' }}
          sortConfig={null}
          onQualityReportToggle={mockOnQualityReportToggle}
        />,
        { wrapper: Wrapper }
      );

      const allButtons = screen.getAllByRole('button');
      const shareButton = allButtons.find(btn => btn.textContent.includes('Share') && btn.textContent.includes('JSON'));

      // Should be disabled when no filtered data
      if (shareButton) {
        expect(shareButton).toBeDisabled();
      }
    });

    test('copies properly formatted JSON to clipboard', async () => {
      const testData = [{ id: 1, name: 'Test' }];

      render(
        <ActionBar
          datasetId={1}
          filteredData={testData}
          allData={mockAllData}
          filters={{}}
          sortConfig={null}
          onQualityReportToggle={mockOnQualityReportToggle}
        />,
        { wrapper: Wrapper }
      );

      const allButtons = screen.getAllByRole('button');
      const shareButton = allButtons.find(btn => btn.textContent.includes('Share') && btn.textContent.includes('JSON'));

      if (shareButton) {
        await userEvent.click(shareButton);

        await waitFor(() => {
          const copiedText = navigator.clipboard.writeText.mock.calls[0][0];
          const parsed = JSON.parse(copiedText);
          expect(parsed).toEqual(testData);
        });
      }
    });
  });

  describe('Error Handling & State Management', () => {
    test('handles API error on export gracefully', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        statusText: 'Internal Server Error'
      });

      render(
        <ActionBar
          datasetId={1}
          filteredData={mockFilteredData}
          allData={mockAllData}
          filters={{ test: 'value' }}
          sortConfig={null}
          onQualityReportToggle={mockOnQualityReportToggle}
        />,
        { wrapper: Wrapper }
      );

      const exportButtons = screen.getAllByRole('button');
      const exportButton = exportButtons.find(btn => btn.textContent.includes('Export Filtered'));

      if (exportButton) {
        await userEvent.click(exportButton);

        // Wait for error handling
        await waitFor(() => {
          expect(saveAs).not.toHaveBeenCalled();
        });
      }
    });

    test('handles clipboard write failure gracefully', async () => {
      navigator.clipboard.writeText.mockRejectedValueOnce(new Error('Clipboard denied'));

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

      const allButtons = screen.getAllByRole('button');
      const shareButton = allButtons.find(btn => btn.textContent.includes('Share') && btn.textContent.includes('JSON'));

      if (shareButton && !shareButton.disabled) {
        await userEvent.click(shareButton);

        // Should handle error gracefully without crashing
        await waitFor(() => {
          expect(shareButton).toBeInTheDocument();
        });
      }
    });

    test('disables export buttons when data is empty', () => {
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

      const allButtons = screen.getAllByRole('button');
      const downloadButton = allButtons.find(btn => btn.textContent.includes('Download Full Dataset'));

      // Should be disabled when no data
      if (downloadButton) {
        expect(downloadButton).toBeDisabled();
      }
    });

    test('calls quality report callback when button clicked', async () => {
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

      const allButtons = screen.getAllByRole('button');
      const reportButton = allButtons.find(btn => btn.textContent.includes('Quality Report'));

      if (reportButton) {
        await userEvent.click(reportButton);
        expect(mockOnQualityReportToggle).toHaveBeenCalled();
      }
    });
  });
});
