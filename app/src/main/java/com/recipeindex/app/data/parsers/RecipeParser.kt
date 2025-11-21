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
 * RecipeParseResult - Result of parsing that includes both recipe and extracted media URLs
 * Used by URL parser to communicate found image/video URLs before downloading
 */
data class RecipeParseResult(
    val recipe: Recipe,
    val imageUrls: List<String> = emptyList()
)

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
    val servingSize: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val totalTimeMinutes: Int? = null,
    val tags: List<String> = emptyList(),
    val cuisine: String? = null,
    val description: String? = null,
    val sourceTips: String? = null, // Tips, substitutions, notes from source
    val imageUrls: List<String> = emptyList(), // Multiple images/videos from URL
    val sourceUrl: String? = null
)

/**
 * Convert ParsedRecipeData to RecipeParseResult
 * Returns both the recipe entity and the list of image URLs for user selection
 */
fun ParsedRecipeData.toRecipeParseResult(sourceUrl: String): RecipeParseResult {
    val now = System.currentTimeMillis()

    DebugConfig.debugLog(
        DebugConfig.Category.IMPORT,
        "Creating recipe with ${imageUrls.size} image URLs found"
    )

    val recipe = Recipe(
        id = 0,
        title = title ?: "Imported Recipe",
        ingredients = ingredients,
        instructions = instructions,
        servings = servings ?: 4,
        servingSize = servingSize,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        tags = tags,
        cuisine = cuisine,
        notes = null, // Notes should be user-added only, not populated during import
        sourceTips = sourceTips, // Tips/substitutions from the source
        source = RecipeSource.URL,
        sourceUrl = sourceUrl,
        photoPath = null, // Deprecated - use mediaPaths instead
        mediaPaths = emptyList(), // Will be populated after user selects and downloads images
        isFavorite = false,
        isTemplate = false,
        createdAt = now,
        updatedAt = now
    )

    return RecipeParseResult(
        recipe = recipe,
        imageUrls = imageUrls
    )
}
