---
title: "CHECKLIST SCRUM MASTER - Replanification Sprint 2"
date: 2026-02-28
audience: "Bob (Scrum Master)"
status: "ACTIONABLE"
---

# Checklist Scrum Master - Sprint 2 Replanification
## Restructuration + Capacity Adjustment Implementation

**Date:** 28 février 2026  
**Owner:** Bob (Scrum Master)  
**Goal:** Execute Option B replan before Sprint 2 kickoff (17/03/2026)  
**Timeline:** 28/02 - 16/03 (17 days)

---

## 📋 IMMEDIATE ACTIONS (28/02 - 01/03)

### **Decision Making (1-2 days):**

- [x] **Schedule Quick Sync with Leads**
  - **Participants:** John (PM), Amelia (dev), Sally (frontend), Winston (architect)
  - **Duration:** 1 hour
  - **Goal:** Present Option B recommendation, collect approval
  - **Decision:** Approved? YES / NO / NEEDS DISCUSSION
  - **Date/Time:** ____________

- [ ] **Confirm with Amelia**
  - [x] S2.2 Type Detection (8 pts) - realistic? yes
  - [x] S2.4 JSON Export (5 pts) - realistic? yes
  - [ ] Backend total 23 pts (excluding S2.1✅, S2.3✅) - achievable?
  - [ ] Noted concerns/risks: No risks
  - [ ] Approval to defer S2.8 + S2.9: YES

- [ ] **Confirm with Sally**
  - [x] S2.5 CSV Upload (6 pts) - realistic for first React sprint? yes
  - [x] S2.6 Config UI (6 pts) - realistic? yes
  - [ ] S2.7 Viewer UI (6 pts) - realistic? yes
  - [ ] Frontend total 18 pts - achievable? yes
  - [ ] Need pair programming support? YES
  - [ ] Noted concerns/risks: _No Risks___________________

- [ ] **Decision Logged**
  - **Option B APPROVED:** ✅ YES 
  - **Date approved:** _28/02/2026___________
  - **Approved by:** PM ___yes__, Dev Lead __yes___, UX Lead __yes___

---

## 📝 ARTIFACT UPDATES (02/03 - 05/03)

### **Sprint Status Sync:**

- [ ] **Update sprint-status.yaml**
  - [x ] Change S2.8 from "sprint: 2" to "sprint: 3"
  - [x ] Change S2.9 from "sprint: 2" to "sprint: 3"
  - [x ] Update development_status for S2.8, S2.9: Moved to "backlog", status: "backlog"
  - [x ] Verify S2.1-S2.7 are properly marked (done or ready)
  - [x ] Double-check S3 now has 38 pts (28 original + 10 deferred)
  - **File:** `prjdocs/implementation-artifacts/sprint-status.yaml`
  - **Verified by:** PM_______________ (Date: _______)

- [ ] **Update backlog.md (if exists)**
  - [x] Document S2 scope change: 34 pts → 44 pts
  - [x] List 7 stories now in S2 (S2.1✅ + S2.2-S2.7)
  - [x] List S2.8 + S2.9 moved to S3 (10 pts)
  - [x] Update priority indicators (S2.2 = CRITICAL)
  - **File:** `prjdocs/implementation-artifacts/backlog.md`
  - **Verified by:** ___PM_____________ (Date: _______)

- [ ] **Create Replan Communication Document**
  - [ ] Copy template: `SPRINT-2-REPLAN-TIMELINE-2026-02-28.md`
  - [ ] Customize with team names & dates
  - [ ] Generate PDF for distribution (if needed)
  - **Saved to:** `prjdocs/implementation-artifacts/`
  - **Distributed to:** PM, team leads, team
  - **Date:** ____________

---

## 👥 STAKEHOLDER COMMUNICATION (05/03)

### **Notifications (Do at team ceremony or 1-on-1):**

- [ ] **Notify PM (John)**
  - [ ] S2 scope: 34 pts → 44 pts (+10 pts)
  - [ ] Confidence: 85% (vs 50% if all-in)
  - [ ] Timeline: Still 17/03 - 30/03 (unchanged)
  - [ ] Quality: Better (not rushed)
  - [ ] Stakeholder impact: S2.8 + S2.9 move to S3 (explained)
  - [ ] Approval given: YES _____ DATE: _____

