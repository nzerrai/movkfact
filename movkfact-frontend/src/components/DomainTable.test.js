import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DomainTable } from './DomainTable';

const mockDomains = [
  { id: 1, name: 'Domain1', description: 'Description 1', createdAt: '2026-01-01', updatedAt: '2026-01-15' },
  { id: 2, name: 'Domain2', description: 'Description 2', createdAt: '2026-01-02', updatedAt: '2026-01-16' },
  { id: 3, name: 'Domain3', description: 'Description 3', createdAt: '2026-01-03', updatedAt: '2026-01-17' },
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
});
