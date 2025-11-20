package com.recipeindex.app.utils.filtersort.mealplan

import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.utils.filtersort.core.BaseSort
import com.recipeindex.app.utils.filtersort.core.Sort
import com.recipeindex.app.utils.filtersort.core.SortDirection

/**
 * Sort meal plans by name (alphabetically)
 */
class NameSort(
    direction: SortDirection = SortDirection.ASC
) : BaseSort<MealPlan>("name", "Name", direction) {

    override fun getBaseComparator(): Comparator<MealPlan> {
        return compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
    }

    override fun reversed(): Sort<MealPlan> {
        return NameSort(direction.reversed())
    }
}

/**
 * Sort meal plans by start date (most recent first by default)
 */
class StartDateSort(
    direction: SortDirection = SortDirection.DESC
) : BaseSort<MealPlan>("start_date", "Start Date", direction) {

    override fun getBaseComparator(): Comparator<MealPlan> {
        return compareBy {
            it.startDate ?: Long.MAX_VALUE  // Plans without dates go last
        }
    }

    override fun reversed(): Sort<MealPlan> {
        return StartDateSort(direction.reversed())
    }
}

/**
 * Sort meal plans by creation date (newest first by default)
 */
class DateCreatedSort(
    direction: SortDirection = SortDirection.DESC
) : BaseSort<MealPlan>("date_created", "Date Created", direction) {

    override fun getBaseComparator(): Comparator<MealPlan> {
        return compareBy { it.createdAt }
    }

    override fun reversed(): Sort<MealPlan> {
        return DateCreatedSort(direction.reversed())
    }
}

/**
 * Sort meal plans by number of recipes (most recipes first by default)
 */
class RecipeCountSort(
    direction: SortDirection = SortDirection.DESC
) : BaseSort<MealPlan>("recipe_count", "Recipe Count", direction) {

    override fun getBaseComparator(): Comparator<MealPlan> {
        return compareBy { it.recipeIds.size }
    }

    override fun reversed(): Sort<MealPlan> {
        return RecipeCountSort(direction.reversed())
    }
}
