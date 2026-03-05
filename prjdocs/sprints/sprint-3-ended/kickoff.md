---
sprint: 3
title: Activity Tracking & Batch Generation
duration: 2 semaines
startDate: 2026-03-31
endDate: 2026-04-13
status: ready
dependsOn: [Sprint 1, Sprint 2]
---

# Sprint 3 Kickoff Summary

**Sprint :** Activity Tracking & Batch Generation  
**Durée :** 2 semaines (31/03 - 13/04/2026)  
**Objectif :** Compléter MVP avec suivi d'activité, génération par lots et tests complets

---

## Objectifs du Sprint

1. **Activity Tracking :** Implémenter suivi des jeux de données (statuts, historique)
2. **Batch Generation :** Spring Batch pour générer plusieurs jeux en parallèle
3. **WebSockets Notifications :** Notifications temps réel aux utilisateurs
4. **Testing & Quality :** Tests complets, couverture >80%
5. **Documentation & Deployment :** Docker ready, déploiement simplifié

---

## Métriques Clés

- **Stories :** 5
- **Points d'effort :** 28
- **Vélocité cible :** 14 points/semaine
- **Risques :** WebSockets complexity, performance lot jobs
- **Blockers :** Sprint 1-2 doivent être complétés

---

## Definition of Done (Sprint 3)

- [ ] Tests de couverture >80% (backend + frontend)
- [ ] Tests E2E avec Cypress/Playwright
- [ ] Tests de performance (1000 lots génération)
- [ ] Tous les codes HTTP validés
- [ ] Documentation API finalisée
- [ ] Déploiement Docker testé
- [ ] User feedback collecté et intégré

---

## Jalons Clés

- **Jour 1-2 :** ActivityService + tracking DB
- **Jour 3-5 :** Spring Batch configuration
- **Jour 6-8 :** WebSockets endpoints
- **Jour 9-11 :** Frontend notifications + tests
- **Jour 12-14 :** E2E tests, Docker, buffer

---

## Équipe Assignée

- **Backend :** Amelia (Dev) + Winston (Architect)
- **Frontend :** Sally (UX) + Amelia (Dev)
- **QA :** Quinn (QA Engineer) - Tests E2E, performance
- **DevOps :** Amelia (Dev) - Docker, CI/CD
- **Coordination :** Bob (SM)

---

## Dépendances Externes

- [ ] Sprint 1-2 complétés
- [ ] Spring Batch disponible (Maven)
- [ ] WebSocket support (Spring Websocket)
- [ ] Docker/Docker Compose configuré
- [ ] GitHub Actions pour CI/CD

---

## Précisions Techniques

- **Activity Statuses :** downloaded, modified, viewed
- **Batch Jobs :** Spring Batch avec JobLauncher
- **WebSockets :** Spring Websocket + STOMP protocol
- **Original Copy :** Versioning léger (version 1 = original)
- **Reset :** Remplacer actuel par original stocké

---

## MVP Completion Criteria

Sprint 3 complète le MVP de Phase 1 PRD :
- ✅ Génération basique + 3 typologies
- ✅ IHM intuitive
- ✅ APIs CRUD
- ✅ Suivi d'activité
- ✅ Génération par lots
- ✅ Tests & Documentation
- ✅ Déployable (Docker)

---

Voir les [User Stories Sprint 3](stories.md)
