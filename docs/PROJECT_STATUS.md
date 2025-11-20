# Recipe Index Project Status

> **Purpose**: Current status, core principles, completed features, and backlog
> **Last Updated**: 2025-11-20

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

**Current Phase:** Advanced Features (Phase 5) - IN PROGRESS
**Current Focus:** UX improvements (card compaction, icon buttons, context menus), intelligent parsing (canned items), meal plan auto-naming from dates, settings infrastructure

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
- ✅ All drawer screens with TopAppBar and menu button for navigation (Home, Meal Planning, Grocery Lists, Settings, Recipe Index)

### Recipe Management (Phase 1)
- ✅ Recipe entity with Room (title, ingredients, instructions, servings, times, tags, source, photos, notes, behavioral flags)
- ✅ RecipeDao with Flow-based queries (CRUD, search, favorites)
- ✅ RecipeManager for business logic (validation, CRUD, favorite toggle)
- ✅ RecipeViewModel with StateFlow (delegates to Manager)
- ✅ ViewModelFactory for dependency injection
- ✅ RecipeListScreen with cards (servings/times above ingredients per request, favorite toggle, expandable FAB menu for create/import)
- ✅ RecipeDetailScreen (view recipe, edit/delete/favorite actions, BackHandler)
- ✅ AddEditRecipeScreen (single screen form with validation, auto-save on back navigation)
- ✅ Full navigation integration (add, edit, detail, list)

### Recipe Import (Phase 2)
- ✅ RecipeParser interface for extensible parsing (URL/PDF/Photo support)
- ✅ SchemaOrgRecipeParser with Jsoup and Ktor (parses Schema.org JSON-LD markup, HowToStep/HowToSection instructions, ISO 8601 durations, Open Graph fallback, debug logging, main photo extraction, comma-separated tags)
- ✅ TextRecipeParser with smart pattern matching (detects ingredients/instructions sections, filters website noise, validates content, parses times/servings, cleans formatting)
- ✅ PdfRecipeParser with PdfBox-Android (extracts text from PDFs, delegates to TextRecipeParser)
- ✅ PhotoRecipeParser with ML Kit OCR (extracts text from photos/camera, supports multiple images, delegates to TextRecipeParser)
- ✅ ImportSourceSelectionScreen (choose URL/PDF/Photo import source)
- ✅ ImportUrlScreen (URL input, loading state, recipe preview/edit before save, auto-save on back)
- ✅ ImportPdfScreen (file picker, loading state, recipe preview/edit before save)
- ✅ ImportPhotoScreen (camera/gallery pickers, multiple photo support, photo preview, recipe preview/edit before save)
- ✅ ImportViewModel with UI states (Input → Loading → Editing → Saved)
- ✅ ImportPdfViewModel with UI states (SelectFile → Loading → Editing → Saved)
- ✅ ImportPhotoViewModel with UI states (SelectPhoto → Loading → Editing → Saved)
- ✅ Import navigation routes (ImportSourceSelection, ImportUrl, ImportPdf, ImportPhoto)
- ✅ HTTP client setup (Ktor with OkHttp engine, logging)
- ✅ Coil library for async image loading
- ✅ Recipe photo display (list cards and detail screen)
- ✅ Tabbed instruction sections (Slow Cooker, Instant Pot, etc.)

### Meal Planning (Phase 3)
- ✅ MealPlan entity with Room (name, optional date range, recipe IDs, auto-aggregated tags, notes)
- ✅ MealPlanDao with Flow-based queries (CRUD, search)
- ✅ MealPlanManager for business logic (auto-tag aggregation from recipes, special event detection, validation)
- ✅ RecipeTags with 150+ predefined tags (8 categories: Season, Ingredient, Special Event, Dish Type, Cooking Method, Cuisine, Dietary, Time)
- ✅ MealPlanViewModel with StateFlow (delegates to Manager)
- ✅ MealPlanningScreen with card-based list (search, duplicate, delete dialogs, shows all recipes and tags)
- ✅ AddEditMealPlanScreen (flexible date ranges, recipe picker bottom sheet with search, auto-save on back)
- ✅ Full navigation integration (add, edit, list)

