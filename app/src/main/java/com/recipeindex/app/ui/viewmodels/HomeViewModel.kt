package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.managers.MealPlanManager
import com.recipeindex.app.data.managers.RecipeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * HomeViewModel - Manages data for the home screen
 */
class HomeViewModel(
    private val recipeManager: RecipeManager,
    private val mealPlanManager: MealPlanManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    /**
     * Load all data for home screen
     */
    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                // Get this week's meal plans (plans that include today)
                val today = System.currentTimeMillis()
                val allMealPlans = mealPlanManager.getAllMealPlans().value
                val thisWeeksPlan = allMealPlans.firstOrNull { plan ->
                    // Check if plan includes today
                    val startDate = plan.startDate ?: return@firstOrNull false
                    val endDate = plan.endDate ?: return@firstOrNull false
                    today >= startDate && today <= endDate
                }

                // Get recent recipes (last 5 by creation date)
                val allRecipes = recipeManager.getAllRecipes().value
                val recentRecipes = allRecipes
                    .sortedByDescending { it.createdAt }
                    .take(5)

                // Get favorite recipes (limit to 5)
                val favoriteRecipes = allRecipes
                    .filter { it.isFavorite }
                    .sortedByDescending { it.updatedAt }
                    .take(5)

                _uiState.value = UiState.Success(
                    thisWeeksMealPlan = thisWeeksPlan,
                    recentRecipes = recentRecipes,
                    favoriteRecipes = favoriteRecipes
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load home data")
            }
        }
    }

    /**
     * Refresh home data
     */
    fun refresh() {
        loadHomeData()
    }

    /**
     * UI State for home screen
     */
    sealed class UiState {
        data object Loading : UiState()
        data class Success(
            val thisWeeksMealPlan: MealPlan?,
            val recentRecipes: List<Recipe>,
            val favoriteRecipes: List<Recipe>
        ) : UiState()
        data class Error(val message: String) : UiState()
    }
}
