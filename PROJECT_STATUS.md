# Refract Project Status

> **Purpose**: Current status, core principles, completed features, and backlog
> **Last Updated**: <Date>

**See Also:**
- [DECISION_LOG.md](./DECISION_LOG.md) - Architectural decision records (WHAT/WHY/WHEN decisions were made)
- [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) - Quick lookup ("I need to...") and architecture patterns (HOW to implement)
- [FILE_CATALOG.md](./FILE_CATALOG.md) - Complete file tree, system relationships, and component descriptions
- [TEST_SCENARIOS.md](./TEST_SCENARIOS.md) - Test coverage and scenarios to implement for automated testing

**Quick Navigation:** [How to Update](#how-to-update-this-file) | [Overview](#1-project-overview) | [References](#2-reference-files) | [Principles](#3-core-principles) | [Completed](#4-completed-features) | [Backlog](#5-active-backlog)

---

## How to Update This File

### When you complete a feature:
1. **Add bullet** to relevant subsection in [§4 Completed Features](#4-completed-features) (UI/UX, Database, Analysis, etc.)
2. **Update ALL FOUR documentation files**: PROJECT_STATUS.md, DECISION_LOG.md, DEVELOPER_GUIDE.md, FILE_CATALOG.md (always update all four, assess relevance for each)
3. **Add ADR entry** to DECISION_LOG.md if it involved architectural decision
4. **Update "Current Focus"** in [§1 Overview](#1-project-overview) if needed
5. **Add new subsection** in §4 if introducing a major new component category (e.g., "Export System", "Backup/Restore")

### When you make an architectural decision:
1. **Add ADR entry** to DECISION_LOG.md - newest first
2. **Format:** `#### MMM DD, YYYY: [Title]` → Decision/Rationale/Implementation (1 sentence each)
3. **Update relevant pattern** in [§3 Core Principles](#3-core-principles) if introducing new pattern
4. **Add to DEVELOPER_GUIDE.md** if decision establishes a reusable HOW-TO pattern

### When you fix a significant bug:
1. **Case-by-case basis** - document if architecturally important or impacts user experience significantly
2. **Add to backlog** in [§5 Active Backlog](#5-active-backlog) if discovered but not yet fixed
3. **Add ADR** to DECISION_LOG.md if the fix involved architectural changes worth documenting
4. **Skip documenting** small bugs or issues caught during initial implementation

### When you start new work:
1. **Add to backlog** in [§5 Active Backlog](#5-active-backlog) under appropriate category
2. **Add new subsection** in §5 if introducing a major new work area
3. **Move from backlog** to [§4 Completed Features](#4-completed-features) when done

### What NOT to add:
- ❌ Small bugs caught during initial implementation
- ❌ Minor refactorings without architectural impact
- ❌ Duplicate information (one location per fact)
- ❌ New top-level sections (§1-5 are fixed)

### Common pitfalls:
- Keep ADR entries concise (1 sentence per field)
- Completed features = stable, shipped work only
- Use existing subsections when possible; add new ones sparingly
- Current Focus should only reflect active work, not completed items (those go in Completed Features)

---

## 1. Project Overview

## 2. Reference files

| File | Purpose | Search Priority |
|------|---------|-----------------|
| DEVELOPER_GUIDE.md | Quick lookup ("I need to...") + Architecture patterns (HOW to implement) | 🔍 Search FIRST |
| FILE_CATALOG.md | Complete file tree + detailed component descriptions with dependencies | 🔍 Search for file details |
| PROJECT_STATUS.md | Current focus + Core principles (high-level constraints) + Completed features | 🔍 Search SECOND |
| DECISION_LOG.md | Historical ADRs (WHAT/WHY/WHEN decisions were made) | 🔍 Check for context |

**Three documentation purposes:**
- **Core Principles** (PROJECT_STATUS.md §3): High-level values/constraints - guide all development ("Manager pattern", "MediaStore = metadata source")
- **Architecture Patterns** (DEVELOPER_GUIDE.md): Reusable HOW-TO implementation patterns ("Unified Entity Pattern: when types share DB table, use id + flags")
- **ADRs** (DECISION_LOG.md): Historical record (WHAT/WHY/WHEN) - "Nov 8: Added Playlist.id because ruleId was dual-purpose"

## 3. Core Principles

> **Purpose**: High-level project values and constraints that guide ALL development decisions
> **Tone**: Directive ("MUST", "NEVER", "ALWAYS")
> **NOT for**: Historical context (use DECISION_LOG.md) or implementation details (use DEVELOPER_GUIDE.md)

### Architecture
- Manager pattern for complex operations
- Screen over dialog for navigation
- Extract components when used 2+ times
- Clean data modelling (one major class per file)

### Design
- Unified context menus, empty states, detail headers
- Dark/Light/System mode support

### Technical Architecture
- Android native (Kotlin), clean architecture (data/ui/utils)
- Hierarchical debug logging

## Section 4: Completed Features


## 5. Active Backlog
