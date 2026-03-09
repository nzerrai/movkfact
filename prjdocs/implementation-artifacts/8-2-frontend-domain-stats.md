# Story 8.2: Frontend Enrichissement liste domaines

Status: review

## Story

En tant que développeur ou analyste de données,
Je veux voir directement dans la liste des domaines le nombre de datasets, le total de lignes et les statuts d'activité,
Afin de naviguer efficacement sans avoir à ouvrir chaque domaine individuellement.

## Acceptance Criteria

1. **AC1 — Colonnes enrichies dans DomainTable (FR-002)**
   - Nouvelles colonnes : "Datasets" (chip MUI), "Lignes totales" (formaté : "12 500", "1,2M"), "Statuts" (badges icônes — visibles seulement si `true`)
   - Colonnes masquables sur mobile (masquer "Lignes totales" < 768px)
   - Données chargées depuis `GET /api/domains` enrichi (S8.1)
   - Skeleton loaders pendant le chargement initial

2. **AC2 — Modal datasets enrichie (FR-003)**
   - `DomainDatasetsModal` affiche pour chaque dataset : `datasetName`, `rowCount`, `columnCount` + badges Téléchargé / Modifié / Consulté + date dernière modification formatée
   - Tri par "Dernière modification" par défaut (desc)
   - Filtre rapide par statut : Tous / Modifiés / Téléchargés
   - Appel via `domainService.getDatasetsByDomain(domainId)` (pas de fetch hardcodé)

3. **AC3 — Indicateurs visuels cohérents (composant StatusBadge)**
   - Badge "Téléchargé" : chip vert (`#e8f5e9`) + icône `DownloadDoneIcon`
   - Badge "Modifié" : chip orange (`#fff3e0`) + icône `EditIcon`
   - Badge "Consulté" : chip bleu (`#e3f2fd`) + icône `VisibilityIcon`
   - Si aucun statut : chip gris "Nouveau"
   - Même composant utilisé dans liste domaines ET dans la modal

4. **AC4 — Tests Jest**
   - `StatusBadge` : les 4 états (downloaded, modified, viewed, none)
   - `DomainTable` : colonnes enrichies, skeleton, badges
   - `DomainDatasetsModal` : affichage statuts, tri, filtre
   - Coverage > 80% sur composants créés/modifiés

## Tasks / Subtasks

- [x] Ajouter `getDatasetsByDomain(domainId)` dans `domainService.js` (AC: 2)
  - [x] `GET /api/domains/{domainId}/data-sets` via `api.js` (pas de fetch hardcodé)
  - [x] Unwrapper `response.data.data`
- [x] Ajouter `formatRowCount` dans `utils/formatters.js` (AC: 1)
  - [x] ≥ 1 000 000 → "1,2M" ; ≥ 1 000 → "12 500" (toLocaleString fr-FR) ; sinon String
  - [x] Tests unitaires `formatters.test.js` — 7 tests (null, undefined, 0, <1000, ≥1000, ≥1M, décimales)
- [x] Créer `StatusBadge.jsx` + `StatusBadge.test.jsx` (AC: 3)
  - [x] Props : `{ downloaded, modified, viewed }` — tous boolean
  - [x] Chip MUI avec icône et couleur par état
  - [x] Cas "Nouveau" si tous false
  - [x] `index.js` d'export
- [x] Localiser et modifier le composant `DomainTable` (AC: 1)
  - [x] Fichier : `components/DomainTable.jsx` (séparé de DomainsPage)
  - [x] Ajouter 3 colonnes : Datasets (Chip), Lignes totales (formatRowCount), Statuts (StatusBadge)
  - [x] Skeleton MUI pendant chargement (remplace CircularProgress)
  - [x] Masquer "Lignes totales" en mobile via `useMediaQuery` (breakpoint sm)
  - [x] Tests Jest correspondants (7 existants + 7 nouveaux = 14 tests)
- [x] Enrichir `DomainDatasetsModal.jsx` (AC: 2)
  - [x] Remplacer `fetch('http://localhost:8080/...')` par `domainService.getDatasetsByDomain(domainId)`
  - [x] Afficher `StatusBadge` par dataset
  - [x] Afficher date `updatedAt` formatée (via `formatDateTime` de formatters.js)
  - [x] Tri par `updatedAt DESC` par défaut
  - [x] Filtre rapide par statut (Select MUI : Tous / Modifiés / Téléchargés)
  - [x] Mettre à jour `DomainDatasetsModal.test.jsx` (11 tests, mock domainService)

