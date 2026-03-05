# Story S5.3: Données Aléatoires Personnalisables — BOOLEAN, INTEGER, DECIMAL, LOREM_IPSUM

**Sprint:** Sprint 5
**Points:** 5
**Epic:** EPIC 5 - Extended Data Types
**Type:** Full-Stack Feature
**Lead:** Amelia (Backend) + Sally (Frontend)
**Status:** Backlog
**Dependencies:** S2.1 DataGeneratorService, S2.6 Data Configuration UI

---

## User Story

**En tant que** développeur ou testeur QA,
**Je veux** pouvoir générer des colonnes avec des types de données primitifs et personnalisables (booléens, entiers, décimaux, texte lorem ipsum),
**Afin de** couvrir tous les cas de test nécessitant des valeurs numériques bornées ou du texte de remplissage réaliste.

---

## Contexte métier

Le PRD section 2.2 définit les **Données aléatoires personnalisables** comme suit :
> Textes lorem ipsum, booléens, nombres entiers/décimaux avec contraintes (min/max), et même des images ou fichiers fictifs si on étend l'app.

Ces types primitifs sont les plus polyvalents et permettent de couvrir des colonnes non identifiées par le système de détection automatique, ainsi que des champs numériques généraux (scores, quantités, identifiants séquentiels, etc.).

---

## ✅ Acceptance Criteria

### AC1 — Nouveaux types dans ColumnType enum
- [ ] `BOOLEAN("random", "Booléen")` ajouté dans `ColumnType.java`
- [ ] `INTEGER("random", "Entier")` ajouté dans `ColumnType.java`
- [ ] `DECIMAL("random", "Décimal")` ajouté dans `ColumnType.java`
- [ ] `LOREM_IPSUM("random", "Lorem Ipsum")` ajouté dans `ColumnType.java`
- [ ] La catégorie `"random"` est reconnue dans `DataTypeDetectionService`

### AC2 — Générateurs backend
- [ ] `BooleanGenerator` créé dans `service/generator/random/`
  - Valeurs : `true` / `false` (distribution 50/50 par défaut)
  - Config optionnelle `additionalConfig.trueRatio` (ex: `0.8` pour 80% true)
  - Format de sortie configurable : `true/false`, `1/0`, `oui/non`, `Yes/No`
  - Défaut : format booléen Java natif (`Boolean`)
- [ ] `IntegerGenerator` créé dans `service/generator/random/`
  - Utilise `columnConfig.getMinValue()` (défaut: 0) et `columnConfig.getMaxValue()` (défaut: 100)
  - Génère : `min + random.nextInt(max - min + 1)`
  - Retourne `Integer`
- [ ] `DecimalGenerator` créé dans `service/generator/random/`
  - Utilise `columnConfig.getMinValue()` (défaut: 0) et `columnConfig.getMaxValue()` (défaut: 100)
  - Nombre de décimales configurable via `additionalConfig.precision` (défaut: 2)
  - Utilise `Locale.US` pour le séparateur décimal (même pattern que `AmountGenerator`)
  - Retourne `BigDecimal`
- [ ] `LoremIpsumGenerator` créé dans `service/generator/random/`
  - Mode `word` : retourne 1 mot lorem ipsum aléatoire
  - Mode `sentence` : retourne 1 phrase (5-15 mots) — défaut
  - Mode `paragraph` : retourne un paragraphe (3-5 phrases)
  - Config via `additionalConfig.mode` : `"word"` | `"sentence"` | `"paragraph"`
  - Corpus statique intégré (pas de dépendance externe)
- [ ] `GeneratorFactory` mis à jour pour les 4 nouveaux `ColumnType`

### AC3 — API sans régression
- [ ] `POST /api/datasets/generate` accepte les nouveaux types
- [ ] `POST /api/batch/generate` accepte les nouveaux types
- [ ] Les tests existants passent toujours (0 régression)

### AC4 — Composant frontend `RandomFieldConfig`
- [ ] Composant React `RandomFieldConfig.jsx` créé dans `DataConfigurationPanel/`
- [ ] Affiché pour les colonnes `BOOLEAN`, `INTEGER`, `DECIMAL`, `LOREM_IPSUM`
- [ ] `INTEGER` : affiche les champs min/max (NumberInput MUI)
- [ ] `DECIMAL` : affiche min/max + sélecteur précision (1, 2, 3, 4 décimales)
- [ ] `BOOLEAN` : affiche toggle ratio true/false + sélecteur format (true/false, 1/0, oui/non)
- [ ] `LOREM_IPSUM` : affiche sélecteur mode (mot / phrase / paragraphe) + aperçu
- [ ] Le composant s'intègre dans `ConfigurationPanel.jsx`

