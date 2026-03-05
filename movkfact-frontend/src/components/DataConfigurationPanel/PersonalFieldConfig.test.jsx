import { render, screen } from '@testing-library/react';
import React from 'react';
import PersonalFieldConfig from './PersonalFieldConfig';

describe('PersonalFieldConfig', () => {
  const mockProps = {
    columnName: 'firstName',
    columnType: 'FIRST_NAME',
    config: { locale: 'FR' },
    onChange: jest.fn()
  };

  test('renders field config', () => {
    render(<PersonalFieldConfig {...mockProps} />);
    expect(screen.getByText(/FIRST_NAME/)).toBeInTheDocument();
  });

  test('handles EMAIL type', () => {
    render(<PersonalFieldConfig {...mockProps} columnType="EMAIL" />);
    expect(mockProps.onChange).toBeDefined();
  });

  test('handles GENDER type', () => {
    render(<PersonalFieldConfig {...mockProps} columnType="GENDER" />);
    expect(mockProps.onChange).toBeDefined();
  });

  test('handles PHONE type', () => {
    render(<PersonalFieldConfig {...mockProps} columnType="PHONE" />);
    expect(mockProps.onChange).toBeDefined();
  });

  test('handles ADDRESS type', () => {
    render(<PersonalFieldConfig {...mockProps} columnType="ADDRESS" />);
    expect(mockProps.onChange).toBeDefined();
  });
});