### Grocery Lists (Phase 4)
- ✅ GroceryList entity with Room (simple container with id, name, timestamps)
- ✅ GroceryItem entity with Room (list ID FK, name, quantity, unit, checked status, source recipe IDs, notes)
- ✅ GroceryListDao and GroceryItemDao with special queries (item count, checked count, delete checked items)
- ✅ GroceryListManager with intelligent consolidation (removes ignored modifiers like diced/chopped/shredded/sliced, sums quantities for matching name+unit pairs, tracks source recipes)
- ✅ GroceryListViewModel with StateFlow (search, create, update, delete lists and items)
- ✅ GroceryListScreen with card-based list view (progress indicators showing checked/total items, create/delete dialogs, search)
- ✅ GroceryListDetailScreen with quick-entry text field at top (like Out of Milk app), item checkboxes, item detail dialog showing source recipes, bottom actions for clear checked/add recipes/add meal plans
- ✅ GroceryListPickerDialog component (reusable for selecting existing list or creating new)
- ✅ Recipe-to-list integration ("Add to Grocery List" button on recipe cards)
- ✅ Meal plan-to-list integration ("Generate List" button on meal plan cards)
- ✅ Full navigation integration (list of lists, detail view with all functionality)
- ✅ Canned/packaged items parsing ("9 oz can of tomatoes" → qty:1, unit:"can", name:"tomatoes", notes:"9 oz")
- ✅ Item interaction: click to toggle checkbox, long-press for detail dialog
- ✅ Units dropdown in detail dialog (none, cup, tbsp, tsp, oz, lb, g, kg, ml, L, can, pack, bottle, jar)

### User Settings (Phase 5)
- ✅ AppSettings data class with UnitSystem and TemperatureUnit enums
- ✅ SettingsManager with SharedPreferences persistence and StateFlow reactivity
- ✅ UnitConverter utility with volume/weight/temperature conversions (imperial ↔ metric)
- ✅ SettingsViewModel delegates to SettingsManager
- ✅ SettingsScreen with unit system, temperature, display preferences, recipe defaults
- ✅ Full integration into MainActivity and Navigation

### UX Improvements (Phase 5)
- ✅ Recipe cards: Reduced size (image 180dp→140dp, padding 16dp→12dp), smaller typography (titleLarge→titleMedium, bodyMedium→bodySmall)
- ✅ Recipe cards: Tags wrap to multiple lines with custom FlowRow implementation, calendar icon for "Add to Meal Plan", favorite icon only when favorited
- ✅ Recipe cards: Limited to max 3 tags with smart prioritization (cook method > cuisine > ingredients > meal type)
- ✅ Recipe cards: "Add to Grocery List" moved to context menu (3-dot dropdown)
- ✅ Recipe detail: Tags now wrap properly (was squishing horizontally, now uses FlowRow like cards)
- ✅ Recipe detail: "Add to Grocery List" and "Add to Meal Plan" moved inline as icon buttons (was in overflow menu), only Delete remains in overflow
- ✅ Recipe editor: Chip-based tag UI replacing comma-separated input (removable chips + add field for mobile-friendly editing)
- ✅ Meal plan cards: Icon-only buttons (Edit, Generate List), context menu for Duplicate/Delete, entire card clickable to navigate to detail
- ✅ Meal plan detail: Enhanced recipe cards with servings, time, and tags (was simple text list)
- ✅ Meal plan: Auto-populate name from selected dates ("Nov 18-22" format), full Material3 date picker for start/end dates
- ✅ Meal plan picker: Reusable dialog for adding recipes to plans from cards/detail screen (select existing or create new)
- ✅ Date picker: Reusable AppDatePickerDialog component (Material3 DatePicker in ui/components, replaced placeholder "Set Today" dialog)
- ✅ Import screens: Chip-based tag UI on all verification screens (URL/PDF/Photo), notes field removed (user-added only)
- ✅ Import: Tag standardization during parsing (normalizes "italian food"→"italian", removes noise words, deduplicates, 100+ mappings)
- ✅ Import: Tag modification dialog showing original→standardized transformations with user edit/accept controls (prevents silent modifications like "vegan bowls"→"vegan")
- ✅ Import: Tag auto-suggestion from existing tags (appears after 2 chars, click to add, integrated in all import screens)
- ✅ Import: Notes NOT populated during import from any source (user-added only per design)
- ✅ Import: Discard button with confirmation dialog for escaping auto-save
- ✅ Grocery lists: Icon-over-text button pattern for all actions (Select All toggle, Clear, Recipes, Meal Plans), consolidated Select/Deselect into single toggle button
- ✅ Grocery lists: Fixed empty list bug when generating from meal plan (was using broken createListAndReturn)
- ✅ Grocery lists: Fixed Add to Grocery button in meal planner not adding items (addMealPlanToList now returns result)
- ✅ Meal plan cards: Action buttons repositioned inline with date range for compact layout
- ✅ Navigation drawer: Collapse button even in landscape/tablet mode for more screen space (FloatingActionButton to re-expand)
- ✅ Landscape mode: Recipe and meal plan cards display in 2-column grid
- ✅ Error handling: ErrorHandler utility, SnackbarHost for error display, BackHandler for system back button

