---
stepsCompleted: ["step-01-init", "step-02-discovery", "step-e-01-discovery", "step-e-02-review", "step-e-03-edit", "step-v-01-discovery", "step-v-02-format-detection", "step-v-03-density-validation", "step-v-04-brief-coverage-validation", "step-v-05-measurability-validation", "step-v-06-traceability-validation", "step-v-07-implementation-leakage-validation", "step-v-08-domain-compliance-validation", "step-v-09-project-type-validation", "step-v-10-smart-validation", "step-v-11-holistic-quality-validation", "step-v-12-completeness-validation"]
inputDocuments: ["product-brief-movkfact-2026-02-26.md"]
workflowType: 'prd'
classification:
  projectType: 'Application web'
  domain: 'Outils de développement / Gestion de données'
  complexity: 'Moyenne'
  projectContext: 'Greenfield'
lastEdited: '2026-03-06'
editHistory:
  - date: '2026-03-03'
    changes: 'Ajout exigence fonctionnelle : Onglet Éditeur de données par dataset (lecture/modification/suppression de lignes avec traçabilité activité). Sections modifiées : 2.3, 2.5, 2.6.'
  - date: '2026-03-03'
    changes: 'Ajout écran dédié Domain Management + wizard de création manuelle de dataset (colonnes configurables, contraintes dynamiques par type, réordonnancement, prévisualisation). Sections modifiées : 2.1, 2.2, 2.3.'
  - date: '2026-03-04'
    changes: 'Refactorisation BMAD complète : ajout section User Journeys (§2), reformatage toutes EFs au format [Acteur] peut [capacité] avec IDs FR-001–FR-020, purge fuites d'implémentation ENFs (suppression §3.4 Architecture, noms de technos), ENFs SMART avec métriques précises, mise à jour §5 Contraintes Techniques (PostgreSQL), §7 Critères d'acceptation enrichis, §8 Plan de livraison mis à jour.'
  - date: '2026-03-06'
    changes: 'Alignement Sprint 8 : ajout FR-021 Anonymisation RGPD + user journey §2.4 ; FR-008 enrichi avec ENUM et PERCENTAGE ; NFR §4.1 cache retiré (non implémenté) ; NFR §4.2 JWT requalifié Phase 2 ; §8 plan de livraison mis à jour.'
---

# Product Requirements Document - movkfact

**Author:** Nouredine
**Date:** 2026-02-26

## 1. Executive Summary

### 1.1 Vue d'ensemble du produit
Movkfact est une application web de génération de jeux de données personnalisables pour les développeurs, analystes et équipes QA. L'application propose un écran Domain Management, un wizard de création manuelle, une détection automatique de typologies via upload CSV, un éditeur de données, des APIs CRUD, un suivi d'activité complet, et un module d'anonymisation RGPD irréversible de fichiers CSV/JSON.

### 1.2 Objectifs du produit
- Réduire le temps de création de données de test de 80% par rapport à la saisie manuelle.
- Couvrir au minimum 10 typologies de données pour divers domaines métier.
- Exposer des APIs CRUD intégrables dans les pipelines CI/CD.

### 1.3 Portée du projet
- Application web full-stack : interface React, backend Spring Boot, base de données PostgreSQL.
- Fonctionnalités MVP : gestion de domaines, création de datasets (CSV ou wizard manuel), génération JSON, éditeur de données en ligne, APIs CRUD, suivi d'activité avec traçabilité, anonymisation RGPD de fichiers CSV/JSON.
- Hors scope MVP : authentification JWT (Phase 2), IA pour matching automatique, microservices, système de plugins, modes sombre/clair.
- Évolutivité prévue : migration vers microservices (Phase 3).

## 2. User Journeys

### 2.1 Parcours — Développeur (création via CSV)

**Acteur :** Développeur logiciel
**Objectif :** Créer rapidement un dataset réaliste à partir de données existantes.

1. L'utilisateur accède à l'écran Domain Management et crée un domaine "Client".
2. L'utilisateur upload un fichier CSV (en-têtes + 1 ligne de valeurs).
3. Le système détecte automatiquement les typologies de colonnes.
4. L'utilisateur ajuste les typologies et définit le nombre de lignes (ex. 1 000).
5. L'utilisateur lance la génération et télécharge le fichier JSON produit.

