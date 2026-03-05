import React from 'react';
import { Typography } from '@mui/material';
import EndpointCard from './EndpointCard';

const ENDPOINTS = [
  {
    method: 'GET',
    path: '/api/domains',
    description: 'Liste tous les domaines',
    entityType: 'domain',
    params: 'Aucun paramètre',
  },
  {
    method: 'POST',
    path: '/api/domains',
    description: 'Crée un domaine',
    entityType: 'domain',
    params: 'Body JSON : { "name": "string", "description": "string" }',
    defaultBody: '{\n  "name": "Mon domaine",\n  "description": "Description du domaine"\n}',
  },
  {
    method: 'DELETE',
    path: '/api/domains/{id}',
    description: 'Supprime un domaine',
    entityType: 'domain',
    params: 'Path : {id} — identifiant du domaine',
  },
  {
    method: 'GET',
    path: '/api/domains/{id}/data-sets',
    description: 'Liste les datasets d\'un domaine',
    entityType: 'domain',
    params: 'Path : {id} — identifiant du domaine',
  },
  {
    method: 'GET',
    path: '/api/data-sets/{id}',
    description: 'Détail d\'un dataset',
    entityType: 'dataset',
    params: 'Path : {id} — identifiant du dataset',
  },
  {
    method: 'POST',
    path: '/api/domains/{id}/data-sets',
    description: 'Génère un dataset pour un domaine',
    entityType: 'domain',
    params: 'Body JSON : { name, rowCount, columns[] } | Path : {id} — identifiant du domaine',
    defaultBody: '{\n  "name": "Mon dataset",\n  "rowCount": 10,\n  "columns": [\n    { "columnName": "Prénom", "columnType": "FIRST_NAME" },\n    { "columnName": "Nom", "columnType": "LAST_NAME" },\n    { "columnName": "Email", "columnType": "EMAIL" }\n  ]\n}',
  },
  {
    method: 'DELETE',
    path: '/api/data-sets/{id}',
    description: 'Supprime un dataset',
    entityType: 'dataset',
    params: 'Path : {id} — identifiant du dataset',
  },
  {
    method: 'GET',
    path: '/api/data-sets/{id}/rows',
    description: 'Accès paginé aux lignes',
    entityType: 'dataset',
    params: 'Path : {id} | Query : page, size, rowIds (optionnel), cols (optionnel)',
  },
];

export default function CrudTab({ defaultDomainId, defaultDatasetId }) {
  return (
    <div>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Endpoints de gestion des domaines et datasets. Cliquez sur "Essayer" pour exécuter un appel en live.
      </Typography>
      {ENDPOINTS.map((ep, idx) => (
        <EndpointCard
          key={idx}
          endpoint={ep}
          defaultDomainId={defaultDomainId}
          defaultDatasetId={defaultDatasetId}
        />
      ))}
    </div>
  );
}
