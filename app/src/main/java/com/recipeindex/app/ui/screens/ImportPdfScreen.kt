package com.recipeindex.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeindex.app.ui.components.RecipeImportPreview
import com.recipeindex.app.ui.components.isRecipeValid
import com.recipeindex.app.ui.viewmodels.BaseFileImportViewModel
import com.recipeindex.app.ui.viewmodels.ImportPdfViewModel

/**
 * Import PDF Screen - Select PDF file and preview/edit imported recipe
 *
 * Uses shared RecipeImportPreview component for consistent preview experience.
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
        val errorMessage = (uiState as? BaseFileImportViewModel.BaseUiState)?.errorMessage
        errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
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
        val state = uiState
        if (state is BaseFileImportViewModel.EditingState) {
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
        } else {
            onNavigateBack()
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
                    if (uiState is BaseFileImportViewModel.EditingState) {
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
            is ImportPdfViewModel.SelectFile -> {
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

            is BaseFileImportViewModel.LoadingState -> {
                LoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    message = "Extracting text from PDF..."
                )
            }

            is BaseFileImportViewModel.EditingState -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Use shared preview component
                    RecipeImportPreview(
                        modifier = Modifier.weight(1f),
                        recipe = state.recipe,
                        onRecipeChange = { viewModel.updateRecipe(it) },
                        errorMessage = state.errorMessage
                    )

                    // Save button at bottom
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveRecipe(state.recipe)
                                onSaveComplete()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(56.dp),
                            enabled = isRecipeValid(state.recipe)
                        ) {
                            Text(
                                text = "Save Recipe",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            is BaseFileImportViewModel.SavedState -> {
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
 * Loading content
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
