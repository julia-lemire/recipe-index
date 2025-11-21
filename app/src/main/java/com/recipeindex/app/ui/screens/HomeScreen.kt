package com.recipeindex.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.ui.components.RecipeCard
import com.recipeindex.app.ui.viewmodels.HomeViewModel
import com.recipeindex.app.utils.DebugConfig
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home Screen - Landing page with quick actions and recipe highlights
 *
 * Layout:
 * - Quick action buttons (Import, Create, View All)
 * - This week's meal plan
 * - Recent recipes (horizontal scroll)
 * - Favorite recipes (horizontal scroll)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMenuClick: () -> Unit = {},
    onNavigateToImport: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToRecipes: () -> Unit = {},
    onNavigateToRecipeDetail: (Long) -> Unit = {},
    onNavigateToMealPlanDetail: (Long) -> Unit = {}
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "HomeScreen composed")

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is HomeViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is HomeViewModel.UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is HomeViewModel.UiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    // Quick Actions
                    item {
                        QuickActionsSection(
                            onImportClick = onNavigateToImport,
                            onCreateClick = onNavigateToCreate,
                            onViewAllClick = onNavigateToRecipes
                        )
                    }

                    // This Week's Meal Plan
                    item {
                        ThisWeekMealPlanSection(
                            mealPlan = state.thisWeeksMealPlan,
                            onMealPlanClick = { plan ->
                                onNavigateToMealPlanDetail(plan.id)
                            }
                        )
                    }

                    // Recent Recipes
                    if (state.recentRecipes.isNotEmpty()) {
                        item {
                            RecipeCarouselSection(
                                title = "Recent Recipes",
                                recipes = state.recentRecipes,
                                onRecipeClick = onNavigateToRecipeDetail
                            )
                        }
                    }

                    // Favorite Recipes
                    if (state.favoriteRecipes.isNotEmpty()) {
                        item {
                            RecipeCarouselSection(
                                title = "Favorites",
                                recipes = state.favoriteRecipes,
                                onRecipeClick = onNavigateToRecipeDetail
                            )
                        }
                    }

                    // Empty state if no recipes
                    if (state.recentRecipes.isEmpty() && state.favoriteRecipes.isEmpty()) {
                        item {
                            EmptyStateSection(onImportClick = onNavigateToImport)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onImportClick: () -> Unit,
    onCreateClick: () -> Unit,
    onViewAllClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onImportClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Import")
            }

            OutlinedButton(
                onClick = onCreateClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Create")
            }

            FilledTonalButton(
                onClick = onViewAllClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("View All")
            }
        }
    }
}

@Composable
private fun ThisWeekMealPlanSection(
    mealPlan: MealPlan?,
    onMealPlanClick: (MealPlan) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "This Week's Meal Plan",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (mealPlan != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onMealPlanClick(mealPlan) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = mealPlan.name.ifBlank { "Meal Plan" },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    if (mealPlan.startDate != null && mealPlan.endDate != null) {
                        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                        val startStr = dateFormat.format(Date(mealPlan.startDate))
                        val endStr = dateFormat.format(Date(mealPlan.endDate))
                        Text(
                            text = "$startStr - $endStr",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Text(
                        text = "${mealPlan.recipeIds.size} recipes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No meals planned yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start planning your weekly meals in the Meal Planning section",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeCarouselSection(
    title: String,
    recipes: List<Recipe>,
    onRecipeClick: (Long) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes) { recipe ->
                Box(modifier = Modifier.width(280.dp)) {
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) },
                        onToggleFavorite = { /* Handled by RecipeCard internally */ },
                        onAddToGroceryList = { /* Handled by RecipeCard internally */ },
                        onAddToMealPlan = { /* Handled by RecipeCard internally */ },
                        onShare = { /* Handled by RecipeCard internally */ },
                        onDelete = { /* Handled by RecipeCard internally */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateSection(
    onImportClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "No recipes yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Get started by importing your first recipe from a URL or creating one from scratch",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Button(onClick = onImportClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Import Recipe")
            }
        }
    }
}