## Dev Notes

### État réel du code — points critiques

> ⚠️ **`DomainDatasetsModal` existe déjà** à `components/DomainDatasetsModal/DomainDatasetsModal.jsx`.
> Il utilise `UploadedDatasetsList` et fait un `fetch` hardcodé vers `http://localhost:8080`.
> **Ne pas réécrire** — enrichir en remplaçant le fetch par `domainService.getDatasetsByDomain()`.

> ⚠️ **`formatters.js` existe déjà** à `utils/formatters.js` avec `formatDate` et `formatDateTime`.
> **Ajouter `formatRowCount`** dans ce fichier — ne pas créer un nouveau fichier utils.

> ⚠️ **`domainService.js` n'a pas `getDatasetsByDomain()`** — à ajouter.
> L'URL réelle est `/api/domains/{domainId}/data-sets` (avec tiret, conforme au backend).

> ⚠️ **Le champ `name` du dataset** côté backend s'appelle `datasetName` (pas `name`).
> Vérifier ce que retourne le backend après S8.1 dans `DataSetSummaryDTO`.

> ℹ️ **`DomainTable`** — vérifier d'abord si c'est un composant séparé ou inliné dans `DomainsPage.jsx`.
> Adapter en conséquence.

### Ajout dans domainService.js

```javascript
/**
 * Get datasets for a domain with status info (requires S8.1 backend)
 * @param {number} domainId
 * @returns {Promise<Array>} Array of dataset objects with status
 */
export const getDatasetsByDomain = async (domainId) => {
  try {
    const response = await api.get(`/api/domains/${domainId}/data-sets`);
    return response.data.data || [];
  } catch (error) {
    throw error;
  }
};
```

### Ajout dans utils/formatters.js

```javascript
// Ajouter après les exports existants (formatDate, formatDateTime)
export const formatRowCount = (count) => {
  if (!count && count !== 0) return '—';
  if (count >= 1_000_000) return `${(count / 1_000_000).toFixed(1)}M`;
  if (count >= 1_000)     return count.toLocaleString('fr-FR');
  return String(count);
};
```

### Composant StatusBadge

```jsx
// movkfact-frontend/src/components/StatusBadge/StatusBadge.jsx
import { Stack, Chip } from '@mui/material';
import DownloadDoneIcon from '@mui/icons-material/DownloadDone';
import EditIcon from '@mui/icons-material/Edit';
import VisibilityIcon from '@mui/icons-material/Visibility';

const StatusBadge = ({ downloaded, modified, viewed }) => {
  if (!downloaded && !modified && !viewed) {
    return <Chip label="Nouveau" size="small" color="default" />;
  }
  return (
    <Stack direction="row" spacing={0.5} flexWrap="wrap">
      {downloaded && <Chip icon={<DownloadDoneIcon />} label="Téléchargé" size="small" sx={{ bgcolor: '#e8f5e9' }} />}
      {modified   && <Chip icon={<EditIcon />}         label="Modifié"    size="small" sx={{ bgcolor: '#fff3e0' }} />}
      {viewed     && <Chip icon={<VisibilityIcon />}   label="Consulté"   size="small" sx={{ bgcolor: '#e3f2fd' }} />}
    </Stack>
  );
};
export default StatusBadge;
```

### Enrichissement DomainDatasetsModal

```jsx
// Remplacer le fetch hardcodé :
// ❌ Avant :
const result = await fetch(`http://localhost:8080/api/domains/${domainId}/data-sets`, { method: 'GET' });
const data = await result.json();

// ✅ Après :
import { getDatasetsByDomain } from '../../services/domainService';
const datasets = await getDatasetsByDomain(domainId);
// datasets est déjà unwrappé (response.data.data)
```

### Responsive mobile

```jsx
import { useTheme, useMediaQuery } from '@mui/material';
const theme = useTheme();
const isMobile = useMediaQuery(theme.breakpoints.down('sm')); // < 600px

