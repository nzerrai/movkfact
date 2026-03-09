import React from 'react';
import { render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import StatusBadge from './StatusBadge';

const theme = createTheme();
const renderWithTheme = (ui) =>
  render(<ThemeProvider theme={theme}>{ui}</ThemeProvider>);

describe('StatusBadge', () => {
  test('affiche "Nouveau" quand tous les statuts sont false', () => {
    renderWithTheme(
      <StatusBadge downloaded={false} modified={false} viewed={false} />
    );
    expect(screen.getByText('Nouveau')).toBeInTheDocument();
    expect(screen.queryByText('Téléchargé')).not.toBeInTheDocument();
    expect(screen.queryByText('Modifié')).not.toBeInTheDocument();
    expect(screen.queryByText('Consulté')).not.toBeInTheDocument();
  });

  test('affiche "Nouveau" quand aucune prop n\'est fournie', () => {
    renderWithTheme(<StatusBadge />);
    expect(screen.getByText('Nouveau')).toBeInTheDocument();
  });

  test('affiche le badge "Téléchargé" quand downloaded=true', () => {
    renderWithTheme(
      <StatusBadge downloaded={true} modified={false} viewed={false} />
    );
    expect(screen.getByText('Téléchargé')).toBeInTheDocument();
    expect(screen.queryByText('Nouveau')).not.toBeInTheDocument();
    expect(screen.queryByText('Modifié')).not.toBeInTheDocument();
  });

  test('affiche le badge "Modifié" quand modified=true', () => {
    renderWithTheme(
      <StatusBadge downloaded={false} modified={true} viewed={false} />
    );
    expect(screen.getByText('Modifié')).toBeInTheDocument();
    expect(screen.queryByText('Nouveau')).not.toBeInTheDocument();
  });

  test('affiche le badge "Consulté" quand viewed=true', () => {
    renderWithTheme(
      <StatusBadge downloaded={false} modified={false} viewed={true} />
    );
    expect(screen.getByText('Consulté')).toBeInTheDocument();
    expect(screen.queryByText('Nouveau')).not.toBeInTheDocument();
  });

  test('affiche plusieurs badges simultanément', () => {
    renderWithTheme(
      <StatusBadge downloaded={true} modified={true} viewed={true} />
    );
    expect(screen.getByText('Téléchargé')).toBeInTheDocument();
    expect(screen.getByText('Modifié')).toBeInTheDocument();
    expect(screen.getByText('Consulté')).toBeInTheDocument();
    expect(screen.queryByText('Nouveau')).not.toBeInTheDocument();
  });
});
