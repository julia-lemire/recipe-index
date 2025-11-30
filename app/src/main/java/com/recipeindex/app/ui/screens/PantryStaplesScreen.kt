package com.recipeindex.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeindex.app.data.entities.PantryStapleConfig
import com.recipeindex.app.ui.viewmodels.PantryStapleViewModel
import com.recipeindex.app.utils.DebugConfig

/**
 * PantryStaplesScreen - Manage pantry staple filtering configurations
 *
 * Allows users to view, add, edit, and delete pantry staple configurations
 * including threshold quantities and units
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryStaplesScreen(
    viewModel: PantryStapleViewModel,
    onNavigateBack: () -> Unit
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "PantryStaplesScreen composed")

    val allConfigs by viewModel.allConfigs.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<PantryStapleConfig?>(null) }
    var showDeleteDialog by remember { mutableStateOf<PantryStapleConfig?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showCategoryFilter by remember { mutableStateOf(false) }

    val categories = remember(allConfigs) {
        listOf("All") + allConfigs.map { it.category }.distinct().sorted()
    }

    val filteredConfigs = remember(allConfigs, selectedCategory) {
        if (selectedCategory == "All") allConfigs else allConfigs.filter { it.category == selectedCategory }
    }

    // Error snackbar
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Show error, then clear it
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { config ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Pantry Staple") },
            text = { Text("Are you sure you want to delete '${config.itemName}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteConfig(config)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to Defaults") },
            text = { Text("This will delete all custom items and reset all thresholds to default values. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add/Edit dialog
    if (showAddDialog || showEditDialog != null) {
        PantryStapleEditDialog(
            config = showEditDialog,
            categories = categories.filter { it != "All" },
            onDismiss = {
                showAddDialog = false
                showEditDialog = null
            },
            onSave = { config ->
                viewModel.saveConfig(config)
                showAddDialog = false
                showEditDialog = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pantry Staples Filter") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Category filter
                    IconButton(onClick = { showCategoryFilter = !showCategoryFilter }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter by category")
                    }

                    // Reset to defaults
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset to defaults")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add pantry staple")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category filter chips
            if (showCategoryFilter) {
                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = category == selectedCategory,
                                onClick = { viewModel.setCategory(category) },
                                label = {
                                    Text(
                                        "$category (${if (category == "All") allConfigs.size else allConfigs.count { it.category == category }})"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Divider()
            }

            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Pantry Staple Filtering",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Items below their threshold are filtered from grocery lists. Items exceeding the threshold will appear WITH quantity shown.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Error message
            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Config list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
            ) {
                items(
                    items = filteredConfigs,
                    key = { it.id }
                ) { config ->
                    PantryStapleItem(
                        config = config,
                        onToggleEnabled = { viewModel.toggleEnabled(config) },
                        onEdit = { showEditDialog = config },
                        onDelete = { showDeleteDialog = config }
                    )
                    Divider()
                }

                // Empty state
                if (filteredConfigs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedCategory == "All")
                                    "No pantry staples configured. Tap + to add one."
                                else
                                    "No items in '$selectedCategory' category.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PantryStapleItem(
    config: PantryStapleConfig,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Enable checkbox + Item info
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = config.enabled,
                onCheckedChange = { onToggleEnabled() }
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = config.itemName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (config.alwaysFilter) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Always Filter", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        )
                    }
                    if (config.isCustom) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Custom", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text(
                    text = if (config.alwaysFilter) {
                        "Never shown • ${config.category}"
                    } else {
                        "Show if > ${config.thresholdQuantity} ${config.thresholdUnit} • ${config.category}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                config.alternativeNames?.let { altNames ->
                    Text(
                        text = "Also matches: $altNames",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Right side: Action buttons
        Row {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantryStapleEditDialog(
    config: PantryStapleConfig?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (PantryStapleConfig) -> Unit
) {
    var itemName by remember { mutableStateOf(config?.itemName ?: "") }
    var thresholdQuantity by remember { mutableStateOf(config?.thresholdQuantity?.toString() ?: "") }
    var thresholdUnit by remember { mutableStateOf(config?.thresholdUnit ?: "cups") }
    var category by remember { mutableStateOf(config?.category ?: categories.firstOrNull() ?: "Other") }
    var alwaysFilter by remember { mutableStateOf(config?.alwaysFilter ?: false) }
    var alternativeNames by remember { mutableStateOf(config?.alternativeNames ?: "") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showUnitDropdown by remember { mutableStateOf(false) }

    val commonUnits = listOf(
        "cups", "tbsp", "tsp", "oz", "lb", "g", "kg", "ml", "L",
        "cloves", "onions", "shallots", "eggs", "can", "jar"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (config == null) "Add Pantry Staple" else "Edit Pantry Staple") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Item name
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    placeholder = { Text("e.g., salt, flour, olive oil") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("+ New Category") },
                            onClick = {
                                // Could implement custom category input here
                                showCategoryDropdown = false
                            }
                        )
                    }
                }

                // Always filter checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = alwaysFilter,
                        onCheckedChange = { alwaysFilter = it }
                    )
                    Text(
                        text = "Always filter (never show in grocery lists)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (!alwaysFilter) {
                    // Threshold quantity
                    OutlinedTextField(
                        value = thresholdQuantity,
                        onValueChange = { thresholdQuantity = it },
                        label = { Text("Threshold Quantity") },
                        placeholder = { Text("e.g., 2, 0.5, 1.0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Threshold unit dropdown
                    ExposedDropdownMenuBox(
                        expanded = showUnitDropdown,
                        onExpandedChange = { showUnitDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = thresholdUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Threshold Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showUnitDropdown,
                            onDismissRequest = { showUnitDropdown = false }
                        ) {
                            commonUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        thresholdUnit = unit
                                        showUnitDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "Item will appear in grocery lists only if quantity exceeds $thresholdQuantity $thresholdUnit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Alternative names
                OutlinedTextField(
                    value = alternativeNames,
                    onValueChange = { alternativeNames = it },
                    label = { Text("Alternative Names (optional)") },
                    placeholder = { Text("e.g., sea salt, kosher salt") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Comma-separated list of alternative names to match") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = thresholdQuantity.toDoubleOrNull() ?: 0.0
                    val newConfig = PantryStapleConfig(
                        id = config?.id ?: 0L,
                        itemName = itemName.trim(),
                        thresholdQuantity = qty,
                        thresholdUnit = thresholdUnit,
                        category = category,
                        alwaysFilter = alwaysFilter,
                        enabled = config?.enabled ?: true,
                        isCustom = config?.isCustom ?: true,
                        alternativeNames = alternativeNames.ifBlank { null }
                    )
                    onSave(newConfig)
                },
                enabled = itemName.isNotBlank() && (alwaysFilter || thresholdQuantity.toDoubleOrNull() != null)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
