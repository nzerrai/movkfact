import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DomainForm } from './DomainForm';

const mockOnSubmit = jest.fn();
const mockOnCancel = jest.fn();

describe('DomainForm Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render form inputs', () => {
    render(<DomainForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} loading={false} error={null} />);

    expect(screen.getByLabelText(/domain name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
  });

  it('should have save and cancel buttons', () => {
    render(<DomainForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} loading={false} error={null} />);

    const buttons = screen.getAllByRole('button');
    expect(buttons.length).toBeGreaterThanOrEqual(2);
  });

  it('should start with empty values', () => {
    render(<DomainForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} loading={false} error={null} />);

    expect(screen.getByLabelText(/domain name/i)).toHaveValue('');
  });

  it('should populate fields with initial data', () => {
    const initialData = {
      id: 1,
      name: 'Test Domain',
      description: 'Test Description',
    };

    render(
      <DomainForm
        initialData={initialData}
        onSubmit={mockOnSubmit}
        onCancel={mockOnCancel}
        loading={false}
        error={null}
      />
    );

    expect(screen.getByLabelText(/domain name/i)).toHaveValue('Test Domain');
  });

  it('should have description with maxLength attribute', () => {
    render(<DomainForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} loading={false} error={null} />);

    const descInput = screen.getByLabelText(/description/i);
    expect(descInput).toHaveAttribute('maxLength', '2000');
  });

  it('should display character counter', () => {
    render(<DomainForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} loading={false} error={null} />);

    expect(screen.getByText(/\/2000 characters/i)).toBeInTheDocument();
  });

  it('should disable fields when loading', () => {
    render(<DomainForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} loading={true} error={null} />);

    expect(screen.getByLabelText(/domain name/i)).toBeDisabled();
    expect(screen.getByLabelText(/description/i)).toBeDisabled();
  });

  it('should handle field change', async () => {
    const user = userEvent.default || userEvent;
    render(<DomainForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} loading={false} error={null} />);

    const nameInput = screen.getByLabelText(/domain name/i);
    await user.type(nameInput, 'New Domain');

    expect(nameInput).toHaveValue('New Domain');
  });

  it('should render with error state', () => {
    const error = {
      status: 400,
      message: 'Validation error',
      data: { fieldErrors: { name: 'Required' } },
    };

    const { container } = render(
      <DomainForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} loading={false} error={error} />
    );

    // Form should still be visible with error state
    expect(screen.getByLabelText(/domain name/i)).toBeInTheDocument();
    expect(container).toBeTruthy();
  });

  it('should render multiline description', () => {
    render(<DomainForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} loading={false} error={null} />);

    const descInput = screen.getByLabelText(/description/i);
    expect(descInput).toHaveAttribute('rows');
  });
});
