---
sprint: 2
totalStories: 6
totalPoints: 34
---

# Sprint 2 User Stories

## Story S2.1 : Implement DataGeneratorService Backend

**Points :** 8  
**Epic :** EPIC 2 : Data Generation Engine  
**Type :** Backend Feature

### Description
Implémenter le service core de génération de données avec support pour 3 typologies de base.

### Acceptance Criteria
- [ ] DataGeneratorService créé dans src/main/java/.../service/
- [ ] 3 typologies implémentées :
  - **Personnelles** : Prénoms, noms, emails, genres, téléphones
  - **Financières** : Montants, devises, numéros de compte masqués
  - **Temporelles** : Dates, heures, fuseaux horaires
- [ ] Configuration par colonne (type, format, plage)
- [ ] Génération JSON configurable (nombre de lignes)
- [ ] Performance : 1000 lignes < 2s
- [ ] Tests unitaires pour chaque typologie (>80% coverage)
- [ ] Aucune exception pour cas standards

### Technical Notes
- Utiliser Faker.js (npm) ou librairie Java équivalente
- ColumnConfig entity pour stocker configs colonnes
- Stratégie pattern pour chaque typologie
- DataSet entity pour résultats

### Tâches
- Créer DataGeneratorService.java
- Implémenter 3 typologies
- Créer entités ColumnConfig, DataSet
- Écrire tests JUnit
- Benchmark génération

---

## Story S2.2 : Implement CSV Column Type Detection

**Points :** 5  
**Epic :** EPIC 2 : Data Generation Engine  
**Type :** Backend Feature

### Description
Implémenter algorithme de détection intelligente des types de colonnes à partir d'un fichier CSV.

### Acceptance Criteria
- [ ] CSV parser acceptant en-têtes + données exemples
- [ ] Détection types pour 3 typologies MVP :
  - Personnelles (ex: "firstname", "email", "gender")
  - Financières (ex: "amount", "account_number", "currency")
  - Temporelles (ex: "date_birth", "created_at", "timezone")
- [ ] Propositions utilisateur confirmées avant génération
- [ ] Accuracy >90% pour cas standards
- [ ] Gestion données manquantes/malformées
- [ ] Tests avec fichiers CSV variés

### Technical Notes
- Pattern matching sur noms colonnes
- Analyse valeurs pour confirmation
- Config fallback si détection échoue
- Logging détecté vs proposé

### Tâches
- Créer TypeDetectionService.java
- Parser CSV
- Implémenter heuristiques détection
- Écrire tests avec fichiers samples
- Valider accuracy

---

## Story S2.3 : Create Data Generation REST API

**Points :** 5  
**Epic :** EPIC 2 : Data Generation Engine  
**Type :** Backend Feature

### Description
Implémenter endpoints REST pour générer des données basées sur une Domain et sa configuration.

### Acceptance Criteria
- [ ] Endpoints créés :
  - POST /api/domains/{domainId}/data-sets : Créer jeu de données
  - GET /api/domains/{domainId}/data-sets : Lister jeux
  - GET /api/data-sets/{id}/data : Récupérer données JSON
  - DELETE /api/data-sets/{id} : Supprimer jeu
- [ ] Request body : {numberOfRows: 100, columns: [{name, type, config}]}
- [ ] Validation des inputs (>0 lignes, types valides)
- [ ] Réponses API format standard
- [ ] Codes HTTP corrects
- [ ] Tests RestAssured
- [ ] Documentation OpenAPI

### Technical Notes
- Stocker DataSet avec métadonnées (générationTime, rowCount)
- Supporter JSON array output
- Error handling pour inputs invalides

### Tâches
- Créer DataController.java
- Implémenter endpoints
- Créer DTOs (GenerationRequestDTO, etc)
- Écrire tests RestAssured
- Update Swagger

---

## Story S2.4 : CSV Upload & Preview UI

**Points :** 6  
**Epic :** EPIC 2 : Data Generation Engine  
**Type :** Frontend Feature

