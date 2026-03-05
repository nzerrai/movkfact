# Story S7.1: Backend Wizard Support

**Sprint:** À planifier (Epic 7)
**Points:** 5
**Epic:** EPIC 7 - Wizard de création manuelle
**Type:** Backend Feature
**Lead:** Amelia
**Status:** Backlog
**Dependencies:** S2.1 (DataGeneratorService), S2.3 (DataGenerationController)
**FRs couverts:** FR-004 (chemin manual), FR-006 (étape 3 preview), FR-008 (contraintes dynamiques)

---

## User Story

**En tant que** ingénieur QA ou développeur,
**Je veux** pouvoir prévisualiser 5 lignes générées à partir d'une configuration manuelle avant de lancer la génération complète,
**Afin de** valider la cohérence des types et contraintes sans attendre la génération de milliers de lignes.

---

## Contexte métier

Le flux CSV existant (S2.2/S2.5) permet l'auto-détection, mais aucun flux "from scratch" n'existe.
Le wizard en 4 étapes (FR-006) requiert :
1. Un endpoint `preview` : génère 5 lignes sans persistance (étape 3 du wizard)
2. Une extension des contraintes dynamiques dans l'API de génération (min/max, dateFrom/dateTo, maxLength) — actuellement les colonnes acceptent un `type` mais les contraintes sont limitées

---

## ✅ Acceptance Criteria

### AC1 — Endpoint Preview (5 lignes sans persistance)
- [ ] `POST /api/datasets/preview` accepte `{ columns: [{name, type, config}], count: 5 }`
- [ ] Génère exactement 5 lignes via `DataGeneratorService` sans créer de `DataSet` en base
- [ ] Réponse : `{ previewRows: [{...}, ...], columnCount: N }` (tableau de 5 objets)
- [ ] Temps de réponse < 500ms (5 lignes uniquement)
- [ ] Supporte tous les types existants + les contraintes étendues (AC2)
- [ ] `count` limité à 5 maximum (ignorer une valeur supérieure, retourner 5)

### AC2 — Contraintes dynamiques étendues dans ColumnConfig
- [ ] `ColumnConfigDTO` étendu avec champ `constraints` : `Map<String, Object>`
- [ ] Contraintes supportées par type :
  - `INTEGER` / `DECIMAL` : `{ "min": number, "max": number }`
  - `DATE` / temporels : `{ "dateFrom": "YYYY-MM-DD", "dateTo": "YYYY-MM-DD" }`
  - `TEXT` / `LOREM_IPSUM` : `{ "maxLength": number }` (longueur max en caractères)
  - `FIRST_NAME`, `EMAIL`, `BOOLEAN` : aucune contrainte (ignorées si présentes)
- [ ] Les générateurs existants (`AmountGenerator`, `DateGenerator`, etc.) lisent les contraintes depuis `ColumnConfig.additionalConfig`
- [ ] Contraintes invalides (min > max, dateFrom > dateTo) → 400 Bad Request avec message explicite

### AC3 — Endpoint génération standard enrichi (pas de breaking change)
- [ ] `POST /api/datasets/generate` (existant) accepte les contraintes de AC2 sans régression
- [ ] Tests existants passent sans modification (0 régression)

### AC4 — Validation des inputs wizard
- [ ] Nom de dataset : 3–50 caractères, `[a-zA-Z0-9 _-]` uniquement
- [ ] Nombre de lignes : 1–100 000
- [ ] Chaque colonne : nom non vide (1–50 chars), type valide (enum `ColumnType`)
- [ ] Minimum 1 colonne requise
- [ ] Messages d'erreur explicites (400 avec détail par champ)

### AC4b — Validation type de colonne inconnu *(ajouté party mode 04/03)*
- [ ] `POST /api/datasets/preview` retourne `400 Bad Request` si un `type` de colonne n'existe pas dans l'enum `ColumnType`
- [ ] Message d'erreur : `"Type inconnu : 'XYZ'. Types valides : [FIRST_NAME, EMAIL, INTEGER, ...]"`
- [ ] Intercepté via `@ExceptionHandler(IllegalArgumentException.class)` dans `GlobalExceptionHandler` (pas de NullPointerException propagé)
- [ ] Test d'intégration : `POST /api/datasets/preview` avec `type: "INVALID_TYPE"` → 400 avec message explicite

