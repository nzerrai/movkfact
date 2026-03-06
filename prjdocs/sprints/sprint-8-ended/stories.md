---
sprint: 8
status: completed
totalPoints: 17
---

# Sprint 8 — User Stories

## S8.1 — Type ENUM : liste de valeurs configurables

**Points :** 3 | **Statut :** Done | **Epic :** 5 (Extended Data Types)

**En tant que** utilisateur du wizard de creation de dataset,
**Je veux** pouvoir definir une colonne de type "Liste de valeurs" avec mes propres valeurs,
**Afin de** generer des donnees categoriques representant des etats metier (ex: Actif, Inactif, Suspendu).

### Criteres d'acceptation

- [x] Le type ENUM apparait dans le selecteur de typologies
- [x] Un champ TextField permet de saisir les valeurs separees par des virgules
- [x] Le generateur pioche aleatoirement dans la liste fournie
- [x] Si la liste est vide, la cellule generee est une chaine vide
- [x] Le type est disponible dans le moteur de generation batch

### Implementation

- Backend : `ColumnType.ENUM` + `EnumGenerator.java` + `GeneratorFactory` case ENUM
- Frontend : `DynamicConstraintsPanel.jsx` — bloc ENUM avec TextField
- Frontend : `columnTypes.js` — ajout `{ value: 'ENUM', label: 'Liste de valeurs' }`

---

## S8.2 — Anonymisation RGPD fichiers CSV/JSON

**Points :** 8 | **Statut :** Done | **Epic :** 11 (Anonymisation RGPD)

**En tant que** responsable de la conformite RGPD,
**Je veux** pouvoir charger un fichier CSV ou JSON, choisir les colonnes a anonymiser et leur typologie,
**Afin d'** obtenir un fichier ou aucune valeur ne peut etre reliee a une personne physique, meme en cas de fuite.

### Criteres d'acceptation

- [x] Etape 1 : upload CSV/JSON + detection automatique des colonnes
- [x] Etape 2 : tableau de colonnes avec checkbox anonymiser + selecteur de type + strategie affichee
- [x] Chaque type applique une strategie irreversible (pas de hachage, donnees synthetiques)
- [x] IBAN : 27 caracteres regeneres aleatoirement (FR + 25 chiffres)
- [x] IP : 4 octets completement regeneres
- [x] BIRTH_DATE : annee uniquement (generalisation RGPD)
- [x] DATE : decalage aleatoire +/- 180 jours
- [x] TEXT et colonnes inconnues : detection du format reel (entier/decimal/booleen/texte) respectee
- [x] Aucune donnee originale n'est persistee cote serveur

### Implementation

- Backend : `AnonymizationService.java` — moteur de transformation par ColumnType
- Backend : `AnonymizationController.java` — POST /api/anonymize/inspect + /process
- Backend : `AnonymizationColumnConfig.java` — DTO colonne + type + flag anonymize
- Backend : `SecurityConfig.java` — permit /api/anonymize/**
- Frontend : `AnonymizationPage.jsx` — Stepper 3 etapes
- Frontend : `Sidebar.jsx` — menu "Anonymisation RGPD" avec ShieldIcon

---

## S8.3 — Sauvegarde du resultat anonymise en dataset

**Points :** 3 | **Statut :** Done | **Epic :** 11 (Anonymisation RGPD)

**En tant que** utilisateur,
**Je veux** que le resultat de l'anonymisation soit sauvegarde comme un dataset MockFact dans un domaine de mon choix,
**Afin de** pouvoir le consulter, le telecharger et le reutiliser depuis l'application.

### Criteres d'acceptation

- [x] Selecteur de domaine disponible a l'etape 2 (chargement via getDomains())
- [x] Champ nom de dataset pre-rempli depuis le nom du fichier
- [x] Appel POST /api/anonymize/save avec domainId + datasetName
- [x] Dataset cree avec rowCount, columnCount, generationTimeMs corrects
- [x] Etape 3 : lien direct "Voir le dataset" vers /data-viewer/{id}
- [x] Les erreurs objet JSON (format Spring) sont correctement extraites en string

### Implementation

- Backend : `AnonymizationController.java` — POST /api/anonymize/save
- Backend : `AnonymizationService.java` — anonymizeCsvToRows() + anonymizeJsonToRows()
- Frontend : `AnonymizationPage.jsx` — mise a jour step 2 + step 3

---

## S8.4 — Fix batch generation : domaine sans upload CSV prealable

**Points :** 2 | **Statut :** Done | **Epic :** 3 (Advanced Features)

**En tant que** utilisateur de la generation batch,
**Je veux** pouvoir lancer un batch sur un domaine cree via le wizard manuel,
**Afin de** ne pas etre bloque par le message "Aucune configuration" lorsque je n'ai pas utilise le flux CSV upload.

### Criteres d'acceptation

- [x] Apres creation d'un dataset via le wizard, le domaine apparait comme configurable dans BatchGenerationModal
- [x] Le chip "Aucune configuration" n'apparait plus pour les domaines ayant au moins un dataset
- [x] Les configurations sont synchronisees automatiquement, sans action manuelle supplementaire

### Cause racine

`column_configurations` n'etait alimentee qu'apres upload CSV via `ColumnConfigurationController.saveColumnConfigurations()`. Les datasets crees via le wizard manuel ne declenchaient pas cette sauvegarde.

### Implementation

- Backend : `DataGenerationController.generateDataset()` — appel `columnConfigurationService.saveColumnConfigurations()` apres chaque creation de dataset

---

## S8.5 — API Reference : zone JSON body pour endpoints POST

**Points :** 1 | **Statut :** Done | **Epic :** 9 (Advanced API)

**En tant que** developpeur utilisant l'API Reference de MockFact,
**Je veux** pouvoir saisir un corps JSON pour les requetes POST directement depuis l'interface,
**Afin de** tester les endpoints de creation sans avoir recours a un outil externe.

### Criteres d'acceptation

- [x] Une zone textarea apparait pour tous les endpoints POST
- [x] Le JSON saisi est envoye avec le header Content-Type: application/json
- [x] Les erreurs de parsing JSON sont gerees proprement
- [x] La zone est pre-remplie avec un exemple par defaut si defini

### Implementation

- Frontend : `EndpointCard.jsx` — state body + textarea + axios data
- Frontend : `CrudTab.jsx` — champ defaultBody sur POST /api/domains et POST /api/domains/{id}/data-sets
