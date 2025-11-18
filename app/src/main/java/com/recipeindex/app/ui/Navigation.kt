package com.recipeindex.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.recipeindex.app.navigation.Screen
import com.recipeindex.app.ui.screens.*
import com.recipeindex.app.ui.viewmodels.ImportViewModel
import com.recipeindex.app.ui.viewmodels.RecipeViewModel
import com.recipeindex.app.ui.viewmodels.ViewModelFactory
import com.recipeindex.app.utils.DebugConfig

/**
 * RecipeIndexNavigation - All navigation logic for Recipe Index
 *
 * Follows design principle: Navigation separated from MainActivity
 * MainActivity is just an orchestrator with no business logic
 */
@Composable
fun RecipeIndexNavigation(
    navController: NavHostController,
    viewModelFactory: ViewModelFactory,
    onMenuClick: () -> Unit
) {
    val recipeViewModel: RecipeViewModel = viewModel(factory = viewModelFactory)
    val importViewModel: ImportViewModel = viewModel(factory = viewModelFactory)

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home screen
        composable(Screen.Home.route) {
            HomeScreen(onMenuClick = onMenuClick)
        }

        // Recipe Index (list)
        composable(Screen.RecipeIndex.route) {
            RecipeListScreen(
                viewModel = recipeViewModel,
                onAddRecipe = {
                    navController.navigate(Screen.AddRecipe.route)
                },
                onImportRecipe = {
                    navController.navigate(Screen.ImportSourceSelection.route)
                },
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                onMenuClick = onMenuClick
            )
        }

        // Add Recipe
        composable(Screen.AddRecipe.route) {
            AddEditRecipeScreen(
                recipe = null,
                onSave = { recipe ->
                    recipeViewModel.createRecipe(recipe) { recipeId ->
                        DebugConfig.debugLog(
                            DebugConfig.Category.NAVIGATION,
                            "Recipe created, navigating to detail: $recipeId"
                        )
                        navController.popBackStack()
                        navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        // Edit Recipe
        composable(
            route = Screen.EditRecipe.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable

            recipeViewModel.loadRecipe(recipeId)
            val currentRecipe by recipeViewModel.currentRecipe.collectAsState()

            currentRecipe?.let { recipe ->
                AddEditRecipeScreen(
                    recipe = recipe,
                    onSave = { updatedRecipe ->
                        recipeViewModel.updateRecipe(updatedRecipe) {
                            DebugConfig.debugLog(
                                DebugConfig.Category.NAVIGATION,
                                "Recipe updated, navigating back"
                            )
                            navController.popBackStack()
                        }
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Recipe Detail
        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable

            recipeViewModel.loadRecipe(recipeId)
            val currentRecipe by recipeViewModel.currentRecipe.collectAsState()

            currentRecipe?.let { recipe ->
                RecipeDetailScreen(
                    recipe = recipe,
                    onEdit = {
                        navController.navigate(Screen.EditRecipe.createRoute(recipeId))
                    },
                    onDelete = {
                        recipeViewModel.deleteRecipe(recipeId) {
                            DebugConfig.debugLog(
                                DebugConfig.Category.NAVIGATION,
                                "Recipe deleted, navigating to list"
                            )
                            navController.popBackStack(Screen.RecipeIndex.route, inclusive = false)
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    onToggleFavorite = { isFavorite ->
                        recipeViewModel.toggleFavorite(recipeId, isFavorite)
                    }
                )
            }
        }

        // Meal Planning
        composable(Screen.MealPlanning.route) {
            MealPlanningScreen(onMenuClick = onMenuClick)
        }

        // Grocery Lists
        composable(Screen.GroceryLists.route) {
            GroceryListScreen(onMenuClick = onMenuClick)
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(onMenuClick = onMenuClick)
        }

        // Import Source Selection
        composable(Screen.ImportSourceSelection.route) {
            ImportSourceSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSourceSelected = { importSource ->
                    when (importSource) {
                        ImportSource.URL -> {
                            navController.navigate(Screen.ImportUrl.route)
                        }
                        ImportSource.PDF -> {
                            // TODO: Implement PDF import
                        }
                        ImportSource.PHOTO -> {
                            // TODO: Implement photo import
                        }
                    }
                }
            )
        }

        // Import from URL
        composable(Screen.ImportUrl.route) {
            ImportUrlScreen(
                viewModel = importViewModel,
                onNavigateBack = {
                    importViewModel.reset()
                    navController.popBackStack()
                },
                onSaveComplete = {
                    DebugConfig.debugLog(
                        DebugConfig.Category.NAVIGATION,
                        "Recipe imported, navigating to recipe list"
                    )
                    importViewModel.reset()
                    // Pop back to recipe list, clearing import screens from stack
                    navController.popBackStack(Screen.RecipeIndex.route, inclusive = false)
                }
            )
        }
    }
}
