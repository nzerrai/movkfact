---
title: 'S10.2 — Gestion dynamique des patterns de détection'
slug: 'detection-patterns-db-ui'
created: '2026-03-09'
status: 'in-progress'
stepsCompleted: [1, 2, 3, 4]
tech_stack: ['Spring Boot', 'JPA/Hibernate', 'Flyway', 'React', 'MUI']
files_to_modify:
  - src/main/resources/db/migration/V010__detection_patterns.sql
  - src/main/java/com/movkfact/entity/DetectionPattern.java
  - src/main/java/com/movkfact/repository/DetectionPatternRepository.java
  - src/main/java/com/movkfact/service/detection/PatternCache.java
  - src/main/java/com/movkfact/controller/PatternController.java
  - movkfact-frontend/src/pages/SettingsPage.jsx
code_patterns:
  - 'Modèle JPA identique à BankingLexiconEntry (BIGSERIAL, VARCHAR, index)'
  - 'Flyway V010 avec INSERT depuis contenu de patterns.yml'
  - 'PatternCache : remplacer @PostConstruct YAML par chargement BDD + méthode reload()'
  - 'Controller REST : pattern CRUD identique à BankingLexiconRepository'
test_patterns:
  - '@SpringBootTest pour PatternCache.reload()'
  - 'Tests REST via MockMvc pour CRUD /api/settings/patterns'
  - 'Tests unitaires : regex invalide → 400, reload immédiat après save'
---

# Tech-Spec : S10.2 — Gestion dynamique des patterns de détection

**Créé :** 2026-03-09

---

## Overview

### Problem Statement

Les patterns de détection de types de colonnes sont codés dans `patterns.yml` (fichier statique sur le classpath). Il est impossible de les modifier sans redéployer l'application. Tout ajout de nouveau pattern (ex: domaine métier spécifique) nécessite un accès au code source et un rebuild.

### Solution

Migrer les patterns vers une table BDD `detection_pattern`. Au premier démarrage (Flyway V010), les patterns de `patterns.yml` sont importés automatiquement. `PatternCache` charge ensuite depuis la BDD. Un endpoint REST `CRUD` + une IHM dans `SettingsPage` permettent d'ajouter, modifier ou supprimer des patterns à chaud, sans redémarrage.

### Scope

**In Scope :**
- Table BDD `detection_pattern` + migration Flyway V010 avec seed depuis patterns.yml
- Refactoring `PatternCache` : chargement BDD + méthode `reload()` publique
- API REST `/api/settings/patterns` : GET all, POST (créer), PUT (modifier), DELETE
- Endpoint `POST /api/settings/patterns/reload` → recharge le cache en mémoire
- IHM SettingsPage : onglet "Patterns de détection" avec DataGrid, formulaire ajout/édition, suppression avec confirmation, bouton rechargement
- Validation backend : regex invalide → HTTP 400 avec message d'erreur

**Out of Scope :**
- Contrôle d'accès rôle admin (JWT phase 2, future story)
- Export/import YAML depuis l'IHM
- Versioning ou historique des modifications de patterns
- Tests de performance du rechargement sous charge

---

## Context for Development

### Codebase Patterns

**Entité JPA :**
- Modèle de référence : `BankingLexiconEntry` (`@Entity`, `@Table`, `@Column`, `BIGSERIAL`, getters sans setter — lecture seule)
- Pour `DetectionPattern`, des setters sont nécessaires (entité mutable)
- Pas de Lombok dans le projet — getters/setters manuels

**Repository :**
- Étend `JpaRepository<T, Long>` — même pattern que `BankingLexiconRepository`
- Méthode custom à ajouter : `List<DetectionPattern> findByColumnType(String columnType)`

**PatternCache (fichier clé) :**
- Actuellement : charge `patterns.yml` via classpath dans `@PostConstruct`
- Après refactor : `@PostConstruct` appelle `reload()` qui charge depuis `DetectionPatternRepository`
- `reload()` : vider `patternCache`, re-itérer `ColumnType.values()`, recompiler depuis BDD
- La map interne reste `Map<ColumnType, List<Pattern>> patternCache`
- `reload()` doit être `synchronized` pour thread-safety

