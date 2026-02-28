---
title: "ACTION ITEM: PM Capacity Review - Sprint 2 Restructuration"
date: 2026-02-28
priority: "🔴 HIGH"
assignee: "John (PM)"
cc: ["Nouredine", "Bob (SM)", "Amelia (Dev)"]
dueDate: 2026-02-28 (EOD)
---

# ACTION: PM Capacity Review - Sprint 2 Restructuration

**Context:** Sprint 2 restructuration approved by user Nouredine adds **+20 pts of scope** (54 pts vs 34 planned). Backend resource Amelia now has **6 backend stories** (was 3).

---

## 🎯 YOUR IMMEDIATE ACTIONS

### Action 1: Validate Sprint 2 Capacity (URGENT - Today EOD)

**Situation:**
```
Original Planned: 34 pts
- S2.1: 5 pts ✅ Done
- S2.2: 8 pts 📋 Ready
- S2.3: 8 pts ✅ Done
- S2.4: 5 pts 📋 Ready (was "not scope-clear")
- S2.5: 4 pts 📋 Ready (was "CSV Upload UI")
- S2.6: 4 pts 📋 Ready (was "Data Viewer")

NEW Planned: 54 pts (+20 pts / +59%)
+ S2.4: 5 pts (JSON Export) ← NEW
+ S2.5: 6 pts (CSV Upload & Preview UI) ← EXPANDED
+ S2.6: 6 pts (Data Configuration) ← RENAMED
+ S2.7: 6 pts (Data Viewer) ← RENAMED
+ S2.8: 5 pts (Activity Tracking) ← NEW
+ S2.9: 5 pts (Dataset Versioning) ← NEW
```

**Sprint 2 Timeline:** 17/03/2026 - 30/03/2026 (14 days, typically ~40 velocity for 4-person team)

**Team Velocity Estimate:**
- Backend (Amelia only): ~16 pts/sprint (vs 23 pts now planned for S2)
- Frontend (Sally only): ~20 pts/sprint (vs 18 pts planned for S2)

**Decision Required:** Choose ONE option:

#### Option A: Keep All 54 pts in S2 (Timeline Extension)
- ✅ Deliver all requirements S2
- ❌ Need extend timeline to 21 days (vs 14)
- ❌ Amelia overloaded for 3 weeks
- 💰 Cost: 1 week extension

#### Option B: Split to S2 + extended S3 (Balanced Load)
- S2.1-S2.7: Keep (40 pts, fits capacity)
- S2.8-S2.9: Move to S3 (10 pts, activity/versioning less critical MVP)
- ✅ On-time delivery S2
- ✅ Better resource balance
- ⚠️ Activity tracking delayed 3 weeks

#### Option C: Add Backend Resource (Best Case)
- Hire/assign second backend dev
- Complete all 54 pts in S2 timeline
- ✅ Full scope + on-time
- 💰 Cost: 1 additional developer 3 weeks

---

### Action 2: Briefing with Amelia (Developer)

**Prepare for discussion:**
- [ ] Print/send `RESTRUCTURATION-2026-02-28.md` to Amelia
- [ ] Highlight new work: S2.4 (Export), S2.8 (Activity), S2.9 (Versioning)
- [ ] Ask for **capacity estimate** (est. hours per story)
- [ ] Ask for **blockers** or **risks**
- [ ] Get her **preference** on Option A/B/C above

**Expected Response:** Amelia confirms she can:
- [ ] Complete S2.2 (Type Detection) - 8 pts
- [ ] Complete S2.4 (Export Engine) - 5 pts  
- [ ] Complete S2.8 (Activity Tracking) - 5 pts
- OR defer S2.8/S2.9 if timeline constraint

---

### Action 3: Briefing with Scrum Master (Bob)

**Prepare for discussion:**
- [ ] Inform Bob that Sprint 2 scope expanded 59%
- [ ] Ask if timeline extension possible (17/03 - 30/03 → 14 days)
- [ ] Ask if resource addition possible (second backend dev)
- [ ] Request **replan decision** by **2026-03-01 (Friday)**

**Expected Outcome:** Bob confirms:
- [ ] New sprint capacity (54 pts vs original 34)
- [ ] Timeline (extend 14→21 days OR split S2/S3)
- [ ] Resource plan (1 person or +1 dev)
- [ ] Updated sprint goal

---

### Action 4: Stakeholder Communication

**Send to Nouredine (User):**
```
Subject: Sprint 2 Capacity Confirmation Needed

Hi Nouredine,

Your restructuration approval expanded Sprint 2 scope by 20 pts (34→54 total).

DECISION NEEDED:
A) Keep all 54 pts → Extend timeline 14→21 days (~1 week delay)
B) Keep 40 pts (S2.1-S2.7) → Move Activity & Versioning to S3
C) Add backend resource → Complete all 54 pts on time

Recommend: Option B (balanced) or Option A if timeline flexible

Please confirm your preference by EOD 2026-02-28
```

---

## 📊 SUMMARY TABLE

| Scenario | Timeline | Scope | Quality | Risk |
|----------|----------|-------|---------|------|
| **A: Keep 54pts** | 21 days | 100% | ⚠️ Lower (crunch) | High |
| **B: Split S2/S3** | 14 days | 85% | ✅ Normal | Low |
| **C: Add Dev** | 14 days | 100% | ✅ Normal | Med |

---

## ✅ COMPLETION CHECKLIST

- [ ] Read `RESTRUCTURATION-2026-02-28.md` (15 min)
- [ ] Meet Amelia for capacity assessment (30 min) 
- [ ] Meet Bob for sprint replan (30 min)
- [ ] Decide on Option A/B/C (15 min)
- [ ] Communicate decision to Nouredine (10 min)
- [ ] Update sprint-status.yaml if replan needed (10 min)

**Estimated Time:** 2 hours  
**Target Completion:** 2026-02-28 15:00

---

## 📞 ESCALATION

If cannot decide by EOD today:
1. Call Nouredine for final call on timeline flexibility
2. Loop in Bob if resource addition needed
3. Update backlog/sprint-status with final decision

**Contact:** John (PM) - john@movkfact.dev

---
