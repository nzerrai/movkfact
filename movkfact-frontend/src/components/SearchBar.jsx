import React, { useState, useCallback, useEffect, useRef } from 'react';
import { Box, TextField, IconButton } from '@mui/material';
import ClearIcon from '@mui/icons-material/Clear';
import { useDomainContext } from '../context/DomainContext';
import { ACTIONS } from '../context/DomainContext';

/**
 * SearchBar component for filtering domains
 * Debounces search input for 300ms before updating context
 */
export const SearchBar = () => {
  const { state, dispatch } = useDomainContext();
  const [searchValue, setSearchValue] = useState(state.searchText);

  // Debounce timer ref and mount check ref
  const debounceTimerRef = useRef(null);
  const isMountedRef = useRef(true);

  // Handle input change with debounce
  const handleSearchChange = useCallback((e) => {
    const value = e.target.value;
    setSearchValue(value);

    // Clear previous timer
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    // Set new timer for debounced dispatch
    debounceTimerRef.current = setTimeout(() => {
      // Only dispatch if component is still mounted
      if (isMountedRef.current) {
        dispatch({
          type: ACTIONS.SET_SEARCH_TEXT,
          payload: value,
        });
      }
    }, 300);
  }, [dispatch]);

  // Clear search
  const handleClear = useCallback(() => {
    setSearchValue('');
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }
    // Only dispatch if component is still mounted
    if (isMountedRef.current) {
      dispatch({
        type: ACTIONS.SET_SEARCH_TEXT,
        payload: '',
      });
    }
  }, [dispatch]);

  // Cleanup timer and mount state on unmount
  useEffect(() => {
    return () => {
      isMountedRef.current = false;
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, []);

  return (
    <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
      <TextField
        placeholder="Search domains..."
        value={searchValue}
        onChange={handleSearchChange}
        size="small"
        sx={{ flex: 1 }}
      />
      {searchValue && (
        <IconButton onClick={handleClear} size="small" title="Clear search">
          <ClearIcon />
        </IconButton>
      )}
    </Box>
  );
};

export default SearchBar;
