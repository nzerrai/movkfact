# Story S3.5b: Docker Deployment & Documentation (reprise)

**Sprint:** Sprint 7
**Points:** 4
**Epic:** EPIC 10 — Quality Gate & Déploiement
**Type:** DevOps + Documentation
**Lead:** Amelia
**Status:** Backlog
**Reprend :** S3.5 (skippé Sprint 3) + S5-Docker (skippé Sprint 5)
**FRs couverts :** NFR §4.3 (déploiement), §5 (stack PostgreSQL)

---

## User Story

**En tant que** Tech Lead ou DevOps engineer,
**Je veux** pouvoir déployer l'application complète en une seule commande `docker-compose up`,
**Afin de** disposer d'un environnement reproductible intégrant backend, frontend et PostgreSQL.

---

## Contexte métier

La story S3.5 avait été skippée en Sprint 3 (contrainte temps). Depuis, PostgreSQL a remplacé H2 (S4.2), ce qui change le docker-compose. Cette story livre le déploiement containerisé **production-ready** avec la stack PostgreSQL complète et la documentation finale de l'API.

---

## ✅ Acceptance Criteria

### AC1 — Dockerfile Backend
- [ ] `Dockerfile.backend` : multi-stage build (Maven build → openjdk:17-slim runtime)
- [ ] Port 8080 exposé, variable `SPRING_PROFILES_ACTIVE` configurable
- [ ] Health check configuré : `curl -f http://localhost:8080/actuator/health`
- [ ] Image finale < 300MB

### AC2 — Dockerfile Frontend
- [ ] `Dockerfile.frontend` : multi-stage (node:18 build → nginx:alpine serve)
- [ ] Port 80 exposé
- [ ] `nginx.conf` configuré pour SPA (fallback `index.html` pour React Router)
- [ ] Variables d'environnement `REACT_APP_API_URL` injectables au build

### AC3 — docker-compose.yml
- [ ] Services : `backend`, `frontend`, `postgres` (image `postgres:15-alpine`)
- [ ] PostgreSQL : variables `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` depuis `.env`
- [ ] Backend attend PostgreSQL healthy (health check + `depends_on: condition: service_healthy`)
- [ ] Frontend attend Backend (depends_on)
- [ ] Volume persistant pour les données PostgreSQL (`postgres_data`)
- [ ] Réseau interne partagé (`movkfact_net`)
- [ ] `docker-compose up --build` démarre l'application complète en < 2min

### AC4 — Fichier `.env.example`
- [ ] `.env.example` fourni avec toutes les variables requises commentées
- [ ] `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- [ ] `JWT_SECRET`, `SPRING_PROFILES_ACTIVE`
- [ ] `REACT_APP_API_URL`

### AC5 — README.md
- [ ] Sections : Prérequis, Installation, Lancement local (dev), Lancement Docker, API documentation
- [ ] Commandes exactes testées et fonctionnelles
- [ ] Lien vers Swagger UI (`http://localhost:8080/swagger-ui.html`)
- [ ] Architecture overview (1 diagramme ASCII)

### AC6 — Documentation API Swagger
- [ ] Swagger UI accessible en dev et prod
- [ ] Tous les endpoints documentés (Domains, DataSets, Rows, Batch, Preview, Auth)
- [ ] Exemples de requêtes/réponses pour les endpoints principaux

### AC7 — Smoke test automatisé *(ajouté party mode 04/03)*
- [ ] Script `smoke-test.sh` créé à la racine du projet
- [ ] Teste 3 appels API séquentiels : `POST /api/domains` → `GET /api/domains` → `GET /actuator/health`
- [ ] Script retourne exit code 0 si tous les appels répondent 2xx, exit code 1 sinon
- [ ] Documenté dans README.md section "Vérification post-déploiement"
- [ ] `docker-compose up --build` sur machine propre sans erreur
- [ ] Flyway migrations V001–V006 appliquées au démarrage

---

## 🏗️ Spécifications Techniques

### Nouveaux fichiers à créer

```
/
  Dockerfile.backend
  Dockerfile.frontend
  docker-compose.yml
  docker-compose.dev.yml       ← optionnel, pour dev avec hot-reload
  .env.example
  README.md
  nginx.conf                   ← config nginx pour le frontend SPA
```

### docker-compose.yml — Structure

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-movkfact}
      POSTGRES_USER: ${POSTGRES_USER:-movkfact}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-movkfact_secret}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-movkfact}"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: .
      dockerfile: Dockerfile.backend
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-movkfact}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-movkfact}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-movkfact_secret}
      JWT_SECRET: ${JWT_SECRET:-changeme_in_production_32chars_min}
    depends_on:
      postgres:
        condition: service_healthy

  frontend:
    build:
      context: ./movkfact-frontend
      dockerfile: ../Dockerfile.frontend
      args:
        REACT_APP_API_URL: ${REACT_APP_API_URL:-http://localhost:8080}
    ports:
      - "3000:80"
    depends_on:
      - backend

volumes:
  postgres_data:

networks:
  default:
    name: movkfact_net
```

### Dockerfile.backend — Multi-stage

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 📝 Dev Notes

- PostgreSQL est déjà configuré en production (S4.2 — Flyway migrations V001–V005 existantes)
- Ajouter la migration V006 (S6.1 row editor) avant ce sprint
- S'assurer que `application-prod.yml` utilise les variables d'environnement Spring (`${...}`)
- Le frontend en React nécessite que `REACT_APP_API_URL` soit défini au BUILD TIME (pas au runtime) — crucial pour Docker
- `nginx.conf` doit inclure `try_files $uri /index.html` pour React Router

---

## 📊 Estimation

| Composant | Effort | Responsable |
|-----------|--------|-------------|
| `Dockerfile.backend` (multi-stage) | 0.25j | Amelia |
| `Dockerfile.frontend` + `nginx.conf` | 0.25j | Amelia |
| `docker-compose.yml` + `.env.example` | 0.5j | Amelia |
| `README.md` (complet + testé) | 0.5j | Amelia |
| Test déploiement bout-en-bout | 0.5j | Amelia |
| **Total** | **2j** | **4 pts** |
