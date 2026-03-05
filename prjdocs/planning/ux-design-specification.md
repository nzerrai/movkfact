---
stepsCompleted: [1,2,3,4,5,6,7,8,9,10,11,12,13,14]
inputDocuments: ["prd.md", "docs/besoin.txt"]
---

# UX Design Specification movkfact

**Author:** Nouredine
**Date:** 26 février 2026

---

<!-- UX design content will be appended sequentially through collaborative workflow steps -->

## Étape 2: Compréhension du Projet

**Résumé des insights clés :**
- Movkfact est une application web pour générer des jeux de données personnalisables, destinée aux développeurs, analystes et équipes QA.
- Elle permet de créer des domaines (ex. : Client, Commande) et de générer des données variées à partir d'un fichier CSV exemple, avec configuration par colonnes (dates, emails, UUID, listes, etc.).
- Fonctionnalités principales : IHM intuitive, génération en JSON, APIs CRUD, suivi d'activité (statuts des jeux), téléchargement, génération par lots asynchrone.
- Architecture : React (front-end), Spring Boot (back-end), H2 (base de données), avec sécurité et performance.
- Propositions retenues : séparation en microservices (Winston), améliorations UX (Sally), critères d'acceptation et priorisation (John).

**Utilisateurs cibles :**
- Développeurs, analystes de données, équipes QA cherchant à créer des données de test réalistes sans effort manuel.

**Fonctionnalités/goals clés :**
- Faciliter la génération de données pour tests, réduire le temps et coûts, offrir flexibilité et extensibilité.
- Intégration APIs, suivi complet, modularité et scalabilité.

**Challenges UX :**
- Simplifier la configuration des données, ajouter des guides visuels, prioriser les features essentielles.

## Étape 3: Définition de l'Expérience Cœur

**Action utilisateur centrale :**
- Paramétrer et générer les jeux de données.

**Plateforme :**
- Web uniquement.

**Interactions effortless :**
- Propositions automatiques puis guidées.

**Moments de succès :**
- Les données générées de qualité.

**Principes d'expérience :**
1. **Action centrale simplifiée** : Le paramétrage et la génération de jeux de données est l'interaction principale, rendue intuitive via un flux linéaire : charger CSV → propositions automatiques → génération guidée.
2. **Plateforme web exclusive** : Focus sur une expérience web optimisée pour ordinateurs, avec accessibilité et performance.
3. **Interactions effortless** : Propositions automatiques des types de colonnes (dates, emails, etc.) basées sur l'analyse du CSV, suivies de guides visuels pour ajustements fins, éliminant les erreurs courantes.
4. **Moments de succès** : L'utilisateur se sent accompli en voyant les données générées de qualité, prêtes pour les tests, avec un feedback immédiat sur la pertinence.
5. **Élimination des frictions** : Automatisation des tâches répétitives, comme la détection de types et la validation, pour que la génération soit quasi-instantanée.

## Étape 4: Réponse Émotionnelle Souhaitée

Étape sautée sur demande de l'utilisateur.

## Étape 5: Analyse des Patterns UX et Inspiration

Étape passée rapidement en raison de difficulté à identifier des apps spécifiques. Suggestions générales : GitHub (collaboration), Postman (APIs), Jira (gestion de tâches) pour patterns de navigation et interactions.

## Étape 6: Choix du Système de Design

**Système choisi : MUI (Material-UI)**

**Rapport de faisabilité :**
- Intégration technique : Parfaite compatibilité avec React, facile à installer via npm. Théming flexible pour personnaliser couleurs, typographie et composants.
- Courbe d'apprentissage : Modérée, documentation excellente et communauté active.
- Maintenance : Mises à jour régulières, support long-terme. Composants pré-construits réduisent le développement personnalisé.
- Performance : Optimisé, léger, avec tree-shaking.

**Aspect professionnel :**
- Utilisé dans des milliers d'applications d'entreprise.
- Accessibilité intégrée (WCAG), cohérence visuelle, patterns éprouvés pour une expérience pro.
- Réduction des coûts de développement et assurance de qualité.

## Étape 7: Définition de l'Expérience Définissante

**Expérience définissante : "Charger, configurer automatiquement, générer instantanément"**

