# Story 7.1: Backend Wizard Support

Status: done

## Story

En tant qu'ingénieur QA ou développeur,
Je veux pouvoir prévisualiser 5 lignes générées à partir d'une configuration manuelle avant de lancer la génération complète,
so that je puisse valider la cohérence des types et contraintes sans attendre la génération de milliers de lignes.

## Acceptance Criteria

1. `POST /api/datasets/preview` accepte `{ columns: [{name, columnType, constraints}], count: 5 }`. Génère exactement `min(count, 5)` lignes via les générateurs existants SANS créer de `DataSet` en base. Réponse : `{ previewRows: [{...}, ...], columnCount: N }`. Temps < 500ms.
2. `ColumnConfigDTO` étendu avec `Map<String, Object> constraints` (optionnel, null = pas de contraintes). Contraintes par type : `INTEGER`/`AMOUNT` : `{ "min": number, "max": number }` ; `DATE`/`BIRTH_DATE` : `{ "dateFrom": "YYYY-MM-DD", "dateTo": "YYYY-MM-DD" }` ; `TEXT`/`LOREM_IPSUM` : `{ "maxLength": number }` (non encore supportés = ignorées). Contraintes invalides (min > max) → 400.
3. `POST /api/domains/{id}/data-sets` (génération standard) accepte les nouvelles contraintes sans régression. 0 test existant ne doit casser.
4. Validation : `columns` non vide, chaque colonne a nom non vide, type valide. Contraintes invalides → 400 avec message explicite.
5. `POST /api/datasets/preview` retourne 400 si `columnType` inconnu. Message : `"Type inconnu : 'XYZ'. Types valides : [FIRST_NAME, EMAIL, AMOUNT, ...]"`. Géré via `GlobalExceptionHandler` (@ExceptionHandler) — pas de NPE propagé. Test d'intégration : preview avec `"columnType": "INVALID_TYPE"` → 400.
6. Test unitaire `DataPreviewService` : 5 lignes générées avec types corrects. Test intégration `POST /api/datasets/preview` (succès, contrainte invalide, type inconnu). Tests régression `POST /api/domains/{id}/data-sets` avec contraintes. Coverage >80%.

## Tasks / Subtasks

