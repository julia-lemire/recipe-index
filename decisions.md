# Decision Log

> Running record of architectural and implementation decisions with rationale.
> Newest entries first. Keep entries concise (1-2 sentences per field).
> Maintained by the agent as part of normal task workflow.

---

<!-- FORMAT REFERENCE
#### MMM DD, YYYY: [Decision Title]
- **Decision**: What was decided (1 sentence)
- **Rationale**: Why (1 sentence)
- **Consequences**: What this means going forward (1 sentence)
-->

#### Apr 24, 2026: Top tab bar for primary navigation
- **Decision**: Replaced hamburger-only navigation with a `TabRow` (RecipeListScreen, MealPlanningScreen, GroceryListScreen) while keeping the drawer for secondary pages (Home, Search, SubstitutionGuide, Settings).
- **Rationale**: Three primary screens were too many clicks away via the drawer; top tabs match Material 3 patterns and user preference.
- **Consequences**: All three primary screens expose `onTabSelect: (Screen) -> Unit`; `NavGraph.drawerScreens` reduced to 4 items.

#### Apr 24, 2026: Date-first meal plan creation flow
- **Decision**: `CreateMealPlanDialog` shows a date range picker as step 1, auto-names the plan from the date range, and lets the user edit the name in step 2.
- **Rationale**: Users were forced to invent a name before seeing dates, making naming feel arbitrary; date range makes naming obvious.
- **Consequences**: `MealPlanPickerDialog.onCreateNew` signature widened to `(name, startDate, endDate)`; all call sites in Navigation.kt and RecipeListScreen.kt updated.

#### Apr 24, 2026: Default "Weekly Groceries" grocery list
- **Decision**: `GroceryListViewModel.init` calls `ensureDefaultList()` which creates "Weekly Groceries" on first launch if no list by that name exists.
- **Rationale**: Users should not have to create a list before adding groceries; one permanent default list covers most use cases.
- **Consequences**: List creation is idempotent — runs once at startup and is a no-op thereafter.

#### Apr 24, 2026: Shared DateFormatting and ImportSourceCard
- **Decision**: Extracted `DateFormatting` object and `ImportSourceCard` composable into shared files, removing 6+ duplicate private implementations.
- **Rationale**: Identical date-formatting and card-rendering code was copy-pasted into every file that needed it.
- **Consequences**: `DateFormatting.kt` in `utils/`, `ImportSourceCard.kt` in `ui/components/`; all callers updated.

#### Apr 24, 2026: Grocery list ingredient cleaning pipeline
- **Decision**: `GroceryListManager.parseIngredient` runs a 6-step pipeline: unit conversion, strip parenthetical prep notes, strip comma clauses, remove prep/size modifiers, validate unit tokens, round fractional count-only quantities up to 1.
- **Rationale**: Grocery items showed raw prep notes ("cut into strips") and fractional quantities ("1/2 onion"); unit conversion settings were applied in the recipe viewer but ignored during grocery list creation.
- **Consequences**: `GroceryListManager` requires `SettingsManager` as a 6th constructor arg; `MainActivity` init order adjusted to declare `settingsManager` first.
