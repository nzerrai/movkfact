---
project: movkfact
author: Nouredine
date: 06 mars 2026
version: 2.0
status: active
lastUpdated: 06 mars 2026
---

# Product Backlog — Movkfact

## Vue d'Ensemble

Backlog complet pour le projet movkfact, refletant l'etat reel au 06/03/2026 apres 8 sprints livres.

---

## Epics livrees

| Epic | Titre | Sprints | Statut |
|------|-------|---------|--------|
| Epic 1 | Foundation & Core MVP | S1, S2 | Done |
| Epic 2 | Data Generation Engine | S2, S3 | Done |
| Epic 3 | Advanced Features (Batch, WebSocket) | S3, S4 | Done |
| Epic 4 | PostgreSQL Migration | S4 | Done |
| Epic 5 | Extended Data Types (PERCENTAGE, ENUM) | S5 (skipped) + S8 | Done |
| Epic 6 | Data Editor (edition inline) | S6 | Done |
| Epic 7 | Manual Wizard (creation sans CSV) | S6 | Done |
| Epic 8 | Domain Enrichment (stats agregees) | S7 | Backlog |
| Epic 9 | Advanced API (filtrage rowIds + cols) | S7 | Backlog |
| Epic 10 | Quality Gate (Docker, tests, docs) | S7 | Backlog |
| Epic 11 | Anonymisation RGPD | S8 | Done |

---

## EPIC 1 — Foundation & Core MVP

**Objectif :** Infrastructure backend/frontend, gestion CRUD des domaines.
**Sprints :** 1, 2 | **Statut :** Done

- Infrastructure Spring Boot + React + H2
- Gestion domaines (CRUD)
- Interface basique, Sidebar, Dashboard
- APIs REST standards

---

## EPIC 2 — Data Generation Engine

**Objectif :** Moteur de generation, typologies, configuration par colonnes, export JSON/CSV.
**Sprints :** 2, 3 | **Statut :** Done

- DataGeneratorService + GeneratorFactory (Strategy pattern)
- Typologies : Personnelles, Financieres, Temporelles, Techniques, Geographiques
- Upload CSV + detection de types
- Configuration colonnes, export JSON/CSV
- Suivi d'activite + versioning + reset

---

## EPIC 3 — Advanced Features & Scalability

**Objectif :** Generation par lots (Spring Batch), WebSockets temps reel, notifications.
**Sprints :** 3, 4 | **Statut :** Done

- Spring Batch : batch multi-datasets en parallele (4 workers)
- BatchGenerationModal + BatchHistoryDrawer
- WebSocket notifications progression
- Nommage custom des datasets

---

## EPIC 4 — PostgreSQL Migration

**Objectif :** Migration H2 vers PostgreSQL en production avec Flyway.
**Sprint :** 4 | **Statut :** Done

- Profil application-prod.yml avec PostgreSQL
- Migrations Flyway V001–V007
- docker-compose.yml avec stack complete

---

## EPIC 5 — Extended Data Types

**Objectif :** Enrichissement du referentiel de types disponibles.
**Sprints :** 5 (skipped) + 8 | **Statut :** Done

- PERCENTAGE : float [0.0, 100.0], 2 decimales, contraintes min/max
- ENUM : liste de valeurs configurables par l'utilisateur
- Banking lexicon autocomplete dans le wizard

---

## EPIC 6 — Data Editor

**Objectif :** Edition inline des cellules d'un dataset depuis le Data Viewer.
**Sprint :** 6 | **Statut :** Done

- DataRowEditorController + DataRowEditorService
- Composant DataEditor (edition in-place, sauvegarde)

---

## EPIC 7 — Manual Wizard

**Objectif :** Creation de datasets sans upload CSV, directement depuis l'interface.
**Sprint :** 6 | **Statut :** Done

- Wizard 4 etapes (domaine, colonnes, nb lignes, generation)
- Autocomplete noms colonnes (lexique bancaire)
- Contraintes dynamiques par type (DynamicConstraintsPanel)

---

## EPIC 8 — Domain Enrichment

**Objectif :** Statistiques agregees visibles dans la liste des domaines.
**Sprint :** 7 | **Statut :** Backlog (non demarre)

- S8.1 : Backend agregats (nb datasets, total lignes, statuts)
- S8.2 : Frontend enrichissement DomainTable + modal stats

---

## EPIC 9 — Advanced API

**Objectif :** Acces granulaire aux donnees pour pipelines CI/CD.
**Sprint :** 7 | **Statut :** Backlog (non demarre)

- S9.1 : GET /api/data-sets/{id}/rows?rowIds=&cols= filtrage avance
- S9.2 : API Reference Page (interactive) — Done S8

---

## EPIC 10 — Quality Gate

**Objectif :** Deploiement Docker production-ready, documentation complete.
**Sprint :** 7 | **Statut :** Backlog (non demarre)

- S3.5b : docker-compose up --build en < 2min
- README.md complet avec instructions

---

## EPIC 11 — Anonymisation RGPD

**Objectif :** Module d'anonymisation irreversible de fichiers CSV/JSON avec sauvegarde en dataset.
**Sprint :** 8 | **Statut :** Done

- S8.2 : Anonymisation RGPD fichiers (strategies par ColumnType, donnees synthetiques)
- S8.3 : Sauvegarde du resultat en dataset par domaine
- Voir spec : [Epic 11](../epics/epic-11-anonymisation-rgpd/11-1-anonymisation-rgpd.md)

---

## Statut global au 06/03/2026

| Metrique | Valeur |
|----------|--------|
| Sprints livres | 8 (S1 a S8) |
| Epics completes | 7 (1, 2, 3, 4, 5, 6, 7, 11) |
| Epics backlog | 3 (8, 9, 10) |
| Stories livrees estimees | 40+ |
| FRs PRD couverts | 18/20 (FR-002/003 restants — Epic 8) |
| Statut deploiement | Dev : H2 in-memory | Prod-ready : PostgreSQL + Docker |

## Prochains sprints possibles

| Sprint | Contenu suggere |
|--------|-----------------|
| Sprint 9 | Epics 8 + 9 + 10 (enrichissement domaines, API filtrage, Docker) |
| Sprint 10 | Authentification JWT, roles utilisateurs, multi-tenancy |
| Sprint 11 | Performance (Redis cache), IA detection types, plugins |
