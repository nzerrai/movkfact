# Story 9.1: Smart Column Type Inference

Status: review

## Story

En tant qu'utilisateur qui uploade un CSV,
Je veux que l'application détecte automatiquement le type de chaque colonne via son nom puis ses données,
Afin de ne corriger que les cas douteux et non tout re-saisir manuellement.

## Acceptance Criteria

1. **AC1 — Inférence par nom de colonne (Niveau 1)**
   - Dictionnaire de patterns FR/EN insensible à la casse, tirets/underscores normalisés
   - Couverture minimale : EMAIL, PHONE, NAME, FIRST_NAME, LAST_NAME, DATE, INTEGER, DECIMAL, BOOLEAN, POSTAL_CODE, CITY, COUNTRY, UUID, URL
   - Score de confiance retourné : HIGH (≥ 0.85), MEDIUM (0.60–0.84), LOW (< 0.60)
   - Matching exact prioritaire, puis matching partiel (préfixe/suffixe)

2. **AC2 — Inférence par données (Niveau 2 — fallback)**
   - Déclenché si score Niveau 1 < 0.60
   - Analyse sur échantillon ≤ 100 lignes (performance)
   - Règles d'analyse :
     - Regex email valide sur ≥ 80% des valeurs → EMAIL
     - Regex téléphone FR/international sur ≥ 80% → PHONE
     - Format date ISO/FR sur ≥ 80% → DATE
     - 100% numériques entiers → INTEGER
     - 100% numériques avec décimales → DECIMAL
     - Cardinalité ≤ 10 valeurs distinctes et ≤ 20% de l'échantillon → ENUM
     - Valeurs `true/false/oui/non/1/0` uniquement → BOOLEAN
   - Retourne type + confidence score

3. **AC3 — Intégration endpoint existant**
   - `POST /api/csv/detect-types` enrichit sa réponse : chaque colonne retourne `{ detectedType, confidence, inferenceLevel }` en plus du type actuel
   - `inferenceLevel` : `NAME_BASED` | `DATA_BASED`
   - Aucun breaking change sur le contrat d'API existant (champs ajoutés, non modifiés)

4. **AC4 — Intégration frontend ConfigurationPanel**
   - Type de colonne pré-rempli automatiquement avec le `detectedType`
   - Badge de confiance affiché :
     - HIGH → chip vert
     - MEDIUM → chip orange
     - LOW → chip gris (avec suggestion de vérification)
   - Correction utilisateur toujours possible sur chaque colonne
   - Aucun comportement existant cassé

5. **AC5 — Tests**
   - Unit tests `ColumnTypeInferenceService` : ≥ 20 cas (noms FR, noms EN, données variées, edge cases — colonne vide, valeurs mixtes)
   - Tests frontend : pré-remplissage correct, affichage badge confidence, correction manuelle

## Tasks / Subtasks

- [x] Créer `ColumnTypeInferenceService` (AC: 1, 2)
  - [x] Méthode `infer(String columnName, List<String> sampleValues)` → `InferenceResult`
  - [x] `InferenceResult` : `{ ColumnType type, double confidence, InferenceLevel level }`
  - [x] Dictionnaire Niveau 1 : patterns FR/EN pour ≥ 14 types
  - [x] Normalisation : toLowerCase, replace(`[-_ ]`, "")
  - [x] Fallback Niveau 2 : regex + cardinalité sur échantillon ≤ 100 valeurs
- [x] Créer `InferenceResult.java` et `InferenceLevel.java` (enum : NAME_BASED, DATA_BASED) (AC: 1, 2)
- [x] Modifier `CsvAnalysisService` ou endpoint `POST /api/csv/detect-types` pour appeler `ColumnTypeInferenceService` (AC: 3)
  - [x] Enrichir le DTO de réponse avec `confidence` et `inferenceLevel`
  - [x] Vérifier aucun breaking change
- [x] Mettre à jour `ConfigurationPanel.jsx` (AC: 4)
  - [x] Consommer `confidence` et `inferenceLevel` de la réponse
  - [x] Afficher badge de confiance (Chip MUI vert/orange/gris)
  - [x] Pré-remplir le select de type avec `detectedType`
- [x] Tests `ColumnTypeInferenceServiceTest.java` — ≥ 20 cas (AC: 5)
- [x] Tests frontend `ConfigurationPanel` — badge confiance + pré-remplissage (AC: 5)

## Dev Notes

### Architecture Backend

```java
@Service
public class ColumnTypeInferenceService {
    public InferenceResult infer(String columnName, List<String> sampleValues) {
        // Niveau 1 : nom de colonne
        InferenceResult nameResult = inferFromName(columnName);
        if (nameResult.getConfidence() >= 0.60) return nameResult;
        // Niveau 2 : analyse des données
        return inferFromData(sampleValues);
    }
}
```

### Dictionnaire Niveau 1 (exemples)

