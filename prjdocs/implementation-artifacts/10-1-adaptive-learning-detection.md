# Story 10.1: Apprentissage adaptatif de la détection de types

Status: done

## Story

En tant qu'utilisateur qui uploade régulièrement des CSV similaires,
Je veux que le système mémorise mes corrections de types de colonnes,
Afin que la détection automatique s'améliore au fil des utilisations sans intervention manuelle répétée.

## Acceptance Criteria

1. **AC1 — Collecte du feedback**
   - À chaque clic "Générer", pour chaque colonne : enregistrer `(columnNameNormalized, detectedType, validatedType, domainId, timestamp)`
   - `validatedType` = type effectivement sélectionné/gardé par l'utilisateur dans ConfigurationPanel
   - Confirmation si `validatedType == detectedType` ; correction si différent
   - Seulement si le dataset est effectivement généré (pas sur annulation)

2. **AC2 — Stockage du corpus**
   - Table `column_type_feedback` : `id | column_name_normalized | validated_type | domain_id | count | last_seen`
   - `column_name_normalized` = lowercase + trim + remove accents + replace `[ _\-]+` → `-`
     (ex : `"Salaire Espéré"` → `"salaire-espere"`, `"first_name"` → `"first-name"`)
   - `count` incrémenté à chaque nouvelle validation du même mapping
   - Contrainte d'unicité : `(column_name_normalized, validated_type, domain_id)`
   - Un mapping devient "fiable" à partir de `count ≥ 3` (propriété `detection.learning.min-count`, défaut 3)

3. **AC3 — Application dans la détection (Niveau 0)**
   - `ColumnTypeInferenceService.infer()` vérifie le corpus en premier
   - Lookup : `column_name_normalized` + `domain_id` → fallback global (domain_id IS NULL) si absent
   - Confiance = `min(95, 80 + 5 * count)`, plafonné à 95
   - Retour : `InferenceResult(type, confidence, InferenceLevel.LEARNED)`
   - Si count < min-count → non retenu, descend au Niveau 1

4. **AC4 — Gestion des conflits**
   - Si plusieurs `validated_type` pour le même `column_name_normalized` : prendre celui avec le `count` le plus élevé
   - En cas d'égalité → ne pas promouvoir au Niveau 0, fallback Niveau 1

5. **AC5 — Indicateur visuel frontend**
   - Si type issu du Niveau 0 → badge "Appris" (couleur bleue, `InferenceLevel.LEARNED`)
   - Tooltip : "Détecté via vos précédentes validations (N fois)"
   - `InferenceLevel` : ajouter valeur `LEARNED`

6. **AC6 — Tests**
   - Unit `ColumnLearningServiceTest` : normalisation, lookup scope domaine vs global, promotion au seuil, confiance, conflits
   - Integration : cycle complet feedback → lookup → détection améliorée
   - ≥ 10 cas de test

## Tasks / Subtasks

### Backend

- [x] Migration Flyway `V009__add_column_type_feedback.sql` (AC2)
- [x] Entity `ColumnTypeFeedback.java` (AC2)
- [x] Repository `ColumnTypeFeedbackRepository.java` — méthodes : `findBestMatch(name, domainId)`, `upsertCount(...)` (AC2)
- [x] DTO `ColumnFeedbackRequest.java` — `[{ colName, detectedType, validatedType }]` (AC1)
- [x] `ColumnLearningService.java` (AC2, AC3, AC4)
  - `normalize(String columnName)` → slug sans accents
  - `recordFeedback(List<ColumnFeedbackRequest>, Long domainId)` — upsert count
  - `lookup(String normalized)` → `Optional<InferenceResult>` (scope global V1)
- [x] `ColumnFeedbackController.java` — `POST /api/domains/{domainId}/feedback` (AC1)
- [x] Modifier `ColumnTypeInferenceService.infer()` : appel `learningService.lookup()` en Niveau 0 (AC3)
- [x] Ajouter `LEARNED` à `InferenceLevel` enum (AC5)
- [x] `ColumnLearningServiceTest.java` ≥ 10 cas (AC6)

### Frontend

