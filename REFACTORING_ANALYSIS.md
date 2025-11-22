# Recipe Index Codebase Refactoring Analysis

## Summary
Analyzed Kotlin codebase with 23,621 total lines across 51 files. Identified multiple refactoring opportunities focusing on:
- Repetitive code patterns in 3+ locations
- Large files (300+ lines) that can be modularized
- Common utility functions scattered across files
- Consolidatable ViewModels and error handling

---

## CRITICAL FINDINGS (3+ Occurrences)

### 1. REPETITIVE IMPORT SCREEN VALIDATION LOGIC
**Files Affected:** 3 files, ~100+ duplicated lines of identical code
- `/home/user/recipe-index/app/src/main/java/com/recipeindex/app/ui/screens/ImportUrlScreen.kt` (410 lines)
- `/home/user/recipe-index/app/src/main/java/com/recipeindex/app/ui/screens/ImportPhotoScreen.kt` (476 lines)
- `/home/user/recipe-index/app/src/main/java/com/recipeindex/app/ui/screens/ImportPdfScreen.kt` (335 lines)

**Repetitive Pattern:**
All three screens have IDENTICAL validation logic in `handleBack()` function (lines 115-140):
```kotlin
fun handleBack() {
    when (val state = uiState) {
        is ImportViewModel.UiState.Editing -> {
            when {
                state.recipe.title.isBlank() -> {
                    viewModel.showError("Title is required")
                }
                state.recipe.ingredients.isEmpty() -> {
                    viewModel.showError("At least one ingredient is required")
                }
                state.recipe.instructions.isEmpty() -> {
                    viewModel.showError("At least one instruction step is required")
                }
                else -> {
                    viewModel.saveRecipe(state.recipe, ...)
                    onSaveComplete()
                }
            }
        }
        else -> onNavigateBack()
    }
}
```

**Refactoring Suggestion:**
Extract into a utility function:
```kotlin
// In ui/utils/ImportValidationUtils.kt
fun validateImportRecipe(recipe: Recipe): ValidationResult {
    return when {
        recipe.title.isBlank() -> ValidationResult.Error("Title is required")
        recipe.ingredients.isEmpty() -> ValidationResult.Error("At least one ingredient is required")
        recipe.instructions.isEmpty() -> ValidationResult.Error("At least one instruction step is required")
        else -> ValidationResult.Valid
    }
}
```

**Impact:** Eliminates ~30 lines of duplication, centralizes validation logic, makes future changes easier

---

### 2. NEARLY IDENTICAL IMPORT VIEWMODELS
**Files Affected:** 3 files with 96% similar code
- `/home/user/recipe-index/app/src/main/java/com/recipeindex/app/ui/viewmodels/ImportPdfViewModel.kt` (120 lines)
- `/home/user/recipe-index/app/src/main/java/com/recipeindex/app/ui/viewmodels/ImportPhotoViewModel.kt` (150 lines)
- `/home/user/recipe-index/app/src/main/java/com/recipeindex/app/ui/viewmodels/ImportViewModel.kt` (291 lines - slightly different)

**Repetitive Code:**
All ViewModels have identical methods with same structure:
- `updateRecipe(recipe: Recipe)` - Identical implementation
- `saveRecipe(recipe: Recipe)` - Nearly identical save logic
- `showError(message: String)` - Identical error handling
- `reset()` - Identical reset logic
- UiState sealed classes with identical structure (different state names only)

Example duplication (lines 101-119 in both):
```kotlin
fun showError(message: String) {
    val currentState = _uiState.value
    when (currentState) {
        is UiState.SelectFile -> _uiState.value = currentState.copy(errorMessage = message)
        is UiState.Editing -> _uiState.value = currentState.copy(errorMessage = message)
        else -> DebugConfig.debugLog(DebugConfig.Category.IMPORT, "Error: $message")
    }
}
```

**Refactoring Suggestion:**
Create a base ViewModel class:
```kotlin
// In ui/viewmodels/BaseImportViewModel.kt
abstract class BaseImportViewModel<T : Any>(
    protected val recipeManager: RecipeManager
) : ViewModel() {
    
    protected abstract fun createInitialUiState(): T
    
    fun updateRecipe(recipe: Recipe) { /* shared implementation */ }
    fun saveRecipe(recipe: Recipe) { /* shared save logic */ }
    fun showError(message: String) { /* shared error handling */ }
    fun reset() { /* shared reset logic */ }
}
```
Then have ImportPdfViewModel and ImportPhotoViewModel inherit from this base class.

