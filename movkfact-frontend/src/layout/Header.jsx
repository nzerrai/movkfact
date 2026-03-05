import React, { useState } from 'react';
import { AppBar, Toolbar, Typography, IconButton, Badge, Tooltip } from '@mui/material';
import HistoryIcon from '@mui/icons-material/History';
import { useBatchJobs } from '../context/BatchJobsContext';
import BatchHistoryDrawer from '../components/BatchHistoryDrawer/BatchHistoryDrawer';

const Header = ({ onMenuClick }) => {
  const [historyOpen, setHistoryOpen] = useState(false);
  const { state } = useBatchJobs();
  const historyCount = state.history?.length ?? 0;

  return (
    <>
      <AppBar position="static" sx={{ mb: 2 }}>
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            🚀 Movkfact
          </Typography>
          <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.7)', mr: 1 }}>
            Data Generation Platform
          </Typography>
          <Tooltip title="Historique des batchs">
            <IconButton
              color="inherit"
              onClick={() => setHistoryOpen(true)}
              size="small"
            >
              <Badge badgeContent={historyCount} color="error" max={99}>
                <HistoryIcon />
              </Badge>
            </IconButton>
          </Tooltip>
        </Toolbar>
      </AppBar>

      <BatchHistoryDrawer open={historyOpen} onClose={() => setHistoryOpen(false)} />
    </>
  );
};

export default Header;
