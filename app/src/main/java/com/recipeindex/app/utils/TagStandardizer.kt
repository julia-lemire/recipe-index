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
        "mexican food" to "mexican",
        "mexican cuisine" to "mexican",
        "chinese food" to "chinese",
        "chinese cuisine" to "chinese",
        "japanese food" to "japanese",
        "japanese cuisine" to "japanese",
        "thai food" to "thai",
        "thai cuisine" to "thai",
        "indian food" to "indian",
        "indian cuisine" to "indian",
        "mediterranean food" to "mediterranean",
        "mediterranean cuisine" to "mediterranean",

        // Meal types
        "breakfast recipe" to "breakfast",
        "breakfast meal" to "breakfast",
        "lunch recipe" to "lunch",
        "lunch meal" to "lunch",
        "dinner recipe" to "dinner",
        "dinner meal" to "dinner",
        "supper" to "dinner",
        "dessert recipe" to "dessert",
        "snack recipe" to "snack",

        // Cook methods
        "oven baked" to "baked",
        "oven-baked" to "baked",
        "pan fried" to "fried",
        "pan-fried" to "fried",
        "deep fried" to "fried",
        "deep-fried" to "fried",
        "slow cooker" to "slow-cook",
        "crockpot" to "slow-cook",
        "crock pot" to "slow-cook",
        "pressure cooker" to "instant pot",
        "stovetop" to "stove-top",

        // Speed/difficulty
        "quick recipe" to "quick",
        "fast recipe" to "quick",
        "easy recipe" to "easy",
        "simple recipe" to "easy",
        "30 minute" to "quick",
        "30-minute" to "quick",
        "30 min" to "quick",

        // Dietary
        "vegetarian recipe" to "vegetarian",
        "vegan recipe" to "vegan",
        "gluten free" to "gluten-free",
        "dairy free" to "dairy-free",
        "low carb" to "low-carb",
        "keto diet" to "keto",
        "paleo diet" to "paleo",

        // Proteins
        "chicken recipe" to "chicken",
        "beef recipe" to "beef",
        "pork recipe" to "pork",
        "fish recipe" to "fish",
        "seafood recipe" to "seafood",
        "shrimp recipe" to "shrimp",
        "salmon recipe" to "salmon",

        // Common foods
        "pasta recipe" to "pasta",
        "rice recipe" to "rice",
        "potato recipe" to "potato",
        "salad recipe" to "salad",
        "soup recipe" to "soup",
        "sandwich recipe" to "sandwich",
        "pizza recipe" to "pizza"
    )

    // Words to remove from tags (noise)
    private val noiseWords = setOf(
        "recipe", "food", "meal", "dish", "cuisine", "cooking", "cook",
        "homemade", "delicious", "tasty", "yummy", "perfect", "best",
        "traditional", "authentic", "classic", "modern", "new"
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
            .map { removeNoiseWords(it) }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()
    }

    /**
     * Normalize a single tag
     * - Remove extra whitespace
     * - Replace multiple spaces with single space
     * - Remove special characters (except hyphens)
     */
    private fun normalizeTag(tag: String): String {
        return tag
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[^a-z0-9\\s-]"), "")
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

        return true
    }
}
