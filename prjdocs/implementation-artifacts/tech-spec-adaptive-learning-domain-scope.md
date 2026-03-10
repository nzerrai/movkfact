---
title: 'Apprentissage adaptatif — Lookup scope-domaine'
slug: 'adaptive-learning-domain-scope'
created: '2026-03-09'
status: 'ready-for-dev'
stepsCompleted: [1, 2, 3, 4]
party_mode_applied: true
party_mode_rounds: 2
tech_stack: ['Spring Boot 3', 'Spring Data JPA', 'React 18', 'MUI']
files_to_modify:
  - 'src/main/java/com/movkfact/service/detection/ColumnLearningService.java'
  - 'src/main/java/com/movkfact/service/detection/ColumnTypeInferenceService.java'
  - 'src/main/java/com/movkfact/controller/TypeDetectionController.java'
  - 'src/test/java/com/movkfact/service/detection/ColumnLearningServiceTest.java'
  - 'src/test/java/com/movkfact/controller/TypeDetectionControllerTest.java'
code_patterns: ['@RequestScope', 'Spring bean injection', 'Optional chaining', 'null-safe guard']
test_patterns: ['@SpringBootTest', '@Transactional', 'assertThat', 'direct service call (no HTTP context)']
---

# Tech-Spec: Apprentissage adaptatif — Lookup scope-domaine

**Created:** 2026-03-09

## Overview

### Problem Statement

S10.1 implante l'apprentissage adaptatif avec un lookup **global uniquement** (`domain_id IS NULL`). Cette V1 signifie que tous les domaines partagent le même corpus : un domaine "Finance" qui apprend `salaire_net → AMOUNT` influence la détection dans un domaine "Logistique" qui n'a aucune colonne similaire. Pire, un domaine ne peut pas surcharger un mapping global incorrect pour son contexte métier spécifique.

### Solution

Introduire un bean `@RequestScope DetectionContext` qui transporte le `domainId` courant de manière transparente à travers la chaîne de détection, sans modifier les signatures des méthodes internes (`infer()`, `detect()`). `ColumnLearningService.lookup()` reçoit un second paramètre `domainId` et applique la stratégie **domain-first → fallback global** :

1. Si un mapping fiable existe pour `(normalized, domainId)` → retourner LEARNED (domaine)
2. Sinon si un mapping fiable existe pour `(normalized, NULL)` → retourner LEARNED (global)
3. Sinon → continuer vers Niveau 1

### Scope

**In Scope:**
- Bean `@RequestScope DetectionContext` (porte `domainId`)
- `ColumnLearningService.lookup(normalized, domainId)` — domain-first + fallback global
- `TypeDetectionController` alimente `DetectionContext` avant d'appeler `CsvTypeDetectionService`
- `ColumnTypeInferenceService.infer()` lit `DetectionContext` pour obtenir `domainId`
- Tests `ColumnLearningServiceTest` : ≥ 3 nouveaux cas (domain priorité sur global, fallback global, isolation domaine)

**Out of Scope:**
- Frontend : aucun changement (badge "Appris" identique)
- Schéma DB : `column_type_feedback` déjà prévu avec `domain_id`
- Endpoint feedback `/api/domains/{domainId}/feedback` : inchangé
- `ColumnFeedbackController` : inchangé (stocke déjà en domain + global)

## Context for Development

### Codebase Patterns

