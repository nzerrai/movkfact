---
title: "Manual E2E Testing Guide - Epic 12 AddColumnModal"
subtitle: "Complete CSV Upload → Add Columns → Generate Data Workflow"
date: "2026-03-10"
---

## 🎯 Testing Overview

This guide provides step-by-step instructions to manually test the complete CSV Column Addition feature end-to-end through the user interface.

**Estimated Duration**: 10-15 minutes  
**Browser Requirements**: Chrome, Firefox, or Safari  
**DevTools Required**: Optional (for network inspection)

---

## 📋 Pre-Test Checklist

Before starting, verify:
- ✅ Backend is running: `./devctl.sh V` → Backend: OK
- ✅ Frontend is running: `./devctl.sh V` → Frontend: OK
- ✅ Browser has cache cleared (DevTools → Settings → Network → Disable cache)
- ✅ Test CSV file ready (see Sample CSV Data below)

---

## 📊 Sample CSV File for Testing

Create a file named `test_data.csv` with the following content:

```csv
firstname,email,phone,company
John,john@example.com,555-0101,Acme Corp
Jane,jane@example.com,555-0102,TechCorp
Bob,bob@example.com,555-0103,DataSystems
Alice,alice@example.com,555-0104,CloudInc
```

---

## 🧪 Test Scenarios

### Test Scenario 1: Basic CSV Upload and Type Detection

**Objective**: Verify CSV file uploads and types are correctly detected

**Steps**:

1. **Open the Application**
   - Navigate to: http://localhost:3000
   - Expected: Application loads, homepage visible

2. **Navigate to CSV Upload**
   - Click on "CSV Upload" or equivalent menu item
   - Expected: Upload panel displays with drag-and-drop zone

3. **Upload CSV File**
   - Drag `test_data.csv` onto the upload zone, OR
   - Click to select file and choose `test_data.csv`
   - Expected: File appears as "test_data.csv", loading indicator shows

4. **Verify Type Detection**
   - Wait for processing to complete
   - Expected: Detected types appear (FIRST_NAME, EMAIL, PHONE, COMPANY)
   - Confidence scores visible (typically 85%+ for names/emails)

5. **Confirm Type Detection**
   - Click "Confirm Types" or equivalent button
   - Expected: Move to next step

**Expected Result**: ✅ CSV processed and types detected correctly

---

### Test Scenario 2: Open AddColumnModal and Add a Column

**Objective**: Test the AddColumnModal functionality for adding extra columns

**Prerequisites**: Completed Test Scenario 1

**Steps**:

1. **Proceed to Confirmed Step**
   - You should be in the "Confirmed" step showing detected columns
   - If not, complete Test Scenario 1
   - Expected: See table with 4 detected columns (firstname, email, phone, company)

2. **Open Add Column Modal**
   - Click button "+ Ajouter colonne" (Add Column)
   - Expected: Modal dialog opens with title "Ajouter une colonne supplémentaire"

3. **Fill Column Name**
   - In "Nom de la colonne" field, type: `status`
   - Expected: Text appears in field, helper text shows "Alphanumeric, underscore, hyphen only"

4. **Select Column Type**
   - Click on "Type" dropdown
   - Select: `ENUM`
   - Expected: Additional fields appear (depends on type)

5. **Fill Type-Specific Constraints**
   - For ENUM type, you should see "Enum Values" field
   - Enter comma-separated values: `active, inactive, pending`
   - Expected: Text appears in textarea

6. **Submit Column**
   - Click "Ajouter colonne" button
   - Expected: Modal closes, returns to confirmed step

7. **Verify Column Added**
   - Look for new section "Extra Columns Added"
   - Should show table with columns: name, type, constraints, action
   - New row: status | ENUM | values: active, inactive, pending | [Delete icon]
   - Row should have "Ajoutée" badge (green chip)
   - Expected: Column successfully added to list

**Expected Result**: ✅ Column added and displayed in extra columns table

