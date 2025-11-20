package com.recipeindex.app.utils.filtersort.recipe

import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeSource
import com.recipeindex.app.utils.filtersort.core.GroupBy

/**
 * Group recipes by source type
 */
class SourceGrouping : GroupBy<Recipe, RecipeSource> {
    override val id: String = "source"
    override val label: String = "Source"

    override fun extractKey(item: Recipe): RecipeSource {
        return item.source
    }

    override fun formatKeyLabel(key: RecipeSource): String {
        return when (key) {
            RecipeSource.URL -> "From URL"
            RecipeSource.PDF -> "From PDF"
            RecipeSource.PHOTO -> "From Photo"
            RecipeSource.MANUAL -> "Manual Entry"
        }
    }
}

/**
 * Group recipes by favorite status
 */
class FavoriteGrouping : GroupBy<Recipe, Boolean> {
    override val id: String = "favorite"
    override val label: String = "Favorites"

    override fun extractKey(item: Recipe): Boolean {
        return item.isFavorite
    }

    override fun formatKeyLabel(key: Boolean): String {
        return if (key) "Favorites" else "Other Recipes"
    }

    override fun compareKeys(key1: Boolean, key2: Boolean): Int {
        // Favorites first
        return when {
            key1 == key2 -> 0
            key1 -> -1  // true (favorites) comes first
            else -> 1
        }
    }
}

/**
 * Group recipes by tag (recipes with multiple tags appear in multiple groups)
 * Note: This returns the first tag as the key for simplicity
 */
class TagGrouping : GroupBy<Recipe, String> {
    override val id: String = "tag"
    override val label: String = "Tag"

    override fun extractKey(item: Recipe): String {
        return item.tags.firstOrNull() ?: "Untagged"
    }

    override fun formatKeyLabel(key: String): String {
        return key.replaceFirstChar { it.uppercase() }
    }
}

/**
 * Cook time categories for grouping
 */
enum class CookTimeCategory {
    QUICK,      // ≤30 min
    MEDIUM,     // 31-60 min
    LONG,       // >60 min
    UNKNOWN     // No time info
}

/**
 * Group recipes by cook time category
 */
class CookTimeGrouping : GroupBy<Recipe, CookTimeCategory> {
    override val id: String = "cook_time"
    override val label: String = "Cook Time"

    override fun extractKey(item: Recipe): CookTimeCategory {
        val totalTime = (item.prepTimeMinutes ?: 0) + (item.cookTimeMinutes ?: 0)
        return when {
            totalTime == 0 -> CookTimeCategory.UNKNOWN
            totalTime <= 30 -> CookTimeCategory.QUICK
            totalTime <= 60 -> CookTimeCategory.MEDIUM
            else -> CookTimeCategory.LONG
        }
    }

    override fun formatKeyLabel(key: CookTimeCategory): String {
        return when (key) {
            CookTimeCategory.QUICK -> "Quick (≤30 min)"
            CookTimeCategory.MEDIUM -> "Medium (31-60 min)"
            CookTimeCategory.LONG -> "Long (>1 hour)"
            CookTimeCategory.UNKNOWN -> "Time Unknown"
        }
    }

    override fun compareKeys(key1: CookTimeCategory, key2: CookTimeCategory): Int {
        val order = listOf(
            CookTimeCategory.QUICK,
            CookTimeCategory.MEDIUM,
            CookTimeCategory.LONG,
            CookTimeCategory.UNKNOWN
        )
        return order.indexOf(key1).compareTo(order.indexOf(key2))
    }
}

/**
 * Servings range categories for grouping
 */
enum class ServingsRange {
    SINGLE,     // 1-2
    SMALL,      // 3-4
    MEDIUM,     // 5-6
    LARGE       // 7+
}

/**
 * Group recipes by servings range
 */
class ServingsGrouping : GroupBy<Recipe, ServingsRange> {
    override val id: String = "servings"
    override val label: String = "Servings"

    override fun extractKey(item: Recipe): ServingsRange {
        return when (item.servings) {
            in 1..2 -> ServingsRange.SINGLE
            in 3..4 -> ServingsRange.SMALL
            in 5..6 -> ServingsRange.MEDIUM
            else -> ServingsRange.LARGE
        }
    }

    override fun formatKeyLabel(key: ServingsRange): String {
        return when (key) {
            ServingsRange.SINGLE -> "1-2 servings"
            ServingsRange.SMALL -> "3-4 servings"
            ServingsRange.MEDIUM -> "5-6 servings"
            ServingsRange.LARGE -> "7+ servings"
        }
    }
}
