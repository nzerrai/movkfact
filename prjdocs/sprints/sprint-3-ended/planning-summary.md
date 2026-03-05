# Sprint 3 Planning Summary
**Date:** 02 mars 2026 @ 08:15 CET  
**Updated:** 02 mars 2026 @ 08:45 CET (Added S3.0 Dataset Naming - Priority HIGH)  
**Sprint:** Sprint 3 - Advanced Features & Scalability  
**Duration:** 02/03/2026 - 13/04/2026 (6 weeks)  
**Total Points:** 31 (28 + 3 new)  
**Status:** ✅ PLANNING COMPLETE - Ready for Implementation  

---

## 📊 Sprint Overview

**Sprint 3** complète le MVP avec 5 fonctionnalités majeures + 1 prioritaire **UX improvement** :
1. **[NEW] Dataset Naming** - Permettre nommer les datasets (priority HIGH) ⭐
2. **Activity Tracking** - Historique & versioning des datasets
3. **Batch Generation** - Génération parallèle multi-datasets
4. **WebSocket Notifications** - Notifications temps réel
5. **E2E Testing** - Test coverage complet
6. **Docker & Docs** - Déploiement containerisé

---

## 🎯 Sprint Goals

✅ **Primary:** Implémenter fonctionnalités avancées + UX improvements requises pour production  
✅ **Secondary:** Atteindre >80% test coverage  
✅ **Tertiary:** Préparer déploiement production (Docker)  
✅ **Quality:** Zéro régression, tous les tests passent  

---

## 📋 Sprint 3 Stories

### **[NEW] Story S3.0: Dataset Naming** ⭐ PRIORITY
**Points:** 3 | **Epic:** 3 (Advanced Features) | **Lead:** Sally (Frontend) + Amelia (Backend)  

**Objectif:** Permettre à l'utilisateur de **définir et modifier le nom du dataset** avant sa création. Le nom doit être saisi obligatoirement, validé pour l'unicité par domaine, et les contraintes de longueur.

**Acceptance Criteria:**
- [ ] TextField "Dataset Name" ajouté au ConfigurationPanel (avant le bouton Generate)
- [ ] Validation: Min 3, Max 50 caractères
- [ ] Caractères autorisés: alphanumériques, tirets, underscores, espaces
- [ ] Bouton "Generate" DÉSACTIVÉ jusqu'à nom valide
- [ ] Endpoint Check-Name: `GET /api/domains/{domainId}/datasets/check-name?name={name}`
- [ ] Validation d'unicité par domaine en base de données
- [ ] DataSet entity: colonne `dataset_name` + UNIQUE(domain_id, dataset_name)
- [ ] Endpoint POST updated: Accept `datasetName` in request body
- [ ] Retour 409 si nom existe, 400 si format invalide
- [ ] Tests: Validation, unicité, API integration

**Technical Notes:**
- Validation regex: `^[a-zA-Z0-9_\-\s]{3,50}$`
- API check avant Generate (debounced 500ms)
- UNIQUE constraint + INDEX sur (domain_id, dataset_name)
- Error handling: 400 (format), 409 (duplicate)

**Dependencies:** S2.3, S2.5, S2.6 (✅ SATISFIED)

**Timeline:** Day 1-2 (parallel Sally/Amelia)

---

### **Story S3.1: Activity Tracking Service** 
**Points:** 5 | **Epic:** 3 (Advanced Features) | **Lead:** Amelia (Backend)  

**Objectif:** Implémenter service pour tracker les activités sur les jeux de données (statuts, historique, versioning).

**Acceptance Criteria:**
- [ ] ActivityService créé pour enregistrer événements
- [ ] Statuts implémentés: downloaded, modified, viewed
- [ ] Activity entity créé (dataSetId, action, timestamp, user)
- [ ] Original dataset copie conservée (versioning)
- [ ] Reset endpoint: /api/data-sets/{id}/reset
- [ ] Activity query endpoint: /api/data-sets/{id}/activity
- [ ] Tests JUnit >80% coverage
- [ ] Aucune perte de données d'activité

**Technical Notes:**
- Activity enregistrée à chaque action
- Original stocké avec version=0, actuel avec version=N
- Index sur dataSetId, timestamp pour performance
- Soft delete possible avec delete_at timestamp

**Dependencies:** S2 Core APIs (✅ SATISFIED)

---

### **Story S3.2: Spring Batch for Batch Generation**
**Points:** 6 | **Epic:** 3 (Advanced Features) | **Lead:** Amelia (Backend)  

**Objectif:** Implémenter Spring Batch pour permettre génération de plusieurs jeux de données en parallèle.

**Acceptance Criteria:**
- [ ] BatchConfiguration créée avec Job, Step
- [ ] JobLauncher exposé en endpoint /api/batch/generate
- [ ] Request body: {dataSetConfigs: [{domainId, columns, count}]}
- [ ] Parallelisation: 4 workers parallèles
- [ ] JobRepository H2 pour tracking
- [ ] Status tracking: Pending → Running → Completed/Error
- [ ] Performance: 10 jeux de 1000 lignes < 10 secondes
- [ ] Error handling avec retry logic
- [ ] Tests intégrés Spring Batch

