---
title: "Refinement Session S2.2 - CSV Column Type Detection"
date: 2026-03-03
sprint: 2
storyId: 2-2
participants: ["Amelia", "Mary", "Winston"]
duration: "2-3 hours"
status: "READY FOR EXECUTION"
---

# Refinement Session: S2.2 Implement CSV Column Type Detection

**Sprint:** 2  
**Story ID:** S2.2  
**Points:** 8 pts  
**Type:** Backend Feature (Type Detection Engine)  
**Critical Path:**  YES - S2.5 CSV Upload blocked on S2.2 API completion

---

## 🎯 Refinement Objectives

1. **Clarify Type Detection Algorithm** - Move from "fuzzy" to concrete implementation
2. **Finalize Feature Scope** - Decide MVP vs. Phase 2 features
3. **Confirm Technical Approach** - Leverage S2.1 patterns (Strategy + Factory)
4. **Test Strategy** - How to verify >80% accuracy?
5. **Risk Assessment** - Encoding edge cases, performance targets
6. **Hard AC Signature** - Get all stakeholders aligned on testability

---

## 📋 PRE-REFINEMENT (Before Session)

### Amelia Preparation (Developer):
- [ ] Review S2.1 `GeneratorFactory` pattern in code
- [ ] Study S2.1 `ColumnType` enum (13 types available)
- [ ] Read story S2.2 AC (all 11 criteria)
- [ ] List 3-5 algorithm questions for Winston
- [ ] Estimate: "How many detectors needed?" (Personal, Financial, Temporal, + Generic?)

### Mary Preparation (Analyst):
- [ ] Prepare 5-10 CSV sample files (diverse headers):
  - Standard: `first_name`, `email`, `phone`
  - French: `prénom`, `courriel`, `téléphone`
  - Camel case: `firstName`, `emailAddress`
  - Edge cases: `person_first_nm_name` (ambiguous)
- [ ] Document: Which columns should match which types
- [ ] Identify: Columns that SHOULD NOT match (false positives)
- [ ] Prepare confusion matrix: "If we see 'name' in header, how confident are we?"

### Winston Preparation (Architect):
- [ ] Sketch: "How should detection flow work?" (sequence diagram)
- [ ] Review: S2.1 Strategy pattern, decide if reusable for S2.2
- [ ] Performance baseline: "What does <500ms target mean?" (includes I/O?)
- [ ] Decision point: "Rule-based vs. ML-based detection?"
- [ ] API contract: What exact format from S2.2 API to S2.5 UI?

---

## 🔍 SESSION AGENDA (2-3 hours)

### **SEGMENT 1: Algorithm Deep Dive (45 min)**

**Topic 1.1: Pattern Matching Strategy (15 min)**

**Question:** How does type detection work?

**Current Approach (from story):**
```
For each column header + sample values:
1. Match header against known patterns
   - "email" → EMAIL type (confidence 95%)
   - "first_name|firstName|first|firstname" → FIRST_NAME (confidence 90%)
   - "amount|montant|price" → AMOUNT (confidence 85%)

2. If header match uncertain, analyze VALUES
   - Sample value: "john@example.com" → EMAIL (confidence 99%)
   - Sample value: "2023-01-15" → DATE_BIRTH or DATE_CREATED (confidence 80%)
   - Sample value: "1500.50" → AMOUNT (confidence 75%)

3. Combine scores: header_confidence + value_confidence
   - If combined > 80% → CONFIRMED
   - If 50-80% → UNCERTAIN (show alternatives)
   - If < 50% → UNKNOWN (user must choose)
```

**Decision Needed:**
- [ ] OK with pattern-matching approach? YES / NO / MODIFY
- [ ] Alternative: Use regex more aggressively? YES / NO
- [ ] Alternative: Pre-train ML model? (Defer to Phase 2?)

**Acceptance:** ✍️ All agree on algorithm foundation

---