### Advanced Features (Phase 5)
- ✅ Portion scaling: Servings dropdown in RecipeDetailScreen (half, original, 2x, 3x, 4x) with automatic ingredient quantity scaling
- ✅ Ingredient parsing: IngredientScaler handles fractions (1/2), mixed numbers (1 1/2), decimals, ranges, smart formatting
- ✅ Granular unit preferences: Separate settings for liquid volume (cups/ml) and weight (oz/g) with IMPERIAL/METRIC/BOTH options per category
- ✅ Unit conversions: IngredientUnitConverter automatically formats ingredients based on user's liquid/weight preferences, supports showing both units inline ("1 cup (237 ml)")
- ✅ Recipe selection: Full-screen grid layout for meal plan recipe picker (2 columns, search, selection counter in TopAppBar)
- ✅ Cook mode: Checkable ingredients/instructions, integrated timer (5-60 min), bold numbers in steps, keep screen awake, session-persistent state
- ✅ Substitution guide: IngredientSubstitution entity with Room database, SubstitutionManager with pre-populated defaults (~100 substitutes for 22 common ingredients), SubstitutionGuideScreen with search/filters/quantity conversion, AddEditSubstitutionScreen for user-editable substitutions, long-press ingredient in recipes for substitution lookup dialog, suitability-based ordering, dietary tags (vegan/gluten-free/etc.)
- ✅ Share/Import system: ShareHelper utility with app-to-app JSON format (base64 photo encoding, max 1920px/10MB) and human-readable text fallback, ShareModels (SharePackage/ShareRecipe/ShareMealPlan/ShareGroceryList/ImportResult/DuplicateAction), ImportManager with duplicate detection (title+sourceUrl matching, incremental naming for "Keep Both"), ImportDialog components (RecipeDuplicateDialog/MealPlanImportDialog/GroceryListImportDialog), share buttons in context menus (RecipeCard/MealPlanCard) and action rows (GroceryListCard, GroceryListDetailScreen), Android share sheet integration, manual import from Settings with file picker, share target via intent filters (ACTION_SEND/ACTION_VIEW), supports email/Samsung Quick Share/Messenger/SMS, cook mode spacing tightened (ingredients 4dp→2dp, instructions 8dp→4dp)

---

## 5. Active Backlog

### Phase 0: Setup
- [x] Android project initialization
- [x] Gradle config (Compose, Navigation, Room)
- [x] Material 3 theme (Hearth colors)
- [x] DebugConfig utility
- [x] Navigation drawer (responsive)
- [x] Screen scaffolding
- [x] AppSettings with SettingsManager (StateFlow + SharedPreferences)

### Phase 1: Recipe Management
- [x] Recipe entity/DAO
- [x] RecipeManager
- [x] Recipe CRUD
- [x] Recipe list/detail screens
- [x] Manual entry
- [ ] Recipe search functionality
- [ ] Recipe tags/categories filtering

### Phase 2: Import
- [x] URL import (Schema.org)
- [x] PDF import (PdfBox-Android)
- [x] Photo-to-recipe (ML Kit OCR, multiple photos)
- [ ] **More testing needed**: PDF and Photo import with diverse recipe formats
- [ ] Instructional photos import
- [ ] Recipe videos import

