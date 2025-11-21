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

            // Debug: Log all lines for analysis
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "=== RAW LINES (${lines.size} total) ==="
            )
            lines.forEachIndexed { idx, line ->
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "  [$idx] ${line.take(80)}${if (line.length > 80) "..." else ""}"
                )
            }

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
            var ingredients = extractIngredients(sections, lines)
            var instructions = extractInstructions(sections, lines)

            // Recovery: If we got 0 ingredients but have instructions with ingredient-like content,
            // the PDF text extraction likely had column ordering issues
            if (ingredients.isEmpty() && instructions.isNotEmpty()) {
                val (recoveredIngredients, filteredInstructions) = recoverMisplacedIngredients(instructions)
                if (recoveredIngredients.isNotEmpty()) {
                    DebugConfig.debugLog(
                        DebugConfig.Category.IMPORT,
                        "Recovered ${recoveredIngredients.size} misplaced ingredients from instructions"
                    )
                    ingredients = recoveredIngredients
                    instructions = filteredInstructions
                }
            }

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
            // CTA patterns - use plurals (recipes?, ingredients?, etc.)
            Regex("\\b(save|shop|get|view|see|click|subscribe|sign\\s*up|log\\s*in|create|download)\\b.*\\b(recipes?|ingredients?|meals?|plans?|lists?|shopping)"),
            // Rating/comment prompts
            Regex("\\b(rating|comment|review|feedback)\\b.*\\b(let|know|help|business|thrive)"),
            Regex("\\blast\\s*step\\b.*\\b(rating|comment|review)"),
            Regex("\\b(leave|please)\\b.*\\b(rating|comment|review)"),
            // Marketing phrases
            Regex("\\b(free|high[\\s-]quality|continue|providing)\\b"),
            Regex("\\b(business|thrive|continue\\s+providing)\\b"),
            // App promo text
            Regex("\\b(meal\\s*plans?)\\b.*\\b(and\\s+more|create)"),
            Regex("\\bshopping\\s+lists?\\b"),
            // Spaced out letters like "S H O P"
            Regex("^\\s*[a-z]\\s+[a-z]\\s+[a-z]"),
            // Social/newsletter
            Regex("\\b(newsletter|social|follow|share|pin|tweet)\\b"),
            // Footer/legal
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

        // Contains common ingredient indicators - with Unicode fractions
        val hasQuantity = Regex("(\\d+|[½¼¾⅓⅔⅛⅜⅝⅞])\\s*(cups?|tablespoons?|teaspoons?|tbsp|tsp|oz|ounces?|pounds?|lbs?|grams?|g|ml|liters?|l|inch|inches|cloves?|slices?|pieces?|cans?|jars?|bunche?s?|stalks?|heads?|sprigs?)").containsMatchIn(lower)
        val hasCommonWords = Regex("\\b(cups?|teaspoons?|tablespoons?|ounces?|pounds?|sliced?|diced?|chopped?|minced?|fresh|dried|whole|large|medium|small|thin|thick|boneless|skinless|shredded)").containsMatchIn(lower)
        // Expanded ingredient name list
        val hasIngredientName = Regex("\\b(chicken|beef|pork|fish|salmon|shrimp|egg|eggs|milk|cream|cheese|butter|oil|olive|flour|sugar|salt|pepper|onion|garlic|tomato|potato|rice|pasta|noodle|bread|lemon|lime|cilantro|parsley|basil|oregano|thyme|rosemary|cumin|paprika|cayenne|chili|jalapeño|bell|carrot|celery|broccoli|spinach|lettuce|cabbage|mushroom|zucchini|squash|corn|bean|pea|chickpea|lentil|avocado|cucumber|apple|banana|berry|orange|ginger|soy|vinegar|wine|broth|stock|honey|maple|vanilla|cinnamon|nutmeg|cherry|jarred|canned|drained|rinsed)s?").containsMatchIn(lower)
        // Starts with Unicode fraction (common in PDFs)
        val startsWithFraction = Regex("^[½¼¾⅓⅔⅛⅜⅝⅞]").containsMatchIn(line)
        // Starts with measurement (e.g., "cup red onion" where number was on previous line)
        val startsWithMeasurement = Regex("^(cups?|tablespoons?|teaspoons?|tbsp|tsp|oz|ounces?|pounds?|lbs?|grams?|g|ml|cloves?|slices?|pieces?|cans?|jars?)\\s").containsMatchIn(lower)

        // Looks like ingredient if it has measurements OR common food words
        return hasQuantity || hasCommonWords || hasIngredientName || startsWithFraction || startsWithMeasurement
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

    /**
     * Recover misplaced ingredients from instructions.
     * PDF text extraction can mix columns, placing ingredients after "Instructions" header.
     * This function separates ingredient-like lines from real instructions.
     *
     * @param instructions List of parsed instruction lines
     * @return Pair of (recovered ingredients, filtered instructions)
     */
    private fun recoverMisplacedIngredients(instructions: List<String>): Pair<List<String>, List<String>> {
        val recoveredIngredients = mutableListOf<String>()
        val filteredInstructions = mutableListOf<String>()

        // Patterns that strongly indicate an ingredient (not an instruction)
        val ingredientPatterns = listOf(
            // Starts with number/fraction and measurement
            Regex("^(\\d+[\\s/]*\\d*|[½¼¾⅓⅔⅛⅜⅝⅞])\\s*(cups?|tablespoons?|teaspoons?|tbsp|tsp|oz|ounces?|pounds?|lbs?|grams?|g|ml|cloves?|slices?|pieces?|cans?|jars?)\\b", RegexOption.IGNORE_CASE),
            // Starts with measurement unit (number on previous line in PDF)
            Regex("^(cups?|tablespoons?|teaspoons?|tbsp|tsp|oz|ounces?|pounds?|lbs?)\\s+\\w", RegexOption.IGNORE_CASE),
            // Contains parenthetical description like "(from 1 small onion)"
            Regex("\\(from\\s+\\d+\\s+\\w+", RegexOption.IGNORE_CASE),
            // Common ingredient list patterns
            Regex(",\\s*(sliced|diced|chopped|minced|quartered|halved)\\b", RegexOption.IGNORE_CASE),
            // Ingredient with thickness: "sliced ¼ inch thick"
            Regex("(sliced|cut)\\s+[¼½¾⅓⅔⅛⅜⅝⅞\\d]+\\s*inch", RegexOption.IGNORE_CASE)
        )

        // Patterns that strongly indicate an instruction (not an ingredient)
        val instructionPatterns = listOf(
            // Starts with cooking verb
            Regex("^(preheat|heat|cook|bake|boil|simmer|fry|saute|stir|mix|combine|add|remove|place|transfer|turn|flip|season|serve|let|allow|cover|uncover|drain|rinse|set|arrange|spread|brush|drizzle|sprinkle|garnish|refrigerate|marinate|rest|cool|warm)\\b", RegexOption.IGNORE_CASE),
            // Contains "until" (indicates cooking process)
            Regex("\\buntil\\b", RegexOption.IGNORE_CASE),
            // Contains cooking time/temperature
            Regex("\\b(\\d+\\s*°?[fc]|\\d+\\s*(minutes?|mins?|hours?|hrs?|seconds?))\\b", RegexOption.IGNORE_CASE),
            // Mentions cooking equipment
            Regex("\\b(oven|pan|skillet|pot|bowl|baking sheet|sheet pan|grill|microwave)\\b", RegexOption.IGNORE_CASE)
        )

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "=== RECOVERY: Analyzing ${instructions.size} lines ==="
        )

        for ((index, line) in instructions.withIndex()) {
            val isLikelyIngredient = ingredientPatterns.any { it.containsMatchIn(line) }
            val isLikelyInstruction = instructionPatterns.any { it.containsMatchIn(line) }

            val classification: String
            when {
                // Clearly an instruction
                isLikelyInstruction && !isLikelyIngredient -> {
                    filteredInstructions.add(line)
                    classification = "INSTR (pattern)"
                }
                // Clearly an ingredient
                isLikelyIngredient && !isLikelyInstruction -> {
                    recoveredIngredients.add(line)
                    classification = "INGR (pattern)"
                }
                // Both match - prefer instruction (likely a step that mentions an ingredient)
                isLikelyIngredient && isLikelyInstruction -> {
                    filteredInstructions.add(line)
                    classification = "INSTR (both)"
                }
                // Neither match strongly - use original looksLikeIngredient check
                else -> {
                    if (looksLikeIngredient(line) && !looksLikeInstruction(line)) {
                        recoveredIngredients.add(line)
                        classification = "INGR (fallback)"
                    } else {
                        filteredInstructions.add(line)
                        classification = "INSTR (fallback)"
                    }
                }
            }

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "  [$index] $classification: ${line.take(60)}${if (line.length > 60) "..." else ""}"
            )
        }

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "Recovery result: ${recoveredIngredients.size} ingredients, ${filteredInstructions.size} instructions"
        )

        return Pair(recoveredIngredients, filteredInstructions)
    }
}
