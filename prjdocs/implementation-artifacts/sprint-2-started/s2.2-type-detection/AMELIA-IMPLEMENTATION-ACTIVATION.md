---
title: "AMELIA - S2.2 IMPLEMENTATION READINESS & ACTIVATION"
date: 2026-02-28
to: "Amelia (Developer)"
from: "Team"
status: "✅ READY TO CODE"
priority: "🔴 CRITICAL PATH"
---

# ✅ Amelia - S2.2 Implementation Ready to Start

**Date:** 28 février 2026  
**For:** Amelia (Developer)  
**Story:** S2.2 Implement CSV Column Type Detection (8 pts)  
**Start Date:** 05 mars 2026  
**Target Completion:** 09 mars 2026  
**Status:** 🟢 **YOU HAVE EVERYTHING TO BEGIN**

---

## 🎯 YOUR MISSION

Implement a production-ready **CSV Column Type Detection Engine** that:
- ✅ Detects 13 column data types (Personal, Financial, Temporal)
- ✅ Achieves 85%+ accuracy on real-world CSV files
- ✅ Processes 10K rows in <500ms
- ✅ Handles edge cases gracefully (nulls, encoding, malformed data)
- ✅ Integrates seamlessly with S2.5 CSV Upload UI

---

## 📚 YOUR COMPLETE DOCUMENTATION PACKAGE

**Location:** `prjdocs/implementation-artifacts/sprint-2-started/s2.2-type-detection/`

### **🔴 CRITICAL READING (Must read before coding)**

| Document | Size | Read Time | Why You Need It |
|----------|------|-----------|-----------------|
| **S2.2-EXECUTIVE-SUMMARY.md** | 9.5K | 10 min | Algorithm overview + quick reference |
| **S2.2-ALGORITHM-CLARIFICATION.md** | 18K | 45 min | ⭐ **ESSENTIAL** - All formulas, examples, logic |
| **S2.2-TECHNICAL-ARCHITECTURE.md** | 21K | 30 min | ⭐ **ESSENTIAL** - Code structure, patterns, examples |
| **S2.2-CONCRETE-EXAMPLES.md** | 21K | 30 min | Real CSV test cases (Easy/Medium/Hard) |

### **🟡 REFERENCE DOCUMENTS**

| Document | Purpose |
|----------|---------|
| S2.2-TEST-DATA-PREPARATION-GUIDE.md | Mary's test data spec (80+ samples) |
| REFINEMENT-S2.2-TYPE-DETECTION-2026-03-03.md | Session decisions (03/03) |
| QAOPS-S2.2-TEST-ENFORCEMENT-PLAN.md | Quinn's test strategy |
| S2.2-GO-APPROVAL.md | Success criteria & timeline |

---

## 🏗️ WHAT YOU NEED TO BUILD

### **Core Architecture: Strategy + Factory Pattern**

```java
// Abstract base class for all detectors
public abstract class ColumnTypeDetector {
  protected List<String> headerPatterns;       // ["email", "mail", "e-mail"]
  protected List<String> valueRegexPatterns;   // Regex for validation
  protected int minimumConfidence = 80;
  
  public DetectionResult detect(String columnHeader, List<String> sampleValues) {
    int headerScore = matchHeaderPatterns(columnHeader);
    int valueScore = analyzeValuePatterns(sampleValues);
    int confidence = (int)((headerScore * 0.4) + (valueScore * 0.6));
    
    return new DetectionResult(
      this.getColumnType(),
      confidence,
      headerScore, valueScore,
      alternativeSuggestions()
    );
  }
  
  protected abstract ColumnType getColumnType();
  protected abstract int matchHeaderPatterns(String header);
  protected abstract int analyzeValuePatterns(List<String> values);
}

// Factory to orchestrate all detectors
public class DetectorFactory {
  public static List<DetectionResult> detectColumn(
      String columnHeader, 
      List<String> sampleValues) {
    
    List<ColumnTypeDetector> detectors = createAllDetectors();
    
    // Run all 13 detectors in PARALLEL
    List<DetectionResult> results = detectors.parallelStream()
      .map(detector -> detector.detect(columnHeader, sampleValues))
      .collect(Collectors.toList());
    
    // Sort by confidence (highest first)
    results.sort((r1, r2) -> Integer.compare(r2.getConfidence(), r1.getConfidence()));
    
    return results;
  }
  
  private static List<ColumnTypeDetector> createAllDetectors() {
    return List.of(
      new EmailTypeDetector(),
      new FirstNameTypeDetector(),
      new LastNameTypeDetector(),
      // ... 13 detectors total
    );
  }
}
```

