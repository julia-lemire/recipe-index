package com.recipeindex.app.data.managers

import com.recipeindex.app.data.dao.RecipeDao
import com.recipeindex.app.data.dao.RecipeLogDao
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeLog
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.flow.Flow

/**
 * RecipeManager - Business logic for recipe operations
 *
 * Follows Manager pattern:
 * - ViewModels delegate to this for all business logic
 * - Handles validation, coordination, multi-step operations
 * - Thin DAO handles simple CRUD only
 */
class RecipeManager(
    private val recipeDao: RecipeDao,
    private val recipeLogDao: RecipeLogDao
) {

    /**
     * Get all recipes as Flow for reactive UI updates
     */
    fun getAllRecipes(): Flow<List<Recipe>> {
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "getAllRecipes")
        return recipeDao.getAllRecipes()
    }

    /**
     * Get single recipe by ID
     */
    fun getRecipeById(recipeId: Long): Flow<Recipe?> {
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "getRecipeById: $recipeId")
        return recipeDao.getRecipeById(recipeId)
    }

    /**
     * Get favorite recipes
     */
    fun getFavoriteRecipes(): Flow<List<Recipe>> {
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "getFavoriteRecipes")
        return recipeDao.getFavoriteRecipes()
    }

    /**
     * Search recipes by title
     */
    fun searchRecipes(query: String): Flow<List<Recipe>> {
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "searchRecipes: $query")
        return recipeDao.searchRecipes(query)
    }

    /**
     * Create new recipe with validation
     */
    suspend fun createRecipe(recipe: Recipe): Result<Long> {
        return try {
            validateRecipe(recipe)

            val updatedRecipe = recipe.copy(
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val recipeId = recipeDao.insertRecipe(updatedRecipe)
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "createRecipe: $recipeId")
            Result.success(recipeId)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "createRecipe failed", e)
            Result.failure(e)
        }
    }

    /**
     * Update existing recipe
     */
    suspend fun updateRecipe(recipe: Recipe): Result<Unit> {
        return try {
            validateRecipe(recipe)

            val updatedRecipe = recipe.copy(
                updatedAt = System.currentTimeMillis()
            )

            recipeDao.updateRecipe(updatedRecipe)
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "updateRecipe: ${recipe.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "updateRecipe failed", e)
            Result.failure(e)
        }
    }

    /**
     * Delete recipe
     */
    suspend fun deleteRecipe(recipeId: Long): Result<Unit> {
        return try {
            recipeDao.deleteRecipeById(recipeId)
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "deleteRecipe: $recipeId")
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "deleteRecipe failed", e)
            Result.failure(e)
        }
    }

    /**
     * Toggle favorite status
     */
    suspend fun toggleFavorite(recipeId: Long, isFavorite: Boolean): Result<Unit> {
        return try {
            recipeDao.updateFavoriteStatus(recipeId, isFavorite)
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "toggleFavorite: $recipeId = $isFavorite")
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "toggleFavorite failed", e)
            Result.failure(e)
        }
    }

    /**
     * Scale recipe servings and ingredient quantities
     */
    fun scaleRecipe(recipe: Recipe, newServings: Int): Recipe {
        val scaleFactor = newServings.toDouble() / recipe.servings.toDouble()

        // TODO: Implement intelligent ingredient quantity scaling
        // For now, just update servings

        return recipe.copy(servings = newServings)
    }

    /**
     * Validate recipe before save
     */
    private fun validateRecipe(recipe: Recipe) {
        require(recipe.title.isNotBlank()) { "Recipe title cannot be empty" }
        require(recipe.ingredients.isNotEmpty()) { "Recipe must have at least one ingredient" }
        require(recipe.instructions.isNotEmpty()) { "Recipe must have at least one instruction" }
        require(recipe.servings > 0) { "Servings must be greater than 0" }
    }

    // Recipe Log Methods

    /**
     * Get all logs for a recipe
     */
    fun getLogsForRecipe(recipeId: Long): Flow<List<RecipeLog>> {
        return recipeLogDao.getLogsForRecipe(recipeId)
    }

    /**
     * Get the most recent log for a recipe
     */
    suspend fun getLastLogForRecipe(recipeId: Long): RecipeLog? {
        return recipeLogDao.getLastLogForRecipe(recipeId)
    }

    /**
     * Get count of times recipe was made
     */
    suspend fun getLogCountForRecipe(recipeId: Long): Int {
        return recipeLogDao.getLogCountForRecipe(recipeId)
    }

    /**
     * Mark recipe as made (create log entry)
     */
    suspend fun markRecipeAsMade(recipeId: Long, notes: String? = null, rating: Int? = null): Result<Long> {
        return try {
            val log = RecipeLog(
                recipeId = recipeId,
                timestamp = System.currentTimeMillis(),
                notes = notes,
                rating = rating
            )
            val logId = recipeLogDao.insertLog(log)
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "markRecipeAsMade: recipeId=$recipeId, logId=$logId")
            Result.success(logId)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "markRecipeAsMade failed", e)
            Result.failure(e)
        }
    }

    /**
     * Update an existing log entry
     */
    suspend fun updateLog(log: RecipeLog): Result<Unit> {
        return try {
            recipeLogDao.updateLog(log)
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "updateLog: ${log.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "updateLog failed", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a log entry
     */
    suspend fun deleteLog(logId: Long): Result<Unit> {
        return try {
            recipeLogDao.deleteLogById(logId)
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "deleteLog: $logId")
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "deleteLog failed", e)
            Result.failure(e)
        }
    }
}
