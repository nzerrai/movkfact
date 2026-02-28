---
title: "🎬 AMELIA - PHASE A IMMEDIATE START"
date: 2026-02-28
time: "21:45 CET"
phase: "A - Code Understanding"
duration: "2 hours"
status: "✅ READY TO START NOW"
---

# 🎬 AMELIA - PHASE A IMMEDIATE START

**Date:** 28 février 2026 @ 21:45  
**Phase:** A - Code Understanding & Architecture Review  
**Duration:** 2 hours (can start tonight or tomorrow morning)  
**Status:** ✅ **READY - ALL RESOURCES PREPARED**

---

## 🎯 PHASE A OBJECTIVE

**Understand how the existing S2.2 type detection system works**

```
Before You Start:
  ✅ 334 tests passing
  ✅ 88% code coverage
  ✅ All 13 types already implemented
  ✅ Architecture is solid

Your Task:
  Read & understand the code flow
  Document how it works
  Answer: "How does detection happen?"
  
Exit Criteria:
  ✅ You can explain the strategy pattern
  ✅ You understand the 3 detector orchestration
  ✅ You know how validators work
  ✅ You see where performance could be optimized
```

---

## 📖 READING PLAN (2 HOURS TOTAL)

### Step 1: Overview (10 minutes)

Read this summary of what you're about to explore:

```
S2.2 TYPE DETECTION ARCHITECTURE

Flow:
  1. REST Controller receives CSV file upload
  2. CsvTypeDetectionService processes it
  3. For each column header:
     a. PersonalTypeDetector tries 6 types (first_name, last_name, email, gender, phone, address)
     b. If not matched → FinancialTypeDetector tries 3 types (amount, account_number, currency)
     c. If not matched → TemporalTypeDetector tries 4 types (birth_date, date, time, timezone)
     d. If not matched → PatternDetector does header pattern matching as fallback
  4. Returns TypeDetectionResult with detected types + confidence scores

Key Classes:
  - CsvTypeDetectionService: Main orchestrator
  - PersonalTypeDetector: Runs 6 validators
  - FinancialTypeDetector: Runs 3 validators
  - TemporalTypeDetector: Runs 4 validators
  - PatternCache: Pre-compiled regex patterns
  - ColumnPatternDetector: Header-based matching

Key Design Pattern:
  Strategy Pattern via Validators
  - Each type (FirstName, Email, Amount, etc.) has a validator
  - Validators independently calculate confidence
  - Orchestrators collect results and pick best match

Current Execution Model:
  SEQUENTIAL (one detector at a time)
  - PersonalTypeDetector completes
  - Then FinancialTypeDetector
  - Then TemporalTypeDetector
  Possible optimization: PARALLEL (run all 3 simultaneously)
```

**Time: 10 minutes** ✅

---

### Step 2: Main Orchestrator (30 minutes)

**File to read:**
```
src/main/java/com/movkfact/service/detection/CsvTypeDetectionService.java
```

**What to understand:**
```
1. How CSV is parsed (Apache Commons CSV)
2. How charset is detected (UTF-8 vs ISO-8859-1)
3. How headers are extracted
4. How sample values are collected
5. How the 3 detectors are called in sequence
6. How results are merged into TypeDetectionResult
```

**Reading Tips:**
- Focus on the `detectTypes()` method (main flow)
- Note the sequential detector calls:
  ```
  if (personalTypeDetector != null) { ... }
  if (detectedType == null && financialTypeDetector != null) { ... }
  if (detectedType == null && temporalTypeDetector != null) { ... }
  ```
- See the confidence calculation: `85.0` for validated types
- Note the fallback to PatternDetector

**Questions to Answer:**
- [ ] How is the CSV parsed?
- [ ] How many sample rows are collected?
- [ ] What's the order of detector execution?
- [ ] How are confidence scores assigned?
- [ ] What happens if a column can't be detected?

**Time: 30 minutes** ⏱️

---

### Step 3: Example Detector (30 minutes)

**File to read:**
```
src/main/java/com/movkfact/service/detection/personal/PersonalTypeDetector.java
```

**What to understand:**
```
1. How PersonalTypeDetector orchestrates 6 validators
2. How it calls each validator (FirstName, LastName, Gender, Phone, Address, Email)
3. How confidence scores are collected
4. How conflicts are resolved (FIRST_NAME vs LAST_NAME)
5. How the best match is selected
6. The 75% minimum confidence threshold
```

