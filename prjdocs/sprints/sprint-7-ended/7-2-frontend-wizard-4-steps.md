# Story S7.2 — Frontend Wizard de création manuelle 4 étapes

**Status:** done
**Sprint:** Sprint 3
**Points:** 8
**Epic:** EPIC 7 — Wizard de création manuelle
**Type:** Frontend Feature
**Lead:** Sally
**Dependencies:** S7.1 (Preview endpoint + contraintes), S1.5 (Domain Management UI)

---

## Story

**En tant que** ingénieur QA,
**Je veux** créer un dataset from scratch via un wizard guidé en 4 étapes, avec configuration de chaque colonne, ses contraintes et un aperçu avant génération,
**Afin de** produire des données de test précises sans avoir besoin d'un fichier CSV source.

---

## Acceptance Criteria

1. **AC1 — Point d'entrée** : DomainTable affiche un bouton "Créer un dataset" par ligne → click ouvre `CreateDatasetChoiceDialog` → choix "Upload CSV" (existant) ou "Création manuelle" (nouveau) → sélection "Création manuelle" ouvre `ManualWizardModal` avec `domainId` pré-rempli.
2. **AC2 — Étape 1 Nom et lignes** : `TextField` nom dataset (3–50 chars, `[a-zA-Z0-9 _-]`) + `TextField` numérique rowCount (1–100 000) + Slider optionnel (100/1000/10000/100000) ; "Suivant →" désactivé tant que formulaire invalide.
3. **AC3 — Étape 2 Colonnes** : bouton "+ Ajouter une colonne" ; chaque ligne = nom + Select type + `DynamicConstraintsPanel` conditionnel ; drag-and-drop pour réordonner (@dnd-kit/sortable) ; bouton poubelle (minimum 1 colonne) ; "← Retour" conserve l'état ; "Prévisualiser →" appelle `POST /api/datasets/preview` et passe à l'étape 3.
4. **AC4 — Étape 3 Prévisualisation** : tableau 5 lignes du résultat preview ; spinner pendant l'appel API ; message d'erreur avec retour si l'appel échoue ; "← Modifier colonnes" revient étape 2 ; "Confirmer →" passe étape 4.
5. **AC5 — Étape 4 Confirmation** : récapitulatif (nom, domaine, rowCount, colonnes+types) ; "Lancer la génération" appelle `POST /api/domains/{domainId}/data-sets` ; toast "Dataset '{name}' créé avec {N} lignes" en succès ; message d'erreur + relancer en échec.
6. **AC6 — Tests** : tests Jest pour chaque étape (rendu + validation + callbacks) ; test flux complet 1→4 avec mocks API ; test retour arrière conserve l'état ; coverage >80% sur les nouveaux composants.

---

## Tasks / Subtasks

- [x] **T1 — Install @dnd-kit** : `npm install @dnd-kit/core @dnd-kit/sortable @dnd-kit/utilities` dans movkfact-frontend/
- [x] **T2 — domainService.js** : ajouter `previewDataset(columns, count)` → `POST /api/datasets/preview`
- [x] **T3 — DomainTable.jsx** : ajouter prop `onCreateDataset`, bouton AddCircleOutlineIcon "Créer un dataset" (desktop: IconButton + Tooltip ; mobile: Button) ; ne PAS supprimer les boutons existants
- [x] **T4 — DomainsPage.jsx** : ajouter états `showChoiceDialog`, `wizardDomainId`, `showManualWizard` ; handlers `handleCreateDatasetClick`, `handleChoiceCSV`, `handleChoiceManual` ; importer et rendre `CreateDatasetChoiceDialog` et `ManualWizardModal` en bas du JSX
- [x] **T5 — CreateDatasetChoiceDialog.jsx** : Dialog simple avec 2 boutons (CSV / Manual) + props `open`, `domainId`, `onChooseCSV`, `onChooseManual`, `onClose`
- [x] **T6 — ManualWizardModal.jsx** : Dialog fullWidth maxWidth="lg" ; MUI Stepper (activeStep 0–3) ; tout l'état du wizard centralisé ici ; rend WizardStep1/2/3/4 en passant props+callbacks
- [x] **T7 — WizardStep1_NameAndRows.jsx** : TextField nom (validation regex + longueur) + TextField rowCount + Slider ; prop `onNext(name, rowCount)` appelée si valide ; boutons navigation
- [x] **T8 — WizardStep2_ColumnConfig.jsx** : liste de `ColumnRow` draggables via @dnd-kit/sortable ; gestion addColumn/removeColumn/reorder ; boutons Retour + Prévisualiser
- [x] **T9 — ColumnRow.jsx** : TextField nom + Select ColumnType + DynamicConstraintsPanel + bouton poubelle ; draggable avec useSortable()
- [x] **T10 — DynamicConstraintsPanel.jsx** : rendu conditionnel selon column.type (MinMax / DateRange / MaxLength / null)
- [x] **T11 — WizardStep3_Preview.jsx** : appel `previewDataset` au montage ; spinner / erreur / tableau 5 lignes ; boutons Modifier colonnes + Confirmer
- [x] **T12 — WizardStep4_Confirm.jsx** : récapitulatif en lecture seule ; bouton "Lancer la génération" → appelle `POST /api/domains/{domainId}/data-sets` via domainService ou api.js ; gestion succès/erreur
- [x] **T13 — Tests** : WizardStep1.test.js (6), WizardStep2.test.js (6), WizardStep3.test.js (5), WizardStep4.test.js (4) — 21 nouveaux tests ; 28/28 ManualWizard+DomainTable passent

