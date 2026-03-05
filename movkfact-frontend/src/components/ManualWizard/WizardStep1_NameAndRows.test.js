import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import WizardStep1_NameAndRows from './WizardStep1_NameAndRows';

const renderStep1 = (props = {}) => {
  const onNext = jest.fn();
  render(
    <WizardStep1_NameAndRows
      datasetName={props.datasetName ?? ''}
      rowCount={props.rowCount ?? 1000}
      onNext={props.onNext ?? onNext}
    />
  );
  return { onNext: props.onNext ?? onNext };
};

test('bouton Suivant désactivé si nom vide', () => {
  renderStep1({ datasetName: '' });
  expect(screen.getByTestId('step1-next-button')).toBeDisabled();
});

test('bouton Suivant désactivé si nom trop court (< 3 chars)', () => {
  renderStep1({ datasetName: 'ab' });
  expect(screen.getByTestId('step1-next-button')).toBeDisabled();
});

test('bouton Suivant désactivé si nom contient caractères invalides', () => {
  renderStep1({ datasetName: 'test@invalid!' });
  expect(screen.getByTestId('step1-next-button')).toBeDisabled();
});

test('bouton Suivant actif si nom et rowCount valides', () => {
  renderStep1({ datasetName: 'Mon Dataset', rowCount: 500 });
  expect(screen.getByTestId('step1-next-button')).not.toBeDisabled();
});

test('onNext appelé avec les bonnes valeurs au clic', () => {
  const onNext = jest.fn();
  renderStep1({ datasetName: 'Mon Dataset', rowCount: 1000, onNext });
  fireEvent.click(screen.getByTestId('step1-next-button'));
  expect(onNext).toHaveBeenCalledWith('Mon Dataset', 1000);
});

test('mise à jour du nom via le champ TextField', () => {
  renderStep1({ datasetName: '' });
  const input = screen.getByTestId('dataset-name-input');
  fireEvent.change(input, { target: { value: 'Nouveau Nom' } });
  expect(screen.getByTestId('step1-next-button')).not.toBeDisabled();
});
