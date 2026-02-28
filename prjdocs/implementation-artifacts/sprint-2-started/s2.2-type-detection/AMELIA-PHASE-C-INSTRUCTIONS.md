---
titre: "🎬 AMELIA - PHASE C INSTRUCTIONS"
date: 2026-02-28
heure: "22:42 CET"
phase: "C - Accuracy Measurement Framework"
duration: "2 heures (6 mars)"
statut: "READY"
---

# 🎬 AMELIA - PHASE C INSTRUCTIONS

**Date:** Démarrage 06 mars 2026 (après données Mary le 04/03)  
**Phase:** C - Accuracy Measurement & Validation Framework  
**Durée:** ~2 heures  
**Statut:** ✅ **INSTRUCTIONS PRÊTES**

---

## 🎯 OBJECTIF PHASE C

**Créer un framework pour mesurer la précision (accuracy) de la détection de types**

```
Définition Accuracy:
  accuracy = (correct + 0.5 × alternatives) / total
  
Exemple:
  - Si 80 types détectés correctement: +80
  - Si 10 acceptables (alternatives valides): +5 (0.5 × 10)
  - Si 10 incorrects: +0
  - Total 100 détections
  → accuracy = (80 + 5) / 100 = 85%
  
Cible AC6: accuracy ≥ 85%
```

---

## 📋 TÂCHES PHASE C

### Tâche 1: Créer AccuracyMeasurement.java (30 min)

**Fichier:** `src/main/java/com/movkfact/service/detection/AccuracyMeasurement.java`

**Responsabilité:** Framework pour mesurer et reporter l'accuracy

**Méthodes requises:**

```java
public class AccuracyMeasurement {
    
    /**
     * Mesurer accuracy sur ensemble de test
     * @param csvFile Fichier test avec en-têtes de vérité établie
     * @param detectionResults Résultats de l'outil de détection
     * @return AccuracyReport avec score et détails
     */
    public AccuracyReport measureAccuracy(
        File csvFile,
        TypeDetectionResult detectionResults) { ... }
    
    /**
     * Comparer type détecté vs type attendu
     * @param detected Type détecté par le système
     * @param expected Type attendu (vérité établie)
     * @param alternatives Types acceptables en alternative
     * @return 1.0 (correct), 0.5 (acceptable), 0.0 (incorrect)
     */
    public double compareTypes(
        ColumnType detected,
        ColumnType expected,
        Set<ColumnType> alternatives) { ... }
    
    /**
     * Calculer accuracy global de l'ensemble
     * @param results Liste AccuracyResult pour chaque colonne
     * @return Pourcentage accuracy (0-100)
     */
    public double calculateAccuracy(List<AccuracyResult> results) { ... }
}
```

**Données attendues du CSV de test:**
- En-têtes colonnes: `column_name, expected_type, alternatives`
- Exemple ligne:
  ```
  first_name, FIRST_NAME, LAST_NAME|NAME
  email, EMAIL, 
  amount, AMOUNT, CURRENCY
  ```

**Output:** AccuracyReport avec:
- Score global accuracy
- Breakdown par type (personal, financial, temporal)
- Erreurs détaillées par colonne
- Confidence scores observés

---

### Tâche 2: Créer AccuracyMeasurementTests.java (40 min)

**Fichier:** `src/test/java/com/movkfact/service/detection/AccuracyMeasurementTests.java`

**Cas de test:**

```java
@SpringBootTest
public class AccuracyMeasurementTests {
    
    /**
     * Test 1: Cas simples (Easy)
     * Colonne first_name avec vrais prénoms français
     * Attendu: 95%+ accuracy
     */
    @Test
    public void test_accuracy_easy_dataset() { ... }
    
    /**
     * Test 2: Cas ambigus (Medium)
     * Colonne avec noms/prénoms mixés
     * Même acceptable si FIRST_NAME détecté vs LAST_NAME attendu
     * Attendu: 75%+ accuracy
     */
    @Test
    public void test_accuracy_medium_dataset() { ... }
    
    /**
     * Test 3: Cas difficiles (Hard)
     * Données bruitées, malformées, multilangues
     * Attendu: 60%+ accuracy (acceptable pour données mauvaises)
     */
    @Test
    public void test_accuracy_hard_dataset() { ... }
    
    /**
     * Test 4: Ensemble robustesse (Robustness)
     * CSV très grand (50K lignes)
     * Format inhabituel
     * Attendu: >70% accuracy même en extrême
     */
    @Test
    public void test_accuracy_robustness_dataset() { ... }
}
```

**Structure test:**
1. Charger CSV de test (données Mary attendues 04/03)
2. Appeler CsvTypeDetectionService.detectTypes()
3. Charger TypeDetectionResult
4. Mesurer accuracy vs vérité établie
5. Assurer: accuracy >= seuil attendu
6. Générer rapport détaillé

