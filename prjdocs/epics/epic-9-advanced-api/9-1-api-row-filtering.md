# Story S9.1: API d'accès avancé — filtrage par row et sélection de colonnes

**Sprint:** À planifier (Epic 9)
**Points:** 5
**Epic:** EPIC 9 - API d'accès avancé
**Type:** Backend Feature
**Lead:** Amelia
**Status:** Backlog
**Dependencies:** S2.3, S6.1 (DataRowEditorController — si livré en premier)
**FRs couverts:** FR-014, FR-015

---

## User Story

**En tant que** système externe intégré dans un pipeline CI/CD,
**Je veux** extraire des lignes spécifiques d'un dataset par identifiant et sélectionner uniquement les colonnes nécessaires via l'API REST,
**Afin d'** intégrer des sous-ensembles de données dans mes tests sans télécharger l'intégralité du dataset.

---

## Contexte métier

FR-014 : "Les systèmes externes peuvent télécharger et extraire des données via des APIs REST, avec filtrage par identifiant de ligne et sélection de colonnes."
Actuellement, `GET /api/data-sets/{id}/data` (S2.3) retourne toutes les lignes sans filtrage.
Cette story ajoute le filtrage granulaire pour les intégrations CI/CD.

---

## ✅ Acceptance Criteria

### AC1 — Filtrage par rowIndex
- [ ] `GET /api/data-sets/{id}/rows?rowIds=0,5,10,99` retourne uniquement les lignes aux index spécifiés
- [ ] `rowIds` : liste d'entiers séparés par virgules (0-based)
- [ ] Si un `rowId` est hors-bornes : ignoré (pas de 404 — retourner les valides uniquement)
- [ ] Si `rowIds` est absent : comportement paginé standard (AC de S6.1)
- [ ] Réponse : `{ rows: [{rowIndex: 0, data: {...}}, ...], requestedCount: N, returnedCount: M }`

### AC2 — Sélection de colonnes
- [ ] `GET /api/data-sets/{id}/rows?cols=firstName,email` retourne uniquement les colonnes listées
- [ ] Colonnes inconnues : ignorées silencieusement (retourner les colonnes valides)
- [ ] `cols` absent : retourner toutes les colonnes (comportement par défaut)
- [ ] Combinaison possible : `?rowIds=1,5&cols=firstName,email`

### AC3 — Accès ligne unique amélioré
- [ ] `GET /api/data-sets/{id}/rows/{rowIndex}?cols=firstName,email` supporte aussi la sélection de colonnes
- [ ] Réponse : `{ rowIndex: N, data: { "firstName": "Alice", "email": "alice@test.com" } }`

### AC4 — Performance
- [ ] Temps de réponse < 500ms pour extraction de 100 rowIds sur un dataset de 100 000 lignes
- [ ] Pas de chargement complet du JSON si seulement quelques rows demandés — utiliser la désérialisation ciblée si possible

### AC5 — Documentation OpenAPI
- [ ] Endpoint documenté dans Swagger avec paramètres `rowIds` et `cols`
- [ ] Exemples de requêtes dans la documentation
- [ ] Codes HTTP documentés : 200, 400 (rowIds non-numériques), 404 (dataset inexistant)

### AC6 — Tests
- [ ] Test `GET /api/data-sets/{id}/rows?rowIds=0,2,4` → retourne 3 lignes exactes
- [ ] Test `GET /api/data-sets/{id}/rows?cols=firstName` → retourne uniquement la colonne `firstName`
- [ ] Test combinaison `rowIds + cols`
- [ ] Test rowIds hors-bornes (ignorés)
- [ ] Test colonnes inconnues (ignorées)
- [ ] Coverage >80%

---

## 🏗️ Spécifications Techniques

### Modifications requises

```
src/main/java/com/movkfact/service/DataRowEditorService.java (S6.1)
  + getRowsByIds(Long datasetId, List<Integer> rowIds, List<String> cols)
  + getRowsPaged(Long datasetId, int page, int size, List<String> cols)

src/main/java/com/movkfact/controller/DataRowEditorController.java (S6.1)
  → Modifier GET /api/data-sets/{id}/rows pour accepter ?rowIds et ?cols

src/main/java/com/movkfact/dto/PagedRowsResponseDTO.java (S6.1)
  + Integer requestedCount
  + Integer returnedCount
```

*Note : si S6.1 n'est pas encore livré, créer `DataAccessController.java` séparé.*

### Logique de filtrage (dans DataRowEditorService)

```java
public List<Map<String, Object>> getRowsByIds(Long datasetId,
        List<Integer> rowIds, List<String> cols) {
    DataSet dataset = dataSetRepository.findById(datasetId)
        .orElseThrow(() -> new EntityNotFoundException("DataSet not found"));

    List<Map<String, Object>> allRows = objectMapper.readValue(
        dataset.getDataJson(), new TypeReference<>() {});

    return rowIds.stream()
        .filter(idx -> idx >= 0 && idx < allRows.size())  // ignorer hors-bornes
        .map(idx -> filterColumns(allRows.get(idx), cols))
        .collect(Collectors.toList());
}

private Map<String, Object> filterColumns(Map<String, Object> row,
        List<String> cols) {
    if (cols == null || cols.isEmpty()) return row;
    return cols.stream()
        .filter(row::containsKey)  // ignorer colonnes inconnues
        .collect(Collectors.toMap(col -> col, row::get, (a, b) -> a, LinkedHashMap::new));
}
```

### Parsing des paramètres query

```java
// Dans le controller :
@GetMapping("/{id}/rows")
public ResponseEntity<?> getRows(
        @PathVariable Long id,
        @RequestParam(required = false) List<Integer> rowIds,
        @RequestParam(required = false) List<String> cols,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size) {
    // Si rowIds présent → filtrage ciblé
    // Sinon → pagination standard
}
```

---

## 📝 Dev Notes

- Pour les datasets de 100k lignes, parser tout le JSON en mémoire (LONGTEXT) reste la seule option avec le modèle actuel de stockage — noter comme dette technique (migration vers stockage lignes-par-lignes en Phase 3)
- Les paramètres `rowIds` et `cols` sont optionnels et indépendants
- Si `rowIds` contient des valeurs non-numériques → 400 Bad Request (géré par Spring validation automatique via `List<Integer>`)
- Cette story peut être développée en parallèle de S6.1 ou après (dépendance partielle sur les DTOs)

---

## 📊 Estimation

| Composant | Effort | Responsable |
|-----------|--------|-------------|
| Extension `DataRowEditorService` (filtrage rowIds + cols) | 0.75j | Amelia |
| Modification endpoint GET /rows (paramètres optionnels) | 0.25j | Amelia |
| Documentation OpenAPI (Swagger annotations) | 0.25j | Amelia |
| Tests d'intégration (6 scénarios) | 0.75j | Amelia |
| **Total** | **2j** | **5 pts** |