- Action cœur : Charger un CSV exemple, obtenir des propositions automatiques de types de colonnes, ajuster si nécessaire, générer des données JSON en un clic.
- Pourquoi définissante : Rend la génération de données aussi simple que "imprimer un document", différencie le produit.
- Modèle mental : Interface simple comme outil de productivité, éliminant la création manuelle.
- Critères de succès : Génération en < 5 secondes, feedback visuel immédiat, zéro erreur dans 90% des propositions, sentiment d'accomplissement.

## Étape 8: Fondation Visuelle

**Guidelines de marque :** Aucune, thème professionnel par défaut.

**Thème couleur :**
- Primaire : Bleu MUI (#1976d2).
- Secondaire : Vert (#4caf50).
- Neutres : Gris.
- Option sombre pour sessions longues.

**Système typographique :**
- Police : Roboto.
- Échelle : h1: 2.125rem, h2: 1.875rem, h3: 1.5rem, body: 1rem, caption: 0.75rem.
- Hauteurs de ligne : 1.5.
- Poids : Regular/Medium.

**Espacement et layout :**
- Unité : 8px.
- Layout : Dense, sidebar navigation, main config/génération.
- Grille : 12 colonnes.
- Principes : Cohérence MUI, accessibilité, hiérarchie claire.

## Étape 9: Directions de Design

**Direction choisie : Hybride Professionnel-Guidé**

- Combinaison de toutes les directions : Layout classique avec sidebar, dense pour efficacité, guidage étape par étape avec tooltips, métriques et suivi intégrés.
- Soutient l'expérience définissante avec équilibre pro/guidé.
- Cohérent visuellement avec la fondation MUI.

**Vitrine HTML créée :** ux-design-directions.html avec 4 variations et comparaisons.

## Étape 10: Parcours Utilisateur

**Parcours critiques conçus :**
1. **Créer et configurer domaine** : Flux avec propositions auto, ajustements guidés, création multiple jeux.
2. **Générer données** : Sélection jeu, lancement, feedback progressif, aperçu résultats.
3. **Gérer et suivre** : Liste avec statuts, actions par jeu, reset à original.

**APIs intégrées :**
- CRUD : POST/GET/PUT/DELETE /datasets
- Chargement/Téléchargement : /upload /download
- Accès ligne : /rows/{uuid} ou /rows?column=value

**IHM de test APIs proposée :** Sections pour upload/download et query lignes, avec feedback et résultats JSON.

**Diagrammes Mermaid inclus pour visualisation.**

## Étape 11: Stratégie des Composants

**Couverture MUI :** Boutons, champs, cartes, tables, dialogues, progress, navigation - composants standards couverts.

**Écarts identifiés :**
- DataTypeSelector : Dropdown avec icônes types.
- CsvUploader : Drag & drop stylisé.
- DatasetCard : Card avec statuts et actions.
- GenerationProgress : Progress avec estimation.
- ColumnConfigurator : Table éditable colonnes.
- StatusBadge : Chip coloré statuts.
- DataPreview : Viewer JSON.

**Composants personnalisés :** 7 composants conçus, basés sur MUI pour cohérence, avec spécifications d'usage, états, accessibilité.

## Étape 12: Patterns de Cohérence UX

**Catégories définies :** Boutons, feedback, formulaires, navigation, modales, états vides, recherche.

**Patterns critiques :**

**Hiérarchie boutons :**
- Primaire : Verts pour génération/téléchargement.
- Secondaire : Bleus pour config.
- Danger : Rouges pour suppression/reset.

**Feedback :**
- Succès : Snackbar vert.
- Erreur : Snackbar rouge détaillé.
- Avertissement : Tooltip jaune.
- Info : Badge bleu.

**Formulaires :**
- Validation inline rouge/verte.
- Tooltips icône info.
- Defaults pré-remplis.

**Navigation :**
- Sidebar expandable.
- Breadcrumbs domaine > jeu > action.
- Tabs pour sections.

**Modales :**
- Confirmation pour destructives.
- Progress non-bloquante.

**États vides :**
- Call-to-action pour création.

**Recherche :**
- Filtres chips, recherche texte.

## Étape 13: Design Responsive et Accessibilité

**Stratégie responsive :**
- Desktop : Layout multi-colonnes, sidebar, densité haute.
- Tablet : Layouts simplifiés, touch-optimisé.
- Mobile : Support basique, menu hamburger, layout vertical.

**Breakpoints :**
- Standards MUI : 600px (sm), 900px (md), 1200px (lg).
- Mobile-first : Oui.

**Accessibilité :**
- WCAG AA : Navigation clavier, screen readers, contraste >4.5:1.
- Composants custom : ARIA labels, focus management.
- Tests : Automatisés + manuels.

## Étape 14: Wireframes Détaillés d'Implémentation

### Écran 1 : Dashboard Principal

**Wireframe Description :**
```
┌─────────────────────────────────────────────────────┐
│ 🎨 MOVKFACT                        [Menu] [Logout]  │
├────────────┬────────────────────────────────────────┤
│ Domains    │ Title: "Vos Domaines"                  │
│ Generate   │ [+ Nouveau Domaine]  [Recherche]       │
│ Settings   │                                         │
│            │ ┌──────────────┐ ┌──────────────┐      │
│            │ │ Domain: User │ │ Domain: Order│      │
│            │ │ 3 jeux       │ │ 5 jeux       │      │
│            │ │ [Action ↓]   │ │ [Action ↓]   │      │
│            │ └──────────────┘ └──────────────┘      │
│            │ ┌──────────────┐ ┌──────────────┐      │
│            │ │ Domain: Prod │ │ [+ Ajouter]  │      │
│            │ │ 2 jeux       │ │              │      │
│            │ │ [Action ↓]   │ │              │      │
│            │ └──────────────┘ └──────────────┘      │
└────────────┴────────────────────────────────────────┘
```

**Composants MUI :**
- `AppBar` : Header avec logo + actions utilisateur
- `Drawer` : Sidebar navigation (Domain, Generate, Settings)
- `Grid` : Layout 2 colonnes responsive
- `Card` : Chaque Domain affiché
- `Button` : Créer domaine, Actions contextuelles
- `TextField` : Recherche domaines

**États visuels :**
- Empty state : Message + bouton "Créer premier domaine"
- Loading : Skeleton cards pendant fetch

---

### Écran 2 : CSV Upload & Type Detection

**Wireframe Description :**
```
┌─────────────────────────────────────────────────────┐
│ MOVKFACT > Domains > User > Generate Data          │
├─────────────────────────────────────────────────────┤
│ STEP 1: CHARGER CSV                                 │
│                                                      │
│ ┌────────────────────────────────────────────────┐ │
│ │  drag & drop CSV ici ou cliquer               │ │
│ │  Max 10MB                                      │ │
│ └────────────────────────────────────────────────┘ │
│                                                      │
│ [Annuler] [Continuer] (disabled)                    │
│                                                      │
│ ─ PREVIEW (après upload) ─                         │
│                                                      │
│ En-têtes détectés:                                  │
│ first_name | email | age | country                 │
│                                                      │
│ Exemple ligne: John | john@ex.com | 28 | FR        │
│                                                      │
│ Types proposés (✏️ editable):                      │
│ ✓ Firstname | ✓ Email | ✓ Age | ✓ Country          │
│ [Confidentiel?] [Modérer]                          │
│                                                      │
│ [Annuler] [Continuer]                              │
└─────────────────────────────────────────────────────┘
```

**Composants MUI :**
- `Dropzone` (custom + MUI Paper) : Drag & drop
- `LinearProgress` : Upload progress
- `Table` : Preview données
- `TextField` : Edit types détectés
- `Button` : Navigation steps
- `Alert` : Messages validation

**États visuels :**
- Upload in progress : Spinner + % complété
- Type detection : Loading skeleton types
- Error : Alert rouge + suggestion correction

---

### Écran 3 : Configuration Colonnes

**Wireframe Description :**
```
┌─────────────────────────────────────────────────────┐
│ MOVKFACT > User > Generate > STEP 2: CONFIGURER     │
├─────────────────────────────────────────────────────┤
│                                                      │
│ Colonnes à générer :                                │
│                                                      │
│ first_name [Personnel ▼]                            │
│   Format: [Prénom aléatoire ▼]                      │
│   Localisation: [France ▼]                          │
│   ℹ️ Tooltip: "Génère prénoms réalistes..."        │
│   Preview: "Marie", "Jean", "Sophie"               │
│                                                      │
│ email [Personnel ▼]                                 │
│   Format: [Email aléatoire ▼]                       │
│   Domaine custom: [________@example.com]            │
│   Preview: "alice23@example.com"                    │
│                                                      │
│ age [Temporel ▼]                                    │
│   Plage: [18 ▬─────────── 65]                       │
│   Preview: "32", "47", "21"                         │
│                                                      │
│ country [Catégorique ▼]                             │
│   Liste personnalisée: [Upload CSV ▼]               │
│   Preview: "France", "USA", "China"                 │
│                                                      │
│ Signature des colonnes [Copier]:                    │
│ [{name:"first_name", type:"personal"...}]          │
│                                                      │
│ Nombre de lignes: [1000___] (slider)                │
│ [⚠️ Estimation: 2s, 45KB]                           │
│                                                      │
│ [Retour] [Générer] ✨                              │
└─────────────────────────────────────────────────────┘
```

**Composants MUI :**
- `Select` : Type colonne dropdown
- `TextField` : Paramètres configurations
- `Slider` : Plage données, nombre lignes
- `Typography` : Preview exemples
- `Button` : Actions
- `Tooltip` : Info par colonne
- `Chip` : Tags types
- `Accordion` : Colonnes collapsible

**États visuels :**
- Type selection change : Paramètres dynamiques
- Estimation temps/taille : Calcul + affichage
- Erreur param : Champ rouge + message

---

### Écran 4 : Affichage & Téléchargement Résultats

**Wireframe Description :**
```
┌─────────────────────────────────────────────────────┐
│ MOVKFACT > User > Generated Dataset #5              │
├─────────────────────────────────────────────────────┤
│ Status: ✅ Générées (26/02/2026 14:30)              │
│ Lignes: 1000 | Taille: 45KB                         │
│                                                      │
│ [📋 Copier] [⬇️ JSON] [⬇️ CSV] [🔄 Régénérer]       │
│                                                      │
│ Données (Page 1 de 20):                             │
│                                                      │
│ ┌──────────────┬────────────────────┬─────┬────────┐│
│ │ first_name   │ email              │age  │country ││
│ ├──────────────┼────────────────────┼─────┼────────┤│
│ │ Marie        │ marie@example.com  │ 28  │ France ││
│ │ Jean         │ jean@example.com   │ 45  │ USA    ││
│ │ Sophie       │ sophie@example.com │ 32  │ China  ││
│ │ ...          │ ...                │ ... │ ...    ││
│ └──────────────┴────────────────────┴─────┴────────┘│
│ [< Prev] Page 1 / 20 [Next >]                       │
│                                                      │
│ Rechercher: [_____] par first_name                  │
│ Filtres: [Type ▼] [Date ▼]                          │
│                                                      │
│ Statut activité:                                    │
│ ✓ Créé | ✓ Consulté | ☐ Téléchargé | ☐ Modifié   │
│ [📜 Voir historique]                                │
│                                                      │
│ Copie originale: Disponible [🔄 Reset]              │
└─────────────────────────────────────────────────────┘
```

**Composants MUI :**
- `Table` : Affichage données paginée
- `TablePagination` : Navigation pages
- `Button` : Actions (copier, download, régénérer)
- `TextField` : Recherche/filter
- `Chip` : Status badges
- `Alert` : Messages info

**États visuels :**
- Loading: Skeleton table
- Empty: "Aucune donnée ne correspond"
- Download progress: Snackbar avec % complété
- Success: Toast vert "Téléchargé !"

---

## Tutoriels Interactifs (Tours Guidés)

### Tour 1 : Premier Lancement - "Bienvenue à Movkfact"

**Étapes du tour :**
1. **Étape 1 (Dashboard)**
   - Spotlight : Bouton "Nouveau Domaine"
   - Titre : "Créer votre premier domaine"
   - Description : "Un domaine regroupe vos jeux de données. Par exemple 'Utilisateurs', 'Commandes', etc."
   - Bouton : [Créer mon premier domaine]

2. **Étape 2 (Domain List)**
   - Spotlight : Card domaine newly created
   - Titre : "Vos domaines"
   - Description : "Ici sont listés tous vos domaines avec le nombre de jeux générés."
   - Bouton : [Continuer]

3. **Étape 3 (Generate Button)**
   - Spotlight : "Generate Data" button
   - Titre : "Prêt à générer des données ?"
   - Description : "Cliquez ici pour charger un fichier CSV et générer des données de test."
   - Bouton : [Commencer]

4. **Étape 4 (Type Detection)**
   - Spotlight : Type proposé (ex. "Email")
   - Titre : "Détection automatique"
   - Description : "Movkfact analyse vos données et propose automatiquement les types. Vous pouvez les modifier."
   - Bouton : [Génial !] [Ignorer tour]

**Composants :**
- Library : Joyride ou similar pour tours
- Overlay semi-transparent
- Step markers 1/4, 2/4, etc.
- Boutons : Suivant, Passer, Terminer

---

### Tour 2 : Upload CSV - "Comment charger des données"

**Étapes :**
1. Explication drag & drop
2. Validation fichier
3. Preview détection types
4. Validation colonnes

---

### Tour 3 : Configuration - "Paramétrer votre génération"

**Étapes :**
1. Sélection type colonne
2. Paramètres selon type
3. Preview exemples
4. Nombre de lignes
5. Estimation temps

---

## Mise en Œuvre Interactive

### Composants d'État Loading

**DataGeneratorLoading Component :**
```jsx
// Loading state avec spinner MUI + estimated time
<Box>
  <CircularProgress variant="determinate" value={progress} />
  <Typography>Génération en cours...</Typography>
  <Typography variant="caption">
    {progress}% complété (~{estimatedTime}s)
  </Typography>
  <LinearProgress variant="determinate" value={progress} />
</Box>
```

### Composants d'État Error

**ErrorBoundary Component :**
```jsx
// Affiche error elegamment avec bouton retry
<Alert severity="error" icon={<ErrorIcon />}>
  <AlertTitle>Erreur lors de la génération</AlertTitle>
  Détail: {errorMessage}
  [🔄 Réessayer]
</Alert>
```

### Composants d'État Success

**SuccessNotification Component :**
```jsx
// Toast success animé
<Snackbar open={success} autoHideDuration={6000}>
  <Alert severity="success">
    ✅ Données générées avec succès !
  </Alert>
</Snackbar>
```

---

## Interactions Détaillées

### Interaction 1 : Drag & Drop CSV

**Flux utilisateur :**
1. Utilisateur drag CVSfile vers zone
2. Zone feedback : Highlight + "Relâcher pour uploader"
3. Upload progress : LinearProgress bar
4. Parsing : Détection types automatique
5. Preview affichée

**Visual feedback :**
- Hover : Zone color change (hover state)
- Drag over : Dashed border, background highlight
- Upload : Progress bar animated
- Success : Checkmark + "CSV uploaded"

---

### Interaction 2 : Type Selection Change

**Flux utilisateur :**
1. Utilisateur change type dropdown (ex. Personnel → Financier)
2. Paramètres actualisent dynamiquement
3. Preview recalculé
4. Estimation temps/taille mis à jour

**Visual feedback :**
- Transition smooth between parameters
- Preview values updated instantly
- Estimation blinks (feedback de change)

---

## Résumé Enrichissement UX

✅ **Nouveaux ajouts :**
- Wireframes détaillés pour 4 écrans principaux
- Composants MUI spécifiques pour chaque écran
- Tours guidés interactifs (3 tours)
- États visuels (loading, error, success)
- Interactions détaillées (drag & drop, selection changes)
- Code examples JSX pour implémentation

**Alignement Sprint 2 :**
- S2.4 (CSV Upload) : Wireframe 2, Tour 2, Interactions 1-2
- S2.5 (Type Selector) : Wireframe 3, État loading
- S2.6 (Data Viewer) : Wireframe 4, États success

**Prochaines étapes :**
- Développeurs utilisent wireframes + composants spécifiés pour stories Sprint 2
- Tours interactifs implémentés en S2.4-2.5
- A/B testing des tours après Sprint 2 déploiement