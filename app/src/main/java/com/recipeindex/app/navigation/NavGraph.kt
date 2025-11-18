package com.recipeindex.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for Recipe Index
 *
 * Sealed class ensures type-safe navigation
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object RecipeIndex : Screen("recipe_index", "Recipe Index", Icons.Default.List)
    object MealPlanning : Screen("meal_planning", "Meal Planning", Icons.Default.DateRange)
    object GroceryLists : Screen("grocery_lists", "Grocery Lists", Icons.Default.ShoppingCart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    // Recipe screens (not in drawer)
    object AddRecipe : Screen("add_recipe", "Add Recipe", Icons.Default.List)
    object EditRecipe : Screen("edit_recipe/{recipeId}", "Edit Recipe", Icons.Default.List) {
        fun createRoute(recipeId: Long) = "edit_recipe/$recipeId"
    }
    object RecipeDetail : Screen("recipe_detail/{recipeId}", "Recipe", Icons.Default.List) {
        fun createRoute(recipeId: Long) = "recipe_detail/$recipeId"
    }

    // Import screens (not in drawer)
    object ImportSourceSelection : Screen("import_source", "Import Recipe", Icons.Default.List)
    object ImportUrl : Screen("import_url", "Import from URL", Icons.Default.List)

    companion object {
        val drawerScreens = listOf(Home, RecipeIndex, MealPlanning, GroceryLists, Settings)
    }
}
