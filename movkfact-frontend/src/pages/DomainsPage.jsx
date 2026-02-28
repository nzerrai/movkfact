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
import { useDomainContext } from '../context/DomainContext';
import { ACTIONS } from '../context/DomainContext';
import * as domainService from '../services/domainService';
import useApi from '../hooks/useApi';

const DomainsPage = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const { enqueueSnackbar } = useSnackbar();
  const { state, dispatch } = useDomainContext();
  const { execute: executeApi } = useApi();

  // Modal and dialog states
  const [openCreateModal, setOpenCreateModal] = useState(false);
  const [openEditModal, setOpenEditModal] = useState(false);
  const [editingDomain, setEditingDomain] = useState(null);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

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
    </Box>
  );
};

export default DomainsPage;
