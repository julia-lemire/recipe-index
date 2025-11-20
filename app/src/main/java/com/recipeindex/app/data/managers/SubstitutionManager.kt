package com.recipeindex.app.data.managers

import com.recipeindex.app.data.Substitute
import com.recipeindex.app.data.dao.SubstitutionDao
import com.recipeindex.app.data.entities.IngredientSubstitution
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.SubstitutionData
import kotlinx.coroutines.flow.Flow

/**
 * SubstitutionManager - Business logic for ingredient substitutions
 *
 * Handles CRUD operations, search, filtering, and initial data population
 */
class SubstitutionManager(
    private val dao: SubstitutionDao
) {

    /**
     * Get all substitutions
     */
    fun getAllSubstitutions(): Flow<List<IngredientSubstitution>> {
        return dao.getAllSubstitutions()
    }

    /**
     * Search substitutions by ingredient name
     */
    fun searchSubstitutions(query: String): Flow<List<IngredientSubstitution>> {
        return if (query.isBlank()) {
            dao.getAllSubstitutions()
        } else {
            dao.searchSubstitutions(query.trim())
        }
    }

    /**
     * Get substitutions by category
     */
    fun getSubstitutionsByCategory(category: String): Flow<List<IngredientSubstitution>> {
        return dao.getSubstitutionsByCategory(category)
    }

    /**
     * Get substitution for a specific ingredient
     */
    suspend fun getSubstitutionByIngredient(ingredient: String): IngredientSubstitution? {
        return dao.getSubstitutionByIngredient(ingredient.trim())
    }

    /**
     * Get substitution by ID
     */
    suspend fun getSubstitutionById(id: Long): IngredientSubstitution? {
        return dao.getSubstitutionById(id)
    }

    /**
     * Get all categories
     */
    fun getAllCategories(): Flow<List<String>> {
        return dao.getAllCategories()
    }

    /**
     * Create new substitution
     */
    suspend fun createSubstitution(
        ingredient: String,
        category: String,
        substitutes: List<Substitute>
    ): Long {
        val normalizedIngredient = ingredient.trim().lowercase()

        if (normalizedIngredient.isBlank()) {
            throw IllegalArgumentException("Ingredient name cannot be empty")
        }

        if (substitutes.isEmpty()) {
            throw IllegalArgumentException("Must provide at least one substitute")
        }

        val substitution = IngredientSubstitution(
            ingredient = normalizedIngredient,
            category = category.trim(),
            substitutes = substitutes.sortedByDescending { it.suitability },
            isUserAdded = true,
            lastModified = System.currentTimeMillis()
        )

        val id = dao.insertSubstitution(substitution)
        DebugConfig.debugLog(DebugConfig.Category.DATA, "Created substitution for: $normalizedIngredient")
        return id
    }

    /**
     * Update existing substitution
     */
    suspend fun updateSubstitution(
        id: Long,
        ingredient: String,
        category: String,
        substitutes: List<Substitute>,
        isUserAdded: Boolean
    ) {
        val normalizedIngredient = ingredient.trim().lowercase()

        if (normalizedIngredient.isBlank()) {
            throw IllegalArgumentException("Ingredient name cannot be empty")
        }

        if (substitutes.isEmpty()) {
            throw IllegalArgumentException("Must provide at least one substitute")
        }

        val substitution = IngredientSubstitution(
            id = id,
            ingredient = normalizedIngredient,
            category = category.trim(),
            substitutes = substitutes.sortedByDescending { it.suitability },
            isUserAdded = isUserAdded,
            lastModified = System.currentTimeMillis()
        )

        dao.updateSubstitution(substitution)
        DebugConfig.debugLog(DebugConfig.Category.DATA, "Updated substitution for: $normalizedIngredient")
    }

    /**
     * Delete substitution
     */
    suspend fun deleteSubstitution(id: Long) {
        dao.deleteSubstitutionById(id)
        DebugConfig.debugLog(DebugConfig.Category.DATA, "Deleted substitution: $id")
    }

    /**
     * Populate database with default substitutions if empty
     * Should be called on app initialization
     */
    suspend fun populateDefaultSubstitutionsIfNeeded() {
        val count = dao.getSubstitutionCount()

        if (count == 0) {
            DebugConfig.debugLog(DebugConfig.Category.DATA, "Populating substitution database with default data")
            dao.insertAll(SubstitutionData.getDefaultSubstitutions())
            DebugConfig.debugLog(DebugConfig.Category.DATA, "Populated ${SubstitutionData.getDefaultSubstitutions().size} default substitutions")
        }
    }

    /**
     * Calculate converted amount based on substitution ratio
     * Example: 2.0 cups with ratio 0.75 = 1.5 cups
     */
    fun calculateConvertedAmount(originalAmount: Double, ratio: Double): Double {
        return originalAmount * ratio
    }

    /**
     * Format converted amount as human-readable string
     * Prefers fractions for common values
     */
    fun formatConvertedAmount(amount: Double): String {
        return when {
            amount == 0.25 -> "1/4"
            amount == 0.33 || amount == 0.333 -> "1/3"
            amount == 0.5 -> "1/2"
            amount == 0.66 || amount == 0.667 -> "2/3"
            amount == 0.75 -> "3/4"
            amount == 1.0 -> "1"
            amount == 1.5 -> "1 1/2"
            amount == 2.0 -> "2"
            amount % 1.0 == 0.0 -> amount.toInt().toString()
            else -> String.format("%.2f", amount)
        }
    }
}
