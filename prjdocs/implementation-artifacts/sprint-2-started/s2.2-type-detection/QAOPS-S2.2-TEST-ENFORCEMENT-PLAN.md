---
title: "S2.2 TYPE DETECTION - QA TEST ENFORCEMENT PLAN"
date: 2026-02-28
owner: "Quinn (QA Engineer)"
status: "🔴 ACTIVE TASKING"
priority: "HIGH"
---

# 🧪 S2.2 Type Detection - QA Test Enforcement Plan

**Assigned To:** Quinn (QA Engineer)  
**Story:** S2.2 Data Type Detection (8 pts)  
**Timeline:** 03/03 - 10/03/2026  
**Responsibility:** Test Automation, Accuracy Validation, QA Gate  
**Status:** 🔴 **ACTIVE - AWAITING YOUR RESPONSE**

---

## 🎯 YOUR MISSION

> **Enforce test quality for S2.2 implementation. Ensure:**
> - ✅ 85%+ accuracy on 80+ real-world CSV samples
> - ✅ 100% code coverage (>85% target minimum)
> - ✅ Zero crashes on edge cases (nulls, encoding, large files)
> - ✅ Performance <500ms on 10K rows
> - ✅ API contract validation (TypeDetectionResponse DTO)

---

## 📋 YOUR DELIVERABLES (By 09/03)

### **Deliverable 1: Test Automation Suite** (05-08/03)
**File:** `src/test/java/com/movkfact/service/detection/`

```
├── ColumnTypeDetectorTests.java (Unit tests - 13 detectors)
├── ColumnValueAnalyzerTests.java (Value scoring logic)
├── ColumnPatternDetectorTests.java (Header matching)
├── TypeDetectionControllerTests.java (API integration)
├── TypeDetectionServiceTests.java (Service layer)
├── TypeDetectionE2ETests.java (End-to-end with real CSV)
└── TypeDetectionPerformanceTests.java (Benchmarking)
```

**Coverage Target:** >85% (Jacoco report by 08/03)

### **Deliverable 2: Accuracy Validation Report** (08-09/03)
**File:** `prjdocs/test-artifacts/qa-test-execution-report-s2.2.md`

```
├── Test Summary (overall accuracy %)
├── By Test Set Results
│   ├── Set 1: Easy cases (target 95%+)
│   ├── Set 2: Medium cases (target 80%+)
│   ├── Set 3: Hard cases (target 60%+)
│   ├── Set 4: Robustness cases (0 crashes)
│   └── Set 5: Performance cases (<500ms)
├── Detector-by-Detector Breakdown (13 detectors)
├── Edge Case Analysis (nulls, encoding, malformed)
├── Performance Profiling (avg time per file, slowest detector)
└── Sign-Off (Quinn + Amelia + Mary approval)
```

### **Deliverable 3: Test Data Validation** (07/03)
**File:** `prjdocs/test-artifacts/s2.2-test-data-verification.md`

```
├── Verify Mary's 80+ CSV files
├── Ground truth correctness check
├── Sample detection runs (spot check 5 files)
└── Flag any test data issues to Mary
```

### **Deliverable 4: Code Coverage Report** (08/03)
**File:** `target/site/jacoco/index.html` (generated)

```
├── Overall coverage % (target >85%)
├── Package coverage breakdown
├── File coverage (all detector classes)
├── Gap analysis (untested code paths)
└── Coverage trend (vs baseline)
```

---

## 🧪 TESTING PHASES (Your Timeline)

### **Phase 0: Preparation (03/03)**
```
BEFORE SESSION (morning):
[ ] Review S2.2-ALGORITHM-CLARIFICATION.md (understand logic)
[ ] Review S2.2-CONCRETE-EXAMPLES.md (6 test cases)
[ ] Review S2.2-TEST-DATA-PREPARATION-GUIDE.md (80+ samples spec)

DURING SESSION (segment 4, 11:15-11:55):
[ ] Listen to Mary present test strategy
[ ] Clarify accuracy measurement methodology
[ ] Propose additional test scenarios (edge cases)
[ ] Confirm 80+ sample count is appropriate

POST-SESSION (03/03 evening):
[ ] Update your test automation plan based on session decisions
[ ] Prepare TDD checklist for Amelia (deliver 04/03 morning)
```

