import React from 'react';
import { Box, Typography, Divider, Paper, List, ListItem, ListItemText } from '@mui/material';
import SettingsIcon from '@mui/icons-material/Settings';

const SettingsPage = () => {
  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
        <SettingsIcon color="action" />
        <Typography variant="h5">Settings</Typography>
      </Box>
      <Divider sx={{ mb: 3 }} />

      <Paper variant="outlined" sx={{ mb: 2 }}>
        <List disablePadding>
          <ListItem divider>
            <ListItemText
              primary="Version de l'application"
              secondary="movkfact v1.0.0 — Sprint 7"
            />
          </ListItem>
          <ListItem divider>
            <ListItemText
              primary="Backend"
              secondary="http://localhost:8080"
            />
          </ListItem>
          <ListItem>
            <ListItemText
              primary="Fonctionnalités à venir"
              secondary="Authentification JWT, thème sombre, gestion des utilisateurs"
            />
          </ListItem>
        </List>
      </Paper>

      <Typography variant="caption" color="text.secondary">
        Les paramètres avancés seront disponibles dans une prochaine version.
      </Typography>
    </Box>
  );
};

export default SettingsPage;
