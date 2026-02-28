import api from './api';

/**
 * Get list of domains with pagination
 * @param {number} offset - Starting position (default 0)
 * @param {number} limit - Number of items to fetch (default 20)
 * @returns {Promise<Array>} Array of domain objects
 */
export const getDomains = async (offset = 0, limit = 20) => {
  try {
    const response = await api.get('/api/domains', {
      params: {
        offset,
        limit,
      },
    });
    console.log('getDomains response:', response);
    console.log('getDomains response.data:', response.data);
    console.log('getDomains response.data.data:', response.data.data);
    // Unwrap ApiResponse<T>.data
    const domains = response.data.data || [];
    console.log('Returning domains:', domains);
    return domains;
  } catch (error) {
    console.error('getDomains error:', error);
    throw error;
  }
};

/**
 * Get single domain by ID
 * @param {number} id - Domain ID
 * @returns {Promise<Object>} Domain object
 */
export const getDomainById = async (id) => {
  try {
    const response = await api.get(`/api/domains/${id}`);
    return response.data.data || response.data;
  } catch (error) {
    throw error;
  }
};

/**
 * Create new domain
 * @param {Object} domain - Domain data { name, description }
 * @returns {Promise<Object>} Created domain object
 */
export const createDomain = async (domain) => {
  try {
    const response = await api.post('/api/domains', domain);
    return response.data.data || response.data;
  } catch (error) {
    throw error;
  }
};

/**
 * Update existing domain
 * @param {number} id - Domain ID
 * @param {Object} domain - Updated domain data { name, description }
 * @returns {Promise<Object>} Updated domain object
 */
export const updateDomain = async (id, domain) => {
  try {
    const response = await api.put(`/api/domains/${id}`, domain);
    return response.data.data || response.data;
  } catch (error) {
    throw error;
  }
};

/**
 * Delete domain (soft delete)
 * @param {number} id - Domain ID
 * @returns {Promise<void>}
 */
export const deleteDomain = async (id) => {
  try {
    await api.delete(`/api/domains/${id}`);
  } catch (error) {
    throw error;
  }
};
