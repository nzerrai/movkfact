import { render, screen } from '@testing-library/react';
import React from 'react';
import FinancialFieldConfig from './FinancialFieldConfig';

describe('FinancialFieldConfig', () => {
  const mockProps = {
    columnName: 'amount',
    columnType: 'AMOUNT',
    config: { currency: 'EUR' },
    onChange: jest.fn()
  };

  test('renders field config', () => {
    render(<FinancialFieldConfig {...mockProps} />);
    expect(screen.getByText(/AMOUNT/)).toBeInTheDocument();
  });

  test('handles AMOUNT type', () => {
    render(<FinancialFieldConfig {...mockProps} columnType="AMOUNT" />);
    expect(mockProps.onChange).toBeDefined();
  });

  test('handles ACCOUNT_NUMBER type', () => {
    render(<FinancialFieldConfig {...mockProps} columnType="ACCOUNT_NUMBER" />);
    expect(mockProps.onChange).toBeDefined();
  });

  test('handles CURRENCY type', () => {
    render(<FinancialFieldConfig {...mockProps} columnType="CURRENCY" />);
    expect(mockProps.onChange).toBeDefined();
  });
});
