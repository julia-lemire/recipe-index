# Recipe Index Decision Log

> **Purpose**: Architectural decision records (WHAT/WHY/WHEN decisions were made)
> **Last Updated**: 2025-11-18

**See Also:**
- [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) - Quick lookup ("I need to...") and architecture patterns (HOW to implement)
- [FILE_CATALOG.md](./FILE_CATALOG.md) - Complete file tree, system relationships, and component descriptions
- [PROJECT_STATUS.md](./PROJECT_STATUS.md) - Current status, core principles, completed features, and backlog
- [TEST_SCENARIOS.md](./TEST_SCENARIOS.md) - Test coverage and scenarios to implement for automated testing
- [PROJECT_CONVENTIONS.md](../PROJECT_CONVENTIONS.md) - How to maintain documentation

**Quick Navigation:** [How to Update](#how-to-update-this-file) | [Decision Records](#architectural-decision-records)

---

## How to Update This File

### When you make an architectural decision:
1. **Add new ADR** at top of [Architectural Decision Records](#architectural-decision-records) (reverse chronological)
2. **Format:** `#### MMM DD, YYYY: [Title]` with Decision/Rationale/Implementation bullets (1 sentence each)
3. **Update PROJECT_STATUS.md** §3 Core Principles if new pattern introduced
4. **Add to DEVELOPER_GUIDE.md** if decision establishes reusable HOW-TO pattern

### What goes in an ADR:
- ✅ Architectural choices (Manager pattern, StateFlow vs LiveData, navigation approach)
- ✅ Design system decisions (Hearth theme, color palette)
- ✅ Technology selections (Room, Compose, no cloud sync)
- ✅ Structural patterns (Config Over Code, Unified Entities)

### What does NOT go here:
- ❌ Feature implementations (those go in PROJECT_STATUS.md §4 Completed Features)
- ❌ Bug fixes (unless architecturally significant)
- ❌ Implementation details (those go in code comments)
- ❌ File creation logs (those go in FILE_CATALOG.md)

### Format Guidelines:
- Keep each bullet to 1 sentence maximum
- Date format: `MMM DD, YYYY` (e.g., Nov 18, 2025)
- Newest entries first

---

## Architectural Decision Records

> **Organization**: Newest entries first (reverse chronological order)
> **Keep it concise**: 1 sentence per field (Decision/Rationale/Implementation)

#### Nov 18, 2025: Auto-Save on Back Navigation
- **Decision**: Auto-save form data when navigating back instead of manual save button; skip save if form is empty, validate before saving
- **Rationale**: Reduces friction by eliminating manual save step while preventing data loss; aligns with modern mobile UX patterns
- **Implementation**: AddEditRecipeScreen handleBack() checks for content, validates, auto-saves if valid; shows errors and blocks navigation if invalid

#### Nov 18, 2025: MainActivity as Orchestrator Pattern
- **Decision**: MainActivity only sets up dependencies and wires components - navigation logic in Navigation.kt, business logic in Managers
- **Rationale**: Keeps MainActivity clean and focused, easier to test navigation independently, follows single responsibility principle
- **Implementation**: MainActivity creates AppDatabase/RecipeManager/ViewModelFactory, calls Navigation.kt composable; AppNavigationDrawer accepts content parameter

#### Nov 18, 2025: Responsive Navigation Drawer
- **Decision**: Modal drawer for phones (Samsung S23 Ultra), permanent drawer for tablets (Samsung Galaxy Tab S10+) using WindowSizeClass
- **Rationale**: Tablet's larger screen benefits from always-visible navigation, phone needs max content space with drawer overlay
- **Implementation**: AppNavigationDrawer checks WindowWidthSizeClass.Expanded for drawer type, NavGraph sealed class with routes/icons

#### Nov 18, 2025: Never Use BottomNavigation
- **Decision**: TopAppBar or NavigationDrawer only - BottomNavigation prohibited
- **Rationale**: Screen space critical for recipe content (ingredient lists, instructions, images)
- **Implementation**: Navigation rule added to ../ANDROID_DESIGN_PRINCIPLES.md with code examples

#### Nov 18, 2025: Offline-First Architecture with Local Storage Only
- **Decision**: Room database, no cloud sync, Samsung Quick Share for recipe sharing
- **Rationale**: User wants recipes available without internet, simplified privacy model
- **Implementation**: Room database for all entities, Quick Share integration for export/import

#### Nov 18, 2025: Manager Pattern for Complex Business Logic
- **Decision**: Business logic in `data/ContentManagers/`, ViewModels delegate to Managers
- **Rationale**: Keeps ViewModels thin (UI state only), enables reusable business logic, testable
- **Implementation**: RecipeManager, MealPlanManager handle all CRUD and business rules

#### Nov 18, 2025: StateFlow Over LiveData
- **Decision**: All observable state uses StateFlow, never LiveData
- **Rationale**: Modern coroutines-first approach, better Compose integration with `collectAsState()`
- **Implementation**: ViewModels expose `StateFlow<State>`, Settings classes use `MutableStateFlow`

#### Nov 18, 2025: Extract Components Proactively
- **Decision**: Extract when >50 lines, self-contained, or might be reused (not just 2+ uses)
- **Rationale**: Keeps files small from start, reduces need for future refactoring
- **Implementation**: Component extraction rule in ../ANDROID_DESIGN_PRINCIPLES.md

#### Nov 18, 2025: Hearth Design System
- **Decision**: Terracotta/clay tones (#E8997A, #2C1810), dark cards for browsing, light for cooking
- **Rationale**: Warm, home-kitchen aesthetic differentiates from clinical recipe apps
- **Implementation**: DESIGN_SYSTEM.md with complete Material 3 theme specification

#### Nov 18, 2025: Config Over Code Pattern
- **Decision**: User preferences in Settings classes with StateFlow, not hardcoded values
- **Rationale**: Enables runtime configuration changes, better testability, user customization
- **Implementation**: AppSettings class for global prefs, category-specific Settings where needed

---
