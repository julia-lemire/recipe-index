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
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object RecipeIndex : Screen("recipe_index", "Recipe Index", Icons.Default.List)
    data object MealPlanning : Screen("meal_planning", "Meal Planning", Icons.Default.DateRange)
    data object GroceryLists : Screen("grocery_lists", "Grocery Lists", Icons.Default.ShoppingCart)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val drawerScreens = listOf(Home, RecipeIndex, MealPlanning, GroceryLists, Settings)
    }
}
