# Recipe Index File Catalog

> **Purpose**: Complete file tree, system relationships, and component descriptions
> **Last Updated**: 2025-11-18

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
│   │   └── RecipeDao.kt
│   │
│   ├── entities/
│   │   └── Recipe.kt
│   │
│   ├── managers/
│   │   └── RecipeManager.kt
│   │
│   ├── parsers/
│   │   ├── PdfRecipeParser.kt
│   │   ├── PhotoRecipeParser.kt
│   │   ├── RecipeParser.kt
│   │   ├── SchemaOrgRecipeParser.kt
│   │   └── TextRecipeParser.kt
│   │
│   ├── AppDatabase.kt
│   └── Converters.kt
│
├── navigation/
│   └── NavGraph.kt
│
├── ui/
│   ├── components/
│   │   └── AppNavigationDrawer.kt
│   │
│   ├── screens/
│   │   ├── AddEditRecipeScreen.kt
│   │   ├── GroceryListScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── ImportPdfScreen.kt
│   │   ├── ImportPhotoScreen.kt
│   │   ├── ImportSourceSelectionScreen.kt
│   │   ├── ImportUrlScreen.kt
│   │   ├── MealPlanningScreen.kt
│   │   ├── RecipeDetailScreen.kt
│   │   ├── RecipeListScreen.kt
│   │   └── SettingsScreen.kt
│   │
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── HearthTheme.kt
│   │   └── Type.kt
│   │
│   ├── viewmodels/
│   │   ├── ImportPdfViewModel.kt
│   │   ├── ImportPhotoViewModel.kt
│   │   ├── ImportViewModel.kt
│   │   ├── RecipeViewModel.kt
│   │   └── ViewModelFactory.kt
│   │
│   ├── MainActivity.kt
│   └── Navigation.kt
│
└── utils/
    └── DebugConfig.kt

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
- PhotoRecipeParser → ML Kit Text Recognition (OCR), supports multiple photos
- TextRecipeParser → Smart pattern matching (detects sections, parses times/servings)
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

### Data - DAOs
- **RecipeDao.kt** - Recipe CRUD operations: getAllRecipes, getRecipeById, getFavoriteRecipes, searchRecipes, insert/update/delete, updateFavoriteStatus (all return Flow)

### Data - Managers
- **RecipeManager.kt** - Recipe business logic: validation, CRUD operations, favorite toggle, recipe scaling stub (delegates to RecipeDao)

### Data - Parsers
- **RecipeParser.kt** - Recipe parser interface: parse(source: String): Result<Recipe> for URL/PDF/Photo parsers
- **SchemaOrgRecipeParser.kt** - Schema.org JSON-LD parser: Jsoup HTML parsing, Schema.org Recipe extraction (HowToStep/HowToSection instructions), ISO 8601 duration conversion, Open Graph fallback, debug logging
- **TextRecipeParser.kt** - Smart pattern matching parser: detects ingredients/instructions sections via regex, parses time strings ("1h 30min"), extracts servings, cleans bullets/numbering from unstructured text
- **PdfRecipeParser.kt** - PDF text extraction parser: Uses PdfBox-Android PDFTextStripper to extract all text from PDF files, delegates to TextRecipeParser for recipe parsing
- **PhotoRecipeParser.kt** - OCR-based parser: Uses ML Kit Text Recognition to extract text from photos/camera, supports multiple photos via parseMultiple(List<Uri>), combines OCR results, delegates to TextRecipeParser

### Data - Database
- **AppDatabase.kt** - Room database singleton: Recipe table, version 1, Converters for List<String> and RecipeSource
- **Converters.kt** - Room type converters: List<String> ↔ delimited string, RecipeSource ↔ string

### Navigation
- **NavGraph.kt** - Navigation routes sealed class: Home, RecipeIndex, MealPlanning, GroceryLists, Settings (drawer), AddRecipe, EditRecipe, RecipeDetail, ImportSourceSelection, ImportUrl, ImportPdf, ImportPhoto (import screens)

