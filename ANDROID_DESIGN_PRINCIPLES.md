# Android Design Principles & Standards

**Version:** 2.0
**Last Updated:** 2025-11-18

---

## 🎯 Quick Reference - Core Rules

### ❌ CRITICAL - Never Do This
1. **NEVER use `android.util.Log` directly** → Always use `DebugConfig.debugLog(context, category, message)`
2. **NO business logic in ViewModels** → Business logic belongs in Manager classes in `data/`
3. **NO hardcoded settings in business logic** → Use Settings classes with StateFlow
4. **NO duplicate data sources** → Single Source of Truth (SSOT) only
5. **NO direct DAO/database access in UI** → Use ViewModels that call Managers/Repositories
6. **NEVER use LiveData in new code** → Always use StateFlow

### 🏗️ Architecture Rules
1. **Manager Pattern**: Complex business logic → `data/ContentManagers/` or `data/Playback/`
2. **Single Source of Truth**: Each piece of data has exactly ONE authoritative source
3. **Config Over Code**: User preferences → Settings classes, not hardcoded behavior
4. **Unified Entities**: Single data class with behavioral flags vs. separate entity classes
5. **Thin Repositories**: Simple CRUD only; complex operations → Managers

### 📊 State Management Rules
1. **StateFlow for all observable state** (settings, ViewModels, managers)
2. **ViewModel pattern**: Expose `StateFlow<State>`, handle events via functions, delegate to Managers
3. **Compose state**: `remember { mutableStateOf() }` for local UI, `collectAsState()` for StateFlow
4. **Never expose MutableStateFlow** → Use `.asStateFlow()` for public API

### 🎨 UI/Code Organization Rules
1. **Extract components proactively**: If >50 lines, self-contained, or might be reused → extract to `ui/components/`
2. **Material 3 spacing constants**: Use `Spacing.small/medium/large` (4dp/8dp/16dp/24dp/32dp)
3. **Detail screens**: Use `contentWindowInsets = WindowInsets(0, 0, 0, 0)` to prevent double padding
4. **Package structure**: `data/`, `ui/screens/`, `ui/components/`, `ui/theme/`, `utils/`, `navigation/`

### 📝 Documentation Rules
1. **Maintain 5 core docs**: PROJECT_STATUS.md, DECISION_LOG.md, DEVELOPER_GUIDE.md, FILE_CATALOG.md, TEST_SCENARIOS.md
2. **Update docs with code changes**: When adding features, update all relevant documentation files
3. **Log architectural decisions**: Record major decisions with rationale in DECISION_LOG.md

### 🧪 Testing Rules
1. **Test coverage priorities**: Managers (80%+), Data layer (70%+), Utils (60%+), UI (manual OK)
2. **Use in-memory database** for DAO/repository tests
3. **Document test scenarios** in TEST_SCENARIOS.md

### 💾 Performance Rules
1. **Always use `Dispatchers.IO`** for database and file operations
2. **Use Flow for reactive queries** instead of suspend functions returning lists
3. **Batch database inserts** with `@Transaction`
4. **LazyColumn with keys** for long lists: `items(list, key = { it.id })`

### 📦 Git/Commit Rules
1. **Commit format**: `<type>: <description>` with types: feat, fix, refactor, docs, test, chore, style
2. **Include attribution**: `Generated with [Claude Code](https://claude.com/claude-code)` and `Co-Authored-By: Claude <noreply@anthropic.com>`
3. **Branch naming**: `<type>/<short-description>` (e.g., `feat/smart-playlists`)
4. **NEVER merge directly to main**: Always create PR for review, even for small changes
5. **PR requirements**: Summary, test plan, screenshots (if UI changes)

---

## 📚 Detailed Sections

