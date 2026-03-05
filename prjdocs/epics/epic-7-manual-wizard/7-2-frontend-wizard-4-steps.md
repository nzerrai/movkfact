# Story S7.2: Frontend Wizard de création manuelle 4 étapes

**Sprint:** À planifier (Epic 7)
**Points:** 8
**Epic:** EPIC 7 - Wizard de création manuelle
**Type:** Frontend Feature
**Lead:** Sally
**Status:** Backlog
**Dependencies:** S7.1 (Preview endpoint + contraintes), S1.5 (Domain Management UI)
**FRs couverts:** FR-004 (chemin manual depuis Domain Management), FR-006, FR-010

---

## User Story

**En tant que** ingénieur QA,
**Je veux** créer un dataset from scratch via un wizard guidé en 4 étapes, avec configuration de chaque colonne, ses contraintes et un aperçu avant génération,
**Afin de** produire des données de test précises sans avoir besoin d'un fichier CSV source.

---

## Contexte métier

L'écran Domain Management propose actuellement uniquement le flux CSV (S2.5).
Le bouton "Créer un dataset" doit offrir un choix : "Upload CSV" (existant) ou "Création manuelle" (nouveau wizard).
Le wizard guide en 4 étapes linéaires avec retour arrière possible jusqu'à l'étape 2.

---

## ✅ Acceptance Criteria

### AC1 — Point d'entrée depuis Domain Management (FR-010)
- [ ] L'écran Domain Management dispose d'un bouton "Créer un dataset" par domaine
- [ ] Click → Dialog de choix : "Upload CSV" (existant) ou "Création manuelle" (nouveau)
- [ ] Choix "Création manuelle" → ouvre le `ManualWizardModal` avec `domainId` pré-rempli

### AC2 — Étape 1 : Nom et nombre de lignes
- [ ] `TextField` pour le nom du dataset (validation : 3–50 chars, `[a-zA-Z0-9 _-]`)
- [ ] Affichage unicité : vérification live (`GET /api/domains/{id}/datasets` — check doublons)
- [ ] `TextField` numérique pour le nombre de lignes (1–100 000)
- [ ] Slider MUI optionnel avec valeurs prédéfinies (100, 1 000, 10 000, 100 000)
- [ ] Bouton "Suivant →" désactivé tant que le formulaire est invalide

### AC3 — Étape 2 : Configuration des colonnes
- [ ] Bouton "+ Ajouter une colonne" → ajoute une ligne de configuration
- [ ] Chaque ligne : `TextField` nom + `Select` type (dropdown de tous les `ColumnType`) + champs contraintes dynamiques
- [ ] Contraintes dynamiques par type (FR-008) :
  - `INTEGER` / `DECIMAL` : champs "Min" et "Max" (TextField numérique)
  - `DATE` / temporels : date pickers "De" et "Jusqu'à" (MUI DatePicker)
  - `TEXT` / `LOREM_IPSUM` : champ "Longueur max" (TextField numérique)
  - `FIRST_NAME`, `EMAIL`, `BOOLEAN`, `GENDER`, etc. : aucun champ supplémentaire
- [ ] Drag-and-drop pour réordonner les colonnes (via `@dnd-kit/sortable` ou similaire)
- [ ] Bouton poubelle pour supprimer une colonne (minimum 1 colonne requise)
- [ ] Bouton "← Retour" (revient étape 1 avec état conservé)
- [ ] Bouton "Prévisualiser →" → appelle `POST /api/datasets/preview` et passe en étape 3

### AC4 — Étape 3 : Prévisualisation 5 lignes
- [ ] Affiche un tableau avec les 5 lignes retournées par l'endpoint preview
- [ ] Indicateur de chargement (spinner) pendant l'appel API
- [ ] Message d'erreur si l'appel preview échoue (avec option de retour)
- [ ] Bouton "← Modifier les colonnes" → retour étape 2 avec état conservé
- [ ] Bouton "Confirmer et générer →" → passe en étape 4

### AC5 — Étape 4 : Confirmation et génération
- [ ] Récapitulatif : nom du dataset, domaine, nombre de lignes, liste des colonnes avec types
- [ ] Bouton "Lancer la génération" → appelle `POST /api/domains/{domainId}/data-sets` (endpoint existant)
- [ ] Indicateur de progression (si batch) ou spinner simple
- [ ] En succès : ferme le wizard, actualise la liste des datasets du domaine, toast "Dataset '{name}' créé avec {N} lignes"
- [ ] En erreur : message d'erreur + option de relancer

### AC6 — Tests
- [ ] Tests Jest pour chaque étape (`WizardStep1.test.js`, etc.) — rendu + validation
- [ ] Test d'intégration : flux complet étape 1 → 4 avec mocks API
- [ ] Test retour arrière : vérifier que l'état est conservé entre les étapes
- [ ] Test drag-and-drop : réordonnancement des colonnes
- [ ] Coverage >80% sur les nouveaux composants

