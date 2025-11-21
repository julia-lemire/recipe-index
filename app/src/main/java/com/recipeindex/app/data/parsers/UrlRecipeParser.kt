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

    override suspend fun parse(source: String): Result<Recipe> {
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
                    "[IMPORT] Found Schema.org JSON-LD data"
                )
            }

            // Step 2: Supplement with HTML scraping for missing fields
            val htmlScrapedData = htmlScraper.scrape(document)
            if (htmlScrapedData != null) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[IMPORT] Found HTML scraped data - supplementing missing fields"
                )
                recipeData = if (recipeData != null) {
                    // Merge: prefer Schema.org data, supplement with HTML scraping
                    recipeData.copy(
                        title = recipeData.title ?: htmlScrapedData.title,
                        description = recipeData.description ?: htmlScrapedData.description,
                        imageUrl = recipeData.imageUrl ?: htmlScrapedData.imageUrl,
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
                    "[IMPORT] Found Open Graph data - supplementing remaining missing metadata"
                )
                recipeData = if (recipeData != null) {
                    // Merge: prefer previous data, supplement with Open Graph
                    recipeData.copy(
                        title = recipeData.title ?: openGraphData.title,
                        description = recipeData.description ?: openGraphData.description,
                        imageUrl = recipeData.imageUrl ?: openGraphData.imageUrl
                    )
                } else {
                    // No Schema.org or HTML scraping data, use Open Graph as last resort
                    openGraphData
                }
            }

            // Convert merged data to Recipe
            if (recipeData != null) {
                val recipe = recipeData.toRecipe(sourceUrl = source)
                return Result.success(recipe)
            }

            // No data found from any parser
            Result.failure(Exception("No recipe data found at URL"))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse recipe from URL: ${e.message}", e))
        }
    }
}
