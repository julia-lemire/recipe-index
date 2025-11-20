package com.recipeindex.app.utils.filtersort.mealplan

import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.utils.filtersort.core.Filter

/**
 * Filter meal plans by date range (plans that overlap with the given range)
 */
class DateRangeFilter(
    private val startDate: Long,
    private val endDate: Long
) : Filter<MealPlan> {
    override val id: String = "date_range_${startDate}_${endDate}"
    override val label: String = formatDateRange(startDate, endDate)

    override fun matches(item: MealPlan): Boolean {
        val planStart = item.startDate ?: return false
        val planEnd = item.endDate ?: planStart

        // Check if date ranges overlap
        return planStart <= endDate && planEnd >= startDate
    }

    private fun formatDateRange(start: Long, end: Long): String {
        val dateFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        return "${dateFormat.format(java.util.Date(start))} - ${dateFormat.format(java.util.Date(end))}"
    }
}

/**
 * Filter meal plans by number of recipes
 */
class RecipeCountFilter(
    private val minRecipes: Int,
    private val maxRecipes: Int? = null
) : Filter<MealPlan> {
    override val id: String = "recipe_count_${minRecipes}_${maxRecipes}"
    override val label: String = when {
        maxRecipes == null -> "$minRecipes+ recipes"
        minRecipes == maxRecipes -> "$minRecipes recipe${if (minRecipes == 1) "" else "s"}"
        else -> "$minRecipes-$maxRecipes recipes"
    }

    override fun matches(item: MealPlan): Boolean {
        val count = item.recipeIds.size
        return count >= minRecipes && (maxRecipes == null || count <= maxRecipes)
    }
}

/**
 * Filter meal plans by tag
 */
class TagFilter(private val tag: String) : Filter<MealPlan> {
    override val id: String = "tag_$tag"
    override val label: String = tag.replaceFirstChar { it.uppercase() }

    override fun matches(item: MealPlan): Boolean {
        return item.tags.any { it.equals(tag, ignoreCase = true) }
    }
}

/**
 * Filter meal plans that contain a specific recipe (by recipe name or ID)
 * This is the KEY FILTER for the user's use case: "find a meal plan with a specific recipe"
 */
class ContainsRecipeFilter(
    private val recipeNameOrId: String,
    private val availableRecipes: List<Recipe>
) : Filter<MealPlan> {
    override val id: String = "contains_recipe_$recipeNameOrId"
    override val label: String = "Contains \"$recipeNameOrId\""

    override fun matches(item: MealPlan): Boolean {
        // Try to match by recipe ID first (if it's a number)
        val recipeIdLong = recipeNameOrId.toLongOrNull()
        if (recipeIdLong != null && item.recipeIds.contains(recipeIdLong)) {
            return true
        }

        // Match by recipe name (partial match, case-insensitive)
        val matchingRecipes = availableRecipes.filter {
            it.title.contains(recipeNameOrId, ignoreCase = true)
        }

        return matchingRecipes.any { recipe ->
            item.recipeIds.contains(recipe.id)
        }
    }
}

/**
 * Filter meal plans with notes
 */
class HasNotesFilter : Filter<MealPlan> {
    override val id: String = "has_notes"
    override val label: String = "With Notes"

    override fun matches(item: MealPlan): Boolean {
        return !item.notes.isNullOrBlank()
    }
}

/**
 * Filter meal plans with dates (date-based plans vs. standalone event plans)
 */
class HasDatesFilter : Filter<MealPlan> {
    override val id: String = "has_dates"
    override val label: String = "Date-based Plans"

    override fun matches(item: MealPlan): Boolean {
        return item.startDate != null || item.endDate != null
    }
}
