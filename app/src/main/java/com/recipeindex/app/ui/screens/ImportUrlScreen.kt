package com.recipeindex.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.ui.components.TagModificationDialog
import com.recipeindex.app.ui.viewmodels.ImportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Import URL Screen - Enter URL and preview/edit imported recipe
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportUrlScreen(
    viewModel: ImportViewModel,
    onNavigateBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    var url by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    var showDiscardDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showTagModificationDialog by remember { mutableStateOf(false) }
    var existingTags by remember { mutableStateOf<List<String>>(emptyList()) }

    // Load existing tags for auto-suggestion
    LaunchedEffect(Unit) {
        existingTags = withContext(Dispatchers.IO) {
            viewModel.getAllExistingTags()
        }
    }

    // Show tag modification dialog when recipe is loaded with modifications
    LaunchedEffect(uiState) {
        if (uiState is ImportViewModel.UiState.Editing) {
            val editingState = uiState as ImportViewModel.UiState.Editing
            if (editingState.tagModifications?.isNotEmpty() == true && !showTagModificationDialog) {
                showTagModificationDialog = true
            }
        }
    }

    // Show error messages via Snackbar
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ImportViewModel.UiState.Input -> {
                state.errorMessage?.let { error ->
                    snackbarHostState.showSnackbar(
                        message = error,
                        duration = SnackbarDuration.Long
                    )
                }
            }
            is ImportViewModel.UiState.Editing -> {
                state.errorMessage?.let { error ->
                    snackbarHostState.showSnackbar(
                        message = error,
                        duration = SnackbarDuration.Long
                    )
                }
            }
            else -> { /* No error to show */ }
        }
    }

    // Auto-save when navigating back (if recipe was successfully parsed)
    fun handleBack() {
        when (val state = uiState) {
            is ImportViewModel.UiState.Editing -> {
                // Validate before saving
                when {
                    state.recipe.title.isBlank() -> {
                        viewModel.showError("Title is required")
                    }
                    state.recipe.ingredients.isEmpty() -> {
                        viewModel.showError("At least one ingredient is required")
                    }
                    state.recipe.instructions.isEmpty() -> {
                        viewModel.showError("At least one instruction step is required")
                    }
                    else -> {
                        viewModel.saveRecipe(state.recipe)
                        onSaveComplete()
                    }
                }
            }
            else -> {
                onNavigateBack()
            }
        }
    }

    // Handle system back button
    BackHandler {
        handleBack()
    }

    // Discard confirmation dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard imported recipe?") },
            text = { Text("Are you sure you want to discard this recipe? All changes will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        viewModel.reset()
                        onNavigateBack()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Import from URL") },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Show discard button when editing
                    if (uiState is ImportViewModel.UiState.Editing) {
                        IconButton(onClick = { showDiscardDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Discard",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ImportViewModel.UiState.Input -> {
                InputUrlContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    url = url,
                    onUrlChange = { url = it },
                    onFetchClick = {
                        if (url.isNotBlank()) {
                            viewModel.fetchRecipeFromUrl(url)
                        }
                    },
                    errorMessage = state.errorMessage
                )
            }

            is ImportViewModel.UiState.Loading -> {
                LoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is ImportViewModel.UiState.Editing -> {
                RecipePreviewContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    recipe = state.recipe,
                    existingTags = existingTags,
                    onRecipeChange = { viewModel.updateRecipe(it) },
                    onSave = {
                        viewModel.saveRecipe(state.recipe)
                        onSaveComplete()
                    },
                    errorMessage = state.errorMessage
                )
            }

            is ImportViewModel.UiState.Saved -> {
                LaunchedEffect(Unit) {
                    onSaveComplete()
                }
            }
        }
    }

    // Tag modification dialog
    if (showTagModificationDialog && uiState is ImportViewModel.UiState.Editing) {
        val editingState = uiState as ImportViewModel.UiState.Editing
        editingState.tagModifications?.let { modifications ->
            TagModificationDialog(
                modifications = modifications,
                onAccept = { acceptedTags ->
                    viewModel.applyTagModifications(acceptedTags)
                    showTagModificationDialog = false
                },
                onDismiss = {
                    // Keep the standardized tags
                    showTagModificationDialog = false
                }
            )
        }
    }
}

/**
 * URL input content
 */