**Topic 1.2: Detector Architecture (15 min)**

**Proposal (based on S2.1 patterns):**

```java
// Abstract detector
public abstract class ColumnTypeDetector {
  protected List<String> headerPatterns;    // ["email", "mail", "e-mail"]
  protected List<String> valuePatterns;     // regex patterns
  protected int minimumConfidence = 80;
  
  public DetectionResult detect(String columnHeader, List<String> sampleValues) {
    int headerScore = matchHeaderPatterns(columnHeader);
    int valueScore = matchValuePatterns(sampleValues);
    int confidence = (headerScore + valueScore) / 2;
    
    return new DetectionResult(
      this.getColumnType(),      // EMAIL, FIRST_NAME, etc.
      confidence,
      headerScore, valueScore,
      alternativeSuggestions()   // [FIRST_NAME, FULL_NAME] if EMAIL not sure
    );
  }
  
  protected abstract ColumnType getColumnType();
  protected abstract int matchHeaderPatterns(String header);
  protected abstract int matchValuePatterns(List<String> values);
}

// Concrete detectors
public class EmailTypeDetector extends ColumnTypeDetector { ... }
public class AmountTypeDetector extends ColumnTypeDetector { ... }
public class DateBirthTypeDetector extends ColumnTypeDetector { ... }
// ... etc (one per type)
```

**Parallel to S2.1:**
- S2.1: GeneratorFactory instantiates `DataTypeGenerator` based on ColumnType
- S2.2: DetectorFactory instantiates `ColumnTypeDetector` to identify ColumnType

**Decision Needed:**
- [ ] Pattern looks right? YES / NO / FEEDBACK: ______
- [ ] How many detector classes? (13 for each type, or 3 for 3 categories?)
  - Option A: 1 detector per ColumnType (13 classes, granular)
  - Option B: 3 category detectors (Personal, Financial, Temporal, simpler)
  - **Recommend:** Option A (cleaner architecture, easier to test)
- [ ] Fallback detector for unknown types? YES / NO

**Acceptance:** ✍️ Detector architecture confirmed

---

**Topic 1.3: Accuracy Definition (15 min)**

**Current AC states:** ">80% match for detection"

**Question:** What does this mean operationally?

**Scenario 1:** CSV has column "sales" (ambiguous)
- Training set shows "sales" is AMOUNT 70% of the time, but could be DESCRIPTION
- Should we flag as UNCERTAIN or CONFIDENT?
- Decision: UNCERTAIN (require user confirmation if <80%)

**Scenario 2:** CSV has "date" column with values ["2023-01-15", "2023-02-20"]
- Could be DATE_BIRTH, DATE_CREATED, or other DATE type
- Propose: [DATE_CREATED (90%), DATE_BIRTH (50%)]
- Decision: Show top suggestions

**Test Data Requirements:**
- Mary prepares: 100-1000 CSV samples with known type mappings
- We measure: (Correct Predictions) / (Total Predictions) = Accuracy %
- Target: >85% accuracy on test set

**Decision Needed:**
- [ ] Accuracy = (TP + TN) / (TP + TN + FP + FN)? YES
- [ ] Phase 1: Manual test set (1000 samples)? YES
- [ ] Phase 2: Active learning from user feedback? (Defer)
- [ ] What's acceptable FP rate? (False positives = user frustration)
  - False positive: Suggest EMAIL when it's really FULL_NAME
  - Accept: <5% FP rate (1 in 20 suggestions wrong)

**Acceptance:** ✍️ Accuracy metrics defined

---

### **SEGMENT 2: Edge Cases & Robustness (45 min)**

**Topic 2.1: Encoding Handling (15 min)**

**Challenge:** CSV files come in multiple encodings

**Test Cases to Support:**
```
UTF-8:        "François", "Müller", "Łukasz"
ISO-8859-1:   "Francois", "Muller" 
Cp1252:       French accents, Windows legacy
BOM markers:  UTF-8 with BOM
Mixed:        (should fail gracefully)
```

