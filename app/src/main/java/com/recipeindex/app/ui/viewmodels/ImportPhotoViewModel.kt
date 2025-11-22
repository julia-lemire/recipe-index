package com.recipeindex.app.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.data.parsers.PhotoRecipeParser
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.launch

/**
 * ImportPhotoViewModel - Handles photo/OCR recipe import flow
 *
 * Extends BaseFileImportViewModel for shared updateRecipe/saveRecipe/showError/reset functionality.
 * Implements photo-specific fetchRecipeFromPhoto(s) and initial SelectPhoto state.
 */
class ImportPhotoViewModel(
    private val photoParser: PhotoRecipeParser,
    recipeManager: RecipeManager
) : BaseFileImportViewModel<ImportPhotoViewModel.SelectPhoto>(recipeManager, "Photo") {

    /**
     * Initial state for photo import - waiting for photo selection
     */
    data class SelectPhoto(override val errorMessage: String? = null) : BaseUiState

    override fun createInitialState(): SelectPhoto = SelectPhoto()

    override fun setInitialError(message: String) {
        _uiState.value = SelectPhoto(errorMessage = message)
    }

    /**
     * Fetch recipe from single photo
     */
    fun fetchRecipeFromPhoto(uri: Uri) {
        viewModelScope.launch {
            setLoading()

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
                setEditing(recipe)
            }.onFailure { error ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Failed to parse photo: ${error.message}"
                )
                setInitialError("Failed to parse photo: ${error.message}")
            }
        }
    }

    /**
     * Fetch recipe from multiple photos
     * Combines OCR text from all photos before parsing
     */
    fun fetchRecipeFromPhotos(uris: List<Uri>) {
        viewModelScope.launch {
            setLoading()

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
                setEditing(recipe)
            }.onFailure { error ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Failed to parse photos: ${error.message}"
                )
                setInitialError("Failed to parse photos: ${error.message}")
            }
        }
    }
}
