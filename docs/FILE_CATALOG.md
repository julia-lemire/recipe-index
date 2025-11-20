# Recipe Index File Catalog

> **Purpose**: Complete file tree, system relationships, and component descriptions
> **Last Updated**: 2025-11-20

**See Also:**
- [DECISION_LOG.md](./DECISION_LOG.md) - Architectural decision records (WHAT/WHY/WHEN decisions were made)
- [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) - Quick lookup ("I need to...") and architecture patterns (HOW to implement)
- [PROJECT_STATUS.md](./PROJECT_STATUS.md) - Current status, core principles, completed features, and backlog
- [TEST_SCENARIOS.md](./TEST_SCENARIOS.md) - Test coverage and scenarios to implement for automated testing
- [PROJECT_CONVENTIONS.md](../PROJECT_CONVENTIONS.md) - How to maintain documentation

**Quick Navigation:** [How to Update](#how-to-update-this-file) | [File Tree](#complete-file-tree) | [System Relationships](#system-relationships) | [Component Details](#component-details-by-layer)

**System Flows:** [Recipe Management](#recipe-management-flow) | [Meal Planning](#meal-planning-flow) | [Grocery Lists](#grocery-list-flow) | [Import](#import-flow)

---

## How to Update This File

### When you create a new file:
1. **Add to File Tree** under appropriate package/folder (keep alphabetical within folders)
2. **Add to Component Details** under appropriate layer section (data/, ui/, utils/, cpp/)
3. **Add to System Relationships** if file participates in a major flow
4. **Include**: Purpose (1 sentence), key capabilities, dependencies, used by
5. **Update DEVELOPER_GUIDE.md** if file provides user-facing capability for Quick Lookup

### When you modify a file's purpose:
1. **Update Component Details** description and capabilities
2. **Update System Relationships** if dependencies changed
3. **Update DEVELOPER_GUIDE.md** if it affects Quick Lookup tasks

### When you delete a file:
1. **Remove from File Tree**
2. **Remove from Component Details**
3. **Remove from System Relationships**
4. **Update DEVELOPER_GUIDE.md** if it was referenced in Quick Lookup
5. **Check for broken dependencies** in other components

### What goes here:
- ✅ ALL application source files (data/, ui/, utils/, cpp/)
- ✅ Detailed component descriptions with dependencies
- ✅ "Used by" and "Depends on" relationships
- ✅ Complete file tree structure matching actual codebase

### What does NOT go here:
- ❌ Build artifacts or generated files
- ❌ Gradle configuration files (unless relevant to architecture)
- ❌ Temporary or experimental files
- ❌ Architecture patterns (those go in DEVELOPER_GUIDE.md)
- ❌ Design decisions (those go in DECISION_LOG.md)

### Format Guidelines:
- File descriptions: 1-2 sentences max
- Key capabilities: Bullet list, 3-5 items
- Dependencies: List only direct dependencies
- Keep alphabetical within each section
- Use consistent formatting: `file/path/Name.kt` - Description

---

## Complete File Tree

**Quick Links**: Jump to component descriptions: [Recipes](#recipes) | [Meal Plans](#meal-plans) | [Grocery Lists](#grocery-lists) | [Importers](#importers) | [Settings](#settings) | [Database](#database) | [UI Screens](#ui-screens) | [UI Components](#ui-components) | [Theme](#theme) | [Utils](#utils)

```
com.recipeindex.app/
├── data/
│   ├── dao/
│   │   ├── GroceryItemDao.kt
│   │   ├── GroceryListDao.kt
│   │   ├── MealPlanDao.kt
│   │   ├── RecipeDao.kt
│   │   └── SubstitutionDao.kt
│   │
│   ├── entities/
│   │   ├── GroceryItem.kt
│   │   ├── GroceryList.kt
│   │   ├── IngredientSubstitution.kt
│   │   ├── MealPlan.kt
│   │   └── Recipe.kt
│   │
│   ├── managers/
│   │   ├── GroceryListManager.kt
│   │   ├── ImportManager.kt
│   │   ├── MealPlanManager.kt
│   │   ├── RecipeManager.kt
│   │   ├── SettingsManager.kt
│   │   └── SubstitutionManager.kt
│   │
│   ├── parsers/
│   │   ├── PdfRecipeParser.kt
│   │   ├── PhotoRecipeParser.kt
│   │   ├── RecipeParser.kt
│   │   ├── SchemaOrgRecipeParser.kt
│   │   └── TextRecipeParser.kt
│   │
│   ├── AppDatabase.kt
│   ├── AppSettings.kt
│   ├── Converters.kt
│   ├── RecipeTags.kt
│   └── Substitute.kt
│
├── navigation/
│   └── NavGraph.kt
│
├── ui/
│   ├── components/
│   │   ├── AppNavigationDrawer.kt
│   │   ├── DatePickerDialog.kt
│   │   ├── GroceryListPickerDialog.kt
│   │   ├── ImportDialog.kt
│   │   ├── MealPlanPickerDialog.kt
│   │   ├── SubstitutionDialog.kt
│   │   └── TagModificationDialog.kt
│   │
│   ├── screens/
│   │   ├── AddEditMealPlanScreen.kt
│   │   ├── AddEditRecipeScreen.kt
│   │   ├── AddEditSubstitutionScreen.kt
│   │   ├── GroceryListDetailScreen.kt
│   │   ├── GroceryListScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── ImportPdfScreen.kt
│   │   ├── ImportPhotoScreen.kt
│   │   ├── ImportSourceSelectionScreen.kt
│   │   ├── ImportUrlScreen.kt
│   │   ├── MealPlanningScreen.kt
│   │   ├── RecipeDetailScreen.kt
│   │   ├── RecipeListScreen.kt
│   │   ├── SearchScreen.kt
│   │   ├── SettingsScreen.kt
│   │   └── SubstitutionGuideScreen.kt
│   │
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── HearthTheme.kt
│   │   └── Type.kt
│   │
│   ├── viewmodels/
│   │   ├── GroceryListViewModel.kt
│   │   ├── ImportPdfViewModel.kt
│   │   ├── ImportPhotoViewModel.kt
│   │   ├── ImportViewModel.kt
│   │   ├── MealPlanViewModel.kt
│   │   ├── RecipeViewModel.kt
│   │   ├── SettingsViewModel.kt
│   │   ├── SubstitutionViewModel.kt
│   │   └── ViewModelFactory.kt
│   │
│   ├── MainActivity.kt
│   └── Navigation.kt
│
└── utils/
    ├── filtersort/
    │   ├── core/
    │   │   ├── Filter.kt
    │   │   ├── FilterSortGroupManager.kt
    │   │   ├── GroupBy.kt
    │   │   └── Sort.kt
    │   ├── grocerylist/
    │   │   ├── GroceryListFilters.kt
    │   │   ├── GroceryListGroupings.kt
    │   │   └── GroceryListSorts.kt
    │   ├── mealplan/
    │   │   ├── MealPlanFilters.kt
    │   │   ├── MealPlanGroupings.kt
    │   │   └── MealPlanSorts.kt
    │   ├── recipe/
    │   │   ├── RecipeFilters.kt
    │   │   ├── RecipeGroupings.kt
    │   │   └── RecipeSorts.kt
    │   └── ui/
    │       ├── FilterChipRow.kt
    │       └── SortMenu.kt
    ├── DebugConfig.kt
    ├── ErrorHandler.kt
    ├── IngredientScaler.kt
    ├── IngredientUnitConverter.kt
    ├── ShareHelper.kt
    ├── ShareModels.kt
    ├── SubstitutionData.kt
    ├── TagStandardizer.kt
    ├── TextFormatUtils.kt
    └── UnitConverter.kt

```

---

## System Relationships

> **Purpose**: Shows how files work together to implement features - cross-references between related components
>
> **When to add a relationship**:
> - When files have direct dependencies (A imports/uses B)
> - When files work together to implement a feature (flow chains)
> - When understanding one file requires knowing about another
> - Format: `FileA.kt → FileB.kt, FileC.kt` (A uses/depends on B and C)

### Navigation Flow
- MainActivity → AppDatabase → RecipeManager → ViewModelFactory
- MainActivity → Navigation.kt → RecipeViewModel → RecipeManager → RecipeDao
- AppNavigationDrawer → Navigation.kt (content parameter)
- Navigation.kt → All screens with ViewModel integration
- SearchScreen → RecipeViewModel → RecipeManager (global recipe search accessible from drawer)

### Recipe Management Flow
- RecipeListScreen → RecipeViewModel → RecipeManager → RecipeDao → AppDatabase
- AddEditRecipeScreen → RecipeViewModel.createRecipe/updateRecipe → RecipeManager
- RecipeDetailScreen → RecipeViewModel → RecipeManager (delete, favorite toggle)
- Navigation.kt orchestrates screen transitions with navController

### Import Flow
- MainActivity → HttpClient (Ktor), PdfRecipeParser, PhotoRecipeParser → ViewModelFactory
- ImportSourceSelectionScreen → Navigation.kt (route to ImportUrl/ImportPdf/ImportPhoto)
- ImportUrlScreen → ImportViewModel → SchemaOrgRecipeParser → RecipeManager
- ImportPdfScreen → ImportPdfViewModel → PdfRecipeParser → TextRecipeParser → RecipeManager
- ImportPhotoScreen → ImportPhotoViewModel → PhotoRecipeParser → TextRecipeParser → RecipeManager
- SchemaOrgRecipeParser → Jsoup (HTML parsing), kotlinx-serialization (JSON parsing)
- PdfRecipeParser → PdfBox-Android (PDF text extraction)

### Share/Import Flow
- MainActivity → ImportManager → RecipeManager, MealPlanManager, GroceryListManager
- MainActivity.handleIncomingIntent() → ACTION_SEND/ACTION_VIEW intent filters → ImportManager.importFromJson()
- ShareHelper.shareRecipe/shareMealPlan/shareGroceryList() → Android share sheet (email, Quick Share, Messenger, SMS)
- ShareHelper.encodePhoto() → Base64 encoding (max 1920px, 10MB limit)
- ImportManager.importFromJson() → ShareHelper.importFromJson() → duplicate detection → ImportDialog
- ImportDialog → RecipeDuplicateDialog, MealPlanImportDialog, GroceryListImportDialog
- SettingsScreen → File picker (OpenDocument) → MainActivity.pendingImportJson → ImportManager
- RecipeCard/MealPlanCard/GroceryListCard → Share button → ShareHelper
- GroceryListDetailScreen → Share icon button → ShareHelper
- RecipeDetailScreen → Share action button → ShareHelper

### Meal Planning Flow
- MealPlanningScreen → MealPlanViewModel, RecipeViewModel, GroceryListViewModel
- MealPlanningScreen → GroceryListPickerDialog (for "Generate List" action)
- RecipeListScreen → Calendar icon → MealPlanPickerDialog → MealPlanViewModel.addRecipeToPlan()
- RecipeDetailScreen → "Add to Meal Plan" menu → MealPlanPickerDialog → MealPlanViewModel.addRecipeToPlan()
- MealPlanPickerDialog → Used by RecipeListScreen, RecipeDetailScreen (reusable component)
- AddEditMealPlanScreen → MealPlanViewModel → MealPlanManager → MealPlanDao, RecipeDao
- MealPlanManager → RecipeTags (for auto-tag aggregation and special event detection)
- Navigation.kt → AddMealPlan, EditMealPlan screens with ViewModel integration

### Grocery List Flow
- GroceryListScreen → GroceryListViewModel → GroceryListManager → GroceryListDao, GroceryItemDao
- GroceryListDetailScreen → GroceryListViewModel, RecipeViewModel, MealPlanViewModel (for available recipes/plans)
- GroceryListManager → RecipeDao, MealPlanDao (for fetching ingredient data)
- GroceryListManager → Intelligent consolidation logic (quantity parsing, modifier removal, unit matching)
- GroceryListPickerDialog → Used by MealPlanningScreen, RecipeListScreen (reusable component)
- RecipeListScreen → "Add to Grocery List" button → GroceryListPickerDialog
- MealPlanningScreen → "Generate List" button → GroceryListPickerDialog
- Navigation.kt → GroceryListDetail route with ViewModel integration
- PhotoRecipeParser → ML Kit Text Recognition (OCR), supports multiple photos
- TextRecipeParser → Smart pattern matching (detects sections, filters website noise, validates content, parses times/servings), uses TagStandardizer for tag cleanup
- TagStandardizer → Normalizes imported tags (removes noise words, maps variations, deduplicates)
- ImportViewModel UI states: Input → Loading → Editing → Saved
- ImportPdfViewModel UI states: SelectFile → Loading → Editing → Saved
- ImportPhotoViewModel UI states: SelectPhoto → Loading → Editing → Saved

---

## Component Details by Layer

> **Organization**: Components grouped by package/layer (data/, navigation/, ui/, utils/)
>
> Using simple one-liner format for all files

### Data - Entities
- **Recipe.kt** - Recipe entity with Room annotations: title, ingredients, instructions, servings, times, tags, source, photos, notes, behavioral flags (isFavorite, isTemplate)
- **IngredientSubstitution.kt** - Substitution entity with Room annotations: ingredient name (normalized lowercase), category, List<Substitute> (JSON-encoded), isUserAdded, lastModified timestamp

### Data - Support Classes
- **Substitute.kt** - Serializable data class for substitution options: name, conversionRatio (default 1.0), conversionNote, notes, suitability (1-10 rating), dietaryTags (vegan/gluten-free/etc.)

### Data - DAOs
- **RecipeDao.kt** - Recipe CRUD operations: getAllRecipes, getRecipeById, getFavoriteRecipes, searchRecipes, insert/update/delete, updateFavoriteStatus (all return Flow)
- **SubstitutionDao.kt** - Substitution CRUD operations: getAllSubstitutions, searchSubstitutions, getSubstitutionByIngredient, getSubstitutionsByCategory, getAllCategories, insert/update/delete, getSubstitutionCount (all return Flow)

### Data - Managers
- **RecipeManager.kt** - Recipe business logic: validation, CRUD operations with cascading deletion (removes recipe from meal plans before deleting to maintain referential integrity), favorite toggle, recipe scaling stub, recipe log operations (delegates to RecipeDao and MealPlanDao)
- **GroceryListManager.kt** - Grocery list business logic: list/item CRUD operations, intelligent ingredient consolidation (removes modifiers, sums quantities), canned item parsing, recipe-to-list conversion with ingredient count tracking, meal plan-to-list conversion (delegates to GroceryListDao, GroceryItemDao, RecipeDao, MealPlanDao)
- **ImportManager.kt** - Import business logic: importFromJson() with duplicate detection (title+sourceUrl matching), importRecipeWithAction() for Replace/Keep Both/Skip actions, importMealPlanFromJson() with per-recipe duplicate handling, importGroceryListFromJson() (always creates new), photo decoding and storage, delegates to RecipeManager/MealPlanManager/GroceryListManager
- **SubstitutionManager.kt** - Substitution business logic: CRUD operations, database initialization with defaults, quantity conversion calculations (calculateConvertedAmount), amount formatting (formatConvertedAmount prefers fractions), ingredient validation

### Data - Parsers
- **RecipeParser.kt** - Recipe parser interface: parse(source: String): Result<Recipe> for URL/PDF/Photo parsers
- **SchemaOrgRecipeParser.kt** - Schema.org JSON-LD parser: Jsoup HTML parsing, Schema.org Recipe extraction from recipeCategory/recipeCuisine (keywords field excluded), parses HTML category links (rel="category"/"tag"), HowToStep/HowToSection instructions, ISO 8601 duration conversion, Open Graph fallback, debug logging
- **TextRecipeParser.kt** - Smart pattern matching parser: detects ingredients/instructions sections via regex, filters website noise (CTAs/footers), validates ingredient/instruction content, parses time strings ("1h 30min"), extracts servings, cleans bullets/numbering from unstructured text
- **PdfRecipeParser.kt** - PDF text extraction parser: Uses PdfBox-Android PDFTextStripper to extract all text from PDF files, delegates to TextRecipeParser for recipe parsing
- **PhotoRecipeParser.kt** - OCR-based parser: Uses ML Kit Text Recognition to extract text from photos/camera, supports multiple photos via parseMultiple(List<Uri>), combines OCR results, delegates to TextRecipeParser

### Data - Database
- **AppDatabase.kt** - Room database singleton: Recipe, MealPlan, GroceryList, GroceryItem, IngredientSubstitution tables, version 4, fallbackToDestructiveMigration
- **Converters.kt** - Room type converters: List<String> ↔ delimited string, RecipeSource ↔ string, List<Substitute> ↔ JSON string (kotlinx.serialization), RecipeSourceType ↔ string

### Navigation
- **NavGraph.kt** - Navigation routes sealed class: Home, RecipeIndex, Search, MealPlanning, GroceryLists, SubstitutionGuide, Settings (drawer screens), AddRecipe, EditRecipe, RecipeDetail, AddEditSubstitution, ImportSourceSelection, ImportUrl, ImportPdf, ImportPhoto (detail/import screens)

### UI - MainActivity
- **MainActivity.kt** - Orchestrator only: Setup dependencies (AppDatabase, RecipeManager, SubstitutionManager, HttpClient, parsers, ViewModelFactory), wire theme and navigation, NO business/navigation logic
- **Navigation.kt** - All navigation logic: NavHost with routes for Home, RecipeIndex, Search, MealPlanning, GroceryLists, SubstitutionGuide, Settings (drawer), AddRecipe, EditRecipe, RecipeDetail, AddEditSubstitution, ImportSourceSelection, ImportUrl, ImportPdf, ImportPhoto, uses LaunchedEffect for ViewModel data loading on detail/edit screens (prevents recomposition race conditions), DisposableEffect cleanup for shared ViewModel state, LaunchedEffect initializes substitution database with defaults

### UI - Screens
- **HomeScreen.kt** - Landing page: This week's meal plans, recipe suggestions
- **RecipeListScreen.kt** - Recipe browsing: LazyColumn with RecipeCards (photo, title, servings/times, tags), toggle-able search bar (matching MealPlanningScreen), single FAB goes to tabbed add recipe screen, favorite toggle, delete from card menu, FilterChipRow and SortMenu integrated, empty state, Coil AsyncImage for photos (180dp)
- **SearchScreen.kt** - Global recipe search: Dedicated search screen accessible from navigation drawer, always-visible search bar at top, displays results as clickable cards with recipe details (title, description, cook time, servings), empty state and loading indicators, navigates to RecipeDetailScreen on card click
- **AddEditRecipeScreen.kt** - Recipe add/edit form: Single screen with title, servings, times, ingredients, instructions, tags, notes, validation, auto-save on back
- **RecipeDetailScreen.kt** - Recipe detail view: Photo (240dp), servings dropdown with auto-scaling, cook mode (checkable ingredients/instructions, timer, keep awake), long-press ingredient for substitution lookup, tabbed instruction sections, tags, notes, favorite/edit/delete actions, Coil AsyncImage
- **ImportSourceSelectionScreen.kt** - Import/create source selection: Tabbed interface with Import tab (URL/PDF/Photo/File cards) and Create tab (manual entry button), unified entry point for all recipe creation methods, File card launches OpenDocument file picker storing JSON in MainActivity.pendingImportJson with success feedback
- **ImportUrlScreen.kt** - URL import flow: URL input → loading → RecipePreviewContent (WYSIWYG preview) → save, auto-save on back navigation, inline tag editing with auto-suggestions, per-field edit dialogs (title/metadata/ingredients/instructions), shows first 5 ingredients and first 3 instruction steps in preview, TagModificationDialog for reviewing standardization changes, Coil AsyncImage
- **ImportPdfScreen.kt** - PDF import flow: File picker (ActivityResultContracts.GetContent) → loading → recipe preview/edit → save, auto-save on back navigation
- **ImportPhotoScreen.kt** - Photo import flow: Camera/gallery pickers (GetMultipleContents for multiple photos) → photo preview grid → loading → recipe preview/edit → save, auto-save on back navigation
- **MealPlanningScreen.kt** - Meal planning list: Card-based list with search, duplicate/delete dialogs, shows all recipes and tags, enhanced recipe cards with servings/time/tags, action buttons (Edit, Generate List) inline with date range, full-screen recipe picker grid, auto-naming from dates, snackbar feedback for grocery list operations, FileUpload icon button in TopAppBar for file import with snackbar feedback
- **AddEditMealPlanScreen.kt** - Meal plan add/edit form: Name field with inline calendar icon button, optional date range selection with Material3 DateRangePicker (supports single date or range with clear button), tabbed recipe picker (defaults to Existing tab with 2-column grid and search, Import tab with URL/PDF/Photo options), notes field, auto-save on back navigation, auto-naming from selected dates
- **GroceryListScreen.kt** - Grocery lists: Card-based list with progress indicators (checked/total), create/delete dialogs, search, FileUpload icon button in TopAppBar for file import with snackbar feedback
- **GroceryListDetailScreen.kt** - Grocery list detail: Quick-entry text field at top, item checkboxes, item detail dialog showing source recipes, icon-over-text bottom actions (toggle Select All/Deselect, Clear, Recipes, Meal Plans)
- **SettingsScreen.kt** - Settings UI: Granular unit preferences (liquid volume, weight) with IMPERIAL/METRIC/BOTH radio buttons, temperature unit, display preferences, recipe defaults
- **SubstitutionGuideScreen.kt** - Substitution guide browsing: Search field, quantity/unit input for conversions, category filter chips, dietary filter chips (vegan/gluten-free/etc.), expandable cards with substitutes ordered by suitability, long-press card to edit, FAB to add new
- **AddEditSubstitutionScreen.kt** - Substitution add/edit form: Ingredient name, category dropdown, substitutes list (each with name, conversion ratio, conversion note, notes, suitability slider 1-10, dietary tags), add/remove substitutes, auto-save on back, validation

### UI - Components
- **AppNavigationDrawer.kt** - Responsive navigation drawer: Modal for phones, permanent for tablets with collapse button, accepts content parameter, drawer header with logo/name, UI only
- **DatePickerDialog.kt** - Reusable Material3 date picker dialog: AppDatePickerDialog component for selecting dates across the app, replaces placeholder "Set Today" dialogs, used by AddEditMealPlanScreen
- **GroceryListPickerDialog.kt** - Reusable grocery list picker dialog: Select existing list or create new, used by RecipeListScreen/RecipeDetailScreen/MealPlanningScreen for "Add to Grocery List" actions
- **ImportDialog.kt** - Import dialogs for share/import system: RecipeDuplicateDialog (Replace/Keep Both/Skip actions with side-by-side preview), MealPlanImportDialog (per-recipe duplicate selection with mutable state), GroceryListImportDialog (simple confirmation with item count), used by MainActivity intent handling and Settings import
- **MealPlanPickerDialog.kt** - Reusable meal plan picker dialog: Select existing plan or create new, used by RecipeListScreen/RecipeDetailScreen for "Add to Meal Plan" actions from calendar icon
- **SubstitutionDialog.kt** - Ingredient substitution lookup dialog: Shows substitutes for ingredient from recipe (triggered by long-press), displays converted amounts based on quantity/unit from parsed ingredient string, substitutes ordered by suitability, shows dietary tags
- **TagModificationDialog.kt** - Tag standardization review dialog: Shows original→standardized tag transformations during recipe import, allows users to edit/remove each tag before accepting with minus icon button, visual feedback for deleted tags (red background, strikethrough), restore button to undo deletions, prevents silent modifications (e.g., "vegan bowls"→"vegan"), used by ImportUrlScreen/ImportPdfScreen/ImportPhotoScreen

### UI - ViewModels
- **RecipeViewModel.kt** - Recipe UI state: StateFlow for recipes/currentRecipe/isLoading/error, delegates all business logic to RecipeManager, event functions (loadRecipes, createRecipe, updateRecipe, deleteRecipe, toggleFavorite, searchRecipes)
- **ImportViewModel.kt** - URL import UI state: StateFlow<UiState> (Input → Loading → Editing(recipe, errorMessage, tagModifications) → Saved), fetchRecipeFromUrl() with tag standardization tracking, applyTagModifications(), getAllExistingTags() for auto-suggestion, updateRecipe(), saveRecipe(), reset()
- **ImportPdfViewModel.kt** - PDF import UI state: StateFlow<UiState> (SelectFile → Loading → Editing → Saved), fetchRecipeFromPdf(Uri), updateRecipe(), saveRecipe(), reset()
- **ImportPhotoViewModel.kt** - Photo import UI state: StateFlow<UiState> (SelectPhoto → Loading → Editing → Saved), fetchRecipeFromPhoto(Uri), fetchRecipeFromPhotos(List<Uri>), updateRecipe(), saveRecipe(), reset()
- **SubstitutionViewModel.kt** - Substitution UI state: StateFlow for searchQuery/selectedCategory/selectedDietaryTag/substitutions/categories, reactive filtering using Flow operators (combine, flatMapLatest, map), getSubstitutionByIngredient(), observeSubstitutionById(), createOrUpdateSubstitution(), deleteSubstitution(), calculateConvertedAmount(), formatAmount(), initializeDefaultSubstitutions(), delegates CRUD to SubstitutionManager
- **ViewModelFactory.kt** - ViewModel dependency injection: Creates RecipeViewModel, MealPlanViewModel, GroceryListViewModel, ImportViewModel, ImportPdfViewModel, ImportPhotoViewModel, SettingsViewModel, SubstitutionViewModel with manager and parser dependencies

### UI - Theme
- **Color.kt** - Hearth color palette: Terracotta, Clay, SageGreen, Cream neutrals, cooking mode high-contrast colors
- **Type.kt** - Hearth typography: Material 3 type scale with readable fonts for recipe content
- **HearthTheme.kt** - Theme composable: Light/dark color schemes, dynamic color support, typography integration

### Settings
- **AppSettings.kt** - User preferences data class: liquidVolumePreference/weightPreference enums (IMPERIAL/METRIC/BOTH), TemperatureUnit enum (FAHRENHEIT/CELSIUS), showPhotosInList, defaultServings
- **SettingsManager.kt** - Settings persistence with StateFlow reactivity: SharedPreferences storage, exposes StateFlow<AppSettings>, setter methods for each preference with DebugConfig logging
- **SettingsViewModel.kt** - Settings UI state: Delegates all operations to SettingsManager, exposes settings StateFlow
- **SettingsScreen.kt** - Settings UI: Granular unit preference sections (liquid volume, weight) with IMPERIAL/METRIC/BOTH radio buttons, temperature radio buttons, display preferences switches, recipe defaults filter chips, reset button

### Substitutions
- **SubstitutionData.kt** - Pre-populated substitution defaults: getDefaultSubstitutions() returns List<IngredientSubstitution> with ~100 substitutes across 22 common ingredients (butter, milk, eggs, flour, sugar, oils, vinegars, herbs, spices, condiments), includes suitability ratings, conversion ratios, dietary tags

### Utils - FilterSortGroup Library
- **Filter.kt** - Generic filter interface: Filter<T> with id/label/matches() method, AndFilter and OrFilter for composite filters with multiple conditions
- **Sort.kt** - Generic sort interface: Sort<T> with id/label/direction/comparator, SortDirection enum (ASC/DESC with reversed()), BaseSort abstract implementation with getBaseComparator() and automatic direction handling
- **GroupBy.kt** - Generic grouping interface: GroupBy<T,K> with id/label/extractKey() method, optional formatKeyLabel() for display text, optional compareKeys() for custom group ordering
- **FilterSortGroupManager.kt** - Reactive state manager for filtering/sorting/grouping: Combines source Flow with search/filters/sort/groupBy using Flow.combine(), exposes filteredItems and groupedItems StateFlows, provides setSearchQuery/addFilter/removeFilter/setSort/setGroupBy/toggleSortDirection methods, handles AND logic for multiple filters, applies grouping with sorted keys, 100% generic with zero app dependencies
- **RecipeFilters.kt** - Recipe filter implementations: FavoriteFilter (favorites only), TagFilter (by tag name), SourceFilter (URL/PDF/Photo/Manual), CookTimeFilter (≤30/≤60/>60 min ranges), ServingsFilter (min/max range), HasPhotoFilter, HasNotesFilter
- **RecipeSorts.kt** - Recipe sort implementations: TitleSort (alphabetical), DateCreatedSort (newest first default), CookTimeSort (shortest first default, recipes without time sorted last), ServingsSort (fewest first default), FavoriteSort (favorites first default)
- **RecipeGroupings.kt** - Recipe grouping implementations: SourceGrouping (by RecipeSource enum), FavoriteGrouping (Favorites/Other Recipes with favorites first), TagGrouping (by first tag or "Untagged"), CookTimeGrouping (Quick ≤30min / Medium 31-60min / Long >60min / Unknown), ServingsGrouping (Single 1-2 / Small 3-4 / Medium 5-6 / Large 7+)
- **MealPlanFilters.kt** - Meal plan filter implementations: DateRangeFilter (plans overlapping date range), RecipeCountFilter (min/max recipes), TagFilter (by tag name), ContainsRecipeFilter (finds plans with specific recipe by name or ID), HasNotesFilter, HasDatesFilter (date-based vs standalone event plans)
- **MealPlanSorts.kt** - Meal plan sort implementations: NameSort (alphabetical), StartDateSort (most recent first default, plans without dates last), DateCreatedSort (newest first default), RecipeCountSort (most recipes first default)
- **MealPlanGroupings.kt** - Meal plan grouping implementations: MonthGrouping (by start date month, most recent first), TagGrouping (by first tag or "Untagged"), RecipeCountGrouping (Empty / Single / Small 2-3 / Medium 4-6 / Large 7+, largest first), PlanTypeGrouping (Date-based / Event plans, date-based first)
- **GroceryListFilters.kt** - Grocery list filter implementations: CreatedRecentlyFilter (last N days with presets: Today/This Week/This Month), ModifiedRecentlyFilter (last N days), note: item-based filters (unchecked count, completion %) require composite data type
- **GroceryListSorts.kt** - Grocery list sort implementations: NameSort (alphabetical), DateCreatedSort (newest first default), DateModifiedSort (most recently modified first default)
- **GroceryListGroupings.kt** - Grocery list grouping implementations: MonthGrouping (by creation month, most recent first), AgeGrouping (Today / This Week / This Month / Older)
- **FilterChipRow.kt** - Horizontal scrollable row of filter chips: Takes availableFilters list and activeFilterIds set, renders FilterChip for each with selected state, optional "Clear All" AssistChip with leading close icon, onFilterToggle callback, horizontalScroll modifier
- **SortMenu.kt** - Sort dropdown menu: IconButton with Sort icon, DropdownMenu with sort options, shows checkmark for active sort, shows direction arrow (up/down) for active sort, "Clear Sort" option when sort active, clicking active sort toggles direction, clicking inactive sort selects it

### Utils - Core
- **DebugConfig.kt** - Centralized logging: Category-based filtering (NAVIGATION, DATABASE, IMPORT, UI, MANAGER, SETTINGS, GENERAL), replaces android.util.Log
- **ErrorHandler.kt** - Error handling utility: User-friendly error messages (network, validation, state errors), handleResult() for Result<T> processing, executeSafely() and executeWithRetry() for suspending operations
- **IngredientScaler.kt** - Ingredient quantity parsing and scaling: scaleIngredient() parses fractions (1/2), mixed numbers (1 1/2), decimals, ranges (2-3), scales by factor, formats output preferring common fractions (1/4, 1/2, 3/4), preserves units
- **IngredientUnitConverter.kt** - Ingredient unit formatting based on user preferences: formatIngredient() detects liquid vs weight units, applies liquidVolumePreference and weightPreference conversions, supports BOTH mode showing "1 cup (237 ml)" inline, uses UnitConverter for conversions
- **ShareHelper.kt** - Share utility for app-to-app and text sharing: shareRecipe()/shareMealPlan()/shareGroceryList() launch Android share sheet with JSON (app-to-app) and text fallback, encodePhoto() with base64 encoding (max 1920px, 10MB limit), decodePhoto() for import, importFromJson() with duplicate detection, generateIncrementedTitle() for "Keep Both" action
- **ShareModels.kt** - Share data models: SharePackage (container for all share types with version/type/content/photos map), ShareRecipe/ShareMealPlan/ShareGroceryList (serializable versions of entities), ShareType enum (RECIPE/MEAL_PLAN/GROCERY_LIST), ImportResult sealed class (Success/DuplicateDetected/Error), DuplicateAction enum (REPLACE/KEEP_BOTH/SKIP)
- **TagStandardizer.kt** - Tag normalization utility: Converts tag variations to standard forms (e.g., "italian food" → "italian"), removes noise words ("recipe", "food"), filters junk tags/phrases ("how to make...", "Weight Watchers"), consolidates holidays to "special occasion", consolidates ingredient types (chicken thigh → chicken), filters >4 word tags, maps common patterns, deduplicates, standardize() returns cleaned tags, standardizeWithTracking() returns TagModification objects showing original→standardized transformations with wasModified flag, used by TextRecipeParser and ImportViewModel during import
- **TextFormatUtils.kt** - Text formatting utilities: highlightNumbersInText() uses AnnotatedString to bold numbers (temps/times) in instruction text with regex pattern matching, supports ranges (350-375°F) and decimals (1.5 hours)
- **UnitConverter.kt** - Cooking unit conversions: Imperial↔Metric (volume: cups/tbsp/tsp↔ml/L, weight: oz/lb↔g/kg, temperature: F↔C), smart helpers (volumeToMetric chooses ml/L), formatNumber

---
> **Format Guidelines**:
> - **Simple one-liner format** (for MOST files): `**FileName.kt** - Purpose: key capabilities`
>   - Example: `**BPMAnalyzer.kt** - BPM detection via Essentia RhythmExtractor2013: 80-90% accuracy, 15-song validation library, ±10 BPM target`
> - **Detailed format** (ONLY for critical infrastructure):
>   ```
>   - **`FileName.kt`**
>     - Purpose: [One sentence describing what it does]
>     - Key capabilities: [Brief list of key features]
>     - Used by: [Files that depend on this]
>     - Depends on: [Files this depends on]
>     - Note: [ONLY if there's a critical warning like "CRITICAL - Always use ID3v23Tag"]
>   ```
>
> **When to use detailed format**:
> - ✅ Core infrastructure
> - ✅ Components with critical warnings
> - ✅ Complex managers with unique architectural patterns
> - ✅ Services with complex lifecycle
>
> **When to use simple one-liner format**:
> - ✅ ALL UI screens, cards, components, dialogs
> - ✅ Most DAOs and entities (unless critical architectural note needed)
> - ✅ Most analyzers and utilities
> - ✅ Data models
>
> **Important**: Default to simple one-liner format unless the file truly qualifies for detailed format based on criteria above

---
