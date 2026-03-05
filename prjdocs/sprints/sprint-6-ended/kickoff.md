---
sprint: 6
title: MVP Completion — Éditeur de données + Wizard manuel
duration: 2 semaines
startDate: 2026-04-01
endDate: 2026-04-14
status: backlog
dependsOn: [Sprint 5]
---

# Sprint 6 Kickoff Summary

**Sprint :** MVP Completion — Éditeur de données + Wizard manuel
**Durée :** 2 semaines (01/04 – 14/04/2026)
**Objectif :** Implémenter les 2 dernières fonctionnalités bloquantes pour le MVP complet du PRD : l'Éditeur de données inline (FR-012/016) et le Wizard de création manuelle (FR-006)

---

## Objectifs du Sprint

1. **Éditeur de données inline (Epic 6)** — Permettre modifier et supprimer des lignes directement dans l'UI avec traçabilité row-level
2. **Wizard de création manuelle (Epic 7)** — Flux 4 étapes de création from scratch avec contraintes dynamiques et preview 5 lignes
3. **Couverture PRD MVP** — Atteindre 20/20 FRs couverts après ce sprint

---

## Métriques Clés

- **Stories :** 4
- **Points d'effort :** 24 pts
- **Vélocité cible :** 12 pts/semaine
- **Risques :** Parsing JSON row-level sur grands datasets · Drag-and-drop wizard UI
- **Blockers :** Sprint 5 doit être complété (types étendus disponibles dans le wizard)

---

## Stories du Sprint

| Story | Titre | Points | Lead | Epic |
|-------|-------|--------|------|------|
| S6.1 | Backend Row Editor API | 5 | Amelia | 6 |
| S6.2 | Frontend Éditeur inline | 6 | Sally | 6 |
| S7.1 | Backend Wizard Support | 5 | Amelia | 7 |
| S7.2 | Frontend Wizard 4 étapes | 8 | Sally | 7 |

**Total :** 24 pts

---

## Definition of Done (Sprint 6)

- [ ] FR-011, FR-012, FR-016, FR-019 : éditeur inline fonctionnel avec traçabilité row-level
- [ ] FR-004, FR-006, FR-010 : wizard 4 étapes opérationnel depuis Domain Management
- [ ] Couverture tests >80% sur les nouveaux composants
- [ ] Aucune régression sur les fonctionnalités Sprint 1–5
- [ ] Preview 5 lignes < 500ms
- [ ] Toutes les typologies Sprint 5 disponibles dans le wizard (dépendance S5.1/5.2/5.3)

---

## Jalons Clés

- **Jour 1–3 :** S6.1 Backend Row Editor API (Amelia) + S7.1 Backend Wizard Support (Amelia, parallèle)
- **Jour 2–5 :** S6.2 Frontend Éditeur inline (Sally, démarre après validation S6.1 contrats API)
- **Jour 4–9 :** S7.2 Frontend Wizard 4 étapes (Sally, démarre après validation S7.1)
- **Jour 8–10 :** Intégration + tests croisés
- **Jour 11–14 :** Buffer, corrections, démo interne

---

## Équipe Assignée

- **Backend :** Amelia — S6.1 (5pts) + S7.1 (5pts) = 10 pts
- **Frontend :** Sally — S6.2 (6pts) + S7.2 (8pts) = 14 pts
- **QA :** Quinn — Validation croisée éditeur + wizard (non-pointé, tests manuels inclus)
- **Coordination :** Bob (SM)

---

## Dépendances

- ✅ DataSet entity avec `dataJson` (LONGTEXT) — disponible
- ✅ ActivityService + ActivityActionType — disponible
- ✅ DataGeneratorService + GeneratorFactory — disponible
- ⏳ Sprint 5 complété (types catégorielles/techniques/aléatoires dans wizard — S5.1/5.2/5.3)
- ⏳ Migration PostgreSQL (S4.2 done) — V006 Flyway pour S6.1

---

## Précisions Techniques

- **S6.1 :** Stockage JSON row-level via `objectMapper.readValue/writeValueAsString` — acceptable pour MVP (100k lignes ~5MB)
- **S6.2 :** Pagination server-side (contrairement à S2.7 client-side) — critère de performance
- **S7.1 :** Endpoint `/api/datasets/preview` sans persistance — réutilise `GeneratorFactory` existant
- **S7.2 :** Drag-and-drop via `@dnd-kit/sortable` ou boutons ↑/↓ si contrainte timeline

---

Voir les [User Stories Sprint 6](stories.md)
Voir les specs: [S6.1](../../epic-6-data-editor/6-1-backend-row-editor-api.md) · [S6.2](../../epic-6-data-editor/6-2-frontend-data-editor-inline.md) · [S7.1](../../epic-7-manual-wizard/7-1-backend-wizard-support.md) · [S7.2](../../epic-7-manual-wizard/7-2-frontend-wizard-4-steps.md)
