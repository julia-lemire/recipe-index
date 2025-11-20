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
│   │   ├── MealPlanPickerDialog.kt
│   │   ├── SubstitutionDialog.kt
│   │   └── TagModificationDialog.kt
│   │
│   ├── screens/
│   │   ├── AddEditMealPlanScreen.kt
│   │   ├── AddEditRecipeScreen.kt
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
    ├── DebugConfig.kt
    ├── ErrorHandler.kt
    ├── IngredientScaler.kt
    ├── IngredientUnitConverter.kt
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
- **RecipeManager.kt** - Recipe business logic: validation, CRUD operations, favorite toggle, recipe scaling stub (delegates to RecipeDao)
- **SubstitutionManager.kt** - Substitution business logic: CRUD operations, database initialization with defaults, quantity conversion calculations (calculateConvertedAmount), amount formatting (formatConvertedAmount prefers fractions), ingredient validation

### Data - Parsers
- **RecipeParser.kt** - Recipe parser interface: parse(source: String): Result<Recipe> for URL/PDF/Photo parsers
- **SchemaOrgRecipeParser.kt** - Schema.org JSON-LD parser: Jsoup HTML parsing, Schema.org Recipe extraction (HowToStep/HowToSection instructions), ISO 8601 duration conversion, Open Graph fallback, debug logging
- **TextRecipeParser.kt** - Smart pattern matching parser: detects ingredients/instructions sections via regex, filters website noise (CTAs/footers), validates ingredient/instruction content, parses time strings ("1h 30min"), extracts servings, cleans bullets/numbering from unstructured text
- **PdfRecipeParser.kt** - PDF text extraction parser: Uses PdfBox-Android PDFTextStripper to extract all text from PDF files, delegates to TextRecipeParser for recipe parsing
- **PhotoRecipeParser.kt** - OCR-based parser: Uses ML Kit Text Recognition to extract text from photos/camera, supports multiple photos via parseMultiple(List<Uri>), combines OCR results, delegates to TextRecipeParser

### Data - Database
- **AppDatabase.kt** - Room database singleton: Recipe, MealPlan, GroceryList, GroceryItem, IngredientSubstitution tables, version 4, fallbackToDestructiveMigration
- **Converters.kt** - Room type converters: List<String> ↔ delimited string, RecipeSource ↔ string, List<Substitute> ↔ JSON string (kotlinx.serialization), RecipeSourceType ↔ string

### Navigation
- **NavGraph.kt** - Navigation routes sealed class: Home, RecipeIndex, MealPlanning, GroceryLists, SubstitutionGuide, Settings (drawer screens), AddRecipe, EditRecipe, RecipeDetail, AddEditSubstitution, ImportSourceSelection, ImportUrl, ImportPdf, ImportPhoto (detail/import screens)

### UI - MainActivity
- **MainActivity.kt** - Orchestrator only: Setup dependencies (AppDatabase, RecipeManager, SubstitutionManager, HttpClient, parsers, ViewModelFactory), wire theme and navigation, NO business/navigation logic
- **Navigation.kt** - All navigation logic: NavHost with routes for Home, RecipeIndex, MealPlanning, GroceryLists, SubstitutionGuide, Settings (drawer), AddRecipe, EditRecipe, RecipeDetail, AddEditSubstitution, ImportSourceSelection, ImportUrl, ImportPdf, ImportPhoto, LaunchedEffect initializes substitution database with defaults

