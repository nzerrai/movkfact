import React, { useState } from 'react';
import { Box, Tabs, Tab, IconButton, Tooltip, Typography } from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import CheckIcon from '@mui/icons-material/Check';

export default function SnippetBlock({ snippets }) {
  const [activeTab, setActiveTab] = useState(0);
  const [copied, setCopied] = useState(false);

  const langs = Object.keys(snippets);
  const currentCode = snippets[langs[activeTab]];

  const handleCopy = () => {
    navigator.clipboard.writeText(currentCode).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    });
  };

  return (
    <Box sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1, overflow: 'hidden' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', bgcolor: '#2d2d2d' }}>
        <Tabs
          value={activeTab}
          onChange={(_, v) => setActiveTab(v)}
          textColor="inherit"
          TabIndicatorProps={{ style: { backgroundColor: '#9cdcfe' } }}
          sx={{ flex: 1, minHeight: 36, '& .MuiTab-root': { minHeight: 36, py: 0, color: '#ccc', fontSize: '0.75rem' } }}
        >
          {langs.map((lang, idx) => (
            <Tab key={lang} label={lang} id={`snippet-tab-${idx}`} />
          ))}
        </Tabs>
        <Tooltip title={copied ? 'Copié !' : 'Copier'}>
          <IconButton
            size="small"
            onClick={handleCopy}
            aria-label={copied ? 'Copié !' : 'Copier'}
            sx={{ color: copied ? '#4ec9b0' : '#ccc', mr: 0.5 }}
          >
            {copied ? <CheckIcon fontSize="small" /> : <ContentCopyIcon fontSize="small" />}
          </IconButton>
        </Tooltip>
      </Box>
      <Box
        component="pre"
        sx={{
          m: 0,
          p: 2,
          bgcolor: '#1e1e1e',
          color: '#d4d4d4',
          fontSize: '0.78rem',
          fontFamily: 'monospace',
          overflow: 'auto',
          maxHeight: 280,
          whiteSpace: 'pre',
        }}
      >
        {currentCode}
      </Box>
    </Box>
  );
}
