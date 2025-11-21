package com.recipeindex.app.utils.filtersort.recipe

import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeSource
import com.recipeindex.app.utils.filtersort.core.Filter

/**
 * Filter recipes by favorite status
 */
class FavoriteFilter(
    private val favoritesOnly: Boolean = true
) : Filter<Recipe> {
    override val id: String = "favorite_$favoritesOnly"
    override val label: String = if (favoritesOnly) "Favorites" else "Not Favorite"

    override fun matches(item: Recipe): Boolean {
        return item.isFavorite == favoritesOnly
    }
}

/**
 * Filter recipes by tag
 */
class TagFilter(
    private val tag: String
) : Filter<Recipe> {
    override val id: String = "tag_$tag"
    override val label: String = tag.replaceFirstChar { it.uppercase() }

    override fun matches(item: Recipe): Boolean {
        return item.tags.any { it.equals(tag, ignoreCase = true) }
    }
}

/**
 * Filter recipes by source type
 */
class SourceFilter(
    private val source: RecipeSource
) : Filter<Recipe> {
    override val id: String = "source_${source.name}"
    override val label: String = when (source) {
        RecipeSource.URL -> "From URL"
        RecipeSource.PDF -> "From PDF"
        RecipeSource.PHOTO -> "From Photo"
        RecipeSource.MANUAL -> "Manual Entry"
    }

    override fun matches(item: Recipe): Boolean {
        return item.source == source
    }
}

/**
 * Filter recipes by cook time range (in minutes)
 */
class CookTimeFilter(
    private val maxMinutes: Int
) : Filter<Recipe> {
    override val id: String = "cooktime_$maxMinutes"
    override val label: String = when {
        maxMinutes <= 30 -> "Quick (≤30 min)"
        maxMinutes <= 60 -> "Medium (≤1 hour)"
        else -> "Long (>1 hour)"
    }

    override fun matches(item: Recipe): Boolean {
        val totalTime = (item.prepTimeMinutes ?: 0) + (item.cookTimeMinutes ?: 0)
        return totalTime > 0 && totalTime <= maxMinutes
    }
}

/**
 * Filter recipes by servings range
 */
class ServingsFilter(
    private val minServings: Int,
    private val maxServings: Int
) : Filter<Recipe> {
    override val id: String = "servings_${minServings}_${maxServings}"
    override val label: String = "$minServings-$maxServings servings"

    override fun matches(item: Recipe): Boolean {
        return item.servings in minServings..maxServings
    }
}

/**
 * Filter recipes that have photos
 */
class HasPhotoFilter : Filter<Recipe> {
    override val id: String = "has_photo"
    override val label: String = "With Photos"

    override fun matches(item: Recipe): Boolean {
        return !item.photoPath.isNullOrBlank()
    }
}

/**
 * Filter recipes that have notes
 */
class HasNotesFilter : Filter<Recipe> {
    override val id: String = "has_notes"
    override val label: String = "With Notes"

    override fun matches(item: Recipe): Boolean {
        return !item.notes.isNullOrBlank()
    }
}

/**
 * Filter recipes by cuisine
 */
class CuisineFilter(
    private val cuisine: String
) : Filter<Recipe> {
    override val id: String = "cuisine_$cuisine"
    override val label: String = cuisine.replaceFirstChar { it.uppercase() }

    override fun matches(item: Recipe): Boolean {
        return item.cuisine?.equals(cuisine, ignoreCase = true) == true
    }
}
