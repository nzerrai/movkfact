import { useState, useCallback } from 'react';

/**
 * Generic API call hook with error handling
 * @param {Function} apiFunction - The API function to call (async)
 * @returns {Object} { data, error, loading, execute }
 */
export const useApi = () => {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  /**
   * Execute an API function with error handling
   * @param {Function} apiFunction - The API function to call
   * @param {*} ...args - Arguments to pass to the API function
   * @returns {Promise<any>} The API response data
   */
  const execute = useCallback(async (apiFunction, ...args) => {
    console.log('🔍 useApi.execute called with function:', apiFunction.name, 'args:', args);
    setLoading(true);
    setError(null);
    try {
      const result = await apiFunction(...args);
      console.log('✅ useApi.execute success:', result);
      setData(result);
      return result;
    } catch (err) {
      console.error('❌ useApi.execute error:', err);
      // Parse error response from backend
      const errorData = err.response?.data;
      const status = err.response?.status;

      let errorMessage = 'An error occurred';

      // Parse different error responses based on status
      if (status === 400) {
        // Validation error - return field errors if available
        errorMessage = errorData?.error || 'Validation error';
      } else if (status === 404) {
        errorMessage = errorData?.error || 'Not found';
      } else if (status === 409) {
        // Conflict error (duplicate name, version mismatch)
        errorMessage = errorData?.error || 'Conflict: Resource already exists';
      } else if (status === 500) {
        errorMessage = 'Server error. Please try again later.';
      } else {
        errorMessage = errorData?.error || errorData?.message || err.message;
      }

      const errorObject = {
        message: errorMessage,
        status,
        data: errorData,
      };

      console.error('❌ useApi.execute parsed error:', errorObject);
      setError(errorObject);
      throw errorObject;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    data,
    error,
    loading,
    execute,
  };
};

export default useApi;