**Flyway :**
- Dernier script : `V009__add_column_type_feedback.sql`
- Prochain : `V010__detection_patterns.sql`
- Les INSERTs doivent inclure TOUTES les entrées actuelles de `patterns.yml` (24 types, ~90 patterns)

**Frontend :**
- `SettingsPage.jsx` est une coquille vide (MUI `List`, `Paper`, `Divider` importés)
- Stack UI : MUI v5, `axios` pour les appels API, `useState`/`useEffect` hooks
- Pas de routing interne dans Settings — tout sur une seule page avec onglets MUI `Tabs`

### Files to Reference

| Fichier | Rôle |
|---------|------|
| `src/main/java/com/movkfact/entity/BankingLexiconEntry.java` | Modèle JPA de référence |
| `src/main/java/com/movkfact/repository/BankingLexiconRepository.java` | Repository JPA de référence |
| `src/main/resources/db/migration/V007__banking_lexicon.sql` | Modèle de migration Flyway avec INSERTs |
| `src/main/java/com/movkfact/service/detection/PatternCache.java` | Fichier principal à refactorer |
| `src/main/resources/patterns.yml` | Source des patterns initiaux (seed V010) |
| `movkfact-frontend/src/pages/SettingsPage.jsx` | Page à enrichir avec l'onglet patterns |

### Technical Decisions

1. **Une ligne BDD par regex** (pas un blob JSON) : chaque pattern est une ligne `(column_type VARCHAR, pattern VARCHAR, description VARCHAR)`. Cela permet l'édition individuelle en IHM sans parser de JSON.

2. **Reload synchrone côté serveur** : `PatternCache.reload()` est `synchronized`. L'appel depuis le controller bloque jusqu'à la fin. Délai acceptable (< 200ms pour ~90 patterns).

3. **Validation regex avant persistence** : le service tente `Pattern.compile(regex)` avant `save()`. Si exception → lever `IllegalArgumentException` → controller renvoie HTTP 400.

4. **patterns.yml conservé** mais non utilisé au runtime après V010. Il reste comme documentation de référence et backup.

---

## Implementation Plan

### Tasks

Les tâches sont ordonnées par dépendance (BDD → backend → frontend).

---

**TÂCHE 1 — Migration Flyway V010**
- Fichier : `src/main/resources/db/migration/V010__detection_patterns.sql`
- Créer la table :
```sql
CREATE TABLE detection_pattern (
    id          BIGSERIAL PRIMARY KEY,
    column_type VARCHAR(50)  NOT NULL,
    pattern     VARCHAR(255) NOT NULL,
    description VARCHAR(255)
);
CREATE INDEX idx_detection_pattern_type ON detection_pattern(column_type);
```
- Insérer TOUS les patterns de `patterns.yml` actuels (24 types, ~90 lignes) :
```sql
INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('FIRST_NAME', '(?i)^first_?name$', 'EN standard'),
  ('FIRST_NAME', '(?i)^prenom$',      'FR standard'),
  ...
  ('COMPANY',    '(?i)^employeur$',   'FR employeur');
```

---

**TÂCHE 2 — Entité JPA `DetectionPattern`**
- Fichier à créer : `src/main/java/com/movkfact/entity/DetectionPattern.java`
- Package : `com.movkfact.entity`
- Annotations : `@Entity`, `@Table(name = "detection_pattern")`
- Champs :
  - `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id`
  - `@Column(name = "column_type", nullable = false, length = 50) private String columnType`
  - `@Column(nullable = false, length = 255) private String pattern`
  - `@Column(length = 255) private String description`
- Getters + setters complets (pas de Lombok)

---

**TÂCHE 3 — Repository `DetectionPatternRepository`**
- Fichier à créer : `src/main/java/com/movkfact/repository/DetectionPatternRepository.java`
- Étend `JpaRepository<DetectionPattern, Long>`
- Méthode custom : `List<DetectionPattern> findByColumnType(String columnType);`

---

