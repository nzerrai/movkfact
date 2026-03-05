# Story S6.2: Frontend Éditeur de données inline

**Sprint:** À planifier (Epic 6)
**Points:** 6
**Epic:** EPIC 6 - Data Editor (Éditeur de données inline)
**Type:** Frontend Feature
**Lead:** Sally
**Status:** Backlog
**Dependencies:** S6.1 (Row Editor API), S2.7 (DataViewer existant)
**FRs couverts:** FR-011, FR-012, FR-016, FR-019

---

## User Story

**En tant que** développeur ou Tech Lead,
**Je veux** consulter, modifier et supprimer des lignes directement dans l'interface sans outil externe,
**Afin de** corriger des données incorrectes et auditer les modifications depuis un onglet dédié "Éditeur de données".

---

## Contexte métier

Le `DataViewer` existant (S2.7) est en lecture seule (tri, filtrage, export).
Cette story ajoute un onglet "Éditeur de données" distinct dans la vue dataset, avec des cellules éditables in-place, une suppression de ligne confirmée et un journal d'activité visible.

---

## ✅ Acceptance Criteria

### AC1 — Onglet "Éditeur de données" dédié
- [ ] La vue dataset expose 2 onglets : "Visualiseur" (S2.7 existant) et "Éditeur de données" (nouveau)
- [ ] Navigation entre onglets sans perte d'état (pagination et filtres conservés)
- [ ] L'onglet "Éditeur de données" affiche le même tableau paginé que le visualiseur
- [ ] Badge sur l'onglet indique le nombre de modifications depuis l'ouverture de la session

### AC2 — Consultation des lignes (FR-011)
- [ ] Tableau paginé (50 lignes/page par défaut) depuis `GET /api/data-sets/{id}/rows`
- [ ] Colonnes avec en-têtes fixes, tri par colonne
- [ ] `rowIndex` affiché comme première colonne non-éditable (lecture seule, grisée)
- [ ] Chargement paginé côté serveur (pas de chargement du dataset complet en mémoire)

### AC3 — Garde dataset large (cohérence S6.1 AC7) *(ajouté party mode 04/03)*
- [ ] Si `dataset.rowCount > 50 000` : banner d'avertissement affiché en haut de l'onglet : *"Ce dataset dépasse 50 000 lignes. L'édition inline est désactivée. Utilisez le reset ou régénérez."*
- [ ] Boutons Modifier et Supprimer grisés (`disabled`) — lecture seule uniquement
- [ ] La pagination et le tri restent fonctionnels en mode lecture

### AC4 — Modification inline de cellules (FR-012)
- [ ] Double-clic sur une cellule → passe en mode édition (TextField MUI inline)
- [ ] Touche Entrée ou clic hors cellule → confirme la modification (appel `PUT /api/data-sets/{id}/rows/{rowIndex}`)
- [ ] Touche Échap → annule sans appel API
- [ ] Indicateur visuel : cellule modifiée mais non sauvegardée (bordure orange)
- [ ] Toast de confirmation "Ligne X modifiée avec succès" après sauvegarde
- [ ] Toast d'erreur si l'API renvoie une erreur (avec message)
- [ ] Bouton "Modifier la ligne" en fin de ligne (alternative au double-clic) — ouvre un mini-formulaire en ligne

### AC4 — Suppression de ligne (FR-016)
- [ ] Bouton icône poubelle (MUI IconButton) sur chaque ligne
- [ ] Dialog de confirmation : "Supprimer la ligne X ? Cette action est irréversible dans l'éditeur. Le reset complet du dataset reste disponible."
- [ ] Après confirmation : appel `DELETE /api/data-sets/{id}/rows/{rowIndex}`, ligne disparaît du tableau
- [ ] `rowCount` mis à jour dans le header de la vue (ex: "4 999 lignes")
- [ ] Toast "Ligne X supprimée"

### AC5 — Journal d'activité visible (FR-019)
- [ ] Panneau latéral rétractable "Historique des modifications" dans l'onglet Éditeur
- [ ] Chaque entrée affiche : horodatage, action (MODIFIED/DELETED), rowIndex, colonnes concernées
- [ ] Filtre par type d'action (Modifications / Suppressions / Tout)
- [ ] Bouton "Actualiser" pour recharger depuis `GET /api/data-sets/{id}/activity`
- [ ] Maximum 100 entrées affichées (les plus récentes en tête)

