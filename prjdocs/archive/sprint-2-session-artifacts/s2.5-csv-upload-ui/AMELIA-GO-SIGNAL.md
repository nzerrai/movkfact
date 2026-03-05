---
title: "🟢 AMELIA GO SIGNAL - S2.5 Starts Now"
titleFR: "🟢 SIGNAL GO AMELIA - S2.5 Commence Maintenant"
date: "2026-02-28"
time: "12:55 CET"
recipient: "Amelia (Developer)"
phase: "EXECUTION"
status: "ACTIVE"
---

# 🟢 AMELIA GO SIGNAL - S2.5 Backend Verification START

**To:** Amelia  
**From:** Bob (Scrum Master)  
**Date:** 28 février 2026 @ 12:55 CET  
**Status:** 🟢 **GO - START NOW**

---

## 📌 YOUR MISSION STARTS NOW

**Sprint 2 Kickoff:** 17 mars 2026 @ 09:00  
**Your Immediate Tasks (Next 3 Days):**

1. **Day 1 (17 mars Morning):** API Verification + Performance Benchmarks
2. **Day 1 (17 mars Afternoon - EOD):** Start S2.4 JSON Export
3. **Days 2-3 (18-20 mars):** Code Review Sally's S2.5 + Finish S2.4

---

## ✅ DETAILED ASSIGNMENT

**See:** [S2.5-ASSIGNMENT-AMELIA.md](S2.5-ASSIGNMENT-AMELIA.md)

### Task 1: S2.5 API Verification (17 mars Morning)

```
Verify S2.2 Type Detection API:
  POST /api/domains/{domainId}/detect-types

Checklist:
  ✓ Endpoint exists and responds
  ✓ Response format correct (columns, confidence, detector)
  ✓ CORS headers present
  ✓ File upload validation works
  ✓ Performance: <5 sec for 10K rows
```

### Task 2: Performance Benchmarks (17 mars Morning)

```
Run 3 benchmark tests:
  Test 1: 100 rows    → Expected: < 500ms ✅
  Test 2: 1000 rows   → Expected: < 2 sec ✅
  Test 3: 10K rows    → Expected: < 5 sec ✅

Document: /tmp/s2.5_api_performance.txt
```

### Task 3: S2.4 Start (17 mars Afternoon)

```
After verifying S2.5 API, start S2.4:
  Create: POST /api/domains/{domainId}/export/json
  Test locally
  Write initial unit tests
```

---

## 📞 CONTACT

- **Questions about S2.5 API?** Message Sally
- **Blockers?** Message Bob immediately
- **S2.4 questions?** Check spec in sprint-2-started/

---

## 🚀 ACTION NOW

```
✅ Read: S2.5-ASSIGNMENT-AMELIA.md (this folder)
✅ Prepare: Benchmark test environment
✅ Scheduled Start: 17 mars 2026 @ 09:00

Next: Sally receives complementary GO SIGNAL
```

---

**Status:** 🟢 **READY FOR EXECUTION**  
**Confirmation:** Please acknowledge receipt

