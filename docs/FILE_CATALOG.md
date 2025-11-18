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

### Data - Database
- **AppDatabase.kt** - Room database singleton: Recipe table, version 1, Converters for List<String> and RecipeSource
- **Converters.kt** - Room type converters: List<String> ↔ delimited string, RecipeSource ↔ string

### Navigation
- **NavGraph.kt** - Navigation routes sealed class: Home, RecipeIndex, MealPlanning, GroceryLists, Settings (drawer), AddRecipe, EditRecipe, RecipeDetail (recipe screens with createRoute helpers)

### UI - MainActivity
- **MainActivity.kt** - Orchestrator only: Setup dependencies (AppDatabase, RecipeManager, ViewModelFactory), wire theme and navigation, NO business/navigation logic
- **Navigation.kt** - All navigation logic: NavHost with routes for Home, RecipeIndex, AddRecipe, EditRecipe, RecipeDetail, MealPlanning, GroceryLists, Settings

### UI - Screens
- **HomeScreen.kt** - Landing page: This week's meal plans, recipe suggestions
- **RecipeListScreen.kt** - Recipe browsing: LazyColumn with RecipeCards, FAB for add, search, favorite toggle, empty state, ViewModel integration
- **AddEditRecipeScreen.kt** - Recipe add/edit form: Single screen with title, servings, times, ingredients, instructions, tags, notes, validation
- **RecipeDetailScreen.kt** - Recipe detail view: Servings/time card, ingredients list, numbered instructions, tags, notes, favorite/edit/delete actions, delete confirmation dialog
- **MealPlanningScreen.kt** - Weekly meal planning: Placeholder for future implementation
- **GroceryListScreen.kt** - Shopping lists: Placeholder for future implementation
- **SettingsScreen.kt** - App preferences: Placeholder for future implementation

### UI - Components
- **AppNavigationDrawer.kt** - Responsive navigation drawer: Modal for phones, permanent for tablets, accepts content parameter, drawer header with logo/name, UI only

### UI - ViewModels
- **RecipeViewModel.kt** - Recipe UI state: StateFlow for recipes/currentRecipe/isLoading/error, delegates all business logic to RecipeManager, event functions (loadRecipes, createRecipe, updateRecipe, deleteRecipe, toggleFavorite, searchRecipes)
- **ViewModelFactory.kt** - ViewModel dependency injection: Creates RecipeViewModel with RecipeManager dependency

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
