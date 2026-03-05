import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import GuideTab from './GuideTab';

describe('GuideTab', () => {
  it('renders 6 use case titles', () => {
    render(<GuideTab />);
    expect(screen.getByText(/1\. Lister tous les domaines/)).toBeInTheDocument();
    expect(screen.getByText(/2\. Créer un domaine/)).toBeInTheDocument();
    expect(screen.getByText(/3\. Générer un dataset/)).toBeInTheDocument();
    expect(screen.getByText(/4\. Extraire toutes les lignes en JSON/)).toBeInTheDocument();
    expect(screen.getByText(/5\. Extraire un échantillon/)).toBeInTheDocument();
    expect(screen.getByText(/6\. Récupérer des lignes spécifiques/)).toBeInTheDocument();
  });

  it('renders BASE_URL field with default value', () => {
    render(<GuideTab />);
    expect(screen.getByDisplayValue('http://localhost:8080')).toBeInTheDocument();
  });

  it('updates snippets when BASE_URL changes', async () => {
    render(<GuideTab />);
    const input = screen.getByDisplayValue('http://localhost:8080');
    fireEvent.change(input, { target: { value: 'https://api.myapp.com' } });
    await waitFor(() => {
      const pres = screen.getAllByText(/https:\/\/api\.myapp\.com/);
      expect(pres.length).toBeGreaterThan(0);
    });
  });

  it('renders curl as first tab in each SnippetBlock', () => {
    render(<GuideTab />);
    const curlTabs = screen.getAllByText('curl');
    expect(curlTabs.length).toBe(6);
  });

  it('renders JavaScript and Python tabs', () => {
    render(<GuideTab />);
    const jsTabs = screen.getAllByText('JavaScript (fetch)');
    const pyTabs = screen.getAllByText('Python (requests)');
    expect(jsTabs.length).toBe(6);
    expect(pyTabs.length).toBe(6);
  });
});
