# Story S3.5: Docker Deployment & Final Documentation (LIGHT)

**Sprint:** Sprint 3  
**Points:** 4 (LIGHT scope, -2 from baseline 6)  
**Epic:** EPIC 3 - Advanced Features & Scalability  
**Type:** DevOps & Documentation  
**Lead:** Amelia  
**Status:** Backlog  
**Dependencies:** S3.1, S3.2, S3.3, S3.4 (All features implemented)  

---

## 📋 Objectif

Créer Dockerfiles pour backend et frontend, docker-compose pour orchestration, et documentation finale pour déploiement et API. **LIGHT scope** inclut: basic containerization, docker-compose setup, health checks, et README complet. Exclut: stage builds, Kubernetes, CI/CD automation, advanced monitoring.

---

## ✅ Acceptance Criteria

### Dockerfile Backend
- [ ] Fichier: `Dockerfile` (dans root du projet)
- [ ] Base image: `openjdk:17`
- [ ] Build:
  - Copy source files
  - Run Maven build: `mvn clean package -DskipTests`
  - Final stage: copy JAR from build
- [ ] Runtime:
  - Expose port 8080
  - Default profile: dev
  - Health check configured
- [ ] Size: <500MB (optimized)

**Dockerfile.backend:**
```dockerfile
FROM openjdk:17-jdk-slim as builder
WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=20s --retries=3 \
  CMD java -jar /app/app.jar actuator health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]
```

### Dockerfile Frontend
- [ ] Fichier: `Dockerfile.frontend` (dans root du projet)
- [ ] Build stage: Node 18 + npm build
  - Copy package.json, install deps
  - Run: `npm run build`
- [ ] Runtime stage: nginx
  - Base: `nginx:alpine` (lightweight)
  - Copy built artifacts to /usr/share/nginx/html
  - Expose port 3000 (or 80)
  - nginx config: proxy to backend
- [ ] Size: <100MB (optimized)

