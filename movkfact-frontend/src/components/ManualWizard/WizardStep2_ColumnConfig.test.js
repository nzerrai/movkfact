import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import WizardStep2_ColumnConfig from './WizardStep2_ColumnConfig';

// Mock @dnd-kit to avoid complex drag setup in tests
jest.mock('@dnd-kit/core', () => ({
  DndContext: ({ children }) => <div>{children}</div>,
  closestCenter: jest.fn(),
}));
jest.mock('@dnd-kit/sortable', () => ({
  SortableContext: ({ children }) => <div>{children}</div>,
  verticalListSortingStrategy: jest.fn(),
  arrayMove: jest.fn((arr, from, to) => arr),
  useSortable: () => ({
    attributes: {},
    listeners: {},
    setNodeRef: jest.fn(),
    transform: null,
    transition: null,
  }),
}));
jest.mock('@dnd-kit/utilities', () => ({
  CSS: { Transform: { toString: () => '' } },
}));

const renderStep2 = (props = {}) => {
  const onColumnsChange = jest.fn();
  const onBack = jest.fn();
  const onPreview = jest.fn();
  render(
    <WizardStep2_ColumnConfig
      columns={props.columns ?? []}
      onColumnsChange={props.onColumnsChange ?? onColumnsChange}
      onBack={props.onBack ?? onBack}
      onPreview={props.onPreview ?? onPreview}
    />
  );
  return {
    onColumnsChange: props.onColumnsChange ?? onColumnsChange,
    onBack: props.onBack ?? onBack,
    onPreview: props.onPreview ?? onPreview,
  };
};

test('rendu initial avec une colonne par défaut', () => {
  renderStep2();
  expect(screen.getByTestId('column-name-input')).toBeInTheDocument();
});

test('bouton + Ajouter une colonne présent', () => {
  renderStep2();
  expect(screen.getByTestId('add-column-button')).toBeInTheDocument();
});

test('bouton Retour appelle onBack', () => {
  const { onBack } = renderStep2();
  fireEvent.click(screen.getByTestId('step2-back-button'));
  expect(onBack).toHaveBeenCalled();
});

test('bouton Prévisualiser désactivé si nom de colonne vide', () => {
  renderStep2();
  expect(screen.getByTestId('step2-preview-button')).toBeDisabled();
});

test('bouton Prévisualiser activé après saisie du nom', () => {
  renderStep2();
  const input = screen.getByTestId('column-name-input');
  fireEvent.change(input, { target: { value: 'prenom' } });
  expect(screen.getByTestId('step2-preview-button')).not.toBeDisabled();
});

test('clic + Ajouter ajoute une colonne', () => {
  renderStep2();
  const before = screen.getAllByTestId('column-name-input').length;
  fireEvent.click(screen.getByTestId('add-column-button'));
  expect(screen.getAllByTestId('column-name-input').length).toBe(before + 1);
});
