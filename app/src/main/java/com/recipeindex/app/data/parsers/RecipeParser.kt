package com.recipeindex.app.data.parsers

import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeSource
import com.recipeindex.app.utils.DebugConfig

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
    val cuisine: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val sourceUrl: String? = null
)

/**
 * Convert ParsedRecipeData to Recipe entity
 */
fun ParsedRecipeData.toRecipe(sourceUrl: String): Recipe {
    val now = System.currentTimeMillis()

    DebugConfig.debugLog(
        DebugConfig.Category.IMPORT,
        "Creating recipe with photo: ${imageUrl ?: "none"}"
    )

    return Recipe(
        id = 0,
        title = title ?: "Imported Recipe",
        ingredients = ingredients,
        instructions = instructions,
        servings = servings ?: 4,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        tags = tags,
        cuisine = cuisine,
        notes = null, // Notes should be user-added only, not populated during import
        source = RecipeSource.URL,
        sourceUrl = sourceUrl,
        photoPath = imageUrl, // Save image URL to photoPath
        isFavorite = false,
        isTemplate = false,
        createdAt = now,
        updatedAt = now
    )
}
