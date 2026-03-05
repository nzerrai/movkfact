import React, { useState } from 'react';
import {
  Box, Chip, Typography, Button, TextField,
  CircularProgress, Alert,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import axios from 'axios';

const METHOD_COLORS = {
  GET: 'success',
  POST: 'primary',
  PUT: 'warning',
  DELETE: 'error',
};

function buildDefaultUrl(endpoint, defaultDomainId, defaultDatasetId) {
  let url = `http://localhost:8080${endpoint.path}`;
  if (defaultDomainId != null) url = url.replace('{domainId}', defaultDomainId).replace('{id}', endpoint.entityType === 'domain' ? defaultDomainId : (defaultDatasetId ?? defaultDomainId));
  if (defaultDatasetId != null && endpoint.entityType === 'dataset') url = url.replace('{id}', defaultDatasetId);
  return url;
}

export default function EndpointCard({ endpoint, defaultDomainId, defaultDatasetId }) {
  const [open, setOpen] = useState(false);
  const [url, setUrl] = useState(() => buildDefaultUrl(endpoint, defaultDomainId, defaultDatasetId));
  const [body, setBody] = useState(endpoint.defaultBody ?? '');
  const [response, setResponse] = useState(null);
  const [loading, setLoading] = useState(false);

  const canTry = endpoint.method === 'GET' || endpoint.method === 'POST';

  const handleExecute = async () => {
    setLoading(true);
    setResponse(null);
    const t0 = performance.now();
    try {
      // Use a raw path (strip host prefix if same origin via proxy)
      const path = url.replace(/^https?:\/\/[^/]+/, '') || url;
      const hasBody = endpoint.method === 'POST' && body.trim() !== '';
      const res = await axios({
        method: endpoint.method.toLowerCase(),
        url: path,
        validateStatus: () => true,
        ...(hasBody && { data: JSON.parse(body), headers: { 'Content-Type': 'application/json' } }),
      });
      const durationMs = Math.round(performance.now() - t0);
      setResponse({
        status: res.status,
        statusText: res.statusText,
        durationMs,
        body: JSON.stringify(res.data, null, 2),
      });
    } catch (e) {
      setResponse({ status: 'ERR', statusText: e.message, durationMs: Math.round(performance.now() - t0), body: e.message });
    } finally {
      setLoading(false);
    }
  };

  const noData = defaultDomainId == null && defaultDatasetId == null;

  return (
    <Box
      sx={{
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 1,
        mb: 1.5,
        overflow: 'hidden',
      }}
    >
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1.5,
          px: 2,
          py: 1.5,
          bgcolor: 'background.paper',
        }}
      >
        <Chip
          label={endpoint.method}
          color={METHOD_COLORS[endpoint.method] || 'default'}
          size="small"
          sx={{ fontWeight: 700, minWidth: 56 }}
        />
        <Typography variant="body2" fontFamily="monospace" sx={{ flex: 1, wordBreak: 'break-all' }}>
          {endpoint.path}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ display: { xs: 'none', sm: 'block' } }}>
          {endpoint.description}
        </Typography>
        {canTry && (
          <Button
            size="small"
            variant="outlined"
            endIcon={open ? <ExpandLessIcon /> : <ExpandMoreIcon />}
            onClick={() => setOpen(o => !o)}
            sx={{ ml: 1, whiteSpace: 'nowrap' }}
          >
            Essayer
          </Button>
        )}
      </Box>

      {/* Params summary */}
      {endpoint.params && (
        <Box sx={{ px: 2, pb: 1, bgcolor: 'grey.50' }}>
          <Typography variant="caption" color="text.secondary">
            {endpoint.params}
          </Typography>
        </Box>
      )}

      {/* Try panel */}
      {canTry && open && (
        <Box sx={{ px: 2, py: 1.5, bgcolor: 'grey.50', borderTop: '1px solid', borderColor: 'divider' }}>
            {noData && (
              <Alert severity="info" sx={{ mb: 1.5 }}>
                Aucune donnée — créez d'abord un domaine et un dataset.
              </Alert>
            )}

            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', mb: 1.5 }}>
              <TextField
                value={url}
                onChange={e => setUrl(e.target.value)}
                size="small"
                fullWidth
                InputProps={{ sx: { fontFamily: 'monospace', fontSize: '0.8rem' } }}
              />
              <Button
                variant="contained"
                size="small"
                startIcon={loading ? <CircularProgress size={14} color="inherit" /> : <PlayArrowIcon />}
                onClick={handleExecute}
                disabled={loading}
                sx={{ whiteSpace: 'nowrap' }}
              >
                Exécuter
              </Button>
            </Box>

            {endpoint.method === 'POST' && (
              <Box sx={{ mb: 1.5 }}>
                <Typography variant="caption" color="text.secondary" sx={{ mb: 0.5, display: 'block' }}>
                  Body JSON
                </Typography>
                <TextField
                  value={body}
                  onChange={e => setBody(e.target.value)}
                  multiline
                  minRows={4}
                  maxRows={10}
                  fullWidth
                  size="small"
                  InputProps={{ sx: { fontFamily: 'monospace', fontSize: '0.8rem' } }}
                  placeholder='{ "name": "...", ... }'
                />
              </Box>
            )}

            {response && (
              <Box>
                <Typography variant="caption" color={response.status >= 200 && response.status < 300 ? 'success.main' : 'error.main'}>
                  {response.status} {response.statusText} · {response.durationMs}ms
                </Typography>
                <Box
                  component="pre"
                  sx={{
                    mt: 0.5,
                    p: 1.5,
                    bgcolor: '#1e1e1e',
                    color: '#d4d4d4',
                    borderRadius: 1,
                    overflow: 'auto',
                    maxHeight: 300,
                    fontSize: '0.75rem',
                    fontFamily: 'monospace',
                  }}
                >
                  {response.body}
                </Box>
              </Box>
            )}
          </Box>
      )}
    </Box>
  );
}
