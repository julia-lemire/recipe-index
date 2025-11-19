package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.managers.MealPlanManager
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * MealPlanViewModel - UI state for meal planning screens
 *
 * Delegates business logic to MealPlanManager
 * Handles UI state via StateFlow
 */
class MealPlanViewModel(
    private val mealPlanManager: MealPlanManager
) : ViewModel() {

    private val _mealPlans = MutableStateFlow<List<MealPlan>>(emptyList())
    val mealPlans: StateFlow<List<MealPlan>> = _mealPlans.asStateFlow()

    private val _currentMealPlan = MutableStateFlow<MealPlan?>(null)
    val currentMealPlan: StateFlow<MealPlan?> = _currentMealPlan.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val selectedDateRange: StateFlow<Pair<Long, Long>?> = _selectedDateRange.asStateFlow()

    init {
        loadMealPlans()
    }

    /**
     * Load all meal plans (default sorted by newest first)
     */
    fun loadMealPlans() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                mealPlanManager.getAllMealPlans().collect { plans ->
                    _mealPlans.value = plans
                    _isLoading.value = false
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Loaded ${plans.size} meal plans")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load meal plans: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "loadMealPlans failed", e)
            }
        }
    }

    /**
     * Load single meal plan by ID
     */
    fun loadMealPlan(planId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val plan = mealPlanManager.getMealPlanById(planId)
                _currentMealPlan.value = plan
                _isLoading.value = false
                DebugConfig.debugLog(DebugConfig.Category.UI, "Loaded meal plan: ${plan?.name}")
            } catch (e: Exception) {
                _error.value = "Failed to load meal plan: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "loadMealPlan failed", e)
            }
        }
    }

    /**
     * Search meal plans by name
     */
    fun searchMealPlans(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    loadMealPlans()
                } else {
                    mealPlanManager.searchMealPlans(query).collect { plans ->
                        _mealPlans.value = plans
                        DebugConfig.debugLog(DebugConfig.Category.UI, "Search found ${plans.size} meal plans")
                    }
                }
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "searchMealPlans failed", e)
            }
        }
    }

    /**
     * Filter meal plans by date range
     */
    fun filterByDateRange(startDate: Long, endDate: Long) {
        _selectedDateRange.value = Pair(startDate, endDate)
        viewModelScope.launch {
            try {
                mealPlanManager.getMealPlansByDateRange(startDate, endDate).collect { plans ->
                    _mealPlans.value = plans
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Filtered to ${plans.size} meal plans")
                }
            } catch (e: Exception) {
                _error.value = "Filter failed: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "filterByDateRange failed", e)
            }
        }
    }

    /**
     * Clear date range filter
     */
    fun clearDateFilter() {
        _selectedDateRange.value = null
        loadMealPlans()
    }

    /**
     * Search by tag (e.g., "Thanksgiving", "Chicken")
     */
    fun searchByTag(tag: String) {
        viewModelScope.launch {
            try {
                mealPlanManager.searchMealPlansByTag(tag).collect { plans ->
                    _mealPlans.value = plans
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Tag search found ${plans.size} meal plans")
                }
            } catch (e: Exception) {
                _error.value = "Tag search failed: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "searchByTag failed", e)
            }
        }
    }

    /**
     * Create new meal plan
     */
    fun createMealPlan(mealPlan: MealPlan, onSuccess: (Long) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = mealPlanManager.createMealPlan(mealPlan)
                _isLoading.value = false

                result.onSuccess { planId ->
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Created meal plan: $planId")
                    onSuccess(planId)
                }.onFailure { e ->
                    _error.value = "Failed to create meal plan: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "createMealPlan failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to create meal plan: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "createMealPlan exception", e)
            }
        }
    }

    /**
     * Update existing meal plan
     */
    fun updateMealPlan(mealPlan: MealPlan, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = mealPlanManager.updateMealPlan(mealPlan)
                _isLoading.value = false

                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Updated meal plan: ${mealPlan.id}")
                    onSuccess()
                }.onFailure { e ->
                    _error.value = "Failed to update meal plan: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "updateMealPlan failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update meal plan: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "updateMealPlan exception", e)
            }
        }
    }

    /**
     * Delete meal plan
     */
    fun deleteMealPlan(mealPlan: MealPlan, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = mealPlanManager.deleteMealPlan(mealPlan)
                _isLoading.value = false

                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Deleted meal plan: ${mealPlan.id}")
                    onSuccess()
                }.onFailure { e ->
                    _error.value = "Failed to delete meal plan: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "deleteMealPlan failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete meal plan: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "deleteMealPlan exception", e)
            }
        }
    }

    /**
     * Duplicate meal plan with new name
     */
    fun duplicateMealPlan(mealPlan: MealPlan, newName: String, onSuccess: (Long) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = mealPlanManager.duplicateMealPlan(mealPlan, newName)
                _isLoading.value = false

                result.onSuccess { planId ->
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Duplicated meal plan: $planId")
                    onSuccess(planId)
                }.onFailure { e ->
                    _error.value = "Failed to duplicate meal plan: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "duplicateMealPlan failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to duplicate meal plan: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "duplicateMealPlan exception", e)
            }
        }
    }

    /**
     * Add recipe to existing meal plan
     */
    fun addRecipeToPlan(planId: Long, recipeId: Long) {
        viewModelScope.launch {
            try {
                val plan = mealPlanManager.getMealPlanById(planId)
                if (plan != null) {
                    val updatedRecipeIds = plan.recipeIds + recipeId
                    val updatedPlan = plan.copy(recipeIds = updatedRecipeIds)
                    updateMealPlan(updatedPlan)
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Added recipe $recipeId to plan $planId")
                } else {
                    _error.value = "Meal plan not found"
                    DebugConfig.debugLog(DebugConfig.Category.UI, "addRecipeToPlan: Plan $planId not found")
                }
            } catch (e: Exception) {
                _error.value = "Failed to add recipe to plan: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "addRecipeToPlan failed", e)
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Reset current meal plan
     */
    fun clearCurrentMealPlan() {
        _currentMealPlan.value = null
    }
}
