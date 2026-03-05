# Sprint 7 Planning Summary

**Date :** 04 mars 2026
**Sprint :** Sprint 7 — Domain UX + Advanced API + Quality Gate
**Durée :** 2 semaines | 15/04/2026 – 28/04/2026
**Total Points :** 17 pts
**Status :** ✅ PLANNING COMPLETE — Backlog (démarrage après Sprint 6)

---

## Contexte

Sprint 7 clôture les epics de second niveau (8, 9, 10) issus de l'analyse différentielle PRD.
Après Sprint 6, les 20 FRs MVP sont couverts. Sprint 7 couvre les enrichissements UX (FR-002/003), l'API avancée CI/CD (FR-014) et le déploiement production.

---

## Sprint Goal

> **"Après Sprint 7, movkfact est déployable en production en une commande, avec une vue domaines enrichie et une API d'accès granulaire pour les pipelines CI/CD."**

---

## Vue d'ensemble des Stories

| ID | Titre | Pts | Lead | Epic | FRs |
|----|-------|-----|------|------|-----|
| S8.1 | Backend Agrégats domaines | 4 | Amelia | 8 | FR-002/003 |
| S8.2 | Frontend Enrichissement domaines | 4 | Sally | 8 | FR-002/003 |
| S9.1 | API Filtrage avancé rowIds + cols | 5 | Amelia | 9 | FR-014 |
| S3.5b | Docker + Documentation | 4 | Amelia | 10 | NFRs |
| **Total** | | **17** | | | |

---

## Plan de Parallélisation

```
SEMAINE 1                           SEMAINE 2
J1   J2   J3   J4   J5  |  J6   J7   J8   J9   J10
─────────────────────────┼──────────────────────────
Amelia: [S8.1 ████] [S9.1 ████████] [S3.5b ████████]
Sally:  [────] [S8.2 ████████████] [Tests / Fixes]
               ↑ débute J2
               (S8.1 contrats API)
─────────────────────────┼──────────────────────────
J11  J12  J13  J14
Tests croisés + Smoke Docker + Buffer + Validation Quinn
```

---

## Critères d'Acceptation Sprint

| Critère | Cible |
|---------|-------|
| FR-002/003 : stats domaines | Visible dans DomainTable + Modal |
| FR-014 : API filtrage | `?rowIds=&cols=` opérationnel |
| docker-compose up | < 2min, smoke test OK |
| Test coverage | >80% nouveaux composants |
| Zéro régression | Suite tests Sprint 1–6 passe |
| Swagger docs | Tous endpoints documentés |

---

## Couverture PRD cumulée après Sprint 7

| FR | Description | Couverture |
|----|-------------|-----------|
| FR-001 | Créer domaine | ✅ Sprint 1 |
| FR-002 | Consulter domaines (stats agrégées) | ✅ **Sprint 7** |
| FR-003 | Accéder datasets avec statuts | ✅ **Sprint 7** |
| FR-004/005 | Flux CSV + initiation | ✅ Sprint 2 |
| FR-006/010 | Wizard + lancer depuis DM | ✅ Sprint 6 |
| FR-007 | Générer dataset JSON | ✅ Sprint 2 |
| FR-008 | Typologies complètes | ✅ Sprint 5 |
| FR-009 | Écran Domain Management | ✅ Sprint 1 |
| FR-011/012/016 | Éditeur inline | ✅ Sprint 6 |
| FR-013 | Télécharger dataset | ✅ Sprint 2 |
| FR-014 | API filtrage avancé | ✅ **Sprint 7** |
| FR-015 | CRUD APIs | ✅ Sprint 2 |
| FR-017/018/019 | Activité + reset + trace | ✅ Sprint 2–3–6 |
| FR-020 | Génération par lots | ✅ Sprint 3–4 |
| **NFRs déploiement** | Docker + PostgreSQL | ✅ **Sprint 7** |

**→ 20/20 FRs + déploiement production couverts après Sprint 7.**

---

## Risques identifiés

| Risque | Mitigation |
|--------|------------|
| N+1 requêtes agrégats S8.1 | JPQL GROUP BY + chargement bulk activities |
| `REACT_APP_API_URL` au build time | Documenter dans README + `.env.example` |
| Volumes Docker data loss entre rebuilds | Volume nommé `postgres_data` persistant |
| Swagger incomplet (endpoints Sprint 6) | S3.5b inclut un audit complet Swagger |

---

## Références

- [Kickoff Summary](kickoff-summary.md)
- [Sprint 7 Stories](stories.md)
- [Spec S8.1](../../epic-8-domain-enrichment/8-1-backend-domain-aggregates.md)
- [Spec S8.2](../../epic-8-domain-enrichment/8-2-frontend-domain-stats.md)
- [Spec S9.1](../../epic-9-advanced-api/9-1-api-row-filtering.md)
- [Spec S3.5b](../../epic-10-quality-gate/s3-5b-docker-documentation.md)
- [Sprint Status](../sprint-status.yaml)
- [PRD](../../planning-artifacts/prd.md)

---

**Sprint 7 Status :** ✅ PLANNING COMPLETE — Backlog
**Auteur :** John (PM Agent) — 04/03/2026
