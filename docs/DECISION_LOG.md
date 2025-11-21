# Recipe Index Decision Log

> **Purpose**: Architectural decision records (WHAT/WHY/WHEN decisions were made)
> **Last Updated**: 2025-11-21

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

#### Nov 21, 2025: Import Navigation Fix for Home Screen Entry Point
- **Decision**: Changed all import screen navigation from `popBackStack(RecipeIndex)` to `navigate(RecipeIndex) { popUpTo(Home) }` when saving imported recipes
- **Rationale**: popBackStack() failed silently when user entered import flow from Home screen because RecipeIndex wasn't in the back stack; users would see the import screen again instead of navigating to recipe list
- **Implementation**: Navigation.kt updated ImportUrlScreen, ImportPdfScreen, ImportPhotoScreen onSaveComplete callbacks to use navigate() with popUpTo(Home, inclusive=false), ensures consistent navigation to recipe list regardless of entry point

#### Nov 21, 2025: PDF Parsing Improvements for Long Webpage Prints
- **Decision**: Enhanced TextRecipeParser to handle noisy PDFs from webpage prints by requiring standalone ingredients headers, skipping breadcrumb lines, and filtering more title noise patterns
- **Rationale**: Long webpage PDFs contain breadcrumb navigation (e.g., "Skinnytaste > Main Ingredient > Ground Turkey") that triggered false positive ingredients detection, plus marketing/CTA text that was incorrectly extracted as titles
- **Implementation**: Changed ingredients section detection from word-boundary match to standalone header regex `^ingredients?\s*:?\s*$`, added isBreadcrumb check for lines containing " > ", enhanced isValidTitle() with filters for subscribe/newsletter, "more X recipes", and CTA patterns (jump to, print, save, share, pin, rate, email)

#### Nov 21, 2025: ServingSize Extraction for Unitless Fractions
- **Decision**: Added pattern to extractServingSize() for fractions without units (e.g., "Serving Size: 1/4")
- **Rationale**: Recipe websites often express serving size as recipe fraction ("1/4 of recipe") without explicit units, OCR results may include multiplier buttons ("1x 2x 3x") that need filtering
- **Implementation**: New regex pattern captures standalone fractions or Unicode fractions not followed by unit words, terminated by whitespace/end/multiplier pattern, supplements existing unit-based patterns

#### Nov 21, 2025: Simplified Photo Picker in Recipe Detail
- **Decision**: Replaced camera/gallery selection dialog with single file picker using ActivityResultContracts.GetContent()
- **Rationale**: User requested simpler interface; file picker provides access to both camera captures and gallery images through system UI without requiring custom dialog
- **Implementation**: RecipeDetailScreen uses single GetContent("image/*") launcher, directly launches on add photo button click, removes AlertDialog and separate camera/gallery launchers

#### Nov 21, 2025: Text Labels Replacing Emoji Icons
- **Decision**: Replaced emoji icons (🍽️, ⏱️, 🔥, ⏰, 📏) with text labels (Servings, Prep:, Cook:, Total:, Portion:) in RecipeDetailScreen info row
- **Rationale**: User feedback that emojis are not clear; text labels provide better accessibility and clarity
- **Implementation**: Updated info row in RecipeDetailScreen to use descriptive text labels instead of emoji characters

#### Nov 21, 2025: Photo Management in Recipe Detail Screen
- **Decision**: Added inline photo management to RecipeDetailScreen allowing users to add photos via file picker directly from the recipe view
- **Rationale**: Users need to add photos to recipes after import (capturing completed dish, adding step photos) without navigating to edit screen; missing photos placeholder encourages photo addition
- **Implementation**: File picker launcher using ActivityResultContracts.GetContent("image/*"), savePhotoToStorage() compresses and saves to media/images/, "Add Photo" placeholder card when no photos, IconButton overlay on photo carousel when photos exist, updates recipe.mediaPaths via recipeViewModel.updateRecipe()

#### Nov 21, 2025: OCR Noise Cleaning for Photo Import
- **Decision**: Added pre-cleaning of OCR artifacts in TextRecipeParser's looksLikeIngredient() and cleanIngredient() functions to handle common OCR errors
- **Rationale**: Photo OCR often misreads checkbox icons as "U" or "O" characters and confuses "oz" with "0z" (zero instead of letter), causing valid ingredients like "16 oz radishes" to be filtered out
- **Implementation**: cleanIngredient() removes leading U/O/☐/□ followed by space, replaces word-boundary "0z" with "oz" and "0unces" with "ounces", looksLikeIngredient() pre-cleans line before pattern matching to ensure "U 16 0z radishes" matches quantity patterns

#### Nov 21, 2025: Breadcrumb Navigation Filtering in Title Extraction
- **Decision**: Added breadcrumb pattern detection (" > ") to extractTitle()'s isValidTitle() filter in TextRecipeParser
- **Rationale**: Photo OCR of recipe websites captures breadcrumb navigation (e.g., "Skinnytaste > Recipes > Air Fryer Recipes > Air Fryer Radishes") as first line, incorrectly using it as recipe title instead of the actual title
- **Implementation**: isValidTitle() now returns false for lines containing " > ", allowing extraction to skip breadcrumb lines and find the actual recipe title

#### Nov 21, 2025: OCR Fraction Handling in Serving Size Extraction
- **Decision**: Enhanced extractServingSizeFromLine() to handle OCR-style fractions with spaces (e.g., "1 /2") and normalize them to Unicode fractions
- **Rationale**: OCR often splits fractions with spaces ("1 /2 cup" instead of "½ cup"), causing serving size patterns to fail matching; normalizing improves extraction accuracy
- **Implementation**: Added "/" to pattern character class, post-processing removes spaces around slashes ("1 /2" → "1/2") then converts ASCII fractions to Unicode (1/2→½, 1/4→¼, 3/4→¾, 1/3→⅓, 2/3→⅔)

#### Nov 21, 2025: Serving Size / Portion Size Field
- **Decision**: Added servingSize field to Recipe entity for storing portion/serving size information (e.g., "1 ½ cups", "200g") separate from the servings count
- **Rationale**: Users need to know portion size per serving for dietary tracking and meal planning; extracting from PDF patterns like "Serving Size: 1 ½ cups" preserves valuable nutritional context
- **Implementation**: Added servingSize: String? to Recipe entity and ParsedRecipeData, TextRecipeParser extracts via extractServingSize() with patterns for "Serving Size:", "Portion:", "Per Serving:", displayed in RecipeDetailScreen info row with serving indicator, editable in ImportPdfScreen/ImportUrlScreen/AddEditRecipeScreen, database version bumped to 9

#### Nov 21, 2025: PDF Line Continuation Joining for Instructions and Tips
- **Decision**: Added line joining logic to reassemble content that PDF extraction splits across multiple lines, plus filtering for PDF page noise (URLs, page headers)
- **Rationale**: PDF text extraction splits long sentences into separate lines; instructions like "Transfer to pan, cook 35 minutes, turning" + "everything halfway" were being treated as separate steps instead of one complete instruction
- **Implementation**: Added isPdfPageNoise() to filter URLs/page headers/page numbers, joinInstructionLines() joins lines where non-digit-starting lines continue previous step, joinParagraphLines() joins prose content for tips/notes, both extractInstructions() and extractTips() now filter PDF noise and join continuation lines before processing

#### Nov 21, 2025: Source Tips Field for Website Notes and Substitutions
- **Decision**: Added sourceTips field to Recipe entity for storing tips, variations, and substitutions from source websites/PDFs, separate from user-added notes
- **Rationale**: Website recipes often include valuable tips and substitution suggestions that should be preserved during import but kept distinct from user's personal notes
- **Implementation**: Added sourceTips: String? to Recipe entity and ParsedRecipeData, TextRecipeParser detects Notes/Tips/Variations/Substitutions sections and extracts content with isMarketingNoise() filter, displayed as "Tips & Substitutions" in RecipeDetailScreen (visible in cook mode), editable in ImportUrlScreen/ImportPdfScreen/AddEditRecipeScreen, user notes renamed to "My Notes"

