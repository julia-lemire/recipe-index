# Recipe Index Test Scenarios

> **Purpose**: Test coverage and scenarios to implement for automated testing
> **Last Updated**: 2025-11-18

**See Also:**
- [DECISION_LOG.md](./DECISION_LOG.md) - Architectural decision records (WHAT/WHY/WHEN decisions were made)
- [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) - Quick lookup ("I need to...") and architecture patterns (HOW to implement)
- [FILE_CATALOG.md](./FILE_CATALOG.md) - Complete file tree, system relationships, and component descriptions
- [PROJECT_STATUS.md](./PROJECT_STATUS.md) - Current status, core principles, completed features, and backlog

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
- **Total Scenarios**: 0 implemented, 15 planned

### Priority Areas
1. Recipe Management (CRUD operations, import validation)
2. Meal Planning (weekly planning, recipe assignment)
3. Grocery Lists (ingredient extraction, aggregation)
4. Database (migrations, entity relationships)

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
- [ ] Create weekly meal plan with 7 days (planned)
- [ ] Assign recipe to specific meal slot (planned)
- [ ] Remove recipe from meal plan preserves recipe (planned)
- [ ] Meal plan persists across app restarts (planned)

### Grocery Lists
- [ ] Generate grocery list from meal plan aggregates ingredients (planned)
- [ ] Duplicate ingredients combine quantities (planned)
- [ ] Unit conversion handles metric/imperial (planned)

### Database
- [ ] Room database migration from v1 to v2 preserves data (planned)
- [ ] Entity relationships maintain referential integrity (planned)

---