### Phase 3: Meal Planning
- [x] Meal plan entity/manager
- [x] Weekly planning UI
- [x] Special event planning
- [ ] **More testing needed**: Meal planning with various scenarios (date ranges, multiple recipes, tag aggregation)

### Phase 4: Grocery Lists
- [x] List generation from meal plans
- [x] List generation from recipes
- [x] Manual list creation and editing
- [x] Ingredient consolidation (intelligent quantity summing, modifier removal)
- [x] Shopping UI with check-off
- [x] Context menu actions (generate from meal plan, add from recipe)
- [ ] **More testing needed**: Grocery lists with various recipes (different units, fraction parsing, edge cases)

### Phase 5: Advanced
- [x] Settings infrastructure (AppSettings, SettingsManager, SettingsScreen)
- [x] Unit converter utility
- [x] Canned items intelligent parsing
- [x] UX improvements (card compaction, icon buttons, context menus)
- [x] Meal plan auto-naming from dates
- [x] Portion scaling with servings dropdown (half, original, 2x, 3x, 4x servings with smart ingredient scaling)
- [x] Unit conversion display in recipes (toggle to show metric/imperial conversions inline)
- [x] Full-screen recipe selection for meal plans (grid layout, 2/row, search, selection counter)
- [x] Landscape mode: 2 cards per row (recipes/meal plans)
- [x] Granular unit preferences by category (weight, liquid) - Separate settings for liquid volume and weight units
- [x] Cook mode (checkable ingredients/instructions, timer, bold numbers, keep screen awake)
- [x] Substitution guide (search, filters, quantity conversion, long-press from recipes, user-editable with add/edit screen)
- [ ] Nutritional info
- [ ] Recipe suggestions

### Phase 6: Polish
- [ ] Samsung Quick Share
- [x] Ratings/favorites (cooking logs with 5-star rating system, notes, timestamps)
- [ ] Advanced filtering/search
- [ ] Automated tagging
- [x] Preview-first import verification (WYSIWYG recipe preview before save with inline tag editing)

---

## Recent Updates (Latest Session)

### Meal Planning UX Improvements
- ✅ **Simplified Date Entry**: Replaced two separate date buttons with single calendar icon button inline with meal plan name field
  - Uses Material 3 DateRangePicker for selecting single date or range
  - Shows selected dates as supporting text under name field
  - Clear button to remove dates  - Reduces visual clutter and simplifies date selection workflow
- ✅ **Recipe Picker Defaults to Existing**: Changed RecipePickerBottomSheet to open on "Existing" tab (was "Import")
  - Users more commonly select existing recipes than import new ones while planning
  - Import tab still easily accessible with single tap
- ✅ **Grocery List Feedback**: Added snackbar notification when meal plan ingredients added to grocery list
  - Shows success message with meal plan name and list name
  - Confirms operation completed (previously no feedback)
  - Helps users understand that ingredients were successfully added

### Tag Filtering Refinements  - ✅ **Meal Type Noise Words**: Added dinner/dinners, lunch/lunches, breakfast/breakfasts to noiseWords
  - Fixes tags like "high-fiber dinners" → "high-fiber"
  - Standalone meal type tags ("dinner", "lunch") still kept valid
  - Uses "keep original if all words filtered" logic in removeNoiseWords()

### Recipe Creation Flow Simplification
- ✅ **Tabbed Add Recipe Screen**: Consolidated recipe creation into single `ImportSourceSelectionScreen` with 2 tabs
  - **Import tab**: Shows 3 import sources (URL, PDF, Photo)
  - **Create tab**: Button to create recipe manually
  - FAB now goes directly to this screen (removed expandable FAB menu)
  - Reduces clicks and provides cleaner, more intuitive flow

### Tag Filtering & Quality Improvements
- ✅ **Schema.org Keywords Removed**: Stopped parsing `keywords` field which contained garbage phrases ("easy slow cooker chicken recipe", recipe titles, etc)
- ✅ **HTML Category Parsing**: Added parsing of `<a rel="category">` and `<a rel="tag">` links from WordPress/CMS (captures legitimate categories like "Low Carb")
- ✅ **Comprehensive Tag Filters**: Enhanced `TagStandardizer` with multiple filter layers
  - Standalone noise words: "recipes", "meals", "ideas", "cooking", "dinner ideas"
  - Junk phrases: "how to make", "for beginners", "best recipes"
  - Long tags: >4 words automatically filtered as junk
  - Branded diets: "Weight Watchers", "WW" removed
