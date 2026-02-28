---
stepsCompleted: ["step-01-init", "step-02-discovery"]
inputDocuments: ["product-brief-movkfact-2026-02-26.md"]
workflowType: 'prd'
classification:
  projectType: 'Application web'
  domain: 'Outils de développement / Gestion de données'
  complexity: 'Moyenne'
  projectContext: 'Greenfield'
---

# Product Requirements Document - movkfact

**Author:** Nouredine
**Date:** 2026-02-26

## 1. Introduction

### 1.1 Vue d'ensemble du produit
Movkfact est une application web de génération de jeux de données personnalisables, conçue pour aider les développeurs, analystes et équipes QA à créer des données de test réalistes sans effort manuel. L'application utilise une interface utilisateur intuitive pour configurer la génération de données variées, avec des APIs pour l'intégration et un suivi d'activité complet.

### 1.2 Objectifs du produit
- Faciliter la création de données de test pour les équipes de développement.
- Réduire le temps et les coûts associés à la génération manuelle de données.
- Offrir une solution flexible et extensible pour divers domaines d'application.

### 1.3 Portée du projet
- Développement d'une application web full-stack.
- Intégration de technologies modernes : React (front-end), Spring Boot (back-end), H2 (base de données).
- Fonctionnalités clés : Génération de données par domaines, IHM de configuration, APIs CRUD, suivi d'activité.

## 2. Exigences Fonctionnelles

### 2.1 Gestion des domaines
- Créer et consulter des domaines (e.g., Client, Commande).
- Chaque domaine contient un ou plusieurs jeux de données.
- Aperçu par domaine : nombre de jeux de données, nombre de lignes par jeu, statuts (téléchargé, modifié, consulté).

### 2.2 Génération de données
- Charger un fichier CSV d'exemple (en-tête + ligne de valeurs).
- Configurer les colonnes par typologie : dates (intervalles réalistes), emails aléatoires, UUID, listes déroulantes, upload de listes custom.
- Générer en format JSON, nombre de lignes configurable.
- Types de données étendus détaillés :
  - **Données personnelles étendues** : Prénoms, noms de famille, genres, numéros de téléphone (avec formats locaux/internationaux), adresses complètes (rue, ville, code postal, pays) – idéal pour des domaines comme 'Utilisateur' ou 'Employé'.
  - **Données financières** : Montants (avec devises et plages réalistes), numéros de carte de crédit (masqués pour la sécurité), soldes de compte, taux d'intérêt – pour des domaines 'Transaction' ou 'Paiement'.
  - **Données temporelles avancées** : Horaires, fuseaux horaires, périodes (semaines, mois), événements récurrents – utile pour 'Calendrier' ou 'Planning'.
  - **Données catégorielles** : Listes de catégories (produits, secteurs d'activité), statuts (actif/inactif, approuvé/rejeté), niveaux (débutant/expert) – pour structurer des hiérarchies dans des domaines comme 'Produit' ou 'Projet'.
  - **Données techniques** : URLs, adresses IP, versions logicielles, tailles de fichiers – pour des domaines 'Système' ou 'API'.
  - **Données aléatoires personnalisables** : Textes lorem ipsum, booléens, nombres entiers/décimaux avec contraintes (min/max), et même des images ou fichiers fictifs si on étend l'app.

### 2.3 Interface utilisateur
- IHM pour consulter/créer domaines et jeux de données.
- Composants pour chaque type de colonne avec defaults et overrides.
- Tutoriels in-app (tooltips animés) pour guider les utilisateurs.
- Responsive et accessible.

### 2.4 Accès aux données
- Consultation directe dans l'app.
- Téléchargement en JSON ou CSV.
- APIs pour téléchargement automatique et extraction par critère (UUID, colonnes).

### 2.5 APIs CRUD
- Opérations complètes sur les jeux de données : Créer, Lire, Mettre à jour, Supprimer.

### 2.6 Suivi d'activité
- Statuts des jeux : téléchargé, modifié, consulté au moins une fois.
- Conservation d'une copie originale.
- Action pour reset à l'original.

### 2.7 Génération par lots
- Génération de plusieurs datasets en parallèle via jobs asynchrones (Spring Batch).

## 3. Exigences Non Fonctionnelles

### 3.1 Performance
- Génération de gros volumes (e.g., 10k lignes) sans dégradation.
- Temps de réponse < 2s pour les opérations standard.
- Cache Redis pour les générations fréquentes.

### 3.2 Sécurité
- Validation des inputs pour éviter les injections.
- Gestion sécurisée des données sensibles (e.g., masquage des cartes de crédit).
- Utilisation de Spring Security pour les APIs.
- Scénarios de sécurité OWASP pour les inputs.

### 3.3 Qualité
- Tests automatisés : JUnit/TestNG (back), Jest/React Testing Library (front), RestAssured (APIs), end-to-end (Selenium/Cypress).
- Tests de charge (JMeter) pour 100k lignes.
- Métriques de couverture en temps réel dans l'IHM.
- Couverture de code > 80%.

### 3.4 Architecture
- Modularité : Service DataGenerator avec stratégies par type.
- Persistance : JPA/Hibernate, tables DataType et ColumnConfig.
- Intégration : Surcouche sur Faker.js et framework JSON.
- Système de plugins pour étendre les typologies sans redéploiement.
- Séparation en microservices (génération vs stockage) pour la scalabilité.
- Algorithme de matching intelligent (NLP) pour détecter les types de colonnes.
- Génération asynchrone avec WebSockets pour notifications utilisateur.

### 3.5 Interface Utilisateur (UX)
- Wireframes conceptuels (dashboard avec drag-and-drop).
- Accessibilité WCAG 2.1.
- Modes sombre/clair.
- Tutoriels interactifs (tours guidés) au lieu de tooltips seuls.

## 4. Contraintes Techniques
- Stack : React, Spring Boot Java, H2.
- Compatibilité : Open-source, licences vérifiées.
- Évolutivité : Préparation pour migration vers PostgreSQL et microservices.

## 5. Risques et Mitigations
- Complexité de génération : Tests intensifs et validation utilisateur.
- Performance : Optimisations et cache (Redis).
- Adoption : Tutoriels interactifs et documentation.
- Sécurité : Spring Security et tests OWASP (coût estimé : 10% du budget).
- Scalabilité : Microservices dès le départ (migration DB : 20% du budget).

## 6. Critères d'acceptation
- MVP : Génération basique + IHM + APIs de base (limité à 3 typologies : personnelles, financières, temporelles).
- Tests : Tous les scénarios passent, couverture >80%, métriques en temps réel.
- Utilisateur : Facilité d'usage confirmée par feedback, accessibilité WCAG.

## 7. Plan de livraison
- Phase 1 (Q1) : MVP avec génération basique, IHM (wireframes), APIs CRUD, suivi d'activité.
- Phase 2 (Q2) : APIs avancées, génération par lots, plugins pour typologies.
- Phase 3 (Q3+) : Évolutions (microservices, IA pour matching, modes UX avancés).