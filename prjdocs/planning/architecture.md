---
stepsCompleted: [1,2,3,4,5,6,7]
inputDocuments: ["prd.md", "product-brief-movkfact-2026-02-26.md", "ux-design-specification.md", "docs/besoin.txt"]
workflowType: 'architecture'
project_name: 'movkfact'
user_name: 'Nouredine'
date: '26 février 2026'
---

# Architecture Decision Document

_Ce document se construit de manière collaborative à travers une découverte étape par étape. Les sections sont ajoutées au fur et à mesure que nous travaillons ensemble sur chaque décision architecturale._

## Analyse du Contexte du Projet

### Vue d'ensemble des Exigences

**Exigences Fonctionnelles :**
- Gestion de domaines : Création et consultation de domaines avec aperçu des jeux de données (implique une architecture de stockage organisée par domaines, avec métadonnées pour statuts et comptages).
- Génération de données : Chargement CSV, configuration par colonnes (types étendus : dates, emails, UUID, listes, etc.), génération JSON configurable (nécessite un moteur de génération flexible, analyse intelligente des types, et gestion de volumes importants).
- Interface utilisateur : IHM pour domaines et jeux, composants par type de colonne, tutoriels in-app, responsive et accessible (architecture front-end modulaire avec composants réutilisables, intégration MUI).
- Accès aux données : Consultation, téléchargement JSON/CSV, APIs pour téléchargement et extraction (APIs RESTful avec filtres, gestion de formats multiples).
- APIs CRUD : Opérations complètes sur les jeux de données (architecture back-end avec endpoints REST, validation et sécurité).
- Suivi d'activité : Statuts des jeux, copie originale, reset (persistance avec historique et versioning léger).
- Génération par lots : Jobs asynchrones via Spring Batch (architecture asynchrone avec files d'attente, notifications WebSockets).

**Exigences Non Fonctionnelles :**
- Performance : Génération de gros volumes sans dégradation, réponse < 2s, cache Redis (nécessite optimisation back-end, mise en cache, scalabilité horizontale).
- Sécurité : Validation inputs, masquage données sensibles, Spring Security, OWASP (architecture sécurisée avec authentification, chiffrement, audits).
- Qualité : Tests automatisés étendus, couverture > 80%, métriques temps réel (intégration CI/CD, frameworks de test, monitoring).

**Échelle et Complexité :**
Projet de complexité moyenne, domaine technique full-stack web, composants architecturaux estimés : 5-7 (front-end React, back-end Spring Boot, base H2, APIs, génération, sécurité).

- Domaine principal : Application web full-stack
- Niveau de complexité : Moyen
- Composants architecturaux estimés : 6 (IHM, génération, persistance, APIs, sécurité, asynchrone)

### Contraintes Techniques et Dépendances

- Technologies imposees : React (front), Spring Boot (back), H2 (dev) / PostgreSQL (prod), MUI (design)
- Dependances reelles : Spring Batch (lots), Apache Commons CSV, Jackson, Flyway (migrations DB)
- Contraintes : Modularite, extensibilite via Strategy pattern (GeneratorFactory)
- Note : Redis et Faker.js mentionnes initialement — non implementes dans l'etat Sprint 8

### Préoccupations Transversales Identifiées

- Sécurité : Validation, masquage, conformité OWASP
- Performance : Cache, asynchrone, scalabilité
- Accessibilité : WCAG via MUI
- Maintenabilité : Modularité, tests automatisés

## Évaluation des Modèles de Démarrage

### Domaine Technologique Principal
Application web full-stack

### Préférences Techniques Identifiées
- Langages : JavaScript/TypeScript (front), Java (back)
- Frameworks : React (front), Spring Boot (back)
- Base de données : H2 (dev), PostgreSQL potentiel (prod)
- Design : MUI (Material-UI)
- Autres : Redis pour cache, Spring Batch pour lots

### Option Recommandée : React + Spring Boot
- **Alignement :** Parfait avec PRD
- **Version actuelle :** React 18.2.0, Spring Boot 3.2.0
- **Support :** Communautés actives
- **Scalabilité :** Bonne avec microservices potentiels

### Alternatives Évaluées
- Next.js + NestJS : Bonne performance, mais changement de stack
- Remix + Prisma : Moderne, mais non aligné
- SvelteKit : Léger, mais moins adapté à la complexité

## Décisions Architecturales Principales

### Architecture des Données
- **Approche de modélisation :** JPA/Hibernate avec entités pour mapping objet-relationnel
- **Pattern d'accès :** Repository pattern pour abstraction et requêtes
- **Validation :** Bean Validation pour contraintes métier
- **Raison :** Standard Spring Boot, support des relations complexes pour Domain/Dataset/Column

### Authentification et Securite
- **Methode d'authentification :** Aucune (mode dev — toutes routes /api/** permises en Sprint 8)
- **Prevu :** JWT avec Spring Security (non implemente — cible Sprint 10+)
- **Middleware de securite :** Spring Security configure (SecurityConfig.java) — mode permissif actuel
- **RGPD :** Module d'anonymisation irreversible livre en Sprint 8 (AnonymizationService)
- **Strategie API :** Validation inputs via Bean Validation (@Valid), GlobalExceptionHandler

### API et Communication
- **Patterns de conception API :** REST avec ressources et HTTP methods
- **Documentation API :** OpenAPI/Swagger
- **Gestion d'erreurs :** Codes HTTP standards, messages JSON cohérents
- **Stratégie de limitation :** Non appliquée (pas nécessaire)
- **Communication inter-services :** REST direct entre front et back
- **Raison :** Standard pour CRUD, facile à documenter et consommer, aligné avec exigences

### Architecture Frontend
- **Gestion d'état :** Context API pour état partagé
- **Architecture composants :** Composants fonctionnels avec hooks
- **Stratégie de routage :** React Router
- **Optimisation performance :** React.memo et lazy loading
- **Optimisation bundle :** Webpack via Create React App
- **Raison :** Suffisant pour l'interface modulaire avec MUI, maintenable et performant

### Infrastructure et Déploiement
- **Stratégie d'hébergement :** Docker pour conteneurisation, déploiement local/cloud
- **Pipeline CI/CD :** GitHub Actions pour build et tests
- **Configuration environnement :** Variables d'environnement, profiles Spring
- **Monitoring et logging :** SLF4J/Logback, métriques basiques
- **Stratégie de scalabilité :** Verticale initiale, horizontale potentielle
- **Raison :** Flexible pour dev/prod, automatisation CI/CD, monitoring suffisant pour démarrage

## Patterns d'Implémentation et Règles de Cohérence

### Conventions de Nommage
- **Tables DB :** snake_case (e.g., data_domains, data_sets)
- **Colonnes DB :** snake_case (e.g., created_at, domain_name)
- **Entités JPA :** PascalCase (e.g., DataDomain, DataSet)
- **APIs :** kebab-case pour endpoints (e.g., /api/data-domains)
- **Composants React :** PascalCase (e.g., DataTypeSelector)
- **Variables/fonctions JS :** camelCase (e.g., handleSubmit, fetchData)
- **Fichiers :** kebab-case (e.g., data-type-selector.js)

### Structure des Projets
- **Backend (Spring Boot) :** src/main/java/com/movkfact/, controllers/, services/, repositories/, entities/
- **Frontend (React) :** src/components/, src/services/, src/hooks/, src/utils/
- **Tests :** src/test/ pour backend, src/__tests__/ pour frontend
- **Config :** application.yml pour profiles, .env pour front

### Formats d'API
- **Réponses succès :** {data: object, message: "success"}
- **Erreurs :** {error: "message", code: 400, details: {}}
- **Dates :** ISO 8601 (e.g., 2026-02-26T10:00:00Z)
- **Noms champs JSON :** camelCase
- **Codes statut :** Standards HTTP (200, 201, 400, 404, 500)

### Patterns de Communication
- **Événements :** PascalCase (e.g., DataGenerated)
- **Payloads :** {type: "event", payload: data}
- **Logs :** Niveaux INFO/ERROR, format "timestamp [level] message"
- **États :** loading, success, error pour async

### Patterns de Processus
- **Validation :** Côté client et serveur, messages cohérents
- **Gestion erreurs :** Try-catch, propagation d'erreurs
- **Auth :** Bearer token dans header Authorization
- **Cache :** Redis pour données fréquentes, TTL 1h

## Structure du Projet et Limites Architecturales

### Mapping des Exigences aux Composants (etat reel — Sprint 8)

**Gestion de domaines :**
- Controller: `DomainController` (GET/POST/PUT/DELETE /api/domains)
- Repository: `DomainRepository` (acces DB, soft-delete)
- Entity: `Domain` (JPA)
- Frontend pages: `DomainsPage`, `DatasetsPage`
- Frontend composants: `DomainTable`, `DomainDatasetsModal`, `BatchGenerationModal`, `BatchHistoryDrawer`

**Generation de donnees :**
- Service: `DataGeneratorServiceImpl` (moteur generation, Strategy pattern)
- Controller: `DataGenerationController` (POST /api/domains/{id}/data-sets, GET /api/data-sets/*)
- Factory: `GeneratorFactory` — dispatche vers les generateurs par ColumnType
- Entity: `DataSet` (JPA — dataJson LONGTEXT, originalData, version)
- DTOs: `GenerationRequestDTO`, `ColumnConfigDTO`, `DataSetDTO`
- Frontend composants: `ManualWizard/` (wizard 4 etapes), `CsvUploadPanel/`, `DataConfigurationPanel/`
- Frontend pages: `DataViewerPage`

**Typologies de generation (ColumnType enum — 20+ types) :**
- Personnelles : FIRST_NAME, LAST_NAME, EMAIL, PHONE, ADDRESS, GENDER
- Temporelles : DATE, BIRTH_DATE, TIME, TIMEZONE
- Financieres : AMOUNT, ACCOUNT_NUMBER, CURRENCY
- Techniques : UUID, IP_ADDRESS, URL, BOOLEAN
- Geographiques : CITY, COUNTRY, ZIP_CODE, COMPANY
- Numeriques : INTEGER, DECIMAL, PERCENTAGE
- Generiques : TEXT, ENUM

**Configuration colonnes (batch) :**
- Controller: `ColumnConfigurationController` (/api/domains/{id}/columns/*)
- Service: `ColumnConfigurationService`
- Entity: `ColumnConfiguration` (JPA — domainId, columnName, detectedType)
- Synchronisation automatique : `DataGenerationController` appelle `saveColumnConfigurations` a chaque creation de dataset

**Detection de types CSV :**
- Controller: `TypeDetectionController`
- Service: `DataPreviewService`

**Visualisation et export :**
- Controllers: `DataPreviewController`, `DataExportController`
- Services: `DataPreviewService`, `DataExportService`
- Frontend composant: `DataViewer/`

**Edition inline :**
- Controller: `DataRowEditorController` (/api/data-sets/{id}/rows/*)
- Service: `DataRowEditorService`
- Frontend composant: `DataEditor/`

**Generation batch :**
- Package: `com.movkfact.batch/` (Spring Batch)
- Controller: `BatchJobController` (POST /api/batch/generate, GET /api/batch/{jobId})
- Config store: `BatchJobConfigStore` (InMemory entre soumission et execution)
- Frontend composant: `BatchGenerationModal`, `BatchHistoryDrawer`
- Temps reel: WebSocket `/ws` — `BatchProgressWebSocketHandler`

**Anonymisation RGPD :**
- Controller: `AnonymizationController` (/api/anonymize/inspect, /process, /save)
- Service: `AnonymizationService` (strategies par ColumnType, sel runtime, aucune persistance)
- DTO: `AnonymizationColumnConfig`
- Frontend page: `AnonymizationPage` (Stepper 3 etapes)

**Lexique bancaire :**
- Controller: `LexiconController` (GET /api/lexicon/banking)
- Entity: `BankingLexiconEntry` (JPA)
- Flyway: V007__banking_lexicon.sql
- Frontend: fallback statique `bankingLexicon.js` (dev H2 sans Flyway)

**Suivi d'activite :**
- Service: `ActivityService`
- Entity: `Activity`, `ActivityActionType`
- Repository: `ActivityRepository`

**Securite :**
- Config: `SecurityConfig` — Spring Security, endpoints publics (pas de JWT implemente)
- Toutes les routes /api/** sont permises (mode dev sans authentification)

**Interface utilisateur — Pages :**
- `Dashboard` — statistiques globales
- `DomainsPage` — gestion domaines
- `DatasetsPage` — vue tous les datasets
- `DataViewerPage` — visualisation + edition inline
- `ApiReferencePage` — reference API interactive (execute, body JSON)
- `AnonymizationPage` — module RGPD
- `SettingsPage` — parametres application

### Structure de Répertoires Complète

```
movkfact/
├── backend/
│   ├── src/main/java/com/movkfact/
│   │   ├── controller/
│   │   │   ├── DomainController.java
│   │   │   └── DataController.java
│   │   ├── service/
│   │   │   ├── DomainService.java
│   │   │   ├── DataGeneratorService.java
│   │   │   └── ActivityService.java
│   │   ├── repository/
│   │   │   ├── DomainRepository.java
│   │   │   └── DataSetRepository.java
│   │   ├── entity/
│   │   │   ├── Domain.java
│   │   │   ├── DataSet.java
│   │   │   └── ColumnConfig.java
│   │   ├── dto/
│   │   │   ├── DomainDTO.java
│   │   │   └── DataRequestDTO.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── JpaConfig.java
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   └── application-prod.yml
│   └── src/test/java/com/movkfact/
│       ├── controller/
│       ├── service/
│       └── repository/
├── frontend/
│   ├── public/
│   │   ├── index.html
│   │   └── favicon.ico
│   ├── src/
│   │   ├── components/
│   │   │   ├── common/
│   │   │   │   ├── Button.js
│   │   │   │   └── Input.js
│   │   │   ├── domain/
│   │   │   │   ├── DomainList.js
│   │   │   │   └── DomainForm.js
│   │   │   ├── data/
│   │   │   │   ├── CsvUploader.js
│   │   │   │   ├── DataTypeSelector.js
│   │   │   │   └── GenerationForm.js
│   │   │   └── layout/
│   │   │       ├── Header.js
│   │   │       └── Sidebar.js
│   │   ├── services/
│   │   │   ├── api.js
│   │   │   └── domainService.js
│   │   ├── hooks/
│   │   │   ├── useDomains.js
│   │   │   └── useDataGeneration.js
│   │   ├── utils/
│   │   │   ├── formatters.js
│   │   │   └── validators.js
│   │   ├── App.js
│   │   ├── index.js
│   │   └── theme.js
│   └── src/__tests__/
│       ├── components/
│       └── services/
├── docker/
│   ├── Dockerfile.backend
│   ├── Dockerfile.frontend
│   └── docker-compose.yml
├── .github/workflows/
│   └── ci-cd.yml
├── docs/
│   ├── README.md
│   └── API.md
├── package.json
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── .gitignore
```

## Validation de l'Architecture et Achèvement

### Validation de Cohérence

**Compatibilité des Décisions :**
- ✅ Toutes les technologies (React, Spring Boot, H2, MUI) sont compatibles et versions vérifiées.
- ✅ Patterns d'implémentation s'alignent avec les choix technologiques.
- ✅ Aucune contradiction détectée dans les décisions architecturales.

**Cohérence des Patterns :**
- ✅ Conventions de nommage cohérentes à travers tous les domaines.
- ✅ Patterns de structure supportent les décisions technologiques.
- ✅ Patterns de communication alignés et cohérents.

**Alignement de Structure :**
- ✅ Structure de projet supporte toutes les décisions architecturales.
- ✅ Limites bien définies et respectées.
- ✅ Points d'intégration correctement structurés.

### Validation de Couverture des Exigences

**Exigences Fonctionnelles :**
- ✅ Gestion de domaines : Supportée par DomainController, Service, Repository, Entity, composants React.
- ✅ Génération de données : Supportée par DataGeneratorService, types étendus, moteur flexible.
- ✅ Interface utilisateur : Supportée par composants MUI, responsive, accessible.
- ✅ Accès aux données : Supportée par APIs REST, téléchargement JSON/CSV.
- ✅ APIs CRUD : Supportées par controllers, services, DTOs.
- ✅ Suivi d'activité : Supporté par ActivityService, versioning.
- ✅ Génération par lots : Supportée par Spring Batch, WebSockets.

**Exigences Non Fonctionnelles :**
- ✅ Performance : Cache Redis, optimisation back-end, génération asynchrone.
- ✅ Sécurité : Spring Security, JWT, validation OWASP, masquage données.
- ✅ Qualité : Tests automatisés, couverture >80%, CI/CD.

**Préoccupations Transversales :**
- ✅ Sécurité, performance, accessibilité, maintenabilité toutes adressées.

### Résultats de Validation
- **Cohérence :** Excellente - Aucune incompatibilité détectée.
- **Couverture :** Complète - Toutes les exigences du PRD sont supportées.
- **Lacunes :** Aucune identifiée.
- **Prêt pour Implémentation :** Oui - Architecture complète et cohérente.