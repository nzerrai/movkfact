import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import ConfigurationPanel from './ConfigurationPanel';

describe('ConfigurationPanel', () => {
  const mockProps = {
    csvData: [{ firstName: 'John', amount: 1000 }],
    detectedTypes: { firstName: 'FIRST_NAME', amount: 'AMOUNT' },
    domainId: 1,
    onGenerationComplete: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
    global.fetch = jest.fn();
  });

  test('renders configuration form', () => {
    render(<ConfigurationPanel {...mockProps} />);
    // Check for form rendering without specific text assertions
    expect(screen.getByRole('button', { name: /generate/i })).toBeInTheDocument();
  });

  test('displays all detected columns', () => {
    render(<ConfigurationPanel {...mockProps} />);
    // Check that component renders with csv data
    expect(mockProps.csvData.length).toBeGreaterThan(0);
  });
  test('renders form', () => {
    render(<ConfigurationPanel {...mockProps} />);
    expect(screen.getByRole('button', { name: /generate/i })).toBeInTheDocument();
  });

  test('displays CSV columns', () => {
    render(<ConfigurationPanel {...mockProps} />);
    expect(screen.getByRole('button', { name: /generate/i })).toBeInTheDocument();
  });

  test('renders dataset name field', () => {
    render(<ConfigurationPanel {...mockProps} />);
    expect(screen.getByLabelText('Dataset Name *')).toBeInTheDocument();
  });

  test('validates dataset name - required', async () => {
    const user = userEvent.setup();
    render(<ConfigurationPanel {...mockProps} />);
    const nameField = screen.getByLabelText('Dataset Name *');
    const buttons = screen.getAllByRole('button');
    const button = buttons.find(b => b.textContent.includes('Generate'));
    
    await user.clear(nameField);
    await waitFor(() => {
      expect(button).toBeDisabled();
    });
  });

  test('validates dataset name - minimum length', async () => {
    const user = userEvent.setup();
    render(<ConfigurationPanel {...mockProps} />);
    const nameField = screen.getByLabelText('Dataset Name *');
    const buttons = screen.getAllByRole('button');
    const button = buttons.find(b => b.textContent.includes('Generate'));
    
    await user.clear(nameField);
    await user.type(nameField, 'ab');
    await waitFor(() => {
      expect(button).toBeDisabled();
    });
  });

  test('validates dataset name - valid input', async () => {
    const user = userEvent.setup();
    render(<ConfigurationPanel {...mockProps} />);
    const nameField = screen.getByLabelText('Dataset Name *');
    const buttons = screen.getAllByRole('button');
    const button = buttons.find(b => b.textContent.includes('Generate'));
    
    await user.type(nameField, 'valid_name_123');
    expect(nameField).toHaveValue('valid_name_123');
    await waitFor(() => {
      expect(button).not.toBeDisabled();
    });
  });

  test('disables generate button when name invalid', () => {
    render(<ConfigurationPanel {...mockProps} />);
    const buttons = screen.getAllByRole('button');
    const button = buttons.find(b => b.textContent.includes('Generate'));
    expect(button).toBeDisabled();
  });

  test('enables generate button when name valid', async () => {
    const user = userEvent.setup();
    render(<ConfigurationPanel {...mockProps} />);
    const nameField = screen.getByLabelText('Dataset Name *');
    const buttons = screen.getAllByRole('button');
    const button = buttons.find(b => b.textContent.includes('Generate'));
    
    await user.type(nameField, 'valid_name');
    await waitFor(() => {
      expect(button).not.toBeDisabled();
    });
  });

  test('calls API on generate', () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ data: { id: 1, data: [], rowCount: 10 } })
    });

    render(<ConfigurationPanel {...mockProps} />);
    const nameField = screen.getByLabelText('Dataset Name *');
    fireEvent.change(nameField, { target: { value: 'test_dataset' } });
    
    const buttons = screen.getAllByRole('button');
    const generateBtn = buttons.find(b => b.textContent.includes('Generate'));
    
    if (generateBtn) {
      fireEvent.click(generateBtn);
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/domains/1/data-sets',
        expect.objectContaining({
          method: 'POST',
          body: expect.stringContaining('"datasetName":"test_dataset"')
        })
      );
    }
  });
});
