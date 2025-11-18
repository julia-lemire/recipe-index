# Recipe Index Project Status

> **Purpose**: Current status, core principles, completed features, and backlog
> **Last Updated**: 2025-11-18

**See Also:**
- [DECISION_LOG.md](./DECISION_LOG.md) - Architectural decision records (WHAT/WHY/WHEN decisions were made)
- [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) - Quick lookup ("I need to...") and architecture patterns (HOW to implement)
- [FILE_CATALOG.md](./FILE_CATALOG.md) - Complete file tree, system relationships, and component descriptions
- [TEST_SCENARIOS.md](./TEST_SCENARIOS.md) - Test coverage and scenarios
- [PROJECT_CONVENTIONS.md](../PROJECT_CONVENTIONS.md) - How to maintain documentation

**Quick Navigation:** [How to Update](#how-to-update-this-file) | [Overview](#1-project-overview) | [References](#2-reference-files) | [Principles](#3-core-principles) | [Completed](#4-completed-features) | [Backlog](#5-active-backlog)

---

## How to Update This File

### When you complete a feature:
1. **Add bullet** to relevant subsection in [§4 Completed Features](#4-completed-features)
2. **Update ALL FOUR docs**: PROJECT_STATUS.md, DECISION_LOG.md, DEVELOPER_GUIDE.md, FILE_CATALOG.md
3. **Add ADR entry** to DECISION_LOG.md if architectural decision involved
4. **Update "Current Focus"** in [§1 Overview](#1-project-overview) if needed
5. **Add new subsection** in §4 if introducing major new component category

### When you make an architectural decision:
1. **Add ADR entry** to DECISION_LOG.md - newest first
2. **Format:** `#### MMM DD, YYYY: [Title]` → Decision/Rationale/Implementation (1 sentence each)
3. **Update relevant pattern** in [§3 Core Principles](#3-core-principles) if new pattern
4. **Add to DEVELOPER_GUIDE.md** if establishes reusable HOW-TO pattern

### When you fix a significant bug:
1. **Case-by-case basis** - document if architecturally important or impacts UX significantly
2. **Add to backlog** in [§5 Active Backlog](#5-active-backlog) if discovered but not fixed
3. **Skip documenting** small bugs caught during initial implementation

### When you start new work:
1. **Add to backlog** in [§5 Active Backlog](#5-active-backlog) under appropriate category
2. **Move from backlog** to [§4 Completed Features](#4-completed-features) when done

### What NOT to add:
- ❌ Small bugs caught during initial implementation
- ❌ Minor refactorings without architectural impact
- ❌ Duplicate information
- ❌ New top-level sections (§1-5 are fixed)

### Common pitfalls:
- Keep ADR entries concise (1 sentence per field)
- Completed features = stable, shipped work only
- Current Focus = active work only (not completed items)

---

## 1. Project Overview

**Current Phase:** Recipe Management (Phase 1)
**Current Focus:** Recipe CRUD complete with full navigation, ready for recipe import features (Phase 2)

Recipe Index: Offline-first Android app for home cooks to store, organize, and plan meals with recipes from URLs/PDFs/photos.

---

## 2. Reference Files

| File | Purpose | Search Priority |
|------|---------|-----------------|
| DEVELOPER_GUIDE.md | Quick lookup ("I need to...") + Architecture patterns (HOW) | 🔍 Search FIRST |
| FILE_CATALOG.md | Complete file tree + component descriptions | 🔍 Search for file details |
| PROJECT_STATUS.md | Current focus + Core principles + Completed features | 🔍 Search SECOND |
| DECISION_LOG.md | Historical ADRs (WHAT/WHY/WHEN) | 🔍 Check for context |

**Three documentation purposes:**
- **Core Principles** (PROJECT_STATUS.md §3): High-level values/constraints - guide all development
- **Architecture Patterns** (DEVELOPER_GUIDE.md): Reusable HOW-TO implementation patterns
- **ADRs** (DECISION_LOG.md): Historical record (WHAT/WHY/WHEN)

---

## 3. Core Principles

> **Purpose**: High-level project values and constraints that guide ALL development decisions
> **Tone**: Directive ("MUST", "NEVER", "ALWAYS")
> **NOT for**: Historical context (use DECISION_LOG.md) or implementation details (use DEVELOPER_GUIDE.md)

### Architecture
- Manager pattern for complex business logic (data/ContentManagers/)
- ViewModels handle UI state only, delegate to Managers
- Thin Repositories for simple CRUD only
- Single Source of Truth (SSOT) - each data has ONE authoritative source
- Unified Entities - use behavioral flags vs separate entity classes
- Extract components when >50 lines, self-contained, or might be reused

### State Management
- StateFlow for all observable state (NEVER LiveData)
- ViewModels expose StateFlow<State>, handle events via functions
- Config Over Code - user preferences in Settings classes, not hardcoded

### UI/UX
- NEVER use BottomNavigation - use TopAppBar or NavigationDrawer
- Material 3 spacing constants (4dp/8dp/16dp/24dp/32dp)
- Detail screens use WindowInsets(0,0,0,0)
- Hearth design system - terracotta/clay tones, dark cards for browsing, light for cooking

### Technical
- Android native (Kotlin), Jetpack Compose, offline-first
- NEVER use android.util.Log - use DebugConfig.debugLog()
- Package structure: data/, ui/screens/, ui/components/, ui/theme/, utils/, navigation/

### Development
- NEVER merge directly to main - always create PR
- Update all relevant docs before PR (keep updates 1-3 sentences max)

---

## 4. Completed Features

### Planning & Documentation
- ✅ Product brief (PRODUCT_BRIEF.md)
- ✅ Design principles (../ANDROID_DESIGN_PRINCIPLES.md)
- ✅ Hearth design system (DESIGN_SYSTEM.md)
- ✅ 5-document system (docs/)
- ✅ Git workflow

### Foundation
- ✅ Android project setup with Compose dependencies
- ✅ DebugConfig utility with category-based logging
- ✅ Hearth theme implementation (Color.kt, Type.kt, HearthTheme.kt)
- ✅ Responsive navigation drawer (modal for phone, permanent for tablet)
- ✅ Navigation structure with Screen sealed class
- ✅ MainActivity as orchestrator pattern (setup dependencies, wire components only)
- ✅ Navigation.kt with all NavHost logic separated from MainActivity
- ✅ Screen placeholders (Home, Meal Planning, Grocery Lists, Settings)

### Recipe Management (Phase 1)
- ✅ Recipe entity with Room (title, ingredients, instructions, servings, times, tags, source, photos, notes, behavioral flags)
- ✅ RecipeDao with Flow-based queries (CRUD, search, favorites)
- ✅ RecipeManager for business logic (validation, CRUD, favorite toggle)
- ✅ RecipeViewModel with StateFlow (delegates to Manager)
- ✅ ViewModelFactory for dependency injection
- ✅ RecipeListScreen with cards (servings/times above ingredients per request, favorite toggle, FAB)
- ✅ RecipeDetailScreen (view recipe, edit/delete/favorite actions, BackHandler)
- ✅ AddEditRecipeScreen (single screen form with validation, "SAVE" text button, BackHandler)
- ✅ Full navigation integration (add, edit, detail, list)

---

## 5. Active Backlog

### Phase 0: Setup
- [x] Android project initialization
- [x] Gradle config (Compose, Navigation, Room)
- [x] Material 3 theme (Hearth colors)
- [x] DebugConfig utility
- [x] Navigation drawer (responsive)
- [x] Screen scaffolding
- [ ] AppSettings with StateFlow

### Phase 1: Recipe Management
- [x] Recipe entity/DAO
- [x] RecipeManager
- [x] Recipe CRUD
- [x] Recipe list/detail screens
- [x] Manual entry
- [ ] Recipe search functionality
- [ ] Recipe tags/categories filtering

### Phase 2: Import
- [ ] URL import
- [ ] PDF import
- [ ] Photo-to-recipe (OCR)

### Phase 3: Meal Planning
- [ ] Meal plan entity/manager
- [ ] Weekly planning UI
- [ ] Special event planning

### Phase 4: Grocery Lists
- [ ] List generation from meal plans
- [ ] Ingredient consolidation
- [ ] Shopping UI with check-off

### Phase 5: Advanced
- [ ] Portion scaling
- [ ] Unit conversion
- [ ] Nutritional info
- [ ] Recipe suggestions
- [ ] Cooking mode
- [ ] Substitution guide

### Phase 6: Polish
- [ ] Samsung Quick Share
- [ ] Ratings/favorites
- [ ] Advanced filtering/search
- [ ] Automated tagging

---