Jump to: [Architecture](#architecture-details) | [State Management](#state-management-details) | [UI/UX](#uiux-details) | [Code Organization](#code-organization-details) | [Logging](#logging-details) | [Documentation](#documentation-details) | [Testing](#testing-details) | [Git](#git-details)

---

## Architecture Details

### Single Source of Truth (SSOT)

**Intent:** Eliminate data synchronization bugs by having exactly one place where each piece of data is defined.

**Pattern:**
```kotlin
// GOOD: Single source of truth
object GenreNormalizer {
    private val canonicalGenres = setOf("Rock", "Jazz", "Classical")
    fun getCanonicalGenres(): List<String> = canonicalGenres.sorted()
}

// BAD: Duplicate data
class FilterScreen {
    private val genres = listOf("Rock", "Jazz") // DON'T DO THIS
}
```

**Examples:**
- Canonical genre lists → `GenreNormalizer.kt`
- User settings → `AppSettings.kt`, `[Feature]Settings.kt`
- Persisted data → Room DAOs

---

### Config Over Code

**Intent:** Make app behavior configurable through settings instead of hardcoding values in business logic.

**Pattern:**
```kotlin
// GOOD: Configurable via settings
class GenreSettings(context: Context) {
    private val _enabledGenres = MutableStateFlow(getDefaults())
    val enabledGenres: StateFlow<Set<String>> = _enabledGenres.asStateFlow()

    fun update(genres: Set<String>) {
        _enabledGenres.value = genres
        saveToPreferences()
    }
}

// Usage in business logic
class PlaylistManager {
    fun filter(tracks: List<Track>): List<Track> {
        val enabled = genreSettings.enabledGenres.value
        return tracks.filter { it.genre in enabled }
    }
}

// BAD: Hardcoded behavior
fun filter(tracks: List<Track>) = tracks.filter { it.genre != "Christmas" }
```

---

### Manager Pattern

**Intent:** Separate complex business logic from UI concerns. Managers orchestrate multi-step operations and coordinate between data sources.

**Location:** `data/ContentManagers/` or `data/Playback/`

**Responsibilities:**
- **Managers**: Business logic, data transformation, multi-step operations, orchestration
- **ViewModels**: UI state, user events, calling managers, exposing StateFlow
- **Repositories**: Simple CRUD operations, thin DAO wrappers

**Pattern:**
```kotlin
// data/ContentManagers/PlaylistManager.kt
class PlaylistManager(
    private val db: AppDatabase,
    private val context: Context
) {
    suspend fun createSmartPlaylist(
        name: String,
        criteria: SmartCriteria
    ): Playlist = withContext(Dispatchers.IO) {
        val tracks = db.trackDao().getAll()
        val filtered = applySmartCriteria(tracks, criteria)

        val playlist = Playlist(
            name = name,
            isSmartPlaylist = true,
            smartCriteria = criteria.toJson()
        )
        val id = db.playlistDao().insert(playlist)

        // Insert cross-references
        filtered.forEach { track ->
            db.playlistTrackDao().insert(PlaylistTrack(id, track.id))
        }

        playlist.copy(id = id)
    }
}

// ui/screens/PlaylistViewModel.kt
class PlaylistViewModel(
    private val playlistManager: PlaylistManager
) : ViewModel() {
    private val _state = MutableStateFlow(PlaylistState())
    val state: StateFlow<PlaylistState> = _state.asStateFlow()

    fun createPlaylist(name: String, criteria: SmartCriteria) {
        viewModelScope.launch {
            playlistManager.createSmartPlaylist(name, criteria)
        }
    }
}
```

---

### Unified Entity Pattern

**Intent:** Use single data class with behavioral flags instead of creating separate entities for variations.

**Pattern:**
```kotlin
// GOOD: Unified entity
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val trackCount: Int = 0,
    val isSmartPlaylist: Boolean = false,      // Behavioral flag
    val smartCriteria: String? = null,         // Only used if isSmartPlaylist=true
    val createdAt: Long = System.currentTimeMillis()
)

// BAD: Separate entities
@Entity(tableName = "playlists")
data class Playlist(...)

@Entity(tableName = "smart_playlists")
data class SmartPlaylist(...)  // Duplicates fields
```

**Benefits:**
- Single DAO handles all variants
- Easy type conversion
- Simpler database schema
- UI doesn't need to handle multiple types

---

### Repository Pattern

**Intent:** Thin wrapper around DAOs for simple CRUD. Keep it simple.

**Pattern:**
```kotlin
// GOOD: Simple repository
class PlaylistRepository(private val dao: PlaylistDao) {
    fun getAll(): Flow<List<Playlist>> = dao.getAllPlaylists()
    suspend fun getById(id: Long): Playlist? = dao.getById(id)
    suspend fun insert(playlist: Playlist): Long = dao.insert(playlist)
    suspend fun delete(playlist: Playlist) = dao.delete(playlist)
}

// Complex operations go in Manager
class PlaylistManager(private val repository: PlaylistRepository) {
    suspend fun createSmartPlaylist(...) { /* complex logic */ }
}
```

---

## State Management Details

### StateFlow for Observable State

**Intent:** Modern, type-safe reactive state that works seamlessly with Kotlin coroutines and Compose.

**Pattern:**
```kotlin
// Settings class
class GenreSettings(private val context: Context) {
    private val _enabledGenres = MutableStateFlow(getDefaults())
    val enabledGenres: StateFlow<Set<String>> = _enabledGenres.asStateFlow()

    fun updateEnabledGenres(genres: Set<String>) {
        _enabledGenres.value = genres
        saveToPreferences()
    }
}

// Compose consumption
@Composable
fun GenreFilterScreen(settings: GenreSettings) {
    val enabledGenres by settings.enabledGenres.collectAsState()
    // Use enabledGenres in UI
}
```

**Rules:**
- Never expose `MutableStateFlow` publicly
- Always use `.asStateFlow()` for external API
- Never use LiveData in new code

---

### ViewModel State Pattern

**Intent:** Centralize UI state in a single data class for predictability and testability.

**Pattern:**
```kotlin
data class PlaylistScreenState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPlaylist: Playlist? = null
)

class PlaylistViewModel(
    private val manager: PlaylistManager
) : ViewModel() {
    private val _state = MutableStateFlow(PlaylistScreenState())
    val state: StateFlow<PlaylistScreenState> = _state.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                manager.getPlaylists().collect { playlists ->
                    _state.update { it.copy(playlists = playlists, isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onEvent(event: PlaylistEvent) {
        when (event) {
            is PlaylistEvent.Create -> createPlaylist(event.name)
            is PlaylistEvent.Delete -> deletePlaylist(event.id)
        }
    }
}
```

---

### Compose State Rules

**When to use what:**
- `remember { mutableStateOf() }`: Local UI state (expanded/collapsed, text field, dialog open)
- `collectAsState()`: StateFlow from ViewModel or settings
- `derivedStateOf`: Computed values based on other state
- Hoist state: When multiple components need to share it

**Example:**
```kotlin
@Composable
fun FilterSection(
    enabledGenres: Set<String>,           // Hoisted state
    onGenresChange: (Set<String>) -> Unit // Callback
) {
    var expanded by remember { mutableStateOf(false) }  // Local UI state

    Card(modifier = Modifier.clickable { expanded = !expanded }) {
        // UI implementation
    }
}
```

---

## UI/UX Details

### Material 3 Spacing Constants

**Intent:** Consistent spacing creates visual harmony and professional appearance.

**Implementation:**
```kotlin
// ui/theme/Spacing.kt
object Spacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
}

// Usage
Column(
    modifier = Modifier.padding(Spacing.medium),
    verticalArrangement = Arrangement.spacedBy(Spacing.small)
) {
    // Content
}
```

**Standard Use Cases:**
- `4.dp`: Between related text elements, icon padding
- `8.dp`: Between list items, dense layouts, card content padding
- `16.dp`: Default screen padding, between sections
- `24.dp`: Between major UI sections
- `32.dp`: Top/bottom screen padding for prominent sections

---

### Detail Screen Pattern

**Intent:** Prevent double padding and allow natural scrolling.

**Pattern:**
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Title") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)  // CRITICAL
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Content
        }
    }
}
```

---

### Component Extraction Guidelines

**Intent:** Keep files small and readable. Encourage modularity and reusability.

**Extract to `ui/components/` when:**
- Component is >50 lines
- Component has self-contained, clear responsibility
- Component might reasonably be reused (even if not used yet)
- Extracting would make parent screen more readable

**Don't extract when:**
- Component is <20 lines and very simple
- Component is tightly coupled to single screen's specific state
- Extraction would create unnecessary indirection

**Example:**
```kotlin
// Before: Inline in screen (getting long)
@Composable
fun RecipeScreen() {
    LazyColumn {
        items(recipes) { recipe ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { /* navigate */ }
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                        Text("${recipe.prepTime} min")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(recipe.description, maxLines = 2)
                    Spacer(Modifier.height(8.dp))
                    FlowRow {
                        recipe.tags.forEach { tag ->
                            AssistChip(onClick = {}, label = { Text(tag) })
                        }
                    }
                }
            }
        }
    }
}

