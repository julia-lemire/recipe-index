package com.recipeindex.app.utils.filtersort.mealplan

import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.utils.filtersort.core.GroupBy
import java.text.SimpleDateFormat
import java.util.*

/**
 * Month key for grouping meal plans by month
 */
data class MealPlanMonthKey(val year: Int, val month: Int) : Comparable<MealPlanMonthKey> {
    override fun compareTo(other: MealPlanMonthKey): Int {
        return if (year != other.year) {
            year.compareTo(other.year)
        } else {
            month.compareTo(other.month)
        }
    }
}

/**
 * Recipe count categories for grouping meal plans
 */
enum class MealPlanRecipeCountCategory {
    EMPTY,      // 0 recipes
    SINGLE,     // 1 recipe
    SMALL,      // 2-3 recipes
    MEDIUM,     // 4-6 recipes
    LARGE       // 7+ recipes
}

/**
 * Group meal plans by month (based on start date)
 */
class MonthGrouping : GroupBy<MealPlan, MealPlanMonthKey> {
    override val id: String = "month"
    override val label: String = "Month"

    override fun extractKey(item: MealPlan): MealPlanMonthKey {
        val date = item.startDate ?: item.createdAt
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
        }
        return MealPlanMonthKey(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH)
        )
    }

    override fun formatKeyLabel(key: MealPlanMonthKey): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, key.year)
            set(Calendar.MONTH, key.month)
        }
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    override fun compareKeys(key1: MealPlanMonthKey, key2: MealPlanMonthKey): Int {
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
class RecipeCountGrouping : GroupBy<MealPlan, MealPlanRecipeCountCategory> {
    override val id: String = "recipe_count"
    override val label: String = "Recipe Count"

    override fun extractKey(item: MealPlan): MealPlanRecipeCountCategory {
        return when (item.recipeIds.size) {
            0 -> MealPlanRecipeCountCategory.EMPTY
            1 -> MealPlanRecipeCountCategory.SINGLE
            in 2..3 -> MealPlanRecipeCountCategory.SMALL
            in 4..6 -> MealPlanRecipeCountCategory.MEDIUM
            else -> MealPlanRecipeCountCategory.LARGE
        }
    }

    override fun formatKeyLabel(key: MealPlanRecipeCountCategory): String {
        return when (key) {
            MealPlanRecipeCountCategory.EMPTY -> "No Recipes"
            MealPlanRecipeCountCategory.SINGLE -> "1 Recipe"
            MealPlanRecipeCountCategory.SMALL -> "2-3 Recipes"
            MealPlanRecipeCountCategory.MEDIUM -> "4-6 Recipes"
            MealPlanRecipeCountCategory.LARGE -> "7+ Recipes"
        }
    }

    override fun compareKeys(key1: MealPlanRecipeCountCategory, key2: MealPlanRecipeCountCategory): Int {
        val order = listOf(
            MealPlanRecipeCountCategory.LARGE,
            MealPlanRecipeCountCategory.MEDIUM,
            MealPlanRecipeCountCategory.SMALL,
            MealPlanRecipeCountCategory.SINGLE,
            MealPlanRecipeCountCategory.EMPTY
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
