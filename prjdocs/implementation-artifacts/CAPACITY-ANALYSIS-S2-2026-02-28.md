---
title: "ÉVALUATION CAPACITÉ SPRINT 2 - 2026-02-28"
date: 2026-02-28
status: "DRAFT - Awaiting Team Validation"
context: "Suite à restructuration Sprint 2 (54 pts vs 34 pts planifiés)"
---

# Évaluation Capacité Sprint 2 - Restructuration Impact

**Date:** 28 février 2026  
**Contexte:** Restructuration S2 ajoute 3 stories (20 pts) pour combler exigences PRD  
**Objectif:** Valider si 54 pts réalisable ou nécessite replan

---

## 📊 ANALYSE CAPACITÉ ACTUELLE

### **Par Personne:**

#### **Amelia (Backend Developer)**
```
Points actuels (Sprint 2):
- S2.1: DataGeneratorService (5 pts) ✅ DONE
- S2.2: Type Detection (8 pts) - ready, should start ASAP
- S2.3: Data Gen REST API (8 pts) ✅ DONE
- S2.4: JSON Export Engine (5 pts) ← NEW
- S2.8: Activity Tracking (5 pts) ← NEW
- S2.9: Dataset Versioning (5 pts) ← NEW

TOTAL: 36 pts (in Sprint 2)
COMPLETED: 13 pts
REMAINING: 23 pts
```

**Velocity Estimate:** 
- S2.1 velocity: 5 pts in 1 day (27/02-28/02) = 5 pts/day
- Historical: ~5-8 pts/day for single developer
- Sprint 2 Duration: 17/03 - 30/03 = 14 jours calendar ≈ 10 jours "work days"
- Capacity theoretically: 50-80 pts/10 jours = 5-8 pts/jour

**Calcul pour 23 pts restants:**
- Optimistic (8 pts/jour): 23÷8 = 2.9 jours → FEASIBLE ✅
- Pessimistic (5 pts/jour): 23÷5 = 4.6 jours → TIGHT ⚠️
- With holidays/interruptions: 5-6 days → STRETCHED 🚨

---

#### **Sally (Frontend/UX Designer)**
```
Points actuels (Sprint 2):
- S2.5: CSV Upload UI (6 pts)
- S2.6: Data Configuration UI (6 pts)
- S2.7: Data Viewer UI (6 pts)

TOTAL: 18 pts
COMPLETED: 0 pts
REMAINING: 18 pts
```

**Velocity Estimate:**
- No historical data for Sally in movkfact (new contributor)
- Estimate: ~4-6 pts/day for React components
- Sprint 2: 10 jours work = 40-60 pts capacity
- **FEASIBLE:** 18 pts well within capacity ✅

---

### **Sprint Total:**

| Ressource | Total (pts) | Done | Remaining | Capacity | Status |
|-----------|-------------|------|-----------|----------|--------|
| Amelia | 36 | 13 | 23 | 50-80 | ⚠️ TIGHT |
| Sally | 18 | 0 | 18 | 40-60 | ✅ OK |
| **TOTAL** | **54** | **13** | **41** | **90-140** | **🚨 RISKY** |

**Risk Level:** 🚨 **HIGH** if 23 Amelia pts need all to be done  
**Confidence:** 60% (depends on S2.2 velocity, interruptions)

---

## 🔍 FACTEURS DE RISQUE

### **Blockers pour Amelia:**

1. **S2.2 Type Detection (8 pts)**
   - **Difficulty:** Medium-High (NLP-like pattern matching)
   - **Risk:** Need to understand Faker patterns, edge cases
   - **Estimate:** Could be 8-10 pts if complex CSV parsing needed
   - **Mitigation:** Start immediately, get clarification on requirements

2. **S2.4 JSON Export (5 pts)**
   - **Difficulty:** Low-Medium (building on S2.3)
   - **Estimate:** 4-6 pts realistic
   - **Risk:** Performance optimization (10K rows < 500ms) could add complexity
   - **Mitigation:** Focus on core export first, optimize if needed

3. **S2.8 Activity Tracking (5 pts)**
   - **Difficulty:** Low (standard CRUD + timestamps)
   - **Estimate:** 3-5 pts realistic
   - **Parallelizable:** YES - can do after S2.3, doesn't block others
   - **Risk:** Low

4. **S2.9 Dataset Versioning (5 pts)**
   - **Difficulty:** Medium (entity relationships, reset logic)
   - **Estimate:** 5-7 pts realistic
   - **Parallelizable:** YES - can do after S2.3, doesn't block others
   - **Risk:** Medium