### UI - Screens
- **HomeScreen.kt** - Landing page: This week's meal plans, recipe suggestions
- **RecipeListScreen.kt** - Recipe browsing: LazyColumn with RecipeCards (photo, title, servings/times, tags), expandable FAB menu (create/import), favorite toggle, empty state, Coil AsyncImage for photos (180dp)
- **AddEditRecipeScreen.kt** - Recipe add/edit form: Single screen with title, servings, times, ingredients, instructions, tags, notes, validation, auto-save on back
- **RecipeDetailScreen.kt** - Recipe detail view: Photo (240dp), servings dropdown with auto-scaling, cook mode (checkable ingredients/instructions, timer, keep awake), long-press ingredient for substitution lookup, tabbed instruction sections, tags, notes, favorite/edit/delete actions, Coil AsyncImage
- **ImportSourceSelectionScreen.kt** - Import source selection: Choose URL/PDF/Photo import source with cards (all three enabled)
- **ImportUrlScreen.kt** - URL import flow: URL input → loading → recipe preview/edit with photo → save, auto-save on back navigation, tag auto-suggestion from existing tags (appears after 2 chars), TagModificationDialog for reviewing standardization changes, Coil AsyncImage
- **ImportPdfScreen.kt** - PDF import flow: File picker (ActivityResultContracts.GetContent) → loading → recipe preview/edit → save, auto-save on back navigation
- **ImportPhotoScreen.kt** - Photo import flow: Camera/gallery pickers (GetMultipleContents for multiple photos) → photo preview grid → loading → recipe preview/edit → save, auto-save on back navigation
- **MealPlanningScreen.kt** - Meal planning list: Card-based list with search, duplicate/delete dialogs, shows all recipes and tags, enhanced recipe cards with servings/time/tags, full-screen recipe picker grid, auto-naming from dates
- **AddEditMealPlanScreen.kt** - Meal plan add/edit form: Name field, optional start/end date selection with Material3 AppDatePickerDialog, full-screen recipe picker grid (2 columns, search), notes field, auto-save on back navigation, auto-naming from selected dates
- **GroceryListScreen.kt** - Grocery lists: Card-based list with progress indicators (checked/total), create/delete dialogs, search
- **GroceryListDetailScreen.kt** - Grocery list detail: Quick-entry text field at top, item checkboxes, item detail dialog showing source recipes, Select All/Deselect All buttons, bottom actions for clear checked/add recipes/add meal plans
- **SettingsScreen.kt** - Settings UI: Granular unit preferences (liquid volume, weight) with IMPERIAL/METRIC/BOTH radio buttons, temperature unit, display preferences, recipe defaults
- **SubstitutionGuideScreen.kt** - Substitution guide browsing: Search field, quantity/unit input for conversions, category filter chips, dietary filter chips (vegan/gluten-free/etc.), expandable cards with substitutes ordered by suitability, long-press card to edit, FAB to add new
- **AddEditSubstitutionScreen.kt** - Substitution add/edit form: Ingredient name, category dropdown, substitutes list (each with name, conversion ratio, conversion note, notes, suitability slider 1-10, dietary tags), add/remove substitutes, auto-save on back, validation

### UI - Components
- **AppNavigationDrawer.kt** - Responsive navigation drawer: Modal for phones, permanent for tablets with collapse button, accepts content parameter, drawer header with logo/name, UI only
- **DatePickerDialog.kt** - Reusable Material3 date picker dialog: AppDatePickerDialog component for selecting dates across the app, replaces placeholder "Set Today" dialogs, used by AddEditMealPlanScreen
- **GroceryListPickerDialog.kt** - Reusable grocery list picker dialog: Select existing list or create new, used by RecipeListScreen/RecipeDetailScreen/MealPlanningScreen for "Add to Grocery List" actions
- **MealPlanPickerDialog.kt** - Reusable meal plan picker dialog: Select existing plan or create new, used by RecipeListScreen/RecipeDetailScreen for "Add to Meal Plan" actions from calendar icon
- **SubstitutionDialog.kt** - Ingredient substitution lookup dialog: Shows substitutes for ingredient from recipe (triggered by long-press), displays converted amounts based on quantity/unit from parsed ingredient string, substitutes ordered by suitability, shows dietary tags
- **TagModificationDialog.kt** - Tag standardization review dialog: Shows original→standardized tag transformations during recipe import, allows users to edit each tag before accepting, prevents silent modifications (e.g., "vegan bowls"→"vegan"), used by ImportUrlScreen/ImportPdfScreen/ImportPhotoScreen

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

### Utils
- **DebugConfig.kt** - Centralized logging: Category-based filtering (NAVIGATION, DATABASE, IMPORT, UI, MANAGER, SETTINGS, GENERAL), replaces android.util.Log
- **ErrorHandler.kt** - Error handling utility: User-friendly error messages (network, validation, state errors), handleResult() for Result<T> processing, executeSafely() and executeWithRetry() for suspending operations
- **IngredientScaler.kt** - Ingredient quantity parsing and scaling: scaleIngredient() parses fractions (1/2), mixed numbers (1 1/2), decimals, ranges (2-3), scales by factor, formats output preferring common fractions (1/4, 1/2, 3/4), preserves units
- **IngredientUnitConverter.kt** - Ingredient unit formatting based on user preferences: formatIngredient() detects liquid vs weight units, applies liquidVolumePreference and weightPreference conversions, supports BOTH mode showing "1 cup (237 ml)" inline, uses UnitConverter for conversions
- **TagStandardizer.kt** - Tag normalization utility: Converts tag variations to standard forms (e.g., "italian food" → "italian"), removes noise words ("recipe", "food"), maps common patterns, deduplicates, standardize() returns cleaned tags, standardizeWithTracking() returns TagModification objects showing original→standardized transformations with wasModified flag, used by TextRecipeParser and ImportViewModel during import
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
