# Story S4.2 — Migration PostgreSQL

**Status**: done
**Sprint**: S4 (03/03–16/03/2026)
**Story Points**: 5
**Priority**: High
**Epic**: E4 — Infrastructure Production-Ready
**Assignee**: Amelia (Dev Agent)

---

## Story

En tant que développeur backend,
Je veux migrer la base de données de H2 vers PostgreSQL en production,
Afin de garantir la persistance des données, la compatibilité SQL standards, et la production-readiness du système.

---

## Acceptance Criteria

| # | Critère | Vérifié |
|---|---------|---------|
| AC1 | Driver PostgreSQL (`org.postgresql`) et Flyway Core ajoutés dans `pom.xml` | [ ] |
| AC2 | `V001__initial_schema.sql` crée la table `domain_master` en syntaxe PostgreSQL | [ ] |
| AC3 | `V002__datasets_and_columns.sql` crée les tables `datasets` et `column_configurations` | [ ] |
| AC4 | `V003__job_status.sql` crée la table `job_status` | [ ] |
| AC5 | `V004__activity_tracking.sql` réécrit en syntaxe PostgreSQL pure (pas de `AUTO_INCREMENT`, pas de `LONGTEXT`, pas de `INDEX` inline) | [ ] |
| AC6 | `application-prod.yml` configure PostgreSQL via variables d'environnement et active Flyway | [ ] |
| AC7 | `application-dev.yml` désactive Flyway explicitement (`spring.flyway.enabled: false`) | [ ] |
| AC8 | `application-dev-pg.yml` créé pour permettre le dev local avec PostgreSQL Docker | [ ] |
| AC9 | `docker-compose.yml` créé à la racine du projet avec service `postgres:15-alpine` | [ ] |
| AC10 | Spring Batch `initialize-schema: always` conservé dans les profils PostgreSQL | [ ] |
| AC11 | Tous les tests existants passent sans modification (profil `dev` H2 inchangé) | [ ] |
| AC12 | `ddl-auto: validate` actif en prod — Hibernate valide le schéma généré par Flyway | [ ] |

---

## Context — État Actuel du Projet

### Base de données actuelle
- **Dev**: H2 in-memory (`jdbc:h2:mem:movkfactdb`), `ddl-auto: create-drop`
- **Prod** (`application-prod.yml`): H2 fallback, PostgreSQL commenté, `ddl-auto: validate`
- **Flyway**: `V004__activity_tracking.sql` existe mais **Flyway n'est pas dans pom.xml** → ce fichier n'a jamais été exécuté

### Tables à créer (depuis les entités JPA)

| Entité Java | Table SQL | Fichier source |
|-------------|-----------|----------------|
| `Domain` | `domain_master` | `entity/Domain.java` |
| `DataSet` | `datasets` | `entity/DataSet.java` |
| `ColumnConfiguration` | `column_configurations` | `entity/ColumnConfiguration.java` |
| `JobStatus` | `job_status` | `entity/JobStatus.java` |
| `Activity` | `activity` | `entity/Activity.java` |

### Nommage Hibernate (SpringPhysicalNamingStrategy)
Hibernate Spring Boot convertit les champs camelCase → snake_case :
- `domainId` → `domain_id`
- `deletedAt` → `deleted_at`
- `createdAt` → `created_at`
- `columnName` → `column_name`
- etc.

### Spring Batch
Le Batch (S3.2) utilise ses propres tables de métadonnées (`BATCH_JOB_INSTANCE`, etc.).
`initialize-schema: always` fait créer ces tables automatiquement par Spring Boot (script bundlé `schema-postgresql.sql`).

---

## Tasks / Subtasks

### Task 1 — pom.xml : Ajouter les dépendances
- [ ] 1.1 Ajouter le driver PostgreSQL (`org.postgresql:postgresql`, scope `runtime`)
- [ ] 1.2 Ajouter Flyway Core (`org.flywaydb:flyway-core`, version gérée par Spring Boot BOM)

