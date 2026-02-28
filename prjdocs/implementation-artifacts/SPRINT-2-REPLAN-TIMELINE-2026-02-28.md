---
title: "PLAN REPLANIFICATION SPRINT 2 - 2026-02-28"
date: 2026-02-28
status: "READY FOR SM REVIEW"
recommendation: "Option B - Prioritized (44 pts S2, 10 pts deferred to S3)"
---

# Plan Replanification Sprint 2 - Restructuration + Capacity Adjustment

**Context:** Sprint 2 restructuration identifie 54 pts vs 34 originally planned  
**Solution:** Option B - Prioritized approach: 44 pts Sprint 2, 10 pts defer to S3  
**Timeline:** Implementation before Sprint 2 Start (17/03/2026)

---

## 📋 CHANGEMENTS SPRINT 2

### **Scope: 34 pts → 44 pts (+10 pts net, 30% increase)**

#### **KEEP in Sprint 2 (35 pts):**
```
BACKEND:
✅ S2.1: DataGeneratorService (5 pts) - DONE
📋 S2.2: Type Detection (8 pts) - CRITICAL DEPENDENCY
✅ S2.3: Data Gen REST API (8 pts) - DONE
📋 S2.4: JSON Export Engine (5 pts) - NEW, builds on S2.3
  Subtotal: 26 pts

FRONTEND:
📋 S2.5: CSV Upload UI (6 pts) - NEW, depends S2.2
📋 S2.6: Data Configuration UI (6 pts) - NEW
📋 S2.7: Data Viewer UI (6 pts) - NEW - was S2.6
  Subtotal: 18 pts

TOTAL: 44 pts (manageable)
```

#### **DEFER to Sprint 3 (10 pts):**
```yaml
BACKEND:
  📦 S2.8: Activity Tracking (5 pts)
     Reason: Non-blocking, can integrate post-MVP
     Priority: Medium (enhances UX, not critical)
     Fits in: S3 where other "advanced features" live
     
  📦 S2.9: Dataset Versioning (5 pts)
     Reason: Non-blocking, enterprise feature
     Priority: Medium (nice-to-have for data management)
     Fits in: S3 where versioning/history features live
     
TOTAL DEFERRED: 10 pts → moves to S3
```

### **Why This Balance:**

| Metric | Before | After | Reason |
|--------|--------|-------|--------|
| Amelia backend load | 23 pts | 18 pts | Remove S2.8 + S2.9 (10 pts deferred) |
| Sally frontend load | 18 pts | 18 pts | No change |
| Critical path | S2.2→S2.3→S2.5 | S2.2→S2.3→S2.5 | Same |
| Confidence | 50% | 85% | Better capacity match |
| Achievable? | Risky | Comfortable | Clear yes |

---

## 📅 IMPLEMENTATION TIMELINE

### **PHASE 1: Sprint 2 Replan (Today - 28/02 → 02/03)**

**Day 1-2 (28/02-01/03): PM + SM Decision**
- [ ] PM (John): Review Capacity Analysis, decide on Option B
- [ ] SM (Bob): Prepare replan communication
- [ ] Dev Lead (Amelia): Confirm velocity estimates
- [ ] UX Lead (Sally): Confirm component velocity estimates

**Day 3 (02/03): Update Artifacts**
- [ ] [ ] Update `sprint-status.yaml`: Move S2.8, S2.9 to sprint-3 status
- [ ] [ ] Update `backlog.md`: Reflect 44 pts for S2, 38 pts for S3
- [ ] [ ] Create `SPRINT-2-REPLAN-2026-02-28.md`: This communication
- [ ] [ ] Notify team: New S2 scope confirmed

---

### **PHASE 2: Pre-Kickoff Refinement (03/03 → 16/03)**

**Refinement Session 1: S2.2 Type Detection (8 pts)**
- **Date:** Week of 03/03
- **Duration:** 2-3 hours
- **Participants:** Amelia (dev), Mary (analyst), Winston (architect)
- **Goals:**
  - Deep dive into CSV type detection algorithm
  - Edge cases, encoding handling, confidence scoring
  - Integration with Faker patterns from S2.1
  - Finalize AC acceptance criteria
  - Risk assessment & mitigation

**Refinement Session 2: S2.5-S2.7 Dependencies (18 pts)**
- **Date:** Week of 03/03 (after S2.2 session)
- **Duration:** 1-2 hours
- **Participants:** Sally (frontend), Winston (architect), Amelia (backend liaison)
- **Goals:**
  - Frontend flow: S2.5 (upload) → S2.6 (config) → S2.7 (viewer)
  - Context API state management across components
  - API contracts from S2.2, S2.3, S2.4
  - Component composition, reusability
  - MUI theme consistency

