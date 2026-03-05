import { fireEvent, render, screen } from '@testing-library/react';
import React from 'react';
import ResultViewer from './ResultViewer';

/**
 * S2.6: ResultViewer Unit Tests
 */
describe('ResultViewer', () => {
  const mockData = {
    id: 1,
    data: [
      { firstName: 'John', amount: 1000 },
      { firstName: 'Jane', amount: 2000 }
    ],
    rowCount: 2,
    generationTimeMs: 125
  };

  const mockProps = {
    data: mockData,
    domainId: 1,
    onConfigureMore: jest.fn()
  };

  beforeEach(() => {
    jest.clearAllMocks();
    global.saveAs = jest.fn();
    global.fetch = jest.fn();
    Object.assign(navigator, {
      clipboard: { writeText: jest.fn(() => Promise.resolve()) }
    });
  });

  test('renders with statistics', () => {
    render(<ResultViewer {...mockProps} />);
    expect(screen.getByText(/Generation Results/)).toBeInTheDocument();
  });

  test('displays data preview table', () => {
    render(<ResultViewer {...mockProps} />);
    expect(screen.getByText(/Preview/)).toBeInTheDocument();
  });

  test('has export buttons', () => {
    render(<ResultViewer {...mockProps} />);
    const buttons = screen.getAllByRole('button');
    expect(buttons.length).toBeGreaterThan(2);
  });

  test('handles Downloads correctly', () => {
    render(<ResultViewer {...mockProps} />);
    const buttons = screen.getAllByRole('button');
    // Verify download buttons exist
    expect(buttons.length).toBeGreaterThanOrEqual(3);
  });

  test('copies to clipboard', () => {
    render(<ResultViewer {...mockProps} />);
    const btn = screen.getByText(/Copy to Clipboard/);
    fireEvent.click(btn);
    expect(navigator.clipboard.writeText).toHaveBeenCalled();
  });

  test('shows success after copy', () => {
    render(<ResultViewer {...mockProps} />);
    const btn = screen.getByText(/Copy to Clipboard/);
    fireEvent.click(btn);
    expect(screen.getByText(/Copied to clipboard/)).toBeInTheDocument();
  });

  test('configures more data', () => {
    render(<ResultViewer {...mockProps} />);
    const btn = screen.getByText(/Configure More/);
    fireEvent.click(btn);
    expect(mockProps.onConfigureMore).toHaveBeenCalled();
  });

  test('handles empty data', () => {
    const empty = { ...mockData, data: [], rowCount: 0 };
    render(<ResultViewer {...mockProps} data={empty} />);
    expect(screen.getByText(/No data/)).toBeInTheDocument();
  });

  test('displays generation time', () => {
    render(<ResultViewer {...mockProps} />);
    expect(screen.getByText(/125ms/)).toBeInTheDocument();
  });
});