---

## Dev Notes

### CRITIQUE — Prérequis npm

`@dnd-kit/core`, `@dnd-kit/sortable`, `@dnd-kit/utilities` **ne sont PAS dans package.json**. Sans ce `npm install`, tous les imports @dnd-kit plantent. Exécuter en premier :

```bash
cd movkfact-frontend
npm install @dnd-kit/core @dnd-kit/sortable @dnd-kit/utilities
```

Alternative légère si @dnd-kit pose problème : utiliser des boutons ↑/↓ MUI (IconButton ArrowUpward/ArrowDownward) pour réordonner — plus simple, moins UX mais fonctionnel.

---

### Pattern d'état dans DomainsPage.jsx (T4)

DomainsPage gère tous ses modals avec un couple `(showXxx, setShowXxx)` + `(selectedXxxDomain, setSelectedXxxDomain)`. Copier le pattern existant :

```jsx
// Lignes à ajouter après la ligne 52 (showBatchModal)
const [showChoiceDialog, setShowChoiceDialog] = useState(false);
const [wizardDomainId, setWizardDomainId] = useState(null);
const [showManualWizard, setShowManualWizard] = useState(false);
```

```jsx
// Handler à ajouter après handleViewDatasets (~ligne 143)
const handleCreateDatasetClick = useCallback((domain) => {
  setWizardDomainId(domain.id);
  setShowChoiceDialog(true);
}, []);

const handleChoiceCSV = useCallback(() => {
  setShowChoiceDialog(false);
  setSelectedDomainId(wizardDomainId);
  setShowUploader(true);
}, [wizardDomainId]);

const handleChoiceManual = useCallback(() => {
  setShowChoiceDialog(false);
  setShowManualWizard(true);
}, []);
```

DomainTable dans le JSX (~ligne 243) — ajouter `onCreateDataset={handleCreateDatasetClick}` :

```jsx
<DomainTable
  domains={state.domains}
  searchText={state.searchText}
  onEdit={handleEditClick}
  onDelete={(domain) => { setDeleteTarget(domain); setDeleteConfirmOpen(true); }}
  onUpload={handleUploadClick}
  onViewDatasets={handleViewDatasets}
  onCreateDataset={handleCreateDatasetClick}   // ← NOUVEAU
  loading={state.loading}
/>
```

Rendre les deux nouveaux composants en bas du JSX (après le `DomainDatasetsModal`) :

```jsx
{/* Choice Dialog (S7.2) */}
<CreateDatasetChoiceDialog
  open={showChoiceDialog}
  onChooseCSV={handleChoiceCSV}
  onChooseManual={handleChoiceManual}
  onClose={() => { setShowChoiceDialog(false); setWizardDomainId(null); }}
/>

{/* Manual Wizard (S7.2) */}
<ManualWizardModal
  open={showManualWizard}
  domainId={wizardDomainId}
  onClose={() => { setShowManualWizard(false); setWizardDomainId(null); }}
  onSuccess={(datasetName, rowCount) => {
    setShowManualWizard(false);
    setWizardDomainId(null);
    enqueueSnackbar(`Dataset '${datasetName}' créé avec ${rowCount} lignes`, { variant: 'success' });
  }}
/>
```

