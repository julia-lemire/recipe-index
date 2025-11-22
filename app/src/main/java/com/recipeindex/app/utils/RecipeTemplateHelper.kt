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
     * Section headers recognized by the parser:
     * - Ingredients / Ingredient
     * - Instructions / Directions / Steps / Method
     * - Servings / Yield / Serves
     * - Prep Time
     * - Cook Time
     * - Tags / Categories / Cuisine
     * - Notes / Tips / Recipe Notes
     */
    val RECIPE_TEMPLATE = """
My Recipe Title

Servings: 4
Prep Time: 15 minutes
Cook Time: 30 minutes
Tags: dinner, easy, healthy

Ingredients:
2 cups all-purpose flour
1 teaspoon salt
1/2 cup butter, softened
3 large eggs
1 cup milk

Instructions:
1. Preheat oven to 350°F.
2. In a large bowl, combine flour and salt.
3. Add butter and mix until crumbly.
4. Beat eggs with milk, then add to flour mixture.
5. Stir until just combined - do not overmix.
6. Pour into a greased 9x13 inch pan.
7. Bake for 25-30 minutes until golden brown.
8. Let cool for 10 minutes before serving.

Notes:
You can substitute whole wheat flour for half of the all-purpose flour.
Store leftovers in an airtight container for up to 3 days.
""".trimIndent()

    /**
     * Instructions text explaining how to use the template
     */
    private val TEMPLATE_INSTRUCTIONS = """
=== RECIPE INDEX - RECIPE TEMPLATE ===

HOW TO USE THIS TEMPLATE:
1. Edit the recipe below in any text editor
2. Replace the sample content with your recipe
3. Save the file (any name, .txt extension recommended)
4. In Recipe Index app: Add Recipe > Import > From File
5. Select your saved file

FORMATTING TIPS:
- The first line before "Ingredients:" becomes the recipe title
- Use "Servings:", "Prep Time:", "Cook Time:", "Tags:" on their own lines
- Time can be written as: "30 minutes", "1 hour 15 min", "45m", etc.
- Tags should be comma-separated
- Each ingredient should be on its own line
- Instructions can be numbered (1. 2. 3.) or unnumbered
- Notes section is optional

SUPPORTED SECTION HEADERS:
- Ingredients (or Ingredient)
- Instructions (or Directions, Steps, Method)
- Servings (or Yield, Serves)
- Prep Time
- Cook Time
- Tags (or Categories, Cuisine)
- Notes (or Tips, Recipe Notes)

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