---

## 🏗️ Spécifications Techniques

### Hiérarchie des composants

```
DomainManagementPage (existant)
└── CreateDatasetChoiceDialog (nouveau — choix CSV vs Manual)
    └── ManualWizardModal (nouveau — Dialog MUI fullWidth)
        ├── WizardStepper (MUI Stepper — 4 étapes)
        ├── WizardStep1_NameAndRows.jsx
        ├── WizardStep2_ColumnConfig.jsx
        │   ├── ColumnRow.jsx × N (draggable)
        │   │   ├── ColumnNameField
        │   │   ├── ColumnTypeSelect
        │   │   └── DynamicConstraintsPanel
        │   │       ├── MinMaxConstraint (INTEGER/DECIMAL)
        │   │       ├── DateRangeConstraint (DATE/temporels)
        │   │       └── MaxLengthConstraint (TEXT/LOREM_IPSUM)
        ├── WizardStep3_Preview.jsx
        │   └── PreviewTable (5 lignes)
        └── WizardStep4_Confirm.jsx
            └── ColumnSummaryList
```

### Nouveaux fichiers à créer

```
movkfact-frontend/src/components/ManualWizard/
  ManualWizardModal.jsx
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

### Modifications requises

```
movkfact-frontend/src/pages/DomainsPage.jsx (ou DomainTable.jsx)
  + Bouton "Créer un dataset" par ligne de domaine
  + Ouvrir CreateDatasetChoiceDialog avec domainId

movkfact-frontend/src/services/api.js (ou domainService.js)
  + previewDataset(columns, count)   → POST /api/datasets/preview
  + createDataset(domainId, payload) → POST /api/domains/{id}/data-sets (existant)
```

### État global du wizard (dans ManualWizardModal)

```javascript
const [step, setStep] = useState(0);          // 0-3
const [datasetName, setDatasetName] = useState('');
const [rowCount, setRowCount] = useState(1000);
const [columns, setColumns] = useState([]);    // [{ id, name, type, constraints }]
const [previewRows, setPreviewRows] = useState([]);
const [loading, setLoading] = useState(false);
const [error, setError] = useState(null);
```

### DynamicConstraintsPanel — Rendu conditionnel

```javascript
const renderConstraints = (column, onChange) => {
  switch (column.type) {
    case 'INTEGER':
    case 'DECIMAL':
      return <MinMaxConstraint value={column.constraints} onChange={onChange} />;
    case 'DATE':
    case 'BIRTH_DATE':
      return <DateRangeConstraint value={column.constraints} onChange={onChange} />;
    case 'TEXT':
    case 'LOREM_IPSUM':
      return <MaxLengthConstraint value={column.constraints} onChange={onChange} />;
    default:
      return null; // FIRST_NAME, EMAIL, BOOLEAN, etc.
  }
};
```

### Dépendance drag-and-drop

```bash
npm install @dnd-kit/core @dnd-kit/sortable @dnd-kit/utilities
```
*Alternative légère si @dnd-kit déjà absent : utiliser des boutons ↑/↓ (plus simple, moins UX)*

---

## 📝 Dev Notes

- Le `ManualWizardModal` est le seul gestionnaire d'état — les 4 steps reçoivent props + callbacks
- La génération finale (étape 4) réutilise l'API existante `POST /api/domains/{id}/data-sets` — aucun nouvel endpoint backend côté génération
- Le `domainId` est passé au wizard depuis la page Domain Management
- Utiliser `MUI Stepper` non-linéaire (bloqué sur étape suivante tant que non valide)
- L'étape 3 déclenche l'appel preview uniquement au passage de l'étape 2 → 3 (pas de refresh auto)
- Pour le drag-and-drop des colonnes : assigner un `id` UUID côté client à chaque colonne au moment de sa création

---

## 📊 Estimation

| Composant | Effort | Responsable |
|-----------|--------|-------------|
| `ManualWizardModal` + `WizardStepper` | 0.5j | Sally |
| `WizardStep1_NameAndRows` + validation | 0.5j | Sally |
| `WizardStep2_ColumnConfig` + `ColumnRow` + drag-and-drop | 2j | Sally |
| `DynamicConstraintsPanel` (3 variants) | 0.75j | Sally |
| `WizardStep3_Preview` (appel preview + tableau) | 0.5j | Sally |
| `WizardStep4_Confirm` + génération + toast | 0.5j | Sally |
| `CreateDatasetChoiceDialog` + intégration DomainsPage | 0.25j | Sally |
| Tests Jest (flux complet + états) | 1.5j | Sally |
| **Total** | **6.5j** | **8 pts** |