---

### DomainTable.jsx — Ajout du bouton (T3)

Imports à ajouter en haut du fichier :

```jsx
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
```

Desktop (dans les `<TableCell align="center">` de chaque ligne, **AVANT** le bouton StorageIcon) :

```jsx
<Tooltip title="Créer un dataset">
  <IconButton
    size="small"
    onClick={() => onCreateDataset?.(domain)}
    sx={{ color: 'success.main' }}
  >
    <AddCircleOutlineIcon fontSize="small" />
  </IconButton>
</Tooltip>
```

Mobile (dans `<Box sx={{ display: 'flex', gap: 1, mt: 2 }}>`, avant "Upload CSV") :

```jsx
<Button
  size="small"
  variant="outlined"
  startIcon={<AddCircleOutlineIcon />}
  onClick={() => onCreateDataset?.(domain)}
  fullWidth
>
  Créer
</Button>
```

---

### domainService.js — previewDataset (T2)

Ajouter à la fin de `domainService.js`. Attention : cet endpoint est défini dans S7.1 backend — la réponse est un tableau de rows (tableau d'objets) encapsulé dans `ApiResponse<List<Map<String,Object>>>`.

```js
/**
 * Preview 5 sample rows from column configuration (S7.1)
 * @param {Array} columns - [{ name, type, constraints }]
 * @param {number} count - Number of preview rows (5)
 * @returns {Promise<Array>} Array of row objects
 */
export const previewDataset = async (columns, count = 5) => {
  try {
    const response = await api.post('/api/datasets/preview', { columns, count });
    return response.data.data || response.data;
  } catch (error) {
    throw error;
  }
};
```

Pour la génération finale (étape 4), réutiliser l'endpoint existant `POST /api/domains/{domainId}/data-sets`. Le payload doit correspondre à `GenerationRequestDTO` :

```js
// Appel dans WizardStep4_Confirm.jsx (via api.js direct ou domainService)
const payload = {
  dataSetName: datasetName,
  domainId: domainId,
  rowCount: rowCount,
  columns: columns.map(col => ({
    name: col.name,
    type: col.type,
    constraints: col.constraints || {}
  }))
};
const response = await api.post(`/api/domains/${domainId}/data-sets`, payload);
```

---

### ManualWizardModal.jsx — Structure complète (T6)

```jsx
import React, { useState } from 'react';
import { Dialog, DialogTitle, DialogContent, Stepper, Step, StepLabel, IconButton, Box } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import WizardStep1_NameAndRows from './WizardStep1_NameAndRows';
import WizardStep2_ColumnConfig from './WizardStep2_ColumnConfig';
import WizardStep3_Preview from './WizardStep3_Preview';
import WizardStep4_Confirm from './WizardStep4_Confirm';

const STEPS = ['Nom & lignes', 'Colonnes', 'Aperçu', 'Confirmation'];

const ManualWizardModal = ({ open, domainId, onClose, onSuccess }) => {
  const [step, setStep] = useState(0);
  const [datasetName, setDatasetName] = useState('');
  const [rowCount, setRowCount] = useState(1000);
  const [columns, setColumns] = useState([]);
  const [previewRows, setPreviewRows] = useState([]);

  const handleReset = () => {
    setStep(0);
    setDatasetName('');
    setRowCount(1000);
    setColumns([]);
    setPreviewRows([]);
  };

  const handleClose = () => {
    handleReset();
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="lg" fullWidth>
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        Créer un dataset — Étape {step + 1}/{STEPS.length}
        <IconButton onClick={handleClose}><CloseIcon /></IconButton>
      </DialogTitle>
      <Box sx={{ px: 3, pt: 1 }}>
        <Stepper activeStep={step}>
          {STEPS.map((label) => (
            <Step key={label}><StepLabel>{label}</StepLabel></Step>
          ))}
        </Stepper>
      </Box>
      <DialogContent>
        {step === 0 && (
          <WizardStep1_NameAndRows
            datasetName={datasetName}
            rowCount={rowCount}
            onNext={(name, count) => { setDatasetName(name); setRowCount(count); setStep(1); }}
          />
        )}
        {step === 1 && (
          <WizardStep2_ColumnConfig
            columns={columns}
            onColumnsChange={setColumns}
            onBack={() => setStep(0)}
            onPreview={(cols) => { setColumns(cols); setStep(2); }}
          />
        )}
        {step === 2 && (
          <WizardStep3_Preview
            columns={columns}
            rowCount={rowCount}
            previewRows={previewRows}
            onPreviewLoaded={setPreviewRows}
            onBack={() => setStep(1)}
            onConfirm={() => setStep(3)}
          />
        )}
        {step === 3 && (
          <WizardStep4_Confirm
            datasetName={datasetName}
            domainId={domainId}
            rowCount={rowCount}
            columns={columns}
            onBack={() => setStep(2)}
            onSuccess={() => { handleReset(); onSuccess(datasetName, rowCount); }}
          />
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ManualWizardModal;
```

---

### WizardStep1_NameAndRows.jsx — Validation (T7)

```jsx
const NAME_REGEX = /^[a-zA-Z0-9 _-]{3,50}$/;
const isNameValid = NAME_REGEX.test(datasetName);
const isRowCountValid = rowCount >= 1 && rowCount <= 100000;
const isValid = isNameValid && isRowCountValid;
```

Slider avec marks prédéfinis :

```jsx
const MARKS = [
  { value: 100, label: '100' },
  { value: 1000, label: '1k' },
  { value: 10000, label: '10k' },
  { value: 100000, label: '100k' },
];
<Slider
  value={rowCount}
  min={1}
  max={100000}
  marks={MARKS}
  onChange={(_, val) => setLocalRowCount(val)}
  valueLabelDisplay="auto"
/>
```

---

### WizardStep2_ColumnConfig.jsx — Drag-and-drop (T8)

```jsx
import { DndContext, closestCenter } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy, arrayMove } from '@dnd-kit/sortable';

const handleDragEnd = (event) => {
  const { active, over } = event;
  if (active.id !== over?.id) {
    setLocalColumns((cols) => {
      const oldIndex = cols.findIndex(c => c.id === active.id);
      const newIndex = cols.findIndex(c => c.id === over.id);
      return arrayMove(cols, oldIndex, newIndex);
    });
  }
};

const addColumn = () => {
  setLocalColumns(prev => [
    ...prev,
    { id: crypto.randomUUID(), name: '', type: 'FIRST_NAME', constraints: {} }
  ]);
};
```

Chaque colonne reçoit un `id` UUID généré à la création — **ne jamais utiliser l'index de tableau comme id drag-and-drop** (cause des bugs de réordonnancement).

```jsx
<DndContext collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
  <SortableContext items={localColumns.map(c => c.id)} strategy={verticalListSortingStrategy}>
    {localColumns.map((col) => (
      <ColumnRow
        key={col.id}
        column={col}
        onChange={(updated) => updateColumn(col.id, updated)}
        onRemove={() => removeColumn(col.id)}
        canRemove={localColumns.length > 1}
      />
    ))}
  </SortableContext>
</DndContext>
```

---

### ColumnRow.jsx — useSortable (T9)

```jsx
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

const ColumnRow = ({ column, onChange, onRemove, canRemove }) => {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({ id: column.id });
  const style = { transform: CSS.Transform.toString(transform), transition };

  return (
    <Box ref={setNodeRef} style={style} sx={{ display: 'flex', gap: 1, mb: 1, alignItems: 'center' }}>
      <Box {...attributes} {...listeners} sx={{ cursor: 'grab', color: 'text.secondary' }}>
        <DragIndicatorIcon />
      </Box>
      {/* TextField nom, Select type, DynamicConstraintsPanel, bouton poubelle */}
    </Box>
  );
};
```

---

### DynamicConstraintsPanel.jsx — Types existants (T10)

ATTENTION : les types `INTEGER`, `DECIMAL`, `TEXT`, `LOREM_IPSUM` ne sont **PAS** dans le `ColumnType` enum actuel (13 types : FIRST_NAME, LAST_NAME, EMAIL, PHONE, GENDER, ADDRESS, AMOUNT, CURRENCY, ACCOUNT_NUMBER, DATE, TIME, TIMEZONE, BIRTH_DATE). Adapter le switch :

```jsx
const renderConstraints = (column, onChange) => {
  switch (column.type) {
    case 'AMOUNT':
      return <MinMaxConstraint value={column.constraints} onChange={onChange} />;
    case 'DATE':
    case 'BIRTH_DATE':
      return <DateRangeConstraint value={column.constraints} onChange={onChange} />;
    default:
      return null;
  }
};
```

Le Select "type" doit lister exactement les 13 valeurs de l'enum backend :
`FIRST_NAME, LAST_NAME, EMAIL, PHONE, GENDER, ADDRESS, AMOUNT, CURRENCY, ACCOUNT_NUMBER, DATE, TIME, TIMEZONE, BIRTH_DATE`

---

### WizardStep3_Preview.jsx — Appel au montage (T11)

L'appel preview ne se déclenche qu'une seule fois à l'entrée de l'étape 3 (useEffect avec dépendances vides). Ne pas relancer automatiquement si l'utilisateur revient en étape 2 et revient en étape 3 — les données preview sont dans l'état de ManualWizardModal et réutilisées.

```jsx
import { previewDataset } from '../../services/domainService';

useEffect(() => {
  // Appel uniquement si previewRows vides (évite double-call)
  if (previewRows.length > 0) return;

  const fetchPreview = async () => {
    setLoading(true);
    setError(null);
    try {
      const rows = await previewDataset(columns, 5);
      onPreviewLoaded(rows);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de la prévisualisation');
    } finally {
      setLoading(false);
    }
  };
  fetchPreview();
}, []); // eslint-disable-line react-hooks/exhaustive-deps
```

---

### WizardStep4_Confirm.jsx — Payload génération (T12)

```jsx
import api from '../../services/api';

const handleGenerate = async () => {
  setLoading(true);
  setError(null);
  try {
    await api.post(`/api/domains/${domainId}/data-sets`, {
      dataSetName: datasetName,
      domainId: domainId,
      rowCount: rowCount,
      columns: columns.map(col => ({
        name: col.name,
        type: col.type,
        constraints: col.constraints || {}
      }))
    });
    onSuccess();
  } catch (err) {
    setError(err.response?.data?.message || 'Erreur lors de la génération');
  } finally {
    setLoading(false);
  }
};
```

---

### Pitfalls à éviter

1. **NE PAS importer BatchGenerationModal** dans ManualWizardModal — deux Dialog fullWidth en parallèle causent des z-index problèmes.
2. **NE PAS utiliser index comme key** pour les ColumnRow — le drag-and-drop nécessite des ids stables.
3. **NE PAS appeler preview dans useEffect sans guard** — double-call React StrictMode en dev.
4. **NE PAS casser les props existantes de DomainTable** — `onCreateDataset` est additive uniquement, les 4 props existantes (`onEdit`, `onDelete`, `onUpload`, `onViewDatasets`) restent inchangées.
5. **ColumnType enum côté backend** = 13 valeurs fixes — ne pas inventer `INTEGER`/`TEXT`/etc. pour le Select wizard. Seuls AMOUNT/DATE/BIRTH_DATE ont des contraintes.
6. **`handleReset()` avant `onSuccess()`** dans ManualWizardModal — sinon le wizard reste en étape 4 si on le rouvre.

---

### Structure fichiers à créer

```
movkfact-frontend/src/components/ManualWizard/
  ManualWizardModal.jsx
  CreateDatasetChoiceDialog.jsx
  WizardStep1_NameAndRows.jsx
  WizardStep1_NameAndRows.test.js
  WizardStep2_ColumnConfig.jsx
  WizardStep2_ColumnConfig.test.js
  ColumnRow.jsx
  DynamicConstraintsPanel.jsx
  WizardStep3_Preview.jsx
  WizardStep3_Preview.test.js
  WizardStep4_Confirm.jsx
  WizardStep4_Confirm.test.js
  index.js
```

---

### Pattern de test Jest (T13)

Suivre le pattern de `DeleteConfirmDialog.test.js` :

```js
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import WizardStep1_NameAndRows from './WizardStep1_NameAndRows';

test('bouton Suivant désactivé si nom invalide', () => {
  const onNext = jest.fn();
  render(<WizardStep1_NameAndRows datasetName="" rowCount={1000} onNext={onNext} />);
  expect(screen.getByText(/suivant/i)).toBeDisabled();
});

test('bouton Suivant actif si formulaire valide', () => {
  const onNext = jest.fn();
  render(<WizardStep1_NameAndRows datasetName="Mon Dataset" rowCount={500} onNext={onNext} />);
  expect(screen.getByText(/suivant/i)).not.toBeDisabled();
});
```

Pour WizardStep3, mocker `domainService.previewDataset` :

```js
jest.mock('../../services/domainService', () => ({
  previewDataset: jest.fn().mockResolvedValue([{ col1: 'val1' }])
}));
```

---

## References

- Spec : [epic-7-manual-wizard/7-2-frontend-wizard-4-steps.md](../epic-7-manual-wizard/7-2-frontend-wizard-4-steps.md)
- Backend preview endpoint : [7-1-backend-wizard-support.md](7-1-backend-wizard-support.md)
- DomainTable source : [movkfact-frontend/src/components/DomainTable.jsx](../../movkfact-frontend/src/components/DomainTable.jsx)
- DomainsPage source : [movkfact-frontend/src/pages/DomainsPage.jsx](../../movkfact-frontend/src/pages/DomainsPage.jsx)
- domainService.js source : [movkfact-frontend/src/services/domainService.js](../../movkfact-frontend/src/services/domainService.js)
- Existing modal pattern (CsvUploadPanel) : DomainsPage.jsx ligne 332–358
- ColumnType enum backend : src/main/java/com/movkfact/enums/ColumnType.java (13 valeurs)
- DeleteConfirmDialog.test.js : pattern de test référence

---

## Dev Agent Record

- **Model:** claude-sonnet-4-6
- **Debug Log:** N/A
- **Completion Notes:**
  - Tous les 13 tasks implémentés. 21 nouveaux tests Jest passent. 28/28 sur les suites ManualWizard + DomainTable.
  - **Fix `crypto.randomUUID()` jsdom** : remplacé par `String(Date.now() + Math.random())` dans WizardStep2_ColumnConfig.jsx et ManualWizardModal.jsx — `crypto.randomUUID()` n'est pas exposé dans Node.js test env (jsdom).
  - **npm `--legacy-peer-deps`** : nécessaire pour installer @dnd-kit avec CRA (peer deps conflict react-dom).
  - **6 failures pré-existantes** : BatchJobsContext.test.js + ConfigurationPanel.test.jsx — confirmées pré-existantes via `git stash`, ne sont pas des régressions S7.2. Total suite : 315/321 (6 failures non régressives).
  - DomainTable.jsx : `onCreateDataset` prop ajoutée sans casser les 4 props existantes (additive uniquement).
  - WizardStep3 : guard `if (previewRows.length > 0) return` dans useEffect pour éviter double-call React StrictMode.
  - WizardStep4 : payload `{ dataSetName, domainId, rowCount, columns: [{name, columnType, constraints}] }` envoyé via `api.post` direct.
- **Files created/modified:**
  - `movkfact-frontend/package.json` (modified — @dnd-kit/core, @dnd-kit/sortable, @dnd-kit/utilities ajoutés)
  - `movkfact-frontend/src/services/domainService.js` (modified — previewDataset added)
  - `movkfact-frontend/src/components/DomainTable.jsx` (modified — onCreateDataset prop + AddCircleOutlineIcon button desktop+mobile)
  - `movkfact-frontend/src/pages/DomainsPage.jsx` (modified — wizard states + handlers + CreateDatasetChoiceDialog + ManualWizardModal)
  - `movkfact-frontend/src/components/ManualWizard/CreateDatasetChoiceDialog.jsx` (new)
  - `movkfact-frontend/src/components/ManualWizard/ManualWizardModal.jsx` (new)
  - `movkfact-frontend/src/components/ManualWizard/WizardStep1_NameAndRows.jsx` (new)
  - `movkfact-frontend/src/components/ManualWizard/WizardStep1_NameAndRows.test.js` (new — 6 tests)
  - `movkfact-frontend/src/components/ManualWizard/WizardStep2_ColumnConfig.jsx` (new)
  - `movkfact-frontend/src/components/ManualWizard/WizardStep2_ColumnConfig.test.js` (new — 6 tests)
  - `movkfact-frontend/src/components/ManualWizard/ColumnRow.jsx` (new)
  - `movkfact-frontend/src/components/ManualWizard/DynamicConstraintsPanel.jsx` (new)
  - `movkfact-frontend/src/components/ManualWizard/WizardStep3_Preview.jsx` (new)
  - `movkfact-frontend/src/components/ManualWizard/WizardStep3_Preview.test.js` (new — 5 tests)
  - `movkfact-frontend/src/components/ManualWizard/WizardStep4_Confirm.jsx` (new)
  - `movkfact-frontend/src/components/ManualWizard/WizardStep4_Confirm.test.js` (new — 4 tests)
  - `movkfact-frontend/src/components/ManualWizard/index.js` (new)
