---
audit_story: "1.3"
audit_title: "Design Audit - Domain REST Controller"
audit_date: "27 février 2026"
audit_by: "Amelia (Developer)"
review_required_from: ["Winston (Architect)"]
status: "PENDING_ARCHITECT_REVIEW"
---

# DESIGN AUDIT REPORT - Story 1.3: Domain REST Controller

**Audit Date:** 27 février 2026  
**Auditor:** Amelia (Developer Agent)  
**Design File:** `1-3-domain-rest-controller.md`  
**Status:** 📋 PENDING ARCHITECT REVIEW  
**Severity Levels:** 🟢 OK | 🟡 MINOR | 🟠 IMPORTANT | 🔴 CRITICAL

---

## Executive Summary

✅ **OVERALL ASSESSMENT: Design is Well-Structured and Ready for Implementation with Minor Improvements**

The design follows established patterns from S1.2, includes comprehensive test specifications, and maps all 7 acceptance criteria to executable tasks. Key strengths: correct HTTP semantics, centralized error handling, detailed testing strategy. Areas requiring architect input: Service layer pattern alignment, DTO/Entity validation separation, concurrency handling with @Version field, pagination strategy.

**Recommendation:** Approve with 3 Minor Clarifications (see section 5)

---

## 1. Technical Strengths ✅

### 1.1 REST API Contract Clarity - 🟢 OK
**Evidence:** Lines 150-180 specify response formats with JSON examples
- ✅ Success format: `{data: {...}, message: "..."}`
- ✅ Error format: `{error: "...", timestamp: "...", status: ..., path: "..."}`
- ✅ HTTP status codes table (200/201/204/400/404/409/500) correctly mapped
- **Verification:** Matches conventions from `architecture.md`

### 1.2 Endpoint Completeness & RESTful Design - 🟢 OK
**Evidence:** Task 4 specifies 5 endpoints covering full CRUD
- POST /api/domains → 201 Created with Location header (correct)
- GET /api/domains → 200 OK with list (correct)
- GET /api/domains/{id} → 200 OK or 404 (correct)
- PUT /api/domains/{id} → 200 OK with updated entity (correct)
- DELETE /api/domains/{id} → 204 No Content (soft delete - correct choice)
- **Verification:** All endpoints follow REST/HTTP conventions

### 1.3 DTO Separation Pattern - 🟢 OK
**Evidence:** Task 1 creates two separate DTO classes
- `DomainCreateDTO` for request input (name, description)
- `DomainResponseDTO` for response output (id, name, description, timestamps)
- **Benefit:** Decouples API contract from internal entity, prevents accidental exposure of fields
- **Verification:** Aligns with `architecture.md` DTO pattern decision

### 1.4 Validation Strategy - 🟢 OK
**Evidence:** Task 1 & 3 include Bean Validation annotations
- DTOs: `@NotBlank` on name, `@Size(max=2000)` on description
- Controller: `@Valid` annotation triggers validation (Task 4)
- Error handler: MethodArgumentNotValidException caught (Task 3)
- **Verification:** Matches S1.2 entity validation approach

### 1.5 Error Handling Architecture - 🟢 OK
**Evidence:** Task 3 implements GlobalExceptionHandler with @ControllerAdvice
- ✅ Centralized: Single class handles all exceptions
- ✅ Handlers for: MethodArgumentNotValidException (400), DataIntegrityViolationException (409), EntityNotFoundException (404), Generic Exception (500)
- ✅ Standardized responses: All use ApiErrorResponse format
- ✅ Logging: SLF4J integration mentioned
- **Verification:** Same pattern used in S1.1 JwtUtil context

### 1.6 Test Coverage Depth - 🟢 OK
**Evidence:** Task 6 specifies 12+ RestAssured tests organized by scenario
- **CREATE (4 tests):** valid, missing name, too long, duplicate
- **READ (3 tests):** list all, get by ID, not found
- **UPDATE (3 tests):** valid, invalid, not found
- **DELETE (2 tests):** delete existing, not found
- **PLUS:** Soft delete verification (deleted domains excluded from list)
- **Verification:** >80% coverage target achievable with 12 tests

