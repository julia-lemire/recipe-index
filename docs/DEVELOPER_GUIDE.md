# Recipe Index Developer Guide

> **Purpose**: Quick lookup ("I need to...") and architecture patterns (HOW to implement)
> **Last Updated**: 2025-11-21

**See Also:**
- [DECISION_LOG.md](./DECISION_LOG.md) - Architectural decision records (WHAT/WHY/WHEN decisions were made)
- [FILE_CATALOG.md](./FILE_CATALOG.md) - Complete file tree, system relationships, and component descriptions
- [PROJECT_STATUS.md](./PROJECT_STATUS.md) - Current status, core principles, completed features, and backlog
- [TEST_SCENARIOS.md](./TEST_SCENARIOS.md) - Test coverage and scenarios to implement for automated testing
- [PROJECT_CONVENTIONS.md](../PROJECT_CONVENTIONS.md) - How to maintain documentation

**Quick Navigation:** [How to Update](#how-to-update-this-file) | [Quick Lookup](#quick-lookup-i-need-to) | [Patterns](#architecture-patterns) | [Back Button](#system-back-button-pattern) | [Auto-Save](#auto-save-pattern)

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
- **Entity**: `data/entities/Recipe.kt` (includes servingSize for portion size e.g. "1 ½ cups", "200g", cuisine field for filtering and display)
- **DAO**: `data/dao/RecipeDao.kt` (searchRecipes() includes cuisine in LIKE query)
- **Screens**: `ui/screens/RecipeListScreen.kt` (displays cuisine as first tag in cards, shows cuisine in list view info row), `ui/screens/RecipeDetailScreen.kt` (servingSize in info row), `ui/screens/AddEditRecipeScreen.kt` (servingSize and cuisine TextFields after servings), `ui/screens/ImportUrlScreen.kt` (servingSize in metadata dialog), `ui/screens/ImportPdfScreen.kt` (servingSize field)

### Work with Meal Plans
- **Manager**: `data/ContentManagers/MealPlanManager.kt`
- **Entity**: `data/entities/MealPlan.kt`
- **DAO**: `data/dao/MealPlanDao.kt`
- **Screen**: `ui/screens/MealPlanningScreen.kt`

### Work with Grocery Lists
- **Manager**: `data/ContentManagers/GroceryListManager.kt`
- **Entity**: `data/entities/GroceryList.kt`
- **Screen**: `ui/screens/GroceryListScreen.kt`

### Work with Ingredient Substitutions
- **Manager**: `data/managers/SubstitutionManager.kt`
- **Entity**: `data/entities/IngredientSubstitution.kt`
- **Support Class**: `data/Substitute.kt` (serializable data class for substitute options)
- **DAO**: `data/dao/SubstitutionDao.kt`
- **ViewModel**: `ui/viewmodels/SubstitutionViewModel.kt`
- **Screens**: `ui/screens/SubstitutionGuideScreen.kt`, `ui/screens/AddEditSubstitutionScreen.kt`
- **Dialog**: `ui/components/SubstitutionDialog.kt` (for recipe ingredient lookup)
- **Defaults**: `utils/SubstitutionData.kt` (pre-populated ~100 substitutes)

### Import Recipes
- **Parser Interface**: `data/parsers/RecipeParser.kt`
- **URL Import Controller**: `data/parsers/UrlRecipeParser.kt`
  - Orchestrates cascading supplementation strategy: Schema.org JSON-LD → HTML scraping → Open Graph
  - Each parser supplements missing data without overwriting data from previous parsers
  - Enables saving partial recipes (e.g., ingredients without instructions) with metadata from multiple sources
  - Delegates to specialized parsers: SchemaOrgRecipeParser, HtmlScraper, OpenGraphParser
  - Handles HTTP fetching with Ktor client
- **Schema.org Parser**: `data/parsers/SchemaOrgRecipeParser.kt`
  - Handles nested JSON structures via recursive parseJsonArrayToStrings
  - Extracts from text/name/@value fields in Schema.org objects
  - Handles @type as string or array (["Recipe", "Article"])
  - Detects Article/BlogPosting types with embedded recipe data (recipeIngredient/recipeInstructions fields)
  - Extracts cuisine from titles when recipeCuisine is missing/incorrect (100+ cuisines)
- **HTML Scraper**: `data/parsers/HtmlScraper.kt`
  - Searches for ingredients/instructions using CSS selectors when structured data unavailable
  - Returns data if ingredients OR instructions found (saves whatever content is available)
  - Uses copy() merging in UrlRecipeParser to supplement with Open Graph metadata
- **Open Graph Parser**: `data/parsers/OpenGraphParser.kt`
  - Supplements missing metadata (title/description/image) from og: meta tags
  - Used to fill gaps when Schema.org or HTML scraping miss certain fields
- **Import Screens**: `ui/screens/ImportSourceSelectionScreen.kt`, `ui/screens/ImportUrlScreen.kt`
- **ViewModel**: `ui/viewmodels/ImportViewModel.kt` (auto-normalizes URLs: http:// → https://, adds https:// when missing)
- **Tag Processing**: `utils/TagStandardizer.kt` (removes subset tags, standardizes variations, filters junk)
- **Debugging**: Enable [IMPORT] logs in DebugConfig to see cascading supplementation attempts, JSON-LD parsing progress, HTML scraping results, Open Graph supplementation, field types, extraction results, URL normalization, and tag value logging

### Share/Import Content
- **Share Helper**: `utils/ShareHelper.kt` (shareRecipe/shareMealPlan/shareGroceryList, photo encoding/decoding, duplicate detection)
- **Models**: `utils/ShareModels.kt` (SharePackage, ImportResult, DuplicateAction)
- **Import Manager**: `data/managers/ImportManager.kt` (importFromJson with duplicate detection, importRecipeWithAction, importMealPlanFromJson, importGroceryListFromJson)
- **Import Dialogs**: `ui/components/ImportDialog.kt` (RecipeDuplicateDialog, MealPlanImportDialog, GroceryListImportDialog)
- **Manual Import (Multiple Entry Points)**:
  - `ui/screens/ImportSourceSelectionScreen.kt` (4th card "From File" in Import tab - primary location)
  - `ui/screens/MealPlanningScreen.kt` (FileUpload icon button in TopAppBar)
  - `ui/screens/GroceryListScreen.kt` (FileUpload icon button in TopAppBar)
  - `ui/screens/SettingsScreen.kt` (backup catch-all location)
  - All use OpenDocument contract, store JSON in MainActivity.pendingImportJson, show snackbar feedback
- **Intent Handling**: `ui/MainActivity.kt` (handleIncomingIntent for ACTION_SEND/ACTION_VIEW, AndroidManifest intent filters)
- **Share Buttons**: RecipeCard/MealPlanCard context menus, GroceryListCard action row, GroceryListDetailScreen top bar

### Filter, Sort, and Group Lists
- **Core Library**: `utils/filtersort/core/` (Filter<T>, Sort<T>, GroupBy<T,K>, FilterSortGroupManager)
- **Recipe Implementations**: `utils/filtersort/recipe/` (RecipeFilters.kt with FavoriteFilter/TagFilter/CuisineFilter/SourceFilter/CookTimeFilter/ServingsFilter/HasPhotoFilter/HasNotesFilter, RecipeSorts.kt, RecipeGroupings.kt)
- **Meal Plan Implementations**: `utils/filtersort/mealplan/` (MealPlanFilters.kt with ContainsRecipeFilter for finding plans with specific recipes, MealPlanSorts.kt, MealPlanGroupings.kt)
- **Grocery List Implementations**: `utils/filtersort/grocerylist/` (GroceryListFilters.kt, GroceryListSorts.kt, GroceryListGroupings.kt)
- **UI Components**: `utils/filtersort/ui/` (FilterChipRow with horizontal scroll and clear all, SortMenu with dropdown and direction toggle)
- **Usage**: Create FilterSortGroupManager(sourceItems flow, searchPredicate, scope) in ViewModel, expose filterSortManager publicly, UI collects filteredItems/groupedItems StateFlow, UI uses toggleFilter/setSort/setGroupBy functions, add convenience methods in ViewModel delegating to filterSortManager
- **UI Integration**: Add FilterChipRow below search bar/TopAppBar, add SortMenu in TopAppBar actions, collect val items by viewModel.filterSortManager.filteredItems.collectAsState()
- **Extractable**: Zero app dependencies, 100% type-safe generics, ready to extract as standalone library

### Handle App Settings
- **Settings**: `data/AppSettings.kt`
- **StateFlow-based**: Exposes preferences as StateFlow for reactive UI

### Handle Errors and User Messages
- **Utility**: `utils/ErrorHandler.kt`
- **Pattern**: See [Error Handling with Snackbar Pattern](#error-handling-with-snackbar-pattern)
- **Components**: SnackbarHostState, SnackbarHost (Material 3), LaunchedEffect for monitoring

### Debug and Analyze
- **Debug Logging**: `utils/DebugConfig.kt` - Centralized logging with category-based filtering
- **Tag Standardization Analysis**: Filter Logcat by tag `RecipeIndex` and search `TAG_STANDARDIZATION` to see all tag transformations during import (original → normalized → mapped → final, filtered tags, duplicates, saved tags)
- **Categories**: NAVIGATION, DATABASE, IMPORT, UI, MANAGER, SETTINGS, GENERAL, TAG_STANDARDIZATION
- **Pattern**: See [Tag Standardization with Tracking Pattern](#tag-standardization-with-tracking-pattern)

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

### Auto-Save Pattern
**Use when**: Forms or edit screens where user expects automatic saving
**Structure**:
- Create handleBack() function that checks for content, validates, and auto-saves
- Skip save if form is empty (no meaningful content entered)
- Show validation errors and block navigation if data is invalid
- Only navigate back on successful save or empty form
- Wire handleBack() to both BackHandler and navigationIcon

**Example**: AddEditRecipeScreen handleBack() checks `hasContent`, validates fields, saves if valid, or shows error and stays on screen

### Flow Loading State Pattern
**Use when**: Loading data from Room database using Flow
**Structure**:
- Set `_isLoading.value = true` before collecting Flow
- Set `_isLoading.value = false` INSIDE collect block after first emission
- Also set false in catch block to handle errors
- Flow.collect() never completes (keeps listening), so finally block never executes

**Example**: RecipeViewModel loadRecipes() sets loading false inside collect block, not in finally

### Error Handling with Snackbar Pattern
**Use when**: Any screen that needs to display errors or messages to the user
**Structure**:
- Create `val snackbarHostState = remember { SnackbarHostState() }` at composable top level
- Add `LaunchedEffect(uiState)` to monitor ViewModel error states and call `snackbarHostState.showSnackbar()`
- Add `snackbarHost = { SnackbarHost(snackbarHostState) }` parameter to Scaffold
- Use `ErrorHandler.getErrorMessage(throwable)` for user-friendly error text
- Combine with BackHandler for validation errors that should prevent navigation

**Example**: ImportUrlScreen monitors `ImportViewModel.UiState` with LaunchedEffect, shows errors via SnackbarHost at bottom of screen, BackHandler validates before navigating back

### Success Feedback with Snackbar Pattern
**Use when**: Async operations need confirmation feedback (e.g., adding to grocery list, saving data)
**Structure**:
- Create `val snackbarHostState = remember { SnackbarHostState() }` and `val scope = rememberCoroutineScope()` at composable top level
- Add `snackbarHost = { SnackbarHost(snackbarHostState) }` parameter to Scaffold
- Pass success callback to ViewModel operation: `viewModel.operation() { /* success */ }`
- Inside success callback, launch coroutine: `scope.launch { snackbarHostState.showSnackbar("Success message", duration = SnackbarDuration.Short) }`
- Keep messages concise and specific about what succeeded

**Example**: MealPlanningScreen shows "Added [meal plan name] ingredients to [list name]" after addMealPlanToList() completes

**Important**: When using variables in async callbacks, capture them in local variables before clearing state:
```kotlin
// BAD - will crash when callback runs
val planName = plan!!.name
showDialog = false
plan = null
viewModel.operation() {
    showSnackbar("Added $planName") // planName references plan which is now null!
}

// GOOD - capture before clearing
val planId = plan!!.id
val planName = plan!!.name
showDialog = false
plan = null
viewModel.operation() {
    showSnackbar("Added $planName") // uses captured local variable
}
```

### Date Range Picker Pattern
**Use when**: User needs to select single date or date range (meal plans, events, filtering)
**Structure**:
- Create DateRangePickerDialog composable with `rememberDateRangePickerState()`
- Use Material 3 `DateRangePicker` component with title and headline customization
- Include Clear button when dates already selected: `if (initialStartDate != null || initialEndDate != null)`
- Return both start and end dates via callback: `onDatesSelected: (startDate: Long?, endDate: Long?) -> Unit`
- Show selected dates as supporting text or secondary display near input field

**Example**: AddEditMealPlanScreen shows calendar icon button inline with name field, opens DateRangePickerDialog, displays selected dates as supporting text under name field

### Tag Standardization with Tracking Pattern
**Use when**: Importing or processing user-generated tag data that needs normalization
**Structure**:
- Use `TagStandardizer.standardize()` for silent standardization (returns List<String>)
- Use `TagStandardizer.standardizeWithTracking()` when user needs visibility into changes (returns List<TagModification>)
- Show TagModificationDialog for tracked changes, allowing per-tag editing/removal before accepting
- Store `tagModifications` in ViewModel UiState to trigger dialog display
- Apply accepted tags via ViewModel update function
- All standardization is automatically logged via `DebugConfig.Category.TAG_STANDARDIZATION` for analysis

**Filtering Layers** (applied in order):
1. Normalize (lowercase, trim, remove special chars)
2. Apply standard mappings (variations → canonical forms, holidays → "special occasion", ingredient types → general)
3. Remove noise words ("recipe", "meal", "dish", "ideas", etc.)
4. Filter junk tags (standalone noise, junk phrases like "how to make", >4 words, branded terms)
5. Deduplicate

**Example**: ImportViewModel calls `TagStandardizer.standardizeWithTracking()`, stores modifications in `UiState.Editing`, ImportUrlScreen shows TagModificationDialog with original→standardized arrows and remove buttons, user can edit/remove each tag before accepting

**Debugging Tag Standardization**:
To analyze tag transformations and improve standardizer mappings, filter Android Studio Logcat by:
- **Tag**: `RecipeIndex`
- **Search**: `TAG_STANDARDIZATION` for tag processing details
- **Search**: `TAG_DIALOG` for dialog lifecycle and user interactions (composition, accept, dismiss)
- **Search**: `TAG_DIALOG_TRIGGER` for LaunchedEffect triggers and state changes
- **Search**: `TAG_DIALOG_SHOW` for when dialog displays and callbacks invoke

Tag processing logs show: recipe name, original tags, step-by-step transformations (normalized → mapped → final), filtered tags with reasons, duplicates removed, and final saved tags

Dialog logs help diagnose issues with multiple dialogs appearing or edits not being saved

### Tabbed Screen Pattern
**Use when**: Single screen needs to present multiple related but distinct content views or workflows
**Structure**:
- Use Material 3 TabRow with Tab composables for navigation
- Store selectedTab index in remember { mutableStateOf(0) }
- Use when expression to show content based on selectedTab
- Extract tab content into private @Composable functions (e.g., ImportTabContent, CreateTabContent)
- Use icons + text for tab clarity on mobile
- Default to most common use case tab (index 0)

**Example**: ImportSourceSelectionScreen has 2 tabs (Import showing URL/PDF/Photo cards, Create showing manual entry), TabRow with CloudDownload and Add icons, when(selectedTab) switches between ImportTabContent and CreateTabContent functions, provides unified entry point for recipe creation

### Reusable Component Pattern
**Use when**: Multiple screens need the same UI component (dialogs, pickers, specialized inputs)
**Structure**:
- Create component in `ui/components/` directory
- Use clear, descriptive naming (e.g., `AppDatePickerDialog`, `TagModificationDialog`)
- Accept necessary state as parameters, emit actions via callbacks
- Keep components stateless when possible (parent manages state)
- Document parameters and usage in file header comment

**Example**: AppDatePickerDialog in `ui/components/DatePickerDialog.kt` accepts `initialDate/onDateSelected/onDismiss`, used by AddEditMealPlanScreen for start/end date selection, replaces inline date picker implementations

### Icon-Over-Text Button Pattern
**Use when**: Bottom action bars with 3-5 primary actions need clear labeling on mobile
**Structure**:
- Row with SpaceEvenly arrangement
- Each action is clickable Column (weight=1f) containing: Icon(24dp) + Spacer(4dp) + Text(labelSmall)
- Use conditional tint for disabled states (onSurfaceVariant.copy(alpha=0.38f))
- Use conditional icon/text for toggle states (e.g., Select All ↔ Deselect)
- Wrap in Card with elevation for visual separation from content

**Example**: GroceryListDetailScreen bottom actions use icon-over-text for Select All toggle, Clear, Recipes, and Meal Plans buttons, providing large touch targets with clear labels

### Text/PDF Parsing Pattern
**Use when**: Extracting structured recipe data from unstructured text (PDFs, OCR results)
**Structure** (TextRecipeParser):
- Detect section boundaries via regex (Ingredients/Instructions/Notes headers)
- Filter noise: `isWebsiteNoise()` (CTAs, ratings, marketing), `isPdfPageNoise()` (URLs, page headers)
- Join continuation lines: `joinInstructionLines()` for numbered steps, `joinParagraphLines()` for prose
- Validate content: `looksLikeIngredient()`, `looksLikeInstruction()` with pattern matching
- Clean content: Remove bullets/numbering but preserve quantities

**Key insight**: PDF text extraction splits long lines across multiple rows. Lines not starting with a digit continue the previous instruction. URLs and page headers (`11/18/25, 12:34 PM...`) must be filtered.

**Example**: "3 Transfer to pan..." + "everything halfway." → joined as one instruction step. Tips section joins lines into coherent paragraphs.

---