### Task 2 — Scripts de migration Flyway (PostgreSQL)
- [ ] 2.1 Créer `V001__initial_schema.sql` — table `domain_master`
- [ ] 2.2 Créer `V002__datasets_and_columns.sql` — tables `datasets` + `column_configurations`
- [ ] 2.3 Créer `V003__job_status.sql` — table `job_status`
- [ ] 2.4 Réécrire `V004__activity_tracking.sql` — syntaxe PostgreSQL (remplacer le contenu H2 existant)

### Task 3 — Mise à jour des configurations Spring Boot
- [ ] 3.1 `application-dev.yml` — ajouter `spring.flyway.enabled: false`
- [ ] 3.2 `application-prod.yml` — décommenter PostgreSQL, activer Flyway, supprimer le fallback H2
- [ ] 3.3 Créer `application-dev-pg.yml` — profil PostgreSQL local pour tests d'intégration

### Task 4 — Docker Compose
- [ ] 4.1 Créer `docker-compose.yml` à la racine du projet (service `postgres:15-alpine`)

### Task 5 — Validation
- [ ] 5.1 Lancer `mvn test` — confirmer tous les tests H2 passent (0 régression)
- [ ] 5.2 Démarrer PostgreSQL via Docker, lancer l'appli avec `--spring.profiles.active=dev-pg`, vérifier les migrations Flyway

---

## Dev Notes — Implémentation Détaillée

### 1. pom.xml — Dépendances à ajouter

Insérer **après** la dépendance `h2` existante (ligne ~64) :

```xml
<!-- PostgreSQL Driver - Production database -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Flyway - Database schema migration -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

---

### 2. Scripts de migration Flyway

**Emplacement** : `src/main/resources/db/migration/`

#### V001__initial_schema.sql — Table domain_master

```sql
-- V001__initial_schema.sql
-- Initial schema: domain_master table
-- Domain entity: entity/Domain.java

CREATE TABLE domain_master (
    id          BIGSERIAL       PRIMARY KEY,
    version     BIGINT          NOT NULL DEFAULT 0,
    name        VARCHAR(255)    NOT NULL,
    description VARCHAR(2000),
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,
    deleted_at  TIMESTAMP,
    CONSTRAINT uk_domain_name UNIQUE (name)
);

CREATE INDEX idx_domain_name       ON domain_master (name);
CREATE INDEX idx_domain_deleted_at ON domain_master (deleted_at);
```

#### V002__datasets_and_columns.sql — Tables datasets + column_configurations

```sql
-- V002__datasets_and_columns.sql
-- datasets and column_configurations tables
-- DataSet entity: entity/DataSet.java
-- ColumnConfiguration entity: entity/ColumnConfiguration.java

CREATE TABLE datasets (
    id                  BIGSERIAL       PRIMARY KEY,
    domain_id           BIGINT          NOT NULL,
    dataset_name        VARCHAR(255)    NOT NULL,
    row_count           INT             NOT NULL,
    column_count        INT,
    generation_time_ms  BIGINT          NOT NULL,
    data_json           TEXT,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,
    deleted_at          TIMESTAMP,
    version             INT             NOT NULL DEFAULT 0,
    original_data       TEXT,
    CONSTRAINT uk_domain_dataset_name UNIQUE (domain_id, dataset_name)
);

CREATE INDEX idx_domain_id          ON datasets (domain_id);
CREATE INDEX idx_deleted_at         ON datasets (deleted_at);
CREATE INDEX idx_domain_dataset_nm  ON datasets (domain_id, dataset_name);

-- Column configurations: CSV column type detection results
CREATE TABLE column_configurations (
    id              BIGSERIAL       PRIMARY KEY,
    domain_id       BIGINT          NOT NULL,
    column_name     VARCHAR(255)    NOT NULL,
    detected_type   VARCHAR(255)    NOT NULL,
    confidence      FLOAT8          NOT NULL,
    detector        TEXT,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP       NOT NULL
);
```

#### V003__job_status.sql — Table job_status

```sql
-- V003__job_status.sql
-- Persistent job status for batch jobs (S3.3)
-- JobStatus entity: entity/JobStatus.java
-- Note: job_id is not auto-generated — set by Spring Batch job ID