// After: Extracted component
// ui/components/RecipeCard.kt
@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                Text("${recipe.prepTime} min")
            }
            Spacer(Modifier.height(8.dp))
            Text(recipe.description, maxLines = 2)
            Spacer(Modifier.height(8.dp))
            FlowRow {
                recipe.tags.forEach { tag ->
                    AssistChip(onClick = {}, label = { Text(tag) })
                }
            }
        }
    }
}

// Screen is now much cleaner
@Composable
fun RecipeScreen() {
    LazyColumn {
        items(recipes) { recipe ->
            RecipeCard(
                recipe = recipe,
                onClick = { /* navigate */ }
            )
        }
    }
}
```

---

### Card Consistency

**Intent:** Uniform card styling across the app.

**Patterns:**
```kotlin
// Standard card
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Content
    }
}

// Clickable card
Card(
    modifier = Modifier.fillMaxWidth(),
    onClick = { /* action */ },
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
) {
    // Content
}
```

---

### Collapsible Section Pattern

**Intent:** Reduce scrolling on long settings/filter screens.

**Pattern:**
```kotlin
@Composable
fun CollapsibleSection(
    title: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess
                                  else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            if (expanded) {
                content()
            }
        }
    }
}
```

---

## Code Organization Details

### Package Structure

```
app/src/main/java/com/yourapp/
├── data/
│   ├── ContentManagers/       # Business logic managers
│   ├── Database/              # Room entities, DAOs, database
│   ├── Playback/             # Playback-related managers (if applicable)
│   └── Settings/             # Settings classes
├── ui/
│   ├── screens/              # Full-screen composables
│   ├── components/           # Reusable UI components
│   └── theme/                # Material theme, spacing, colors
├── utils/
│   ├── DebugConfig.kt        # Debug logging utility
│   ├── PermissionUtils.kt    # Permission helpers
│   └── FileUtils.kt          # File operation helpers
├── navigation/
│   └── NavGraph.kt           # Navigation setup
└── MainActivity.kt
```

**Rules:**
- Business logic → `data/ContentManagers/` or `data/Playback/`
- Database code → `data/Database/`
- Settings classes → `data/Settings/`
- Screen composables → `ui/screens/`
- Reusable components → `ui/components/`
- Utilities → `utils/`

---

### File Naming Conventions

- **Screens:** `[Feature]Screen.kt` (e.g., `RecipeScreen.kt`, `SettingsScreen.kt`)
- **Components:** `[Component].kt` (e.g., `RecipeCard.kt`, `TagChip.kt`)
- **Managers:** `[Domain]Manager.kt` (e.g., `RecipeManager.kt`, `MealPlanManager.kt`)
- **Settings:** `[Category]Settings.kt` (e.g., `RecipeSettings.kt`, `AppSettings.kt`)
- **DAOs:** `[Entity]Dao.kt` (e.g., `RecipeDao.kt`, `MealPlanDao.kt`)
- **Repositories:** `[Entity]Repository.kt` (e.g., `RecipeRepository.kt`)

---

## Logging Details

### CRITICAL: DebugConfig System

**Why this matters:**
- Production apps should NOT log sensitive data
- Debug logs create performance overhead
- Users don't want log spam
- Developers need targeted logging during development

**Implementation:**
```kotlin
// utils/DebugConfig.kt
package com.yourapp.utils