### 1.7 Soft Delete Awareness - 🟢 OK
**Evidence:** Task 4 implementation and tests verify soft-delete pattern
- All GET queries use `findByDeletedAtIsNull()` → excludes deleted domains
- DELETE operation calls `domain.softDelete()` (from S1.2)
- Response includes `deletedAt` field (null if active, timestamp if deleted)
- Test: Verify deleted domain excluded from GET list (Task 6, test 12)
- **Verification:** Consistent with S1.2 soft-delete architecture

### 1.8 API Documentation Automation - 🟢 OK
**Evidence:** Task 5 configures Springdoc-openapi
- ✅ Dependency: springdoc-openapi-starter-webmvc-ui:2.0.4
- ✅ Endpoints accessible: `/swagger-ui.html` (UI) + `/v3/api-docs` (JSON)
- ✅ Auto-generation: DTOs and endpoints automatically included
- **Verification:** Reduces manual documentation overhead

### 1.9 Dependency Management - 🟢 OK
**Evidence:** Task 7 lists dependencies to add
- `spring-boot-starter-web` (REST support)
- `mapstruct:1.5.5.Final` (DTO mapping, type-safe)
- `springdoc-openapi-starter-webmvc-ui:2.0.4` (Swagger)
- `rest-assured` (test framework)
- **Verification:** All compatible with Spring Boot 3.2.0 parent

---

## 2. Minor Issues Requiring Fixes 🟡

### 2.1 Location Header Malformed - 🟡 MINOR
**Issue:** Task 4 (POST endpoint) shows:
```java
.header("Location", "/api/domains/{id}")
```
**Problem:** Should use full URL or use Spring helper to construct proper absolute URI

**Fix Required:**
```java
return ResponseEntity
    .created(ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(domain.getId())
        .toUri())
    .body(ApiResponse.success(dto, "Domain created successfully"));
```
**Severity:** 🟡 MINOR - Current approach works but not RFC 7231 compliant

**Evidence:** RFC 7231 Section 7.1.2 requires Location to be absolute URI

---

### 2.2 Duplicate Name Check Logic Incomplete - 🟡 MINOR
**Issue:** Task 4 PUT implementation says:
> "if (new name exists in active domains) → throw DataIntegrityViolationException"

**Problem:** If client updates domain A from "Finance" to "Finance" (same name), will trigger false positive 409

**Fix Required:** Add check:
```java
if (!domain.getName().equals(dto.getName()) && 
    existsByNameAndDeletedAtIsNull(dto.getName())) {
    throw new DataIntegrityViolationException(...);
}
```

**Severity:** 🟡 MINOR - Edge case but affects user experience

---

### 2.3 DateTime Format Specification - 🟡 MINOR
**Issue:** Task 1 DTOs specify `@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")`

**Problem:** S1.2 `Domain` entity has LocalDateTime fields without format specification. DTOs may serialize differently than when returned directly.

**Fix Required:** Either:
- Option A: Add same `@JsonFormat` to Domain entity timestamps (S1.2 change)
- Option B: Document that DTOs enforce format, ensure consistent handling

**Severity:** 🟡 MINOR - Can cause timestamp mismatch in integration tests/frontend

---

### 2.4 Tests Missing DTO→Entity Mapping Verification - 🟡 MINOR
**Issue:** Task 6 tests verify response status codes and messages, but not field-by-field mapping

**Current:** Test checks `response.name == "Finance"` (message level)  
**Should Also Check:** MapStruct mapped the fields correctly

**Suggested Test Addition:**
```java
@Test
void testDTOMappingCorrectness() {
    DomainCreateDTO dto = new DomainCreateDTO("Finance", "Financial data");
    post_success = valid post...
    DomainResponseDTO response = response.as(DomainResponseDTO.class);
    
    assertThat(response.name).isEqualTo(dto.name);
    assertThat(response.description).isEqualTo(dto.description);
    assertThat(response.id).isNotNull();
    assertThat(response.createdAt).isNotNull();
}
```