**Refinement Session 3: S2.4 JSON Export (5 pts)**
- **Date:** Week of 10/03
- **Duration:** 1 hour
- **Participants:** Amelia (dev), Winston (architect)
- **Goals:**
  - Export filtering & formatting options
  - Performance expectations (10K rows < 500ms)
  - Caching strategy if needed
  - Integration with S2.7 download buttons

---

### **PHASE 3: Sprint 2 Kickoff (17/03) - Updated Agenda**

**Sprint 2 Refined Scope: 44 pts**

**Kickoff Meeting Agenda (1.5 hours):**

1. **Welcome & Context (10 min)**
   - Sprint theme: "Core MVP + Data Export"
   - Scope: 44 pts vs originally 34 pts (explanation)
   - Team composition: Amelia (backend), Sally (frontend), QA support

2. **Story Breakdown (30 min)**
   - Story cards: S2.1✅ + S2.2→S2.7 (6 stories in sprint)
   - Dependencies visualization
   - Critical path: S2.2 (8 pts) → must complete by mid-sprint
   - Parallel opportunities: S2.4 + S2.8's work done in parallel

3. **Velocity & Metrics (15 min)**
   - Target velocity: 44 pts / 10 work days = 4.4 pts/day
   - Historical velocity: Amelia 5-8 pts/day → comfortable
   - Sally new, estimate 4-6 pts/day for UI components → comfortable
   - No parallel critical, focus on serial completion

4. **Risk Management (20 min)**
   - Key risks: S2.2 complexity, Sally ramp-up time
   - Mitigation: Daily standup focus on blockers
   - Escalation path: Report to Winston (architect) for design help

5. **Definition of Done (10 min)**
   - All tests written + passing
   - Code review approved (peer review mandatory)
   - Swagger/API docs complete
   - No test regressions
   - Performance metrics met

6. **Communication Plan (5 min)**
   - Daily standup: 10:00 (all)
   - Mid-sprint check-in: 24/03 (velocity assessment)
   - Code review ceremony: Ad-hoc (no batch reviews)

---

### **PHASE 4: Sprint 2 Execution (17/03 → 30/03)**

**Week 1 (17-21/03): S2.2 Focus**
```
Mon 17/03: Kickoff
  - All team review stories
  - Amelia starts S2.2 (critical path)
  - Sally starts S2.5 preliminary work (blocked on S2.2)

Tue-Fri 18-21/03: Development
  - Amelia: Deep dive S2.2 type detection
  - Sally: Study S2.2 API contract, prepare S2.5 structure
  - Daily standup: 10:00
  - Target: S2.2 50% complete by Friday

Checkpoint 1 (Fri 21/03):
  - S2.2 expected: ~6 pts done (need 2 more in week 2)
  - No blockers reported yet (✅ good sign)
```

**Week 2 (24-28/03): S2.2 Finish + Main Flow**
```
Mon 24/03: S2.2 Completion + Code Review
  - Amelia: Finalize S2.2 type detection
  - Code review: Winston + (peer dev if available)
  - S2.2 tests passing, documentation complete
  - Sally: S2.5 implementation starts

Tue-Wed 25-26/03: S2.4 + S2.5/S2.6 Parallel
  - Amelia: S2.4 JSON Export (build on S2.3)
  - Sally: S2.5 + S2.6 UI components
  - Daily standup: 10:00
  - Integration: Ensure API contracts match

Checkpoint 2 (Wed 26/03): MID-SPRINT ASSESSMENT
  - S2.2: ✅ DONE (8 pts)
  - S2.4: 50% done (2-3 pts done)
  - S2.5: 50% done (3 pts done) - depends S2.2 completion
  - S2.6: 30% done (2 pts done)
  
  Velocity so far: 15-17 pts done in 6 days = 2.5-2.8 pts/day
  Pace: ON TRACK (need 27 pts in remaining 4 days = 6.75 pts/day doable)

Thu 27/03: Continued Development
  - Amelia: S2.4 completion + start S2.7 support
  - Sally: S2.6 + S2.7 coordination
  - Testing phase

Fri 28/03: Finalization & Testing
  - All features to code review
  - Bug fixes
  - Documentation review
```

**Week 3 (31/03): Final Polish**
```
Mon 31/03 (half day only - likely): Final testing
  - Integration testing: S2.5→S2.6→S2.7 flow
  - Performance testing: S2.2 accuracy, S2.4 export speed
  - Regression testing: No S2.1/S2.3 breakage
  - Documentation final review

Status: SPRINT COMPLETE (44 pts) ✅
```

---

## 📊 REVISED METRICS & TRACKING

### **Sprint 2 Metrics (Target):**

