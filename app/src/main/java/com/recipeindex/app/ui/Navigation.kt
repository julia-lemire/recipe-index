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
    viewModelFactory: ViewModelFactory
) {
    val recipeViewModel: RecipeViewModel = viewModel(factory = viewModelFactory)

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home screen
        composable(Screen.Home.route) {
            HomeScreen()
        }

        // Recipe Index (list)
        composable(Screen.RecipeIndex.route) {
            RecipeListScreen(
                viewModel = recipeViewModel,
                onAddRecipe = {
                    navController.navigate(Screen.AddRecipe.route)
                },
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                }
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
            MealPlanningScreen()
        }

        // Grocery Lists
        composable(Screen.GroceryLists.route) {
            GroceryListScreen()
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
