# Story 8.1: Backend Agrégats par domaine

Status: ready-for-dev

## Story

En tant que développeur ou Tech Lead,
Je veux que `GET /api/domains` retourne le nombre de datasets, le total de lignes et les statuts agrégés pour chaque domaine,
Afin d'évaluer rapidement l'utilisation de mes domaines sans ouvrir chacun individuellement.

## Acceptance Criteria

1. **AC1 — GET /api/domains enrichi (FR-002)**
   - `GET /api/domains` retourne pour chaque domaine : `datasetCount` (Integer), `totalRows` (Long), `statuses` : `{ downloaded, modified, viewed }` (boolean chacun)
   - La pagination offset/limit existante est conservée
   - Performance : requête avec agrégation en < 200ms pour 100 domaines
   - Aucune régression sur les champs existants (`id`, `name`, `description`, `createdAt`, `updatedAt`, `deletedAt`)

2. **AC2 — GET /api/domains/{domainId}/data-sets avec statuts (FR-003)**
   - Retourne pour chaque dataset : champs existants + `status { downloaded, modified, viewed }` + `lastActivity` (timestamp ISO8601, null si jamais consulté)
   - Tri par défaut : `updatedAt DESC`
   - 404 si le domaine n'existe pas

3. **AC3 — Calcul des statuts agrégés**
   - `downloaded = true` : au moins une `Activity(DOWNLOADED)` pour le dataset
   - `modified = true` : `dataset.version > 0` (champ déjà présent, défaut = 0)
   - `viewed = true` : au moins une `Activity(VIEWED)` pour le dataset
   - Calcul sans N+1 : une seule requête pour charger toutes les activities des datasets d'un domaine

4. **AC4 — Tests**
   - Test unitaire `DomainService` : calcul statuts avec mock ActivityRepository
   - Test intégration `DomainController` : `GET /api/domains` avec 2 domaines, 3 datasets
   - Test intégration : `GET /api/domains/{domainId}/data-sets` avec statuts corrects
   - Coverage > 80% sur les classes créées/modifiées

## Tasks / Subtasks

- [ ] Créer `DomainStatusDTO.java` (AC: 1, 2)
  - [ ] Champs : `boolean downloaded`, `boolean modified`, `boolean viewed`
  - [ ] Constructeur all-args + no-arg + getters/setters
- [ ] Étendre `DomainResponseDTO.java` (AC: 1)
  - [ ] Ajouter `Integer datasetCount`, `Long totalRows`, `DomainStatusDTO statuses`
  - [ ] Mettre à jour le constructeur existant (ou ajouter un builder)
- [ ] Créer `DataSetSummaryDTO.java` (AC: 2) — DTO pour la liste des datasets enrichie
  - [ ] Champs existants : `id`, `datasetName`, `rowCount`, `columnCount`, `createdAt`, `updatedAt`
  - [ ] Nouveaux champs : `DomainStatusDTO status`, `LocalDateTime lastActivity`
- [ ] Créer `DomainService.java` (AC: 1, 2, 3)
  - [ ] Injection : `DomainRepository`, `DataSetRepository`, `ActivityRepository`
  - [ ] Méthode `getDomainsWithStats(int offset, int limit)` → `List<DomainResponseDTO>`
  - [ ] Méthode `getDatasetsByDomainWithStats(Long domainId)` → `List<DataSetSummaryDTO>`
  - [ ] Logique anti-N+1 : charger activities en une requête via `findByDataSetIdIn()`
- [ ] Ajouter `findByDataSetIdIn(List<Long> ids)` dans `ActivityRepository` (AC: 3)
  - [ ] Spring Data dérive automatiquement depuis le nom de méthode
- [ ] Modifier `DomainController` (AC: 1, 2)
  - [ ] Injecter `DomainService` (remplace appels directs à `DomainRepository`)
  - [ ] `getAllDomains()` → déléguer à `domainService.getDomainsWithStats()`
  - [ ] Ajouter `GET /api/domains/{domainId}/data-sets` → `domainService.getDatasetsByDomainWithStats()`
