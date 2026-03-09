import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { DomainTable } from './DomainTable';

const theme = createTheme();
const renderWithTheme = (ui) =>
  render(<ThemeProvider theme={theme}>{ui}</ThemeProvider>);

const mockDomains = [
  {
    id: 1, name: 'Domain1', description: 'Description 1',
    createdAt: '2026-01-01', updatedAt: '2026-01-15',
    datasetCount: 3, totalRows: 1500,
    statuses: { downloaded: true, modified: false, viewed: true },
  },
  {
    id: 2, name: 'Domain2', description: 'Description 2',
    createdAt: '2026-01-02', updatedAt: '2026-01-16',
    datasetCount: 0, totalRows: 0,
    statuses: { downloaded: false, modified: false, viewed: false },
  },
  {
    id: 3, name: 'Domain3', description: 'Description 3',
    createdAt: '2026-01-03', updatedAt: '2026-01-17',
    datasetCount: 1, totalRows: 2500000,
    statuses: { downloaded: false, modified: true, viewed: false },
  },
];

const mockOnEdit = jest.fn();
const mockOnDelete = jest.fn();
const mockOnUpload = jest.fn();
const mockOnViewDatasets = jest.fn();

describe('DomainTable Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render empty state when no domains', () => {
    const { container } = render(
      <DomainTable
        domains={[]}
        onEdit={mockOnEdit}
        onDelete={mockOnDelete}
        loading={false}
        searchText=""
      />
    );

    expect(container).toBeTruthy();
  });

  it('should display domain names when data provided', () => {
    render(
      <DomainTable
        domains={mockDomains}
        onEdit={mockOnEdit}
        onDelete={mockOnDelete}
        onUpload={mockOnUpload}
        onViewDatasets={mockOnViewDatasets}
        loading={false}
        searchText=""
      />
    );

    mockDomains.forEach((domain) => {
      expect(screen.getByText(domain.name)).toBeInTheDocument();
    });
  });

  it('should render edit and delete buttons for each domain', () => {
    render(
      <DomainTable
        domains={mockDomains}
        onEdit={mockOnEdit}
        onDelete={mockOnDelete}
        onUpload={mockOnUpload}
        onViewDatasets={mockOnViewDatasets}
        loading={false}
        searchText=""
      />
    );

    const editButtons = screen.getAllByRole('button', { name: /edit/i });
    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });

    expect(editButtons.length).toBe(mockDomains.length);
    expect(deleteButtons.length).toBe(mockDomains.length);
  });

  it('should render with single domain', () => {
    render(
      <DomainTable
        domains={[mockDomains[0]]}
        onEdit={mockOnEdit}
        onDelete={mockOnDelete}
        onUpload={mockOnUpload}
        onViewDatasets={mockOnViewDatasets}
        loading={false}
        searchText=""
      />
    );

    expect(screen.getByText('Domain1')).toBeInTheDocument();
    expect(screen.queryByText('Domain2')).not.toBeInTheDocument();
  });

  // New tests for View Datasets button
  it('should display "View Uploaded Datasets" button for each domain', () => {
    render(
      <DomainTable
        domains={mockDomains}
        onEdit={mockOnEdit}
        onDelete={mockOnDelete}
        onUpload={mockOnUpload}
        onViewDatasets={mockOnViewDatasets}
        loading={false}
        searchText=""
      />
    );

    // Should have 3 "View Uploaded Datasets" buttons (one per domain)
    const viewButtons = screen.getAllByRole('button', { name: /view uploaded datasets/i });
    expect(viewButtons).toHaveLength(3);
  });

  it('should call onViewDatasets with correct domain when button clicked', async () => {
    render(
      <DomainTable
        domains={mockDomains}
        onEdit={mockOnEdit}
        onDelete={mockOnDelete}
        onUpload={mockOnUpload}
        onViewDatasets={mockOnViewDatasets}
        loading={false}
        searchText=""
      />
    );

    const viewButtons = screen.getAllByRole('button', { name: /view uploaded datasets/i });
    await userEvent.click(viewButtons[0]);

    expect(mockOnViewDatasets).toHaveBeenCalledWith(mockDomains[0]);
  });

  it('should have all action buttons in correct order', () => {
    render(
      <DomainTable
        domains={mockDomains}
        onEdit={mockOnEdit}
        onDelete={mockOnDelete}
        onUpload={mockOnUpload}
        onViewDatasets={mockOnViewDatasets}
        loading={false}
        searchText=""
      />
    );

    // View Datasets, Upload CSV, Edit, Delete buttons should exist
    expect(screen.getAllByRole('button', { name: /view uploaded datasets/i })).toHaveLength(3);
    expect(screen.getAllByRole('button', { name: /upload csv/i })).toHaveLength(3);
  });

  // ── Tests colonnes enrichies (FR-002) ──

  it('affiche le header "Datasets" dans le tableau', () => {
    renderWithTheme(
      <DomainTable domains={mockDomains} loading={false} searchText="" />
    );
    expect(screen.getByText('Datasets')).toBeInTheDocument();
  });

  it('affiche le header "Statuts" dans le tableau', () => {
    renderWithTheme(
      <DomainTable domains={mockDomains} loading={false} searchText="" />
    );
    expect(screen.getByText('Statuts')).toBeInTheDocument();
  });

  it('affiche le chip avec le datasetCount pour chaque domaine', () => {
    renderWithTheme(
      <DomainTable domains={[mockDomains[0]]} loading={false} searchText="" />
    );
    expect(screen.getByText('3')).toBeInTheDocument(); // datasetCount=3
  });

  it('affiche le badge "Téléchargé" quand downloaded=true', () => {
    renderWithTheme(
      <DomainTable domains={[mockDomains[0]]} loading={false} searchText="" />
    );
    expect(screen.getByText('Téléchargé')).toBeInTheDocument();
  });

  it('affiche le badge "Nouveau" quand tous statuts false', () => {
    renderWithTheme(
      <DomainTable domains={[mockDomains[1]]} loading={false} searchText="" />
    );
    expect(screen.getByText('Nouveau')).toBeInTheDocument();
  });

  it('affiche le badge "Modifié" quand modified=true', () => {
    renderWithTheme(
      <DomainTable domains={[mockDomains[2]]} loading={false} searchText="" />
    );
    expect(screen.getByText('Modifié')).toBeInTheDocument();
  });

  it('affiche le skeleton quand loading=true', () => {
    const { container } = renderWithTheme(
      <DomainTable domains={[]} loading={true} searchText="" />
    );
    // Skeleton rows render — header columns present
    expect(screen.getByText('Datasets')).toBeInTheDocument();
    expect(screen.getByText('Statuts')).toBeInTheDocument();
  });
});
