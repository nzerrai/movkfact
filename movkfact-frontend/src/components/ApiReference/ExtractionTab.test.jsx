import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import axios from 'axios';
import ExtractionTab from './ExtractionTab';

jest.mock('axios');

const DATASETS = [
  { id: 3, name: 'Clients' },
  { id: 7, name: 'Orders' },
];

function selectDataset(name) {
  const combobox = screen.getByRole('combobox', { name: /dataset/i });
  fireEvent.mouseDown(combobox);
  fireEvent.click(screen.getByText(name));
}

describe('ExtractionTab', () => {
  afterEach(() => jest.clearAllMocks());

  it('renders dataset selector with provided datasets', () => {
    render(<ExtractionTab datasets={DATASETS} />);
    const combobox = screen.getByRole('combobox', { name: /dataset/i });
    fireEvent.mouseDown(combobox);
    expect(screen.getByText('Clients (id: 3)')).toBeInTheDocument();
    expect(screen.getByText('Orders (id: 7)')).toBeInTheDocument();
  });

  it('shows "Aucun dataset" when datasets is empty', () => {
    render(<ExtractionTab datasets={[]} />);
    const combobox = screen.getByRole('combobox', { name: /dataset/i });
    fireEvent.mouseDown(combobox);
    expect(screen.getByText('Aucun dataset disponible')).toBeInTheDocument();
  });

  it('shows built URL with full mode after selecting dataset', async () => {
    render(<ExtractionTab datasets={DATASETS} />);
    selectDataset('Clients (id: 3)');
    await waitFor(() =>
      expect(screen.getByText(/GET \/api\/data-sets\/3\/export\?format=JSON&mode=full/)).toBeInTheDocument()
    );
  });

  it('shows rowIds field when mode=filtered', () => {
    render(<ExtractionTab datasets={DATASETS} />);
    fireEvent.click(screen.getByLabelText('filtered'));
    expect(screen.getByLabelText(/rowIds/i)).toBeInTheDocument();
  });

  it('shows count field when mode=sample', () => {
    render(<ExtractionTab datasets={DATASETS} />);
    fireEvent.click(screen.getByLabelText('sample'));
    expect(screen.getByLabelText(/count/i)).toBeInTheDocument();
  });

  it('does not show rowIds or count field in full mode', () => {
    render(<ExtractionTab datasets={DATASETS} />);
    expect(screen.queryByLabelText(/rowIds/i)).not.toBeInTheDocument();
    expect(screen.queryByLabelText(/count/i)).not.toBeInTheDocument();
  });

  it('appends rowIds to URL when mode=filtered', async () => {
    render(<ExtractionTab datasets={DATASETS} />);
    selectDataset('Clients (id: 3)');
    fireEvent.click(screen.getByLabelText('filtered'));
    fireEvent.change(screen.getByLabelText(/rowIds/i), { target: { value: '0,5,10' } });
    await waitFor(() => expect(screen.getByText(/rowIds=0,5,10/)).toBeInTheDocument());
  });

  it('appends count to URL when mode=sample', async () => {
    render(<ExtractionTab datasets={DATASETS} />);
    selectDataset('Clients (id: 3)');
    fireEvent.click(screen.getByLabelText('sample'));
    await waitFor(() => expect(screen.getByText(/count=50/)).toBeInTheDocument());
  });

  it('disables Télécharger/Prévisualiser when no dataset selected', () => {
    render(<ExtractionTab datasets={DATASETS} />);
    expect(screen.getByText('Télécharger').closest('button')).toBeDisabled();
    expect(screen.getByText('Prévisualiser (50 lignes)').closest('button')).toBeDisabled();
  });

  it('shows preview table on successful preview', async () => {
    axios.get.mockResolvedValue({
      data: [
        { firstName: 'Alice', email: 'alice@test.com' },
        { firstName: 'Bob', email: 'bob@test.com' },
      ],
    });
    render(<ExtractionTab datasets={DATASETS} />);
    selectDataset('Clients (id: 3)');
    fireEvent.click(screen.getByText('Prévisualiser (50 lignes)').closest('button'));
    await waitFor(() => expect(screen.getByText('Alice')).toBeInTheDocument());
    expect(screen.getByText('Bob')).toBeInTheDocument();
  });

  it('shows error alert on API failure', async () => {
    axios.get.mockRejectedValue({ message: 'Server Error' });
    render(<ExtractionTab datasets={DATASETS} />);
    selectDataset('Clients (id: 3)');
    fireEvent.click(screen.getByText('Prévisualiser (50 lignes)').closest('button'));
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
  });
});