---

### Test Scenario 3: Add Multiple Columns

**Objective**: Test adding multiple columns in sequence

**Prerequisites**: Completed Test Scenario 2 (one column added)

**Steps**:

1. **Add Second Column**
   - Click "+ Ajouter colonne" again
   - Fill: Name: `created_date`, Type: `DATE`
   - Click "Ajouter colonne"
   - Expected: Second column added to table

2. **Add Third Column**
   - Click "+ Ajouter colonne" again
   - Fill: Name: `score`, Type: `INTEGER`
   - In constraints: min: `0`, max: `100`
   - Click "Ajouter colonne"
   - Expected: Third column added to table

3. **Verify All Columns in Table**
   - Extra Columns Added table should show 3 rows:
     - status (ENUM)
     - created_date (DATE)
     - score (INTEGER with constraints min: 0, max: 100)
   - All should have "Ajoutée" badges
   - Expected: All columns visible

**Expected Result**: ✅ Multiple columns added successfully

---

### Test Scenario 4: Delete Extra Column

**Objective**: Test removing columns from the extra columns list

**Prerequisites**: At least one column in extra columns table (from previous tests)

**Steps**:

1. **Identify Delete Button**
   - In "Extra Columns Added" table, find the delete icon (trash can)
   - Most rightward column in any row
   - Expected: Icon visible with tooltip "Delete column"

2. **Delete First Column**
   - Hover over delete icon in first row → tooltip shows
   - Click delete icon
   - Expected: Row disappears, table updates

3. **Verify Deletion**
   - Count remaining columns (should be one less)
   - Total column count in header should decrease
   - Expected: Column successfully removed

**Expected Result**: ✅ Column deleted successfully

---

### Test Scenario 5: Form Validation - Duplicate Column Name

**Objective**: Test validation prevents duplicate column names

**Prerequisites**: Have at least one extra column added (status)

**Steps**:

1. **Open Add Column Modal**
   - Click "+ Ajouter colonne"
   - Expected: Modal opens

2. **Try to Add Duplicate Name**
   - In "Nom de la colonne", type: `status` (same as existing)
   - Expected: No immediate error (validation happens on submit)

3. **Try to Submit**
   - Click "Ajouter colonne"
   - Expected: Error alert appears: "Column 'status' already exists"
   - Modal stays open, form not cleared
   - Proposed behavior: Clear error when user clicks close on alert

4. **Verify Error Dismissal**
   - Click X on error alert
   - Expected: Error message disappears

5. **Fix and Resubmit**
   - Clear field, type different name: `department`
   - Click "Ajouter colonne"
   - Expected: Modal closes, new column added to table

**Expected Result**: ✅ Duplicate validation prevents duplicate names

---

### Test Scenario 6: Form Validation - Invalid Name Format

**Objective**: Test validation of column name format

**Steps**:

1. **Open Add Column Modal**
   - Click "+ Ajouter colonne"
   - Expected: Modal opens

2. **Try Invalid Characters**
   - In "Nom de la colonne", type: `status@code` (contains @)
   - Click "Ajouter colonne"
   - Expected: Error alert: "Column name can only contain alphanumeric characters, underscores, and hyphens"

3. **Fix Name**
   - Clear field, type: `status-code` (valid with hyphen)
   - Click "Ajouter colonne"
   - Expected: Modal closes, column successfully added

**Expected Result**: ✅ Format validation working

---

### Test Scenario 7: Proceed to Data Generation

**Objective**: Test complete workflow through data generation

**Prerequisites**: Multiple extra columns added (status, created_date, score, etc.)

**Steps**:

1. **Review Summary**
   - Verify "Upload Summary" section shows:
     - File name: test_data.csv
     - Rows: 4 (or count from your file)
     - Columns: 4 (detected) + 3 (extra) = 7 total
     - Detection Success Rate: high percentage