- [ ] **Notify Dev Team (Amelia)**
  - [ ] Backend scope confirmed: S2.2, S2.4, (S2.3✅ done)
  - [ ] S2.8 + S2.9 deferred to S3 (more breathing room)
  - [ ] S2.2 is critical path (start immediately?)
  - [ ] Dependencies clear: S2.2→S2.5, S2.3→S2.4/S2.6/S2.7
  - [ ] Any questions/concerns: ____________

- [ ] **Notify UX Team (Sally)**
  - [ ] Frontend scope confirmed: S2.5, S2.6, S2.7 (18 pts)
  - [ ] Dependencies: S2.5 blocked on S2.2 completion
  - [ ] Pair programming available if needed: YES / NO
  - [ ] Design review with Winston before coding: YES / NO
  - [ ] Any questions/concerns: ____________

- [ ] **Notify Architect (Winston)**
  - [ ] S2.4 JSON export technical needs
  - [ ] Frontend integration approach for S2.5-S2.7
  - [ ] Performance targets: S2.2 <500ms, S2.4 <500ms for 10K rows
  - [ ] Ready to support refinement sessions: YES / NO
  - [ ] Any technical blockers anticipated: ____________

---

## 🎓 REFINEMENT SESSIONS (03/03 - 16/03)

### **Session 1: S2.2 Type Detection (Week of 03/03)**

- [ ] **Schedule:**
  - **Date/Time:** ____________
  - **Duration:** 2-3 hours
  - **Location:** ____________
  - **Participants:** Amelia (lead), Mary (analyst), Winston (architect), [others?]

- [ ] **Preparation (before session):**
  - [ ] Review story S2.2 requirements
  - [ ] Mary prepares CSV edge case samples
  - [ ] Winston reviews NLP/pattern matching approach
  - [ ] Amelia studies S2.1 GeneratorFactory patterns

- [ ] **During Session (agenda):**
  - [ ] Algorithm deep dive (pattern matching + value analysis)
  - [ ] Edge cases: encoding, malformed data, nulls
  - [ ] Confidence scoring approach
  - [ ] Integration with S2.1 Faker generators
  - [ ] Performance target: <500ms for detection
  - [ ] Hard acceptance criteria confirm

- [ ] **Output:**
  - [ ] AC confirmed & signed off: YES / NO
  - [ ] Risks identified: ___________________________
  - [ ] Tech spike needed? YES / NO (if yes, schedule)
  - [ ] Estimated effort still 8 pts? YES / NO / REVISED TO: ___

- [ ] **Minutes recorded:** ________________ (prepared by: _____)

### **Session 2: S2.5-S2.7 Frontend (Week of 03/03)**

- [ ] **Schedule:**
  - **Date/Time:** ____________ (after S2.2 session)
  - **Duration:** 1-2 hours
  - **Location:** ____________
  - **Participants:** Sally (lead), Winston (architect), Amelia (backend liaison), [others?]

- [ ] **Preparation:**
  - [ ] Review S2.5, S2.6, S2.7 stories
  - [ ] Winston sketches component flow
  - [ ] Sally reviews MUI best practices / patterns from S1
  - [ ] Amelia prepares API contract sketches (S2.2, S2.3, S2.4)

- [ ] **During Session:**
  - [ ] Component hierarchy: S2.5 (upload)→S2.6 (config)→S2.7 (viewer)
  - [ ] State management: Context API for shared CSV data
  - [ ] API contracts: Request/response format align
  - [ ] Reusability: Can components be extracted for later use?
  - [ ] Performance: Handle 10K rows without freezing?
  - [ ] Accessibility: WCAG 2.1 Level AA target

- [ ] **Output:**
  - [ ] Component design sketched: YES ✅
  - [ ] API contracts aligned: YES ✅
  - [ ] Context structure documented: YES ✅
  - [ ] Pair programming needed for Sally? YES / NO
  - [ ] Estimated effort S2.5-S2.7: 18 pts confirmed? YES / NO

- [ ] **Minutes recorded:** ________________ (prepared by: _____)

### **Session 3: S2.4 JSON Export (Week of 10/03)**

- [ ] **Schedule:**
  - **Date/Time:** ____________
  - **Duration:** 1 hour
  - **Location:** ____________
  - **Participants:** Amelia (lead), Winston (architect)

- [ ] **Preparation:**
  - [ ] Review S2.4 story & AC
  - [ ] Winston sketches export service architecture
  - [ ] Performance baseline: What's "acceptable" for 10K rows export?

