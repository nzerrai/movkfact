import React from 'react';
import { Stack, Chip } from '@mui/material';
import DownloadDoneIcon from '@mui/icons-material/DownloadDone';
import EditIcon from '@mui/icons-material/Edit';
import VisibilityIcon from '@mui/icons-material/Visibility';

/**
 * StatusBadge — affiche les badges d'activité d'un domaine ou dataset (FR-002, FR-003).
 * Props : { downloaded, modified, viewed } — tous boolean.
 * Si tous false : chip gris "Nouveau".
 */
const StatusBadge = ({ downloaded = false, modified = false, viewed = false }) => {
  if (!downloaded && !modified && !viewed) {
    return <Chip label="Nouveau" size="small" color="default" />;
  }
  return (
    <Stack direction="row" spacing={0.5} flexWrap="wrap">
      {downloaded && (
        <Chip
          icon={<DownloadDoneIcon />}
          label="Téléchargé"
          size="small"
          sx={{ bgcolor: '#e8f5e9' }}
        />
      )}
      {modified && (
        <Chip
          icon={<EditIcon />}
          label="Modifié"
          size="small"
          sx={{ bgcolor: '#fff3e0' }}
        />
      )}
      {viewed && (
        <Chip
          icon={<VisibilityIcon />}
          label="Consulté"
          size="small"
          sx={{ bgcolor: '#e3f2fd' }}
        />
      )}
    </Stack>
  );
};

export default StatusBadge;