- **@RequestScope** : Spring crée une nouvelle instance du bean par requête HTTP. Le bean est injecté via `@Autowired` dans les services. Convient parfaitement pour transporter `domainId` sans ThreadLocal manuel.
- **Chaîne d'appel actuelle** : `TypeDetectionController` → `CsvTypeDetectionService.detectTypes(file, size)` → pour chaque colonne `ColumnTypeInferenceService.infer(name, values)` → `ColumnLearningService.lookup(normalized)`
- **lookup() V1 — méthode réelle** : signature `public Optional<InferenceResult> lookup(String normalizedName)` — 1 arg. Appelle `feedbackRepository.findGlobal(normalizedName)` qui est `ORDER BY f.count DESC`. Logique de tie check : `candidates.get(1).getCount() >= best.getCount()`.
- **findByNameAndDomain() — déjà `ORDER BY f.count DESC`** : le `@Query` existant inclut `ORDER BY f.count DESC`. Le tri explicite dans `resolveResult` reste défensif mais n'est pas strictement nécessaire pour ce repo.
- **TypeDetectionController — UN SEUL handler** : `@PostMapping("/{domainId}/detect-types")` avec `@PathVariable Long domainId`. T4 est simple — un seul endroit à modifier. `domainId` est toujours présent dans le path.
- **ColumnTypeInferenceService — point d'appel unique** : `learningService.lookup(normalized)` appelé une seule fois dans `infer()`. Confirmé — T3 modifie exactement une ligne.
- **Tests existants — impact de la suppression du 1-arg lookup** : 13 tests existants dans `ColumnLearningServiceTest`. Tests `lookup_*` (tests 6-12) appellent `service.lookup(normalized)` avec 1 arg. Après suppression de la méthode V1, **ces tests doivent être mis à jour** vers `service.lookup(normalized, null)` pour compiler. C'est un changement mécanique, pas un changement de comportement.
- **DetectionContext.java — n'existe pas encore** : le fichier `src/main/java/com/movkfact/context/DetectionContext.java` est à créer (T1 confirmé).
- **Gestion du domainId null** : hors requête HTTP (tests unitaires), `DetectionContext.domainId` sera `null` → `lookup()` ne fait que le global — comportement identique à V1

### Files to Reference

| Fichier | Rôle |
|---------|------|
| `src/main/java/com/movkfact/context/DetectionContext.java` | À CRÉER — bean @RequestScope portant `domainId` |
| `src/main/java/com/movkfact/service/detection/ColumnLearningService.java` | Remplacer `lookup(String)` par `lookup(String, Long)` + extraire `resolveResult()` |
| `src/main/java/com/movkfact/service/detection/ColumnTypeInferenceService.java` | Injecter `DetectionContext` (required=false), 1 ligne dans `infer()` |
| `src/main/java/com/movkfact/controller/TypeDetectionController.java` | Injecter `DetectionContext`, ajouter `setDomainId(domainId)` avant `detectTypes()` — 1 handler uniquement |
| `src/main/java/com/movkfact/repository/ColumnTypeFeedbackRepository.java` | Inchangé — `findByNameAndDomain()` déjà présent avec `ORDER BY count DESC` |
| `src/test/java/com/movkfact/service/detection/ColumnLearningServiceTest.java` | Mettre à jour les 7 tests existants `lookup_*` vers `lookup(normalized, null)` + ajouter 6 nouveaux tests domain-scope |
| `src/test/java/com/movkfact/controller/TypeDetectionControllerTest.java` | À CRÉER — test `@WebMvcTest` vérifiant `setDomainId` avant `detectTypes` (AC5-bis) |

### Technical Decisions

- **@RequestScope plutôt que paramètre** : évite de modifier `infer(name, values)` et `detect(name, values)` qui sont appelés depuis `PiiDetectionService`, ce qui casserait 500+ tests.
- **Null-safety du DetectionContext** : `DetectionContext.domainId` vaut `null` par défaut (champ non initialisé). Deux cas de null à distinguer : (a) bean non injecté (hors contexte HTTP — `detectionContext == null`) → fallback global. (b) bean injecté mais `domainId` non setté → `getDomainId() == null` → fallback global. Les deux cas sont couverts par la même garde `Long currentDomainId = (detectionContext != null) ? detectionContext.getDomainId() : null;`.
- **`lookup(String)` à 1 argument SUPPRIMÉE** : la méthode V1 `lookup(String normalizedName)` est **supprimée** (pas dépréciée). Tous les appels existants sont dans `ColumnTypeInferenceService` — un seul endroit, mis à jour en T3. Laisser la vieille signature créerait un appel orphelin silencieux qui bypass le scope-domaine.
- **Stratégie domain-first** : si count domaine ≥ min-count → LEARNED (domaine). Sinon tenter global. Cette priorité est stricte — un mapping domaine invalide surcharge le global ; acceptable car le feedback est explicitement fourni par l'utilisateur de ce domaine.
- **Ambiguïté per-domain** : même règle qu'en global (tie → pas de promotion), appliquée indépendamment au niveau domaine puis au niveau global.
- **Pollution du global par feedback domaine** : `recordFeedback(feedbacks, domainId)` stocke à la fois en global ET en domain-spécifique (S10.1 inchangé). Conséquence : si on enregistre EMAIL et PHONE pour le même nom via deux domaines différents, le global atteint un tie → ambiguïté globale → pas de promotion globale. Comportement attendu et correct.
- **Confiance** : formule identique `min(95, 80 + 5 * count)` — le count domain peut être inférieur au count global mais ça n'affecte pas la formule.
- **`resolveResult` autonome** : la méthode trie elle-même les candidats par `count DESC` via `Comparator`. Ne pas se fier à l'ordre retourné par les méthodes repository (`findByNameAndDomain`, `findGlobal`) — cet ordre n'est pas contractuellement garanti.
- **`@RequestScope` dans les tests du controller** : `TypeDetectionController` injecte `DetectionContext` avec `@Autowired` standard (non optional). Les tests du controller doivent utiliser `@WebMvcTest` (contexte web simulé) ou déclarer `DetectionContext` comme `@MockBean`. Les tests `@SpringBootTest` sans MockMvc déclencheraient une erreur de wiring.