### **Phase 1: Unit Test Foundation (05-06/03)**
```
PARALLEL WITH: Amelia's Phase 1 (foundation detectors)

YOUR TASKS:
[ ] Create test stubs for all 13 detectors
[ ] Write unit tests for EmailTypeDetector
  - Regex validation (10 test cases)
  - Header matching (5 cases)
  - Different encodings (3 cases)
  - Null/empty handling (3 cases)
  - Expected confidence scores
  
[ ] TDD cycle with Amelia:
  - Create test → Fails ❌
  - Amelia implements → Passes ✅
  - Refactor
  
[ ] Target: 3 detectors fully tested + passing by 06/03

DELIVERABLE: ColumnTypeDetectorTests.java (first draft)
```

### **Phase 2: Expand Test Coverage (06-07/03)**
```
PARALLEL WITH: Amelia's Phase 2 (expand 10 more detectors)

YOUR TASKS:
[ ] Extend unit tests for all 13 detectors
[ ] Test value analyzer (scoring logic)
  - 100% match: should be 100%
  - 50% match: should be ~50%
  - No match: should be ~0%
  - Edge: Empty data → handled gracefully
  
[ ] Test header pattern matching
  - Exact match (e.g., "email" → EMAIL)
  - Close match (e.g., "emailaddress" → EMAIL)
  - Partial match (e.g., "user_email" → partial credit)
  - No match (e.g., "xyz" → 0%)
  - Normalization (lowercase, underscores)

[ ] Test combined scoring formula
  - (header_score × 0.4) + (value_score × 0.6) = confidence
  - Verify with 5 manual examples

DELIVERABLE: ColumnValueAnalyzerTests.java + ColumnPatternDetectorTests.java
```

### **Phase 3: Integration Testing (07-08/03)**
```
PARALLEL WITH: Amelia's Phase 3 (testing phase)

YOUR TASKS:
[ ] Create TypeDetectionControllerTests.java
  - POST /api/type-detection endpoint
  - Request: CSV file upload or raw data
  - Response: TypeDetectionResponse (13 types, confidence %)
  - Status codes: 200, 400, 500
  
[ ] Create TypeDetectionServiceTests.java
  - Service layer methods
  - DTO mapping
  - Parallel execution verification
  - Error handling
  
[ ] Test real CSV files (Mary's samples)
  - Load 5 representative files (1 from each set)
  - Run through full pipeline
  - Verify accuracy ≥ target %
  - Check response format

[ ] Edge case stress tests
  - Empty file (0 rows) → UNKNOWN for all
  - Header only (1 row) → UNKNOWN
  - 10K rows (performance) → <500ms
  - Mixed encoding (UTF-8 + Latin-1) → auto-detect
  - Bad regex in detector → graceful fallback
  
[ ] Concurrency tests
  - Parallel detector execution
  - Thread safety (no race conditions)
  - Memory under load

DELIVERABLE: TypeDetectionControllerTests.java + TypeDetectionServiceTests.java
```

### **Phase 4: Full Accuracy Validation (08-09/03)**
```
PARALLEL WITH: Amelia's Phase 4 (polish) + CODE REVIEW

YOUR TASKS:
[ ] Load all 80+ CSV files from Mary (received 04/03)
[ ] Execute detection algorithm on each file
[ ] Measure accuracy:
  
  PRIMARY ACCURACY = correct_type_matches / total_columns
  
  EXAMPLE:
  - File has 5 columns
  - Results: 4 correct, 1 alternative (user selected from 2 options)
  - Primary accuracy: 4/5 = 80%
  - Partial credit: 4 + 0.5 = 4.5/5 = 90%
  
[ ] Aggregate results by test set:
  - Easy (20 files): Avg ___% (target 95%+) ✓/✗
  - Medium (20 files): Avg ___% (target 80%+) ✓/✗
  - Hard (15 files): Avg ___% (target 60%+) ✓/✗
  - Robustness (15 files): 0 crashes ✓/✗
  - Performance (10 files): Avg <500ms ✓/✗
  
[ ] Detector-by-detector breakdown
  - EMAIL: 98% accuracy (5/5 files detected correctly)
  - PHONE: 92% accuracy (4/5 detected, 1 UNCERTAIN)
  - [for all 13 detectors]
  
[ ] Identify failures
  - Which files failed?
  - Which detectors failed?
  - Root cause: Algorithm issue? Test data issue? Encoding?
  
[ ] Performance profiling
  - Average time per file (target <50ms for 100-row file)
  - Slowest detector (why?)
  - Memory consumption
  
[ ] Generate coverage report
  - mvn clean test jacoco:report
  - Verify >85% coverage
  - File-by-file breakdown

DELIVERABLE: qa-test-execution-report-s2.2.md (final, signed)
```

