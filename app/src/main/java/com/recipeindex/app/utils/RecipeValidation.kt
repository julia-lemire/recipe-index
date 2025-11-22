package com.recipeindex.app.utils

import com.recipeindex.app.data.entities.Recipe

/**
 * RecipeValidation - Centralized recipe validation logic
 *
 * Single source of truth for recipe validation, used by:
 * - RecipeManager (throws exceptions)
 * - Import screens (displays error messages)
 * - UI components (button enablement)
 */
object RecipeValidation {

    /**
     * Check if recipe is valid for saving (used for button enablement)
     */
    fun isValid(recipe: Recipe): Boolean {
        return recipe.title.isNotBlank() &&
               recipe.ingredients.isNotEmpty() &&
               recipe.instructions.isNotEmpty() &&
               recipe.servings > 0
    }

    /**
     * Get validation error message if recipe is invalid, null if valid
     * Used by import screens to show specific error messages
     */
    fun getValidationError(recipe: Recipe): String? {
        return when {
            recipe.title.isBlank() -> "Title is required"
            recipe.ingredients.isEmpty() -> "At least one ingredient is required"
            recipe.instructions.isEmpty() -> "At least one instruction step is required"
            recipe.servings <= 0 -> "Servings must be greater than 0"
            else -> null
        }
    }

    /**
     * Validate recipe and throw IllegalArgumentException if invalid
     * Used by RecipeManager for create/update operations
     */
    fun validateOrThrow(recipe: Recipe) {
        require(recipe.title.isNotBlank()) { "Recipe title cannot be empty" }
        require(recipe.ingredients.isNotEmpty()) { "Recipe must have at least one ingredient" }
        require(recipe.instructions.isNotEmpty()) { "Recipe must have at least one instruction" }
        require(recipe.servings > 0) { "Servings must be greater than 0" }
    }
}