**Then look at one concrete validator:**
```
src/main/java/com/movkfact/service/detection/personal/FirstNameValidator.java
```

**What to understand:**
```
1. How validator pattern is implemented
2. Pattern matching (regex) for header
3. Value analysis (length heuristics, pattern matching)
4. Confidence calculation formula
5. Logging for debugging
```

**Questions to Answer:**
- [ ] How do validators calculate confidence?
- [ ] What patterns does FirstNameValidator use?
- [ ] How are length heuristics used?
- [ ] What's the 75% threshold purpose?
- [ ] How is conflict resolution done?

**Time: 30 minutes** ⏱️

---

### Step 4: REST Controller (20 minutes)

**File to read:**
```
src/main/java/com/movkfact/controller/TypeDetectionController.java
```

**What to understand:**
```
1. REST endpoint: POST /api/domains/{domainId}/detect-types
2. File validation (size, extension, MIME type)
3. Error handling (400, 413, 415, 500)
4. Request parameters (sampleSize)
5. Response structure (TypeDetectionResult)
6. Logging & debugging
```

**Questions to Answer:**
- [ ] What's the maximum file size?
- [ ] What validations are performed?
- [ ] What HTTP status codes are returned?
- [ ] How are errors handled?
- [ ] What does a successful response look like?

**Time: 20 minutes** ⏱️

---

### Step 5: Run Tests & See Logs (20 minutes)

**Command to run:**
```bash
cd /home/seplos/mockfact
mvn test -Dtest=CsvTypeDetectionServiceTests
```

**What to observe:**
```
✅ 9 tests should pass
✅ Watch DEBUG logs to see detection in action
✅ See messages like:
   - "CsvTypeDetectionService: Starting detection for file"
   - "FirstNameValidator: X of Y values match first name pattern"
   - "PersonalTypeDetector: Column detected as EMAIL"
   - "CsvTypeDetectionService: Detection complete"
```

**Questions to Answer:**
- [ ] Do all 9 tests pass?
- [ ] Can you trace one detection in the logs?
- [ ] How confident is the detector on different columns?
- [ ] What confidence scores are typical?

**Time: 20 minutes** ⏱️

---

## ✅ PHASE A CHECKLIST

After reading, you should be able to check these:

- [ ] **Architecture**: I can draw/describe the 3-detector orchestration pattern
- [ ] **Flow**: I can trace how a CSV column goes from header → detection → result
- [ ] **Validators**: I understand how one validator (e.g., FirstNameValidator) works
- [ ] **Confidence**: I can explain how confidence scores are calculated
- [ ] **Patterns**: I know what patterns FirstName/Email/Amount validators use
- [ ] **Edge Cases**: I see how nulls, empty columns, mismatches are handled
- [ ] **Performance**: I notice the sequential execution (not parallel)
- [ ] **Error Handling**: I know the REST endpoint validates inputs
- [ ] **Testing**: I've seen tests run and understand DEBUG logs
- [ ] **Improvement Opportunities**: I've identified what could be optimized

**When all checked:** Phase A complete ✅

---

## 🔗 DIRECT LINKS TO FILES

Click these to jump directly to code:

### Core Files to Read

1. **CsvTypeDetectionService.java** (Main orchestrator)
   ```
   /src/main/java/com/movkfact/service/detection/CsvTypeDetectionService.java
   Lines 1-100: Read first (main flow)
   Lines 80-150: Detector orchestration
   ```

2. **PersonalTypeDetector.java** (6 types example)
   ```
   /src/main/java/com/movkfact/service/detection/personal/PersonalTypeDetector.java
   Lines 1-50: Overview + detect() method
   Lines 50-150: Full implementation
   ```

3. **FirstNameValidator.java** (One validator example)
   ```
   /src/main/java/com/movkfact/service/detection/personal/FirstNameValidator.java
   Complete file - ~80 lines, easy to understand
   ```

4. **TypeDetectionController.java** (REST API)
   ```
   /src/main/java/com/movkfact/controller/TypeDetectionController.java
   Lines 1-50: Read first (endpoint definition)
   Lines 50-125: Full implementation
   ```

### Supporting Files

5. **FinancialTypeDetector.java** (3 types, similar structure)
6. **TemporalTypeDetector.java** (4 types, similar structure)
7. **ColumnPatternDetector.java** (Pattern matching logic)
8. **PatternCache.java** (Regex caching)

