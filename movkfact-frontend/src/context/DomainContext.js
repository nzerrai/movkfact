import React, { createContext } from 'react';

// Create context
export const DomainContext = createContext();

// Initial state - EXPORTED for use in provider
export const initialState = {
  domains: [],
  loading: false,
  error: null,
  searchText: '',
  pageSize: 20,
  offset: 0,
  hasMore: true,
};

// Action types
export const ACTIONS = {
  LOAD_DOMAINS_START: 'LOAD_DOMAINS_START',
  LOAD_DOMAINS_SUCCESS: 'LOAD_DOMAINS_SUCCESS',
  LOAD_DOMAINS_ERROR: 'LOAD_DOMAINS_ERROR',
  ADD_DOMAIN: 'ADD_DOMAIN',
  UPDATE_DOMAIN: 'UPDATE_DOMAIN',
  DELETE_DOMAIN: 'DELETE_DOMAIN',
  SET_SEARCH_TEXT: 'SET_SEARCH_TEXT',
  LOAD_MORE: 'LOAD_MORE',
  LOAD_MORE_SUCCESS: 'LOAD_MORE_SUCCESS',
};

// Reducer function
export const domainReducer = (state, action) => {
  switch (action.type) {
    // Load initial domains
    case ACTIONS.LOAD_DOMAINS_START:
      return {
        ...state,
        loading: true,
        error: null,
      };

    case ACTIONS.LOAD_DOMAINS_SUCCESS:
      return {
        ...state,
        domains: action.payload,
        loading: false,
        error: null,
        offset: action.payload.length === state.pageSize ? state.pageSize : 0,
        hasMore: action.payload.length === state.pageSize,
      };

    case ACTIONS.LOAD_DOMAINS_ERROR:
      return {
        ...state,
        loading: false,
        error: action.payload,
      };

    // Add new domain to list (prepend)
    case ACTIONS.ADD_DOMAIN:
      return {
        ...state,
        domains: [action.payload, ...state.domains],
      };

    // Update domain in list (find and replace)
    case ACTIONS.UPDATE_DOMAIN:
      return {
        ...state,
        domains: state.domains.map(domain =>
          domain.id === action.payload.id ? action.payload : domain
        ),
      };

    // Delete domain from list
    case ACTIONS.DELETE_DOMAIN:
      return {
        ...state,
        domains: state.domains.filter(domain => domain.id !== action.payload),
      };

    // Set search text
    case ACTIONS.SET_SEARCH_TEXT:
      return {
        ...state,
        searchText: action.payload,
      };

    // Load More pagination start
    case ACTIONS.LOAD_MORE:
      return {
        ...state,
        loading: true,
        error: null,
      };

    // Load More pagination success (append to existing domains)
    case ACTIONS.LOAD_MORE_SUCCESS:
      return {
        ...state,
        domains: [...state.domains, ...action.payload],
        loading: false,
        offset: state.offset + state.pageSize,
        hasMore: action.payload.length === state.pageSize,
      };

    default:
      return state;
  }
};

// Custom hook to use domain context
export const useDomainContext = () => {
  const context = React.useContext(DomainContext);
  if (!context) {
    throw new Error('useDomainContext must be used within a DomainProvider');
  }
  return context;
};