CREATE TABLE job_status (
    job_id          BIGINT          PRIMARY KEY,
    status          VARCHAR(50)     NOT NULL,
    progress        INT,
    completed       INT,
    total           INT,
    rows_generated  INT,
    error_count     INT,
    last_error      TEXT,
    created_at      TIMESTAMP,
    completed_at    TIMESTAMP
);
```

#### V004__activity_tracking.sql — RÉÉCRITURE pour PostgreSQL

**Remplacer intégralement le contenu existant** (syntaxe H2) par :

```sql
-- V004__activity_tracking.sql
-- Activity tracking for S3.1
-- Activity entity: entity/Activity.java
-- REWRITTEN for PostgreSQL (original used H2/MySQL syntax)

CREATE TABLE activity (
    id          BIGSERIAL       PRIMARY KEY,
    dataset_id  BIGINT          NOT NULL,
    action      VARCHAR(50)     NOT NULL,
    timestamp   TIMESTAMP       DEFAULT NOW(),
    user_name   VARCHAR(255),
    created_at  TIMESTAMP       DEFAULT NOW()
);

CREATE INDEX idx_activity_dataset   ON activity (dataset_id);
CREATE INDEX idx_activity_timestamp ON activity (timestamp);
```

> **Note** : La FK `REFERENCES datasets(id)` est omise intentionnellement — le DataSet peut être soft-deleted (deletedAt non null) sans supprimer ses activités. Une FK stricte créerait des problèmes de suppression. La cohérence est gérée par la couche service.

---

### 3. application-dev.yml — Désactiver Flyway

Ajouter dans `application-dev.yml` (sous `spring:`):

```yaml
  flyway:
    enabled: false  # H2 uses ddl-auto: create-drop, no Flyway needed in dev
```

Config complète résultante :
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:movkfactdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true

  flyway:
    enabled: false   # ← AJOUTÉ

  batch:
    job:
      enabled: false
    jdbc:
      initialize-schema: always

jwt:
  secret: ${JWT_SECRET:movkfact-dev-secret-key-change-in-production-immediately}

server:
  port: 8080
  servlet:
    context-path: /
```

---

### 4. application-prod.yml — PostgreSQL + Flyway

**Remplacer le contenu actuel** par :

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:movkfactdb}
    driver-class-name: org.postgresql.Driver
    username: ${DB_USER:movkfact_user}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    show-sql: false

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
    validate-on-migrate: true

  batch:
    job:
      enabled: false
    jdbc:
      initialize-schema: always  # Creates Spring Batch tables for PostgreSQL

  h2:
    console:
      enabled: false

jwt:
  secret: ${JWT_SECRET:change-me-to-secure-key-in-production}

server:
  port: 8080
  servlet:
    context-path: /

logging:
  level:
    root: WARN
    com.movkfact: INFO
```

**Variables d'environnement requises en prod** :
| Variable | Description | Exemple |
|----------|-------------|---------|
| `DB_HOST` | Hôte PostgreSQL | `postgres.internal` |
| `DB_PORT` | Port PostgreSQL | `5432` |
| `DB_NAME` | Nom de la base | `movkfactdb` |
| `DB_USER` | Utilisateur | `movkfact_user` |
| `DB_PASSWORD` | Mot de passe | *(secret)* |
| `JWT_SECRET` | Clé JWT | *(secret, min 32 chars)* |

---

### 5. application-dev-pg.yml — Profil PostgreSQL local (optionnel)

Créer `src/main/resources/application-dev-pg.yml` :

```yaml
# Profil PostgreSQL local pour tests d'intégration
# Usage: --spring.profiles.active=dev-pg
# Prérequis: docker-compose up -d

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/movkfactdb
    driver-class-name: org.postgresql.Driver
    username: movkfact_user
    password: movkfact_dev_password

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    show-sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true  # Safe pour re-runs sur DB existante

  h2:
    console:
      enabled: false

  batch:
    job:
      enabled: false
    jdbc:
      initialize-schema: always