- [ ] **During Session:**
  - [ ] Export filtering & formatting approach
  - [ ] Performance targets & optimization strategy
  - [ ] Caching needs (if any)
  - [ ] Integration with S2.7 (download buttons)
  - [ ] Hard AC confirm

- [ ] **Output:**
  - [ ] AC confirmed: YES ✅
  - [ ] Performance approach clear: YES ✅
  - [ ] 5 pts estimate confirmed? YES / NO
  - [ ] Tech spike needed? YES / NO

- [ ] **Minutes recorded:** ________________ (prepared by: _____)

---

## 🎯 SPRINT 2 KICKOFF PREP (16/03)

### **Pre-Kickoff Checklist (2 days before 17/03):**

- [ ] **Sprint Board Setup**
  - [ ] Jira/Trello: Create 7 stories (S2.1✅ + S2.2-S2.7)
  - [ ] Story cards populated with AC, tasks
  - [ ] Add dependency links (S2.2→S2.5, S2.3→S2.4/S2.6/S2.7)
  - [ ] Assign story points (verified from replan)
  - [ ] Set sprint dates: 17/03 - 30/03

- [ ] **Burndown Chart Setup**
  - [ ] Target: 44 pts total
  - [ ] Daily pace line: Should reach 0 by 30/03
  - [ ] Establish baseline for forecasting

- [ ] **Communication Materials**
  - [ ] Slides prepared: Sprint 2 overview + scope change rationale
  - [ ] Burndown chart template ready
  - [ ] Velocity history displayed (if exists)
  - [ ] Risk mitigation strategies documented

- [ ] **Team Ready?**
  - [ ] All team members confirmed available 17/03-30/03: YES ✅
  - [ ] No vacation/holidays blocking sprint: YES ✅
  - [ ] Necessary tools/access provisioned: YES ✅
  - [ ] Development environment set up: YES ✅

- [ ] **Documentation**
  - [ ] Kickoff agenda finalized
  - [ ] DoD (Definition of Done) reviewed
  - [ ] Velocity tracking template prepared
  - [ ] Issue escalation process communicated

---

## 🚀 SPRINT 2 KICKOFF (17/03) - SM ROLE

### **During Kickoff Meeting (1.5 hours):**

- [ ] **Facilitate Meeting** (40 min prep, 90 min meeting)
  - [ ] Welcome & opening remarks (PM or Winston)
  - [ ] Context: Sprint 2 theme & scope (44 pts)
  - [ ] Story breakdown: 6 new stories (S2.2-S2.7)
  - [ ] Dependencies visualization
  - [ ] Velocity target: 4.4 pts/day
  - [ ] Risks & mitigation strategies
  - [ ] Definition of Done review
  - [ ] Q&A

- [ ] **Breakout: Story Estimation Review**
  - [ ] Verify points still accurate: All agree? YES / NO / REVISED
  - [ ] Commit to 44 pts: All team members: YES ✅
  - [ ] Adjust if needed: __________________________

- [ ] **Establish Team Norms**
  - [ ] Daily standup time: ____________
  - [ ] Location (Zoom/office): ___________
  - [ ] Maximum duration: 15 min ✓
  - [ ] Code review ceremony: Ad-hoc same-day preferred ✓
  - [ ] Escalation protocol: Report blockers immediately ✓

- [ ] **Metrics & Tracking Setup**
  - [ ] Burndown chart: Visible to all ✓
  - [ ] Velocity history displayed ✓
  - [ ] Code coverage dashboard (if available) ✓
  - [ ] Performance metrics baseline (S2.2, S2.4 targets) ✓

---

## 📊 SPRINT EXECUTION SUPPORT (17/03 - 30/03)

### **Daily Activities (SM Responsibilities):**

- [ ] **Daily Standup (10:00 every day)**
  - [ ] Note: Date _____, attendees: _____
  - [ ] S2.2 progress: On track? YES / NO / BLOCKERS: _____
  - [ ] S2.5-S2.7 progress: On track? YES / NO / BLOCKERS: _____
  - [ ] Burndown pace: Expected? YES / NO
  - [ ] Velocity early warning: _______________
  - [ ] Action items captured: YES ✓

- [ ] **Code Review Turnaround**
  - [ ] Code review requests tracked
  - [ ] Target: Approve/reject within 4 hours
  - [ ] No review bottlenecks: YES / NO
  - [ ] Escalate if reviews pending >24h

