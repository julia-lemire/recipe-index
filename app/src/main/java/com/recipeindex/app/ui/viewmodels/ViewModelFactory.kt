package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.recipeindex.app.data.managers.GroceryListManager
import com.recipeindex.app.data.managers.MealPlanManager
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.data.parsers.PdfRecipeParser
import com.recipeindex.app.data.parsers.PhotoRecipeParser
import com.recipeindex.app.data.parsers.RecipeParser

/**
 * ViewModelFactory - Creates ViewModels with dependencies
 *
 * Follows dependency injection pattern without external DI framework
 */
class ViewModelFactory(
    private val recipeManager: RecipeManager,
    private val mealPlanManager: MealPlanManager,
    private val groceryListManager: GroceryListManager,
    private val urlRecipeParser: RecipeParser,
    private val pdfRecipeParser: PdfRecipeParser,
    private val photoRecipeParser: PhotoRecipeParser
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RecipeViewModel::class.java) -> {
                RecipeViewModel(recipeManager) as T
            }
            modelClass.isAssignableFrom(MealPlanViewModel::class.java) -> {
                MealPlanViewModel(mealPlanManager) as T
            }
            modelClass.isAssignableFrom(GroceryListViewModel::class.java) -> {
                GroceryListViewModel(groceryListManager) as T
            }
            modelClass.isAssignableFrom(ImportViewModel::class.java) -> {
                ImportViewModel(urlRecipeParser, recipeManager) as T
            }
            modelClass.isAssignableFrom(ImportPdfViewModel::class.java) -> {
                ImportPdfViewModel(pdfRecipeParser, recipeManager) as T
            }
            modelClass.isAssignableFrom(ImportPhotoViewModel::class.java) -> {
                ImportPhotoViewModel(photoRecipeParser, recipeManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
