---
title: "RESTRUCTURATION SPRINT 2 - 2026-02-28"
date: 2026-02-28
status: "COMPLETED"
executed_by: "Copilot Code Review"
approval_from: "User (Nouredine)"
---

# Restructuration Sprint 2: Clarification & Expansion

**Date:** 28 février 2026  
**Raison:** Incohérence scope S2.4 + combler exigences PRD manquantes  
**Approbation:** User approuve Priority 1 (scission), Priority 2 (3 stories), Priority 3 (sync dépendances)

---

## 📋 CHANGEMENTS EXÉCUTÉS

### Priority 1: SCISSON S2.4 EN DEUX STORIES ✅

**AVANT:**
```
S2.4: "CSV Upload & Preview UI" (Frontend, 6 pts)
  - Contenu réel ≠ titre planifié
  - Confusion avec "JSON Export Engine"
```

**APRÈS:** 
```
S2.4: JSON Export Engine (Backend, 5 pts) - NOUVEAU
  - Export datasets en JSON structuré
  - Endpoints: GET /api/data-sets/{id}/export
  - Filtering, formatting, performance <500ms
  - Depends on: S2.3

S2.5: CSV Upload & Preview UI (Frontend, 6 pts) - PROMOTIONNÉ
  - Upload fichier CSV
  - Type detection via S2.2 API
  - Override interactif des types
  - Drag & drop + file picker
  - Depends on: S2.2, S2.3
```

**Fichiers créés:**
- ✅ `2-4-implement-json-export-engine.md` (5 pts)
- ✅ `2-5-implement-csv-upload-preview-ui.md` (6 pts)

---

### Priority 2: AJOUTER 3 STORIES MANQUANTES ✅

#### Story #1: Activity Tracking (S2.8)
```yaml
Title: "Implement Activity Tracking System"
Epic: EPIC 2
Points: 5
Type: Backend
Status: ready
Depends on: S2.3
New Fields: viewedAt, downloadedAt, downloadCount, viewCount, activityStatus enum
Endpoints: Track views/downloads on data access
```

**Fichier:** ✅ `2-8-implement-activity-tracking.md`

#### Story #2: Dataset Versioning & Reset (S2.9)
```yaml
Title: "Implement Dataset Versioning & Reset"
Epic: EPIC 2
Points: 5
Type: Backend
Status: ready
Depends on: S2.3
New Entity: DataSetVersion (immutable original copy)
Endpoints: GET versions, POST reset, DELETE version
Feature: Reset modified datasets to original
```

**Fichier:** ✅ `2-9-implement-dataset-versioning.md`

#### Existing Stories Renumbered:
- S2.5 → S2.6: "Data Configuration Interface"
- S2.6 → S2.7: "Data Viewer & Management UI"

**Fichiers créés (renombrés):**
- ✅ `2-6-implement-data-configuration-ui.md` (6 pts, formerly S2.5)
- ✅ `2-7-implement-data-viewer-ui.md` (6 pts, formerly S2.6)

---

### Priority 3: SYNCHRONISER DÉPENDANCES ET MÉTADATA ✅

**Fichiers modifiés:**

1. ✅ `sprint-status.yaml`
   - Updated `development_status` section: New story keys (2-4, 2-5, 2-6, 2-7, 2-8, 2-9)
   - Updated `story_metadata` with correct titles, points, assignees
   - Added note for S2.3: "Actual implementation: REST API (not CSV parser)"

2. ✅ Tous les fichiers stories
   - Dépendances mises à jour
   - Cross-references corrigées
   - Task numbering cohérent

---

## 📊 NOUVELLES STRUCTURE SPRINT 2

| ID | Title | Type | Points | Status | Depends | Assignee |
|----|-------|------|--------|--------|---------|----------|
| S2.1 | DataGeneratorService | BE | 5 | ✅ done | - | Amelia |
| S2.2 | Type Detection | BE | 8 | ready | S2.1 | Amelia |
| S2.3 | Data Gen REST API | BE | 8 | ✅ done | S2.1,S2.2 | Amelia |
| **S2.4** | **JSON Export Engine** | **BE** | **5** | **ready** | **S2.3** | **Amelia** |
| **S2.5** | **CSV Upload UI** | **FE** | **6** | **ready** | **S2.2,S2.3** | **Sally** |
| S2.6 | Config Interface | FE | 6 | ready | S2.5,S2.3 | Sally |
| S2.7 | Data Viewer UI | FE | 6 | ready | S2.3,S2.6 | Sally |
| **S2.8** | **Activity Tracking** | **BE** | **5** | **ready** | **S2.3** | **Amelia** |
| **S2.9** | **Dataset Versioning** | **BE** | **5** | **ready** | **S2.3** | **Amelia** |

**Sprint Total Points:** 54 pts (vs 34 originally planned)  
**Completed:** 13 pts (S2.1✅, S2.3✅)  
**Ready to Start:** 41 pts (S2.2, S2.4-S2.9)

