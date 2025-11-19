package com.recipeindex.app.data.parsers

import com.recipeindex.app.data.entities.RecipeSource
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for TextRecipeParser
 *
 * Focus: Section detection, noise filtering, ingredient/instruction validation, time parsing
 */
class TextRecipeParserTest {

    // ========== Section Detection Tests ==========

    @Test
    fun `detectSections finds ingredients section`() {
        val text = """
            Chocolate Chip Cookies

            Ingredients:
            2 cups flour
            1 cup sugar

            Instructions:
            Mix well
        """.trimIndent()

        val result = TextRecipeParser.parseText(text, RecipeSource.PDF)

        assertTrue("Should succeed parsing", result.isSuccess)
        val recipe = result.getOrNull()!!
        assertEquals("Should extract 2 ingredients", 2, recipe.ingredients.size)
    }

    @Test
    fun `detectSections finds instructions with variations`() {
        val testCases = listOf(
            "Instructions:",
            "Directions:",
            "Steps:",
            "Method:"
        )

        testCases.forEach { header ->
            val text = """
                Test Recipe

                Ingredients:
                1 cup flour

                $header
                Mix the flour
                Bake at 350°F
            """.trimIndent()

            val result = TextRecipeParser.parseText(text, RecipeSource.PDF)
            val recipe = result.getOrNull()!!

            assertEquals(
                "Should find instructions with header: $header",
                2,
                recipe.instructions.size
            )
        }
    }

    @Test
    fun `detectSections skips footer with ingredients keyword`() {
        val text = """
            Test Recipe

            Ingredients:
            1 cup flour
            2 eggs

            Instructions:
            Mix well

            Click here to save ingredients
            Shop these ingredients now
        """.trimIndent()

        val result = TextRecipeParser.parseText(text, RecipeSource.PDF)
        val recipe = result.getOrNull()!!

        // Should only find the real ingredients section, not the footer CTAs
        assertEquals("Should have 2 ingredients", 2, recipe.ingredients.size)
        assertFalse("Should not include footer text", recipe.ingredients.any { it.contains("Click") })
    }

    @Test
    fun `detectSections finds servings variations`() {
        val testCases = listOf(
            "Servings: 4" to 4,
            "Serves 6" to 6,
            "Yield: 8 cookies" to 8
        )

        testCases.forEach { (line, expectedServings) ->
            val text = """
                Recipe
                $line
                Ingredients:
                1 cup flour
                Instructions:
                Mix
            """.trimIndent()

            val result = TextRecipeParser.parseText(text, RecipeSource.PDF)
            val recipe = result.getOrNull()!!

            assertEquals(
                "Should parse servings from: $line",
                expectedServings,
                recipe.servings
            )
        }
    }

    // ========== Website Noise Filtering Tests ==========

    @Test
    fun `isWebsiteNoise detects save recipe CTAs`() {
        // Use reflection to access private method
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "isWebsiteNoise",
            String::class.java
        )
        method.isAccessible = true

        val noisyLines = listOf(
            "Save this recipe for later",
            "Click here to shop ingredients",
            "Get the recipe now",
            "View all recipes in our collection",
            "Subscribe to our newsletter"
        )

