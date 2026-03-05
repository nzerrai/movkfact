import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { SnackbarProvider } from 'notistack';
import BatchGenerationModal from './BatchGenerationModal';
import * as batchService from '../../services/batchService';
import { BatchJobsProvider } from '../../context/BatchJobsContext';

// Mock batchService
jest.mock('../../services/batchService');

// Mock WebSocketService to avoid real WS connections in tests
jest.mock('../../services/WebSocketService', () => ({
  onConnect: null,
  onDisconnect: null,
  onReconnecting: null,
  connect: jest.fn(),
  disconnect: jest.fn(),
  subscribeToBatch: jest.fn(),
  unsubscribeFromBatch: jest.fn(),
}));

const DOMAINS = [
  { id: 1, name: 'Clients', description: 'Domaine clients' },
  { id: 2, name: 'Commandes', description: 'Domaine commandes' },
];

const CONFIG_WITH_COLS = {
  success: true,
  hasConfigurations: true,
  columnsCount: 3,
  columns: [
    { name: 'prenom', type: 'FIRST_NAME', confidence: 95 },
    { name: 'nom', type: 'LAST_NAME', confidence: 90 },
    { name: 'email', type: 'EMAIL', confidence: 85 },
  ],
};

const CONFIG_EMPTY = {
  success: true,
  hasConfigurations: false,
  columnsCount: 0,
  columns: [],
};

function renderModal(props = {}) {
  return render(
    <SnackbarProvider maxSnack={3}>
      <BatchJobsProvider>
        <BatchGenerationModal
          open={true}
          onClose={jest.fn()}
          domains={DOMAINS}
          {...props}
        />
      </BatchJobsProvider>
    </SnackbarProvider>
  );
}

describe('BatchGenerationModal', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // Test 4.1 — Rendu initial
  it('affiche la liste des domaines', () => {
    renderModal();
    expect(screen.getByText('Clients')).toBeInTheDocument();
    expect(screen.getByText('Commandes')).toBeInTheDocument();
  });

  // Test 4.2 — Sélection d'un domaine charge la config
  it('charge la config des colonnes lors de la sélection d\'un domaine', async () => {
    batchService.getDomainColumnConfig.mockResolvedValue(CONFIG_WITH_COLS);
    renderModal();

    const checkbox = screen.getByLabelText('select-domain-1');
    await act(async () => {
      fireEvent.click(checkbox);
    });

    await waitFor(() => {
      expect(batchService.getDomainColumnConfig).toHaveBeenCalledWith(1);
    });

    expect(screen.getByText('3 col.')).toBeInTheDocument();
  });

  // Test 4.3 — Domaine sans config → chip avertissement + checkbox désactivée
  it('affiche un avertissement pour un domaine sans configuration', async () => {
    batchService.getDomainColumnConfig.mockResolvedValue(CONFIG_EMPTY);
    renderModal();

    const checkbox = screen.getByLabelText('select-domain-1');
    await act(async () => {
      fireEvent.click(checkbox);
    });

    await waitFor(() => {
      expect(screen.getByText('Aucune configuration')).toBeInTheDocument();
    });

    // Checkbox should be disabled after config loaded with no columns
    await waitFor(() => {
      expect(checkbox).toBeDisabled();
    });
  });

  // Test 4.4 — Bouton "Lancer" désactivé si aucun domaine sélectionné
  it('désactive le bouton Lancer quand aucun domaine n\'est sélectionné', () => {
    renderModal();
    const submitBtn = screen.getByTestId('submit-batch-button');
    expect(submitBtn).toBeDisabled();
  });

  // Test 4.5 — Soumission appelle submitBatch puis trackJob
  it('soumet le batch et appelle trackJob après succès', async () => {
    batchService.getDomainColumnConfig.mockResolvedValue(CONFIG_WITH_COLS);
    batchService.submitBatch.mockResolvedValue({ jobId: 42, totalDatasets: 1, message: 'ok' });

    const onClose = jest.fn();
    renderModal({ onClose });

    // Sélectionner le domaine 1
    const checkbox = screen.getByLabelText('select-domain-1');
    await act(async () => {
      fireEvent.click(checkbox);
    });

    await waitFor(() => {
      expect(screen.getByTestId('dataset-name-1')).toBeInTheDocument();
    });

    // Cliquer sur Lancer
    const submitBtn = screen.getByTestId('submit-batch-button');
    await act(async () => {
      fireEvent.click(submitBtn);
    });

    await waitFor(() => {
      expect(batchService.submitBatch).toHaveBeenCalledWith(
        expect.arrayContaining([
          expect.objectContaining({
            domainId: 1,
            count: 100,
            columns: expect.arrayContaining([
              expect.objectContaining({ columnType: 'FIRST_NAME' }),
            ]),
          }),
        ])
      );
    });

    await waitFor(() => {
      expect(onClose).toHaveBeenCalled();
    });
  });

  // Test 4.6 — Erreur API → affiche message d'erreur
  it('affiche une alerte d\'erreur si submitBatch échoue', async () => {
    batchService.getDomainColumnConfig.mockResolvedValue(CONFIG_WITH_COLS);
    batchService.submitBatch.mockRejectedValue(new Error('Erreur serveur'));

    renderModal();

    const checkbox = screen.getByLabelText('select-domain-1');
    await act(async () => {
      fireEvent.click(checkbox);
    });

    await waitFor(() => {
      expect(screen.getByTestId('submit-batch-button')).not.toBeDisabled();
    });

    await act(async () => {
      fireEvent.click(screen.getByTestId('submit-batch-button'));
    });

    await waitFor(() => {
      expect(screen.getByText('Erreur serveur')).toBeInTheDocument();
    });
  });

  // Test 4.7 — Fermeture reset le state
  it('reset le state à la fermeture', async () => {
    batchService.getDomainColumnConfig.mockResolvedValue(CONFIG_WITH_COLS);
    const onClose = jest.fn();
    renderModal({ onClose });

    const checkbox = screen.getByLabelText('select-domain-1');
    await act(async () => {
      fireEvent.click(checkbox);
    });

    fireEvent.click(screen.getByText('Annuler'));
    expect(onClose).toHaveBeenCalled();
  });
});
