package com.recipeindex.app.utils

import android.content.Context
import android.content.Intent

/**
 * RecipeTemplateHelper - Provides a plain text recipe template for manual entry
 *
 * Users can email this template to themselves, edit it in any text editor,
 * then import via "Import from File" in the app.
 */
object RecipeTemplateHelper {

    /**
     * Plain text recipe template compatible with TextRecipeParser
     *
     * Uses [brackets] to indicate fields that should be replaced.
     */
    val RECIPE_TEMPLATE = """
Title: [Recipe Title]
Servings: [number]
Prep Time: [time, e.g. 15 minutes]
Cook Time: [time, e.g. 30 minutes]
Tags: [tag1, tag2, tag3]

Ingredients:
[amount] [ingredient]
[amount] [ingredient]
[amount] [ingredient]

Instructions:
[Step 1]
[Step 2]
[Step 3]

Notes:
[Optional notes or tips]
""".trimIndent()

    /**
     * Instructions text explaining how to use the template
     */
    private val TEMPLATE_INSTRUCTIONS = """
=== RECIPE INDEX - RECIPE TEMPLATE ===

Replace everything in [brackets] with your recipe content.
Delete any sections you don't need.
Save as a .txt file and import via: Add Recipe > Import > From Text File

============================================

""".trimIndent()

    /**
     * Get the full template with instructions
     */
    fun getTemplateWithInstructions(): String {
        return TEMPLATE_INSTRUCTIONS + RECIPE_TEMPLATE
    }

    /**
     * Share the recipe template via Android share sheet
     *
     * Users can email it to themselves or save to cloud storage
     */
    fun shareTemplate(context: Context) {
        val templateText = getTemplateWithInstructions()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "Recipe Index - Recipe Template")
            putExtra(Intent.EXTRA_SUBJECT, "Recipe Template for Recipe Index")
            putExtra(Intent.EXTRA_TEXT, templateText)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Recipe Template").apply {
            // Exclude Recipe Index from the share options
            val componentName = android.content.ComponentName(
                context.packageName,
                "${context.packageName}.ui.MainActivity"
            )
            putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(componentName))
        }

        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)

        DebugConfig.debugLog(DebugConfig.Category.UI, "Recipe template shared")
    }
}