- [ ] **Remove Blockers Immediately**
  - [ ] Amelia blocked on S2.2 algo? → Escalate to Winston
  - [ ] Sally blocked on API contract? → Escalate to Amelia
  - [ ] Missing tool/access? → Provision immediately
  - [ ] Severity: High (report daily), Medium (report in standup)

---

## 📈 MID-SPRINT CHECKPOINT (24/03 @ 2pm)

### **Velocity Check (1 hour meeting):**

- [ ] **Burndown Status**
  - [ ] Points completed so far: _____ / 44
  - [ ] Expected pace: ~22 pts by 24/03
  - [ ] Actual pace: On track? YES / NO / BEHIND by: ___

- [ ] **Story Status (as of 24/03)**
  - [ ] S2.2 Type Detection: DONE ✓ / IN PROGRESS / AT RISK
  - [ ] S2.4 JSON Export: STARTED / NOT STARTED
  - [ ] S2.5 CSV Upload: _____ / NOT STARTED (blocked on S2.2?)
  - [ ] S2.6 Config UI: _____ / NOT STARTED
  - [ ] S2.7 Viewer UI: _____ / NOT STARTED

- [ ] **Risks & Adjustments**
  - [ ] Any risks materialized? YES / NO → _____
  - [ ] Contingency needed? YES / NO → _____
  - [ ] Scope adjustment required? YES / NO
  - [ ] If behind: Which story can be de-scoped?
    - Option 1: Remove S2.4 (JSON export, moved to S3)
    - Option 2: Reduce S2.7 scope (basic viewer, advanced later)
    - Option 3: Extend sprint by 2-3 days (if possible)

- [ ] **Decision: Stay the course?**
  - [ ] YES - Continue as planned 
  - [ ] NO - Adjust scope, explain to PM

- [ ] **Minutes:** ________________ (prepared by: _____)

---

## ✅ SPRINT COMPLETION (30/03 - 03/04)

### **Sprint Review (1 hour):**

- [ ] **Deliverables Demonstrated**
  - [ ] S2.2 Type Detection API working demo ✓
  - [ ] S2.4 JSON Export API working demo ✓
  - [ ] S2.5 CSV Upload UI working demo ✓
  - [ ] S2.6 Config UI working demo ✓
  - [ ] S2.7 Data Viewer UI working demo ✓
  - [ ] All tests passing: 334+ tests ✓

- [ ] **Metrics Report**
  - [ ] Sprint Points: 44 pts / 44 completed: _____ %
  - [ ] Velocity achieved: _____ pts (vs 4.4 target)
  - [ ] Burndown: Expected vs actual ✓
  - [ ] Code coverage: _____ % (target >80%)
  - [ ] No regressions: YES ✓
  - [ ] Zero critical bugs: YES ✓

- [ ] **Retrospective (30 min):**
  - [ ] What went well? _______________________
  - [ ] What could improve? _______________________
  - [ ] Action items for S3: _______________________
  - [ ] Velocity trend: Increasing / Stable / Decreasing
  - [ ] Recommend velocity for S3? _____ pts/sprint

---

## 📋 SIGN-OFF & FINAL CHECKLIST

- [ ] **Replan communication sent:** Date: _______
- [ ] **All approvals collected:** YES ✅
- [ ] **Refinement sessions completed:** 3/3 done ✅
- [ ] **Sprint board setup & tested:** YES ✅
- [ ] **Team confirmed ready for 17/03 kickoff:** YES ✅
- [ ] **Executive summary for stakeholders:** Sent ✅
- [ ] **Risk register updated:** YES ✅
- [ ] **Escalation procedures communicated:** YES ✅

---

## 📞 CONTACT & ESCALATION

**SM (Bob):**
- [ ] Email: bob@movkfact.dev
- [ ] Slack: @bob_sm
- [ ] Available: 8:00 - 18:00 daily (standups 10:00)
- [ ] Escalation: Contact immediately if blocker

**PM (John):** Strategic decisions, scope changes  
**Winston (Architect):** Technical blockers, design decisions  
**Amelia (Dev Lead):** Backend progress, S2.2 concerns  
**Sally (UX Lead):** Frontend progress, component issues  

---

**Checklist Status:** ✅ READY FOR IMPLEMENTATION  
**Last Updated:** 28 février 2026  
**Prepared by:** Copilot (SM Support)  
**Reviewed by:** [Bob SM approval]

