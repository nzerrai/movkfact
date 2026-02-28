import React, { useReducer, useEffect, useMemo } from 'react';
import { DomainContext, domainReducer, initialState, ACTIONS } from './DomainContext';
import * as domainService from '../services/domainService';

export const DomainProvider = ({ children }) => {
  const [state, dispatch] = useReducer(domainReducer, initialState);

  // Load initial domains on component mount
  useEffect(() => {
    console.log('[DomainProvider] Component mounted, loading domains...');
    
    const loadInitialDomains = async () => {
      try {
        console.log('[DomainProvider] Fetching domains from API...');
        dispatch({ type: ACTIONS.LOAD_DOMAINS_START });
        
        const domains = await domainService.getDomains(0, 20);
        
        console.log('[DomainProvider] Domains loaded:', domains);
        dispatch({
          type: ACTIONS.LOAD_DOMAINS_SUCCESS,
          payload: domains,
        });
      } catch (error) {
        console.error('[DomainProvider] Error loading domains:', error);
        dispatch({
          type: ACTIONS.LOAD_DOMAINS_ERROR,
          payload: error.message || 'Failed to load domains',
        });
      }
    };

    loadInitialDomains();
  }, []);

  // Memoize context value to prevent unnecessary re-renders
  const value = useMemo(
    () => ({
      state,
      dispatch,
    }),
    [state]
  );

  return (
    <DomainContext.Provider value={value}>
      {children}
    </DomainContext.Provider>
  );
};
