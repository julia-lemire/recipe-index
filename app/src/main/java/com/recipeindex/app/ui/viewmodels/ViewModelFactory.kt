package com.recipeindex.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.recipeindex.app.data.managers.GroceryListManager
import com.recipeindex.app.data.managers.MealPlanManager
import com.recipeindex.app.data.managers.PantryStapleManager
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.data.managers.SettingsManager
import com.recipeindex.app.data.managers.SubstitutionManager
import com.recipeindex.app.data.parsers.PdfRecipeParser
import com.recipeindex.app.data.parsers.PhotoRecipeParser
import com.recipeindex.app.data.parsers.RecipeParser
import io.ktor.client.*

/**
 * ViewModelFactory - Creates ViewModels with dependencies
 *
 * Follows dependency injection pattern without external DI framework
 */
class ViewModelFactory(
    private val recipeManager: RecipeManager,
    private val mealPlanManager: MealPlanManager,
    private val groceryListManager: GroceryListManager,
    private val settingsManager: SettingsManager,
    private val substitutionManager: SubstitutionManager,
    private val pantryStapleManager: PantryStapleManager,
    private val urlRecipeParser: RecipeParser,
    private val pdfRecipeParser: PdfRecipeParser,
    private val photoRecipeParser: PhotoRecipeParser,
    private val context: Context,
    private val httpClient: HttpClient
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
                ImportViewModel(urlRecipeParser, recipeManager, context, httpClient) as T
            }
            modelClass.isAssignableFrom(ImportPdfViewModel::class.java) -> {
                ImportPdfViewModel(pdfRecipeParser, recipeManager) as T
            }
            modelClass.isAssignableFrom(ImportPhotoViewModel::class.java) -> {
                ImportPhotoViewModel(photoRecipeParser, recipeManager) as T
            }
            modelClass.isAssignableFrom(ImportTextViewModel::class.java) -> {
                ImportTextViewModel(recipeManager) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(settingsManager) as T
            }
            modelClass.isAssignableFrom(SubstitutionViewModel::class.java) -> {
                SubstitutionViewModel(substitutionManager) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(recipeManager, mealPlanManager) as T
            }
            modelClass.isAssignableFrom(PantryStapleViewModel::class.java) -> {
                PantryStapleViewModel(pantryStapleManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
