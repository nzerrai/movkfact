import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import DeleteRowDialog from './DeleteRowDialog';

describe('DeleteRowDialog', () => {
  const defaultProps = {
    open: true,
    rowIndex: 3,
    onConfirm: jest.fn(),
    onCancel: jest.fn(),
    loading: false,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('affiche le dialog quand open=true', () => {
    render(<DeleteRowDialog {...defaultProps} />);
    expect(screen.getByText(/Supprimer la ligne/i)).toBeInTheDocument();
    expect(screen.getByText(/ligne 3/i)).toBeInTheDocument();
  });

  test('ne rend pas le dialog quand open=false', () => {
    render(<DeleteRowDialog {...defaultProps} open={false} />);
    expect(screen.queryByText(/Supprimer la ligne/i)).not.toBeInTheDocument();
  });

  test('appelle onConfirm au clic Supprimer', () => {
    render(<DeleteRowDialog {...defaultProps} />);
    fireEvent.click(screen.getByRole('button', { name: /Supprimer/i }));
    expect(defaultProps.onConfirm).toHaveBeenCalledTimes(1);
  });

  test('appelle onCancel au clic Annuler', () => {
    render(<DeleteRowDialog {...defaultProps} />);
    fireEvent.click(screen.getByRole('button', { name: /Annuler/i }));
    expect(defaultProps.onCancel).toHaveBeenCalledTimes(1);
  });

  test('désactive les boutons en état loading', () => {
    render(<DeleteRowDialog {...defaultProps} loading={true} />);
    expect(screen.getByRole('button', { name: /Suppression/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /Annuler/i })).toBeDisabled();
  });
});
