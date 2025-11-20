package com.recipeindex.app.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.recipeindex.app.data.entities.GroceryItem
import com.recipeindex.app.data.entities.GroceryList
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.entities.Recipe
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * ShareHelper - Utility for sharing and importing recipes, meal plans, and grocery lists
 *
 * Features:
 * - App-to-app sharing via JSON with base64 encoded photos
 * - Human-readable text fallback
 * - Duplicate detection on import
 * - Standard Android share sheet (email, Samsung Quick Share, messaging apps, etc.)
 */
object ShareHelper {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private const val MAX_PHOTO_SIZE_MB = 10
    private const val MAX_PHOTO_DIMENSION = 1920 // Max width/height for photos

    /**
     * Share a recipe with optional photo
     *
     * Creates SharePackage with JSON and human-readable text fallback
     * Uses standard Android share sheet
     */
    fun shareRecipe(context: Context, recipe: Recipe, photoPath: String? = null) {
        val shareRecipe = recipe.toShareRecipe()
        val photos = mutableMapOf<String, String>()

        // Encode photo if exists
        photoPath?.let { path ->
            encodePhoto(path)?.let { encoded ->
                photos[recipe.id.toString()] = encoded
            }
        }

        val sharePackage = SharePackage(
            type = ShareType.RECIPE,
            recipe = shareRecipe,
            photos = photos
        )

        val json = gson.toJson(sharePackage)
        val humanText = formatRecipeAsText(recipe)

        shareContent(context, "Share Recipe", json, humanText)
    }

    /**
     * Share a meal plan with all recipes and photos
     *
     * Includes all recipe details and photos for complete meal plan sharing
     */
    fun shareMealPlan(
        context: Context,
        mealPlan: MealPlan,
        recipes: List<Recipe>,
        recipePhotos: Map<Long, String> // recipeId -> photoPath
    ) {
        val shareMealPlan = mealPlan.toShareMealPlan()
        val shareRecipes = recipes.map { it.toShareRecipe() }
        val photos = mutableMapOf<String, String>()

        // Encode all photos
        recipePhotos.forEach { (recipeId, path) ->
            encodePhoto(path)?.let { encoded ->
                photos[recipeId.toString()] = encoded
            }
        }

        val sharePackage = SharePackage(
            type = ShareType.MEAL_PLAN,
            mealPlan = shareMealPlan,
            recipes = shareRecipes,
            photos = photos
        )

        val json = gson.toJson(sharePackage)
        val humanText = formatMealPlanAsText(mealPlan, recipes)

        shareContent(context, "Share Meal Plan", json, humanText)
    }

    /**
     * Share a grocery list with all items
     */
    fun shareGroceryList(
        context: Context,
        groceryList: GroceryList,
        items: List<GroceryItem>
    ) {
        val shareGroceryList = groceryList.toShareGroceryList(items)

        val sharePackage = SharePackage(
            type = ShareType.GROCERY_LIST,
            groceryList = shareGroceryList
        )

        val json = gson.toJson(sharePackage)
        val humanText = formatGroceryListAsText(groceryList, items)

        shareContent(context, "Share Grocery List", json, humanText)
    }