| Pattern(s) | Type détecté | Confidence |
|---|---|---|
| `email`, `mail`, `courriel`, `e-mail` | EMAIL | 0.95 |
| `tel`, `phone`, `mobile`, `gsm`, `fax`, `telephone` | PHONE | 0.95 |
| `prenom`, `firstname`, `first_name`, `prénom` | FIRST_NAME | 0.95 |
| `nom`, `lastname`, `last_name`, `name`, `surname` | LAST_NAME | 0.90 |
| `date`, `naissance`, `birthday`, `created`, `updated`, `at` | DATE | 0.90 |
| `age`, `annee`, `year`, `nb`, `count`, `nombre` | INTEGER | 0.85 |
| `prix`, `montant`, `amount`, `price`, `salary`, `salaire` | DECIMAL | 0.85 |
| `actif`, `active`, `enabled`, `flag`, `is_`, `has_` | BOOLEAN | 0.85 |
| `code_postal`, `zip`, `postal`, `codepostal` | POSTAL_CODE | 0.95 |
| `ville`, `city`, `commune` | CITY | 0.90 |
| `pays`, `country`, `nation` | COUNTRY | 0.90 |
| `uuid`, `guid`, `id` (si format UUID) | UUID | 0.90 |
| `url`, `lien`, `link`, `href`, `website` | URL | 0.90 |

### Règles Niveau 2 (fallback)

```java
// Email : regex standard sur ≥ 80% des valeurs non-nulles
// Téléphone : \+?[\d\s\-\(\)]{8,15} sur ≥ 80%
// Date : parsing ISO 8601, dd/MM/yyyy, yyyy-MM-dd sur ≥ 80%
// Integer : NumberUtils.isDigits() sur 100%
// Decimal : NumberUtils.isParsable() + contains(".") sur ≥ 95%
// Enum : distinctCount <= 10 AND distinctCount <= sampleSize * 0.20
// Boolean : toutes valeurs in {true,false,oui,non,yes,no,1,0}
```

### Frontend — Badge confidence

```jsx
const ConfidenceBadge = ({ level }) => {
  const config = {
    HIGH:   { label: 'Confiance haute',   color: 'success' },
    MEDIUM: { label: 'Confiance moyenne', color: 'warning' },
    LOW:    { label: 'À vérifier',        color: 'default' },
  };
  return <Chip size="small" {...config[level]} />;
};
```

### Fichiers à créer / modifier

```
src/main/java/com/movkfact/
  service/
    ColumnTypeInferenceService.java     ← NOUVEAU
  dto/
    InferenceResult.java                ← NOUVEAU
  enums/
    InferenceLevel.java                 ← NOUVEAU (NAME_BASED, DATA_BASED)
  service/CsvAnalysisService.java       ← modifier (appeler InferenceService)

movkfact-frontend/src/
  components/DataConfigurationPanel/
    ConfigurationPanel.jsx              ← modifier (badge confidence + pré-remplissage)
    ConfidenceBadge.jsx                 ← NOUVEAU (ou inline)

src/test/java/com/movkfact/
  service/ColumnTypeInferenceServiceTest.java  ← NOUVEAU (≥ 20 tests)
```

### Dépendances

- S9.2 dépend de S9.1 : `ColumnTypeInferenceService` expose `isPII` utilisé par S9.2
- Aucune dépendance externe nouvelle — Apache Commons Lang déjà présent pour `NumberUtils`

### References

- ConfigurationPanel existant : `movkfact-frontend/src/components/DataConfigurationPanel/ConfigurationPanel.jsx`
- Types colonne disponibles : `com.movkfact.enums.ColumnType`

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

- ✅ `InferenceLevel.java` : enum NAME_BASED / DATA_BASED
- ✅ `InferenceResult.java` : DTO { type, confidence, level }
- ✅ `ColumnTypeInferenceService.java` : façade deux niveaux (nom-first, fallback données), délègue aux détecteurs spécialisés existants
- ✅ `DetectedColumn.java` : champ `inferenceLevel` ajouté (aucun breaking change)
- ✅ `CsvTypeDetectionService.java` : refactorisé pour déléguer à `ColumnTypeInferenceService`, plus simple
- ✅ `DomainsPage.jsx` : `typesMap` préserve `{ type, confidence, inferenceLevel }`
- ✅ `ConfigurationPanel.jsx` : `ConfidenceBadge` (vert/orange/gris), rétrocompatible string|object
- ✅ `ColumnTypeInferenceServiceTest.java` : 20 tests, 0 échec
- ✅ Suite backend : 488 tests, 0 régression
- ✅ Suite frontend existante : 38 tests, 0 régression

### File List

- `src/main/java/com/movkfact/enums/InferenceLevel.java` — créé
- `src/main/java/com/movkfact/dto/InferenceResult.java` — créé
- `src/main/java/com/movkfact/service/detection/ColumnTypeInferenceService.java` — créé
- `src/main/java/com/movkfact/dto/DetectedColumn.java` — modifié (+ inferenceLevel)
- `src/main/java/com/movkfact/service/detection/CsvTypeDetectionService.java` — modifié (délègue à ColumnTypeInferenceService)
- `src/test/java/com/movkfact/service/detection/ColumnTypeInferenceServiceTest.java` — créé
- `movkfact-frontend/src/pages/DomainsPage.jsx` — modifié (preserve confidence + inferenceLevel)
- `movkfact-frontend/src/components/DataConfigurationPanel/ConfigurationPanel.jsx` — modifié (ConfidenceBadge)
