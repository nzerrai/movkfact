---
sprint: 6
totalStories: 4
totalPoints: 24
---

# Sprint 6 User Stories

## Story S6.1 : Backend Row Editor API

**Points :** 5
**Epic :** EPIC 6 — Data Editor (Éditeur de données inline)
**Type :** Backend Feature
**Assigné :** Amelia
**FRs :** FR-011, FR-012, FR-016, FR-019

### Description
Implémenter les endpoints REST pour lire, modifier et supprimer des lignes individuelles d'un dataset, avec traçabilité row-level dans la table `Activity`.

### Acceptance Criteria
- [ ] `GET /api/data-sets/{id}/rows?page=0&size=50` — liste paginée avec `rowIndex`
- [ ] `GET /api/data-sets/{id}/rows/{rowIndex}` — ligne unique
- [ ] `PUT /api/data-sets/{id}/rows/{rowIndex}` — modifier colonnes (merge partiel)
- [ ] `DELETE /api/data-sets/{id}/rows/{rowIndex}` — supprimer + réindexer
- [ ] `ActivityActionType.ROW_MODIFIED` et `ROW_DELETED` ajoutés
- [ ] `Activity` étendue : `rowIndex`, `modifiedColumns`, `previousValue`
- [ ] Flyway `V006__add_row_editor_activity_columns.sql`
- [ ] `originalData` jamais modifiée par les opérations row-level
- [ ] Tests unitaires + intégration >80% coverage

**Spec complète :** [6-1-backend-row-editor-api.md](../../epic-6-data-editor/6-1-backend-row-editor-api.md)

---

## Story S6.2 : Frontend Éditeur de données inline

**Points :** 6
**Epic :** EPIC 6 — Data Editor (Éditeur de données inline)
**Type :** Frontend Feature
**Assigné :** Sally
**FRs :** FR-011, FR-012, FR-016, FR-019

### Description
Ajouter un onglet "Éditeur de données" dans la vue dataset, avec cellules éditables in-place (double-clic), suppression de ligne confirmée et journal d'activité latéral.

### Acceptance Criteria
- [ ] 2 onglets dans la vue dataset : "Visualiseur" (existant) + "Éditeur de données" (nouveau)
- [ ] Tableau paginé server-side (50 lignes/page) depuis `GET /api/data-sets/{id}/rows`
- [ ] Double-clic cellule → TextField inline ; Entrée = confirme (PUT) ; Échap = annule
- [ ] Bouton suppression par ligne → Dialog de confirmation → DELETE
- [ ] Toast succès/erreur pour chaque opération
- [ ] Panneau latéral "Historique" (rétractable) : activités ROW_MODIFIED/ROW_DELETED
- [ ] Badge sur l'onglet indiquant le nb de modifications en session
- [ ] Tests Jest >80% coverage

**Spec complète :** [6-2-frontend-data-editor-inline.md](../../epic-6-data-editor/6-2-frontend-data-editor-inline.md)

---

## Story S7.1 : Backend Wizard Support

**Points :** 5
**Epic :** EPIC 7 — Wizard de création manuelle
**Type :** Backend Feature
**Assigné :** Amelia
**FRs :** FR-004, FR-006, FR-008

### Description
Créer l'endpoint `POST /api/datasets/preview` (5 lignes sans persistance) et étendre `ColumnConfigDTO` avec les contraintes dynamiques par type (min/max, dateFrom/dateTo, maxLength).

### Acceptance Criteria
- [ ] `POST /api/datasets/preview` génère 5 lignes max sans persistance en base
- [ ] Temps de réponse < 500ms
- [ ] `ColumnConfigDTO.constraints` : `Map<String, Object>` (optionnel)
- [ ] `INTEGER`/`DECIMAL` : contraintes `min`/`max` respectées par les générateurs
- [ ] `DATE`/temporels : contraintes `dateFrom`/`dateTo` respectées
- [ ] `TEXT`/`LOREM_IPSUM` : contrainte `maxLength` appliquée
- [ ] 400 si contraintes invalides (min > max, dateFrom > dateTo)
- [ ] Validation : nom 3–50 chars, lignes 1–100 000, ≥1 colonne
- [ ] Tests unitaires + intégration >80%

**Spec complète :** [7-1-backend-wizard-support.md](../../epic-7-manual-wizard/7-1-backend-wizard-support.md)

---

## Story S7.2 : Frontend Wizard de création manuelle 4 étapes

**Points :** 8
**Epic :** EPIC 7 — Wizard de création manuelle
**Type :** Frontend Feature
**Assigné :** Sally
**FRs :** FR-004, FR-006, FR-010

### Description
Implémenter le wizard guidé en 4 étapes depuis Domain Management : (1) nom + nb lignes, (2) configuration colonnes + types + contraintes + réordonnancement, (3) prévisualisation 5 lignes, (4) confirmation + génération.

### Acceptance Criteria
- [ ] Bouton "Créer un dataset" sur chaque domaine → Dialog choix CSV / Wizard manuel
- [ ] Étape 1 : nom validé (3–50 chars, unicité live) + nb lignes (1–100 000)
- [ ] Étape 2 : ajout/suppression colonnes, dropdown type, champs contraintes dynamiques par type, réordonnancement
- [ ] Étape 3 : 5 lignes preview depuis `POST /api/datasets/preview`, bouton retour étape 2
- [ ] Étape 4 : récapitulatif + bouton "Lancer la génération" (appel API existant)
- [ ] Toast "Dataset '{name}' créé avec {N} lignes" en succès
- [ ] État wizard conservé lors des retours arrière
- [ ] Tests Jest flux complet + tests unitaires par étape >80%

**Spec complète :** [7-2-frontend-wizard-4-steps.md](../../epic-7-manual-wizard/7-2-frontend-wizard-4-steps.md)

---

## Résumé Sprint 6

**Total Points :** 24
**Estimation effort :** 2 semaines (équipe Amelia + Sally)

| Story | Points | Assigné | Type | Dépend de |
|-------|--------|---------|------|-----------|
| S6.1 Backend Row Editor | 5 | Amelia | Backend | S2.1, S3.1 |
| S6.2 Frontend Éditeur inline | 6 | Sally | Frontend | S6.1 |
| S7.1 Backend Wizard Support | 5 | Amelia | Backend | S2.1 |
| S7.2 Frontend Wizard 4 étapes | 8 | Sally | Frontend | S7.1, Sprint 5 |

**Parallélisation :**
- S6.1 et S7.1 peuvent démarrer simultanément (Amelia — 2 stories backend indépendantes)
- S6.2 démarre dès S6.1 contrats API validés (J2–J3)
- S7.2 démarre dès S7.1 preview endpoint validé (J3–J4)
- Intégration et tests croisés : J8–J10

**État :** Backlog (démarre après Sprint 5)
