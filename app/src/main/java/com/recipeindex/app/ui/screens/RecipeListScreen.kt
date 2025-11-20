package com.recipeindex.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.ui.components.GroceryListPickerDialog
import com.recipeindex.app.ui.viewmodels.GroceryListViewModel
import com.recipeindex.app.ui.viewmodels.RecipeViewModel
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.ShareHelper
import com.recipeindex.app.utils.filtersort.recipe.*
import com.recipeindex.app.utils.filtersort.ui.FilterBottomSheet
import com.recipeindex.app.utils.filtersort.ui.SortMenu

/**
 * Recipe Index Screen - Browse and search recipes
 *
 * Features FAB for adding new recipes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeViewModel,
    groceryListViewModel: GroceryListViewModel,
    mealPlanViewModel: com.recipeindex.app.ui.viewmodels.MealPlanViewModel,
    onAddRecipe: () -> Unit,
    onImportRecipe: () -> Unit = {},
    onRecipeClick: (Long) -> Unit,
    onMenuClick: () -> Unit = {}
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "RecipeListScreen composed")

    val recipes by viewModel.filterSortManager.filteredItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val groceryLists by groceryListViewModel.groceryLists.collectAsState()
    val mealPlans by mealPlanViewModel.mealPlans.collectAsState()
    var showListPicker by remember { mutableStateOf(false) }
    var recipeForGroceryList by remember { mutableStateOf<Recipe?>(null) }
    var showMealPlanPicker by remember { mutableStateOf(false) }
    var recipeForMealPlan by remember { mutableStateOf<Recipe?>(null) }
    var showSearchBar by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Available filters and sorts
    val availableFilters = remember {
        listOf(
            FavoriteFilter(favoritesOnly = true),
            HasPhotoFilter(),
            CookTimeFilter(maxMinutes = 30),
            CookTimeFilter(maxMinutes = 60)
        )
    }
    val availableSorts = remember {
        listOf(
            TitleSort(),
            DateCreatedSort(),
            CookTimeSort(),
            ServingsSort(),
            FavoriteSort()
        )
    }
    val activeFilterIds by viewModel.filterSortManager.activeFilterIds.collectAsState()
    val currentSort by viewModel.filterSortManager.currentSort.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    val filterSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true  // Always open to full height
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Index") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = if (activeFilterIds.isNotEmpty()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                LocalContentColor.current
                            }
                        )
                    }
                    SortMenu(
                        availableSorts = availableSorts,
                        currentSort = currentSort,
                        onSortSelected = { viewModel.setSort(it) },
                        onSortDirectionToggle = { viewModel.toggleSortDirection() }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Add Recipe FAB clicked")
                    onImportRecipe()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Recipe"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            if (showSearchBar) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchRecipes(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search recipes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchRecipes("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
            }

            // Check orientation for layout choice
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    recipes.isEmpty() -> {
                        EmptyState(modifier = Modifier.align(Alignment.Center))
                    }
                    isLandscape -> {
                    // Grid layout for landscape (2 columns)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recipes, key = { it.id }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) },
                                onAddToGroceryList = {
                                    recipeForGroceryList = recipe
                                    showListPicker = true
                                },
                                onAddToMealPlan = {
                                    recipeForMealPlan = recipe
                                    showMealPlanPicker = true
                                },
                                onShare = {
                                    ShareHelper.shareRecipe(context, recipe, recipe.photoPath)
                                },
                                onDelete = {
                                    viewModel.deleteRecipe(recipe.id) {}
                                }
                            )
                        }
                    }
                }
                else -> {
                    // Column layout for portrait
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recipes, key = { it.id }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) },
                                onAddToGroceryList = {
                                    recipeForGroceryList = recipe
                                    showListPicker = true
                                },
                                onAddToMealPlan = {
                                    recipeForMealPlan = recipe
                                    showMealPlanPicker = true
                                },
                                onShare = {
                                    ShareHelper.shareRecipe(context, recipe, recipe.photoPath)
                                },
                                onDelete = {
                                    viewModel.deleteRecipe(recipe.id) {}
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    }

    // Grocery list picker dialog
    if (showListPicker && recipeForGroceryList != null) {
        GroceryListPickerDialog(
            availableLists = groceryLists,
            onDismiss = { showListPicker = false },
            onListSelected = { listId ->
                groceryListViewModel.addRecipesToList(listId, listOf(recipeForGroceryList!!.id))
                showListPicker = false
                recipeForGroceryList = null
            },
            onCreateNew = { listName ->
                val recipeId = recipeForGroceryList!!.id  // Capture before clearing
                groceryListViewModel.createList(listName) { listId ->
                    groceryListViewModel.addRecipesToList(listId, listOf(recipeId))
                }
                showListPicker = false
                recipeForGroceryList = null
            }
        )
    }

    // Meal plan picker dialog
    if (showMealPlanPicker && recipeForMealPlan != null) {
        com.recipeindex.app.ui.components.MealPlanPickerDialog(
            availablePlans = mealPlans,
            onDismiss = { showMealPlanPicker = false },
            onPlanSelected = { planId ->
                mealPlanViewModel.addRecipeToPlan(planId, recipeForMealPlan!!.id)
                showMealPlanPicker = false
                recipeForMealPlan = null
            },
            onCreateNew = { planName ->
                // Create a new meal plan with just this recipe
                val newPlan = com.recipeindex.app.data.entities.MealPlan(
                    name = planName,
                    recipeIds = listOf(recipeForMealPlan!!.id),
                    startDate = null,
                    endDate = null
                )
                mealPlanViewModel.createMealPlan(newPlan) { _ ->
                    showMealPlanPicker = false
                    recipeForMealPlan = null
                }
            }
        )
    }

    // Filter bottom sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            availableFilters = availableFilters,
            activeFilterIds = activeFilterIds,
            onFilterToggle = { viewModel.toggleFilter(it) },
            onClearAll = { viewModel.clearFilters() },
            onDismiss = { showFilterSheet = false },
            sheetState = filterSheetState
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "No recipes yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Tap + to add your first recipe",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToGroceryList: () -> Unit,
    onAddToMealPlan: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Recipe Photo (reduced from 180dp to 140dp)
            recipe.photoPath?.let { photoUrl ->
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Recipe photo for ${recipe.title}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Card Content (reduced padding from 16dp to 12dp)
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Row {
                        // Show favorite icon only if favorited
                        if (recipe.isFavorite) {
                            IconButton(onClick = onToggleFavorite) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Unfavorite",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // Add to meal plan button
                        IconButton(onClick = onAddToMealPlan) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Add to Meal Plan",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Context menu button
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options"
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Add to Grocery List") },
                                    onClick = {
                                        showMenu = false
                                        onAddToGroceryList()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share") },
                                    onClick = {
                                        showMenu = false
                                        onShare()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                    }
                                )
                                if (!recipe.isFavorite) {
                                    DropdownMenuItem(
                                        text = { Text("Mark as Favorite") },
                                        onClick = {
                                            showMenu = false
                                            onToggleFavorite()
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Favorite, contentDescription = null)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Info row: Servings, Prep, Cook
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${recipe.servings} servings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    recipe.prepTimeMinutes?.let {
                        Text(
                            text = "$it min prep",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    recipe.cookTimeMinutes?.let {
                        Text(
                            text = "$it min cook",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tags with wrapping (using custom FlowRow) - limit to 3 tags
                if (recipe.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalSpacing = 6.dp,
                        verticalSpacing = 4.dp
                    ) {
                        prioritizeAndLimitTags(recipe.tags, maxTags = 3).forEach { tag ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom FlowRow implementation for wrapping tags
 * Tags are only as wide as their content, wrapping to next line when needed
 */
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hSpacing = horizontalSpacing.roundToPx()
        val vSpacing = verticalSpacing.roundToPx()

        // Measure each child with no minimum width constraint - only as wide as needed
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0))
        }

        // Build rows
        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        placeables.forEach { placeable ->
            val itemWidth = placeable.width + hSpacing

            // Check if adding this item would exceed the max width
            if (currentRowWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }

            currentRow.add(placeable)
            currentRowWidth += itemWidth
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        // Calculate total height
        val height = rows.mapIndexed { index, row ->
            row.maxOf { it.height } + if (index < rows.size - 1) vSpacing else 0
        }.sum()

        layout(constraints.maxWidth, height) {
            var yPosition = 0
            rows.forEach { row ->
                var xPosition = 0
                val rowHeight = row.maxOf { it.height }

                row.forEach { placeable ->
                    placeable.placeRelative(xPosition, yPosition)
                    xPosition += placeable.width + hSpacing
                }

                yPosition += rowHeight + vSpacing
            }
        }
    }
}

