# Story S8.2: Frontend Enrichissement liste domaines

**Sprint:** À planifier (Epic 8)
**Points:** 4
**Epic:** EPIC 8 - Enrichissement vue domaines
**Type:** Frontend Feature
**Lead:** Sally
**Status:** Backlog
**Dependencies:** S8.1 (API enrichie), S1.5 (Domain Management UI existant)
**FRs couverts:** FR-002, FR-003

---

## User Story

**En tant que** développeur ou analystes de données,
**Je veux** voir directement dans la liste des domaines le nombre de datasets, le total de lignes et les statuts d'activité,
**Afin de** naviguer efficacement sans ouvrir chaque domaine individuellement.

---

## Contexte métier

La liste des domaines actuelle (`DomainTable.jsx`) affiche uniquement nom, description et actions.
Cette story ajoute les colonnes statistiques et les badges statuts, et enrichit la modal de datasets avec les informations de statut par dataset.

---

## ✅ Acceptance Criteria

### AC1 — Colonnes enrichies dans DomainTable (FR-002)
- [ ] Nouvelles colonnes dans le tableau des domaines :
  - "Datasets" : nombre de datasets (chip MUI avec compteur)
  - "Lignes totales" : total formaté (ex: "12 500", "1,2M" pour >1 000 000)
  - "Statuts" : badges icônes (📥 téléchargé, ✏️ modifié, 👁 consulté) — affichés seulement si `true`
- [ ] Colonnes masquables sur mobile (masquer "Lignes totales" < 768px)
- [ ] Données chargées depuis `GET /api/domains` enrichi (S8.1)
- [ ] Skeleton loaders pendant le chargement initial

### AC2 — Modal datasets enrichie (FR-003)
- [ ] La modal "Voir les datasets" (ou `DomainDatasetsModal`) affiche pour chaque dataset :
  - Nom, nombre de lignes, nombre de colonnes (existants)
  - Badges statuts : Téléchargé / Modifié / Consulté (MUI Chip colorés)
  - Date de dernière modification formatée ("il y a 2h", "hier", "04/03/2026")
- [ ] Tri par "Dernière modification" par défaut (desc)
- [ ] Filtre rapide par statut (Tous / Modifiés / Téléchargés)

### AC3 — Indicateurs visuels cohérents
- [ ] Badge "Téléchargé" : chip vert (#4caf50) avec icône DownloadDone
- [ ] Badge "Modifié" : chip orange (#ff9800) avec icône Edit
- [ ] Badge "Consulté" : chip bleu (#2196f3) avec icône Visibility
- [ ] Si aucun statut : chip gris "Nouveau"
- [ ] Les mêmes badges utilisés dans la liste domaines ET dans la modal datasets

### AC4 — Tests
- [ ] Tests Jest pour `DomainTable.jsx` : colonnes enrichies, skeleton, badges statuts
- [ ] Tests Jest pour `DomainDatasetsModal.jsx` : affichage statuts, tri, filtre
- [ ] Tests Jest pour `StatusBadge.jsx` (composant partagé) : les 4 états
- [ ] Coverage >80% sur les composants modifiés/créés

---

## 🏗️ Spécifications Techniques

### Nouveaux fichiers à créer

```
movkfact-frontend/src/components/
  StatusBadge/
    StatusBadge.jsx               ← composant partagé (domain list + modal)
    StatusBadge.test.js
    index.js
```

### Modifications requises

```
movkfact-frontend/src/components/DomainTable.jsx
  + Colonnes "Datasets", "Lignes totales", "Statuts"
  + Skeleton pour chargement
  + Utilisation StatusBadge

movkfact-frontend/src/components/DomainDatasetsModal/
  + Colonne statuts (StatusBadge)
  + Colonne "Dernière activité" formatée
  + Tri + filtre par statut
  + Chargement depuis GET /api/domains/{id}/datasets enrichi

movkfact-frontend/src/services/domainService.js
  → getDomains() retourne déjà datasetCount, totalRows, statuses (via API S8.1)
  → getDatasetsByDomain(id) retourne statuts + lastActivity (via API S8.1)
```

### StatusBadge — Composant partagé

```jsx
// Usage: <StatusBadge downloaded={true} modified={false} viewed={true} />
const StatusBadge = ({ downloaded, modified, viewed }) => {
  if (!downloaded && !modified && !viewed) {
    return <Chip label="Nouveau" size="small" color="default" />;
  }
  return (
    <Stack direction="row" spacing={0.5}>
      {downloaded && <Chip icon={<DownloadDoneIcon />} label="Téléchargé" size="small" sx={{ bgcolor: '#e8f5e9' }} />}
      {modified  && <Chip icon={<EditIcon />}         label="Modifié"    size="small" sx={{ bgcolor: '#fff3e0' }} />}
      {viewed    && <Chip icon={<VisibilityIcon />}   label="Consulté"   size="small" sx={{ bgcolor: '#e3f2fd' }} />}
    </Stack>
  );
};
```

### Formatage "Lignes totales"

```javascript
const formatRowCount = (count) => {
  if (count >= 1_000_000) return `${(count / 1_000_000).toFixed(1)}M`;
  if (count >= 1_000)     return count.toLocaleString('fr-FR');
  return String(count);
};
```

---

## 📝 Dev Notes

- `StatusBadge` est un composant présentationnel pur (props only, pas de state)
- Le tri et filtre de la modal datasets se font côté client (les données sont peu nombreuses pour un domaine)
- "Dernière activité" : utiliser `date-fns` (`formatDistanceToNow`) déjà installé ou sinon `dayjs`
- Skeleton MUI : `<Skeleton variant="rectangular" />` pour les colonnes statistiques pendant le chargement

---

## 📊 Estimation

| Composant | Effort | Responsable |
|-----------|--------|-------------|
| `StatusBadge.jsx` (composant partagé) | 0.25j | Sally |
| `DomainTable.jsx` — 3 nouvelles colonnes + skeleton | 0.75j | Sally |
| `DomainDatasetsModal` — statuts + tri + filtre | 0.75j | Sally |
| Tests Jest | 0.75j | Sally |
| **Total** | **2.5j** | **4 pts** |