#### Nov 21, 2025: PDF Multi-Column Recovery for Misplaced Ingredients
- **Decision**: Added comprehensive ingredient recovery for PDF multi-column layouts: PdfRecipeParser uses sortByPosition=true, TextRecipeParser re-extracts ALL lines (skipping instruction filter) when ingredients section is empty, recovery function uses pattern matching to separate ingredients from instructions
- **Rationale**: PDF text extraction from multi-column layouts places sidebar UI text between section headers and actual content, causing ingredients to appear after "Instructions" header; the looksLikeInstruction() filter was removing ingredient lines before recovery could see them
- **Implementation**: PdfRecipeParser.sortByPosition=true for better column ordering, extractInstructions(skipInstructionFilter=true) parameter to bypass filter during recovery, enhanced ingredientPatterns in recoverMisplacedIngredients() to match "4 boneless skinless chicken thighs" (number + food word), "4 (12 oz total)" (number + parenthetical), "1 red bell pepper" (number + descriptor + food), plus isWebsiteNoise() plurals and rating prompts, verbose debug logging for each line's classification

#### Nov 21, 2025: Unit Conversion Toggle Button Restoration
- **Decision**: Restored SwapHoriz toggle button in RecipeDetailScreen ingredients section header that was previously removed, allowing users to toggle between showing both units inline or using their settings preference
- **Rationale**: Users need ability to quickly switch between unit systems while viewing recipes without navigating to settings, especially when cooking with unfamiliar measurements
- **Implementation**: Added showUnitConversions state variable (remember{false}), IconButton with SwapHoriz icon in ingredients header Row, when enabled passes UnitSystem.BOTH to IngredientUnitConverter.formatIngredient() forcing "1 cup (237 ml)" format, when disabled uses user's granular settings (liquidVolumePreference, weightPreference), button highlights with secondaryContainer color when active

#### Nov 21, 2025: Quick Note Button in Cook Mode
- **Decision**: Added "Quick Note" button to cook mode card that opens dialog with text field for adding/editing recipe notes without leaving cook mode
- **Rationale**: Users want to capture cooking observations (adjustments, timing notes, results) immediately during cooking without disrupting workflow by navigating to edit screen
- **Implementation**: Added showQuickNoteDialog state and quickNoteText pre-populated with recipe.notes, FilledTonalButton with Note icon in cook mode card after timer section, AlertDialog with OutlinedTextField (200dp height, 10 maxLines), Save button calls recipeViewModel.updateRecipe(recipe.copy(notes = quickNoteText.ifBlank { null }))

#### Nov 21, 2025: Hide Tags in Cook Mode
- **Decision**: Added !cookModeEnabled condition to tags section in RecipeDetailScreen to hide tags when cook mode is active
- **Rationale**: Tags provide no value during cooking and add visual clutter, users need to focus on ingredients and instructions while cooking
- **Implementation**: Modified tags display condition from `if (recipe.tags.isNotEmpty())` to `if (recipe.tags.isNotEmpty() && !cookModeEnabled)` in RecipeDetailScreen.kt line 631

#### Nov 21, 2025: Simplified Grocery List Quantity Display
- **Decision**: Rewrote GroceryListDetailScreen.formatQuantity() to use common fractions (1/4, 1/2, 3/4) for small quantities and max 1 decimal place for larger quantities
- **Rationale**: Overly precise decimals like "0.20 tsp" are harder to measure and less readable than fractions, grocery shopping benefits from practical measurements over precision
- **Implementation**: Added fraction mapping for quantities < 1.0 (>0.9→"1", >0.625→"3/4", >0.4→"1/2", >0.25→"1/3", else→"1/4"), changed decimal formatting from %.2f to %.1f, rounds very small quantities (< 0.25) to "1/4"

#### Nov 21, 2025: Dry Volume Unit Preference Addition
- **Decision**: Added separate dryVolumePreference setting in AppSettings/SettingsManager/SettingsScreen for ingredients like flour, sugar, and spices, distinct from liquidVolumePreference
- **Rationale**: Baking benefits from weight measurements (grams) for dry ingredients while cooking often uses volume (cups) for liquids, forcing single preference causes compromise
- **Implementation**: Added dryVolumePreference field to AppSettings (between liquidVolumePreference and weightPreference), SettingsManager.setDryVolumePreference() method with SharedPreferences persistence, "Dry Volume Units" section in SettingsScreen UI with radio buttons for IMPERIAL/METRIC/BOTH, follows same pattern as existing granular preferences

#### Nov 21, 2025: Recipe Detail Image Carousel with HorizontalPager
- **Decision**: Replaced single image display in RecipeDetailScreen with HorizontalPager carousel showing all images from mediaPaths, with page indicator dots
- **Rationale**: Users could only see first imported image even when they selected multiple during import, wasting selected media and providing incomplete recipe context
- **Implementation**: Added HorizontalPager with rememberPagerState for swipeable image gallery, page indicator dots (CircleShape Box) shown at bottom when 2+ images exist, filters mediaPaths for MediaType.IMAGE, falls back to photoPath for legacy recipes, indicator uses primary color for active page and 30% alpha for inactive

#### Nov 21, 2025: Cook Mode UI Reorganization
- **Decision**: Moved Select All/Deselect All toggle buttons from Cook Mode card header to individual section headers (Ingredients and Instructions), with intelligent button text based on checked state
- **Rationale**: Button in Cook Mode card header looked like part of timer section causing confusion, and didn't work because state wasn't being updated
- **Implementation**: Removed button from Cook Mode title row, added toggle buttons to Ingredients and Instructions section headers (only visible in cook mode), button shows "Select All" when not all items checked or "Deselect All" when all checked, wired onClick to toggle all items using recipe.ingredients.indices.toSet() or instructionSteps.indices.toSet()

#### Nov 21, 2025: MediaPaths UI Adoption Across All Screens
- **Decision**: Updated all UI screens (RecipeCard, RecipeDetailScreen, MealPlanningScreen, HomeScreen) to display images from new mediaPaths field instead of deprecated photoPath
- **Rationale**: Images were being saved correctly to mediaPaths during import but UI was still reading from empty photoPath field, causing "images not appearing" bug despite successful storage
- **Implementation**: Changed all AsyncImage model parameters from recipe.photoPath to recipe.mediaPaths.firstOrNull { it.type == MediaType.IMAGE }?.path with fallback to photoPath for backward compatibility, updated share operations to use first image from mediaPaths

#### Nov 21, 2025: Home Screen Navigation Callback Wiring
- **Decision**: Wired all RecipeCard action callbacks (delete, favorite, add to grocery/meal plan, share) in HomeScreen by adding callback parameters and implementing them in Navigation.kt with proper ViewModel access
- **Rationale**: RecipeCard callbacks in home screen carousels were placeholder comments, making all card menu actions non-functional (delete, favorite, etc. did nothing)
- **Implementation**: Added onToggleFavorite, onDeleteRecipe, onAddToGroceryList, onAddToMealPlan, onShareRecipe parameters to HomeScreen, wired them in Navigation.kt with recipeViewModel.toggleFavorite(), recipeViewModel.deleteRecipe() + homeViewModel.refresh(), grocery/meal plan picker dialogs, ShareHelper.shareRecipe() with first mediaPaths image

#### Nov 21, 2025: Meal Plan Context Menu Visibility Fix
- **Decision**: Changed AddEditMealPlanScreen to always show overflow menu when onAddToGroceryList callback provided, with "Add to Grocery List" option disabled when no recipes selected
- **Rationale**: Menu was completely hidden unless plan was existing with recipes (mealPlan != null && selectedRecipeIds.isNotEmpty()), making "Add to Grocery List" feature undiscoverable
- **Implementation**: Simplified condition from (mealPlan != null && selectedRecipeIds.isNotEmpty() && onAddToGroceryList != null) to just (onAddToGroceryList != null), added enabled = selectedRecipeIds.isNotEmpty() to DropdownMenuItem for proper disabled state when no recipes

#### Nov 21, 2025: Home Screen Redesign with Recipe Highlights
- **Decision**: Redesigned home screen to be content-focused with recipe carousels (Recent, Favorites) and This Week's Meal Plan section, with Quick Actions at the top
- **Rationale**: Original home screen was empty placeholder with just navigation links, providing no value or engagement for users returning to the app
- **Implementation**: Created HomeViewModel to load recent recipes (last 5 by createdAt), favorite recipes (up to 5), and current week's meal plan (filtering by date range), completely rewrote HomeScreen.kt with horizontal scrolling LazyRow carousels for recipes, all navigation callbacks wired in Navigation.kt for seamless navigation to all sections

#### Nov 21, 2025: Tag Dialog State Preservation Bug Fix
- **Decision**: Fixed tag modification dialog appearing multiple times by preserving tagModifications and imageUrls fields in ImportViewModel.updateRecipe() using state.copy() instead of creating new UiState.Editing
- **Rationale**: Creating new state object changed reference, retriggering LaunchedEffect(uiState) observer that shows dialog, causing it to reappear each time user edited recipe (added tags, etc.)
- **Implementation**: Modified updateRecipe() to preserve existing fields via currentState.copy(recipe = recipe), added clearTagModifications() method to explicitly clear after user review, updated both onAccept and onDismiss callbacks to clear modifications preventing retrigger

