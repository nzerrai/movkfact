import React from 'react';
import { AppBar, Toolbar, Typography } from '@mui/material';

const Header = ({ onMenuClick }) => {
  return (
    <AppBar position="static" sx={{ mb: 2 }}>
      <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          🚀 Movkfact
        </Typography>
        <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.7)' }}>
          Data Generation Platform
        </Typography>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
