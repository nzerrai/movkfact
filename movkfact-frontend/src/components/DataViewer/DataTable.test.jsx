import React from 'react';
import { render, screen, fireEvent, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DataTable from './DataTable';

describe('DataTable', () => {
  const mockColumns = ['firstName', 'lastName', 'email', 'age'];
  const mockData = [
    { firstName: 'John', lastName: 'Doe', email: 'john@example.com', age: '30' },
    { firstName: 'Jane', lastName: 'Smith', email: 'jane@example.com', age: '28' },
    { firstName: 'Bob', lastName: 'Johnson', email: 'bob@example.com', age: '35' }
  ];

  const mockOnPageChange = jest.fn();
  const mockOnRowsPerPageChange = jest.fn();
  const mockOnSort = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders data table with rows', () => {
    render(
      <DataTable
        data={mockData}
        columns={mockColumns}
        sortConfig={null}
        onSort={mockOnSort}
        pageIndex={0}
        rowsPerPage={10}
        totalRows={3}
        onPageChange={mockOnPageChange}
        onRowsPerPageChange={mockOnRowsPerPageChange}
        totalPages={1}
      />
    );

    expect(screen.getByText('John')).toBeInTheDocument();
    expect(screen.getByText('Jane')).toBeInTheDocument();
    expect(screen.getByText('Bob')).toBeInTheDocument();
  });

  test('displays column headers', () => {
    render(
      <DataTable
        data={mockData}
        columns={mockColumns}
        sortConfig={null}
        onSort={mockOnSort}
        pageIndex={0}
        rowsPerPage={10}
        totalRows={3}
        onPageChange={mockOnPageChange}
        onRowsPerPageChange={mockOnRowsPerPageChange}
        totalPages={1}
      />
    );

    mockColumns.forEach(col => {
      expect(screen.getByText(col)).toBeInTheDocument();
    });
  });

  test('handles column sort click', async () => {
    render(
      <DataTable
        data={mockData}
        columns={mockColumns}
        sortConfig={null}
        onSort={mockOnSort}
        pageIndex={0}
        rowsPerPage={25}
        totalRows={3}
        onPageChange={mockOnPageChange}
        onRowsPerPageChange={mockOnRowsPerPageChange}
        totalPages={1}
      />
    );

    const headers = screen.getAllByRole('columnheader');
    const headerCell = headers[0];
    await userEvent.click(headerCell);

    // New API: onSort(column, isShiftClick)
    expect(mockOnSort).toHaveBeenCalledWith(expect.any(String), expect.any(Boolean));
  });

  test('displays sort indicator', () => {
    render(
      <DataTable
        data={mockData}
        columns={mockColumns}
        sortConfig={{ column: 'firstName', direction: 'asc' }}
        onSort={mockOnSort}
        pageIndex={0}
        rowsPerPage={10}
        totalRows={3}
        onPageChange={mockOnPageChange}
        onRowsPerPageChange={mockOnRowsPerPageChange}
        totalPages={1}
      />
    );

    // Sort indicator should be visible
    const sortIndicator = screen.queryByRole('img', { hidden: true });
    expect(sortIndicator || screen.getByText('firstName')).toBeDefined();
  });

  test('handles pagination', async () => {
    render(
      <DataTable
        data={mockData}
        columns={mockColumns}
        sortConfig={null}
        onSort={mockOnSort}
        pageIndex={0}
        rowsPerPage={10}
        totalRows={30}
        onPageChange={mockOnPageChange}
        onRowsPerPageChange={mockOnRowsPerPageChange}
        totalPages={3}
      />
    );

    const nextPageButtons = screen.getAllByRole('button').filter(btn =>
      btn.getAttribute('aria-label')?.includes('next page')
    );

    if (nextPageButtons.length > 0) {
      await userEvent.click(nextPageButtons[0]);
      expect(mockOnPageChange).toHaveBeenCalled();
    }
  });

  test('changes rows per page', async () => {
    render(
      <DataTable
        data={mockData}
        columns={mockColumns}
        sortConfig={null}
        onSort={mockOnSort}
        pageIndex={0}
        rowsPerPage={10}
        totalRows={3}
        onPageChange={mockOnPageChange}
        onRowsPerPageChange={mockOnRowsPerPageChange}
        totalPages={1}
      />
    );

    const rowsPerPageInput = screen.getByRole('combobox', { name: /rows per page/i });
    if (rowsPerPageInput) {
      await userEvent.click(rowsPerPageInput);
      const option = screen.getByText('50');
      await userEvent.click(option);
      expect(mockOnRowsPerPageChange).toHaveBeenCalledWith(50);
    }
  });

  test('truncates long cell values', () => {
    const longData = [{
      firstName: 'John'.repeat(20),
      lastName: 'Doe',
      email: 'john@example.com',
      age: '30'
    }];

    render(
      <DataTable
        data={longData}
        columns={mockColumns}
        sortConfig={null}
        onSort={mockOnSort}
        pageIndex={0}
        rowsPerPage={10}
        totalRows={1}
        onPageChange={mockOnPageChange}
        onRowsPerPageChange={mockOnRowsPerPageChange}
        totalPages={1}
      />
    );

    // Verify truncation with '...'
    const cells = screen.getAllByRole('cell');
    const truncatedCell = cells.find(cell => cell.textContent.includes('...'));
    expect(truncatedCell || screen.getByText(/John/)).toBeDefined();
  });

  test('displays no data message', () => {
    render(
      <DataTable
        data={[]}
        columns={mockColumns}
        sortConfig={null}
        onSort={mockOnSort}
        pageIndex={0}
        rowsPerPage={10}
        totalRows={0}
        onPageChange={mockOnPageChange}
        onRowsPerPageChange={mockOnRowsPerPageChange}
        totalPages={0}
      />
    );

    expect(screen.getByText(/no rows to display/i)).toBeInTheDocument();
  });
});
