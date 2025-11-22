package com.recipeindex.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeindex.app.ui.components.RecipeImportPreview
import com.recipeindex.app.ui.components.TagModificationDialog
import com.recipeindex.app.ui.components.isRecipeValid
import com.recipeindex.app.ui.viewmodels.ImportViewModel
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.RecipeValidation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Import URL Screen - Enter URL and preview/edit imported recipe
 *
 * Uses shared RecipeImportPreview component for consistent preview experience.
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

    // Selected images - lifted to this level so handleBack can access it
    var selectedImageUrls by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Initialize selected images when entering Editing state
    LaunchedEffect(uiState) {
        if (uiState is ImportViewModel.UiState.Editing) {
            val editingState = uiState as ImportViewModel.UiState.Editing
            // Only initialize if not already set (first time entering Editing)
            if (selectedImageUrls.isEmpty() && editingState.imageUrls.isNotEmpty()) {
                selectedImageUrls = editingState.imageUrls.take(1).toSet()
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Initialized selectedImageUrls with first image: ${selectedImageUrls.size} selected"
                )
            }
        }
    }

    // Load existing tags for auto-suggestion
    LaunchedEffect(Unit) {
        existingTags = withContext(Dispatchers.IO) {
            viewModel.getAllExistingTags()
        }
    }

    // Show tag modification dialog when recipe is loaded with modifications
    LaunchedEffect(uiState) {
        DebugConfig.debugLog(
            DebugConfig.Category.UI,
            "[TAG_DIALOG_TRIGGER] LaunchedEffect triggered. uiState type: ${uiState::class.simpleName}, showTagModificationDialog: $showTagModificationDialog"
        )
        if (uiState is ImportViewModel.UiState.Editing) {
            val editingState = uiState as ImportViewModel.UiState.Editing
            val hasModifications = editingState.tagModifications?.any { it.wasModified } == true
            DebugConfig.debugLog(
                DebugConfig.Category.UI,
                "[TAG_DIALOG_TRIGGER] In Editing state. hasModifications: $hasModifications, showTagModificationDialog: $showTagModificationDialog"
            )
            // Only show dialog if there are tags that were actually modified (not just lowercased)
            if (hasModifications && !showTagModificationDialog) {
                DebugConfig.debugLog(
                    DebugConfig.Category.UI,
                    "[TAG_DIALOG_TRIGGER] Setting showTagModificationDialog = true"
                )
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
                // Validate before saving using centralized validation
                val error = RecipeValidation.getValidationError(state.recipe)
                if (error != null) {
                    viewModel.showError(error)
                } else {
                    // Pass selected images when saving
                    viewModel.saveRecipe(state.recipe, selectedImageUrls.toList())
                    onSaveComplete()
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Use shared preview component
                    RecipeImportPreview(
                        modifier = Modifier.weight(1f),
                        recipe = state.recipe,
                        imageUrls = state.imageUrls,
                        selectedImageUrls = selectedImageUrls,
                        existingTags = existingTags,
                        onRecipeChange = { viewModel.updateRecipe(it) },
                        onSelectedImagesChange = { selectedImageUrls = it },
                        errorMessage = state.errorMessage
                    )

                    // Save button at bottom
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveRecipe(state.recipe, selectedImageUrls.toList())
                                onSaveComplete()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(56.dp),
                            enabled = isRecipeValid(state.recipe)
                        ) {
                            Text(
                                text = "Save Recipe" + if (selectedImageUrls.isNotEmpty()) " (${selectedImageUrls.size} images)" else "",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
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
        DebugConfig.debugLog(
            DebugConfig.Category.UI,
            "[TAG_DIALOG_SHOW] Showing TagModificationDialog"
        )
        val editingState = uiState as ImportViewModel.UiState.Editing
        editingState.tagModifications?.let { modifications ->
            TagModificationDialog(
                modifications = modifications,
                onAccept = { acceptedTags ->
                    DebugConfig.debugLog(
                        DebugConfig.Category.UI,
                        "[TAG_DIALOG_SHOW] onAccept called with ${acceptedTags.size} tags"
                    )
                    viewModel.applyTagModifications(acceptedTags)
                    showTagModificationDialog = false
                },
                onDismiss = {
                    DebugConfig.debugLog(
                        DebugConfig.Category.UI,
                        "[TAG_DIALOG_SHOW] onDismiss called from screen"
                    )
                    // Keep the standardized tags, but clear modifications so dialog doesn't show again
                    viewModel.clearTagModifications()
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
