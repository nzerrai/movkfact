import { renderHook, act, waitFor } from '@testing-library/react';
import { useApi } from './useApi';

describe('useApi Hook', () => {
  const mockAsyncFunction = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should initialize with default state', () => {
    const { result } = renderHook(() => useApi());

    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
    expect(result.current.data).toBeNull();
  });

  it('should handle successful async call', async () => {
    const mockResponse = { data: 'success' };
    mockAsyncFunction.mockResolvedValue(mockResponse);
    const { result } = renderHook(() => useApi());

    let response;
    await act(async () => {
      response = await result.current.execute(mockAsyncFunction, 'arg1');
    });

    expect(response).toEqual(mockResponse);
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
    expect(result.current.data).toEqual(mockResponse);
  });

  it('should show loading during execution', async () => {
    let resolveFunc;
    const delayedPromise = new Promise((resolve) => {
      resolveFunc = resolve;
    });
    mockAsyncFunction.mockReturnValue(delayedPromise);

    const { result } = renderHook(() => useApi());

    act(() => {
      result.current.execute(mockAsyncFunction);
    });

    // Loading should be true immediately
    expect(result.current.loading).toBe(true);
    expect(result.current.error).toBeNull();

    act(() => {
      resolveFunc({ data: 'result' });
    });

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });
  });

  it('should handle 400 validation errors', async () => {
    const mockError = new Error('Validation failed');
    mockError.response = {
      status: 400,
      data: { error: 'Bad request', fieldErrors: { name: 'Required' } },
    };
    mockAsyncFunction.mockRejectedValue(mockError);

    const { result } = renderHook(() => useApi());

    await act(async () => {
      try {
        await result.current.execute(mockAsyncFunction);
      } catch (error) {
        expect(error.status).toBe(400);
        expect(error.message).toBe('Bad request');
        expect(error.data).toHaveProperty('fieldErrors');
      }
    });

    expect(result.current.loading).toBe(false);
    expect(result.current.error?.status).toBe(400);
  });

  it('should handle 409 conflict errors specifically', async () => {
    const mockError = new Error('Conflict');
    mockError.response = {
      status: 409,
      data: { error: 'Resource already exists', reason: 'Duplicate name' },
    };
    mockAsyncFunction.mockRejectedValue(mockError);

    const { result } = renderHook(() => useApi());

    await act(async () => {
      try {
        await result.current.execute(mockAsyncFunction);
      } catch (error) {
        expect(error.status).toBe(409);
      }
    });

    await waitFor(() => {
      expect(result.current.error.status).toBe(409);
    });
  });

  it('should handle 404 not found errors', async () => {
    const mockError = new Error('Not found');
    mockError.response = {
      status: 404,
      data: { error: 'Domain not found' },
    };
    mockAsyncFunction.mockRejectedValue(mockError);

    const { result } = renderHook(() => useApi());

    await act(async () => {
      try {
        await result.current.execute(mockAsyncFunction);
      } catch (error) {
        expect(error.status).toBe(404);
      }
    });

    expect(result.current.error?.status).toBe(404);
  });

  it('should handle 500 server errors', async () => {
    const mockError = new Error('Server error');
    mockError.response = {
      status: 500,
      data: null,
    };
    mockAsyncFunction.mockRejectedValue(mockError);

    const { result } = renderHook(() => useApi());

    await act(async () => {
      try {
        await result.current.execute(mockAsyncFunction);
      } catch (error) {
        expect(error.status).toBe(500);
      }
    });

    expect(result.current.error?.message).toBe('Server error. Please try again later.');
  });

  it('should clear error on successful retry', async () => {
    const mockError = new Error('Error');
    mockError.response = {
      status: 400,
      data: { error: 'Validation failed' },
    };
    const mockResponse = { data: 'success' };

    mockAsyncFunction.mockRejectedValueOnce(mockError);
    mockAsyncFunction.mockResolvedValueOnce(mockResponse);

    const { result } = renderHook(() => useApi());

    // First call fails
    await act(async () => {
      try {
        await result.current.execute(mockAsyncFunction);
      } catch (error) {
        expect(error).toBeDefined();
      }
    });

    expect(result.current.error?.status).toBe(400);

    // Second call succeeds
    await act(async () => {
      await result.current.execute(mockAsyncFunction);
    });

    await waitFor(() => {
      expect(result.current.error).toBeNull();
      expect(result.current.data).toEqual(mockResponse);
    });
  });

  it('should pass multiple arguments to async function', async () => {
    const mockResponse = { id: 1 };
    mockAsyncFunction.mockResolvedValue(mockResponse);
    const { result } = renderHook(() => useApi());

    await act(async () => {
      await result.current.execute(mockAsyncFunction, 'arg1', 'arg2', { id: 1 });
    });

    expect(mockAsyncFunction).toHaveBeenCalledWith('arg1', 'arg2', { id: 1 });
  });

  it('should handle network errors without response', async () => {
    const mockError = new Error('Network error');
    mockAsyncFunction.mockRejectedValue(mockError);

    const { result } = renderHook(() => useApi());

    await act(async () => {
      try {
        await result.current.execute(mockAsyncFunction);
      } catch (error) {
        expect(error.message).toBe('Network error');
      }
    });

    expect(result.current.error?.status).toBeUndefined();
  });
});
