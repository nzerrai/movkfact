import React, { useState, useEffect } from 'react';
import { Box, Tabs, Tab, Typography, CircularProgress } from '@mui/material';
import axios from 'axios';
import CrudTab from '../components/ApiReference/CrudTab';
import ExtractionTab from '../components/ApiReference/ExtractionTab';
import GuideTab from '../components/ApiReference/GuideTab';

function TabPanel({ children, value, index }) {
  return (
    <div role="tabpanel" hidden={value !== index} id={`api-tabpanel-${index}`}>
      {value === index && <Box sx={{ pt: 2 }}>{children}</Box>}
    </div>
  );
}

export default function ApiReferencePage() {
  const [activeTab, setActiveTab] = useState(0);
  const [context, setContext] = useState({
    defaultDomainId: null,
    defaultDatasetId: null,
    domains: [],
    datasets: [],
    loading: true,
  });

  useEffect(() => {
    axios.get('/api/domains')
      .then(r => {
        const domains = r.data?.data ?? r.data ?? [];
        const firstDomain = domains[0];
        if (!firstDomain) {
          setContext({ defaultDomainId: null, defaultDatasetId: null, domains: [], datasets: [], loading: false });
          return;
        }
        // Datasets are scoped per domain: GET /api/domains/{id}/data-sets
        axios.get(`/api/domains/${firstDomain.id}/data-sets`)
          .then(r2 => {
            const datasets = r2.data?.data ?? r2.data ?? [];
            setContext({
              defaultDomainId: firstDomain.id,
              defaultDatasetId: datasets[0]?.id ?? null,
              domains,
              datasets,
              loading: false,
            });
          })
          .catch(() => setContext({
            defaultDomainId: firstDomain.id,
            defaultDatasetId: null,
            domains,
            datasets: [],
            loading: false,
          }));
      })
      .catch(() => setContext({
        defaultDomainId: null,
        defaultDatasetId: null,
        domains: [],
        datasets: [],
        loading: false,
      }));
  }, []);

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        API Reference
      </Typography>

      {context.loading && (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <CircularProgress size={16} />
          <Typography variant="body2" color="text.secondary">Chargement du contexte…</Typography>
        </Box>
      )}

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)} aria-label="API reference tabs">
          <Tab label="CRUD" id="api-tab-0" aria-controls="api-tabpanel-0" />
          <Tab label="Extraction" id="api-tab-1" aria-controls="api-tabpanel-1" />
          <Tab label="Guide" id="api-tab-2" aria-controls="api-tabpanel-2" />
        </Tabs>
      </Box>

      <TabPanel value={activeTab} index={0}>
        <CrudTab
          defaultDomainId={context.defaultDomainId}
          defaultDatasetId={context.defaultDatasetId}
        />
      </TabPanel>

      <TabPanel value={activeTab} index={1}>
        <ExtractionTab
          datasets={context.datasets}
          defaultDomainId={context.defaultDomainId}
        />
      </TabPanel>

      <TabPanel value={activeTab} index={2}>
        <GuideTab />
      </TabPanel>
    </Box>
  );
}
