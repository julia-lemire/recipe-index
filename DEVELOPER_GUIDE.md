# Refract Developer Guide

> **Purpose**: Quick lookup ("I need to...") and architecture patterns (HOW to implement)
> **Last Updated**: <Date>

**See Also:**
- [DECISION_LOG.md](./DECISION_LOG.md) - Architectural decision records (WHAT/WHY/WHEN decisions were made)
- [FILE_CATALOG.md](./FILE_CATALOG.md) - Complete file tree, system relationships, and component descriptions
- [PROJECT_STATUS.md](./PROJECT_STATUS.md) - Current status, core principles, completed features, and backlog
- [TEST_SCENARIOS.md](./TEST_SCENARIOS.md) - Test coverage and scenarios to implement for automated testing

**Quick Navigation:** [How to Update](#how-to-update-this-file) | [Quick Lookup](#quick-lookup-i-need-to) | [Patterns](#architecture-patterns) | [New Screen Guide](#new-screen-creation-guide) | [Back Button](#system-back-button-pattern) | [Debug Logging](#debug-logging-pattern)

---

## How to Update This File

**Purpose of this file**: Quick task lookup + reusable HOW-TO implementation patterns. Use instructional tone ("Use this approach when...").

**This file vs other docs:**
- **DEVELOPER_GUIDE.md** (this file): Quick lookup + HOW-TO patterns - "Unified Entity Pattern: use id + behavioral flags when types share DB table"
- **FILE_CATALOG.md**: Complete file tree + system relationships + component descriptions with dependencies
- **Core Principles** (PROJECT_STATUS.md): High-level constraints - "All persisted entities expose their primary key"
- **ADRs** (DECISION_LOG.md): Historical WHAT/WHY/WHEN - "Nov 8, 2025: Added Playlist.id because ruleId was dual-purpose"

### When you create a new file:
1. **Add to Quick Lookup** if it provides a user-facing capability (e.g., "Work with Playlists", "Handle Playback")
2. **Add to FILE_CATALOG.md** under appropriate layer (data/, ui/, utils/, cpp/)
3. **Add to System Relationships** in FILE_CATALOG.md if file participates in a major flow

### When you modify a file's purpose or capabilities:
1. **Update Quick Lookup** if user-facing capability changed
2. **Update FILE_CATALOG.md** component description and relationships
3. **Keep descriptions concise** - focus on what it does, not how it does it

### When you establish a new pattern:
1. **Add to Architecture Patterns** section if used in 3+ places
2. **Include rationale** and examples
3. **Link to relevant files** that demonstrate the pattern
4. **Add to DECISION_LOG.md** as ADR explaining WHAT/WHY pattern was adopted

### What NOT to add:
- ❌ Implementation details (save for inline code comments)
- ❌ Temporary experimental files
- ❌ Build artifacts or generated files
- ❌ Duplicate information across sections

### Common pitfalls:
- Keep file descriptions to 1-2 sentences max
- Use "Purpose/Key capabilities/Used by/Depends on" format in Component Definitions
- Don't create new top-level sections - use existing structure
- Update all relevant sections when a file's role changes

---

## Quick Lookup: "I Need To..."

> **What goes here**: Common tasks mapped to relevant files. Add entries when you find yourself searching for "how do I do X?" Keep it task-focused, not implementation-focused.
>
> **Format**:
> ```
> ### [Task Description]
> - **[Component Type]**: `path/FileName.kt`
> - [Optional: Related files or patterns]
> ```

### Work with <XYZ>


---

## Complete Component Registry

> **MOVED TO FILE_CATALOG.md** - See [FILE_CATALOG.md](./FILE_CATALOG.md) for the complete file tree and detailed component descriptions organized by layer (Data, UI, Utils, C++).
---

## Architecture Patterns

> **What goes here**: Established architectural patterns and principles that guide development across the codebase.
>
> **When to add a new pattern**:
> - When you've applied the same approach in 3+ different places successfully
> - When the pattern solves a recurring architectural challenge
> - When it represents a key design principle (like "Single Source of Truth")
> - Examples worthy of inclusion: Manager Pattern, Config Over Code, Screen Over Dialog
> - Don't add: One-off solutions, file-specific implementations, minor code conventions

### Pattern


---