## Implementation Plan

### Tasks

**Ordre de dépendance (bas niveau en premier) :**

#### T1 — Créer `DetectionContext` bean @RequestScope
**Fichier :** `src/main/java/com/movkfact/context/DetectionContext.java` ← CRÉER

```java
package com.movkfact.context;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class DetectionContext {
    private Long domainId;
    public Long getDomainId() { return domainId; }
    public void setDomainId(Long domainId) { this.domainId = domainId; }
}
```

#### T2 — Modifier `ColumnLearningService.lookup()` — domain-first + fallback global
**Fichier :** `src/main/java/com/movkfact/service/detection/ColumnLearningService.java` ← MODIFIER

Changer la signature :
```java
// AVANT
public Optional<InferenceResult> lookup(String normalizedName)

// APRÈS
public Optional<InferenceResult> lookup(String normalizedName, Long domainId)
```

Logique interne :
```java
public Optional<InferenceResult> lookup(String normalizedName, Long domainId) {
    if (normalizedName == null || normalizedName.isBlank()) return Optional.empty();

    // 1. Lookup domaine-spécifique si domainId fourni
    if (domainId != null) {
        List<ColumnTypeFeedback> domainCandidates = feedbackRepository.findByNameAndDomain(normalizedName, domainId);
        Optional<InferenceResult> domainResult = resolveResult(normalizedName, domainCandidates);
        if (domainResult.isPresent()) return domainResult;
    }

    // 2. Fallback global (domain_id IS NULL)
    List<ColumnTypeFeedback> globalCandidates = feedbackRepository.findGlobal(normalizedName);
    return resolveResult(normalizedName, globalCandidates);
}
```

Extraire la logique de résolution dans une méthode privée `resolveResult(name, candidates)`.

**Imports à ajouter en tête de `ColumnLearningService.java` :**
```java
import java.util.Comparator;
import java.util.stream.Collectors;
```

```java
private Optional<InferenceResult> resolveResult(String normalizedName, List<ColumnTypeFeedback> candidates) {
    if (candidates.isEmpty()) return Optional.empty();
    // Trier explicitement par count DESC — ne pas dépendre de l'ordre de la requête repo
    List<ColumnTypeFeedback> sorted = candidates.stream()
            .sorted(Comparator.comparingInt(ColumnTypeFeedback::getCount).reversed())
            .collect(Collectors.toList());
    ColumnTypeFeedback best = sorted.get(0);
    if (best.getCount() < minCount) return Optional.empty();
    // Tie check : deuxième candidat aussi fort → ambiguïté → pas de promotion
    if (sorted.size() > 1 && sorted.get(1).getCount() >= best.getCount()) return Optional.empty();
    ColumnType type;
    try { type = ColumnType.valueOf(best.getValidatedType()); }
    catch (IllegalArgumentException e) { return Optional.empty(); }
    double confidence = Math.min(95.0, 80.0 + 5.0 * best.getCount());
    return Optional.of(new InferenceResult(type, confidence, InferenceLevel.LEARNED, best.getCount()));
}
```

**⚠️ R2-1** : `resolveResult` trie lui-même les candidats par `count DESC`. Ne pas supposer que `findByNameAndDomain` ou `findGlobal` retournent des résultats dans un ordre particulier. La méthode est autonome.

