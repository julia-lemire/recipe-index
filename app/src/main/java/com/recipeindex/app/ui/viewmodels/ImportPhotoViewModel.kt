package com.recipeindex.app.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.data.parsers.PhotoRecipeParser
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ImportPhotoViewModel - Handles photo/OCR recipe import flow
 */
class ImportPhotoViewModel(
    private val photoParser: PhotoRecipeParser,
    private val recipeManager: RecipeManager
) : ViewModel() {

    sealed class UiState {
        data object SelectPhoto : UiState()
        data object Loading : UiState()
        data class Editing(val recipe: Recipe, val errorMessage: String? = null) : UiState()
        data object Saved : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.SelectPhoto)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun fetchRecipeFromPhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Fetching recipe from photo: $uri"
            )

            val result = photoParser.parse(uri.toString())

            result.onSuccess { recipe ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Successfully parsed photo recipe: ${recipe.title}"
                )
                _uiState.value = UiState.Editing(recipe)
            }.onFailure { error ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Failed to parse photo: ${error.message}"
                )
                _uiState.value = UiState.SelectPhoto
                showError("Failed to parse photo: ${error.message}")
            }
        }
    }

    /**
     * Fetch recipe from multiple photos
     * Combines OCR text from all photos before parsing
     */
    fun fetchRecipeFromPhotos(uris: List<Uri>) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Fetching recipe from ${uris.size} photos"
            )

            val result = photoParser.parseMultiple(uris)

            result.onSuccess { recipe ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Successfully parsed recipe from ${uris.size} photos: ${recipe.title}"
                )
                _uiState.value = UiState.Editing(recipe)
            }.onFailure { error ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Failed to parse photos: ${error.message}"
                )
                _uiState.value = UiState.SelectPhoto
                showError("Failed to parse photos: ${error.message}")
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
                "Saving imported photo recipe: ${recipe.title}"
            )

            val result = if (recipe.id == 0L) {
                recipeManager.createRecipe(recipe)
            } else {
                recipeManager.updateRecipe(recipe)
            }

            result.onSuccess {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Successfully saved photo recipe: ${recipe.title}"
                )
                _uiState.value = UiState.Saved
            }.onFailure { error ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Failed to save photo recipe: ${error.message}"
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
            is UiState.Editing -> {
                _uiState.value = currentState.copy(errorMessage = message)
            }
            else -> {
                // For SelectPhoto state, log error
                DebugConfig.debugLog(DebugConfig.Category.IMPORT, "Error: $message")
            }
        }
    }

    fun reset() {
        _uiState.value = UiState.SelectPhoto
    }
}
