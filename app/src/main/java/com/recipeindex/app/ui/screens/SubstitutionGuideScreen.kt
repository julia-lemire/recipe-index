package com.recipeindex.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.recipeindex.app.data.entities.IngredientSubstitution
import com.recipeindex.app.ui.viewmodels.SubstitutionViewModel
import com.recipeindex.app.utils.DebugConfig

/**
 * SubstitutionGuideScreen - Main substitution guide interface
 *
 * Search for ingredient substitutions with optional quantity conversion
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubstitutionGuideScreen(
    viewModel: SubstitutionViewModel,
    onMenuClick: () -> Unit = {},
    onAddSubstitution: () -> Unit = {},
    onEditSubstitution: (Long) -> Unit = {}
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "SubstitutionGuideScreen composed")

    // State
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedDietaryTag by viewModel.selectedDietaryTag.collectAsState()
    val substitutions by viewModel.substitutions.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var quantityInput by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("") }
    var showUnitDropdown by remember { mutableStateOf(false) }

    val units = listOf("", "cup", "tbsp", "tsp", "oz", "lb", "g", "kg", "ml", "L", "can", "pack", "bottle", "jar")
    val dietaryTags = listOf("vegan", "gluten-free", "dairy-free", "lactose-free", "low-fat", "caffeine-free", "less-processed")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Substitution Guide") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSubstitution) {
                Icon(Icons.Default.Add, "Add Substitution")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Input Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Find Substitutes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Quantity and Unit Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quantity input
                        OutlinedTextField(
                            value = quantityInput,
                            onValueChange = { quantityInput = it },
                            label = { Text("Quantity (optional)") },
                            placeholder = { Text("2") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        // Unit dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = if (selectedUnit.isEmpty()) "Unit" else selectedUnit,
                                onValueChange = {},
                                label = { Text("Unit") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showUnitDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, "Select unit")
                                    }
                                },
                                singleLine = true
                            )
                            DropdownMenu(
                                expanded = showUnitDropdown,
                                onDismissRequest = { showUnitDropdown = false }
                            ) {
                                units.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(if (unit.isEmpty()) "None" else unit) },
                                        onClick = {
                                            selectedUnit = unit
                                            showUnitDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Ingredient search
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        label = { Text("Ingredient") },
                        placeholder = { Text("butter, eggs, milk...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Category Filter
            if (categories.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.setCategory(null) },
                            label = { Text("All") }
                        )
                        categories.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { viewModel.setCategory(category) },
                                label = { Text(category) }
                            )
                        }
                    }
                }
            }

            // Dietary Filter
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Dietary",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedDietaryTag == null,
                        onClick = { viewModel.setDietaryTag(null) },
                        label = { Text("All") }
                    )
                    dietaryTags.forEach { tag ->
                        FilterChip(
                            selected = selectedDietaryTag == tag,
                            onClick = { viewModel.setDietaryTag(tag) },
                            label = { Text(tag) }
                        )
                    }
                }
            }

            Divider()

            // Results
            if (substitutions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "Search for an ingredient to see substitutes" else "No substitutes found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(substitutions, key = { it.id }) { substitution ->
                        SubstitutionCard(
                            substitution = substitution,
                            quantity = quantityInput.toDoubleOrNull(),
                            unit = selectedUnit,
                            viewModel = viewModel,
                            onEdit = { onEditSubstitution(substitution.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubstitutionCard(
    substitution: IngredientSubstitution,
    quantity: Double?,
    unit: String,
    viewModel: SubstitutionViewModel,
    onEdit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { expanded = !expanded },
                onLongClick = onEdit
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = substitution.ingredient.capitalize(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = substitution.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (substitution.isUserAdded) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "User added",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            // Substitutes (when expanded)
            if (expanded) {
                Divider()
                substitution.substitutes.sortedByDescending { it.suitability }.forEach { substitute ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = substitute.name.capitalize(),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            // Suitability rating
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${substitute.suitability}/10",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Show converted amount if quantity provided
                        if (quantity != null && quantity > 0) {
                            val convertedAmount = viewModel.calculateConvertedAmount(quantity, substitute.conversionRatio)
                            val formattedAmount = viewModel.formatAmount(convertedAmount)
                            Text(
                                text = "$formattedAmount $unit",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Conversion note
                        substitute.conversionNote?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Notes
                        substitute.notes?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Dietary tags
                        if (substitute.dietaryTags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                            ) {
                                substitute.dietaryTags.forEach { tag ->
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }
                    if (substitute != substitution.substitutes.sortedByDescending { it.suitability }.last()) {
                        Divider()
                    }
                }
            } else {
                // Collapsed: Show count
                Text(
                    text = "${substitution.substitutes.size} substitute${if (substitution.substitutes.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
