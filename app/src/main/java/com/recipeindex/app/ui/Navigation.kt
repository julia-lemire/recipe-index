package com.recipeindex.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.recipeindex.app.navigation.Screen
import com.recipeindex.app.ui.components.GroceryListPickerDialog
import com.recipeindex.app.ui.screens.*
import com.recipeindex.app.ui.viewmodels.GroceryListViewModel
import com.recipeindex.app.ui.viewmodels.ImportPdfViewModel
import com.recipeindex.app.ui.viewmodels.ImportPhotoViewModel
import com.recipeindex.app.ui.viewmodels.ImportViewModel
import com.recipeindex.app.ui.viewmodels.MealPlanViewModel
import com.recipeindex.app.ui.viewmodels.RecipeViewModel
import com.recipeindex.app.ui.viewmodels.SettingsViewModel
import com.recipeindex.app.ui.viewmodels.SubstitutionViewModel
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
    val substitutionViewModel: SubstitutionViewModel = viewModel(factory = viewModelFactory)
    val homeViewModel: com.recipeindex.app.ui.viewmodels.HomeViewModel = viewModel(factory = viewModelFactory)

    // Initialize substitution database with default data on first run
    LaunchedEffect(Unit) {
        substitutionViewModel.initializeDefaultSubstitutions()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home screen
        composable(Screen.Home.route) {
            var showListPicker by remember { mutableStateOf(false) }
            var showMealPlanPicker by remember { mutableStateOf(false) }
            var selectedRecipeId by remember { mutableStateOf(0L) }

            val groceryLists by groceryListViewModel.groceryLists.collectAsState()
            val mealPlans by mealPlanViewModel.mealPlans.collectAsState()

            HomeScreen(
                viewModel = homeViewModel,
                onMenuClick = onMenuClick,
                onNavigateToImport = {
                    navController.navigate(Screen.ImportSourceSelection.route)
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.AddRecipe.route)
                },
                onNavigateToRecipes = {
                    navController.navigate(Screen.RecipeIndex.route)
                },
                onNavigateToRecipeDetail = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                onNavigateToMealPlanDetail = { planId ->
                    navController.navigate(Screen.EditMealPlan.createRoute(planId))
                },
                onToggleFavorite = { recipeId, isFavorite ->
                    recipeViewModel.toggleFavorite(recipeId, isFavorite)
                    homeViewModel.refresh()
                },
                onDeleteRecipe = { recipeId ->
                    recipeViewModel.deleteRecipe(recipeId) {
                        homeViewModel.refresh()
                    }
                },
                onAddToGroceryList = { recipeId ->
                    selectedRecipeId = recipeId
                    showListPicker = true
                },
                onAddToMealPlan = { recipeId ->
                    selectedRecipeId = recipeId
                    showMealPlanPicker = true
                },
                onShareRecipe = { recipe ->
                    com.recipeindex.app.utils.ShareHelper.shareRecipe(
                        navController.context,
                        recipe,
                        recipe.mediaPaths.firstOrNull { it.type == com.recipeindex.app.data.entities.MediaType.IMAGE }?.path
                            ?: recipe.photoPath
                    )
                }
            )

            // Grocery list picker dialog
            if (showListPicker) {
                GroceryListPickerDialog(
                    availableLists = groceryLists,
                    onDismiss = { showListPicker = false },
                    onListSelected = { listId ->
                        groceryListViewModel.addRecipesToList(listId, listOf(selectedRecipeId))
                        showListPicker = false
                    },
                    onCreateNew = { listName ->
                        groceryListViewModel.createList(listName) { listId ->
                            groceryListViewModel.addRecipesToList(listId, listOf(selectedRecipeId))
                        }
                        showListPicker = false
                    }
                )
            }

            // Meal plan picker dialog
            if (showMealPlanPicker) {
                com.recipeindex.app.ui.components.MealPlanPickerDialog(
                    availablePlans = mealPlans,
                    onDismiss = { showMealPlanPicker = false },
                    onPlanSelected = { planId ->
                        mealPlanViewModel.addRecipeToPlan(planId, selectedRecipeId)
                        showMealPlanPicker = false
                    },
                    onCreateNew = { planName ->
                        val newPlan = com.recipeindex.app.data.entities.MealPlan(
                            name = planName,
                            recipeIds = listOf(selectedRecipeId)
                        )
                        mealPlanViewModel.createMealPlan(newPlan)
                        showMealPlanPicker = false
                    }
                )
            }
        }

        // Recipe Index (list)
        composable(Screen.RecipeIndex.route) {
            RecipeListScreen(
                viewModel = recipeViewModel,
                groceryListViewModel = groceryListViewModel,
                mealPlanViewModel = mealPlanViewModel,
                settingsViewModel = settingsViewModel,
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

        // Search
        composable(Screen.Search.route) {
            SearchScreen(
                viewModel = recipeViewModel,
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
            val groceryLists by groceryListViewModel.groceryLists.collectAsState()
            val mealPlans by mealPlanViewModel.mealPlans.collectAsState()
            var showListPicker by remember { mutableStateOf(false) }
            var showMealPlanPicker by remember { mutableStateOf(false) }

            currentRecipe?.let { recipe ->
                RecipeDetailScreen(
                    recipe = recipe,
                    settingsViewModel = settingsViewModel,
                    substitutionViewModel = substitutionViewModel,
                    recipeViewModel = recipeViewModel,
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
                    },
                    onAddToGroceryList = {
                        showListPicker = true
                    },
                    onAddToMealPlan = {
                        showMealPlanPicker = true
                    }
                )

                // Grocery list picker dialog
                if (showListPicker) {
                    GroceryListPickerDialog(
                        availableLists = groceryLists,
                        onDismiss = { showListPicker = false },
                        onListSelected = { listId ->
                            groceryListViewModel.addRecipesToList(listId, listOf(recipeId))
                            showListPicker = false
                        },
                        onCreateNew = { listName ->
                            groceryListViewModel.createList(listName) { listId ->
                                groceryListViewModel.addRecipesToList(listId, listOf(recipeId))
                            }
                            showListPicker = false
                        }
                    )
                }

                // Meal plan picker dialog
                if (showMealPlanPicker) {
                    com.recipeindex.app.ui.components.MealPlanPickerDialog(
                        availablePlans = mealPlans,
                        onDismiss = { showMealPlanPicker = false },
                        onPlanSelected = { planId ->
                            mealPlanViewModel.addRecipeToPlan(planId, recipeId)
                            showMealPlanPicker = false
                        },
                        onCreateNew = { planName ->
                            // Create a new meal plan with just this recipe
                            val newPlan = com.recipeindex.app.data.entities.MealPlan(
                                name = planName,
                                recipeIds = listOf(recipeId),
                                startDate = null,
                                endDate = null
                            )
                            mealPlanViewModel.createMealPlan(newPlan) { _ ->
                                showMealPlanPicker = false
                            }
                        }
                    )
                }
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
                // Note: onAddToGroceryList not provided for new meal plans
                // Menu will not appear until plan is saved and edited
            )
        }

        // Edit Meal Plan
        composable(
            route = Screen.EditMealPlan.route,
            arguments = listOf(navArgument("planId") { type = NavType.LongType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getLong("planId") ?: return@composable

            // Only load meal plan when planId changes, not on every recomposition
            LaunchedEffect(planId) {
                mealPlanViewModel.loadMealPlan(planId)
            }

            // Clear current meal plan when leaving this screen
            DisposableEffect(Unit) {
                onDispose {
                    mealPlanViewModel.clearCurrentMealPlan()
                }
            }

            val currentPlan by mealPlanViewModel.currentMealPlan.collectAsState()
            val recipes by recipeViewModel.recipes.collectAsState()
            val groceryLists by groceryListViewModel.groceryLists.collectAsState()
            var showListPicker by remember { mutableStateOf(false) }

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
                    },
                    onAddToGroceryList = {
                        showListPicker = true
                    }
                )

                // Grocery list picker dialog
                if (showListPicker) {
                    GroceryListPickerDialog(
                        availableLists = groceryLists,
                        onDismiss = { showListPicker = false },
                        onListSelected = { listId ->
                            groceryListViewModel.addRecipesToList(listId, plan.recipeIds)
                            showListPicker = false
                        },
                        onCreateNew = { listName ->
                            groceryListViewModel.createList(listName) { listId ->
                                groceryListViewModel.addRecipesToList(listId, plan.recipeIds)
                            }
                            showListPicker = false
                        }
                    )
                }
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

        // Substitution Guide
        composable(Screen.SubstitutionGuide.route) {
            SubstitutionGuideScreen(
                viewModel = substitutionViewModel,
                onMenuClick = onMenuClick,
                onAddSubstitution = {
                    navController.navigate(Screen.AddEditSubstitution.createRouteNew())
                },
                onEditSubstitution = { substitutionId ->
                    navController.navigate(Screen.AddEditSubstitution.createRoute(substitutionId))
                }
            )
        }

        // Add/Edit Substitution
        composable(
            route = Screen.AddEditSubstitution.route,
            arguments = listOf(navArgument("substitutionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val substitutionId = backStackEntry.arguments?.getLong("substitutionId") ?: -1

            if (substitutionId == -1L) {
                // New substitution
                AddEditSubstitutionScreen(
                    substitution = null,
                    viewModel = substitutionViewModel,
                    onSave = {
                        DebugConfig.debugLog(
                            DebugConfig.Category.NAVIGATION,
                            "Substitution created, navigating back"
                        )
                        navController.popBackStack()
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            } else {
                // Edit existing substitution
                val substitution by substitutionViewModel.observeSubstitutionById(substitutionId).collectAsState(initial = null)

                substitution?.let { sub ->
                    AddEditSubstitutionScreen(
                        substitution = sub,
                        viewModel = substitutionViewModel,
                        onSave = {
                            DebugConfig.debugLog(
                                DebugConfig.Category.NAVIGATION,
                                "Substitution updated, navigating back"
                            )
                            navController.popBackStack()
                        },
                        onCancel = {
                            navController.popBackStack()
                        }
                    )
                }
            }
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
                },
                onCreateManually = {
                    navController.navigate(Screen.AddRecipe.route)
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