| Metric | Target | Actual (to update) |
|--------|--------|----------|
| Sprint Scope | 44 pts | - |
| Velocity | 4.4 pts/day | - |
| Burndown | Linear to 44 pts | - |
| Code Review Cycle | < 4 hours | - |
| Test Coverage | > 80% | - |
| Zero Regressions | Yes | - |
| Sprint Goal Met %| 100% | - |

### **S2.4-S2.7 Delivery Dates (Expected):**

| Story | Est. Complete | Confidence |
|-------|---|---|
| S2.2 Type Detection | 24/03 | 90% |
| S2.4 JSON Export | 26/03 | 85% |
| S2.5 CSV Upload UI | 28/03 | 80% |
| S2.6 Config UI | 29/03 | 85% |
| S2.7 Viewer UI | 30/03 | 85% |

---

## 📦 MOVED TO SPRINT 3 (10 pts)

### **S2.8: Activity Tracking (5 pts)**
- **Reason:** Non-blocking feature, can be added post-MVP
- **Sprint 3 Timing:** Can start immediately after Sprint 2 (best fit)
- **Why S3, not later:** Tracks S2 data, should be close chronologically
- **Dependency:** Depends on S2.3 DataSet entity (will be proven)

### **S2.9: Dataset Versioning (5 pts)**
- **Reason:** Enterprise feature, non-critical for MVP
- **Sprint 3 Timing:** Fits with S3 "Advanced Features"
- **Why S3, not later:** Users won't immediately need reset functionality
- **Complementary:** Works well with S2.8 activity tracking already done

**Benefits of Deferral:**
- ✅ Gives Amelia breathing room in S2
- ✅ Allows S3 to focus on "nice-to-have" features
- ✅ No user-facing impact (internal features)
- ✅ Provides S3 with clear starting tasks

---

## ⚠️ RISKS & CONTINGENCIES

### **Risk 1: S2.2 Complexity (High Impact)**
- **Risk:** S2.2 type detection takes longer than 8 pts
- **Mitigation:** 
  - Pre-refinement deep dive (week of 03/03)
  - Daily standup check-ins on progress
  - Escalation: Winston (architect) on algorithm questions
- **Contingency:** If S2.2 not done by 24/03, defer S2.5 to S3

### **Risk 2: Sally Ramp-up (Medium Impact)**
- **Risk:** React components take longer than estimate (new contributor)
- **Mitigation:**
  - Pair programming with existing frontend dev (if available)
  - Reusable component patterns from S1
  - Design review from Winston before coding
- **Contingency:** S2.7 can be reduced scope (basic viewer, advanced features later)

### **Risk 3: Integration Issues (Medium Impact)**
- **Risk:** APIs from backend don't match frontend expectations
- **Mitigation:**
  - API contracts finalized in refinement sessions
  - Contract testing in S2.4 implementation
  - Mock APIs early (fake data)
- **Contingency:** Extend S2 by 2-3 days if needed

### **Risk 4: Test Coverage (Low Impact)**
- **Risk:** Can't reach 80% coverage target
- **Mitigation:**
  - TDD approach from day 1
  - Coverage checking in CI/CD
  - Code review gate: No merge without tests
- **Contingency:** Reduce scope of feature (e.g., skip advanced filtering)

---

## ✅ APPROVAL & SIGN-OFF

### **Required Approvals:**

- [ ] **PM (John):** Scope change from 34→44 pts approved?
- [ ] **Dev Lead (Amelia):** Capacity estimate confirmed? (18 pts backend feasible?)
- [ ] **UX Lead (Sally):** React velocity estimate confirmed? (18 pts frontend feasible?)
- [ ] **SM (Bob):** Timeline & execution plan acceptable?
- [ ] **Architect (Winston):** Technical approach for S2.4 compatible?

### **Sign-off by:** [DATE to be filled]

---

## 📝 FOLLOW-UP ACTIONS

### **Before Kickoff (16/03):**
1. [ ] All approvals collected
2. [ ] Refinement sessions completed
3. [ ] Sprint board setup with 7 stories (S2.1✅ + S2.2-S2.7)
4. [ ] Velocity history documented
5. [ ] Contingency plans shared with team

### **During Sprint (17/03-30/03):**
1. [ ] Daily standups focus on blockers
2. [ ] Mid-sprint check (24/03) validate pace
3. [ ] Code reviews approved same-day if possible
4. [ ] Performance metrics tracked
5. [ ] Communication on progress to stakeholders

### **After Sprint (31/03+):**
1. [ ] Sprint retrospective (lessons learned)
2. [ ] Velocity calculation (actual vs estimates)
3. [ ] Adjust S3 planning based on S2 learnings
4. [ ] S2.8 + S2.9 brought into S3 planning

---

**Status:** READY FOR REVIEW  
**Prepared by:** Copilot (Planning)  
**Approval needed from:** PM, SM, Leads  
**Target approval date:** 02/03/2026

