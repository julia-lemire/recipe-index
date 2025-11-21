package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.data.parsers.RecipeParser
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.TagStandardizer
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
    private val recipeManager: RecipeManager
) : ViewModel() {

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

                val result = recipeParser.parse(normalizedUrl)

                result.onSuccess { recipe ->
                    DebugConfig.debugLog(
                        DebugConfig.Category.IMPORT,
                        "Successfully parsed recipe: ${recipe.title}"
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
                        tagModifications = tagModifications
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
     */
    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Saving imported recipe: ${recipe.title}"
            )

            // Save recipe using RecipeManager (inserts or updates based on ID)
            val result = if (recipe.id == 0L) {
                recipeManager.createRecipe(recipe)
            } else {
                recipeManager.updateRecipe(recipe)
            }

            result.onSuccess {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Successfully saved recipe: ${recipe.title}"
                )
                DebugConfig.debugLog(
                    DebugConfig.Category.TAG_STANDARDIZATION,
                    "Saved to database - Recipe: \"${recipe.title}\" with tags: ${recipe.tags.joinToString(", ") { "\"$it\"" }}"
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
            val tagModifications: List<TagStandardizer.TagModification>? = null
        ) : UiState()
        data object Saved : UiState()
    }
}