**Dockerfile.frontend:**
```dockerfile
# Build stage
FROM node:18-alpine as builder
WORKDIR /app
COPY movkfact-frontend/package*.json ./
RUN npm ci
COPY movkfact-frontend/src ./src
COPY movkfact-frontend/public ./public
RUN npm run build

# Runtime stage
FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### nginx.conf (for frontend proxy)
- [ ] Serve static files from `/usr/share/nginx/html`
- [ ] Proxy `/api` requests to backend:8080
- [ ] Gzip compression enabled
- [ ] Browser caching configured

**nginx.conf:**
```nginx
server {
    listen 80;
    server_name localhost;
    
    root /usr/share/nginx/html;
    
    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
    
    # Browser cache
    location ~* \.(js|css|png|jpg|jpeg|gif|icon)$ {
        expires 31d;
        add_header Cache-Control "public, immutable";
    }
    
    # API proxy
    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # WebSocket proxy
    location /ws {
        proxy_pass http://backend:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
    
    # SPA fallback
    try_files $uri $uri/ /index.html =404;
}
```

### docker-compose.yml
- [ ] Fichier: `docker-compose.yml` (dans root du projet)
- [ ] Services:
  - **backend:** Spring Boot (port 8080)
  - **frontend:** nginx (port 3000)
  - **database:** H2 (ou optionnel: PostgreSQL)
- [ ] Environment variables:
  - Backend: SPRING_PROFILES_ACTIVE=docker
  - Database: H2 persistence
- [ ] Networking: Custom network `movkfact-network`
- [ ] Volumes:
  - Optional: H2 database persistence
- [ ] Health checks configured
- [ ] Startup order: database → backend → frontend

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: movkfact-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:h2:./h2db/movkfact
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.h2.Driver
    volumes:
      - ./h2db:/app/h2db
    networks:
      - movkfact-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 20s
    depends_on:
      - database

  frontend:
    build:
      context: .
      dockerfile: Dockerfile.frontend
    container_name: movkfact-frontend
    ports:
      - "3000:80"
    networks:
      - movkfact-network
    depends_on:
      backend:
        condition: service_healthy

  database:
    image: h2-db:latest
    container_name: movkfact-h2
    ports:
      - "8082:8082"
    environment:
      H2_OPTIONS: "-tcp -tcpAllowOthers -tcpPort 8082"
    volumes:
      - h2-data:/h2-data
    networks:
      - movkfact-network

volumes:
  h2-data:

networks:
  movkfact-network:
    driver: bridge
```

### README.md (Final Documentation)
- [ ] File: `README.md` (update existing or create new)
- [ ] Sections:
  1. **Project Overview**
  2. **Quick Start (Local)**
  3. **Quick Start (Docker)**
  4. **Development Setup**
  5. **API Documentation**
  6. **Architecture**
  7. **Testing**
  8. **Deployment**
  9. **Troubleshooting**
  10. **Contributing**

**README structure:**
```markdown
# MovKFact - Data Generation Platform

## 📋 Overview
Short description of the project, features, and technology stack.

## 🚀 Quick Start (Local Development)

### Prerequisites
- Java 17
- Node 18+
- Maven 3.8+
- Git

### Install & Run
\`\`\`bash
# Backend
./mvnw spring-boot:run

# Frontend (new terminal)
cd movkfact-frontend
npm install
npm start
\`\`\`

### Access Application
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- API Docs: http://localhost:8080/swagger-ui.html

## 🐳 Quick Start (Docker)

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+

### Run with Docker

\`\`\`bash
# Start all services
docker-compose up

# Wait for services to start (~30 seconds)
# Access application: http://localhost:3000
\`\`\`

### Stop Services
\`\`\`bash
docker-compose down
\`\`\`

## 📚 API Documentation

### Base URL
- Development: http://localhost:8080/api
- Production: [production-url]/api

### Key Endpoints

#### Domains
- GET /api/domains - List all domains
- POST /api/domains - Create domain
- GET /api/domains/{id} - Get domain details
- PUT /api/domains/{id} - Update domain
- DELETE /api/domains/{id} - Delete domain

#### Datasets
- GET /api/data-sets - List datasets
- POST /api/data-sets - Generate dataset
- GET /api/data-sets/{id} - Get dataset
- GET /api/data-sets/{id}/activity - Get activity history
- GET /api/data-sets/{id}/reset - Reset dataset
- POST /api/batch/generate - Batch generation

#### Full API Documentation
See [Swagger UI](http://localhost:8080/swagger-ui.html) for complete API reference.

## 🏗️ Architecture

### Tech Stack
- **Backend:** Spring Boot 3.x, H2 Database, Spring Batch
- **Frontend:** React 18, Material-UI 5
- **DevOps:** Docker, Docker Compose

### Project Structure
\`\`\`
movkfact/
├── src/maimjava/com/movfact/
│   ├── controller/       # REST Controllers
│   ├── service/          # Business Logic
│   ├── entity/           # JPA Entities
│   ├── repository/       # Data Access
│   └── config/           # Spring Configuration
├── movkfact-frontend/
│   ├── src/
│   │   ├── components/   # React Components
│   │   ├── services/     # API Services
│   │   ├── theme/        # Material-UI Theme
│   │   └── pages/        # Page Components
│   └── public/           # Static Assets
├── Dockerfile            # Backend Container
├── Dockerfile.frontend   # Frontend Container
├── docker-compose.yml    # Orchestration
└── nginx.conf            # Frontend Proxy
\`\`\`

## 🧪 Testing

### Backend Tests
\`\`\`bash
./mvnw clean test              # Run all tests
./mvnw clean test -DskipTests  # Skip tests
./mvnw clean package           # Package for production
\`\`\`

### Frontend Tests
\`\`\`bash
cd movkfact-frontend
npm test                    # Run Jest tests
npm run test:e2e            # Run Cypress E2E tests
npm run test:e2e:headless   # Run E2E in headless mode
npm run build               # Build for production
\`\`\`

## 📊 Deployment

### Local Docker Deployment
\`\`\`bash
# Build and run
docker-compose up --build

# View logs
docker-compose logs -f

# Stop services
docker-compose down -v  # -v removes volumes
\`\`\`

### Production Deployment
[Coming Soon] - Kubernetes/Cloud Platform specific instructions

## 🔧 Troubleshooting

### Backend not starting
- Check port 8080 is available: \`lsof -i :8080\`
- Check Java version: \`java -version\` (should be 17+)
- View logs: \`docker-compose logs backend\`

### Frontend not loading
- Check port 3000 is available: \`lsof -i :3000\`
- Clear browser cache and reload
- View logs: \`docker-compose logs frontend\`

### WebSocket connection errors
- Ensure backend healthy: http://localhost:8080/actuator/health
- Check browser console for connection errors
- Verify nginx proxy configuration

### Database issues
- H2 data persists in ./h2db directory
- To reset database: \`rm -rf ./h2db\`

## 📝 Contributing

See [Contributing Guidelines](CONTRIBUTING.md)

## 📄 License

MIT License - See LICENSE.md

## 📞 Support

For issues, see [Issues](../../issues)
```

### Health Check Endpoints
- [ ] Backend: `/actuator/health` (Spring Boot Actuator)
- [ ] Frontend: Health via HTTP status 200
- [ ] docker-compose health checks configured

### Startup Scripts (Optional)
- [ ] Script: `start.sh` - Start all services locally
- [ ] Script: `docker-build.sh` - Build Docker images
- [ ] Script: `docker-run.sh` - Run Docker containers

**start.sh:**
```bash
#!/bin/bash

echo "Starting MovKFact..."

# Start backend
./mvnw -q spring-boot:run &
BACKEND_PID=$!

echo "Backend started (PID: $BACKEND_PID)"
sleep 5

# Start frontend
cd movkfact-frontend
npm start &
FRONTEND_PID=$!

echo "Frontend started (PID: $FRONTEND_PID)"
echo "Application ready at http://localhost:3000"

# Wait for both processes
wait $BACKEND_PID $FRONTEND_PID
```

### API Documentation (Swagger/OpenAPI)
- [ ] Enable Swagger UI: already configured in Spring Boot
- [ ] Endpoint: `http://localhost:8080/swagger-ui.html`
- [ ] OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- [ ] Document all endpoints with @Operation annotations

### Environment Configuration
- [ ] Support for profiles: `dev`, `docker`, `test`, `prod`
- [ ] Environment variables overridable via:
  - `application.yml`
  - `application-{profile}.yml`
  - Docker ENV
- [ ] Key variables documented in README

### Database Persistence (Docker)
- [ ] H2 database volme: `h2-data`
- [ ] Persists across container restarts
- [ ] Optional backup script for h2db files

---

## 📊 Tâches

| # | Tâche | Assigné | Durée | Dépend de |
|---|-------|---------|-------|-----------|
| 1 | Create Dockerfile (backend) | Amelia | 1h | - |
| 2 | Create Dockerfile.frontend (frontend) | Amelia | 1h | - |
| 3 | Create nginx.conf for proxy | Amelia | 0.75h | T2 |
| 4 | Create docker-compose.yml | Amelia | 1h | T1, T2 |
| 5 | Test local Docker deployment | Amelia | 1.5h | T4 |
| 6 | Create/update README.md | Amelia | 2h | T5 |
| 7 | Create startup scripts (start.sh, docker-*.sh) | Amelia | 1h | T6 |
| 8 | Verify Swagger/API docs | Amelia | 0.5h | T6 |
| 9 | Document environment configuration | Amelia | 0.75h | T8 |
| 10 | Test health checks | Amelia | 0.5h | T5 |
| 11 | Final review & optimization | Amelia | 1h | T10 |

**Durée Totale Estimée:** 11 heures (~1.4 jours)

---

## 🔗 Dependencies

**From Sprint 3:**
- S3.1, S3.2, S3.3, S3.4 (All features must be implemented and stable)

---

## 📈 Definition of Done

- [ ] Dockerfile.backend created and tested
- [ ] Dockerfile.frontend created and tested
- [ ] nginx.conf configured correctly
- [ ] docker-compose.yml fully functional
- [ ] Local Docker deployment works: `docker-compose up`
- [ ] Health checks passing
- [ ] README.md complete with all sections
- [ ] API documentation (Swagger) accessible
- [ ] Startup scripts functional
- [ ] Environment configuration documented
- [ ] Code reviewed et mergé
- [ ] No console errors/warnings on startup

---

## 🚀 Implementation Strategy

1. **Phase 1 - Dockerfiles (Day 1, 2h):** Backend & Frontend Dockerfiles
2. **Phase 2 - Compose & Proxy (Day 1, 1.75h):** docker-compose.yml, nginx.conf
3. **Phase 3 - Testing & Scripts (Day 1-2, 2.5h):** Local deployment, startup scripts
4. **Phase 4 - Documentation (Day 2, 2.75h):** README, API docs, environment config
5. **Phase 5 - Final Validation (Day 2, 2h):** Full testing, optimization

---

## 📚 Reference

- **Docker Docs:** https://docs.docker.com/
- **Docker Compose:** https://docs.docker.com/compose/
- **Spring Boot Docker:** https://spring.io/guides/gs/spring-boot-docker/
- **React Docker:** https://docs.docker.com/language/nodejs/
- **Sprint Planning:** [SPRINT-3-PLANNING-SUMMARY.md](SPRINT-3-PLANNING-SUMMARY.md)

---

## ⚠️ LIGHT Scope Notes

**Included:**
- ✅ Basic containerization (backend/frontend)
- ✅ docker-compose orchestration
- ✅ Health checks
- ✅ Final documentation & README

**Excluded (for future sprints):**
- ❌ Multi-stage advanced builds (just use FROM multi-stage)
- ❌ Kubernetes deployment
- ❌ CI/CD Docker automation (handled in S3.4 GitHub Actions)
- ❌ Advanced monitoring (Prometheus/Grafana)
- ❌ Load balancing (NGINX advanced configs)
- ❌ SSL/TLS certificates (for production)

---

**Status:** Ready for Implementation  
**Priority:** NORMAL  
**Scope:** LIGHT (4 pts, -2 from baseline)  
**Created:** 02 mars 2026
