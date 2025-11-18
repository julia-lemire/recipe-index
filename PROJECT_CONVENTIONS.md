# Recipe Index Project Conventions

> Quick reference for maintaining Recipe Index documentation and code

## Documentation Structure

**Root:**
- `ANDROID_DESIGN_PRINCIPLES.md` - Architecture rules, patterns, critical constraints
- `PROJECT_CONVENTIONS.md` - This file (how to maintain docs)

**docs/ folder:**
- `PROJECT_STATUS.md` - Current status, core principles, completed features, backlog
- `DECISION_LOG.md` - Architectural decision records (WHAT/WHY/WHEN)
- `DEVELOPER_GUIDE.md` - Quick lookup ("I need to...") and HOW-TO patterns
- `FILE_CATALOG.md` - Complete file tree, system relationships, component descriptions
- `TEST_SCENARIOS.md` - Test coverage and scenarios
- `PRODUCT_BRIEF.md` - Product vision, features, user stories
- `DESIGN_SYSTEM.md` - Hearth visual design system

## CRITICAL: Keep Documentation BRIEF

**Maximum:** 1-3 sentences per update. Use bullets, not paragraphs.

## Before Updating ANY Doc

**ALWAYS read the "How to Update This File" section at the bottom of each file first.**

## Doc Update Checklist (for PRs)

When making changes that affect documentation:
1. Update `docs/PROJECT_STATUS.md` if completing features or changing status
2. Update `docs/DECISION_LOG.md` if making architectural decision
3. Update `docs/DEVELOPER_GUIDE.md` if adding new user-facing capability
4. Update `docs/FILE_CATALOG.md` if creating/modifying/deleting files
5. Update `docs/TEST_SCENARIOS.md` if adding/completing tests

## Brevity Examples

❌ **Too verbose:**
"In this implementation, we decided to use the Manager pattern because it provides better separation of concerns and makes the code more testable by extracting business logic from ViewModels, which should only handle UI state."

✅ **Correct:**
"Manager pattern: Business logic in data/ContentManagers/, ViewModels handle UI state only"

---

**Last Updated:** 2025-11-18
