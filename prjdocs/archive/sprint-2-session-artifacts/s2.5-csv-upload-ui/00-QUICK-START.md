# 🚀 Immediate Action Items - Run Now!
**Generated:** 28 février 2026 - 14:35 CET  
**Duration:** ~45 minutes total

---

## Quick Start Commands

### 1️⃣ AMELIA - Backend Build & Test (15 min)

```bash
# Navigate to project root
cd /home/seplos/mockfact

# Clean and build
mvn clean install -DskipTests

# Run S2.4 tests specifically
mvn test -Dtest=DataExportControllerTest

# Expected output ↓ (all green ✅)
# Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

**If tests pass:**
```bash
# Verify endpoints start correctly
# Leave this running while Sally tests frontend
mvn spring-boot:run
```

**Expected console output:**
```
Started movkfact in X seconds (JVM running for Y)
```

**Then test one endpoint manually:**
```bash
curl -X POST http://localhost:8080/api/domains/1/export/json
```

---

### 2️⃣ SALLY - Frontend Setup & Test (20 min)

```bash
# Navigate to frontend folder
cd /home/seplos/mockfact/movkfact-frontend

# Install Papa Parse dependency
npm install

# Verify it installed
ls node_modules | grep papaparse
# Should show: papaparse

# Start development server
npm start
# Will open http://localhost:3000 automatically
```

**In Browser (Chrome/Firefox/Safari):**
1. Navigate to http://localhost:3000
2. Click "Domain Management" or go to /domains
3. Find a domain in the table
4. Look for a **cloud icon** (☁️) in the Actions column
5. Click it → Modal opens with "Upload CSV Data"
6. **Drag a test CSV file** or **click to select file**
7. Verify the upload zone renders without errors

**Check browser console:**
- Press `F12` to open Dev Tools
- Check "Console" tab - should be no red errors

---

### 3️⃣ Run Frontend Tests (10 min)

**In a NEW terminal** (keep npm start running):

```bash
cd /home/seplos/mockfact/movkfact-frontend

npm test -- CsvUploadPanel.test.jsx

# Press 'a' to run all tests
# Expected: 8/8 tests pass ✅
```

**Expected output:**
```
PASS src/components/CsvUploadPanel/CsvUploadPanel.test.jsx
  ✓ renders upload zone initially (25ms)
  ✓ shows select file button (15ms)
  ✓ accepts file input (12ms)
  ✓ shows error for non-CSV files (18ms)
  ✓ shows error for oversized files (15ms)
  ✓ displays UploadZone text (10ms)
  ✓ handles cancel button (20ms)
  ✓ PreviewTable conditional rendering (12ms)

Test Suites: 1 passed, 1 total
Tests:       8 passed, 8 total
```

---

## 📋 Verification Checklist

### ✅ Backend Checklist (Amelia)

- [ ] Maven build succeeds (`BUILD SUCCESS`)
- [ ] 4 DataExportControllerTests pass
- [ ] Application starts without errors (`Started movkfact`)
- [ ] Can call `/api/domains/{id}/export/json` endpoint
- [ ] Response returns valid JSON with domain data
- [ ] Errors handled gracefully (404 if domain not found)
- [ ] No compilation warnings

**Status when complete:** Ready for S2.4 code review ✅

---

### ✅ Frontend Checklist (Sally)

- [ ] `npm install` completes without errors
- [ ] `npm start` launches browser to http://localhost:3000
- [ ] Domain Management page loads
- [ ] Can see domain table with cloud icons in Actions
- [ ] Click cloud icon → CSV upload modal opens
- [ ] UploadZone renders with drag-drop area
- [ ] Can drag files to upload zone
- [ ] Can click to select files
- [ ] No errors in browser Dev Tools Console
- [ ] 8/8 unit tests pass with `npm test`

**Status when complete:** Ready for S2.5 browser testing ✅

---

## 🧪 Manual Integration Test (After Both ✅)

**Test S2.5 → S2.2 API Integration**

1. **Prepare test CSV file** (save as `test.csv`):
```csv
firstName,lastName,age,salary,dateOfBirth
John,Doe,30,50000,1994-03-15
Jane,Smith,28,60000,1996-07-22
Bob,Johnson,35,75000,1989-11-10
```

2. **With backend running** (`mvn spring-boot:run`):
   - Windows: `http://localhost:8080`
   - Mac/Linux: `http://localhost:8080`

3. **With frontend running** (`npm start`):
   - http://localhost:3000

4. **Integration test steps:**
   - Go to a domain
   - Click cloud icon → upload modal
   - Drag test.csv to upload zone
   - Wait for S2.2 API to process
   - Verify results show: firstName, lastName, age, salary, dateOfBirth
   - Verify confidence scores appear (🟢🟡🔴)
   - Click "Confirm & Continue"
   - Modal closes, success message appears

---

## 🐛 Troubleshooting

### Backend Issues

**Error: "Cannot find symbol: DataExportService"**
```bash
# Make sure you're in the right folder
cd /home/seplos/mockfact
ls src/main/java/com/movfact/service/DataExportService.java
# Should exist
```

**Error: "Tests run: 0"**
```bash
# Make sure test file exists
ls src/test/java/com/movfact/controller/DataExportControllerTest.java
# Should exist

# Try running all tests instead
mvn test
```

---

### Frontend Issues

**Error: "Cannot find module 'papaparse'"**
```bash
# Install dependencies
npm install
# Or specifically
npm install papaparse@5.4.1
```

**Error: "React component not rendering"**
```bash
# Check imports in DomainsPage.jsx
grep -n "CsvUploadPanel" src/pages/DomainsPage.jsx
# Should show import line

# Clear npm cache
npm cache clean --force
npm install
```

**Browser shows blank page**
```bash
# Check console (F12 → Console tab)
# If errors, restart dev server:
npm start
```

---

## 📊 Progress Tracking

| Item | Status | Time | Notes |
|------|--------|------|-------|
| Backend build | ⏳ | 10 min | Amelia |
| Backend tests | ⏳ | 5 min | Amelia |
| Frontend install | ⏳ | 5 min | Sally |
| Frontend start | ⏳ | 5 min | Sally |
| Browser verification | ⏳ | 10 min | Sally |
| Unit tests | ⏳ | 5 min | Sally |
| Integration test | ⏳ | 15 min | Both |
| **TOTAL** | ⏳ | **45 min** | **~3:20 PM CET** |

---

## 📞 Communication Protocol

### When Backend Tests Pass ✅ (Amelia)
```
"S2.4 backend tests passing ✅
 - 4/4 tests pass
 - Spring boot running
 - Ready for integration test"
```

### When Frontend Tests Pass ✅ (Sally)
```
"S2.5 frontend tests passing ✅
 - 8/8 unit tests pass
 - Components rendering correctly
 - Ready for integration test"
```

### When Integration Works ✅ (Both)
```
"S2.5 + S2.4 integration verified ✅
 - CSV upload modal opens
 - S2.2 API integration working
 - Ready for code review"
```

---

## ✨ Next: Code Review & Merge

Once all tests pass:

1. **Amelia:** Push S2.4 branch to GitHub
2. **Sally:** Push S2.5 branch to GitHub
3. **Create Pull Requests** with links to:
   - Test execution screenshots
   - Browser verification screenshots
   - Integration test confirmation
4. **Code Review** by team leads
5. **Merge to main** when approved

**Target:** Merged by end of day 28 février

---

**Estimated Start Time:** Now (14:35 CET)  
**Estimated Completion:** ~15:20 CET  
**Next Sync:** When first test suite passes
