<!-- BMAD:START -->
# BMAD Method — Project Instructions

## Project Configuration

- **Project**: movkfact
- **User**: Nouredine
- **Communication Language**: French
- **Document Output Language**: French
- **User Skill Level**: intermediate
- **Output Folder**: {project-root}/prjdocs
- **Planning Artifacts**: {project-root}/prjdocs/planning-artifacts
- **Implementation Artifacts**: {project-root}/prjdocs/implementation-artifacts
- **Project Knowledge**: {project-root}/docs

## BMAD Runtime Structure

- **Agent definitions**: `_bmad/bmm/agents/` (BMM module) and `_bmad/core/agents/` (core)
- **Workflow definitions**: `_bmad/bmm/workflows/` (organized by phase)
- **Core tasks**: `_bmad/core/tasks/` (help, editorial review, indexing, sharding, adversarial review)
- **Core workflows**: `_bmad/core/workflows/` (brainstorming, party-mode, advanced-elicitation)
- **Workflow engine**: `_bmad/core/tasks/workflow.xml` (executes YAML-based workflows)
- **Module configuration**: `_bmad/bmm/config.yaml`
- **Core configuration**: `_bmad/core/config.yaml`
- **Agent manifest**: `_bmad/_config/agent-manifest.csv`
- **Workflow manifest**: `_bmad/_config/workflow-manifest.csv`
- **Help manifest**: `_bmad/_config/bmad-help.csv`
- **Agent memory**: `_bmad/_memory/`

## Key Conventions

- Always load `_bmad/bmm/config.yaml` before any agent activation or workflow execution
- Store all config fields as session variables: `{user_name}`, `{communication_language}`, `{output_folder}`, `{planning_artifacts}`, `{implementation_artifacts}`, `{project_knowledge}`
- MD-based workflows execute directly — load and follow the `.md` file
- YAML-based workflows require the workflow engine — load `workflow.xml` first, then pass the `.yaml` config
- Follow step-based workflow execution: load steps JIT, never multiple at once
- Save outputs after EACH step when using the workflow engine
- The `{project-root}` variable resolves to the workspace root at runtime

## Sprint Organization Conventions

See [sprint-organization-conventions.md](../../docs/sprint-organization-conventions.md) for complete details. Quick reference:

**Sprint Folder Naming:**
- `sprint-N/` → Sprint not started (before start date)
- `sprint-N-started/` → Sprint currently active (ONLY ONE at a time)
- `sprint-N-ended/` → Sprint completed (for archival)

**File Organization:**
- Individual story files → inside `sprint-N-started/` directory
- `stories.md`, `kickoff-summary.md` → inside sprint directory
- `backlog.md`, `sprint-status.yaml` → root of implementation-artifacts
- Only the active sprint contains individual story files; others use summary files only

**Current Status (movkfact):**
- ✅ `sprint-1-started/` — Active (started 27/02/2026)
- ⏳ `sprint-2/` — Upcoming (17/03/2026)
- ⏳ `sprint-3/` — Future (31/03/2026)

## Available Agents

| Agent | Persona | Title | Capabilities |
|---|---|---|---|
| bmad-master | BMad Master | BMad Master Executor, Knowledge Custodian, and Workflow Orchestrator | runtime resource management, workflow orchestration, task execution, knowledge custodian |
| analyst | Mary | Business Analyst | market research, competitive analysis, requirements elicitation, domain expertise |
| architect | Winston | Architect | distributed systems, cloud infrastructure, API design, scalable patterns |
| dev | Amelia | Developer Agent | story execution, test-driven development, code implementation |
| pm | John | Product Manager | PRD creation, requirements discovery, stakeholder alignment, user interviews |
| qa | Quinn | QA Engineer | test automation, API testing, E2E testing, coverage analysis |
| quick-flow-solo-dev | Barry | Quick Flow Solo Dev | rapid spec creation, lean implementation, minimum ceremony |
| sm | Bob | Scrum Master | sprint planning, story preparation, agile ceremonies, backlog management |
| tech-writer | Paige | Technical Writer | documentation, Mermaid diagrams, standards compliance, concept explanation |
| ux-designer | Sally | UX Designer | user research, interaction design, UI patterns, experience strategy |

## Slash Commands

Type `/bmad-` in Copilot Chat to see all available BMAD workflows and agent activators. Agents are also available in the agents dropdown.
<!-- BMAD:END -->
