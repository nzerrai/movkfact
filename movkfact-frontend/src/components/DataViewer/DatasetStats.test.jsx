import React from 'react';
import { render, screen } from '@testing-library/react';
import DatasetStats from './DatasetStats';

describe('DatasetStats', () => {
  const mockStats = {
    totalRows: 100,
    totalColumns: 5,
    nullCounts: { col1: 2, col2: 0, col3: 5 },
    columnTypes: {
      col1: 'PERSONAL',
      col2: 'FINANCIAL',
      col3: 'TEMPORAL',
      col4: 'PERSONAL',
      col5: 'NUMERIC'
    }
  };

  test('renders dataset stats card', () => {
    render(<DatasetStats stats={mockStats} />);
    // Check for the main stat elements
    expect(screen.getByText('Total Rows')).toBeInTheDocument();
  });

  test('displays row and column counts', () => {
    render(<DatasetStats stats={mockStats} />);
    expect(screen.getByText('100')).toBeInTheDocument();
    expect(screen.getByText('5')).toBeInTheDocument();
  });

  test('displays type distribution badges', () => {
    render(<DatasetStats stats={mockStats} />);
    expect(screen.getByText('Total Rows')).toBeInTheDocument();
    expect(screen.getByText('Total Columns')).toBeInTheDocument();
  });
});
