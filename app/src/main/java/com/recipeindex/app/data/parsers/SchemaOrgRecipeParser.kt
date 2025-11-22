package com.recipeindex.app.data.parsers

import com.recipeindex.app.utils.DebugConfig
import kotlinx.serialization.json.*
import org.jsoup.nodes.Document

/**
 * SchemaOrgRecipeParser - Parses Schema.org Recipe JSON-LD from HTML documents
 *
 * Extracts structured recipe data from Schema.org JSON-LD markup embedded in HTML.
 * Handles Recipe, Article, and BlogPosting types with embedded recipe fields.
 */
class SchemaOrgRecipeParser {

    private val htmlScraper = HtmlScraper()

    /**
     * Parse Schema.org Recipe JSON-LD from document
     * @return ParsedRecipeData if Recipe found, null otherwise
     */
    fun parse(document: Document): ParsedRecipeData? {
        // Find script tags with type="application/ld+json"
        val jsonLdScripts = document.select("script[type=application/ld+json]")

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "[IMPORT] Found ${jsonLdScripts.size} JSON-LD script tags"
        )

        for ((index, script) in jsonLdScripts.withIndex()) {
            try {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[IMPORT] Parsing JSON-LD script tag ${index + 1}"
                )

                val json = Json.parseToJsonElement(script.data())

                // Handle both single object and array of objects
                val recipes = when {
                    json is JsonObject && isRecipeType(json) -> {
                        DebugConfig.debugLog(
                            DebugConfig.Category.IMPORT,
                            "[IMPORT] Found Recipe object (single)"
                        )
                        listOf(json)
                    }
                    json is JsonArray -> {
                        val recipeList = json.filterIsInstance<JsonObject>()
                            .filter { isRecipeType(it) }
                        DebugConfig.debugLog(
                            DebugConfig.Category.IMPORT,
                            "[IMPORT] Found ${recipeList.size} Recipe objects in array"
                        )
                        recipeList
                    }
                    json is JsonObject && json["@graph"] is JsonArray -> {
                        val recipeList = (json["@graph"] as JsonArray).filterIsInstance<JsonObject>()
                            .filter { isRecipeType(it) }
                        DebugConfig.debugLog(
                            DebugConfig.Category.IMPORT,
                            "[IMPORT] Found ${recipeList.size} Recipe objects in @graph"
                        )
                        recipeList
                    }
                    else -> {
                        // Log what @type we actually found for debugging
                        val typeInfo = when {
                            json is JsonObject -> {
                                val type = json["@type"]
                                when (type) {
                                    is JsonPrimitive -> "found @type: \"${type.content}\""
                                    is JsonArray -> "found @type array: [${type.joinToString { (it as? JsonPrimitive)?.content ?: "?" }}]"
                                    null -> "no @type field found"
                                    else -> "unknown @type structure"
                                }
                            }
                            json is JsonArray -> "array with ${json.size} objects, none are Recipe type"
                            else -> "unknown JSON structure: ${json.javaClass.simpleName}"
                        }
                        DebugConfig.debugLog(
                            DebugConfig.Category.IMPORT,
                            "[IMPORT] No Recipe objects found in this script tag ($typeInfo)"
                        )
                        emptyList()
                    }
                }

                // Parse first recipe found
                recipes.firstOrNull()?.let { recipeJson ->
                    DebugConfig.debugLog(
                        DebugConfig.Category.IMPORT,
                        "[IMPORT] Parsing recipe data from JSON-LD"
                    )
                    val parsedData = parseRecipeFromJsonLd(recipeJson)

                    // Also parse HTML categories and add them to tags
                    val htmlCategories = htmlScraper.parseCategories(document)
                    return parsedData.copy(tags = parsedData.tags + htmlCategories)
                }
            } catch (e: Exception) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[IMPORT] Error parsing JSON-LD script tag ${index + 1}: ${e.message}"
                )
                // Continue to next script tag
                continue
            }
        }

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "[IMPORT] No Recipe objects found in any JSON-LD script tags"
        )
        return null
    }

    /**
     * Parse Recipe from Schema.org JSON-LD object
     */
    private fun parseRecipeFromJsonLd(json: JsonObject): ParsedRecipeData {
        // Log available fields for debugging
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "[IMPORT] Recipe JSON-LD fields: ${json.keys.joinToString(", ")}"
        )

        val ingredients = parseJsonArrayToStrings(json["recipeIngredient"], "recipeIngredient")
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "[IMPORT] Parsed ${ingredients.size} ingredients from recipeIngredient field"
        )

        val instructions = parseInstructions(json["recipeInstructions"])
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "[IMPORT] Parsed ${instructions.size} instructions from recipeInstructions field"
        )

        val title = json["name"]?.jsonPrimitive?.contentOrNull ?: ""
        val categories = parseJsonArrayToStrings(json["recipeCategory"], "recipeCategory")
        val schemaOrgCuisines = parseJsonArrayToStrings(json["recipeCuisine"], "recipeCuisine")
        val keywords = parseJsonArrayToStrings(json["keywords"], "keywords")

        // Extract serving size from nutrition field
        val servingSize = parseServingSize(json["nutrition"])

        // Determine cuisine: prefer Schema.org, fallback to title extraction
        val titleCuisine = extractCuisineFromTitle(title)
        val cuisine = when {
            schemaOrgCuisines.isNotEmpty() -> {
                // Prefer title cuisine if it exists and Schema.org looks wrong (e.g., "American")
                if (titleCuisine != null && schemaOrgCuisines.firstOrNull()?.lowercase() == "american") {
                    titleCuisine
                } else {
                    schemaOrgCuisines.first() // Take first cuisine from Schema.org
                }
            }
            titleCuisine != null -> titleCuisine
            else -> null
        }

        // Log extracted values for debugging
        if (categories.isNotEmpty()) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[IMPORT] Recipe categories: ${categories.joinToString(", ")}"
            )
        }
        if (cuisine != null) {
            val source = when {
                titleCuisine != null && schemaOrgCuisines.firstOrNull()?.lowercase() == "american" ->
                    " (from title, Schema.org had \"${schemaOrgCuisines.first()}\")"
                titleCuisine != null && schemaOrgCuisines.isEmpty() ->
                    " (from title)"
                else ->
                    " (from recipeCuisine)"
            }
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[IMPORT] Recipe cuisine: $cuisine$source"
            )
        }
        if (keywords.isNotEmpty()) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[IMPORT] Recipe keywords: ${keywords.joinToString(", ")}"
            )
        }

        return ParsedRecipeData(
            title = title,
            description = json["description"]?.jsonPrimitive?.contentOrNull,
            ingredients = ingredients,
            instructions = instructions,
            servings = parseServings(json["recipeYield"]),
            servingSize = servingSize,
            prepTimeMinutes = parseIsoDuration(json["prepTime"]?.jsonPrimitive?.contentOrNull),
            cookTimeMinutes = parseIsoDuration(json["cookTime"]?.jsonPrimitive?.contentOrNull),
            totalTimeMinutes = parseIsoDuration(json["totalTime"]?.jsonPrimitive?.contentOrNull),
            tags = categories + keywords,  // Note: cuisines NOT included in tags anymore
            cuisine = cuisine,
            imageUrls = parseImages(json["image"])
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
     * Parse serving size from nutrition field
     * Schema.org NutritionInformation has servingSize field
     * Example: "nutrition": { "@type": "NutritionInformation", "servingSize": "1 cup" }
     */
    private fun parseServingSize(nutrition: JsonElement?): String? {
        if (nutrition !is JsonObject) return null

        val servingSize = nutrition["servingSize"]?.jsonPrimitive?.contentOrNull
        if (!servingSize.isNullOrBlank()) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[IMPORT] Found serving size from nutrition: $servingSize"
            )
        }
        return servingSize?.takeIf { it.isNotBlank() }
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
     * Parse images - can be string URL, ImageObject, or array of either
     * Returns list of all image URLs found
     */
    private fun parseImages(element: JsonElement?): List<String> {
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "Parsing images, element type: ${element?.javaClass?.simpleName}"
        )

        val imageUrls = when (element) {
            is JsonPrimitive -> {
                listOfNotNull(element.contentOrNull)
            }
            is JsonObject -> {
                // Single ImageObject - extract url field
                listOfNotNull(element["url"]?.jsonPrimitive?.contentOrNull)
            }
            is JsonArray -> {
                // Array of images - extract all
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Image is array with ${element.size} items"
                )
                element.mapNotNull { item ->
                    when (item) {
                        is JsonPrimitive -> item.contentOrNull
                        is JsonObject -> item["url"]?.jsonPrimitive?.contentOrNull
                        else -> null
                    }
                }
            }
            else -> emptyList()
        }

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "Extracted ${imageUrls.size} image URLs: ${imageUrls.joinToString(", ")}"
        )

        return imageUrls.filter { it.isNotBlank() }
    }

    /**
     * Parse JSON array to list of strings
     * Handles comma-separated strings (e.g., "tag1, tag2, tag3")
     * Handles nested objects and arrays by extracting text recursively
     */
    private fun parseJsonArrayToStrings(element: JsonElement?, fieldName: String = "unknown"): List<String> {
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "[IMPORT] Parsing field '$fieldName', type: ${element?.javaClass?.simpleName ?: "null"}"
        )

        val result = when (element) {
            is JsonArray -> {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[IMPORT] Field '$fieldName' is array with ${element.size} items"
                )
                element.mapNotNull { item ->
                    when (item) {
                        is JsonPrimitive -> item.contentOrNull
                        is JsonObject -> {
                            // Try to extract text from common Schema.org text fields
                            val extracted = item["text"]?.jsonPrimitive?.contentOrNull
                                ?: item["name"]?.jsonPrimitive?.contentOrNull
                                ?: item["@value"]?.jsonPrimitive?.contentOrNull

                            if (extracted == null) {
                                DebugConfig.debugLog(
                                    DebugConfig.Category.IMPORT,
                                    "[IMPORT] Could not extract text from object in '$fieldName'. Available fields: ${item.keys.joinToString()}"
                                )
                            }
                            extracted
                        }
                        is JsonArray -> {
                            // Recursively parse nested arrays and join with comma
                            parseJsonArrayToStrings(item, "$fieldName[nested]").joinToString(", ")
                                .ifBlank { null }
                        }
                        else -> null
                    }
                }.filter { it.isNotBlank() }
            }
            is JsonPrimitive -> {
                // Split comma-separated values and trim whitespace
                element.content.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }
            is JsonObject -> {
                // Single object - try to extract text field
                listOfNotNull(
                    element["text"]?.jsonPrimitive?.contentOrNull
                        ?: element["name"]?.jsonPrimitive?.contentOrNull
                        ?: element["@value"]?.jsonPrimitive?.contentOrNull
                )
            }
            else -> emptyList()
        }

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "[IMPORT] Field '$fieldName' parsed to ${result.size} strings"
        )

        return result
    }

    /**
     * Extract cuisine from recipe title
     * Looks for common cuisine names in titles like "Georgian Beef Stew" or "Italian Pasta"
     */
    private fun extractCuisineFromTitle(title: String): String? {
        val knownCuisines = listOf(
            "afghan", "african", "albanian", "algerian", "american", "argentinian", "armenian",
            "asian", "australian", "austrian", "azerbaijani",
            "bangladeshi", "basque", "belarusian", "belgian", "brazilian", "british", "bulgarian",
            "cajun", "cambodian", "canadian", "caribbean", "caucasian", "chinese", "colombian",
            "creole", "croatian", "cuban", "cypriot", "czech",
            "danish", "dutch",
            "eastern european", "egyptian", "english", "estonian", "ethiopian",
            "filipino", "finnish", "french", "fusion",
            "georgian", "german", "greek", "guatemalan",
            "haitian", "hawaiian", "honduran", "hungarian",
            "icelandic", "indian", "indonesian", "iranian", "iraqi", "irish", "israeli", "italian",
            "jamaican", "japanese", "jewish", "jordanian",
            "kenyan", "korean", "kurdish",
            "laotian", "latin", "latvian", "lebanese", "libyan", "lithuanian",
            "macedonian", "malaysian", "mediterranean", "mexican", "middle eastern", "moldovan",
            "mongolian", "moroccan", "mozambican",
            "nepalese", "new zealand", "nicaraguan", "nigerian", "norwegian",
            "pakistani", "palestinian", "peruvian", "polish", "portuguese", "puerto rican",
            "romanian", "russian",
            "salvadoran", "scandinavian", "scottish", "serbian", "singaporean", "slovak",
            "slovenian", "somali", "south african", "southern", "spanish", "sri lankan",
            "sudanese", "swedish", "swiss", "syrian",
            "taiwanese", "tajik", "thai", "tibetan", "trinidadian", "tunisian", "turkish", "turkmen",
            "ugandan", "ukrainian", "uruguayan", "uzbek",
            "venezuelan", "vietnamese",
            "welsh",
            "yemeni"
        )

        val lowerTitle = title.lowercase()

        // Look for cuisine names at the start of the title or in parentheses
        for (cuisine in knownCuisines) {
            // Check if title starts with cuisine name (e.g., "Georgian Beef Stew")
            if (lowerTitle.startsWith("$cuisine ") || lowerTitle.startsWith("($cuisine ")) {
                return cuisine
            }
            // Check if cuisine is in parentheses (e.g., "Beef Stew (Georgian)")
            if (lowerTitle.contains("($cuisine)") || lowerTitle.contains("($cuisine ")) {
                return cuisine
            }
        }

        return null
    }

    /**
     * Check if a JSON object has @type of "Recipe" or is an Article with recipe fields
     * Handles both string and array @type values
     */
    private fun isRecipeType(obj: JsonObject): Boolean {
        val typeElement = obj["@type"] ?: return false

        // Check if explicitly typed as Recipe
        val hasRecipeType = when (typeElement) {
            is JsonPrimitive -> typeElement.content == "Recipe"
            is JsonArray -> typeElement.any {
                it is JsonPrimitive && it.content == "Recipe"
            }
            else -> false
        }

        if (hasRecipeType) return true

        // Some sites use Article type but embed recipe data
        // Check if this Article has recipe-specific fields
        val hasArticleType = when (typeElement) {
            is JsonPrimitive -> typeElement.content == "Article" || typeElement.content == "BlogPosting"
            is JsonArray -> typeElement.any {
                it is JsonPrimitive && (it.content == "Article" || it.content == "BlogPosting")
            }
            else -> false
        }

        if (hasArticleType) {
            // Log what fields the Article has
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[IMPORT] Article/BlogPosting found with fields: ${obj.keys.joinToString(", ")}"
            )

            // Consider it a recipe if it has recipeIngredient or recipeInstructions
            val hasRecipeFields = obj.containsKey("recipeIngredient") || obj.containsKey("recipeInstructions")
            if (hasRecipeFields) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[IMPORT] Article/BlogPosting with recipe fields detected, treating as Recipe"
                )
                return true
            }
        }

        return false
    }

}