import android.content.Context
import android.util.Log
import com.yourapp.data.Settings.AppSettings
import kotlinx.coroutines.runBlocking

/**
 * DebugConfig - Centralized debug logging system
 *
 * CRITICAL: NEVER use android.util.Log directly in the codebase.
 * Always use DebugConfig.debugLog() which respects user's debug mode setting.
 */
object DebugConfig {
    /**
     * Category-based debug logging
     *
     * @param context Application context
     * @param category Log category (e.g., "RecipeManager", "URLParser")
     * @param message Log message
     */
    fun debugLog(context: Context, category: String, message: String) {
        val appSettings = AppSettings.getInstance(context)
        val debugMode = runBlocking {
            appSettings.getSettings().debugMode
        }

        if (debugMode) {
            Log.d("RecipeIndex-$category", message)
        }
    }
}
```

**Usage:**
```kotlin
// GOOD
class RecipeManager(private val context: Context, private val db: AppDatabase) {
    suspend fun createRecipe(name: String): Recipe {
        DebugConfig.debugLog(context, "RecipeManager", "Creating recipe: $name")

        val recipe = Recipe(name = name)
        val id = db.recipeDao().insert(recipe)

        DebugConfig.debugLog(context, "RecipeManager", "Recipe created with ID: $id")

        return recipe.copy(id = id)
    }
}