**Impact:** Eliminates ~60 lines of duplication, improves maintainability, reduces bugs from code drift

---

### 3. REPETITIVE MANAGER ERROR HANDLING PATTERN
**Files Affected:** 3 managers with 20+ occurrences of identical pattern
- `/home/user/recipe-index/app/src/main/java/com/recipeindex/app/data/managers/GroceryListManager.kt` (497 lines)
- `/home/user/recipe-index/app/src/main/java/com/recipeindex/app/data/managers/RecipeManager.kt` (244 lines)
- `/home/user/recipe-index/app/src/main/java/com/recipeindex/app/data/managers/MealPlanManager.kt` (240 lines)

**Repetitive Pattern:**
Every suspend function follows this identical try-catch-Result pattern (appears 20+ times):
```kotlin
suspend fun createSomething(item: Something): Result<Long> {
    return try {
        // operation here
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "operation: $id")
        Result.success(id)
    } catch (e: Exception) {
        DebugConfig.error(DebugConfig.Category.MANAGER, "operation failed", e)
        Result.failure(e)
    }
}
```

**Refactoring Suggestion:**
Create extension function for reusable Result wrapping:
```kotlin
// In utils/ResultUtils.kt
suspend inline fun <T> resultOf(
    operation: String,
    block: suspend () -> T
): Result<T> {
    return try {
        val result = block()
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "$operation: success")
        Result.success(result)
    } catch (e: Exception) {
        DebugConfig.error(DebugConfig.Category.MANAGER, "$operation failed", e)
        Result.failure(e)
    }
}

// Usage in managers:
suspend fun createRecipe(recipe: Recipe): Result<Long> = 
    resultOf("createRecipe") {
        validateRecipe(recipe)
        val updatedRecipe = recipe.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        recipeDao.insertRecipe(updatedRecipe)
    }
```

**Impact:** Eliminates ~400 lines of boilerplate, centralizes error handling strategy, reduces DebugConfig call duplication

---

### 4. COMMON SNACKBAR ERROR DISPLAY PATTERN
**Files Affected:** 3 import screens + multiple other screens (28+ usages)
- ImportUrlScreen.kt (lines 92-112)
- ImportPhotoScreen.kt (lines 54-74)
- ImportPdfScreen.kt (lines 39-59)

**Repetitive Pattern:**
All screens have identical LaunchedEffect for error snackbar display:
```kotlin
LaunchedEffect(uiState) {
    when (val state = uiState) {
        is SomeState -> {
            state.errorMessage?.let { error ->
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
            }
        }
        is AnotherState -> {
            state.errorMessage?.let { error ->
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
            }
        }
        else -> { /* No error to show */ }
    }
}
```

**Refactoring Suggestion:**
Extract to a composable helper:
```kotlin
// In ui/utils/SnackbarUtils.kt
@Composable
fun ShowErrorSnackbar(
    uiState: Any,
    snackbarHostState: SnackbarHostState,
    errorExtractor: (Any) -> String? = { 
        (it as? HasErrorMessage)?.errorMessage 
    }
) {
    LaunchedEffect(uiState) {
        errorExtractor(uiState)?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
        }
    }
}
```

**Impact:** Eliminates ~20+ lines per screen, simplifies state handling

---

## LARGE FILES (300+ LINES) REQUIRING MODULARIZATION

### Top 5 Largest Files:

1. **RecipeDetailScreen.kt** - 1,541 lines
   - Shows full recipe with cooking mode, photo carousel, substitution dialog, etc.
   - **Suggestions:** Split into sub-components:
     - `RecipeDetailHeader.kt` (title, servings, times)
     - `RecipeDetailContent.kt` (ingredients, instructions)
     - `RecipeDetailMedia.kt` (photo carousel with pager)
     - `RecipeDetailActions.kt` (buttons, menus)

2. **RecipeListScreen.kt** - 867 lines
   - Recipe browsing with filters, sorting, view modes, dialogs
   - **Suggestions:** Extract:
     - `RecipeListHeader.kt` (search bar, view mode toggle)
     - `RecipeListFilters.kt` (filter bottom sheet logic)
     - `RecipeListContent.kt` (grid/list display logic)

3. **RecipeImportPreview.kt** - 859 lines (COMPONENT)
   - Already a reusable component, but large
   - **Suggestions:** Split into:
     - `RecipeImportPreviewForm.kt` (core preview fields)
     - `RecipeImportImageSelector.kt` (image selection UI)
     - `RecipeImportEditDialog.kt` (edit field dialog)

