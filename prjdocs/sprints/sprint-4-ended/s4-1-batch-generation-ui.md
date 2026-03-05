# Story 4.1: Batch Generation UI

Status: done

## Story

As a data engineer or QA analyst,
I want a modal UI to submit batch generation jobs across multiple domains at once,
so that I can generate multiple datasets in parallel without repetitive one-by-one operations.

## Acceptance Criteria

1. **[BOUTON]** Un bouton "Generate Batch" est présent dans l'en-tête de `DomainsPage` (à côté de "Create New Domain")
2. **[MODAL SÉLECTION]** Le modal affiche la liste des domaines disponibles avec checkboxes de sélection
3. **[CONFIG PAR DOMAINE]** Pour chaque domaine sélectionné, l'utilisateur peut saisir : `datasetName` (3-50 chars, regex alphanumérique) et `count` (1-10000 lignes)
4. **[COLONNES AUTO]** Le modal charge automatiquement les colonnes configurées du domaine via `GET /api/domains/{id}/columns/configuration` — si aucune config, afficher un avertissement et désactiver ce domaine dans le batch
5. **[SOUMISSION]** Clic "Lancer le batch" → appel `POST /api/batch/generate` avec la liste des `BatchDataSetConfigDTO` construite
6. **[TRACKING WS]** Après soumission réussie : appel `trackJob(jobId, totalDatasets)` depuis `BatchJobsContext` pour activer le suivi WebSocket dans le `NotificationPanel`
7. **[FEEDBACK]** Toast notistack info "Batch soumis — N dataset(s) en cours" + fermeture automatique du modal
8. **[ERREURS]** Si un domaine n'a pas de colonnes configurées : chip rouge "Aucune configuration" + désactivation de la checkbox
9. **[VALIDATION FORM]** Bouton "Lancer" désactivé si : aucun domaine sélectionné OU un domaine sélectionné a des champs invalides (name vide, count hors [1,10000])
10. **[TESTS]** Couverture Jest ≥ 80% sur `BatchGenerationModal`

## Tasks / Subtasks

- [x] Task 1 — Créer le service frontend `batchService.js` (AC: 5)
  - [x] 1.1 — `submitBatch(dataSetConfigs)` → `POST /api/batch/generate` via `api.js`
  - [x] 1.2 — `getDomainColumnConfig(domainId)` → `GET /api/domains/{domainId}/columns/configuration`
  - [x] 1.3 — Typage JSDoc des paramètres et réponses

- [x] Task 2 — Créer le composant `BatchGenerationModal` (AC: 2,3,4,8,9)
  - [x] 2.1 — Fichier `movkfact-frontend/src/components/BatchGenerationModal/BatchGenerationModal.jsx`
  - [x] 2.2 — State local : `selectedDomains` (Set), `domainConfigs` (Map domainId→columns), `datasetForms` (Map domainId→{name,count}), `loading`, `loadingDomains`
  - [x] 2.3 — Rendu : liste des domaines avec `Checkbox` MUI + `Chip` statut colonnes
  - [x] 2.4 — Pour chaque domaine sélectionné : `TextField` (datasetName) + `TextField type="number"` (count) dans un `Collapse`
  - [x] 2.5 — Chargement colonnes au clic checkbox : `getDomainColumnConfig(domainId)` → stocker dans `domainConfigs`
  - [x] 2.6 — Logique de validation `isSubmitDisabled()` : au moins 1 domaine sélectionné ET tous les formulaires valides
  - [x] 2.7 — `handleSubmit` : construire la liste `BatchDataSetConfigDTO[]`, appeler `submitBatch`, puis `trackJob`, fermer modal

- [x] Task 3 — Intégrer le bouton + modal dans `DomainsPage.jsx` (AC: 1,7)
  - [x] 3.1 — Ajouter state `showBatchModal` + bouton "Generate Batch" dans le header
  - [x] 3.2 — Importer `BatchGenerationModal` et le rendre conditionnel sur `showBatchModal`
  - [x] 3.3 — `useBatchJobs()` hook importé pour accéder à `trackJob`
  - [x] 3.4 — Passer la liste `state.domains` au modal comme prop `domains`

