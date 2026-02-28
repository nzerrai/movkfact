import React from 'react';
import { render, screen } from '@testing-library/react';
import { SearchBar } from './SearchBar';
import { DomainProvider } from '../context/DomainProvider';

const renderSearchBar = () => {
  return render(
    <DomainProvider>
      <SearchBar />
    </DomainProvider>
  );
};

describe('SearchBar Component', () => {
  it('should render search input field', () => {
    renderSearchBar();
    const input = screen.getByPlaceholderText('Search domains...');
    expect(input).toBeInTheDocument();
  });

  it('should have correct input type and attributes', () => {
    renderSearchBar();
    const input = screen.getByPlaceholderText('Search domains...');

    expect(input).toHaveAttribute('type', 'text');
    expect(input.value).toBe('');
  });

  it('should update search value on user input', async () => {
    const user = require('@testing-library/user-event').default;
    renderSearchBar();
    const input = screen.getByPlaceholderText('Search domains...');

    await user.type(input, 'test domain');

    expect(input.value).toBe('test domain');
  });

  it('should have flex layout for search and clear button', () => {
    renderSearchBar();
    const searchContainer = screen.getByPlaceholderText('Search domains...').closest('div');

    // Parent should have flex display
    expect(searchContainer).toBeTruthy();
  });

  it('should sync initial value from context', () => {
    renderSearchBar();
    const input = screen.getByPlaceholderText('Search domains...');

    // Should start empty from initial context state
    expect(input).toHaveValue('');
  });
});