### **13 Concrete Detector Implementations**

**Personal (6):**
- EmailTypeDetector
- FirstNameTypeDetector
- LastNameTypeDetector
- PhoneTypeDetector
- GenderTypeDetector
- AddressTypeDetector

**Financial (3):**
- AccountNumberTypeDetector
- AmountTypeDetector
- CurrencyTypeDetector

**Temporal (4):**
- DateBirthTypeDetector
- DateCreatedTypeDetector
- TimezoneTypeDetector
- TimeTypeDetector

### **REST Endpoint**

```java
@PostMapping("/api/domains/{domainId}/detect-types")
public ResponseEntity<TypeDetectionResponse> detectColumnTypes(
    @PathVariable String domainId,
    @RequestParam("file") MultipartFile file) {
  
  // 1. Parse CSV
  // 2. Sample rows (first 50 + every 10th + random 50)
  // 3. Run DetectorFactory for each column
  // 4. Return TypeDetectionResponse (primary + 2 alternatives)
  
  return ResponseEntity.ok(response);
}
```

### **DTOs**

```java
public class TypeDetectionResponse {
  List<ColumnDetection> columns;  // One per column
  String domainId;
  String fileName;
  long processingTimeMs;
}

public class ColumnDetection {
  String columnName;
  ColumnType primaryType;        // EMAIL, FIRST_NAME, etc.
  int confidence;                // 0-100%
  int headerScore;               // 0-100
  int valueScore;                // 0-100
  List<TypeAlternative> alternatives;
  String rationale;
}

public class TypeAlternative {
  ColumnType type;
  int confidence;
}
```

---

## 📋 YOUR 5-DAY IMPLEMENTATION PLAN

### **Day 1 (05/03): Foundation**
```
Goals: 5 pts estimated
Time: 2 days of work

PHASE 1A: Core Detector Architecture (Day 1, AM)
  [ ] Create ColumnTypeDetector abstract class
  [ ] Header matching logic (normalize, pattern match)
  [ ] Value regex patterns (validation)
  [ ] Combined scoring formula (40% header + 60% value)
  
PHASE 1B: Sample Detectors (Day 1, PM)
  [ ] EmailTypeDetector (regex: ^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$)
  [ ] AmountTypeDetector (decimal pattern)
  [ ] DateBirthTypeDetector (YYYY-MM-DD + "birth" context)
  
PHASE 1C: DetectorFactory (Day 1, EOD)
  [ ] Factory instantiation pattern
  [ ] Parallel execution setup (parallelStream)
  [ ] Sorting by confidence
  [ ] Alternative suggestions logic
  
MILESTONE 1: 3 detectors + factory working
Deliverables: 
  - ColumnTypeDetector.java (abstract)
  - EmailTypeDetector.java (example)
  - AmountTypeDetector.java (example)
  - DateBirthTypeDetector.java (example)
  - DetectorFactory.java
  - Unit tests (>80% coverage for these classes)

TDD Approach:
  1. Quinn creates failing test for EmailTypeDetector
  2. You implement EmailTypeDetector
  3. Test passes ✅
  4. Repeat for Amount + DateBirth
```

### **Day 2 (06/03): Expand**
```
Goals: 3 pts estimated
Time: 2 days of work

PHASE 2A: Complete Personal Detectors (06/03, AM)
  [ ] FirstNameTypeDetector
  [ ] LastNameTypeDetector
  [ ] PhoneTypeDetector
  [ ] GenderTypeDetector
  [ ] AddressTypeDetector
  
PHASE 2B: Financial Detectors (06/03, PM)
  [ ] AccountNumberTypeDetector (IBAN patterns)
  [ ] CurrencyTypeDetector (EUR, USD, GBP, etc.)
  
PHASE 2C: Temporal Detectors (06/03, EOD)
  [ ] DateCreatedTypeDetector
  [ ] TimezoneTypeDetector (IANA zone strings)
  [ ] TimeTypeDetector (HH:MM format)
  
MILESTONE 2: All 13 detectors implemented
Code Review Checkpoint: Winston reviews architecture
  - Check: Strategy pattern implementation
  - Check: Parallel execution working
  - Check: Pattern quality
  - Feedback: Early issues caught + fixed

Deliverables:
  - All 13 detector classes
  - Updated DetectorFactory
  - Unit tests (>85% coverage)
```

