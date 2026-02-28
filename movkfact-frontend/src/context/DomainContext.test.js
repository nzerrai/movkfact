import { ACTIONS, domainReducer, initialState } from './DomainContext';

describe('DomainContext Reducer', () => {
  describe('LOAD_DOMAINS_SUCCESS', () => {
    it('should load domains correctly on first page', () => {
      const mockDomains = [
        { id: 1, name: 'Domain1', description: 'Desc1' },
        { id: 2, name: 'Domain2', description: 'Desc2' },
        { id: 3, name: 'Domain3', description: 'Desc3' },
      ];

      const state = {
        ...initialState,
        pageSize: 20,
      };

      const action = {
        type: ACTIONS.LOAD_DOMAINS_SUCCESS,
        payload: mockDomains,
      };

      const result = domainReducer(state, action);

      expect(result.domains).toEqual(mockDomains);
      expect(result.loading).toBe(false);
      expect(result.error).toBeNull();
      // Since we got 3 items (< 20), offset should be 0 and hasMore should be false
      expect(result.offset).toBe(0);
      expect(result.hasMore).toBe(false);
    });

    it('should calculate offset correctly with full page load', () => {
      const mockDomains = Array.from({ length: 20 }, (_, i) => ({
        id: i + 1,
        name: `Domain${i + 1}`,
      }));

      const state = {
        ...initialState,
        pageSize: 20,
      };

      const action = {
        type: ACTIONS.LOAD_DOMAINS_SUCCESS,
        payload: mockDomains,
      };

      const result = domainReducer(state, action);

      // Should set offset to pageSize since we got full page
      expect(result.offset).toBe(20);
      expect(result.hasMore).toBe(true);
    });
  });

  describe('ADD_DOMAIN', () => {
    it('should add new domain to the beginning of list', () => {
      const state = {
        ...initialState,
        domains: [{ id: 1, name: 'Existing' }],
      };

      const newDomain = { id: 2, name: 'New Domain' };
      const action = {
        type: ACTIONS.ADD_DOMAIN,
        payload: newDomain,
      };

      const result = domainReducer(state, action);

      expect(result.domains[0]).toEqual(newDomain);
      expect(result.domains.length).toBe(2);
    });
  });

  describe('UPDATE_DOMAIN', () => {
    it('should update domain by id', () => {
      const state = {
        ...initialState,
        domains: [
          { id: 1, name: 'Original' },
          { id: 2, name: 'Other' },
        ],
      };

      const updatedDomain = { id: 1, name: 'Updated' };
      const action = {
        type: ACTIONS.UPDATE_DOMAIN,
        payload: updatedDomain,
      };

      const result = domainReducer(state, action);

      expect(result.domains[0]).toEqual(updatedDomain);
      expect(result.domains[1]).toEqual({ id: 2, name: 'Other' });
    });

    it('should handle update when domain not found', () => {
      const state = {
        ...initialState,
        domains: [{ id: 1, name: 'Domain' }],
      };

      const action = {
        type: ACTIONS.UPDATE_DOMAIN,
        payload: { id: 999, name: 'NotFound' },
      };

      const result = domainReducer(state, action);

      // Should not crash, domains should remain unchanged (no match)
      expect(result.domains.length).toBe(1);
      expect(result.domains[0]).toEqual({ id: 1, name: 'Domain' });
    });
  });

  describe('DELETE_DOMAIN', () => {
    it('should remove domain by id', () => {
      const state = {
        ...initialState,
        domains: [
          { id: 1, name: 'Domain1' },
          { id: 2, name: 'Domain2' },
          { id: 3, name: 'Domain3' },
        ],
      };

      const action = {
        type: ACTIONS.DELETE_DOMAIN,
        payload: 2,
      };

      const result = domainReducer(state, action);

      expect(result.domains.length).toBe(2);
      expect(result.domains.find((d) => d.id === 2)).toBeUndefined();
      expect(result.domains.find((d) => d.id === 1)).toBeDefined();
      expect(result.domains.find((d) => d.id === 3)).toBeDefined();
    });
  });

  describe('SET_SEARCH_TEXT', () => {
    it('should update search text', () => {
      const state = {
        ...initialState,
        offset: 40,
      };

      const action = {
        type: ACTIONS.SET_SEARCH_TEXT,
        payload: 'test search',
      };

      const result = domainReducer(state, action);

      expect(result.searchText).toBe('test search');
      // Offset is preserved in SET_SEARCH_TEXT (reset happens elsewhere)
      expect(result.offset).toBe(40);
    });
  });

  describe('LOAD_DOMAINS_START', () => {
    it('should set loading state', () => {
      const state = { ...initialState };

      const action = {
        type: ACTIONS.LOAD_DOMAINS_START,
      };

      const result = domainReducer(state, action);

      expect(result.loading).toBe(true);
      expect(result.error).toBeNull();
    });
  });

  describe('LOAD_DOMAINS_ERROR', () => {
    it('should set error message', () => {
      const state = { ...initialState };
      const errorMsg = 'Something went wrong';

      const action = {
        type: ACTIONS.LOAD_DOMAINS_ERROR,
        payload: errorMsg,
      };

      const result = domainReducer(state, action);

      expect(result.error).toBe(errorMsg);
      expect(result.loading).toBe(false);
    });
  });

  describe('LOAD_MORE_SUCCESS', () => {
    it('should append new domains and update offset', () => {
      const state = {
        ...initialState,
        domains: [{ id: 1, name: 'Domain1' }],
        offset: 0,
        pageSize: 20,
      };

      const newDomains = [
        { id: 2, name: 'Domain2' },
        { id: 3, name: 'Domain3' },
      ];

      const action = {
        type: ACTIONS.LOAD_MORE_SUCCESS,
        payload: newDomains,
      };

      const result = domainReducer(state, action);

      expect(result.domains.length).toBe(3);
      expect(result.domains[0]).toEqual({ id: 1, name: 'Domain1' });
      expect(result.domains[1]).toEqual({ id: 2, name: 'Domain2' });
      expect(result.offset).toBe(20);
    });

    it('should set hasMore based on full page load', () => {
      const mockDomains = Array.from({ length: 20 }, (_, i) => ({
        id: i + 21,
        name: `Domain${i + 21}`,
      }));

      const state = {
        ...initialState,
        domains: [{ id: 1, name: 'Domain1' }],
        offset: 0,
        pageSize: 20,
      };

      const action = {
        type: ACTIONS.LOAD_MORE_SUCCESS,
        payload: mockDomains,
      };

      const result = domainReducer(state, action);

      expect(result.hasMore).toBe(true);
      expect(result.offset).toBe(20);
    });
  });
});
