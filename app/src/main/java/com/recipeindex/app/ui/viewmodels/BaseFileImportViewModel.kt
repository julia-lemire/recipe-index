package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * BaseFileImportViewModel - Shared base class for PDF and Photo import ViewModels
 *
 * Consolidates common functionality:
 * - updateRecipe()
 * - saveRecipe() (basic version without media download)
 * - showError()
 * - reset()
 *
 * Subclasses implement:
 * - UiState sealed class with source-specific initial state
 * - Source-specific fetch methods (fetchRecipeFromPdf, fetchRecipeFromPhoto)
 */
abstract class BaseFileImportViewModel<InitialState : BaseFileImportViewModel.BaseUiState>(
    protected val recipeManager: RecipeManager,
    protected val sourceType: String // "PDF" or "Photo" for logging
) : ViewModel() {

    /**
     * Base UI states shared by file import flows
     */
    interface BaseUiState {
        val errorMessage: String?
    }

    /**
     * Editing state - common across all file import types
     */
    data class EditingState(
        val recipe: Recipe,
        override val errorMessage: String? = null
    ) : BaseUiState

    /**
     * Saved state - indicates successful save
     */
    data object SavedState : BaseUiState {
        override val errorMessage: String? = null
    }

    /**
     * Loading state - indicates parsing in progress
     */
    data object LoadingState : BaseUiState {
        override val errorMessage: String? = null
    }

    protected val _uiState = MutableStateFlow<BaseUiState>(createInitialState())
    val uiState: StateFlow<BaseUiState> = _uiState.asStateFlow()

    /**
     * Create the initial state for this ViewModel
     * Subclasses override to provide their specific initial state (SelectFile, SelectPhoto, etc.)
     */
    protected abstract fun createInitialState(): InitialState

    /**
     * Update the recipe being edited
     */
    fun updateRecipe(recipe: Recipe) {
        val currentState = _uiState.value
        if (currentState is EditingState) {
            _uiState.value = EditingState(recipe = recipe, errorMessage = null)
        }
    }

    /**
     * Save the imported recipe to the database
     */
    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Saving imported $sourceType recipe: ${recipe.title}"
            )

            val result = if (recipe.id == 0L) {
                recipeManager.createRecipe(recipe)
            } else {
                recipeManager.updateRecipe(recipe)
            }

            result.onSuccess {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Successfully saved $sourceType recipe: ${recipe.title}"
                )
                _uiState.value = SavedState
            }.onFailure { error ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Failed to save $sourceType recipe: ${error.message}"
                )
                val currentState = _uiState.value
                if (currentState is EditingState) {
                    _uiState.value = EditingState(
                        recipe = recipe,
                        errorMessage = "Failed to save recipe: ${error.message}"
                    )
                }
            }
        }
    }

    /**
     * Show error message in current state
     */
    fun showError(message: String) {
        val currentState = _uiState.value
        _uiState.value = when (currentState) {
            is EditingState -> EditingState(
                recipe = currentState.recipe,
                errorMessage = message
            )
            else -> {
                // For initial states, let subclass handle via setInitialError
                setInitialError(message)
                return
            }
        }
    }

    /**
     * Set error on the initial state
     * Subclasses override to handle their specific initial state type
     */
    protected abstract fun setInitialError(message: String)

    /**
     * Reset to initial state
     */
    fun reset() {
        _uiState.value = createInitialState()
    }

    /**
     * Set to loading state
     */
    protected fun setLoading() {
        _uiState.value = LoadingState
    }

    /**
     * Set to editing state with parsed recipe
     */
    protected fun setEditing(recipe: Recipe) {
        _uiState.value = EditingState(recipe)
    }

    /**
     * Set error on initial state and transition to it
     */
    protected fun setInitialWithError(message: String) {
        setInitialError(message)
    }
}