**⚠️ SUPPRESSION OBLIGATOIRE :** La méthode `lookup(String normalizedName)` à 1 argument doit être **supprimée** du fichier, pas dépréciée. Son unique appelant est `ColumnTypeInferenceService` (mis à jour en T3). Laisser la vieille signature provoquerait une détection silencieuse sans scope-domaine.

#### T3 — Injecter `DetectionContext` dans `ColumnTypeInferenceService`
**Fichier :** `src/main/java/com/movkfact/service/detection/ColumnTypeInferenceService.java` ← MODIFIER

```java
@Autowired(required = false)  // required=false : null hors contexte HTTP (tests)
private DetectionContext detectionContext;
```

Dans `infer()`, Niveau 0 :
```java
// AVANT
String normalized = learningService.normalize(columnName);
Optional<InferenceResult> learned = learningService.lookup(normalized);

// APRÈS
String normalized = learningService.normalize(columnName);
Long currentDomainId = (detectionContext != null) ? detectionContext.getDomainId() : null;
Optional<InferenceResult> learned = learningService.lookup(normalized, currentDomainId);
```

#### T4 — Alimenter `DetectionContext` dans `TypeDetectionController`
**Fichier :** `src/main/java/com/movkfact/controller/TypeDetectionController.java` ← MODIFIER

Injecter `DetectionContext` dans le controller :
```java
@Autowired
private DetectionContext detectionContext;
```

Le controller a **un seul handler** (`@PostMapping("/{domainId}/detect-types")`). Setter `domainId` **avant** l'appel à `detectTypes()` (ordre impératif) :
```java
detectionContext.setDomainId(domainId);                          // ← en premier
TypeDetectionResult result = detectionService.detectTypes(csvFile, sampleSize);  // ← en second
```

**Action :** Lire le fichier `TypeDetectionController.java` en entier avant de modifier pour confirmer qu'il n'y a qu'un seul point d'appel à `detectTypes()`. Si un handler futur est ajouté sans `domainId` dans son path, passer `null` explicitement (`detectionContext.setDomainId(null)`).

**⚠️ R2-3 — Tests du controller :** `DetectionContext` est un bean `@RequestScope`. Les tests qui chargent `TypeDetectionController` dans un contexte Spring **sans** contexte HTTP (ex. `@SpringBootTest` sans MockMvc) planteront au wiring. Les tests du controller **doivent** utiliser `@WebMvcTest` (qui fournit un contexte web simulé) ou mocker `DetectionContext` avec `@MockBean`.

#### T5 — Tests `ColumnLearningServiceTest` — ≥ 5 cas domain-scope
**Fichier :** `src/test/java/com/movkfact/service/detection/ColumnLearningServiceTest.java` ← MODIFIER

**⚠️ AVANT D'AJOUTER LES NOUVEAUX TESTS — Mise à jour obligatoire des tests existants :**

La suppression de `lookup(String)` à 1 arg (T2) casse la compilation des tests existants. Les tests suivants appellent `service.lookup(normalized)` directement et doivent être mis à jour **mécaniquement** (`lookup(normalized)` → `lookup(normalized, null)`) :
- `lookup_unknownName_returnsEmpty`
- `lookup_belowMinCount_returnsEmpty`
- `lookup_atMinCount_returnsLearned`
- `lookup_aboveMinCount_higherConfidence`
- `lookup_ambiguousTie_returnsEmpty`
- `recordFeedback_emptyValidatedType_ignored` ← **⚠️ F5 : ne pas oublier ce test**, il appelle aussi `service.lookup()` à 1 arg

Note : `fullCycle_feedbackThenInfer_returnsLearned` appelle `inferenceService.infer()`, pas `service.lookup()` directement — il n'a pas besoin de mise à jour de signature mais recompilera une fois T3 appliqué.

Ces modifications ne changent pas le comportement — `null` domainId = comportement V1 global uniquement.

**⚠️ R2-4 — Normalisation :** `service.normalize(rawName)` convertit `_` → `-` et passe en minuscules (ex : `"ref_country"` → `"ref-country"`). Les tests ci-dessous appellent `lookup()` avec le nom déjà normalisé. Pour éviter toute dérive, préférer `service.normalize(raw)` pour construire la clé de lookup dans les tests.

Ajouter après les tests existants :

