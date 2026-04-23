package com.recipeindex.app.data.managers

import com.recipeindex.app.data.UnitSystem
import com.recipeindex.app.data.dao.GroceryItemDao
import com.recipeindex.app.data.dao.GroceryListDao
import com.recipeindex.app.data.dao.RecipeDao
import com.recipeindex.app.data.entities.GroceryItem
import com.recipeindex.app.data.entities.GroceryList
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.IngredientUnitConverter
import com.recipeindex.app.utils.resultOf
import com.recipeindex.app.utils.resultOfValidated
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.math.ceil

/**
 * GroceryListManager - Business logic for grocery list operations
 *
 * Handles ingredient consolidation with smart quantity summing and
 * pantry staple filtering based on user-configured thresholds
 */
class GroceryListManager(
    private val groceryListDao: GroceryListDao,
    private val groceryItemDao: GroceryItemDao,
    private val recipeDao: RecipeDao,
    private val mealPlanDao: com.recipeindex.app.data.dao.MealPlanDao,
    private val pantryStapleManager: PantryStapleManager,
    private val settingsManager: SettingsManager
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
    suspend fun createList(name: String): Result<Long> = resultOfValidated(
        successLog = "createList: $name",
        errorLog = "createList failed",
        validate = { require(name.isNotBlank()) { "List name cannot be blank" } }
    ) {
        val list = GroceryList(
            name = name.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        groceryListDao.insert(list)
    }

    /**
     * Update grocery list
     */
    suspend fun updateList(list: GroceryList): Result<Unit> = resultOfValidated(
        successLog = "updateList: ${list.id}",
        errorLog = "updateList failed",
        validate = { require(list.name.isNotBlank()) { "List name cannot be blank" } }
    ) {
        val updatedList = list.copy(updatedAt = System.currentTimeMillis())
        groceryListDao.update(updatedList)
    }

    /**
     * Delete grocery list
     */
    suspend fun deleteList(list: GroceryList): Result<Unit> = resultOf(
        successLog = "deleteList: ${list.id}",
        errorLog = "deleteList failed"
    ) {
        groceryListDao.delete(list)
    }

    /**
     * Add recipes to grocery list with consolidation
     * @return Result<Int> containing the count of ingredients added
     */
    suspend fun addRecipesToList(listId: Long, recipeIds: List<Long>): Result<Int> = resultOf(
        successLog = "addRecipesToList: ${recipeIds.size} recipes",
        errorLog = "addRecipesToList failed"
    ) {
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

        DebugConfig.debugLog(
            DebugConfig.Category.MANAGER,
            "addRecipesToList: Consolidated into ${consolidated.size} items"
        )

        // Filter pantry staples based on configured thresholds
        val filteredItems = filterPantryStaples(consolidated)

        DebugConfig.debugLog(
            DebugConfig.Category.MANAGER,
            "addRecipesToList: After filtering pantry staples: ${filteredItems.size} items remain"
        )

        // Get existing items to check for duplicates
        val existingItems = groceryItemDao.getItemsForList(listId).first()

        // Add or update items
        for (item in filteredItems) {
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

        filteredItems.size
    }

    /**
     * Add manual item to list
     */
    suspend fun addManualItem(listId: Long, itemName: String): Result<Long> = resultOfValidated(
        successLog = "addManualItem: $itemName",
        errorLog = "addManualItem failed",
        validate = { require(itemName.isNotBlank()) { "Item name cannot be blank" } }
    ) {
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
        groceryItemDao.insert(item)
    }

    /**
     * Toggle item checked status
     */
    suspend fun toggleItemChecked(item: GroceryItem): Result<Unit> = resultOf(
        errorLog = "toggleItemChecked failed"
    ) {
        val updated = item.copy(
            isChecked = !item.isChecked,
            updatedAt = System.currentTimeMillis()
        )
        groceryItemDao.update(updated)
    }

    /**
     * Update grocery item
     */
    suspend fun updateItem(item: GroceryItem): Result<Unit> = resultOf(
        errorLog = "updateItem failed"
    ) {
        val updated = item.copy(updatedAt = System.currentTimeMillis())
        groceryItemDao.update(updated)
    }

    /**
     * Delete grocery item
     */
    suspend fun deleteItem(item: GroceryItem): Result<Unit> = resultOf(
        errorLog = "deleteItem failed"
    ) {
        groceryItemDao.delete(item)
    }

    /**
     * Clear all checked items from a list
     */
    suspend fun clearCheckedItems(listId: Long): Result<Unit> = resultOf(
        successLog = "clearCheckedItems for list: $listId",
        errorLog = "clearCheckedItems failed"
    ) {
        groceryItemDao.deleteCheckedItems(listId)
    }

    /**
     * Check all items in a list
     */
    suspend fun checkAllItems(listId: Long): Result<Unit> = resultOf(
        successLog = "checkAllItems for list: $listId",
        errorLog = "checkAllItems failed"
    ) {
        val items = groceryItemDao.getItemsForList(listId).first()
        items.forEach { item ->
            if (!item.isChecked) {
                groceryItemDao.update(
                    item.copy(isChecked = true, updatedAt = System.currentTimeMillis())
                )
            }
        }
    }

    /**
     * Uncheck all items in a list
     */
    suspend fun uncheckAllItems(listId: Long): Result<Unit> = resultOf(
        successLog = "uncheckAllItems for list: $listId",
        errorLog = "uncheckAllItems failed"
    ) {
        val items = groceryItemDao.getItemsForList(listId).first()
        items.forEach { item ->
            if (item.isChecked) {
                groceryItemDao.update(
                    item.copy(isChecked = false, updatedAt = System.currentTimeMillis())
                )
            }
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
    suspend fun toggleItemCheckedById(itemId: Long, checked: Boolean): Result<Unit> = resultOf(
        errorLog = "toggleItemCheckedById failed"
    ) {
        groceryItemDao.updateCheckedStatus(itemId, checked)
    }

    /**
     * Delete item by ID
     */
    suspend fun deleteItemById(itemId: Long): Result<Unit> = resultOf(
        errorLog = "deleteItemById failed"
    ) {
        groceryItemDao.deleteById(itemId)
    }

    /**
     * Add meal plan to grocery list
     * Fetches all recipes in the meal plan and adds their ingredients
     * @return Result<Int> containing the count of ingredients added
     */
    suspend fun addMealPlanToList(listId: Long, planId: Long): Result<Int> = resultOf(
        errorLog = "addMealPlanToList failed"
    ) {
        // Get the meal plan
        val mealPlan = mealPlanDao.getById(planId)
            ?: throw Exception("Meal plan not found")

        DebugConfig.debugLog(
            DebugConfig.Category.MANAGER,
            "addMealPlanToList: plan ${mealPlan.name} has ${mealPlan.recipeIds.size} recipes"
        )

        // Add all recipes from the meal plan
        addRecipesToList(listId, mealPlan.recipeIds).getOrThrow()
    }

    /**
     * Parse ingredient string into structured data for a grocery list.
     *
     * Processing order:
     * 1. Apply user's unit-conversion preference (oz→g, cups→ml, etc.)
     * 2. Strip parenthetical prep notes  "(cut into strips)"
     * 3. Strip comma-separated prep notes ", trimmed"
     * 4. Remove preparation-only words (skinless, diced, sliced, …)
     * 5. Extract quantity, validate unit against known units, rest = name
     * 6. Round fractional counts up to 1 for countable items (½ onion → 1)
     */
    private fun parseIngredient(ingredientStr: String, recipeId: Long?): ParsedIngredient {
        DebugConfig.debugLog(
            DebugConfig.Category.MANAGER,
            "parseIngredient: Input='$ingredientStr' recipeId=$recipeId"
        )

        val settings = settingsManager.settings.value

        // Step 1: Apply unit conversion based on user preferences
        var remaining = IngredientUnitConverter.formatIngredient(
            ingredientStr.trim(),
            liquidPreference = settings.liquidVolumePreference,
            weightPreference = settings.weightPreference
        )

        // Step 2: Strip parenthetical content (prep notes, alternates, brand notes)
        remaining = remaining.replace(Regex("\\s*\\([^)]*\\)"), "").trim()

        // Step 3: Strip comma-separated prep clauses (e.g. ", cut into strips")
        remaining = remaining.replace(Regex(",.*$"), "").trim()

        // Step 4: Remove preparation and size modifier words
        val prepModifiers = setOf(
            "diced", "chopped", "shredded", "sliced", "cubed", "minced", "peeled",
            "trimmed", "skinless", "boneless", "deveined", "halved", "quartered",
            "thawed", "frozen", "softened", "melted", "beaten", "divided",
            "cut", "into", "strips", "chunks", "pieces", "rings",
            "small", "medium", "large", "fresh", "dry", "dried"
        )
        remaining = remaining.split(Regex("\\s+"))
            .filter { word -> word.lowercase() !in prepModifiers }
            .joinToString(" ")
            .trim()

        DebugConfig.debugLog(
            DebugConfig.Category.MANAGER,
            "parseIngredient: After cleaning='$remaining'"
        )

        // Units that are valid grocery-list units (non-unit tokens treated as part of name)
        val knownUnits = setOf(
            "cup", "cups", "tbsp", "tablespoon", "tablespoons",
            "tsp", "teaspoon", "teaspoons",
            "oz", "ounce", "ounces", "fl", "floz",
            "lb", "lbs", "pound", "pounds",
            "g", "gram", "grams", "kg", "kilogram", "kilograms",
            "ml", "milliliter", "milliliters", "l", "liter", "liters",
            "can", "cans", "jar", "jars", "bottle", "bottles",
            "pack", "package", "packages", "bunch", "bunches",
            "sprig", "sprigs", "clove", "cloves", "slice", "slices",
            "head", "heads", "stalk", "stalks", "ear", "ears",
            "sheet", "sheets", "fillet", "fillets", "piece", "pieces",
            "bag", "bags", "box", "boxes", "container", "containers"
        )

        // Check for canned/packaged items: "9 oz can of tomatoes"
        val cannedItemPattern = Regex(
            "^([\\d./]+)\\s+(oz|g|ml|lb)\\s+(can|jar|bottle|pack|package)s?\\s+(?:of\\s+)?(.+)",
            RegexOption.IGNORE_CASE
        )
        val cannedMatch = cannedItemPattern.find(remaining)
        if (cannedMatch != null) {
            val sizeQuantity = cannedMatch.groupValues[1]
            val sizeUnit = cannedMatch.groupValues[2]
            val containerType = cannedMatch.groupValues[3].lowercase()
            val itemName = cannedMatch.groupValues[4].trim()
            val result = ParsedIngredient(
                name = itemName,
                quantity = 1.0,
                unit = containerType,
                recipeId = recipeId,
                notes = "$sizeQuantity $sizeUnit"
            )
            DebugConfig.debugLog(
                DebugConfig.Category.MANAGER,
                "parseIngredient: Canned -> name='${result.name}' qty=${result.quantity} unit='${result.unit}'"
            )
            return result
        }

        // Extract quantity (handles integers, decimals, fractions, dash-mixed-numbers)
        // Pattern: optional mixed-number like "1-1/3" or "1 1/2", then unit candidate, then name
        val qtyPattern = Regex(
            "^(\\d+[-\\s]\\d+/\\d+|\\d+/\\d+|\\d+\\.\\d+|\\d+)\\s*([a-zA-Z]+)?\\s*(.*)",
            RegexOption.IGNORE_CASE
        )
        val match = qtyPattern.find(remaining)

        val result = if (match != null) {
            val quantityStr = match.groupValues[1].trim()
            val unitCandidate = match.groupValues[2].trim().lowercase()
            val nameAfterUnit = match.groupValues[3].trim()

            val quantity = parseMixedQuantity(quantityStr)

            // Only treat the candidate as a unit if it's in the known-units list
            val (unit, name) = if (unitCandidate.isNotBlank() && unitCandidate in knownUnits) {
                Pair(match.groupValues[2].trim(), nameAfterUnit.ifBlank { unitCandidate })
            } else {
                // Not a known unit — prepend it back onto the name
                val fullName = listOf(match.groupValues[2], nameAfterUnit)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                Pair(null, fullName.ifBlank { remaining })
            }

            var parsed = ParsedIngredient(
                name = name.trim(),
                quantity = if ((quantity ?: 0.0) > 0) quantity else null,
                unit = unit,
                recipeId = recipeId
            )

            // Round up fractional quantities for countable (unit-less) items
            // e.g. 1/2 onion → 1 onion; 0.5 avocado → 1 avocado
            if (parsed.unit == null && (parsed.quantity ?: 1.0) in 0.001..0.999) {
                parsed = parsed.copy(quantity = ceil(parsed.quantity!!).coerceAtLeast(1.0))
            }

            parsed
        } else {
            ParsedIngredient(
                name = remaining.trim(),
                quantity = null,
                unit = null,
                recipeId = recipeId
            )
        }

        DebugConfig.debugLog(
            DebugConfig.Category.MANAGER,
            "parseIngredient: Result -> name='${result.name}' qty=${result.quantity} unit='${result.unit}'"
        )
        return result
    }

    /**
     * Parse a quantity string that may be a whole number, decimal, simple fraction,
     * or mixed number separated by space or dash (e.g. "1-1/3", "1 1/2", "2/3", "1.5").
     */
    private fun parseMixedQuantity(quantityStr: String): Double? {
        val s = quantityStr.trim()

        // Dash or space mixed number: "1-1/3" or "1 1/2"
        val mixedDash = Regex("^(\\d+)[-\\s](\\d+)/(\\d+)$")
        mixedDash.find(s)?.let { m ->
            val whole = m.groupValues[1].toDoubleOrNull() ?: return null
            val num   = m.groupValues[2].toDoubleOrNull() ?: return null
            val den   = m.groupValues[3].toDoubleOrNull()?.takeIf { it != 0.0 } ?: return null
            return whole + num / den
        }

        // Simple fraction: "1/2"
        val fraction = Regex("^(\\d+)/(\\d+)$")
        fraction.find(s)?.let { m ->
            val num = m.groupValues[1].toDoubleOrNull() ?: return null
            val den = m.groupValues[2].toDoubleOrNull()?.takeIf { it != 0.0 } ?: return null
            return num / den
        }

        return s.toDoubleOrNull()
    }

    /**
     * Consolidate ingredients: sum quantities for same item+unit
     */
    private fun consolidateIngredients(ingredients: List<ParsedIngredient>): List<GroceryItem> {
        DebugConfig.debugLog(
            DebugConfig.Category.MANAGER,
            "consolidateIngredients: Starting with ${ingredients.size} ingredients"
        )

        val grouped = ingredients.groupBy {
            // Group by name and unit (case-insensitive)
            Pair(it.name.lowercase(), it.unit?.lowercase())
        }

        DebugConfig.debugLog(
            DebugConfig.Category.MANAGER,
            "consolidateIngredients: Grouped into ${grouped.size} unique items"
        )

        return grouped.map { (key, items) ->
            val (name, unit) = key
            val totalQuantity = items.mapNotNull { it.quantity }.sum()
            val sourceRecipes = items.mapNotNull { it.recipeId }.distinct()

            DebugConfig.debugLog(
                DebugConfig.Category.MANAGER,
                "consolidateIngredients: Grouping '${items.first().name}' (${items.size} occurrences) -> total qty=$totalQuantity unit='$unit'"
            )

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

    /**
     * Filter pantry staples based on user-configured thresholds
     *
     * Returns only items that should appear in the grocery list:
     * - Items not in the pantry staples config (always show)
     * - Pantry staples with alwaysFilter=true (always hide)
     * - Pantry staples where quantity exceeds threshold (show with quantity)
     */
    private suspend fun filterPantryStaples(items: List<GroceryItem>): List<GroceryItem> {
        val pantryConfigs = pantryStapleManager.getAllEnabled().first()

        DebugConfig.debugLog(
            DebugConfig.Category.MANAGER,
            "filterPantryStaples: Loaded ${pantryConfigs.size} pantry staple configurations"
        )

        return items.filter { item ->
            val matchingConfig = findMatchingConfig(item, pantryConfigs)

            if (matchingConfig == null) {
                // Not a pantry staple, always include
                DebugConfig.debugLog(
                    DebugConfig.Category.MANAGER,
                    "filterPantryStaples: '${item.name}' - NOT a pantry staple, including"
                )
                true
            } else if (matchingConfig.alwaysFilter) {
                // Always filter this item
                DebugConfig.debugLog(
                    DebugConfig.Category.MANAGER,
                    "filterPantryStaples: '${item.name}' - ALWAYS FILTER, excluding"
                )
                false
            } else {
                // Check if quantity exceeds threshold
                val exceedsThreshold = quantityExceedsThreshold(
                    item.quantity,
                    item.unit,
                    matchingConfig.thresholdQuantity,
                    matchingConfig.thresholdUnit
                )

                DebugConfig.debugLog(
                    DebugConfig.Category.MANAGER,
                    "filterPantryStaples: '${item.name}' ${item.quantity} ${item.unit ?: ""} vs threshold ${matchingConfig.thresholdQuantity} ${matchingConfig.thresholdUnit} - ${if (exceedsThreshold) "EXCEEDS (including)" else "BELOW (excluding)"}"
                )

                exceedsThreshold
            }
        }
    }

    /**
     * Find matching pantry staple configuration for an item
     */
    private fun findMatchingConfig(
        item: GroceryItem,
        configs: List<com.recipeindex.app.data.entities.PantryStapleConfig>
    ): com.recipeindex.app.data.entities.PantryStapleConfig? {
        val itemNameLower = item.name.lowercase().trim()

        return configs.find { config ->
            val configNameLower = config.itemName.lowercase().trim()

            // Check main name match
            if (itemNameLower == configNameLower || itemNameLower.contains(configNameLower)) {
                return@find true
            }

            // Check alternative names
            config.alternativeNames?.split(",")?.any { altName ->
                val altNameLower = altName.trim().lowercase()
                itemNameLower == altNameLower || itemNameLower.contains(altNameLower)
            } ?: false
        }
    }

    /**
     * Check if an item's quantity exceeds the threshold
     *
     * Converts both quantities to a common unit for comparison
     */
    private fun quantityExceedsThreshold(
        itemQty: Double?,
        itemUnit: String?,
        thresholdQty: Double,
        thresholdUnit: String
    ): Boolean {
        // No quantity means this is a small/negligible amount - filter it out
        if (itemQty == null || itemQty <= 0) {
            return false
        }

        // No unit means we can't compare properly - assume small amount and filter it out
        if (itemUnit == null) {
            return false
        }

        // Normalize both quantities to a common unit for comparison
        val normalizedItemQty = normalizeQuantityToMl(itemQty, itemUnit)
        val normalizedThresholdQty = normalizeQuantityToMl(thresholdQty, thresholdUnit)

        // If we couldn't normalize both, fall back to direct comparison if units match
        if (normalizedItemQty == null || normalizedThresholdQty == null) {
            return if (itemUnit.equals(thresholdUnit, ignoreCase = true)) {
                itemQty > thresholdQty
            } else {
                // Can't compare different units, include the item to be safe
                true
            }
        }

        return normalizedItemQty > normalizedThresholdQty
    }

    /**
     * Normalize quantity to milliliters for comparison
     * Returns null if unit cannot be converted
     */
    private fun normalizeQuantityToMl(quantity: Double, unit: String): Double? {
        return when (unit.lowercase().trim()) {
            // Volume - liquid
            "cup", "cups" -> quantity * 236.588  // 1 cup = 236.588 ml
            "tbsp", "tablespoon", "tablespoons" -> quantity * 14.787  // 1 tbsp = 14.787 ml
            "tsp", "teaspoon", "teaspoons" -> quantity * 4.929  // 1 tsp = 4.929 ml
            "fl oz", "floz", "fluid ounce", "fluid ounces" -> quantity * 29.574  // 1 fl oz = 29.574 ml
            "ml", "milliliter", "milliliters" -> quantity
            "l", "liter", "liters" -> quantity * 1000

            // Weight (using density of water for approximation: 1g ≈ 1ml)
            "g", "gram", "grams" -> quantity
            "kg", "kilogram", "kilograms" -> quantity * 1000
            "oz", "ounce", "ounces" -> quantity * 28.35  // 1 oz = 28.35 g ≈ 28.35 ml
            "lb", "lbs", "pound", "pounds" -> quantity * 453.592  // 1 lb = 453.592 g

            // Count-based units (eggs, onions, cloves, etc.)
            "egg", "eggs" -> quantity  // Compare counts directly
            "onion", "onions" -> quantity
            "clove", "cloves" -> quantity
            "shallot", "shallots" -> quantity
            "head", "heads" -> quantity

            // Special cases
            "can", "cans", "jar", "jars", "bottle", "bottles" -> quantity  // Compare counts

            else -> null  // Unknown unit
        }
    }
}
