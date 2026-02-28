---
sprint: 3
totalStories: 5
totalPoints: 28
---

# Sprint 3 User Stories

## Story S3.1 : Implement Activity Tracking Service

**Points :** 5  
**Epic :** EPIC 1 : Foundation & Core MVP  
**Type :** Backend Feature

### Description
Implémenter service pour tracker les activités sur les jeux de données (statuts, historique, versioning).

### Acceptance Criteria
- [ ] ActivityService créé pour enregistrer événements
- [ ] Statuts implémentés :
  - downloaded : Jeu téléchargé
  - modified : Jeu modifié depuis création
  - viewed : Jeu consulté au moins une fois
- [ ] Activity entity créé (dataSetId, action, timestamp, user)
- [ ] Original dataset copie conservée (versioning)
- [ ] Reset endpoint : /api/data-sets/{id}/reset remplace par original
- [ ] Activity query endpoint : /api/data-sets/{id}/activity
- [ ] Tests JUnit pour service (>80% coverage)
- [ ] Aucune perte de données d'activité

### Technical Notes
- Activity enregistrée à chaque action (create, download, modify)
- Original stocké avec version=0, actuel avec version=N
- Soft delete possible avec delete_at timestamp
- Requêtes optimisées (index sur dataSetId, timestamp)

### Tâches
- Créer Activity entity
- Créer ActivityService
- Ajouter listeners événements
- Implémenter reset logic
- Écrire tests JUnit

---

## Story S3.2 : Implement Spring Batch for Batch Generation

**Points :** 6  
**Epic :** EPIC 3 : Advanced Features & Scalability  
**Type :** Backend Feature

### Description
Implémenter Spring Batch pour permettre génération de plusieurs jeux de données en parallèle.

### Acceptance Criteria
- [ ] BatchConfiguration créée avec Job, Step
- [ ] JobLauncher exposé en endpoint /api/batch/generate
- [ ] Request body : {dataSetConfigs: [{domainId, columns, count}]}
- [ ] Parallelisation : 4 workers parallèles
- [ ] JobRepository H2 pour tracking
- [ ] Status tracking : Pending → Running → Completed/Error
- [ ] Performance : 10 jeux de 1000 lignes < 10 secondes
- [ ] Error handling avec retry logic
- [ ] Tests intégrés Batch

### Technical Notes
- Spring Batch ItemReader/Processor/Writer pattern
- ThreadPoolExecutor pour parallélisation
- JobParameters pour configuration dynamique
- ChunkSize = 500 lignes pour optimisation

### Tâches
- Configurer Spring Batch
- Implémenter Job/Step
- Ajouter parallélisation
- Créer ItemReader/Writer
- Tester performance

---

## Story S3.3 : WebSocket Notifications for Batch Jobs

**Points :** 5  
**Epic :** EPIC 3 : Advanced Features & Scalability  
**Type :** Backend & Frontend Feature

### Description
Implémenter WebSocket pour envoyer notifications temps réel aux clients lors de progression/completion des batch jobs.

### Acceptance Criteria
**Backend :**
- [ ] WebSocket configuration avec Spring Websocket
- [ ] Endpoint : /ws/batch-notifications
- [ ] Messages envoyés :
  - {type: "job_started", jobId, timestamp}
  - {type: "job_progress", jobId, completed, total, percentage}
  - {type: "job_completed", jobId, status, rowsGenerated}
  - {type: "job_error", jobId, error}
- [ ] STOMP protocol supporté
- [ ] Chaque client reçoit notifications de ses jobs

**Frontend :**
- [ ] WebSocket client établissant connexion
- [ ] NotificationPanel affichant jobs en progress
- [ ] Real-time progress bar
- [ ] Toast notifications pour completion/errors
- [ ] Déconnexion/reconnexion gérée

### Technical Notes
- STOMP simplifie WebSocket
- Chaque job = topic /topic/job/{jobId}
- Client subscribe après batch request
- Heartbeat pour keepalive

