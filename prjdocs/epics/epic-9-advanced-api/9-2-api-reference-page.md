# Story S9.2 : Page Référence API — Onglets CRUD, Extraction, Guide

**Sprint:** À planifier (Epic 9)
**Points:** 8
**Epic:** EPIC 9 - API d'accès avancé
**Type:** Frontend Feature
**Lead:** Dev Frontend
**Status:** review
**Dependencies:** S6.1 (DataRowEditorController), S9.1 (filtrage avancé), S2.3 (export endpoint)
**FRs couverts:** FR-014, FR-015

---

## User Story

**En tant que** développeur ou intégrateur utilisant la plateforme Movkfact,
**Je veux** consulter une page de référence API structurée en onglets (CRUD, Extraction, Guide),
**Afin de** comprendre rapidement les endpoints disponibles, les tester en live avec de vraies données, et copier des snippets d'intégration pour mon projet.

---

## Contexte métier

La plateforme expose une API REST complète (gestion domaines, datasets, génération, export) mais aucune interface ne la documente. Les intégrateurs doivent deviner les URLs ou consulter le Swagger technique. Cette story crée une page dédiée, accessible depuis le menu latéral, offrant trois onglets complémentaires :

- **CRUD** : référence des endpoints de gestion (domaines, datasets)
- **Extraction** : endpoint d'export avec configuration interactive et téléchargement
- **Guide** : snippets de code prêts à copier pour intégration (curl, JS, Python)

Le bouton "Essayer" de chaque endpoint est pré-rempli avec des identifiants existants (domaines et datasets réels issus du contexte courant), et affiche la réponse JSON réelle en direct.

---

## ✅ Acceptance Criteria

### AC1 — Onglet CRUD : référence des endpoints

- [ ] La page `/api-reference` est accessible depuis le menu latéral (icône `ApiIcon` ou `CodeIcon`)
- [ ] L'onglet "CRUD" liste les endpoints suivants :

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/domains` | Liste tous les domaines |
| POST | `/api/domains` | Crée un domaine |
| DELETE | `/api/domains/{id}` | Supprime un domaine |
| GET | `/api/data-sets` | Liste tous les datasets |
| GET | `/api/data-sets/{id}` | Détail d'un dataset |
| POST | `/api/data-sets/generate` | Génère un dataset (inline) |
| DELETE | `/api/data-sets/{id}` | Supprime un dataset |
| GET | `/api/data-sets/{id}/rows` | Accès paginé aux lignes |

- [ ] Chaque endpoint affiche : méthode (badge coloré), URL, description courte, paramètres (path/query/body résumés)
- [ ] Un bouton **"Essayer"** est présent pour chaque endpoint GET et POST
- [ ] Cliquer sur "Essayer" ouvre un panneau inline qui :
  - pré-remplit les paramètres `{id}` avec un vrai domaine/dataset existant (chargé depuis `GET /api/domains` ou `GET /api/data-sets` au montage de la page)
  - affiche un champ URL éditable
  - dispose d'un bouton **"Exécuter"** déclenchant l'appel REST réel
  - affiche la réponse JSON dans un bloc `<pre>` formaté (avec code HTTP + durée en ms)
- [ ] Si aucun domaine/dataset n'existe, le champ est vide et un message guide l'utilisateur à en créer un

### AC2 — Onglet Extraction : export interactif

- [ ] L'onglet "Extraction" présente un formulaire d'export interactif avec les champs suivants :
  - **Dataset** : sélecteur chargé depuis `GET /api/data-sets` (affiche `nom (id)`)
  - **Mode** : radio buttons `full` | `filtered` | `sample`
    - `filtered` : active un champ `rowIds` (liste d'entiers séparés par virgule, ex: `0,5,10`)
    - `sample` : active un champ `count` (entier, ex: `50`)
  - **Format** : sélecteur `JSON` | `CSV`
- [ ] Un bouton **"Télécharger"** déclenche l'appel `GET /api/data-sets/{id}/export?mode=...&format=...` et télécharge le fichier
- [ ] Un bouton **"Prévisualiser (50 lignes)"** affiche les 50 premières lignes dans un tableau MUI sous le formulaire
- [ ] L'URL construite est affichée en temps réel au-dessus des boutons (ex: `GET /api/data-sets/3/export?mode=sample&count=50&format=CSV`)
- [ ] Les états loading/erreur sont gérés avec spinner et message d'erreur visible

### AC3 — Onglet Guide : snippets d'intégration

- [ ] L'onglet "Guide" propose plusieurs sections d'exemples, chacune avec :
  - Un titre de cas d'usage (ex: "Lister tous les domaines", "Générer un dataset", "Extraire 100 lignes en CSV")
  - Trois sous-onglets : `curl` | `JavaScript (fetch)` | `Python (requests)`
  - Un bloc de code `<pre>` avec coloration syntaxique minimale (fond sombre, texte vert/blanc)
  - Un bouton **"Copier"** (`ContentCopyIcon`) qui copie le snippet dans le presse-papiers avec feedback "Copié !"
- [ ] Les snippets sont statiques mais utilisent la variable `BASE_URL` (configurable via un champ en haut de l'onglet, valeur par défaut : `http://localhost:8080`)
- [ ] Cas d'usage couverts :
  1. Lister tous les domaines
  2. Créer un domaine
  3. Générer un dataset (corps JSON minimal)
  4. Extraire toutes les lignes en JSON
  5. Extraire un échantillon de 100 lignes en CSV
  6. Récupérer des lignes spécifiques par index (`rowIds`)

