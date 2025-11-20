package com.recipeindex.app.utils.filtersort.recipe

import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.utils.filtersort.core.BaseSort
import com.recipeindex.app.utils.filtersort.core.Sort
import com.recipeindex.app.utils.filtersort.core.SortDirection

/**
 * Sort recipes by title (alphabetically)
 */
class TitleSort(
    direction: SortDirection = SortDirection.ASC
) : BaseSort<Recipe>("title", "Title", direction) {

    override fun getBaseComparator(): Comparator<Recipe> {
        return compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
    }

    override fun reversed(): Sort<Recipe> {
        return TitleSort(direction.reversed())
    }
}

/**
 * Sort recipes by creation date (newest first by default)
 */
class DateCreatedSort(
    direction: SortDirection = SortDirection.DESC
) : BaseSort<Recipe>("date_created", "Date Added", direction) {

    override fun getBaseComparator(): Comparator<Recipe> {
        return compareBy { it.createdAt }
    }

    override fun reversed(): Sort<Recipe> {
        return DateCreatedSort(direction.reversed())
    }
}

/**
 * Sort recipes by cook time (shortest first by default)
 */
class CookTimeSort(
    direction: SortDirection = SortDirection.ASC
) : BaseSort<Recipe>("cook_time", "Cook Time", direction) {

    override fun getBaseComparator(): Comparator<Recipe> {
        return compareBy {
            val total = (it.prepTimeMinutes ?: 0) + (it.cookTimeMinutes ?: 0)
            if (total == 0) Int.MAX_VALUE else total  // Recipes with no time go last
        }
    }

    override fun reversed(): Sort<Recipe> {
        return CookTimeSort(direction.reversed())
    }
}

/**
 * Sort recipes by servings (fewest first by default)
 */
class ServingsSort(
    direction: SortDirection = SortDirection.ASC
) : BaseSort<Recipe>("servings", "Servings", direction) {

    override fun getBaseComparator(): Comparator<Recipe> {
        return compareBy { it.servings }
    }

    override fun reversed(): Sort<Recipe> {
        return ServingsSort(direction.reversed())
    }
}

/**
 * Sort recipes by favorite status (favorites first by default)
 */
class FavoriteSort(
    direction: SortDirection = SortDirection.DESC
) : BaseSort<Recipe>("favorite", "Favorites", direction) {

    override fun getBaseComparator(): Comparator<Recipe> {
        return compareBy { if (it.isFavorite) 0 else 1 }
    }

    override fun reversed(): Sort<Recipe> {
        return FavoriteSort(direction.reversed())
    }
}