### Tâches
- Configurer Spring Websocket
- Implémenter message handlers backend
- Créer WebSocket client frontend
- Implémenter NotificationPanel
- Tester connexion/déconnexion

---

## Story S3.4 : End-to-End Testing & Test Automation

**Points :** 6  
**Epic :** EPIC 1 : Foundation & Core MVP  
**Type :** QA/Testing

### Description
Implémenter tests E2E complets couvrant flux utilisateur principal et cas d'erreur.

### Acceptance Criteria
- [ ] Tests E2E framework (Cypress ou Playwright)
- [ ] Scénarios couverts :
  1. Créer domain → Upload CSV → Générer données → Download JSON ✅
  2. Error case : Upload invalid CSV → Handle error ✅
  3. Batch generation : Soumettre 3 jeux + track progress ✅
  4. Activity tracking : Vérifier statuts après actions ✅
- [ ] Coverage global >80% (backend code, frontend components)
- [ ] Performance tests : 10k lignes génération < 5s
- [ ] Smoke tests avant deploy
- [ ] Tests automatisés en CI/CD (GitHub Actions)

### Technical Notes
- E2E tests lancer avec npm test:e2e
- Mock API optionnel pour tests rapides
- Performance metriques en logs
- Test data (sample CSVs) inclus

### Tâches
- Setup Cypress/Playwright
- Écrire test scenarios
- Implémenter page objects
- Ajouter coverage reports
- Setup CI/CD tests

---

## Story S3.5 : Docker Deployment & Final Documentation

**Points :** 6  
**Epic :** EPIC 1 : Foundation & Core MVP  
**Type :** DevOps & Documentation

### Description
Créer Dockerfiles, docker-compose et documenter déploiement + API complètement.

### Acceptance Criteria
- [ ] Dockerfile.backend pour Spring Boot
  - Base : openjdk:17
  - Build Maven → Run container
  - Port 8080 exposé
- [ ] Dockerfile.frontend pour React
  - Build : npm build
  - Serve : nginx
  - Port 3000 exposé
- [ ] docker-compose.yml orchestrant :
  - backend service
  - frontend service
  - H2 database
  - Network commun
- [ ] README.md complet avec :
  - Installation instructions
  - Running locally (npm start, mvn spring-boot:run)
  - Docker deployment (docker-compose up)
  - API documentation link
- [ ] OpenAPI/Swagger docs complètes
- [ ] startup scripts fonctionnels
- [ ] Deploy en un seul `docker-compose up`

### Technical Notes
- docker-compose v3+
- Environment variables pour config
- Volume mounts pour persistence H2
- Health checks configurés
- README avec quick-start

### Tâches
- Créer Dockerfiles
- Créer docker-compose.yml
- Écrire README.md
- Finaliser OpenAPI docs
- Tester local Docker deploy

---

## Résumé Sprint 3

**Total Points :** 28  
**Estimation effort :** ~5 pts/jour / personne = 28 / 5 = 5-6 jours (avec buffer)

| Story | Points | Assigné | Type |
|-------|--------|---------|------|
| S3.1 Activity Tracking | 5 | Amelia | Backend |
| S3.2 Spring Batch | 6 | Amelia | Backend |
| S3.3 WebSocket Notifications | 5 | Amelia+Sally | Full-stack |
| S3.4 E2E Testing | 6 | Quinn | QA |
| S3.5 Docker & Docs | 6 | Amelia | DevOps |

**État :** Ready (dépend Sprint 1-2)

---

## MVP Completion après Sprint 3

✅ **Toutes les exigences Phase 1 PRD couverte :**
- Génération de données (3 typologies)
- Interface utilisateur (MUI, responsive)
- APIs CRUD
- Suivi d'activité
- Génération par lots (batch)
- Tests (>80% coverage)
- Documentation
- Déploiement (Docker)

**Prochaines phases :**
- Phase 2 : APIs avancées, plugins typologies, scalabilité
- Phase 3 : Microservices, IA pour matching, modes avancés

---

Retour à [Sprint 3 Kickoff](kickoff-summary.md)
