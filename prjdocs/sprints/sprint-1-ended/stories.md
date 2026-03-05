---
sprint: 1
totalStories: 5
totalPoints: 21
---

# Sprint 1 User Stories

## Story S1.1 : Setup Backend Infrastructure

**Points :** 3  
**Epic :** EPIC 1 : Foundation & Core MVP  
**Type :** Technical Setup

### Description
Établir l'infrastructure Spring Boot de base pour movkfact, incluant configuration Maven, dépendances, base de données H2, et framework REST.

### Acceptance Criteria
- [ ] Maven project créé avec structure standard (src/main/java, src/test/java)
- [ ] Spring Boot 3.2.0 configuré et démarrant
- [ ] H2 database configurée (dev profile)
- [ ] Application.yml créé avec profiles (dev, prod)
- [ ] Spring Security initié avec base JWT
- [ ] Application démarre sans erreur sur localhost:8080
- [ ] Endpoint health check fonctionnel (/health)

### Technical Notes
- Utiliser Spring Boot starter-web, starter-data-jpa, starter-security
- H2 console accessible pour debug : http://localhost:8080/h2-console
- JWT secret stocké en variable environment

### Tâches
- Créer Maven project
- Configurer dépendances
- Setup H2 database
- Configurer Spring Security
- Setup CI/CD workflow (GitHub Actions basic)

---

## Story S1.2 : Implement Domain Entity & Repository

**Points :** 5  
**Epic :** EPIC 1 : Foundation & Core MVP  
**Type :** Backend Feature

### Description
Implémenter l'entité JPA Domain et son Repository pour permettre la gestion persistante des domaines en base.

### Acceptance Criteria
- [ ] Entité Domain créée avec champs : id, name, description, createdAt
- [ ] Annotations JPA correctement appliquées (@Entity, @Table)
- [ ] Table domain_master créée en H2
- [ ] DomainRepository interface créée (extends JpaRepository)
- [ ] Méthodes de recherche : findById, findAll, existsByName
- [ ] Tests unitaires pour Repository (>80% coverage)
- [ ] Aucune exception à la création/lecture

### Technical Notes
- Utiliser snake_case pour table/colonnes (domain_master)
- Ajouter timestamps : created_at, updated_at
- Implémenter soft delete (deleted_at) pour archivage

### Tâches
- Créer entité Domain.java
- Créer DomainRepository
- Écrire tests JUnit
- Migrer schéma H2

---

## Story S1.3 : Implement Domain REST Controller

**Points :** 5  
**Epic :** EPIC 1 : Foundation & Core MVP  
**Type :** Backend Feature

### Description
Implémenter les endpoints REST pour les opérations CRUD complètes sur les domaines.

### Acceptance Criteria
- [ ] DomainController créé avec endpoints standard
  - POST /api/domains : Create
  - GET /api/domains : List all
  - GET /api/domains/{id} : Get one
  - PUT /api/domains/{id} : Update
  - DELETE /api/domains/{id} : Delete
- [ ] Utiliser DTOs pour request/response (DomainCreateDTO, DomainResponseDTO)
- [ ] Validation inputs (Bean Validation)
- [ ] Réponses API format standard : {data: {...}, message: "..."}
- [ ] Error handling avec GlobalExceptionHandler
- [ ] Tests RestAssured pour chaque endpoint
- [ ] Documentation Swagger/OpenAPI auto-générée

### Technical Notes
- Codes HTTP : 200 OK, 201 Created, 400 Bad Request, 404 Not Found, 500 Error
- JWT token requis en header Authorization
- Tous les endpoints sauf POST sans auth pour MVP (optionnel)

### Tâches
- Créer DomainController.java
- Créer DTOs
- Implémenter validation
- Écrire tests RestAssured
- Générer Swagger docs

---

## Story S1.4 : Setup Frontend Project & Dashboard

**Points :** 4  
**Epic :** EPIC 1 : Foundation & Core MVP  
**Type :** Frontend Setup

### Description
Créer le projet React avec Create React App, MUI, et un dashboard basique avec navigation.

### Acceptance Criteria
- [ ] React 18.2.0 project créé avec CRA
- [ ] MUI 5.14.0 intégré et themé (couleurs primaires/secondaires)
- [ ] Dashboard principal component avec layout (Header, Sidebar, Content)
- [ ] Routing basique avec React Router
- [ ] API service client créé (axios/fetch wrapper)
- [ ] Page d'accueil affichant "Bienvenue Movkfact"
- [ ] Responsive design testé (mobile, tablet, desktop)
- [ ] Accessible (WCAG base)
- [ ] npm start fonctionne

### Technical Notes
- Utiliser CRA pour simplifier config
- MUI theme.js pour centraliser couleurs/typo
- Services API en /src/services/
- Composants en /src/components/

### Tâches
- Créer React project CRA
- Installer MUI, React Router
- Créer structure dossiers
- Implémenter layout de base
- Tester responsive

---

## Story S1.5 : Implement Domain Management UI

**Points :** 4  
**Epic :** EPIC 1 : Foundation & Core MVP  
**Type :** Frontend Feature

### Description
Créer l'interface pour afficher la liste des domaines et permettre création/édition/suppression via formulaires MUI.

### Acceptance Criteria
- [ ] DomainList component affichant tous les domaines en Table MUI
- [ ] DomainForm component pour créer/éditer domaines
- [ ] Boutons d'action : Create, Edit, Delete, View
- [ ] Modales pour Create/Edit avec validation
- [ ] Intégration API backend fonctionnelle
- [ ] Feedback utilisateur (loading states, messages d'erreur)
- [ ] Page responsive et accessible
- [ ] Tests Jest/React Testing Library pour composants

### Technical Notes
- Utiliser React hooks (useState, useEffect, useContext)
- API calls en useEffect avec try-catch
- Valider formulaires côté client avant submission
- Afficher erreurs API à l'utilisateur

### Tâches
- Créer DomainList.js
- Créer DomainForm.js
- Intégrer API calls
- Écrire tests Jest
- Tester flows utilisateur

---

## Résumé Sprint 1

**Total Points :** 21  
**Estimation effort :** ~5 pts/jour pour 2 personnes = 21 pts / 5 = 4 jours effectifs, + buffer

| Story | Points | Assigné |
|-------|--------|---------|
| S1.1 Setup Backend | 3 | Amelia |
| S1.2 Domain Entity | 5 | Amelia |
| S1.3 Domain API | 5 | Amelia |
| S1.4 Frontend Setup | 4 | Sally |
| S1.5 Domain UI | 4 | Sally |

**État :** Ready to start

---

Retour à [Sprint 1 Kickoff](kickoff-summary.md)
