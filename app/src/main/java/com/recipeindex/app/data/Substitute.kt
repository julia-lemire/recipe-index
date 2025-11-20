package com.recipeindex.app.data

import kotlinx.serialization.Serializable

/**
 * Substitute - Represents a substitution for an ingredient
 *
 * Stored as JSON in IngredientSubstitution entity
 */
@Serializable
data class Substitute(
    /** Name of the substitute ingredient (e.g., "margarine") */
    val name: String,

    /** Conversion ratio for quantity (1.0 = 1:1, 0.75 = use 3/4 amount, 1.5 = use 1.5x amount) */
    val conversionRatio: Double = 1.0,

    /** Human-readable conversion note (e.g., "Use 3/4 the amount", "Use equal amount") */
    val conversionNote: String? = null,

    /** Additional notes (e.g., "Best for baking", "May alter flavor slightly") */
    val notes: String? = null,

    /** Suitability rating 1-10 (10=best substitute, 1=works in a pinch), used for ordering */
    val suitability: Int = 5,

    /** Dietary tags for filtering (e.g., ["vegan", "gluten-free", "dairy-free"]) */
    val dietaryTags: List<String> = emptyList()
)