#### Nov 21, 2025: Image Selection Save Flow Fix
- **Decision**: Removed premature onSaveComplete() call from Save button's onClick handler in ImportUrlScreen, allowing existing UiState.Saved observer to handle navigation after async save completes
- **Rationale**: Button was calling onSaveComplete() immediately after onSave(), navigating away before MediaDownloader could download selected images, resulting in images not being saved with recipe
- **Implementation**: Removed onSaveComplete() from line 888 button onClick, existing LaunchedEffect watching for UiState.Saved (lines 235-239) properly waits for ViewModel.saveRecipe() including image downloads to complete before navigation

#### Nov 21, 2025: Multiple Media Support with Selection UI
- **Decision**: Implemented comprehensive multi-media support (images/videos) with user-selectable download UI during URL import
- **Rationale**: Preserves recipe media beyond URL availability, supports instructional photos/videos, gives users control over storage usage by selecting relevant images
- **Implementation**: MediaItem data class with MediaType enum, MediaDownloader utility for downloading/compression, parser updates to extract all images via cascading supplementation, ImportUrlScreen selection grid with visual feedback, automatic download of selected media before saving

#### Nov 21, 2025: Cuisine Field UI Support
- **Decision**: Added UI support for cuisine field across recipe import, edit, display, filtering, and search functionality
- **Rationale**: The cuisine field was already in the Recipe model (populated during URL import) but had no UI exposure, preventing users from viewing, editing, or filtering by cuisine, resulting in lost data value
- **Implementation**: Added cuisine TextField to AddEditRecipeScreen (after time fields) and ImportUrlScreen metadata dialog (with Place icon in preview), displays cuisine as first tag (1/3) in recipe cards using buildList to prioritize cuisine before regular tags, shows cuisine in list view info row after time, created CuisineFilter in RecipeFilters.kt following TagFilter pattern, updated RecipeDao.searchRecipes() query to include "OR cuisine LIKE" clause for search functionality

#### Nov 21, 2025: Cascading Data Supplementation for URL Recipe Import
- **Decision**: Refactored UrlRecipeParser to use cascading supplementation strategy where each parser tier supplements missing data without overwriting data from previous tiers (Schema.org → HTML scraping → Open Graph)
- **Rationale**: Previous all-or-nothing approach (Schema.org OR HTML scraping OR Open Graph) meant partial data was lost when one parser failed completely, even when another parser could supplement missing fields (e.g., HTML scraping found 14 ingredients but no instructions → rejected → fell back to Open Graph with no recipe data)
- **Implementation**: Changed from if/else branching to cascading merge using copy() with elvis operators (title = recipeData.title ?: newData.title) and ifEmpty { } for lists, all three parsers attempt extraction regardless of previous results, data preference order: Schema.org > HTML scraping > Open Graph, enables saving partial recipes (ingredients without instructions) supplemented with metadata from other sources, comprehensive logging at each supplementation step

#### Nov 21, 2025: HTML Scraping Accepts Partial Recipe Data
- **Decision**: Changed HtmlScraper validation to accept ingredients OR instructions instead of requiring BOTH
- **Rationale**: Real-world recipe sites may have ingredients in structured lists but instructions in unconventional formats, rejecting partial data meant losing valuable ingredient lists that users could manually supplement
- **Implementation**: Changed HtmlScraper.scrape() validation from (ingredients.isNotEmpty() && instructions.isNotEmpty()) to (ingredients.isNotEmpty() || instructions.isNotEmpty()), updated logging to clarify "need at least one" instead of "need both", enables cascading supplementation to fill missing fields from other parsers

#### Nov 21, 2025: Controller Pattern for URL Recipe Import
- **Decision**: Refactored URL import to use controller pattern with UrlRecipeParser orchestrating specialized parsers (SchemaOrgRecipeParser, HtmlScraper, OpenGraphParser) instead of having all logic in SchemaOrgRecipeParser
- **Rationale**: SchemaOrgRecipeParser was handling multiple responsibilities (HTTP fetching, orchestration, Schema.org parsing, HTML scraping, Open Graph parsing) violating single responsibility principle and making code harder to test and maintain
- **Implementation**: Created UrlRecipeParser as controller that fetches HTML and delegates to specialized parsers in order, refactored SchemaOrgRecipeParser to pure Schema.org JSON-LD parser with parse(Document) signature removing HTTP client dependency, extracted OpenGraphParser for Open Graph meta tag extraction, moved toRecipe() extension to RecipeParser.kt for reuse, updated MainActivity to use UrlRecipeParser, follows same delegation pattern as PdfRecipeParser→TextRecipeParser

#### Nov 21, 2025: HTML Scraping Fallback for Recipe Import
- **Decision**: Added HTML scraping fallback layer using common CSS selector patterns to extract ingredients and instructions from HTML when structured data is unavailable
- **Rationale**: Many recipe sites lack proper Schema.org markup or use Article types without embedded recipe fields, causing imports to fall back to Open Graph (title + image only) and lose all ingredient/instruction data
- **Implementation**: Created HtmlScraper class that searches for ingredient/instruction lists using CSS selectors ([class*=ingredient] li, [id*=instruction] li, etc.), returns ParsedRecipeData if ingredients OR instructions found (saves whatever is available), integrated into cascading supplementation flow: Schema.org → HTML scraping → Open Graph, comprehensive logging for debugging selector matches

#### Nov 21, 2025: Article/BlogPosting Type Detection with Embedded Recipe Data
- **Decision**: Enhanced isRecipeType() to detect Article/BlogPosting Schema.org types that contain embedded recipe data (recipeIngredient or recipeInstructions fields) and treat them as valid recipes
- **Rationale**: Some recipe websites use @type: "Article" or "BlogPosting" instead of explicit "Recipe" type while still embedding complete recipe data in Schema.org fields, causing parser to skip valid recipes and extract only Open Graph metadata
- **Implementation**: Modified isRecipeType() to check if Article/BlogPosting types contain recipeIngredient or recipeInstructions keys, added logging when Article type with recipe fields is detected, allows extraction of full recipe data from sites like Occupy Kitchen that use article-based markup

#### Nov 21, 2025: Cuisine Extraction from Recipe Titles
- **Decision**: Extract cuisine names from recipe titles as fallback when recipeCuisine Schema.org field is missing or incorrect, using comprehensive list of 100+ known cuisines
- **Rationale**: Recipe websites often set recipeCuisine to their publication location (e.g., "American" for Food & Wine) rather than the actual cuisine origin, causing Georgian/Thai/Italian recipes to be misclassified
- **Implementation**: Created extractCuisineFromTitle() that checks if title starts with cuisine name ("Georgian Beef Stew") or has cuisine in parentheses ("Beef Stew (Georgian)"), adds extracted cuisine to tags if not already present from recipeCuisine field, logs when cuisine is extracted from title vs Schema.org

#### Nov 21, 2025: Subset Tag Removal
- **Decision**: Remove multi-word tags when a component word exists as standalone tag (e.g., "japanese eggplant" removed when "eggplant" present) via findSubsets() function
- **Rationale**: Prevents tag fragmentation where recipes with same ingredient get split across multiple similar tags, reduces clutter in tag lists while maintaining searchability through general terms
- **Implementation**: Added removeSubsets() applied after distinct() in both standardize() and standardizeWithTracking(), checks each multi-word tag to see if any 3+ character word exists standalone, logs which subsets are removed and why, only removes more specific tag keeping general term

#### Nov 21, 2025: Array Type Handling in Schema.org JSON-LD
- **Decision**: Handle @type field as either string ("Recipe") or array (["Recipe", "Article"]) in Schema.org JSON-LD detection via isRecipeType() helper
- **Rationale**: Modern recipe sites use multi-type Schema.org markup for SEO (combining Recipe with Article/BlogPosting), accessing .jsonPrimitive on array caused "is not a JsonPrimitive" crashes before ingredient parsing
- **Implementation**: Created isRecipeType() that checks JsonPrimitive.content == "Recipe" for strings, uses .any() to check if "Recipe" exists in JsonArray for arrays, replaced direct .jsonPrimitive?.content checks across all three detection paths (single object, array, @graph)