### **Day 3 (07/03): Complete & Integrate**
```
Goals: Final implementation
Time: 1 day of work

PHASE 3A: REST API Setup (07/03, AM)
  [ ] Create REST controller /api/domains/{id}/detect-types
  [ ] CSV parsing (PapaParse or OpenCSV)
  [ ] Sampling strategy (first 50 + every 10th + random 50)
  [ ] TypeDetectionResponse DTO
  [ ] Error handling (400/422/500 responses)
  
PHASE 3B: Edge Case Handling (07/03, PM)
  [ ] Null value handling (skip in scoring)
  [ ] Empty column detection (all nulls → UNKNOWN)
  [ ] Encoding auto-detection (juniversalchardet)
  [ ] Malformed CSV handling (graceful fallback)
  [ ] Performance optimization (sampling verified <500ms)
  
MILESTONE 3: Full API working with Quinn's integration tests
  - POST /api/domains/{id}/detect-types accepts file upload
  - Returns TypeDetectionResponse with 13 types
  - All edge cases handled
  - Performance <500ms on 10K rows

Deliverables:
  - TypeDetectionController.java
  - TypeDetectionService.java
  - TypeDetectionResponse.java + DTOs
  - Integration tests passing
```

### **Day 4 (08/03): Testing & Validation**
```
Goals: Accuracy validation
Time: 1 day of work (parallel with Quinn's tests)

PHASE 4A: Run Mary's 80+ Test Samples (08/03, AM)
  [ ] Load all CSV files from Mary (04/03 delivery)
  [ ] Execute detection on each file
  [ ] Compare with ground truth labels
  [ ] Calculate accuracy by test set:
      - Easy (20 files): target 95%+
      - Medium (20 files): target 80%+
      - Hard (15 files): target 60%+
      - Robustness (15 files): 0 crashes
      - Performance (10 files): <500ms each
  
PHASE 4B: Debug & Fix (08/03, PM)
  [ ] Analyze accuracy failures
  [ ] Identify pattern issues
  [ ] Adjust regex or weights if needed
  [ ] Re-run samples
  [ ] Generate accuracy report
  
MILESTONE 4: 85%+ accuracy achieved
Collaborators: Mary (test data), Quinn (accuracy measurement)

Deliverables:
  - Accuracy report (Mary + you sign)
  - All 334+ unit tests passing
  - Integration tests passing
  - Performance validation complete
```

### **Day 5 (09/03): Code Review & Polish**
```
Goals: Production readiness
Time: 1 day of work

PHASE 5A: Code Review (09/03, AM)
Winston reviews using checklist from S2.2-TECHNICAL-ARCHITECTURE.md:
  [ ] Architecture: Strategy + Factory pattern correct?
  [ ] Code quality: No code smells, clean naming
  [ ] Performance: All <500ms, profiler output OK
  [ ] Robustness: Edge cases handled, no crashes
  [ ] Test coverage: >85% (Jacoco report)
  [ ] Javadoc: 100% documented
  [ ] API contract: TypeDetectionResponse matches spec
  
PHASE 5B: Final Polish (09/03, AM-PM)
  [ ] Complete Javadoc (100%)
  [ ] Swagger/OpenAPI documentation
  [ ] Logging (DEBUG level for troubleshooting)
  [ ] Final edge case testing
  
PHASE 5C: Sign-Off (09/03, EOD)
  [ ] Winston: Architecture approved ✅
  [ ] Quinn: Accuracy 85%+ confirmed ✅
  [ ] You: All 11 AC met ✅
  [ ] PM: Ready for production ✅
  
MILESTONE 5: S2.2 DONE ✅
  - Merge to main
  - Story marked complete
  - S2.5 can start (CSV Upload UI)

Deliverables:
  - PR ready for merge
  - Final accuracy report
  - Code review approved
  - S2.2 marked DONE
```

---

## 🧮 ALGORITHM YOU'RE IMPLEMENTING

### **Core Formula**

