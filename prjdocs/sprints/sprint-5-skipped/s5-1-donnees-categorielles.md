# Story S5.1: Données Catégorielles — STATUS, CATEGORY, LEVEL

**Sprint:** Sprint 5
**Points:** 5
**Epic:** EPIC 5 - Extended Data Types
**Type:** Full-Stack Feature
**Lead:** Amelia (Backend) + Sally (Frontend)
**Status:** Backlog
**Dependencies:** S2.1 DataGeneratorService, S2.6 Data Configuration UI

---

## User Story

**En tant que** développeur ou analyste de données,
**Je veux** pouvoir configurer des colonnes de type catégoriel (statut, catégorie, niveau),
**Afin de** générer des datasets réalistes avec des valeurs d'énumération cohérentes (ex: actif/inactif, produit/service, débutant/expert).

---

## Contexte métier

Le PRD section 2.2 définit les **Données catégorielles** comme suit :
> Listes de catégories (produits, secteurs d'activité), statuts (actif/inactif, approuvé/rejeté), niveaux (débutant/expert) — pour structurer des hiérarchies dans des domaines comme 'Produit' ou 'Projet'.

Ces types sont essentiels pour les équipes QA qui testent des workflows avec des états multiples (onboarding, approval flows, user levels).

---

## ✅ Acceptance Criteria

### AC1 — Nouveaux types dans ColumnType enum
- [ ] `STATUS("categorical", "Statut")` ajouté dans `ColumnType.java`
- [ ] `CATEGORY("categorical", "Catégorie")` ajouté dans `ColumnType.java`
- [ ] `LEVEL("categorical", "Niveau")` ajouté dans `ColumnType.java`
- [ ] La catégorie `"categorical"` est reconnue dans `DataTypeDetectionService`

### AC2 — Générateurs backend
- [ ] `StatusGenerator` créé dans `service/generator/categorical/`
  - Valeurs par défaut : `["actif", "inactif", "en_attente", "suspendu"]`
  - Sélection aléatoire pondérée (actif=60%, inactif=20%, en_attente=15%, suspendu=5%)
- [ ] `CategoryGenerator` créé dans `service/generator/categorical/`
  - Valeurs par défaut : `["Électronique", "Vêtements", "Alimentation", "Services", "Mobilier", "Sport"]`
  - Support liste custom via `additionalConfig.values` (JSON array)
- [ ] `LevelGenerator` créé dans `service/generator/categorical/`
  - Valeurs par défaut : `["débutant", "intermédiaire", "avancé", "expert"]`
  - Distribution réaliste : débutant=40%, intermédiaire=35%, avancé=20%, expert=5%
- [ ] `GeneratorFactory` mis à jour pour les 3 nouveaux `ColumnType`

### AC3 — API sans régression
- [ ] `POST /api/datasets/generate` accepte les nouveaux types
- [ ] `POST /api/batch/generate` accepte les nouveaux types
- [ ] Les tests existants passent toujours (0 régression)

### AC4 — Composant frontend `CategoricalFieldConfig`
- [ ] Composant React `CategoricalFieldConfig.jsx` créé dans `DataConfigurationPanel/`
- [ ] Affiché pour les colonnes de type `STATUS`, `CATEGORY`, `LEVEL`
- [ ] Affiche un aperçu des valeurs possibles (chips MUI)
- [ ] Pour `CATEGORY` : champ pour entrer des valeurs personnalisées (TextField multiline ou Tags input)
- [ ] Le composant s'intègre dans `ConfigurationPanel.jsx` avec les autres champs

### AC5 — Détection automatique depuis CSV
- [ ] `DataTypeDetectionService` reconnaît les colonnes nommées `statut`, `status`, `état` → type `STATUS`
- [ ] Reconnaît `catégorie`, `categorie`, `category`, `type` → type `CATEGORY`
- [ ] Reconnaît `niveau`, `level`, `grade`, `rang` → type `LEVEL`

### AC6 — Tests
- [ ] Tests unitaires pour les 3 générateurs (min 3 tests chacun)
- [ ] Test d'intégration : génération complète avec colonnes catégorielles
- [ ] Tests Jest pour `CategoricalFieldConfig.jsx` (render + interaction)
- [ ] Coverage global maintenu > 80%

---

## 🏗️ Spécifications Techniques

### Nouveaux fichiers à créer

```
backend/
  src/main/java/com/movkfact/service/generator/categorical/
    StatusGenerator.java
    CategoryGenerator.java
    LevelGenerator.java

frontend/
  src/components/DataConfigurationPanel/
    CategoricalFieldConfig.jsx
    CategoricalFieldConfig.test.js
```

### Modifications requises

```
backend/
  src/main/java/com/movkfact/enums/ColumnType.java
    + STATUS("categorical", "Statut")
    + CATEGORY("categorical", "Catégorie")
    + LEVEL("categorical", "Niveau")

  src/main/java/com/movkfact/service/generator/GeneratorFactory.java
    + case STATUS: return new StatusGenerator(columnConfig);
    + case CATEGORY: return new CategoryGenerator(columnConfig);
    + case LEVEL: return new LevelGenerator(columnConfig);

  src/main/java/com/movkfact/service/DataTypeDetectionService.java
    + Patterns: statut/status/état → STATUS
    + Patterns: catégorie/category/type → CATEGORY
    + Patterns: niveau/level/grade → LEVEL

frontend/
  src/components/DataConfigurationPanel/ConfigurationPanel.jsx
    + Importer CategoricalFieldConfig
    + Ajouter rendu conditionnel pour STATUS/CATEGORY/LEVEL
```

### StatusGenerator — Logique de pondération

```java
// Weighted random selection
double rand = random.nextDouble();
if (rand < 0.60) return "actif";
else if (rand < 0.80) return "inactif";
else if (rand < 0.95) return "en_attente";
else return "suspendu";
```

### CategoryGenerator — Support listes custom

```java
// Check additionalConfig for custom values
// Format: {"values": ["val1", "val2", "val3"]}
// Si absent → utiliser FRENCH_CATEGORIES par défaut
```

---

## 📝 Dev Notes

- Respecter le pattern existant : étendre `DataTypeGenerator`, implémenter `generate()`
- Créer le package `categorical/` dans `service/generator/` (même structure que `personal/`, `financial/`, `temporal/`)
- Pour la pondération dans `StatusGenerator` et `LevelGenerator`, utiliser `random.nextDouble()` (déjà importé dans la base)
- `CategoryGenerator` doit appeler `columnConfig.getAdditionalConfig()` pour récupérer les valeurs custom — voir `getConfigValue()` dans `DataTypeGenerator` (actuellement stub, peut être complété ici)
- Frontend : `CategoricalFieldConfig` doit suivre le même pattern que `PersonalFieldConfig.jsx` et `FinancialFieldConfig.jsx`

---

## 📊 Estimation

| Composant | Effort | Responsable |
|-----------|--------|-------------|
| 3 générateurs Java | 1j | Amelia |
| Mise à jour ColumnType + Factory + Detection | 0.5j | Amelia |
| Tests backend | 0.5j | Amelia |
| `CategoricalFieldConfig.jsx` + tests | 1j | Sally |
| Intégration ConfigurationPanel | 0.5j | Sally |
| **Total** | **3.5j** | **5 pts** |