@Composable
private fun InputUrlContent(
    modifier: Modifier = Modifier,
    url: String,
    onUrlChange: (String) -> Unit,
    onFetchClick: () -> Unit,
    errorMessage: String?
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter Recipe URL",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Paste a link from your favorite recipe website. We'll automatically extract the recipe details.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text("Recipe URL") },
            placeholder = { Text("https://www.skinnytaste.com/...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3,
            isError = errorMessage != null
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = onFetchClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = url.isNotBlank()
        ) {
            Text("Fetch Recipe")
        }

        // Example URLs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Supported Sites",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "• Skinnytaste\n• AllRecipes\n• Food Network\n• Bon Appétit\n• Most sites with Schema.org markup",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Loading content
 */
@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Fetching recipe...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Recipe Preview Content - Shows imported recipe in a clean, formatted preview
 * with minimal inline editing (especially for tags)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipePreviewContent(
    modifier: Modifier = Modifier,
    recipe: Recipe,
    existingTags: List<String>,
    onRecipeChange: (Recipe) -> Unit,
    onSave: () -> Unit,
    errorMessage: String?
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
        // Header with action button
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

        // Recipe Photo
        recipe.photoPath?.let { photoUrl ->
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
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

        // Save button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = recipe.title.isNotBlank() && recipe.ingredients.isNotEmpty() && recipe.instructions.isNotEmpty()
        ) {
            Text(
                text = "Save Recipe",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Help text
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
 * Helper enum for edit dialog
 */
private enum class EditField {
    TITLE, METADATA, INGREDIENTS, INSTRUCTIONS
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
                        OutlinedTextField(
                            value = editedRecipe.servings.toString(),
                            onValueChange = {
                                val servings = it.toIntOrNull() ?: editedRecipe.servings
                                editedRecipe = editedRecipe.copy(servings = servings)
                            },
                            label = { Text("Servings") },
                            modifier = Modifier.fillMaxWidth()
                        )
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
 * Edit recipe content - Similar to AddEditRecipeScreen but simplified
 */
@Composable
private fun EditRecipeContent(
    modifier: Modifier = Modifier,
    recipe: Recipe,
    existingTags: List<String>,
    onRecipeChange: (Recipe) -> Unit,
    onSave: () -> Unit,
    errorMessage: String?
) {
    var tagInput by remember { mutableStateOf("") }
    var showTagSuggestions by remember { mutableStateOf(false) }

    // Filter existing tags based on user input
    val suggestedTags = remember(tagInput, existingTags, recipe.tags) {
        if (tagInput.length >= 2) {
            existingTags
                .filter { it.contains(tagInput, ignoreCase = true) }
                .filter { candidate ->
                    // Don't suggest if already in recipe tags
                    if (recipe.tags.contains(candidate)) return@filter false

                    // Don't suggest if it's a duplicate/subset of existing tags
                    // e.g., don't suggest "creamy soup" if "soup" is already there
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
        Text(
            text = "Review & Edit",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Review the imported recipe and make any necessary edits before saving.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recipe Photo
        recipe.photoPath?.let { photoUrl ->
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

        // Title
        OutlinedTextField(
            value = recipe.title,
            onValueChange = { onRecipeChange(recipe.copy(title = it)) },
            label = { Text("Title*") },
            modifier = Modifier.fillMaxWidth(),
            isError = recipe.title.isBlank()
        )

        // Ingredients
        OutlinedTextField(
            value = recipe.ingredients.joinToString("\n"),
            onValueChange = {
                val ingredients = it.split("\n").filter { line -> line.isNotBlank() }
                onRecipeChange(recipe.copy(ingredients = ingredients))
            },
            label = { Text("Ingredients* (one per line)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 10,
            isError = recipe.ingredients.isEmpty()
        )

        // Instructions
        OutlinedTextField(
            value = recipe.instructions.joinToString("\n"),
            onValueChange = {
                val instructions = it.split("\n").filter { line -> line.isNotBlank() }
                onRecipeChange(recipe.copy(instructions = instructions))
            },
            label = { Text("Instructions* (one step per line)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 10,
            isError = recipe.instructions.isEmpty()
        )

        // Servings
        OutlinedTextField(
            value = recipe.servings.toString(),
            onValueChange = {
                val servings = it.toIntOrNull() ?: recipe.servings
                onRecipeChange(recipe.copy(servings = servings))
            },
            label = { Text("Servings") },
            modifier = Modifier.fillMaxWidth()
        )

        // Times
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = recipe.prepTimeMinutes?.toString() ?: "",
                onValueChange = {
                    val time = it.toIntOrNull()
                    onRecipeChange(recipe.copy(prepTimeMinutes = time))
                },
                label = { Text("Prep (min)") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = recipe.cookTimeMinutes?.toString() ?: "",
                onValueChange = {
                    val time = it.toIntOrNull()
                    onRecipeChange(recipe.copy(cookTimeMinutes = time))
                },
                label = { Text("Cook (min)") },
                modifier = Modifier.weight(1f)
            )
        }

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
            if (recipe.tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalSpacing = 6.dp,
                    verticalSpacing = 6.dp
                ) {
                    recipe.tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = {
                                val updatedTags = recipe.tags.filter { it != tag }
                                onRecipeChange(recipe.copy(tags = updatedTags))
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
                                val newTag = tagInput.trim().lowercase()
                                if (newTag.isNotBlank() && !recipe.tags.contains(newTag)) {
                                    val updatedTags = recipe.tags + newTag
                                    onRecipeChange(recipe.copy(tags = updatedTags))
                                }
                                tagInput = ""
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add tag")
                        }
                    }
                }
            )

            // Suggested tags
            if (suggestedTags.isNotEmpty()) {
                Text(
                    text = "Suggested:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalSpacing = 6.dp,
                    verticalSpacing = 6.dp
                ) {
                    suggestedTags.forEach { tag ->
                        SuggestionChip(
                            onClick = {
                                val updatedTags = recipe.tags + tag
                                onRecipeChange(recipe.copy(tags = updatedTags))
                                tagInput = ""
                            },
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // Save button
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = recipe.title.isNotBlank() &&
                    recipe.ingredients.isNotEmpty() &&
                    recipe.instructions.isNotEmpty()
        ) {
            Text("Save Recipe")
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