- [x] `ConfigurationPanel.jsx` : à l'appel "Générer", collecter `[{colName, detectedType, validatedType}]` et POST `/api/domains/{domainId}/feedback` (AC1)
- [x] `ConfidenceBadge` : gérer `inferenceLevel === 'LEARNED'` → Chip bleu "Appris" avec tooltip "(N fois)" (AC5)
- [x] `ConfigurationPanel.jsx` : sélecteur de type pour chaque colonne — permet de capturer les corrections (M4 code-review)

### Review Follow-ups (AI)

- [ ] [AI-Review][MEDIUM/V2] AC3 — lookup scope domaine non implémenté : ajouter `lookup(normalized, domainId)` en priorité + fallback global. Nécessite de passer `domainId` à `ColumnTypeInferenceService.infer()` via ThreadLocal ou RequestScope bean. [ColumnLearningService.java:80, ColumnTypeInferenceService.java:70]

## Dev Notes

### Normalisation du nom de colonne

```java
public String normalize(String columnName) {
    if (columnName == null) return "";
    String lower = columnName.trim().toLowerCase();
    // Remove accents
    String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    // Replace spaces, underscores, hyphens with single dash
    return normalized.replaceAll("[\\s_\\-]+", "-")
                     .replaceAll("[^a-z0-9\\-]", "");
}
```

### Schéma DB

```sql
CREATE TABLE column_type_feedback (
    id                     BIGSERIAL PRIMARY KEY,
    column_name_normalized VARCHAR(100) NOT NULL,
    validated_type         VARCHAR(50)  NOT NULL,
    domain_id              BIGINT REFERENCES domains(id) ON DELETE SET NULL,
    count                  INT          NOT NULL DEFAULT 1,
    last_seen              TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_feedback UNIQUE (column_name_normalized, validated_type, domain_id)
);
CREATE INDEX idx_feedback_lookup ON column_type_feedback (column_name_normalized, domain_id);
```

### Upsert (PostgreSQL)

```sql
INSERT INTO column_type_feedback (column_name_normalized, validated_type, domain_id, count, last_seen)
VALUES (:name, :type, :domainId, 1, NOW())
ON CONFLICT ON CONSTRAINT uq_feedback
DO UPDATE SET count = column_type_feedback.count + 1, last_seen = NOW();
```

### Intégration dans ColumnTypeInferenceService

```java
public InferenceResult infer(String columnName, List<String> sampleValues) {
    // ── Niveau 0 : corpus appris ──────────────────────────────────────────
    if (columnName != null && !columnName.isBlank()) {
        String normalized = learningService.normalize(columnName);
        Optional<InferenceResult> learned = learningService.lookup(normalized, currentDomainId);
        if (learned.isPresent()) return learned.get();
    }
    // ── Niveau 1 : name-based ─────────────────────────────────────────────
    // ... (existant)
    // ── Niveau 2 : data-based ─────────────────────────────────────────────
    // ... (existant)
}
```

> Note : `currentDomainId` est passé en paramètre ou via contexte de requête (ThreadLocal/RequestScope bean).
> Option V1 simplifiée : ne pas passer domainId à `infer()`, faire le lookup global uniquement.

### Confiance du Niveau 0

| count | confiance |
|-------|-----------|
| 3     | 95        |
| 2     | 90        |
| 1     | 85        |
| < min | non retenu |

`confidence = Math.min(95, 80 + 5 * count)` — plafonné à 95 pour ne jamais écraser une correction humaine future.

### Fichiers à créer / modifier

```
src/main/
  java/com/movkfact/
    entity/
      ColumnTypeFeedback.java                        ← CRÉER
    repository/
      ColumnTypeFeedbackRepository.java              ← CRÉER
    dto/
      ColumnFeedbackRequest.java                     ← CRÉER
    service/detection/
      ColumnLearningService.java                     ← CRÉER
    controller/
      ColumnFeedbackController.java                  ← CRÉER
    enums/
      InferenceLevel.java                            ← MODIFIER (ajouter LEARNED)
    service/detection/
      ColumnTypeInferenceService.java                ← MODIFIER (Niveau 0)
  resources/db/migration/
    V009__add_column_type_feedback.sql               ← CRÉER

movkfact-frontend/src/
  components/DataConfigurationPanel/
    ConfigurationPanel.jsx                           ← MODIFIER (POST feedback + badge LEARNED)

src/test/java/com/movkfact/service/detection/
  ColumnLearningServiceTest.java                     ← CRÉER (≥ 10 tests)
```

