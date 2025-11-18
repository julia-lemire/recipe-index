package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.data.parsers.RecipeParser

/**
 * ViewModelFactory - Creates ViewModels with dependencies
 *
 * Follows dependency injection pattern without external DI framework
 */
class ViewModelFactory(
    private val recipeManager: RecipeManager,
    private val recipeParser: RecipeParser
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RecipeViewModel::class.java) -> {
                RecipeViewModel(recipeManager) as T
            }
            modelClass.isAssignableFrom(ImportViewModel::class.java) -> {
                ImportViewModel(recipeParser, recipeManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
