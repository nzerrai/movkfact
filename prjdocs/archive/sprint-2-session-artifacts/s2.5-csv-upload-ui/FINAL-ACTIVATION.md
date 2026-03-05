---
title: "✅ FINAL ACTIVATION - Amelia & Sally GO"
titleFR: "✅ ACTIVATION FINALE - Amelia & Sally GO"
date: "2026-02-28"
time: "13:00 CET"
to: "Amelia & Sally"
from: "Bob (Scrum Master)"
status: "ACTIVATED"
---

# ✅ FINAL ACTIVATION - GO (28 février 13:00)

**To:** Amelia & Sally  
**From:** Bob  
**Date:** 28 février 2026 @ 13:00 CET  
**Status:** 🟢 **YOU ARE GO - START NOW**

---

## 🎯 AMELIA - YOUR NEXT 3 ACTIONS

### Action 1: Open Implementation Guide
```
📖 Read: AMELIA-IMPLEMENTATION-START.md
📍 Location: s2.5-csv-upload-ui/ folder
⏱️ Time: 5 minutes to scan overview
```

### Action 2: Run Verification (30 mins)
```bash
# Step 1: Build project
cd /home/seplos/mockfact && mvn clean install -DskipTests

# Step 2: Start backend
mvn spring-boot:run &

# Step 3: Verify S2.2 API
curl -X POST http://localhost:8080/api/domains/1/detect-types \
  -F "file=@/tmp/test.csv" -v
```

### Action 3: Run Benchmarks (1 hour)
```bash
# Create test files + run benchmarks
# Follow: AMELIA-IMPLEMENTATION-START.md (STEP 3-4)
# Document results in: /tmp/s2.5_api_performance.txt
```

---

## 🎯 SALLY - YOUR NEXT 3 ACTIONS

### Action 1: Open Implementation Guide
```
📖 Read: SALLY-IMPLEMENTATION-START.md
📍 Location: s2.5-csv-upload-ui/ folder
⏱️ Time: 10 minutes to scan overview
```

### Action 2: Setup Environment (15 mins)
```bash
# Step 1: Frontend directory
cd /home/seplos/mockfact/movkfact-frontend

# Step 2: Install Papa Parse
npm install papaparse

# Step 3: Create component directory
mkdir -p src/components/CsvUploadPanel
cd src/components/CsvUploadPanel
touch {UploadZone,PreviewTable,TypeDetectionResults,CsvUploadPanel}.jsx
touch CsvUploadPanel.test.jsx
```

### Action 3: Create First Component (1-2 hours)
```
FILE: src/components/CsvUploadPanel/UploadZone.jsx
COPY: Full code from SALLY-IMPLEMENTATION-START.md (STEP 3)
TEST: Drag & drop zone visible in browser
```

---

## 📞 WHEN TO CONTACT EACH OTHER

### Amelia → Sally
✅ When: **"S2.5 API verification complete!"**  
Message: "API is working, CORS configured, performance good ✅ You can start API integration now"

### Sally → Amelia  
✅ When: **"Component scaffolding done"**  
Message: "Upload component ready. Waiting on API verification to integrate. Can you check?"

### Either → Bob  
🚨 When: **Blocker identified**  
Message: "BLOCKER: [description] - Need help"

---

## ⏱️ TODAY'S TIMELINE (28 février)

```
13:00 - 13:30:  Both read implementation guides
13:30 - 14:00:  Amelia: Maven build + API verification
                Sally: npm install + create directory structure

14:00 - 15:00:  Amelia: Run performance benchmarks
                Sally: Create UploadZone component + test

15:00 - 17:00:  Amelia: Start S2.4 JSON export code
                Sally: Create PreviewTable + Type results components

EOD (17:00):    Check-in with progress
```

---

## 🎯 EACH TEAM'S SUCCESS CRITERIA (by end of today if possible, or by 17 mars)

### Amelia
- ✅ S2.2 API verified working
- ✅ CORS configured
- ✅ Benchmarks showing < 5 sec for 10K rows
- ✅ Performance report created
- ✅ S2.4 command structure started

### Sally
- ✅ All 5 component files created
- ✅ UploadZone component working
- ✅ PreviewTable component working
- ✅ TypeDetectionResults component working
- ✅ Unit test scaffold created

---

## 📂 YOUR IMPLEMENTATION DOCUMENTS

### Amelia Has Access To:
- AMELIA-IMPLEMENTATION-START.md ← **START HERE**
- AMELIA-GO-SIGNAL.md
- S2.5-ASSIGNMENT-AMELIA.md
- 00-IMPLEMENTATION-ACTIVE.md

### Sally Has Access To:
- SALLY-IMPLEMENTATION-START.md ← **START HERE**
- SALLY-GO-SIGNAL.md
- S2.5-SPEC-SALLY.md
- 00-IMPLEMENTATION-ACTIVE.md

---

## 🟢 STATUS: GO

```
✅ Amelia: Ready to verify API + start S2.4
✅ Sally: Ready to build CSVUploadPanel
✅ Communication channels: Open
✅ Documentation: Complete
✅ Blockers: None identified
✅ Support: Available

🚀 YOU ARE CLEARED FOR IMPLEMENTATION
```

---

## 💡 QUICK TIPS

**For Amelia:**
- Backend already running? Kill it first: `pkill -9 java`
- Need quick API test? Use curl commands in implementation guide
- S2.4 files go in: `src/main/java/com/movfact/{controller,service}/`

**For Sally:**
- Frontend already running? That's fine, changes auto-reload
- Component code is complete in implementation guide - just copy-paste
- Test in browser: http://localhost:3000

---

## 🎯 SPRINT 2 CONTEXT

Remember: S2.5 is the **critical path** 🎯
- Unblocks S2.6 (Sally continues)
- Unblocks S2.7 (Sally continues)
- S2.4 parallel with S2.5
- All due by 20 mars EOD

**You've got this! 💪**

---

**FINAL ACTIVATION:** 28 février 2026 @ 13:00 CET  
**Status:** 🟢 **IMPLEMENTATION LIVE**

GO! 🚀

