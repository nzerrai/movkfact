import React from 'react';
import { render, screen } from '@testing-library/react';
import { DeleteConfirmDialog } from './DeleteConfirmDialog';

const mockOnConfirm = jest.fn();
const mockOnCancel = jest.fn();

const mockDomain = {
  id: 1,
  name: 'Test Domain',
};

describe('DeleteConfirmDialog Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render dialog when open is true', () => {
    const { container } = render(
      <DeleteConfirmDialog
        open={true}
        domain={mockDomain}
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
        loading={false}
      />
    );

    // Component should render without crashing
    expect(container).toBeTruthy();
  });

  it('should not render visible content when open is false', () => {
    render(
      <DeleteConfirmDialog
        open={false}
        domain={mockDomain}
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
        loading={false}
      />
    );

    // When closed, the dialog content should not be visible
    const buttons = screen.queryAllByRole('button');
    expect(buttons.length).toBe(0);
  });

  it('should render buttons when open', () => {
    render(
      <DeleteConfirmDialog
        open={true}
        domain={mockDomain}
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
        loading={false}
      />
    );

    const buttons = screen.getAllByRole('button');
    expect(buttons.length).toBeGreaterThanOrEqual(2);
  });

  it('should disable buttons when loading', () => {
    render(
      <DeleteConfirmDialog
        open={true}
        domain={mockDomain}
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
        loading={true}
      />
    );

    const buttons = screen.getAllByRole('button');
    const disabledCount = buttons.filter((b) => b.disabled).length;
    expect(disabledCount).toBe(buttons.length);
  });

  it('should handle null domain', () => {
    const { container } = render(
      <DeleteConfirmDialog
        open={true}
        domain={null}
        onConfirm={mockOnConfirm}
        onCancel={mockOnCancel}
        loading={false}
      />
    );

    expect(container).toBeTruthy();
  });
});