- [x] Tâche 1 — Étendre `ColumnConfigDTO` (AC: #2, #3)
  - [x] Ajouter `private Map<String, Object> constraints;` avec getter/setter
  - [x] Aucune annotation `@NotNull` — le champ est optionnel (null acceptable)
  - [x] Champ compatible JSON : Jackson le sérialise/désérialise automatiquement

- [x] Tâche 2 — Mettre à jour `AmountGenerator` pour lire les contraintes (AC: #2)
  - [x] Lire `constraints.get("min")` en priorité sur `columnConfig.getMinValue()` (backward compat)
  - [x] Lire `constraints.get("max")` en priorité sur `columnConfig.getMaxValue()`
  - [x] Conversion : `((Number) constraints.get("min")).doubleValue()` (les JSON numbers arrivent comme Integer ou Double)
  - [x] Garder le comportement actuel si `constraints` est null ou clé absente

- [x] Tâche 3 — Mettre à jour `DateGenerator` pour lire les contraintes (AC: #2)
  - [x] Si `constraints.get("dateFrom")` et `constraints.get("dateTo")` présents : générer dans la plage
  - [x] Parse ISO 8601 : `LocalDate.parse((String) constraints.get("dateFrom"))`
  - [x] Fallback comportement actuel si contraintes absentes

- [x] Tâche 4 — Créer `DataPreviewService` (AC: #1, #2, #4)
  - [x] `@Service` dans `com.movkfact.service`
  - [x] Méthode `generatePreview(PreviewRequestDTO request)` → `PreviewResponseDTO`
  - [x] `int count = Math.min(request.getCount(), 5);`
  - [x] Boucle : `GeneratorFactory.createGenerator(col).generate()` pour chaque colonne
  - [x] Validation contraintes AVANT génération : min > max → throw `IllegalArgumentException`
  - [x] NE PAS injecter `DataSetRepository`, NE PAS sauvegarder

- [x] Tâche 5 — Créer DTOs `PreviewRequestDTO` et `PreviewResponseDTO` (AC: #1)
  - [x] `PreviewRequestDTO` : `List<ColumnConfigDTO> columns` (@NotNull @NotEmpty) + `int count` (default 5)
  - [x] `PreviewResponseDTO` : `List<Map<String, Object>> previewRows` + `int columnCount`
  - [x] Getters/setters + constructeur complet (pattern `DataSetDTO.java`)

- [x] Tâche 6 — Ajouter l'endpoint `POST /api/datasets/preview` dans un nouveau controller (AC: #1, #4, #5)
  - [x] Créer `DataPreviewController.java` dans `com.movkfact.controller`
  - [x] `@PostMapping("/api/datasets/preview")` avec `@Valid @RequestBody PreviewRequestDTO`
  - [x] 200 OK avec `ApiResponse.success(previewResponseDTO, "Preview generated")`
  - [x] Annotations Swagger `@Operation`, `@Tag`, `@ApiResponses`

- [x] Tâche 7 — Gérer l'erreur 400 pour type inconnu (AC: #5)
  - [x] Ajouter `@ExceptionHandler(HttpMessageNotReadableException.class)` dans `GlobalExceptionHandler`
  - [x] Vérifier si la cause est un `InvalidFormatException` sur un enum `ColumnType`
  - [x] Message : `"Type inconnu : '"+value+"'. Types valides : "+Arrays.toString(ColumnType.values())`
  - [x] Retourner 400 avec `ApiErrorResponse`

- [x] Tâche 8 — Tests (AC: #6)
  - [x] `DataPreviewServiceTest.java` : 10 tests unitaires — count limité à 5, contraintes amount/date, validation, backward compat
  - [x] `DataPreviewControllerTest.java` : 9 tests RestAssured — succès, count>5, INVALID_TYPE, min>max, null/empty columns, dateConstraints
  - [x] Régression : 453/453 tests passent (0 régression)

## Dev Notes

### ColumnConfigDTO — Structure actuelle et extension

```java
// Structure ACTUELLE (src/main/java/com/movkfact/dto/ColumnConfigDTO.java) :
private String name;
private ColumnType columnType;  // enum : FIRST_NAME, LAST_NAME, EMAIL, PHONE, GENDER, ADDRESS,
                                //        AMOUNT, CURRENCY, ACCOUNT_NUMBER,
                                //        DATE, TIME, TIMEZONE, BIRTH_DATE (13 types)
private String format;
private Integer minValue;       // déjà utilisé par AmountGenerator
private Integer maxValue;       // déjà utilisé par AmountGenerator
private Boolean nullable;
private String additionalConfig; // String JSON — getConfigValue() non fonctionnel (retourne defaultValue)

// À AJOUTER :
private Map<String, Object> constraints;   // optionnel — null si absent du JSON

public Map<String, Object> getConstraints() { return constraints; }
public void setConstraints(Map<String, Object> constraints) { this.constraints = constraints; }
```

### AmountGenerator — Mise à jour contraintes

```java
@Override
public Object generate() {
    // Priorité : constraints > minValue/maxValue existants
    double min = 1.0;
    double max = 1000000.0;

    Map<String, Object> constraints = columnConfig.getConstraints();
    if (constraints != null) {
        if (constraints.get("min") != null) min = ((Number) constraints.get("min")).doubleValue();
        if (constraints.get("max") != null) max = ((Number) constraints.get("max")).doubleValue();
    } else {
        // Backward compat avec les champs existants
        if (columnConfig.getMinValue() != null) min = columnConfig.getMinValue();
        if (columnConfig.getMaxValue() != null) max = columnConfig.getMaxValue();
    }

    double amount = min + (max - min) * random.nextDouble();
    String formattedAmount = String.format(Locale.US, "%.2f", amount);
    return new BigDecimal(formattedAmount);
}
```

### DateGenerator — Mise à jour contraintes

```java
@Override
public Object generate() {
    Map<String, Object> constraints = columnConfig.getConstraints();
    if (constraints != null && constraints.get("dateFrom") != null && constraints.get("dateTo") != null) {
        LocalDate from = LocalDate.parse((String) constraints.get("dateFrom"));
        LocalDate to   = LocalDate.parse((String) constraints.get("dateTo"));
        long days = ChronoUnit.DAYS.between(from, to);
        return from.plusDays(days <= 0 ? 0 : random.nextLong(days)).toString();
    }
    // Comportement actuel inchangé :
    long daysAgo = random.nextLong(3650);
    return LocalDate.now().minusDays(daysAgo).toString();
}
```

### DataPreviewService — Logique complète

```java
@Service
public class DataPreviewService {

    public PreviewResponseDTO generatePreview(PreviewRequestDTO request) {
        // 1. Validation contraintes
        validateConstraints(request.getColumns());

        // 2. Limiter à 5 lignes
        int count = Math.min(request.getCount() > 0 ? request.getCount() : 5, 5);

        // 3. Génération sans persistance
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (ColumnConfigDTO col : request.getColumns()) {
                DataTypeGenerator gen = GeneratorFactory.createGenerator(col);
                // GeneratorFactory.createGenerator() throw IllegalArgumentException si type inconnu
                // → capturé par GlobalExceptionHandler → 400 (AC5 pris en charge en amont)
                row.put(col.getName(), gen.generate());
            }
            rows.add(row);
        }

        return new PreviewResponseDTO(rows, request.getColumns().size());
    }

    private void validateConstraints(List<ColumnConfigDTO> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Au moins une colonne est requise");
        }
        for (ColumnConfigDTO col : columns) {
            Map<String, Object> c = col.getConstraints();
            if (c == null) continue;

            // Valider min <= max
            if (c.get("min") != null && c.get("max") != null) {
                double min = ((Number) c.get("min")).doubleValue();
                double max = ((Number) c.get("max")).doubleValue();
                if (min > max) throw new IllegalArgumentException(
                    "Contrainte invalide pour '" + col.getName() + "' : min (" + min + ") doit être <= max (" + max + ")"
                );
            }
            // Valider dateFrom <= dateTo
            if (c.get("dateFrom") != null && c.get("dateTo") != null) {
                LocalDate from = LocalDate.parse((String) c.get("dateFrom"));
                LocalDate to   = LocalDate.parse((String) c.get("dateTo"));
                if (from.isAfter(to)) throw new IllegalArgumentException(
                    "Contrainte invalide pour '" + col.getName() + "' : dateFrom doit être <= dateTo"
                );
            }
        }
    }
}
```

### Gestion 400 type inconnu — HttpMessageNotReadableException (AC5)

```java
// GlobalExceptionHandler.java — AJOUTER :
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.movkfact.enums.ColumnType;
import java.util.Arrays;

@ExceptionHandler(HttpMessageNotReadableException.class)
public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex, WebRequest request) {

    String message = "Requête invalide";
    Throwable cause = ex.getCause();

    if (cause instanceof InvalidFormatException ife) {
        if (ife.getTargetType() != null && ife.getTargetType().isEnum()
                && ColumnType.class.isAssignableFrom(ife.getTargetType())) {
            message = "Type inconnu : '" + ife.getValue() + "'. Types valides : "
                + Arrays.toString(ColumnType.values());
        }
    }

    logger.warn("Message not readable: {}", ex.getMessage());
    ApiErrorResponse errorResponse = ApiErrorResponse.of(message, 400,
        request.getDescription(false).replace("uri=", ""));
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
}
```

### GeneratorFactory — Comportement actuel (CRITIQUE)

```java
// GeneratorFactory.createGenerator() → throw IllegalArgumentException si ColumnType null
// Mais : Jackson parse "INVALID_TYPE" en ColumnType → HttpMessageNotReadableException
// AVANT d'atteindre GeneratorFactory
// ⚠️ Ne PAS dupliquer la validation — laisser Jackson + GlobalExceptionHandler gérer
```

### DataPreviewController — Pattern endpoint

```java
@RestController
@RequestMapping("/api")
@Tag(name = "Data Preview", description = "Preview data generation without persistence")
public class DataPreviewController {

    @Autowired
    private DataPreviewService dataPreviewService;

    @PostMapping("/datasets/preview")
    @Operation(summary = "Preview dataset generation", description = "Generate 5 rows without persisting")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Preview generated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid type or constraints")
    })
    public ResponseEntity<ApiResponse<PreviewResponseDTO>> previewDataset(
            @Valid @RequestBody PreviewRequestDTO request) {
        PreviewResponseDTO response = dataPreviewService.generatePreview(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Preview generated"));
    }
}
```

### Project Structure Notes

- Package : `com.movkfact` (controller, service, dto)
- `GeneratorFactory` est une classe **statique** (`GeneratorFactory.createGenerator(col)`) — pas un `@Bean`
- `DataPreviewService` N'est PAS `@Transactional` — pas de DB
- Tests intégration pattern : RestAssured + `@SpringBootTest(webEnvironment = RANDOM_PORT)` (voir `DataGenerationControllerTests.java`)
- Pas de nouvelle migration Flyway nécessaire pour cette story
- `ApiResponse.success(...)` et `ApiErrorResponse` déjà existants dans `com.movkfact.response`
- ColumnType valide actuellement : `FIRST_NAME, LAST_NAME, EMAIL, PHONE, GENDER, ADDRESS, AMOUNT, CURRENCY, ACCOUNT_NUMBER, DATE, TIME, TIMEZONE, BIRTH_DATE` (13 types)
- `TEXT` et `LOREM_IPSUM` n'existent pas dans l'enum — la contrainte `maxLength` ne peut pas être testée avant Sprint 5 (S5.x)

### Fichiers à créer / modifier

```
Nouveaux :
src/main/java/com/movkfact/
  service/DataPreviewService.java
  controller/DataPreviewController.java
  dto/PreviewRequestDTO.java
  dto/PreviewResponseDTO.java

src/test/java/com/movkfact/
  service/DataPreviewServiceTest.java
  controller/DataPreviewControllerTest.java

Modifiés :
src/main/java/com/movkfact/dto/ColumnConfigDTO.java
  + Map<String, Object> constraints
src/main/java/com/movkfact/service/generator/financial/AmountGenerator.java
  → lire constraints.get("min")/get("max") en priorité
src/main/java/com/movkfact/service/generator/temporal/DateGenerator.java
  → lire constraints.get("dateFrom")/get("dateTo")
src/main/java/com/movkfact/exception/GlobalExceptionHandler.java
  + @ExceptionHandler(HttpMessageNotReadableException.class) pour ColumnType inconnu
```

### References

- [Source: dto/ColumnConfigDTO.java] — champs existants (minValue, maxValue, additionalConfig)
- [Source: enums/ColumnType.java] — 13 types disponibles, enum avec dataType + description
- [Source: service/generator/GeneratorFactory.java] — factory statique, switch sur ColumnType
- [Source: service/generator/financial/AmountGenerator.java] — utilise getMinValue()/getMaxValue()
- [Source: service/generator/temporal/DateGenerator.java] — génère -10 ans à aujourd'hui
- [Source: service/generator/DataTypeGenerator.java] — `getConfigValue()` NON FONCTIONNEL (retourne defaultValue)
- [Source: dto/GenerationRequestDTO.java] — pattern @Valid, @NotNull, @NotEmpty, @Min pour référence DTOs
- [Source: exception/GlobalExceptionHandler.java] — handlers existants, pattern ApiErrorResponse
- [Source: controller/DataGenerationController.java] — pattern ApiResponse.success, @Tag, @Operation
- [Source: test/controller/DataGenerationControllerTests.java] — pattern RestAssured RANDOM_PORT
- [Source: epic-7-manual-wizard/7-1-backend-wizard-support.md] — spec complète avec AC4b type inconnu

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

- Fix : SecurityConfig manquait `/api/datasets/**` → 403 sur tous les tests d'intégration. Corrigé en ajoutant `requestMatchers("/api/datasets/**").permitAll()`.

### Completion Notes List

- **T1** : `ColumnConfigDTO` étendu avec `Map<String, Object> constraints` (import java.util.Map ajouté). Backward compatible — null si absent du JSON.
- **T2** : `AmountGenerator` lit `constraints.min`/`constraints.max` en priorité, puis minValue/maxValue (backward compat). Cast via `((Number) val).doubleValue()`.
- **T3** : `DateGenerator` lit `constraints.dateFrom`/`constraints.dateTo` avec `LocalDate.parse()` + `ChronoUnit.DAYS.between()`. Fallback `-10 ans à aujourd'hui` si absentes.
- **T4** : `DataPreviewService` — pas de `@Transactional`, pas de DB, `count` limité à 5, validation contraintes avant génération, throw `IllegalArgumentException` → 400 via handler existant.
- **T5** : `PreviewRequestDTO` (`@NotNull @NotEmpty columns`, `int count=5`) + `PreviewResponseDTO` (`List<Map<String,Object>> previewRows`, `int columnCount`).
- **T6** : `DataPreviewController` — `POST /api/datasets/preview`, `@Valid`, retourne `ApiResponse.success(response, "Preview generated")` 200 OK.
- **T7** : `GlobalExceptionHandler` — `@ExceptionHandler(HttpMessageNotReadableException.class)` détecte `InvalidFormatException` sur `ColumnType` enum, retourne `"Type inconnu : 'XYZ'. Types valides : [...]"` 400.
- **T8** : 19 nouveaux tests — `DataPreviewServiceTest` (10 unitaires) + `DataPreviewControllerTest` (9 RestAssured). Total : 453/453 tests passants.
- **AC3 régression** : 0 test existant cassé. `DataGenerationControllerTests` intacts.

### File List

- `src/main/java/com/movkfact/dto/ColumnConfigDTO.java` (modifié — ajout `Map<String,Object> constraints`)
- `src/main/java/com/movkfact/service/generator/financial/AmountGenerator.java` (modifié — lecture constraints)
- `src/main/java/com/movkfact/service/generator/temporal/DateGenerator.java` (modifié — lecture dateFrom/dateTo)
- `src/main/java/com/movkfact/dto/PreviewRequestDTO.java` (nouveau)
- `src/main/java/com/movkfact/dto/PreviewResponseDTO.java` (nouveau)
- `src/main/java/com/movkfact/service/DataPreviewService.java` (nouveau)
- `src/main/java/com/movkfact/controller/DataPreviewController.java` (nouveau)
- `src/main/java/com/movkfact/exception/GlobalExceptionHandler.java` (modifié — ajout HttpMessageNotReadableException handler)
- `src/main/java/com/movkfact/config/SecurityConfig.java` (modifié — ajout `/api/datasets/**` permit)
- `src/test/java/com/movkfact/service/DataPreviewServiceTest.java` (nouveau — 10 tests)
- `src/test/java/com/movkfact/controller/DataPreviewControllerTest.java` (nouveau — 9 tests)