4. **GroceryListDetailScreen.kt** - 850 lines
   - Grocery list with quick entry, item management, recipe/meal plan pickers
   - **Suggestions:** Extract:
     - `GroceryQuickEntryBar.kt` (quick add logic)
     - `GroceryItemListContent.kt` (items display)
     - `GroceryListActions.kt` (bottom actions)

5. **AddEditMealPlanScreen.kt** - 815 lines
   - Form for meal plan creation/editing
   - **Suggestions:** Extract:
     - `MealPlanDateRangePicker.kt` (date range selection)
     - `MealPlanRecipePicker.kt` (recipe selection logic)

---

## CONSOLIDATABLE UTILITY/VALIDATION FUNCTIONS

### Issue: Validation Logic Scattered Across Files

1. **Recipe Validation** - 3 different implementations:
   - `RecipeManager.kt`: `validateRecipe()` (private, lines 167-172)
   - `RecipeImportPreview.kt`: `isRecipeValid()` (public, lines 628-632)
   - Import screens: Inline validation in `handleBack()` functions

   **Suggestion:** Consolidate into single utility:
   ```kotlin
   // In utils/RecipeValidation.kt
   object RecipeValidation {
       fun validate(recipe: Recipe): ValidationResult
       fun isValid(recipe: Recipe): Boolean
   }
   ```

2. **Date Formatting** - Scattered across files:
   - `ShareHelper.kt`: `formatDate()` (private, line 407)
   - `AddEditMealPlanScreen.kt`: `formatDateRange()` (local function)
   - Recommendation: Add to `TextFormatUtils.kt`

3. **DebugConfig Logging** - Repetitive pattern (14+ files):
   - Every screen/manager has boilerplate logging at start
   - Could extract common entry/exit logging

---

## PARSER CODE REUSE OPPORTUNITY

### Files Affected:
- `/home/user/recipe-index/app/src/main/java/com/recipeindex/app/data/parsers/PhotoRecipeParser.kt` (118 lines)
- `/home/user/recipe-index/app/src/main/java/com/recipeindex/app/data/parsers/PdfRecipeParser.kt` (75 lines)

**Common Pattern:**
Both parsers follow identical flow:
1. Extract text (from PDF/Photo)
2. Check if text is blank
3. Call `TextRecipeParser.parseText()`
4. Handle errors identically

**Suggestion:**
Create base parser class:
```kotlin
abstract class TextBasedRecipeParser : RecipeParser {
    protected abstract suspend fun extractText(source: String): String
    
    override suspend fun parse(source: String): Result<Recipe> = 
        withContext(Dispatchers.IO) {
            try {
                val text = extractText(source)
                if (text.isBlank()) {
                    return@withContext Result.failure(...)
                }
                TextRecipeParser.parseText(text, getSource(), source)
            } catch (e: Exception) {
                Result.failure(...)
            }
        }
    
    protected abstract fun getSource(): RecipeSource
}
```

---

## SUMMARY TABLE

| Issue | Count | Files | Approx LOC | Priority |
|-------|-------|-------|-----------|----------|
| Import validation duplication | 3 screens | ImportPdfScreen, ImportUrlScreen, ImportPhotoScreen | 30 | HIGH |
| Import ViewModel duplication | 3 ViewModels | ImportPdfViewModel, ImportPhotoViewModel | 60 | HIGH |
| Manager Result pattern | 20+ methods | GroceryListManager, RecipeManager, MealPlanManager | 400+ | HIGH |
| Snackbar error pattern | 28+ usages | Multiple screens | 100+ | MEDIUM |
| Recipe validation | 3 locations | RecipeManager, RecipeImportPreview, screens | 15 | MEDIUM |
| Large screens | 5 screens | RecipeDetailScreen, RecipeListScreen, etc. | 5000+ | MEDIUM |
| Parser code reuse | 2 parsers | PhotoRecipeParser, PdfRecipeParser | 50+ | LOW |

---

## IMPLEMENTATION PRIORITY

**Phase 1 (Highest Impact):**
1. Extract import validation utility → removes 30 lines, fixes 3 places
2. Create BaseImportViewModel → removes 60 lines, prevents future duplication
3. Create ResultUtils extension → removes 400+ lines, improves error handling

**Phase 2 (Medium Impact):**
4. Extract snackbar error display helper → removes 100+ lines
5. Consolidate validation functions → centralizes rules
6. Split large screens into sub-components

**Phase 3 (Refactoring):**
7. Create base parser class for text extraction
8. Extract date formatting utilities
9. Consolidate debug logging patterns