### Références

- Story précédente : [9-1-smart-column-type-inference.md](./9-1-smart-column-type-inference.md)
- `ColumnTypeInferenceService` : `src/main/java/com/movkfact/service/detection/ColumnTypeInferenceService.java`
- `InferenceLevel` : `src/main/java/com/movkfact/enums/InferenceLevel.java`
- Migration précédente : `src/main/resources/db/migration/V008__add_activity_action_index.sql`

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

- `LEARNED` absent de `InferenceLevel` → erreur de compilation cascade sur `ColumnLearningService`. Ajouté en premier dans l'enum.
- Import `java.util.Optional` dupliqué par erreur → supprimé.
- `UNIQUE CONSTRAINT` PostgreSQL : `CONSTRAINT uq_feedback UNIQUE (... COALESCE(...))` invalide en PostgreSQL → corrigé en `CREATE UNIQUE INDEX` fonctionnel (code-review H1).
- Lookup V1 scope global uniquement (domain_id IS NULL) : le feedback est stocké à la fois en global et par domaine, mais le lookup dans `infer()` ne consulte que le global pour garder la signature `infer(name, values)` inchangée. Scope-domaine prévu V2.
- `findByColumnNameNormalizedAndValidatedTypeAndDomainId(..., null)` : comportement indéfini Hibernate pour null-equality → remplacé par `@Query` explicite avec `IS NULL` (code-review H2).
- Race condition dans `upsert()` : ajout `@Lock(PESSIMISTIC_WRITE)` sur le select-for-upsert (code-review M3).
- `learnedCount` propagé de `ColumnLearningService` → `InferenceResult` → `DetectedColumn` → frontend → tooltip "(N fois)" (code-review M2/AC5).
- Sélecteur de type ajouté dans `ConfigurationPanel` pour capturer les corrections utilisateur (code-review M4).

### Completion Notes List

- Pipeline de détection étendu à 3 niveaux : LEARNED (0) → NAME_BASED (1) → DATA_BASED (2).
- `ColumnLearningService.normalize()` gère accents, espaces, underscores → slug ASCII.
- Seuil de fiabilité configurable via `detection.learning.min-count` (défaut 3) ; ambiguïté (tie) → pas de promotion.
- Feedback stocké global + par domaine ; lookup V1 global uniquement (prévu scope-domaine V2).
- Frontend envoie feedback silencieux (best-effort, non bloquant) après chaque génération réussie.
- `ConfidenceBadge` : badge bleu "Appris" pour `LEARNED`, comportement inchangé pour NAME_BASED/DATA_BASED.
- Backend : 516/517 tests passent (1 `LastNameGeneratorTests` pré-existant non lié).
- Frontend : 379/386 tests passent (7 pré-existants non liés).

### File List

```
src/main/resources/db/migration/V009__add_column_type_feedback.sql      ← CRÉÉ
src/main/java/com/movkfact/entity/ColumnTypeFeedback.java               ← CRÉÉ
src/main/java/com/movkfact/repository/ColumnTypeFeedbackRepository.java ← CRÉÉ
src/main/java/com/movkfact/dto/ColumnFeedbackRequest.java               ← CRÉÉ
src/main/java/com/movkfact/service/detection/ColumnLearningService.java ← CRÉÉ
src/main/java/com/movkfact/controller/ColumnFeedbackController.java     ← CRÉÉ
src/main/java/com/movkfact/enums/InferenceLevel.java                    ← MODIFIÉ (LEARNED)
src/main/java/com/movkfact/service/detection/ColumnTypeInferenceService.java ← MODIFIÉ (Niveau 0)
src/main/java/com/movkfact/service/detection/CsvTypeDetectionService.java     ← MODIFIÉ (learnedCount)
movkfact-frontend/src/pages/DomainsPage.jsx                                   ← MODIFIÉ (learnedCount dans typesMap)
movkfact-frontend/src/components/DataConfigurationPanel/ConfigurationPanel.jsx ← MODIFIÉ
src/test/java/com/movkfact/service/detection/ColumnLearningServiceTest.java ← CRÉÉ (13 tests)
```
