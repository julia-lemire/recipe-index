package com.recipeindex.app.utils.filtersort.mealplan

import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.utils.filtersort.core.GroupBy
import java.text.SimpleDateFormat
import java.util.*

/**
 * Group meal plans by month (based on start date)
 */
class MonthGrouping : GroupBy<MealPlan, MonthKey> {
    override val id: String = "month"
    override val label: String = "Month"

    data class MonthKey(val year: Int, val month: Int) : Comparable<MonthKey> {
        override fun compareTo(other: MonthKey): Int {
            return if (year != other.year) {
                year.compareTo(other.year)
            } else {
                month.compareTo(other.month)
            }
        }
    }

    override fun extractKey(item: MealPlan): MonthKey {
        val date = item.startDate ?: item.createdAt
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
        }
        return MonthKey(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH)
        )
    }

    override fun formatKeyLabel(key: MonthKey): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, key.year)
            set(Calendar.MONTH, key.month)
        }
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    override fun compareKeys(key1: MonthKey, key2: MonthKey): Int {
        return key2.compareTo(key1)  // Reverse order (most recent first)
    }
}

/**
 * Group meal plans by tag (plans with multiple tags appear in multiple groups)
 * Note: This returns the first tag as the key for simplicity
 */
class TagGrouping : GroupBy<MealPlan, String> {
    override val id: String = "tag"
    override val label: String = "Tag"

    override fun extractKey(item: MealPlan): String {
        return item.tags.firstOrNull() ?: "Untagged"
    }

    override fun formatKeyLabel(key: String): String {
        return key.replaceFirstChar { it.uppercase() }
    }
}

/**
 * Group meal plans by recipe count category
 */
class RecipeCountGrouping : GroupBy<MealPlan, RecipeCountCategory> {
    override val id: String = "recipe_count"
    override val label: String = "Recipe Count"

    enum class RecipeCountCategory {
        EMPTY,      // 0 recipes
        SINGLE,     // 1 recipe
        SMALL,      // 2-3 recipes
        MEDIUM,     // 4-6 recipes
        LARGE       // 7+ recipes
    }

    override fun extractKey(item: MealPlan): RecipeCountCategory {
        return when (item.recipeIds.size) {
            0 -> RecipeCountCategory.EMPTY
            1 -> RecipeCountCategory.SINGLE
            in 2..3 -> RecipeCountCategory.SMALL
            in 4..6 -> RecipeCountCategory.MEDIUM
            else -> RecipeCountCategory.LARGE
        }
    }

    override fun formatKeyLabel(key: RecipeCountCategory): String {
        return when (key) {
            RecipeCountCategory.EMPTY -> "No Recipes"
            RecipeCountCategory.SINGLE -> "1 Recipe"
            RecipeCountCategory.SMALL -> "2-3 Recipes"
            RecipeCountCategory.MEDIUM -> "4-6 Recipes"
            RecipeCountCategory.LARGE -> "7+ Recipes"
        }
    }

    override fun compareKeys(key1: RecipeCountCategory, key2: RecipeCountCategory): Int {
        val order = listOf(
            RecipeCountCategory.LARGE,
            RecipeCountCategory.MEDIUM,
            RecipeCountCategory.SMALL,
            RecipeCountCategory.SINGLE,
            RecipeCountCategory.EMPTY
        )
        return order.indexOf(key1).compareTo(order.indexOf(key2))
    }
}

/**
 * Group meal plans by whether they have dates or not
 */
class PlanTypeGrouping : GroupBy<MealPlan, Boolean> {
    override val id: String = "plan_type"
    override val label: String = "Plan Type"

    override fun extractKey(item: MealPlan): Boolean {
        return item.startDate != null || item.endDate != null
    }

    override fun formatKeyLabel(key: Boolean): String {
        return if (key) "Date-based Plans" else "Event Plans"
    }

    override fun compareKeys(key1: Boolean, key2: Boolean): Int {
        // Date-based plans first
        return when {
            key1 == key2 -> 0
            key1 -> -1  // true (date-based) comes first
            else -> 1
        }
    }
}