### AC5 — Détection automatique depuis CSV
- [ ] Détecte colonnes nommées `actif`, `active`, `enabled`, `flag` → type `BOOLEAN`
- [ ] Détecte `quantité`, `quantity`, `count`, `nombre`, `score`, `age`, `qty` → type `INTEGER`
- [ ] Détecte `prix`, `price`, `taux`, `rate`, `ratio`, `pct`, `pourcentage` → type `DECIMAL`
- [ ] Détecte `description`, `commentaire`, `notes`, `remarques`, `texte` → type `LOREM_IPSUM`

### AC6 — Tests
- [ ] Tests unitaires pour les 4 générateurs (min 3 tests chacun)
- [ ] `IntegerGenerator` : vérification valeur dans [min, max]
- [ ] `DecimalGenerator` : vérification nombre de décimales correct
- [ ] `BooleanGenerator` : vérification distribution sur 1000 itérations
- [ ] `LoremIpsumGenerator` : vérification mode word/sentence/paragraph (longueur)
- [ ] Test d'intégration : génération complète avec colonnes aléatoires
- [ ] Tests Jest pour `RandomFieldConfig.jsx`
- [ ] Coverage global maintenu > 80%

---

## 🏗️ Spécifications Techniques

### Nouveaux fichiers à créer

```
backend/
  src/main/java/com/movkfact/service/generator/random/
    BooleanGenerator.java
    IntegerGenerator.java
    DecimalGenerator.java
    LoremIpsumGenerator.java

frontend/
  src/components/DataConfigurationPanel/
    RandomFieldConfig.jsx
    RandomFieldConfig.test.js
```

### Modifications requises

```
backend/
  src/main/java/com/movkfact/enums/ColumnType.java
    + BOOLEAN("random", "Booléen")
    + INTEGER("random", "Entier")
    + DECIMAL("random", "Décimal")
    + LOREM_IPSUM("random", "Lorem Ipsum")

  src/main/java/com/movkfact/service/generator/GeneratorFactory.java
    + case BOOLEAN: return new BooleanGenerator(columnConfig);
    + case INTEGER: return new IntegerGenerator(columnConfig);
    + case DECIMAL: return new DecimalGenerator(columnConfig);
    + case LOREM_IPSUM: return new LoremIpsumGenerator(columnConfig);

  src/main/java/com/movkfact/service/DataTypeDetectionService.java
    + Patterns pour les 4 nouveaux types

frontend/
  src/components/DataConfigurationPanel/ConfigurationPanel.jsx
    + Importer RandomFieldConfig
    + Ajouter rendu conditionnel pour BOOLEAN/INTEGER/DECIMAL/LOREM_IPSUM
```

### IntegerGenerator — Implémentation

```java
@Override
public Object generate() {
    int min = columnConfig.getMinValue() != null ? columnConfig.getMinValue() : 0;
    int max = columnConfig.getMaxValue() != null ? columnConfig.getMaxValue() : 100;
    return min + random.nextInt(max - min + 1);
}
```

### LoremIpsumGenerator — Corpus

```java
// Corpus statique — extrait du vrai lorem ipsum + variantes
private static final String[] WORDS = {
    "lorem", "ipsum", "dolor", "sit", "amet", "consectetur",
    "adipiscing", "elit", "sed", "do", "eiusmod", "tempor",
    "incididunt", "ut", "labore", "et", "dolore", "magna", "aliqua"
    // ... 50+ mots
};

// Mode sentence : 5-15 mots concaténés, première lettre majuscule, point final
// Mode paragraph : 3-5 phrases jointes par " "
```

### Note sur `INTEGER` vs `AMOUNT`

`INTEGER` génère des entiers sans format monétaire — pour les quantités, âges, scores.
`AMOUNT` génère des décimaux à 2 chiffres dans une plage financière avec `BigDecimal`.
`DECIMAL` est la version générique d'`AMOUNT` avec précision configurable.

---

## 📊 Estimation

| Composant | Effort | Responsable |
|-----------|--------|-------------|
| 4 générateurs Java | 1j | Amelia |
| Corpus lorem ipsum + logique modes | 0.5j | Amelia |
| Mise à jour ColumnType + Factory + Detection | 0.5j | Amelia |
| Tests backend | 0.5j | Amelia |
| `RandomFieldConfig.jsx` + tests | 1j | Sally |
| Intégration ConfigurationPanel | 0.5j | Sally |
| **Total** | **4j** | **5 pts** |
