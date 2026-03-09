# Story 9.2: Auto-flag PII + Pré-remplissage RGPD

Status: review

## Story

En tant qu'analyste ou DPO,
Je veux que les colonnes contenant des données personnelles soient automatiquement identifiées lors de l'upload CSV,
Afin que l'anonymisation RGPD soit pré-configurée sans intervention manuelle.

## Acceptance Criteria

1. **AC1 — Détection PII automatique**
   - Une colonne est flaggée PII si son nom OU ses données correspondent à des patterns d'identification personnelle
   - Catégories PII supportées :
     - `CONTACT` : EMAIL, PHONE
     - `IDENTITY` : FIRST_NAME, LAST_NAME, et patterns IBAN/SIRET/numéro sécu dans les données
     - `LOCATION` : POSTAL_CODE, CITY + patterns d'adresse
   - Résultat par colonne : `{ isPII: boolean, piiCategory: "CONTACT|IDENTITY|LOCATION|null" }`
   - Réutilise `ColumnTypeInferenceService` (S9.1) — pas de duplication de logique

2. **AC2 — Pré-remplissage frontend**
   - Colonne détectée PII → case "Anonymisation" pré-cochée dans ConfigurationPanel
   - Tooltip sur la case : "Donnée personnelle détectée automatiquement — anonymisation recommandée (RGPD Art. 25)"
   - Utilisateur peut décocher avec affichage d'un avertissement (Alert MUI warning) : "Attention : cette colonne contient potentiellement des données personnelles. Désactiver l'anonymisation peut engager votre responsabilité RGPD."
   - Pas de blocage : l'utilisateur reste maître de sa configuration

3. **AC3 — Audit log**
   - Chaque flag PII loggué avec : `columnName`, `piiCategory`, `detectedBy` (NAME_BASED | DATA_BASED), `timestamp`, `datasetId`
   - Log niveau INFO dans les logs applicatifs existants
   - Format : `PII_DETECTED | dataset={id} | column={name} | category={cat} | detectedBy={level}`

4. **AC4 — Tests**
   - Unit tests `PiiDetectionService` : ≥ 15 cas (patterns CONTACT, IDENTITY, LOCATION, faux positifs)
   - Frontend : pré-cochage automatique, affichage tooltip, avertissement sur décochage

## Tasks / Subtasks

- [ ] Créer `PiiDetectionService` (AC: 1)
  - [ ] Méthode `detect(String columnName, List<String> sampleValues, ColumnType inferredType)` → `PiiResult`
  - [ ] `PiiResult` : `{ boolean isPII, PiiCategory category, InferenceLevel detectedBy }`
  - [ ] Déléguer l'inférence de type à `ColumnTypeInferenceService` (S9.1)
  - [ ] Patterns IBAN : `[A-Z]{2}\d{2}[A-Z0-9]{4}\d{7}([A-Z0-9]?){0,16}`
  - [ ] Patterns NIR (sécu) : `[12]\d{2}(0[1-9]|1[0-2])\d{5}\d{3}\d{2}`
  - [ ] Patterns SIRET : `\d{14}`
- [ ] Créer `PiiCategory.java` (enum : CONTACT, IDENTITY, LOCATION) (AC: 1)
- [ ] Créer `PiiResult.java` (AC: 1)
- [ ] Modifier `POST /api/csv/detect-types` pour inclure `isPII` et `piiCategory` dans la réponse (AC: 1)
- [ ] Ajouter audit log dans `PiiDetectionService` (AC: 3)
- [ ] Mettre à jour `ConfigurationPanel.jsx` (AC: 2)
  - [ ] Consommer `isPII` et `piiCategory` de la réponse
  - [ ] Pré-cocher la case Anonymisation si `isPII: true`
  - [ ] Afficher Tooltip RGPD sur la case
  - [ ] Afficher Alert warning MUI si l'utilisateur décoche une colonne PII
- [ ] Tests `PiiDetectionServiceTest.java` — ≥ 15 cas (AC: 4)
- [ ] Tests frontend `ConfigurationPanel` — pré-cochage + alerte décochage (AC: 4)

## Dev Notes

### Dépendance sur S9.1

**S9.1 doit être mergé avant S9.2.**
`PiiDetectionService` appelle `ColumnTypeInferenceService.infer()` pour obtenir le type inféré, puis applique sa logique PII par-dessus.

```java
@Service
public class PiiDetectionService {
    @Autowired
    private ColumnTypeInferenceService inferenceService;

    public PiiResult detect(String columnName, List<String> sampleValues) {
        InferenceResult inferred = inferenceService.infer(columnName, sampleValues);
        PiiCategory category = resolveCategory(inferred.getType(), columnName, sampleValues);
        boolean isPii = category != null;
        return new PiiResult(isPii, category, inferred.getLevel());
    }

    private PiiCategory resolveCategory(ColumnType type, String name, List<String> values) {
        // CONTACT : EMAIL, PHONE
        if (type == EMAIL || type == PHONE) return PiiCategory.CONTACT;
        // IDENTITY : FIRST_NAME, LAST_NAME + patterns IBAN/NIR/SIRET dans les données
        if (type == FIRST_NAME || type == LAST_NAME) return PiiCategory.IDENTITY;
        if (matchesPattern(values, IBAN_PATTERN) || matchesPattern(values, NIR_PATTERN)
                || matchesPattern(values, SIRET_PATTERN)) return PiiCategory.IDENTITY;
        // LOCATION : POSTAL_CODE, CITY
        if (type == POSTAL_CODE || type == CITY) return PiiCategory.LOCATION;
        return null; // non PII
    }
}
```

