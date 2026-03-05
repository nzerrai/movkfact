import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import DomainDatasetsModal from './DomainDatasetsModal';

const theme = createTheme();

// Mock fetch
global.fetch = jest.fn();

const renderWithTheme = (component) => {
  return render(
    <BrowserRouter>
      <ThemeProvider theme={theme}>
        {component}
      </ThemeProvider>
    </BrowserRouter>
  );
};

describe('DomainDatasetsModal', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('displays header with domain name', async () => {
    global.fetch.mockResolvedValueOnce({
      json: () => Promise.resolve({ data: [] }),
    });

    renderWithTheme(
      <DomainDatasetsModal
        open={true}
        domainId="domain-1"
        domainName="Test Domain"
        onClose={jest.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/Domain:/)).toBeInTheDocument();
      const titleText = screen.getByText(/Domain:/);
      expect(titleText).toHaveTextContent('Test Domain');
    });
  });

  test('loads dataset count on open', async () => {
    global.fetch.mockResolvedValueOnce({
      json: () => Promise.resolve({ 
        data: [
          { id: 1, name: 'dataset1.csv' },
          { id: 2, name: 'dataset2.csv' }
        ] 
      }),
    });

    renderWithTheme(
      <DomainDatasetsModal
        open={true}
        domainId="domain-1"
        domainName="Test Domain"
        onClose={jest.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/2 datasets/)).toBeInTheDocument();
    });
  });

  test('displays 0 datasets when no datasets', async () => {
    global.fetch.mockResolvedValueOnce({
      json: () => Promise.resolve({ data: [] }),
    });

    renderWithTheme(
      <DomainDatasetsModal
        open={true}
        domainId="domain-1"
        domainName="Test Domain"
        onClose={jest.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/0 datasets/)).toBeInTheDocument();
    });
  });

  test('calls onClose when close button clicked', async () => {
    const mockOnClose = jest.fn();
    global.fetch.mockResolvedValueOnce({
      json: () => Promise.resolve({ data: [] }),
    });

    renderWithTheme(
      <DomainDatasetsModal
        open={true}
        domainId="domain-1"
        domainName="Test Domain"
        onClose={mockOnClose}
      />
    );

    const closeButton = screen.getByRole('button', { name: /close/i });
    await userEvent.click(closeButton);
    
    expect(mockOnClose).toHaveBeenCalled();
  });

  test('has refresh button to reload datasets', async () => {
    global.fetch.mockResolvedValueOnce({
      json: () => Promise.resolve({ data: [] }),
    });

    renderWithTheme(
      <DomainDatasetsModal
        open={true}
        domainId="domain-1"
        domainName="Test Domain"
        onClose={jest.fn()}
      />
    );

    const refreshButton = screen.getByRole('button', { name: /refresh/i });
    expect(refreshButton).toBeInTheDocument();
  });

  test('renders info alert about domain', async () => {
    global.fetch.mockResolvedValueOnce({
      json: () => Promise.resolve({ data: [] }),
    });

    renderWithTheme(
      <DomainDatasetsModal
        open={true}
        domainId="domain-1"
        domainName="Test Domain"
        onClose={jest.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/Tous les fichiers CSV uploadés/)).toBeInTheDocument();
    });
  });

  test('does not load data when closed', () => {
    global.fetch.mockResolvedValueOnce({
      json: () => Promise.resolve({ data: [] }),
    });

    renderWithTheme(
      <DomainDatasetsModal
        open={false}
        domainId="domain-1"
        domainName="Test Domain"
        onClose={jest.fn()}
      />
    );

    // fetch should not be called if modal is closed
    expect(global.fetch).not.toHaveBeenCalled();
  });

  test('does not call API if no domainId', async () => {
    renderWithTheme(
      <DomainDatasetsModal
        open={true}
        domainId={null}
        domainName="Test Domain"
        onClose={jest.fn()}
      />
    );

    await waitFor(() => {
      expect(global.fetch).not.toHaveBeenCalled();
    });
  });

  test('renders UploadedDatasetsList component', async () => {
    global.fetch.mockResolvedValueOnce({
      json: () => Promise.resolve({ data: [] }),
    });

    renderWithTheme(
      <DomainDatasetsModal
        open={true}
        domainId="domain-1"
        domainName="Test Domain"
        onClose={jest.fn()}
      />
    );

    await waitFor(() => {
      // UploadedDatasetsList should render (it's in the content)
      expect(screen.getByText(/Tous les fichiers CSV uploadés/)).toBeInTheDocument();
    });
  });
});
