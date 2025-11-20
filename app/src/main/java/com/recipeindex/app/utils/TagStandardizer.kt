package com.recipeindex.app.utils

/**
 * TagStandardizer - Normalize and standardize recipe tags
 *
 * Cleans up messy tag data by:
 * - Converting to lowercase
 * - Trimming whitespace
 * - Removing duplicates
 * - Mapping common variations to standard forms
 * - Removing noise words
 */
object TagStandardizer {

    // Map of variations to standard forms
    private val standardMappings = mapOf(
        // Cuisines
        "italian food" to "italian",
        "italian cuisine" to "italian",
        "italian meals" to "italian",
        "mexican food" to "mexican",
        "mexican cuisine" to "mexican",
        "mexican meals" to "mexican",
        "chinese food" to "chinese",
        "chinese cuisine" to "chinese",
        "chinese meals" to "chinese",
        "japanese food" to "japanese",
        "japanese cuisine" to "japanese",
        "japanese meals" to "japanese",
        "thai food" to "thai",
        "thai cuisine" to "thai",
        "thai meals" to "thai",
        "indian food" to "indian",
        "indian cuisine" to "indian",
        "indian meals" to "indian",
        "mediterranean food" to "mediterranean",
        "mediterranean cuisine" to "mediterranean",
        "mediterranean meals" to "mediterranean",

        // Meal types
        "breakfast recipe" to "breakfast",
        "breakfast meal" to "breakfast",
        "breakfast meals" to "breakfast",
        "lunch recipe" to "lunch",
        "lunch meal" to "lunch",
        "lunch meals" to "lunch",
        "dinner recipe" to "dinner",
        "dinner meal" to "dinner",
        "dinner meals" to "dinner",
        "supper" to "dinner",
        "dessert recipe" to "dessert",
        "dessert recipes" to "dessert",
        "snack recipe" to "snack",
        "snack recipes" to "snack",

        // Cook methods
        "oven baked" to "baked",
        "oven-baked" to "baked",
        "pan fried" to "fried",
        "pan-fried" to "fried",
        "deep fried" to "fried",
        "deep-fried" to "fried",
        "slow cooker" to "slow cooker",
        "slow cooker recipes" to "slow cooker",
        "crockpot" to "slow cooker",
        "crock pot" to "slow cooker",
        "pressure cooker" to "instant pot",
        "instant pot recipes" to "instant pot",
        "air fryer" to "air fryer",
        "air fryer recipes" to "air fryer",
        "stovetop" to "stove-top",
        "freezer meals" to "freezer-friendly",
        "freezer" to "freezer-friendly",

        // Speed/difficulty
        "quick recipe" to "quick",
        "quick recipes" to "quick",
        "fast recipe" to "quick",
        "easy recipe" to "easy",
        "easy recipes" to "easy",
        "simple recipe" to "easy",
        "simple recipes" to "easy",
        "30 minute" to "quick",
        "30-minute" to "quick",
        "30 min" to "quick",

        // Dietary
        "vegetarian recipe" to "vegetarian",
        "vegetarian recipes" to "vegetarian",
        "vegetarian meal" to "vegetarian",
        "vegetarian meals" to "vegetarian",
        "vegan recipe" to "vegan",
        "vegan recipes" to "vegan",
        "vegan meal" to "vegan",
        "vegan meals" to "vegan",
        "gluten free" to "gluten-free",
        "dairy free" to "dairy-free",
        "dairy free recipes" to "dairy-free",
        "egg free recipes" to "egg-free",
        "egg free" to "egg-free",
        "low carb" to "low-carb",
        "keto diet" to "keto",
        "keto recipes" to "keto",
        "paleo diet" to "paleo",
        "plant based" to "plant-based",
        "plant-based meals" to "plant-based",
        "whole30 recipes" to "paleo",
        "whole30" to "paleo",

        // Proteins (including specific cuts -> general)
        "chicken recipe" to "chicken",
        "chicken recipes" to "chicken",
        "chicken meals" to "chicken",
        "chicken breast" to "chicken",
        "chicken breast recipes" to "chicken",
        "chicken thigh" to "chicken",
        "chicken thigh recipes" to "chicken",
        "beef recipe" to "beef",
        "beef recipes" to "beef",
        "beef meals" to "beef",
        "ground beef" to "beef",
        "ground beef recipes" to "beef",
        "pork recipe" to "pork",
        "pork recipes" to "pork",
        "pork meals" to "pork",
        "fish recipe" to "fish",
        "fish recipes" to "fish",
        "fish meals" to "fish",
        "seafood recipe" to "seafood",
        "seafood recipes" to "seafood",
        "seafood meals" to "seafood",
        "shrimp recipe" to "shrimp",
        "shrimp recipes" to "shrimp",
        "salmon recipe" to "salmon",
        "salmon recipes" to "salmon",

        // Common foods
        "pasta recipe" to "pasta",
        "pasta recipes" to "pasta",
        "pasta meals" to "pasta",
        "rice recipe" to "rice",
        "rice recipes" to "rice",
        "rice meals" to "rice",
        "potato recipe" to "potato",
        "potato recipes" to "potato",
        "salad recipe" to "salad",
        "salad recipes" to "salad",
        "soup recipe" to "soup",
        "soup recipes" to "soup",
        "sandwich recipe" to "sandwich",
        "sandwich recipes" to "sandwich",
        "pizza recipe" to "pizza",
        "pizza recipes" to "pizza",

        // Holidays -> special occasion
        "valentines day" to "special occasion",
        "valentines day recipes" to "special occasion",
        "valentine's day" to "special occasion",
        "valentine's day recipes" to "special occasion",
        "christmas" to "special occasion",
        "christmas recipes" to "special occasion",
        "thanksgiving" to "special occasion",
        "thanksgiving recipes" to "special occasion",
        "easter" to "special occasion",
        "easter recipes" to "special occasion",
        "halloween" to "special occasion",
        "halloween recipes" to "special occasion",
        "new year" to "special occasion",
        "new years" to "special occasion",
        "new year's" to "special occasion",
        "mothers day" to "special occasion",
        "mother's day" to "special occasion",
        "fathers day" to "special occasion",
        "father's day" to "special occasion",
        "4th of july" to "special occasion",
        "fourth of july" to "special occasion",
        "independence day" to "special occasion",
        "memorial day" to "special occasion",
        "labor day" to "special occasion",
        "super bowl" to "special occasion",
        "game day" to "special occasion"
    )

