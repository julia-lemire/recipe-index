# Recipe Index Developer Guide

> **Purpose**: Quick lookup ("I need to...") and architecture patterns (HOW to implement)
> **Last Updated**: 2025-11-18

**See Also:**
- [DECISION_LOG.md](./DECISION_LOG.md) - Architectural decision records (WHAT/WHY/WHEN decisions were made)
- [FILE_CATALOG.md](./FILE_CATALOG.md) - Complete file tree, system relationships, and component descriptions
- [PROJECT_STATUS.md](./PROJECT_STATUS.md) - Current status, core principles, completed features, and backlog
- [TEST_SCENARIOS.md](./TEST_SCENARIOS.md) - Test coverage and scenarios to implement for automated testing
- [PROJECT_CONVENTIONS.md](../PROJECT_CONVENTIONS.md) - How to maintain documentation

**Quick Navigation:** [How to Update](#how-to-update-this-file) | [Quick Lookup](#quick-lookup-i-need-to) | [Patterns](#architecture-patterns) | [Back Button](#system-back-button-pattern) | [Save Button](#save-button-pattern)

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

### Work with Recipes
- **Manager**: `data/ContentManagers/RecipeManager.kt`
- **Entity**: `data/entities/Recipe.kt`
- **DAO**: `data/dao/RecipeDao.kt`
- **Screens**: `ui/screens/RecipeListScreen.kt`, `ui/screens/RecipeDetailScreen.kt`

### Work with Meal Plans
- **Manager**: `data/ContentManagers/MealPlanManager.kt`
- **Entity**: `data/entities/MealPlan.kt`
- **DAO**: `data/dao/MealPlanDao.kt`
- **Screen**: `ui/screens/MealPlanningScreen.kt`

### Work with Grocery Lists
- **Manager**: `data/ContentManagers/GroceryListManager.kt`
- **Entity**: `data/entities/GroceryList.kt`
- **Screen**: `ui/screens/GroceryListScreen.kt`

### Import Recipes
- **URL Import**: `data/importers/UrlRecipeImporter.kt`
- **PDF Import**: `data/importers/PdfRecipeImporter.kt`
- **Photo Import**: `data/importers/PhotoRecipeImporter.kt` (OCR)

### Handle App Settings
- **Settings**: `data/AppSettings.kt`
- **StateFlow-based**: Exposes preferences as StateFlow for reactive UI


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

### Manager Pattern
**Use when**: Complex business logic needed (CRUD + rules/validation/coordination)
**Structure**:
- Manager in `data/ContentManagers/` (e.g., RecipeManager)
- ViewModel delegates all business logic to Manager
- Manager handles state coordination, validation, multi-step operations
- Thin Repositories for simple CRUD only

**Example**: RecipeManager handles recipe import, tag extraction, duplicate detection; RecipeViewModel just exposes StateFlow<RecipeListState>

### Config Over Code
**Use when**: Any value that might change or vary by user preference
**Structure**:
- Settings class with `MutableStateFlow<T>` for each preference
- ViewModels/Managers read from Settings StateFlow
- Never hardcode values like "max ingredients = 50"

**Example**: `AppSettings.defaultServings: StateFlow<Int>` instead of hardcoded constant

### Single Source of Truth (SSOT)
**Use when**: Always - each piece of data has ONE authoritative source
**Structure**:
- Database is SSOT for persisted data
- StateFlow in Manager/ViewModel is SSOT for UI state
- Never duplicate state across multiple StateFlows

**Example**: RecipeManager exposes `recipes: StateFlow<List<Recipe>>` from Room, UI observes this only

### Unified Entities
**Use when**: Multiple types share same database table and most fields
**Structure**:
- Single entity class with behavioral flags (e.g., `isTemplate: Boolean`)
- Avoid separate entity classes that duplicate fields
- Use `when` statements on flags for type-specific behavior

**Example**: Recipe entity with `isTemplate` flag vs separate RecipeTemplate entity class

### System Back Button Pattern
**Use when**: Any screen with back/cancel navigation (detail screens, forms, dialogs)
**Structure**:
- Import `androidx.activity.compose.BackHandler`
- Add `BackHandler { onBack() }` or `BackHandler { onCancel() }` before Scaffold
- TopAppBar navigationIcon uses `ArrowBack` icon with same callback

**Example**: `BackHandler { onCancel() }` in AddEditRecipeScreen calls same function as navigationIcon

### Save Button Pattern
**Use when**: Forms or edit screens with primary save action
**Structure**:
- Use `TextButton` with "SAVE" text in TopAppBar actions (not icon-only button)
- Place in TopAppBar actions slot (top right)
- Include validation before calling onSave callback

**Example**: AddEditRecipeScreen uses `TextButton { Text("SAVE") }` instead of `IconButton { Icon(Icons.Default.Check) }`


---