### **Phase 5: Code Review + QA Gate (09/03)**
```
REQUIREMENTS FOR PR APPROVAL:

Amelia submits PR, you verify:

[ ] All 13 detectors implemented ✓
[ ] Unit test coverage >85% ✓ (see jacoco report)
[ ] All unit tests passing ✓ (mvn test)
[ ] No regressions in S2.1, S2.3 ✓ (baseline tests)
[ ] API contract correct ✓ (TypeDetectionResponse DTO)
[ ] Edge cases handled ✓ (no NPE crashes)
[ ] Performance <500ms ✓ (profiler output)
[ ] Accuracy ≥ 85% ✓ (Mary's test set)
[ ] Javadoc 100% complete ✓ (grep JavaDoc)
[ ] No code smell/warnings ✓ (SonarQube if available)

DECISION:
- If ALL ✓: Approve PR, sign-off to Winston
- If ANY ✗: Request changes, specify what needs fixing

DELIVERABLE: PR review comment with sign-off
```

---

## 🎯 TEST SCENARIOS (Comprehensive Checklist)

### **Scenario A: Happy Path (Easy Detection)**
```
Input: CSV with clear headers + clean data
  first_name | last_name | email | phone | gender | birth_date
  John       | Doe       | j@ex  | +1234 | M      | 1980-01-15

Expected Output:
  {
    columns: [
      { type: "FIRST_NAME", confidence: 98% },
      { type: "LAST_NAME", confidence: 97% },
      { type: "EMAIL", confidence: 99% },
      { type: "PHONE", confidence: 95% },
      { type: "GENDER", confidence: 92% },
      { type: "DATE_BIRTH", confidence: 96% }
    ]
  }

Test Assertion: All confidence > 90%
```

### **Scenario B: Ambiguous Detection (Medium)**
```
Input: CSV with generic headers
  name | date | id | value
  John | 2020-01-15 | 12345 | 1000.50

Expected Output:
  {
    columns: [
      { 
        type: "UNKNOWN", 
        confidence: 45%,
        alternatives: ["FIRST_NAME", "LAST_NAME", "FULL_NAME"]
      },
      { 
        type: "UNCERTAIN", 
        confidence: 72%,
        alternatives: ["DATE_BIRTH", "DATE_CREATED"]
      },
      { 
        type: "UNKNOWN", 
        confidence: 30%,
        alternatives: ["ACCOUNT_NUMBER", ...] 
      },
      { 
        type: "UNCERTAIN", 
        confidence: 68%,
        alternatives: ["AMOUNT", "CURRENCY"]
      }
    ]
  }

Test Assertion: At least alternatives provided for ambiguous columns
```

### **Scenario C: Edge Case - Nulls/Missing Values**
```
Input: CSV with 35% null values
  email | phone | address | age
  j@ex | NULL | 123 Main | 35
  NULL | +1234 | NULL | NULL
  ...

Expected Output:
  {
    columns: [
      { type: "EMAIL", confidence: 88%, warning: "35% nulls" },
      { type: "PHONE", confidence: 72%, warning: "35% nulls" },
      { type: "ADDRESS", confidence: 75%, warning: "35% nulls" },
      { type: "UNKNOWN", confidence: 20%, warning: "90% nulls - insufficient data" }
    ]
  }

Test Assertion: No crash, graceful null handling, warnings issued
```

### **Scenario D: Edge Case - Large File Performance**
```
Input: 10,000 rows × 15 columns

Time to process:
- Expected: <500ms
- Sampling: 195 rows (~2% strategic sampling)
- Parallel: 13 detectors simultaneously

Test Assertion:
  - Processing time < 500ms
  - Memory < 256MB
  - Results accurate (sampling validated)
  - No OOM (out of memory) errors
```

### **Scenario E: Encoding Edge Cases**
```
Input Files:
  1. UTF-8 with accents: François, Müller, García
  2. ISO-8859-1 (Latin-1): Français, español
  3. Windows-1252: Portugais, Bosnian
  
Expected Output:
  - All files correctly detected
  - Names recognized despite encoding
  - No garbled output
  - Accuracy >90%

Test Assertion: Encoding auto-detection works, fallback to UTF-8 safe
```

### **Scenario F: CSV Format Variations**
```
Input Variations:
  1. Tab-delimited (not comma)
  2. Semicolon-delimited (European locales)
  3. Quoted headers with spaces
  4. No header row (just data)
  5. Header only (no data rows)

Expected: Algorithms handles gracefully
- Cases 1-3: Detected properly
- Case 4: User prompted for headers
- Case 5: UNKNOWN (no data to analyze)

Test Assertion: Format variations don't crash system
```

