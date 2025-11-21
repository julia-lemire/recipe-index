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
 * Orchestrates three-tier parsing strategy:
 * 1. Schema.org Recipe JSON-LD (best: structured data with all fields)
 * 2. HTML scraping (fallback: extracts ingredients/instructions from HTML)
 * 3. Open Graph meta tags (last resort: only title/description/image)
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
                "[IMPORT] Starting URL parse with three-tier strategy"
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

            // Try Open Graph last resort
            val openGraphData = openGraphParser.parse(document)
            if (openGraphData != null) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[IMPORT] Using Open Graph data (minimal)"
                )
                val recipe = openGraphData.toRecipe(sourceUrl = source)
                return Result.success(recipe)
            }

            // No data found
            Result.failure(Exception("No recipe data found at URL"))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse recipe from URL: ${e.message}", e))
        }
    }
}