- [ ] Ajouter migration Flyway V008 si index manquant (AC: 3)
  - [ ] Vérifier si `idx_activity_dataset_action` existe dans V004
  - [ ] Si absent : `CREATE INDEX IF NOT EXISTS idx_activity_action ON activity(dataset_id, action)`
- [ ] Écrire tests (AC: 4)
  - [ ] `DomainServiceTest.java` — mock repositories, tester les 3 statuts
  - [ ] `DomainControllerIT.java` — intégration GET /api/domains enrichi

## Dev Notes

### État réel du code — points critiques

> ⚠️ `DomainController` **n'a pas de DomainService** — il appelle directement `DomainRepository`.
> Le commentaire dans le controller dit explicitement : "FUTURE: Extract business logic to DomainService".
> **Cette story est l'occasion de créer `DomainService.java`** et d'y migrer la logique.

> ⚠️ **`Activity.getAction()`** — le champ s'appelle `action` (pas `actionType`).
> Getter : `getAction()`. Utiliser `ActivityActionType.DOWNLOADED` et `ActivityActionType.VIEWED`.

> ⚠️ **Table SQL** : `domain_master` (pas `domain`) pour les @Query natives.
> La table DataSet s'appelle `datasets`. Voir `@Table` annotations.

> ⚠️ **Endpoint datasets par domaine** : `GET /api/domains/{domainId}/data-sets` (avec tiret, dans `DataGenerationController`).
> Le nouveau endpoint `data-sets enrichi` peut être ajouté dans `DomainController` ou `DataGenerationController` — préférer `DomainController` pour la cohérence de cette story.

### Schéma des entités réelles

```
Domain (@Table "domain_master")
  id, name, description, createdAt, updatedAt, deletedAt, version (optimistic lock)
  → findByDeletedAtIsNull()  ← méthode existante

DataSet (@Table "datasets")
  id, domainId, datasetName, rowCount, columnCount, generationTimeMs
  createdAt, updatedAt, deletedAt, version (default=0), dataJson, originalData
  → findByDomainIdAndDeletedAtIsNull(domainId)  ← méthode existante dans DataSetRepository

Activity (@Table "activity")
  id, dataSetId, action (enum ActivityActionType), timestamp, createdAt
  userName, rowIndex, modifiedColumns, previousValue
  → findByDataSetIdOrderByTimestampDesc(Long)   ← existe
  → findByDataSetIdAndActionOrderByTimestampDesc(Long, ActivityActionType)  ← existe
  → findByDataSetIdIn(List<Long>)  ← À CRÉER

ActivityActionType (enum)
  DOWNLOADED, MODIFIED, VIEWED, CREATED, RESET, DELETED, ROW_MODIFIED, ROW_DELETED
```

### Implémentation DomainService — logique anti-N+1

```java
// DomainService.java
@Service
public class DomainService {

    @Autowired private DomainRepository domainRepository;
    @Autowired private DataSetRepository dataSetRepository;
    @Autowired private ActivityRepository activityRepository;

    public List<DomainResponseDTO> getDomainsWithStats(int offset, int limit) {
        List<Domain> all = domainRepository.findByDeletedAtIsNull();
        List<Domain> page = all.subList(Math.min(offset, all.size()),
                                        Math.min(offset + limit, all.size()));

        List<Long> domainIds = page.stream().map(Domain::getId).collect(Collectors.toList());

        // Charger tous les datasets des domaines en une requête
        Map<Long, List<DataSet>> datasetsByDomain = dataSetRepository
            .findByDomainIdInAndDeletedAtIsNull(domainIds)
            .stream().collect(Collectors.groupingBy(DataSet::getDomainId));

        return page.stream().map(domain -> {
            List<DataSet> datasets = datasetsByDomain.getOrDefault(domain.getId(), List.of());
            List<Long> dsIds = datasets.stream().map(DataSet::getId).collect(Collectors.toList());

            // Statuts agrégés au niveau domaine
            DomainStatusDTO statuses = computeDomainStatus(dsIds, datasets);

            DomainResponseDTO dto = mapToDTO(domain);
            dto.setDatasetCount(datasets.size());
            dto.setTotalRows(datasets.stream().mapToLong(ds -> ds.getRowCount() != null ? ds.getRowCount() : 0).sum());
            dto.setStatuses(statuses);
            return dto;
        }).collect(Collectors.toList());
    }

    private DomainStatusDTO computeDomainStatus(List<Long> dsIds, List<DataSet> datasets) {
        if (dsIds.isEmpty()) return new DomainStatusDTO(false, false, false);

        List<Activity> activities = activityRepository.findByDataSetIdIn(dsIds);
        Set<ActivityActionType> actions = activities.stream()
            .map(Activity::getAction)   // ← getAction(), pas getActionType()
            .collect(Collectors.toSet());

        boolean downloaded = actions.contains(ActivityActionType.DOWNLOADED);
        boolean modified   = datasets.stream().anyMatch(ds -> ds.getVersion() != null && ds.getVersion() > 0);
        boolean viewed     = actions.contains(ActivityActionType.VIEWED);
        return new DomainStatusDTO(downloaded, modified, viewed);
    }
}
```

