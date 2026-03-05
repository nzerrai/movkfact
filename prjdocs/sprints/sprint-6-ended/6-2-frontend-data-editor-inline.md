# Story 6.2: Frontend Éditeur de données inline

Status: review

## Story

En tant que développeur ou Tech Lead,
Je veux consulter, modifier et supprimer des lignes directement dans l'interface sans outil externe,
so that je puisse corriger des données incorrectes et auditer les modifications depuis un onglet dédié "Éditeur de données".

## Acceptance Criteria

1. La vue dataset expose 2 onglets dans `DataViewerPage.jsx` : "Visualiseur" (S2.7 existant, inchangé) et "Éditeur de données" (nouveau). Navigation par onglets sans perte d'état : les deux composants restent montés (pattern `display: 'none'`). Badge sur l'onglet "Éditeur" affiche le nb de modifications de session (>0).
2. Tableau paginé server-side (50 lignes/page par défaut) depuis `GET /api/data-sets/{id}/rows`. Tri par colonne. `rowIndex` affiché comme première colonne grisée non-éditable.
3. Si `dataset.rowCount > 50 000` : banner MUI Alert en haut de l'onglet avec le message d'avertissement. Boutons Modifier et Supprimer grisés (`disabled`). Pagination et tri restent fonctionnels.
4. Double-clic sur une cellule → TextField MUI inline. Entrée ou clic hors cellule → PUT. Échap → annule sans appel API. Bordure orange sur cellule modifiée non-encore-sauvegardée. Toast "Ligne X modifiée avec succès" après sauvegarde. Toast d'erreur si API échoue. Bouton "Modifier" alternatif par ligne.
5. Bouton icône poubelle (MUI IconButton) par ligne. Dialog de confirmation. Après confirmation : DELETE, ligne disparaît, `rowCount` mis à jour dans le header. Toast "Ligne X supprimée".
6. Panneau latéral rétractable "Historique des modifications". Chaque entrée : horodatage, action (ROW_MODIFIED/ROW_DELETED), rowIndex. Filtre par type d'action. Bouton "Actualiser". Max 100 entrées.
7. Tests Jest : `DataEditorTab.jsx` (rendu, double-clic, annulation, soumission), `DeleteRowDialog.jsx` (affichage, confirmation, annulation), `ActivityPanel.jsx` (rendu liste, filtre). Coverage >80%.

## Tasks / Subtasks

