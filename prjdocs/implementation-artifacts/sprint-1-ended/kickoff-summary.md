---
sprint: 1
title: Foundation & Domain Management
duration: 2 semaines
startDate: 2026-03-03
endDate: 2026-03-16
status: ready
---

# Sprint 1 Kickoff Summary

**Sprint :** Foundation & Domain Management  
**Durée :** 2 semaines (03/03 - 16/03/2026)  
**Objectif :** Établir infrastructure Backend/Frontend et permettre gestion de domaines

---

## Objectifs du Sprint

1. **Infrastructure Backend :** API Spring Boot fonctionnelle sur port 8080
2. **Interface Frontend :** Dashboard React basique avec MUI
3. **Gestion Domaines :** CRUD complet pour domaines
4. **Tests & Documentation :** Setup CI/CD, tests initiaux

---

## Métriques Clés

- **Stories :** 5
- **Points d'effort :** 21
- **Vélocité cible :** 10-11 points/semaine
- **Risques :** Configuration initiale, dépendances
- **Blockers :** Aucun identifié

---

## Definition of Done (Sprint 1)

- [ ] Tests unitaires écrits et passants (>80% coverage)
- [ ] Tests d'intégration API réussis
- [ ] Code revu et approuvé
- [ ] Documentation API (Swagger/OpenAPI)
- [ ] Déployable localement (Docker)
- [ ] Story validée par PM

---

## Jalons Clés

- **Jour 1-2 :** Setup projet, configuration Maven/npm, DB H2
- **Jour 3-5 :** Backend API domaines
- **Jour 6-8 :** Frontend dashboard, composants MUI
- **Jour 9-10 :** Tests intégrés, CI/CD, Docker
- **Jour 11-14 :** Buffer, fixes, documentation

---

## Équipe Assignée

- **Backend :** Amelia (Dev) + Winston (Architect consultation)
- **Frontend :** Sally (UX) + Amelia (Dev)
- **QA :** Quinn (QA Engineer)
- **Coordination :** Bob (SM)

---

## Dépendances Externes

- [ ] Environnement dev setup (IDE, Git, Docker)
- [ ] Accès base données H2 configuré
- [ ] MUI npm packages disponibles
- [ ] GitHub Actions configuré

---

## Notes Importantes

- Utilisez les conventions de nommage définies en Architecture (snake_case DB, PascalCase composants, etc.)
- Suivez les patterns d'API définis : format {data, message}, codes HTTP standards
- Implémenter authentication (JWT) dès le départ pour sécurité
- Tests avant déploiement

---

Voir les [User Stories Sprint 1](stories.md)