    /**
     * Share content using Android share sheet
     *
     * Sends both JSON (for app-to-app) and human text (fallback) formats
     */
    private fun shareContent(
        context: Context,
        title: String,
        json: String,
        humanText: String
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, title)
            putExtra(Intent.EXTRA_SUBJECT, title)
            // Primary: JSON for app-to-app sharing
            putExtra("recipeindex.json", json)
            // Fallback: Human-readable text
            putExtra(Intent.EXTRA_TEXT, humanText)
        }

        // Create chooser and exclude Recipe Index from appearing in its own share sheet
        val chooser = Intent.createChooser(shareIntent, title).apply {
            // Exclude Recipe Index from the share options
            val componentName = android.content.ComponentName(
                context.packageName,
                "${context.packageName}.ui.MainActivity"
            )
            putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(componentName))
        }

        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)

        DebugConfig.debugLog(DebugConfig.Category.UI, "Share dialog opened: $title")
    }

    /**
     * Import a shared package from JSON
     *
     * Returns ImportResult with duplicate detection
     */
    fun importFromJson(
        json: String,
        existingRecipes: List<Recipe>
    ): ImportResult {
        return try {
            val sharePackage = gson.fromJson(json, SharePackage::class.java)

            when (sharePackage.type) {
                ShareType.RECIPE -> {
                    sharePackage.recipe?.let { shareRecipe ->
                        // Check for duplicates
                        val duplicate = findDuplicate(shareRecipe, existingRecipes)
                        if (duplicate != null) {
                            val photoBase64 = sharePackage.photos[duplicate.id.toString()]
                            ImportResult.DuplicateDetected(duplicate, shareRecipe, photoBase64)
                        } else {
                            // No duplicate, ready to import
                            ImportResult.Success(message = "Recipe ready to import")
                        }
                    } ?: ImportResult.Error("Invalid recipe data")
                }
                ShareType.MEAL_PLAN -> {
                    // Import all recipes first, then create meal plan
                    ImportResult.Success(message = "Meal plan with ${sharePackage.recipes.size} recipes ready to import")
                }
                ShareType.GROCERY_LIST -> {
                    sharePackage.groceryList?.let {
                        ImportResult.Success(message = "Grocery list with ${it.items.size} items ready to import")
                    } ?: ImportResult.Error("Invalid grocery list data")
                }
            }
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "Import failed", e)
            ImportResult.Error("Failed to parse shared data: ${e.message}")
        }
    }

    /**
     * Find duplicate recipe based on title and sourceUrl
     *
     * Matching logic:
     * 1. If both have sourceUrl and they match -> duplicate
     * 2. Otherwise, normalized title match (case-insensitive, trimmed)
     */
    private fun findDuplicate(shareRecipe: ShareRecipe, existingRecipes: List<Recipe>): Recipe? {
        // First try URL match if both have URLs
        if (!shareRecipe.sourceUrl.isNullOrBlank()) {
            existingRecipes.firstOrNull {
                !it.sourceUrl.isNullOrBlank() &&
                it.sourceUrl.equals(shareRecipe.sourceUrl, ignoreCase = true)
            }?.let { return it }
        }

        // Fall back to normalized title match
        val normalizedTitle = shareRecipe.title.trim().lowercase()
        return existingRecipes.firstOrNull {
            it.title.trim().lowercase() == normalizedTitle
        }
    }

    /**
     * Generate incremented title for "Keep Both" duplicate action
     *
     * Examples: "Chicken Soup" -> "Chicken Soup (2)", "Recipe (2)" -> "Recipe (3)"
     */
    fun generateIncrementedTitle(originalTitle: String, existingTitles: List<String>): String {
        var counter = 2
        var newTitle = "$originalTitle ($counter)"

        while (existingTitles.any { it.equals(newTitle, ignoreCase = true) }) {
            counter++
            newTitle = "$originalTitle ($counter)"
        }

        return newTitle
    }

    /**
     * Encode photo to base64 with size optimization
     *
     * Scales down large images to keep share package manageable
     */
    private fun encodePhoto(photoPath: String): String? {
        return try {
            val file = File(photoPath)
            if (!file.exists()) return null

            // Load and scale bitmap
            val bitmap = BitmapFactory.decodeFile(photoPath) ?: return null
            val scaledBitmap = scaleBitmapIfNeeded(bitmap)

            // Encode to base64
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytes = outputStream.toByteArray()

            // Check size limit
            val sizeMB = bytes.size / (1024.0 * 1024.0)
            if (sizeMB > MAX_PHOTO_SIZE_MB) {
                DebugConfig.debugLog(DebugConfig.Category.MANAGER, "Photo too large (${sizeMB}MB), skipping: $photoPath")
                return null
            }

            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "Photo encoding failed", e)
            null
        }
    }

    /**
     * Decode base64 photo to file
     */
    fun decodePhoto(base64: String, outputPath: String): Boolean {
        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            File(outputPath).writeBytes(bytes)
            true
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.MANAGER, "Photo decoding failed", e)
            false
        }
    }

    /**
     * Scale bitmap to reasonable size for sharing
     */
    private fun scaleBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val maxDimension = MAX_PHOTO_DIMENSION

        if (bitmap.width <= maxDimension && bitmap.height <= maxDimension) {
            return bitmap
        }

        val ratio = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    // === Human-readable text formatting ===

    private fun formatRecipeAsText(recipe: Recipe): String {
        val sb = StringBuilder()

        sb.appendLine("📖 ${recipe.title}")
        sb.appendLine()

        // Meta info
        sb.append("🍽️  $${recipe.servings} servings")
        recipe.prepTimeMinutes?.let { sb.append(" • ⏱️ ${it}m prep") }
        recipe.cookTimeMinutes?.let { sb.append(" • 🔥 ${it}m cook") }
        sb.appendLine()

        if (recipe.tags.isNotEmpty()) {
            sb.appendLine("🏷️  ${recipe.tags.joinToString(", ")}")
        }

        recipe.sourceUrl?.let {
            sb.appendLine("🔗 $it")
        }

        sb.appendLine()
        sb.appendLine("INGREDIENTS:")
        recipe.ingredients.forEachIndexed { index, ingredient ->
            sb.appendLine("${index + 1}. $ingredient")
        }

        sb.appendLine()
        sb.appendLine("INSTRUCTIONS:")
        recipe.instructions.forEachIndexed { index, instruction ->
            sb.appendLine("${index + 1}. $instruction")
        }

        recipe.notes?.let {
            sb.appendLine()
            sb.appendLine("NOTES:")
            sb.appendLine(it)
        }

        sb.appendLine()
        sb.appendLine("—")
        sb.appendLine("Shared from Recipe Index")

        return sb.toString()
    }

    private fun formatMealPlanAsText(mealPlan: MealPlan, recipes: List<Recipe>): String {
        val sb = StringBuilder()

        sb.appendLine("📅 ${mealPlan.name}")
        sb.appendLine()

        // Date range if exists
        if (mealPlan.startDate != null && mealPlan.endDate != null) {
            sb.appendLine("${formatDate(mealPlan.startDate)} - ${formatDate(mealPlan.endDate)}")
            sb.appendLine()
        }

        sb.appendLine("RECIPES (${recipes.size}):")
        recipes.forEachIndexed { index, recipe ->
            sb.appendLine()
            sb.appendLine("${index + 1}. ${recipe.title}")
            sb.appendLine("   Ingredients:")
            recipe.ingredients.take(5).forEach { sb.appendLine("   • $it") }
            if (recipe.ingredients.size > 5) {
                sb.appendLine("   ... ${recipe.ingredients.size - 5} more")
            }
        }

        mealPlan.notes?.let {
            sb.appendLine()
            sb.appendLine("NOTES:")
            sb.appendLine(it)
        }

        sb.appendLine()
        sb.appendLine("—")
        sb.appendLine("Shared from Recipe Index")

        return sb.toString()
    }

    private fun formatGroceryListAsText(list: GroceryList, items: List<GroceryItem>): String {
        val sb = StringBuilder()

        sb.appendLine("🛒 ${list.name}")
        sb.appendLine()
        sb.appendLine("Items (${items.size}):")

        items.forEach { item ->
            sb.append("☐ ")
            item.quantity?.let { sb.append("$it ") }
            item.unit?.let { sb.append("$it ") }
            sb.append(item.name)
            item.notes?.let { sb.append(" ($it)") }
            sb.appendLine()
        }

        sb.appendLine()
        sb.appendLine("—")
        sb.appendLine("Shared from Recipe Index")

        return sb.toString()
    }

    private fun formatDate(epochMillis: Long): String {
        val dateFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(epochMillis))
    }
}