### AC4 — Navigation et UX

- [ ] La page est ajoutée à la `Sidebar` avec libellé "API Reference" et icône appropriée
- [ ] Le routing React est configuré sur `/api-reference`
- [ ] Le composant `ApiReferencePage` est autonome (aucune modification des pages existantes)
- [ ] L'onglet actif est conservé dans l'URL (ex: `/api-reference?tab=extraction`) ou dans l'état local du composant
- [ ] La page est responsive : sur mobile, les panneaux "Essayer" s'affichent en dessous de l'endpoint

---

## 🏗️ Spécifications Techniques

### Nouveaux fichiers

```
movkfact-frontend/src/pages/ApiReferencePage.jsx
  → Composant principal avec MUI Tabs (CRUD / Extraction / Guide)
  → Charge les domaines et datasets au montage (useEffect)

movkfact-frontend/src/components/ApiReference/CrudTab.jsx
  → Liste des endpoints avec EndpointCard
  → Gère l'état ouvert/fermé du panneau "Essayer"

movkfact-frontend/src/components/ApiReference/EndpointCard.jsx
  → Affiche méthode + URL + description + bouton Essayer
  → Panneau inline : champ URL éditable + bouton Exécuter + réponse JSON

movkfact-frontend/src/components/ApiReference/ExtractionTab.jsx
  → Formulaire dataset/mode/format
  → Boutons Télécharger + Prévisualiser

movkfact-frontend/src/components/ApiReference/GuideTab.jsx
  → Sections par cas d'usage
  → Sous-onglets curl/JS/Python avec bouton Copier
```

### Modifications requises

```
movkfact-frontend/src/App.jsx
  + Route path="/api-reference" element={<ApiReferencePage />}

movkfact-frontend/src/layout/Sidebar.jsx
  + Entrée "API Reference" avec lien vers /api-reference
```

### Structure du composant EndpointCard (logique "Essayer")

```jsx
// État local
const [open, setOpen] = useState(false);
const [url, setUrl] = useState(buildDefaultUrl(endpoint, defaultIds));
const [response, setResponse] = useState(null);
const [loading, setLoading] = useState(false);
const [elapsed, setElapsed] = useState(null);

const handleExecute = async () => {
  setLoading(true);
  const t0 = performance.now();
  try {
    const res = await fetch(url);
    const json = await res.json();
    setResponse({ status: res.status, body: JSON.stringify(json, null, 2) });
  } catch (e) {
    setResponse({ status: 'ERR', body: e.message });
  } finally {
    setElapsed(Math.round(performance.now() - t0));
    setLoading(false);
  }
};
```

### Chargement des IDs par défaut