**Question:** How do we detect encoding?

**Approach 1: Auto-detect**
- Use library: `juniversalchardet` or `CharsetDetector`
- Read file sample (first 1KB), detect encoding
- Risk: ~95% accuracy (not perfect)

**Approach 2: User-specified**
- UI accepts encoding selection
- Simpler, more reliable
- Extra UX step

**Approach 3: Hybrid (Recommended)**
- Default: Auto-detect with warning
- Allow override in UI if wrong

**Decision Needed:**
- [ ] Use auto-detect library? YES (juniversalchardet recommended)
- [ ] Fallback to UTF-8 if detection fails? YES
- [ ] Allow user override in S2.5 UI? YES
- [ ] Test with Mary's multi-encoding samples? YES

**Acceptance:** ✍️ Encoding strategy agreed

---

**Topic 2.2: Missing/Null/Malformed Data (15 min)**

**Scenario 1: Missing values**
```
Column: "email"
Values: ["john@example.com", null, "", "jane@example.com", null]
Question: Should we still detect as EMAIL?
Answer: YES - 75% non-empty matches EMAIL pattern = 75% confidence
```

**Scenario 2: Completely empty column**
```
Column: "unknown"
Values: [null, null, null, null, null]
Question: What type?
Answer: UNKNOWN (no pattern match, no values to analyze) → User must choose
```

**Scenario 3: Malformed values**
```
Column: "amount"
Values: ["1500.50", "abc", "2000", "error", "$3000"]
Question: Still detect as AMOUNT?
Answer: Partial - 60% match AMOUNT pattern = confidence 60% → UNCERTAIN
```

**Strategy:**
- Never crash on bad data
- Count valid values that match type pattern
- confidence = validMatches / totalValues
- If confidence < 50%, treat as UNKNOWN

**Decision Needed:**
- [ ] Skip null values in pattern matching? YES
- [ ] Require minimum % valid values? (>50%?) YES
- [ ] Special handling for currency symbols? (e.g., $3000 → AMOUNT)
  - YES - regex should match "$\d+\.\d+"
- [ ] Error-safe: Only log, don't throw exception? YES

**Acceptance:** ✍️ Robustness strategy confirmed

---

**Topic 2.3: Header Ambiguity (15 min)**

**Problem:** Some headers are ambiguous

```
"name" → Could be FIRST_NAME, LAST_NAME, or FULL_NAME
"date" → Could be DATE_BIRTH, DATE_CREATED, DATE_EVENT
"id" → Could be ID, ACCOUNT_NUMBER, or just a row number
```

**Solution: Propose Alternatives**

```java
DetectionResult {
  primaryType: FIRST_NAME (confidence 70%)
  alternatives: [
    { type: FULL_NAME, confidence 60% },
    { type: LAST_NAME, confidence 40% }
  ]
}
```

**UI Implication (for S2.5):**
- Show primary suggestion
- Allow user to click on dropdown for alternatives
- Or show "3 possible types - which one?"

**Decision Needed:**
- [ ] Return top 3 alternatives always? YES
- [ ] Min confidence for showing alternative? 20%? YES
- [ ] Allow user to mark "Not a type column" (UNKNOWN/SKIP)? YES

**Acceptance:** ✍️ Alternative proposal strategy agreed

---

### **SEGMENT 3: Performance & Scalability (30 min)**

**Topic 3.1: Target Performance (10 min)**

**Current AC:** Endpoint <500ms

**Question:** For what data size?

**Proposed:**
- Small file (100 rows × 10 columns): ~50ms
- Medium file (1000 rows × 20 columns): ~200ms
- Large file (10K rows × 30 columns): <500ms