### ActivityRepository — méthode à ajouter

```java
// Ajouter dans ActivityRepository.java
List<Activity> findByDataSetIdIn(List<Long> dataSetIds);
```

### DataSetRepository — méthode à ajouter (si absente)

```java
// Vérifier d'abord si existe, sinon ajouter :
List<DataSet> findByDomainIdInAndDeletedAtIsNull(List<Long> domainIds);
```

### Flyway

- Migrations existantes : V001 → V007
- Prochaine : **V008** — vérifier d'abord dans `V004__activity_tracking.sql` si l'index `idx_activity_dataset` couvre `(dataset_id, action)`. Si oui, pas besoin de V008. Si non :

```sql
-- V008__add_activity_action_index.sql
CREATE INDEX IF NOT EXISTS idx_activity_dataset_action ON activity(dataset_id, action);
```

### Fichiers à créer / modifier

```
src/main/java/com/movkfact/
  dto/
    DomainStatusDTO.java            ← NOUVEAU
    DataSetSummaryDTO.java          ← NOUVEAU (datasets enrichis)
    DomainResponseDTO.java          ← ajouter datasetCount, totalRows, statuses
  service/
    DomainService.java              ← NOUVEAU
  controller/
    DomainController.java           ← injecter DomainService, déléguer getAllDomains()
                                       + ajouter GET /api/domains/{id}/data-sets enrichi
  repository/
    ActivityRepository.java         ← ajouter findByDataSetIdIn()
    DataSetRepository.java          ← vérifier/ajouter findByDomainIdInAndDeletedAtIsNull()

src/main/resources/db/migration/
  V008__add_activity_action_index.sql  ← si index manquant (vérifier V004 d'abord)

src/test/java/com/movkfact/
  service/DomainServiceTest.java    ← NOUVEAU
  controller/DomainControllerIT.java ← mettre à jour / créer
```

### Project Structure Notes

- Package : `com.movkfact` (tout en minuscules)
- Pattern de test intégration : `@SpringBootTest` + `@AutoConfigureMockMvc` — voir `DataGenerationControllerTest` pour le pattern
- Profil test : H2 in-memory (`@ActiveProfiles("test")` si configuré, sinon application.properties par défaut)
- `ApiResponse<T>` : wrapper standard — les responses doivent être enveloppées dans `ApiResponse.success(data, message)`

### References

- Epic spec : [prjdocs/epics/epic-8-domain-enrichment/8-1-backend-domain-aggregates.md](../epics/epic-8-domain-enrichment/8-1-backend-domain-aggregates.md)
- Entity réelle : `src/main/java/com/movkfact/entity/Domain.java` (@Table "domain_master")
- Activity entity : `src/main/java/com/movkfact/entity/Activity.java` — champ `action`, getter `getAction()`
- Controller existant : `src/main/java/com/movkfact/controller/DomainController.java`
- PRD : FR-002, FR-003

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List