// BAD - NEVER DO THIS
Log.d("RecipeManager", "Creating recipe")  // ❌ NEVER
```

**Category Guidelines:**
- `"MainActivity"`: App lifecycle events
- `"RecipeManager"`: Recipe operations
- `"URLParser"`: URL parsing operations
- `"PermissionHandler"`: Permission requests
- `"DatabaseMigration"`: Database migrations

**Benefits:**
- Filter logs: `adb logcat | grep "RecipeIndex-RecipeManager"`
- Users can disable in production
- No sensitive data leaks
- Easy operation tracing

---

## Documentation Details

### The 5-Document System

**Intent:** Maintain institutional knowledge and make codebase understandable for new developers (including future you).

Every project MUST maintain these files in the root:

1. **PROJECT_STATUS.md**: High-level overview, current state, completed features, known issues, next milestones
2. **DECISION_LOG.md**: Architectural decisions with context, rationale, consequences, alternatives considered
3. **DEVELOPER_GUIDE.md**: Onboarding guide, setup instructions, coding standards, common tasks
4. **FILE_CATALOG.md**: Directory of important files with descriptions and purposes
5. **TEST_SCENARIOS.md**: Automated test coverage, manual test scenarios, regression tests

---

### PROJECT_STATUS.md Structure

```markdown
# Project Status

## Overview
[Brief description of the app and its purpose]

## Current State
- **Version:** X.X.X
- **Status:** [Alpha/Beta/Production]
- **Last Updated:** YYYY-MM-DD

## Completed Features
- Feature 1
- Feature 2

## In Progress
- Feature 3 (70% complete)

## Known Issues
- Issue 1 [Severity: High/Medium/Low]

## Next Milestones
- Milestone 1 (Target: Date)
```

**Update when:** Completing features, starting new phases, releasing versions, discovering critical bugs

---

### DECISION_LOG.md Structure

```markdown
# Decision Log

## [YYYY-MM-DD] Decision Title

**Context:**
[Why this decision was needed]

**Decision:**
[What was decided]

**Rationale:**
[Why this approach was chosen]

**Consequences:**
[Impact on codebase, performance, future development]

**Alternatives Considered:**
- Alternative 1: [Why not chosen]
- Alternative 2: [Why not chosen]
```

**Example:**
```markdown
## [2025-11-18] Use Unified Recipe Entity for All Import Sources

