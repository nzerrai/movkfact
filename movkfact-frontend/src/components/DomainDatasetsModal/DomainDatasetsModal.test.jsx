import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import DomainDatasetsModal from './DomainDatasetsModal';

// Mock du service — remplace le fetch hardcodé
jest.mock('../../services/domainService', () => ({
  getDatasetsByDomain: jest.fn(),
}));
import { getDatasetsByDomain } from '../../services/domainService';

const theme = createTheme();

const renderWithTheme = (component) =>
  render(
    <BrowserRouter>
      <ThemeProvider theme={theme}>{component}</ThemeProvider>
    </BrowserRouter>
  );

const mockDatasets = [
  {
    id: 1,
    datasetName: 'dataset-alpha',
    rowCount: 500,
    columnCount: 5,
    updatedAt: '2026-02-10T10:00:00Z',
    status: { downloaded: true, modified: false, viewed: true },
  },
  {
    id: 2,
    datasetName: 'dataset-beta',
    rowCount: 200,
    columnCount: 3,
    updatedAt: '2026-01-20T08:00:00Z',
    status: { downloaded: false, modified: true, viewed: false },
  },
];

describe('DomainDatasetsModal', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('affiche le nom du domaine dans le header', async () => {
    getDatasetsByDomain.mockResolvedValueOnce([]);

    renderWithTheme(
      <DomainDatasetsModal
        open={true}
        domainId={1}
        domainName="Test Domain"
        onClose={jest.fn()}
      />
    );

    // Le domainName apparaît dans plusieurs éléments (header + alert)
    const occurrences = screen.getAllByText('Test Domain');
    expect(occurrences.length).toBeGreaterThanOrEqual(1);
  });

  test('affiche "0 datasets" quand aucun dataset', async () => {
    getDatasetsByDomain.mockResolvedValueOnce([]);

    renderWithTheme(
      <DomainDatasetsModal open={true} domainId={1} domainName="D1" onClose={jest.fn()} />
    );

    await waitFor(() => {
      expect(screen.getByText(/0 datasets/)).toBeInTheDocument();
    });
  });

  test('charge et affiche les datasets via domainService', async () => {
    getDatasetsByDomain.mockResolvedValueOnce(mockDatasets);

    renderWithTheme(
      <DomainDatasetsModal open={true} domainId={1} domainName="D1" onClose={jest.fn()} />
    );

    await waitFor(() => {
      expect(screen.getByText('dataset-alpha')).toBeInTheDocument();
      expect(screen.getByText('dataset-beta')).toBeInTheDocument();
    });
  });

  test('affiche les badges de statut pour chaque dataset', async () => {
    getDatasetsByDomain.mockResolvedValueOnce(mockDatasets);

    renderWithTheme(
      <DomainDatasetsModal open={true} domainId={1} domainName="D1" onClose={jest.fn()} />
    );

    await waitFor(() => {
      expect(screen.getByText('Téléchargé')).toBeInTheDocument();
      expect(screen.getByText('Modifié')).toBeInTheDocument();
    });
  });

  test('filtre par "Modifiés" ne montre que dataset-beta', async () => {
    getDatasetsByDomain.mockResolvedValueOnce(mockDatasets);

    renderWithTheme(
      <DomainDatasetsModal open={true} domainId={1} domainName="D1" onClose={jest.fn()} />
    );

    await waitFor(() => {
      expect(screen.getByText('dataset-alpha')).toBeInTheDocument();
    });

    fireEvent.mouseDown(screen.getByRole('combobox'));
    const modifiedOption = await screen.findByRole('option', { name: 'Modifiés' });
    fireEvent.click(modifiedOption);

    await waitFor(() => {
      expect(screen.queryByText('dataset-alpha')).not.toBeInTheDocument();
      expect(screen.getByText('dataset-beta')).toBeInTheDocument();
    });
  });

  test('filtre par "Téléchargés" ne montre que dataset-alpha', async () => {
    getDatasetsByDomain.mockResolvedValueOnce(mockDatasets);

    renderWithTheme(
      <DomainDatasetsModal open={true} domainId={1} domainName="D1" onClose={jest.fn()} />
    );

    await waitFor(() => {
      expect(screen.getByText('dataset-alpha')).toBeInTheDocument();
    });

    fireEvent.mouseDown(screen.getByRole('combobox'));
    const downloadedOption = await screen.findByRole('option', { name: 'Téléchargés' });
    fireEvent.click(downloadedOption);

    await waitFor(() => {
      expect(screen.getByText('dataset-alpha')).toBeInTheDocument();
      expect(screen.queryByText('dataset-beta')).not.toBeInTheDocument();
    });
  });

  test('appelle onClose quand le bouton Fermer est cliqué', async () => {
    getDatasetsByDomain.mockResolvedValueOnce([]);
    const mockOnClose = jest.fn();

    renderWithTheme(
      <DomainDatasetsModal open={true} domainId={1} domainName="D1" onClose={mockOnClose} />
    );

    await waitFor(() => screen.getByText('Fermer'));
    await userEvent.click(screen.getByText('Fermer'));

    expect(mockOnClose).toHaveBeenCalled();
  });

  test('affiche le bouton Refresh', async () => {
    getDatasetsByDomain.mockResolvedValueOnce([]);

    renderWithTheme(
      <DomainDatasetsModal open={true} domainId={1} domainName="D1" onClose={jest.fn()} />
    );

    expect(screen.getByRole('button', { name: /refresh/i })).toBeInTheDocument();
  });

  test("n'appelle pas l'API si domainId est null", async () => {
    renderWithTheme(
      <DomainDatasetsModal open={true} domainId={null} domainName="D1" onClose={jest.fn()} />
    );

    await waitFor(() => {
      expect(getDatasetsByDomain).not.toHaveBeenCalled();
    });
  });

  test("n'appelle pas l'API si la modal est fermée", () => {
    renderWithTheme(
      <DomainDatasetsModal open={false} domainId={1} domainName="D1" onClose={jest.fn()} />
    );

    expect(getDatasetsByDomain).not.toHaveBeenCalled();
  });

  test('affiche une erreur si le service échoue', async () => {
    getDatasetsByDomain.mockRejectedValueOnce(new Error('Network error'));

    renderWithTheme(
      <DomainDatasetsModal open={true} domainId={1} domainName="D1" onClose={jest.fn()} />
    );

    await waitFor(() => {
      expect(screen.getByText(/Impossible de charger les datasets/)).toBeInTheDocument();
    });
  });
});
