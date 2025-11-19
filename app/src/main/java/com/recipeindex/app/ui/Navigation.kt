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
import com.recipeindex.app.ui.viewmodels.GroceryListViewModel
import com.recipeindex.app.ui.viewmodels.ImportPdfViewModel
import com.recipeindex.app.ui.viewmodels.ImportPhotoViewModel
import com.recipeindex.app.ui.viewmodels.ImportViewModel
import com.recipeindex.app.ui.viewmodels.MealPlanViewModel
import com.recipeindex.app.ui.viewmodels.RecipeViewModel
import com.recipeindex.app.ui.viewmodels.SettingsViewModel
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
    val mealPlanViewModel: MealPlanViewModel = viewModel(factory = viewModelFactory)
    val groceryListViewModel: GroceryListViewModel = viewModel(factory = viewModelFactory)
    val importViewModel: ImportViewModel = viewModel(factory = viewModelFactory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)

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
                groceryListViewModel = groceryListViewModel,
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
            MealPlanningScreen(
                mealPlanViewModel = mealPlanViewModel,
                recipeViewModel = recipeViewModel,
                groceryListViewModel = groceryListViewModel,
                onAddMealPlan = {
                    navController.navigate(Screen.AddMealPlan.route)
                },
                onEditMealPlan = { planId ->
                    navController.navigate(Screen.EditMealPlan.createRoute(planId))
                },
                onMenuClick = onMenuClick
            )
        }

        // Add Meal Plan
        composable(Screen.AddMealPlan.route) {
            val recipes by recipeViewModel.recipes.collectAsState()

            AddEditMealPlanScreen(
                mealPlan = null,
                availableRecipes = recipes,
                onSave = { mealPlan ->
                    mealPlanViewModel.createMealPlan(mealPlan) { planId ->
                        DebugConfig.debugLog(
                            DebugConfig.Category.NAVIGATION,
                            "Meal plan created: $planId"
                        )
                        navController.popBackStack()
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        // Edit Meal Plan
        composable(
            route = Screen.EditMealPlan.route,
            arguments = listOf(navArgument("planId") { type = NavType.LongType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getLong("planId") ?: return@composable

            mealPlanViewModel.loadMealPlan(planId)
            val currentPlan by mealPlanViewModel.currentMealPlan.collectAsState()
            val recipes by recipeViewModel.recipes.collectAsState()

            currentPlan?.let { plan ->
                AddEditMealPlanScreen(
                    mealPlan = plan,
                    availableRecipes = recipes,
                    onSave = { updatedPlan ->
                        mealPlanViewModel.updateMealPlan(updatedPlan) {
                            DebugConfig.debugLog(
                                DebugConfig.Category.NAVIGATION,
                                "Meal plan updated"
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

        // Grocery Lists
        composable(Screen.GroceryLists.route) {
            GroceryListScreen(
                groceryListViewModel = groceryListViewModel,
                onViewList = { listId ->
                    navController.navigate(Screen.GroceryListDetail.createRoute(listId))
                },
                onMenuClick = onMenuClick
            )
        }

        // Grocery List Detail
        composable(
            route = Screen.GroceryListDetail.route,
            arguments = listOf(navArgument("listId") { type = NavType.LongType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong("listId") ?: return@composable

            val recipes by recipeViewModel.recipes.collectAsState()
            val mealPlans by mealPlanViewModel.mealPlans.collectAsState()

            GroceryListDetailScreen(
                listId = listId,
                groceryListViewModel = groceryListViewModel,
                availableRecipes = recipes,
                availableMealPlans = mealPlans,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onMenuClick = onMenuClick
            )
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
                            navController.navigate(Screen.ImportPdf.route)
                        }
                        ImportSource.PHOTO -> {
                            navController.navigate(Screen.ImportPhoto.route)
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

        // Import from PDF
        composable(Screen.ImportPdf.route) {
            val pdfImportViewModel: ImportPdfViewModel = viewModel(factory = viewModelFactory)

            ImportPdfScreen(
                viewModel = pdfImportViewModel,
                onNavigateBack = {
                    pdfImportViewModel.reset()
                    navController.popBackStack()
                },
                onSaveComplete = {
                    DebugConfig.debugLog(
                        DebugConfig.Category.NAVIGATION,
                        "PDF recipe imported, navigating to recipe list"
                    )
                    pdfImportViewModel.reset()
                    // Pop back to recipe list, clearing import screens from stack
                    navController.popBackStack(Screen.RecipeIndex.route, inclusive = false)
                }
            )
        }

        // Import from Photo
        composable(Screen.ImportPhoto.route) {
            val photoImportViewModel: ImportPhotoViewModel = viewModel(factory = viewModelFactory)

            ImportPhotoScreen(
                viewModel = photoImportViewModel,
                onNavigateBack = {
                    photoImportViewModel.reset()
                    navController.popBackStack()
                },
                onSaveComplete = {
                    DebugConfig.debugLog(
                        DebugConfig.Category.NAVIGATION,
                        "Photo recipe imported, navigating to recipe list"
                    )
                    photoImportViewModel.reset()
                    // Pop back to recipe list, clearing import screens from stack
                    navController.popBackStack(Screen.RecipeIndex.route, inclusive = false)
                }
            )
        }
    }
}
