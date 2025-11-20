package com.recipeindex.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.recipeindex.app.data.Substitute
import com.recipeindex.app.data.entities.IngredientSubstitution
import com.recipeindex.app.ui.viewmodels.SubstitutionViewModel
import com.recipeindex.app.utils.DebugConfig

/**
 * AddEditSubstitutionScreen - Form for creating/editing ingredient substitutions
 *
 * Auto-saves when navigating back. Only saves if there's meaningful content.
 * Layout order: Ingredient name, Category, Substitutes list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubstitutionScreen(
    substitution: IngredientSubstitution? = null,
    viewModel: SubstitutionViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "AddEditSubstitutionScreen - editing: ${substitution != null}")

    var ingredient by remember { mutableStateOf(substitution?.ingredient ?: "") }
    var category by remember { mutableStateOf(substitution?.category ?: "Other") }
    var substitutes by remember { mutableStateOf(substitution?.substitutes ?: emptyList()) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val categories = listOf(
        "Dairy", "Baking", "Oils & Fats", "Sweeteners",
        "Vinegars & Acids", "Herbs & Spices", "Condiments", "Other"
    )

    // Auto-save logic when navigating back
    fun handleBack() {
        // Check if form has any meaningful content
        val hasContent = ingredient.isNotBlank() || substitutes.isNotEmpty()

        if (!hasContent) {
            // Empty form, just cancel
            onCancel()
            return
        }

        // Validate before saving
        when {
            ingredient.isBlank() -> {
                errorMessage = "Ingredient name is required"
                showError = true
            }
            substitutes.isEmpty() -> {
                errorMessage = "At least one substitute is required"
                showError = true
            }
            substitutes.any { it.name.isBlank() } -> {
                errorMessage = "All substitutes must have a name"
                showError = true
            }
            else -> {
                // Valid data, auto-save
                viewModel.createOrUpdateSubstitution(
                    id = substitution?.id ?: 0,
                    ingredient = ingredient.trim(),
                    category = category,
                    substitutes = substitutes,
                    isUserAdded = true
                )
                onSave()
            }
        }
    }

    BackHandler(onBack = { handleBack() })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (substitution == null) "Add Substitution" else "Edit Substitution") },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(Icons.Default.Check, "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ingredient name
            OutlinedTextField(
                value = ingredient,
                onValueChange = { ingredient = it },
                label = { Text("Ingredient Name") },
                placeholder = { Text("e.g., butter, milk, eggs") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
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
                }
            }

            Divider()

            // Substitutes section
            Text(
                text = "Substitutes",
                style = MaterialTheme.typography.titleMedium
            )

            substitutes.forEachIndexed { index, substitute ->
                SubstituteItem(
                    substitute = substitute,
                    onUpdate = { updated ->
                        substitutes = substitutes.toMutableList().apply {
                            set(index, updated)
                        }
                    },
                    onRemove = {
                        substitutes = substitutes.toMutableList().apply {
                            removeAt(index)
                        }
                    }
                )
            }

            // Add substitute button
            OutlinedButton(
                onClick = {
                    substitutes = substitutes + Substitute(
                        name = "",
                        conversionRatio = 1.0,
                        conversionNote = null,
                        notes = null,
                        suitability = 5,
                        dietaryTags = emptyList()
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, "Add substitute")
                Spacer(Modifier.width(8.dp))
                Text("Add Substitute")
            }
        }

        // Error snackbar
        if (showError) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showError = false }) {
                        Text("OK")
                    }
                }
            ) {
                Text(errorMessage)
            }
        }
    }
}

@Composable
private fun SubstituteItem(
    substitute: Substitute,
    onUpdate: (Substitute) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(substitute.name) }
    var conversionRatio by remember { mutableStateOf(substitute.conversionRatio.toString()) }
    var conversionNote by remember { mutableStateOf(substitute.conversionNote ?: "") }
    var notes by remember { mutableStateOf(substitute.notes ?: "") }
    var suitability by remember { mutableFloatStateOf(substitute.suitability.toFloat()) }
    var dietaryTags by remember { mutableStateOf(substitute.dietaryTags) }
    var tagInput by remember { mutableStateOf("") }

    val commonDietaryTags = listOf(
        "vegan", "vegetarian", "gluten-free", "dairy-free",
        "nut-free", "soy-free", "low-carb", "keto", "paleo"
    )

    // Update parent whenever fields change
    LaunchedEffect(name, conversionRatio, conversionNote, notes, suitability, dietaryTags) {
        onUpdate(
            Substitute(
                name = name,
                conversionRatio = conversionRatio.toDoubleOrNull() ?: 1.0,
                conversionNote = conversionNote.ifBlank { null },
                notes = notes.ifBlank { null },
                suitability = suitability.toInt(),
                dietaryTags = dietaryTags
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row with name and remove button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Substitute Name") },
                    placeholder = { Text("e.g., olive oil") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(Modifier.width(8.dp))

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, "Remove substitute")
                }
            }

            // Conversion ratio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = conversionRatio,
                    onValueChange = { conversionRatio = it },
                    label = { Text("Ratio") },
                    placeholder = { Text("1.0") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = conversionNote,
                    onValueChange = { conversionNote = it },
                    label = { Text("Conversion Note") },
                    placeholder = { Text("Optional") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            // Suitability slider
            Column {
                Text(
                    text = "Suitability: ${suitability.toInt()}/10",
                    style = MaterialTheme.typography.bodySmall
                )
                Slider(
                    value = suitability,
                    onValueChange = { suitability = it },
                    valueRange = 1f..10f,
                    steps = 8
                )
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                placeholder = { Text("Optional notes about this substitute") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Dietary tags
            Text(
                text = "Dietary Tags",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Tag chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                dietaryTags.forEach { tag ->
                    InputChip(
                        selected = true,
                        onClick = {
                            dietaryTags = dietaryTags - tag
                        },
                        label = { Text(tag) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove tag",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            // Add tag field with suggestions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    label = { Text("Add dietary tag") },
                    placeholder = { Text("e.g., vegan") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    trailingIcon = {
                        if (tagInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    if (tagInput.isNotBlank() && tagInput !in dietaryTags) {
                                        dietaryTags = dietaryTags + tagInput.trim().lowercase()
                                        tagInput = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, "Add tag")
                            }
                        }
                    }
                )
            }

            // Common tag suggestions
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                commonDietaryTags.filter { it !in dietaryTags }.forEach { tag ->
                    SuggestionChip(
                        onClick = {
                            dietaryTags = dietaryTags + tag
                        },
                        label = { Text(tag) }
                    )
                }
            }
        }
    }
}

/**
 * FlowRow for wrapping dietary tags
 */
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hSpacing = 8.dp.roundToPx()
        val vSpacing = 4.dp.roundToPx()

        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }

        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        placeables.forEach { placeable ->
            if (currentRowWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + hSpacing
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

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