**Context:**
Recipes can be imported from URLs, PDFs, photos, or manual entry. Need to decide whether to use separate entities or unified entity.

**Decision:**
Use single Recipe entity with `importSource` enum and source-specific nullable fields.

**Rationale:**
- Simpler database schema (1 table vs 4)
- Easier to convert between import methods
- Single DAO handles all recipe operations
- UI doesn't need type-specific handling

**Consequences:**
- Some fields nullable and only used for specific sources
- Need to validate source-specific fields based on importSource
- Slight storage overhead for unused fields

**Alternatives Considered:**
- Separate entities per source: Rejected due to massive code duplication
- Inheritance: Rejected due to Room's limited inheritance support
```

**Update when:** Making architectural decisions, choosing libraries, changing major patterns, significant refactoring

---

### Documentation Update Checklist

When making changes, update relevant docs:
- [ ] PROJECT_STATUS.md - If feature state changed
- [ ] DECISION_LOG.md - If architectural decision made
- [ ] DEVELOPER_GUIDE.md - If new pattern/standard introduced
- [ ] FILE_CATALOG.md - If new major file added
- [ ] TEST_SCENARIOS.md - If new tests added

---

## Testing Details

### Testing Framework

**Use:**
- **JUnit 4** for unit tests
- **Robolectric** for Android framework tests (if needed)
- **Room In-Memory Database** for DAO/repository tests
- **Kotlin Coroutines Test** for suspend function tests

**Dependencies:**
```gradle
testImplementation 'junit:junit:4.13.2'
testImplementation 'androidx.test:core:1.5.0'
testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3'
testImplementation 'androidx.room:room-testing:2.6.1'
```

---

### Test Organization

```
app/src/test/java/com/yourapp/
├── data/
│   ├── managers/
│   │   └── RecipeManagerTest.kt
│   ├── database/
│   │   └── RecipeDaoTest.kt
│   └── normalizers/
│       └── IngredientNormalizerTest.kt
└── utils/
    └── UnitConverterTest.kt
```

**Naming:** `[ClassName]Test.kt`

---

### Test Pattern for Managers

```kotlin
@RunWith(RobolectricTestRunner::class)
class RecipeManagerTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context
    private lateinit var recipeManager: RecipeManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        recipeManager = RecipeManager(database, context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `createRecipe creates recipe in database`() = runBlocking {
        val name = "Chocolate Chip Cookies"

        val recipe = recipeManager.createRecipe(name)

        assertNotNull(recipe.id)
        assertEquals(name, recipe.name)

        val retrieved = database.recipeDao().getById(recipe.id)
        assertEquals(name, retrieved?.name)
    }
}
```

---

### Test Coverage Goals

**Priority:**
- ✅ **Critical:** Business logic in Managers (80%+ coverage)
- ✅ **High:** Data layer (DAOs, Repositories) (70%+ coverage)
- ✅ **Medium:** Utilities and helpers (60%+ coverage)
- ⚠️ **Low:** UI composables (manual testing acceptable)

---

### Manual Testing Checklist

**Before Each Release:**
- [ ] Fresh install flow
- [ ] Permission grant/deny scenarios
- [ ] Core feature walkthrough
- [ ] Edge cases (empty states, no data)
- [ ] Performance with large datasets
- [ ] Rotation and configuration changes

---

## Git Details

### Commit Message Format

```
<type>: <short description>

<detailed description (optional)>

Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code restructuring without behavior change
- `docs`: Documentation updates
- `test`: Adding or updating tests
- `chore`: Build, dependencies, or tooling changes
- `style`: Code formatting (not UI style)

**Examples:**
```
feat: Add recipe import from URLs with metadata extraction

Implemented RecipeManager.importFromUrl() with support for:
- Recipe Schema JSON-LD parsing
- Fallback HTML parsing for common recipe sites
- Automatic image download and storage

Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

```
fix: Prevent crash when scaling recipe with zero servings