**EFs couvertes :** FR-001, FR-002, FR-005, FR-007, FR-013

### 2.2 Parcours — QA Engineer (création manuelle via wizard)

**Acteur :** Ingénieur QA
**Objectif :** Créer un dataset de test précis sans fichier CSV source.

1. L'utilisateur ouvre l'écran Domain Management et sélectionne un domaine "Commande".
2. L'utilisateur lance le wizard de création manuelle.
3. L'utilisateur saisit le nom du dataset et le nombre de lignes (ex. 500).
4. L'utilisateur ajoute des colonnes : nom, typologie, contraintes dynamiques.
5. L'utilisateur réordonne les colonnes selon l'ordre d'affichage souhaité.
6. L'utilisateur prévisualise 5 lignes générées et vérifie la cohérence.
7. L'utilisateur confirme — la génération complète est lancée.
8. L'utilisateur consulte et télécharge le dataset.

**EFs couvertes :** FR-001, FR-003, FR-004, FR-006, FR-008, FR-009, FR-010, FR-013

### 2.3 Parcours — Tech Lead (intégration API et supervision)

**Acteur :** Tech Lead / Architecte
**Objectif :** Intégrer movkfact dans un pipeline CI/CD et auditer les modifications de données.

1. Le Tech Lead consulte les APIs CRUD disponibles et les intègre dans son pipeline.
2. Le système externe effectue des appels API pour créer, lire, mettre à jour et supprimer des datasets.
3. Le Tech Lead ouvre l'onglet Éditeur de données pour vérifier des lignes spécifiques.
4. Le Tech Lead modifie ou supprime une ligne incorrecte directement dans l'éditeur.
5. Le système enregistre une trace d'activité horodatée pour chaque opération.
6. Le Tech Lead consulte l'historique d'activité pour auditer les modifications.

**EFs couvertes :** FR-011, FR-012, FR-015, FR-016, FR-017, FR-018, FR-019

### 2.4 Parcours — Responsable RGPD (anonymisation de données réelles)

**Acteur :** Responsable conformité / Data Officer
**Objectif :** Anonymiser un fichier de données réelles pour le rendre réutilisable sans risque de ré-identification.

1. Le responsable accède au module "Anonymisation RGPD" depuis la sidebar.
2. Il uploade un fichier CSV ou JSON contenant des données personnelles.
3. Le système détecte les colonnes et les affiche dans un tableau de configuration.
4. Le responsable coche les colonnes à anonymiser et sélectionne la stratégie par type (FIRST_NAME, EMAIL, IBAN, BIRTH_DATE, etc.).
5. Le responsable sélectionne le domaine de destination et saisit le nom du dataset.
6. Il lance l'anonymisation — le système remplace chaque valeur par une donnée synthétique irréversible du même type structurel.
7. Le dataset anonymisé est sauvegardé dans MockFact et accessible depuis le Data Viewer.

**EFs couvertes :** FR-021

---

## 3. Exigences Fonctionnelles

### FR-001 — Créer un domaine
L'utilisateur peut créer un domaine avec un nom unique et une description depuis l'écran Domain Management.

### FR-002 — Consulter les domaines
L'utilisateur peut consulter la liste paginée de ses domaines avec pour chaque domaine : nombre de datasets, nombre total de lignes, et statuts (téléchargé, modifié, consulté).

### FR-003 — Accéder aux datasets d'un domaine
L'utilisateur peut accéder depuis un domaine à la liste de ses datasets avec indicateurs de statut et date de dernière modification.

### FR-004 — Initier la création d'un dataset
L'utilisateur peut initier la création d'un nouveau dataset via upload CSV ou via le wizard de création manuelle, depuis l'écran Domain Management.

### FR-005 — Flux CSV : upload et auto-détection
L'utilisateur peut charger un fichier CSV (en-tête + au moins une ligne de valeurs) ; le système détecte automatiquement les typologies de colonnes et les propose comme configurables.

