import React, { useState, useCallback } from 'react';
import {
  Box,
  Typography,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  useMediaQuery,
  CircularProgress,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { useSnackbar } from 'notistack';
import AddIcon from '@mui/icons-material/Add';
import SearchBar from '../components/SearchBar';
import DomainTable from '../components/DomainTable';
import DomainForm from '../components/DomainForm';
import DeleteConfirmDialog from '../components/DeleteConfirmDialog';
import CsvUploadPanel from '../components/CsvUploadPanel/CsvUploadPanel';
import ConfigurationPanel from '../components/DataConfigurationPanel/ConfigurationPanel';
import DomainDatasetsModal from '../components/DomainDatasetsModal/DomainDatasetsModal';
import BatchGenerationModal from '../components/BatchGenerationModal/BatchGenerationModal';
import CreateDatasetChoiceDialog from '../components/ManualWizard/CreateDatasetChoiceDialog';
import ManualWizardModal from '../components/ManualWizard/ManualWizardModal';
import { useDomainContext } from '../context/DomainContext';
import { ACTIONS } from '../context/DomainContext';
import * as domainService from '../services/domainService';
import useApi from '../hooks/useApi';
import { useBatchJobs } from '../context/BatchJobsContext';

const DomainsPage = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const { enqueueSnackbar } = useSnackbar();
  const { state, dispatch } = useDomainContext();
  const { execute: executeApi } = useApi();
  const { trackJob } = useBatchJobs();

  // Modal and dialog states
  const [openCreateModal, setOpenCreateModal] = useState(false);
  const [openEditModal, setOpenEditModal] = useState(false);
  const [editingDomain, setEditingDomain] = useState(null);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [showUploader, setShowUploader] = useState(false);
  const [selectedDomainId, setSelectedDomainId] = useState(null);
  const [uploadedData, setUploadedData] = useState(null);
  const [csvData, setCsvData] = useState([]);
  const [detectedTypes, setDetectedTypes] = useState({});
  const [showConfigurationStep, setShowConfigurationStep] = useState(false);
  const [showSuccessDialog, setShowSuccessDialog] = useState(false);
  const [showDatasetModal, setShowDatasetModal] = useState(false);
  const [selectedDomainForDatasets, setSelectedDomainForDatasets] = useState(null);
  const [showBatchModal, setShowBatchModal] = useState(false);
  const [showChoiceDialog, setShowChoiceDialog] = useState(false);
  const [wizardDomainId, setWizardDomainId] = useState(null);
  const [showManualWizard, setShowManualWizard] = useState(false);

  // Form loading states
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState(null);
  const [editLoading, setEditLoading] = useState(false);
  const [editError, setEditError] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  // Handle create domain
  const handleCreateSubmit = useCallback(async (formData) => {
    setCreateLoading(true);
    setCreateError(null);
    try {
      const created = await executeApi(domainService.createDomain, formData);
      dispatch({ type: ACTIONS.ADD_DOMAIN, payload: created });
      enqueueSnackbar('Domain created successfully', { variant: 'success' });
      setOpenCreateModal(false);
    } catch (error) {
      setCreateError(error);
      enqueueSnackbar(error.message || 'Failed to create domain', { variant: 'error' });
    } finally {
      setCreateLoading(false);
    }
  }, [executeApi, dispatch, enqueueSnackbar]);

  // Handle edit domain
  const handleEditSubmit = useCallback(async (formData) => {
    if (!editingDomain) return;
    setEditLoading(true);
    setEditError(null);
    try {
      const updated = await executeApi(
        domainService.updateDomain,
        editingDomain.id,
        formData
      );
      dispatch({ type: ACTIONS.UPDATE_DOMAIN, payload: updated });
      enqueueSnackbar('Domain updated successfully', { variant: 'success' });
      setOpenEditModal(false);
      setEditingDomain(null);
    } catch (error) {
      setEditError(error);
      enqueueSnackbar(error.message || 'Failed to update domain', { variant: 'error' });
    } finally {
      setEditLoading(false);
    }
  }, [editingDomain, executeApi, dispatch, enqueueSnackbar]);

  // Handle delete domain
  const handleDeleteConfirm = useCallback(async () => {
    if (!deleteTarget) return;
    setDeleteLoading(true);
    try {
      await executeApi(domainService.deleteDomain, deleteTarget.id);
      dispatch({ type: ACTIONS.DELETE_DOMAIN, payload: deleteTarget.id });
      enqueueSnackbar('Domain deleted successfully', { variant: 'success' });
      setDeleteConfirmOpen(false);
      setDeleteTarget(null);
    } catch (error) {
      enqueueSnackbar(error.message || 'Failed to delete domain', { variant: 'error' });
      setDeleteConfirmOpen(false);
      setDeleteTarget(null);
    } finally {
      setDeleteLoading(false);
    }
  }, [deleteTarget, executeApi, dispatch, enqueueSnackbar]);

  // Handle edit button click (modal on desktop, navigate on mobile)
  const handleEditClick = useCallback((domain) => {
    if (isMobile) {
      // Mobile: would navigate to /domains/:id/edit route (S1.6)
      setEditingDomain(domain);
      setOpenEditModal(true);
    } else {
      // Desktop: show modal
      setEditingDomain(domain);
      setOpenEditModal(true);
    }
  }, [isMobile]);

  // Handle CSV upload click
  const handleUploadClick = useCallback((domain) => {
    setSelectedDomainId(domain.id);
    setShowUploader(true);
  }, []);

  // Handle view datasets click
  const handleViewDatasets = useCallback((domain) => {
    setSelectedDomainForDatasets(domain);
    setShowDatasetModal(true);
  }, []);

  // Handle create dataset click → ouvre le dialog de choix (S7.2 AC1)
  const handleCreateDatasetClick = useCallback((domain) => {
    setWizardDomainId(domain.id);
    setShowChoiceDialog(true);
  }, []);

  const handleChoiceCSV = useCallback(() => {
    setShowChoiceDialog(false);
    setSelectedDomainId(wizardDomainId);
    setShowUploader(true);
  }, [wizardDomainId]);

  const handleChoiceManual = useCallback(() => {
    setShowChoiceDialog(false);
    setShowManualWizard(true);
  }, []);

  // Handle proceeding from CSV upload (after configuration)
  const handleProceedToConfiguration = useCallback((data) => {
    // Extract csvData and detectionResults from the payload
    const { csvData: rawCsv, detectionResults } = data;
    
    // Store the raw CSV data
    setCsvData(rawCsv || []);
    
    // Convert detectionResults array to detectedTypes object
    const typesMap = {};
    if (detectionResults && Array.isArray(detectionResults)) {
      detectionResults.forEach(col => {
        typesMap[col.name] = col.type;
      });
    }
    setDetectedTypes(typesMap);
    
    // Store the uploaded data for reference
    setUploadedData(detectionResults);
    
    // Show the configuration step
    setShowConfigurationStep(true);
    
    // Close the upload modal
    setShowUploader(false);
    
    // Show success message
    enqueueSnackbar('CSV data uploaded successfully. Configure your columns and generate data.', { variant: 'success' });
  }, [enqueueSnackbar]);
  
  // Handle generation completion
  const handleGenerationComplete = useCallback((generatedDataset) => {
    // After data is generated, close configuration and show success
    setShowConfigurationStep(false);
    
    enqueueSnackbar('Data generated successfully!', { variant: 'success' });
    
    // Show success dialog (keeps uploadedData visible)
    setShowSuccessDialog(true);
  }, [enqueueSnackbar]);

  // Handle load more
  const handleLoadMore = useCallback(async () => {
    dispatch({ type: ACTIONS.LOAD_MORE });
    try {
      const nextBatch = await executeApi(
        domainService.getDomains,
        state.offset,
        state.pageSize
      );
      dispatch({ type: ACTIONS.LOAD_MORE_SUCCESS, payload: nextBatch });
    } catch (error) {
      enqueueSnackbar('Failed to load more domains', { variant: 'error' });
      // Reset loading state
      dispatch({ type: ACTIONS.LOAD_DOMAINS_SUCCESS, payload: state.domains });
    }
  }, [state.offset, state.pageSize, state.domains, executeApi, dispatch, enqueueSnackbar]);

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Domain Management
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            variant="outlined"
            onClick={() => setShowBatchModal(true)}
            data-testid="generate-batch-button"
          >
            Generate Batch
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              setEditingDomain(null);
              setCreateLoading(false);
              setCreateError(null);
              setOpenCreateModal(true);
            }}
          >
            Create New Domain
          </Button>
        </Box>
      </Box>

      {/* Search Bar */}
      <SearchBar />

      {/* Domain Table/Cards */}
      {state.loading && state.domains.length === 0 ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          <DomainTable
            domains={state.domains}
            searchText={state.searchText}
            onEdit={handleEditClick}
            onDelete={(domain) => {
              setDeleteTarget(domain);
              setDeleteConfirmOpen(true);
            }}
            onUpload={handleUploadClick}
            onViewDatasets={handleViewDatasets}
            onCreateDataset={handleCreateDatasetClick}
            loading={state.loading}
          />

          {/* Load More button */}
          {state.hasMore && state.domains.length > 0 && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
              <Button
                onClick={handleLoadMore}
                disabled={state.loading}
                startIcon={state.loading ? <CircularProgress size={20} /> : null}
              >
                {state.loading ? 'Loading...' : 'Load More'}
              </Button>
            </Box>
          )}
        </>
      )}

      {/* Create Modal */}
      <Dialog
        open={openCreateModal}
        onClose={() => setOpenCreateModal(false)}
        maxWidth="sm"
        fullWidth
        fullScreen={isMobile}
      >
        <DialogTitle>Create New Domain</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <DomainForm
              onSubmit={handleCreateSubmit}
              onCancel={() => setOpenCreateModal(false)}
              loading={createLoading}
              error={createError}
            />
          </Box>
        </DialogContent>
      </Dialog>

      {/* Edit Modal */}
      <Dialog
        open={openEditModal}
        onClose={() => {
          setOpenEditModal(false);
          setEditingDomain(null);
        }}
        maxWidth="sm"
        fullWidth
        fullScreen={isMobile}
      >
        <DialogTitle>Edit Domain</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <DomainForm
              initialData={editingDomain}
              onSubmit={handleEditSubmit}
              onCancel={() => {
                setOpenEditModal(false);
                setEditingDomain(null);
              }}
              loading={editLoading}
              error={editError}
            />
          </Box>
        </DialogContent>
      </Dialog>

      {/* Delete Confirm Dialog */}
      <DeleteConfirmDialog
        open={deleteConfirmOpen}
        domainName={deleteTarget?.name}
        onConfirm={handleDeleteConfirm}
        onCancel={() => {
          setDeleteConfirmOpen(false);
          setDeleteTarget(null);
        }}
        loading={deleteLoading}
      />

      {/* CSV Upload Modal (S2.5) */}
      <Dialog
        open={showUploader}
        onClose={() => {
          setShowUploader(false);
          setSelectedDomainId(null);
        }}
        maxWidth="md"
        fullWidth
        fullScreen={isMobile}
      >
        <DialogTitle>Upload CSV Data</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            {selectedDomainId && (
              <CsvUploadPanel
                domainId={selectedDomainId}
                onProceedToConfiguration={handleProceedToConfiguration}
                onCancel={() => {
                  setShowUploader(false);
                  setSelectedDomainId(null);
                }}
              />
            )}
          </Box>
        </DialogContent>
      </Dialog>

      {/* Configuration Step - S2.6 ConfigurationPanel */}
      <Dialog
        open={showConfigurationStep}
        onClose={() => {
          setShowConfigurationStep(false);
          setUploadedData(null);
          setCsvData([]);
          setDetectedTypes({});
        }}
        maxWidth="lg"
        fullWidth
        fullScreen={isMobile}
      >
        <DialogTitle>🛠️ Data Configuration & Generation (S2.6)</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            {csvData.length > 0 && Object.keys(detectedTypes).length > 0 && selectedDomainId && (
              <ConfigurationPanel
                csvData={csvData}
                detectedTypes={detectedTypes}
                domainId={selectedDomainId}
                onGenerationComplete={handleGenerationComplete}
              />
            )}
          </Box>
        </DialogContent>
      </Dialog>

      {/* Success Dialog - After configuration saved */}
      <Dialog
        open={showSuccessDialog}
        onClose={() => {
          setShowSuccessDialog(false);
          setUploadedData(null);
        }}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>✅ Configuration Saved Successfully!</DialogTitle>
        <DialogContent sx={{ pt: 3 }}>
          <Box sx={{ mb: 3 }}>
            <Typography variant="h5" sx={{ mb: 2 }}>🎉 Data Ready for Generation</Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Your CSV configuration has been saved. You can now use this data for generation.
            </Typography>
          </Box>

          {uploadedData && (
            <Box>
              <Typography variant="subtitle2" sx={{ mb: 2, fontWeight: 'bold' }}>
                📊 Configured Columns ({uploadedData.length}):
              </Typography>
              <Box sx={{ 
                backgroundColor: '#f9f9f9', 
                padding: 2, 
                borderRadius: 1, 
                mb: 3,
                maxHeight: '300px',
                overflowY: 'auto'
              }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ backgroundColor: '#f0f0f0', borderBottom: '2px solid #ddd' }}>
                      <th style={{ padding: '10px', textAlign: 'left', fontSize: '12px' }}>Column</th>
                      <th style={{ padding: '10px', textAlign: 'left', fontSize: '12px' }}>Type</th>
                      <th style={{ padding: '10px', textAlign: 'center', fontSize: '12px' }}>Confidence</th>
                    </tr>
                  </thead>
                  <tbody>
                    {uploadedData.map((col, idx) => (
                      <tr key={col.name} style={{ borderBottom: '1px solid #eee' }}>
                        <td style={{ padding: '8px', fontSize: '12px' }}>{col.name}</td>
                        <td style={{ padding: '8px', fontSize: '12px' }}>{col.type}</td>
                        <td style={{ padding: '8px', textAlign: 'center', fontSize: '12px' }}>
                          {Math.round(col.confidence)}%
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </Box>

              <Box sx={{ 
                backgroundColor: '#e3f2fd', 
                padding: 2, 
                borderRadius: 1, 
                mb: 3,
                borderLeft: '4px solid #1976d2'
              }}>
                <Typography variant="body2">
                  <strong>💡 Tip:</strong> Click on the domain to view the configuration again or start data generation.
                </Typography>
              </Box>
            </Box>
          )}

          <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
            <Button 
              variant="outlined"
              onClick={() => {
                setShowSuccessDialog(false);
                setUploadedData(null);
              }}
            >
              View Configuration Later
            </Button>
            <Button 
              variant="contained" 
              onClick={async () => {
                try {
                  // Transform data to match API requirements
                  const configData = uploadedData.map(col => ({
                    name: col.name,
                    type: col.type,
                    confidence: col.confidence,
                    detector: col.detector || 'unknown'
                  }));

                  // Save to backend
                  const response = await fetch(
                    `http://localhost:8080/api/domains/${selectedDomainId}/columns/save-configuration`,
                    {
                      method: 'POST',
                      headers: { 'Content-Type': 'application/json' },
                      body: JSON.stringify(configData)
                    }
                  );

                  if (!response.ok) {
                    throw new Error('Failed to save configuration');
                  }

                  const result = await response.json();
                  enqueueSnackbar('Configuration saved to database! Ready for data generation.', { variant: 'success' });
                  setShowSuccessDialog(false);
                  setUploadedData(null);
                  setSelectedDomainId(null);
                } catch (error) {
                  enqueueSnackbar('Error saving configuration: ' + error.message, { variant: 'error' });
                }
              }}
            >
              Return to Domains →
            </Button>
          </Box>
        </DialogContent>
      </Dialog>

      {/* View Datasets Modal (S2.5) */}
      <DomainDatasetsModal
        open={showDatasetModal}
        domainId={selectedDomainForDatasets?.id}
        domainName={selectedDomainForDatasets?.name}
        onClose={() => {
          setShowDatasetModal(false);
          setSelectedDomainForDatasets(null);
        }}
      />

      {/* Batch Generation Modal (S4.1) */}
      <BatchGenerationModal
        open={showBatchModal}
        onClose={() => setShowBatchModal(false)}
        domains={state.domains}
      />

      {/* Choice Dialog — CSV ou Manuel (S7.2 AC1) */}
      <CreateDatasetChoiceDialog
        open={showChoiceDialog}
        onChooseCSV={handleChoiceCSV}
        onChooseManual={handleChoiceManual}
        onClose={() => { setShowChoiceDialog(false); setWizardDomainId(null); }}
      />

      {/* Manual Wizard Modal (S7.2) */}
      <ManualWizardModal
        open={showManualWizard}
        domainId={wizardDomainId}
        onClose={() => { setShowManualWizard(false); setWizardDomainId(null); }}
        onSuccess={(name, count) => {
          setShowManualWizard(false);
          setWizardDomainId(null);
          enqueueSnackbar(`Dataset '${name}' créé avec ${count} lignes`, { variant: 'success' });
        }}
      />
    </Box>
  );
};

export default DomainsPage;