**TÂCHE 4 — Refactoring `PatternCache`**
- Fichier : `src/main/java/com/movkfact/service/detection/PatternCache.java`
- Injecter `DetectionPatternRepository` via constructeur (pas de `@Autowired` sur champ)
- Remplacer le corps de `initializePatterns()` par un appel à `reload()`
- Nouvelle méthode publique `synchronized void reload()` :
```java
public synchronized void reload() {
    patternCache.clear();
    List<DetectionPattern> all = repository.findAll();
    for (ColumnType columnType : ColumnType.values()) {
        String typeName = columnType.name();
        List<Pattern> compiled = all.stream()
            .filter(p -> typeName.equals(p.getColumnType()))
            .map(p -> {
                try { return Pattern.compile(p.getPattern()); }
                catch (Exception e) {
                    logger.warn("Pattern invalide ignoré: {} - {}", typeName, p.getPattern());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (!compiled.isEmpty()) patternCache.put(columnType, compiled);
    }
    logger.info("PatternCache: rechargé — {} types actifs", patternCache.size());
}
```
- Supprimer les imports liés à YAML (`snakeyaml`, `InputStream`)
- Ajouter imports : `DetectionPattern`, `DetectionPatternRepository`, `Objects`

---

**TÂCHE 5 — Service `DetectionPatternService`**
- Fichier à créer : `src/main/java/com/movkfact/service/DetectionPatternService.java`
- Package : `com.movkfact.service`
- Annoté `@Service`
- Injecter `DetectionPatternRepository` + `PatternCache` via constructeur
- Méthodes :
```java
List<DetectionPattern> findAll()
DetectionPattern create(DetectionPattern dto)   // valide regex avant save + reload()
DetectionPattern update(Long id, DetectionPattern dto) // valide regex + reload()
void delete(Long id)                            // findById ou 404 + delete + reload()
void reload()                                   // patternCache.reload() uniquement
```
- Validation regex : `Pattern.compile(dto.getPattern())` — si `PatternSyntaxException` → `throw new IllegalArgumentException("Regex invalide : " + e.getMessage())`

---

**TÂCHE 6 — Controller REST `PatternController`**
- Fichier à créer : `src/main/java/com/movkfact/controller/PatternController.java`
- `@RestController @RequestMapping("/api/settings/patterns")`
- Endpoints :
  - `GET /` → `200 List<DetectionPattern>`
  - `POST /` → body `DetectionPattern` → `201 DetectionPattern` | `400` si regex invalide
  - `PUT /{id}` → body `DetectionPattern` → `200 DetectionPattern` | `400` | `404`
  - `DELETE /{id}` → `204 No Content` | `404`
  - `POST /reload` → `200 { "reloaded": true, "types": N }`
- Gestion des erreurs : `@ExceptionHandler(IllegalArgumentException.class)` → `ResponseEntity.badRequest().body(Map.of("error", e.getMessage()))`

---