RecipeManager now validates servings > 0 before scaling to prevent
division by zero.

Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

---

### Commit Size Guidelines

**Good Commit:**
- Single logical change
- Includes related tests
- Updates relevant documentation
- Builds successfully

**Too Large:**
- Multiple features in one commit
- Mixing refactoring with new features

**Too Small:**
- Separate commits for whitespace
- Breaking single feature across 10+ commits

---

### Branch Naming

**Pattern:** `<type>/<short-description>`

**Examples:**
- `feat/recipe-import`
- `fix/scaling-crash`
- `refactor/recipe-manager`
- `docs/update-decision-log`

---

### Pull Request Workflow

**Rule:** NEVER merge directly to main. Always create a PR for review, even for small changes.

**PR Template:**
```markdown
## Summary
[Brief description of changes]

## Changes
- Change 1
- Change 2
- Change 3

## Test Plan
- [ ] Tested feature X
- [ ] Verified edge case Y
- [ ] Ran full test suite
- [ ] Manual testing completed

## Screenshots (if UI changes)
[Add screenshots or video]

## Documentation Updated
- [ ] PROJECT_STATUS.md
- [ ] DECISION_LOG.md
- [ ] DEVELOPER_GUIDE.md
- [ ] FILE_CATALOG.md
- [ ] TEST_SCENARIOS.md

## Checklist
- [ ] Code follows design principles
- [ ] All tests pass
- [ ] No direct Log.d() usage
- [ ] Business logic in Managers
- [ ] Documentation updated
```

**PR Review Checklist:**
- [ ] Code follows architectural patterns (SSOT, Manager pattern, Config over Code)
- [ ] No business logic in ViewModels
- [ ] DebugConfig used instead of Log.d()
- [ ] StateFlow used for observable state
- [ ] Components extracted appropriately
- [ ] Tests added for business logic
- [ ] Documentation updated
- [ ] Commit messages follow format
- [ ] No merge conflicts

**Merging:**
- Require at least 1 approval before merging
- Use "Squash and merge" for cleaner history
- Delete branch after merging

---

## Performance Considerations

### Database Operations

**Always use IO dispatcher:**
```kotlin
suspend fun loadRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
    database.recipeDao().getAll()
}
```

**Use Flow for reactive queries:**
```kotlin
fun observeRecipes(): Flow<List<Recipe>> {
    return database.recipeDao().getAllRecipes()
}
```

**Batch inserts:**
```kotlin
@Transaction
suspend fun insertRecipes(recipes: List<Recipe>) {
    recipes.forEach { recipeDao.insert(it) }
}
```

---

### File Operations

**Use background threads:**
```kotlin
class RecipeImportManager {
    suspend fun importPDF(uri: Uri) = withContext(Dispatchers.IO) {
        // File operations here
    }
}
```

---

### UI Performance

**LazyColumn for long lists:**
```kotlin
LazyColumn {
    items(recipes, key = { it.id }) { recipe ->
        RecipeCard(recipe)
    }
}
```

**Avoid expensive operations in composition:**
```kotlin
// BAD
@Composable
fun Screen() {
    val filtered = recipes.filter { /* expensive */ }  // Every recomposition
}

// GOOD
@Composable
fun Screen() {
    val filtered = remember(recipes) {
        recipes.filter { /* expensive */ }
    }
}
```

---

## Common Anti-Patterns to Avoid

### ❌ Business Logic in ViewModels

**DON'T:**
```kotlin
class RecipeViewModel : ViewModel() {
    fun createRecipe() {
        viewModelScope.launch {
            val recipe = db.recipeDao().getAll()  // Direct DB access - BAD
            val filtered = recipe.filter { /* logic */ }
            db.recipeDao().insert(...)
        }
    }
}
```

**DO:**
```kotlin
class RecipeViewModel(private val manager: RecipeManager) : ViewModel() {
    fun createRecipe(name: String) {
        viewModelScope.launch {
            manager.createRecipe(name)
        }
    }
}
```

---

### ❌ Using Log.d() Directly

