---
sprint: 7
title: Domain UX + Advanced API + Quality Gate
duration: 2 semaines
startDate: 2026-04-15
endDate: 2026-04-28
status: backlog
dependsOn: [Sprint 6]
---

# Sprint 7 Kickoff Summary

**Sprint :** Domain UX + Advanced API + Quality Gate
**Durée :** 2 semaines (15/04 – 28/04/2026)
**Objectif :** Enrichir la vue domaines avec les statistiques agrégées, exposer l'API de filtrage avancé par ligne, et livrer le déploiement Docker production-ready

---

## Objectifs du Sprint

1. **Enrichissement vue domaines (Epic 8)** — Statistiques agrégées (nb datasets, total lignes, statuts) dans la liste et les modals
2. **API filtrage avancé (Epic 9)** — Accès ciblé aux lignes par index + sélection de colonnes pour intégrations CI/CD
3. **Docker + Documentation (S3.5b)** — Déploiement containerisé production-ready avec stack PostgreSQL complète

---

## Métriques Clés

- **Stories :** 4
- **Points d'effort :** 17 pts
- **Vélocité cible :** 8–9 pts/semaine
- **Risques :** Performance requête JPQL agrégats · Variables REACT_APP_API_URL au build Docker
- **Blockers :** Sprint 6 doit être complété (S6.1 DataRowEditorService réutilisé par S9.1)

---

## Stories du Sprint

| Story | Titre | Points | Lead | Epic |
|-------|-------|--------|------|------|
| S8.1 | Backend Agrégats par domaine | 4 | Amelia | 8 |
| S8.2 | Frontend Enrichissement liste domaines | 4 | Sally | 8 |
| S9.1 | API Filtrage avancé (rowIds + cols) | 5 | Amelia | 9 |
| S3.5b | Docker Deployment & Documentation | 4 | Amelia | 10 |

**Total :** 17 pts

---

## Definition of Done (Sprint 7)

- [ ] FR-002 + FR-003 : liste domaines avec nb datasets, total lignes, statuts agrégés
- [ ] FR-014 : `GET /api/data-sets/{id}/rows?rowIds=&cols=` opérationnel + documenté Swagger
- [ ] `docker-compose up --build` démarre l'application complète sans erreur
- [ ] README.md complet avec instructions testées
- [ ] Couverture tests >80% sur nouveaux composants
- [ ] Aucune régression Sprint 1–6

---

## Jalons Clés

- **Jour 1–3 :** S8.1 Backend agrégats (Amelia) + S9.1 API filtrage (Amelia, parallèle si possible)
- **Jour 2–5 :** S8.2 Frontend stats domaines (Sally)
- **Jour 4–8 :** S3.5b Docker + Documentation (Amelia, après S9.1)
- **Jour 9–12 :** Tests croisés + Smoke test Docker
- **Jour 13–14 :** Buffer + démo finale

---

## Équipe Assignée

- **Backend :** Amelia — S8.1 (4pts) + S9.1 (5pts) + S3.5b (4pts) = 13 pts
- **Frontend :** Sally — S8.2 (4pts) = 4 pts
- **QA :** Quinn — Validation smoke test Docker + non-régression
- **Coordination :** Bob (SM)

---

## Dépendances

- ✅ Sprint 6 complété : S6.1 `DataRowEditorService` réutilisé par S9.1
- ✅ PostgreSQL + Flyway (S4.2) — toutes migrations V001–V006 appliquées
- ✅ Activity entity + statuts (S3.1) — utilisés par S8.1

---

## Précisions Techniques

- **S8.1 :** Requête JPQL avec GROUP BY + LEFT JOIN évite N+1 — vérifier `@DataJpaTest` performance
- **S9.1 :** Réutilise `DataRowEditorService.getRowsByIds()` créé en S6.1 — pas de duplication
- **S3.5b :** `REACT_APP_API_URL` doit être injecté au **build time** React (pas runtime) — variable ARG Dockerfile

---

Voir les [User Stories Sprint 7](stories.md)
Voir les specs: [S8.1](../../epic-8-domain-enrichment/8-1-backend-domain-aggregates.md) · [S8.2](../../epic-8-domain-enrichment/8-2-frontend-domain-stats.md) · [S9.1](../../epic-9-advanced-api/9-1-api-row-filtering.md) · [S3.5b](../../epic-10-quality-gate/s3-5b-docker-documentation.md)