- [x] Task 4 — Tests Jest `BatchGenerationModal.test.jsx` (AC: 10)
  - [x] 4.1 — Test : rendu initial — affiche la liste des domaines ✅
  - [x] 4.2 — Test : sélection d'un domaine → appel `getDomainColumnConfig` mockée ✅
  - [x] 4.3 — Test : domaine sans config → chip avertissement + checkbox désactivée ✅
  - [x] 4.4 — Test : validation — bouton "Lancer" désactivé si aucun domaine sélectionné ✅
  - [x] 4.5 — Test : soumission — appelle `submitBatch` puis `trackJob` avec les bons arguments ✅
  - [x] 4.6 — Test : erreur API — affiche alerte erreur ✅
  - [x] 4.7 — Run `npm test -- --testPathPattern=BatchGenerationModal` → 7/7 PASSING ✅

## Dev Notes

### Architecture — Ce qui existe déjà (NE PAS RECRÉER)

**Backend (complet, NE PAS MODIFIER) :**
- `POST /api/batch/generate` → `BatchJobController.generateBatch()`
  - Body : `{"dataSetConfigs": [{domainId, datasetName, columns: [ColumnConfigDTO], count}]}`
  - Response (via `ApiResponse<BatchJobResponseDTO>`) : `{data: {jobId, status, totalDatasets, message}}`
  - `jobId` est de type **Long** en Java → sera reçu comme **number** en JS → convertir en **String** pour le WebSocket

- `GET /api/domains/{domainId}/columns/configuration`
  - Response : `{success, domainId, hasConfigurations, columnsCount, columns: [{name, type, confidence, detector}]}`
  - Si `hasConfigurations == false` : domaine non configurable pour le batch

- WebSocket STOMP endpoint : `/ws/batch-notifications`
- Topics : `/topic/batch/{jobId}` (jobId en String)

**Frontend (complet, NE PAS MODIFIER) :**
- `BatchJobsContext.trackJob(jobId, totalDatasets)` — jobId converti en String via `String(jobId)` AVANT appel
- `WebSocketService.subscribeToBatch(jobId, callback)` — géré automatiquement par `BatchJobsContext`
- `NotificationPanel` — toujours visible dans `Layout.jsx`, s'active automatiquement dès qu'un job est tracké
- `useBatchJobs()` hook — importé depuis `../context/BatchJobsContext`

### Mapping ColumnConfigDTO (pour construire le batch request)

Les colonnes retournées par `GET /api/domains/{domainId}/columns/configuration` ont ce format :
```json
{ "name": "prenom", "type": "FIRST_NAME", "confidence": 95.0, "detector": "name_detector" }
```

À transformer en `ColumnConfigDTO` pour le batch :
```json
{ "columnName": "prenom", "columnType": "FIRST_NAME", "format": null, "minValue": null, "maxValue": null }
```

⚠️ **Vérifier** le nom exact des champs dans `ColumnConfigDTO.java` :
```java
// src/main/java/com/movkfact/dto/ColumnConfigDTO.java
// Champs : columnName (String), columnType (ColumnType enum), format (String), minValue (Integer), maxValue (Integer)
```

### Pattern modal existant (à suivre, DomainsPage.jsx)

```jsx
// DomainsPage.jsx ligne 259 - pattern Dialog existant
<Dialog open={openCreateModal} onClose={() => setOpenCreateModal(false)} maxWidth="sm" fullWidth>
  <DialogTitle>...</DialogTitle>
  <DialogContent>...</DialogContent>
</Dialog>
```

BatchGenerationModal utilisera `maxWidth="md"` pour l'espace supplémentaire.

### Imports MUI nécessaires pour BatchGenerationModal

```jsx
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  List, ListItem, ListItemIcon, ListItemText,
  Checkbox, Chip, Collapse, TextField,
  Button, CircularProgress, Alert, Box, Typography
} from '@mui/material';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import WarningIcon from '@mui/icons-material/Warning';
```