**DON'T:**
```kotlin
Log.d("MyTag", "User action")  // NEVER
```

**DO:**
```kotlin
DebugConfig.debugLog(context, "RecipeScreen", "User action")
```

---

### ❌ Hardcoded Settings

**DON'T:**
```kotlin
fun filterRecipes(recipes: List<Recipe>) =
    recipes.filter { it.cuisine != "Mexican" }  // Hardcoded
```

**DO:**
```kotlin
fun filterRecipes(recipes: List<Recipe>): List<Recipe> {
    val excluded = cuisineSettings.excludedCuisines.value
    return recipes.filter { it.cuisine !in excluded }
}
```

---

### ❌ Duplicate Data Sources

**DON'T:**
```kotlin
// IngredientNormalizer.kt
object IngredientNormalizer {
    private val units = setOf("cup", "tbsp", "tsp")
}

// RecipeScreen.kt
class RecipeScreen {
    private val units = setOf("cup", "tbsp", "tsp")  // DUPLICATE - BAD
}
```

**DO:**
```kotlin
// Single source of truth
object IngredientNormalizer {
    private val canonicalUnits = setOf("cup", "tbsp", "tsp")
    fun getUnits(): List<String> = canonicalUnits.sorted()
}

// Usage everywhere
class RecipeScreen {
    val units = IngredientNormalizer.getUnits()
}
```

---

### ❌ Direct Room Usage in UI

**DON'T:**
```kotlin
@Composable
fun RecipeScreen(database: AppDatabase) {
    val recipes by database.recipeDao().getAll()
        .collectAsState(initial = emptyList())
    // Direct DAO - BAD
}
```

**DO:**
```kotlin
@Composable
fun RecipeScreen(viewModel: RecipeViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    // ViewModel abstracts data access
}
```

---

## Development Workflow

### Before Starting Work

- [ ] Review relevant documentation
- [ ] Check existing patterns in codebase
- [ ] Identify affected Manager/Settings classes
- [ ] Plan architectural approach

### During Development

- [ ] Follow SSOT principle
- [ ] Use DebugConfig for logging
- [ ] Write tests for business logic
- [ ] Keep UI code thin
- [ ] Use StateFlow for observable state
- [ ] Follow Material 3 spacing
- [ ] Extract components proactively (>50 lines or self-contained)

### After Completing Work

- [ ] Run full test suite
- [ ] Manual testing of features
- [ ] Update documentation:
  - [ ] PROJECT_STATUS.md
  - [ ] DECISION_LOG.md
  - [ ] DEVELOPER_GUIDE.md
  - [ ] FILE_CATALOG.md
  - [ ] TEST_SCENARIOS.md
- [ ] Write clear commit message
- [ ] Create PR with summary

---

## Summary Checklist - New Project Setup

### Architecture
- [ ] Create `data/`, `ui/`, `utils/`, `navigation/` packages
- [ ] Set up Room database with DAOs
- [ ] Create AppSettings with SharedPreferences
- [ ] Implement DebugConfig utility

### Documentation
- [ ] Create PROJECT_STATUS.md
- [ ] Create DECISION_LOG.md
- [ ] Create DEVELOPER_GUIDE.md
- [ ] Create FILE_CATALOG.md
- [ ] Create TEST_SCENARIOS.md

### Code Standards
- [ ] Define spacing constants
- [ ] Set up Material 3 theme
- [ ] Establish Manager pattern
- [ ] Configure StateFlow usage
- [ ] Set up ViewModel pattern

### Testing
- [ ] Add JUnit dependencies
- [ ] Add Room in-memory database testing
- [ ] Set up Kotlin coroutines test
- [ ] Create first Manager test

### Git
- [ ] Configure commit message template
- [ ] Set up branch naming convention
- [ ] Create .gitignore

---

**Document Version:** 2.0
**Last Updated:** 2025-11-18
**Changes from 1.0:** Restructured with Quick Reference section at top, updated component extraction rule to be more proactive

**End of Document**