### **Dependencies Bottleneck:**

```
S2.2 must complete before:
  → S2.5 (frontend depends on S2.2 API for type detection)
  
S2.3 must complete before:
  → S2.4 (export needs DataSetRepository)
  → S2.6 (config UI needs generation endpoint)
  → S2.7 (viewer needs S2.3 API)
  → S2.8 (activity tracking on S2.3 endpoints)
  → S2.9 (versioning on S2.3 entities)
```

**Critical Path:** S2.2 (8 pts) → S2.5 (6 pts) → S2.6 (6 pts) → S2.7 (6 pts) = **26 pts serial**

**Opportunity for Parallelization:**
- S2.4, S2.8, S2.9 can be done in parallel after S2.3 ✅

---

## 📅 TIMELINE OPTIONS

### **Option A: All-in (Aggressive) - 54 pts in Sprint 2**

```
Timeline: 17/03 - 30/03 (14 jours calendar, ~10 work days)

Week 1 (17/03-21/03):
  Amelia:
    - Thu 17/03: S2.2 Type Detection (5 pts) ✅
    - Fri 18/03: S2.2 continued (3 pts) ✅
    - Week total: 8 pts
  Sally:
    - Start S2.5 CSV Upload (2 pts) ▶️

Week 2 (24/03-28/03):
  Amelia:
    - Mon 24/03: Code review S2.2, S2.4 start
    - Tue 25/03: S2.4 JSON Export (5 pts) ✅
    - Wed 26/03: S2.8 Activity Tracking (3 pts) ✅
    - Thu 27/03: S2.8 continued (2 pts) ✅
    - Fri 28/03: S2.9 Dataset Versioning start (3 pts) ▶️
  Sally:
    - S2.5 & S2.6 progress (6 pts) ▶️

Week 3 (31/03+):
  Amelia:
    - S2.9 continued (2 pts) ✅
  Sally:
    - S2.6 & S2.7 completion (6 pts) ✅

Status: SUPER TIGHT, High risk of carryover
Confidence: 50%
```

**Verdict:** 🚨 **NOT RECOMMENDED** - Risk too high, quality will suffer

---

### **Option B: Prioritized (Recommended) - 44 pts Sprint 2, 10 pts Sprint 3**

```
SPRINT 2 (44 pts completed):
Critical Path Only:
  - S2.1: ✅ DataGeneratorService (5 pts) DONE
  - S2.2: 📋 Type Detection (8 pts) HIGH PRIORITY
  - S2.3: ✅ Data Gen REST API (8 pts) DONE
  - S2.5: 📋 CSV Upload UI (6 pts) MEDIUM
  - S2.6: 📋 Data Configuration UI (6 pts) MEDIUM
  - S2.7: 📋 Data Viewer UI (6 pts) MEDIUM

  Backend to "ready" state:
  - S2.4: 📋 JSON Export Engine (5 pts) LOW (can defer to S3)

MOVED TO SPRINT 3 (10 pts):
  - S2.8: Activity Tracking (5 pts) ← DEFER
  - S2.9: Dataset Versioning (5 pts) ← DEFER
  
Rationale:
  - S2.8 & S2.9 are "nice to have", not blocking user flow
  - Can be added to S3 without impacting core MVP
  - Allows Amelia to focus on S2.2 (critical)

Timeline: 17/03 - 30/03 (14 jours, ~10 work days)
- Week 1: S2.2 completion (8 pts) ✅
- Week 2: S2.4 + parallel Sally on S2.5/S2.6 ✅
- Final: Testing + refinement ✅

Status: COMFORTABLE
Confidence: 85%
```

**Verdict:** ✅ **RECOMMENDED** - Better quality, achievable timeline

---

### **Option C: Phased (Conservative) - 34 pts S2, 20 pts S3**

```
SPRINT 2 (34 pts - ORIGINAL):
  - S2.1: ✅ DataGeneratorService (5 pts) DONE
  - S2.2: 📋 Type Detection (8 pts)
  - S2.3: ✅ Data Gen REST API (8 pts) DONE
  - S2.5: 📋 CSV Upload UI (6 pts)
  - S2.7: 📋 Data Viewer UI (7 pts)

SPRINT 3 (20 pts + existing):
  - S2.4: JSON Export Engine (5 pts)
  - S2.6: Data Config UI (6 pts)
  - S2.8: Activity Tracking (5 pts)
  - S2.9: Versioning (5 pts)

Timeline: Relaxed, lower pressure
Status: LOW RISK
Confidence: 95%
```

**Verdict:** ✅ **SAFE** but delays features to S3

