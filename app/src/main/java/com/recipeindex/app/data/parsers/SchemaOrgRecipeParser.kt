package com.recipeindex.app.data.parsers

import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeSource
import com.recipeindex.app.utils.DebugConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * SchemaOrgRecipeParser - Parses recipes from URLs using Schema.org Recipe JSON-LD
 *
 * Supports recipe websites that use Schema.org Recipe markup (most modern sites)
 * Falls back to Open Graph meta tags if Schema.org not found
 */
class SchemaOrgRecipeParser(
    private val httpClient: HttpClient
) : RecipeParser {

    override suspend fun parse(source: String): Result<Recipe> {
        return try {
            // Fetch HTML from URL
            val html = httpClient.get(source).bodyAsText()
            val document = Jsoup.parse(html)

            // Try Schema.org JSON-LD first
            val parsedData = parseSchemaOrg(document)
                ?: parseOpenGraph(document)
                ?: return Result.failure(Exception("No recipe data found at URL"))

            // Convert to Recipe entity
            val recipe = parsedData.toRecipe(sourceUrl = source)
            Result.success(recipe)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse recipe from URL: ${e.message}", e))
        }
    }

    /**
     * Parse Schema.org Recipe JSON-LD from document
     */
    private fun parseSchemaOrg(document: Document): ParsedRecipeData? {
        // Find script tags with type="application/ld+json"
        val jsonLdScripts = document.select("script[type=application/ld+json]")

        for (script in jsonLdScripts) {
            try {
                val json = Json.parseToJsonElement(script.data())

                // Handle both single object and array of objects
                val recipes = when {
                    json is JsonObject && json["@type"]?.jsonPrimitive?.content == "Recipe" -> {
                        listOf(json)
                    }
                    json is JsonArray -> {
                        json.filterIsInstance<JsonObject>()
                            .filter { it["@type"]?.jsonPrimitive?.content == "Recipe" }
                    }
                    json is JsonObject && json["@graph"] is JsonArray -> {
                        (json["@graph"] as JsonArray).filterIsInstance<JsonObject>()
                            .filter { it["@type"]?.jsonPrimitive?.content == "Recipe" }
                    }
                    else -> emptyList()
                }

                // Parse first recipe found
                recipes.firstOrNull()?.let { recipeJson ->
                    return parseRecipeFromJsonLd(recipeJson)
                }
            } catch (e: Exception) {
                // Continue to next script tag
                continue
            }
        }

        return null
    }

    /**
     * Parse Recipe from Schema.org JSON-LD object
     */
    private fun parseRecipeFromJsonLd(json: JsonObject): ParsedRecipeData {
        return ParsedRecipeData(
            title = json["name"]?.jsonPrimitive?.contentOrNull,
            description = json["description"]?.jsonPrimitive?.contentOrNull,
            ingredients = parseJsonArrayToStrings(json["recipeIngredient"]),
            instructions = parseInstructions(json["recipeInstructions"]),
            servings = parseServings(json["recipeYield"]),
            prepTimeMinutes = parseIsoDuration(json["prepTime"]?.jsonPrimitive?.contentOrNull),
            cookTimeMinutes = parseIsoDuration(json["cookTime"]?.jsonPrimitive?.contentOrNull),
            totalTimeMinutes = parseIsoDuration(json["totalTime"]?.jsonPrimitive?.contentOrNull),
            tags = parseJsonArrayToStrings(json["recipeCategory"]) +
                   parseJsonArrayToStrings(json["recipeCuisine"]) +
                   parseJsonArrayToStrings(json["keywords"]),
            imageUrl = parseImage(json["image"])
        )
    }

    /**
     * Parse instructions - can be array of strings, HowToStep objects, or HowToSection objects
     */
    private fun parseInstructions(element: JsonElement?): List<String> {
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "Parsing instructions, element type: ${element?.javaClass?.simpleName}"
        )

        return when (element) {
            is JsonArray -> {
                val instructions = mutableListOf<String>()

                element.forEach { item ->
                    when (item) {
                        is JsonPrimitive -> {
                            item.contentOrNull?.let { instructions.add(it) }
                        }
                        is JsonObject -> {
                            val type = item["@type"]?.jsonPrimitive?.contentOrNull
                            DebugConfig.debugLog(
                                DebugConfig.Category.IMPORT,
                                "Instruction item @type: $type"
                            )

                            when (type) {
                                "HowToStep" -> {
                                    // Single step with text
                                    val text = item["text"]?.jsonPrimitive?.contentOrNull
                                        ?: item["name"]?.jsonPrimitive?.contentOrNull
                                    text?.let { instructions.add(it) }
                                }
                                "HowToSection" -> {
                                    // Section containing multiple steps
                                    // Add section name as header if present
                                    val sectionName = item["name"]?.jsonPrimitive?.contentOrNull
                                    if (!sectionName.isNullOrBlank()) {
                                        instructions.add("$sectionName:")
                                    }

                                    val steps = item["itemListElement"]
                                    if (steps is JsonArray) {
                                        steps.forEach { step ->
                                            if (step is JsonObject) {
                                                val text = step["text"]?.jsonPrimitive?.contentOrNull
                                                    ?: step["name"]?.jsonPrimitive?.contentOrNull
                                                text?.let { instructions.add(it) }
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    // Fallback: try to get text or name field
                                    val text = item["text"]?.jsonPrimitive?.contentOrNull
                                        ?: item["name"]?.jsonPrimitive?.contentOrNull
                                    text?.let { instructions.add(it) }
                                }
                            }
                        }
                        else -> {
                            DebugConfig.debugLog(
                                DebugConfig.Category.IMPORT,
                                "Unknown instruction item type: ${item?.javaClass?.simpleName}"
                            )
                        }
                    }
                }

                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Parsed ${instructions.size} instruction steps"
                )

                instructions.filter { it.isNotBlank() }
            }
            is JsonPrimitive -> {
                listOf(element.content)
            }
            is JsonObject -> {
                // Single HowToStep or HowToSection
                val type = element["@type"]?.jsonPrimitive?.contentOrNull
                when (type) {
                    "HowToStep" -> {
                        listOfNotNull(
                            element["text"]?.jsonPrimitive?.contentOrNull
                                ?: element["name"]?.jsonPrimitive?.contentOrNull
                        )
                    }
                    else -> emptyList()
                }
            }
            else -> {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "No instructions found or unknown format"
                )
                emptyList()
            }
        }
    }

    /**
     * Parse servings - can be string or number
     */
    private fun parseServings(element: JsonElement?): Int? {
        return when (element) {
            is JsonPrimitive -> {
                element.contentOrNull?.let { str ->
                    // Extract first number from string like "4 servings" or "4-6"
                    Regex("\\d+").find(str)?.value?.toIntOrNull()
                }
            }
            else -> null
        }
    }

    /**
     * Parse ISO 8601 duration to minutes (e.g., "PT30M" = 30 minutes)
     */
    private fun parseIsoDuration(duration: String?): Int? {
        if (duration.isNullOrBlank()) return null

        val hoursMatch = Regex("(\\d+)H").find(duration)
        val minutesMatch = Regex("(\\d+)M").find(duration)

        val hours = hoursMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minutes = minutesMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0

        return if (hours == 0 && minutes == 0) null else (hours * 60 + minutes)
    }

    /**
     * Parse image - can be string URL or ImageObject
     */
    private fun parseImage(element: JsonElement?): String? {
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "Parsing image, element type: ${element?.javaClass?.simpleName}"
        )

        val imageUrl = when (element) {
            is JsonPrimitive -> element.contentOrNull
            is JsonObject -> element["url"]?.jsonPrimitive?.contentOrNull
            is JsonArray -> {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Image is array with ${element.size} items"
                )
                element.firstOrNull()?.let { parseImage(it) }
            }
            else -> null
        }

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "Extracted image URL: ${imageUrl ?: "none"}"
        )

        return imageUrl
    }

    /**
     * Parse JSON array to list of strings
     * Handles comma-separated strings (e.g., "tag1, tag2, tag3")
     */
    private fun parseJsonArrayToStrings(element: JsonElement?): List<String> {
        return when (element) {
            is JsonArray -> element.mapNotNull { it.jsonPrimitive?.contentOrNull }
            is JsonPrimitive -> {
                // Split comma-separated values and trim whitespace
                element.content.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }
            else -> emptyList()
        }
    }

    /**
     * Fallback: Parse Open Graph meta tags
     */
    private fun parseOpenGraph(document: Document): ParsedRecipeData? {
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

/**
 * Convert ParsedRecipeData to Recipe entity
 */
private fun ParsedRecipeData.toRecipe(sourceUrl: String): Recipe {
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
        notes = description,
        source = RecipeSource.URL,
        sourceUrl = sourceUrl,
        photoPath = imageUrl, // Save image URL to photoPath
        isFavorite = false,
        isTemplate = false,
        createdAt = now,
        updatedAt = now
    )
}