        noisyLines.forEach { line ->
            val isNoise = method.invoke(TextRecipeParser, line) as Boolean
            assertTrue("Should detect noise: $line", isNoise)
        }
    }

    @Test
    fun `isWebsiteNoise detects footer text`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "isWebsiteNoise",
            String::class.java
        )
        method.isAccessible = true

        val footerLines = listOf(
            "Home",
            "About",
            "Contact",
            "Privacy Policy",
            "Terms and Conditions",
            "Copyright 2025"
        )

        footerLines.forEach { line ->
            val isNoise = method.invoke(TextRecipeParser, line) as Boolean
            assertTrue("Should detect footer: $line", isNoise)
        }
    }

    @Test
    fun `isWebsiteNoise allows valid recipe content`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "isWebsiteNoise",
            String::class.java
        )
        method.isAccessible = true

        val validLines = listOf(
            "2 cups flour",
            "Preheat oven to 350°F",
            "Mix the ingredients together",
            "Bake for 30 minutes"
        )

        validLines.forEach { line ->
            val isNoise = method.invoke(TextRecipeParser, line) as Boolean
            assertFalse("Should allow valid content: $line", isNoise)
        }
    }

    // ========== Ingredient Validation Tests ==========

    @Test
    fun `looksLikeIngredient accepts lines with quantities and units`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "looksLikeIngredient",
            String::class.java
        )
        method.isAccessible = true

        val ingredients = listOf(
            "2 cups flour",
            "1 tablespoon vanilla extract",
            "1/2 teaspoon salt",
            "3 oz cream cheese",
            "1 pound ground beef"
        )

        ingredients.forEach { line ->
            val isIngredient = method.invoke(TextRecipeParser, line) as Boolean
            assertTrue("Should accept ingredient: $line", isIngredient)
        }
    }

    @Test
    fun `looksLikeIngredient accepts common food words`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "looksLikeIngredient",
            String::class.java
        )
        method.isAccessible = true

        val ingredients = listOf(
            "chicken breast",
            "fresh garlic",
            "diced tomatoes",
            "eggs",
            "butter"
        )

        ingredients.forEach { line ->
            val isIngredient = method.invoke(TextRecipeParser, line) as Boolean
            assertTrue("Should accept food word: $line", isIngredient)
        }
    }

    @Test
    fun `looksLikeIngredient rejects very short lines`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "looksLikeIngredient",
            String::class.java
        )
        method.isAccessible = true

        val shortLines = listOf("a", "on", "")

        shortLines.forEach { line ->
            val isIngredient = method.invoke(TextRecipeParser, line) as Boolean
            assertFalse("Should reject short line: '$line'", isIngredient)
        }
    }

    @Test
    fun `looksLikeIngredient rejects website navigation`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "looksLikeIngredient",
            String::class.java
        )
        method.isAccessible = true

        val nonIngredients = listOf(
            "Rate this recipe",
            "Leave a comment",
            "Subscribe now"
        )

        nonIngredients.forEach { line ->
            val isIngredient = method.invoke(TextRecipeParser, line) as Boolean
            assertFalse("Should reject navigation: $line", isIngredient)
        }
    }

    // ========== Instruction Validation Tests ==========

    @Test
    fun `looksLikeInstruction accepts lines with cooking verbs`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "looksLikeInstruction",
            String::class.java
        )
        method.isAccessible = true

        val instructions = listOf(
            "Preheat oven to 350 degrees",
            "Mix the dry ingredients together",
            "Bake for 30 minutes until golden",
            "Stir frequently to prevent burning",
            "Serve hot with rice"
        )

        instructions.forEach { line ->
            val isInstruction = method.invoke(TextRecipeParser, line) as Boolean
            assertTrue("Should accept instruction: $line", isInstruction)
        }
    }

    @Test
    fun `looksLikeInstruction accepts lines with temperature or time`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "looksLikeInstruction",
            String::class.java
        )
        method.isAccessible = true

        val instructions = listOf(
            "Set temperature to 350°F",
            "Cook for 20 minutes",
            "Let rest for 5 min",
            "Refrigerate for 1 hour"
        )

        instructions.forEach { line ->
            val isInstruction = method.invoke(TextRecipeParser, line) as Boolean
            assertTrue("Should accept temp/time: $line", isInstruction)
        }
    }

    @Test
    fun `looksLikeInstruction rejects very short lines`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "looksLikeInstruction",
            String::class.java
        )
        method.isAccessible = true

        val shortLines = listOf("Mix", "Bake", "Stir", "")

        shortLines.forEach { line ->
            val isInstruction = method.invoke(TextRecipeParser, line) as Boolean
            assertFalse("Should reject short line: '$line'", isInstruction)
        }
    }

    @Test
    fun `looksLikeInstruction rejects footer patterns`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "looksLikeInstruction",
            String::class.java
        )
        method.isAccessible = true

        val footerLines = listOf(
            "Rate this recipe and let us know what you think",
            "Leave a comment below to help our business",
            "Subscribe to our newsletter for more recipes",
            "Visit our website for cooking tips"
        )

        footerLines.forEach { line ->
            val isInstruction = method.invoke(TextRecipeParser, line) as Boolean
            assertFalse("Should reject footer: $line", isInstruction)
        }
    }

    // ========== Time Parsing Tests ==========

    @Test
    fun `parseTimeString handles minutes only`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "parseTimeString",
            String::class.java
        )
        method.isAccessible = true

        val testCases = listOf(
            "30 minutes" to 30,
            "45 min" to 45,
            "15m" to 15,
            "Prep time: 20 minutes" to 20
        )

        testCases.forEach { (input, expected) ->
            val result = method.invoke(TextRecipeParser, input) as Int?
            assertEquals("Should parse minutes from: $input", expected, result)
        }
    }

    @Test
    fun `parseTimeString handles hours only`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "parseTimeString",
            String::class.java
        )
        method.isAccessible = true

        val testCases = listOf(
            "1 hour" to 60,
            "2 hours" to 120,
            "3 hr" to 180,
            "Cook time: 1 hour" to 60
        )

        testCases.forEach { (input, expected) ->
            val result = method.invoke(TextRecipeParser, input) as Int?
            assertEquals("Should parse hours from: $input", expected, result)
        }
    }

    @Test
    fun `parseTimeString handles hours and minutes`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "parseTimeString",
            String::class.java
        )
        method.isAccessible = true

        val testCases = listOf(
            "1 hour 30 minutes" to 90,
            "2 hours 15 min" to 135,
            "1 hr 45 minutes" to 105
        )

        testCases.forEach { (input, expected) ->
            val result = method.invoke(TextRecipeParser, input) as Int?
            assertEquals("Should parse hours+minutes from: $input", expected, result)
        }
    }

    @Test
    fun `parseTimeString returns null for no time`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "parseTimeString",
            String::class.java
        )
        method.isAccessible = true

        val invalidStrings = listOf(
            "No time specified",
            "Quick recipe",
            ""
        )

        invalidStrings.forEach { input ->
            val result = method.invoke(TextRecipeParser, input) as Int?
            assertNull("Should return null for: $input", result)
        }
    }

    // ========== Cleaning Tests ==========

    @Test
    fun `cleanIngredient removes bullets and numbering`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "cleanIngredient",
            String::class.java
        )
        method.isAccessible = true

        val testCases = listOf(
            "• 2 cups flour" to "2 cups flour",
            "- 1 cup sugar" to "1 cup sugar",
            "* 3 eggs" to "3 eggs",
            "1. 2 cups flour" to "2 cups flour",
            "2 1/2 cups milk" to "1/2 cups milk"
        )

        testCases.forEach { (input, expected) ->
            val result = method.invoke(TextRecipeParser, input) as String
            assertEquals("Should clean: $input", expected, result)
        }
    }

    @Test
    fun `cleanInstruction removes step numbers`() {
        val method = TextRecipeParser.javaClass.getDeclaredMethod(
            "cleanInstruction",
            String::class.java
        )
        method.isAccessible = true

        val testCases = listOf(
            "Step 1: Preheat oven" to "Preheat oven",
            "Step 2 Mix ingredients" to "Mix ingredients",
            "1. Combine flour and sugar" to "Combine flour and sugar",
            "2 Bake for 30 minutes" to "Bake for 30 minutes"
        )

        testCases.forEach { (input, expected) ->
            val result = method.invoke(TextRecipeParser, input) as String
            assertEquals("Should clean: $input", expected, result)
        }
    }

    // ========== Integration Tests ==========

    @Test
    fun `parseText succeeds with well-formed recipe`() {
        val text = """
            Chocolate Chip Cookies

            Servings: 24 cookies
            Prep time: 15 minutes
            Cook time: 12 minutes

            Ingredients:
            2 1/4 cups all-purpose flour
            1 teaspoon baking soda
            1 cup butter, softened
            3/4 cup sugar
            2 large eggs
            2 cups chocolate chips

            Instructions:
            Preheat oven to 375°F.
            Mix flour and baking soda in a bowl.
            Beat butter and sugar until creamy.
            Add eggs one at a time.
            Stir in chocolate chips.
            Bake for 9-12 minutes until golden.

            Tags: dessert, cookies, chocolate
        """.trimIndent()

        val result = TextRecipeParser.parseText(text, RecipeSource.PDF)

        assertTrue("Should succeed parsing", result.isSuccess)

        val recipe = result.getOrNull()!!
        assertEquals("Should extract title", "Chocolate Chip Cookies", recipe.title)
        assertEquals("Should extract 6 ingredients", 6, recipe.ingredients.size)
        assertEquals("Should extract 6 instructions", 6, recipe.instructions.size)
        assertEquals("Should extract servings", 24, recipe.servings)
        assertEquals("Should extract prep time", 15, recipe.prepTimeMinutes)
        assertEquals("Should extract cook time", 12, recipe.cookTimeMinutes)
        assertEquals("Should extract 3 tags", 3, recipe.tags.size)
        assertTrue("Should have dessert tag", recipe.tags.contains("dessert"))
    }

    @Test
    fun `parseText filters website noise from ingredients`() {
        val text = """
            Test Recipe

            Ingredients:
            2 cups flour
            Save these ingredients to your list
            1 cup sugar
            Shop ingredients now
            3 eggs

            Instructions:
            Mix everything together
        """.trimIndent()

        val result = TextRecipeParser.parseText(text, RecipeSource.PDF)
        val recipe = result.getOrNull()!!

        assertEquals("Should have 3 ingredients (no CTA lines)", 3, recipe.ingredients.size)
        assertFalse("Should not include 'Save' line", recipe.ingredients.any { it.contains("Save") })
        assertFalse("Should not include 'Shop' line", recipe.ingredients.any { it.contains("Shop") })
    }

    @Test
    fun `parseText filters website noise from instructions`() {
        val text = """
            Test Recipe

            Ingredients:
            2 cups flour

            Instructions:
            Preheat oven to 350°F
            Rate this recipe if you enjoyed it
            Mix the ingredients well
            Subscribe to our newsletter for more
            Bake for 30 minutes
        """.trimIndent()

        val result = TextRecipeParser.parseText(text, RecipeSource.PDF)
        val recipe = result.getOrNull()!!

        assertEquals("Should have 3 instructions (no footer lines)", 3, recipe.instructions.size)
        assertFalse("Should not include 'Rate' line", recipe.instructions.any { it.contains("Rate") })
        assertFalse("Should not include 'Subscribe' line", recipe.instructions.any { it.contains("Subscribe") })
    }

    @Test
    fun `parseText fails with empty text`() {
        val result = TextRecipeParser.parseText("", RecipeSource.PDF)

        assertTrue("Should fail with empty text", result.isFailure)
        assertTrue(
            "Should have meaningful error message",
            result.exceptionOrNull()?.message?.contains("No text found") == true
        )
    }

    @Test
    fun `parseText fails with whitespace only`() {
        val result = TextRecipeParser.parseText("   \n\n   \n  ", RecipeSource.PDF)

        assertTrue("Should fail with whitespace only", result.isFailure)
    }

    @Test
    fun `parseText sets correct source`() {
        val textPdf = "Ingredients:\n2 cups flour\nInstructions:\nMix well"
        val textPhoto = "Ingredients:\n1 cup sugar\nInstructions:\nBake"

        val pdfResult = TextRecipeParser.parseText(textPdf, RecipeSource.PDF)
        val photoResult = TextRecipeParser.parseText(textPhoto, RecipeSource.PHOTO)

        assertEquals("Should set PDF source", RecipeSource.PDF, pdfResult.getOrNull()?.source)
        assertEquals("Should set PHOTO source", RecipeSource.PHOTO, photoResult.getOrNull()?.source)
    }

    @Test
    fun `parseText uses source identifier as sourceUrl`() {
        val text = "Ingredients:\n2 cups flour\nInstructions:\nMix"
        val identifier = "/path/to/recipe.pdf"

        val result = TextRecipeParser.parseText(text, RecipeSource.PDF, identifier)

        assertEquals("Should set source identifier as sourceUrl", identifier, result.getOrNull()?.sourceUrl)
    }

    @Test
    fun `parseText defaults to 4 servings when not specified`() {
        val text = """
            Test Recipe
            Ingredients:
            2 cups flour
            Instructions:
            Mix and bake
        """.trimIndent()

        val result = TextRecipeParser.parseText(text, RecipeSource.PDF)

        assertEquals("Should default to 4 servings", 4, result.getOrNull()?.servings)
    }

    @Test
    fun `parseText uses first line as title when no ingredients section`() {
        val text = """
            My Amazing Recipe
            Mix 2 cups flour with 1 cup sugar
            Bake at 350 for 30 minutes
        """.trimIndent()

        val result = TextRecipeParser.parseText(text, RecipeSource.PDF)

        assertEquals("Should use first line as title", "My Amazing Recipe", result.getOrNull()?.title)
    }
}
