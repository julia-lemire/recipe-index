package com.recipeindex.app.data.managers

import com.recipeindex.app.data.dao.RecipeDao
import com.recipeindex.app.data.dao.RecipeLogDao
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeLog
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.RecipeValidation
import com.recipeindex.app.utils.resultOf
import com.recipeindex.app.utils.resultOfValidated
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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
    private val recipeLogDao: RecipeLogDao,
    private val mealPlanDao: com.recipeindex.app.data.dao.MealPlanDao
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
    suspend fun createRecipe(recipe: Recipe): Result<Long> = resultOfValidated(
        successLog = "createRecipe: ${recipe.title}",
        errorLog = "createRecipe failed",
        validate = { RecipeValidation.validateOrThrow(recipe) }
    ) {
        val updatedRecipe = recipe.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        recipeDao.insertRecipe(updatedRecipe)
    }

    /**
     * Update existing recipe
     */
    suspend fun updateRecipe(recipe: Recipe): Result<Unit> = resultOfValidated(
        successLog = "updateRecipe: ${recipe.id}",
        errorLog = "updateRecipe failed",
        validate = { RecipeValidation.validateOrThrow(recipe) }
    ) {
        val updatedRecipe = recipe.copy(updatedAt = System.currentTimeMillis())
        recipeDao.updateRecipe(updatedRecipe)
    }

    /**
     * Delete recipe and cascade to meal plans
     *
     * Removes the recipe from all meal plans that reference it to maintain
     * referential integrity
     */
    suspend fun deleteRecipe(recipeId: Long): Result<Unit> = resultOf(
        successLog = "deleteRecipe: $recipeId completed",
        errorLog = "deleteRecipe failed"
    ) {
        // First, remove this recipe from all meal plans that reference it
        val allMealPlans = mealPlanDao.getAll().first()
        val affectedPlans = allMealPlans.filter { recipeId in it.recipeIds }

        DebugConfig.debugLog(
            DebugConfig.Category.MANAGER,
            "deleteRecipe: $recipeId found in ${affectedPlans.size} meal plans"
        )

        // Update each affected meal plan to remove the recipe ID
        affectedPlans.forEach { plan ->
            val updatedPlan = plan.copy(
                recipeIds = plan.recipeIds.filter { it != recipeId },
                updatedAt = System.currentTimeMillis()
            )
            mealPlanDao.update(updatedPlan)
            DebugConfig.debugLog(
                DebugConfig.Category.MANAGER,
                "Removed recipe $recipeId from meal plan '${plan.name}' (${plan.id})"
            )
        }

        // Now delete the recipe
        recipeDao.deleteRecipeById(recipeId)
    }

    /**
     * Toggle favorite status
     */
    suspend fun toggleFavorite(recipeId: Long, isFavorite: Boolean): Result<Unit> = resultOf(
        successLog = "toggleFavorite: $recipeId = $isFavorite",
        errorLog = "toggleFavorite failed"
    ) {
        recipeDao.updateFavoriteStatus(recipeId, isFavorite)
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
    suspend fun markRecipeAsMade(recipeId: Long, notes: String? = null, rating: Int? = null): Result<Long> = resultOf(
        successLog = "markRecipeAsMade: recipeId=$recipeId",
        errorLog = "markRecipeAsMade failed"
    ) {
        val log = RecipeLog(
            recipeId = recipeId,
            timestamp = System.currentTimeMillis(),
            notes = notes,
            rating = rating
        )
        recipeLogDao.insertLog(log)
    }

    /**
     * Update an existing log entry
     */
    suspend fun updateLog(log: RecipeLog): Result<Unit> = resultOf(
        successLog = "updateLog: ${log.id}",
        errorLog = "updateLog failed"
    ) {
        recipeLogDao.updateLog(log)
    }

    /**
     * Delete a log entry
     */
    suspend fun deleteLog(logId: Long): Result<Unit> = resultOf(
        successLog = "deleteLog: $logId",
        errorLog = "deleteLog failed"
    ) {
        recipeLogDao.deleteLogById(logId)
    }
}
