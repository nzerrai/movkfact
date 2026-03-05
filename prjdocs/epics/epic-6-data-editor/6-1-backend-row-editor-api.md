# Story S6.1: Backend Row Editor API

**Sprint:** À planifier (Epic 6)
**Points:** 5
**Epic:** EPIC 6 - Data Editor (Éditeur de données inline)
**Type:** Backend Feature
**Lead:** Amelia
**Status:** Backlog
**Dependencies:** S2.1, S2.8, S3.1 (DataSet entity, ActivityService)
**FRs couverts:** FR-011, FR-012, FR-016, FR-019

---

## User Story

**En tant que** développeur ou Tech Lead,
**Je veux** pouvoir lire, modifier et supprimer des lignes individuelles d'un dataset via l'API,
**Afin de** corriger des valeurs incorrectes sans régénérer l'intégralité du dataset, avec une traçabilité complète de chaque opération.

---

## Contexte métier

Les données stockées dans `DataSet.dataJson` (LONGTEXT) sont un tableau JSON sérialisé.
Les opérations row-level nécessitent de parser ce JSON, localiser la ligne par son index (0-based), effectuer la modification ou suppression, puis re-sérialiser.
Chaque modification doit générer une `Activity` horodatée avec les colonnes modifiées.
La `originalData` existante dans `DataSet` garantit que le reset (FR-018) reste fonctionnel après modifications.

---

## ✅ Acceptance Criteria

### AC1 — GET rows paginé
- [ ] `GET /api/data-sets/{id}/rows?page=0&size=50` retourne les lignes paginées
- [ ] Réponse : `{ rows: [{rowIndex: 0, data: {...}}, ...], totalRows: N, page: X, size: Y }`
- [ ] `rowIndex` = index 0-based dans le tableau JSON original (stable pour adressage)
- [ ] 404 si dataset inexistant ou soft-deleted
- [ ] Réponse < 200ms pour 10 000 lignes (pagination côté serveur)

### AC2 — GET ligne unique
- [ ] `GET /api/data-sets/{id}/rows/{rowIndex}` retourne la ligne exacte
- [ ] 404 si rowIndex hors bornes
- [ ] Réponse : `{ rowIndex: N, data: {...} }`

### AC3 — PUT modification de ligne
- [ ] `PUT /api/data-sets/{id}/rows/{rowIndex}` accepte `{ columns: { "colName": "newValue", ... } }`
- [ ] Met à jour uniquement les colonnes spécifiées (merge partiel, pas de remplacement total)
- [ ] Met à jour `DataSet.dataJson` en base
- [ ] Incrémente `DataSet.version`
- [ ] Met à jour `DataSet.updatedAt`
- [ ] Retourne la ligne mise à jour : `{ rowIndex: N, data: {...} }`
- [ ] 400 si `columns` vide ou null
- [ ] 404 si dataset ou rowIndex inexistant

### AC4 — DELETE suppression de ligne
- [ ] `DELETE /api/data-sets/{id}/rows/{rowIndex}` supprime la ligne du JSON
- [ ] Le tableau JSON est compacté (les indices suivants décrémentent)
- [ ] `DataSet.rowCount` décrémenté de 1
- [ ] `DataSet.version` incrémenté
- [ ] 204 No Content en succès
- [ ] 404 si dataset ou rowIndex inexistant

### AC5 — Trace d'activité row-level (FR-019)
- [ ] `ActivityActionType` étendu : `ROW_MODIFIED`, `ROW_DELETED`
- [ ] `Activity` étendue : champs `rowIndex` (Integer) + `modifiedColumns` (String JSON) + `previousValue` (String JSON, snapshot avant modif)
- [ ] Chaque PUT génère une `Activity(ROW_MODIFIED, rowIndex, modifiedColumns, previousValue)`
- [ ] Chaque DELETE génère une `Activity(ROW_DELETED, rowIndex, previousValue=snapshot complet de la ligne)`
- [ ] `GET /api/data-sets/{id}/activity` retourne les activités row-level avec les nouvelles colonnes
- [ ] `originalData` jamais modifiée par les opérations row-level

### AC6 — Tests
- [ ] Tests unitaires `DataRowEditorService` (>80% coverage)
- [ ] Tests d'intégration pour les 4 endpoints (GET paginé, GET unique, PUT, DELETE)
- [ ] Test de régression : reset via `POST /api/data-sets/{id}/reset` restaure bien l'état original après modifications

