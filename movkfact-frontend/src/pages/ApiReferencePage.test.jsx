import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import axios from 'axios';
import ApiReferencePage from './ApiReferencePage';

jest.mock('axios');
jest.mock('../components/ApiReference/CrudTab', () => () => <div data-testid="crud-tab">CrudTab</div>);
jest.mock('../components/ApiReference/ExtractionTab', () => () => <div data-testid="extraction-tab">ExtractionTab</div>);
jest.mock('../components/ApiReference/GuideTab', () => () => <div data-testid="guide-tab">GuideTab</div>);

beforeEach(() => {
  axios.get.mockImplementation((url) => {
    if (url === '/api/domains') return Promise.resolve({ data: [{ id: 1, name: 'TestDomain' }] });
    if (url === '/api/domains/1/data-sets') return Promise.resolve({ data: [{ id: 42, name: 'TestDataset' }] });
    return Promise.resolve({ data: [] });
  });
});

afterEach(() => jest.clearAllMocks());

describe('ApiReferencePage', () => {
  it('renders page title and 3 tabs', async () => {
    render(<ApiReferencePage />);
    expect(screen.getByText('API Reference')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText('CRUD')).toBeInTheDocument();
      expect(screen.getByText('Extraction')).toBeInTheDocument();
      expect(screen.getByText('Guide')).toBeInTheDocument();
    });
  });

  it('shows CRUD tab by default', async () => {
    render(<ApiReferencePage />);
    await waitFor(() => expect(screen.getByTestId('crud-tab')).toBeInTheDocument());
    expect(screen.queryByTestId('extraction-tab')).not.toBeInTheDocument();
    expect(screen.queryByTestId('guide-tab')).not.toBeInTheDocument();
  });

  it('navigates to Extraction tab on click', async () => {
    render(<ApiReferencePage />);
    await waitFor(() => expect(screen.getByText('Extraction')).toBeInTheDocument());
    fireEvent.click(screen.getByText('Extraction'));
    expect(screen.getByTestId('extraction-tab')).toBeInTheDocument();
    expect(screen.queryByTestId('crud-tab')).not.toBeInTheDocument();
  });

  it('navigates to Guide tab on click', async () => {
    render(<ApiReferencePage />);
    await waitFor(() => expect(screen.getByText('Guide')).toBeInTheDocument());
    fireEvent.click(screen.getByText('Guide'));
    expect(screen.getByTestId('guide-tab')).toBeInTheDocument();
  });

  it('loads domains then datasets on mount', async () => {
    render(<ApiReferencePage />);
    await waitFor(() => {
      expect(axios.get).toHaveBeenCalledWith('/api/domains');
      expect(axios.get).toHaveBeenCalledWith('/api/domains/1/data-sets');
    });
  });

  it('handles domains API error gracefully (no crash)', async () => {
    axios.get.mockRejectedValue(new Error('Network error'));
    render(<ApiReferencePage />);
    await waitFor(() => expect(screen.getByText('CRUD')).toBeInTheDocument());
  });

  it('handles empty domains list gracefully', async () => {
    axios.get.mockResolvedValue({ data: [] });
    render(<ApiReferencePage />);
    await waitFor(() => expect(screen.getByText('CRUD')).toBeInTheDocument());
  });
});