2. **Click Proceed to Configuration**
   - Button: "Proceed to Configuration →"
   - Expected: Navigates to next step, passes both detected and extra columns to backend

3. **Verify Data Generated**
   - Wait for data generation to complete
   - Expected: Success message, dataset created

4. **Inspect Generated Data** (Optional)
   - View the generated dataset
   - Verify contains:
     - Original columns: firstname, email, phone, company
     - Extra columns: status, created_date, score
     - Data values populated for all columns
   - Expected: Extra columns appear in generated data

**Expected Result**: ✅ Data generated with all columns (detected and extra)

---

### Test Scenario 8: Cancel Modal (Negative Test)

**Objective**: Verify cancel button works without losing progress

**Steps**:

1. **Open Add Column Modal**
   - Click "+ Ajouter colonne"
   - Expected: Modal opens

2. **Fill Form Partially**
   - Name: `partial_data`
   - Don't fill type or other fields
   - Expected: Fields populated

3. **Click Cancel**
   - Click "Annuler" button
   - Expected: Modal closes without adding column

4. **Verify No Column Added**
   - Extra columns table should not have "partial_data"
   - Expected: Cancellation worked, no data saved

5. **Verify Other Columns Still There**
   - Previously added columns should still be visible
   - Expected: Only modal closed, no data loss

**Expected Result**: ✅ Cancel works without side effects

---

### Test Scenario 9: Max Columns Limit (Boundary Test)

**Objective**: Test behavior when approaching column count limit

**Objective**: Test what happens when limit reached (if enforced)

**Prerequisites**: Add columns until reaching limit (default 50)

**Steps**:

1. **Add Multiple Columns** (repeated)
   - Add 8-10 extra columns (various types)
   - Expected: Each column successfully added

2. **Check Button Status**
   - After adding multiple columns, "+ Ajouter colonne" button might disable
   - Expected: Button disabled with tooltip "Maximum columns reached" (if implemented)

3. **If Button Still Enabled**
   - Try adding more columns
   - Expected: No error (feature may not enforce limit in this version)

**Expected Result**: ✅ Graceful handling of column limits

---

### Test Scenario 10: Type Constraints Display

**Objective**: Verify constraints show correctly for different column types

**Prerequisites**: Complete Test Scenario 3 (multiple columns with different types)

**Steps**:

1. **View Extra Columns Table**
   - Look at "Constraints" column
   - For score column (INTEGER): should show "min: 0 | max: 100"
   - For status column (ENUM): should show "values: active, inactive, pending"
   - For date column: should show "—" (no constraints) or similar
   - Expected: Constraints properly formatted

2. **Edit Values** (if supported)
   - Hover over constraint cells
   - Expected: Shows constraint details clearly

**Expected Result**: ✅ Constraints displayed correctly

---

## 🧠 Form Validation Rules Reference

When testing form submission, these validations should trigger errors:

| Rule | Example | Expected Error |
|------|---------|-----------------|
| Empty Name | Submit with blank | "Column name is required" |
| Duplicate Name | "status" when exists | "Column 'status' already exists" |
| Invalid Characters | "status@code" | "Column name can only contain alphanumeric..." |
| Too Long Name | 60+ characters | "Name must be less than 50 characters" |
| INTEGER without min/max | Type: INTEGER, no values | Success (optional constraints) |
| ENUM without values | Type: ENUM, blank values | "Enum values are required..." |
| Min > Max | min: 100, max: 10 | "Min value cannot be greater than max" |

---

## 📸 Expected UI Elements

### Modal Dialog
```
┌─ Ajouter une colonne supplémentaire ─────────────┐
│                                                   │
│  [Alert Error] (if validation fails)              │
│                                                   │
│  [Nom de la colonne ]  [text field]               │
│                        ↳ Helper: "Alphanumeric.." │
│                                                   │
│  [Type ⮸              ]  [select dropdown]        │
│                        ↳ 12 options in list       │
│                                                   │
│  [Constraints]         [conditional fields]       │
│  └─ For INTEGER: [Min] [Max]                      │
│  └─ For ENUM: [Enum Values] [textarea]            │
│  └─ For others: hidden/disabled                   │
│                                                   │
│           [Annuler]  [Ajouter colonne]            │
└─────────────────────────────────────────────────┘
```