#### Nov 21, 2025: Auto-Upgrade HTTP to HTTPS in URL Import
- **Decision**: Added automatic URL normalization in ImportViewModel.fetchRecipeFromUrl() that upgrades http:// to https:// and adds https:// prefix when protocol is missing
- **Rationale**: Android's network security policy blocks cleartext HTTP traffic by default, causing "CLEARTEXT communication not permitted" errors when users paste http:// URLs or forget protocol entirely
- **Implementation**: Created normalizeUrl() private function with case-insensitive protocol detection, logs normalization when URL is changed for transparency, handles three cases: upgrade http:// → https://, add https:// when missing, pass through https:// unchanged

#### Nov 21, 2025: Nested JSON Structure Handling in URL Import
- **Decision**: Enhanced SchemaOrgRecipeParser to recursively handle nested JSON objects and arrays in Schema.org fields (recipeIngredient, recipeCategory, recipeCuisine, keywords) by extracting text from common Schema.org properties (text, name, @value)
- **Rationale**: Many modern recipe websites use complex nested JSON-LD structures where array elements are objects rather than simple strings, causing the parser to crash with "is not a JsonPrimitive" errors and fall back to Open Graph metadata (title + image only), missing all ingredients and instructions
- **Implementation**: Modified parseJsonArrayToStrings() to recursively handle JsonObject items by checking text/name/@value fields, JsonArray items by recursively parsing and joining results, added comprehensive logging showing field types and parsing progress for debugging, added fieldName parameter to track which field is being parsed for better diagnostics