### FR-006 — Flux manuel : wizard de création
L'utilisateur peut créer un dataset from scratch via un wizard 4 étapes :
- Étape 1 : saisir le nom du dataset et le nombre de lignes à générer.
- Étape 2 : ajouter des colonnes (nom + typologie + contraintes dynamiques selon le type) et les réordonner librement.
- Étape 3 : prévisualiser un échantillon de 5 lignes générées ; retour à l'étape 2 possible.
- Étape 4 : confirmer pour lancer la génération complète.

### FR-007 — Générer un dataset JSON
L'utilisateur peut générer un dataset en format JSON pour un nombre de lignes entre 1 et 100 000.

### FR-008 — Typologies de données supportées
L'utilisateur peut sélectionner parmi les typologies suivantes avec contraintes dynamiques par type :
- Types à format fixe (`FIRST_NAME`, `EMAIL`, `BOOLEAN`) : aucune contrainte supplémentaire.
- `TEXT` / `LOREM_IPSUM` : longueur max en caractères ou nombre de mots.
- `INTEGER` / `DECIMAL` : valeur min et valeur max.
- `PERCENTAGE` : float entre 0.0 et 100.0 avec précision à 2 décimales, contraintes min/max configurables.
- `DATE` / types temporels : date de début et date de fin.
- `ENUM` : liste de valeurs catégorielles définies par l'utilisateur (séparées par des virgules) ; le générateur pioche aléatoirement dans la liste.
- Données personnelles étendues : prénom, nom, genre, téléphone (formats locaux/internationaux), adresse complète.
- Données financières : montants avec devises, numéros de carte masqués, soldes, taux d'intérêt.
- Données catégorielles : listes, statuts, niveaux hiérarchiques.
- Données techniques : URLs, adresses IP, versions logicielles.

### FR-009 — Écran Domain Management
L'utilisateur peut accéder à un écran dédié Domain Management affichant la liste paginée des domaines avec actions : créer, consulter, supprimer.

### FR-010 — Lancer le wizard depuis Domain Management
L'utilisateur peut lancer le wizard de création manuelle depuis l'écran Domain Management pour n'importe quel domaine.

### FR-011 — Éditeur de données : consulter les lignes
L'utilisateur peut consulter les lignes d'un dataset directement dans l'application via l'onglet "Éditeur de données".

### FR-012 — Éditeur de données : modifier des colonnes
L'utilisateur peut modifier la valeur d'une ou plusieurs colonnes d'une ligne depuis l'onglet "Éditeur de données" sans outillage externe.

### FR-013 — Télécharger un dataset
L'utilisateur peut télécharger un dataset en format JSON ou CSV.

### FR-014 — Accès aux données via API
Les systèmes externes peuvent télécharger et extraire des données de datasets via des APIs REST, avec filtrage par identifiant de ligne et sélection de colonnes.

### FR-015 — CRUD APIs datasets
Les systèmes externes peuvent effectuer des opérations Créer, Lire, Mettre à jour et Supprimer sur les datasets via l'API REST, avec un temps de réponse inférieur à 500ms pour les opérations unitaires.

### FR-016 — Éditeur de données : supprimer une ligne
L'utilisateur peut supprimer une ligne d'un dataset depuis l'onglet "Éditeur de données".

### FR-017 — Suivi des statuts de dataset
Le système maintient et affiche le statut de chaque dataset : téléchargé, modifié, consulté au moins une fois.

### FR-018 — Copie originale et reset
L'utilisateur peut réinitialiser un dataset à sa version originale générée ; le système conserve la copie originale sans la modifier.

### FR-019 — Trace d'activité horodatée
Le système enregistre automatiquement une trace d'activité horodatée pour toute modification ou suppression de ligne via l'Éditeur de données, incluant : ligne concernée, colonne(s) modifiée(s), action effectuée.

### FR-020 — Génération par lots
L'utilisateur peut lancer la génération simultanée de jusqu'à 10 datasets en parallèle, avec notification de progression en temps réel.

### FR-021 — Anonymisation RGPD de fichiers
Le responsable conformité peut charger un fichier CSV ou JSON contenant des données personnelles, configurer par colonne la stratégie d'anonymisation (type de donnée synthétique), et sauvegarder le résultat anonymisé comme dataset MockFact dans un domaine de son choix.