### AC5 — Tests
- [ ] Test unitaire `PreviewService` : 5 lignes générées, bon type, contraintes respectées
- [ ] Test d'intégration `POST /api/datasets/preview` (succès, trop de colonnes, contrainte invalide)
- [ ] Tests de régression sur `POST /api/datasets/generate` avec contraintes
- [ ] Coverage >80% sur les nouveaux composants

---

## 🏗️ Spécifications Techniques

### Nouveaux fichiers à créer

```
src/main/java/com/movkfact/
  service/
    DataPreviewService.java          ← génère N lignes sans persistance
  dto/
    PreviewRequestDTO.java           ← { columns: List<ColumnConfigDTO>, count: int }
    PreviewResponseDTO.java          ← { previewRows: List<Map<String,Object>>, columnCount: int }
```

### Modifications requises

```
src/main/java/com/movkfact/dto/ColumnConfigDTO.java
  + Map<String, Object> constraints   ← contraintes dynamiques par type

src/main/java/com/movkfact/service/generator/financial/AmountGenerator.java
  → lire constraints.get("min") / constraints.get("max")

src/main/java/com/movkfact/service/generator/temporal/DateGenerator.java
  → lire constraints.get("dateFrom") / constraints.get("dateTo")

src/main/java/com/movkfact/controller/DataGenerationController.java
  + POST /api/datasets/preview   ← nouveau endpoint
```

### DataPreviewService — Logique clé

```java
// Réutilise DataGeneratorService sans persistance
public PreviewResponseDTO generatePreview(PreviewRequestDTO request) {
    // Validation des contraintes (min < max, dateFrom < dateTo, etc.)
    validateConstraints(request.getColumns());

    int count = Math.min(request.getCount(), 5);
    List<Map<String, Object>> rows = new ArrayList<>();

    for (int i = 0; i < count; i++) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (ColumnConfigDTO col : request.getColumns()) {
            DataTypeGenerator gen = generatorFactory.create(col);
            row.put(col.getName(), gen.generate());
        }
        rows.add(row);
    }

    return new PreviewResponseDTO(rows, request.getColumns().size());
}
```

### Contraintes dans les générateurs — Exemple AmountGenerator

```java
// Lecture depuis ColumnConfig.additionalConfig (déjà existant, compléter)
double min = getConfigDouble("min", 0.0);
double max = getConfigDouble("max", 10000.0);
if (min > max) throw new IllegalArgumentException("min doit être <= max");
return BigDecimal.valueOf(min + random.nextDouble() * (max - min))
    .setScale(2, RoundingMode.HALF_UP);
```

---

## 📝 Dev Notes

- `DataPreviewService` réutilise `GeneratorFactory` et les générateurs existants — aucune duplication
- Le champ `constraints` dans `ColumnConfigDTO` est additionnel et optionnel — aucun test existant ne devrait casser
- `additionalConfig` existe déjà dans `ColumnConfig` entity (comme Map ou String JSON) — vérifier le format exact avant implémentation
- Le générateur `LOREM_IPSUM` n'existe pas encore — si non disponible, retourner une chaîne de mots aléatoires tronquée à `maxLength`
- `TEXT` avec `maxLength` peut être un substring d'une valeur `LOREM_IPSUM` ou une chaîne aléatoire

---

## 📊 Estimation

| Composant | Effort | Responsable |
|-----------|--------|-------------|
| `DataPreviewService` + `PreviewRequestDTO/ResponseDTO` | 0.5j | Amelia |
| Extension contraintes `ColumnConfigDTO` + générateurs | 1j | Amelia |
| Validation inputs (400 avec messages) | 0.25j | Amelia |
| Tests unitaires + intégration | 1j | Amelia |
| **Total** | **2.75j** | **5 pts** |