### Mapping type → catégorie PII

| ColumnType inféré | PiiCategory |
|---|---|
| EMAIL | CONTACT |
| PHONE | CONTACT |
| FIRST_NAME | IDENTITY |
| LAST_NAME | IDENTITY |
| Données IBAN | IDENTITY |
| Données NIR (sécu) | IDENTITY |
| Données SIRET | IDENTITY |
| POSTAL_CODE | LOCATION |
| CITY | LOCATION |
| Autres | null (non PII) |

### Frontend — Comportement pré-cochage

```jsx
// Dans ConfigurationPanel, pour chaque colonne :
const isPii = columnInfo.isPII;

<FormControlLabel
  control={
    <Checkbox
      checked={config.anonymize || isPii}
      onChange={(e) => {
        if (!e.target.checked && isPii) {
          setShowPiiWarning(true); // déclenche Alert warning
        }
        handleAnonymizeChange(colName, e.target.checked);
      }}
    />
  }
  label={
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      Anonymisation
      {isPii && (
        <Tooltip title="Donnée personnelle détectée — anonymisation recommandée (RGPD Art. 25)">
          <ShieldIcon fontSize="small" color="warning" />
        </Tooltip>
      )}
    </Box>
  }
/>
{showPiiWarning && (
  <Alert severity="warning" onClose={() => setShowPiiWarning(false)}>
    Attention : cette colonne contient potentiellement des données personnelles.
    Désactiver l'anonymisation peut engager votre responsabilité RGPD.
  </Alert>
)}
```

### Patterns regex clés

```java
private static final Pattern IBAN_PATTERN =
    Pattern.compile("[A-Z]{2}\\d{2}[A-Z0-9]{4}\\d{7}([A-Z0-9]?){0,16}");

private static final Pattern NIR_PATTERN =
    Pattern.compile("[12]\\d{2}(0[1-9]|1[0-2])\\d{5}\\d{3}\\d{2}");

private static final Pattern SIRET_PATTERN =
    Pattern.compile("\\d{14}");
```

### Fichiers à créer / modifier

```
src/main/java/com/movkfact/
  service/
    PiiDetectionService.java       ← NOUVEAU
  dto/
    PiiResult.java                 ← NOUVEAU
  enums/
    PiiCategory.java               ← NOUVEAU (CONTACT, IDENTITY, LOCATION)
  service/CsvAnalysisService.java  ← modifier (appeler PiiDetectionService)

movkfact-frontend/src/
  components/DataConfigurationPanel/
    ConfigurationPanel.jsx         ← modifier (pré-cochage + tooltip + alert)

src/test/java/com/movkfact/
  service/PiiDetectionServiceTest.java  ← NOUVEAU (≥ 15 tests)
```

### References

- Story dépendante : [9-1-smart-column-type-inference.md](./9-1-smart-column-type-inference.md)
- Anonymisation RGPD existante (Sprint 8) : `AnonymizationService.java`
- ConfigurationPanel : `movkfact-frontend/src/components/DataConfigurationPanel/ConfigurationPanel.jsx`

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

- `PiiDetectionServiceTest` initially: 4 failures due to (a) ZIP_CODE/CITY missing from patterns.yml and (b) IBAN/SIRET data misidentified by specialized detectors (AddressValidator/PhoneValidator) before `scanDataPatterns` could run.
- Fix 1: Added `ZIP_CODE` and `CITY` pattern blocks to `patterns.yml`.
- Fix 2: Reordered `PiiDetectionService.detect()` to run `scanDataPatterns` (IBAN/NIR/SIRET) FIRST, before delegating to `ColumnTypeInferenceService`.

### Completion Notes List

- `PiiDetectionService` scans IBAN/NIR/SIRET patterns before type inference to avoid misclassification by generic specialized detectors.
- `DomainsPage.jsx` maps `col.pii` + `col.piiCategory` from API response into `typesMap`.
- `ConfigurationPanel.jsx` adds `AnonymizeCheckbox` component: pre-checked for PII columns, MUI Alert warning if unchecked. `PiiBadge` shows category chip.
- Backend: 503 tests pass (1 pre-existing `LastNameGeneratorTests` failure unrelated to S9.2).
- Frontend: 379 tests pass (7 pre-existing failures unrelated to S9.2).

### File List

```
src/main/java/com/movkfact/enums/PiiCategory.java                         ← CREATED
src/main/java/com/movkfact/dto/PiiResult.java                              ← CREATED
src/main/java/com/movkfact/service/detection/PiiDetectionService.java      ← CREATED
src/main/java/com/movkfact/dto/DetectedColumn.java                         ← MODIFIED (isPII, piiCategory)
src/main/java/com/movkfact/service/detection/CsvTypeDetectionService.java  ← MODIFIED (PII integration)
src/main/resources/patterns.yml                                             ← MODIFIED (ZIP_CODE, CITY)
movkfact-frontend/src/pages/DomainsPage.jsx                                ← MODIFIED (isPII, piiCategory in typesMap)
movkfact-frontend/src/components/DataConfigurationPanel/ConfigurationPanel.jsx ← MODIFIED (PiiBadge, AnonymizeCheckbox)
src/test/java/com/movkfact/service/detection/PiiDetectionServiceTest.java  ← CREATED (16 tests, all pass)
```
