import React, { useState } from 'react';
import { Box, Container, useMediaQuery, useTheme, IconButton } from '@mui/material';
import { Outlet } from 'react-router-dom';
import MenuIcon from '@mui/icons-material/Menu';
import Header from './Header';
import Sidebar from './Sidebar';

const Layout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Header onMenuClick={() => setSidebarOpen(true)} />
      <Box sx={{ display: 'flex', flex: 1 }}>
        <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
        <Box
          component="main"
          sx={{
            flex: 1,
            p: { xs: 2, md: 3 },
            backgroundColor: '#f5f5f5',
            overflowY: 'auto',
          }}
        >
          {isMobile && (
            <Box sx={{ mb: 2 }}>
              <IconButton
                onClick={() => setSidebarOpen(true)}
                sx={{ mb: 2 }}
              >
                <MenuIcon />
              </IconButton>
            </Box>
          )}
          <Container maxWidth="lg">
            <Outlet />
          </Container>
        </Box>
      </Box>
    </Box>
  );
};

export default Layout;