### Tests to Run

9. **CsvTypeDetectionServiceTests.java**
   ```bash
   mvn test -Dtest=CsvTypeDetectionServiceTests
   ```

10. **PersonalTypeDetectorTests.java** (if exists)
    ```bash
    mvn test -Dtest=PersonalTypeDetectorTests
    ```

---

## 📝 DOCUMENTATION TO READ

**Before Phase A:**
- AMELIA-AUDIT-FINDINGS.md (5 min) - What you're walking into

**During Phase A:**
- S2.2-ALGORITHM-CLARIFICATION.md (25 min) - Understand scoring formula
  - Read: Email example (pages XX-XX)
  - Read: Ambiguous date example (pages XX-XX)

**After Phase A:**
- S2.2-TECHNICAL-ARCHITECTURE.md (reference, not required during Phase A)

---

## 🎯 YOUR NOTES TO TAKE

As you read, create a simple notes file:

```markdown
# AMELIA Phase A Notes

## Overall Architecture
- Main service: CsvTypeDetectionService
- 3 detectors: Personal, Financial, Temporal
- Pattern: Strategy via validators
- Execution: Sequential (opportunity for parallel)

## CsvTypeDetectionService Flow
1. Read CSV with charset detection
2. Extract headers
3. Collect sample values
4. For each column:
   - Try PersonalTypeDetector
   - Try FinancialTypeDetector
   - Try TemporalTypeDetector
   - Fallback to PatternDetector
5. Return TypeDetectionResult

## PersonalTypeDetector Details
- Runs 6 validators: FirstName, LastName, Gender, Phone, Address, Email
- Collects confidence scores
- Selects best match
- Minimum 75% threshold
- Returns null if inconclusive

## FirstNameValidator Pattern
- Header patterns: ["firstname", "first_name", "fname", "prenom"]
- Header score: Check if header matches patterns
- Value patterns: Lowercase letters, accents OK, length 3-9 chars typical
- Value score: % of values matching pattern + length heuristics
- Combined: (header_score × 0.4) + (value_score × 0.6)

## Observations
- Current implementation: SEQUENTIAL (one detector after another)
- Opportunity: Could be PARALLEL (run all 3 simultaneously)
- Performance: Need to verify <500ms on 10K rows
- Good code quality: Well-documented, clear structure
- Test coverage: 334 tests passing, 88% coverage
```

---

## ⏱️ TIME TRACKING

Record your actual time:

```
Start Time: ________
Activity 1 (Overview): _____ min (target 10)
Activity 2 (CsvTypeDetectionService): _____ min (target 30)
Activity 3 (PersonalTypeDetector + FirstNameValidator): _____ min (target 30)
Activity 4 (TypeDetectionController): _____ min (target 20)
Activity 5 (Run tests & logs): _____ min (target 20)
TOTAL TIME: _____ min (target 120)
End Time: ________
```

If you go over 2 hours, that's OK - understanding is more important than speed.

---

## 🚀 NEXT STEPS AFTER PHASE A

When you complete Phase A, you'll move to:

**Phase B (05/03, PM): Performance Baseline**
- Create PerformanceTests.java
- Test 1K and 10K row files
- Verify <500ms on 10K rows
- Document findings

**Phase C (06/03, AM): Accuracy Framework**
- Create AccuracyMeasurement class
- Prepare to run on Mary's test data (arriving 04/03)

---

## 💪 YOU'VE GOT THIS

**Remember:**
- ✅ 80% of S2.2 is already done
- ✅ Code is solid (334 tests passing)
- ✅ Architecture is clean
- ✅ You're NOT building from scratch
- ✅ Your job is understand → validate → optimize

**Phase A is just reading and understanding.**

No coding required yet. Just learn how it works.

---

## 📞 IF YOU HAVE QUESTIONS

During Phase A:
- Winston: Code architecture questions
- Previous developers: Code intent/reasoning
- Test logs: See actual behavior
- Documentation (S2.2-ALGORITHM-CLARIFICATION.md): Formula details

---

**Document:** AMELIA-PHASE-A-IMMEDIATE-START.md  
**Status:** ✅ **READY - START NOW**

🎯 **TARGET:** Understand architecture in 2 hours, then ready for Phase B

🚀 **Let's go!**
