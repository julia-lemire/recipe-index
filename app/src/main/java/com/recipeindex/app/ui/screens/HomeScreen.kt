package com.recipeindex.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.recipeindex.app.R
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.ui.viewmodels.HomeViewModel
import com.recipeindex.app.utils.DebugConfig
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMenuClick: () -> Unit = {},
    onNavigateToImport: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToRecipes: () -> Unit = {},
    onNavigateToGroceryLists: () -> Unit = {},
    onNavigateToMealPlan: () -> Unit = {},
    onNavigateToRecipeDetail: (Long) -> Unit = {},
    onNavigateToMealPlanDetail: (Long) -> Unit = {},
    onToggleFavorite: (Long, Boolean) -> Unit = { _, _ -> },
    onDeleteRecipe: (Long) -> Unit = {},
    onAddToGroceryList: (Long) -> Unit = {},
    onAddToMealPlan: (Long) -> Unit = {},
    onShareRecipe: (Recipe) -> Unit = {}
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "HomeScreen composed")

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Recipe Index",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logo_header),
                            contentDescription = "Recipe Index",
                            modifier = Modifier.size(28.dp),
                            tint = Color.Unspecified
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: navigate to search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
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
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    item {
                        QuickActionsSection(
                            onImportClick = onNavigateToImport,
                            onViewAllClick = onNavigateToRecipes,
                            onGroceryListClick = onNavigateToGroceryLists,
                            onMealPlanClick = onNavigateToMealPlan
                        )
                    }

                    item {
                        ThisWeekMealPlanSection(
                            mealPlan = state.thisWeeksMealPlan,
                            onMealPlanClick = { plan -> onNavigateToMealPlanDetail(plan.id) }
                        )
                    }

                    if (state.recentRecipes.isNotEmpty()) {
                        item {
                            RecipeCarouselSection(
                                title = "Recent Recipes",
                                recipes = state.recentRecipes,
                                onRecipeClick = onNavigateToRecipeDetail,
                                onToggleFavorite = onToggleFavorite,
                                onAddToGroceryList = onAddToGroceryList,
                                onAddToMealPlan = onAddToMealPlan,
                                onShareRecipe = onShareRecipe,
                                onDeleteRecipe = onDeleteRecipe
                            )
                        }
                    }

                    if (state.favoriteRecipes.isNotEmpty()) {
                        item {
                            RecipeCarouselSection(
                                title = "Favorites",
                                recipes = state.favoriteRecipes,
                                onRecipeClick = onNavigateToRecipeDetail,
                                onToggleFavorite = onToggleFavorite,
                                onAddToGroceryList = onAddToGroceryList,
                                onAddToMealPlan = onAddToMealPlan,
                                onShareRecipe = onShareRecipe,
                                onDeleteRecipe = onDeleteRecipe
                            )
                        }
                    }

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
    onViewAllClick: () -> Unit,
    onGroceryListClick: () -> Unit,
    onMealPlanClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Quick actions",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.Download,
                label = "Import",
                onClick = onImportClick,
                containerColor = MaterialTheme.colorScheme.surface,
                iconColor = MaterialTheme.colorScheme.secondary,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.MenuBook,
                label = "Recipes",
                onClick = onViewAllClick,
                containerColor = MaterialTheme.colorScheme.primary,
                iconColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.ShoppingCart,
                label = "Grocery",
                onClick = onGroceryListClick,
                containerColor = MaterialTheme.colorScheme.primary,
                iconColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.CalendarMonth,
                label = "Meal plan",
                onClick = onMealPlanClick,
                containerColor = MaterialTheme.colorScheme.tertiary,
                iconColor = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            color = containerColor,
            border = border
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(22.dp),
                    tint = iconColor
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThisWeekMealPlanSection(
    mealPlan: MealPlan?,
    onMealPlanClick: (MealPlan) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "This week",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (mealPlan != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onMealPlanClick(mealPlan) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(15.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (mealPlan.startDate != null && mealPlan.endDate != null) {
                        val monthDay = SimpleDateFormat("MMM d", Locale.getDefault())
                        val dayOnly = SimpleDateFormat("d", Locale.getDefault())
                        val startStr = monthDay.format(Date(mealPlan.startDate)).uppercase()
                        val endStr = dayOnly.format(Date(mealPlan.endDate))
                        Text(
                            text = "$startStr – $endStr",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Text(
                        text = mealPlan.name.ifBlank { "Meal Plan" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "${mealPlan.recipeIds.size} recipes planned",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(15.dp)
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
    onRecipeClick: (Long) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onAddToGroceryList: (Long) -> Unit,
    onAddToMealPlan: (Long) -> Unit,
    onShareRecipe: (Recipe) -> Unit,
    onDeleteRecipe: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(recipes) { recipe ->
                Box(modifier = Modifier.width(280.dp)) {
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) },
                        onToggleFavorite = { onToggleFavorite(recipe.id, !recipe.isFavorite) },
                        onAddToGroceryList = { onAddToGroceryList(recipe.id) },
                        onAddToMealPlan = { onAddToMealPlan(recipe.id) },
                        onShare = { onShareRecipe(recipe) },
                        onDelete = { onDeleteRecipe(recipe.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateSection(onImportClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(17.dp)
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
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Import Recipe")
            }
        }
    }
}
