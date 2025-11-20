package com.recipeindex.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Search
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
    object Search : Screen("search", "Search", Icons.Default.Search)
    object MealPlanning : Screen("meal_planning", "Meal Planning", Icons.Default.DateRange)
    object GroceryLists : Screen("grocery_lists", "Grocery Lists", Icons.Default.ShoppingCart)
    object SubstitutionGuide : Screen("substitution_guide", "Substitution Guide", Icons.Default.SwapHoriz)
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
    object ImportPdf : Screen("import_pdf", "Import from PDF", Icons.Default.List)
    object ImportPhoto : Screen("import_photo", "Import from Photo", Icons.Default.List)

    // Meal planning screens (not in drawer)
    object AddMealPlan : Screen("add_meal_plan", "Add Meal Plan", Icons.Default.DateRange)
    object EditMealPlan : Screen("edit_meal_plan/{planId}", "Edit Meal Plan", Icons.Default.DateRange) {
        fun createRoute(planId: Long) = "edit_meal_plan/$planId"
    }

    // Grocery list screens (not in drawer)
    object GroceryListDetail : Screen("grocery_list/{listId}", "Grocery List", Icons.Default.ShoppingCart) {
        fun createRoute(listId: Long) = "grocery_list/$listId"
    }

    // Substitution screens (SubstitutionGuide is in drawer, AddEditSubstitution is not)
    object AddEditSubstitution : Screen("add_edit_substitution/{substitutionId}", "Add/Edit Substitution", Icons.Default.SwapHoriz) {
        fun createRoute(substitutionId: Long) = "add_edit_substitution/$substitutionId"
        fun createRouteNew() = "add_edit_substitution/-1"
    }

    companion object {
        val drawerScreens = listOf(Home, RecipeIndex, Search, MealPlanning, GroceryLists, SubstitutionGuide, Settings)
    }
}