#### Nov 20, 2025: FilterSortGroup Integration Across All Screens
- **Decision**: Integrated FilterSortGroup library across RecipeListScreen, MealPlanningScreen, and GroceryListScreen with domain-specific filter/sort/grouping implementations (meal plan implementations include ContainsRecipeFilter for finding plans with specific recipes per user requirement), ViewModels expose filterSortManager with convenience methods, UI components (FilterChipRow, SortMenu) replace ad-hoc search/filter implementations
- **Rationale**: Users need consistent filtering and sorting across all entity types (recipes, meal plans, grocery lists); meal plan search specifically needs ability to find plans containing specific recipes ("I want to find a meal plan with chicken") which is now possible via ContainsRecipeFilter; consistent UX patterns (filter chips always below TopAppBar, sort menu always in actions) improve discoverability
- **Implementation**: Created mealplan package (DateRangeFilter, RecipeCountFilter, TagFilter, ContainsRecipeFilter matching recipe titles against meal plan's recipeIds, HasNotesFilter, HasDatesFilter, NameSort, StartDateSort, RecipeCountSort, MonthGrouping, TagGrouping, RecipeCountGrouping, PlanTypeGrouping) and grocerylist package (CreatedRecentlyFilter, ModifiedRecentlyFilter, NameSort, DateCreatedSort, DateModifiedSort, MonthGrouping, AgeGrouping); MealPlanViewModel and GroceryListViewModel instantiate FilterSortGroupManager with getAllMealPlans()/getAllLists() flows and search predicates; all three screens replace val items by viewModel.items with viewModel.filterSortManager.filteredItems, add FilterChipRow below search bars, add SortMenu in TopAppBar actions; ViewModels add convenience methods (addFilter, removeFilter, toggleFilter, setSort, toggleSortDirection, setGroupBy) delegating to filterSortManager

#### Nov 20, 2025: Generic FilterSortGroup Library
- **Decision**: Created generic, extractable filter/sort/group library in utils/filtersort/ with core interfaces (Filter<T>, Sort<T>, GroupBy<T,K>), FilterSortGroupManager for reactive state management, recipe-specific implementations, and reusable UI components (FilterChipRow, SortMenu)
- **Rationale**: Multiple screens need filtering (recipes, meal plans, grocery lists, substitutions) and sorting capabilities; creating a generic, type-safe library enables code reuse across all screens, ensures consistent UX, simplifies future extraction to standalone module, and eliminates duplicate filter/sort logic scattered across ViewModels
- **Implementation**: Core package (utils/filtersort/core/) contains Filter<T> interface with matches(), Sort<T> interface with comparator and reversible direction, GroupBy<T,K> interface with extractKey(), and FilterSortGroupManager with combine() flow operators for reactive filtering/sorting/grouping; recipe package (utils/filtersort/recipe/) provides FavoriteFilter, TagFilter, SourceFilter, CookTimeFilter, ServingsFilter, TitleSort, DateCreatedSort, CookTimeSort, SourceGrouping, FavoriteGrouping, etc.; UI package (utils/filtersort/ui/) provides FilterChipRow with horizontal scroll and active state, SortMenu with dropdown and direction toggle; RecipeViewModel instantiates FilterSortGroupManager with getAllRecipes() flow and search predicate matching title/ingredients/tags, exposes filterSortManager publicly for UI binding

#### Nov 20, 2025: Accessible Import from Multiple Entry Points
- **Decision**: Added "Import from File" option to ImportSourceSelectionScreen (4th card in Import tab) and import icon buttons to MealPlanningScreen and GroceryListScreen top bars, making Settings import a backup catch-all
- **Rationale**: Users shouldn't have to navigate to Settings for file import - it should be accessible from the same screens where they add/view content; Settings remains as fallback for users who don't know where else to find it
- **Implementation**: ImportSourceSelectionScreen shows FileUpload card launching file picker with OpenDocument contract storing JSON in MainActivity.pendingImportJson, MealPlanningScreen and GroceryListScreen add FileUpload icon button in TopAppBar actions launching same file picker flow, all three use identical snackbar feedback pattern ("Import file loaded. Navigate to Recipes screen to complete import.")

#### Nov 20, 2025: Share/Import System with Duplicate Detection
- **Decision**: Implemented comprehensive share/import system using ShareHelper (JSON format with base64 photo encoding and text fallback), ImportManager (duplicate detection via title+sourceUrl matching), ImportDialog components (Replace/Keep Both/Skip actions), share buttons in context menus/action rows across all card types and detail screens, Android share sheet integration, manual import from Settings with file picker, share target via ACTION_SEND/ACTION_VIEW intent filters
- **Rationale**: Users need to share recipes/meal plans/grocery lists across devices or with friends via email/messaging apps (Samsung Quick Share, Messenger, SMS), and import shared content without creating duplicates; app-to-app JSON format with photos enables full recipe transfer while human-readable text ensures compatibility with any share target; duplicate detection prevents database bloat and offers user choice for handling conflicts
- **Implementation**: Created ShareModels.kt (SharePackage/ShareRecipe/ShareMealPlan/ShareGroceryList/ImportResult/DuplicateAction), ShareHelper.kt (shareRecipe/shareMealPlan/shareGroceryList with Android share intent, encodePhoto with max 1920px/10MB limit, importFromJson with duplicate detection, generateIncrementedTitle for "Keep Both"), ImportManager.kt (importFromJson/importRecipeWithAction/importMealPlanFromJson/importGroceryListFromJson), ImportDialog.kt (RecipeDuplicateDialog/MealPlanImportDialog/GroceryListImportDialog), share buttons in RecipeCard/MealPlanCard context menus and GroceryListCard/GroceryListDetailScreen action rows, MainActivity.handleIncomingIntent for ACTION_SEND/ACTION_VIEW with pendingImportJson pattern, AndroidManifest intent filters with launchMode=singleTop, SettingsScreen file picker with OpenDocument contract

#### Nov 20, 2025: Callback Pattern for Async List Creation
- **Decision**: Removed createListAndReturn() method and enforced callback pattern for all async list creation operations (createList with onSuccess callback)
- **Rationale**: createListAndReturn() launched a coroutine but immediately returned 0, causing FOREIGN KEY constraint failures when subsequent addRecipesToList() tried to insert items with foreign key to non-existent list ID 0, creating a race condition
- **Implementation**: Removed createListAndReturn() from GroceryListViewModel, updated RecipeListScreen.kt and Navigation.kt to use createList(name) { listId -> addRecipesToList(listId, ...) } pattern ensuring items are only added after list creation completes successfully

#### Nov 20, 2025: LaunchedEffect for Navigation Data Loading
- **Decision**: Wrap ViewModel data loading calls in LaunchedEffect with proper keys and add DisposableEffect cleanup when navigating to detail/edit screens
- **Rationale**: Calling ViewModel methods directly in composable body (e.g., Navigation.kt loadMealPlan) runs on every recomposition, causing race conditions where shared ViewModel state gets overwritten with wrong data, leading to meal plans showing incorrect metadata or data corruption
- **Implementation**: Changed Navigation.kt EditMealPlan route to use LaunchedEffect(planId) { loadMealPlan(planId) } instead of direct call, added DisposableEffect cleanup to clear currentMealPlan on screen disposal, ensures data loads exactly once per navigation and state is cleared when leaving screen

#### Nov 20, 2025: Cascading Recipe Deletion to Maintain Referential Integrity
- **Decision**: RecipeManager.deleteRecipe() now removes recipe IDs from all meal plans before deleting the recipe, ensuring no orphaned references remain
- **Rationale**: Deleting recipes without cleaning up meal plan references created data integrity issues where meal plans referenced non-existent recipes, causing 0 ingredients when generating grocery lists and confusing users with inaccurate counts
- **Implementation**: Added MealPlanDao dependency to RecipeManager, deleteRecipe() fetches all meal plans via Flow.first(), filters for affected plans containing the recipe ID, updates each plan with filtered recipeIds list, then deletes the recipe; added test coverage for cascading behavior

#### Nov 20, 2025: Accurate Ingredient Count Feedback for Grocery Lists
- **Decision**: Changed addRecipesToList() and addMealPlanToList() to return Result<Int> containing count of ingredients actually added, updated UI to show accurate count or warning when 0 ingredients found
- **Rationale**: Previously showed generic success messages even when 0 ingredients were added (e.g., when recipes were missing), misleading users about whether ingredients were actually added to their grocery list
- **Implementation**: GroceryListManager methods return consolidated.size instead of Unit, GroceryListViewModel passes count to success callbacks, UI screens show "Added N ingredient(s)" when successful or "No ingredients found - recipes may be missing" when count is 0

#### Nov 20, 2025: Simplified Meal Plan Date Entry with Inline Calendar Button
- **Decision**: Replaced two separate "Start Date" and "End Date" buttons with single calendar icon button positioned inline with meal plan name field, using Material 3 DateRangePicker
- **Rationale**: Two buttons took significant vertical space and required two interactions for date ranges; single inline button reduces visual clutter and simplifies workflow
- **Implementation**: Added DateRangePickerDialog component to AddEditMealPlanScreen, shows selected dates as supporting text under name field, includes Clear button to remove dates

#### Nov 20, 2025: Recipe Picker Defaults to Existing Tab
- **Decision**: Changed RecipePickerBottomSheet initial tab from 0 (Import) to 1 (Existing) when adding recipes to meal plans
- **Rationale**: Users more commonly select from existing recipes than import new ones while creating meal plans; import tab remains one tap away
- **Implementation**: Set `selectedTab by remember { mutableStateOf(1) }` in RecipePickerBottomSheet composable

#### Nov 20, 2025: Grocery List Operation Feedback with Snackbar
- **Decision**: Added SnackbarHost to MealPlanningScreen and show success message when meal plan ingredients are added to grocery list
- **Rationale**: Users had no confirmation that "Add to Grocery List" button completed successfully, leading to confusion about whether ingredients were actually added
- **Implementation**: Added snackbarHostState and coroutine scope to MealPlanningScreen, pass onSuccess callback to addMealPlanToList showing snackbar with meal plan and list names

#### Nov 20, 2025: Meal Type Words as Noise in Tag Filtering
- **Decision**: Added dinner/dinners, lunch/lunches, breakfast/breakfasts to noiseWords set in TagStandardizer
- **Rationale**: Compound tags like "high-fiber dinners" were not being cleaned to "high-fiber" because "dinner" wasn't recognized as noise; standalone meal type tags still needed to remain valid
- **Implementation**: noiseWords set now includes meal type variations, leveraging existing "keep original if all words filtered" logic to preserve standalone "dinner"/"lunch"/"breakfast" tags

#### Nov 20, 2025: Remove Button in Tag Modification Dialog
- **Decision**: Added minus icon button to `TagModificationDialog` allowing users to mark individual tags for deletion before accepting changes
- **Rationale**: Users had to accept all changes then manually remove unwanted tags from the preview screen; direct deletion streamlines workflow and reduces friction
- **Implementation**: IconButton with Remove icon next to Edit button, clicking sets tag to empty string, visual feedback shows red background with strikethrough text and "Marked for removal" message, Restore button to undo deletion

#### Nov 20, 2025: Delete Option in Recipe Card Context Menu
- **Decision**: Added "Delete" menu item to recipe card context menu (⋮ button) in `RecipeListScreen`
- **Rationale**: Users had to open recipe detail screen to delete recipes, adding unnecessary navigation; list view should support common bulk management tasks
- **Implementation**: DropdownMenuItem with Delete text and trash icon in red, calls `viewModel.deleteRecipe()` directly from list, positioned after "Add to Grocery List" and "Mark as Favorite" options

#### Nov 20, 2025: Tabbed Add Recipe Screen
- **Decision**: Consolidated recipe creation into single `ImportSourceSelectionScreen` with 2 tabs (Import showing URL/PDF/Photo cards, Create showing manual entry button), FAB goes directly to this screen
- **Rationale**: Expandable FAB menu with Create/Import options required too many clicks and was visually cluttered; unified tabbed interface provides cleaner entry point for all recipe creation methods
- **Implementation**: TabRow with Import/Create tabs using CloudDownload and Add icons, Import tab shows existing 3-card layout, Create tab shows single "Create Recipe" card, removed expandable FAB logic from RecipeListScreen

#### Nov 20, 2025: Comprehensive Tag Filtering System
- **Decision**: Enhanced `TagStandardizer` with multi-layered filtering: standalone noise tags set, junk phrase detection, >4 word filter, and branded diet removal
- **Rationale**: Schema.org keywords field and HTML parsing introduced garbage tags ("recipes", "how to make...", "Weight Watchers WW"); needed systematic filtering beyond simple noise word removal
- **Implementation**: Added `junkTags` set (recipes, meals, ideas, dinner ideas, weight watchers), `junkPhrases` list (how to make, for beginners), `isJunkTag()` function checks all conditions including word count >4, filters applied after noise word removal in `standardize()` and `standardizeWithTracking()`

#### Nov 20, 2025: Holiday Consolidation to "Special Occasion"
- **Decision**: Map all holiday-specific tags (Valentine's Day, Christmas, Thanksgiving, etc) to single "special occasion" tag in `TagStandardizer`
- **Rationale**: Holiday tags fragment recipe organization (same recipe for multiple holidays), users think in terms of "special occasions" rather than specific holidays for meal planning
- **Implementation**: Added 15+ holiday mappings to `standardMappings` (valentines day, christmas, thanksgiving, easter, halloween, new year, mother's/father's day, 4th of july, super bowl, game day) all mapping to "special occasion"

#### Nov 20, 2025: Ingredient Type Consolidation
- **Decision**: Map specific protein cuts to general protein type in `TagStandardizer` ("chicken thigh" → "chicken", "ground beef" → "beef")
- **Rationale**: Specific cuts create duplicate tags for same protein type, users search by protein not cut, cut-level detail belongs in ingredients list not tags
- **Implementation**: Added mappings for chicken breast/thigh recipes → chicken, ground beef recipes → beef, applied in `standardMappings` before noise word removal to ensure consistent consolidation

#### Nov 20, 2025: Schema.org Keywords Field Removal
- **Decision**: Stop parsing Schema.org `keywords` field in `SchemaOrgRecipeParser`, only parse `recipeCategory` and `recipeCuisine`
- **Rationale**: Keywords field contained garbage phrases ("easy slow cooker chicken recipe"), recipe titles, and marketing copy instead of meaningful tags; contributed 90% of junk tags
- **Implementation**: Removed `parseJsonArrayToStrings(json["keywords"])` from line 97 in parseRecipeFromJsonLd(), tags now only include recipeCategory + recipeCuisine + HTML categories

#### Nov 20, 2025: HTML Category Link Parsing
- **Decision**: Parse `<a rel="category">` and `<a rel="tag">` links from HTML document in `SchemaOrgRecipeParser` to supplement Schema.org tags
- **Rationale**: WordPress and CMS sites store legitimate category taxonomies (Low Carb, Gluten Free) in HTML links rather than Schema.org fields, Schema.org alone misses valuable categorization
- **Implementation**: Added `parseHtmlCategories()` function selecting `a[rel*=category], a[rel*=tag]` with JSoup, extracts text content, combines with Schema.org tags in `parseSchemaOrg()` via `parsedData.copy(tags = parsedData.tags + htmlCategories)`

#### Nov 20, 2025: Preview-First Recipe Import UI
- **Decision**: Replaced `EditRecipeContent` with `RecipePreviewContent` that shows imported recipes in a formatted, WYSIWYG preview with inline tag editing and per-field edit dialogs
- **Rationale**: Users should see how the recipe will look before saving rather than staring at text fields; most imports don't need editing and inline tag editing is the most common adjustment
- **Implementation**: Card-based layout with inline tag management (InputChips with remove, add field with auto-suggestions), IconButton + Edit icon for title/metadata/ingredients/instructions that open focused dialogs, shows preview of first 5 ingredients and first 3 instruction steps

#### Nov 20, 2025: Tabbed Recipe Picker in Meal Plan Creation
- **Decision**: Modified `RecipePickerBottomSheet` to include two tabs: Import (default) showing URL/PDF/Photo import options, and Existing showing recipe selection grid
- **Rationale**: Meal plan creation workflow required too many navigation steps (navigate to recipes → import → back → meal plans → create → select); users should be able to import recipes directly while building meal plans
- **Implementation**: TabRow with Import tab (shows ImportRecipesTab with source cards) and Existing tab (shows existing recipe grid); Import tab defaults first, automatically switches to Existing after import completion

#### Nov 20, 2025: Tag Standardization Debug Logging
- **Decision**: Added comprehensive logging to `TagStandardizer.standardizeWithTracking()` via new `DebugConfig.Category.TAG_STANDARDIZATION`
- **Rationale**: Need visibility into tag transformations across recipe imports to identify patterns for improving standardizer mappings and noise word lists
- **Implementation**: Logs recipe name, original tags, step-by-step transformations (normalized → mapped → final), filtered tags with reasons, duplicates removed, and final saved tags; filter Logcat by tag `RecipeIndex` + search `TAG_STANDARDIZATION`

#### Nov 20, 2025: Icon-Over-Text Button Pattern for Grocery List Actions
- **Decision**: Replace all OutlinedButtons in GroceryListDetailScreen with icon-over-text Column layout pattern (icon stacked above text label)
- **Rationale**: Icon-over-text provides better touch targets on mobile while using less horizontal space than icon+text side-by-side, creates visual consistency with other bottom action bars
- **Implementation**: Replaced Row with OutlinedButtons with Row of clickable Columns, each containing Icon (24dp) + Spacer(4dp) + Text(labelSmall), applied to Select All, Clear, Recipes, and Meal Plans buttons

#### Nov 20, 2025: Smart Toggle Button for Select/Deselect All
- **Decision**: Consolidate "Select All" and "Deselect All" into single toggle button that changes icon and text based on current state (all checked → show "Deselect", not all checked → show "Select All")
- **Rationale**: Reduces button clutter by eliminating redundant option, makes action more predictable (one button for bulk selection state), follows common UI pattern for state toggles
- **Implementation**: Single clickable Column with conditional icon (allChecked ? RadioButtonUnchecked : CheckCircle), conditional text (allChecked ? "Deselect" : "Select All"), clicks call checkAllItems() or uncheckAllItems() based on state

#### Nov 20, 2025: Reusable AppDatePickerDialog Component
- **Decision**: Extract Material3 DatePickerDialog into reusable AppDatePickerDialog component in ui/components/DatePickerDialog.kt
- **Rationale**: Multiple screens need date selection (AddEditMealPlanScreen now, future features), placeholder "Set Today" dialog was insufficient, centralized component ensures consistent UX
- **Implementation**: AppDatePickerDialog with initialDate, onDateSelected, onDismiss parameters, wraps Material3 DatePickerDialog with DatePicker and rememberDatePickerState, used in AddEditMealPlanScreen for start/end dates

#### Nov 20, 2025: Tag Auto-Suggestion from Existing Tags
- **Decision**: Add auto-suggestion to import screens showing existing tags as SuggestionChips when user types in tag input field (appears after 2 characters)
- **Rationale**: Encourages tag consistency by surfacing already-used tags, reduces typos and duplicate variations, speeds up tagging workflow
- **Implementation**: ImportViewModel.getAllExistingTags() collects all tags from recipes using Flow.first(), EditRecipeContent filters by tagInput with contains(ignoreCase), displays up to 5 suggestions as clickable SuggestionChips

#### Nov 20, 2025: Tag Modification Dialog with Tracking
- **Decision**: Add TagModificationDialog showing original→standardized tag transformations during import with per-tag edit capability before accepting changes
- **Rationale**: Users need visibility into tag standardization (e.g., "vegan bowls"→"vegan") to understand what changed, prevents silent data modification, allows correction of over-aggressive standardization
- **Implementation**: TagStandardizer.standardizeWithTracking() returns TagModification objects with original/standardized/wasModified fields, TagModificationDialog shows Cards with strikethrough original + arrow + standardized, Edit icon per tag opens inline TextField, ImportViewModel stores tagModifications in UiState.Editing

#### Nov 20, 2025: AddEditSubstitutionScreen for User-Editable Substitutions
- **Decision**: Add comprehensive add/edit screen for user-created substitutions with multiple substitute options per ingredient
- **Rationale**: Users need to add custom substitutions for regional ingredients, personal preferences, and dietary restrictions beyond pre-populated defaults
- **Implementation**: AddEditSubstitutionScreen with ingredient/category fields, dynamic substitute list (name, conversion ratio, suitability 1-10 slider, dietary tag chips), createOrUpdateSubstitution() handles create/edit, auto-save on back with validation

#### Nov 20, 2025: Substitution Guide with Database and Recipe Integration
- **Decision**: Add comprehensive substitution guide with Room database storage (IngredientSubstitution entity), pre-populated defaults (~100 substitutes for 22 common ingredients), dedicated SubstitutionGuideScreen accessible from nav drawer, and long-press ingredient lookup from RecipeDetailScreen with quantity-aware conversions
- **Rationale**: Users need quick substitution suggestions while cooking (out of ingredient, dietary restrictions, or preference changes), storing substitutions in database allows user editing and offline access, long-press from recipe provides contextual lookup with portion already included, suitability ratings and dietary tags help users choose best alternative
- **Implementation**: SubstitutionManager handles CRUD operations and default population, SubstitutionViewModel manages search/category/dietary filters using Flow operators (combine, flatMapLatest), SubstitutionGuideScreen shows expandable cards with search and filters, SubstitutionDialog for recipe lookup parses ingredient strings to extract quantity/unit/name using regex patterns similar to IngredientScaler, substitutes ordered by suitability (1-10 rating), converted amounts displayed using ratio multiplication and fraction formatting

#### Nov 20, 2025: Cook Mode with Checkable Steps and Timer
- **Decision**: Add comprehensive cook mode toggle to RecipeDetailScreen with checkable ingredients/instructions, integrated timer (5-60 min dropdown), bold numbers in text, keep screen awake, and session-persistent state
- **Rationale**: Users need hands-free tracking while cooking, manual step tracking is error-prone, timer prevents context switching to separate app, screen staying awake prevents interruption mid-recipe
- **Implementation**: Cook mode state uses remember{} for session persistence (survives toggle, resets on navigation), TextFormatUtils.highlightNumbersInText() bolds temps/times with regex, DisposableEffect manages screen wake lock, LaunchedEffect handles timer countdown, checked items show as 50% alpha italic, "Deselect All" button in cook mode card

#### Nov 20, 2025: Granular Unit Preferences for Liquid and Weight
- **Decision**: Replace single unit system setting with separate liquidVolumePreference and weightPreference in AppSettings, each supporting IMPERIAL/METRIC/BOTH independently
- **Rationale**: Users often want mixed preferences (e.g., metric for liquids, imperial for weight), previous all-or-nothing approach forced compromise, granular control provides flexibility for regional cooking habits
- **Implementation**: AppSettings adds liquidVolumePreference and weightPreference fields, SettingsScreen replaces "Unit System" with two sections ("Liquid Volume Units", "Weight Units"), IngredientUnitConverter.formatIngredient() accepts separate preferences and detects liquid vs weight units, removed toggle button from RecipeDetailScreen in favor of global settings

#### Nov 19, 2025: Full-Screen Recipe Selection Grid
- **Decision**: Replace ModalBottomSheet with full Scaffold-based screen using LazyVerticalGrid (2 columns) for meal plan recipe selection
- **Rationale**: Full screen provides more space to browse recipes, grid layout shows more recipes at once than vertical list, matches user expectation from original request for "full-screen recipe selection"
- **Implementation**: Scaffold with TopAppBar (Close button, Done counter), search field, LazyVerticalGrid with GridCells.Fixed(2), cards show title/servings/time, selected state with primaryContainer color and CheckCircle icon in top-right

#### Nov 19, 2025: Unit Conversion Toggle in Recipe Detail
- **Decision**: Add SwapHoriz icon toggle button in ingredients section header to show/hide inline unit conversions (e.g., "1 cup (237 ml) flour")
- **Rationale**: Users cooking with unfamiliar measurement systems need quick conversions without manually calculating, toggle allows users who don't need conversions to keep interface clean
- **Implementation**: IngredientUnitConverter utility parses ingredient strings, detects units (cups/oz/lbs/ml/g/kg), converts using UnitConverter, formats as "original (converted) remainder", works with scaled ingredients from portion sizing

#### Nov 19, 2025: Portion Scaling with Servings Dropdown
- **Decision**: Add servings dropdown in RecipeDetailScreen info card allowing users to select half, original, 2x, 3x, or 4x servings with automatic ingredient quantity scaling
- **Rationale**: Users frequently cook for different numbers of people than recipe's original servings, manual calculation of scaled quantities is error-prone and time-consuming
- **Implementation**: IngredientScaler utility parses quantities (fractions, mixed numbers, decimals, ranges), scales by factor (selectedServings/recipe.servings), formats output preferring common fractions (1/2, 1/4, 3/4), shows "Scaled for N servings" indicator when not at original

#### Nov 19, 2025: Select All/Deselect All for Grocery Lists
- **Decision**: Add "Select All" and "Deselect All" buttons to GroceryListDetailScreen with smart enable logic (only enabled when there are unchecked or checked items respectively)
- **Rationale**: Allows batch operations when shopping (select all to mark as complete) or when planning (deselect all to reuse list), reduces tedious individual clicking for long lists
- **Implementation**: checkAllItems() and uncheckAllItems() in GroceryListManager iterate through items and update checked status, UI buttons arranged in Row with weight(1f), enabled based on items.any { !it.isChecked } / items.any { it.isChecked }

#### Nov 19, 2025: Inline Action Buttons for Recipe Detail
- **Decision**: Move "Add to Grocery List" and "Add to Meal Plan" from DropdownMenu to inline IconButtons in TopAppBar actions, leaving only Delete in overflow menu
- **Rationale**: These are primary actions users want quick access to (adding recipes to planning tools), hiding them in overflow menu required extra taps and reduced discoverability
- **Implementation**: IconButtons with ShoppingCart and CalendarMonth icons placed between Edit button and MoreVert overflow menu in TopAppBar actions

#### Nov 19, 2025: Enhanced Recipe Cards in Meal Plan Detail
- **Decision**: Replace simple text list of recipes in AddEditMealPlanScreen with rich cards showing title, servings, time, and tags (same visual style as RecipeListScreen cards)
- **Rationale**: Provides at-a-glance meal planning information (how many servings, cooking time) to help users make decisions without opening each recipe, improves visual consistency with rest of app
- **Implementation**: Card with Column layout containing title Row with remove IconButton, servings/time Row with Person and Schedule icons, FlowRow for tags (max 3 + overflow indicator)

#### Nov 19, 2025: Clickable Meal Plan Cards
- **Decision**: Add onClick parameter to Card composable in MealPlanCard to make entire card clickable for navigation to detail/edit screen
- **Rationale**: Improves UX by expanding tap target from small Edit button to entire card, follows common pattern where list cards are clickable to view details
- **Implementation**: Card onClick parameter set to onEdit callback, same navigation as Edit IconButton but larger interaction area

#### Nov 19, 2025: Notes Field Removal from Import Screens
- **Decision**: Remove notes OutlinedTextField from all import verification screens (ImportUrlScreen, ImportPdfScreen, ImportPhotoScreen) and ensure parsers set notes=null
- **Rationale**: Notes should be user-added context/modifications only, not auto-populated from import sources (prevents description text clutter, maintains clean separation between imported data and user notes)
- **Implementation**: Removed OutlinedTextField from EditRecipeContent in all three import screens, SchemaOrgRecipeParser changed from notes=description to notes=null, TextRecipeParser already had notes=null

#### Nov 19, 2025: Grocery List Generation Bug Fix
- **Decision**: Replace synchronous createListAndReturn() with async createList() callback in MealPlanningScreen grocery list picker onCreateNew handler
- **Rationale**: createListAndReturn() was broken (always returned 0 due to async timing), causing meal plan recipes to never be added to newly created grocery lists, proper callback ensures listId is available before calling addMealPlanToList()
- **Implementation**: Changed from "val newListId = groceryListViewModel.createListAndReturn(listName); groceryListViewModel.addMealPlanToList(newListId, planId)" to "groceryListViewModel.createList(listName) { newListId -> groceryListViewModel.addMealPlanToList(newListId, planId) }"

#### Nov 19, 2025: Collapsible Navigation Drawer in All Orientations
- **Decision**: Add collapse button to permanent drawer header (tablets/landscape) with floating expand button when collapsed, allowing drawer to be hidden even on wide screens
- **Rationale**: Provides more screen real estate when needed for content-focused tasks, especially useful on tablets or landscape phones where drawer is permanent
- **Implementation**: Box+Row layout with conditional Surface rendering based on isDrawerCollapsed state, IconButton with ChevronLeft in drawer header, FloatingActionButton with Menu icon overlays content when collapsed

#### Nov 19, 2025: Meal Plan Picker Dialog Component
- **Decision**: Create MealPlanPickerDialog component (parallel to GroceryListPickerDialog) for adding recipes to meal plans from recipe cards/detail screen
- **Rationale**: Calendar icon on recipe cards needed functional meal planning workflow, reusable dialog provides consistent UX across recipe list and detail screens
- **Implementation**: ModalBottomSheet-style AlertDialog with list of existing plans (showing name + formatted date), "Create New Plan" button, integrates with MealPlanViewModel.addRecipeToPlan()

#### Nov 19, 2025: Tag Display Prioritization on Recipe Cards
- **Decision**: Limit recipe cards to maximum 3 tags with smart prioritization (cook method weight=3, cuisine weight=2, ingredients weight=1, meal type weight=-1)
- **Rationale**: Too many tags make cards inconsistent heights and cluttered, prioritizing actionable/descriptive tags (how it's cooked, what cuisine) over generic ones (breakfast/dinner)
- **Implementation**: prioritizeAndLimitTags() function scores tags by keyword matching against category lists, sorts by score descending, takes top 3, displays in existing FlowRow

#### Nov 19, 2025: Chip-Based Tag Input in Recipe Editor
- **Decision**: Replace single-line comma-separated TextField with chip-based UI showing removable InputChips + add TextField with + button
- **Rationale**: Comma-separated input is hard to edit on mobile (poor tap targets, difficult to remove specific tags), chips provide clear visual boundaries and easy removal
- **Implementation**: FlowRow displays tags as InputChips with trailing close icon (onTrailingIconClick removes tag), separate OutlinedTextField with add IconButton to append new tags

#### Nov 19, 2025: Tag Standardization with TagStandardizer Utility
- **Decision**: Create TagStandardizer utility that normalizes imported tags by mapping variations to standard forms, removing noise words, and deduplicating
- **Rationale**: Import sources contain messy tag data ("italian food", "italian cuisine", "Italian Recipe" should all become "italian"), standardization improves filtering/search and reduces clutter
- **Implementation**: Object with standardMappings map (variations→standard), noiseWords set, standardize() function applies mappings, removes noise, lowercases, deduplicates, integrated into TextRecipeParser

#### Nov 19, 2025: Error Handling with SnackbarHost Pattern
- **Decision**: Establish SnackbarHost + BackHandler pattern for all screens: LaunchedEffect monitors ViewModel error states, SnackbarHost displays errors at bottom, BackHandler intercepts system back button for validation/save logic
- **Rationale**: Provides consistent user experience for errors across app, ensures users see error messages without blocking UI (unlike dialogs), prevents accidental data loss via back button
- **Implementation**: Created ErrorHandler utility with getErrorMessage() for user-friendly error text; applied to ImportUrlScreen, ImportPdfScreen, ImportPhotoScreen, GroceryListDetailScreen with SnackbarHostState + Scaffold snackbarHost parameter

#### Nov 19, 2025: FlowRow Layout for Wrapping Tags
- **Decision**: Implement custom FlowRow layout for recipe tags that measures children with minWidth=0 and wraps to next line when exceeding maxWidth
- **Rationale**: Built-in Row doesn't wrap; LazyVerticalGrid doesn't preserve tag order or handle dynamic widths; custom layout provides natural wrapping like web flexbox
- **Implementation**: Layout composable measures all children without minimum width constraint, builds rows by checking cumulative width, places items with proper spacing

#### Nov 19, 2025: Context Menus for Less Common Actions
- **Decision**: Move Duplicate/Delete (meal plans), "Add to Grocery List" (recipes) to DropdownMenu accessed via MoreVert icon button
- **Rationale**: Reduces button clutter, reserves inline space for primary actions (Edit, Generate List, Add to Meal Plan), follows Material Design pattern for secondary actions
- **Implementation**: IconButton with MoreVert icon, DropdownMenu with DropdownMenuItem components, separate destructive actions (Delete) with error color tint

#### Nov 19, 2025: Icon-Only Buttons for Meal Plan Cards
- **Decision**: Replace text buttons with icon-only IconButton components for Edit and Generate List actions on meal plan cards
- **Rationale**: Saves vertical space in cards, allows more content (recipes, dates, tags) to be visible, aligns with app-wide pattern of preferring icons over text
- **Implementation**: IconButton with primary color tint for enabled state, disabled tint for inactive state (no recipes), removed Text labels

#### Nov 19, 2025: Auto-Name Meal Plans from Date Selection
- **Decision**: AddEditMealPlanScreen auto-populates name field from selected dates using formatDateRange() only if user hasn't manually edited name
- **Rationale**: Most users name plans by dates ("Nov 18-22"), auto-fill saves typing; tracking userHasEditedName prevents overwriting custom names
- **Implementation**: LaunchedEffect monitors startDate/endDate changes, formatDateRange() generates "Nov 18-22" (same month) or "Nov 28 - Dec 5" (different months), userHasEditedName state set true on name TextField changes

#### Nov 19, 2025: Canned Items Intelligent Parsing
- **Decision**: GroceryListManager parseIngredient() detects "N oz/g/lb can/jar/bottle/pack of item" pattern and normalizes to qty=1, unit=container, name=item, notes=size
- **Rationale**: Recipe ingredients list container sizes ("9 oz can") but shoppers buy by container count; normalizing to countable units simplifies shopping while preserving size info for verification
- **Implementation**: Regex pattern `([\\d./]+)\\s+(oz|g|ml|lb)\\s+(can|jar|bottle|pack|package)s?\\s+(?:of\\s+)?(.+)` captures size+container, creates ParsedIngredient with container as unit and size in notes

#### Nov 19, 2025: Units Dropdown for Grocery Items
- **Decision**: Replace unit text field in GroceryItemDetailDialog with ExposedDropdownMenuBox containing predefined units (none, cup, tbsp, tsp, oz, lb, g, kg, ml, L, can, pack, bottle, jar)
- **Rationale**: Prevents typos, ensures consistent unit naming for consolidation, provides better UX than keyboard entry for common units
- **Implementation**: ExposedDropdownMenuBox with OutlinedTextField (readOnly), DropdownMenu with DropdownMenuItem for each unit, stores empty string for "none"

#### Nov 19, 2025: Click/Long-Press Interactions for Grocery List Items
- **Decision**: GroceryItemRow uses combinedClickable with onClick for checkbox toggle, onLongClick for detail dialog (replacing direct checkbox interaction and short-click detail)
- **Rationale**: Faster shopping workflow - tap anywhere on item to check/uncheck without precise checkbox targeting; long-press for editing follows mobile conventions
- **Implementation**: combinedClickable modifier on Row, checkbox with onCheckedChange=null (read-only), onClick toggles via viewModel, onLongClick shows dialog

#### Nov 19, 2025: Discard Button for Import Screens
- **Decision**: Add Delete icon button to ImportUrlScreen, ImportPdfScreen, ImportPhotoScreen TopAppBar that shows confirmation dialog then calls viewModel.reset() and onNavigateBack()
- **Rationale**: Auto-save on back prevented escaping broken imports (failed parsing, partial data); explicit discard with confirmation provides escape hatch
- **Implementation**: IconButton with Delete icon (error tint) in TopAppBar actions, AlertDialog for confirmation, button only shown in Editing state, discards without saving

#### Nov 19, 2025: Settings Infrastructure with SettingsManager
- **Decision**: Create SettingsManager class wrapping SharedPreferences with StateFlow exposure pattern (similar to existing Managers)
- **Rationale**: Maintains architectural consistency with Manager pattern, provides reactive state for UI updates, centralizes settings logic away from ViewModels
- **Implementation**: SettingsManager(Context) creates SharedPreferences, exposes StateFlow<AppSettings>, provides setter methods that update both prefs and StateFlow, DebugConfig logging for changes

#### Nov 19, 2025: UnitConverter Utility Pattern
- **Decision**: Create UnitConverter object with direct conversion functions (cupsToMl, fahrenheitToCelsius, etc.) and smart helpers (volumeToMetric, formatNumber)
- **Rationale**: Cooking conversions are pure functions with no state, object singleton provides global access without dependency injection overhead
- **Implementation**: Object with const conversion factors, smart helpers that choose ml/L based on quantity, formatNumber removes unnecessary decimals (1.0→"1", 1.33→"1.33")

#### Nov 19, 2025: Intelligent Ingredient Consolidation for Grocery Lists
- **Decision**: GroceryListManager consolidates ingredients by removing ignored modifiers (diced, chopped, shredded, sliced, cubed) and summing quantities for matching name+unit pairs
- **Rationale**: Users transform ingredients at home (dicing, chopping), so modifiers don't affect shopping; consolidation reduces list length and simplifies shopping
- **Implementation**: parseIngredient() removes modifiers via regex, consolidateIngredients() groups by normalized name+unit, sums quantities (handles fractions like 1/2), merges source recipe IDs

#### Nov 19, 2025: Quick-Entry Text Field for Grocery Lists
- **Decision**: GroceryListDetailScreen places text field at top for one-tap manual item entry (inspired by Out of Milk app)
- **Rationale**: While shopping, users discover additional items and need instant entry without navigating away; top placement makes it most accessible
- **Implementation**: OutlinedTextField + IconButton in Card at top of screen, adds to list immediately on button click, no dialog required for basic entry

#### Nov 19, 2025: Source Recipe Tracking for Grocery Items
- **Decision**: GroceryItem stores List<Long> sourceRecipeIds to track which recipes contributed each ingredient
- **Rationale**: Users want to see where consolidated items came from for context and verification; supports future features like removing recipe items
- **Implementation**: sourceRecipeIds field with @TypeConverter, merged when consolidating duplicate items, displayed in item detail dialog

#### Nov 19, 2025: Grocery List Picker Dialog Pattern
- **Decision**: Create reusable GroceryListPickerDialog component for selecting/creating lists from multiple entry points (recipes, meal plans)
- **Rationale**: Consistent UX for adding to lists from different screens; single component ensures uniform behavior and reduces code duplication
- **Implementation**: GroceryListPickerDialog with availableLists, onListSelected, onCreateNew callbacks; used by RecipeListScreen and MealPlanningScreen

#### Nov 19, 2025: Auto-Tag Aggregation for Meal Plans
- **Decision**: MealPlanManager auto-aggregates ingredient and special event tags from all recipes in plan, plus detects special events from plan name
- **Rationale**: Manual tagging is tedious; auto-tagging provides immediate filtering/search value without user effort, supports grocery list generation
- **Implementation**: getAutoTags() fetches recipe tags via RecipeDao, detectSpecialEventFromName() matches plan name against RecipeTags.SPECIAL_EVENTS, combines and deduplicates

#### Nov 19, 2025: Flexible Date Ranges for Meal Planning
- **Decision**: MealPlan uses optional startDate/endDate (both nullable) instead of strict week structure
- **Rationale**: Users plan for varying durations (Sun-Thu, single-day events, full weeks); nullable dates support indefinite plans and special events without dates
- **Implementation**: startDate and endDate as Long? (nullable timestamps), formatDateRange() displays range or "No dates set", UI shows optional date pickers

#### Nov 19, 2025: Intelligent Content Filtering for PDF Extraction
- **Decision**: TextRecipeParser filters extracted content with isWebsiteNoise(), looksLikeIngredient(), and looksLikeInstruction() before returning ingredients/instructions
- **Rationale**: PDF text extraction from web pages includes navigation, CTAs, and footer text mixed with recipe content due to non-visual extraction order
- **Implementation**: Regex patterns detect website noise (save/shop CTAs, ratings prompts, spaced letters), validate ingredients (measurements, food words), validate instructions (cooking verbs, temps/times)

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
