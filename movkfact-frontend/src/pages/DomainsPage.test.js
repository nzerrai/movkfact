import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { SnackbarProvider } from 'notistack';
import DomainsPage from './DomainsPage';
import { DomainProvider } from '../context/DomainProvider';
import { BatchJobsProvider } from '../context/BatchJobsContext';
import * as domainService from '../services/domainService';

// Mock the domain service
jest.mock('../services/domainService');

// Mock WebSocketService to avoid real WS connections in tests
jest.mock('../services/WebSocketService', () => ({
  onConnect: null,
  onDisconnect: null,
  onReconnecting: null,
  connect: jest.fn(),
  disconnect: jest.fn(),
  subscribeToBatch: jest.fn(),
  unsubscribeFromBatch: jest.fn(),
}));

const renderDomainsPage = () => {
  return render(
    <MemoryRouter>
      <SnackbarProvider>
        <BatchJobsProvider>
          <DomainProvider>
            <DomainsPage />
          </DomainProvider>
        </BatchJobsProvider>
      </SnackbarProvider>
    </MemoryRouter>
  );
};

describe('DomainsPage Integration', () => {
  beforeEach(() => {
    jest.clearAllMocks();

    // Mock service responses
    domainService.getDomains.mockResolvedValue([
      { id: 1, name: 'Existing Domain', description: 'Existing description' },
    ]);
    domainService.createDomain.mockResolvedValue({
      id: 2,
      name: 'New Domain',
      description: 'New description',
    });
    domainService.updateDomain.mockResolvedValue({
      id: 1,
      name: 'Updated Domain',
      description: 'Updated description',
    });
    domainService.deleteDomain.mockResolvedValue(null);
  });

  it('should render without crashing', async () => {
    const { container } = renderDomainsPage();

    // Page should render without errors
    expect(container).toBeTruthy();
  });

  it('should call getDomains on mount', async () => {
    renderDomainsPage();

    await waitFor(() => {
      expect(domainService.getDomains).toHaveBeenCalled();
    });
  });

  it('should display loaded domains', async () => {
    renderDomainsPage();

    await waitFor(() => {
      expect(screen.getByText('Existing Domain')).toBeInTheDocument();
    });
  });

  it('should render search input', () => {
    renderDomainsPage();

    expect(screen.getByPlaceholderText(/search domains/i)).toBeInTheDocument();
  });

  it('should handle empty domain list', async () => {
    domainService.getDomains.mockResolvedValueOnce([]);

    renderDomainsPage();

    await waitFor(() => {
      expect(domainService.getDomains).toHaveBeenCalled();
    });
  });

  it('should render action buttons', async () => {
    renderDomainsPage();

    await waitFor(() => {
      expect(screen.getByText('Existing Domain')).toBeInTheDocument();
    });

    // Should have edit and delete buttons
    const buttons = screen.getAllByRole('button');
    expect(buttons.length).toBeGreaterThan(0);
  });
});
