package com.recipeindex.app.data.parsers

import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.utils.DebugConfig
import org.jsoup.nodes.Document

/**
 * HtmlScraper - Scrapes recipe data from HTML when Schema.org markup unavailable
 *
 * Fallback scraper that searches for ingredients and instructions using common CSS selector patterns.
 * Returns data if ingredients OR instructions are found (saves whatever content is available).
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

        // Try to find images - extract all relevant recipe images
        val imageUrls = findImages(document)

        // Return data if we found ingredients OR instructions (save what we can find)
        return if (ingredients.isNotEmpty() || instructions.isNotEmpty()) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[IMPORT] HTML scraping successful: ${ingredients.size} ingredients, ${instructions.size} instructions, ${imageUrls.size} images"
            )

            // Also try to get categories from HTML
            val htmlCategories = parseCategories(document)

            ParsedRecipeData(
                title = title ?: "Scraped Recipe",
                ingredients = ingredients,
                instructions = instructions,
                tags = htmlCategories,
                imageUrls = imageUrls
            )
        } else {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[IMPORT] HTML scraping failed: ${ingredients.size} ingredients, ${instructions.size} instructions (need at least one)"
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
            "[class*=method] li",  // Some sites use "method"
            "ol[class*=instruction] li",
            "ol[class*=direction] li",
            ".instructions li",
            "#instructions li",
            ".directions li",
            "#directions li",
            ".method li",
            "#method li",
            // Also try paragraphs within instruction containers
            "[class*=instruction] p",
            "[class*=direction] p",
            "[class*=step] p",
            "[class*=method] p",
            // Try ordered lists without specific classes (common pattern)
            "ol li"
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
                    // For generic "ol li" selector, require at least 3 items to reduce false positives
                    if (selector == "ol li" && instructions.size < 3) {
                        DebugConfig.debugLog(
                            DebugConfig.Category.IMPORT,
                            "[IMPORT] Found only ${instructions.size} items with generic 'ol li' selector - skipping"
                        )
                        instructions.clear()
                        continue
                    }

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
     * Find images using common CSS selector patterns
     * Extracts multiple images that are likely recipe-related (main images, step images, etc.)
     */
    private fun findImages(document: Document): List<String> {
        val imageUrls = mutableSetOf<String>()  // Use set to avoid duplicates

        // Priority 1: Look for recipe-specific images with class/id patterns
        val recipeImageSelectors = listOf(
            "[class*=recipe] img",
            "[id*=recipe] img",
            "[class*=hero] img",
            "[class*=featured] img",
            "[class*=main-image] img",
            ".recipe-image img",
            "#recipe-image img",
            ".recipe-photo img",
            "#recipe-photo img",
            // Step-by-step instruction images
            "[class*=instruction] img",
            "[class*=step] img",
            "[class*=direction] img",
            // Look for figures within recipe content
            "article img",
            "figure img"
        )

        for (selector in recipeImageSelectors) {
            val images = document.select(selector)
            images.forEach { img ->
                val src = img.attr("src")
                val dataSrc = img.attr("data-src")  // Lazy-loaded images
                val dataSrcset = img.attr("data-srcset")  // Responsive images

                // Add absolute URLs only (filter out placeholders, icons, etc.)
                listOf(src, dataSrc, dataSrcset.split(",").firstOrNull()?.trim()?.split(" ")?.firstOrNull())
                    .forEach { url ->
                        if (url != null && url.isNotBlank() && isValidImageUrl(url)) {
                            imageUrls.add(url)
                        }
                    }
            }
        }

        // Priority 2: If no recipe-specific images found, look for larger images in the page
        if (imageUrls.isEmpty()) {
            val allImages = document.select("img")
            allImages.forEach { img ->
                val src = img.attr("src")
                val width = img.attr("width").toIntOrNull() ?: 0
                val height = img.attr("height").toIntOrNull() ?: 0

                // Only include larger images (likely to be content images, not icons/buttons)
                if (isValidImageUrl(src) && (width > 200 || height > 200 || (width == 0 && height == 0))) {
                    imageUrls.add(src)
                }
            }
        }

        val result = imageUrls.toList()
        if (result.isNotEmpty()) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[IMPORT] Found ${result.size} images via HTML scraping"
            )
        }

        return result
    }

    /**
     * Check if URL is a valid image URL (not a placeholder, icon, or tracking pixel)
     */
    private fun isValidImageUrl(url: String): Boolean {
        if (url.isBlank()) return false

        // Filter out common placeholders and tracking pixels
        val lowerUrl = url.lowercase()
        val invalidPatterns = listOf(
            "placeholder",
            "spacer",
            "pixel",
            "tracking",
            "1x1",
            "blank.gif",
            "transparent.gif",
            "icon",
            "logo",
            "avatar",
            "badge"
        )

        if (invalidPatterns.any { lowerUrl.contains(it) }) {
            return false
        }

        // Must start with http:// or https:// or be a protocol-relative URL (//)
        return url.startsWith("http://") || url.startsWith("https://") || url.startsWith("//")
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