```java
// ── Domain-scope : priorité domaine sur global ───────────────────────────

@Test
void lookup_domainMappingPrioritizedOverGlobal() {
    // AC1 canonical : le domaine doit primer sur un global qui a un vainqueur clair (pas un tie)
    // Setup : global EMAIL fort (5 fois via null), domaine 99 PHONE (3 fois)
    // recordFeedback(fb, 99L) écrit aussi en global → global final : EMAIL(5) + PHONE(3) → EMAIL gagne
    // Sans domain-first, lookup(99L) retournerait EMAIL (global gagne).
    // Avec domain-first, lookup(99L) doit retourner PHONE (domaine-specific).
    List<ColumnFeedbackRequest> globalFb = List.of(
            new ColumnFeedbackRequest("col_ac1", null, "EMAIL")
    );
    service.recordFeedback(globalFb, null);
    service.recordFeedback(globalFb, null);
    service.recordFeedback(globalFb, null);
    service.recordFeedback(globalFb, null);
    service.recordFeedback(globalFb, null); // EMAIL count=5 en global

    List<ColumnFeedbackRequest> domainFb = List.of(
            new ColumnFeedbackRequest("col_ac1", null, "PHONE")
    );
    service.recordFeedback(domainFb, 99L); // global EMAIL(5)+PHONE(1)
    service.recordFeedback(domainFb, 99L); // global EMAIL(5)+PHONE(2)
    service.recordFeedback(domainFb, 99L); // global EMAIL(5)+PHONE(3) → EMAIL gagne clairement

    // Global a un vainqueur clair : EMAIL(5) > PHONE(3)
    Optional<InferenceResult> globalResult = service.lookup(service.normalize("col_ac1"), null);
    assertThat(globalResult).isPresent();
    assertThat(globalResult.get().getType()).isEqualTo(ColumnType.EMAIL); // global gagne = EMAIL

    // Domaine 99 → PHONE (domain-first override le vainqueur global)
    Optional<InferenceResult> domainResult = service.lookup(service.normalize("col_ac1"), 99L);
    assertThat(domainResult).isPresent();
    assertThat(domainResult.get().getType()).isEqualTo(ColumnType.PHONE); // domain-first = PHONE
}

@Test
void lookup_domainFeedbackPollutesGlobal_createsAmbiguity() {
    // Même setup que ci-dessus : global a EMAIL(3) + PHONE(3) = tie
    List<ColumnFeedbackRequest> globalFb = List.of(
            new ColumnFeedbackRequest("col_tie", null, "EMAIL")
    );
    service.recordFeedback(globalFb, null);
    service.recordFeedback(globalFb, null);
    service.recordFeedback(globalFb, null);
    List<ColumnFeedbackRequest> domainFb = List.of(
            new ColumnFeedbackRequest("col_tie", null, "PHONE")
    );
    service.recordFeedback(domainFb, 99L);
    service.recordFeedback(domainFb, 99L);
    service.recordFeedback(domainFb, 99L);

    // Lookup global (domainId=null) → tie → ambiguïté → pas de promotion
    Optional<InferenceResult> globalResult = service.lookup("col-tie", null);
    assertThat(globalResult).isEmpty(); // tie global non résolu

    // Lookup domaine 99 → PHONE (domain-first, pas de tie au niveau domaine)
    Optional<InferenceResult> domainResult = service.lookup("col-tie", 99L);
    assertThat(domainResult).isPresent();
    assertThat(domainResult.get().getType()).isEqualTo(ColumnType.PHONE);
}

@Test
void lookup_domainAbsent_fallsBackToGlobal() {
    // Global : zipcode → ZIP_CODE (3 fois)
    List<ColumnFeedbackRequest> globalFb = List.of(
            new ColumnFeedbackRequest("zipcode", null, "ZIP_CODE")
    );
    service.recordFeedback(globalFb, null);
    service.recordFeedback(globalFb, null);
    service.recordFeedback(globalFb, null);

    // Lookup pour domaine 42 (aucun mapping domaine) → fallback global
    Optional<InferenceResult> result = service.lookup("zipcode", 42L);
    assertThat(result).isPresent();
    assertThat(result.get().getType()).isEqualTo(ColumnType.ZIP_CODE);
    assertThat(result.get().getLevel()).isEqualTo(InferenceLevel.LEARNED);
}

@Test
void lookup_nullDomainId_usesGlobalOnly() {
    // Comportement V1 inchangé : domainId=null → global seulement
    List<ColumnFeedbackRequest> feedbacks = List.of(
            new ColumnFeedbackRequest("city_name", null, "CITY")
    );
    service.recordFeedback(feedbacks, null);
    service.recordFeedback(feedbacks, null);
    service.recordFeedback(feedbacks, null);

    Optional<InferenceResult> result = service.lookup("city-name", null);
    assertThat(result).isPresent();
    assertThat(result.get().getType()).isEqualTo(ColumnType.CITY);
}

@Test
void lookup_noHttpContext_worksAsGlobalOnly() {
    // AC7 — hors contexte HTTP (appel direct sans DetectionContext injecté)
    // ColumnLearningService est injecté directement → DetectionContext = null dans ColumnTypeInferenceService
    // Ce test valide que lookup(normalized, null) est non-régressif
    List<ColumnFeedbackRequest> feedbacks = List.of(
            new ColumnFeedbackRequest("ref_country", null, "COUNTRY")
    );
    service.recordFeedback(feedbacks, null);
    service.recordFeedback(feedbacks, null);
    service.recordFeedback(feedbacks, null);

    // Appel direct avec null = comportement V1 = pas de régression batch/scheduled
    String normalized = service.normalize("ref_country"); // → "ref-country"
    Optional<InferenceResult> result = service.lookup(normalized, null);
    assertThat(result).isPresent();
    assertThat(result.get().getType()).isEqualTo(ColumnType.COUNTRY);
    assertThat(result.get().getLevel()).isEqualTo(InferenceLevel.LEARNED);
}

@Test
void lookup_domainIsolation_noLeakBetweenDomains() {
    // AC3 — R2-5 : les mappings domain-spécifiques de domaine 10 ne sont pas visibles par domaine 20
    // Setup : deux domaines entraînent des types opposés pour "iso_col"
    String raw = "iso_col";

    List<ColumnFeedbackRequest> domain10Fb = List.of(
            new ColumnFeedbackRequest(raw, null, "PHONE")
    );
    service.recordFeedback(domain10Fb, 10L);
    service.recordFeedback(domain10Fb, 10L);
    service.recordFeedback(domain10Fb, 10L);

    List<ColumnFeedbackRequest> domain20Fb = List.of(
            new ColumnFeedbackRequest(raw, null, "AMOUNT")
    );
    service.recordFeedback(domain20Fb, 20L);
    service.recordFeedback(domain20Fb, 20L);
    service.recordFeedback(domain20Fb, 20L);

    String normalized = service.normalize(raw); // → "iso-col"

    // Domaine 10 → PHONE (son mapping domain-specific prime)
    Optional<InferenceResult> d10 = service.lookup(normalized, 10L);
    assertThat(d10).isPresent();
    assertThat(d10.get().getType()).isEqualTo(ColumnType.PHONE);

    // Domaine 20 → AMOUNT (son mapping domain-specific, pas celui de domaine 10)
    Optional<InferenceResult> d20 = service.lookup(normalized, 20L);
    assertThat(d20).isPresent();
    assertThat(d20.get().getType()).isEqualTo(ColumnType.AMOUNT);

    // Note : global a PHONE(3)+AMOUNT(3) = tie → lookup(normalized, null) = empty (ambiguïté)
    assertThat(service.lookup(normalized, null)).isEmpty();
}
```

