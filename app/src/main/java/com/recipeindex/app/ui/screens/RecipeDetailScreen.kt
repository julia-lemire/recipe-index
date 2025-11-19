package com.recipeindex.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.ui.viewmodels.SettingsViewModel
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.IngredientScaler
import com.recipeindex.app.utils.IngredientUnitConverter

/**
 * RecipeDetailScreen - View recipe details
 *
 * Layout: Title, Servings/Prep/Cook time, Ingredients, Instructions, Tags, Notes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    settingsViewModel: SettingsViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    onAddToGroceryList: () -> Unit = {},
    onAddToMealPlan: () -> Unit = {}
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "RecipeDetailScreen - ${recipe.title}")

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var selectedServings by remember { mutableStateOf(recipe.servings) }
    var showServingsMenu by remember { mutableStateOf(false) }

    // Get user's unit preferences from settings
    val settings by settingsViewModel.settings.collectAsState()

    // Calculate scaling factor for ingredients
    val scaleFactor = selectedServings.toDouble() / recipe.servings.toDouble()

    // Handle system back button
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onToggleFavorite(!recipe.isFavorite) }) {
                        Icon(
                            if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Favorite"
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    // Inline buttons for Add to Grocery List and Add to Meal Plan
                    IconButton(onClick = onAddToGroceryList) {
                        Icon(Icons.Default.ShoppingCart, "Add to Grocery List")
                    }
                    IconButton(onClick = onAddToMealPlan) {
                        Icon(Icons.Default.CalendarMonth, "Add to Meal Plan")
                    }
                    // Overflow menu with delete option only
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete recipe") },
                                onClick = {
                                    showOverflowMenu = false
                                    showDeleteDialog = true
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
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Recipe Photo
            recipe.photoPath?.let { photoUrl ->
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Recipe photo for ${recipe.title}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Crop
                )
            }

            // Servings and Time Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Servings dropdown
                        Column {
                            Text(
                                text = "Servings",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box {
                                OutlinedButton(
                                    onClick = { showServingsMenu = true },
                                    modifier = Modifier.height(40.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = selectedServings.toString(),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showServingsMenu,
                                    onDismissRequest = { showServingsMenu = false }
                                ) {
                                    // Servings options: half, original, double, custom multiples
                                    listOf(
                                        recipe.servings / 2,
                                        recipe.servings,
                                        recipe.servings * 2,
                                        recipe.servings * 3,
                                        recipe.servings * 4
                                    ).distinct().filter { it > 0 }.forEach { servings ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(servings.toString())
                                                    if (servings == recipe.servings) {
                                                        Text(
                                                            "(original)",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedServings = servings
                                                showServingsMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        recipe.prepTimeMinutes?.let {
                            InfoItem(label = "Prep", value = "$it min")
                        }
                        recipe.cookTimeMinutes?.let {
                            InfoItem(label = "Cook", value = "$it min")
                        }
                    }
                    recipe.prepTimeMinutes?.let { prep ->
                        recipe.cookTimeMinutes?.let { cook ->
                            InfoItem(label = "Total", value = "${prep + cook} min")
                        }
                    }
                }
            }

            // Ingredients - scaled based on selected servings
            // TODO: Add "Cook Mode" feature with checkable ingredients to help track while cooking
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (selectedServings != recipe.servings) {
                        Text(
                            text = "Scaled for $selectedServings servings",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                recipe.ingredients.forEach { ingredient ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        // Scale ingredient based on servings
                        var processedIngredient = if (scaleFactor != 1.0) {
                            IngredientScaler.scaleIngredient(ingredient, scaleFactor)
                        } else {
                            ingredient
                        }

                        // Format ingredient according to user's granular unit preferences
                        processedIngredient = IngredientUnitConverter.formatIngredient(
                            processedIngredient,
                            liquidPreference = settings.liquidVolumePreference,
                            weightPreference = settings.weightPreference
                        )

                        Text(
                            text = "• $processedIngredient",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Instructions (with sections if present)
            InstructionsSection(instructions = recipe.instructions)

            // Tags
            if (recipe.tags.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.titleMedium
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp
                    ) {
                        recipe.tags.forEach { tag ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            }

            // Notes
            recipe.notes?.let { notes ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete \"${recipe.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Parse instructions into sections
 * Sections are detected by lines ending with ":"
 */
private data class InstructionSection(
    val name: String,
    val steps: List<String>
)

private fun parseInstructionSections(instructions: List<String>): List<InstructionSection> {
    val sections = mutableListOf<InstructionSection>()
    var currentSection: String? = null
    var currentSteps = mutableListOf<String>()

    instructions.forEach { instruction ->
        if (instruction.endsWith(":")) {
            // Save previous section if exists
            if (currentSection != null && currentSteps.isNotEmpty()) {
                sections.add(InstructionSection(currentSection, currentSteps.toList()))
                currentSteps.clear()
            }
            // Start new section
            currentSection = instruction.removeSuffix(":")
        } else {
            // Add to current section
            currentSteps.add(instruction)
        }
    }

    // Add final section
    if (currentSection != null && currentSteps.isNotEmpty()) {
        sections.add(InstructionSection(currentSection, currentSteps.toList()))
    } else if (sections.isEmpty() && currentSteps.isNotEmpty()) {
        // No sections found, treat all as default section
        sections.add(InstructionSection("Instructions", currentSteps.toList()))
    }

    // If no sections detected and we still have instructions, use all instructions
    if (sections.isEmpty() && instructions.isNotEmpty()) {
        sections.add(InstructionSection("Instructions", instructions))
    }

    return sections
}

@Composable
private fun InstructionsSection(instructions: List<String>) {
    val sections = remember(instructions) { parseInstructionSections(instructions) }

    if (sections.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Instructions",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        if (sections.size > 1) {
            // Multiple sections - use tabs
            var selectedTabIndex by remember { mutableIntStateOf(0) }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Tab Row
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        sections.forEachIndexed { index, section ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(section.name) }
                            )
                        }
                    }

                    // Tab Content
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        sections[selectedTabIndex].steps.forEachIndexed { index, step ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Step ${index + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Single section - simple list
            sections.first().steps.forEachIndexed { index, step ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Step ${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyLarge
                    )
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
