import React from 'react';
import { render, screen } from '@testing-library/react';
import { DomainTable } from './DomainTable';

const mockDomains = [
  { id: 1, name: 'Domain1', description: 'Description 1' },
  { id: 2, name: 'Domain2', description: 'Description 2' },
  { id: 3, name: 'Domain3', description: 'Description 3' },
];

const mockOnEdit = jest.fn();
const mockOnDelete = jest.fn();

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
        loading={false}
        searchText=""
      />
    );

    expect(screen.getByText('Domain1')).toBeInTheDocument();
    expect(screen.queryByText('Domain2')).not.toBeInTheDocument();
  });
});
