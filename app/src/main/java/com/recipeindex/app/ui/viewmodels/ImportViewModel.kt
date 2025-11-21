package com.recipeindex.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.data.parsers.RecipeParser
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.MediaDownloader
import com.recipeindex.app.utils.TagStandardizer
import io.ktor.client.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for recipe import functionality
 */
class ImportViewModel(
    private val recipeParser: RecipeParser,
    private val recipeManager: RecipeManager,
    private val context: Context,
    private val httpClient: HttpClient
) : ViewModel() {

    private val mediaDownloader = MediaDownloader(context, httpClient)

    private val _uiState = MutableStateFlow<UiState>(UiState.Input())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * Fetch recipe from URL and parse it
     */
    fun fetchRecipeFromUrl(url: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                // Normalize URL: upgrade http:// to https://, add https:// if missing
                val normalizedUrl = normalizeUrl(url)
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Fetching recipe from URL: $normalizedUrl" + if (url != normalizedUrl) " (normalized from: $url)" else ""
                )

                // Check if parser is UrlRecipeParser and use parseWithMedia for image URLs
                val parseResult = if (recipeParser is com.recipeindex.app.data.parsers.UrlRecipeParser) {
                    recipeParser.parseWithMedia(normalizedUrl)
                } else {
                    // Fallback for other parsers
                    recipeParser.parse(normalizedUrl).map {
                        com.recipeindex.app.data.parsers.RecipeParseResult(recipe = it)
                    }
                }

                parseResult.onSuccess { result ->
                    val recipe = result.recipe
                    val imageUrls = result.imageUrls

                    DebugConfig.debugLog(
                        DebugConfig.Category.IMPORT,
                        "Successfully parsed recipe: ${recipe.title} with ${imageUrls.size} image URLs"
                    )

                    // Track tag modifications if tags were standardized
                    val tagModifications = if (recipe.tags.isNotEmpty()) {
                        DebugConfig.debugLog(
                            DebugConfig.Category.TAG_STANDARDIZATION,
                            "Recipe: \"${recipe.title}\" from URL"
                        )
                        TagStandardizer.standardizeWithTracking(recipe.tags)
                    } else {
                        null
                    }

                    // Apply standardized tags to recipe
                    val standardizedRecipe = if (tagModifications != null) {
                        recipe.copy(tags = tagModifications.map { it.standardized })
                    } else {
                        recipe
                    }

                    _uiState.value = UiState.Editing(
                        recipe = standardizedRecipe,
                        tagModifications = tagModifications,
                        imageUrls = imageUrls
                    )
                }.onFailure { error ->
                    DebugConfig.debugLog(
                        DebugConfig.Category.IMPORT,
                        "Failed to parse recipe: ${error.message}"
                    )
                    _uiState.value = UiState.Input(
                        errorMessage = error.message ?: "Failed to fetch recipe from URL"
                    )
                }
            } catch (e: Exception) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Exception fetching recipe: ${e.message}"
                )
                _uiState.value = UiState.Input(
                    errorMessage = "Failed to fetch recipe: ${e.message}"
                )
            }
        }
    }

    /**
     * Update the recipe being edited
     */
    fun updateRecipe(recipe: Recipe) {
        _uiState.value = UiState.Editing(recipe = recipe)
    }

    /**
     * Save the imported recipe to the database
     * Downloads selected images before saving
     */
    fun saveRecipe(recipe: Recipe, selectedImageUrls: List<String> = emptyList()) {
        viewModelScope.launch {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Saving imported recipe: ${recipe.title} with ${selectedImageUrls.size} selected images"
            )

            // Download selected images if any
            val recipeWithMedia = if (selectedImageUrls.isNotEmpty()) {
                try {
                    DebugConfig.debugLog(
                        DebugConfig.Category.IMPORT,
                        "Downloading ${selectedImageUrls.size} images..."
                    )
                    val mediaItems = mediaDownloader.downloadMediaList(selectedImageUrls)
                    DebugConfig.debugLog(
                        DebugConfig.Category.IMPORT,
                        "Successfully downloaded ${mediaItems.size} images"
                    )
                    recipe.copy(mediaPaths = mediaItems)
                } catch (e: Exception) {
                    DebugConfig.debugLog(
                        DebugConfig.Category.IMPORT,
                        "Failed to download images: ${e.message}"
                    )
                    _uiState.value = UiState.Editing(
                        recipe = recipe,
                        errorMessage = "Failed to download images: ${e.message}"
                    )
                    return@launch
                }
            } else {
                recipe
            }

            // Save recipe using RecipeManager (inserts or updates based on ID)
            val result = if (recipeWithMedia.id == 0L) {
                recipeManager.createRecipe(recipeWithMedia)
            } else {
                recipeManager.updateRecipe(recipeWithMedia)
            }

            result.onSuccess {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Successfully saved recipe: ${recipeWithMedia.title} with ${recipeWithMedia.mediaPaths.size} media items"
                )
                DebugConfig.debugLog(
                    DebugConfig.Category.TAG_STANDARDIZATION,
                    "Saved to database - Recipe: \"${recipeWithMedia.title}\" with tags: ${recipeWithMedia.tags.joinToString(", ") { "\"$it\"" }}"
                )
                _uiState.value = UiState.Saved
            }.onFailure { error ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Failed to save recipe: ${error.message}"
                )
                _uiState.value = UiState.Editing(
                    recipe = recipe,
                    errorMessage = "Failed to save recipe: ${error.message}"
                )
            }
        }
    }

    /**
     * Show error message
     */
    fun showError(message: String) {
        val currentState = _uiState.value
        when (currentState) {
            is UiState.Editing -> {
                _uiState.value = currentState.copy(errorMessage = message)
            }
            is UiState.Input -> {
                _uiState.value = currentState.copy(errorMessage = message)
            }
            else -> {
                // No-op for other states
            }
        }
    }

    /**
     * Reset to input state
     */
    fun reset() {
        _uiState.value = UiState.Input()
    }

    /**
     * Get all existing tags from the recipe database for auto-suggestion
     */
    suspend fun getAllExistingTags(): List<String> {
        return try {
            recipeManager.getAllRecipes()
                .first()
                .flatMap { it.tags }
                .distinct()
                .sorted()
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.IMPORT, "Failed to get existing tags", e)
            emptyList()
        }
    }

    /**
     * Apply tag modifications from the dialog
     */
    fun applyTagModifications(tags: List<String>) {
        val currentState = _uiState.value
        if (currentState is UiState.Editing) {
            _uiState.value = currentState.copy(recipe = currentState.recipe.copy(tags = tags))
        }
    }

    /**
     * Normalize URL: upgrade http:// to https://, add https:// if missing
     */
    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        return when {
            // Upgrade http:// to https://
            trimmed.startsWith("http://", ignoreCase = true) -> {
                "https://" + trimmed.substring(7)
            }
            // Add https:// if no protocol specified
            !trimmed.startsWith("https://", ignoreCase = true) && !trimmed.startsWith("http://", ignoreCase = true) -> {
                "https://$trimmed"
            }
            // Already https://, just return
            else -> trimmed
        }
    }

    /**
     * UI State for import flow
     */
    sealed class UiState {
        data class Input(val errorMessage: String? = null) : UiState()
        data object Loading : UiState()
        data class Editing(
            val recipe: Recipe,
            val errorMessage: String? = null,
            val tagModifications: List<TagStandardizer.TagModification>? = null,
            val imageUrls: List<String> = emptyList() // URLs of images found during parsing
        ) : UiState()
        data object Saved : UiState()
    }
}