    // Words to remove from tags (noise)
    private val noiseWords = setOf(
        "recipe", "recipes", "food", "meal", "meals", "dish", "cuisine", "cooking", "cook",
        "homemade", "delicious", "tasty", "yummy", "perfect", "best",
        "traditional", "authentic", "classic", "modern", "new",
        "ideas", "tips", "guide", "how", "to", "make", "making",
        "dinner", "dinners", "lunch", "lunches", "breakfast", "breakfasts"
    )

    // Tags to completely filter out (even if alone)
    private val junkTags = setOf(
        "recipes", "recipe", "meals", "meal", "food", "dishes", "dish",
        "ideas", "cooking", "cook", "dinner ideas",
        "weight watchers", "ww", "weight watchers ww"
    )

    // Phrases that indicate junk tags
    private val junkPhrases = listOf(
        "how to make",
        "how to cook",
        "best recipes",
        "top recipes",
        "easy recipes",
        "simple recipes",
        "for beginners"
    )

    /**
     * Result of tag standardization, tracking what changed
     */
    data class TagModification(
        val original: String,
        val standardized: String,
        val wasModified: Boolean
    )

    /**
     * Standardize a list of tags
     * @param tags Raw tags from import
     * @return Cleaned, standardized, and deduplicated tags
     */
    fun standardize(tags: List<String>): List<String> {
        return tags
            .asSequence()
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() && it.length >= 2 }
            .map { normalizeTag(it) }
            .map { applyStandardMapping(it) }
            .filter { !isJunkTag(it) } // Check junk BEFORE removing noise words
            .map { removeNoiseWords(it) }
            .filter { it.isNotBlank() }
            .filter { !isJunkTag(it) } // Check again after noise removal
            .distinct()
            .toList()
    }

    /**
     * Standardize tags with tracking of modifications
     * @param tags Raw tags from import
     * @return List of TagModification objects showing what changed
     */
    fun standardizeWithTracking(tags: List<String>): List<TagModification> {
        DebugConfig.debugLog(
            DebugConfig.Category.TAG_STANDARDIZATION,
            "=== Tag Standardization Started ==="
        )
        DebugConfig.debugLog(
            DebugConfig.Category.TAG_STANDARDIZATION,
            "Original tags (${tags.size}): ${tags.joinToString(", ") { "\"$it\"" }}"
        )

        // Track filtered out tags
        val filteredOut = mutableListOf<Pair<String, String>>()

        val modifications = tags
            .asSequence()
            .map { it.trim() }
            .filter {
                val isValid = it.isNotBlank() && it.length >= 2
                if (!isValid) {
                    filteredOut.add(it to "too short or blank")
                }
                isValid
            }
            .map { original ->
                val normalized = normalizeTag(original.lowercase())
                val mapped = applyStandardMapping(normalized)
                val final = removeNoiseWords(mapped)

                // Log the transformation steps for modified tags
                if (original.lowercase() != final) {
                    DebugConfig.debugLog(
                        DebugConfig.Category.TAG_STANDARDIZATION,
                        "  \"$original\" -> normalized: \"$normalized\" -> mapped: \"$mapped\" -> final: \"$final\""
                    )
                }

                TagModification(
                    original = original,
                    standardized = final,
                    wasModified = original.lowercase() != final
                )
            }
            .filter {
                val isValid = it.standardized.isNotBlank()
                if (!isValid) {
                    filteredOut.add(it.original to "noise words removed all content")
                }
                isValid
            }
            .filter {
                val isValid = !isJunkTag(it.standardized)
                if (!isValid) {
                    filteredOut.add(it.original to "junk tag filtered")
                }
                isValid
            }
            .toList()

        // Track duplicates removed
        val beforeDedup = modifications.size
        val result = modifications.distinctBy { it.standardized }
        val duplicatesRemoved = beforeDedup - result.size

        // Log summary
        DebugConfig.debugLog(
            DebugConfig.Category.TAG_STANDARDIZATION,
            "Modified tags: ${result.count { it.wasModified }}, Unchanged: ${result.count { !it.wasModified }}"
        )

        if (filteredOut.isNotEmpty()) {
            DebugConfig.debugLog(
                DebugConfig.Category.TAG_STANDARDIZATION,
                "Filtered out (${filteredOut.size}): ${filteredOut.joinToString(", ") { "\"${it.first}\" (${it.second})" }}"
            )
        }

        if (duplicatesRemoved > 0) {
            DebugConfig.debugLog(
                DebugConfig.Category.TAG_STANDARDIZATION,
                "Duplicates removed: $duplicatesRemoved"
            )
        }

        DebugConfig.debugLog(
            DebugConfig.Category.TAG_STANDARDIZATION,
            "Final tags (${result.size}): ${result.joinToString(", ") { "\"${it.standardized}\"" }}"
        )
        DebugConfig.debugLog(
            DebugConfig.Category.TAG_STANDARDIZATION,
            "=== Tag Standardization Complete ==="
        )

        return result
    }

    /**
     * Normalize a single tag
     * - Remove special characters (except hyphens)
     * - Replace multiple spaces with single space
     * - Remove extra whitespace
     */
    private fun normalizeTag(tag: String): String {
        return tag
            .replace(Regex("[^a-z0-9\\s-]"), "") // Remove special chars first
            .replace(Regex("\\s+"), " ")         // Then collapse spaces
            .trim()
    }

    /**
     * Apply standard mappings to common variations
     */
    private fun applyStandardMapping(tag: String): String {
        return standardMappings[tag] ?: tag
    }

    /**
     * Remove noise words from tag
     * e.g., "chicken recipe" -> "chicken"
     */
    private fun removeNoiseWords(tag: String): String {
        val words = tag.split(" ")
        val filtered = words.filter { it !in noiseWords }

        // If filtering removes all words, keep original
        return if (filtered.isEmpty()) tag else filtered.joinToString(" ")
    }

    /**
     * Check if a tag is junk and should be filtered out
     */
    private fun isJunkTag(tag: String): Boolean {
        val normalized = tag.trim().lowercase()

        // Check if in junk tags set
        if (normalized in junkTags) return true

        // Check if contains junk phrases
        if (junkPhrases.any { normalized.contains(it) }) return true

        // Filter overly long tags (>4 words = likely junk)
        val wordCount = normalized.split(" ").size
        if (wordCount > 4) return true

        return false
    }

    /**
     * Validate tag quality
     * Returns true if tag is worth keeping
     */
    fun isValidTag(tag: String): Boolean {
        val normalized = tag.trim().lowercase()

        // Too short
        if (normalized.length < 2) return false

        // All noise
        if (normalized.split(" ").all { it in noiseWords }) return false

        // Just numbers
        if (normalized.all { it.isDigit() }) return false

        // Check if junk
        if (isJunkTag(normalized)) return false

        return true
    }
}
