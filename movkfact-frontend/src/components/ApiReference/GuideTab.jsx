import React, { useState } from 'react';
import { Box, TextField, Typography, Divider } from '@mui/material';
import SnippetBlock from './SnippetBlock';

function buildSnippets(baseUrl, { method = 'GET', path, body = null }) {
  const url = `${baseUrl}${path}`;
  const curl = body
    ? `curl -X ${method} "${url}" \\\n  -H "Content-Type: application/json" \\\n  -d '${JSON.stringify(body, null, 2)}'`
    : `curl "${url}"`;

  const js = body
    ? `const response = await fetch("${url}", {\n  method: "${method}",\n  headers: { "Content-Type": "application/json" },\n  body: JSON.stringify(${JSON.stringify(body, null, 2)})\n});\nconst data = await response.json();\nconsole.log(data);`
    : `const response = await fetch("${url}");\nconst data = await response.json();\nconsole.log(data);`;

  const python = body
    ? `import requests\n\nresponse = requests.${method.toLowerCase()}(\n    "${url}",\n    json=${JSON.stringify(body, null, 4).replace(/null/g, 'None')}\n)\nprint(response.json())`
    : `import requests\n\nresponse = requests.${method.toLowerCase()}("${url}")\nprint(response.json())`;

  return { curl, 'JavaScript (fetch)': js, 'Python (requests)': python };
}

const USE_CASES = [
  {
    title: '1. Lister tous les domaines',
    config: { method: 'GET', path: '/api/domains' },
  },
  {
    title: '2. Créer un domaine',
    config: { method: 'POST', path: '/api/domains', body: { name: 'Mon domaine', description: 'Description' } },
  },
  {
    title: '3. Générer un dataset pour un domaine',
    config: {
      method: 'POST',
      path: '/api/domains/1/data-sets',
      body: {
        name: 'Mon dataset',
        rowCount: 100,
        columns: [{ name: 'firstName', type: 'FIRST_NAME' }, { name: 'email', type: 'EMAIL' }],
      },
    },
  },
  {
    title: '4. Extraire toutes les lignes en JSON',
    config: { method: 'GET', path: '/api/data-sets/1/export?format=JSON&mode=full' },
  },
  {
    title: '5. Extraire un échantillon de 100 lignes en CSV',
    config: { method: 'GET', path: '/api/data-sets/1/export?format=CSV&mode=sample&count=100' },
  },
  {
    title: '6. Récupérer des lignes spécifiques par index',
    config: { method: 'GET', path: '/api/data-sets/1/rows?rowIds=0,5,10,99' },
  },
];

export default function GuideTab() {
  const [baseUrl, setBaseUrl] = useState('http://localhost:8080');

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
        <Typography variant="body2" color="text.secondary" sx={{ whiteSpace: 'nowrap' }}>
          Base URL :
        </Typography>
        <TextField
          value={baseUrl}
          onChange={e => setBaseUrl(e.target.value)}
          size="small"
          sx={{ width: 280 }}
          inputProps={{ 'data-testid': 'base-url-input' }}
        />
      </Box>

      {USE_CASES.map((uc, idx) => (
        <Box key={idx} sx={{ mb: 3 }}>
          <Typography variant="subtitle2" fontWeight={600} sx={{ mb: 1 }}>
            {uc.title}
          </Typography>
          <SnippetBlock snippets={buildSnippets(baseUrl, uc.config)} />
          {idx < USE_CASES.length - 1 && <Divider sx={{ mt: 3 }} />}
        </Box>
      ))}
    </Box>
  );
}