**Severity:** 🟡 MINOR - Good to have, not critical

---

## 3. Important Questions for Architect 🟠

### 3.1 Service Layer Pattern - 🟠 IMPORTANT
**Question:** Should design include `DomainService` layer?

**Context:**
- `architecture.md` shows: Controller → Service → Repository pattern
- Current S1.3 design: Controller → Repository directly
- S1.2 tests only Repository layer (@DataJpaTest), no service layer

**Analysis:**
- ✅ FOR direct Controller → Repository: Simpler for MVP, Domain logic is straightforward (CRUD only)
- ❌ AGAINST: Future stories (S1.5+) may need shared business logic (e.g., validation, authorization)
- 🤔 DECIDE: Add service layer now or as technical debt in S1.5?

**Recommendation:** Request Winston input on whether to:
1. Keep current Controller → Repository (simpler)
2. Add DomainService layer now (future-proof)
3. Add Service layer marker for future (comment-based)

**Impact:** Medium - Affects test structure and code organization

---

### 3.2 DTO Validation Duplication - 🟠 IMPORTANT
**Question:** How to handle validation in both DTOs and Entity?

**Current Design:**
- Entity (S1.2): `@NotBlank` on name, `@Size(max=2000)` on description
- DTO (S1.3): Same validations

**Problem:** If Entity validation changes (e.g., name min length 3), must update DTO too

**Options:**
- Option A: Keep both (current) - explicit but maintenance burden
- Option B: Validate only in DTOs, entity validates automatically (requires test)
- Option C: Create shared validation group/interface

**Recommendation:** Need architect input on strategy - is duplication acceptable for MVP?

**Impact:** Low now, Medium if validation rules expand

---

### 3.3 Concurrency & Version Field Usage - 🟠 IMPORTANT
**Question:** Should PUT endpoint include `@Version` field in request?

**Context:**
- S1.2 Domain entity has `@Version` field for optimistic locking
- Current S1.3 PUT doesn't expose version in requests
- Client updates domain but doesn't know current version

**Scenario:** 
- Client A fetches domain (version=1)
- Client B updates domain (version increments to 2)
- Client A tries to update → version mismatch, but client doesn't know

**Options:**
- Option A: Include version in DomainResponseDTO, require in DomainCreateDTO for PUT
- Option B: Hide version (current), rely on database exception handling
- Option C: Use ETags header instead of version field

**Recommendation:** Need architect input:
- If POP architecture show version is client concern → Option A
- If stateless REST preferred → Option B is fine
- EPTag approach: need additional library

**Impact:** High - Affects API contract and concurrency safety

---

### 3.4 Pagination Strategy - 🟠 IMPORTANT
**Question:** Should GET /api/domains support pagination?

**Current Design:** Returns `findByDeletedAtIsNull()` → all active domains

**Problem:** 
- For 10,000 domains, entire list serialized in one response
- No filtering, searching, or limit

**Options:**
- Option A: Keep current (MVP-simple)
- Option B: Add Pageable parameter with Spring Data pagination
- Option C: Add optional limit/offset query parameters

**Expected Volume:** Not specified in requirements, but movkfact is data-generation tool

**Recommendation:** Request Winston guidance:
- For MVP scope, keep simple (Option A)
- For future S1.5+ (frontend), may need Option B/C

**Impact:** Medium - Affects API scale-ability

---

### 3.5 Soft Delete Query Efficiency - 🟠 IMPORTANT
**Question:** Are indexes sufficient for soft-delete queries?

**Current S1.2 Indexes:**
- `idx_domain_name` on name column
- `idx_domain_deleted_at` on deleted_at column

**Current S1.3 Queries:**
- `findByDeletedAtIsNull()` → uses deleted_at index ✓
- `findByNameIgnoreCaseAndDeletedAtIsNull()` → uses both params, but compound index missing?

**Concern:** Query `WHERE name LIKE '%X%' AND deleted_at IS NULL` may not use index efficiently

