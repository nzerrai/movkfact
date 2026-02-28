# Sprint Organization Conventions - movkfact

**Effective Date:** 27 février 2026  
**Project:** movkfact  
**Purpose:** Standardize sprint folder naming and file organization

---

## Sprint Directory Naming Convention

### States and Suffixes

Sprints progress through three states, each with a specific naming convention:

1. **Pre-Start State** → `sprint-N/`
   - Used before the sprint's start date
   - No suffix
   - Example: Sprint 1 before 03/03/2026 is `sprint-1/`

2. **Active State** → `sprint-N-started/`
   - Used when a sprint is currently in progress
   - Only ONE sprint can have `-started` suffix at any time
   - Example: Sprint 1 from 27/02 onwards is `sprint-1-started/`

3. **Completed State** → `sprint-N-ended/`
   - Used after a sprint ends
   - Remains for archival purposes
   - Example: Sprint 1 after 16/03/2026 becomes `sprint-1-ended/`

### Timeline Example (movkfact)

| Date | Sprint 1 | Sprint 2 | Sprint 3 |
|------|----------|----------|----------|
| 27/02/2026 | `sprint-1/` | `sprint-2/` | `sprint-3/` |
| 27/02 (started) | `sprint-1-started/` ⚡ | `sprint-2/` | `sprint-3/` |
| 03/03-16/03 | `sprint-1-started/` ⚡ | `sprint-2/` | `sprint-3/` |
| 17/03 (end+start) | `sprint-1-ended/` | `sprint-2-started/` ⚡ | `sprint-3/` |
| 17/03-30/03 | `sprint-1-ended/` | `sprint-2-started/` ⚡ | `sprint-3/` |
| 31/03 (end+start) | `sprint-1-ended/` | `sprint-2-ended/` | `sprint-3-started/` ⚡ |
| 31/03-13/04 | `sprint-1-ended/` | `sprint-2-ended/` | `sprint-3-started/` ⚡ |
| 14/04+ | `sprint-1-ended/` | `sprint-2-ended/` | `sprint-3-ended/` |

**Legend:** ⚡ = Currently active sprint

---

## File Organization Structure

### Directory Layout

```
prjdocs/implementation-artifacts/
├── backlog.md                      # Project backlog overview (all epics/stories)
├── sprint-status.yaml              # Global sprint tracking (statuses, assignees)
│
├── sprint-1-started/               # Currently active sprint
│   ├── stories.md                  # Story summary/index for this sprint
│   ├── kickoff-summary.md          # Sprint kickoff details
│   ├── 1-1-setup-backend-infrastructure.md
│   ├── 1-2-implement-domain-entity.md
│   ├── 1-3-implement-rest-controller.md
│   ├── 1-4-frontend-setup.md
│   └── 1-5-ui-dashboard.md
│
├── sprint-2/                       # Upcoming sprint (not started)
│   ├── stories.md
│   ├── kickoff-summary.md
│   └── [story files added when sprint starts]
│
└── sprint-3/                       # Future sprint
    ├── stories.md
    ├── kickoff-summary.md
    └── [story files added when sprint starts]
```

### File Organization Rules

1. **Individual Story Files**
   - Belong in their sprint directory: `sprint-N*/S{N}.{#}-*.md`
   - Example: `sprint-1-started/1-1-setup-backend-infrastructure.md`
   - Only created after sprint starts (moved into directory when sprint transitions to `-started`)

2. **Summary Files**
   - `stories.md` - High-level overview of all stories in sprint
   - `kickoff-summary.md` - Sprint planning/kickoff details
   - Created during sprint planning, before sprint starts
   - Remain in sprint directory through all states

3. **Root Level Files**
   - `backlog.md` - Project-wide backlog (not sprint-specific)
   - `sprint-status.yaml` - Global tracking file
   - These never move and track cross-sprint information

4. **Sprint Story Assignment Rule**
   - ⚠️ **ALL stories of a sprint MUST be created in their corresponding sprint directory**
   - Stories for Sprint N belong in `sprint-N-started/` (or `sprint-N/` if not yet started, before moving to `-started`)
   - Never create stories in other sprint directories or outside sprint folders
   - This ensures clear ownership, prevents cross-sprint confusion, and maintains audit trails
   - Violation: Story for S2 created in `sprint-1-started/` directory ❌
   - Correct: Story for S2 created in `sprint-2/` or `sprint-2-started/` directory ✅

---

## Renaming Workflow

### When Sprint Starts

**Trigger:** Sprint start date arrives (or sprint manually started)

**Action:**
```bash
mv sprint-N sprint-N-started
```

**Example:** Sprint 1 started on 27/02/2026
```bash
mv sprint-1 sprint-1-started
```

### When Sprint Ends

**Trigger:** Sprint end date arrives

**Action:**
```bash
mv sprint-N-started sprint-N-ended
mv sprint-N+1 sprint-N+1-started  # If next sprint starting same day
```

**Example:** Sprint 1 ends & Sprint 2 starts on 17/03/2026
```bash
mv sprint-1-started sprint-1-ended
mv sprint-2 sprint-2-started
```

---

## Automation Recommendations

For BMAD workflow integration:

1. **During Sprint Planning Workflow**
   - Check if sprint already exists
   - Create as `sprint-N/` if not started
   - Stories added to root during planning
   - Stories moved to sprint directory when sprint starts

2. **During Dev Story Workflow**
   - Check current active sprint (has `-started` suffix)
   - Look for `.md` files in active sprint directory
   - Automatically update file paths when sprint status changes

3. **During Sprint Status Workflow**
   - Perform rename operations when transitioning state
   - Update paths in sprint-status.yaml
   - Log transitions for audit trail

---

## Status Tracking File (sprint-status.yaml)

The `sprint-status.yaml` file tracks all stories across all sprints:

```yaml
development_status:
  epic-1: "in-progress"
  1-1-setup-backend-infrastructure: "review"
  1-2-implement-domain-entity-repository: "backlog"
  # ... all 16 stories tracked here
```

**Key:** Paths reference stories but don't include sprint folder prefix
- Used by dev-story workflow to discover next ready story
- Independent of directory structure (can rename folders without breaking tracking)

---

## Current Project Status (movkfact)

**As of 27 février 2026:**

- ✅ **sprint-1-started/** - ACTIVE
  - Started: 27 février 2026 (early start)
  - Originally planned: 03/03/2026
  - Due: 16/03/2026
  - Stories: 5 (21 points)
  - Completed: 1 (S1.1 - Setup Backend Infrastructure)

- ⏳ **sprint-2/** - Not started
  - Planned start: 17/03/2026
  - Due: 30/03/2026
  - Stories: 6 (34 points)

- ⏳ **sprint-3/** - Not started
  - Planned start: 31/03/2026
  - Due: 13/04/2026
  - Stories: 5 (28 points)

---

## Integration with BMAD Workflows

### dev-story Workflow
- Checks sprint-status.yaml for current status
- Looks for backlog/ready-for-dev stories in active sprint directory
- Uses story file paths without referencing sprint folder name

### create-story Workflow
- Creates story file in active sprint directory
- Uses sprint naming to determine which directory to use
- Updates sprint-status.yaml with new story key

### sprint-planning Workflow
- Creates sprint folder as `sprint-N/`
- Plans stories and adds kickoff-summary.md
- When sprint starts, folder renamed to `sprint-N-started/`

---

## Notes

- Only **ONE** sprint can have `-started` suffix at a time
- Folder renaming is manual or automated (depending on workflow)
- Story files can be moved between sprints (change directory, update sprint-status.yaml)
- Archive old sprints (sprint-N-ended) in separate location after project completion if needed