**Technical Notes:**
- Spring Batch ItemReader/Processor/Writer pattern
- ThreadPoolExecutor pour parallélisation
- JobParameters pour configuration dynamique
- ChunkSize = 500 lignes pour optimisation

**Dependencies:** S2 Generation APIs (✅ SATISFIED)

---

### **Story S3.3: WebSocket Notifications for Batch Jobs**
**Points:** 5 | **Epic:** 3 (Advanced Features) | **Lead:** Amelia (Backend) + Sally (Frontend)  

**Objectif:** Implémenter WebSocket pour envoyer notifications temps réel aux clients lors de progression/completion des batch jobs.

**Acceptance Criteria - Backend:**
- [ ] WebSocket configuration avec Spring Websocket
- [ ] Endpoint: /ws/batch-notifications
- [ ] Messages envoyés: job_started, job_progress, job_completed, job_error
- [ ] STOMP protocol supporté
- [ ] Chaque client reçoit notifications de ses jobs

**Acceptance Criteria - Frontend:**
- [ ] WebSocket client établissant connexion
- [ ] NotificationPanel affichant jobs en progress
- [ ] Real-time progress bar
- [ ] Toast notifications pour completion/errors
- [ ] Déconnexion/reconnexion gérée

**Technical Notes:**
- STOMP simplifie WebSocket
- Chaque job = topic /topic/job/{jobId}
- Client subscribe après batch request
- Heartbeat pour keepalive

**Dependencies:** S3.1 & S3.2 (Activity + Batch)

**Implementation Sequence:** S3.1 → S3.2 → S3.3 (serié)

---

### **Story S3.4: End-to-End Testing & Test Automation**
**Points:** 6 | **Epic:** 3 (Advanced Features) | **Lead:** Quinn (QA)  

**Objectif:** Implémenter tests E2E complets couvrant flux utilisateur principal et cas d'erreur.

**Acceptance Criteria:**
- [ ] Tests E2E framework (Cypress ou Playwright)
- [ ] 4 Scénarios couverts:
  1. Créer domain → Upload CSV → Générer données → Download JSON ✅
  2. Error case: Upload invalid CSV → Handle error ✅
  3. Batch generation: Soumettre 3 jeux + track progress ✅
  4. Activity tracking: Vérifier statuts après actions ✅
- [ ] Coverage global >80% (backend code, frontend components)
- [ ] Performance tests: 10k lignes génération < 5s
- [ ] Smoke tests avant deploy
- [ ] Tests automatisés en CI/CD (GitHub Actions)

**Technical Notes:**
- E2E tests lancer avec npm test:e2e
- Mock API optionnel pour tests rapides
- Performance metriques en logs
- Test data (sample CSVs) inclus

**Dependencies:** S3.1 & S3.2 (Backend features mature)

**Parallel Possible:** Can run in parallel with S3.3

---

### **Story S3.5: Docker Deployment & Final Documentation**
**Points:** 6 | **Epic:** 3 (Advanced Features) | **Lead:** Amelia (DevOps)  

**Objectif:** Créer Dockerfiles, docker-compose et documenter déploiement + API complètement.

**Acceptance Criteria:**
- [ ] Dockerfile.backend (openjdk:17, Maven build, port 8080)
- [ ] Dockerfile.frontend (npm build, nginx, port 3000)
- [ ] docker-compose.yml orchestrant backend, frontend, H2, network
- [ ] README.md complet avec:
  - Installation instructions
  - Running locally (npm start, mvn spring-boot:run)
  - Docker deployment (docker-compose up)
  - API documentation link
- [ ] OpenAPI/Swagger docs complètes
- [ ] Startup scripts fonctionnels
- [ ] Deploy en un seul `docker-compose up`

**Technical Notes:**
- docker-compose v3+
- Environment variables pour config
- Volume mounts pour persistence H2
- Health checks configurés

**Dependencies:** S3.1, S3.2, S3.3, S3.4 (All features implemented)

**Load:** Parallel with final testing

---

## 📈 Sprint Velocity & Timeline

**Team Velocity:** ~5 pts/jour/personne  
**Team:** Amelia (Backend Lead, 19 pts) + Sally (Frontend, 8 pts) + Quinn (QA, 6 pts)

**Estimated Timeline:**
- **Week 1 (02/03 - 06/03):** S3.0 (Dataset Naming) + S3.1 Start (Activity) - 3+2 pts = 5 pts
- **Week 2 (09/03 - 13/03):** S3.1 Complete (Activity Tracking) + S3.2 Start (Batch) - 3+4 pts = 7 pts
- **Week 3 (16/03 - 20/03):** S3.2 Complete (Batch) + S3.3 Start (WebSocket) - 2+3 pts = 5 pts
- **Week 4 (23/03 - 27/03):** S3.3 Complete + S3.4 (E2E Testing) - 2+4 pts = 6 pts
- **Week 5 (30/03 - 03/04):** S3.4 Complete + S3.5 Start (Docker) - 2+3 pts = 5 pts
- **Week 6 (06/04 - 13/04):** S3.5 Complete + Testing, Validation, Fixes, Production Ready - 3 pts