**Recommendation:** Request architect confirm:
- Is `idx_domain_deleted_at` sufficient for filtering?
- Need compound index `(deleted_at, name)` for soft-delete + search queries?

**Impact:** Medium - Performance concern for future scaling

---

### 3.6 DELETE Endpoint Idempotency - 🟠 IMPORTANT
**Question:** Should DELETE be idempotent?

**Current Design:** DELETE non-existent domain → 404 Not Found

**Problem:** 
- If client deletes domain, loses network, retries → 404 on retry
- Not idempotent (repeated request changes response)

**REST Best Practice:** DELETE should be idempotent (safe to retry)

**Options:**
- Option A: Current (404 on not-found) - clear but not idempotent
- Option B: Return 204 always (idempotent but less feedback)
- Option C: Check if already deleted, return 204 anyway

**Recommendation:** Need architect clarification:
- Is idempotency required for this API?
- Common practice for soft-delete APIs?

**Impact:** Low - Usually acceptable, but matters for distributed scenarios

---

## 4. Coherence with S1.2 ✅

| Aspect | S1.2 | S1.3 | Alignment |
|--------|------|------|-----------|
| Soft Delete Pattern | findByDeletedAtIsNull() | Reused in GET methods | ✅ PERFECT |
| Validation Approach | Bean Validation annotations | Replicated in DTOs | ✅ CONSISTENT |
| Timestamps | @PrePersist/@PreUpdate | Passed through DTOs | ✅ PRESERVED |
| Package Structure | .entity, .repository | .controller, .dto, .exception | ✅ LOGICAL |
| Exception Handling | Not specified | GlobalExceptionHandler NEW | ✅ GOOD ADDITION |
| Testing Framework | @DataJpaTest (repo slice) | @SpringBootTest (integration) | ✅ APPROPRIATE |
| Naming Conventions | Domain (entity), snake_case DB | DomainController, /api/domains | ✅ FOLLOWS CONVENTIONS |

---

## 5. Alignment with architecture.md ✅

### Conventions Verified
- ✅ **API endpoint naming:** `/api/domains` matches `kebab-case` pattern guidance (though "domains" is plural, consistent)
- ✅ **DTO naming:** `DomainCreateDTO`, `DomainResponseDTO` follow PascalCase + "DTO"
- ✅ **Controller naming:** `DomainController` follows PascalCase + "Controller"
- ✅ **Package structure:** `.controller`, `.dto`, `.exception` consistent with backend layout
- ✅ **Response format:** `{data: {...}, message: "..."}` matches "Réponses succès" pattern
- ✅ **Error format:** `{error: "...", status: ..., timestamp: ...}` matches "Erreurs" pattern
- ✅ **DateTime format:** ISO 8601 with Z timezone (architecture.md specifies 2026-02-26T10:00:00Z)
- ✅ **HTTP status codes:** Architecture.md identifies standards 200/201/400/404/500, S1.3 adds 204/409

### Architectural Patterns Implemented
- ✅ **REST Pattern:** Standard HTTP methods + resources
- ✅ **DTO Pattern:** Input/output DTOs separate from entity
- ✅ **Centralized Error Handling:** GlobalExceptionHandler (@ControllerAdvice)
- ✅ **Bean Validation:** @Valid on request bodies
- ✅ **Soft Delete Awareness:** All queries exclude deleted domains

---

## 6. Implementation Readiness Assessment 📋

| Criterion | Status | Notes |
|-----------|--------|-------|
| Requirements Clarity | ✅ 100% | All 7 ACs mapped to 9 tasks |
| Acceptance Criteria | ✅ 7/7 | All crisp and testable |
| Task Sequencing | ✅ ORDERED | Tasks 1-9 in logical order |
| Test Strategy | ✅ >12 tests | Covers success + all error paths |
| Dependencies Listed | ✅ 4 deps | spring-boot-starter-web, mapstruct, springdoc-openapi, rest-assured |
| API Contract Defined | ✅ YES | JSON examples provided |
| Error Handling Plan | ✅ YES | 4 exception handlers specified |
| Documentation Plan | ✅ YES | Swagger auto-generated |
| Coherence with S1.2 | ✅ ALIGNED | Reuses patterns + repositories |
| Code Estimate | ✅ 5pts | ~400 lines code + 300 lines tests (reasonable) |
| Risk Identification | ⚠️ PARTIAL | Missing concurrency, pagination, service layer decisions |

