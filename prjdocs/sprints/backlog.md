---
project: movkfact
author: Nouredine
date: 26 février 2026
version: 1.0
status: active
---

# Product Backlog - Movkfact

## Vue d'Ensemble

Le backlog complet pour le projet movkfact, organisé en 3 épics majeurs alignés sur les phases de livraison du PRD.

---

## EPIC 1 : Foundation & Core MVP

**Objectif :** Établir les fondations et délivrer le MVP avec gestion de domaines, interface basique et APIs CRUD.

**Alignement PRD :** Exigences 2.1, 2.3, 2.5 (partiellement), 2.6

**Sprints :** Sprint 1, Sprint 2 (partiellement)

### Détail Epic

- Créer infrastructure backend Spring Boot
- Créer interface frontend React
- Implémenter gestion des domaines
- Implémenter APIs CRUD basiques
- Implémenter suivi d'activité

---

## EPIC 2 : Data Generation Engine

**Objectif :** Implémenter le moteur de génération de données avec 3 typologies de base.

**Alignement PRD :** Exigences 2.2 (partiellement avec MVP limitée à 3 typologies)

**Sprints :** Sprint 2, Sprint 3 (partiellement)

### Détail Epic

- Implémenter DataGeneratorService
- Implémenter 3 typologies : Personnelles, Financières, Temporelles
- Implémenter configuration par colonnes
- Implémenter sortie JSON
- Implémenter détection intelligente de types

---

## EPIC 3 : Advanced Features & Scalability

**Objectif :** Ajouter génération par lots, APIs avancées, plugins et scalabilité.

**Alignement PRD :** Exigences 2.4, 2.7, 3.4 (microservices), Section 4 (Phase 2/3)

**Sprints :** Sprint 3, Phases futures

### Détail Epic

- Implémenter Spring Batch pour génération par lots
- Implémenter WebSockets pour notifications
- Implémenter APIs avancées (extraction par critères)
- Système de plugins pour nouvelles typologies
- Préparation microservices

---

# SPRINT 1 : Foundation & Domain Management (2 semaines)

**Objectif :** Établir l'infrastructure et permettre la gestion de domaines.

**Livrables :** Backend API, Frontend basique, Gestion domaines opérationnelle

**Nombre de stories :** 5

**Points d'effort :** 21 points

**Alignement :** Phase 1 PRD (Infrastructure + Gestion domaines)

**Dates :** 03/03/2026 - 16/03/2026

Voir [Sprint 1 Details](sprint-1-2026-03-03/kickoff-summary.md)

---

# SPRINT 2 : Data Generation & Configuration (2 semaines)

**Objectif :** Implémenter le cœur du produit - génération de données configurables.

**Livrables :** Moteur génération, 3 typologies, Interface configuration, APIs complètes

**Nombre de stories :** 6

**Points d'effort :** 34 points

**Alignement :** Phase 1 PRD (Génération + IHM)

**Dates :** 17/03/2026 - 30/03/2026

Voir [Sprint 2 Details](sprint-2-2026-03-17/kickoff-summary.md)

---

# SPRINT 3 : Activity Tracking & Batch Generation (2 semaines)

**Objectif :** Compléter le MVP avec suivi d'activité et génération par lots.

**Livrables :** Suivi d'activité opérationnel, Batch jobs, Notifications, Tests complets

**Nombre de stories :** 5

**Points d'effort :** 28 points

**Alignement :** Phase 1 PRD (Suivi) + Début Phase 2 (Lots)

**Dates :** 31/03/2026 - 13/04/2026

Voir [Sprint 3 Details](sprint-3-2026-03-31/kickoff-summary.md)

---

## Résumé du Backlog

| Métrique | Valeur |
|----------|--------|
| Épics | 3 |
| Sprints | 3 |
| Total Stories | 16 |
| Total Points | 83 |
| Durée estimée | 6 semaines |
| État | À démarrer |

## Alignement avec Phases de Livraison PRD

- **Phase 1 (Q1)** : Sprints 1-3 (MVP complet)
- **Phase 2 (Q2)** : EPIC 3, stockage plugins
- **Phase 3 (Q3+)** : Microservices, IA, modes avancés
