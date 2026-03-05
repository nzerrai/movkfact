# Story 6.1: Backend Row Editor API

Status: done

## Story

En tant que développeur ou Tech Lead,
Je veux pouvoir lire, modifier et supprimer des lignes individuelles d'un dataset via l'API,
so that je puisse corriger des valeurs incorrectes sans régénérer l'intégralité du dataset, avec une traçabilité complète de chaque opération.

## Acceptance Criteria

1. `GET /api/data-sets/{id}/rows?page=0&size=50` retourne les lignes paginées : `{ rows: [{rowIndex: 0, data: {...}}, ...], totalRows: N, page: X, size: Y }`. 404 si dataset inexistant ou soft-deleted. < 200ms pour 10 000 lignes.
2. `GET /api/data-sets/{id}/rows/{rowIndex}` retourne `{ rowIndex: N, data: {...} }`. 404 si rowIndex hors bornes.
3. `PUT /api/data-sets/{id}/rows/{rowIndex}` accepte `{ columns: { "colName": "newValue" } }`, merge partiel (ne remplace pas les colonnes non mentionnées), incrémente `version`, met à jour `updatedAt` via `@PreUpdate`. Retourne la ligne mise à jour. 400 si `columns` vide/null. 404 si dataset ou rowIndex inexistant.
4. `DELETE /api/data-sets/{id}/rows/{rowIndex}` supprime la ligne, compacte le tableau (les indices suivants décrémentent), décrémente `rowCount`, incrémente `version`. 204 No Content. 404 si dataset ou rowIndex inexistant.
5. `ActivityActionType` étendu avec `ROW_MODIFIED` et `ROW_DELETED`. `Activity` étendu avec `rowIndex` (Integer nullable), `modifiedColumns` (String nullable — JSON array), `previousValue` (String nullable — JSON snapshot). Chaque PUT génère `Activity(ROW_MODIFIED)` avec snapshot partiel des colonnes modifiées. Chaque DELETE génère `Activity(ROW_DELETED)` avec snapshot complet de la ligne. `originalData` jamais modifiée.
6. Tests unitaires `DataRowEditorService` >80% coverage. Tests intégration pour les 4 endpoints. Test de régression reset : `POST /api/data-sets/{id}/reset` restaure l'état original après modifications.
7. `PUT` et `DELETE` retournent `413 Payload Too Large` si `dataset.rowCount > 50 000`. Message : `"Dataset trop volumineux pour édition ligne par ligne. Utilisez l'API bulk ou régénérez."`. GET non affecté.
8. Flyway `V006__add_row_editor_activity_columns.sql` avec colonnes `NULLABLE`. Les Activity existantes (CREATED, DOWNLOADED, etc.) ont `row_index = NULL` sans erreur.

## Tasks / Subtasks

