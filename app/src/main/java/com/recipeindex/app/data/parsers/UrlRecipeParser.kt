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
 * Orchestrates two-tier parsing strategy:
 * 1. Schema.org Recipe JSON-LD (best: structured data with all fields)
 * 2. HTML scraping (fallback: extracts whatever ingredients/instructions found in HTML)
 *
 * Note: Open Graph fallback removed - only provides metadata without recipe content.
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
                "[IMPORT] Starting URL parse with two-tier strategy"
            )

            // Try Schema.org JSON-LD first
            val schemaOrgData = schemaOrgParser.parse(document)
            if (schemaOrgData != null) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[IMPORT] Using Schema.org JSON-LD data"
                )
                val recipe = schemaOrgData.toRecipe(sourceUrl = source)
                return Result.success(recipe)
            }

            // Try HTML scraping fallback
            val htmlScrapedData = htmlScraper.scrape(document)
            if (htmlScrapedData != null) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[IMPORT] Using HTML scraped data"
                )
                val recipe = htmlScrapedData.toRecipe(sourceUrl = source)
                return Result.success(recipe)
            }

            // Open Graph is truly a last resort - only provides metadata (title/description/image)
            // Note: No longer fall back to Open Graph if we have nothing else
            // Users can manually add ingredients/instructions if they really want to save the recipe

            // No data found
            Result.failure(Exception("No recipe data found at URL"))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse recipe from URL: ${e.message}", e))
        }
    }
}
