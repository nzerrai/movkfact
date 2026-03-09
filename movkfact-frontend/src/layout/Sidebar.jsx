import React from 'react';
import {
  Box,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import HomeIcon from '@mui/icons-material/Home';
import PersonIcon from '@mui/icons-material/Person';
import StorageIcon from '@mui/icons-material/Storage';
import SettingsIcon from '@mui/icons-material/Settings';
import IntegrationInstructionsIcon from '@mui/icons-material/IntegrationInstructions';
import ShieldIcon from '@mui/icons-material/Shield';

const Sidebar = ({ open, onClose }) => {
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  const menuItems = [
    { label: 'Dashboard', icon: <HomeIcon />, path: '/' },
    { label: 'Domains', icon: <PersonIcon />, path: '/domains' },
    { label: 'Datasets', icon: <StorageIcon />, path: '/datasets' },
    { label: 'API Reference', icon: <IntegrationInstructionsIcon />, path: '/api-reference' },
    { label: 'Anonymisation RGPD', icon: <ShieldIcon />, path: '/anonymization' },
    { label: 'Settings', icon: <SettingsIcon />, path: '/settings' },
  ];

  const drawerContent = (
    <Box sx={{ width: 240 }}>
      <Box sx={{ p: 2 }}>
        <ListItemText primary="Menu" primaryTypographyProps={{ variant: 'h6' }} />
      </Box>
      <Divider />
      <List>
        {menuItems.map((item) => (
          <ListItem key={item.path} disablePadding>
            <ListItemButton
              onClick={() => {
                navigate(item.path);
                if (isMobile) onClose();
              }}
              disabled={item.disabled}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </Box>
  );

  if (isMobile) {
    return (
      <Drawer anchor="left" open={open} onClose={onClose}>
        {drawerContent}
      </Drawer>
    );
  }

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: 240,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: 240,
          boxSizing: 'border-box',
          mt: 8,
          height: 'calc(100vh - 64px)',
        },
      }}
    >
      {drawerContent}
    </Drawer>
  );
};

export default Sidebar;