### Description
Créer interface pour uploader un fichier CSV et afficher aperçu avec détection types proposée.

### Acceptance Criteria
- [ ] CsvUploader component créé avec MUI
  - Drag & drop support
  - File picker
  - Validation fichier (CSV only, <10MB)
- [ ] Preview affichant en-têtes + 1 ligne exemple
- [ ] Types détectés affichés avec propositions (editable)
- [ ] Bouton "Confirm & Next" pour valider
- [ ] Error messages utilisateur clairs
- [ ] Accessible et responsive
- [ ] Tests Jest pour composant

### Technical Notes
- Utiliser react-dropzone pour drag & drop
- CSV parser côté front (papaparse lib)
- Appel backend pour détection types
- Store config en Context API

### Tâches
- Créer CsvUploader.js
- Implémenter drag & drop
- Parser CSV front-end
- Afficher preview
- Tester avec fichiers variés

---

## Story S2.5 : Data Type Selector & Configuration UI

**Points :** 5  
**Epic :** EPIC 2 : Data Generation Engine  
**Type :** Frontend Feature

### Description
Créer interface pour configurer chaque colonne avec son type et paramètres spécifiques.

### Acceptance Criteria
- [ ] DataTypeSelector component MUI créé
- [ ] Pour chaque colonne CSV :
  - Dropdown sélectioner type (Personnel, Financier, Temporel)
  - Champs parameters selon type :
    - Personnel : Format téléphone, Localisation
    - Financier : Devise, Plage montants
    - Temporel : Plage dates, Fuseau horaire
- [ ] Aperçu valeurs générées pour 5 exemple
- [ ] Defaults intelligents pré-remplis
- [ ] Bouton "Générer" pour lancer génération
- [ ] Validation complète avant submission

### Technical Notes
- Utiliser MUI TextField, Select, FormControl
- Dynamic fields selon type sélectionné
- State management Context API
- Appel API génération au click "Générer"

### Tâches
- Créer DataTypeSelector.js
- Implémenter fields dynamiques
- Ajouter previews
- Intégrer API génération
- Tester tous les types

---

## Story S2.6 : Display & Download Generated Data

**Points :** 5  
**Epic :** EPIC 2 : Data Generation Engine  
**Type :** Frontend Feature

### Description
Afficher les données générées, permettre consultation et téléchargement en JSON/CSV.

### Acceptance Criteria
- [ ] DataViewer component affichant résultats JSON
  - Table pagination (50 lignes/page)
  - Search/filter sur colonnes
  - Copy to clipboard
- [ ] Téléchargement supports :
  - JSON format
  - CSV format
- [ ] Affichage nombre total lignes généré
- [ ] Performance : Affichage rapide même 10k lignes
- [ ] Accessible (table headers, alt text)
- [ ] Responsive design

### Technical Notes
- Utiliser MUI Table component
- Virtual scrolling pour performance grands volumes
- Downloader lib (FileSaver.js)
- Format data pour export correct

### Tâches
- Créer DataViewer.js
- Implémenter table avec pagination
- Ajouter search/filter
- Créer fonctions export JSON/CSV
- Tester avec gros volumes

---

## Résumé Sprint 2

**Total Points :** 34  
**Estimation effort :** ~5 pts/jour / personne = 34 / 5 = 6-7 jours (buffer inclus)

| Story | Points | Assigné | Type |
|-------|--------|---------|------|
| S2.1 DataGenerator Service | 8 | Amelia | Backend |
| S2.2 Type Detection | 5 | Amelia | Backend |
| S2.3 Generation API | 5 | Amelia | Backend |
| S2.4 CSV Upload UI | 6 | Sally | Frontend |
| S2.5 Type Selector UI | 5 | Sally | Frontend |
| S2.6 Data Viewer UI | 5 | Sally | Frontend |

**État :** Ready (dépend Sprint 1)

---

Retour à [Sprint 2 Kickoff](kickoff-summary.md)