- ✅ **Holiday Consolidation**: All holidays now map to "special occasion" (Valentine's, Christmas, Thanksgiving, etc)
- ✅ **Ingredient Consolidation**: Specific cuts map to general protein to avoid duplicates
  - "chicken thigh" → "chicken"
  - "chicken breast" → "chicken"
  - "ground beef" → "beef"
- ✅ **Consistent Mappings**: Standardized air fryer, slow cooker, freezer-friendly, Whole30→paleo, egg-free, dairy-free

### Tag Management UI
- ✅ **Remove Button in Dialog**: Added minus icon button to `TagModificationDialog` to delete tags without accepting first
  - Visual feedback: deleted tags show in red with strikethrough
  - Restore button to undo deletion
  - Eliminates need to accept then manually remove tags

### Recipe List Improvements
- ✅ **Delete from Card Menu**: Added "Delete" option to recipe card context menu (⋮ button)
  - Shows in red with trash icon
  - Allows deleting recipes directly from list view without opening detail screen

### Previous Session Features

#### Import & Preview Enhancements
- ✅ **Preview-First Import UI**: Replaced text-field-heavy edit screen with `RecipePreviewContent` - clean, card-based layout showing recipe as it will appear
  - Inline tag editing with auto-suggestions directly in preview
  - Edit dialogs for title, metadata, ingredients, and instructions
  - Shows first 5 ingredients, first 3 instruction steps
  - Prominent "Save Recipe" button with clear validation messages
  - WYSIWYG experience - users see final result while making quick edits

#### Meal Planning Workflow Improvements
- ✅ **Tabbed Recipe Picker**: Simplified meal plan creation with two-tab interface in `RecipePickerBottomSheet`
  - **Import tab** (default): Shows URL/PDF/Photo import options for importing recipes directly while creating meal plan
  - **Existing tab**: Shows existing recipes to select from
  - Reduces clicks significantly - no more navigating between screens to import then select
  - After import, automatically switches to Existing tab to select newly imported recipe

#### Tag Management Improvements
- ✅ **Tag Modification Dialog**: Import screens show detailed dialog when tags are standardized, displaying original→standardized transformations
- ✅ **Tag Auto-Suggestion**: Import screens suggest existing tags as you type (appears after 2 characters), with smart filtering to avoid duplicates/subsets
- ✅ **Tag Tracking**: Added `TagStandardizer.standardizeWithTracking()` method returning `TagModification` objects
- ✅ **Tag Standardization Logging**: Comprehensive logging of all tag transformations (original → normalized → mapped → final, filtered tags, duplicates removed)
  - Filter Logcat by tag `RecipeIndex` + search `TAG_STANDARDIZATION` to review imports
  - Helps identify patterns for improving standardizer mappings and noise words

#### UI/UX Enhancements
- ✅ **Reusable Date Picker**: Created `AppDatePickerDialog` component (Material3 DatePicker) in `ui/components/`
- ✅ **Fixed Meal Planner Date Picker**: Replaced placeholder "Set Today" dialog with full-featured Material3 date picker
- ✅ **Icon-Over-Text Buttons**: Grocery list detail screen uses consistent icon-over-text pattern for all actions
- ✅ **Smart Toggle Button**: Consolidated "Select All" and "Deselect All" into single toggle that changes based on state
- ✅ **Compact Meal Plan Cards**: Action buttons (Edit, Generate List) now appear inline with date range

#### Bug Fixes
- ✅ **Grocery List Generation**: Fixed `addMealPlanToList()` not returning result
- ✅ **Grocery List Crash**: Fixed NullPointerException when adding meal plan to grocery list (captured variables before async callback cleared them)
- ✅ **Build Errors**: Fixed `Icons.Default.Star` references to use `Icons.Default.Favorite`
- ✅ **Flow Handling**: Fixed `getAllExistingTags()` to properly collect from Flow using `.first()`

---
