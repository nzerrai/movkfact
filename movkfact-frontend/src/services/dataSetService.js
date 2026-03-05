import api from './api';

/**
 * Get dataset metadata by ID
 * @param {number} id - Dataset ID
 * @returns {Promise<Object>} Dataset metadata
 */
export const getDatasetById = async (id) => {
  try {
    const response = await api.get(`/api/data-sets/${id}`);
    return response.data.data || response.data;
  } catch (error) {
    throw error;
  }
};

/**
 * Get dataset data with pagination
 * @param {number} id - Dataset ID
 * @param {number} page - Page number (0-based)
 * @param {number} size - Page size
 * @returns {Promise<Object>} Paginated data
 */
export const getDatasetData = async (id, page = 0, size = 50) => {
  try {
    const response = await api.get(`/api/data-sets/${id}/data`, {
      params: { page, size }
    });
    return response.data.data || response.data;
  } catch (error) {
    throw error;
  }
};

/**
 * Get dataset activity history
 * @param {number} id - Dataset ID
 * @param {string} action - Optional action type filter
 * @param {number} page - Page number (0-based)
 * @param {number} size - Page size
 * @returns {Promise<Array>} Activity history
 */
export const getDatasetActivity = async (id, action = null, page = 0, size = 50) => {
  try {
    const params = { page, size };
    if (action) {
      params.action = action;
    }
    const response = await api.get(`/api/data-sets/${id}/activity`, { params });
    return response.data.data || response.data;
  } catch (error) {
    throw error;
  }
};

/**
 * Reset dataset to original version
 * @param {number} id - Dataset ID
 * @returns {Promise<Object>} Reset dataset
 */
export const resetDataset = async (id) => {
  try {
    const response = await api.get(`/api/data-sets/${id}/reset`);
    return response.data.data || response.data;
  } catch (error) {
    throw error;
  }
};

/**
 * Delete dataset
 * @param {number} id - Dataset ID
 * @returns {Promise<void>}
 */
export const deleteDataset = async (id) => {
  try {
    await api.delete(`/api/data-sets/${id}`);
  } catch (error) {
    throw error;
  }
};

export const getRows = async (datasetId, page = 0, size = 50) => {
  try {
    const response = await api.get(`/api/data-sets/${datasetId}/rows`, { params: { page, size } });
    return response.data.data || response.data;
  } catch (error) {
    throw error;
  }
};

export const getRow = async (datasetId, rowIndex) => {
  try {
    const response = await api.get(`/api/data-sets/${datasetId}/rows/${rowIndex}`);
    return response.data.data || response.data;
  } catch (error) {
    throw error;
  }
};

export const updateRow = async (datasetId, rowIndex, columns) => {
  try {
    const response = await api.put(`/api/data-sets/${datasetId}/rows/${rowIndex}`, { columns });
    return response.data.data || response.data;
  } catch (error) {
    throw error;
  }
};

export const deleteRow = async (datasetId, rowIndex) => {
  try {
    await api.delete(`/api/data-sets/${datasetId}/rows/${rowIndex}`);
  } catch (error) {
    throw error;
  }
};