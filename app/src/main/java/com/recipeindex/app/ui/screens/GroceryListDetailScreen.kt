package com.recipeindex.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.recipeindex.app.data.entities.GroceryItem
import com.recipeindex.app.data.entities.GroceryList
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.ui.viewmodels.GroceryListViewModel
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.ShareHelper
import kotlinx.coroutines.launch

/**
 * Grocery List Detail Screen - Shopping list with quick entry and checkboxes
 *
 * Layout:
 * - Text field at top for quick manual entry (like Out of Milk app)
 * - List of items with checkboxes
 * - Click item to see details (source recipes, edit, delete)
 * - Bottom actions: Clear Checked, Add from Recipes, Add from Meal Plans
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListDetailScreen(
    listId: Long,
    groceryListViewModel: GroceryListViewModel,
    availableRecipes: List<Recipe>,
    availableMealPlans: List<MealPlan>,
    onBack: () -> Unit
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "GroceryListDetailScreen - listId: $listId")

    val groceryList by groceryListViewModel.getCurrentList(listId).collectAsState(initial = null)
    val items by groceryListViewModel.getItems(listId).collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var manualEntryText by remember { mutableStateOf("") }
    var showItemDetailDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<GroceryItem?>(null) }
    var showRecipePicker by remember { mutableStateOf(false) }
    var showMealPlanPicker by remember { mutableStateOf(false) }
    var showClearCheckedDialog by remember { mutableStateOf(false) }

    // Handle system back button
    BackHandler {
        onBack()
    }

    if (groceryList == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(groceryList!!.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        ShareHelper.shareGroceryList(context, groceryList!!, items)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Quick entry text field at top (like Out of Milk app)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualEntryText,
                        onValueChange = { manualEntryText = it },
                        placeholder = { Text("Add item...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (manualEntryText.isNotBlank()) {
                                groceryListViewModel.addManualItem(listId, manualEntryText)
                                manualEntryText = ""
                            }
                        },
                        enabled = manualEntryText.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = if (manualEntryText.isNotBlank()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            }
                        )
                    }
                }
            }

            // Items list
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "No items yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Type above or add from recipes/meal plans",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        GroceryItemRow(
                            item = item,
                            onClick = {
                                // Short click: toggle checkbox
                                groceryListViewModel.toggleItemChecked(item.id, !item.isChecked)
                            },
                            onLongClick = {
                                // Long press: show detail dialog
                                selectedItem = item
                                showItemDetailDialog = true
                            }
                        )
                    }
                }
            }

            // Bottom actions - Icon over text pattern
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large.copy(
                    bottomStart = androidx.compose.foundation.shape.CornerSize(0.dp),
                    bottomEnd = androidx.compose.foundation.shape.CornerSize(0.dp)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Toggle Select/Deselect All
                    val allChecked = items.isNotEmpty() && items.all { it.isChecked }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = items.isNotEmpty()) {
                                if (allChecked) {
                                    groceryListViewModel.uncheckAllItems(listId)
                                } else {
                                    groceryListViewModel.checkAllItems(listId)
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (allChecked) Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle,
                            contentDescription = if (allChecked) "Deselect All" else "Select All",
                            tint = if (items.isEmpty()) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (allChecked) "Deselect" else "Select All",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (items.isEmpty()) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    // Clear checked items
                    val checkedItemsCount = items.count { it.isChecked }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = checkedItemsCount > 0) {
                                showClearCheckedDialog = true
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Checked",
                            tint = if (checkedItemsCount > 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Clear ($checkedItemsCount)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (checkedItemsCount > 0) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            }
                        )
                    }

                    // Add from Recipes
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showRecipePicker = true
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Add from Recipes",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Recipes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Add from Meal Plans
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showMealPlanPicker = true
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Add from Meal Plans",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Meal Plans",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    // Item detail dialog
    if (showItemDetailDialog && selectedItem != null) {
        ItemDetailDialog(
            item = selectedItem!!,
            availableRecipes = availableRecipes,
            onDismiss = { showItemDetailDialog = false },
            onUpdate = { updatedItem ->
                groceryListViewModel.updateItem(updatedItem)
                showItemDetailDialog = false
            },
            onDelete = { itemId ->
                groceryListViewModel.deleteItem(itemId)
                showItemDetailDialog = false
            }
        )
    }

    // Recipe picker bottom sheet
    if (showRecipePicker) {
        RecipePickerForGroceryBottomSheet(
            availableRecipes = availableRecipes,
            onDismiss = { showRecipePicker = false },
            onRecipesSelected = { recipeIds ->
                groceryListViewModel.addRecipesToList(listId, recipeIds) { count ->
                    scope.launch {
                        val message = if (count > 0) {
                            "Added $count ingredient${if (count == 1) "" else "s"}"
                        } else {
                            "No ingredients found - recipes may be empty"
                        }
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                showRecipePicker = false
            }
        )
    }

    // Meal plan picker bottom sheet
    if (showMealPlanPicker) {
        MealPlanPickerBottomSheet(
            availableMealPlans = availableMealPlans,
            onDismiss = { showMealPlanPicker = false },
            onMealPlanSelected = { planId ->
                groceryListViewModel.addMealPlanToList(listId, planId) { count ->
                    scope.launch {
                        val message = if (count > 0) {
                            "Added $count ingredient${if (count == 1) "" else "s"}"
                        } else {
                            "No ingredients found - recipes may be missing"
                        }
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                showMealPlanPicker = false
            }
        )
    }

    // Clear checked confirmation dialog
    if (showClearCheckedDialog) {
        val checkedItemsCount = items.count { it.isChecked }
        AlertDialog(
            onDismissRequest = { showClearCheckedDialog = false },
            title = { Text("Clear Checked Items") },
            text = { Text("Are you sure you want to remove $checkedItemsCount checked items?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        groceryListViewModel.clearCheckedItems(listId)
                        showClearCheckedDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCheckedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroceryItemRow(
    item: GroceryItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = null // Read-only, controlled by row click
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatItemName(item),
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isChecked) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            if (!item.notes.isNullOrBlank()) {
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            if (item.sourceRecipeIds.isNotEmpty()) {
                Text(
                    text = "${item.sourceRecipeIds.size} recipe(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Icon(
            Icons.Default.Info,
            contentDescription = "Details",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDetailDialog(
    item: GroceryItem,
    availableRecipes: List<Recipe>,
    onDismiss: () -> Unit,
    onUpdate: (GroceryItem) -> Unit,
    onDelete: (Long) -> Unit
) {
    var quantity by remember { mutableStateOf(item.quantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(item.unit ?: "") }
    var notes by remember { mutableStateOf(item.notes ?: "") }
    var showUnitDropdown by remember { mutableStateOf(false) }

    val availableUnits = listOf(
        "" to "none",
        "cup" to "cup",
        "tbsp" to "tbsp",
        "tsp" to "tsp",
        "oz" to "oz",
        "lb" to "lb",
        "g" to "g",
        "kg" to "kg",
        "ml" to "ml",
        "L" to "L",
        "can" to "can",
        "pack" to "pack",
        "bottle" to "bottle",
        "jar" to "jar"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Quantity and unit
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // Unit dropdown
                    ExposedDropdownMenuBox(
                        expanded = showUnitDropdown,
                        onExpandedChange = { showUnitDropdown = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = availableUnits.find { it.first == unit }?.second ?: unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitDropdown) },
                            modifier = Modifier.menuAnchor(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = showUnitDropdown,
                            onDismissRequest = { showUnitDropdown = false }
                        ) {
                            availableUnits.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        unit = value
                                        showUnitDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Source recipes
                if (item.sourceRecipeIds.isNotEmpty()) {
                    Divider()
                    Text(
                        text = "From recipes:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val sourceRecipes = availableRecipes.filter { it.id in item.sourceRecipeIds }
                    sourceRecipes.forEach { recipe ->
                        Text(
                            text = "• ${recipe.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { onDelete(item.id) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = {
                        val updatedItem = item.copy(
                            quantity = quantity.toDoubleOrNull(),
                            unit = unit.ifBlank { null },
                            notes = notes.ifBlank { null }
                        )
                        onUpdate(updatedItem)
                    }
                ) {
                    Text("Save")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipePickerForGroceryBottomSheet(
    availableRecipes: List<Recipe>,
    onDismiss: () -> Unit,
    onRecipesSelected: (List<Long>) -> Unit
) {
    var selectedRecipeIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredRecipes = if (searchQuery.isBlank()) {
        availableRecipes
    } else {
        availableRecipes.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Add Recipes to List",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search recipes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recipe list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRecipes, key = { it.id }) { recipe ->
                    val isSelected = recipe.id in selectedRecipeIds

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selectedRecipeIds = if (isSelected) {
                                selectedRecipeIds.filter { it != recipe.id }
                            } else {
                                selectedRecipeIds + recipe.id
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = recipe.title,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { onRecipesSelected(selectedRecipeIds) },
                    modifier = Modifier.weight(1f),
                    enabled = selectedRecipeIds.isNotEmpty()
                ) {
                    Text("Add (${selectedRecipeIds.size})")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealPlanPickerBottomSheet(
    availableMealPlans: List<MealPlan>,
    onDismiss: () -> Unit,
    onMealPlanSelected: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredPlans = if (searchQuery.isBlank()) {
        availableMealPlans
    } else {
        availableMealPlans.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Add Meal Plan to List",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search meal plans...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Meal plan list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPlans, key = { it.id }) { plan ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onMealPlanSelected(plan.id) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = plan.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${plan.recipeIds.size} recipes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}

private fun formatItemName(item: GroceryItem): String {
    return buildString {
        if (item.quantity != null && item.unit != null) {
            append("${formatQuantity(item.quantity)} ${item.unit} ")
        } else if (item.quantity != null) {
            append("${formatQuantity(item.quantity)} ")
        } else if (item.unit != null) {
            append("${item.unit} ")
        }
        append(item.name)
    }
}

private fun formatQuantity(quantity: Double): String {
    return if (quantity % 1.0 == 0.0) {
        quantity.toInt().toString()
    } else {
        String.format("%.2f", quantity).trimEnd('0').trimEnd('.')
    }
}