### **Scenario G: Detector Specific - Email**
```
Valid emails:
  ✓ user@domain.com
  ✓ first.last@company.co.uk
  ✓ user+tag@service.org
  ✓ _user.name_@test.io

Invalid (no match):
  ✗ @domain.com (no user)
  ✗ user@.com (no domain)
  ✗ user@domain (no TLD)
  ✗ plaintext (no @)

Test Assertion: Regex correctly validates >95% of real emails
```

### **Scenario H: Detector Specific - Amount**
```
Valid amounts:
  ✓ 1000.50 (decimal)
  ✓ 1000 (integer)
  ✓ -500.99 (negative)
  ✓ 0.01 (decimal)

Invalid (UNCERTAIN):
  ⚠ 1000,50 (comma separator - locale-specific)
  ⚠ $1000.50 (currency symbol - might be CURRENCY instead)

Test Assertion: Amount detector distinguishes from CURRENCY type
```

### **Scenario I: Detector Specific - Date Variations**
```
Date formats that should all match DATE_BIRTH:
  ✓ 1980-01-15 (ISO)
  ✓ 01/15/1980 (US)
  ✓ 15-01-1980 (EU)
  ✓ 1980-01-15T00:00:00Z (ISO with time)

Edge case:
  ⚠ 2030-01-15 (future date - biologically impossible birth)
  → Should flag as DATE_CREATED, not DATE_BIRTH

Test Assertion: Future dates correctly classified as different date type
```

---

## 📊 ACCEPTANCE CRITERIA (Your Sign-Off Checklist)

**For S2.2 to be "DONE", Quinn must verify:**

| AC # | Criteria | Status | Evidence |
|------|----------|--------|----------|
| AC-1 | 85% accuracy on 80+ test samples | [ ] | Report section 2.1 |
| AC-2 | >85% code coverage (Jacoco) | [ ] | target/site/jacoco/index.html |
| AC-3 | All 334+ unit tests passing | [ ] | mvn test output |
| AC-4 | Zero crashes on edge cases | [ ] | Robustness test results |
| AC-5 | Performance <500ms (10K rows) | [ ] | Performance profiler |
| AC-6 | API TypeDetectionResponse correct | [ ] | Integration tests |
| AC-7 | No S2.1, S2.3 regressions | [ ] | Baseline test suite |
| AC-8 | 13 detectors all implemented | [ ] | Code review checklist |
| AC-9 | Parallel execution verified | [ ] | Concurrency tests |
| AC-10 | Javadoc 100% complete | [ ] | Code inspection |
| AC-11 | No OWASP security issues | [ ] | Security scan (if available) |

**DECISION GATE:**
```
IF all 11 AC = ✅ APPROVED:
  → Story ready for merge
  → S2.2 marked DONE
  → S2.5 can start

IF any AC = ❌ BLOCKED:
  → Request changes from Amelia
  → Re-test & verify fix
  → Re-submit for review
```

---

## 🔧 TOOLS & COMMANDS (Your Toolkit)

### **Unit Testing**
```bash
# Run all tests
mvn clean test

# Run specific test class
mvn test -Dtest=ColumnTypeDetectorTests

# Run specific test method
mvn test -Dtest=ColumnTypeDetectorTests#testEmailDetectionPositive

# View test report
open target/surefire-reports/index.html
```

### **Code Coverage**
```bash
# Generate Jacoco report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Coverage for specific package
grep -A5 "com.movkfact.service.detection" target/site/jacoco/index.html
```

### **Performance Profiling**
```bash
# Run performance test class
mvn test -Dtest=TypeDetectionPerformanceTests

# Profile with JProfiler (if available)
java -javaagent:/path/to/jprofiler -cp ... com.movkfact...

# Simple timing (in code)
long start = System.currentTimeMillis();
// ... detection code ...
long duration = System.currentTimeMillis() - start;
```

### **CSV Testing**
```bash
# Load sample CSV into test
Path csvFile = Paths.get("src/test/resources/s2.2-samples/set-1/personal-basic.csv");
List<String> lines = Files.readAllLines(csvFile);
// ... detect types ...
```

### **API Testing (with RestAssured)**
```java
given()
  .multiPart("file", csvFile, "text/csv")
  .when()
  .post("/api/type-detection")
  .then()
  .statusCode(200)
  .body("columns[0].type", equalTo("FIRST_NAME"))
  .body("columns[0].confidence", greaterThan(0.85));
```

