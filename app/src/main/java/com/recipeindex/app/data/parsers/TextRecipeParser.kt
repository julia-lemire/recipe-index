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

            // Recovery: If we got 0 ingredients, the PDF text extraction likely had column ordering issues
            // Re-extract ALL lines after Instructions header (without instruction filter) and let recovery sort them
            if (ingredients.isEmpty()) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "No ingredients found - attempting recovery with unfiltered instruction lines"
                )

                // Re-extract without the looksLikeInstruction filter to get ALL candidate lines
                val allCandidateLines = extractInstructions(sections, lines, skipInstructionFilter = true)

                if (allCandidateLines.isNotEmpty()) {
                    val (recoveredIngredients, filteredInstructions) = recoverMisplacedIngredients(allCandidateLines)
                    if (recoveredIngredients.isNotEmpty()) {
                        DebugConfig.debugLog(
                            DebugConfig.Category.IMPORT,
                            "Recovered ${recoveredIngredients.size} misplaced ingredients from ${allCandidateLines.size} candidate lines"
                        )
                        ingredients = recoveredIngredients
                        instructions = filteredInstructions
                    }
                }
            }

            val servings = extractServings(sections, lines)
            val servingSize = extractServingSize(sections, lines)
            val prepTime = extractPrepTime(sections, lines)
            val cookTime = extractCookTime(sections, lines)
            val tags = extractTags(sections, lines)
            val sourceTips = extractTips(sections, lines)

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Parsed: title='$title', ${ingredients.size} ingredients, ${instructions.size} instructions, servingSize='$servingSize', tips=${sourceTips != null}"
            )

            val now = System.currentTimeMillis()
            val recipe = Recipe(
                id = 0,
                title = title,
                ingredients = ingredients,
                instructions = instructions,
                servings = servings,
                servingSize = servingSize,
                prepTimeMinutes = prepTime,
                cookTimeMinutes = cookTime,
                tags = TagStandardizer.standardize(tags),
                notes = null,
                sourceTips = sourceTips,
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

            // Skip breadcrumb navigation lines for section detection
            val isBreadcrumb = line.contains(" > ")

            when {
                // Title - explicit "Title:" header
                normalized.contains(Regex("^title\\s*:")) && !sections.containsKey("title") -> {
                    DebugConfig.debugLog(DebugConfig.Category.IMPORT, "Found title at line $index: $line")
                    sections["title"] = index
                }
                // Ingredients - must be standalone header, not in breadcrumbs or prose
                normalized.contains(Regex("^ingredients?\\s*:?\\s*$")) &&
                !sections.containsKey("ingredients") &&
                !isFooterOrCTA &&
                !isBreadcrumb -> {
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
                // Notes/Tips section
                (normalized.contains(Regex("^notes?\\s*:?\\s*$")) ||
                 normalized.contains(Regex("^tips?\\s*:?\\s*$")) ||
                 normalized.contains(Regex("^recipe\\s*(notes?|tips?)\\s*:?\\s*$")) ||
                 normalized.contains(Regex("^cooking\\s*tips?\\s*:?\\s*$")) ||
                 normalized.contains(Regex("^variations?\\s*:?\\s*$")) ||
                 normalized.contains(Regex("^substitutions?\\s*:?\\s*$"))) && !sections.containsKey("notes") -> {
                    DebugConfig.debugLog(DebugConfig.Category.IMPORT, "Found notes/tips at line $index: $line")
                    sections["notes"] = index
                }
                // Nutrition section (marks end of notes)
                normalized.contains(Regex("^nutrition\\s*:?\\s*$")) && !sections.containsKey("nutrition") -> {
                    sections["nutrition"] = index
                }
            }
        }

        return sections
    }

    /**
     * Extract title - from explicit "Title:" header, first line, or line before ingredients
     * Skips lines that look like dates, timestamps, URLs, breadcrumbs, or metadata
     */
    private fun extractTitle(sections: Map<String, Int>, lines: List<String>): String {
        // Check for explicit "Title:" section first
        val titleIndex = sections["title"]
        if (titleIndex != null) {
            val line = lines.getOrNull(titleIndex) ?: ""
            // Remove "Title:" prefix and return the value
            val title = line.replace(Regex("^title\\s*:\\s*", RegexOption.IGNORE_CASE), "").trim()
            if (title.isNotBlank()) {
                DebugConfig.debugLog(DebugConfig.Category.IMPORT, "Using explicit title: $title")
                return title
            }
        }

        val ingredientsIndex = sections["ingredients"]

        // Filter out lines that shouldn't be titles
        fun isValidTitle(line: String): Boolean {
            if (line.length < 4) return false
            val lower = line.lowercase()
            // Skip date/time patterns like "11/18/25, 12:34 PM"
            if (Regex("^\\d{1,2}/\\d{1,2}/\\d{2,4}").containsMatchIn(line)) return false
            // Skip URL-like lines
            if (Regex("^https?://|www\\.|\\.(com|org|net|io)").containsMatchIn(lower)) return false
            // Skip breadcrumb navigation patterns like "Site > Category > Recipe Name"
            if (line.contains(" > ")) return false
            // Skip lines that are mostly numbers/punctuation
            if (line.count { it.isLetter() } < line.length / 2) return false
            // Skip CTA/marketing lines
            if (lower.contains("subscribe") || lower.contains("sign up") || lower.contains("newsletter")) return false
            // Skip "More X Recipes You May Like" type lines
            if (lower.startsWith("more ") && lower.contains("recipe")) return false
            // Skip lines that start with common website noise
            if (Regex("^(jump to|print|save|share|pin|rate|email)\\b").containsMatchIn(lower)) return false
            // Skip section headers (Title:, Servings:, etc.)
            if (Regex("^(title|servings?|prep\\s*time|cook\\s*time|tags?)\\s*:", RegexOption.IGNORE_CASE).containsMatchIn(line)) return false
            return true
        }

        return if (ingredientsIndex != null && ingredientsIndex > 0) {
            // Title is likely before ingredients section - find first valid title
            lines.take(ingredientsIndex).firstOrNull { isValidTitle(it) }
                ?: lines.firstOrNull { isValidTitle(it) }
                ?: "Imported Recipe"
        } else {
            // Use first valid line
            lines.firstOrNull { isValidTitle(it) } ?: "Imported Recipe"
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
     * @param skipInstructionFilter If true, skip looksLikeInstruction filter (for recovery scenarios)
     */
    private fun extractInstructions(
        sections: Map<String, Int>,
        lines: List<String>,
        skipInstructionFilter: Boolean = false
    ): List<String> {
        val startIndex = sections["instructions"] ?: return emptyList()
        val endIndex = findNextSection(startIndex, sections)

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "Extracting instructions from line $startIndex to $endIndex (${endIndex - startIndex - 1} lines), skipFilter=$skipInstructionFilter"
        )
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "  Header line: '${lines.getOrNull(startIndex)}'"
        )
        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "  First content lines: '${lines.getOrNull(startIndex + 1)}', '${lines.getOrNull(startIndex + 2)}'"
        )

        // First filter noise, then join continuation lines, then clean
        val filteredLines = lines.subList(startIndex + 1, endIndex.coerceAtMost(lines.size))
            .filter { it.isNotBlank() }
            .filter { !isWebsiteNoise(it) }
            .filter { !isPdfPageNoise(it) }

        // Join continuation lines (lines not starting with a digit continue previous)
        val joinedLines = joinInstructionLines(filteredLines)

        val extracted = joinedLines
            .let { joined ->
                if (skipInstructionFilter) joined
                else joined.filter { looksLikeInstruction(it) }
            }
            .map { cleanInstruction(it) }

        DebugConfig.debugLog(
            DebugConfig.Category.IMPORT,
            "Extracted ${extracted.size} instructions (from ${filteredLines.size} lines, joined to ${joinedLines.size}): ${extracted.take(2).joinToString(", ")}..."
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
     * Extract serving size (portion size) from lines
     * Patterns: "Serving Size: 1 ½ cups", "Portion: 200g", "Per serving: 1 cup"
     */
    private fun extractServingSize(sections: Map<String, Int>, lines: List<String>): String? {
        // First check the servings line for "Serving Size:" pattern
        val servingsIndex = sections["servings"]
        if (servingsIndex != null) {
            val line = lines.getOrNull(servingsIndex)
            if (line != null) {
                val servingSizeMatch = extractServingSizeFromLine(line)
                if (servingSizeMatch != null) {
                    DebugConfig.debugLog(
                        DebugConfig.Category.IMPORT,
                        "Found serving size in servings line: $servingSizeMatch"
                    )
                    return servingSizeMatch
                }
            }
        }

        // Scan all lines for serving size patterns
        for (line in lines) {
            val match = extractServingSizeFromLine(line)
            if (match != null) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "Found serving size in line: $match"
                )
                return match
            }
        }

        return null
    }

    /**
     * Extract serving size value from a single line
     */
    private fun extractServingSizeFromLine(line: String): String? {
        // Pattern: "Serving Size: X" or "Portion: X" or "Per Serving: X"
        // Include / for OCR fractions like "1/2" or "1 /2"
        val patterns = listOf(
            // "Serving Size: 1 ½ cups" or "Serving Size: 1/2 cup" (with unit)
            Regex("serving\\s*size[:\\s]+([\\d½¼¾⅓⅔⅛⅜⅝⅞/\\s]+(?:cups?|tablespoons?|teaspoons?|tbsp|tsp|oz|ounces?|pounds?|lbs?|grams?|g|ml|liters?|l|pieces?|slices?|servings?))", RegexOption.IGNORE_CASE),
            // "Serving Size: 1/4" (fraction without unit - means 1/4 of recipe)
            Regex("serving\\s*size[:\\s]+([\\d½¼¾⅓⅔⅛⅜⅝⅞]+\\s*/\\s*[\\d½¼¾⅓⅔⅛⅜⅝⅞]+|[½¼¾⅓⅔⅛⅜⅝⅞])(?:\\s|$|\\d+x)", RegexOption.IGNORE_CASE),
            // "Portion: 200g"
            Regex("portion[:\\s]+([\\d½¼¾⅓⅔⅛⅜⅝⅞/\\s]+(?:cups?|tablespoons?|teaspoons?|tbsp|tsp|oz|ounces?|pounds?|lbs?|grams?|g|ml|liters?|l|pieces?|slices?)?)", RegexOption.IGNORE_CASE),
            // "Per Serving: 1 cup"
            Regex("per\\s+serving[:\\s]+([\\d½¼¾⅓⅔⅛⅜⅝⅞/\\s]+(?:cups?|tablespoons?|teaspoons?|tbsp|tsp|oz|ounces?|pounds?|lbs?|grams?|g|ml|liters?|l|pieces?|slices?)?)", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(line)
            if (match != null) {
                val value = match.groupValues[1].trim()
                // Clean up: remove trailing multipliers like "1x 2x 3x" and extra whitespace
                var cleaned = value.replace(Regex("\\s*\\d+x.*$", RegexOption.IGNORE_CASE), "").trim()
                // Normalize OCR fractions: "1 /2" -> "1/2", then convert to Unicode
                cleaned = cleaned
                    .replace(Regex("\\s*/\\s*"), "/") // Remove spaces around /
                    .replace("1/2", "½")
                    .replace("1/4", "¼")
                    .replace("3/4", "¾")
                    .replace("1/3", "⅓")
                    .replace("2/3", "⅔")
                if (cleaned.isNotBlank()) {
                    return cleaned
                }
            }
        }

        return null
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
     * Extract tips/notes from the source (website/PDF)
     * This captures helpful tips, variations, substitutions from the original source
     */
    private fun extractTips(sections: Map<String, Int>, lines: List<String>): String? {
        val notesIndex = sections["notes"] ?: return null
        val endIndex = findNextSection(notesIndex, sections)

        // Filter noise and join continuation lines for coherent paragraphs
        val filteredLines = lines.subList(notesIndex + 1, endIndex.coerceAtMost(lines.size))
            .filter { it.isNotBlank() }
            .filter { !isMarketingNoise(it) }
            .filter { !isPdfPageNoise(it) }

        // Join lines into coherent paragraphs
        val tips = joinParagraphLines(filteredLines)

        return tips.ifBlank { null }
    }

    /**
     * Check if a line is marketing/CTA noise (NOT useful tips)
     * This is a stricter filter than isWebsiteNoise - only removes clearly promotional content
     */
    private fun isMarketingNoise(line: String): Boolean {
        val lower = line.lowercase()
        val marketingPatterns = listOf(
            // CTAs
            Regex("\\b(save|shop|get|view|see|click|subscribe|sign\\s*up|log\\s*in|download)\\b.*\\b(recipes?|ingredients?|meals?|plans?|lists?)"),
            // Rating prompts
            Regex("\\b(rating|comment|review|feedback)\\b.*\\b(let|know|help|business|thrive)"),
            Regex("\\bleave\\s+a\\s+(rating|comment|review)"),
            // Marketing
            Regex("\\b(free|newsletter|social|follow|share|pin|tweet)\\b"),
            // Footer/legal
            Regex("\\b(privacy|policy|terms|conditions|copyright)\\b"),
            // Nutrition info (Calories, Protein, Carbs, etc.)
            Regex("\\b(calories|kcal|protein|carbs|carbohydrates|fat|fiber|sodium|cholesterol|saturated)\\s*:"),
            Regex("^serving:\\s*\\d"),
            // "Did You Make This Recipe?" prompts
            Regex("did\\s+you\\s+make\\s+this"),
            Regex("post\\s+a\\s+pic"),
            Regex("mention\\s+@|tag\\s+#"),
            // Section headers that aren't useful tips
            Regex("^(nutrition|storage|faq)\\s*:?\\s*$")
        )
        return marketingPatterns.any { it.containsMatchIn(lower) }
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
     * Check if a line is website noise (navigation, CTA, footer text, notes/tips)
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
            Regex("\\bleave\\s+a\\s+(rating|comment|review)"),
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
            Regex("^(home|about|contact|blog|search)$", RegexOption.IGNORE_CASE),
            // Notes, tips, and suggestions (not part of actual recipe steps)
            Regex("^(notes?|tips?|pro\\s*tips?|suggestions?|recipe\\s*notes?|recipe\\s*tips?|cooking\\s*tips?)\\s*:?\\s*$", RegexOption.IGNORE_CASE),
            Regex("^(notes?|tips?|pro\\s*tips?|suggestions?|recipe\\s*notes?):", RegexOption.IGNORE_CASE),
            // Helpful hints / editorial content
            Regex("^(helpful\\s*hints?|did\\s*you\\s*know|fun\\s*fact):", RegexOption.IGNORE_CASE),
            // Variation suggestions
            Regex("^(variations?|substitutions?|alternatives?)\\s*:?\\s*$", RegexOption.IGNORE_CASE),
            // "You can also..." type suggestions
            Regex("^you\\s+(can|could|may|might)\\s+(also|substitute|swap|replace|use)", RegexOption.IGNORE_CASE),
            // Author/source attribution
            Regex("^(recipe\\s+by|adapted\\s+from|source|originally\\s+from|via)\\s*:", RegexOption.IGNORE_CASE)
        )

        return noisyPatterns.any { it.containsMatchIn(lower) }
    }

    /**
     * Check if a line is PDF extraction noise (URLs, page headers, page numbers)
     */
    private fun isPdfPageNoise(line: String): Boolean {
        val lower = line.lowercase()
        return listOf(
            // URLs
            Regex("^https?://"),
            Regex("^www\\."),
            // Page numbers like "10/25" at end of URL lines
            Regex("\\d+/\\d+$"),
            // Date/time page headers like "11/18/25, 12:34 PM"
            Regex("^\\d{1,2}/\\d{1,2}/\\d{2,4},?\\s+\\d{1,2}:\\d{2}"),
            // Repeated page titles (contain " - " and end with "...")
            Regex("\\s+-\\s+.*\\.\\.\\.$")
        ).any { it.containsMatchIn(lower) }
    }

    /**
     * Join continuation lines for numbered instructions.
     * Lines starting with a digit followed by delimiter (., ), :, or space) are new steps;
     * other lines continue the previous step.
     */
    private fun joinInstructionLines(lines: List<String>): List<String> {
        if (lines.isEmpty()) return emptyList()

        val result = mutableListOf<StringBuilder>()

        for (line in lines) {
            // Check if this line starts a new instruction
            // Matches: "1.", "1)", "1:", "1 ", "12.", etc.
            val startsNewInstruction = Regex("^\\d+[.):\\s]").containsMatchIn(line)

            if (startsNewInstruction || result.isEmpty()) {
                // Start new instruction
                result.add(StringBuilder(line))
            } else {
                // Continue previous instruction - append with space
                result.lastOrNull()?.append(" ")?.append(line)
            }
        }

        return result.map { it.toString() }
    }

    /**
     * Join continuation lines for prose content (tips, notes).
     * Lines that don't end with sentence-ending punctuation are continued.
     */
    private fun joinParagraphLines(lines: List<String>): String {
        if (lines.isEmpty()) return ""

        val result = StringBuilder()

        for (line in lines) {
            if (result.isEmpty()) {
                result.append(line)
            } else {
                // Check if previous content ends with sentence-ending punctuation
                val lastChar = result.lastOrNull()
                if (lastChar in listOf('.', '!', '?', ':')) {
                    // Start new sentence - add space
                    result.append(" ").append(line)
                } else {
                    // Continue previous sentence - just add space
                    result.append(" ").append(line)
                }
            }
        }

        return result.toString()
    }

    /**
     * Check if a line looks like an ingredient
     */
    private fun looksLikeIngredient(line: String): Boolean {
        // Pre-clean OCR noise for pattern matching (U checkbox, 0z -> oz)
        val cleaned = line
            .replace(Regex("^[UO☐□]\\s+"), "")
            .replace(Regex("\\b0z\\b", RegexOption.IGNORE_CASE), "oz")
        val lower = cleaned.lowercase()

        // Too short to be an ingredient
        if (cleaned.length < 3) return false

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
     * Clean ingredient line - remove leading bullets, step numbering, and OCR noise
     * Note: Only removes "1. " style numbering, NOT bare quantities like "4 chicken"
     */
    private fun cleanIngredient(line: String): String {
        return line
            .replace(Regex("^[•\\-*]\\s*"), "") // Remove bullets
            .replace(Regex("^\\d+\\.\\s+"), "") // Remove step numbering (requires period + space)
            .replace(Regex("^[UO☐□]\\s+"), "") // Remove OCR checkbox noise (U, O, ☐, □ followed by space)
            .replace(Regex("\\b0z\\b"), "oz") // Fix common OCR error: 0z -> oz
            .replace(Regex("\\b0unces?\\b", RegexOption.IGNORE_CASE), "ounces") // Fix 0unces -> ounces
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
            // Starts with number followed by food word (e.g., "4 boneless skinless chicken thighs")
            Regex("^\\d+\\s+(boneless|skinless|chicken|beef|pork|lamb|fish|salmon|shrimp|turkey|duck|sausage|bacon|ham)", RegexOption.IGNORE_CASE),
            // Starts with number followed by parenthetical (e.g., "4 (12 oz total) sweet Italian")
            Regex("^\\d+\\s*\\([^)]+\\)", RegexOption.IGNORE_CASE),
            // Starts with number followed by color/descriptor + food (e.g., "1 red bell pepper")
            Regex("^\\d+\\s+(red|green|yellow|orange|white|black|sweet|hot|large|medium|small)\\s+\\w+\\s+(pepper|onion|tomato|potato|carrot|celery|garlic)", RegexOption.IGNORE_CASE),
            // Starts with measurement unit (number on previous line in PDF)
            Regex("^(cups?|tablespoons?|teaspoons?|tbsp|tsp|oz|ounces?|pounds?|lbs?)\\s+\\w", RegexOption.IGNORE_CASE),
            // Contains parenthetical description like "(from 1 small onion)"
            Regex("\\(from\\s+\\d+\\s+\\w+", RegexOption.IGNORE_CASE),
            // Common ingredient list patterns - comma followed by prep word
            Regex(",\\s*(sliced|diced|chopped|minced|quartered|halved|cut|peeled|seeded|cored)\\b", RegexOption.IGNORE_CASE),
            // Ingredient with thickness: "sliced ¼ inch thick"
            Regex("(sliced|cut)\\s+[¼½¾⅓⅔⅛⅜⅝⅞\\d]+\\s*inch", RegexOption.IGNORE_CASE),
            // Contains "brine" which appears in ingredient lists
            Regex("\\bbrine\\b", RegexOption.IGNORE_CASE)
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