---

## 🎯 RECOMMENDATION: OPTION B (Prioritized)

### **Sprint 2 Final Scope (44 pts):**

| Story | Points | Priority | Assignee | Timeline |
|-------|--------|----------|----------|----------|
| S2.1 | 5 | - | Amelia | ✅ DONE |
| S2.2 | 8 | 🔴 CRITICAL | Amelia | Week 1 (start immediately) |
| S2.3 | 8 | - | Amelia | ✅ DONE |
| S2.4 | 5 | 🟡 MEDIUM | Amelia | Week 2 (parallel) |
| S2.5 | 6 | 🟡 MEDIUM | Sally | Week 2 onwards (depends S2.2) |
| S2.6 | 6 | 🟡 MEDIUM | Sally | Week 2 onwards |
| S2.7 | 6 | 🟡 MEDIUM | Sally | Completion |

**DEFER to S3 (10 pts):**
- S2.8: Activity Tracking (5 pts) - Non-blocking, add after MVP stable
- S2.9: Dataset Versioning (5 pts) - Non-blocking, add after MVP stable

### **Benefits:**
✅ Amelia can focus on S2.2 (critical dependency)  
✅ Sally unblocked by S2.2 completion  
✅ Comfortable timeline, quality assured  
✅ Core MVP features complete in S2  
✅ S2.8 & S2.9 become S3 starters  

### **Timeline:**
```
17/03 - 20/03: S2.2 Type Detection (Amelia focus)
18/03 onwards: S2.5 CSV Upload starts (Sally, depends S2.2)
24/03 onwards: S2.4 JSON Export + S2.6 Config (Amelia + Sally)
28/03 onwards: S2.7 Viewer finalization
30/03: Sprint 2 Complete with 44 pts ✅
```

**Sprint Velocity:** 44 pts remaining over 10 work days = 4.4 pts/day (conservative)  
**Available Capacity:** ~80 pts potential = 180% of planned  
**Utilization:** ~55% ✅ Healthy, leaves margin for refactor/testing

---

## 📋 ACTION ITEMS

### **Immediate (Today - 28/02):**
1. [ ] **PM (John):** Validate Option B recommendation
2. [ ] **Amelia:** Confirm capacity estimate for S2.2 (8 pts realistic?)
3. [ ] **Sally:** Confirm React component velocity estimate (6 pts per component OK?)
4. [ ] **SM (Bob):** Prepare Sprint 2 replan with new scope

### **Before Sprint Kickoff (17/03):**
1. [ ] Update backlog.md with Option B scope (44 pts for S2, 10 pts moved to S3)
2. [ ] Update sprint-status.yaml: S2.8, S2.9 moved to sprint-3 status
3. [ ] Create Sprint 2 refined backlog (7 stories, 44 pts)
4. [ ] Schedule refinement session for S2.2 & S2.5 (complex dependencies)

### **Sprint 2 Metrics:**
- **Target velocity:** 44 pts
- **Confidence level:** 85%
- **Risk mitigation:** Focus on S2.2 first, parallelize S2.4-S2.7

---

## 📈 S2 vs S3 REVISED SCOPE

### **Sprint 2:** 44 pts (Core MVP + Export)
- Backend: 21 pts (S2.1 + S2.2 + S2.3 + S2.4)
- Frontend: 18 pts (S2.5 + S2.6 + S2.7)
- QA: TBD

### **Sprint 3:** 28 + 10 = 38 pts (Advanced Features)
- S2.8 Activity Tracking (5 pts) ← MOVED from S2
- S2.9 Versioning (5 pts) ← MOVED from S2
- 3-1 Batch Generation (8 pts)
- 3-2 WebSocket Notifications (8 pts)
- 3-3 E2E Testing (5 pts)
- 3-4 Docker Deployment (2 pts)

**Net:** S2 lighter, S3 heavier, but more balanced load

---

## ✅ CONCLUSION

**Recommended:** **OPTION B - Prioritized (44 pts Sprint 2, 10 pts to S3)**

- ✅ Achievable timeline (85% confidence vs 50%)
- ✅ Core MVP features delivered in S2
- ✅ Quality maintained (not rushed)
- ✅ S2.8 & S2.9 fit naturally in S3
- ✅ Allows documentation/polish time
- ✅ Margin for testing & bug fixes

**Next Step:** Await team validation, proceed with Option B planning

---

**Status:** DRAFT - Awaiting Approval  
**Prepared by:** Copilot (Capacity Analysis)  
**Review by:** PM (John), Dev Lead (Amelia), SM (Bob)