```
Combined_Confidence = (Header_Score × 0.4) + (Value_Score × 0.6)

WHY?
- Headers are explicit but often ambiguous ("name", "date")
- Values are more reliable ground truth
- Ratio: 40/60 balances both signals
```

### **Email Example (Step by Step)**

```
Input:
  Header: "email_address"
  Values: ["john@example.com", "jane@example.com", "robert@example.com"]

Step 1: Header Matching
  Normalize: "emailaddress" (lowercase, remove underscores)
  Patterns: ["email", "mail", "e-mail", "emailaddress"]
  Match: "emailaddress" found in patterns
  → Header Score: 98/100

Step 2: Value Pattern Matching
  Regex: ^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$
  john@example.com: ✓ MATCH
  jane@example.com: ✓ MATCH
  robert@example.com: ✓ MATCH
  → Value Score: 100/100

Step 3: Combined Score
  Confidence = (98 × 0.4) + (100 × 0.6)
             = 39.2 + 60
             = 99.2% → CONFIRMED (primary type)

Output:
  {
    primaryType: EMAIL,
    confidence: 99%,
    headerScore: 98,
    valueScore: 100,
    alternatives: []   (confidence > 80%, no alternatives needed)
  }
```

### **Ambiguous "date" Example**

```
Input:
  Header: "date" (too generic)
  Values: ["2023-01-15", "2023-01-16", "2023-01-17"]

Step 1: Header Matching
  Patterns for DateBirth: ["birthdate", "date_birth", "birth_date"]
  Patterns for DateCreated: ["created_at", "createddate", "date_created"]
  Match: NO specific patterns match "date"
  → Header Score: 15/100 (generic match only)

Step 2: Value Matching
  All values match date pattern (YYYY-MM-DD)
  → Value Score: 95/100

Step 3: Combined Scores
  DateBirth: (15 × 0.4) + (95 × 0.6) = 6 + 57 = 63%
  DateCreated: (15 × 0.4) + (95 × 0.6) = 63%
  DateEvent: (15 × 0.4) + (95 × 0.6) = 63%
  → All in UNCERTAIN range (50-80%)

Output:
  {
    primaryType: DATE_CREATED,     (first in rank)
    confidence: 63%,
    alternatives: [
      { type: DATE_BIRTH, confidence: 63% },
      { type: DATE_EVENT, confidence: 62% }
    ],
    userAction: "REVIEW - Please select intended date type"
  }
```

---

## 🚀 YOUR TOOLS & ENVIRONMENT

### **Technology Stack**
```
✅ Spring Boot 3.x (framework)
✅ Spring Data JPA (database)
✅ OpenCSV or PapaParse (CSV parsing)
✅ JUnit 5 (testing)
✅ Jacoco (coverage reporting)
✅ RestAssured (API testing)
✅ juniversalchardet (encoding detection)
```

### **Available from S2.1 (DataGeneratorService)**
```java
// Use these interfaces/classes for consistency
✅ ColumnType enum (13 types available)
✅ GeneratorFactory pattern (Strategy pattern reference)
✅ ColumnConfig entity
✅ DataSet entity
✅ DTOs: ColumnConfigDTO, GenerationRequestDTO, GenerationResponseDTO
```

### **Build & Test Commands**
```bash
# Build
mvn clean install

# Run all tests
mvn clean test

# Run specific test
mvn test -Dtest=EmailTypeDetectorTests

# Coverage report
mvn clean test jacoco:report
open target/site/jacoco/index.html

# Run Spring Boot
mvn spring-boot:run
```

---

## ✅ SUCCESS CRITERIA (11 AC to Meet)

| AC # | Criteria | How to Verify |
|------|----------|---------------|
| AC1 | Endpoint <500ms | Profiler output, mvn test Performance tests |
| AC2 | CSV parser | Validate OpenCSV/PapaParse usage |
| AC3 | 13 type detection | grep "extends ColumnTypeDetector" (13 classes) |
| AC4 | Detection method | Code review: Strategy pattern verified |
| AC5 | TypeDetectionResult DTO | Swagger docs, API response validation |
| AC6 | >85% accuracy | Quinn's accuracy report (80+ samples) |
| AC7 | Robust null handling | E2E tests with missing values |
| AC8 | Multi-encoding test | Test files in UTF-8, ISO-8859-1, Cp1252 |
| AC9 | Error handling | API returns 400/422/500 with messages |
| AC10 | Logging & debug | DEBUG level output in code |
| AC11 | Code review + Javadoc | Winston approval + 100% Javadoc coverage |