```jsx
// Dans ApiReferencePage — au montage
useEffect(() => {
  Promise.all([
    fetch('/api/domains').then(r => r.json()),
    fetch('/api/data-sets').then(r => r.json()),
  ]).then(([domains, datasets]) => {
    setDefaultDomainId(domains[0]?.id ?? null);
    setDefaultDatasetId(datasets[0]?.id ?? null);
  });
}, []);
```

### Format export (AC2)

```
GET /api/data-sets/{id}/export
  ?format=JSON|CSV
  &mode=full|filtered|sample
  &rowIds=0,5,10          (si mode=filtered)
  &count=50               (si mode=sample)
```

*Note : si l'endpoint `/export` n'expose pas encore ces paramètres de mode, le backend devra être étendu en parallèle (hors scope de cette story — créer ticket technique dédié).*

---

## 📝 Dev Notes

### Conventions du projet (à respecter impérativement)

- **HTTP client** : utiliser `axios` via `src/services/api.js` (instance configurée avec `baseURL` et intercepteur 401). Les snippets Guide restent des templates `fetch`/`curl`/`requests` purement statiques — pas d'appel réel dans le Guide.
- **Routing** : React Router v6, routes imbriquées sous `<Layout />` dans `App.jsx`. Pattern : `<Route path="/api-reference" element={<ApiReferencePage />} />`
- **Sidebar** : tableau `menuItems` dans `Sidebar.jsx` — ajouter un objet `{ label: 'API Reference', icon: <ApiIcon />, path: '/api-reference' }`
- **Proxy Vite** : le proxy dev est déjà configuré (`/api` → `http://localhost:8080`). Les appels axios sans `baseURL` explicite passent automatiquement par le proxy.
- **Tests** : Jest + React Testing Library (pattern des fichiers existants `*.test.jsx` ou `*.test.js`)
- **Pas de nouvelle dépendance** : coloration syntaxique en CSS pur (`background: #1e1e1e; color: #d4d4d4`), pas de highlight.js ni Prism.
- Le bouton "Copier" utilise `navigator.clipboard.writeText()` — pas de fallback nécessaire (app moderne uniquement).
- Le panneau "Essayer" des `EndpointCard` utilise `axios` directement (pas l'instance `api.js`) pour afficher le vrai code HTTP retourné.

### Structure des données contextuelles

```javascript
// ApiReferencePage charge au montage et passe en props aux onglets
{
  defaultDomainId: number | null,   // premier domaine existant
  defaultDatasetId: number | null,  // premier dataset existant
  domains: [],                      // liste complète pour ExtractionTab
  datasets: [],                     // liste complète pour ExtractionTab
}
```

### Format réponse "Essayer"

```javascript
{ status: 200, statusText: 'OK', durationMs: 42, body: '[ ... JSON formaté ... ]' }
```

---

## 📊 Estimation

| Composant | Effort | Responsable |
|-----------|--------|-------------|
| `ApiReferencePage.jsx` + routing + sidebar | 0.5j | Dev Frontend |
| `CrudTab.jsx` + `EndpointCard.jsx` (liste + "Essayer") | 1.5j | Dev Frontend |
| `ExtractionTab.jsx` (formulaire + prévisualisation) | 1j | Dev Frontend |
| `GuideTab.jsx` (snippets + copier) | 0.75j | Dev Frontend |
| Tests unitaires (EndpointCard, ExtractionTab, GuideTab) | 1.25j | Dev Frontend |
| **Total** | **5j** | **8 pts** |

---

---

## ✅ Tasks / Subtasks

### Task 1 — Navigation et routing (AC4)

- [x] T1.1 : Ajouter la route `/api-reference` dans `App.jsx` (import `ApiReferencePage`)
- [x] T1.2 : Ajouter l'entrée "API Reference" dans le tableau `menuItems` de `Sidebar.jsx` avec `ApiIcon` (ou `IntegrationInstructionsIcon`)
- [x] T1.3 : Créer `ApiReferencePage.jsx` squelette avec MUI `Tabs` (3 onglets : CRUD / Extraction / Guide), état local `activeTab`
- [x] T1.4 : Tests : `ApiReferencePage.test.jsx` — rendu des 3 onglets, navigation entre onglets

### Task 2 — Chargement contextuel + EndpointCard (AC1)

- [x] T2.1 : Charger `GET /api/domains` et `GET /api/data-sets` au montage dans `ApiReferencePage` (axios), stocker `defaultDomainId`, `defaultDatasetId`, `domains[]`, `datasets[]`
- [x] T2.2 : Créer `EndpointCard.jsx` — affiche méthode (badge coloré MUI Chip), URL, description, paramètres résumés
- [x] T2.3 : Bouton "Essayer" dans `EndpointCard` — toggle panneau inline (rendu conditionnel)
- [x] T2.4 : Panneau inline : champ URL éditable (`TextField`), bouton "Exécuter" → appel axios → affiche réponse JSON dans `<pre>` (status HTTP + durée ms)
- [x] T2.5 : Pré-remplissage automatique `{id}` avec `defaultDomainId` ou `defaultDatasetId` selon le type d'endpoint
- [x] T2.6 : Message si aucun domaine/dataset ("Aucune donnée — créez d'abord un domaine")
- [x] T2.7 : Tests : `EndpointCard.test.jsx` — toggle panneau, pré-remplissage, rendu réponse mock

### Task 3 — CrudTab (AC1)

- [x] T3.1 : Créer `CrudTab.jsx` — liste les 8 endpoints (tableau défini en constante dans le fichier)
- [x] T3.2 : Passer `defaultDomainId` / `defaultDatasetId` en props à `CrudTab` → transmis à chaque `EndpointCard`
- [x] T3.3 : Tests : `CrudTab.test.jsx` — rendu des 8 cards, props transmises

### Task 4 — ExtractionTab (AC2)

- [x] T4.1 : Créer `ExtractionTab.jsx` — sélecteur dataset (MUI `Select` chargé depuis `datasets[]`), radio `full|filtered|sample`, sélecteur format `JSON|CSV`
- [x] T4.2 : Affichage conditionnel : champ `rowIds` si mode=`filtered`, champ `count` si mode=`sample`
- [x] T4.3 : URL construite en temps réel affichée dans un `<code>` (`GET /api/data-sets/{id}/export?...`)
- [x] T4.4 : Bouton "Télécharger" → appel axios `responseType: 'blob'` → déclenchement téléchargement navigateur (`URL.createObjectURL`)
- [x] T4.5 : Bouton "Prévisualiser (50 lignes)" → appel API → tableau MUI (`Table`) limité à 50 lignes
- [x] T4.6 : États loading (spinner CircularProgress) + erreur (Alert MUI)
- [x] T4.7 : Tests : `ExtractionTab.test.jsx` — URL construite correctement par mode, rendu conditionnel des champs

### Task 5 — GuideTab (AC3)

- [x] T5.1 : Créer `GuideTab.jsx` — champ `BASE_URL` en haut (valeur par défaut `http://localhost:8080`)
- [x] T5.2 : Créer composant `SnippetBlock.jsx` — sous-onglets `curl | JavaScript | Python`, bloc `<pre>` style VS Code dark, bouton "Copier" (`ContentCopyIcon`) avec feedback "Copié !" (1,5s)
- [x] T5.3 : Implémenter les 6 cas d'usage (templates string statiques paramétrés par `BASE_URL`)
- [x] T5.4 : Tests : `GuideTab.test.jsx` — rendu 6 sections, changement `BASE_URL` met à jour les snippets, click Copier appelle `navigator.clipboard.writeText`

### Task 6 — Tests de régression et validation AC

- [x] T6.1 : Suite complète — 41 nouveaux tests passent. 7 failures pré-existantes (BatchJobsContext, WizardStep2, ConfigurationPanel) confirmées non régressées par S9.2.
- [x] T6.2 : 41 tests sur 6 nouveaux composants (ApiReferencePage, CrudTab, EndpointCard, ExtractionTab, GuideTab, SnippetBlock)
- [x] T6.3 : Validation manuelle à effectuer par Nouredine après démarrage du frontend

---

## 🎨 Wireframe (référence)

```
┌─────────────────────────────────────────────────────────┐
│  API Reference                                          │
├──────────┬───────────────┬──────────────────────────────┤
│   CRUD   │  Extraction   │  Guide                       │
├──────────┴───────────────┴──────────────────────────────┤
│                                                         │
│  ┌─ GET ──────────────────────────────────────────────┐ │
│  │  /api/domains           Liste tous les domaines    │ │
│  │  Params: aucun                        [Essayer ▼]  │ │
│  │  ┌──────────────────────────────────────────────┐  │ │
│  │  │ URL: http://localhost:8080/api/domains        │  │ │
│  │  │                              [Exécuter]       │  │ │
│  │  │ 200 OK · 42ms                                 │  │ │
│  │  │ [{ "id": 1, "name": "Clients", ... }]         │  │ │
│  │  └──────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────┘ │
│                                                         │
│  ┌─ POST ─────────────────────────────────────────────┐ │
│  │  /api/domains           Crée un domaine    [Essayer]│ │
│  └────────────────────────────────────────────────────┘ │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 📋 Dev Agent Record

### Implementation Plan

T1→T2→T3→T4→T5→T6 en séquence. Rendu conditionnel remplace MUI Collapse (JSDOM incompatible). `labelId` + `inputProps aria-label` ajouté au Select MUI pour l'accessibilité test. `aria-label` ajouté au bouton Copier de SnippetBlock.

### Debug Log

| Task | Issue | Resolution |
|------|-------|------------|
| T2.3 | MUI Collapse.unmountOnExit ne démonte pas en JSDOM (pas de transitions CSS) | Remplacé par `{canTry && open && <Box>}` (rendu conditionnel) |
| T2.7 | `getByTitle('Copier')` échoue — MUI Tooltip ne rend pas `title` en JSDOM | Ajout `aria-label` sur IconButton, utilisation `getByRole('button', {name})` |
| T4.7 | `getByRole('combobox', {name:/dataset/i})` échoue — Select MUI sans accessible name | Ajout `labelId` + `inputProps[aria-label]` sur le Select |

### Completion Notes

- **41 tests** créés sur 6 fichiers, tous verts.
- **0 régression** introduite (7 failures pré-existantes vérifiées par `git stash` + re-run).
- `IntegrationInstructionsIcon` utilisé dans Sidebar (disponible @mui/icons-material).
- Panneau "Essayer" de `EndpointCard` : rendu conditionnel au lieu de `Collapse` pour compatibilité JSDOM.
- Implémentation fidèle aux AC1–AC4 de la story.

---

## 📁 File List

### Nouveaux fichiers

| Fichier | Description |
|---------|-------------|
| `movkfact-frontend/src/pages/ApiReferencePage.jsx` | Page principale (Tabs) |
| `movkfact-frontend/src/pages/ApiReferencePage.test.jsx` | Tests page |
| `movkfact-frontend/src/components/ApiReference/CrudTab.jsx` | Onglet CRUD |
| `movkfact-frontend/src/components/ApiReference/CrudTab.test.jsx` | Tests CrudTab |
| `movkfact-frontend/src/components/ApiReference/EndpointCard.jsx` | Carte endpoint + "Essayer" |
| `movkfact-frontend/src/components/ApiReference/EndpointCard.test.jsx` | Tests EndpointCard |
| `movkfact-frontend/src/components/ApiReference/ExtractionTab.jsx` | Onglet Extraction |
| `movkfact-frontend/src/components/ApiReference/ExtractionTab.test.jsx` | Tests ExtractionTab |
| `movkfact-frontend/src/components/ApiReference/GuideTab.jsx` | Onglet Guide |
| `movkfact-frontend/src/components/ApiReference/GuideTab.test.jsx` | Tests GuideTab |
| `movkfact-frontend/src/components/ApiReference/SnippetBlock.jsx` | Bloc code + Copier |

### Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `movkfact-frontend/src/App.jsx` | Route `/api-reference` |
| `movkfact-frontend/src/layout/Sidebar.jsx` | Entrée menu "API Reference" |

---

## 📜 Change Log

| Date | Auteur | Description |
|------|--------|-------------|
| 2026-03-05 | PM/Arch | Story créée (Party Mode — Winston + Mary) |
| 2026-03-05 | Amelia | Spec enrichie : Tasks/Subtasks, Dev Notes corrigées (axios), Status → ready-for-dev |
| 2026-03-05 | Amelia | Implémentation complète — 41 tests, 0 régression — Status → review |
