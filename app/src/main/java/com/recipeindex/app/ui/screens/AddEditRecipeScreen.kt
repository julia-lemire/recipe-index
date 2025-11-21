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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeSource
import com.recipeindex.app.utils.DebugConfig

/**
 * AddEditRecipeScreen - Single screen form for creating/editing recipes
 *
 * Auto-saves when navigating back. Only saves if there's meaningful content.
 * Layout order: Title, Servings, Prep/Cook Time, Ingredients, Instructions, Tags, Notes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecipeScreen(
    recipe: Recipe? = null,
    onSave: (Recipe) -> Unit,
    onCancel: () -> Unit
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "AddEditRecipeScreen - editing: ${recipe != null}")

    var title by remember { mutableStateOf(recipe?.title ?: "") }
    var servings by remember { mutableStateOf(recipe?.servings?.toString() ?: "4") }
    var servingSize by remember { mutableStateOf(recipe?.servingSize ?: "") }
    var prepTime by remember { mutableStateOf(recipe?.prepTimeMinutes?.toString() ?: "") }
    var cookTime by remember { mutableStateOf(recipe?.cookTimeMinutes?.toString() ?: "") }
    var ingredients by remember { mutableStateOf(recipe?.ingredients?.joinToString("\n") ?: "") }
    var instructions by remember { mutableStateOf(recipe?.instructions?.joinToString("\n\n") ?: "") }
    var tags by remember { mutableStateOf(recipe?.tags ?: emptyList()) }
    var tagInput by remember { mutableStateOf("") }
    var cuisine by remember { mutableStateOf(recipe?.cuisine ?: "") }
    var notes by remember { mutableStateOf(recipe?.notes ?: "") }
    var sourceTips by remember { mutableStateOf(recipe?.sourceTips ?: "") }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Auto-save logic when navigating back
    fun handleBack() {
        // Check if form has any meaningful content
        val hasContent = title.isNotBlank() || ingredients.isNotBlank() || instructions.isNotBlank()

        if (!hasContent) {
            // Empty form, just cancel
            onCancel()
            return
        }

        // Validate before saving
        when {
            title.isBlank() -> {
                errorMessage = "Title is required"
                showError = true
            }
            ingredients.isBlank() -> {
                errorMessage = "At least one ingredient is required"
                showError = true
            }
            instructions.isBlank() -> {
                errorMessage = "Instructions are required"
                showError = true
            }
            servings.toIntOrNull() == null || servings.toInt() <= 0 -> {
                errorMessage = "Valid servings required"
                showError = true
            }
            else -> {
                // Valid data, auto-save
                val newRecipe = Recipe(
                    id = recipe?.id ?: 0,
                    title = title.trim(),
                    servings = servings.toInt(),
                    servingSize = servingSize.trim().ifBlank { null },
                    prepTimeMinutes = prepTime.toIntOrNull(),
                    cookTimeMinutes = cookTime.toIntOrNull(),
                    ingredients = ingredients.split("\n").map { it.trim() }.filter { it.isNotBlank() },
                    instructions = instructions.split("\n\n").map { it.trim() }.filter { it.isNotBlank() },
                    tags = tags.filter { it.isNotBlank() },
                    cuisine = cuisine.trim().ifBlank { null },
                    notes = notes.ifBlank { null },
                    sourceTips = sourceTips.ifBlank { null },
                    source = recipe?.source ?: RecipeSource.MANUAL,
                    sourceUrl = recipe?.sourceUrl,
                    photoPath = recipe?.photoPath,
                    isFavorite = recipe?.isFavorite ?: false,
                    isTemplate = recipe?.isTemplate ?: false,
                    createdAt = recipe?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                onSave(newRecipe)
            }
        }
    }

    // Handle system back button
    BackHandler {
        handleBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recipe == null) "Add Recipe" else "Edit Recipe") },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            if (showError) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showError = false }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(errorMessage)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Recipe Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Servings and Serving Size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it },
                    label = { Text("Servings *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = servingSize,
                    onValueChange = { servingSize = it },
                    label = { Text("Serving Size") },
                    placeholder = { Text("e.g., 1 cup, 200g") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Time fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = prepTime,
                    onValueChange = { prepTime = it },
                    label = { Text("Prep (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cookTime,
                    onValueChange = { cookTime = it },
                    label = { Text("Cook (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Cuisine
            OutlinedTextField(
                value = cuisine,
                onValueChange = { cuisine = it },
                label = { Text("Cuisine") },
                placeholder = { Text("e.g., Italian, Thai, Mexican") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Ingredients
            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Ingredients (one per line) *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10
            )

            // Instructions
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions (separate steps with blank line) *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10
            )

            // Tags (chip-based)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.labelLarge
                )

                // Display existing tags as removable chips
                if (tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalSpacing = 6.dp,
                        verticalSpacing = 6.dp
                    ) {
                        tags.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = {
                                    tags = tags.filter { it != tag }
                                },
                                label = { Text(tag) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove $tag",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                // Input field to add new tags
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    label = { Text("Add tag") },
                    placeholder = { Text("e.g., italian, quick") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        if (tagInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val newTag = tagInput.trim()
                                    if (newTag.isNotBlank() && !tags.contains(newTag)) {
                                        tags = tags + newTag
                                    }
                                    tagInput = ""
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add tag")
                            }
                        }
                    }
                )
            }

            // Tips & Substitutions (from source)
            OutlinedTextField(
                value = sourceTips,
                onValueChange = { sourceTips = it },
                label = { Text("Tips & Substitutions") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            // Notes (user-added)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("My Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Custom FlowRow implementation for wrapping tags
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

        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0))
        }

        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        placeables.forEach { placeable ->
            val itemWidth = placeable.width + hSpacing

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
