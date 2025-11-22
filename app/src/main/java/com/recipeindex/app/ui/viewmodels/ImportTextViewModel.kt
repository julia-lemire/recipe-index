package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeSource
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.data.parsers.TextRecipeParser
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ImportTextViewModel - Handles plain text recipe import flow
 *
 * Used for importing recipes from text files created using the recipe template.
 * Similar to ImportPdfViewModel but uses TextRecipeParser directly.
 */
class ImportTextViewModel(
    private val recipeManager: RecipeManager
) : ViewModel() {

    sealed class UiState {
        data class SelectFile(val errorMessage: String? = null) : UiState()
        data object Loading : UiState()
        data class Editing(val recipe: Recipe, val errorMessage: String? = null) : UiState()
        data object Saved : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.SelectFile())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * Parse recipe from plain text content
     */
    fun parseTextContent(text: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Parsing recipe from text file (${text.length} chars)"
            )

            val result = TextRecipeParser.parseText(
                text = text,
                source = RecipeSource.MANUAL,
                sourceIdentifier = "text_file_import"
            )

            result.onSuccess { recipe ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Successfully parsed text recipe: ${recipe.title}"
                )
                _uiState.value = UiState.Editing(recipe)
            }.onFailure { error ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Failed to parse text: ${error.message}"
                )
                _uiState.value = UiState.SelectFile(
                    errorMessage = "Failed to parse recipe from text: ${error.message}"
                )
            }
        }
    }

    fun updateRecipe(recipe: Recipe) {
        val currentState = _uiState.value
        if (currentState is UiState.Editing) {
            _uiState.value = currentState.copy(recipe = recipe, errorMessage = null)
        }
    }

    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Saving imported text recipe: ${recipe.title}"
            )

            val result = if (recipe.id == 0L) {
                recipeManager.createRecipe(recipe)
            } else {
                recipeManager.updateRecipe(recipe)
            }

            result.onSuccess {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Successfully saved text recipe: ${recipe.title}"
                )
                _uiState.value = UiState.Saved
            }.onFailure { error ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Failed to save text recipe: ${error.message}"
                )
                val currentState = _uiState.value
                if (currentState is UiState.Editing) {
                    _uiState.value = currentState.copy(
                        errorMessage = "Failed to save recipe: ${error.message}"
                    )
                }
            }
        }
    }

    fun showError(message: String) {
        val currentState = _uiState.value
        when (currentState) {
            is UiState.SelectFile -> {
                _uiState.value = currentState.copy(errorMessage = message)
            }
            is UiState.Editing -> {
                _uiState.value = currentState.copy(errorMessage = message)
            }
            else -> {
                DebugConfig.debugLog(DebugConfig.Category.IMPORT, "Error: $message")
            }
        }
    }

    fun reset() {
        _uiState.value = UiState.SelectFile()
    }
}