### AC7 — Circuit breaker datasets larges *(ajouté party mode 04/03)*
- [ ] `PUT` et `DELETE /api/data-sets/{id}/rows/{rowIndex}` retournent `413 Payload Too Large` si `dataset.rowCount > 50 000`
- [ ] Message d'erreur : `"Dataset trop volumineux pour édition ligne par ligne. Utilisez l'API bulk ou régénérez."`
- [ ] `GET /api/data-sets/{id}/rows` (lecture seule) non affecté par cette limite

### AC8 — Migration Flyway V006 sans régression *(ajouté party mode 04/03)*
- [ ] Colonnes `row_index`, `modified_columns`, `previous_value` définies `NULLABLE` dans V006
- [ ] Les `Activity` existantes (DOWNLOADED, VIEWED, CREATED, etc.) conservent `row_index = NULL` sans erreur
- [ ] Tests d'intégration existants sur `ActivityService` passent sans modification

---

## 🏗️ Spécifications Techniques

### Nouveaux fichiers à créer

```
src/main/java/com/movkfact/
  service/
    DataRowEditorService.java       ← logique parse/modify/serialize JSON
  controller/
    DataRowEditorController.java    ← endpoints row-level
  dto/
    RowUpdateRequestDTO.java        ← { columns: Map<String, Object> }
    RowResponseDTO.java             ← { rowIndex: int, data: Map<String, Object> }
    PagedRowsResponseDTO.java       ← { rows: List<RowResponseDTO>, totalRows, page, size }
```

### Modifications requises

```
src/main/java/com/movkfact/entity/ActivityActionType.java
  + ROW_MODIFIED
  + ROW_DELETED

src/main/java/com/movkfact/entity/Activity.java
  + Integer rowIndex
  + String modifiedColumns   (JSON array des noms de colonnes modifiées)
  + String previousValue     (JSON snapshot de la ligne avant modification)

src/main/resources/db/migration/
  V006__add_row_editor_activity_columns.sql
    ALTER TABLE activities ADD COLUMN row_index INTEGER;
    ALTER TABLE activities ADD COLUMN modified_columns TEXT;
    ALTER TABLE activities ADD COLUMN previous_value LONGTEXT;
```

### DataRowEditorService — Logique clé

```java
// Parse dataJson → List<Map<String,Object>>
List<Map<String, Object>> rows = objectMapper.readValue(dataset.getDataJson(),
    new TypeReference<List<Map<String, Object>>>() {});

// PUT: merge partiel
Map<String, Object> row = rows.get(rowIndex);
Map<String, Object> snapshot = new HashMap<>(row);  // snapshot pour Activity
request.getColumns().forEach(row::put);
rows.set(rowIndex, row);

// DELETE: remove + compact
Map<String, Object> snapshot = rows.remove(rowIndex);
dataset.setRowCount(dataset.getRowCount() - 1);

// Re-serialize
dataset.setDataJson(objectMapper.writeValueAsString(rows));
dataset.setVersion(dataset.getVersion() + 1);
dataSetRepository.save(dataset);
```

### Endpoints résumé

| Méthode | URL | Description |
|---------|-----|-------------|
| GET | `/api/data-sets/{id}/rows` | Liste paginée |
| GET | `/api/data-sets/{id}/rows/{rowIndex}` | Ligne unique |
| PUT | `/api/data-sets/{id}/rows/{rowIndex}` | Modifier colonnes |
| DELETE | `/api/data-sets/{id}/rows/{rowIndex}` | Supprimer ligne |

---

## 📝 Dev Notes

- Utiliser `com.fasterxml.jackson.databind.ObjectMapper` (déjà dans le classpath Spring Boot)
- `TypeReference<List<Map<String, Object>>>` pour désérialisation générique
- Attention aux datasets larges (100k lignes) : parser le JSON entier en mémoire est acceptable pour le MVP mais noter comme point de vigilance performance
- La colonne `modifiedColumns` dans `Activity` = JSON array `["firstName", "email"]`
- `previousValue` = JSON object `{"firstName": "Alice", "email": "alice@old.com"}` (snapshot partiel des colonnes modifiées uniquement pour PUT, snapshot complet pour DELETE)
- Ne jamais modifier `originalData` — elle reste le snapshot immuable au moment de la génération initiale

---

## 📊 Estimation

| Composant | Effort | Responsable |
|-----------|--------|-------------|
| `DataRowEditorService` (parse/modify/serialize + activité) | 1j | Amelia |
| `DataRowEditorController` (4 endpoints + DTOs) | 0.5j | Amelia |
| Migration Flyway V006 + `Activity` étendue | 0.25j | Amelia |
| Tests unitaires + intégration | 1j | Amelia |
| **Total** | **2.75j** | **5 pts** |
