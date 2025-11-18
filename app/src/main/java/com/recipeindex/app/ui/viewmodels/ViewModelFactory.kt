package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.recipeindex.app.data.managers.RecipeManager

/**
 * ViewModelFactory - Creates ViewModels with dependencies
 *
 * Follows dependency injection pattern without external DI framework
 */
class ViewModelFactory(
    private val recipeManager: RecipeManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RecipeViewModel::class.java) -> {
                RecipeViewModel(recipeManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
