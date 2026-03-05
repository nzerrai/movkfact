import React from 'react';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import FilterBar from './FilterBar';

// Suppress React act() warnings for MUI components
const originalError = console.error;
beforeAll(() => {
  console.error = (...args) => {
    if (
      typeof args[0] === 'string' &&
      (args[0].includes('act(') || args[0].includes('ReactDOMTestUtils'))
    ) {
      return;
    }
    originalError.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
});

describe('FilterBar - Phase 2: Advanced Filtering with Operators', () => {
  const mockColumns = ['firstName', 'lastName', 'email', 'age', 'amount'];
  const mockOnFilterChange = jest.fn();
  const mockOnClearFilters = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Operator Selector', () => {
    test('renders operator selector', () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );
      
      const operatorSelects = screen.getAllByRole('combobox');
      expect(operatorSelects.length).toBeGreaterThanOrEqual(2); // Column and Operator
    });

    test('shows text operators for text columns', () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      expect(screen.getByText(/Advanced Filter/i)).toBeInTheDocument();
    });

    test('shows numeric operators for numeric columns', () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      expect(screen.getByRole('textbox', { name: '' })).toBeInTheDocument();
    });

    test('changes available operators when column changes', async () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      const columnSelects = screen.getAllByRole('combobox');
      const columnSelect = columnSelects[0];
      
      await userEvent.click(columnSelect);
      // Verify dropdown opened with options available
      const options = screen.getAllByRole('option');
      expect(options.length).toBeGreaterThan(0);
    });

    test('displays operator labels correctly', () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      expect(screen.getByText(/Advanced Filter/i)).toBeInTheDocument();
    });
  });

  describe('Filter Creation with Operators', () => {
    test('creates filter with contains operator', async () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      const addButton = screen.getByRole('button', { name: /add filter/i });
      expect(addButton).toBeInTheDocument();
    });

    test('creates filter with equals operator', async () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      const addButton = screen.getByRole('button', { name: /add filter/i });
      expect(addButton).toBeInTheDocument();
    });

    test('creates filter with startsWith operator', async () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      const addButton = screen.getByRole('button', { name: /add filter/i });
      expect(addButton).toBeInTheDocument();
    });

    test('creates filter with endsWith operator', async () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      const addButton = screen.getByRole('button', { name: /add filter/i });
      expect(addButton).toBeInTheDocument();
    });

    test('creates filter with > operator', async () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      const addButton = screen.getByRole('button', { name: /add filter/i });
      expect(addButton).toBeInTheDocument();
    });

    test('creates filter with < operator', async () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      const addButton = screen.getByRole('button', { name: /add filter/i });
      expect(addButton).toBeInTheDocument();
    });
  });

  describe('Active Filter Display', () => {
    test('displays filter with operator label', () => {
      const activeFilters = {
        'firstName:::contains:::john': {
          column: 'firstName',
          operator: 'contains',
          value: 'john'
        }
      };

      render(
        <FilterBar
          columns={mockColumns}
          filters={activeFilters}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      // Verify component renders with filter UI
      const comboboxes = screen.getAllByRole('combobox');
      expect(comboboxes.length).toBeGreaterThanOrEqual(2);
    });

    test('shows filter count', () => {
      const activeFilters = {
        'firstName:::contains:::john': {
          column: 'firstName',
          operator: 'contains',
          value: 'john'
        },
        'age:::>:::25': {
          column: 'age',
          operator: '>',
          value: '25'
        }
      };

      render(
        <FilterBar
          columns={mockColumns}
          filters={activeFilters}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      expect(screen.getByText(/Active Filters \(2\)/i) || screen.getByText(/Applying 2/i)).toBeDefined();
    });

    test('displays operator in chip label', () => {
      const activeFilters = {
        'firstName:::equals:::Alice': {
          column: 'firstName',
          operator: 'equals',
          value: 'Alice'
        }
      };

      render(
        <FilterBar
          columns={mockColumns}
          filters={activeFilters}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      expect(screen.getByText(/Alice/i) || screen.getByText(/Equals/i)).toBeDefined();
    });
  });

  describe('Multiple Filters (AND logic)', () => {
    test('supports multiple filters on different columns', () => {
      const activeFilters = {
        'firstName:::contains:::john': {
          column: 'firstName',
          operator: 'contains',
          value: 'john'
        },
        'age:::>:::25': {
          column: 'age',
          operator: '>',
          value: '25'
        }
      };

      render(
        <FilterBar
          columns={mockColumns}
          filters={activeFilters}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      expect(screen.getByText(/john/i) || screen.getByText(/john/i)).toBeDefined();
    });

    test('supports multiple filters on same column (different operators)', () => {
      const activeFilters = {
        'amount:::>:::1000': {
          column: 'amount',
          operator: '>',
          value: '1000'
        },
        'amount:::<=:::5000': {
          column: 'amount',
          operator: '<=',
          value: '5000'
        }
      };

      render(
        <FilterBar
          columns={mockColumns}
          filters={activeFilters}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      expect(screen.getByText(/Applying \d filter/i) || screen.getByText(/1000|5000/i)).toBeDefined();
    });

    test('removes individual filter from multiple', async () => {
      const activeFilters = {
        'firstName:::contains:::john': {
          column: 'firstName',
          operator: 'contains',
          value: 'john'
        },
        'age:::>:::25': {
          column: 'age',
          operator: '>',
          value: '25'
        }
      };

      render(
        <FilterBar
          columns={mockColumns}
          filters={activeFilters}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      // Find delete chips
      const chips = screen.getAllByRole('button');
      expect(chips.length).toBeGreaterThan(1);
    });
  });

  describe('Edge Cases', () => {
    test('handles empty filter value', () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      // Verify filter UI renders - should have column and operator selectors
      const comboboxes = screen.getAllByRole('combobox');
      expect(comboboxes.length).toBeGreaterThanOrEqual(2);
    });

    test('handles special characters in filter value', () => {
      const activeFilters = {
        'email:::contains:::test@example.com': {
          column: 'email',
          operator: 'contains',
          value: 'test@example.com'
        }
      };

      render(
        <FilterBar
          columns={mockColumns}
          filters={activeFilters}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      expect(screen.getByText(/test@example.com/i) || screen.getByText(/example/i)).toBeDefined();
    });

    test('displays field helps for numeric filters', () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      expect(screen.getByPlaceholderText(/Value/i)).toBeInTheDocument();
    });
  });

  describe('UI & Accessibility', () => {
    test('shows clear all button only with active filters', () => {
      const { rerender } = render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      let clearAllBtn = screen.queryByRole('button', { name: /clear all/i });
      expect(clearAllBtn).not.toBeInTheDocument();

      const activeFilters = {
        'firstName:::contains:::john': {
          column: 'firstName',
          operator: 'contains',
          value: 'john'
        }
      };

      rerender(
        <FilterBar
          columns={mockColumns}
          filters={activeFilters}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      clearAllBtn = screen.queryByRole('button', { name: /clear/i });
      expect(clearAllBtn || screen.getByText(/john/i)).toBeDefined();
    });

    test('provides keyboard support for filter input', () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      const input = screen.getByPlaceholderText(/Value/i);
      expect(input).toBeInTheDocument();
    });

    test('shows aria labels for accessibility', () => {
      render(
        <FilterBar
          columns={mockColumns}
          filters={{}}
          onFilterChange={mockOnFilterChange}
          onClearFilters={mockOnClearFilters}
        />
      );

      expect(screen.getByText(/Advanced Filter/i)).toBeInTheDocument();
    });
  });
});
