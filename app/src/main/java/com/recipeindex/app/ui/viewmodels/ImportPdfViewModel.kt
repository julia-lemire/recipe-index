package com.recipeindex.app.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.data.parsers.PdfRecipeParser
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.launch

/**
 * ImportPdfViewModel - Handles PDF recipe import flow
 *
 * Extends BaseFileImportViewModel for shared updateRecipe/saveRecipe/showError/reset functionality.
 * Implements PDF-specific fetchRecipeFromPdf and initial SelectFile state.
 */
class ImportPdfViewModel(
    private val pdfParser: PdfRecipeParser,
    recipeManager: RecipeManager
) : BaseFileImportViewModel<ImportPdfViewModel.SelectFile>(recipeManager, "PDF") {

    /**
     * Initial state for PDF import - waiting for file selection
     */
    data class SelectFile(override val errorMessage: String? = null) : BaseUiState

    override fun createInitialState(): SelectFile = SelectFile()

    override fun setInitialError(message: String) {
        _uiState.value = SelectFile(errorMessage = message)
    }

    /**
     * Fetch recipe from PDF file
     */
    fun fetchRecipeFromPdf(uri: Uri) {
        viewModelScope.launch {
            setLoading()

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Fetching recipe from PDF: $uri"
            )

            val result = pdfParser.parse(uri.toString())

            result.onSuccess { recipe ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Successfully parsed PDF recipe: ${recipe.title}"
                )
                setEditing(recipe)
            }.onFailure { error ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Failed to parse PDF: ${error.message}"
                )
                setInitialError("Failed to parse PDF: ${error.message}")
            }
        }
    }
}