---

## 🎯 KEY DEPENDENCIES & BLOCKERS

### **Dependency on S2.1**
- ✅ DataGeneratorService (COMPLETE - S2.1 done 27/02)
- ✅ ColumnType enum available
- ✅ GeneratorFactory pattern for reference

### **Dependency on Mary's Test Data**
- ⏳ 80+ CSV samples (Mary delivers 04/03)
- Used for accuracy validation (08/03)

### **Dependency on Refinement Session**
- ✅ 03/03 session (all decisions made)
- ✅ Algorithm confirmed
- ✅ Architecture approved
- ✅ Test strategy agreed

### **Parallel with Quinn's Testing**
- 05-06/03: You implement, Quinn writes unit tests (TDD)
- 07-08/03: Quinn runs integration tests
- 09/03: Final accuracy validation

---

## 📞 YOUR SUPPORT TEAM

| Person | Role | Contact When |
|--------|------|--------------|
| **Winston** | Architect | Code design questions, performance issues |
| **Quinn** | QA | Test coverage gaps, accuracy issues |
| **Mary** | Analyst | Test data issues, accuracy analysis |
| **John** | PM | Scope/timeline questions, blockers |

---

## 🔄 DAILY STANDUP STRUCTURE

**09:00-09:15 Daily (Mon-Fri)**

**What to report:**
- Yesterday completed
- Today's plan
- Blockers (if any)

**Example:**
```
YESTERDAY (06/03):
  ✅ Implemented 5 more detectors (9/13 total)
  ✅ Code review checkpoint with Winston OK
  ✅ All unit tests passing (>85% coverage)

TODAY (07/03):
  ⏳ Complete REST API endpoint
  ⏳ Implement CSV parsing + sampling
  ⏳ Start integration testing with Quinn

BLOCKERS:
  ⚠️ None currently
```

---

## 🎬 YOU'RE READY TO START

**Status:** 🟢 **100% PREPARED**

You have:
- ✅ Complete algorithm documentation (40+ pages)
- ✅ Technical architecture (design patterns + code examples)
- ✅ Concrete CSV examples (6 test cases)
- ✅ Test templates from Quinn (JUnit 5 ready)
- ✅ Test data spec from Mary (80+ samples)
- ✅ 5-day implementation plan (detailed)
- ✅ Success criteria (11 AC verified)
- ✅ Support team ready
- ✅ Tools & environment prepared

---

## 📅 TIMELINE CONFIRMATION

| Date | Milestone | Target | Status |
|------|-----------|--------|--------|
| **03/03** | Refinement Session | Decisions made | ✅ Done |
| **04/03** | Mary delivers test data | 80+ samples + ground truth | ⏳ Today |
| **05/03** | Day 1: Foundation | 3 detectors + factory | ⏳ Your start |
| **06/03** | Day 2: Expand | 13 detectors complete | ⏳ Code review checkpoint |
| **07/03** | Day 3: Integrate | REST API working | ⏳ Full API functional |
| **08/03** | Day 4: Validate | 85%+ accuracy measured | ⏳ Quinn validates |
| **09/03** | Day 5: Polish | Code review approved | ✅ **S2.2 DONE** |

---

## 💪 LET'S BUILD THIS

> "Amelia, you have everything needed to implement S2.2 successfully:
>
> - Algorithm is crystal clear (40+ page reference)
> - Architecture is proven (Strategy + Factory pattern from S2.1)
> - Team is ready (Quinn testing, Mary data, Winston reviewing)
> - Success is measurable (85%+ accuracy on 80+ samples)
> - Timeline is realistic (5 days, 8 pts, clear phases)
>
> You know what to build. You know how to build it. You have all the support you need.
>
> Time to execute. 🚀"

---

**Document:** AMELIA-IMPLEMENTATION-ACTIVATION.md  
**Location:** `sprint-2-started/s2.2-type-detection/`  
**Date:** 28 février 2026 @ 20:35  
**Status:** ✅ **READY TO CODE**

🎯 **Next Action:** 05/03 09:00 - Start Day 1 (Foundation phase)

