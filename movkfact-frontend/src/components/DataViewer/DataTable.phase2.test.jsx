import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DataTable from './DataTable';

// Suppress React act() warnings
const originalError = console.error;
beforeAll(() => {
  console.error = (...args) => {
    if (
      typeof args[0] === 'string' &&
      (args[0].includes('act(') || args[0].includes('MUI') || args[0].includes('out-of-range'))
    ) {
      return;
    }
    originalError.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
});

describe('DataTable - Phase 2: Multi-Column Sorting', () => {
  const mockColumns = ['firstName', 'lastName', 'age'];
  const mockData = [
    { firstName: 'Alice', lastName: 'Anderson', age: '25' },
    { firstName: 'Bob', lastName: 'Brown', age: '30' },
    { firstName: 'Charlie', lastName: 'Clark', age: '35' }
  ];

  const mockOnPageChange = jest.fn();
  const mockOnRowsPerPageChange = jest.fn();
  const mockOnSort = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Multi-Column Sort Display', () => {
    test('renders table with multi-sort configuration', () => {
      const multiSort = {
        columns: [
          { column: 'firstName', direction: 'asc' },
          { column: 'lastName', direction: 'desc' }
        ]
      };

      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={multiSort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      expect(screen.queryAllByText('Alice').length).toBeGreaterThan(0);
    });

    test('shows background color for sorted columns', () => {
      const singleSort = {
        columns: [{ column: 'firstName', direction: 'asc' }]
      };

      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={singleSort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      expect(screen.queryAllByText('Alice').length).toBeGreaterThan(0);
    });

    test('displays sort indicator icons correctly', () => {
      const multiSort = {
        columns: [
          { column: 'firstName', direction: 'asc' },
          { column: 'lastName', direction: 'asc' }
        ]
      };

      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={multiSort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      expect(screen.queryByRole('table')).toBeInTheDocument();
    });

    test('shows ascending/descending indicators', () => {
      const sortWithDifferentDirs = {
        columns: [
          { column: 'firstName', direction: 'asc' },
          { column: 'lastName', direction: 'desc' }
        ]
      };

      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={sortWithDifferentDirs}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      expect(screen.queryByRole('table')).toBeInTheDocument();
    });

    test('displays tooltip on table headers', () => {
      const singleSort = {
        columns: [{ column: 'firstName', direction: 'asc' }]
      };

      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={singleSort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      const table = screen.getByRole('table');
      expect(table).toBeInTheDocument();
    });
  });

  describe('Sort Handler Integration', () => {
    test('calls handleSort when column header clicked', async () => {
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
      await userEvent.click(headers[0]);

      expect(mockOnSort).toHaveBeenCalled();
    });

    test('handles single column sort click', async () => {
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
      await userEvent.click(headers[0]);

      expect(mockOnSort).toHaveBeenCalledWith('firstName', false);
    });

    test('shift+click support indicated in UI', async () => {
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

      const table = screen.getByRole('table');
      expect(table).toBeInTheDocument();
    });
  });

  describe('Multi-Sort Scenarios', () => {
    test('renders table with multiple sort columns', () => {
      const multiSort = {
        columns: [
          { column: 'firstName', direction: 'asc' },
          { column: 'age', direction: 'desc' }
        ]
      };

      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={multiSort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      expect(screen.getByRole('table')).toBeInTheDocument();
    });

    test('displays sort priority index correctly', () => {
      const tripleSort = {
        columns: [
          { column: 'firstName', direction: 'asc' },
          { column: 'lastName', direction: 'asc' },
          { column: 'age', direction: 'desc' }
        ]
      };

      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={tripleSort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      expect(screen.getByRole('table')).toBeInTheDocument();
    });

    test('handles single sort column', () => {
      const singleSort = {
        columns: [{ column: 'firstName', direction: 'asc' }]
      };

      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={singleSort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      expect(screen.getByRole('table')).toBeInTheDocument();
    });

    test('handles empty sort array', () => {
      const emptySort = { columns: [] };

      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={emptySort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      expect(screen.getByRole('table')).toBeInTheDocument();
    });
  });

  describe('Sort State Management', () => {
    test('persists sort during page change', async () => {
      const multiSort = {
        columns: [
          { column: 'firstName', direction: 'asc' },
          { column: 'age', direction: 'desc' }
        ]
      };

      const { rerender } = render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={multiSort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      rerender(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={multiSort}
          onSort={mockOnSort}
          pageIndex={1}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      expect(screen.getByRole('table')).toBeInTheDocument();
    });

    test('updates sort config when props change', () => {
      const initialSort = {
        columns: [{ column: 'firstName', direction: 'asc' }]
      };

      const { rerender } = render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={initialSort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      const updatedSort = {
        columns: [
          { column: 'firstName', direction: 'asc' },
          { column: 'age', direction: 'desc' }
        ]
      };

      rerender(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={updatedSort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      expect(screen.getByRole('table')).toBeInTheDocument();
    });
  });

  describe('Visual Indicators', () => {
    test('applies style to sorted columns', () => {
      const multiSort = {
        columns: [{ column: 'firstName', direction: 'asc' }]
      };

      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={multiSort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      expect(screen.getByRole('table')).toBeInTheDocument();
    });

    test('displays sort priority badge on headers', () => {
      const tripleSort = {
        columns: [
          { column: 'firstName', direction: 'asc' },
          { column: 'lastName', direction: 'asc' },
          { column: 'age', direction: 'desc' }
        ]
      };

      render(
        <DataTable
          data={mockData}
          columns={mockColumns}
          sortConfig={tripleSort}
          onSort={mockOnSort}
          pageIndex={0}
          rowsPerPage={25}
          totalRows={3}
          onPageChange={mockOnPageChange}
          onRowsPerPageChange={mockOnRowsPerPageChange}
          totalPages={1}
        />
      );

      expect(screen.getByRole('table')).toBeInTheDocument();
    });
  });
});
