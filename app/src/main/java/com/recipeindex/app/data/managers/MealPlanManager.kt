package com.recipeindex.app.data.managers

import com.recipeindex.app.data.RecipeTags
import com.recipeindex.app.data.dao.MealPlanDao
import com.recipeindex.app.data.dao.RecipeDao
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.resultOf
import com.recipeindex.app.utils.resultOfValidated
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * MealPlanManager - Business logic for meal plan operations
 *
 * Handles tag aggregation from recipes and special event detection
 */
class MealPlanManager(
    private val mealPlanDao: MealPlanDao,
    private val recipeDao: RecipeDao
) {

    /**
     * Get all meal plans as Flow for reactive UI updates
     */
    fun getAllMealPlans(): Flow<List<MealPlan>> {
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "getAllMealPlans")
        return mealPlanDao.getAll()
    }

    /**
     * Get single meal plan by ID
     */
    suspend fun getMealPlanById(id: Long): MealPlan? {
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "getMealPlanById: $id")
        return mealPlanDao.getById(id)
    }

    /**
     * Search meal plans by name
     */
    fun searchMealPlans(query: String): Flow<List<MealPlan>> {
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "searchMealPlans: $query")
        return mealPlanDao.searchByName(query)
    }

    /**
     * Get meal plans by date range
     */
    fun getMealPlansByDateRange(startDate: Long, endDate: Long): Flow<List<MealPlan>> {
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "getMealPlansByDateRange: $startDate - $endDate")
        return mealPlanDao.getByDateRange(startDate, endDate)
    }

    /**
     * Search meal plans by tag
     */
    fun searchMealPlansByTag(tag: String): Flow<List<MealPlan>> {
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "searchMealPlansByTag: $tag")
        return mealPlanDao.searchByTag(tag)
    }

    /**
     * Create new meal plan with validation and auto-tagging
     */
    suspend fun createMealPlan(mealPlan: MealPlan): Result<Long> = resultOfValidated(
        successLog = "createMealPlan: ${mealPlan.name}",
        errorLog = "createMealPlan failed",
        validate = { validateMealPlan(mealPlan) }
    ) {
        // Auto-aggregate tags from recipes
        val aggregatedTags = aggregateTagsFromRecipes(mealPlan.recipeIds)
        val eventTags = detectSpecialEventFromName(mealPlan.name)
        val allTags = (aggregatedTags + eventTags).distinct()

        val updatedPlan = mealPlan.copy(
            tags = allTags,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        mealPlanDao.insert(updatedPlan)
    }

    /**
     * Update existing meal plan with auto-tagging
     */
    suspend fun updateMealPlan(mealPlan: MealPlan): Result<Unit> = resultOfValidated(
        successLog = "updateMealPlan: ${mealPlan.id}",
        errorLog = "updateMealPlan failed",
        validate = { validateMealPlan(mealPlan) }
    ) {
        // Auto-aggregate tags from recipes
        val aggregatedTags = aggregateTagsFromRecipes(mealPlan.recipeIds)
        val eventTags = detectSpecialEventFromName(mealPlan.name)
        val allTags = (aggregatedTags + eventTags).distinct()

        val updatedPlan = mealPlan.copy(
            tags = allTags,
            updatedAt = System.currentTimeMillis()
        )
        mealPlanDao.update(updatedPlan)
    }

    /**
     * Delete meal plan
     */
    suspend fun deleteMealPlan(mealPlan: MealPlan): Result<Unit> = resultOf(
        successLog = "deleteMealPlan: ${mealPlan.id}",
        errorLog = "deleteMealPlan failed"
    ) {
        mealPlanDao.delete(mealPlan)
    }

    /**
     * Duplicate meal plan with new name
     */
    suspend fun duplicateMealPlan(mealPlan: MealPlan, newName: String): Result<Long> {
        val duplicatedPlan = mealPlan.copy(
            id = 0, // Auto-generate new ID
            name = newName,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return createMealPlan(duplicatedPlan)
    }

    /**
     * Aggregate tags from recipes in the meal plan
     * Includes ingredient tags and special event tags
     */
    private suspend fun aggregateTagsFromRecipes(recipeIds: List<Long>): List<String> {
        if (recipeIds.isEmpty()) return emptyList()

        val allTags = mutableSetOf<String>()

        for (recipeId in recipeIds) {
            val recipe = recipeDao.getRecipeById(recipeId).first()
            if (recipe != null) {
                // Filter for ingredient and special event tags
                val relevantTags = recipe.tags.filter { tag ->
                    isIngredientTag(tag) || isSpecialEventTag(tag)
                }
                allTags.addAll(relevantTags)
            }
        }

        return allTags.toList()
    }

    /**
     * Detect special event tags from meal plan name
     * Examples: "Thanksgiving Dinner" → "Thanksgiving", "Christmas Party" → "Christmas"
     */
    private fun detectSpecialEventFromName(name: String): List<String> {
        val lowerName = name.lowercase()
        val detectedTags = mutableListOf<String>()

        // Special event keywords (case-insensitive matching)
        val eventMap = mapOf(
            "thanksgiving" to "Thanksgiving",
            "christmas" to "Christmas",
            "xmas" to "Christmas",
            "easter" to "Easter",
            "birthday" to "Birthday",
            "halloween" to "Halloween",
            "hanukkah" to "Hanukkah",
            "passover" to "Passover",
            "new year" to "New Year",
            "valentine" to "Valentine's Day",
            "4th of july" to "4th of July",
            "independence day" to "4th of July",
            "memorial day" to "Memorial Day",
            "labor day" to "Labor Day"
        )

        for ((keyword, tag) in eventMap) {
            if (lowerName.contains(keyword)) {
                detectedTags.add(tag)
            }
        }

        return detectedTags
    }

    /**
     * Check if tag is an ingredient tag
     */
    private fun isIngredientTag(tag: String): Boolean {
        return RecipeTags.INGREDIENTS.contains(tag)
    }

    /**
     * Check if tag is a special event tag
     */
    private fun isSpecialEventTag(tag: String): Boolean {
        return RecipeTags.SPECIAL_EVENTS.contains(tag)
    }

    /**
     * Validate meal plan before saving
     */
    private fun validateMealPlan(mealPlan: MealPlan) {
        require(mealPlan.name.isNotBlank()) { "Meal plan name cannot be blank" }

        // Validate date range if both are provided
        if (mealPlan.startDate != null && mealPlan.endDate != null) {
            require(mealPlan.startDate <= mealPlan.endDate) {
                "Start date must be before or equal to end date"
            }
        }
    }
}