### Extra Columns Table
```
┌─ Extra Columns Added ──────────────────────────────────────┐
│ Column   │ Type    │ Constraints        │ Action           │
├──────────┼─────────┼────────────────────┼──────────────────┤
│ status   │ ENUM    │ values: active,... │ [🗑️] "Ajoutée" │
│ score    │ INTEGER │ min: 0 | max: 100  │ [🗑️] "Ajoutée" │
│ created  │ DATE    │ —                  │ [🗑️] "Ajoutée" │
└──────────┴─────────┴────────────────────┴──────────────────┘
```

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Modal doesn't open | Refresh page, check backend logs |
| Form fields won't populate | Check DevTools Console for JS errors |
| Delete button not working | Refresh page, try again |
| Columns not saved after submit | Check network tab (verify POST 201 response) |
| Can't select column type | Try clicking more deliberately, check z-index |
| Error message doesn't disappear | Click the X button on Alert component |
| Extra columns don't appear in generated data | May be backend issue - check integration test results |

---

## ✅ Final Validation Checklist

After completing all tests, verify:

- ✅ Modal opens and closes smoothly
- ✅ Form fields are properly validated
- ✅ Column names must be unique
- ✅ Invalid characters are rejected
- ✅ Duplicate columns prevented
- ✅ Delete functionality removes columns
- ✅ Constraints display correctly
- ✅ Multiple columns can be added
- ✅ Cancel button doesn't save data
- ✅ Data generation includes extra columns
- ✅ French text displays correctly
- ✅ No JavaScript console errors
- ✅ UI is responsive (works on different screen sizes)
- ✅ Button states (enabled/disabled) work correctly

---

## 📝 Test Results Form

For documenting manual test execution:

```
Test Date: ____________
Tester Name: __________
Browser: _____________ Version: _____
Backend Status: OK / FAIL
Frontend Status: OK / FAIL

Test Scenario Results:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
1. CSV Upload/Detection       ☐ PASS  ☐ FAIL
2. Open Modal/Add Column      ☐ PASS  ☐ FAIL
3. Multiple Columns           ☐ PASS  ☐ FAIL
4. Delete Column              ☐ PASS  ☐ FAIL
5. Duplicate Validation       ☐ PASS  ☐ FAIL
6. Format Validation          ☐ PASS  ☐ FAIL
7. Complete Workflow          ☐ PASS  ☐ FAIL
8. Cancel Modal               ☐ PASS  ☐ FAIL
9. Column Limit               ☐ PASS  ☐ FAIL
10. Constraints Display       ☐ PASS  ☐ FAIL

Overall Status: ☐ PASS  ☐ FAIL
Issues Found: ___________________________________
Notes: ________________________________________
```

---

## 🎉 Success Criteria

The feature is considered **production-ready** when:

✅ All 10 test scenarios pass  
✅ No JavaScript errors in console  
✅ All validations work as expected  
✅ Data is correctly generated with extra columns  
✅ Performance is acceptable (< 1 second per action)  
✅ UI is responsive and intuitive  
✅ French localization is complete and correct  

---

## 📞 Support

If issues occur during testing:

1. Check backend logs: `tail -f /tmp/backend.log`
2. Check frontend console: DevTools → Console
3. Check network requests: DevTools → Network (look for 400/500 errors)
4. Verify services: `./devctl.sh V` (both should be OK)
5. Restart services: `./devctl.sh A` then `./devctl.sh D`

---

**Last Updated**: 10 March 2026  
**Version**: 1.0  
**Status**: Ready for Manual Testing