// Dans le tableau :
{!isMobile && <TableCell>{formatRowCount(domain.totalRows)}</TableCell>}
```

### Fichiers à créer / modifier

```
movkfact-frontend/src/
  services/
    domainService.js                ← ajouter getDatasetsByDomain()
  utils/
    formatters.js                   ← ajouter formatRowCount() (fichier existant)
  components/
    StatusBadge/
      StatusBadge.jsx               ← NOUVEAU
      StatusBadge.test.js           ← NOUVEAU
      index.js                      ← NOUVEAU
    DomainDatasetsModal/
      DomainDatasetsModal.jsx       ← enrichir (remplacer fetch + ajouter StatusBadge + filtre)
      DomainDatasetsModal.test.jsx  ← mettre à jour
    [DomainTable ou DomainsPage]    ← ajouter 3 colonnes + skeleton (localiser d'abord)
```

### Dépendance sur S8.1

- **S8.1 doit être mergé avant** que les données `datasetCount`, `totalRows`, `statuses` arrivent du backend.
- Si S8.1 n'est pas encore livré au moment du dev : utiliser des données mockées pour débloquer le front.

```javascript
// Mock temporaire si S8.1 pas encore dispo :
const mockDomains = domains.map(d => ({
  ...d,
  datasetCount: 0,
  totalRows: 0,
  statuses: { downloaded: false, modified: false, viewed: false }
}));
```

### Project Structure Notes

- Tests : Jest + React Testing Library (`@testing-library/react`) — voir `DomainDatasetsModal.test.jsx` existant pour le pattern
- MUI : `@mui/material` et `@mui/icons-material` — déjà installés
- `date-fns` : **à vérifier dans `package.json`** avant d'importer. Si absent, utiliser `formatDateTime` de `formatters.js` (déjà installé, utilise `Intl`)

### References

- Epic spec : [prjdocs/epics/epic-8-domain-enrichment/8-2-frontend-domain-stats.md](../epics/epic-8-domain-enrichment/8-2-frontend-domain-stats.md)
- Story backend dépendante : [prjdocs/implementation-artifacts/8-1-backend-domain-aggregates.md](./8-1-backend-domain-aggregates.md)
- Modal existante : `movkfact-frontend/src/components/DomainDatasetsModal/DomainDatasetsModal.jsx`
- Service domaine : `movkfact-frontend/src/services/domainService.js`
- Formatters existants : `movkfact-frontend/src/utils/formatters.js`

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

- ✅ `domainService.js` : `getDatasetsByDomain()` ajouté — GET `/api/domains/{domainId}/data-sets`, unwrap `response.data.data`
- ✅ `formatters.js` : `formatRowCount()` ajouté — gère null, M, fr-FR toLocaleString
- ✅ `formatters.test.js` : 7 tests unitaires pour `formatRowCount` (null, 0, <1000, ≥1000, ≥1M)
- ✅ `StatusBadge` : nouveau composant MUI Chip avec 4 états, 6 tests unitaires
- ✅ `DomainTable.jsx` : 3 nouvelles colonnes (Datasets/Chip, Lignes totales/formatRowCount masqué mobile, Statuts/StatusBadge), Skeleton loader, 14 tests (7 existants + 7 nouveaux)
- ✅ `DomainDatasetsModal.jsx` : fetch hardcodé remplacé par `domainService.getDatasetsByDomain()`, table enrichie avec StatusBadge + formatDateTime + filtre Select MUI (Tous/Modifiés/Téléchargés) + tri updatedAt DESC, 11 tests (mock service)
- ⚠️ 3 suites de tests pre-existantes échouent (BatchJobsContext, WizardStep2, ConfigurationPanel) — non liés à cette story (fichiers non modifiés, en échec avant)

### File List

- `movkfact-frontend/src/services/domainService.js` — modifié
- `movkfact-frontend/src/utils/formatters.js` — modifié
- `movkfact-frontend/src/components/StatusBadge/StatusBadge.jsx` — créé
- `movkfact-frontend/src/components/StatusBadge/StatusBadge.test.jsx` — créé
- `movkfact-frontend/src/components/StatusBadge/index.js` — créé
- `movkfact-frontend/src/components/DomainTable.jsx` — modifié
- `movkfact-frontend/src/components/DomainTable.test.js` — modifié
- `movkfact-frontend/src/components/DomainDatasetsModal/DomainDatasetsModal.jsx` — modifié
- `movkfact-frontend/src/components/DomainDatasetsModal/DomainDatasetsModal.test.jsx` — modifié
- `movkfact-frontend/src/utils/formatters.test.js` — créé