Garanties obligatoires :
- Les transformations sont irréversibles : chaque valeur est remplacée par une donnée synthétique du même type structurel — aucun hash, aucune dérivation de la valeur originale.
- Aucune donnée originale n'est persistée côté serveur (traitement streaming).
- Les quasi-identifiants sensibles suivent des règles RGPD strictes : `BIRTH_DATE` généralise à l'année uniquement, `ACCOUNT_NUMBER` est régénéré aléatoirement (FR + 25 chiffres), `IP_ADDRESS` est composé de 4 octets indépendants.
- Pour les colonnes de type `TEXT` ou non typées, le format réel de la valeur (entier, décimal, booléen, texte libre) est détecté et respecté dans le remplacement.

## 4. Exigences Non Fonctionnelles

### 4.1 Performance
- Le système génère jusqu'à 10 000 lignes en moins de 10 secondes pour le 95e percentile, mesuré par tests de charge.
- Le système répond aux requêtes API en moins de 2 secondes pour le 95e percentile sous charge normale (≤ 100 utilisateurs simultanés).

### 4.2 Sécurité
- Toutes les entrées utilisateur sont validées et assainies contre les injections SQL, XSS et command injection — zéro vulnérabilité OWASP Top 10 tolérée en production.
- **(Phase 2)** Toutes les routes API seront protégées par authentification JWT — accès refusé (HTTP 401) sans token valide. Non implémenté en Phase 1 ; les routes sont actuellement accessibles en réseau local de confiance.
- Les numéros de carte de crédit générés sont masqués (`****-****-****-XXXX`) — aucune valeur complète stockée en base.

### 4.3 Qualité
- La couverture de tests automatisés (unitaires + intégration + end-to-end) est supérieure à 80%.
- Le système supporte des tests de charge atteignant 100 000 lignes générées sans dégradation supérieure à 20% du temps de réponse nominal.

### 4.4 Interface Utilisateur
- L'interface est conforme WCAG 2.1 niveau AA — compatible mobile, tablette et desktop.

## 5. Contraintes Techniques
- Stack : React (front-end), Spring Boot Java (back-end), PostgreSQL (base de données).
- Compatibilité : composants open-source avec licences vérifiées (MIT, Apache 2.0).
- Évolutivité : architecture modulaire préparant la migration vers microservices (Phase 3).

## 6. Risques et Mitigations
- Complexité de génération : tests intensifs et validation utilisateur par persona.
- Performance : cache des configurations fréquentes et optimisation des requêtes de génération.
- Adoption : tutoriels interactifs et documentation utilisateur.
- Sécurité : audits OWASP réguliers (coût estimé : 10% du budget).
- Scalabilité : architecture modulaire dès le départ (migration PostgreSQL complétée en Sprint 4).

## 7. Critères d'acceptation
- **MVP** : génération (CSV + wizard manuel) + écran Domain Management + APIs CRUD + suivi d'activité + éditeur de données opérationnels.
- **Performance** : génération 10 000 lignes < 10s, réponse API < 2s au 95e percentile sous charge normale.
- **Qualité** : tous les scénarios de tests passent, couverture > 80%.
- **Éditeur de données** : lecture, modification et suppression de lignes fonctionnelles avec trace d'activité horodatée générée automatiquement.
- **Accessibilité** : conformité WCAG 2.1 AA validée (mobile + tablette + desktop).

## 8. Plan de livraison
- Phase 1 (Q1) : MVP livré — génération CSV + wizard manuel, écran Domain Management, APIs CRUD, éditeur de données, suivi d'activité, génération par lots (10 datasets parallèles), types étendus (ENUM, PERCENTAGE), anonymisation RGPD irréversible (Sprint 8).
- Phase 2 (Q2) : stats agrégées domaines (FR-002/003), API filtrage avancé rowIds + colonnes (FR-014), déploiement Docker production-ready, authentification JWT + gestion des rôles utilisateurs.
- Phase 3 (Q3+) : microservices, IA pour matching automatique de typologies, modes UX avancés, système de plugins, cache Redis pour performances.