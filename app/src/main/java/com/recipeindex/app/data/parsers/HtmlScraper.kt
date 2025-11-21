package com.recipeindex.app.data.parsers

import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.utils.DebugConfig
import org.jsoup.nodes.Document

/**
 * HtmlScraper - Scrapes recipe data from HTML when Schema.org markup unavailable
 *
 * Fallback scraper that searches for ingredients and instructions using common CSS selector patterns.
 * Only returns data if BOTH ingredients AND instructions are found (minimum viable recipe).
 */
class HtmlScraper {

    /**
     * Scrape recipe from HTML document using CSS selectors
     * @return ParsedRecipeData if both ingredients and instructions found, null otherwise
     */
    fun scrape(document: Document): ParsedRecipeData? {
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "[IMPORT] Attempting HTML scraping fallback"
        )

        // Try to find title
        val title = document.select("h1").first()?.text()
            ?: document.select("meta[property=og:title]").attr("content")

        // Try to find ingredients - common patterns
        val ingredients = findIngredients(document)

        // Try to find instructions - common patterns
        val instructions = findInstructions(document)

        // Only return data if we found both ingredients and instructions
        return if (ingredients.isNotEmpty() && instructions.isNotEmpty()) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[IMPORT] HTML scraping successful: ${ingredients.size} ingredients, ${instructions.size} instructions"
            )

            // Also try to get categories from HTML
            val htmlCategories = parseCategories(document)

            ParsedRecipeData(
                title = title ?: "Scraped Recipe",
                ingredients = ingredients,
                instructions = instructions,
                tags = htmlCategories
            )
        } else {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[IMPORT] HTML scraping failed: ${ingredients.size} ingredients, ${instructions.size} instructions (need both)"
            )
            null
        }
    }

    /**
     * Find ingredients using common CSS selector patterns
     */
    private fun findIngredients(document: Document): List<String> {
        val ingredients = mutableListOf<String>()

        // Look for lists with ingredient-related class names or IDs
        val ingredientSelectors = listOf(
            "[class*=ingredient] li",
            "[id*=ingredient] li",
            "[class*=ing] li",
            "ul[class*=ingredient] li",
            "ol[class*=ingredient] li",
            ".ingredients li",
            "#ingredients li"
        )

        for (selector in ingredientSelectors) {
            val items = document.select(selector)
            if (items.isNotEmpty()) {
                items.forEach { item ->
                    val text = item.text().trim()
                    if (text.isNotBlank() && text.length > 3) {
                        ingredients.add(text)
                    }
                }
                if (ingredients.isNotEmpty()) {
                    DebugConfig.debugLog(
                        DebugConfig.Category.IMPORT,
                        "[IMPORT] Found ${ingredients.size} ingredients using selector: $selector"
                    )
                    break  // Found ingredients, stop searching
                }
            }
        }

        return ingredients
    }

    /**
     * Find instructions using common CSS selector patterns
     */
    private fun findInstructions(document: Document): List<String> {
        val instructions = mutableListOf<String>()

        // Look for lists with instruction-related class names or IDs
        val instructionSelectors = listOf(
            "[class*=instruction] li",
            "[id*=instruction] li",
            "[class*=direction] li",
            "[id*=direction] li",
            "[class*=step] li",
            "ol[class*=instruction] li",
            "ol[class*=direction] li",
            ".instructions li",
            "#instructions li",
            ".directions li",
            "#directions li"
        )

        for (selector in instructionSelectors) {
            val items = document.select(selector)
            if (items.isNotEmpty()) {
                items.forEach { item ->
                    val text = item.text().trim()
                    if (text.isNotBlank() && text.length > 10) {  // Instructions are usually longer
                        instructions.add(text)
                    }
                }
                if (instructions.isNotEmpty()) {
                    DebugConfig.debugLog(
                        DebugConfig.Category.IMPORT,
                        "[IMPORT] Found ${instructions.size} instructions using selector: $selector"
                    )
                    break  // Found instructions, stop searching
                }
            }
        }

        return instructions
    }

    /**
     * Parse HTML category and tag links from document
     * Looks for links with rel="category" or rel="tag" (common in WordPress and other CMS)
     */
    fun parseCategories(document: Document): List<String> {
        return document.select("a[rel*=category], a[rel*=tag]")
            .map { it.text().trim() }
            .filter { it.isNotBlank() }
    }
}
