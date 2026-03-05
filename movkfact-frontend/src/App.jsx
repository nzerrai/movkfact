import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { SnackbarProvider } from 'notistack';
import Layout from './layout/Layout';
import Dashboard from './pages/Dashboard';
import DomainsPage from './pages/DomainsPage';
import DataViewerPage from './pages/DataViewerPage';
import DatasetsPage from './pages/DatasetsPage';
import NotFound from './pages/NotFound';
import SettingsPage from './pages/SettingsPage';
import ApiReferencePage from './pages/ApiReferencePage';
import AnonymizationPage from './pages/AnonymizationPage';
import { BatchJobsProvider } from './context/BatchJobsContext';

function App() {
  return (
    <SnackbarProvider maxSnack={5} anchorOrigin={{ vertical: 'top', horizontal: 'right' }}>
      <BatchJobsProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Layout />}>
              <Route index element={<Dashboard />} />
              <Route path="/domains" element={<DomainsPage />} />
              <Route path="/datasets" element={<DatasetsPage />} />
              <Route path="/data-viewer/:datasetId" element={<DataViewerPage />} />
              <Route path="/settings" element={<SettingsPage />} />
              <Route path="/api-reference" element={<ApiReferencePage />} />
              <Route path="/anonymization" element={<AnonymizationPage />} />
              <Route path="*" element={<NotFound />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </BatchJobsProvider>
    </SnackbarProvider>
  );
}

export default App;
