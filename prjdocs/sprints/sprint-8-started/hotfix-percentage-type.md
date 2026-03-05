# Ajout du type PERCENTAGE — Hotfix Sprint 8

**Date :** 05 mars 2026
**Sprint :** Sprint 8 (en cours)
**Epic de rattachement :** Epic 5 — Extended Data Types
**Story de rattachement :** 5-3 — Données Aléatoires Personnalisables (backlog)
**Type :** Ajout rapide (hors story formelle)
**Demandeur :** Nouredine

---

## Contexte

Ajout de la typologie **Pourcentage** (`PERCENTAGE`) au moteur de génération de données, en complément des types numériques existants (`INTEGER`, `DECIMAL`, `BOOLEAN`).

Cette demande est directement liée à l'Epic 5 (story 5-3) qui était en backlog depuis le Sprint 5 skippé.

---

## Changements effectués

### 1. `ColumnType.java`
**Fichier :** `src/main/java/com/movkfact/enums/ColumnType.java`

Ajout de l'entrée dans la section `Numeric Data` :
```java
PERCENTAGE("numeric", "Pourcentage"),
```

### 2. `PercentageGenerator.java` *(nouveau fichier)*
**Fichier :** `src/main/java/com/movkfact/service/generator/numeric/PercentageGenerator.java`

Générateur de pourcentages aléatoires :
- Plage par défaut : **[0.0, 100.0]**
- Précision : **2 décimales** (arrondi `HALF_UP`)
- Contraintes configurables via `min` / `max` dans `ColumnConfigDTO.constraints`

### 3. `GeneratorFactory.java`
**Fichier :** `src/main/java/com/movkfact/service/generator/GeneratorFactory.java`

Import et case ajoutés :
```java
import com.movkfact.service.generator.numeric.PercentageGenerator;
// ...
case PERCENTAGE: return new PercentageGenerator(columnConfig);
```

---

## Comportement du générateur

| Contrainte | Valeur par défaut | Configurable |
|------------|------------------|--------------|
| `min`      | `0.0`            | ✅ oui        |
| `max`      | `100.0`          | ✅ oui        |
| Précision  | 2 décimales      | ❌ non        |

**Exemple de sortie :** `42.87`, `0.15`, `99.01`

---

## Statut

- [x] `ColumnType` mis à jour
- [x] `PercentageGenerator` créé
- [x] `GeneratorFactory` enregistré
- [ ] Tests unitaires (à ajouter si besoin)
- [ ] Intégration UI frontend (sélecteur de type — config constraints min/max)
