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

> **Status**: Planning phase - no tests implemented yet

### Current Coverage
- **Unit Tests**: 0/0 (0%)
- **Integration Tests**: 0/0 (0%)
- **UI Tests**: 0/0 (0%)
- **Total Scenarios**: 0 implemented, 35+ planned

### Priority Areas
1. Grocery List Ingredient Consolidation (quantity parsing, modifier removal, unit matching)
2. Meal Planning (auto-tag aggregation, flexible date ranges)
3. Recipe Management (CRUD operations, import validation)
4. Database (migrations v1→v2→v3, entity relationships)

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
- [ ] Create meal plan with flexible date range (Sun-Thu, single day) (planned)
- [ ] Create meal plan with no dates (indefinite/special event) (planned)
- [ ] Add multiple recipes to meal plan (planned)
- [ ] Auto-aggregate ingredient tags from recipes (planned)
- [ ] Auto-aggregate special event tags from recipes (planned)
- [ ] Detect special event from plan name ("Thanksgiving Dinner" → "Thanksgiving" tag) (planned)
- [ ] Duplicate meal plan creates copy with "(Copy)" suffix (planned)
- [ ] Delete meal plan preserves associated recipes (planned)
- [ ] Search meal plans by name (planned)
- [ ] RecipePickerBottomSheet filters recipes by search query (planned)
- [ ] Meal plan persists across app restarts (planned)

### Grocery Lists - Ingredient Consolidation
- [ ] Parse ingredient with quantity and unit (e.g., "2 lbs chicken breast") (planned)
- [ ] Parse ingredient with fraction (e.g., "1/2 cup flour") (planned)
- [ ] Parse ingredient with mixed number (e.g., "1 1/2 cups sugar") (planned)
- [ ] Remove "diced" modifier when consolidating (planned)
- [ ] Remove "chopped" modifier when consolidating (planned)
- [ ] Remove "shredded" modifier when consolidating (planned)
- [ ] Remove "sliced" modifier when consolidating (planned)
- [ ] Keep "minced" separate from whole (different prep) (planned)
- [ ] Consolidate matching name+unit pairs sums quantities (planned)
- [ ] Different units remain separate (lbs vs cups) (planned)
- [ ] Missing quantity creates item without quantity (planned)
- [ ] Missing unit creates item with quantity only (planned)
- [ ] Merge source recipe IDs when consolidating duplicates (planned)

### Grocery Lists - List Management
- [ ] Create grocery list with name (planned)
- [ ] Add recipes to existing list consolidates with existing items (planned)
- [ ] Add meal plan to list extracts all recipe ingredients (planned)
- [ ] Add manual item via text field (planned)
- [ ] Toggle item checked status (planned)
- [ ] Clear checked items removes only checked (planned)
- [ ] Item detail shows all source recipes (planned)
- [ ] Update item quantity/unit/notes (planned)
- [ ] Delete item removes from list (planned)
- [ ] Delete list removes all items (cascade) (planned)
- [ ] Search lists by name (planned)
- [ ] getItemCount returns correct count (planned)
- [ ] getCheckedCount returns correct checked count (planned)

### Database
- [ ] Room database migration v1→v2 adds MealPlan table (planned)
- [ ] Room database migration v2→v3 adds GroceryList and GroceryItem tables (planned)
- [ ] MealPlan foreign key to Recipe enforces referential integrity (planned)
- [ ] GroceryItem foreign key to GroceryList enforces referential integrity (planned)
- [ ] Cascade delete: deleting GroceryList deletes all GroceryItems (planned)
- [ ] TypeConverters correctly serialize/deserialize List<Long> (planned)
- [ ] TypeConverters correctly serialize/deserialize List<String> (planned)

---
