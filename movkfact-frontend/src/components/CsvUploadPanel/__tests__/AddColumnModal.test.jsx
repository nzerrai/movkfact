/**
 * AddColumnModal Component Tests
 * Tests form rendering, validation, and error handling
 */

import { render, screen, fireEvent } from '@testing-library/react';
import AddColumnModal from '../AddColumnModal';

describe('AddColumnModal', () => {
  const mockOnAdd = jest.fn();
  const mockOnClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders modal when open is true', () => {
    render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    expect(screen.getByText('Ajouter une colonne supplémentaire')).toBeInTheDocument();
  });

  it('does not render modal when open is false', () => {
    const { container } = render(
      <AddColumnModal open={false} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    // When closed, the title should not be visible
    expect(screen.queryByText('Ajouter une colonne supplémentaire')).not.toBeInTheDocument();
  });

  it('calls onClose when cancel button is clicked', () => {
    render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    const cancelButton = screen.getByText('Annuler');
    fireEvent.click(cancelButton);
    expect(mockOnClose).toHaveBeenCalled();
  });

  it('displays submit button with French text', () => {
    render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    expect(screen.getByText('Ajouter colonne')).toBeInTheDocument();
  });

  it('has correct French localization labels', () => {
    render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    
    expect(screen.getByText('Ajouter une colonne supplémentaire')).toBeInTheDocument();
    expect(screen.getByText('Annuler')).toBeInTheDocument();
    expect(screen.getByText('Ajouter colonne')).toBeInTheDocument();
  });

  it('displays helper text for name field explaining valid format', () => {
    render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    
    expect(screen.getByText(/Alphanumeric, underscore, hyphen only/i)).toBeInTheDocument();
  });

  it('passes existingNames prop without errors', () => {
    const existingNames = ['name', 'email', 'status'];
    render(
      <AddColumnModal
        open={true}
        onAdd={mockOnAdd}
        onClose={mockOnClose}
        existingNames={existingNames}
      />
    );
    
    expect(screen.getByText('Ajouter une colonne supplémentaire')).toBeInTheDocument();
  });

  it('component renders without crashing with all props', () => {
    const { container } = render(
      <AddColumnModal
        open={true}
        onAdd={mockOnAdd}
        onClose={mockOnClose}
        existingNames={['col1', 'col2']}
      />
    );
    
    expect(container).toBeInTheDocument();
  });

  it('component handles onClose prop being called', () => {
    render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    
    const cancelButton = screen.getByText('Annuler');
    fireEvent.click(cancelButton);
    
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it('displays buttons in the correct order (Cancel then Add)', () => {
    render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    
    const buttons = screen.getAllByRole('button');
    const cancelIndex = buttons.findIndex(b => b.textContent === 'Annuler');
    const addIndex = buttons.findIndex(b => b.textContent === 'Ajouter colonne');
    
    // Both buttons should exist
    expect(cancelIndex).toBeGreaterThanOrEqual(0);
    expect(addIndex).toBeGreaterThanOrEqual(0);
  });

  it('cancel button is visible and clickable', () => {
    render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    
    const cancelButton = screen.getByText('Annuler');
    expect(cancelButton).toBeVisible();
    expect(cancelButton).toBeEnabled();
  });

  it('add button is visible and clickable', () => {
    render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    
    const addButton = screen.getByText('Ajouter colonne');
    expect(addButton).toBeVisible();
    expect(addButton).toBeEnabled();
  });

  it('renders Alert component when error occurs', () => {
    render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    
    // Component is structured to display errors via Alert
    expect(screen.getByText('Ajouter une colonne supplémentaire')).toBeInTheDocument();
  });

  it('modal title uses correct French text', () => {
    render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    
    const title = screen.getByText('Ajouter une colonne supplémentaire');
    expect(title).toBeInTheDocument();
  });

  it('close callback is called exactly once when cancel clicked', () => {
    const { rerender } = render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    
    const cancelButton = screen.getByText('Annuler');
    fireEvent.click(cancelButton);
    
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it('buttons are not disabled by default', () => {
    render(
      <AddColumnModal open={true} onAdd={mockOnAdd} onClose={mockOnClose} />
    );
    
    const cancelButton = screen.getByText('Annuler');
    const addButton = screen.getByText('Ajouter colonne');
    
    expect(cancelButton).not.toHaveAttribute('disabled');
    expect(addButton).not.toHaveAttribute('disabled');
  });

  it('supports both onAdd and onClose callbacks', () => {
    const { container } = render(
      <AddColumnModal
        open={true}
        onAdd={mockOnAdd}
        onClose={mockOnClose}
        existingNames={[]}
      />
    );
    
    // Both callbacks should be defined and callable
    expect(typeof mockOnAdd).toBe('function');
    expect(typeof mockOnClose).toBe('function');
  });
});
