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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
    var prepTime by remember { mutableStateOf(recipe?.prepTimeMinutes?.toString() ?: "") }
    var cookTime by remember { mutableStateOf(recipe?.cookTimeMinutes?.toString() ?: "") }
    var ingredients by remember { mutableStateOf(recipe?.ingredients?.joinToString("\n") ?: "") }
    var instructions by remember { mutableStateOf(recipe?.instructions?.joinToString("\n\n") ?: "") }
    var tags by remember { mutableStateOf(recipe?.tags?.joinToString(", ") ?: "") }
    var notes by remember { mutableStateOf(recipe?.notes ?: "") }

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
                    prepTimeMinutes = prepTime.toIntOrNull(),
                    cookTimeMinutes = cookTime.toIntOrNull(),
                    ingredients = ingredients.split("\n").map { it.trim() }.filter { it.isNotBlank() },
                    instructions = instructions.split("\n\n").map { it.trim() }.filter { it.isNotBlank() },
                    tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    notes = notes.ifBlank { null },
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

            // Servings
            OutlinedTextField(
                value = servings,
                onValueChange = { servings = it },
                label = { Text("Servings *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

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

            // Tags
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma separated)") },
                placeholder = { Text("dinner, italian, quick") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