---

## 🔄 FLOW UTILISATEUR RÉVISÉ

```
1. CSV Upload (S2.5)
   ↓ [Drag & drop + file validation]
2. Type Detection (S2.2 API called)
   ↓ [Confidence scores, override dropdowns]
3. Configuration (S2.6)
   ↓ [Set generation params, row count]
4. Generation (S2.3 API called)
   ↓ [Generate data, persist]
5. Results Viewer (S2.7)
   ↓ [Display, paginate, export options]
6. Export Options (S2.4 API called)
   ↓ [JSON format, download, filtering]
7. Activity Track (S2.8 recorded)
   ↓ [Views, downloads, timestamps logged]
8. Versioning (S2.9 available)
   └─ [Original preserved, reset possible]
```

---

## ✅ EXIGENCES PRD MAINTENANT COUVERTES

| Exigence | Story | Status |
|----------|-------|--------|
| CSV upload | S2.5 | ✅ Ready |
| Type detection | S2.2 | ✅ Ready |
| Column config | S2.6 | ✅ Ready |
| Data generation | S2.1 + S2.3 | ✅ Done |
| JSON output | S2.3 + S2.4 | ✅ Done + Ready |
| JSON export | S2.4 | ✅ **NEW** Ready |
| Consultation | S2.7 | ✅ Ready |
| CSV export | S2.7 | ✅ Ready |
| APIs CRUD | S2.3 | ✅ Done |
| Activity tracking | S2.8 | ✅ **NEW** Ready |
| Original copy | S2.9 | ✅ **NEW** Ready |
| Reset action | S2.9 | ✅ **NEW** Ready |
| Performance | S2.1 | ✅ **Exceeded** (1K rows in 1ms!) |

---

## 📁 FICHIERS MODIFIÉS

### Créés (6):
- ✅ `2-4-implement-json-export-engine.md` (NEW)
- ✅ `2-5-implement-csv-upload-preview-ui.md` (NEW)
- ✅ `2-6-implement-data-configuration-ui.md` (renamed from 2-5)
- ✅ `2-7-implement-data-viewer-ui.md` (renamed from 2-6)
- ✅ `2-8-implement-activity-tracking.md` (NEW)
- ✅ `2-9-implement-dataset-versioning.md` (NEW)

### Supprimés (3):
- ❌ `2-4-implement-json-export-formatting.md` (old, replaced)
- ❌ `2-5-implement-frontend-csv-upload-ui.md` (old, replaced)
- ❌ `2-6-implement-data-viewer-ui.md` (old, replaced)

### Modifiés (1):
- ✅ `sprint-status.yaml` (story keys, points, metadata)

---

## 🎯 RÉSUMÉ IMPACT

### Positif ✅
1. **Clarification scope:** S2.4 maintenant sans ambiguité (Backend JSON export)
2. **Exigences couvertes:** +3 stories pour combler PRD (Activity, Versioning, Export)
3. **Flow cohérent:** CSV → Type detect → Config → Gen → View → Export → Track
4. **Architecture complète:** Feature set MVP +essentials maintenant dans S2
5. **Dépendances valides:** All stories properly sequenced

### Attention ⚠️
1. **Sprint velocity:** 54 pts vs 34 pts planifiés (159% de capacity)
2. **Timeline:** Sprint2 start: 17/03 → peut need extension si trop chargé
3. **Team capacity:** Backend: Amelia seule pour 8 stories (S2.2, S2.3, S2.4, S2.8, S2.9 + S2.1 done)

### Recommandations 📋
1. **Priorité:** S2.2 → S2.3 (dependencies first)
2. **Backend parallelization:** S2.4 + S2.8 + S2.9 peut be done in parallel after S2.3
3. **Frontend:** S2.5 → S2.6 → S2.7 linear flow
4. **Capacity check:** Consider splitting S2.8 + S2.9 vers S3 si timeline tight

---

## ✨ PROCHAINES ÉTAPES

1. **Immédiat:**
   - [ ] Valider avec le team que la nouvelle structure est acceptable
   - [ ] Confirmer capacity pour 54 pts (vs 34 planifiés)
   - [ ] Sync sprint dates si extension nécessaire

2. **S2.2 (Ready - start immediately):**
   - [ ] Implement CSV type detection endpoints
   - [ ] Call S2.2 API from S2.5

3. **S2.4-S2.9:**
   - [ ] Paralleliser backend après S2.3 complete
   - [ ] Frontend linear: S2.5 → S2.6 → S2.7

4. **Documentation:**
   - [ ] User guide: New activity tracking + versioning features
   - [ ] Architecture: Updated flow diagram

---

**Status:** RESTRUCTURATION COMPLÈTE ✅  
**Date:** 28 février 2026  
**Validation:** Attente confirmation team sur capacity