**Profiling Strategy:**
- Amelia measures: I/O time vs. detection algorithm time
- Likely: file I/O + CSV parsing = 80%, detection = 20%
- Optimization: Lazy evaluation (stop after 100 samples, don't scan all 10K)

**Decision Needed:**
- [ ] Sample rows from file vs. read entire file?
  - Recommend: Sample first 100 rows + every 10th row (stratified sampling)
  - Faster, statistically representative
- [ ] Performance baseline: Measure on Amelia's machine, acceptable?
- [ ] Add metrics: "Detected {n} columns in {t}ms"

**Acceptance:** ✍️ Performance targets confirmed

---

**Topic 3.2: Caching & Reusability (10 min)**

**Question:** Should we cache detection results?

**Scenario:** User uploads same CSV file multiple times
- Option A: Re-detect every time (safest, slower)
- Option B: Cache by file hash (faster, requires cache invalidation)
- Option C: Cache by (filename + size + timestamp) (simpler heuristic)

**Decision:** 
- Phase 1: No caching (simple)
- Phase 2: Add caching if detection becomes bottleneck

**Current Decision:**
- [ ] No caching for S2.2 MVP? YES

**Acceptance:** ✍️ Caching deferred to Phase 2

---

**Topic 3.3: Concurrency (10 min)**

**Question:** If 2 users upload CSVs simultaneously, what happens?

**Response:**
- Each request is independent, no shared state
- Should work fine with Spring Boot thread pool
- No special handling needed for S2 MVP

**Decision:**
- [ ] Assume Spring handles concurrency automatically? YES
- [ ] Add load testing after S2 complete? YES (S3)

**Acceptance:** ✍️ Concurrency strategy confirmed

---

### **SEGMENT 4: Test Strategy (30 min)**

**Topic 4.1: Unit Tests (10 min)**

**What to test:**
1. **Detector Classes** - Each detector tested in isolation
   ```java
   @Test
   void emailDetectorShouldMatchEmailAddresses() {
     EmailTypeDetector detector = new EmailTypeDetector();
     DetectionResult result = detector.detect(
       "email_address",
       Arrays.asList("john@example.com", "jane@example.com")
     );
     assertEquals(EMAIL, result.getType());
     assertTrue(result.getConfidence() > 90);
   }
   ```

2. **DetectorFactory** - Correct detector instantiation
   ```java
   @Test
   void factoryShouldReturnEmailDetectorForEmailType() {
     ColumnTypeDetector detector = DetectorFactory.createDetector("email", sampleData);
     assertInstanceOf(EmailTypeDetector.class, detector);
   }
   ```

3. **Encoding Handling** - UTF-8 vs ISO-8859-1
4. **Edge Cases** - Nulls, empty strings, malformed data
5. **Alternative Suggestions** - Top 3 candidates ranked

**Coverage Target:** >85%

**Decision Needed:**
- [ ] TDD approach? (Write tests first) YES
- [ ] Test parameterization for multiple encodings? YES (use @ParameterizedTest)
- [ ] Mock CSV parser or use real files? Use real CSV files

**Acceptance:** ✍️ Unit test strategy confirmed

---

**Topic 4.2: Integration Tests (10 min)**

**What to test:**
1. **End-to-End API** - Full POST `/api/domains/{id}/detect-types` flow
   ```
   POST /api/domains/1/detect-types
   Request: multipart/form-data with CSV file
   Response: {
     columns: [{name: "email", type: "EMAIL", confidence: 95}, ...],
     statistics: {totalColumns: 5, detectedCount: 4, averageConfidence: 87}
   }
   ```

2. **Real CSV files** - Test with Mary's sample files
   - UTF-8, ISO-8859-1, mixed encoding
   - Small (10 rows), medium (1000), large (10K)
   - Ambiguous headers ("name", "date")

3. **Accuracy measurement** - Compare detected types vs. expected types
   - Target: >85% accuracy across test set

**Test Data Preparation:**
- Mary provides 100-500 CSV samples with ground truth labels
- Amelia writes test that loads samples, detects, measures accuracy

**Decision Needed:**
- [ ] Use RestAssured for API testing? YES (consistent with S2.1)
- [ ] CSV samples provided by Mary? YES (by 03/03)
- [ ] Accuracy report: Before shipping, run on full test set? YES

**Acceptance:** ✍️ Integration test strategy confirmed

---

**Topic 4.3: Test Execution & Coverage (10 min)**

**Coverage Report:**
- Run: `mvn clean test`
- Generate: `mvn jacoco:report`
- Target: >85% coverage (all detectors, factory, API controller)
- Exclude: Getters/setters, trivial logging

**Before Sprint Complete:**
- [ ] All tests passing (0 failures)
- [ ] Coverage >85%
- [ ] No regressions in S2.1 tests (should still be 334 passing)
- [ ] Benchmark: Detection time on large file documented

**Acceptance:** ✍️ QA success criteria confirmed

---

### **SEGMENT 5: Risk Assessment & Contingencies (15 min)**

**Risk 1: Detection Accuracy Lower Than Expected (Medium Impact)**
- **Scenario:** Real-world CSVs have headers we didn't anticipate
- **Impact:** Users get wrong type suggestions, manual correction slow
- **Mitigation:**
  - [ ] Phase 1: Conservative thresholds (>80% confidence only)
  - [ ] Show alternatives always (give user choice)
  - [ ] Collect feedback: Track user overrides, improve in Phase 2
- **Acceptance:** OK to accept 70% accuracy if alternatives shown

**Risk 2: Performance Degradation on Large Files (Low Impact)**
- **Scenario:** 10K row file takes >500ms
- **Impact:** User waits, poor experience
- **Mitigation:**
  - [ ] Lazy loading: Sample 100 rows, not full file
  - [ ] Async option: Fire detection in background, poll for results
  - [ ] Cache popular headers
- **Acceptance:** If >500ms, implement lazy loading

**Risk 3: Encoding Detection Fails (Low Impact)**
- **Scenario:** Auto-detect wrong encoding, headers garbled
- **Impact:** All detections fail for that file
- **Mitigation:**
  - [ ] Default to UTF-8, allow manual override in UI
  - [ ] Show file preview before detection: "Looks OK?"
  - [ ] Error message: "Try changing encoding"
- **Acceptance:** Auto-detect + manual override acceptable

**Risk 4: Detector Factory Explosion (Medium Impact)**
- **Scenario:** Amelia creates 13 detector classes, code gets messy
- **Impact:** Hard to maintain, difficult to add new types
- **Mitigation:**
  - [ ] Use configuration: Define patterns in YAML/JSON
  - [ ] Generic detector with pluggable patterns
  - [ ] Review by Winston in code review
- **Acceptance:** Check code complexity in review

---

### **SEGMENT 6: Acceptance Criteria Final Check (15 min)**

**Status of ACs (Updated after refinement):**

| AC # | Criteria | Status | Notes |
|------|----------|--------|-------|
| AC1 | Endpoint <500ms | ✅ CONFIRMED | Lazy load + sampling strategy |
| AC2 | CSV parser | ✅ CONFIRMED | PapaParse or OpenCSV |
| AC3 | 13 type detection | ✅ CONFIRMED | 6 Personal + 3 Financial + 4 Temporal |
| AC4 | Detection method | ✅ CLARIFIED | Pattern + value analysis (combined confidence) |
| AC5 | TypeDetectionResult DTO | ✅ CONFIRMED | With alternatives + statistics |
| AC6 | >90% accuracy | ⚠️ REVISED | Target 85%, accept 70% if alternatives shown |
| AC7 | Robust null handling | ✅ CONFIRMED | Never crash, return UNKNOWN if can't detect |
| AC8 | Multi-encoding test | ✅ CONFIRMED | UTF-8, ISO-8859-1, Cp1252 |
| AC9 | Error handling | ✅ CONFIRMED | Log errors, return meaningful messages |
| AC10 | Logging & debug | ✅ CONFIRMED | DEBUG level with confidence scores |
| AC11 | Code review + Javadoc | ✅ CONFIRMED | 100% Javadoc, >85% coverage |

**Final Decisions:**
- [ ] All ACs clear? YES ✅
- [ ] Any new ACs discovered? NO
- [ ] Scope creep prevented? YES

---

## 📌 DECISIONS & ACTION ITEMS

### **Decisions Made in Refinement:**

1. ✅ **Algorithm:** Pattern + value confidence combination (not rule-based only)
2. ✅ **Architecture:** Strategy pattern with 13 detector classes + DetectorFactory
3. ✅ **Accuracy Target:** 85% primary + alternatives for <80% confidence
4. ✅ **Encoding:** Auto-detect + user override option
5. ✅ **Robustness:** Never crash, return UNKNOWN for ambiguous data
6. ✅ **Performance:** Lazy load (100 rows + sampling) for >500ms target
7. ✅ **Testing:** TDD approach, >85% coverage, real CSV samples
8. ✅ **Risk Acceptance:** OK with 70% accuracy if alternatives shown
9. ✅ **Phase 2 Deferral:** Caching, ML-based detection, active learning → S3+

### **Action Items for Amelia (Developer):**

- [ ] Review Winston's sequence diagram (to be provided)
- [ ] Create `ColumnTypeDetector` abstract class by 05/03
- [ ] Implement 3 category detectors (Personal, Financial, Temporal) by 06/03
- [ ] Implement each sub-detector (13 total) by 07/03
- [ ] Create `TypeDetectionControllerTests` with RestAssured by 08/03
- [ ] Run accuracy test on Mary's 100+ CSV samples by 09/03
- [ ] Code review ready by 09/03

### **Action Items for Mary (Analyst):**

- [ ] Prepare 100-500 CSV test samples with ground truth labels by 04/03
- [ ] Multi-encoding samples (UTF-8, ISO-8859-1) by 04/03
- [ ] Edge case samples (nulls, malformed, ambiguous headers) by 04/03
- [ ] Accuracy measurement spreadsheet template by 04/03

### **Action Items for Winston (Architect):**

- [ ] Finalize sequence diagram (API → Controller → Service → Detectors) by 04/03
- [ ] Review Amelia's detector architecture by 08/03
- [ ] Code review S2.2 before merge by 09/03

---

## 📊 DEFINITION OF DONE (S2.2 Complete When)

- [ ] All 11 ACs passing (AC1-AC11)
- [ ] All unit tests passing (>85% coverage)
- [ ] All integration tests passing (RestAssured)
- [ ] Accuracy >85% on test set (or 70% with alternatives)
- [ ] No regressions in S2.1 tests (334 still passing)
- [ ] Code review approved (Winston)
- [ ] 100% Javadoc complete
- [ ] Performance verified: Large file <500ms
- [ ] Swagger/OpenAPI docs complete
- [ ] Story state changed to "done" in sprint status

---

## ✅ REFINEMENT SESSION SIGN-OFF

**Date:** 03/03/2026  
**Duration:** 2.5 hours  
**Participants Present:** [Amelia ___], [Mary ___], [Winston ___]

**Outcomes:**
- [ ] Algorithm finalized: ✍️ _____________
- [ ] Test strategy approved: ✍️ _____________
- [ ] Risk mitigation plans accepted: ✍️ _____________
- [ ] AC clarifications complete: ✍️ _____________
- [ ] Ready to start development: ✍️ _____________

**Signature Lines:**
- **Amelia (Developer):** _________________ **Date:** _____
- **Mary (Analyst):** _________________ **Date:** _____
- **Winston (Architect):** _________________ **Date:** _____

**Next Meeting:** Mid-sprint check-in (24/03) - Verify S2.2 completion & accuracy

---

