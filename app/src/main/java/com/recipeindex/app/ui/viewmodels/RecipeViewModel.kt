package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeLog
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * RecipeViewModel - UI state for recipe screens
 *
 * Follows design principles:
 * - Delegates all business logic to RecipeManager
 * - Handles only UI state via StateFlow
 * - ViewModels expose StateFlow, handle events via functions
 */
class RecipeViewModel(
    private val recipeManager: RecipeManager
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _currentRecipe = MutableStateFlow<Recipe?>(null)
    val currentRecipe: StateFlow<Recipe?> = _currentRecipe.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadRecipes()
    }

    /**
     * Load all recipes
     */
    fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                recipeManager.getAllRecipes().collect { recipeList ->
                    _recipes.value = recipeList
                    _isLoading.value = false  // Set to false after first emission
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Loaded ${recipeList.size} recipes")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load recipes: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "loadRecipes failed", e)
            }
        }
    }

    /**
     * Load single recipe by ID
     */
    fun loadRecipe(recipeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                recipeManager.getRecipeById(recipeId).collect { recipe ->
                    _currentRecipe.value = recipe
                    _isLoading.value = false  // Set to false after first emission
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Loaded recipe: ${recipe?.title}")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load recipe: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "loadRecipe failed", e)
            }
        }
    }

    /**
     * Search recipes
     */
    fun searchRecipes(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    loadRecipes()
                } else {
                    recipeManager.searchRecipes(query).collect { recipeList ->
                        _recipes.value = recipeList
                        DebugConfig.debugLog(DebugConfig.Category.UI, "Search found ${recipeList.size} recipes")
                    }
                }
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "searchRecipes failed", e)
            }
        }
    }

    /**
     * Create new recipe
     */
    fun createRecipe(recipe: Recipe, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = recipeManager.createRecipe(recipe)
                result.onSuccess { recipeId ->
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Recipe created: $recipeId")
                    onSuccess(recipeId)
                }.onFailure { exception ->
                    _error.value = "Failed to create recipe: ${exception.message}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to create recipe: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "createRecipe failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update existing recipe
     */
    fun updateRecipe(recipe: Recipe, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = recipeManager.updateRecipe(recipe)
                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Recipe updated: ${recipe.id}")
                    onSuccess()
                }.onFailure { exception ->
                    _error.value = "Failed to update recipe: ${exception.message}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to update recipe: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "updateRecipe failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete recipe
     */
    fun deleteRecipe(recipeId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = recipeManager.deleteRecipe(recipeId)
                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Recipe deleted: $recipeId")
                    onSuccess()
                }.onFailure { exception ->
                    _error.value = "Failed to delete recipe: ${exception.message}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete recipe: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "deleteRecipe failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggle favorite status
     */
    fun toggleFavorite(recipeId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                recipeManager.toggleFavorite(recipeId, isFavorite)
                DebugConfig.debugLog(DebugConfig.Category.UI, "Favorite toggled: $recipeId")
            } catch (e: Exception) {
                _error.value = "Failed to update favorite: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "toggleFavorite failed", e)
            }
        }
    }

    /**
     * Get logs for a recipe
     */
    fun getLogsForRecipe(recipeId: Long): Flow<List<RecipeLog>> {
        return recipeManager.getLogsForRecipe(recipeId)
    }

    /**
     * Mark recipe as made
     */
    fun markRecipeAsMade(recipeId: Long, notes: String? = null, rating: Int? = null, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = recipeManager.markRecipeAsMade(recipeId, notes, rating)
                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Recipe marked as made: $recipeId")
                    onSuccess()
                }.onFailure { exception ->
                    _error.value = "Failed to mark recipe as made: ${exception.message}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to mark recipe as made: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "markRecipeAsMade failed", e)
            }
        }
    }

    /**
     * Delete a log entry
     */
    fun deleteLog(logId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = recipeManager.deleteLog(logId)
                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Log deleted: $logId")
                    onSuccess()
                }.onFailure { exception ->
                    _error.value = "Failed to delete log: ${exception.message}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete log: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "deleteLog failed", e)
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}
