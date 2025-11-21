package com.recipeindex.app.data.parsers

import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.utils.DebugConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.jsoup.Jsoup

/**
 * UrlRecipeParser - Controller for parsing recipes from URLs
 *
 * Orchestrates cascading data supplementation strategy:
 * 1. Schema.org Recipe JSON-LD (best: structured data with all fields)
 * 2. HTML scraping (supplements missing ingredients/instructions)
 * 3. Open Graph (supplements remaining missing metadata: title/description/image)
 *
 * Each step supplements missing data without overwriting data from previous steps.
 * This ensures maximum data extraction from any combination of available sources.
 *
 * Delegates to specialized parsers for each strategy.
 */
class UrlRecipeParser(
    private val httpClient: HttpClient
) : RecipeParser {

    private val schemaOrgParser = SchemaOrgRecipeParser()
    private val htmlScraper = HtmlScraper()
    private val openGraphParser = OpenGraphParser()

    /**
     * Parse recipe from URL with media URLs
     * Returns RecipeParseResult containing both recipe and list of found image URLs
     */
    suspend fun parseWithMedia(source: String): Result<RecipeParseResult> {
        return try {
            // Fetch HTML from URL
            val html = httpClient.get(source).bodyAsText()
            val document = Jsoup.parse(html)

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[IMPORT] Starting URL parse with cascading supplementation strategy"
            )

            // Try all parsers and cascade data: Schema.org → HTML scraping → Open Graph
            // Each step supplements missing data without overwriting previous data

            // Step 1: Try Schema.org JSON-LD (best source)
            var recipeData: ParsedRecipeData? = schemaOrgParser.parse(document)
            if (recipeData != null) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[IMPORT] Found Schema.org JSON-LD data with ${recipeData.imageUrls.size} images"
                )
            }

            // Step 2: Supplement with HTML scraping for missing fields
            val htmlScrapedData = htmlScraper.scrape(document)
            if (htmlScrapedData != null) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[IMPORT] Found HTML scraped data with ${htmlScrapedData.imageUrls.size} images - supplementing missing fields"
                )
                recipeData = if (recipeData != null) {
                    // Merge: prefer Schema.org data, supplement with HTML scraping
                    // Combine imageUrls from both sources (deduped)
                    val combinedImageUrls = (recipeData.imageUrls + htmlScrapedData.imageUrls).distinct()
                    recipeData.copy(
                        title = recipeData.title ?: htmlScrapedData.title,
                        description = recipeData.description ?: htmlScrapedData.description,
                        imageUrls = combinedImageUrls,
                        ingredients = recipeData.ingredients.ifEmpty { htmlScrapedData.ingredients },
                        instructions = recipeData.instructions.ifEmpty { htmlScrapedData.instructions },
                        tags = recipeData.tags.ifEmpty { htmlScrapedData.tags }
                    )
                } else {
                    // No Schema.org data, use HTML scraping as base
                    htmlScrapedData
                }
            }

            // Step 3: Supplement with Open Graph for any remaining missing metadata
            val openGraphData = openGraphParser.parse(document)
            if (openGraphData != null) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[IMPORT] Found Open Graph data with ${openGraphData.imageUrls.size} images - supplementing remaining missing metadata"
                )
                recipeData = if (recipeData != null) {
                    // Merge: prefer previous data, supplement with Open Graph
                    // Add Open Graph images if not already present
                    val combinedImageUrls = (recipeData.imageUrls + openGraphData.imageUrls).distinct()
                    recipeData.copy(
                        title = recipeData.title ?: openGraphData.title,
                        description = recipeData.description ?: openGraphData.description,
                        imageUrls = combinedImageUrls
                    )
                } else {
                    // No Schema.org or HTML scraping data, use Open Graph as last resort
                    openGraphData
                }
            }

            // Convert merged data to RecipeParseResult
            if (recipeData != null) {
                val result = recipeData.toRecipeParseResult(sourceUrl = source)
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[IMPORT] Parse complete with ${result.imageUrls.size} total image URLs"
                )
                return Result.success(result)
            }

            // No data found from any parser
            Result.failure(Exception("No recipe data found at URL"))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse recipe from URL: ${e.message}", e))
        }
    }

    override suspend fun parse(source: String): Result<Recipe> {
        // Call parseWithMedia and extract just the recipe for backward compatibility
        return parseWithMedia(source).map { it.recipe }
    }
}