- [x] Tâche 1 — Étendre `ActivityActionType` et `Activity` (AC: #5, #8)
  - [x] Ajouter `ROW_MODIFIED` et `ROW_DELETED` dans `ActivityActionType.java`
  - [x] Ajouter 3 champs dans `Activity.java` : `rowIndex` (Integer), `modifiedColumns` (String @Column TEXT), `previousValue` (String @Column(columnDefinition="TEXT"))
  - [x] Ajouter constructeur surchargé : `Activity(Long dataSetId, ActivityActionType action, String userName, Integer rowIndex, String modifiedColumns, String previousValue)`
  - [x] Ajouter getters/setters pour les 3 nouveaux champs

- [x] Tâche 2 — Migration Flyway V006 (AC: #8)
  - [x] Créer `src/main/resources/db/migration/V006__add_row_editor_activity_columns.sql`
  - [x] Syntaxe PostgreSQL : `ALTER TABLE activity ADD COLUMN row_index INTEGER;` (nullable par défaut)
  - [x] `ALTER TABLE activity ADD COLUMN modified_columns TEXT;`
  - [x] `ALTER TABLE activity ADD COLUMN previous_value TEXT;`

- [x] Tâche 3 — Étendre `ActivityService` (AC: #5)
  - [x] Ajouter méthode `recordRowActivity(Long dataSetId, ActivityActionType action, String userName, Integer rowIndex, String modifiedColumns, String previousValue)`
  - [x] Créer `Activity` avec le nouveau constructeur surchargé, sauvegarder via `activityRepository.save()`

- [x] Tâche 4 — Créer les DTOs (AC: #1, #2, #3)
  - [x] `RowUpdateRequestDTO.java` : champ `Map<String, Object> columns` avec `@NotNull @NotEmpty`
  - [x] `RowResponseDTO.java` : champs `int rowIndex` + `Map<String, Object> data`
  - [x] `PagedRowsResponseDTO.java` : champs `List<RowResponseDTO> rows`, `int totalRows`, `int page`, `int size`

- [x] Tâche 5 — Créer `DataRowEditorService` (AC: #1-5, #7)
  - [x] Implémenter `getRows(Long datasetId, int page, int size)` → `PagedRowsResponseDTO`
  - [x] Implémenter `getRow(Long datasetId, int rowIndex)` → `RowResponseDTO`
  - [x] Implémenter `updateRow(Long datasetId, int rowIndex, Map<String, Object> columns)` → `RowResponseDTO`
  - [x] Implémenter `deleteRow(Long datasetId, int rowIndex)` → void
  - [x] Circuit breaker 413 dans `updateRow` et `deleteRow` si `rowCount > 50_000`
  - [x] NE PAS modifier `originalData`

- [x] Tâche 6 — Créer `DataRowEditorController` (AC: #1-4, #7)
  - [x] `GET /api/data-sets/{id}/rows` avec `@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size`
  - [x] `GET /api/data-sets/{id}/rows/{rowIndex}`
  - [x] `PUT /api/data-sets/{id}/rows/{rowIndex}` avec `@Valid @RequestBody RowUpdateRequestDTO`
  - [x] `DELETE /api/data-sets/{id}/rows/{rowIndex}` → retourne 204
  - [x] Annotations Swagger `@Operation`, `@ApiResponses`, `@Tag`

- [x] Tâche 7 — Tests unitaires `DataRowEditorService` (AC: #6)
  - [x] Test `getRows` : pagination correcte, rowIndex attaché
  - [x] Test `getRow` : ligne exacte, IndexOutOfBoundsException → 404
  - [x] Test `updateRow` : merge partiel, snapshot Activity correct, version incrémenté
  - [x] Test `deleteRow` : compactage, rowCount décrémenté, Activity ROW_DELETED créée
  - [x] Test circuit breaker : 413 si rowCount > 50_000
  - [x] Test régression : originalData inchangé après PUT

- [x] Tâche 8 — Tests intégration `DataRowEditorController` (AC: #6)
  - [x] GET paginé : 200 avec structure correcte
  - [x] GET ligne : 200, 404 hors bornes
  - [x] PUT : 200 merge partiel, 400 colonnes vides, 404 dataset/rowIndex inexistant
  - [x] DELETE : 204, réindexation vérifiée, 404 inexistant
  - [x] Test reset après modifications (régression originalData)
  - [x] Pattern RestAssured + @SpringBootTest (voir `DataGenerationControllerTests.java`)

## Dev Notes

### Contexte de l'entité DataSet (CRITIQUE)

```java
// DataSet.java — structure réelle (src/main/java/com/movkfact/entity/DataSet.java)
@Column(columnDefinition = "LONGTEXT") private String dataJson;    // tableau JSON éditable
@Column(columnDefinition = "LONGTEXT") private String originalData; // JAMAIS modifier — reset FR-018
@Column(nullable = false) private Integer version = 0;
@Column(nullable = false) private Integer rowCount;
@Column(nullable = true) private LocalDateTime deletedAt;           // soft-delete

// @PreUpdate auto-gère updatedAt — NE PAS appeler setUpdatedAt() manuellement
@PreUpdate protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
```

**Soft-delete pattern** — TOUJOURS utiliser :
```java
dataSetRepository.findByIdAndDeletedAtIsNull(id)
    .orElseThrow(() -> new EntityNotFoundException("Dataset not found with id " + id));
```

### Logique DataRowEditorService — Implémentation exacte

```java
// Injection — ObjectMapper est DÉJÀ dans le contexte Spring (voir DataGenerationController)
@Autowired private ObjectMapper objectMapper;
@Autowired private DataSetRepository dataSetRepository;
@Autowired private ActivityRepository activityRepository;  // ou ActivityService étendu

// ─── Désérialisation — TypeReference idiomatique ───
List<Map<String, Object>> rows = objectMapper.readValue(
    dataset.getDataJson(),
    new TypeReference<List<Map<String, Object>>>() {}
);

// ─── PUT : merge partiel ───
Map<String, Object> row = rows.get(rowIndex);           // IndexOutOfBoundsException si hors bornes → attraper → EntityNotFoundException
Map<String, Object> snapshot = new HashMap<>(row);      // snapshot PARTIEL des colonnes à modifier seulement
Map<String, Object> changedOnly = new HashMap<>();
requestColumns.forEach((key, value) -> {
    changedOnly.put(key, row.get(key));                 // valeur AVANT pour snapshot Activity
    row.put(key, value);                                 // merge
});
rows.set(rowIndex, row);
// Activity : modifiedColumns = JSON array des keys, previousValue = JSON de changedOnly

// ─── DELETE : remove + compact ───
Map<String, Object> snapshot = rows.remove(rowIndex);   // indices suivants décrémentent automatiquement (List.remove)
dataset.setRowCount(dataset.getRowCount() - 1);
// Activity : previousValue = JSON du snapshot complet

// ─── Re-sérialisation + save ───
dataset.setDataJson(objectMapper.writeValueAsString(rows));
dataset.setVersion(dataset.getVersion() + 1);
dataSetRepository.save(dataset);  // @PreUpdate gère updatedAt automatiquement
```

**Circuit breaker 413** — utiliser `ResponseStatusException` (géré par GlobalExceptionHandler) :
```java
if (dataset.getRowCount() > 50_000) {
    throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
        "Dataset trop volumineux pour édition ligne par ligne. Utilisez l'API bulk ou régénérez.");
}
```

**Wrapping try-catch** pour `JsonProcessingException` :
```java
try {
    // objectMapper.readValue / writeValueAsString
} catch (JsonProcessingException e) {
    throw new IllegalArgumentException("Failed to process dataset JSON: " + e.getMessage(), e);
}
```
→ `IllegalArgumentException` est capturée par `GlobalExceptionHandler.handleIllegalArgumentException` → 400.

### Étendre Activity.java — ATTENTION aux constructeurs existants

```java
// Constructeur EXISTANT (ne pas modifier — utilisation dans ActivityService.recordActivity) :
public Activity(Long dataSetId, ActivityActionType action, String userName) {
    this.dataSetId = dataSetId; this.action = action; this.userName = userName;
    this.timestamp = LocalDateTime.now(); this.createdAt = LocalDateTime.now();
}

// NOUVEAU constructeur à ajouter :
public Activity(Long dataSetId, ActivityActionType action, String userName,
                Integer rowIndex, String modifiedColumns, String previousValue) {
    this(dataSetId, action, userName);  // délégation
    this.rowIndex = rowIndex;
    this.modifiedColumns = modifiedColumns;
    this.previousValue = previousValue;
}

// Nouveaux champs avec annotations JPA :
@Column(name = "row_index")
private Integer rowIndex;

@Column(name = "modified_columns", columnDefinition = "TEXT")
private String modifiedColumns;

@Column(name = "previous_value", columnDefinition = "TEXT")
private String previousValue;
```

### Flyway V006 — Syntaxe PostgreSQL OBLIGATOIRE

```sql
-- V006__add_row_editor_activity_columns.sql
-- PostgreSQL syntax (pas de LONGTEXT — utiliser TEXT)
-- Colonnes NULLABLE intentionnellement pour zéro régression sur Activity existants

ALTER TABLE activity ADD COLUMN row_index       INTEGER;
ALTER TABLE activity ADD COLUMN modified_columns TEXT;
ALTER TABLE activity ADD COLUMN previous_value   TEXT;
```

⚠️ Ne PAS utiliser `LONGTEXT` (MySQL uniquement) — le projet est sur PostgreSQL depuis S4.2. Voir `V004__activity_tracking.sql` pour référence syntaxe.

### Pattern de controller à suivre (DataGenerationController.java)

```java
// Imports à réutiliser :
import com.movkfact.exception.EntityNotFoundException;
import com.movkfact.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// Pattern 404 :
.orElseThrow(() -> new EntityNotFoundException("Dataset not found with id " + id));

// Pattern succès GET :
return ResponseEntity.ok(ApiResponse.success(dto, "Rows retrieved"));

// Pattern 204 DELETE :
return ResponseEntity.noContent().build();

// Pattern 400 colonnes vides (dans le service) :
throw new IllegalArgumentException("columns must not be null or empty");
// → capturé automatiquement par GlobalExceptionHandler → 400
```

### Ne PAS utiliser l'event system pour les activités row-level

Le `DatasetActivityEvent` + `ApplicationEventPublisher` existant ne supporte PAS `rowIndex`/`modifiedColumns`. Appeler `activityService.recordRowActivity(...)` **directement** depuis `DataRowEditorService`, pas via event publisher.

### Structure des fichiers à créer

```
src/main/java/com/movkfact/
  service/
    DataRowEditorService.java          ← @Service @Transactional sur updateRow + deleteRow
  controller/
    DataRowEditorController.java       ← @RestController @RequestMapping("/api")
  dto/
    RowUpdateRequestDTO.java           ← { @NotNull @NotEmpty Map<String,Object> columns }
    RowResponseDTO.java                ← { int rowIndex; Map<String,Object> data }
    PagedRowsResponseDTO.java          ← { List<RowResponseDTO> rows; int totalRows, page, size }

src/main/resources/db/migration/
  V006__add_row_editor_activity_columns.sql

src/main/java/com/movkfact/entity/
  ActivityActionType.java              ← ajouter ROW_MODIFIED, ROW_DELETED
  Activity.java                        ← ajouter 3 champs + constructeur

src/main/java/com/movkfact/service/
  ActivityService.java                 ← ajouter recordRowActivity(...)

src/test/java/com/movkfact/
  service/DataRowEditorServiceTest.java   ← JUnit 5 + Mockito
  controller/DataRowEditorControllerTest.java ← RestAssured + @SpringBootTest
```

### Project Structure Notes

- Package racine : `com.movkfact` (pas `com.movkfact.backend`)
- Tests controllers : `@SpringBootTest(webEnvironment = RANDOM_PORT)` + RestAssured (pattern existant dans `DataGenerationControllerTests.java`)
- Tests services : `@Mock` + `@InjectMocks` + `MockitoAnnotations.openMocks(this)` (pattern dans `ActivityServiceTest.java`)
- Gestion erreurs centralisée dans `GlobalExceptionHandler.java` — NE PAS dupliquer la logique d'erreur dans le controller
- `@Transactional` sur les méthodes qui modifient `dataJson` (updateRow, deleteRow)

### References

- [Source: entity/DataSet.java] — champs `dataJson`, `originalData`, `version`, `rowCount`, `deletedAt`, `@PreUpdate`
- [Source: entity/Activity.java] — constructeur existant, pattern de champs JPA
- [Source: entity/ActivityActionType.java] — enum actuel (DOWNLOADED, MODIFIED, VIEWED, CREATED, RESET, DELETED)
- [Source: service/ActivityService.java] — `recordActivity`, `resetDataSet`
- [Source: controller/DataGenerationController.java] — pattern ObjectMapper, EntityNotFoundException, ApiResponse, 204, soft-delete
- [Source: exception/GlobalExceptionHandler.java] — IllegalArgumentException→400, EntityNotFoundException→404, ResponseStatusException→status dynamique
- [Source: db/migration/V004__activity_tracking.sql] — syntaxe PostgreSQL référence
- [Source: test/controller/DataGenerationControllerTests.java] — pattern RestAssured @SpringBootTest
- [Source: test/service/ActivityServiceTest.java] — pattern Mockito @Mock @InjectMocks
- [Source: epic-6-data-editor/6-1-backend-row-editor-api.md] — spec complète avec AC7 (413) et AC8 (Flyway NULLABLE)

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

- Toutes les tâches complètes. 434/434 tests passent (0 régression).
- ActivityActionType étendu avec ROW_MODIFIED + ROW_DELETED.
- Activity étendu avec 3 champs nullable (rowIndex, modifiedColumns, previousValue) + constructeur délégant.
- V006 en syntaxe PostgreSQL avec colonnes NULLABLE — zéro impact sur les Activity existants.
- DataRowEditorService : merge partiel PUT, compactage DELETE, circuit-breaker 413 à 50k lignes, originalData jamais modifiée.
- 16 tests unitaires (DataRowEditorServiceTest) + 12 tests intégration (DataRowEditorControllerTest).

### File List

- src/main/java/com/movkfact/entity/ActivityActionType.java (modifié — ROW_MODIFIED, ROW_DELETED)
- src/main/java/com/movkfact/entity/Activity.java (modifié — 3 champs + constructeur)
- src/main/java/com/movkfact/service/ActivityService.java (modifié — recordRowActivity)
- src/main/resources/db/migration/V006__add_row_editor_activity_columns.sql (créé)
- src/main/java/com/movkfact/dto/RowUpdateRequestDTO.java (créé)
- src/main/java/com/movkfact/dto/RowResponseDTO.java (créé)
- src/main/java/com/movkfact/dto/PagedRowsResponseDTO.java (créé)
- src/main/java/com/movkfact/service/DataRowEditorService.java (créé)
- src/main/java/com/movkfact/controller/DataRowEditorController.java (créé)
- src/test/java/com/movkfact/service/DataRowEditorServiceTest.java (créé)
- src/test/java/com/movkfact/controller/DataRowEditorControllerTest.java (créé)
