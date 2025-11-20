package com.recipeindex.app.utils

import com.recipeindex.app.data.entities.GroceryItem
import com.recipeindex.app.data.entities.GroceryList
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeSource

/**
 * Share package wrapper for Recipe Index app-to-app sharing
 *
 * Supports sharing recipes, meal plans, and grocery lists with photos and metadata
 */
data class SharePackage(
    val version: String = "1.0",
    val type: ShareType,
    val recipe: ShareRecipe? = null,
    val mealPlan: ShareMealPlan? = null,
    val groceryList: ShareGroceryList? = null,
    val recipes: List<ShareRecipe> = emptyList(), // For meal plans
    val photos: Map<String, String> = emptyMap() // recipeId -> base64 encoded image
)

enum class ShareType {
    RECIPE,
    MEAL_PLAN,
    GROCERY_LIST
}

/**
 * Serializable recipe for sharing (excludes internal IDs and timestamps)
 */
data class ShareRecipe(
    val title: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val servings: Int,
    val prepTimeMinutes: Int?,
    val cookTimeMinutes: Int?,
    val tags: List<String>,
    val source: String, // RecipeSource as string
    val sourceUrl: String?,
    val notes: String?
)

/**
 * Serializable meal plan for sharing
 */
data class ShareMealPlan(
    val name: String,
    val startDate: Long?,
    val endDate: Long?,
    val tags: List<String>,
    val notes: String?
)

/**
 * Serializable grocery list for sharing
 */
data class ShareGroceryList(
    val name: String,
    val items: List<ShareGroceryItem>
)

data class ShareGroceryItem(
    val name: String,
    val quantity: Double?,
    val unit: String?,
    val notes: String?
)

/**
 * Result of importing a shared package
 */
sealed class ImportResult {
    data class Success(
        val recipeIds: List<Long> = emptyList(),
        val mealPlanId: Long? = null,
        val groceryListId: Long? = null,
        val message: String
    ) : ImportResult()

    data class DuplicateDetected(
        val existingRecipe: Recipe,
        val newRecipe: ShareRecipe,
        val photoBase64: String?
    ) : ImportResult()

    data class Error(val message: String) : ImportResult()
}

/**
 * Duplicate resolution action
 */
enum class DuplicateAction {
    REPLACE,    // Replace existing recipe
    KEEP_BOTH,  // Keep both, increment title
    SKIP        // Skip import
}

/**
 * Convert Recipe entity to ShareRecipe
 */
fun Recipe.toShareRecipe(): ShareRecipe {
    return ShareRecipe(
        title = title,
        ingredients = ingredients,
        instructions = instructions,
        servings = servings,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        tags = tags,
        source = source.name,
        sourceUrl = sourceUrl,
        notes = notes
    )
}

/**
 * Convert ShareRecipe to Recipe entity
 */
fun ShareRecipe.toRecipe(): Recipe {
    return Recipe(
        title = title,
        ingredients = ingredients,
        instructions = instructions,
        servings = servings,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        tags = tags,
        source = RecipeSource.valueOf(source),
        sourceUrl = sourceUrl,
        notes = notes
    )
}

/**
 * Convert MealPlan entity to ShareMealPlan
 */
fun MealPlan.toShareMealPlan(): ShareMealPlan {
    return ShareMealPlan(
        name = name,
        startDate = startDate,
        endDate = endDate,
        tags = tags,
        notes = notes
    )
}

/**
 * Convert GroceryList and items to ShareGroceryList
 */
fun GroceryList.toShareGroceryList(items: List<GroceryItem>): ShareGroceryList {
    return ShareGroceryList(
        name = name,
        items = items.map { it.toShareGroceryItem() }
    )
}

/**
 * Convert GroceryItem to ShareGroceryItem
 */
fun GroceryItem.toShareGroceryItem(): ShareGroceryItem {
    return ShareGroceryItem(
        name = name,
        quantity = quantity,
        unit = unit,
        notes = notes
    )
}