jwt:
  secret: ${JWT_SECRET:movkfact-dev-pg-secret-key}

server:
  port: 8080
```

---

### 6. docker-compose.yml — Service PostgreSQL local

Créer `docker-compose.yml` à la **racine du projet** (même niveau que `pom.xml`) :

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: movkfact-postgres
    environment:
      POSTGRES_DB: movkfactdb
      POSTGRES_USER: movkfact_user
      POSTGRES_PASSWORD: movkfact_dev_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U movkfact_user -d movkfactdb"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
    name: movkfact_postgres_data
```

**Commandes de base** :
```bash
# Démarrer PostgreSQL
docker-compose up -d

# Lancer l'appli avec profil PostgreSQL
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev-pg

# Arrêter
docker-compose down

# Supprimer le volume (reset DB)
docker-compose down -v
```

---

### 7. Points d'attention techniques

#### `columnDefinition = "LONGTEXT"` dans DataSet.java
Les colonnes `dataJson` et `originalData` ont `@Column(columnDefinition = "LONGTEXT")`.
En mode `ddl-auto: validate`, Hibernate ne génère pas de DDL → cette annotation est ignorée.
Le type `TEXT` de PostgreSQL mappe correctement `String` Java.
**Aucune modification des entités nécessaire pour S4.2.**

#### Spring Batch sur PostgreSQL
`initialize-schema: always` déclenche le script `schema-postgresql.sql` bundlé dans Spring Batch.
Ce script utilise `CREATE TABLE IF NOT EXISTS` → safe pour re-runs.
Spring Batch 5.x (Spring Boot 3.2.0) est compatible PostgreSQL sans configuration additionnelle.

#### Ordre des migrations Flyway
Flyway applique les scripts dans l'ordre de version numérique :
1. `V001` → domain_master
2. `V002` → datasets, column_configurations
3. `V003` → job_status
4. `V004` → activity (pas de FK vers datasets pour éviter les contraintes soft-delete)

Vérifier avec `SELECT * FROM flyway_schema_history;` après démarrage.

#### Tests existants — 0 régression attendue
Le profil `dev` (H2) reste identique. Les 278+ tests existants utilisent H2 via `create-drop`.
La désactivation de Flyway dans `dev` garantit qu'aucun test ne brise.

---

## Definition of Done

- [ ] `pom.xml` contient les dépendances PostgreSQL + Flyway
- [ ] V001-V004 existent dans `src/main/resources/db/migration/` (V004 réécrit PostgreSQL)
- [ ] `application-prod.yml` configuré PostgreSQL + Flyway enabled
- [ ] `application-dev.yml` a `flyway.enabled: false`
- [ ] `application-dev-pg.yml` créé
- [ ] `docker-compose.yml` créé à la racine
- [ ] `mvn test` → tous les tests passent (0 régression)
- [ ] Démarrage avec `dev-pg` profile → migrations Flyway appliquées avec succès
- [ ] `flyway_schema_history` contient 4 entrées (V001-V004) après démarrage `dev-pg`
- [ ] Code review validée

---

## Risques & Mitigation

| Risque | Probabilité | Mitigation |
|--------|-------------|------------|
| Spring Batch schema conflict avec PostgreSQL | Faible | `initialize-schema: always` gère automatiquement |
| `ddl-auto: validate` rejette schéma Flyway | Faible | Types SQL → Java mappings standard; pas de `columnDefinition` custom en prod |
| Tests H2 cassés par l'ajout de Flyway | Nul | Flyway explicitement désactivé dans `application-dev.yml` |
| V004 checksum change (fichier modifié) | Nul | Flyway n'a jamais été exécuté (pas dans pom.xml actuellement) — pas d'historique |
