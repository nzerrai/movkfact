import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import SnippetBlock from './SnippetBlock';

const SNIPPETS = {
  curl: 'curl "http://localhost:8080/api/domains"',
  'JavaScript (fetch)': 'const response = await fetch("http://localhost:8080/api/domains");',
  'Python (requests)': 'import requests\nresponse = requests.get("http://localhost:8080/api/domains")',
};

describe('SnippetBlock', () => {
  beforeEach(() => {
    Object.assign(navigator, {
      clipboard: { writeText: jest.fn().mockResolvedValue(undefined) },
    });
  });

  afterEach(() => jest.clearAllMocks());

  it('renders first snippet (curl) by default', () => {
    render(<SnippetBlock snippets={SNIPPETS} />);
    expect(screen.getByText(/curl "http/)).toBeInTheDocument();
  });

  it('switches to JavaScript tab', () => {
    render(<SnippetBlock snippets={SNIPPETS} />);
    fireEvent.click(screen.getByText('JavaScript (fetch)'));
    expect(screen.getByText(/await fetch/)).toBeInTheDocument();
  });

  it('switches to Python tab', () => {
    render(<SnippetBlock snippets={SNIPPETS} />);
    fireEvent.click(screen.getByText('Python (requests)'));
    expect(screen.getByText(/import requests/)).toBeInTheDocument();
  });

  it('calls clipboard.writeText on copy click', async () => {
    render(<SnippetBlock snippets={SNIPPETS} />);
    fireEvent.click(screen.getByRole('button', { name: 'Copier' }));
    expect(navigator.clipboard.writeText).toHaveBeenCalledWith(SNIPPETS.curl);
  });

  it('shows "Copié !" feedback after copy', async () => {
    render(<SnippetBlock snippets={SNIPPETS} />);
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: 'Copier' }));
    });
    await waitFor(() => expect(screen.getByRole('button', { name: 'Copié !' })).toBeInTheDocument());
  });
});
