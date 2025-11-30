package com.recipeindex.app.data.managers

import com.recipeindex.app.data.dao.PantryStapleConfigDao
import com.recipeindex.app.data.entities.PantryStapleConfig
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.resultOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * PantryStapleManager - Manages pantry staple filtering configurations
 *
 * Handles default configurations and user customizations for filtering
 * common pantry items from grocery lists based on quantity thresholds
 */
class PantryStapleManager(
    private val pantryStapleConfigDao: PantryStapleConfigDao
) {

    /**
     * Get all enabled pantry staple configurations
     */
    fun getAllEnabled(): Flow<List<PantryStapleConfig>> {
        return pantryStapleConfigDao.getAllEnabled()
    }

    /**
     * Get all configurations (for settings UI)
     */
    fun getAll(): Flow<List<PantryStapleConfig>> {
        return pantryStapleConfigDao.getAll()
    }

    /**
     * Get configurations by category
     */
    fun getByCategory(category: String): Flow<List<PantryStapleConfig>> {
        return pantryStapleConfigDao.getByCategory(category)
    }

    /**
     * Add or update a configuration
     */
    suspend fun saveConfig(config: PantryStapleConfig): Result<Long> = resultOf(
        successLog = "saveConfig: ${config.itemName}",
        errorLog = "saveConfig failed"
    ) {
        if (config.id == 0L) {
            pantryStapleConfigDao.insert(config)
        } else {
            pantryStapleConfigDao.update(config)
            config.id
        }
    }

    /**
     * Delete a configuration
     */
    suspend fun deleteConfig(config: PantryStapleConfig): Result<Unit> = resultOf(
        successLog = "deleteConfig: ${config.itemName}",
        errorLog = "deleteConfig failed"
    ) {
        pantryStapleConfigDao.delete(config)
    }

    /**
     * Initialize default pantry staple configurations if database is empty
     */
    suspend fun initializeDefaults(): Result<Unit> = resultOf(
        successLog = "Pantry staples defaults initialized",
        errorLog = "Failed to initialize pantry staples defaults"
    ) {
        val count = pantryStapleConfigDao.getCount()
        if (count == 0) {
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "Initializing default pantry staple configurations")
            pantryStapleConfigDao.insertAll(getDefaultConfigurations())
        } else {
            DebugConfig.debugLog(DebugConfig.Category.MANAGER, "Pantry staple configurations already exist ($count items)")
        }
    }

    /**
     * Reset to default configurations
     */
    suspend fun resetToDefaults(): Result<Unit> = resultOf(
        successLog = "Pantry staples reset to defaults",
        errorLog = "Failed to reset pantry staples"
    ) {
        pantryStapleConfigDao.deleteAll()
        pantryStapleConfigDao.insertAll(getDefaultConfigurations())
    }

    /**
     * Get default pantry staple configurations
     */
    private fun getDefaultConfigurations(): List<PantryStapleConfig> {
        return listOf(
            // SPICES & SEASONINGS
            PantryStapleConfig(itemName = "salt", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "pepper", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "black pepper", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "white pepper", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "paprika", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "cumin", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "coriander", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "turmeric", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "cayenne", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "cayenne pepper", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "chili powder", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "garlic powder", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "onion powder", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "cinnamon", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "nutmeg", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "ground ginger", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "ground cloves", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),
            PantryStapleConfig(itemName = "allspice", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Spices & Seasonings"),

            // DRIED HERBS
            PantryStapleConfig(itemName = "oregano", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "dried oregano", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "basil", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "dried basil", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "thyme", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "dried thyme", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "rosemary", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "dried rosemary", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "bay leaves", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "bay leaf", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "dried parsley", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "dried cilantro", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "dried dill", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "italian seasoning", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),
            PantryStapleConfig(itemName = "herbes de provence", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Dried Herbs"),

            // BAKING
            PantryStapleConfig(itemName = "flour", thresholdQuantity = 3.0, thresholdUnit = "cups", category = "Baking"),
            PantryStapleConfig(itemName = "all-purpose flour", thresholdQuantity = 3.0, thresholdUnit = "cups", category = "Baking"),
            PantryStapleConfig(itemName = "bread flour", thresholdQuantity = 3.0, thresholdUnit = "cups", category = "Baking"),
            PantryStapleConfig(itemName = "whole wheat flour", thresholdQuantity = 3.0, thresholdUnit = "cups", category = "Baking"),
            PantryStapleConfig(itemName = "sugar", thresholdQuantity = 2.0, thresholdUnit = "cups", category = "Baking"),
            PantryStapleConfig(itemName = "granulated sugar", thresholdQuantity = 2.0, thresholdUnit = "cups", category = "Baking"),
            PantryStapleConfig(itemName = "white sugar", thresholdQuantity = 2.0, thresholdUnit = "cups", category = "Baking"),
            PantryStapleConfig(itemName = "brown sugar", thresholdQuantity = 1.0, thresholdUnit = "cup", category = "Baking"),
            PantryStapleConfig(itemName = "baking powder", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Baking"),
            PantryStapleConfig(itemName = "baking soda", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Baking"),
            PantryStapleConfig(itemName = "vanilla extract", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Baking"),
            PantryStapleConfig(itemName = "vanilla", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Baking"),
            PantryStapleConfig(itemName = "almond extract", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Baking"),
            PantryStapleConfig(itemName = "cornstarch", thresholdQuantity = 0.5, thresholdUnit = "cup", category = "Baking"),

            // OILS
            PantryStapleConfig(itemName = "olive oil", thresholdQuantity = 1.0, thresholdUnit = "cup", category = "Oils"),
            PantryStapleConfig(itemName = "extra virgin olive oil", thresholdQuantity = 1.0, thresholdUnit = "cup", category = "Oils"),
            PantryStapleConfig(itemName = "vegetable oil", thresholdQuantity = 1.0, thresholdUnit = "cup", category = "Oils"),
            PantryStapleConfig(itemName = "canola oil", thresholdQuantity = 1.0, thresholdUnit = "cup", category = "Oils"),
            PantryStapleConfig(itemName = "coconut oil", thresholdQuantity = 1.0, thresholdUnit = "cup", category = "Oils"),
            PantryStapleConfig(itemName = "sesame oil", thresholdQuantity = 1.0, thresholdUnit = "cup", category = "Oils"),
            PantryStapleConfig(itemName = "cooking spray", thresholdQuantity = 1.0, thresholdUnit = "cup", category = "Oils"),

            // VINEGARS
            PantryStapleConfig(itemName = "vinegar", thresholdQuantity = 0.5, thresholdUnit = "cup", category = "Vinegars"),
            PantryStapleConfig(itemName = "white vinegar", thresholdQuantity = 0.5, thresholdUnit = "cup", category = "Vinegars"),
            PantryStapleConfig(itemName = "red wine vinegar", thresholdQuantity = 0.5, thresholdUnit = "cup", category = "Vinegars"),
            PantryStapleConfig(itemName = "white wine vinegar", thresholdQuantity = 0.5, thresholdUnit = "cup", category = "Vinegars"),
            PantryStapleConfig(itemName = "balsamic vinegar", thresholdQuantity = 0.5, thresholdUnit = "cup", category = "Vinegars"),
            PantryStapleConfig(itemName = "apple cider vinegar", thresholdQuantity = 0.5, thresholdUnit = "cup", category = "Vinegars"),
            PantryStapleConfig(itemName = "rice vinegar", thresholdQuantity = 0.5, thresholdUnit = "cup", category = "Vinegars"),

            // LIQUIDS
            PantryStapleConfig(itemName = "water", thresholdQuantity = 0.0, thresholdUnit = "cup", category = "Liquids", alwaysFilter = true),
            PantryStapleConfig(itemName = "chicken broth", thresholdQuantity = 3.0, thresholdUnit = "cups", category = "Liquids"),
            PantryStapleConfig(itemName = "beef broth", thresholdQuantity = 3.0, thresholdUnit = "cups", category = "Liquids"),
            PantryStapleConfig(itemName = "vegetable broth", thresholdQuantity = 3.0, thresholdUnit = "cups", category = "Liquids"),
            PantryStapleConfig(itemName = "chicken stock", thresholdQuantity = 3.0, thresholdUnit = "cups", category = "Liquids"),
            PantryStapleConfig(itemName = "beef stock", thresholdQuantity = 3.0, thresholdUnit = "cups", category = "Liquids"),
            PantryStapleConfig(itemName = "vegetable stock", thresholdQuantity = 3.0, thresholdUnit = "cups", category = "Liquids"),
            PantryStapleConfig(itemName = "clam juice", thresholdQuantity = 0.75, thresholdUnit = "cup", category = "Liquids"),
            PantryStapleConfig(itemName = "seafood broth", thresholdQuantity = 0.75, thresholdUnit = "cup", category = "Liquids"),

            // GRAINS & PASTA
            PantryStapleConfig(itemName = "rice", thresholdQuantity = 2.0, thresholdUnit = "cups", category = "Grains & Pasta"),
            PantryStapleConfig(itemName = "white rice", thresholdQuantity = 2.0, thresholdUnit = "cups", category = "Grains & Pasta"),
            PantryStapleConfig(itemName = "brown rice", thresholdQuantity = 2.0, thresholdUnit = "cups", category = "Grains & Pasta"),
            PantryStapleConfig(itemName = "jasmine rice", thresholdQuantity = 2.0, thresholdUnit = "cups", category = "Grains & Pasta"),
            PantryStapleConfig(itemName = "basmati rice", thresholdQuantity = 2.0, thresholdUnit = "cups", category = "Grains & Pasta"),
            PantryStapleConfig(itemName = "pasta", thresholdQuantity = 1.0, thresholdUnit = "lb", category = "Grains & Pasta"),
            PantryStapleConfig(itemName = "spaghetti", thresholdQuantity = 1.0, thresholdUnit = "lb", category = "Grains & Pasta"),
            PantryStapleConfig(itemName = "penne", thresholdQuantity = 1.0, thresholdUnit = "lb", category = "Grains & Pasta"),
            PantryStapleConfig(itemName = "linguine", thresholdQuantity = 1.0, thresholdUnit = "lb", category = "Grains & Pasta"),
            PantryStapleConfig(itemName = "fettuccine", thresholdQuantity = 1.0, thresholdUnit = "lb", category = "Grains & Pasta"),

            // AROMATICS
            PantryStapleConfig(itemName = "garlic", thresholdQuantity = 8.0, thresholdUnit = "cloves", category = "Aromatics"),
            PantryStapleConfig(itemName = "ginger", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Aromatics"),
            PantryStapleConfig(itemName = "onion", thresholdQuantity = 2.0, thresholdUnit = "onions", category = "Aromatics"),
            PantryStapleConfig(itemName = "yellow onion", thresholdQuantity = 2.0, thresholdUnit = "onions", category = "Aromatics"),
            PantryStapleConfig(itemName = "white onion", thresholdQuantity = 2.0, thresholdUnit = "onions", category = "Aromatics"),
            PantryStapleConfig(itemName = "red onion", thresholdQuantity = 2.0, thresholdUnit = "onions", category = "Aromatics"),
            PantryStapleConfig(itemName = "shallots", thresholdQuantity = 1.0, thresholdUnit = "shallots", category = "Aromatics"),
            PantryStapleConfig(itemName = "shallot", thresholdQuantity = 1.0, thresholdUnit = "shallots", category = "Aromatics"),

            // SWEETENERS
            PantryStapleConfig(itemName = "honey", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Sweeteners"),
            PantryStapleConfig(itemName = "maple syrup", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Sweeteners"),
            PantryStapleConfig(itemName = "molasses", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Sweeteners"),
            PantryStapleConfig(itemName = "agave", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Sweeteners"),
            PantryStapleConfig(itemName = "agave nectar", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Sweeteners"),

            // CONDIMENTS & SAUCES
            PantryStapleConfig(itemName = "soy sauce", thresholdQuantity = 0.5, thresholdUnit = "cup", category = "Condiments & Sauces"),
            PantryStapleConfig(itemName = "worcestershire sauce", thresholdQuantity = 0.25, thresholdUnit = "cup", category = "Condiments & Sauces"),
            PantryStapleConfig(itemName = "fish sauce", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Condiments & Sauces"),
            PantryStapleConfig(itemName = "hot sauce", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Condiments & Sauces"),
            PantryStapleConfig(itemName = "ketchup", thresholdQuantity = 0.5, thresholdUnit = "cup", category = "Condiments & Sauces"),
            PantryStapleConfig(itemName = "bbq sauce", thresholdQuantity = 0.5, thresholdUnit = "cup", category = "Condiments & Sauces"),
            PantryStapleConfig(itemName = "mustard", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Condiments & Sauces"),
            PantryStapleConfig(itemName = "mayo", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Condiments & Sauces"),
            PantryStapleConfig(itemName = "mayonnaise", thresholdQuantity = 2.0, thresholdUnit = "tbsp", category = "Condiments & Sauces"),

            // DAIRY
            PantryStapleConfig(itemName = "butter", thresholdQuantity = 4.0, thresholdUnit = "tbsp", category = "Dairy"),
            PantryStapleConfig(itemName = "milk", thresholdQuantity = 0.5, thresholdUnit = "cup", category = "Dairy"),
            PantryStapleConfig(itemName = "eggs", thresholdQuantity = 2.0, thresholdUnit = "eggs", category = "Dairy"),
            PantryStapleConfig(itemName = "egg", thresholdQuantity = 2.0, thresholdUnit = "eggs", category = "Dairy")
        )
    }
}