#### T6 — Test intégration Controller → DetectionContext (AC5-bis)
**Fichier :** `src/test/java/com/movkfact/controller/TypeDetectionControllerTest.java` ← CRÉER

```java
@WebMvcTest(TypeDetectionController.class)
class TypeDetectionControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean CsvTypeDetectionService detectionService;
    @MockBean DetectionContext detectionContext;  // @RequestScope → @MockBean pour @WebMvcTest

    @Test
    void detectTypes_feedsDomainIdToContext() throws Exception {
        // Setup : mock retourne un résultat vide
        when(detectionService.detectTypes(any(), anyInt()))
                .thenReturn(new TypeDetectionResult(List.of()));

        MockMultipartFile csv = new MockMultipartFile(
                "file", "test.csv", "text/csv", "col1,col2\nval1,val2".getBytes());

        mockMvc.perform(multipart("/api/domains/7/detect-types").file(csv))
                .andExpect(status().isOk());

        // Vérifier que setDomainId(7) a bien été appelé avant detectTypes
        verify(detectionContext).setDomainId(7L);
        verify(detectionService).detectTypes(any(), anyInt());
        InOrder order = inOrder(detectionContext, detectionService);
        order.verify(detectionContext).setDomainId(7L);
        order.verify(detectionService).detectTypes(any(), anyInt());
    }
}
```

