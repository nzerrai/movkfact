# Sprint 6 Planning Summary

**Date :** 04 mars 2026
**Sprint :** Sprint 6 — MVP Completion (Éditeur de données + Wizard manuel)
**Durée :** 2 semaines | 01/04/2026 – 14/04/2026
**Total Points :** 24 pts
**Status :** ✅ PLANNING COMPLETE — Backlog (démarrage après Sprint 5)

---

## Contexte

Sprint 6 est issu de l'analyse différentielle PRD vs sprints livrés (04/03/2026).
5 FRs du PRD sont non couverts après Sprint 1–5 :
- **FR-006 + FR-010** (Wizard manuel) — BLOQUANT MVP §7
- **FR-012 + FR-016** (Éditeur inline) — BLOQUANT MVP §7
- **FR-019** (Trace row-level) — complète l'activité tracking

Sprint 6 ferme ces 5 gaps et complète le MVP PRD Phase 1.

---

## Sprint Goal

> **"Après Sprint 6, un utilisateur peut créer un dataset from scratch via wizard ET modifier/supprimer des lignes individuelles — les 20 FRs du PRD sont couverts."**

---

## Vue d'ensemble des Stories

| ID | Titre | Pts | Lead | Epic | FRs |
|----|-------|-----|------|------|-----|
| S6.1 | Backend Row Editor API | 5 | Amelia | 6 | FR-011/012/016/019 |
| S6.2 | Frontend Éditeur inline | 6 | Sally | 6 | FR-011/012/016/019 |
| S7.1 | Backend Wizard Support | 5 | Amelia | 7 | FR-004/006/008 |
| S7.2 | Frontend Wizard 4 étapes | 8 | Sally | 7 | FR-004/006/010 |
| **Total** | | **24** | | | |

---

## Plan de Parallélisation

```
SEMAINE 1                           SEMAINE 2
J1   J2   J3   J4   J5  |  J6   J7   J8   J9   J10
─────────────────────────┼──────────────────────────
Amelia: [S6.1 ████████] [S7.1 ████████] [Tests / Fixes]
Sally:  [──] [S6.2 ████████████]  [S7.2 ██████████████]
                ↑ débute J2         ↑ débute J4
                (S6.1 API contrats  (S7.1 preview endpoint
                 validés J1-J2)      validé J3)
─────────────────────────┼──────────────────────────
J11  J12  J13  J14
Intégration croisée + Buffer + Validation Quinn
```

---

## Dépendances et Risques

### Dépendances satisfaites au démarrage Sprint 6
- ✅ `DataSet.dataJson` (LONGTEXT) — stockage JSON disponible
- ✅ `ActivityService` + `ActivityActionType` — extensibles
- ✅ `GeneratorFactory` — réutilisé pour preview sans persistance
- ✅ PostgreSQL + Flyway (S4.2) — V006 migration prête
- ⏳ Sprint 5 complété : types catégorielles/techniques/aléatoires dans le wizard (S7.2 dépend de S5.1/5.2/5.3)

### Risques identifiés

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|------------|
| Performance parsing JSON 100k lignes | Moyenne | Moyen | Limiter taille page (50 lignes), noter dette technique |
| Drag-and-drop wizard timeline | Faible | Faible | Fallback : boutons ↑/↓ si @dnd-kit bloque |
| Types Sprint 5 non disponibles pour wizard | Faible | Moyen | S7.2 peut démarrer sans types étendus (les ajouter post-intégration) |
| Régression sur S2.7 DataViewer | Faible | Faible | Tests de non-régression explicites dans S6.2 |

---

## Critères d'Acceptation Sprint

| Critère | Cible | Mesure |
|---------|-------|--------|
| FR couverts | 20/20 | Après S6.2 + S7.2 livrées |
| Test coverage | >80% | Jest + JUnit |
| Performance preview | <500ms | Test intégration |
| Performance éditeur | <200ms/page | Test intégration (50 rows) |
| Zéro régression | 0 | Suite tests existants |
| Wizard : retour arrière | État conservé | Test Jest flux complet |

---

## Roadmap Complète Post Sprint 6

```
Sprint 5 (backlog)    : Types étendus + Auth JWT + E2E   = 29 pts
Sprint 6 (backlog)    : Éditeur + Wizard                 = 24 pts  ← CE SPRINT
Sprint 7 (backlog)    : Domain Enrichment + API avancée  = 17 pts
                        (Epic 8 + Epic 9 + Docker S3.5)
```

### Sprint 7 — prévisionnel (Epic 8 + 9 + Docker)

| Story | Titre | Pts | Lead |
|-------|-------|-----|------|
| S8.1 | Backend agrégats domaines | 4 | Amelia |
| S8.2 | Frontend stats domaines | 4 | Sally |
| S9.1 | API filtrage avancé (rowIds + cols) | 5 | Amelia |
| S3.5b | Docker + Documentation (reprise S3.5 skippé) | 4 | Amelia |
| **Total** | | **17** | |

---

## Actions Requises avant Démarrage

**Pour Amelia (Backend Lead) :**
```
[ ] Lire les specs S6.1 et S7.1 en détail
[ ] Vérifier le format actuel de ColumnConfig.additionalConfig (Map ou String JSON ?)
[ ] Confirmer que Jackson ObjectMapper est injectable dans DataRowEditorService
[ ] Préparer V006 Flyway migration localement
[ ] Planifier ordre : S6.1 J1-J2, puis S7.1 J3-J4 (ou en parallèle si possible)
```

**Pour Sally (Frontend Lead) :**
```
[ ] Lire les specs S6.2 et S7.2 en détail
[ ] Confirmer l'emplacement actuel de DataViewerPage (page ou composant ?)
[ ] Décider : drag-and-drop @dnd-kit ou boutons ↑/↓ (estimation effort)
[ ] Préparer la structure ManualWizard/ dès J1
[ ] Aligner avec Amelia sur les contrats API (payloads exact S6.1 et S7.1)
```

**Pour Quinn (QA) :**
```
[ ] Préparer scénarios de validation manuelle pour l'éditeur
[ ] Préparer jeux de test CSV pour le wizard (colonnes mixtes avec contraintes)
[ ] Vérifier plan de non-régression Sprint 1–5
```

---

## Références

- [Kickoff Summary](kickoff-summary.md)
- [Sprint 6 Stories](stories.md)
- [Spec S6.1 — Backend Row Editor](../../epic-6-data-editor/6-1-backend-row-editor-api.md)
- [Spec S6.2 — Frontend Éditeur inline](../../epic-6-data-editor/6-2-frontend-data-editor-inline.md)
- [Spec S7.1 — Backend Wizard Support](../../epic-7-manual-wizard/7-1-backend-wizard-support.md)
- [Spec S7.2 — Frontend Wizard 4 étapes](../../epic-7-manual-wizard/7-2-frontend-wizard-4-steps.md)
- [Sprint Status](../sprint-status.yaml)
- [PRD](../../planning-artifacts/prd.md)

---

**Sprint 6 Status :** ✅ PLANNING COMPLETE — Backlog
**Auteur :** John (PM Agent) — 04/03/2026