### AC6 — Tests
- [ ] Tests Jest pour `DataEditorTab.jsx` : rendu, double-clic édition, annulation, soumission
- [ ] Tests Jest pour `DeleteRowDialog.jsx` : affichage, confirmation, annulation
- [ ] Tests Jest pour `ActivityPanel.jsx` : rendu liste, filtre par type
- [ ] Coverage >80% sur les nouveaux composants
- [ ] Test d'accessibilité : navigation clavier dans la table (Tab, Entrée, Échap)

---

## 🏗️ Spécifications Techniques

### Hiérarchie des composants

```
DatasetView (page existante — ajouter onglets)
├── Tab "Visualiseur" → DataViewerContainer (existant, inchangé)
└── Tab "Éditeur de données" → DataEditorTab (nouveau)
    ├── DataEditorTable
    │   ├── TableHead (headers + sort)
    │   └── TableBody
    │       └── EditableRow × N
    │           ├── EditableCell (double-clic → TextField)
    │           ├── DeleteRowButton → DeleteRowDialog
    │           └── RowIndexCell (lecture seule)
    ├── TablePagination (server-side)
    └── ActivityPanel (panneau latéral rétractable)
        ├── ActivityList
        └── ActivityFilter
```

### Nouveaux fichiers à créer

```
movkfact-frontend/src/components/DataEditor/
  DataEditorTab.jsx
  DataEditorTab.test.js
  DataEditorTable.jsx
  EditableRow.jsx
  EditableCell.jsx
  DeleteRowDialog.jsx
  DeleteRowDialog.test.js
  ActivityPanel.jsx
  ActivityPanel.test.js
  index.js
```

### Modifications requises

```
movkfact-frontend/src/pages/DataViewerPage.jsx  (ou équivalent)
  + Ajouter <Tabs> MUI avec "Visualiseur" et "Éditeur de données"
  + Monter <DataEditorTab datasetId={id} /> pour le 2e onglet

movkfact-frontend/src/services/dataSetService.js
  + getRows(datasetId, page, size)     → GET /api/data-sets/{id}/rows
  + getRow(datasetId, rowIndex)        → GET /api/data-sets/{id}/rows/{rowIndex}
  + updateRow(datasetId, rowIndex, columns) → PUT /api/data-sets/{id}/rows/{rowIndex}
  + deleteRow(datasetId, rowIndex)     → DELETE /api/data-sets/{id}/rows/{rowIndex}
  + getActivity(datasetId)             → GET /api/data-sets/{id}/activity
```

### État de DataEditorTab

```javascript
const [rows, setRows] = useState([]);          // lignes courantes (page)
const [totalRows, setTotalRows] = useState(0);
const [page, setPage] = useState(0);
const [editingCell, setEditingCell] = useState(null); // { rowIndex, colName }
const [pendingValue, setPendingValue] = useState('');
const [activityOpen, setActivityOpen] = useState(false);
const [modifiedCount, setModifiedCount] = useState(0);
```

### Flux modification inline

```
Double-clic cellule (rowIndex=5, col="firstName")
  → setEditingCell({ rowIndex: 5, colName: "firstName" })
  → TextField affiché avec valeur courante

Entrée pressée :
  → appel updateRow(datasetId, 5, { "firstName": "newValue" })
  → onSuccess : mettre à jour rows[5].firstName localement
  → setEditingCell(null)
  → toast "Ligne 5 modifiée"
  → setModifiedCount(prev => prev + 1)

Échap pressée :
  → setEditingCell(null)  // aucun appel API
```

---

## 📝 Dev Notes

- Utiliser `MUI DataGrid` n'est pas requis — `MUI Table` avec gestion manuelle du mode édition est suffisant et cohérent avec le reste du code (S2.6/S2.7)
- La pagination est **server-side** (contrairement à S2.7 qui chargeait tout) car les datasets peuvent atteindre 100k lignes
- `rowIndex` dans l'API est 0-based et stable même après suppression d'autres lignes (réindexé côté serveur après chaque DELETE)
- Le panneau "Historique" utilise le même endpoint `GET /api/data-sets/{id}/activity` que S3.1, filtré sur les types `ROW_MODIFIED` et `ROW_DELETED`
- Ne pas remplacer le visualiseur existant (S2.7) — cohabitation par onglets

---

## 📊 Estimation

| Composant | Effort | Responsable |
|-----------|--------|-------------|
| `DataEditorTable` + `EditableRow` + `EditableCell` | 1.5j | Sally |
| `DeleteRowDialog` | 0.25j | Sally |
| `ActivityPanel` | 0.5j | Sally |
| Intégration onglets dans `DatasetView` | 0.25j | Sally |
| Extension `dataSetService.js` | 0.25j | Sally |
| Tests Jest (>80% coverage) | 1j | Sally |
| **Total** | **3.75j** | **6 pts** |