- [x] Tâche 1 — Étendre `dataSetService.js` (AC: #2, #4, #5, #6)
  - [x] Ajouter `getRows(datasetId, page, size)` → `GET /api/data-sets/{id}/rows?page=&size=`
  - [x] Ajouter `getRow(datasetId, rowIndex)` → `GET /api/data-sets/{id}/rows/{rowIndex}`
  - [x] Ajouter `updateRow(datasetId, rowIndex, columns)` → `PUT /api/data-sets/{id}/rows/{rowIndex}` avec `{ columns }`
  - [x] Ajouter `deleteRow(datasetId, rowIndex)` → `DELETE /api/data-sets/{id}/rows/{rowIndex}`
  - [x] Les 4 nouvelles fonctions suivent le même pattern que les existantes (try/catch, `api.get/put/delete`, `response.data.data || response.data`)

- [x] Tâche 2 — Modifier `DataViewerPage.jsx` pour ajouter les onglets (AC: #1)
  - [x] Ajouter `const [activeTab, setActiveTab] = useState(0)` et `const [editorModifiedCount, setEditorModifiedCount] = useState(0)`
  - [x] Ajouter `<Tabs>` avec 2 `<Tab>` : "Visualiseur" et "Éditeur de données" + Badge conditionnel
  - [x] Encapsuler `<DataViewerContainer>` dans `<Box sx={{ display: activeTab === 0 ? 'block' : 'none' }}>` (préserver le mounting)
  - [x] Ajouter `<Box sx={{ display: activeTab === 1 ? 'block' : 'none' }}>` → `<DataEditorTab datasetId={datasetId} rowCount={dataset?.rowCount} onModifiedCountChange={setEditorModifiedCount} />`
  - [x] Importer `DataEditorTab` depuis `../components/DataEditor/DataEditorTab`

- [x] Tâche 3 — Créer `DataEditorTab.jsx` (AC: #1-6)
  - [x] State : `rows`, `totalRows`, `page`, `editingCell` (`{rowIndex, colName}`), `pendingValue`, `activityOpen`, `modifiedCount`
  - [x] Charger les lignes via `getRows(datasetId, page, 50)` au montage et au changement de page
  - [x] Guard `rowCount > 50 000` : afficher MUI `<Alert severity="warning">` + passer `disabled` aux composants enfants
  - [x] Appeler `onModifiedCountChange(modifiedCount)` quand `modifiedCount` change (via useEffect)
  - [x] Rendre `<DataEditorTable>` et `<ActivityPanel>` (rétractable via Drawer ou Collapse latéral)

- [x] Tâche 4 — Créer `DataEditorTable.jsx` avec `EditableRow.jsx` et `EditableCell.jsx` (AC: #2, #4, #5)
  - [x] `DataEditorTable` : MUI `Table` + `TableHead` (headers + sort indicator) + `TableBody` avec `EditableRow × N`
  - [x] Première colonne `rowIndex` : `<TableCell sx={{ color: 'text.disabled', bgcolor: 'grey.50' }}>` — non cliquable
  - [x] `EditableCell` : si `editingCell.rowIndex === rowIndex && editingCell.colName === colName` → `<TextField size="small" autoFocus value={pendingValue} />`, sinon valeur texte avec `onDoubleClick`
  - [x] Touche Entrée (`onKeyDown` e.key === 'Enter') → appeler `onSubmit(rowIndex, colName, pendingValue)`
  - [x] Touche Échap → appeler `onCancel()`
  - [x] `onBlur` sur le TextField → appeler `onSubmit` (clic hors cellule confirme)
  - [x] Bordure orange si cellule en mode édition : `sx={{ border: '2px solid orange' }}`
  - [x] Bouton Supprimer : `<IconButton size="small" onClick={() => onDeleteClick(rowIndex)}>` avec `<DeleteIcon />`
  - [x] Bouton Modifier : `<IconButton size="small" onClick={() => onEditClick(rowIndex)}>` avec `<EditIcon />`

- [x] Tâche 5 — Créer `DeleteRowDialog.jsx` (AC: #5)
  - [x] Props : `open`, `rowIndex`, `onConfirm`, `onCancel`, `loading`
  - [x] Texte : `"Supprimer la ligne {rowIndex} ? Cette action est irréversible dans l'éditeur. Le reset complet du dataset reste disponible."`
  - [x] Pattern identique à `DeleteConfirmDialog.jsx` existant

- [x] Tâche 6 — Créer `ActivityPanel.jsx` (AC: #6)
  - [x] Props : `datasetId`, `open`, `onClose`
  - [x] Charger activités via `getDatasetActivity(datasetId, filterType, 0, 100)`
  - [x] Filtre : `<ToggleButtonGroup>` ou `<Select>` pour Tout / ROW_MODIFIED / ROW_DELETED
  - [x] Afficher : horodatage, Chip action coloré, `rowIndex` si présent
  - [x] Pattern similaire à `ActivityFeed.jsx` existant (même endpoint, mêmes patterns)
  - [x] Bouton "Actualiser" → re-fetch

- [x] Tâche 7 — Créer `index.js` dans `DataEditor/`
  - [x] Exporter `DataEditorTab`, `DataEditorTable`, `DeleteRowDialog`, `ActivityPanel`

- [x] Tâche 8 — Tests Jest (AC: #7)
  - [x] `DataEditorTab.test.js` : rendu initial, chargement rows (mock `getRows`), guard > 50k lignes
  - [x] `DeleteRowDialog.test.js` : rendu quand `open=true`, boutons présents, appel `onConfirm`/`onCancel`
  - [x] `ActivityPanel.test.js` : rendu liste, changement filtre, bouton Actualiser
  - [x] Mock `dataSetService` : `jest.mock('../../../services/dataSetService', () => ({ getRows: jest.fn(), ... }))`
  - [x] Mock `notistack` pour les tests avec toast : `jest.mock('notistack', () => ({ useSnackbar: () => ({ enqueueSnackbar: jest.fn() }) }))`

## Dev Notes

### Pattern onglets sans perte d'état — CRITIQUE

```jsx
// DataViewerPage.jsx — PATTERN À UTILISER (display:none préserve le state)
// NE PAS utiliser le rendu conditionnel {activeTab === 0 && <Component/>}
// car cela démonte le composant et perd la pagination/filtres

const [activeTab, setActiveTab] = useState(0);
const [editorModifiedCount, setEditorModifiedCount] = useState(0);

// Dans le JSX :
<Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
  <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)}>
    <Tab label="Visualiseur" />
    <Tab
      label={
        editorModifiedCount > 0
          ? `Éditeur de données (${editorModifiedCount})`
          : 'Éditeur de données'
      }
    />
  </Tabs>
</Box>

<Box sx={{ display: activeTab === 0 ? 'block' : 'none' }}>
  <DataViewerContainer dataset={dataset} domainId={dataset.domainId} onBack={handleBack} />
</Box>
<Box sx={{ display: activeTab === 1 ? 'block' : 'none' }}>
  <DataEditorTab
    datasetId={Number(datasetId)}
    rowCount={dataset?.rowCount}
    onModifiedCountChange={setEditorModifiedCount}
  />
</Box>
```

### Toasts notistack — déjà configuré dans App.jsx

```jsx
// SnackbarProvider maxSnack={5} est déjà dans App.jsx — PRÊT À L'EMPLOI
import { useSnackbar } from 'notistack';

const { enqueueSnackbar } = useSnackbar();

// Succès :
enqueueSnackbar(`Ligne ${rowIndex} modifiée avec succès`, { variant: 'success' });
// Erreur :
enqueueSnackbar(`Erreur: ${error.response?.data?.message || error.message}`, { variant: 'error' });
// Suppression :
enqueueSnackbar(`Ligne ${rowIndex} supprimée`, { variant: 'info' });
```

### Logique de soumission cellule dans DataEditorTab

```jsx
const handleCellSubmit = async (rowIndex, colName, newValue) => {
  try {
    await updateRow(datasetId, rowIndex, { [colName]: newValue });
    // Mise à jour locale optimiste :
    setRows(prev => prev.map(r =>
      r.rowIndex === rowIndex ? { ...r, data: { ...r.data, [colName]: newValue } } : r
    ));
    setEditingCell(null);
    setModifiedCount(prev => prev + 1);
    enqueueSnackbar(`Ligne ${rowIndex} modifiée avec succès`, { variant: 'success' });
  } catch (err) {
    enqueueSnackbar(err.response?.data?.message || 'Erreur lors de la modification', { variant: 'error' });
    // Garder la cellule en mode édition si erreur
  }
};

const handleCellCancel = () => {
  setEditingCell(null);  // aucun appel API
};
```

### Logique suppression avec réindexation

```jsx
const handleDeleteConfirm = async () => {
  try {
    await deleteRow(datasetId, pendingDeleteRowIndex);
    // Recharger la page courante (réindexation côté serveur)
    await loadRows(page);
    // Décrémenter rowCount local :
    setTotalRows(prev => prev - 1);
    enqueueSnackbar(`Ligne ${pendingDeleteRowIndex} supprimée`, { variant: 'info' });
    setModifiedCount(prev => prev + 1);
    setDeleteDialogOpen(false);
  } catch (err) {
    enqueueSnackbar(err.response?.data?.message || 'Erreur lors de la suppression', { variant: 'error' });
  }
};
```

### Structure des nouvelles fonctions dans dataSetService.js

```js
// Suivre EXACTEMENT le même pattern que les fonctions existantes :
import api from './api';

export const getRows = async (datasetId, page = 0, size = 50) => {
  try {
    const response = await api.get(`/api/data-sets/${datasetId}/rows`, { params: { page, size } });
    return response.data.data || response.data;
    // Retourne : { rows: [{rowIndex, data: {...}}], totalRows, page, size }
  } catch (error) {
    throw error;
  }
};

export const updateRow = async (datasetId, rowIndex, columns) => {
  try {
    const response = await api.put(`/api/data-sets/${datasetId}/rows/${rowIndex}`, { columns });
    return response.data.data || response.data;
  } catch (error) {
    throw error;  // Laisser le composant gérer (toast d'erreur)
  }
};

export const deleteRow = async (datasetId, rowIndex) => {
  try {
    await api.delete(`/api/data-sets/${datasetId}/rows/${rowIndex}`);
  } catch (error) {
    throw error;
  }
};
```

### Guard datasets larges (AC3) — rowCount > 50 000

```jsx
// En haut du rendu DataEditorTab, AVANT le tableau :
{rowCount > 50000 && (
  <Alert severity="warning" sx={{ mb: 2 }}>
    Ce dataset dépasse 50 000 lignes. L'édition inline est désactivée.
    Utilisez le reset ou régénérez.
  </Alert>
)}

// Passer disabled aux composants enfants :
<DataEditorTable
  rows={rows}
  disabled={rowCount > 50000}
  // ...
/>
// Dans EditableRow : <IconButton disabled={disabled}> pour Modifier et Supprimer
```

### ActivityPanel — Nouveaux types ROW_MODIFIED / ROW_DELETED

```jsx
// ActivityFeed.jsx existant gère ces actions via getActionColor/getActionLabel
// Pour ActivityPanel, ÉTENDRE ces fonctions :

const getActionColor = (action) => {
  switch (action) {
    case 'ROW_MODIFIED': return 'warning';
    case 'ROW_DELETED':  return 'error';
    // ... autres cas existants
  }
};

// Afficher rowIndex si présent dans l'activité :
{activity.rowIndex !== null && activity.rowIndex !== undefined && (
  <Typography variant="caption" color="textSecondary">
    Ligne {activity.rowIndex}
  </Typography>
)}
```

### Pattern Mock pour tests Jest

```js
// Dans les fichiers de test, mocker les services :
jest.mock('../../../services/dataSetService', () => ({
  getRows: jest.fn().mockResolvedValue({
    rows: [{ rowIndex: 0, data: { firstName: 'Alice', email: 'alice@test.com' } }],
    totalRows: 1,
    page: 0,
    size: 50
  }),
  updateRow: jest.fn().mockResolvedValue({ rowIndex: 0, data: { firstName: 'Bob', email: 'alice@test.com' } }),
  deleteRow: jest.fn().mockResolvedValue(undefined),
  getDatasetActivity: jest.fn().mockResolvedValue([])
}));

// Mocker notistack :
jest.mock('notistack', () => ({
  useSnackbar: () => ({ enqueueSnackbar: jest.fn() }),
  SnackbarProvider: ({ children }) => children
}));
```

### Project Structure Notes

- **NE PAS modifier** `DataViewerContainer.jsx` — read-only S2.7
- `DataViewerPage.jsx` : seule modification = ajout Tabs + import `DataEditorTab`
- Nouveaux fichiers dans `src/components/DataEditor/` (dossier à créer)
- `dataSetService.js` : ajouter 4 fonctions à la fin, ne pas modifier l'existant
- MUI v5.14 disponible — `@mui/icons-material` pour `DeleteIcon`, `EditIcon`, `HistoryIcon`
- `notistack` v3 déjà configuré dans `App.jsx` (`SnackbarProvider` avec `maxSnack=5`)
- Pas de TypeScript — JSX pur (pattern cohérent avec la codebase existante)
- Tests : `@testing-library/react` + `@testing-library/user-event` (voir `DeleteConfirmDialog.test.js`)
- Le proxy `http://localhost:8080` est configuré dans `package.json` — les appels `/api/...` fonctionnent en dev

### Fichiers à créer / modifier

```
Nouveaux :
movkfact-frontend/src/components/DataEditor/
  DataEditorTab.jsx         ← orchestrateur principal de l'onglet
  DataEditorTab.test.js
  DataEditorTable.jsx       ← MUI Table + sort + pagination server-side
  EditableRow.jsx           ← ligne avec cellules éditables + boutons actions
  EditableCell.jsx          ← cellule double-clic → TextField inline
  DeleteRowDialog.jsx       ← dialog confirmation suppression (pattern DeleteConfirmDialog)
  DeleteRowDialog.test.js
  ActivityPanel.jsx         ← panneau latéral historique (pattern ActivityFeed)
  ActivityPanel.test.js
  index.js                  ← exports

Modifiés :
movkfact-frontend/src/pages/DataViewerPage.jsx
  + useState activeTab, editorModifiedCount
  + <Tabs> avec 2 <Tab>
  + display:none pattern
  + import + mount DataEditorTab

movkfact-frontend/src/services/dataSetService.js
  + getRows(datasetId, page, size)
  + getRow(datasetId, rowIndex)
  + updateRow(datasetId, rowIndex, columns)
  + deleteRow(datasetId, rowIndex)
```

### References

- [Source: pages/DataViewerPage.jsx] — structure, useParams, dataset prop passé à DataViewerContainer
- [Source: components/DataViewer/DataViewerContainer.jsx] — l.1-68 : imports Tabs/Tab MUI déjà présents, DO NOT MODIFY
- [Source: components/DataViewer/DataTable.jsx] — pattern MUI Table + TablePagination à réutiliser
- [Source: components/DataViewer/ActivityFeed.jsx] — pattern getDatasetActivity, getActionColor, formatTimestamp
- [Source: components/DeleteConfirmDialog.jsx] — pattern Dialog confirmation
- [Source: services/dataSetService.js] — pattern fonctions api.get/put/delete existantes
- [Source: services/api.js] — axios instance avec REACT_APP_API_URL + proxy localhost:8080
- [Source: App.jsx] — SnackbarProvider notistack déjà configuré (maxSnack=5)
- [Source: package.json] — "@mui/material": "^5.14.0", "notistack": "^3.0.0", "@testing-library/react": "^13.4.0"
- [Source: epic-6-data-editor/6-2-frontend-data-editor-inline.md] — spec complète avec AC3 guard 50k lignes

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

- Implémentation complète en 2 sessions (S6.2)
- Tous les composants DataEditor créés : DataEditorTab, DataEditorTable, EditableRow, EditableCell, DeleteRowDialog, ActivityPanel, index.js
- dataSetService.js étendu avec getRows, getRow, updateRow, deleteRow, getDatasetActivity
- DataViewerPage.jsx modifié avec pattern display:none pour 2 onglets
- 16/16 tests passent (DataEditorTab x5, DeleteRowDialog x5, ActivityPanel x5, DeleteRowDialog.test.js x1)
- Fix clé : resetMocks:true dans CRA (react-scripts) efface mockResolvedValue → déplacer dans beforeEach()
- Fix clé : getAllByLabelText() trouve les span MUI Tooltip → utiliser getAllByRole('button', {name: ...})

### File List

- movkfact-frontend/src/components/DataEditor/DataEditorTab.jsx
- movkfact-frontend/src/components/DataEditor/DataEditorTab.test.js
- movkfact-frontend/src/components/DataEditor/DataEditorTable.jsx
- movkfact-frontend/src/components/DataEditor/EditableRow.jsx
- movkfact-frontend/src/components/DataEditor/EditableCell.jsx
- movkfact-frontend/src/components/DataEditor/DeleteRowDialog.jsx
- movkfact-frontend/src/components/DataEditor/DeleteRowDialog.test.js
- movkfact-frontend/src/components/DataEditor/ActivityPanel.jsx
- movkfact-frontend/src/components/DataEditor/ActivityPanel.test.js
- movkfact-frontend/src/components/DataEditor/index.js
- movkfact-frontend/src/services/dataSetService.js (étendu)
- movkfact-frontend/src/pages/DataViewerPage.jsx (modifié)
