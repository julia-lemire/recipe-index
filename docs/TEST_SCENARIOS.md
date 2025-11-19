# Recipe Index Test Scenarios

> **Purpose**: Test coverage and scenarios to implement for automated testing
> **Last Updated**: 2025-11-18

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
- **Unit Tests**: 38 tests in 2 files (~8% file coverage, untested: RecipeManager, all parsers, DAOs)
  - ✅ GroceryListManagerTest.kt (22 tests)
  - ✅ MealPlanManagerTest.kt (16 tests)
- **Integration Tests**: 0 (planned: DAO tests, database migrations)
- **UI Tests**: 0 (planned: screen interactions, navigation flows)
- **Total Scenarios**: 38 implemented, 50+ planned

### Coverage Gaps (Priority Order)
1. **CRITICAL**: TextRecipeParser (PDF/Photo import depends on this)
2. **CRITICAL**: RecipeManager (core CRUD operations)
3. **HIGH**: SchemaOrgRecipeParser (URL import)
4. **HIGH**: Database migrations (v1→v2→v3)
5. **MEDIUM**: DAOs (RecipeDao, MealPlanDao, GroceryListDao, GroceryItemDao)
6. **MEDIUM**: PdfRecipeParser, PhotoRecipeParser
7. **LOW**: ViewModels (mostly delegation, integration tests preferred)
8. **LOW**: UI screens (manual testing acceptable for MVP)

### Next Testing Priorities
1. RecipeManager (create, update, delete, search, favorite toggle)
2. TextRecipeParser (section detection, ingredient/instruction validation, noise filtering)
3. SchemaOrgRecipeParser (Schema.org JSON-LD parsing, ISO 8601 durations, fallback handling)
4. Database migration tests (Room v1→v2→v3 with data preservation)

---

## Test Scenarios by Feature

### Recipe Management
- [ ] Create recipe with all required fields (planned)
- [ ] Update recipe preserves database ID (planned)
- [ ] Delete recipe removes from database (planned)
- [ ] Search recipes by title/ingredients (planned)
- [ ] Scale recipe portions updates ingredient quantities (planned)

### Recipe Import
- [ ] URL import extracts recipe from valid webpage (planned)
- [ ] PDF import parses structured recipe document (planned)
- [ ] Photo OCR import extracts text from image (planned)
- [ ] Import validates required fields before saving (planned)
- [ ] Import handles malformed/missing data gracefully (planned)

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
- [ ] Meal plan persists across app restarts (planned - integration test needed)

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
- [ ] Search lists by name (planned)
- [ ] getItemCount returns correct count (planned - DAO integration test)
- [ ] getCheckedCount returns correct checked count (planned - DAO integration test)

### Database
- [ ] Room database migration v1→v2 adds MealPlan table (planned)
- [ ] Room database migration v2→v3 adds GroceryList and GroceryItem tables (planned)
- [ ] MealPlan foreign key to Recipe enforces referential integrity (planned)
- [ ] GroceryItem foreign key to GroceryList enforces referential integrity (planned)
- [ ] Cascade delete: deleting GroceryList deletes all GroceryItems (planned)
- [ ] TypeConverters correctly serialize/deserialize List<Long> (planned)
- [ ] TypeConverters correctly serialize/deserialize List<String> (planned)

---
