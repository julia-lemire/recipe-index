package com.recipeindex.app.data.parsers

import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeSource
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.TagStandardizer

/**
 * TextRecipeParser - Smart parsing of unstructured recipe text
 *
 * Uses pattern matching to detect and extract recipe sections from plain text.
 * Shared by PDF and Photo parsers.
 */
object TextRecipeParser {

    /**
     * Parse recipe from plain text using pattern matching
     * @param text Raw text extracted from PDF or photo
     * @param source RecipeSource.PDF or RecipeSource.PHOTO
     * @param sourceIdentifier Optional identifier (file path, etc.)
     */
    fun parseText(text: String, source: RecipeSource, sourceIdentifier: String? = null): Result<Recipe> {
        return try {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Parsing text recipe (${text.length} chars)"
            )

            val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }

            if (lines.isEmpty()) {
                return Result.failure(Exception("No text found to parse"))
            }

            // Detect sections
            val sections = detectSections(lines)

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Detected sections: ${sections.keys.joinToString()}"
            )

            // Extract recipe data
            val title = extractTitle(sections, lines)
            val ingredients = extractIngredients(sections, lines)
            val instructions = extractInstructions(sections, lines)
            val servings = extractServings(sections, lines)
            val prepTime = extractPrepTime(sections, lines)
            val cookTime = extractCookTime(sections, lines)
            val tags = extractTags(sections, lines)

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Parsed: title='$title', ${ingredients.size} ingredients, ${instructions.size} instructions"
            )

            val now = System.currentTimeMillis()
            val recipe = Recipe(
                id = 0,
                title = title,
                ingredients = ingredients,
                instructions = instructions,
                servings = servings,
                prepTimeMinutes = prepTime,
                cookTimeMinutes = cookTime,
                tags = TagStandardizer.standardize(tags),
                notes = null,
                source = source,
                sourceUrl = sourceIdentifier,
                photoPath = null,
                isFavorite = false,
                isTemplate = false,
                createdAt = now,
                updatedAt = now
            )

            Result.success(recipe)
        } catch (e: Exception) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Failed to parse text: ${e.message}"
            )
            Result.failure(Exception("Failed to parse recipe from text: ${e.message}", e))
        }
    }

    /**
     * Detect section boundaries in the text
     * Returns map of section name to line index
     */
    private fun detectSections(lines: List<String>): Map<String, Int> {
        val sections = mutableMapOf<String, Int>()

        lines.forEachIndexed { index, line ->
            val lowerLine = line.lowercase()
            // Remove extra spaces and special characters for matching
            val normalized = lowerLine.replace(Regex("\\s+"), " ").trim()

            // Skip lines that look like footers/CTAs (contain "save", "shop", "get", etc. with ingredients)
            val isFooterOrCTA = normalized.contains(Regex("\\b(save|shop|get|view|see|more|click)\\b")) &&
                               normalized.contains(Regex("\\bingredients?\\b"))

            when {
                // Ingredients - be flexible about surrounding text, but skip footers
                normalized.contains(Regex("\\bingredients?\\b")) &&
                !sections.containsKey("ingredients") &&
                !isFooterOrCTA -> {
                    DebugConfig.debugLog(DebugConfig.Category.IMPORT, "Found ingredients at line $index: $line")
                    sections["ingredients"] = index
                }
                // Instructions - multiple common names
                (normalized.contains(Regex("\\binstructions?\\b")) ||
                 normalized.contains(Regex("\\bdirections?\\b")) ||
                 normalized.contains(Regex("\\bsteps?\\b")) ||
                 normalized.contains(Regex("\\bmethod\\b"))) && !sections.containsKey("instructions") -> {
                    DebugConfig.debugLog(DebugConfig.Category.IMPORT, "Found instructions at line $index: $line")
                    sections["instructions"] = index
                }
                // Servings
                (normalized.contains(Regex("\\bservings?\\b")) ||
                 normalized.contains(Regex("\\byield\\b")) ||
                 normalized.contains(Regex("\\bserves\\b"))) && !sections.containsKey("servings") -> {
                    sections["servings"] = index
                }
                // Prep time
                normalized.contains(Regex("\\bprep\\s*time\\b")) && !sections.containsKey("prepTime") -> {
                    sections["prepTime"] = index
                }
                // Cook time
                normalized.contains(Regex("\\bcook\\s*time\\b")) && !sections.containsKey("cookTime") -> {
                    sections["cookTime"] = index
                }
                // Total time
                normalized.contains(Regex("\\btotal\\s*time\\b")) && !sections.containsKey("totalTime") -> {
                    sections["totalTime"] = index
                }
                // Tags
                (normalized.contains(Regex("\\btags?\\b")) ||
                 normalized.contains(Regex("\\bcategories\\b")) ||
                 normalized.contains(Regex("\\bcuisine\\b"))) && !sections.containsKey("tags") -> {
                    sections["tags"] = index
                }
            }
        }

        return sections
    }

    /**
     * Extract title - usually first line or line before ingredients
     */
    private fun extractTitle(sections: Map<String, Int>, lines: List<String>): String {
        val ingredientsIndex = sections["ingredients"]

        return if (ingredientsIndex != null && ingredientsIndex > 0) {
            // Title is likely the line before ingredients section
            lines.take(ingredientsIndex).firstOrNull { it.length > 3 } ?: lines.firstOrNull() ?: "Imported Recipe"
        } else {
            // Use first non-empty line
            lines.firstOrNull() ?: "Imported Recipe"
        }
    }

    /**
     * Extract ingredients - lines between "Ingredients" and next section
     */
    private fun extractIngredients(sections: Map<String, Int>, lines: List<String>): List<String> {
        val startIndex = sections["ingredients"] ?: return emptyList()
        val endIndex = findNextSection(startIndex, sections)

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "Extracting ingredients from line $startIndex to $endIndex (${endIndex - startIndex - 1} lines)"
        )
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "  Header line: '${lines.getOrNull(startIndex)}'"
        )
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "  First content lines: '${lines.getOrNull(startIndex + 1)}', '${lines.getOrNull(startIndex + 2)}'"
        )

        val extracted = lines.subList(startIndex + 1, endIndex.coerceAtMost(lines.size))
            .filter { it.isNotBlank() }
            .filter { !isWebsiteNoise(it) }
            .filter { looksLikeIngredient(it) }
            .map { cleanIngredient(it) }

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "Extracted ${extracted.size} ingredients: ${extracted.take(3).joinToString(", ")}..."
        )

        return extracted
    }

    /**
     * Extract instructions - lines between "Instructions" and next section
     */
    private fun extractInstructions(sections: Map<String, Int>, lines: List<String>): List<String> {
        val startIndex = sections["instructions"] ?: return emptyList()
        val endIndex = findNextSection(startIndex, sections)

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "Extracting instructions from line $startIndex to $endIndex (${endIndex - startIndex - 1} lines)"
        )
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "  Header line: '${lines.getOrNull(startIndex)}'"
        )
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "  First content lines: '${lines.getOrNull(startIndex + 1)}', '${lines.getOrNull(startIndex + 2)}'"
        )

        val extracted = lines.subList(startIndex + 1, endIndex.coerceAtMost(lines.size))
            .filter { it.isNotBlank() }
            .filter { !isWebsiteNoise(it) }
            .filter { looksLikeInstruction(it) }
            .map { cleanInstruction(it) }

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "Extracted ${extracted.size} instructions: ${extracted.take(2).joinToString(", ")}..."
        )

        return extracted
    }

    /**
     * Extract servings from servings line or default to 4
     */
    private fun extractServings(sections: Map<String, Int>, lines: List<String>): Int {
        val servingsIndex = sections["servings"] ?: return 4
        val line = lines.getOrNull(servingsIndex) ?: return 4

        // Extract number from "Servings: 4" or "Serves 6" etc
        val number = Regex("\\d+").find(line)?.value?.toIntOrNull()
        return number ?: 4
    }

    /**
     * Extract prep time in minutes
     */
    private fun extractPrepTime(sections: Map<String, Int>, lines: List<String>): Int? {
        val prepIndex = sections["prepTime"] ?: return null
        val line = lines.getOrNull(prepIndex) ?: return null
        return parseTimeString(line)
    }

    /**
     * Extract cook time in minutes
     */
    private fun extractCookTime(sections: Map<String, Int>, lines: List<String>): Int? {
        val cookIndex = sections["cookTime"] ?: return null
        val line = lines.getOrNull(cookIndex) ?: return null
        return parseTimeString(line)
    }

    /**
     * Extract tags from tags line
     */
    private fun extractTags(sections: Map<String, Int>, lines: List<String>): List<String> {
        val tagsIndex = sections["tags"] ?: return emptyList()
        val line = lines.getOrNull(tagsIndex) ?: return emptyList()

        // Remove "Tags:", "Categories:", etc. and split by comma
        val cleaned = line.replace(Regex("^(tags?|categories?|cuisine):?\\s*", RegexOption.IGNORE_CASE), "")
        return cleaned.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    /**
     * Parse time string like "30 minutes", "1 hour 15 min", "45m" to minutes
     */
    private fun parseTimeString(timeStr: String): Int? {
        val lowerStr = timeStr.lowercase()

        // Extract hours
        val hours = Regex("(\\d+)\\s*h(ou)?r?s?").find(lowerStr)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        // Extract minutes
        val minutes = Regex("(\\d+)\\s*m(in)?(ute)?s?").find(lowerStr)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        val total = hours * 60 + minutes
        return if (total > 0) total else null
    }

    /**
     * Find the index of the next section after the given index
     */
    private fun findNextSection(currentIndex: Int, sections: Map<String, Int>): Int {
        return sections.values
            .filter { it > currentIndex }
            .minOrNull() ?: Int.MAX_VALUE
    }

    /**
     * Check if a line is website noise (navigation, CTA, footer text)
     */
    private fun isWebsiteNoise(line: String): Boolean {
        val lower = line.lowercase()

        // Common website CTAs and navigation
        val noisyPatterns = listOf(
            Regex("\\b(save|shop|get|view|see|click|subscribe|sign\\s*up|log\\s*in|create|download)\\b.*\\b(recipe|ingredient|meal|plan|list)"),
            Regex("\\b(rating|comment|review|feedback)\\b.*\\b(let|know|help|business|thrive)"),
            Regex("\\b(free|high[\\s-]quality|continue|providing)\\b"),
            Regex("^\\s*[a-z]\\s+[a-z]\\s+[a-z]"), // Spaced out letters like "S H O P"
            Regex("\\b(newsletter|social|follow|share|pin|tweet)\\b"),
            Regex("\\b(privacy|policy|terms|conditions|copyright)\\b"),
            Regex("^(home|about|contact|blog|search)$", RegexOption.IGNORE_CASE)
        )

        return noisyPatterns.any { it.containsMatchIn(lower) }
    }

    /**
     * Check if a line looks like an ingredient
     */
    private fun looksLikeIngredient(line: String): Boolean {
        val lower = line.lowercase()

        // Too short to be an ingredient
        if (line.length < 3) return false

        // Contains common ingredient indicators
        val hasQuantity = Regex("\\d+\\s*(cup|tablespoon|teaspoon|tbsp|tsp|oz|pound|lb|gram|ml|liter|inch)").containsMatchIn(lower)
        val hasCommonWords = Regex("\\b(cup|teaspoon|tablespoon|ounce|pound|slice|dice|chop|mince|fresh|dried)").containsMatchIn(lower)
        val hasIngredientName = Regex("\\b(chicken|beef|pork|fish|egg|milk|cheese|butter|oil|flour|sugar|salt|pepper|onion|garlic|tomato|potato|rice|pasta)").containsMatchIn(lower)

        // Looks like ingredient if it has measurements OR common food words
        return hasQuantity || hasCommonWords || hasIngredientName
    }

    /**
     * Check if a line looks like an instruction
     */
    private fun looksLikeInstruction(line: String): Boolean {
        val lower = line.lowercase()

        // Too short to be a useful instruction
        if (line.length < 10) return false

        // Contains cooking verbs
        val hasCookingVerb = Regex("\\b(preheat|heat|cook|bake|boil|simmer|fry|saute|stir|mix|combine|add|remove|place|transfer|turn|flip|season|serve)").containsMatchIn(lower)

        // Has temperature or time indicators
        val hasTemperatureOrTime = Regex("\\b(\\d+\\s*°?[fc]|\\d+\\s*(minute|hour|second|min|hr))").containsMatchIn(lower)

        // Avoid footer text patterns
        val isFooter = Regex("\\b(rate|rating|comment|review|subscribe|newsletter|business|website)").containsMatchIn(lower)

        return (hasCookingVerb || hasTemperatureOrTime) && !isFooter
    }

    /**
     * Clean ingredient line - remove leading bullets, numbers, etc.
     */
    private fun cleanIngredient(line: String): String {
        return line
            .replace(Regex("^[•\\-*]\\s*"), "") // Remove bullets
            .replace(Regex("^\\d+\\.?\\s*"), "") // Remove numbering
            .trim()
    }

    /**
     * Clean instruction line - remove step numbers if present
     */
    private fun cleanInstruction(line: String): String {
        return line
            .replace(Regex("^Step\\s*\\d+:?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^\\d+\\.?\\s*"), "") // Remove numbering
            .trim()
    }
}