**TÂCHE 7 — IHM SettingsPage**
- Fichier : `movkfact-frontend/src/pages/SettingsPage.jsx`
- Ajouter onglets MUI `Tabs` : "Général" (contenu actuel) + "Patterns de détection"
- Onglet "Patterns de détection" :
  - `useEffect` → `GET /api/settings/patterns` → grouper par `columnType`
  - Accordéon MUI `Accordion` par type (`FIRST_NAME`, `EMAIL`...) avec liste des patterns
  - Bouton "+ Ajouter un pattern" par type → ouvre `Dialog` avec champs `columnType` (select `ColumnType`), `pattern` (TextField), `description` (TextField)
  - Icône suppression par ligne → `Dialog` de confirmation → `DELETE /api/settings/patterns/{id}`
  - Icône édition par ligne → ouvre `Dialog` pré-rempli → `PUT`
  - Bouton "Recharger le cache" (en haut de l'onglet) → `POST /api/settings/patterns/reload` → `Snackbar` succès/erreur
  - Validation frontend : champ `pattern` non vide avant submit
  - Après chaque opération CRUD : rechargement automatique de la liste

---

### Acceptance Criteria

**AC1 — Seed initial**
- Étant donné un démarrage à froid sur une BDD vide
- Quand Flyway s'exécute
- Alors la table `detection_pattern` contient exactement tous les patterns de `patterns.yml` (≥ 85 lignes)

**AC2 — PatternCache charge depuis BDD**
- Étant donné l'application démarrée
- Quand `PatternCache.getPatterns(ColumnType.URL)` est appelé
- Alors il retourne les patterns compilés correspondant aux entrées BDD (pas du YAML classpath)

**AC3 — Ajout d'un pattern, rechargement immédiat**
- Étant donné un pattern inexistant pour le type `LAST_NAME`
- Quand `POST /api/settings/patterns` est appelé avec `{"columnType":"LAST_NAME","pattern":"(?i)^nom_personne$"}`
- Et que `POST /api/settings/patterns/reload` est appelé
- Alors `PatternCache.getPatterns(ColumnType.LAST_NAME)` contient le nouveau pattern compilé

**AC4 — Regex invalide rejetée**
- Étant donné un pattern syntaxiquement invalide (`[invalid`)
- Quand `POST /api/settings/patterns` est appelé
- Alors HTTP 400 est retourné avec `{"error": "Regex invalide : ..."}`
- Et la BDD ne contient pas ce pattern

**AC5 — Suppression d'un pattern**
- Étant donné un pattern existant en BDD
- Quand `DELETE /api/settings/patterns/{id}` est appelé
- Alors HTTP 204 est retourné
- Et après reload, le pattern n'est plus dans le cache

**AC6 — IHM : liste groupée par type**
- Étant donné l'onglet "Patterns de détection" ouvert
- Quand la page se charge
- Alors les patterns sont affichés groupés par `columnType` dans des accordéons distincts

**AC7 — IHM : rechargement cache via bouton**
- Étant donné un pattern ajouté via l'IHM
- Quand l'utilisateur clique "Recharger le cache"
- Alors une `Snackbar` verte s'affiche avec "Cache rechargé — N types actifs"

**AC8 — Suppression avec confirmation**
- Étant donné une ligne de pattern visible dans l'IHM
- Quand l'utilisateur clique l'icône supprimer
- Alors un dialog de confirmation s'affiche avant suppression effective

---

## Additional Context

### Dependencies

- **Flyway** : déjà configuré (`flyway-core` dans `pom.xml`), aucune dépendance à ajouter
- **SnakeYAML** : importé dans `PatternCache` mais à retirer après refactoring (s'il n'est plus utilisé ailleurs, retirer du `pom.xml`)
- **MUI DataGrid** : vérifier si déjà dans `package.json` — si non, utiliser `Table` MUI standard (éviter d'ajouter `@mui/x-data-grid` si absent)

### Testing Strategy

| Test | Type | Fichier |
|------|------|---------|
| `PatternCache` charge depuis BDD au démarrage | `@SpringBootTest` | `PatternCacheIntegrationTest.java` |
| `PatternCache.reload()` recharge après INSERT BDD | `@SpringBootTest` | `PatternCacheIntegrationTest.java` |
| `POST /api/settings/patterns` — happy path | `@WebMvcTest` MockMvc | `PatternControllerTest.java` |
| `POST /api/settings/patterns` — regex invalide → 400 | `@WebMvcTest` MockMvc | `PatternControllerTest.java` |
| `DELETE /api/settings/patterns/{id}` — 404 inconnu | `@WebMvcTest` MockMvc | `PatternControllerTest.java` |
| `POST /api/settings/patterns/reload` → cache mis à jour | `@SpringBootTest` | `PatternCacheIntegrationTest.java` |

### Notes

- **SnakeYAML** : vérifier `pom.xml` — si `snakeyaml` était importé explicitement pour `PatternCache`, le retirer après le refactoring pour alléger les dépendances.
- **patterns.yml conservé** : ne pas supprimer le fichier — il sert de documentation et de fallback pour reconstituer la BDD manuellement si besoin.
- **Thread-safety** : `PatternCache.reload()` est `synchronized` car plusieurs requêtes HTTP peuvent déclencher un reload simultané via le controller.
- **Frontend : ColumnType enum** : le select de type doit être alimenté par `GET /api/settings/patterns` (liste des types distincts déjà en BDD) ou codé en dur à partir de l'enum côté front. Recommandé : endpoint `GET /api/settings/patterns/types` → `List<String>` des `ColumnType.values()`.