---

## 📞 YOUR ESCALATION PATH

**If you find critical issues:**

| Issue Type | Action | Escalate To | By When |
|------------|--------|-------------|---------|
| Algorithm failing | Notify Amelia | Winston | <2 hours |
| Performance <500ms | Optimize sampling | Amelia | <4 hours |
| Test data bad | Notify Mary | Mary + PM | <2 hours |
| Coverage gap | Request code path | Amelia | <4 hours |
| Security issue | Block PR | PM + Winston | <1 hour |

---

## 📋 YOUR COMMUNICATION CADENCE

| Day | Action | To Whom | Medium |
|-----|--------|---------|--------|
| 03/03 | Session attendance + input | Team | In-person |
| 04/03 | Test data validation spot-check | Mary | Email |
| 05/03 | TDD checklist for Amelia | Amelia | Slack/Email |
| 06/03 | Mid-week test status | Team | Daily standup |
| 08/03 | Coverage report generation | Team | Email |
| 09/03 | Final sign-off report | PM + Amelia + Mary | Email + PR comment |

---

## 🎯 SUCCESS DEFINITION

**S2.2 QA is SUCCESSFUL when:**

✅ **Accuracy** - 85%+ on 80+ real-world samples  
✅ **Coverage** - >85% code coverage (Jacoco)  
✅ **Tests** - 100% unit tests passing (334+)  
✅ **Robustness** - Zero crashes on edge cases  
✅ **Performance** - <500ms on 10K rows  
✅ **API** - Response format matches contract  
✅ **Stability** - No regressions in other stories  
✅ **Documentation** - Javadoc 100%, test report complete  
✅ **Sign-Off** - Quinn + Amelia + Mary + Winston approved  

---

## 📤 YOUR FIRST ACTION (TODAY 28/02)

**QAOPS-S2.2-001: Test Planning & Session Prep**

```
[ ] Read S2.2-ALGORITHM-CLARIFICATION.md (45 min)
[ ] Read S2.2-CONCRETE-EXAMPLES.md (15 min)
[ ] Read S2.2-TEST-DATA-PREPARATION-GUIDE.md (20 min)
[ ] Create outline of test automation suite (1 hour)
[ ] Prepare questions for refinement session (15 min)
[ ] Confirm you'll attend 03/03 @ 09:00 (calendar accept)

TOTAL: ~2.5 hours prep
RESULT: Ready for productive refinement session
```

---

## 📧 ACTION ITEMS (Confirm Receipt)

**Quinn - Acknowledge Receipt of This Document:**

- [ ] I have read and understood this QA test enforcement plan
- [ ] I confirm my availability: 03/03 @ 09:00 session + 04-09/03 testing
- [ ] I have 5 days to deliver comprehensive test suite + accuracy report
- [ ] I understand the 4 deliverables and target 09/03 completion
- [ ] I'm ready to enforce 85%+ accuracy on S2.2

**Confirm by:** End of day 28/02 (Slack reaction or email reply)

---

## 📎 ATTACHMENTS & REFERENCES

📖 **Required Reading (in order):**
1. [S2.2-ALGORITHM-CLARIFICATION.md](./S2.2-ALGORITHM-CLARIFICATION.md)
2. [S2.2-CONCRETE-EXAMPLES.md](./S2.2-CONCRETE-EXAMPLES.md)
3. [S2.2-TEST-DATA-PREPARATION-GUIDE.md](./S2.2-TEST-DATA-PREPARATION-GUIDE.md)
4. [S2.2-TECHNICAL-ARCHITECTURE.md](./S2.2-TECHNICAL-ARCHITECTURE.md)

📋 **Reference Docs:**
- [REFINEMENT-S2.2-TYPE-DETECTION-2026-03-03.md](./REFINEMENT-S2.2-TYPE-DETECTION-2026-03-03.md) - Session structure
- Sprint backlog & status tracking

🔧 **Test Tools:**
- Maven: mvn test
- JUnit 5: All test classes use @Test
- RestAssured: API integration
- Jacoco: Coverage reporting

---

**Document Status:** 🔴 **ACTIVE TASK ASSIGNMENT**  
**Assigned To:** Quinn (QA Engineer)  
**Due Date:** 09 mars 2026 (final sign-off)  
**Escalation:** If blocked → Winston or PM  
**Next Steps:** Attend 03/03 refinement session @ 09:00