/**
 * Prioritize and limit tags for display on recipe cards
 * Prioritizes: cook method, cuisine, ingredients over meal type
 */
private fun prioritizeAndLimitTags(tags: List<String>, maxTags: Int = 3): List<String> {
    if (tags.size <= maxTags) return tags

    // Define priority keywords for each category
    val cookMethodKeywords = listOf("baked", "grilled", "fried", "roasted", "steamed", "boiled", "sauteed", "stir-fry", "slow-cook", "instant pot", "air fryer")
    val cuisineKeywords = listOf("italian", "mexican", "chinese", "japanese", "thai", "indian", "french", "greek", "mediterranean", "american", "korean", "vietnamese")
    val ingredientKeywords = listOf("chicken", "beef", "pork", "fish", "seafood", "vegetarian", "vegan", "pasta", "rice", "potato", "tofu")
    val mealTypeKeywords = listOf("breakfast", "lunch", "dinner", "snack", "dessert", "appetizer")

    // Score each tag
    val scoredTags = tags.map { tag ->
        val lowerTag = tag.lowercase()
        val score = when {
            cookMethodKeywords.any { lowerTag.contains(it) } -> 3
            cuisineKeywords.any { lowerTag.contains(it) } -> 2
            ingredientKeywords.any { lowerTag.contains(it) } -> 1
            mealTypeKeywords.any { lowerTag.contains(it) } -> -1
            else -> 0
        }
        tag to score
    }

    // Sort by score (descending) and take top N
    return scoredTags.sortedByDescending { it.second }.take(maxTags).map { it.first }
}

