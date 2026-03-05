# Story S8.1: Backend Agrégats par domaine

**Sprint:** À planifier (Epic 8)
**Points:** 4
**Epic:** EPIC 8 - Enrichissement vue domaines
**Type:** Backend Feature
**Lead:** Amelia
**Status:** Backlog
**Dependencies:** S1.2, S1.3, S2.3, S2.8 (Domain + DataSet + Activity entities)
**FRs couverts:** FR-002, FR-003

---

## User Story

**En tant que** développeur ou Tech Lead,
**Je veux** voir le nombre de datasets, le total de lignes générées et les statuts agrégés pour chaque domaine dans la liste,
**Afin d'** évaluer rapidement l'utilisation de mes domaines sans ouvrir chacun individuellement.

---

## Contexte métier

Le PRD FR-002 exige que la liste des domaines affiche, pour chacun : nombre de datasets, nombre total de lignes et statuts (téléchargé, modifié, consulté).
Actuellement `GET /api/domains` retourne les champs de base de `Domain` uniquement.
FR-003 exige que la liste des datasets d'un domaine inclue le statut individuel et la date de dernière modification.

---

## ✅ Acceptance Criteria

### AC1 — GET /api/domains enrichi (FR-002)
- [ ] `GET /api/domains` retourne, pour chaque domaine :
  - `datasetCount` (Integer) : nombre de datasets non-supprimés (soft-delete)
  - `totalRows` (Long) : somme de `dataset.rowCount` pour tous ses datasets
  - `statuses` : `{ downloaded: boolean, modified: boolean, viewed: boolean }` (au moins un dataset du domaine dans cet état)
- [ ] La réponse est paginée (paramètres `page`, `size` existants conservés)
- [ ] Performance : requête avec JOIN/agrégation SQL en < 200ms pour 100 domaines
- [ ] Aucune régression sur les champs existants (`id`, `name`, `description`, `createdAt`)

### AC2 — GET /api/domains/{id}/datasets avec statuts (FR-003)
- [ ] `GET /api/domains/{id}/datasets` retourne pour chaque dataset :
  - `id`, `name`, `rowCount`, `columnCount`, `createdAt`, `updatedAt` (existants)
  - `status` : `{ downloaded: boolean, modified: boolean, viewed: boolean }` (état courant)
  - `lastActivity` : timestamp de la dernière activité (null si jamais consulté)
- [ ] Tri par défaut : `updatedAt DESC`
- [ ] 404 si le domaine n'existe pas

### AC3 — Calcul des statuts agrégés
- [ ] `downloaded = true` si au moins une `Activity(DOWNLOADED)` existe pour le dataset
- [ ] `modified = true` si `dataset.version > 0` (au moins une modification depuis création)
- [ ] `viewed = true` si au moins une `Activity(VIEWED)` existe pour le dataset
- [ ] Calcul via requête JPQL ou SQL natif (pas de N+1 queries)

### AC4 — Tests
- [ ] Test unitaire : calcul des statuts agrégés (mock repository)
- [ ] Test d'intégration : `GET /api/domains` avec données de test (2 domaines, 3 datasets)
- [ ] Test d'intégration : `GET /api/domains/{id}/datasets` avec statuts corrects
- [ ] Coverage >80% sur les nouvelles méthodes

---

## 🏗️ Spécifications Techniques

### Modifications requises

```
src/main/java/com/movkfact/dto/DomainResponseDTO.java
  + Integer datasetCount
  + Long totalRows
  + DomainStatusDTO statuses

src/main/java/com/movkfact/dto/DomainStatusDTO.java  (nouveau)
  + boolean downloaded
  + boolean modified
  + boolean viewed

src/main/java/com/movkfact/dto/DataSetDTO.java
  + DomainStatusDTO status
  + LocalDateTime lastActivity
```

### Requête JPQL recommandée (agrégats domaine)

```java
// DomainRepository — méthode personnalisée
@Query("""
    SELECT d.id, d.name, d.description, d.createdAt,
           COUNT(ds.id) as datasetCount,
           COALESCE(SUM(ds.rowCount), 0) as totalRows
    FROM Domain d
    LEFT JOIN DataSet ds ON ds.domainId = d.id AND ds.deletedAt IS NULL
    GROUP BY d.id, d.name, d.description, d.createdAt
    ORDER BY d.createdAt DESC
    """)
List<Object[]> findDomainsWithAggregates(Pageable pageable);
```

### Calcul des statuts (dans DomainService)

```java
// Éviter N+1 : charger toutes les activities des datasets d'un domaine en une requête
List<Activity> activities = activityRepository
    .findByDatasetIdsIn(datasetIds);

Map<Long, Set<ActivityActionType>> actsByDataset = activities.stream()
    .collect(Collectors.groupingBy(
        Activity::getDataSetId,
        Collectors.mapping(Activity::getActionType, Collectors.toSet())
    ));

DomainStatusDTO status = new DomainStatusDTO(
    actsByDataset.getOrDefault(ds.getId(), Set.of()).contains(DOWNLOADED),
    ds.getVersion() > 0,   // modified
    actsByDataset.getOrDefault(ds.getId(), Set.of()).contains(VIEWED)
);
```

### Migration Flyway (si index manquant)

```sql
-- V007__add_dataset_activity_index.sql
CREATE INDEX IF NOT EXISTS idx_activity_dataset_action
    ON activities (dataset_id, action_type);
```

---

## 📝 Dev Notes

- `ActivityRepository` a besoin d'une méthode `findByDataSetIdIn(List<Long> ids)` pour éviter N+1
- `DomainRepository` peut utiliser une `@Query` JPQL ou une projection interface Spring Data
- Les statuts agrégés au niveau domaine (`DomainStatusDTO statuses`) = OR logique sur tous les datasets du domaine
- Le champ `modified` utilise `dataset.version > 0` (déjà tracé par S2.9/S3.1) — pas besoin d'interroger les activités
- Tester avec `@DataJpaTest` pour la couche repository, `@WebMvcTest` pour le controller

---

## 📊 Estimation

| Composant | Effort | Responsable |
|-----------|--------|-------------|
| Extension DTOs (DomainResponseDTO + DataSetDTO + DomainStatusDTO) | 0.25j | Amelia |
| Requête JPQL agrégats + méthode repository | 0.5j | Amelia |
| Calcul statuts (éviter N+1) | 0.5j | Amelia |
| Tests unitaires + intégration | 0.75j | Amelia |
| **Total** | **2j** | **4 pts** |
