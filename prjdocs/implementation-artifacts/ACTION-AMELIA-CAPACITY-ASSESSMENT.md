---
title: "ACTION ITEM: Backend Capacity Assessment - Sprint 2"
date: 2026-02-28
priority: "🔴 HIGH"
assignee: "Amelia (Backend Developer)"
cc: ["John (PM)", "Bob (SM)"]
dueDate: 2026-02-28 (By 14:00)
---

# ACTION: Backend Capacity Assessment - Sprint 2

**Context:** Your workload increased significantly due to restructuration. We need your honest assessment of what's feasible.

---

## 📋 YOUR NEW SPRINT 2 WORKLOAD

| Story | Type | Points | Status | Complexity | Dependency |
|-------|------|--------|--------|-----------|------------|
| S2.1 | Core Service | 5 | ✅ **DONE** | - | - |
| S2.2 | Type Detection | 8 | ready | **HIGH** | S2.1 ✅ |
| S2.3 | REST API | 8 | ✅ **DONE** | - | S2.1✅, S2.2 |
| **S2.4** | **JSON Export** | **5** | **ready** | **MEDIUM** | **S2.3✅** |
| **S2.8** | **Activity Track** | **5** | **ready** | **MEDIUM** | **S2.3✅** |
| **S2.9** | **Versioning** | **5** | **ready** | **MEDIUM** | **S2.3✅** |

**Total New Backend Work in S2:** 23 pts (S2.2 + S2.4 + S2.8 + S2.9)  
**Timeline:** 14 days (March 17-30, 2026)  
**Average Velocity Needed:** 1.6 pts/day

---

## 🤔 HONEST ASSESSMENT: Please Answer These

### Question 1: Capacity Check
> Based on typical sprint velocity, how many points can YOU realistically complete in 14 days?

**Your estimate:** _____ pts

(Common ranges: Junior dev ~8-12 pts, Senior ~16-22 pts, with dependencies & testing)

### Question 2: S2.2 (Type Detection) - Difficulty
> How complex is implementing CSV column type detection API?
> - **Easy** (5-7 pts): Pattern matching + simple heuristics
> - **Medium** (8-10 pts): Pattern + value analysis + multi-format support
> - **Hard** (10-12 pts): Machine learning / complex edge cases

**Your estimate:** _____ / _____ pts (est. / max)  
**Any blockers?** Y / N - Explain: ________________

### Question 3: S2.4 (JSON Export) - Feasibility  
> After S2.3 complete, how straightforward is adding export endpoints?
> - **Trivial** (2-3 pts): Simple wrapper + existing serialization
> - **Easy** (4-5 pts): Add filtering, formatting, download headers
> - **Complex** (6-8 pts): Streaming, caching, performance optimization

**Your estimate:** _____ pts  
**Reuse from S2.3?** Yes/Partially/No

### Question 4: S2.8 + S2.9 (Activity & Versioning)
> These are similar complexity. Can you do BOTH in 10 pts or need more?
> - Separate implementations? Or combined entity?
> - Need refactoring DataSet entity? Impact on S2.3?

**Your estimate (both):** _____ pts  
**Can't do both?** Which one to defer? S2.8 or S2.9

### Question 5: Dependencies & Risks
> What's worried you?

- [ ] S2.2 complexity underestimated
- [ ] S2.3 needs fixes/tech debt
- [ ] S2.4/S2.8/S2.9 interactions/refactoring
- [ ] Testing time not accounted for
- [ ] No integration testing time budgeted
- [ ] Other: ________________

---

## 📊 CAPACITY SCENARIOS

Based on typical backend dev:

### Scenario A: You can do ~20 pts in 14 days (120% capacity)
✅ Complete: S2.2 (8) + S2.4 (5) + S2.8 (5) = 18 pts  
❌ Defer: S2.9 (5 pts) → S3

### Scenario B: You can do ~15 pts in 14 days (Normal capacity)
✅ Complete: S2.2 (8) + S2.4 (5) = 13 pts  
❌ Defer: S2.8 (5) + S2.9 (5) → S3

### Scenario C: You can do ~10 pts in 14 days (Conservative)
✅ Complete: S2.2 (8) = 8 pts  
❌ Defer: S2.4 (5) + S2.8 (5) + S2.9 (5) → S3/Later

---

## 🎯 WHAT PM NEEDS FROM YOU

By **14:00 today**, provide:

1. **Your scenario answer:** A / B / C (which is realistic?)
2. **Estimated hours** per story (to verify 14-day timeline)
3. **Any risks** that could derail timeline
4. **Resource needs:** Can you do this solo or need support?
5. **Preference:** If can't do all, which to defer?

**Format:** Reply to John (PM) with:
```
Capacity Scenario: [A/B/C]
S2.2 Estimate: X days @ Y hrs/day
S2.4 Estimate: X days @ Y hrs/day
S2.8 Estimate: X days @ Y hrs/day
S2.9 Estimate: X days @ Y hrs/day
Defer: [S2.9 or other]
Risks: [list any concerns]
```

---

## 📌 IMPORTANT NOTES

1. **Be honest.** Underestimating leads to crunch/burnout. Overestimating lead to timeline miss.
2. **Include testing.** 30-40% of time typically for unit + integration tests
3. **Include reviews.** Code review cycle adds ~10-15% overhead
4. **Integration risk.** S2.8/S2.9 touch DataSet entity → risk of regression with S2.3

---

## ✅ ACTION CHECKLIST

- [ ] Read this document carefully
- [ ] Answer all 5 questions above
- [ ] Estimate hours for each story
- [ ] Identify any blockers/dependencies
- [ ] Reply to PM by 14:00 with scenario

---

## 🚀 NEXT STEPS (After Your Input)

1. PM will discuss your capacity with Bob (SM)
2. Decision made: Keep 54pts in S2 OR defer S2.8/S2.9
3. Sprint plan updated 2026-03-01
4. Kickoff S2.2 on 17/03

---

**Your Input Needed By:** 2026-02-28 14:00  
**Send to:** John (PM)  
**Questions?** Reach out to Bob (SM)

