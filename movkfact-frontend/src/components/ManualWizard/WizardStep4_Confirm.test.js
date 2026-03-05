import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import WizardStep4_Confirm from './WizardStep4_Confirm';

jest.mock('../../services/api', () => ({
  post: jest.fn(),
}));

const COLUMNS = [
  { id: '1', name: 'prenom', type: 'FIRST_NAME', constraints: {} },
  { id: '2', name: 'email', type: 'EMAIL', constraints: {} },
];

const defaultProps = {
  datasetName: 'TestDataset',
  domainId: 42,
  rowCount: 500,
  columns: COLUMNS,
  onBack: jest.fn(),
  onSuccess: jest.fn(),
};

beforeEach(() => {
  jest.clearAllMocks();
  const api = require('../../services/api');
  api.post.mockResolvedValue({ data: { data: {}, message: 'ok' } });
});

test('affiche le récapitulatif', () => {
  render(<WizardStep4_Confirm {...defaultProps} />);
  expect(screen.getByTestId('summary-name')).toHaveTextContent('TestDataset');
  expect(screen.getByTestId('summary-rowcount')).toHaveTextContent('500');
  expect(screen.getByTestId('summary-columns')).toHaveTextContent('prenom (FIRST_NAME)');
});

test('bouton Retour appelle onBack', () => {
  render(<WizardStep4_Confirm {...defaultProps} />);
  fireEvent.click(screen.getByTestId('step4-back-button'));
  expect(defaultProps.onBack).toHaveBeenCalled();
});

test('bouton Lancer la génération appelle l\'API avec les bons champs et onSuccess', async () => {
  const api = require('../../services/api');
  render(<WizardStep4_Confirm {...defaultProps} />);
  fireEvent.click(screen.getByTestId('step4-generate-button'));
  await waitFor(() => expect(defaultProps.onSuccess).toHaveBeenCalled());
  expect(api.post).toHaveBeenCalledWith(
    '/api/domains/42/data-sets',
    expect.objectContaining({ datasetName: 'TestDataset', numberOfRows: 500 })
  );
});

test('affiche erreur si l\'API échoue', async () => {
  const api = require('../../services/api');
  api.post.mockRejectedValue({ response: { data: { message: 'Erreur réseau' } } });

  render(<WizardStep4_Confirm {...defaultProps} />);
  fireEvent.click(screen.getByTestId('step4-generate-button'));
  await waitFor(() =>
    expect(screen.getByTestId('confirm-error')).toHaveTextContent('Erreur réseau')
  );
});
