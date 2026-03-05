import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import axios from 'axios';
import EndpointCard from './EndpointCard';

jest.mock('axios');

const GET_ENDPOINT = {
  method: 'GET',
  path: '/api/domains',
  description: 'Liste tous les domaines',
  entityType: 'domain',
  params: 'Aucun paramètre',
};

const POST_ENDPOINT = {
  method: 'POST',
  path: '/api/domains',
  description: 'Crée un domaine',
  entityType: 'domain',
};

const DELETE_ENDPOINT = {
  method: 'DELETE',
  path: '/api/domains/{id}',
  description: 'Supprime un domaine',
  entityType: 'domain',
};

describe('EndpointCard', () => {
  afterEach(() => jest.clearAllMocks());

  it('renders method badge, path, description', () => {
    render(<EndpointCard endpoint={GET_ENDPOINT} defaultDomainId={1} defaultDatasetId={null} />);
    expect(screen.getByText('GET')).toBeInTheDocument();
    expect(screen.getByText('/api/domains')).toBeInTheDocument();
    expect(screen.getByText('Liste tous les domaines')).toBeInTheDocument();
  });

  it('shows Essayer button for GET endpoint', () => {
    render(<EndpointCard endpoint={GET_ENDPOINT} defaultDomainId={1} defaultDatasetId={null} />);
    expect(screen.getByText('Essayer')).toBeInTheDocument();
  });

  it('shows Essayer button for POST endpoint', () => {
    render(<EndpointCard endpoint={POST_ENDPOINT} defaultDomainId={1} defaultDatasetId={null} />);
    expect(screen.getByText('Essayer')).toBeInTheDocument();
  });

  it('does not show Essayer button for DELETE endpoint', () => {
    render(<EndpointCard endpoint={DELETE_ENDPOINT} defaultDomainId={1} defaultDatasetId={null} />);
    expect(screen.queryByText('Essayer')).not.toBeInTheDocument();
  });

  it('toggles try panel on Essayer click (unmountOnExit)', () => {
    render(<EndpointCard endpoint={GET_ENDPOINT} defaultDomainId={1} defaultDatasetId={null} />);
    // Panel unmounted initially
    expect(screen.queryByText('Exécuter')).not.toBeInTheDocument();
    // Open
    fireEvent.click(screen.getByText('Essayer'));
    expect(screen.getByText('Exécuter')).toBeInTheDocument();
    // Close
    fireEvent.click(screen.getByText('Essayer'));
    expect(screen.queryByText('Exécuter')).not.toBeInTheDocument();
  });

  it('pre-fills URL with defaultDomainId for domain endpoint', () => {
    render(<EndpointCard endpoint={GET_ENDPOINT} defaultDomainId={5} defaultDatasetId={null} />);
    fireEvent.click(screen.getByText('Essayer'));
    const urlField = screen.getByDisplayValue(/localhost:8080\/api\/domains/);
    expect(urlField).toBeInTheDocument();
  });

  it('shows warning when no data available', () => {
    render(<EndpointCard endpoint={GET_ENDPOINT} defaultDomainId={null} defaultDatasetId={null} />);
    fireEvent.click(screen.getByText('Essayer'));
    expect(screen.getByText(/Aucune donnée/)).toBeInTheDocument();
  });

  it('executes request and displays response', async () => {
    axios.mockResolvedValue({ status: 200, statusText: 'OK', data: [{ id: 1 }] });
    render(<EndpointCard endpoint={GET_ENDPOINT} defaultDomainId={1} defaultDatasetId={null} />);
    fireEvent.click(screen.getByText('Essayer'));
    fireEvent.click(screen.getByText('Exécuter'));
    await waitFor(() => expect(screen.getByText(/200 OK/)).toBeInTheDocument());
    expect(screen.getByText(/\"id\": 1/)).toBeInTheDocument();
  });

  it('displays error response on failure', async () => {
    axios.mockRejectedValue(new Error('Network Error'));
    render(<EndpointCard endpoint={GET_ENDPOINT} defaultDomainId={1} defaultDatasetId={null} />);
    fireEvent.click(screen.getByText('Essayer'));
    fireEvent.click(screen.getByText('Exécuter'));
    await waitFor(() => expect(screen.getByText(/ERR/)).toBeInTheDocument());
  });
});