**Critical Path:** S3.0 → S3.1 → S3.2 → S3.3 (Series dependency)

---

## 🎯 Team Assignments

| Story | Lead | Support | Est. Days | Priority |
|-------|------|---------|-----------|----------|
| S3.0 Dataset Naming | Sally+Amelia | - | 1-2 days | 🔴 HIGH |
| S3.1 Activity Tracking | Amelia | - | 3-4 days | NORMAL |
| S3.2 Spring Batch | Amelia | - | 4 days | NORMAL |
| S3.3 WebSocket | Amelia | Sally | 3 days | NORMAL |
| S3.4 E2E Testing | Quinn | - | 4 days | NORMAL |
| S3.5 Docker & Docs | Amelia | - | 3 days | NORMAL |

**Parallelization Opportunities:**
- S3.0 (Naming) can run in parallel with team prep
- S3.3 (WebSocket Frontend) can start when S3.2 (Batch) is in review
- S3.4 (E2E Testing) can run in parallel with S3.3
- S3.5 (Docker) can run in parallel with final S3.3/S3.4 testing

---

## ✅ Dependencies & Blockers

**From Sprint 2:**
- ✅ DataGeneratorService (S2.1)
- ✅ Type Detection (S2.2)
- ✅ Generation API (S2.3)
- ✅ JSON Export (S2.4)
- ✅ CSV Upload UI (S2.5)
- ✅ Configuration UI (S2.6)
- ✅ Data Viewer (S2.7)
- ✅ POST-SPRINT-2 CORRECTIONS (uploadedData + payload format) - **VALIDATED 02/03 @ 07:30 CET**

**Within Sprint 3:**
- S3.0 → S3.1 (Dataset Naming needed before Activity)
- S3.1 → S3.2 (Activity needed before Batch)
- S3.2 → S3.3 (Batch needed before WebSocket notifications)
- S3.3, S3.4 → S3.5 (Features needed complete for Docker)

**Known Blockers:** None identified  
**Risk Factors:** 
- Spring Batch learning curve for Amelia (mitigate: reference implementations)
- WebSocket integration complexity (mitigate: use STOMP library)
- E2E test framework setup (mitigate: use standard Cypress)

---

## 📊 Acceptance Criteria Validation

**Sprint 3 Success Criteria:**

| Criteria | Target | Status |
|----------|--------|--------|
| All stories completed | 6/6 | 🔄 IN PROGRESS |
| All ACs met | 100% | 🔄 IN PROGRESS |
| Tests passing | 100% | 🔄 IN PROGRESS |
| Test coverage | >80% | 🔄 IN PROGRESS |
| Performance targets | Met | 🔄 IN PROGRESS |
| No critical bugs | 0 | ✅ BASELINE |
| API documentation | Complete | 🔄 IN PROGRESS |
| Docker deployment | Working | 🔄 IN PROGRESS |
| Production ready | YES | 🔄 IN PROGRESS |

---

## 📋 Next Actions

**For Amelia (Backend Lead):**
```
[ ] Read this planning summary completely
[ ] Review S3.0 Dataset Naming (new priority HIGH story!)
[ ] Review S3.1 acceptance criteria
[ ] Coordinate with Sally on S3.0 API endpoint
[ ] Start Dataset Naming implementation
[ ] Update daily progress in sprint-status.yaml
```

**For Sally (Frontend Lead):**
```
[ ] Read this planning summary completely
[ ] Review S3.0 Dataset Naming (priority HIGH - start immediately!)
[ ] Add TextField to ConfigurationPanel
[ ] Implement validation logic + tests
[ ] Coordinate with Amelia on check-name API
[ ] Then Review S3.3 WebSocket spec
[ ] Prepare NotificationPanel mockups
```

**For Quinn (QA):**
```
[ ] Review E2E test scenarios (S3.4)
[ ] Select E2E framework (Cypress vs Playwright)
[ ] Prepare test environment setup
```

**For Bob (Scrum Master):**
```
[ ] Monitor daily progress
[ ] Facilitate communication between team members
[ ] Update sprint status after each story completion
[ ] Conduct sprint review at end of sprint
```

---

## 📚 Reference Documents

- **Sprint Planning Details:** [S3 Stories](./sprint-3-started/stories.md)
- **Sprint Status Tracker:** [sprint-status.yaml](../sprint-status.yaml)
- **Sprint 2 Closure Report:** [SPRINT-2-CLOSURE-REPORT.md](../SPRINT-2-CLOSURE-REPORT.md)
- **Post-Sprint-2 Corrections:** [POST-SPRINT-2-CORRECTIONS-2026-03-02.md](../sprint-2-ended/POST-SPRINT-2-CORRECTIONS-2026-03-02.md)

---

**Sprint 3 Status:** ✅ PLANNING COMPLETE - Ready for Implementation Kick-off  
**Last Updated:** 02 mars 2026 @ 08:45 CET (Added S3.0 Dataset Naming - Priority HIGH)  
**Next Review:** Kickoff meeting + Daily standups start 02/03/2026
