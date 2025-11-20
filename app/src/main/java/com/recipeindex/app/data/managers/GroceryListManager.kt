package com.recipeindex.app.data.managers

import com.recipeindex.app.data.dao.GroceryItemDao
import com.recipeindex.app.data.dao.GroceryListDao
import com.recipeindex.app.data.dao.RecipeDao
import com.recipeindex.app.data.entities.GroceryItem
import com.recipeindex.app.data.entities.GroceryList
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * GroceryListManager - Business logic for grocery list operations
 *
 * Handles ingredient consolidation with smart quantity summing
 */
class GroceryListManager(
    private val groceryListDao: GroceryListDao,
    private val groceryItemDao: GroceryItemDao,
    private val recipeDao: RecipeDao,
    private val mealPlanDao: com.recipeindex.app.data.dao.MealPlanDao
) {

    /**
     * Get all grocery lists
     */
    fun getAllLists(): Flow<List<GroceryList>> {
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "getAllLists")
        return groceryListDao.getAll()
    }

    /**
     * Get grocery list by ID
     */
    suspend fun getListById(id: Long): GroceryList? {
        DebugConfig.debugLog(DebugConfig.Category.MANAGER, "getListById: $id")
        return groceryListDao.getById(id)
    }

    /**
     * Get grocery list by ID as Flow
     */
    fun getListByIdFlow(id: Long): Flow<GroceryList?> {
        return groceryListDao.getByIdFlow(id)
    }

    /**
     * Get items for a list
     */
    fun getItemsForList(listId: Long): Flow<List<GroceryItem>> {
        return groceryItemDao.getItemsForList(listId)
    }

    /**
     * Create new grocery list
     */
    suspend fun createList(name: String): Result<Long> {
        return try {
            require(name.isNotBlank()) { "List name cannot be blank" }

            val list = GroceryList(
                name = name.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val listId = groceryListDao.insert(list)
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "createList: $listId")
            Result.success(listId)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "createList failed", e)
            Result.failure(e)
        }
    }

    /**
     * Update grocery list
     */
    suspend fun updateList(list: GroceryList): Result<Unit> {
        return try {
            require(list.name.isNotBlank()) { "List name cannot be blank" }

            val updatedList = list.copy(updatedAt = System.currentTimeMillis())
            groceryListDao.update(updatedList)
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "updateList: ${list.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "updateList failed", e)
            Result.failure(e)
        }
    }

    /**
     * Delete grocery list
     */
    suspend fun deleteList(list: GroceryList): Result<Unit> {
        return try {
            groceryListDao.delete(list)
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "deleteList: ${list.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "deleteList failed", e)
            Result.failure(e)
        }
    }

    /**
     * Add recipes to grocery list with consolidation
     * @return Result<Int> containing the count of ingredients added
     */
    suspend fun addRecipesToList(listId: Long, recipeIds: List<Long>): Result<Int> {
        return try {
            DebugConfig.debugLog(
                DebugConfig.Category.MANAGER,
                "addRecipesToList: Starting with ${recipeIds.size} recipe IDs: $recipeIds"
            )

            val ingredients = mutableListOf<ParsedIngredient>()

            // Collect all ingredients from recipes
            for (recipeId in recipeIds) {
                val recipe = recipeDao.getRecipeById(recipeId).first()
                if (recipe == null) {
                    DebugConfig.debugLog(
                        DebugConfig.Category.MANAGER,
                        "addRecipesToList: Recipe $recipeId not found, skipping"
                    )
                    continue
                }

                DebugConfig.debugLog(
                    DebugConfig.Category.MANAGER,
                    "addRecipesToList: Processing recipe ${recipe.title} with ${recipe.ingredients.size} ingredients"
                )

                recipe.ingredients.forEach { ingredientStr ->
                    val parsed = parseIngredient(ingredientStr, recipeId)
                    ingredients.add(parsed)
                }
            }

            DebugConfig.debugLog(
                DebugConfig.Category.MANAGER,
                "addRecipesToList: Collected ${ingredients.size} total ingredients"
            )

            // Consolidate ingredients
            val consolidated = consolidateIngredients(ingredients)

            // Get existing items to check for duplicates
            val existingItems = groceryItemDao.getItemsForList(listId).first()

            // Add or update items
            for (item in consolidated) {
                val existing = existingItems.find {
                    it.name.equals(item.name, ignoreCase = true) &&
                    it.unit?.equals(item.unit, ignoreCase = true) ?: (item.unit == null)
                }

                if (existing != null) {
                    // Update existing item: sum quantities and merge source recipes
                    val newQuantity = (existing.quantity ?: 0.0) + (item.quantity ?: 0.0)
                    val mergedSources = (existing.sourceRecipeIds + item.sourceRecipeIds).distinct()

                    groceryItemDao.update(
                        existing.copy(
                            quantity = if (newQuantity > 0) newQuantity else null,
                            sourceRecipeIds = mergedSources,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Insert new item
                    groceryItemDao.insert(item.copy(listId = listId))
                }
            }

            DebugConfig.debugLog(
                DebugConfig.Category.MANAGER,
                "addRecipesToList: ${recipeIds.size} recipes, ${consolidated.size} items"
            )
            Result.success(consolidated.size)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "addRecipesToList failed", e)
            Result.failure(e)
        }
    }

    /**
     * Add manual item to list
     */
    suspend fun addManualItem(listId: Long, itemName: String): Result<Long> {
        return try {
            require(itemName.isNotBlank()) { "Item name cannot be blank" }

            val parsed = parseIngredient(itemName, null)
            val item = GroceryItem(
                listId = listId,
                name = parsed.name,
                quantity = parsed.quantity,
                unit = parsed.unit,
                isChecked = false,
                sourceRecipeIds = emptyList(),
                notes = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val itemId = groceryItemDao.insert(item)
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "addManualItem: $itemId")
            Result.success(itemId)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "addManualItem failed", e)
            Result.failure(e)
        }
    }

    /**
     * Toggle item checked status
     */
    suspend fun toggleItemChecked(item: GroceryItem): Result<Unit> {
        return try {
            val updated = item.copy(
                isChecked = !item.isChecked,
                updatedAt = System.currentTimeMillis()
            )
            groceryItemDao.update(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "toggleItemChecked failed", e)
            Result.failure(e)
        }
    }

    /**
     * Update grocery item
     */
    suspend fun updateItem(item: GroceryItem): Result<Unit> {
        return try {
            val updated = item.copy(updatedAt = System.currentTimeMillis())
            groceryItemDao.update(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "updateItem failed", e)
            Result.failure(e)
        }
    }

    /**
     * Delete grocery item
     */
    suspend fun deleteItem(item: GroceryItem): Result<Unit> {
        return try {
            groceryItemDao.delete(item)
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "deleteItem failed", e)
            Result.failure(e)
        }
    }

    /**
     * Clear all checked items from a list
     */
    suspend fun clearCheckedItems(listId: Long): Result<Unit> {
        return try {
            groceryItemDao.deleteCheckedItems(listId)
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "clearCheckedItems for list: $listId")
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "clearCheckedItems failed", e)
            Result.failure(e)
        }
    }

    /**
     * Check all items in a list
     */
    suspend fun checkAllItems(listId: Long): Result<Unit> {
        return try {
            val items = groceryItemDao.getItemsForList(listId).first()
            items.forEach { item ->
                if (!item.isChecked) {
                    groceryItemDao.update(
                        item.copy(
                            isChecked = true,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "checkAllItems for list: $listId")
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "checkAllItems failed", e)
            Result.failure(e)
        }
    }

    /**
     * Uncheck all items in a list
     */
    suspend fun uncheckAllItems(listId: Long): Result<Unit> {
        return try {
            val items = groceryItemDao.getItemsForList(listId).first()
            items.forEach { item ->
                if (item.isChecked) {
                    groceryItemDao.update(
                        item.copy(
                            isChecked = false,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "uncheckAllItems for list: $listId")
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "uncheckAllItems failed", e)
            Result.failure(e)
        }
    }

    /**
     * Get item count for a list
     */
    fun getItemCount(listId: Long): Flow<Int> {
        return groceryItemDao.getItemCount(listId)
    }

    /**
     * Get checked item count for a list
     */
    fun getCheckedCount(listId: Long): Flow<Int> {
        return groceryItemDao.getCheckedCount(listId)
    }

    /**
     * Toggle item checked status by ID
     */
    suspend fun toggleItemCheckedById(itemId: Long, checked: Boolean): Result<Unit> {
        return try {
            groceryItemDao.updateCheckedStatus(itemId, checked)
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "toggleItemCheckedById failed", e)
            Result.failure(e)
        }
    }

    /**
     * Delete item by ID
     */
    suspend fun deleteItemById(itemId: Long): Result<Unit> {
        return try {
            groceryItemDao.deleteById(itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "deleteItemById failed", e)
            Result.failure(e)
        }
    }

    /**
     * Add meal plan to grocery list
     * Fetches all recipes in the meal plan and adds their ingredients
     * @return Result<Int> containing the count of ingredients added
     */
    suspend fun addMealPlanToList(listId: Long, planId: Long): Result<Int> {
        return try {
            // Get the meal plan
            val mealPlan = mealPlanDao.getById(planId)
                ?: return Result.failure(Exception("Meal plan not found"))

            DebugConfig.debugLog(
                DebugConfig.Category.MANAGER,
                "addMealPlanToList: plan ${mealPlan.name} has ${mealPlan.recipeIds.size} recipes"
            )

            // Add all recipes from the meal plan
            val result = addRecipesToList(listId, mealPlan.recipeIds)
            result
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "addMealPlanToList failed", e)
            Result.failure(e)
        }
    }

    /**
     * Parse ingredient string into structured data
     */
    private fun parseIngredient(ingredientStr: String, recipeId: Long?): ParsedIngredient {
        var remaining = ingredientStr.trim()

        // Remove modifiers we want to ignore: diced, chopped, shredded, sliced
        val ignoredModifiers = listOf("diced", "chopped", "shredded", "sliced", "cubed")
        ignoredModifiers.forEach { modifier ->
            remaining = remaining.replace(Regex("\\b$modifier\\b", RegexOption.IGNORE_CASE), "")
        }

        // Check for canned/packaged items pattern: "9 oz can of tomatoes" or "14.5 oz can tomatoes"
        val cannedItemPattern = Regex("^([\\d./]+)\\s+(oz|g|ml|lb)\\s+(can|jar|bottle|pack|package)s?\\s+(?:of\\s+)?(.+)", RegexOption.IGNORE_CASE)
        val cannedMatch = cannedItemPattern.find(remaining)

        if (cannedMatch != null) {
            val sizeQuantity = cannedMatch.groupValues[1]
            val sizeUnit = cannedMatch.groupValues[2]
            val containerType = cannedMatch.groupValues[3].lowercase()
            val itemName = cannedMatch.groupValues[4].trim()

            return ParsedIngredient(
                name = itemName,
                quantity = 1.0,
                unit = containerType,
                recipeId = recipeId,
                notes = "$sizeQuantity $sizeUnit"
            )
        }

        // Extract quantity and unit
        val quantityPattern = Regex("^([\\d./]+)\\s*([a-zA-Z]+)?\\s+(.*)")
        val match = quantityPattern.find(remaining)

        return if (match != null) {
            val quantityStr = match.groupValues[1]
            val unit = match.groupValues[2].takeIf { it.isNotBlank() }
            val name = match.groupValues[3].trim()

            // Parse quantity (handle fractions like 1/2)
            val quantity = if (quantityStr.contains('/')) {
                val parts = quantityStr.split('/')
                if (parts.size == 2) {
                    parts[0].toDoubleOrNull()?.div(parts[1].toDoubleOrNull() ?: 1.0) ?: 0.0
                } else {
                    quantityStr.toDoubleOrNull() ?: 0.0
                }
            } else {
                quantityStr.toDoubleOrNull() ?: 0.0
            }

            ParsedIngredient(
                name = name.trim(),
                quantity = if (quantity > 0) quantity else null,
                unit = unit,
                recipeId = recipeId
            )
        } else {
            // No quantity found, treat whole string as name
            ParsedIngredient(
                name = remaining.trim(),
                quantity = null,
                unit = null,
                recipeId = recipeId
            )
        }
    }

    /**
     * Consolidate ingredients: sum quantities for same item+unit
     */
    private fun consolidateIngredients(ingredients: List<ParsedIngredient>): List<GroceryItem> {
        val grouped = ingredients.groupBy {
            // Group by name and unit (case-insensitive)
            Pair(it.name.lowercase(), it.unit?.lowercase())
        }

        return grouped.map { (key, items) ->
            val (name, unit) = key
            val totalQuantity = items.mapNotNull { it.quantity }.sum()
            val sourceRecipes = items.mapNotNull { it.recipeId }.distinct()

            // Combine notes from all items (e.g., multiple cans with different sizes)
            val combinedNotes = items.mapNotNull { it.notes }.distinct().joinToString(", ")

            GroceryItem(
                listId = 0, // Will be set when inserted
                name = items.first().name, // Use original casing from first item
                quantity = if (totalQuantity > 0) totalQuantity else null,
                unit = items.first().unit, // Use original casing from first item
                isChecked = false,
                sourceRecipeIds = sourceRecipes,
                notes = combinedNotes.ifBlank { null }
            )
        }
    }

    /**
     * Data class for parsed ingredient
     */
    private data class ParsedIngredient(
        val name: String,
        val quantity: Double?,
        val unit: String?,
        val recipeId: Long?,
        val notes: String? = null
    )
}