---

### Tâche 3: Formule Accuracy (Implémentation)

**Formule complète:**

```
Pour chaque colonne:
  
  si detectedType == expectedType:
    score = 1.0 (correct)
  
  sinon si alternatives contient detectedType:
    score = 0.5 (acceptable mais pas idéal)
  
  sinon:
    score = 0.0 (incorrect)

accuracyGlobal = (sum(scores) / nombreColonnes) × 100

Cas AC6:
  Cible: accuracyGlobal ≥ 85%
```

**Exemple calcul:**

```
5 colonnes testées:
  1. first_name:  FIRST_NAME vs FIRST_NAME = 1.0 ✅
  2. email:       EMAIL vs EMAIL = 1.0 ✅
  3. amount:      AMOUNT vs CURRENCY = 0.5 (acceptable)
  4. birth_date:  DATE vs BIRTH_DATE = 0.0 ❌
  5. phone:       PHONE vs PHONE = 1.0 ✅

Total: (1.0 + 1.0 + 0.5 + 0.0 + 1.0) / 5 = 3.5 / 5 = 70%

Résultat: 70% < 85% → Non satisfait (mais acceptable pour données difficiles)
```

---

### Tâche 4: Préparer pour Données Mary (04/03)

**Attendu de Mary (04/03):**
- 80+ fichiers CSV d'exemple
- Organisés en ensembles:
  - Easy (20 fichiers) → Cas simples comme prévu
  - Medium (20 fichiers) → Cas avec ambiguïté acceptable
  - Hard (20 fichiers) → Données bruitées/compliquées
  - Robustness (20 fichiers) → Stress tests

**Ce que tu dois préparer (6 mars):**
1. Framework AccuracyMeasurement prêt
2. Tests template vides attendant données
3. CSV format parser prêt
4. Rapport generator prêt

**Quand Mary livre les données (4 mars PM):**
1. Charger fichiers dans `src/test/resources/accuracy-test-data/`
2. Exécuter AccuracyMeasurementTests
3. Générer AccuracyReport
4. Mesurer: accuracy global vs 85% cible

---

## ✅ CHECKLIST PHASE C

- [ ] **Framework:** AccuracyMeasurement.java créé avec méthodes core
- [ ] **Tests:** AccuracyMeasurementTests.java avec 4 cas de test template
- [ ] **Formule:** Implémentée correctement (comparaison type + alternatives)
- [ ] **Parser CSV:** Peut lire format vérité établie
- [ ] **Reporter:** Génère AccuracyReport avec détails
- [ ] **Prêt pour données:** Peut charger/traiter fichiers Mary
- [ ] **Build:** SUCCESS (nouvelle suite de tests)
- [ ] **Documentation:** Commenté et clair

---

## 🔄 WORKFLOW PHASE C

**6 mars - Amelia:**
1. Crée AccuracyMeasurement.java
2. Crée AccuracyMeasurementTests.java
3. Implémente formule accuracy
4. Prépare parser CSV pour vérité établie
5. TEST BUILD SUCCESS
6. Upload fichiers template tests
7. Prêt pour données Mary

**4-5 mars - Mary:**
1. Livre 80+ fichiers test CSV
2. Organise dans ensembles (Easy/Medium/Hard/Robustness)
3. Fournit format vérité établie

**6-7 mars - Amelia (continued):**
1. Intègre fichiers Mary
2. Exécute AccuracyMeasurementTests
3. Mesure accuracy global vs 85%
4. Génère AccuracyReport.md
5. Documente résultats

---

## 📌 IMPORTANT

**Timing Phase C:**
- 6 mars (AM): Préparer framework (2 heures)
- 4-5 mars: Attendre données Mary
- 6-7 mars (PM): Intégrer données + mesurer accuracy

**Dépendance critique:**
- Phase C bloquée sans données Mary
- Action: Attendre 04/03 livraison

**AC6 Acceptance Criteria:**
- Cible: accuracy ≥ 85% sur 80+ samples
- Mesure: (correct + 0.5×alternatives) / total

---

## 🎯 SORTIE PHASE C

**Livrables:**
1. AccuracyMeasurement.java (framework)
2. AccuracyMeasurementTests.java (4 cas test)
3. AccuracyReport.md (résultats)
4. CSV format documentation
5. Test success rate 100%

**Validation:** AC6 satisfait si accuracy ≥ 85%

---

**Status:** ✅ Instructions prêtes  
**Démarrage Phase C:** 06 mars 2026  
**Entrée Blocker:** Données Mary (04/03)

Amelia, tu as tout ce qu'il faut pour démarrer Phase C le 6 mars! 🚀
