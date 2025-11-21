package com.recipeindex.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
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
import com.recipeindex.app.ui.viewmodels.ImportPdfViewModel

/**
 * Import PDF Screen - Select PDF file and preview/edit imported recipe
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPdfScreen(
    viewModel: ImportPdfViewModel,
    onNavigateBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDiscardDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages via Snackbar
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ImportPdfViewModel.UiState.SelectFile -> {
                state.errorMessage?.let { error ->
                    snackbarHostState.showSnackbar(
                        message = error,
                        duration = SnackbarDuration.Long
                    )
                }
            }
            is ImportPdfViewModel.UiState.Editing -> {
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

    // PDF file picker launcher
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.fetchRecipeFromPdf(it) }
    }

    // Auto-save when navigating back (if recipe was successfully parsed)
    fun handleBack() {
        when (val state = uiState) {
            is ImportPdfViewModel.UiState.Editing -> {
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
                title = { Text("Import from PDF") },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Show discard button when editing
                    if (uiState is ImportPdfViewModel.UiState.Editing) {
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
            is ImportPdfViewModel.UiState.SelectFile -> {
                SelectPdfContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onSelectPdf = {
                        pdfPickerLauncher.launch("application/pdf")
                    },
                    errorMessage = state.errorMessage
                )
            }

            is ImportPdfViewModel.UiState.Loading -> {
                LoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    message = "Extracting text from PDF..."
                )
            }

            is ImportPdfViewModel.UiState.Editing -> {
                EditRecipeContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    recipe = state.recipe,
                    onRecipeChange = { viewModel.updateRecipe(it) },
                    onSave = {
                        viewModel.saveRecipe(state.recipe)
                        onSaveComplete()
                    },
                    errorMessage = state.errorMessage
                )
            }

            is ImportPdfViewModel.UiState.Saved -> {
                LaunchedEffect(Unit) {
                    onSaveComplete()
                }
            }
        }
    }
}

/**
 * Select PDF file content
 */
@Composable
private fun SelectPdfContent(
    modifier: Modifier = Modifier,
    onSelectPdf: () -> Unit,
    errorMessage: String? = null
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select PDF Recipe",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Choose a PDF file containing a recipe. We'll automatically extract and parse the recipe details.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Error message
        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // PDF selection button
        Button(
            onClick = onSelectPdf,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose PDF File")
        }

        // Info card
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
                    text = "How It Works",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = """
                        1. Select a PDF file from your device
                        2. We extract all text from the PDF
                        3. Smart parsing detects recipe sections
                        4. Review and edit before saving

                        Best results with PDFs that have clear section headers like "Ingredients:" and "Instructions:".
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Loading content - reused from ImportUrlScreen
 */
@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier,
    message: String
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
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Edit recipe content - reused pattern from ImportUrlScreen
 * (In production, this would be extracted to a shared composable)
 */
@Composable
private fun EditRecipeContent(
    modifier: Modifier = Modifier,
    recipe: Recipe,
    onRecipeChange: (Recipe) -> Unit,
    onSave: () -> Unit,
    errorMessage: String?
) {
    var tagInput by remember { mutableStateOf("") }

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

        // Recipe Photo (if available)
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

        // Servings and Serving Size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = recipe.servings.toString(),
                onValueChange = {
                    val servings = it.toIntOrNull() ?: recipe.servings
                    onRecipeChange(recipe.copy(servings = servings))
                },
                label = { Text("Servings") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = recipe.servingSize ?: "",
                onValueChange = {
                    onRecipeChange(recipe.copy(servingSize = it.ifBlank { null }))
                },
                label = { Text("Serving Size") },
                placeholder = { Text("e.g., 1 cup, 200g") },
                modifier = Modifier.weight(1f)
            )
        }

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

        // Tags - Chip-based UI
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tags",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Display existing tags as removable chips
            if (recipe.tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp
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
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove $tag",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Add new tag field
            OutlinedTextField(
                value = tagInput,
                onValueChange = { tagInput = it },
                label = { Text("Add tag") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                trailingIcon = {
                    if (tagInput.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val newTag = tagInput.trim()
                                if (newTag.isNotBlank() && !recipe.tags.contains(newTag)) {
                                    val updatedTags = recipe.tags + newTag
                                    onRecipeChange(recipe.copy(tags = updatedTags))
                                }
                                tagInput = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add tag"
                            )
                        }
                    }
                },
                singleLine = true
            )
        }

        // Tips & Substitutions (optional)
        OutlinedTextField(
            value = recipe.sourceTips ?: "",
            onValueChange = {
                onRecipeChange(recipe.copy(sourceTips = it.ifBlank { null }))
            },
            label = { Text("Tips & Substitutions (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 6
        )

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
 * FlowRow - Custom layout that arranges children in rows, wrapping to new row when needed
 * (Similar to CSS flexbox with flex-wrap)
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
        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()

        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        // Measure children and organize into rows
        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)

            // Check if we need to start a new row
            if (currentRow.isNotEmpty() &&
                currentRowWidth + horizontalSpacingPx + placeable.width > constraints.maxWidth
            ) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }

            // Add to current row
            currentRow.add(placeable)
            currentRowWidth += placeable.width + if (currentRow.size > 1) horizontalSpacingPx else 0
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }

        // Add last row if not empty
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        // Calculate total height
        val totalHeight = rows.sumOf { row ->
            row.maxOfOrNull { it.height } ?: 0
        } + (rows.size - 1) * verticalSpacingPx

        // Layout children
        layout(
            width = constraints.maxWidth,
            height = totalHeight
        ) {
            var yPosition = 0

            rows.forEach { row ->
                var xPosition = 0
                val rowHeight = row.maxOfOrNull { it.height } ?: 0

                row.forEach { placeable ->
                    placeable.placeRelative(
                        x = xPosition,
                        y = yPosition
                    )
                    xPosition += placeable.width + horizontalSpacingPx
                }

                yPosition += rowHeight + verticalSpacingPx
            }
        }
    }
}
