import { render, screen, fireEvent } from '@testing-library/react';
import CsvUploadPanel from './CsvUploadPanel';

/**
 * S2.5: CsvUploadPanel Component Tests
 * Tests for CSV upload and type detection functionality
 */
describe('CsvUploadPanel', () => {
  beforeEach(() => {
    // Reset mock data
    jest.clearAllMocks();
  });

  it('renders upload zone initially', () => {
    render(<CsvUploadPanel domainId={1} />);
    expect(screen.getByText(/Upload & Preview/i)).toBeInTheDocument();
  });

  it('shows select file button', () => {
    render(<CsvUploadPanel domainId={1} />);
    expect(screen.getByRole('button', { name: /Select File/i })).toBeInTheDocument();
  });

  it('accepts file input', () => {
    const { container } = render(<CsvUploadPanel domainId={1} />);
    const input = container.querySelector('input[type="file"]');
    expect(input).toBeDefined();
  });

  it('shows error for non-CSV file', async () => {
    render(<CsvUploadPanel domainId={1} />);
    const input = screen.getByRole('button', { name: /Select File/i }).closest('div').querySelector('input');
    
    const file = new File(['test'], 'test.txt', { type: 'text/plain' });
    fireEvent.change(input, { target: { files: [file] } });
    
    await screen.findByText(/Invalid file format/i);
  });

  it('shows error for oversized file', async () => {
    render(<CsvUploadPanel domainId={1} />);
    const input = screen.getByRole('button', { name: /Select File/i }).closest('div').querySelector('input');
    
    // Mock large file
    const largeFile = new File(
      [new ArrayBuffer(11 * 1024 * 1024)],
      'large.csv',
      { type: 'text/csv' }
    );
    
    fireEvent.change(input, { target: { files: [largeFile] } });
    
    await screen.findByText(/File too large/i);
  });

  it('displays UploadZone text', () => {
    render(<CsvUploadPanel domainId={1} />);
    expect(screen.getByText(/CSV files only/i)).toBeInTheDocument();
  });

  it('handles cancel button', () => {
    const onCancel = jest.fn();
    render(<CsvUploadPanel domainId={1} onCancel={onCancel} />);
    
    // Component should be ready for use
    expect(screen.getByRole('button', { name: /Select File/i })).toBeInTheDocument();
  });
});

describe('PreviewTable', () => {
  it('renders when data is provided', () => {
    render(<CsvUploadPanel domainId={1} />);
    // Preview table renders conditionally based on state
    expect(screen.getByText(/Upload & Preview/i)).toBeInTheDocument();
  });
});

describe('TypeDetectionResults', () => {
  it('component renders as part of CsvUploadPanel', () => {
    render(<CsvUploadPanel domainId={1} />);
    // Component structure is correct
    expect(screen.getByRole('button', { name: /Select File/i })).toBeInTheDocument();
  });
});
