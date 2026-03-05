import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import WizardStep3_Preview from './WizardStep3_Preview';

jest.mock('../../services/domainService', () => ({
  previewDataset: jest.fn(),
}));

const COLUMNS = [
  { id: '1', name: 'prenom', type: 'FIRST_NAME', constraints: {} },
  { id: '2', name: 'email', type: 'EMAIL', constraints: {} },
];

const PREVIEW_ROWS = [
  { prenom: 'Alice', email: 'alice@test.com' },
  { prenom: 'Bob', email: 'bob@test.com' },
];

beforeEach(() => {
  jest.clearAllMocks();
  const { previewDataset } = require('../../services/domainService');
  previewDataset.mockResolvedValue({ previewRows: PREVIEW_ROWS, columnCount: 2 });
});

test('affiche spinner lors du chargement', async () => {
  const { previewDataset } = require('../../services/domainService');
  previewDataset.mockImplementation(() => new Promise(() => {}));

  render(
    <WizardStep3_Preview
      columns={COLUMNS}
      previewRows={[]}
      onPreviewLoaded={jest.fn()}
      onBack={jest.fn()}
      onConfirm={jest.fn()}
    />
  );
  expect(screen.getByTestId('preview-spinner')).toBeInTheDocument();
});

test('affiche les données après chargement et appelle onPreviewLoaded', async () => {
  const onPreviewLoaded = jest.fn();
  render(
    <WizardStep3_Preview
      columns={COLUMNS}
      previewRows={[]}
      onPreviewLoaded={onPreviewLoaded}
      onBack={jest.fn()}
      onConfirm={jest.fn()}
    />
  );
  await waitFor(() => expect(screen.queryByTestId('preview-spinner')).not.toBeInTheDocument());
  expect(onPreviewLoaded).toHaveBeenCalledWith(PREVIEW_ROWS);
});

test('utilise previewRows existantes sans rappeler l\'API', () => {
  const { previewDataset } = require('../../services/domainService');
  render(
    <WizardStep3_Preview
      columns={COLUMNS}
      previewRows={PREVIEW_ROWS}
      onPreviewLoaded={jest.fn()}
      onBack={jest.fn()}
      onConfirm={jest.fn()}
    />
  );
  expect(previewDataset).not.toHaveBeenCalled();
  expect(screen.getByText('Alice')).toBeInTheDocument();
});

test('affiche erreur si l\'API échoue', async () => {
  const { previewDataset } = require('../../services/domainService');
  previewDataset.mockRejectedValue({ message: 'Erreur serveur' });

  render(
    <WizardStep3_Preview
      columns={COLUMNS}
      previewRows={[]}
      onPreviewLoaded={jest.fn()}
      onBack={jest.fn()}
      onConfirm={jest.fn()}
    />
  );
  await waitFor(() =>
    expect(screen.getByTestId('preview-error')).toBeInTheDocument()
  );
});

test('affiche le message lisible depuis data.message (pas le code error)', async () => {
  const { previewDataset } = require('../../services/domainService');
  previewDataset.mockRejectedValue({
    response: { data: { error: 'INVALID_ARGUMENT', message: 'Contrainte invalide pour montant' } },
  });

  render(
    <WizardStep3_Preview
      columns={COLUMNS}
      previewRows={[]}
      onPreviewLoaded={jest.fn()}
      onBack={jest.fn()}
      onConfirm={jest.fn()}
    />
  );
  await waitFor(() =>
    expect(screen.getByTestId('preview-error')).toHaveTextContent('Contrainte invalide pour montant')
  );
});

test('bouton Confirmer désactivé pendant chargement', () => {
  const { previewDataset } = require('../../services/domainService');
  previewDataset.mockImplementation(() => new Promise(() => {}));

  render(
    <WizardStep3_Preview
      columns={COLUMNS}
      previewRows={[]}
      onPreviewLoaded={jest.fn()}
      onBack={jest.fn()}
      onConfirm={jest.fn()}
    />
  );
  expect(screen.getByTestId('step3-confirm-button')).toBeDisabled();
});
