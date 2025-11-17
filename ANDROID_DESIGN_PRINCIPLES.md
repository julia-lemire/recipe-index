# Android Design Principles & Standards

**Version:** 1.0
**Last Updated:** 2025-11-14

This document captures architectural patterns, coding standards, and best practices for building robust Android applications. These principles were refined through the development of the Refract audio player app.

---

## Table of Contents

1. [Core Architecture Principles](#core-architecture-principles)
2. [State Management](#state-management)
3. [UI/UX Standards](#uiux-standards)
4. [Code Organization](#code-organization)
5. [Debug Logging Standards](#debug-logging-standards)
6. [Documentation Requirements](#documentation-requirements)
7. [Common Anti-Patterns to Avoid](#common-anti-patterns-to-avoid)
8. [Testing Standards](#testing-standards)
9. [Git Commit Standards](#git-commit-standards)
10. [Development Workflow](#development-workflow)
11. [Performance Considerations](#performance-considerations)

---

## Core Architecture Principles

### 1. Single Source of Truth (SSOT)

**Principle:** Each piece of data should have exactly one authoritative source.

**Examples:**
- Genre normalization: `GenreNormalizer.kt` is the ONLY place canonical genre names are defined
- Settings: `AppSettings` and category-specific settings classes (e.g., `GenreSettings`) own their data
- Database entities: Room DAOs are the sole source for persisted data

**Implementation Pattern:**
```kotlin
// GOOD: Single source of truth
object GenreNormalizer {
    private val canonicalGenres = setOf("Rock", "Jazz", "Classical", ...)

    fun getCanonicalGenres(): List<String> = canonicalGenres.sorted()
    fun normalize(input: String): String { /* ... */ }
}

// BAD: Duplicated genre lists in multiple files
class FilterScreen {
    private val genres = listOf("Rock", "Jazz", ...) // DON'T DO THIS
}
```

**Benefits:**
- No sync issues between duplicate data sources
- Clear ownership of data
- Easier to maintain and update

---

### 2. Config Over Code

**Principle:** User preferences and behavioral settings belong in settings files, not hardcoded in business logic.

**Examples:**
- Genre filter preferences → `GenreSettings.kt`
- Genre exclusion preferences → `GenreExclusionSettings.kt`
- App-wide preferences → `AppSettings.kt`

**Implementation Pattern:**
```kotlin
// GOOD: Configurable behavior
class GenreSettings(private val context: Context) {
    private val _enabledGenres = MutableStateFlow(getDefaultEnabledGenres())
    val enabledGenres: StateFlow<Set<String>> = _enabledGenres.asStateFlow()

    fun updateEnabledGenres(genres: Set<String>) {
        _enabledGenres.value = genres
        saveToPreferences()
    }
}

// Usage in business logic
class PlaylistManager {
    fun filterByGenres(tracks: List<Track>): List<Track> {
        val enabledGenres = genreSettings.enabledGenres.value
        return tracks.filter { it.genre in enabledGenres }
    }
}

// BAD: Hardcoded behavior
class PlaylistManager {
    fun filterByGenres(tracks: List<Track>): List<Track> {
        return tracks.filter { it.genre != "Christmas" } // DON'T DO THIS
    }
}
```

**Benefits:**
- User preferences are respected
- Easy to add settings UI
- Behavior can change without code changes

---

### 3. Manager Pattern

**Principle:** Complex operations involving multiple data sources or business logic belong in dedicated Manager classes in the `data/` package, NOT in UI code or ViewModels.

**File Location:** `data/` package (e.g., `data/ContentManagers/`, `data/Playback/`)

**Examples:**
- `AudioFileManager`: File scanning, metadata extraction
- `PlaylistManager`: Playlist creation, smart playlists
- `FileImportManager`: Import/export operations
- `GenreNormalizer`: Genre standardization (object pattern)

**Implementation Pattern:**
```kotlin
// GOOD: Manager in data/ package
package com.refract.app.data.ContentManagers

class PlaylistManager(private val db: AppDatabase, private val context: Context) {
    suspend fun createSmartPlaylist(
        name: String,
        bpmRange: IntRange,
        genres: Set<String>,
        excludeGenres: Set<String>
    ): Playlist = withContext(Dispatchers.IO) {
        // Complex business logic here
        val tracks = db.trackDao().getAll()
        val filtered = tracks.filter { /* complex filtering */ }

        val playlist = Playlist(name = name, trackCount = filtered.size)
        val playlistId = db.playlistDao().insert(playlist)

        // Insert cross-references
        filtered.forEach { track ->
            db.playlistTrackDao().insert(PlaylistTrack(playlistId, track.id))
        }

        playlist.copy(id = playlistId)
    }
}

// BAD: Business logic in ViewModel
class PlaylistViewModel : ViewModel() {
    fun createPlaylist() {
        viewModelScope.launch {
            // DON'T put complex business logic here
            val tracks = db.trackDao().getAll() // Direct DB access - BAD
            val filtered = tracks.filter { /* ... */ }
            // This belongs in a Manager!
        }
    }
}
```

**Guidelines:**
- **Managers** handle: Business logic, data transformation, multi-step operations
- **ViewModels** handle: UI state, user events, calling managers, exposing StateFlow
- **Repositories** handle: Simple CRUD operations, direct DAO wrappers

---

### 4. Unified Entity Pattern

**Principle:** Instead of creating separate entity classes for similar concepts, use a single data class with behavioral flags.

**Example:**
```kotlin
// GOOD: Unified entity with behavioral flags
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val trackCount: Int = 0,
    val isSmartPlaylist: Boolean = false,  // Behavioral flag
    val smartCriteria: String? = null,     // Only used if isSmartPlaylist=true
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// BAD: Separate entities for same concept
@Entity(tableName = "playlists")
data class Playlist(...)

@Entity(tableName = "smart_playlists")
data class SmartPlaylist(...)  // Duplicates most fields from Playlist
```

**Benefits:**
- Single DAO handles all variants
- Easier to convert between types
- Simpler database schema
- UI code doesn't need to handle multiple types

---

### 5. Repository Pattern (Simple CRUD Only)

**Principle:** Repositories are thin wrappers around DAOs for simple CRUD operations. Complex logic goes in Managers.

**Implementation Pattern:**
```kotlin
// GOOD: Simple repository
class PlaylistRepository(private val playlistDao: PlaylistDao) {
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun getPlaylistById(id: Long): Playlist? = playlistDao.getById(id)

    suspend fun insertPlaylist(playlist: Playlist): Long = playlistDao.insert(playlist)

    suspend fun deletePlaylist(playlist: Playlist) = playlistDao.delete(playlist)
}

// Complex operations go in Manager
class PlaylistManager(
    private val repository: PlaylistRepository,
    private val trackRepository: TrackRepository
) {
    suspend fun createSmartPlaylist(criteria: SmartPlaylistCriteria): Playlist {
        // Complex multi-step logic here
    }
}
```

---

## State Management

### 1. StateFlow for Observable State

**Principle:** Use `StateFlow` for all observable state in settings and managers. Never use `LiveData` in new code.

**Pattern:**
```kotlin
class GenreSettings(private val context: Context) {
    private val _enabledGenres = MutableStateFlow(getDefaultEnabledGenres())
    val enabledGenres: StateFlow<Set<String>> = _enabledGenres.asStateFlow()

    fun updateEnabledGenres(genres: Set<String>) {
        _enabledGenres.value = genres
        saveToPreferences()
    }
}

// UI consumption in Compose
@Composable
fun GenreFilterScreen(genreSettings: GenreSettings) {
    val enabledGenres by genreSettings.enabledGenres.collectAsState()

    // Use enabledGenres in UI
}
```

**Benefits:**
- Type-safe, null-safe by default
- Better Kotlin coroutine integration
- Works seamlessly with Compose `collectAsState()`

---

### 2. ViewModel State Pattern

**Principle:** ViewModels expose UI state via `StateFlow` and handle events via functions.

**Pattern:**
```kotlin
data class PlaylistScreenState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPlaylist: Playlist? = null
)

class PlaylistViewModel(
    private val playlistManager: PlaylistManager
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
                playlistManager.getPlaylists().collect { playlists ->
                    _state.update { it.copy(playlists = playlists, isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistManager.createPlaylist(name)
        }
    }
}
```

---

### 3. Compose State Rules

**Guidelines:**
- Use `remember { mutableStateOf() }` for local UI state (e.g., text field, expanded/collapsed)
- Use `collectAsState()` for StateFlow from ViewModel or settings
- Use `derivedStateOf` for computed values based on other state
- Hoist state when multiple components need to share it

**Example:**
```kotlin
@Composable
fun FilterSection(
    enabledGenres: Set<String>,  // Hoisted state
    onGenresChange: (Set<String>) -> Unit  // Callback
) {
    var expanded by remember { mutableStateOf(false) }  // Local UI state

    Card(
        modifier = Modifier.clickable { expanded = !expanded }
    ) {
        // UI implementation
    }
}
```

---

## UI/UX Standards

### 1. Material 3 Spacing Constants

**Principle:** Use consistent spacing throughout the app. Define constants in a utility file.

**Implementation:**
```kotlin
// SpacingConstants.kt
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

**Standard Spacings:**
- `4.dp`: Between related text elements, icon padding
- `8.dp`: Between list items in dense layouts, card content padding
- `16.dp`: Default padding for screens, between sections
- `24.dp`: Between major UI sections
- `32.dp`: Top/bottom screen padding for prominent sections

---

### 2. Detail Screen Pattern

**Principle:** Detail screens (playlist detail, track detail) should use `WindowInsets(0, 0, 0, 0)` to allow full-screen scrolling with content padding.

**Pattern:**
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlist Name") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)  // IMPORTANT
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

**Why:** Prevents double padding and allows content to scroll naturally.

---

### 3. Card Consistency

**Principle:** Use consistent card styling throughout the app.

**Standard Card Pattern:**
```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Card content
    }
}
```

**Clickable Card Pattern:**
```kotlin
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

### 4. Collapsible Section Pattern

**Principle:** For long settings or filter screens, use collapsible sections to reduce scrolling.

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
            // Header row (clickable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            // Expandable content
            if (expanded) {
                content()
            }
        }
    }
}
```

---

### 5. Icon Button Guidelines

**Principle:** Use inline icon buttons for secondary actions. Reserve full buttons for primary actions.

**Examples:**
```kotlin
// GOOD: Inline icon for reset
Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Text("Genre Exclusions", style = MaterialTheme.typography.titleMedium)
    IconButton(onClick = { /* reset */ }) {
        Icon(Icons.Default.Refresh, contentDescription = "Reset to defaults")
    }
}

// BAD: Large button taking up space
OutlinedButton(
    onClick = { /* reset */ },
    modifier = Modifier.fillMaxWidth()
) {
    Text("Reset to Defaults")
}
```

---

## Code Organization

### 1. Package Structure

**Standard Structure:**
```
app/src/main/java/com/yourapp/
├── data/
│   ├── ContentManagers/       # Business logic managers
│   ├── Database/              # Room entities, DAOs, database
│   ├── Playback/             # Playback-related managers
│   └── Settings/             # Settings classes
├── ui/
│   ├── screens/              # Full-screen composables
│   ├── components/           # Reusable UI components
│   └── theme/                # Material theme configuration
├── utils/
│   ├── DebugConfig.kt        # Debug logging utility
│   ├── PermissionUtils.kt    # Permission helpers
│   └── FileUtils.kt          # File operation helpers
├── navigation/
│   └── NavGraph.kt           # Navigation setup
└── MainActivity.kt
```

**Rules:**
- **Business logic** → `data/ContentManagers/` or `data/Playback/`
- **Database code** → `data/Database/`
- **Settings classes** → `data/Settings/`
- **Screen composables** → `ui/screens/`
- **Reusable components** → `ui/components/` (only if used 2+ times)
- **Utilities** → `utils/`

---

### 2. Component Extraction Rule

**Principle:** Extract a composable to `ui/components/` ONLY when it's used in 2 or more places.

**Why:** Avoid premature abstraction. Keep code colocated until reuse is needed.

**Example:**
```kotlin
// Used in 1 place: Keep inline in screen file
@Composable
fun PlaylistScreen() {
    LazyColumn {
        items(playlists) { playlist ->
            // Inline composable - fine for now
            Card { /* ... */ }
        }
    }
}

// Used in 2+ places: Extract to components/
// ui/components/PlaylistCard.kt
@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Card(onClick = onClick) { /* ... */ }
}
```

---

### 3. File Naming Conventions

- **Screens:** `[Feature]Screen.kt` (e.g., `PlaylistScreen.kt`, `SettingsScreen.kt`)
- **Components:** `[Component].kt` (e.g., `TrackListItem.kt`, `GenreChip.kt`)
- **Managers:** `[Domain]Manager.kt` (e.g., `PlaylistManager.kt`, `AudioFileManager.kt`)
- **Settings:** `[Category]Settings.kt` (e.g., `GenreSettings.kt`, `AppSettings.kt`)
- **DAOs:** `[Entity]Dao.kt` (e.g., `TrackDao.kt`, `PlaylistDao.kt`)
- **Repositories:** `[Entity]Repository.kt` (e.g., `TrackRepository.kt`)

---

## Debug Logging Standards

### CRITICAL RULE: NEVER Use `android.util.Log` Directly

**Principle:** ALL debug logging MUST go through a category-based debug system that can be toggled on/off.

**Why:**
- Production apps should not log sensitive data
- Debug logs create performance overhead
- Users don't want log spam
- Developers need targeted logging during development

---

### Implementation: DebugConfig System

**File:** `utils/DebugConfig.kt`

```kotlin
package com.refract.app.utils

import android.content.Context
import android.util.Log
import com.refract.app.data.Settings.AppSettings
import kotlinx.coroutines.runBlocking

/**
 * DebugConfig - Centralized debug logging system
 *
 * IMPORTANT: NEVER use android.util.Log directly in the codebase.
 * Always use DebugConfig.debugLog() which respects user's debug mode setting.
 */
object DebugConfig {

    /**
     * Category-based debug logging
     *
     * @param context Application context
     * @param category Log category (e.g., "PlaylistManager", "AudioScanner")
     * @param message Log message
     */
    fun debugLog(context: Context, category: String, message: String) {
        val appSettings = AppSettings.getInstance(context)
        val debugMode = runBlocking {
            appSettings.getSettings().debugMode
        }

        if (debugMode) {
            Log.d("Refract-$category", message)
        }
    }
}
```

---

### Usage Pattern

```kotlin
// GOOD: Using DebugConfig
class PlaylistManager(private val context: Context, private val db: AppDatabase) {

    suspend fun createPlaylist(name: String): Playlist {
        DebugConfig.debugLog(context, "PlaylistManager", "Creating playlist: $name")

        val playlist = Playlist(name = name)
        val id = db.playlistDao().insert(playlist)

        DebugConfig.debugLog(context, "PlaylistManager", "Playlist created with ID: $id")

        return playlist.copy(id = id)
    }
}

// BAD: Direct Log usage
class PlaylistManager {
    suspend fun createPlaylist(name: String): Playlist {
        Log.d("PlaylistManager", "Creating playlist: $name")  // DON'T DO THIS
        // ...
    }
}
```

---

### Category Guidelines

Use descriptive categories that reflect the class or feature:

- `"MainActivity"` - Main app lifecycle events
- `"PlaylistManager"` - Playlist operations
- `"AudioScanner"` - File scanning operations
- `"GenreNormalizer"` - Genre normalization
- `"PermissionHandler"` - Permission requests
- `"DatabaseMigration"` - Database migrations

**Benefits:**
- Easy to filter logs by category: `adb logcat | grep "Refract-PlaylistManager"`
- Users can disable debug mode in production
- No sensitive data leaks in release builds
- Developers can trace operations through categories

---

## Documentation Requirements

### The 5-Document System

**Principle:** Every project MUST maintain these 5 core documentation files in the root directory.

---

### 1. PROJECT_STATUS.md

**Purpose:** High-level project overview and current state

**Structure:**
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

**Update When:**
- Completing major features
- Starting new work phases
- Releasing versions
- Discovering critical bugs

---

### 2. DECISION_LOG.md

**Purpose:** Record architectural decisions and their rationale

**Structure:**
```markdown
# Decision Log

## [YYYY-MM-DD] Decision Title

**Context:**
[Why this decision was needed]

**Decision:**
[What was decided]

**Rationale:**
[Why this approach was chosen over alternatives]

**Consequences:**
[Impact on codebase, performance, or future development]

**Alternatives Considered:**
- Alternative 1: [Why not chosen]
- Alternative 2: [Why not chosen]
```

**Example:**
```markdown
## [2025-11-10] Use Unified Playlist Entity Instead of Separate Tables

**Context:**
Need to support both manual playlists and smart playlists with genre/BPM filtering.

**Decision:**
Use single Playlist entity with `isSmartPlaylist` flag and `smartCriteria` JSON field.

**Rationale:**
- Simpler database schema (1 table instead of 2)
- Easier to convert between types
- Single DAO handles all playlist operations
- UI code doesn't need to handle multiple types

**Consequences:**
- `smartCriteria` field is nullable and only used when `isSmartPlaylist=true`
- Need to serialize/deserialize criteria JSON
- Slight increase in unused field storage for manual playlists

**Alternatives Considered:**
- Separate SmartPlaylist entity: Rejected due to code duplication
- Inheritance with Room: Rejected due to Room's limited inheritance support
```

**Update When:**
- Making architectural decisions
- Choosing libraries or frameworks
- Changing major patterns
- Refactoring significant code sections

---

### 3. DEVELOPER_GUIDE.md

**Purpose:** Onboarding guide for new developers + coding standards reference

**Structure:**
```markdown
# Developer Guide

## Getting Started
### Prerequisites
- Android Studio [version]
- JDK [version]
- Minimum SDK: [version]

### Setup
1. Clone repository
2. Open in Android Studio
3. Sync Gradle
4. Run app

## Project Architecture
[Brief overview of architecture patterns]

## Code Standards
### Logging
- NEVER use `android.util.Log` directly
- Use `DebugConfig.debugLog(context, category, message)`

### Naming Conventions
[List conventions]

### Package Structure
[Describe organization]

## Testing
[How to run tests, testing patterns]

## Common Tasks
### Adding a New Screen
[Step-by-step guide]

### Creating a Manager
[Step-by-step guide]
```

**Update When:**
- Adding new patterns or utilities
- Changing build configuration
- Adding new dependencies
- Establishing new coding conventions

---

### 4. FILE_CATALOG.md

**Purpose:** Directory of important files with descriptions and purposes

**Structure:**
```markdown
# File Catalog

## Core Application Files

### MainActivity.kt
**Path:** `app/src/main/java/com/yourapp/MainActivity.kt`
**Purpose:** Main entry point, handles permissions, navigation setup
**Key Responsibilities:**
- File picker launchers
- Permission requests
- Scaffold and navigation host

## Data Layer

### PlaylistManager.kt
**Path:** `app/src/main/java/com/yourapp/data/ContentManagers/PlaylistManager.kt`
**Purpose:** Business logic for playlist operations
**Key Functions:**
- `createSmartPlaylist()`: Creates filtered playlists
- `updatePlaylist()`: Updates playlist metadata

[Continue for all major files...]
```

**Update When:**
- Adding new major files
- Changing file responsibilities
- Refactoring file locations

---

### 5. TEST_SCENARIOS.md

**Purpose:** Document test coverage and manual testing scenarios

**Structure:**
```markdown
# Test Scenarios

## Automated Tests

### PlaylistManagerTest
**File:** `test/java/com/yourapp/PlaylistManagerTest.kt`
**Coverage:**
- ✅ Create playlist
- ✅ Add tracks to playlist
- ✅ Smart playlist filtering

## Manual Test Scenarios

### First-Time Setup Flow
**Steps:**
1. Install app (clean install)
2. Grant storage permission
3. Select music folder
4. Complete setup wizard

**Expected:**
- Setup wizard appears
- All steps can be completed
- Settings are saved
- Main screen appears after completion

### Smart Playlist Creation
**Steps:**
1. Navigate to Playlists screen
2. Tap "Create Smart Playlist"
3. Set BPM range: 120-140
4. Enable genres: Rock, Electronic
5. Exclude genres: Christmas
6. Save playlist

**Expected:**
- Only tracks matching criteria appear
- Playlist auto-updates when new tracks scanned
```

**Update When:**
- Adding automated tests
- Fixing bugs (add regression test scenario)
- Adding new features (add test scenarios)
- Discovering edge cases

---

### Documentation Update Rules

**IMPORTANT:** When updating any documentation file, consider whether the other 4 files also need updates.

**Example:**
- Adding a new Manager class:
  - ✅ Update FILE_CATALOG.md (add new file entry)
  - ✅ Update DEVELOPER_GUIDE.md (if it introduces new patterns)
  - ✅ Update TEST_SCENARIOS.md (add test coverage)
  - ✅ Update DECISION_LOG.md (if architectural decision was made)
  - ✅ Update PROJECT_STATUS.md (if completing a feature)

**Checklist Template:**
```
When making changes, update relevant docs:
- [ ] PROJECT_STATUS.md - If feature state changed
- [ ] DECISION_LOG.md - If architectural decision made
- [ ] DEVELOPER_GUIDE.md - If new pattern/standard introduced
- [ ] FILE_CATALOG.md - If new major file added
- [ ] TEST_SCENARIOS.md - If new tests added
```

---

## Common Anti-Patterns to Avoid

### 1. ❌ Business Logic in ViewModels

**DON'T:**
```kotlin
class PlaylistViewModel : ViewModel() {
    fun createSmartPlaylist() {
        viewModelScope.launch {
            val tracks = db.trackDao().getAll()  // Direct DB access
            val filtered = tracks.filter {
                it.bpm in 120..140 && it.genre == "Rock"
            }
            val playlist = Playlist(name = "My Playlist")
            db.playlistDao().insert(playlist)
            // Complex logic in ViewModel - BAD
        }
    }
}
```

**DO:**
```kotlin
class PlaylistViewModel(
    private val playlistManager: PlaylistManager
) : ViewModel() {
    fun createSmartPlaylist(criteria: SmartPlaylistCriteria) {
        viewModelScope.launch {
            playlistManager.createSmartPlaylist(criteria)
        }
    }
}
```

---

### 2. ❌ Using `Log.d()` Directly

**DON'T:**
```kotlin
Log.d("MyTag", "User clicked button")  // NEVER DO THIS
```

**DO:**
```kotlin
DebugConfig.debugLog(context, "ButtonHandler", "User clicked button")
```

---

### 3. ❌ Hardcoded Settings in Business Logic

**DON'T:**
```kotlin
fun filterTracks(tracks: List<Track>): List<Track> {
    return tracks.filter { it.genre != "Christmas" }  // Hardcoded
}
```

**DO:**
```kotlin
fun filterTracks(tracks: List<Track>): List<Track> {
    val excludedGenres = genreExclusionSettings.defaultExclusions.value
    return tracks.filter { it.genre !in excludedGenres }
}
```

---

### 4. ❌ Premature Component Extraction

**DON'T:**
```kotlin
// Used only once, but extracted anyway
// ui/components/PlaylistHeaderSection.kt
@Composable
fun PlaylistHeaderSection(name: String) { /* ... */ }
```

**DO:**
```kotlin
// Keep inline until used 2+ times
@Composable
fun PlaylistScreen(playlist: Playlist) {
    Column {
        // Inline header section - fine for now
        Text(playlist.name, style = MaterialTheme.typography.headlineMedium)
    }
}
```

---

### 5. ❌ Duplicate Data Sources

**DON'T:**
```kotlin
// GenreNormalizer.kt
object GenreNormalizer {
    private val genres = setOf("Rock", "Jazz", "Classical")
}

// GenreFilterScreen.kt
class GenreFilterScreen {
    private val genres = setOf("Rock", "Jazz", "Classical")  // DUPLICATE - BAD
}
```

**DO:**
```kotlin
// Single source of truth
object GenreNormalizer {
    private val canonicalGenres = setOf("Rock", "Jazz", "Classical")
    fun getCanonicalGenres(): List<String> = canonicalGenres.sorted()
}

// Usage everywhere
class GenreFilterScreen {
    val genres = GenreNormalizer.getCanonicalGenres()
}
```

---

### 6. ❌ Direct Room Usage in UI

**DON'T:**
```kotlin
@Composable
fun PlaylistScreen(database: AppDatabase) {
    val playlists by database.playlistDao().getAllPlaylists()
        .collectAsState(initial = emptyList())
    // Direct DAO access from UI - BAD
}
```

**DO:**
```kotlin
@Composable
fun PlaylistScreen(viewModel: PlaylistViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    // ViewModel abstracts data access
}
```

---

## Testing Standards

### 1. Testing Framework

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

### 2. Test Organization

**Structure:**
```
app/src/test/java/com/yourapp/
├── data/
│   ├── managers/
│   │   └── PlaylistManagerTest.kt
│   ├── database/
│   │   └── PlaylistDaoTest.kt
│   └── normalizers/
│       └── GenreNormalizerTest.kt
└── utils/
    └── FileUtilsTest.kt
```

**Naming:** `[ClassName]Test.kt`

---

### 3. Test Pattern for Managers

```kotlin
@RunWith(RobolectricTestRunner::class)
class PlaylistManagerTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context
    private lateinit var playlistManager: PlaylistManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        playlistManager = PlaylistManager(database, context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `createPlaylist creates playlist in database`() = runBlocking {
        val name = "Test Playlist"

        val playlist = playlistManager.createPlaylist(name)

        assertNotNull(playlist.id)
        assertEquals(name, playlist.name)

        val retrieved = database.playlistDao().getById(playlist.id)
        assertEquals(name, retrieved?.name)
    }
}
```

---

### 4. Test Coverage Goals

**Priority:**
- ✅ **Critical:** Business logic in Managers (80%+ coverage)
- ✅ **High:** Data layer (DAOs, Repositories) (70%+ coverage)
- ✅ **Medium:** Utilities and helpers (60%+ coverage)
- ⚠️ **Low:** UI composables (manual testing acceptable)

---

### 5. Manual Testing Checklist

**Before Each Release:**
- [ ] Fresh install flow (setup wizard)
- [ ] Permission grant/deny scenarios
- [ ] Core feature walkthrough
- [ ] Edge cases (empty states, no data)
- [ ] Performance (large datasets)
- [ ] Rotation and configuration changes

---

## Git Commit Standards

### 1. Commit Message Format

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
feat: Add smart playlist creation with BPM and genre filtering

Implemented PlaylistManager.createSmartPlaylist() with support for:
- BPM range filtering
- Genre inclusion/exclusion
- Auto-updating playlist criteria

Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

```
fix: Prevent duplicate genres in filter settings

GenreSettings now deduplicates enabled genres on load to prevent
UI rendering issues.

Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

---

### 2. Commit Size Guidelines

**Good Commit:**
- Single logical change
- Includes related tests
- Updates relevant documentation
- Builds successfully

**Too Large:**
- Multiple features in one commit
- Mixing refactoring with new features

**Too Small:**
- Separate commits for whitespace changes
- Breaking single feature across 10 commits

---

### 3. Branch Naming

**Pattern:** `<type>/<short-description>`

**Examples:**
- `feat/smart-playlists`
- `fix/genre-filter-crash`
- `refactor/playlist-manager`
- `docs/update-decision-log`

---

## Development Workflow

### Before Starting Work

- [ ] Review relevant documentation (DEVELOPER_GUIDE.md, DECISION_LOG.md)
- [ ] Check existing patterns in codebase
- [ ] Identify which Manager or settings class is affected
- [ ] Plan architectural approach (discuss if major)

---

### During Development

- [ ] Follow Single Source of Truth principle
- [ ] Use DebugConfig for all logging
- [ ] Write unit tests for business logic
- [ ] Keep UI code thin (delegate to ViewModels/Managers)
- [ ] Use StateFlow for observable state
- [ ] Follow Material 3 spacing constants
- [ ] Extract components only when used 2+ times

---

### After Completing Work

- [ ] Run full test suite
- [ ] Manual testing of affected features
- [ ] Update all relevant documentation files:
  - [ ] PROJECT_STATUS.md (if feature state changed)
  - [ ] DECISION_LOG.md (if architectural decision made)
  - [ ] DEVELOPER_GUIDE.md (if new pattern introduced)
  - [ ] FILE_CATALOG.md (if new major file added)
  - [ ] TEST_SCENARIOS.md (if new tests added)
- [ ] Write clear commit message
- [ ] Create PR with summary and test plan

---

## Performance Considerations

### 1. Database Operations

**Always use IO dispatcher:**
```kotlin
suspend fun loadPlaylists(): List<Playlist> = withContext(Dispatchers.IO) {
    database.playlistDao().getAll()
}
```

**Use Flow for reactive queries:**
```kotlin
fun observePlaylists(): Flow<List<Playlist>> {
    return database.playlistDao().getAllPlaylists()
}
```

---

### 2. File Scanning

**Use background threads:**
```kotlin
class AudioFileManager {
    suspend fun scanDirectory(path: String) = withContext(Dispatchers.IO) {
        // File operations here
    }
}
```

**Batch database inserts:**
```kotlin
@Transaction
suspend fun insertTracks(tracks: List<Track>) {
    tracks.forEach { trackDao.insert(it) }
}
```

---

### 3. UI Performance

**LazyColumn for long lists:**
```kotlin
LazyColumn {
    items(tracks, key = { it.id }) { track ->
        TrackListItem(track)
    }
}
```

**Avoid expensive operations in composition:**
```kotlin
// BAD
@Composable
fun Screen() {
    val filtered = tracks.filter { /* expensive */ }  // Runs every recomposition
}

// GOOD
@Composable
fun Screen() {
    val filtered = remember(tracks) {
        tracks.filter { /* expensive */ }
    }
}
```

---

## Summary Checklist

When starting a new Android project, use this as your setup checklist:

### Architecture Setup
- [ ] Create `data/`, `ui/`, `utils/`, `navigation/` packages
- [ ] Set up Room database with DAOs
- [ ] Create AppSettings class with SharedPreferences
- [ ] Implement DebugConfig utility

### Documentation Setup
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

### Testing Setup
- [ ] Add JUnit dependencies
- [ ] Add Room in-memory database testing
- [ ] Set up Kotlin coroutines test
- [ ] Create first Manager test

### Git Setup
- [ ] Configure commit message template
- [ ] Set up branch naming convention
- [ ] Create .gitignore

---

## Document Version History

| Version | Date       | Changes                                      |
|---------|------------|----------------------------------------------|
| 1.0     | 2025-11-14 | Initial document based on another project    |

---

**End of Document**