**Readiness Level:** 🟢 **READY FOR IMPLEMENTATION** (with architect clarification on 3.1-3.6)

---

## 7. Risk Registry

### Risks Identified

| # | Risk | Probability | Impact | Mitigation | Owner |
|---|------|-------------|--------|-----------|-------|
| R1 | Service layer pattern not aligned with architecture | Medium | Medium | Seek architect confirmation (3.1) | Winston |
| R2 | Validation duplication between Entity → DTO | Low | Low | Document strategy (3.2) | Winston |
| R3 | Concurrency issue without version field in request | Medium | High | Architect decides on OptLock strategy (3.3) | Winston |
| R4 | API doesn't scale for large domain counts | Medium | Medium | Plan pagination for S1.5 (3.4) | PM |
| R5 | Soft-delete query performance degrades | Low | Medium | Verify index strategy (3.5) | Winston |
| R6 | DELETE endpoint not idempotent | Low | Low | Decide on idempotency (3.6) | Winston |
| R7 | Location header RFC compliance | Low | Low | Fix servlet URI builder (2.1) | Amelia |
| R8 | PUT duplicate name edge case | Low | Low | Add same-name check (2.2) | Amelia |

---

## 8. Recommendations

### APPROVE with Conditional Clarifications

**✅ Proceed to Implementation IF:**

1. ✅ Accept Location header fix (2.1) — Amelia will implement
2. ✅ Accept duplicate name check fix (2.2) — Amelia will implement
3. 🤔 Architect clarifies on:
   - Service layer pattern (3.1): Service layer yes/no?
   - Concurrency handling (3.3): Include version in request?
   - Pagination scope (3.4): Now or S1.5?

**Additional Notes:**
- DateTime format (2.3): Minor but verify at implementation time
- Soft-delete indexes (3.5): Verify during test phase
- Idempotency (3.6): Current approach acceptable, document for future

---

## 9. Next Steps

### Phase 1: Architect Review (PENDING) 🔴
- [ ] Winston reviews audit findings (items 3.1-3.6)
- [ ] Winston provides clarifications
- [ ] Document decisions in story file

### Phase 2: Amelia Fixes Minor Issues ✏️
- [ ] Fix Location header construction (2.1)
- [ ] Add duplicate name same-value check (2.2)
- [ ] Verify DateTime format at implementation

### Phase 3: Implementation Ready 🚀
- [ ] Execute all 9 tasks
- [ ] Run 12+ RestAssured tests
- [ ] Verify >80% coverage
- [ ] Full test suite passes (DomainControllerTest + S1.2 regression)

---

## Appendix: Design Metrics

| Metric | Value | Target | Status |
|--------|-------|--------|--------|
| Endpoints | 5 (POST, GET, GET/{id}, PUT, DELETE) | ≥ 5 | ✅ MET |
| DTOs | 2 (Create, Response) | ≥ 2 | ✅ MET |
| Test Scenarios | 12+ | > 12 | ✅ MET |
| Acceptance Criteria | 7 | 7 | ✅ MET |
| Tasks | 9 | ≥ 8 | ✅ MET |
| Subtasks | 50+ | ≥ 20 | ✅ MET |
| API Documentation | Auto-Swagger | ✅ | ✅ MET |
| Error Response Format | Standardized | ✅ | ✅ MET |
| HTTP Status Codes | 6 types (200/201/204/400/404/409/500) | RESTful | ✅ MET |
| Soft Delete Awareness | Present in all queries | ✅ | ✅ MET |

---

**Audit Completed:** 27 février 2026  
**Status:** 📋 AWAITING ARCHITECT REVIEW  
**Next Review Meeting:** [To be scheduled after Winston confirms 3.1-3.6]
