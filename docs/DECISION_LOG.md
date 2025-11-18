# Recipe Index Decision Log

> **Purpose**: Architectural decision records (WHAT/WHY/WHEN decisions were made)
> **Last Updated**: 2025-11-18

**See Also:**
- [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) - Quick lookup ("I need to...") and architecture patterns (HOW to implement)
- [FILE_CATALOG.md](./FILE_CATALOG.md) - Complete file tree, system relationships, and component descriptions
- [PROJECT_STATUS.md](./PROJECT_STATUS.md) - Current status, core principles, completed features, and backlog
- [TEST_SCENARIOS.md](./TEST_SCENARIOS.md) - Test coverage and scenarios to implement for automated testing
- [PROJECT_CONVENTIONS.md](../PROJECT_CONVENTIONS.md) - How to maintain documentation

**Quick Navigation:** [How to Update](#how-to-update-this-file) | [Decision Records](#architectural-decision-records)

---

## How to Update This File

### When you make an architectural decision:
1. **Add new ADR** at top of [Architectural Decision Records](#architectural-decision-records) (reverse chronological)
2. **Format:** `#### MMM DD, YYYY: [Title]` with Decision/Rationale/Implementation bullets (1 sentence each)
3. **Update PROJECT_STATUS.md** §3 Core Principles if new pattern introduced
4. **Add to DEVELOPER_GUIDE.md** if decision establishes reusable HOW-TO pattern

### What goes in an ADR:
- ✅ Architectural choices (Manager pattern, StateFlow vs LiveData, navigation approach)
- ✅ Design system decisions (Hearth theme, color palette)
- ✅ Technology selections (Room, Compose, no cloud sync)
- ✅ Structural patterns (Config Over Code, Unified Entities)

### What does NOT go here:
- ❌ Feature implementations (those go in PROJECT_STATUS.md §4 Completed Features)
- ❌ Bug fixes (unless architecturally significant)
- ❌ Implementation details (those go in code comments)
- ❌ File creation logs (those go in FILE_CATALOG.md)

### Format Guidelines:
- Keep each bullet to 1 sentence maximum
- Date format: `MMM DD, YYYY` (e.g., Nov 18, 2025)
- Newest entries first

---

## Architectural Decision Records

> **Organization**: Newest entries first (reverse chronological order)
> **Keep it concise**: 1 sentence per field (Decision/Rationale/Implementation)

#### Nov 18, 2025: Multiple Photo Support for Recipe Import
- **Decision**: PhotoRecipeParser supports multiple photos via parseMultiple(List<Uri>) that combines OCR text from all images before parsing
- **Rationale**: Recipes often span multiple photos (ingredient lists, instruction steps); combining text improves parsing accuracy and UX vs forcing single photo
- **Implementation**: ImportPhotoScreen uses GetMultipleContents activity result contract, displays photo preview grid with remove buttons, processes all photos together button

#### Nov 18, 2025: ML Kit Text Recognition for Photo Import
- **Decision**: Use Google ML Kit Text Recognition (16.0.1) for OCR to extract text from recipe photos and camera captures
- **Rationale**: ML Kit provides accurate on-device OCR with no server dependency (offline-first principle), official Google library with coroutines support via kotlinx-coroutines-play-services
- **Implementation**: PhotoRecipeParser uses TextRecognition.getClient() to process InputImage from URI, extracts text blocks, delegates to TextRecipeParser for recipe parsing

#### Nov 18, 2025: PdfBox-Android for PDF Text Extraction
- **Decision**: Use PdfBox-Android (2.0.27.0) for extracting text from PDF recipe files
- **Rationale**: PdfBox-Android is the stable Android port of Apache PDFBox, supports offline text extraction without external dependencies, widely used and maintained
- **Implementation**: PdfRecipeParser loads PDDocument from ContentResolver URI, uses PDFTextStripper to extract all text, delegates to TextRecipeParser for recipe parsing

#### Nov 18, 2025: TextRecipeParser with Smart Pattern Matching
- **Decision**: Create TextRecipeParser with regex pattern matching to parse unstructured recipe text from PDFs and photos (detects sections, extracts times/servings, cleans formatting)
- **Rationale**: Mobile UX requires smart parsing since selecting/copying text is difficult; pattern matching handles various recipe formats without manual organization
- **Implementation**: detectSections() finds ingredients/instructions headers via regex, parseTimeString() converts "1h 30min" to minutes, cleanIngredient()/cleanInstruction() remove bullets/numbering

#### Nov 18, 2025: Tabbed Instruction Sections for Multi-Method Recipes
- **Decision**: Parse instruction sections (detected by lines ending with ":") and display as tabs within instructions card when multiple sections exist
- **Rationale**: Recipes with multiple cooking methods (Slow Cooker, Instant Pot, etc.) need clear separation; users select one method and tabs prevent scrolling through irrelevant instructions
- **Implementation**: RecipeDetailScreen parseInstructionSections() detects section headers, InstructionsSection composable shows TabRow for multi-section recipes, simple list for single-section

#### Nov 18, 2025: Coil for Async Image Loading
- **Decision**: Use Coil Compose library (2.7.0) for loading recipe photos from URLs
- **Rationale**: Coil is the recommended image loading library for Jetpack Compose with native coroutines support, efficient caching, and composable-first API
- **Implementation**: AsyncImage composable in RecipeDetailScreen (240dp) and RecipeListScreen cards (180dp) with ContentScale.Crop

#### Nov 18, 2025: Recipe Photo Import and Display
- **Decision**: Extract main recipe photo from Schema.org image field, save URL to Recipe.photoPath, display in list cards and detail screen
- **Rationale**: Visual recipe identification improves browsing UX; Schema.org provides standardized image field; storing URL avoids local storage complexity
- **Implementation**: SchemaOrgRecipeParser parseImage() extracts URL from string/object/array, toRecipe() saves to photoPath, AsyncImage displays in UI

#### Nov 18, 2025: Comma-Separated Tag Parsing
- **Decision**: Split Schema.org keywords field by comma when it's a single string instead of array
- **Rationale**: Schema.org allows keywords as either array ["tag1", "tag2"] or comma-separated string "tag1, tag2"; must handle both formats to parse all sites correctly
- **Implementation**: parseJsonArrayToStrings() splits JsonPrimitive content by comma, trims whitespace, filters blanks

#### Nov 18, 2025: Expandable FAB Menu for Create and Import
- **Decision**: Expandable FAB on RecipeListScreen with "Create" and "Import" options, main FAB rotates 45° when expanded
- **Rationale**: Provides two primary actions without cluttering UI; rotating + icon communicates expandability and doubles as close affordance
- **Implementation**: RecipeListScreen uses Column with SmallFloatingActionButton for each option, fabExpanded state controls visibility

#### Nov 18, 2025: Schema.org JSON-LD for Recipe Parsing
- **Decision**: Parse Schema.org Recipe JSON-LD markup with Jsoup, support multiple formats (@type, @graph, arrays), fall back to Open Graph meta tags
- **Rationale**: Most modern recipe sites use Schema.org markup for SEO; standardized format reduces parsing complexity vs HTML scraping
- **Implementation**: SchemaOrgRecipeParser extracts JSON-LD scripts, parses with kotlinx-serialization, converts ISO 8601 durations to minutes

#### Nov 18, 2025: RecipeParser Interface for Extensible Import
- **Decision**: RecipeParser interface with parse(source: String): Result<Recipe> method; implementations for URL/PDF/Photo
- **Rationale**: Enables adding new import sources without modifying existing code; single interface for ViewModels to depend on
- **Implementation**: RecipeParser interface in data/parsers/, SchemaOrgRecipeParser first implementation, ViewModelFactory injects parser dependency

#### Nov 18, 2025: Flow Loading State Inside Collect Block
- **Decision**: Set isLoading=false INSIDE Flow.collect() block after first emission, not in finally block
- **Rationale**: Flow.collect() never completes (keeps listening for DB updates), so finally block never executes; caused perpetual loading spinner
- **Implementation**: RecipeViewModel sets _isLoading.value=false inside collect block and in catch block; pattern added to DEVELOPER_GUIDE.md

#### Nov 18, 2025: Auto-Save on Back Navigation
- **Decision**: Auto-save form data when navigating back instead of manual save button; skip save if form is empty, validate before saving
- **Rationale**: Reduces friction by eliminating manual save step while preventing data loss; aligns with modern mobile UX patterns
- **Implementation**: AddEditRecipeScreen handleBack() checks for content, validates, auto-saves if valid; shows errors and blocks navigation if invalid

#### Nov 18, 2025: MainActivity as Orchestrator Pattern
- **Decision**: MainActivity only sets up dependencies and wires components - navigation logic in Navigation.kt, business logic in Managers
- **Rationale**: Keeps MainActivity clean and focused, easier to test navigation independently, follows single responsibility principle
- **Implementation**: MainActivity creates AppDatabase/RecipeManager/ViewModelFactory, calls Navigation.kt composable; AppNavigationDrawer accepts content parameter

#### Nov 18, 2025: Responsive Navigation Drawer
- **Decision**: Modal drawer for phones (Samsung S23 Ultra), permanent drawer for tablets (Samsung Galaxy Tab S10+) using WindowSizeClass
- **Rationale**: Tablet's larger screen benefits from always-visible navigation, phone needs max content space with drawer overlay
- **Implementation**: AppNavigationDrawer checks WindowWidthSizeClass.Expanded for drawer type, NavGraph sealed class with routes/icons

#### Nov 18, 2025: Never Use BottomNavigation
- **Decision**: TopAppBar or NavigationDrawer only - BottomNavigation prohibited
- **Rationale**: Screen space critical for recipe content (ingredient lists, instructions, images)
- **Implementation**: Navigation rule added to ../ANDROID_DESIGN_PRINCIPLES.md with code examples

#### Nov 18, 2025: Offline-First Architecture with Local Storage Only
- **Decision**: Room database, no cloud sync, Samsung Quick Share for recipe sharing
- **Rationale**: User wants recipes available without internet, simplified privacy model
- **Implementation**: Room database for all entities, Quick Share integration for export/import

#### Nov 18, 2025: Manager Pattern for Complex Business Logic
- **Decision**: Business logic in `data/ContentManagers/`, ViewModels delegate to Managers
- **Rationale**: Keeps ViewModels thin (UI state only), enables reusable business logic, testable
- **Implementation**: RecipeManager, MealPlanManager handle all CRUD and business rules

#### Nov 18, 2025: StateFlow Over LiveData
- **Decision**: All observable state uses StateFlow, never LiveData
- **Rationale**: Modern coroutines-first approach, better Compose integration with `collectAsState()`
- **Implementation**: ViewModels expose `StateFlow<State>`, Settings classes use `MutableStateFlow`

#### Nov 18, 2025: Extract Components Proactively
- **Decision**: Extract when >50 lines, self-contained, or might be reused (not just 2+ uses)
- **Rationale**: Keeps files small from start, reduces need for future refactoring
- **Implementation**: Component extraction rule in ../ANDROID_DESIGN_PRINCIPLES.md

#### Nov 18, 2025: Hearth Design System
- **Decision**: Terracotta/clay tones (#E8997A, #2C1810), dark cards for browsing, light for cooking
- **Rationale**: Warm, home-kitchen aesthetic differentiates from clinical recipe apps
- **Implementation**: DESIGN_SYSTEM.md with complete Material 3 theme specification

#### Nov 18, 2025: Config Over Code Pattern
- **Decision**: User preferences in Settings classes with StateFlow, not hardcoded values
- **Rationale**: Enables runtime configuration changes, better testability, user customization
- **Implementation**: AppSettings class for global prefs, category-specific Settings where needed

---
