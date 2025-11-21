package com.recipeindex.app.data.parsers

import org.jsoup.nodes.Document

/**
 * OpenGraphParser - Extracts basic metadata from Open Graph tags
 *
 * Last resort parser that extracts title, description, and image from og: meta tags.
 * Returns minimal recipe data when no other parsing method succeeds.
 */
class OpenGraphParser {

    /**
     * Parse Open Graph meta tags from document
     * @return ParsedRecipeData with basic metadata if title found, null otherwise
     */
    fun parse(document: Document): ParsedRecipeData? {
        val title = document.select("meta[property=og:title]").attr("content").ifBlank { null }
        val description = document.select("meta[property=og:description]").attr("content").ifBlank { null }
        val imageUrl = document.select("meta[property=og:image]").attr("content").ifBlank { null }

        // If we have at least a title, return partial data
        return if (title != null) {
            ParsedRecipeData(
                title = title,
                description = description,
                imageUrl = imageUrl
            )
        } else {
            null
        }
    }
}
