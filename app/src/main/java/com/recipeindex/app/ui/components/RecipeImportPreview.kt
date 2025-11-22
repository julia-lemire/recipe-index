package com.recipeindex.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.utils.RecipeValidation

/**
 * Shared Recipe Import Preview Component
 *
 * Used by all import screens (URL, PDF, Photo) for consistent preview experience.
 * Shows WYSIWYG preview with edit dialogs for each section.
 *
 * @param recipe The recipe being previewed/edited
 * @param imageUrls Optional list of image URLs to choose from (for URL imports)
 * @param selectedImageUrls Currently selected image URLs
 * @param existingTags List of existing tags for auto-suggestions
 * @param onRecipeChange Callback when recipe is modified
 * @param onSelectedImagesChange Callback when image selection changes
 * @param errorMessage Optional error message to display
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeImportPreview(
    modifier: Modifier = Modifier,
    recipe: Recipe,
    imageUrls: List<String> = emptyList(),
    selectedImageUrls: Set<String> = emptySet(),
    existingTags: List<String> = emptyList(),
    onRecipeChange: (Recipe) -> Unit,
    onSelectedImagesChange: (Set<String>) -> Unit = {},
    errorMessage: String? = null
) {
    var tagInput by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editField by remember { mutableStateOf<EditField?>(null) }

    // Filter existing tags based on user input
    val suggestedTags = remember(tagInput, existingTags, recipe.tags) {
        if (tagInput.length >= 2) {
            existingTags
                .filter { it.contains(tagInput, ignoreCase = true) }
                .filter { candidate ->
                    if (recipe.tags.contains(candidate)) return@filter false
                    recipe.tags.none { existing ->
                        candidate.contains(existing, ignoreCase = true) ||
                        existing.contains(candidate, ignoreCase = true)
                    }
                }
                .take(5)
        } else {
            emptyList()
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Review Recipe",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Preview imported recipe before saving",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Media Selection - Show found images for user to select
        if (imageUrls.isNotEmpty()) {
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
                    Text(
                        text = "Recipe Images (${selectedImageUrls.size}/${imageUrls.size} selected)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Tap images to select/deselect. Selected images will be downloaded and saved with the recipe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Image grid
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        imageUrls.forEach { imageUrl ->
                            val isSelected = imageUrl in selectedImageUrls
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable {
                                        val newSelection = if (isSelected) {
                                            selectedImageUrls - imageUrl
                                        } else {
                                            selectedImageUrls + imageUrl
                                        }
                                        onSelectedImagesChange(newSelection)
                                    }
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Recipe image option",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Selection overlay
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.2f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recipe Photo (legacy display - for recipes that already have a photo)
        recipe.photoPath?.let { photoUrl ->
            if (imageUrls.isEmpty()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Recipe photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Title with edit button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    text = recipe.title.ifBlank { "(No title)" },
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                    color = if (recipe.title.isBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = {
                    editField = EditField.TITLE
                    showEditDialog = true
                }) {
                    Icon(Icons.Default.Edit, "Edit title")
                }
            }
        }

        // Recipe metadata
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Servings
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${recipe.servings} servings",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Serving size
                        recipe.servingSize?.let { size ->
                            Text(
                                text = "($size/serving)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Times
                        val totalTime = (recipe.prepTimeMinutes ?: 0) + (recipe.cookTimeMinutes ?: 0)
                        if (totalTime > 0) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "${totalTime}m total",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Cuisine
                    if (!recipe.cuisine.isNullOrBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = recipe.cuisine!!,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                IconButton(onClick = {
                    editField = EditField.METADATA
                    showEditDialog = true
                }) {
                    Icon(Icons.Default.Edit, "Edit metadata")
                }
            }
        }

        // Tags - inline editable
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.labelLarge
                )

                // Current tags
                if (recipe.tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recipe.tags.forEach { tag ->
                            InputChip(
                                selected = true,
                                onClick = {
                                    onRecipeChange(recipe.copy(tags = recipe.tags - tag))
                                },
                                label = { Text(tag) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                // Add tag input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it.lowercase() },
                        label = { Text("Add tag") },
                        placeholder = { Text("Type to add...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        trailingIcon = {
                            if (tagInput.isNotBlank()) {
                                IconButton(onClick = {
                                    if (tagInput.isNotBlank() && !recipe.tags.contains(tagInput.trim())) {
                                        onRecipeChange(recipe.copy(tags = recipe.tags + tagInput.trim()))
                                        tagInput = ""
                                    }
                                }) {
                                    Icon(Icons.Default.Add, "Add tag")
                                }
                            }
                        }
                    )
                }

                // Tag suggestions
                if (suggestedTags.isNotEmpty()) {
                    Text(
                        text = "Suggestions:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        suggestedTags.forEach { tag ->
                            SuggestionChip(
                                onClick = {
                                    if (!recipe.tags.contains(tag)) {
                                        onRecipeChange(recipe.copy(tags = recipe.tags + tag))
                                        tagInput = ""
                                    }
                                },
                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }

        // Ingredients
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingredients (${recipe.ingredients.size})",
                        style = MaterialTheme.typography.labelLarge
                    )
                    IconButton(onClick = {
                        editField = EditField.INGREDIENTS
                        showEditDialog = true
                    }) {
                        Icon(Icons.Default.Edit, "Edit ingredients")
                    }
                }
                if (recipe.ingredients.isEmpty()) {
                    Text(
                        text = "(No ingredients)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    recipe.ingredients.take(5).forEach { ingredient ->
                        Text(
                            text = "• $ingredient",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (recipe.ingredients.size > 5) {
                        Text(
                            text = "... and ${recipe.ingredients.size - 5} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Instructions
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Instructions (${recipe.instructions.size} steps)",
                        style = MaterialTheme.typography.labelLarge
                    )
                    IconButton(onClick = {
                        editField = EditField.INSTRUCTIONS
                        showEditDialog = true
                    }) {
                        Icon(Icons.Default.Edit, "Edit instructions")
                    }
                }
                if (recipe.instructions.isEmpty()) {
                    Text(
                        text = "(No instructions)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    recipe.instructions.take(3).forEachIndexed { index, instruction ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = instruction,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (recipe.instructions.size > 3) {
                        Text(
                            text = "... and ${recipe.instructions.size - 3} more steps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Tips & Substitutions (optional section)
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tips & Substitutions",
                        style = MaterialTheme.typography.labelLarge
                    )
                    IconButton(onClick = {
                        editField = EditField.SOURCE_TIPS
                        showEditDialog = true
                    }) {
                        Icon(Icons.Default.Edit, "Edit tips")
                    }
                }
                if (recipe.sourceTips.isNullOrBlank()) {
                    Text(
                        text = "(No tips from source - tap edit to add)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = recipe.sourceTips,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Validation help text
        if (recipe.title.isBlank() || recipe.ingredients.isEmpty() || recipe.instructions.isEmpty()) {
            Text(
                text = "Please ensure title, ingredients, and instructions are filled before saving",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Edit dialog for detailed field editing
    if (showEditDialog && editField != null) {
        EditFieldDialog(
            field = editField!!,
            recipe = recipe,
            onDismiss = { showEditDialog = false },
            onSave = { updatedRecipe ->
                onRecipeChange(updatedRecipe)
                showEditDialog = false
            }
        )
    }
}

/**
 * Check if recipe is valid for saving
 * Delegates to centralized RecipeValidation
 */
