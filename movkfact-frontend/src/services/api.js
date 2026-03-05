import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || '';
console.log('🔍 API Configuration:');
console.log('- REACT_APP_API_URL:', process.env.REACT_APP_API_URL);
console.log('- API_URL resolved to:', API_URL || '(using proxy)');

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Response interceptor for global error handling
api.interceptors.response.use(
  response => response,
  error => {
    const status = error.response?.status;

    // Handle 401 Unauthorized - redirect to login (S1.7)
    if (status === 401) {
      console.warn('Unauthorized - redirecting to login (S1.7)');
      // TODO: Redirect to login page once authentication is implemented
    }

    // Let component-level handlers deal with other errors
    // Global 500 handling can be added in component via useApi hook
    return Promise.reject(error);
  }
);

export default api;