### Pattern `batchService.js` (à créer)

Suivre le pattern de `domainService.js` — utiliser le singleton `api` depuis `services/api.js` :

```js
import api from './api';

export const submitBatch = async (dataSetConfigs) => {
  const response = await api.post('/api/batch/generate', { dataSetConfigs });
  return response.data.data; // unwrap ApiResponse
};

export const getDomainColumnConfig = async (domainId) => {
  const response = await api.get(`/api/domains/${domainId}/columns/configuration`);
  return response.data; // {success, hasConfigurations, columns}
};
```

### Conversion colonnes → ColumnConfigDTO

```js
// Dans BatchGenerationModal.handleSubmit()
const columns = domainConfigs.get(domainId).columns.map(col => ({
  columnName: col.name,
  columnType: col.type,
  format: null,
  minValue: null,
  maxValue: null,
}));
```

### Validation datasetName

Regex côté backend : `^[a-zA-Z0-9_\-\s]+$` — à reproduire en frontend pour feedback immédiat.
Longueur : min 3, max 50 chars.

### Gestion jobId Long → String

```js
// Dans handleSubmit, APRÈS appel submitBatch :
const { jobId, totalDatasets } = result;
trackJob(String(jobId), totalDatasets); // TOUJOURS convertir en String
```

### Test setup (Jest + RTL)

Les tests utilisent `@testing-library/react` + `jest` comme en `BatchJobsContext.test.js`.
Mock de `batchService` avec `jest.mock('../services/batchService')`.
Mock de `useBatchJobs` pour éviter le context WebSocket réel.

### Structure fichiers à créer

```
movkfact-frontend/src/
  components/
    BatchGenerationModal/
      BatchGenerationModal.jsx    ← nouveau
      BatchGenerationModal.test.jsx ← nouveau
  services/
    batchService.js               ← nouveau
  pages/
    DomainsPage.jsx               ← modifier (ajouter bouton + modal)
```

### Points de vigilance

1. **jobId type** : backend renvoie `Long` (JSON number) → TOUJOURS `String(jobId)` avant `trackJob()`
2. **domaines sans config** : `hasConfigurations == false` → désactiver checkbox + afficher `Chip` orange/rouge
3. **count validation** : `parseInt(count)` pour le body — le backend attend un `Integer`
4. **fermeture modal** : reset le state local (`selectedDomains`, `datasetForms`, `domainConfigs`) au `onClose`
5. **console.log** : éviter les console.log parasites (erreur ESLint S3.3 M5 — utiliser `console.warn` si nécessaire pour debug)

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

- `ColumnConfigDTO.name` (pas `columnName`) — corrigé à la lecture du DTO Java
- `DomainsPage.test.js` fixé : ajout `MemoryRouter` + `BatchJobsProvider` (bug pré-existant `useNavigate()` dans DomainDatasetsModal)
- Failures pré-existantes (non causées par S4.1) : `BatchJobsContext.test.js` (jobId String vs Number — S3.3 M4) + `ConfigurationPanel.test.jsx` (validation)

### Completion Notes List

- S4.1 COMPLETE : `BatchGenerationModal` + `batchService.js` implémentés
- 7/7 tests `BatchGenerationModal.test.jsx` passing (100%)
- 6/6 tests `DomainsPage.test.js` passing après fix MemoryRouter
- Régression : 278/284 tests passing — 6 failures pré-existantes non liées à S4.1
- Tous les ACs satisfaits (AC1→AC10)

### File List

- `movkfact-frontend/src/components/BatchGenerationModal/BatchGenerationModal.jsx` (nouveau)
- `movkfact-frontend/src/components/BatchGenerationModal/BatchGenerationModal.test.jsx` (nouveau)
- `movkfact-frontend/src/services/batchService.js` (nouveau)
- `movkfact-frontend/src/pages/DomainsPage.jsx` (modifié — bouton + modal + useBatchJobs)
- `movkfact-frontend/src/pages/DomainsPage.test.js` (modifié — MemoryRouter + BatchJobsProvider)