fun isRecipeValid(recipe: Recipe): Boolean = RecipeValidation.isValid(recipe)

/**
 * Helper enum for edit dialog
 */
private enum class EditField {
    TITLE, METADATA, INGREDIENTS, INSTRUCTIONS, SOURCE_TIPS
}

/**
 * Dialog for editing specific recipe fields
 */
@Composable
private fun EditFieldDialog(
    field: EditField,
    recipe: Recipe,
    onDismiss: () -> Unit,
    onSave: (Recipe) -> Unit
) {
    var editedRecipe by remember { mutableStateOf(recipe) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (field) {
                    EditField.TITLE -> "Edit Title"
                    EditField.METADATA -> "Edit Servings & Time"
                    EditField.INGREDIENTS -> "Edit Ingredients"
                    EditField.INSTRUCTIONS -> "Edit Instructions"
                    EditField.SOURCE_TIPS -> "Edit Tips & Substitutions"
                }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (field) {
                    EditField.TITLE -> {
                        OutlinedTextField(
                            value = editedRecipe.title,
                            onValueChange = { editedRecipe = editedRecipe.copy(title = it) },
                            label = { Text("Title*") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = editedRecipe.title.isBlank()
                        )
                    }
                    EditField.METADATA -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editedRecipe.servings.toString(),
                                onValueChange = {
                                    val servings = it.toIntOrNull() ?: editedRecipe.servings
                                    editedRecipe = editedRecipe.copy(servings = servings)
                                },
                                label = { Text("Servings") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editedRecipe.servingSize ?: "",
                                onValueChange = {
                                    editedRecipe = editedRecipe.copy(servingSize = it.ifBlank { null })
                                },
                                label = { Text("Serving Size") },
                                placeholder = { Text("e.g., 1 cup") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editedRecipe.prepTimeMinutes?.toString() ?: "",
                                onValueChange = {
                                    val time = it.toIntOrNull()
                                    editedRecipe = editedRecipe.copy(prepTimeMinutes = time)
                                },
                                label = { Text("Prep (min)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editedRecipe.cookTimeMinutes?.toString() ?: "",
                                onValueChange = {
                                    val time = it.toIntOrNull()
                                    editedRecipe = editedRecipe.copy(cookTimeMinutes = time)
                                },
                                label = { Text("Cook (min)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = editedRecipe.cuisine ?: "",
                            onValueChange = {
                                editedRecipe = editedRecipe.copy(cuisine = it.ifBlank { null })
                            },
                            label = { Text("Cuisine") },
                            placeholder = { Text("e.g., Italian, Thai, Mexican") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    EditField.INGREDIENTS -> {
                        OutlinedTextField(
                            value = editedRecipe.ingredients.joinToString("\n"),
                            onValueChange = {
                                val ingredients = it.split("\n").filter { line -> line.isNotBlank() }
                                editedRecipe = editedRecipe.copy(ingredients = ingredients)
                            },
                            label = { Text("Ingredients* (one per line)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 6,
                            maxLines = 12,
                            isError = editedRecipe.ingredients.isEmpty()
                        )
                    }
                    EditField.INSTRUCTIONS -> {
                        OutlinedTextField(
                            value = editedRecipe.instructions.joinToString("\n"),
                            onValueChange = {
                                val instructions = it.split("\n").filter { line -> line.isNotBlank() }
                                editedRecipe = editedRecipe.copy(instructions = instructions)
                            },
                            label = { Text("Instructions* (one step per line)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 6,
                            maxLines = 12,
                            isError = editedRecipe.instructions.isEmpty()
                        )
                    }
                    EditField.SOURCE_TIPS -> {
                        OutlinedTextField(
                            value = editedRecipe.sourceTips ?: "",
                            onValueChange = {
                                editedRecipe = editedRecipe.copy(sourceTips = it.ifBlank { null })
                            },
                            label = { Text("Tips & Substitutions from source") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 10
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(editedRecipe) },
                enabled = when (field) {
                    EditField.TITLE -> editedRecipe.title.isNotBlank()
                    EditField.INGREDIENTS -> editedRecipe.ingredients.isNotEmpty()
                    EditField.INSTRUCTIONS -> editedRecipe.instructions.isNotEmpty()
                    else -> true
                }
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

/**
 * FlowRow - Custom layout that arranges children in rows, wrapping to new row when needed
 */
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    val horizontalSpacing = 8.dp
    val verticalSpacing = 8.dp

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
