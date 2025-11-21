package com.recipeindex.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.ui.MainActivity
import com.recipeindex.app.ui.components.GroceryListPickerDialog
import com.recipeindex.app.ui.viewmodels.GroceryListViewModel
import com.recipeindex.app.ui.viewmodels.MealPlanViewModel
import com.recipeindex.app.ui.viewmodels.RecipeViewModel
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.ShareHelper
import com.recipeindex.app.utils.filtersort.mealplan.*
import com.recipeindex.app.utils.filtersort.ui.FilterBottomSheet
import com.recipeindex.app.utils.filtersort.ui.SortMenu
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Meal Planning Screen - Card-based meal plan list with search and filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanningScreen(
    mealPlanViewModel: MealPlanViewModel,
    recipeViewModel: RecipeViewModel,
    groceryListViewModel: GroceryListViewModel,
    onAddMealPlan: () -> Unit,
    onEditMealPlan: (Long) -> Unit,
    onMenuClick: () -> Unit = {}
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "MealPlanningScreen composed")

    val mealPlans by mealPlanViewModel.filterSortManager.filteredItems.collectAsState()
    val recipes by recipeViewModel.recipes.collectAsState()
    val isLoading by mealPlanViewModel.isLoading.collectAsState()
    val searchQuery by mealPlanViewModel.searchQuery.collectAsState()
    val groceryLists by groceryListViewModel.groceryLists.collectAsState()

    // Available filters and sorts
    val availableFilters = remember(recipes) {
        listOf(
            HasDatesFilter(),
            RecipeCountFilter(minRecipes = 3),
            RecipeCountFilter(minRecipes = 5),
            HasNotesFilter()
        )
    }
    val availableSorts = remember {
        listOf(
            StartDateSort(),
            NameSort(),
            DateCreatedSort(),
            RecipeCountSort()
        )
    }
    val activeFilterIds by mealPlanViewModel.filterSortManager.activeFilterIds.collectAsState()
    val currentSort by mealPlanViewModel.filterSortManager.currentSort.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var planToDelete by remember { mutableStateOf<MealPlan?>(null) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var planToDuplicate by remember { mutableStateOf<MealPlan?>(null) }
    var duplicateName by remember { mutableStateOf("") }
    var showListPicker by remember { mutableStateOf(false) }
    var planForGroceryList by remember { mutableStateOf<MealPlan?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val filterSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true  // Always open to full height
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // File picker for import
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val json = context.contentResolver.openInputStream(it)
                        ?.bufferedReader()
                        ?.use { reader -> reader.readText() }

                    if (json != null) {
                        // Store in MainActivity for import dialog handling
                        MainActivity.pendingImportJson = json
                        snackbarHostState.showSnackbar(
                            message = "Import file loaded. Navigate to Recipes screen to complete import.",
                            duration = SnackbarDuration.Long
                        )
                        DebugConfig.debugLog(DebugConfig.Category.UI, "Import file loaded from MealPlanningScreen")
                    } else {
                        snackbarHostState.showSnackbar(
                            message = "Failed to read file",
                            duration = SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    DebugConfig.error(DebugConfig.Category.UI, "Import file error", e)
                    snackbarHostState.showSnackbar(
                        message = "Error: ${e.message}",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Meal Planning") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        filePicker.launch(arrayOf("application/json", "text/plain"))
                    }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Import")
                    }
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
                        onSortSelected = { mealPlanViewModel.setSort(it) },
                        onSortDirectionToggle = { mealPlanViewModel.toggleSortDirection() }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Add Meal Plan FAB clicked")
                    onAddMealPlan()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Meal Plan"
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
                    onValueChange = { mealPlanViewModel.searchMealPlans(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search meal plans...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { mealPlanViewModel.searchMealPlans("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                mealPlans.isEmpty() -> {
                    EmptyMealPlanState(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    // Check orientation for layout choice
                    val configuration = LocalConfiguration.current
                    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

                    if (isLandscape) {
                        // Grid layout for landscape (2 columns)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(mealPlans, key = { it.id }) { mealPlan ->
                                val planRecipes = recipes.filter { it.id in mealPlan.recipeIds }
                                val recipePhotos = planRecipes.associate {
                                    it.id to (it.mediaPaths.firstOrNull { media ->
                                        media.type == com.recipeindex.app.data.entities.MediaType.IMAGE
                                    }?.path ?: it.photoPath ?: "")
                                }

                                MealPlanCard(
                                    mealPlan = mealPlan,
                                    recipes = recipes,
                                    onEdit = { onEditMealPlan(mealPlan.id) },
                                    onDelete = {
                                        planToDelete = mealPlan
                                        showDeleteDialog = true
                                    },
                                    onDuplicate = {
                                        planToDuplicate = mealPlan
                                        duplicateName = "${mealPlan.name} (Copy)"
                                        showDuplicateDialog = true
                                    },
                                    onShare = {
                                        ShareHelper.shareMealPlan(context, mealPlan, planRecipes, recipePhotos)
                                    },
                                    onGenerateList = {
                                        planForGroceryList = mealPlan
                                        showListPicker = true
                                    }
                                )
                            }
                        }
                    } else {
                        // Column layout for portrait
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(mealPlans, key = { it.id }) { mealPlan ->
                                val planRecipes = recipes.filter { it.id in mealPlan.recipeIds }
                                val recipePhotos = planRecipes.associate {
                                    it.id to (it.mediaPaths.firstOrNull { media ->
                                        media.type == com.recipeindex.app.data.entities.MediaType.IMAGE
                                    }?.path ?: it.photoPath ?: "")
                                }

                                MealPlanCard(
                                    mealPlan = mealPlan,
                                    recipes = recipes,
                                    onEdit = { onEditMealPlan(mealPlan.id) },
                                    onDelete = {
                                        planToDelete = mealPlan
                                        showDeleteDialog = true
                                    },
                                    onDuplicate = {
                                        planToDuplicate = mealPlan
                                        duplicateName = "${mealPlan.name} (Copy)"
                                        showDuplicateDialog = true
                                    },
                                    onShare = {
                                        ShareHelper.shareMealPlan(context, mealPlan, planRecipes, recipePhotos)
                                    },
                                    onGenerateList = {
                                        planForGroceryList = mealPlan
                                        showListPicker = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && planToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Meal Plan") },
            text = { Text("Are you sure you want to delete \"${planToDelete!!.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mealPlanViewModel.deleteMealPlan(planToDelete!!)
                        showDeleteDialog = false
                        planToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Duplicate dialog
    if (showDuplicateDialog && planToDuplicate != null) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { Text("Duplicate Meal Plan") },
            text = {
                Column {
                    Text("Enter name for duplicated plan:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = duplicateName,
                        onValueChange = { duplicateName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mealPlanViewModel.duplicateMealPlan(planToDuplicate!!, duplicateName)
                        showDuplicateDialog = false
                        planToDuplicate = null
                    },
                    enabled = duplicateName.isNotBlank()
                ) {
                    Text("Duplicate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Grocery list picker dialog
    if (showListPicker && planForGroceryList != null) {
        GroceryListPickerDialog(
            availableLists = groceryLists,
            onDismiss = { showListPicker = false },
            onListSelected = { listId ->
                val selectedList = groceryLists.find { it.id == listId }
                val listName = selectedList?.name ?: "list"
                // Capture plan details before clearing (callback is async!)
                val planId = planForGroceryList!!.id
                val planName = planForGroceryList!!.name

                groceryListViewModel.addMealPlanToList(listId, planId) { count ->
                    // Success callback - show snackbar with count
                    scope.launch {
                        val message = if (count > 0) {
                            "Added $count ingredient${if (count == 1) "" else "s"} to $listName"
                        } else {
                            "No ingredients found in $planName - recipes may be missing"
                        }
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                showListPicker = false
                planForGroceryList = null
            },
            onCreateNew = { listName ->
                // Capture plan details before clearing (callbacks are async!)
                val planId = planForGroceryList!!.id
                val planName = planForGroceryList!!.name

                // Create list first, then add meal plan in the success callback
                groceryListViewModel.createList(listName) { newListId ->
                    groceryListViewModel.addMealPlanToList(newListId, planId) { count ->
                        // Success callback - show snackbar with count
                        scope.launch {
                            val message = if (count > 0) {
                                "Added $count ingredient${if (count == 1) "" else "s"} to $listName"
                            } else {
                                "No ingredients found in $planName - recipes may be missing"
                            }
                            snackbarHostState.showSnackbar(
                                message = message,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
                showListPicker = false
                planForGroceryList = null
            }
        )
    }

    // Filter bottom sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            availableFilters = availableFilters,
            activeFilterIds = activeFilterIds,
            onFilterToggle = { mealPlanViewModel.toggleFilter(it) },
            onClearAll = { mealPlanViewModel.clearFilters() },
            onDismiss = { showFilterSheet = false },
            sheetState = filterSheetState
        )
    }
}

@Composable
private fun MealPlanCard(
    mealPlan: MealPlan,
    recipes: List<Recipe>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
    onGenerateList: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onEdit // Make the entire card clickable to go to detail/edit screen
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with name and context menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Name and dates
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mealPlan.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Context menu button
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = {
                                showMenu = false
                                onDuplicate()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
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
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            if (mealPlan.startDate != null || mealPlan.endDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDateRange(mealPlan.startDate, mealPlan.endDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onGenerateList,
                            enabled = mealPlan.recipeIds.isNotEmpty(),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Generate Grocery List",
                                tint = if (mealPlan.recipeIds.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Tags
            if (mealPlan.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    mealPlan.tags.take(5).forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    if (mealPlan.tags.size > 5) {
                        AssistChip(
                            onClick = { },
                            label = { Text("+${mealPlan.tags.size - 5}", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // Recipes
            if (mealPlan.recipeIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Recipes:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                val planRecipes = recipes.filter { it.id in mealPlan.recipeIds }
                planRecipes.forEach { recipe ->
                    Text(
                        text = "• ${recipe.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }

            // Notes
            if (!mealPlan.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mealPlan.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyMealPlanState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No meal plans yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to create your first meal plan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private fun formatDateRange(startDate: Long?, endDate: Long?): String {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    return when {
        startDate != null && endDate != null -> {
            if (startDate == endDate) {
                dateFormat.format(Date(startDate))
            } else {
                "${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}"
            }
        }
        startDate != null -> "Starting ${dateFormat.format(Date(startDate))}"
        endDate != null -> "Ending ${dateFormat.format(Date(endDate))}"
        else -> ""
    }
}
