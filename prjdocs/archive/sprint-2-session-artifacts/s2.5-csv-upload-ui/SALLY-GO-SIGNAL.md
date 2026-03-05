---
title: "🟢 SALLY GO SIGNAL - S2.5 Starts Now"
titleFR: "🟢 SIGNAL GO SALLY - S2.5 Commence Maintenant"
date: "2026-02-28"
time: "12:55 CET"
recipient: "Sally (UX Designer)"
phase: "EXECUTION"
status: "ACTIVE"
---

# 🟢 SALLY GO SIGNAL - S2.5 Frontend Implementation START

**To:** Sally  
**From:** Bob (Scrum Master)  
**Date:** 28 février 2026 @ 12:55 CET  
**Status:** 🟢 **GO - START NOW**

---

## 📌 YOUR MISSION STARTS NOW

**Sprint 2 Kickoff:** 17 mars 2026 @ 09:00  
**Your Mission:** Implement CSV Upload & Preview UI Component  
**Points:** 6 | **Effort:** 1.5 days | **Deadline:** ~ 20 mars EOD

---

## 🎯 WHAT YOU'LL BUILD

**CsvUploadPanel Component with:**
- ✅ Drag & drop file upload
- ✅ CSV preview table (first 10 rows)
- ✅ S2.2 API integration (type detection)
- ✅ Confidence score display (color-coded)
- ✅ Manual type override (dropdowns)
- ✅ Error handling
- ✅ 90%+ unit test coverage

---

## ✅ DETAILED SPECIFICATION

**See:** [S2.5-SPEC-SALLY.md](S2.5-SPEC-SALLY.md)

### Timeline (1.5 days)

**Day 1 Morning (1-2 hours):**
- Component scaffolding
- UploadZone builder
- PreviewTable builder

**Day 1 Afternoon (1-2 hours):**
- API integration (call S2.2)
- TypeDetectionResults builder
- Type override dropdowns

**Day 2 (2-3 hours):**
- Error handling & validation
- UI polish
- Unit tests
- Manual testing
- Code review with Amelia

---

## 🔧 TECH STACK

**Required Components:**
- Create: `src/components/CsvUploadPanel/CsvUploadPanel.jsx`
- Create: `src/components/CsvUploadPanel/UploadZone.jsx`
- Create: `src/components/CsvUploadPanel/PreviewTable.jsx`
- Create: `src/components/CsvUploadPanel/TypeDetectionResults.jsx`
- Create: `src/components/CsvUploadPanel/CsvUploadPanel.test.jsx`
- Modify: `src/pages/DomainsPage.jsx` (add component)

**Libraries:**
```bash
npm install papaparse  # CSV parsing
```

**Already Available:**
- Material-UI components ✅
- React Hooks ✅
- Context API ✅
- S2.2 API endpoint ✅

---

## 🔌 API YOU'LL CALL

```
POST /api/domains/{domainId}/detect-types
Content-Type: multipart/form-data
```

**Amelia verifies this endpoint on Day 1 morning**  
**Backend support available for questions**

---

## 📞 CONTACT

- **S2.2 API questions?** Ask Amelia
- **Component design help?** Ask Bob
- **Blockers?** Message Bob immediately
- **Code review?** Amelia reviews on Day 2

---

## 🚀 ACTION NOW

```
✅ Read: S2.5-SPEC-SALLY.md (this folder)
✅ Prepare: React environment
✅ Install: npm install papaparse
✅ Review: Component architecture in spec

Scheduled Start: 17 mars 2026 @ 09:00
Critical Path: S2.5 unblocks S2.6 & S2.7
```

---

## 📋 SUCCESS CRITERIA

You succeed when ALL of these are true:

- ✅ CSV upload works (drag & drop + file picker)
- ✅ Preview table displays first 10 rows
- ✅ S2.2 API integration works
- ✅ Confidence scores display + color-coded
- ✅ Manual type override works
- ✅ Error messages clear & helpful
- ✅ All AC verified
- ✅ 90%+ test coverage
- ✅ Code review approved by Amelia
- ✅ S2.6 can receive detection results

---

**Status:** 🟢 **READY FOR EXECUTION**  
**Confirmation:** Please acknowledge receipt

