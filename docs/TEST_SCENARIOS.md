# Recipe Index Test Scenarios

> **Purpose**: Test coverage and scenarios to implement for automated testing
> **Last Updated**: 2025-11-21

**See Also:**
- [DECISION_LOG.md](./DECISION_LOG.md) - Architectural decision records (WHAT/WHY/WHEN decisions were made)
- [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) - Quick lookup ("I need to...") and architecture patterns (HOW to implement)
- [FILE_CATALOG.md](./FILE_CATALOG.md) - Complete file tree, system relationships, and component descriptions
- [PROJECT_STATUS.md](./PROJECT_STATUS.md) - Current status, core principles, completed features, and backlog
- [PROJECT_CONVENTIONS.md](../PROJECT_CONVENTIONS.md) - How to maintain documentation

**Quick Navigation:** [How to Update](#how-to-update-this-file) | [Coverage Summary](#test-coverage-summary) | [Scenarios](#test-scenarios-by-feature)

---

## How to Update This File

### When you write a new test:
1. **Add to Coverage Summary** - Update percentage/counts for the relevant category
2. **Add to Test Scenarios** - List the scenario under appropriate feature section
3. **Format:** `- [x] Scenario description (TestClassName.kt:methodName)`

### When you identify a scenario needing tests:
1. **Add to Test Scenarios** with `[ ]` (unchecked) under appropriate feature
2. **Format:** `- [ ] Scenario description (planned)`

### When you implement a planned test:
1. **Change `[ ]` to `[x]`** and add test file reference
2. **Update Coverage Summary** counts

### What goes here:
- ✅ Unit test scenarios (Managers, DAOs, utilities, importers)
- ✅ Integration test scenarios (database operations, multi-component flows)
- ✅ UI test scenarios (screen interactions, navigation, user flows)
- ✅ Edge cases and error handling tests

### What does NOT go here:
- ❌ Manual testing checklists (those go in PR descriptions)
- ❌ Performance benchmarks (add separate doc if needed)
- ❌ Implementation details of tests (those go in test code comments)

### Format Guidelines:
- Keep scenario descriptions to 1 sentence
- Group by feature/component (Recipe Management, Meal Planning, etc.)
- Mark completed tests with `[x]`, planned with `[ ]`
- Include test file reference when implemented

---

## Test Coverage Summary

> **Status**: Initial tests implemented for Phases 3-4

### Current Coverage
- **Unit Tests**: 146 tests in 6 files (~25% file coverage, untested: DAOs, PhotoRecipeParser, PdfRecipeParser)
  - ✅ RecipeManagerTest.kt (28 tests)
  - ✅ TextRecipeParserTest.kt (31 tests)
  - ✅ SchemaOrgRecipeParserTest.kt (25 tests)
  - ✅ ConvertersTest.kt (24 tests)
  - ✅ GroceryListManagerTest.kt (22 tests)
  - ✅ MealPlanManagerTest.kt (16 tests)
- **Integration Tests**: 0 (planned: DAO tests, database migrations)
- **UI Tests**: 0 (planned: screen interactions, navigation flows)
- **Total Scenarios**: 146 implemented, 30+ planned

### Coverage Gaps (Priority Order)
1. ~~**CRITICAL**: TextRecipeParser~~ ✅ COMPLETED (31 tests)
2. ~~**CRITICAL**: RecipeManager~~ ✅ COMPLETED (28 tests)
3. ~~**CRITICAL**: TypeConverters~~ ✅ COMPLETED (24 tests)
4. ~~**HIGH**: SchemaOrgRecipeParser~~ ✅ COMPLETED (25 tests)
5. **HIGH**: Database migrations (v1→v2→v3)
6. **MEDIUM**: DAOs (RecipeDao, MealPlanDao, GroceryListDao, GroceryItemDao)
7. **MEDIUM**: PdfRecipeParser, PhotoRecipeParser
8. **LOW**: ViewModels (mostly delegation, integration tests preferred)
9. **LOW**: UI screens (manual testing acceptable for MVP)

### Next Testing Priorities
1. ~~RecipeManager~~ ✅ COMPLETED
2. ~~TextRecipeParser~~ ✅ COMPLETED
3. ~~SchemaOrgRecipeParser~~ ✅ COMPLETED
4. ~~TypeConverters~~ ✅ COMPLETED
5. Database migration tests (Room v1→v2→v3 with data preservation)
6. DAOs (integration tests with in-memory database)

---

## Test Scenarios by Feature

### Recipe Management
- [x] Create recipe with valid data succeeds (RecipeManagerTest.kt:createRecipe succeeds with valid recipe)
- [x] Create recipe fails with blank title (RecipeManagerTest.kt:createRecipe fails with blank title)
- [x] Create recipe fails with empty ingredients (RecipeManagerTest.kt:createRecipe fails with empty ingredients)
- [x] Create recipe fails with empty instructions (RecipeManagerTest.kt:createRecipe fails with empty instructions)
- [x] Create recipe fails with zero servings (RecipeManagerTest.kt:createRecipe fails with zero servings)
- [x] Create recipe fails with negative servings (RecipeManagerTest.kt:createRecipe with negative servings fails)
- [x] Create recipe handles all optional fields (RecipeManagerTest.kt:createRecipe handles all optional fields)
- [x] Create recipe allows minimal valid recipe (RecipeManagerTest.kt:validateRecipe allows single ingredient and instruction)
- [x] Update recipe preserves database ID (RecipeManagerTest.kt:updateRecipe preserves database ID)
- [x] Update recipe preserves createdAt timestamp (RecipeManagerTest.kt:updateRecipe succeeds with valid recipe)
- [x] Update recipe validates same as create (RecipeManagerTest.kt:updateRecipe validates same as create)
- [x] Delete recipe removes from database (RecipeManagerTest.kt:deleteRecipe removes recipe from database)
- [x] Delete recipe handles DAO errors (RecipeManagerTest.kt:deleteRecipe handles DAO errors)
- [x] Delete recipe removes recipe from meal plans (RecipeManagerTest.kt:deleteRecipe removes recipe from meal plans)
- [x] Toggle favorite sets to true (RecipeManagerTest.kt:toggleFavorite sets favorite to true)
- [x] Toggle favorite sets to false (RecipeManagerTest.kt:toggleFavorite sets favorite to false)
- [x] Toggle favorite handles errors (RecipeManagerTest.kt:toggleFavorite handles DAO errors)
- [x] getAllRecipes returns Flow from DAO (RecipeManagerTest.kt:getAllRecipes returns Flow from DAO)
- [x] getRecipeById returns Flow from DAO (RecipeManagerTest.kt:getRecipeById returns Flow from DAO)
- [x] getFavoriteRecipes returns Flow from DAO (RecipeManagerTest.kt:getFavoriteRecipes returns Flow from DAO)
- [x] Search recipes passes query to DAO (RecipeManagerTest.kt:searchRecipes passes query to DAO)
- [x] Scale recipe updates servings (RecipeManagerTest.kt:scaleRecipe updates servings)
- [x] Scale recipe down preserves structure (RecipeManagerTest.kt:scaleRecipe down preserves recipe structure)

### Recipe Import

#### URL Import (SchemaOrgRecipeParser)
- [x] Parse Schema.org JSON-LD recipe (SchemaOrgRecipeParserTest.kt:parse extracts recipe from Schema org JSON-LD)
- [x] Parse @graph array with Recipe (SchemaOrgRecipeParserTest.kt:parse handles @graph array with Recipe)
- [ ] Parse Article/BlogPosting with embedded recipe data (planned - detect @type: "Article" with recipeIngredient/recipeInstructions)
- [ ] HTML scraping fallback extracts ingredients (planned - finds ingredients using CSS selectors when Schema.org unavailable)
- [ ] HTML scraping fallback extracts instructions (planned - finds instructions using CSS selectors when Schema.org unavailable)
- [ ] Extract multiple image URLs from recipe page (planned - Schema.org + HTML scraping + Open Graph)
- [ ] ImportUrlScreen displays image selection grid with checkboxes (planned)
- [ ] ImportUrlScreen saves selected images to mediaPaths (planned)
- [ ] ImportUrlScreen downloads images before navigation (planned)
- [ ] HTML scraping fallback requires both ingredients and instructions (planned - returns null if only one found)
- [x] Fallback to Open Graph when no Schema.org (SchemaOrgRecipeParserTest.kt:parse falls back to Open Graph)
- [x] Fail when no recipe data found (SchemaOrgRecipeParserTest.kt:parse fails when no recipe data found)
- [x] Default to 4 servings when not specified (SchemaOrgRecipeParserTest.kt:parse defaults to 4 servings)
- [x] Parse ISO 8601 duration - minutes only (SchemaOrgRecipeParserTest.kt:parseIsoDuration handles minutes only)
- [x] Parse ISO 8601 duration - hours only (SchemaOrgRecipeParserTest.kt:parseIsoDuration handles hours only)
- [x] Parse ISO 8601 duration - hours and minutes (SchemaOrgRecipeParserTest.kt:parseIsoDuration handles hours and minutes)
- [x] Parse servings from various formats (SchemaOrgRecipeParserTest.kt:parseServings extracts number from string)
- [x] Parse image URL from string (SchemaOrgRecipeParserTest.kt:parseImage handles string URL)
- [x] Parse image from ImageObject (SchemaOrgRecipeParserTest.kt:parseImage handles ImageObject)
- [x] Parse image from array (SchemaOrgRecipeParserTest.kt:parseImage handles array of URLs)
- [x] Parse HowToStep instructions (SchemaOrgRecipeParserTest.kt:parseInstructions handles HowToStep objects)
- [x] Parse HowToSection with steps (SchemaOrgRecipeParserTest.kt:parseInstructions handles HowToSection)
- [x] Parse comma-separated tags (SchemaOrgRecipeParserTest.kt:parseJsonArrayToStrings handles comma-separated string)
- [ ] Parse tags from recipeCategory only (planned - excludes keywords field)
- [ ] Parse tags from recipeCuisine only (planned - excludes keywords field)
- [ ] Parse HTML category links with rel=category (planned - WordPress categories)
- [ ] Parse HTML tag links with rel=tag (planned - WordPress tags)
- [ ] Combine Schema.org tags with HTML categories (planned - merge both sources)
- [ ] Ignore keywords field even if present (planned - prevent garbage tags)

#### Text Import (TextRecipeParser)
- [x] Detect ingredients section (TextRecipeParserTest.kt:detectSections finds ingredients section)
- [x] Detect instructions with variations (TextRecipeParserTest.kt:detectSections finds instructions with variations)
- [x] Skip footer with ingredients keyword (TextRecipeParserTest.kt:detectSections skips footer with ingredients keyword)
- [x] Detect servings variations (TextRecipeParserTest.kt:detectSections finds servings variations)
- [x] Filter save recipe CTAs (TextRecipeParserTest.kt:isWebsiteNoise detects save recipe CTAs)
- [x] Filter footer text (TextRecipeParserTest.kt:isWebsiteNoise detects footer text)
- [x] Allow valid recipe content (TextRecipeParserTest.kt:isWebsiteNoise allows valid recipe content)
- [x] Validate ingredient with quantities (TextRecipeParserTest.kt:looksLikeIngredient accepts lines with quantities)
- [x] Validate ingredient with food words (TextRecipeParserTest.kt:looksLikeIngredient accepts common food words)
- [x] Reject short lines as ingredients (TextRecipeParserTest.kt:looksLikeIngredient rejects very short lines)
- [x] Validate instruction with cooking verbs (TextRecipeParserTest.kt:looksLikeInstruction accepts lines with cooking verbs)
- [x] Validate instruction with temperature/time (TextRecipeParserTest.kt:looksLikeInstruction accepts lines with temperature or time)
- [x] Reject footer patterns in instructions (TextRecipeParserTest.kt:looksLikeInstruction rejects footer patterns)
- [x] Parse time - minutes only (TextRecipeParserTest.kt:parseTimeString handles minutes only)
- [x] Parse time - hours only (TextRecipeParserTest.kt:parseTimeString handles hours only)
- [x] Parse time - hours and minutes (TextRecipeParserTest.kt:parseTimeString handles hours and minutes)
- [x] Clean ingredient removes bullets (TextRecipeParserTest.kt:cleanIngredient removes bullets and numbering)
- [x] Clean instruction removes step numbers (TextRecipeParserTest.kt:cleanInstruction removes step numbers)
- [x] Parse well-formed recipe (TextRecipeParserTest.kt:parseText succeeds with well-formed recipe)
- [x] Filter noise from ingredients (TextRecipeParserTest.kt:parseText filters website noise from ingredients)
- [x] Filter noise from instructions (TextRecipeParserTest.kt:parseText filters website noise from instructions)
- [x] Fail with empty text (TextRecipeParserTest.kt:parseText fails with empty text)
- [x] Set correct source (TextRecipeParserTest.kt:parseText sets correct source)

#### PDF/Photo Import
- [ ] PDF import parses structured recipe document (planned - PdfRecipeParser delegates to TextRecipeParser)
- [ ] PDF multi-column layout recovers misplaced ingredients (sortByPosition=true, recoverMisplacedIngredients)
- [ ] PDF continuation lines are joined (joinInstructionLines - lines not starting with digit continue previous step)
- [ ] PDF page noise is filtered (isPdfPageNoise - URLs, page headers "11/18/25, 12:34 PM", page numbers)
- [ ] PDF tips/notes section is extracted to sourceTips field (separate from user notes)
- [ ] PDF ingredient quantities are preserved (cleanIngredient only strips "1." not "4 chicken")
- [ ] PDF title extraction skips date/URL lines (isValidTitle in extractTitle)
- [ ] Photo OCR import extracts text from image (planned - PhotoRecipeParser delegates to TextRecipeParser)

#### Tag Management
- [ ] TagStandardizer normalizes tag variations to standard forms (planned - e.g., "italian food"→"italian")
- [ ] TagStandardizer removes noise words from tags (planned - removes "recipe", "meal", "dish", etc.)
- [ ] TagStandardizer filters standalone junk tags (planned - "recipes", "meals", "ideas", "dinner ideas")
- [ ] TagStandardizer filters junk phrases (planned - "how to make", "for beginners", "best recipes")
- [ ] TagStandardizer filters tags with >4 words (planned - long phrases likely junk)
- [ ] TagStandardizer filters branded diet terms (planned - "Weight Watchers", "WW")
- [ ] TagStandardizer consolidates holidays to "special occasion" (planned - Valentine's, Christmas, etc.)
- [ ] TagStandardizer consolidates ingredient types (planned - "chicken thigh"→"chicken", "ground beef"→"beef")
- [ ] TagStandardizer deduplicates identical tags after normalization (planned)
- [ ] TagStandardizer.standardize() returns silent standardized list (planned)
- [ ] TagStandardizer.standardizeWithTracking() returns TagModification objects (planned)
- [ ] TagModification tracks original, standardized, and wasModified flag (planned)
- [ ] Tag auto-suggestion filters existing tags by user input (planned - appears after 2 chars)
- [ ] Tag auto-suggestion excludes already-selected tags (planned)
- [ ] Tag auto-suggestion limits to 5 results (planned)
- [ ] ImportViewModel.getAllExistingTags() collects all unique tags from database (planned)

### Share/Import System
- [ ] ShareHelper.shareRecipe() creates SharePackage with JSON format (planned)
- [ ] ShareHelper.shareRecipe() includes base64-encoded photo when present (planned)
- [ ] ShareHelper.shareRecipe() creates human-readable text fallback (planned)
- [ ] ShareHelper.shareRecipe() launches Android share sheet (planned - integration test)
- [ ] ShareHelper.shareMealPlan() includes all recipes with photos (planned)
- [ ] ShareHelper.shareMealPlan() includes user notes (planned)
- [ ] ShareHelper.shareGroceryList() includes all items with quantities/units (planned)
- [ ] ShareHelper.encodePhoto() scales photo to max 1920px (planned)
- [ ] ShareHelper.encodePhoto() limits file size to 10MB (planned)
- [ ] ShareHelper.decodePhoto() saves base64 string to file (planned)
- [ ] ShareHelper.importFromJson() detects recipe duplicates by title+sourceUrl (planned)
- [ ] ShareHelper.importFromJson() returns DuplicateDetected result when found (planned)
- [ ] ShareHelper.importFromJson() returns Success result when no duplicates (planned)
- [ ] ShareHelper.generateIncrementedTitle() appends (2) for first duplicate (planned)
- [ ] ShareHelper.generateIncrementedTitle() increments to (3), (4), etc. (planned)
- [ ] ImportManager.importFromJson() delegates to ShareHelper for duplicate detection (planned)
- [ ] ImportManager.importRecipeWithAction() replaces existing recipe with REPLACE action (planned)
- [ ] ImportManager.importRecipeWithAction() creates new recipe with incremented title for KEEP_BOTH (planned)
- [ ] ImportManager.importRecipeWithAction() skips import and returns existing ID for SKIP (planned)
- [ ] ImportManager.importRecipeWithAction() decodes and saves photo when present (planned)
- [ ] ImportManager.importMealPlanFromJson() imports all recipes first (planned)
- [ ] ImportManager.importMealPlanFromJson() handles per-recipe duplicate actions (planned)
- [ ] ImportManager.importMealPlanFromJson() creates meal plan with imported recipe IDs (planned)
- [ ] ImportManager.importGroceryListFromJson() creates new list (planned)
- [ ] ImportManager.importGroceryListFromJson() adds all items directly to database (planned)
- [ ] RecipeDuplicateDialog shows side-by-side preview of existing and new recipe (planned - UI test)
- [ ] RecipeDuplicateDialog provides Replace/Keep Both/Skip action buttons (planned - UI test)
- [ ] MealPlanImportDialog shows list of recipes with duplicate detection (planned - UI test)
- [ ] MealPlanImportDialog allows per-recipe duplicate action selection (planned - UI test)
- [ ] GroceryListImportDialog shows item count and confirmation button (planned - UI test)
- [ ] MainActivity.handleIncomingIntent() extracts JSON from ACTION_SEND intent (planned)
- [ ] MainActivity.handleIncomingIntent() reads file from ACTION_VIEW intent (planned)
- [ ] MainActivity.handleIncomingIntent() stores JSON in pendingImportJson (planned)
- [ ] SettingsScreen file picker launches OpenDocument contract (planned - UI test)
- [ ] SettingsScreen stores imported file in pendingImportJson (planned - UI test)
- [ ] RecipeCard context menu includes Share option (planned - UI test)
- [ ] MealPlanCard context menu includes Share option (planned - UI test)
- [ ] GroceryListCard action row includes Share button (planned - UI test)
- [ ] GroceryListDetailScreen top bar includes Share icon button (planned - UI test)
- [ ] ImportSourceSelectionScreen shows "From File" card in Import tab (planned - UI test)
- [ ] ImportSourceSelectionScreen file picker launches when "From File" clicked (planned - UI test)
- [ ] ImportSourceSelectionScreen stores imported JSON in pendingImportJson (planned - UI test)
- [ ] ImportSourceSelectionScreen shows success feedback after file selection (planned - UI test)
- [ ] MealPlanningScreen shows FileUpload icon button in top bar (planned - UI test)
- [ ] MealPlanningScreen file picker launches when import button clicked (planned - UI test)
- [ ] MealPlanningScreen shows snackbar feedback after file selection (planned - UI test)
- [ ] GroceryListScreen shows FileUpload icon button in top bar (planned - UI test)
- [ ] GroceryListScreen file picker launches when import button clicked (planned - UI test)
- [ ] GroceryListScreen shows snackbar feedback after file selection (planned - UI test)

### Meal Planning
- [x] Create meal plan with flexible date range (Sun-Thu, single day) (MealPlanManagerTest.kt:createMealPlan accepts flexible date range)
- [x] Create meal plan with no dates (indefinite/special event) (MealPlanManagerTest.kt:createMealPlan accepts null dates)
- [ ] Add multiple recipes to meal plan (planned)
- [x] Auto-aggregate ingredient tags from recipes (MealPlanManagerTest.kt:getAutoTags aggregates ingredient tags)
- [x] Auto-aggregate special event tags from recipes (MealPlanManagerTest.kt:getAutoTags aggregates ingredient tags)
- [x] Detect special event from plan name ("Thanksgiving Dinner" → "Thanksgiving" tag) (MealPlanManagerTest.kt:detectSpecialEventFromName tests)
- [ ] Duplicate meal plan creates copy with "(Copy)" suffix (planned)
- [x] Delete meal plan preserves associated recipes (MealPlanManagerTest.kt:deleteMealPlan preserves recipes)
- [ ] Search meal plans by name (planned)
- [ ] RecipePickerBottomSheet filters recipes by search query (planned)
- [ ] RecipePickerBottomSheet defaults to Existing tab (planned - UI test)
- [ ] DateRangePickerDialog allows selecting single date (planned - UI test)
- [ ] DateRangePickerDialog allows selecting date range (planned - UI test)
- [ ] DateRangePickerDialog shows Clear button when dates selected (planned - UI test)
- [ ] AddEditMealPlanScreen shows selected dates as supporting text (planned - UI test)
- [ ] AddEditMealPlanScreen overflow menu visible when onAddToGroceryList callback provided (planned)
- [ ] AddEditMealPlanScreen "Add to Grocery List" menu item disabled when no recipes selected (planned)
- [ ] AddEditMealPlanScreen "Add to Grocery List" menu item enabled when recipes selected (planned)
- [ ] Meal plan persists across app restarts (planned - integration test needed)
- [ ] Adding meal plan to grocery list shows success snackbar (planned - UI test)
- [ ] Success snackbar includes meal plan name and list name (planned - UI test)

### Grocery Lists - Ingredient Consolidation
- [x] Parse ingredient with quantity and unit (e.g., "2 lbs chicken breast") (GroceryListManagerTest.kt:parseIngredient extracts quantity and unit)
- [x] Parse ingredient with fraction (e.g., "1/2 cup flour") (GroceryListManagerTest.kt:parseIngredient handles fractions)
- [x] Parse ingredient with mixed number (e.g., "1 1/2 cups sugar") (GroceryListManagerTest.kt:parseIngredient handles mixed numbers)
- [x] Remove "diced" modifier when consolidating (GroceryListManagerTest.kt:parseIngredient removes diced modifier)
- [x] Remove "chopped" modifier when consolidating (GroceryListManagerTest.kt:parseIngredient removes chopped modifier)
- [x] Remove "shredded" modifier when consolidating (GroceryListManagerTest.kt:parseIngredient removes shredded modifier)
- [x] Remove "sliced" modifier when consolidating (GroceryListManagerTest.kt:parseIngredient removes sliced modifier)
- [x] Keep "minced" separate from whole (different prep) (GroceryListManagerTest.kt:parseIngredient keeps minced separate)
- [ ] Consolidate matching name+unit pairs sums quantities (GroceryListManagerTest.kt - needs actual result verification)
- [ ] Different units remain separate (lbs vs cups) (planned)
- [x] Missing quantity creates item without quantity (GroceryListManagerTest.kt:parseIngredient handles ingredient without quantity)
- [x] Missing unit creates item with quantity only (GroceryListManagerTest.kt:parseIngredient handles ingredient without unit)
- [x] Merge source recipe IDs when consolidating duplicates (GroceryListManagerTest.kt:addRecipesToList tracks source recipes)

### Grocery Lists - List Management
- [x] Create grocery list with name (GroceryListManagerTest.kt:createList succeeds with valid name)
- [x] Create list fails with blank name (GroceryListManagerTest.kt:createList fails with blank name)
- [x] Add recipes to existing list consolidates with existing items (GroceryListManagerTest.kt:addRecipesToList consolidates duplicate ingredients)
- [ ] Add meal plan to list extracts all recipe ingredients (planned)
- [ ] Add manual item via text field (planned)
- [ ] Toggle item checked status (planned)
- [ ] Clear checked items removes only checked (planned)
- [ ] Item detail shows all source recipes (planned)
- [ ] Update item quantity/unit/notes (planned)
- [ ] Delete item removes from list (planned)
- [ ] Delete list removes all items (cascade) (planned)
- [ ] Create list and add recipes uses callback pattern to ensure list exists before inserting items (planned - integration test)
- [ ] Search lists by name (planned)
- [ ] getItemCount returns correct count (planned - DAO integration test)
- [ ] getCheckedCount returns correct checked count (planned - DAO integration test)

### Database

#### TypeConverters
- [x] List<String> serializes to delimited string (ConvertersTest.kt:fromStringList serializes list to delimited string)
- [x] List<String> deserializes from delimited string (ConvertersTest.kt:toStringList deserializes delimited string to list)
- [x] List<String> handles empty list/string (ConvertersTest.kt:toStringList handles empty string, fromStringList handles empty list)
- [x] List<String> handles single item (ConvertersTest.kt:fromStringList/toStringList handles single item)
- [x] List<String> roundtrip preserves data (ConvertersTest.kt:stringList roundtrip preserves data)
- [x] List<String> handles special characters (ConvertersTest.kt:stringList handles items with special characters)
- [x] List<String> handles Unicode (ConvertersTest.kt:stringList handles Unicode characters)
- [x] List<Long> serializes to comma-delimited string (ConvertersTest.kt:fromLongList serializes list to comma-delimited string)
- [x] List<Long> deserializes from comma-delimited string (ConvertersTest.kt:toLongList deserializes comma-delimited string to list)
- [x] List<Long> handles empty list/string (ConvertersTest.kt:toLongList handles empty string, fromLongList handles empty list)
- [x] List<Long> handles single item (ConvertersTest.kt:fromLongList/toLongList handles single item)
- [x] List<Long> roundtrip preserves data (ConvertersTest.kt:longList roundtrip preserves data)
- [x] List<Long> handles large numbers (ConvertersTest.kt:longList handles large numbers)
- [x] List<Long> handles many items (ConvertersTest.kt:longList handles many items)
- [x] RecipeSource converts all enum values (ConvertersTest.kt:fromRecipeSource/toRecipeSource converts all values)
- [x] RecipeSource roundtrip preserves data (ConvertersTest.kt:recipeSource roundtrip preserves data)

#### Migrations
- [ ] Room database migration v1→v2 adds MealPlan table (planned)
- [ ] Room database migration v2→v3 adds GroceryList and GroceryItem tables (planned)
- [ ] MealPlan foreign key to Recipe enforces referential integrity (planned)
- [ ] GroceryItem foreign key to GroceryList enforces referential integrity (planned)
- [ ] Cascade delete: deleting GroceryList deletes all GroceryItems (planned)

### UI Screens

#### HomeScreen
- [ ] HomeScreen displays Recent Recipes carousel with last 5 recipes by creation date (planned)
- [ ] HomeScreen displays Favorites carousel with starred recipes (planned)
- [ ] HomeScreen displays This Week's Meal Plan when date range includes today (planned)
- [ ] HomeScreen Quick Actions navigate to import, create, and recipe list (planned)
- [ ] HomeScreen recipe cards support delete, favorite, add to grocery list, add to meal plan (planned)
- [ ] HomeScreen refreshes carousels after recipe deletion (planned)
- [ ] HomeScreen shows empty state when no recipes exist (planned)

#### RecipeListScreen
- [ ] RecipeListScreen displays delete option in card context menu (planned)
- [ ] RecipeListScreen deletes recipe when delete menu item clicked (planned)
- [ ] RecipeListScreen shows delete in red with trash icon (planned)
- [ ] RecipeListScreen FAB navigates to tabbed import screen (planned)
- [ ] RecipeListScreen displays images from mediaPaths with photoPath fallback (planned)
- [ ] RecipeListScreen RecipeCard is internal and reusable in HomeScreen (planned)

#### RecipeDetailScreen
- [ ] RecipeDetailScreen displays swipeable image carousel when multiple images exist (planned)
- [ ] RecipeDetailScreen shows page indicator dots for 2+ images (planned)
- [ ] RecipeDetailScreen Cook Mode Select All button toggles all ingredients (planned)
- [ ] RecipeDetailScreen Cook Mode Select All button toggles all instructions (planned)
- [ ] RecipeDetailScreen Cook Mode buttons show in section headers, not timer card (planned)
- [ ] RecipeDetailScreen Cook Mode button text changes based on checked state (planned)
- [ ] RecipeDetailScreen unit conversion toggle button shows/hides both units inline (planned)
- [ ] RecipeDetailScreen unit conversion toggle button highlights when active (planned)
- [ ] RecipeDetailScreen unit conversion toggle forces BOTH when enabled (planned)
- [ ] RecipeDetailScreen unit conversion toggle uses settings preference when disabled (planned)
- [ ] RecipeDetailScreen Quick Note button appears in cook mode card (planned)
- [ ] RecipeDetailScreen Quick Note dialog pre-populates with existing notes (planned)
- [ ] RecipeDetailScreen Quick Note dialog saves notes to recipe (planned)
- [ ] RecipeDetailScreen Quick Note dialog saves null when notes cleared (planned)
- [ ] RecipeDetailScreen tags hidden when cook mode is active (planned)
- [ ] RecipeDetailScreen tags visible when cook mode is not active (planned)

#### ImportSourceSelectionScreen
- [ ] ImportSourceSelectionScreen displays Import and Create tabs (planned)
- [ ] ImportSourceSelectionScreen Import tab shows URL/PDF/Photo cards (planned)
- [ ] ImportSourceSelectionScreen Create tab shows manual entry button (planned)
- [ ] ImportSourceSelectionScreen navigates to correct import screen (planned)
- [ ] ImportSourceSelectionScreen navigates to manual entry screen (planned)

#### GroceryListDetailScreen
- [ ] GroceryListDetailScreen formats quantities < 0.25 as "1/4" (planned)
- [ ] GroceryListDetailScreen formats quantities 0.25-0.4 as "1/3" (planned)
- [ ] GroceryListDetailScreen formats quantities 0.4-0.625 as "1/2" (planned)
- [ ] GroceryListDetailScreen formats quantities 0.625-0.9 as "3/4" (planned)
- [ ] GroceryListDetailScreen formats quantities >= 0.9 as whole numbers (planned)
- [ ] GroceryListDetailScreen formats large quantities with max 1 decimal place (planned)
- [ ] GroceryListDetailScreen formats whole numbers without decimals (planned)

#### SettingsScreen
- [ ] SettingsScreen displays Liquid Volume Units section (planned)
- [ ] SettingsScreen displays Dry Volume Units section (planned)
- [ ] SettingsScreen displays Weight Units section (planned)
- [ ] SettingsScreen saves liquidVolumePreference to AppSettings (planned)
- [ ] SettingsScreen saves dryVolumePreference to AppSettings (planned)
- [ ] SettingsScreen saves weightPreference to AppSettings (planned)
- [ ] SettingsScreen radio buttons reflect current settings (planned)

### FilterSortGroup Library
- [ ] Filter.matches() returns true when item matches criteria (planned)
- [ ] Filter.matches() returns false when item doesn't match criteria (planned)
- [ ] AndFilter combines multiple filters with AND logic (planned)
- [ ] OrFilter combines multiple filters with OR logic (planned)
- [ ] Sort.comparator sorts items in ASC direction (planned)
- [ ] Sort.comparator sorts items in DESC direction when reversed() (planned)
- [ ] GroupBy.extractKey() returns correct grouping key for items (planned)
- [ ] FilterSortGroupManager.filteredItems applies search query (planned)
- [ ] FilterSortGroupManager.filteredItems applies single filter (planned)
- [ ] FilterSortGroupManager.filteredItems applies multiple filters with AND logic (planned)
- [ ] FilterSortGroupManager.filteredItems applies sort (planned)
- [ ] FilterSortGroupManager.groupedItems returns null when no grouping active (planned)
- [ ] FilterSortGroupManager.groupedItems groups items by key when active (planned)
- [ ] FilterSortGroupManager.groupedItems sorts groups by key (planned)
- [ ] FilterSortGroupManager.groupedItems sorts items within each group when sort active (planned)
- [ ] FavoriteFilter matches only favorite recipes (planned)
- [ ] TagFilter matches recipes with specified tag (planned)
- [ ] SourceFilter matches recipes from specified source (planned)
- [ ] CookTimeFilter matches recipes within time range (planned)
- [ ] TitleSort orders recipes alphabetically case-insensitive (planned)
- [ ] DateCreatedSort orders recipes by creation date (planned)
- [ ] CookTimeSort orders recipes by total time (planned)
- [ ] CookTimeSort places recipes without time at end (planned)
- [ ] SourceGrouping groups recipes by source type (planned)
- [ ] FavoriteGrouping groups recipes with favorites first (planned)
- [ ] CookTimeGrouping categorizes recipes into Quick/Medium/Long/Unknown (planned)

### UI Components
- [ ] AppDatePickerDialog displays Material3 DatePicker (planned)
- [ ] AppDatePickerDialog calls onDateSelected with selected date (planned)
- [ ] AppDatePickerDialog calls onDismiss when dismissed (planned)
- [ ] FilterChipRow renders filter chips for available filters (planned - UI test)
- [ ] FilterChipRow shows active state for selected filters (planned - UI test)
- [ ] FilterChipRow shows "Clear All" button when filters active (planned - UI test)
- [ ] FilterChipRow calls onFilterToggle when chip clicked (planned - UI test)
- [ ] SortMenu opens dropdown when icon clicked (planned - UI test)
- [ ] SortMenu shows checkmark for active sort (planned - UI test)
- [ ] SortMenu shows direction arrow for active sort (planned - UI test)
- [ ] SortMenu toggles direction when active sort clicked (planned - UI test)
- [ ] SortMenu selects new sort when inactive sort clicked (planned - UI test)
- [ ] TagModificationDialog displays original→standardized transformations (planned)
- [ ] TagModificationDialog shows strikethrough on original tags (planned)
- [ ] TagModificationDialog allows inline editing of each tag (planned)
- [ ] TagModificationDialog shows remove button (minus icon) for each tag (planned)
- [ ] TagModificationDialog marks tag for deletion when remove clicked (planned)
- [ ] TagModificationDialog shows deleted tag in red with strikethrough (planned)
- [ ] TagModificationDialog shows restore button for deleted tags (planned)
- [ ] TagModificationDialog restores tag when restore button clicked (planned)
- [ ] TagModificationDialog filters out blank tags when accepting (planned)
- [ ] TagModificationDialog filters out modifications with wasModified=false (planned)
- [ ] Icon-over-text buttons render icon above text label (planned)
- [ ] Icon-over-text buttons use 24dp icons with labelSmall text (planned)
- [ ] Smart toggle button changes icon/text based on state (planned)
- [ ] Smart toggle button disabled when no items (planned - Select All with empty list)

### Error Handling

#### ErrorHandler Utility
- [ ] getErrorMessage() returns user-friendly message for UnknownHostException (planned)
- [ ] getErrorMessage() returns user-friendly message for SocketTimeoutException (planned)
- [ ] getErrorMessage() returns user-friendly message for IOException (planned)
- [ ] getErrorMessage() returns user-friendly message for IllegalArgumentException (planned)
- [ ] getErrorMessage() returns user-friendly message for IllegalStateException (planned)
- [ ] getErrorMessage() returns generic message for unknown exceptions (planned)
- [ ] handleResult() calls onSuccess callback for successful Result (planned)
- [ ] handleResult() calls onError callback for failed Result (planned)
- [ ] handleResult() uses custom error message when provided (planned)
- [ ] executeSafely() returns success Result when operation succeeds (planned)
- [ ] executeSafely() returns failure Result when operation throws exception (planned)
- [ ] executeWithRetry() succeeds on first attempt when operation succeeds (planned)
- [ ] executeWithRetry() retries on failure and succeeds on retry (planned)
- [ ] executeWithRetry() exhausts retries and returns final failure (planned)

#### UI Error Display
- [ ] SnackbarHost displays error message from ViewModel state (planned - UI test)
- [ ] BackHandler validates data before navigating back (planned - UI test)
- [ ] BackHandler shows error via Snackbar and prevents navigation when validation fails (planned - UI test)
- [ ] Import screens show network errors via Snackbar (planned - UI test)
- [ ] Import screens show parsing errors via Snackbar (planned - UI test)

---


### FilterSortGroup Library
- [ ] FilterSortGroupManager combines search, filters, sort, groupBy correctly (planned)
- [ ] FilterSortGroupManager AND logic for multiple filters (planned)
- [ ] FilterSortGroupManager filteredItems updates when sourceItems changes (planned)
- [ ] FilterSortGroupManager groupedItems groups correctly with custom ordering (planned)
- [ ] Recipe filters (FavoriteFilter, TagFilter, CookTimeFilter, etc.) match correctly (planned)
- [ ] Meal plan ContainsRecipeFilter finds plans with specific recipe by name or ID (planned)
- [ ] Sort direction toggle reverses order correctly (planned)
- [ ] GroupBy compareKeys custom ordering works (planned)
