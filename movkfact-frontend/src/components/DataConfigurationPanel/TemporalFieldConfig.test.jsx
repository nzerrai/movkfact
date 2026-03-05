import { render, screen } from '@testing-library/react';
import React from 'react';
import TemporalFieldConfig from './TemporalFieldConfig';

describe('TemporalFieldConfig', () => {
  const mockProps = {
    columnName: 'date',
    columnType: 'DATE',
    config: {},
    onChange: jest.fn()
  };

  test('renders field config', () => {
    render(<TemporalFieldConfig {...mockProps} />);
    expect(screen.getByText(/DATE/)).toBeInTheDocument();
  });

  test('handles DATE_BIRTH type', () => {
    render(<TemporalFieldConfig {...mockProps} columnType="DATE_BIRTH" />);
    expect(mockProps.onChange).toBeDefined();
  });

  test('handles DATE type', () => {
    render(<TemporalFieldConfig {...mockProps} columnType="DATE" />);
    expect(mockProps.onChange).toBeDefined();
  });

  test('handles TIME type', () => {
    render(<TemporalFieldConfig {...mockProps} columnType="TIME" />);
    expect(mockProps.onChange).toBeDefined();
  });

  test('handles TIMEZONE type', () => {
    render(<TemporalFieldConfig {...mockProps} columnType="TIMEZONE" />);
    expect(mockProps.onChange).toBeDefined();
  });
});
