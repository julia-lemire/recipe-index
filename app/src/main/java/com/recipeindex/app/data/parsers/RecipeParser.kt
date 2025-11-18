package com.recipeindex.app.data.parsers

import com.recipeindex.app.data.entities.Recipe

/**
 * RecipeParser - Interface for parsing recipes from various sources
 *
 * Implementations should extract structured recipe data from:
 * - URLs (Schema.org JSON-LD, Open Graph, HTML scraping)
 * - PDF documents
 * - OCR text from photos
 */
interface RecipeParser {
    /**
     * Parse recipe from source data
     *
     * @param source Source data (URL, HTML, text, etc.)
     * @return Parsed Recipe or null if parsing failed
     */
    suspend fun parse(source: String): Result<Recipe>
}

/**
 * ParsedRecipeData - Intermediate representation of parsed recipe data
 *
 * Used before converting to full Recipe entity
 */
data class ParsedRecipeData(
    val title: String? = null,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val servings: Int? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val totalTimeMinutes: Int? = null,
    val tags: List<String> = emptyList(),
    val description: String? = null,
    val imageUrl: String? = null,
    val sourceUrl: String? = null
)
