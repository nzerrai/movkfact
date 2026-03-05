import api from './api';

/**
 * Submit a batch generation job.
 * @param {Array<{domainId: number, datasetName: string, columns: Array, count: number}>} dataSetConfigs
 * @returns {Promise<{jobId: number, status: string, totalDatasets: number, message: string}>}
 */
export const submitBatch = async (dataSetConfigs) => {
  const response = await api.post('/api/batch/generate', { dataSetConfigs });
  return response.data.data; // unwrap ApiResponse<BatchJobResponseDTO>
};

/**
 * Get the current status of a batch job from the backend.
 * @param {string|number} jobId
 * @returns {Promise<{jobId, status, completedCount, totalCount, percentage, skippedCount, errors}>}
 */
export const getJobStatus = async (jobId) => {
  const response = await api.get(`/api/batch/${jobId}`);
  return response.data.data; // unwrap ApiResponse<BatchJobStatusDTO>
};

/**
 * Get saved column configurations for a domain.
 * @param {number} domainId
 * @returns {Promise<{success: boolean, hasConfigurations: boolean, columns: Array, columnsCount: number}>}
 */
export const getDomainColumnConfig = async (domainId) => {
  const response = await api.get(`/api/domains/${domainId}/columns/configuration`);
  return response.data;
};