### UI - MainActivity
- **MainActivity.kt** - Orchestrator only: Setup dependencies (AppDatabase, RecipeManager, HttpClient, SchemaOrgRecipeParser, PdfRecipeParser, PhotoRecipeParser, ViewModelFactory), wire theme and navigation, NO business/navigation logic
- **Navigation.kt** - All navigation logic: NavHost with routes for Home, RecipeIndex, AddRecipe, EditRecipe, RecipeDetail, MealPlanning, GroceryLists, Settings, ImportSourceSelection, ImportUrl, ImportPdf, ImportPhoto

### UI - Screens
- **HomeScreen.kt** - Landing page: This week's meal plans, recipe suggestions
- **RecipeListScreen.kt** - Recipe browsing: LazyColumn with RecipeCards (photo, title, servings/times, tags), expandable FAB menu (create/import), favorite toggle, empty state, Coil AsyncImage for photos (180dp)
- **AddEditRecipeScreen.kt** - Recipe add/edit form: Single screen with title, servings, times, ingredients, instructions, tags, notes, validation, auto-save on back
- **RecipeDetailScreen.kt** - Recipe detail view: Photo (240dp), servings/time card, ingredients list, tabbed instruction sections (detected by ":" suffix), tags, notes, favorite/edit/delete actions, Coil AsyncImage
- **ImportSourceSelectionScreen.kt** - Import source selection: Choose URL/PDF/Photo import source with cards (all three enabled)
- **ImportUrlScreen.kt** - URL import flow: URL input → loading → recipe preview/edit with photo → save, auto-save on back navigation, Coil AsyncImage
- **ImportPdfScreen.kt** - PDF import flow: File picker (ActivityResultContracts.GetContent) → loading → recipe preview/edit → save, auto-save on back navigation
- **ImportPhotoScreen.kt** - Photo import flow: Camera/gallery pickers (GetMultipleContents for multiple photos) → photo preview grid → loading → recipe preview/edit → save, auto-save on back navigation
- **MealPlanningScreen.kt** - Weekly meal planning: Placeholder for future implementation
- **GroceryListScreen.kt** - Shopping lists: Placeholder for future implementation
- **SettingsScreen.kt** - App preferences: Placeholder for future implementation

### UI - Components
- **AppNavigationDrawer.kt** - Responsive navigation drawer: Modal for phones, permanent for tablets, accepts content parameter, drawer header with logo/name, UI only

### UI - ViewModels
- **RecipeViewModel.kt** - Recipe UI state: StateFlow for recipes/currentRecipe/isLoading/error, delegates all business logic to RecipeManager, event functions (loadRecipes, createRecipe, updateRecipe, deleteRecipe, toggleFavorite, searchRecipes)
- **ImportViewModel.kt** - URL import UI state: StateFlow<UiState> (Input → Loading → Editing → Saved), fetchRecipeFromUrl(), updateRecipe(), saveRecipe(), reset()
- **ImportPdfViewModel.kt** - PDF import UI state: StateFlow<UiState> (SelectFile → Loading → Editing → Saved), fetchRecipeFromPdf(Uri), updateRecipe(), saveRecipe(), reset()
- **ImportPhotoViewModel.kt** - Photo import UI state: StateFlow<UiState> (SelectPhoto → Loading → Editing → Saved), fetchRecipeFromPhoto(Uri), fetchRecipeFromPhotos(List<Uri>), updateRecipe(), saveRecipe(), reset()
- **ViewModelFactory.kt** - ViewModel dependency injection: Creates RecipeViewModel, ImportViewModel, ImportPdfViewModel, ImportPhotoViewModel with RecipeManager and parser dependencies (SchemaOrgRecipeParser, PdfRecipeParser, PhotoRecipeParser)

### UI - Theme
- **Color.kt** - Hearth color palette: Terracotta, Clay, SageGreen, Cream neutrals, cooking mode high-contrast colors
- **Type.kt** - Hearth typography: Material 3 type scale with readable fonts for recipe content
- **HearthTheme.kt** - Theme composable: Light/dark color schemes, dynamic color support, typography integration

### Utils
- **DebugConfig.kt** - Centralized logging: Category-based filtering (NAVIGATION, DATABASE, IMPORT, UI, MANAGER, GENERAL), replaces android.util.Log

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
