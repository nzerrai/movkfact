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

describe('FilterBar', () => {
  const mockColumns = ['firstName', 'lastName', 'email', 'age'];
  const mockOnFilterChange = jest.fn();
  const mockOnClearFilters = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders filter controls', () => {
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

  test('displays add filter button', () => {
    render(
      <FilterBar
        columns={mockColumns}
        filters={{}}
        onFilterChange={mockOnFilterChange}
        onClearFilters={mockOnClearFilters}
      />
    );
    expect(screen.getByRole('button', { name: /add filter/i })).toBeInTheDocument();
  });

  test('clears all filters', async () => {
    const initialFilters = { firstName: 'John', email: 'test@example.com' };
    
    render(
      <FilterBar
        columns={mockColumns}
        filters={initialFilters}
        onFilterChange={mockOnFilterChange}
        onClearFilters={mockOnClearFilters}
      />
    );

    const clearButton = screen.getByRole('button', { name: /clear/i });
    await userEvent.click(clearButton);

    expect(mockOnClearFilters).toHaveBeenCalled();
  });

  test('handles empty columns gracefully', () => {
    const { container } = render(
      <FilterBar
        columns={[]}
        filters={{}}
        onFilterChange={mockOnFilterChange}
        onClearFilters={mockOnClearFilters}
      />
    );

    expect(container).toBeInTheDocument();
  });

  test('renders with no active filters', () => {
    render(
      <FilterBar
        columns={mockColumns}
        filters={{}}
        onFilterChange={mockOnFilterChange}
        onClearFilters={mockOnClearFilters}
      />
    );

    const clearButtons = screen.queryAllByRole('button', { name: /clear/i });
    // When no filters, only "Add Filter" button exists, no "Clear" buttons
    const hasOnlyAddButton = clearButtons.length === 0;
    expect(hasOnlyAddButton || clearButtons.every(b => b.textContent.includes('Add'))).toBeTruthy();
  });

  test('renders filter input field', () => {
    render(
      <FilterBar
        columns={mockColumns}
        filters={{}}
        onFilterChange={mockOnFilterChange}
        onClearFilters={mockOnClearFilters}
      />
    );

    const inputs = screen.getAllByRole('textbox');
    expect(inputs.length).toBeGreaterThan(0);
  });
});
