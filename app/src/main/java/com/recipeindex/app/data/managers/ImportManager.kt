package com.recipeindex.app.data.managers

import android.content.Context
import com.recipeindex.app.data.dao.RecipeDao
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.DuplicateAction
import com.recipeindex.app.utils.ImportResult
import com.recipeindex.app.utils.ShareHelper
import com.recipeindex.app.utils.SharePackage
import com.recipeindex.app.utils.ShareRecipe
import com.recipeindex.app.utils.ShareType
import com.recipeindex.app.utils.toRecipe
import kotlinx.coroutines.flow.first
import java.io.File

/**
 * ImportManager - Handles importing shared recipes, meal plans, and grocery lists
 *
 * Features:
 * - Parse SharePackage JSON
 * - Duplicate detection for recipes
 * - Photo decoding and saving
 * - Meal plan import with recipe handling
 * - Grocery list import
 */
class ImportManager(
    private val context: Context,
    private val recipeManager: RecipeManager,
    private val mealPlanManager: MealPlanManager,
    private val groceryListManager: GroceryListManager,
    private val recipeDao: RecipeDao
) {

    /**
     * Import from JSON string
     * Returns ImportResult with duplicate detection
     */
    suspend fun importFromJson(json: String): ImportResult {
        return try {
            val result = ShareHelper.importFromJson(json, recipeDao.getAll().first())

            when (result) {
                is ImportResult.DuplicateDetected -> result
                is ImportResult.Success -> {
                    // No duplicate, proceed with full import
                    val sharePackage = com.google.gson.Gson().fromJson(json, SharePackage::class.java)
                    performImport(sharePackage)
                }
                is ImportResult.Error -> result
            }
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "Import failed", e)
            ImportResult.Error("Failed to import: ${e.message}")
        }
    }

    /**
     * Import recipe with duplicate action
     * Used after user selects action from duplicate dialog
     */
    suspend fun importRecipeWithAction(
        shareRecipe: ShareRecipe,
        photoBase64: String?,
        action: DuplicateAction,
        existingRecipe: Recipe?
    ): Result<Long> {
        return try {
            when (action) {
                DuplicateAction.REPLACE -> {
                    // Delete existing recipe and import new one with same ID
                    if (existingRecipe != null) {
                        recipeManager.deleteRecipe(existingRecipe.id)
                    }
                    importSingleRecipe(shareRecipe, photoBase64)
                }
                DuplicateAction.KEEP_BOTH -> {
                    // Import with incremented title
                    val existingTitles = recipeDao.getAll().first().map { it.title }
                    val newTitle = ShareHelper.generateIncrementedTitle(shareRecipe.title, existingTitles)
                    val modifiedRecipe = shareRecipe.copy(title = newTitle)
                    importSingleRecipe(modifiedRecipe, photoBase64)
                }
                DuplicateAction.SKIP -> {
                    // Return existing recipe ID
                    Result.success(existingRecipe?.id ?: 0L)
                }
            }
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "Import recipe with action failed", e)
            Result.failure(e)
        }
    }

    /**
     * Import meal plan from JSON
     * Handles all recipes first, then creates meal plan
     */
    suspend fun importMealPlanFromJson(
        json: String,
        recipeActions: Map<String, DuplicateAction> = emptyMap() // recipeTitle -> action
    ): Result<Long> {
        return try {
            val sharePackage = com.google.gson.Gson().fromJson(json, SharePackage::class.java)

            if (sharePackage.type != ShareType.MEAL_PLAN || sharePackage.mealPlan == null) {
                return Result.failure(Exception("Invalid meal plan data"))
            }

            // Import all recipes first
            val recipeIds = mutableListOf<Long>()
            sharePackage.recipes.forEach { shareRecipe ->
                val photoBase64 = sharePackage.photos[shareRecipe.title]

                // Check if user provided an action for this recipe
                val action = recipeActions[shareRecipe.title] ?: DuplicateAction.SKIP
                val existingRecipes = recipeDao.getAll().first()
                val duplicate = existingRecipes.find {
                    it.title.equals(shareRecipe.title, ignoreCase = true)
                }

                val result = importRecipeWithAction(shareRecipe, photoBase64, action, duplicate)
                result.onSuccess { recipeId ->
                    if (recipeId > 0) {
                        recipeIds.add(recipeId)
                    }
                }
            }

            // Create meal plan with imported recipe IDs
            val shareMealPlan = sharePackage.mealPlan
            val mealPlan = MealPlan(
                name = shareMealPlan.name,
                startDate = shareMealPlan.startDate,
                endDate = shareMealPlan.endDate,
                recipeIds = recipeIds,
                tags = shareMealPlan.tags,
                notes = shareMealPlan.notes
            )

            val createResult = mealPlanManager.createMealPlan(mealPlan)
            createResult.onSuccess { planId ->
                DebugConfig.debugLog(
                    DebugConfig.Category.MANAGER,
                    "Imported meal plan: $planId with ${recipeIds.size} recipes"
                )
            }

            createResult
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "Import meal plan failed", e)
            Result.failure(e)
        }
    }

    /**
     * Import grocery list from JSON
     * Always creates new list (no duplicate detection)
     */
    suspend fun importGroceryListFromJson(json: String): Result<Long> {
        return try {
            val sharePackage = com.google.gson.Gson().fromJson(json, SharePackage::class.java)

            if (sharePackage.type != ShareType.GROCERY_LIST || sharePackage.groceryList == null) {
                return Result.failure(Exception("Invalid grocery list data"))
            }

            val shareList = sharePackage.groceryList

            // Create grocery list
            val createResult = groceryListManager.createList(shareList.name)
            createResult.onSuccess { listId ->
                // Add all items
                shareList.items.forEach { shareItem ->
                    groceryListManager.addManualItem(
                        listId = listId,
                        name = shareItem.name,
                        quantity = shareItem.quantity,
                        unit = shareItem.unit,
                        notes = shareItem.notes
                    )
                }

                DebugConfig.debugLog(
                    DebugConfig.Category.MANAGER,
                    "Imported grocery list: $listId with ${shareList.items.size} items"
                )
            }

            createResult
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "Import grocery list failed", e)
            Result.failure(e)
        }
    }

    /**
     * Perform import based on SharePackage type
     */
    private suspend fun performImport(sharePackage: SharePackage): ImportResult {
        return when (sharePackage.type) {
            ShareType.RECIPE -> {
                val shareRecipe = sharePackage.recipe
                    ?: return ImportResult.Error("Invalid recipe data")
                val photoBase64 = sharePackage.photos[shareRecipe.title]

                val result = importSingleRecipe(shareRecipe, photoBase64)
                result.fold(
                    onSuccess = { recipeId ->
                        ImportResult.Success(
                            recipeIds = listOf(recipeId),
                            message = "Recipe imported successfully"
                        )
                    },
                    onFailure = { e ->
                        ImportResult.Error("Failed to import recipe: ${e.message}")
                    }
                )
            }
            ShareType.MEAL_PLAN -> {
                val result = importMealPlanFromJson(
                    com.google.gson.Gson().toJson(sharePackage),
                    emptyMap() // No duplicate actions, will skip duplicates
                )
                result.fold(
                    onSuccess = { planId ->
                        ImportResult.Success(
                            mealPlanId = planId,
                            message = "Meal plan imported successfully"
                        )
                    },
                    onFailure = { e ->
                        ImportResult.Error("Failed to import meal plan: ${e.message}")
                    }
                )
            }
            ShareType.GROCERY_LIST -> {
                val result = importGroceryListFromJson(com.google.gson.Gson().toJson(sharePackage))
                result.fold(
                    onSuccess = { listId ->
                        ImportResult.Success(
                            groceryListId = listId,
                            message = "Grocery list imported successfully"
                        )
                    },
                    onFailure = { e ->
                        ImportResult.Error("Failed to import grocery list: ${e.message}")
                    }
                )
            }
        }
    }

    /**
     * Import a single recipe with photo
     */
    private suspend fun importSingleRecipe(shareRecipe: ShareRecipe, photoBase64: String?): Result<Long> {
        return try {
            // Convert ShareRecipe to Recipe
            val recipe = shareRecipe.toRecipe()

            // Create recipe
            val createResult = recipeManager.createRecipe(recipe)

            createResult.onSuccess { recipeId ->
                // Save photo if present
                photoBase64?.let { photo ->
                    val photoDir = File(context.filesDir, "recipe_photos")
                    photoDir.mkdirs()
                    val photoFile = File(photoDir, "recipe_${recipeId}.jpg")

                    val decoded = ShareHelper.decodePhoto(photo, photoFile.absolutePath)
                    if (decoded) {
                        // Update recipe with photo path
                        val updatedRecipe = recipe.copy(
                            id = recipeId,
                            photoPath = photoFile.absolutePath
                        )
                        recipeManager.updateRecipe(updatedRecipe)
                        DebugConfig.debugLog(
                            DebugConfig.Category.MANAGER,
                            "Saved photo for imported recipe: $recipeId"
                        )
                    }
                }
            }

            createResult
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "Import single recipe failed", e)
            Result.failure(e)
        }
    }
}
