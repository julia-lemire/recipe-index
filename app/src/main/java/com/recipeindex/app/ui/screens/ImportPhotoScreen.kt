package com.recipeindex.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.recipeindex.app.ui.components.RecipeImportPreview
import com.recipeindex.app.ui.components.isRecipeValid
import com.recipeindex.app.ui.viewmodels.ImportPhotoViewModel
import java.io.File

/**
 * Import Photo Screen - Capture/select photos and preview/edit imported recipe
 *
 * Supports multiple photos since recipes often span multiple images.
 * Uses shared RecipeImportPreview component for consistent preview experience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPhotoScreen(
    viewModel: ImportPhotoViewModel,
    onNavigateBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages via Snackbar
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ImportPhotoViewModel.UiState.SelectPhoto -> {
                state.errorMessage?.let { error ->
                    snackbarHostState.showSnackbar(
                        message = error,
                        duration = SnackbarDuration.Long
                    )
                }
            }
            is ImportPhotoViewModel.UiState.Editing -> {
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

    // Camera launcher
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            selectedPhotoUris = selectedPhotoUris + tempPhotoUri!!
        }
    }

    // Gallery launcher (multiple selection)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedPhotoUris = selectedPhotoUris + uris
        }
    }

    // Auto-save when navigating back
    fun handleBack() {
        when (val state = uiState) {
            is ImportPhotoViewModel.UiState.Editing -> {
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
                title = { Text("Import from Photo") },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Show discard button when editing
                    if (uiState is ImportPhotoViewModel.UiState.Editing) {
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
            is ImportPhotoViewModel.UiState.SelectPhoto -> {
                SelectPhotoContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    selectedPhotoUris = selectedPhotoUris,
                    onTakePhoto = {
                        // Create temp file for camera using FileProvider
                        val photoUri = createTempPhotoUri(context)
                        tempPhotoUri = photoUri
                        cameraLauncher.launch(photoUri)
                    },
                    onSelectFromGallery = {
                        galleryLauncher.launch("image/*")
                    },
                    onRemovePhoto = { uri ->
                        selectedPhotoUris = selectedPhotoUris.filter { it != uri }
                    },
                    onProcess = {
                        if (selectedPhotoUris.isNotEmpty()) {
                            viewModel.fetchRecipeFromPhotos(selectedPhotoUris)
                        }
                    },
                    errorMessage = state.errorMessage
                )
            }

            is ImportPhotoViewModel.UiState.Loading -> {
                LoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    message = "Extracting text from photos..."
                )
            }

            is ImportPhotoViewModel.UiState.Editing -> {
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

            is ImportPhotoViewModel.UiState.Saved -> {
                LaunchedEffect(Unit) {
                    onSaveComplete()
                }
            }
        }
    }
}

/**
 * Select photo(s) content - supports multiple photos
 */
@Composable
private fun SelectPhotoContent(
    modifier: Modifier = Modifier,
    selectedPhotoUris: List<Uri>,
    onTakePhoto: () -> Unit,
    onSelectFromGallery: () -> Unit,
    onRemovePhoto: (Uri) -> Unit,
    onProcess: () -> Unit,
    errorMessage: String? = null
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Capture or Select Photos",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Add one or more photos of the recipe. We'll extract and parse the text from all images.",
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

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onTakePhoto,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Camera")
            }

            Button(
                onClick = onSelectFromGallery,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gallery")
            }
        }

        // Selected photos preview
        if (selectedPhotoUris.isNotEmpty()) {
            Text(
                text = "${selectedPhotoUris.size} photo(s) selected",
                style = MaterialTheme.typography.titleSmall
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedPhotoUris) { uri ->
                    Box {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected photo",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        // Remove button
                        IconButton(
                            onClick = { onRemovePhoto(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(32.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Process button
            Button(
                onClick = onProcess,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedPhotoUris.isNotEmpty()
            ) {
                Text("Extract Recipe from ${selectedPhotoUris.size} Photo(s)")
            }
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
                    text = "Tips for Best Results",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = """
                        • Add multiple photos if the recipe spans several images
                        • Ensure good lighting and focus
                        • Include all recipe sections (ingredients, instructions, etc.)
                        • Hold camera steady to avoid blur

                        We'll combine text from all photos into a single recipe.
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

/**
 * Create a temporary photo URI using FileProvider
 * This creates a real file that the camera can save to
 */
private fun createTempPhotoUri(context: Context): Uri {
    // Create temp directory if it doesn't exist
    val tempDir = File(context.cacheDir, "camera")
    if (!tempDir.exists()) {
        tempDir.mkdirs()
    }

    // Create temp file
    val tempFile = File.createTempFile(
        "recipe_photo_${System.currentTimeMillis()}",
        ".jpg",
        tempDir
    )

    // Get content URI using FileProvider
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