### Acceptance Criteria

**AC1 — Domain-first lookup**
- Given un mapping `(email-col, PHONE)` avec count=3 pour domaine 99
- And un mapping `(email-col, EMAIL)` avec count=3 global
- When `lookup("email-col", 99L)` est appelé
- Then retourne `PHONE` avec `InferenceLevel.LEARNED`

**AC2 — Fallback global quand domaine absent**
- Given aucun mapping domaine-spécifique pour `(zipcode, domainId=42)`
- And un mapping global `(zipcode, ZIP_CODE)` avec count=3
- When `lookup("zipcode", 42L)` est appelé
- Then retourne `ZIP_CODE` via le corpus global

**AC3 — Isolation domaine**
- Given domaine 99 a mappé `col_x → PHONE` (count≥3)
- And domaine 88 n'a aucun mapping pour `col_x`
- When `lookup("col-x", 88L)` est appelé
- Then retourne le corpus global (ou `Optional.empty()` si absent du global aussi)

**AC4 — Comportement V1 inchangé (null domainId)**
- Given aucun domainId fourni (null)
- When `lookup(normalized, null)` est appelé
- Then comportement identique à S10.1 (global uniquement)

**AC5 — Controller alimente le contexte**
- Given une requête `POST /api/domains/7/detect-types`
- When la détection est lancée
- Then `DetectionContext.domainId = 7` est disponible dans `ColumnTypeInferenceService`

**AC6 — Tests ≥ 6 cas domain-scope**
- `lookup_domainMappingPrioritizedOverGlobal` — domaine prime sur global
- `lookup_domainFeedbackPollutesGlobal_createsAmbiguity` — tie global après feedback domain, domain-specific non ambigu
- `lookup_domainAbsent_fallsBackToGlobal` — fallback global quand domaine vide
- `lookup_nullDomainId_usesGlobalOnly` — null domainId = comportement V1
- `lookup_noHttpContext_worksAsGlobalOnly` — appel direct sans contexte HTTP (batch/scheduled)
- `lookup_domainIsolation_noLeakBetweenDomains` — mappings domaine 10 non visibles par domaine 20 (AC3)

**AC5-bis — Intégration Controller → DetectionContext (R2-2)**
- Test `@WebMvcTest(TypeDetectionController.class)` avec MockMvc
- Given `POST /api/domains/7/detect-types` appelé avec un CSV valide
- When la requête est traitée
- Then `detectionService.detectTypes()` est appelé après `detectionContext.setDomainId(7L)`
- Implémentation : mocker `CsvTypeDetectionService` avec `@MockBean`, capturer la valeur de `detectionContext.getDomainId()` dans le mock

**AC7 — Non-régression hors contexte HTTP**
- Given `ColumnLearningService.lookup()` appelé directement (sans requête HTTP active)
- And `DetectionContext` non disponible dans le contexte Spring
- When `lookup(normalized, null)` est invoqué
- Then le comportement est identique à S10.1 (corpus global uniquement, pas d'exception)

## Additional Context

### Dependencies

- **S10.1** (done) : structure DB, `ColumnLearningService`, `ColumnTypeFeedbackRepository.findByNameAndDomain()` déjà créé
- Aucune nouvelle migration Flyway nécessaire

### Testing Strategy

- Tests `@SpringBootTest @Transactional` sur `ColumnLearningService` directement
- `DetectionContext` mock/null dans les tests unitaires existants (inchangés car `@Autowired(required = false)`)
- Vérifier que les 516 tests existants passent toujours après refactoring de `lookup()`

### Notes

- `recordFeedback(feedbacks, null)` stocke uniquement en global (domainId=null) — ce comportement est inchangé
- `recordFeedback(feedbacks, 99L)` stocke à la fois en global ET pour domaine 99 — comportement S10.1 inchangé
- Le fait de stocker aussi en global lors d'un feedback domaine-spécifique garantit que le corpus global s'enrichit progressivement pour tous les domaines
- `@Autowired(required = false)` sur `DetectionContext` : Spring injecte null si hors contexte HTTP (tests, batch) → `ColumnTypeInferenceService` gère null avec `detectionContext != null`
