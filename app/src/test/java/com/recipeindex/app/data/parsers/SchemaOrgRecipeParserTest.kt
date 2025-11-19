package com.recipeindex.app.data.parsers

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import org.jsoup.Jsoup
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SchemaOrgRecipeParser
 *
 * Focus: JSON-LD parsing, ISO 8601 durations, HowToStep/HowToSection handling
 */
class SchemaOrgRecipeParserTest {

    // ========== ISO 8601 Duration Parsing Tests ==========

    @Test
    fun `parseIsoDuration handles minutes only`() {
        val parser = createParser()

        val testCases = listOf(
            "PT30M" to 30,
            "PT15M" to 15,
            "PT45M" to 45
        )

        testCases.forEach { (input, expected) ->
            val result = callParseIsoDuration(parser, input)
            assertEquals("Should parse minutes from: $input", expected, result)
        }
    }

    @Test
    fun `parseIsoDuration handles hours only`() {
        val parser = createParser()

        val testCases = listOf(
            "PT1H" to 60,
            "PT2H" to 120,
            "PT3H" to 180
        )

        testCases.forEach { (input, expected) ->
            val result = callParseIsoDuration(parser, input)
            assertEquals("Should parse hours from: $input", expected, result)
        }
    }

    @Test
    fun `parseIsoDuration handles hours and minutes`() {
        val parser = createParser()

        val testCases = listOf(
            "PT1H30M" to 90,
            "PT2H15M" to 135,
            "PT1H45M" to 105
        )

        testCases.forEach { (input, expected) ->
            val result = callParseIsoDuration(parser, input)
            assertEquals("Should parse hours+minutes from: $input", expected, result)
        }
    }

    @Test
    fun `parseIsoDuration returns null for empty or invalid`() {
        val parser = createParser()

        val invalidInputs = listOf("", null, "PT0M", "invalid")

        invalidInputs.forEach { input ->
            val result = callParseIsoDuration(parser, input)
            assertNull("Should return null for: $input", result)
        }
    }

    // ========== Servings Parsing Tests ==========

    @Test
    fun `parseServings extracts number from string`() {
        val parser = createParser()

        val testCases = listOf(
            "4" to 4,
            "4 servings" to 4,
            "Makes 6" to 6,
            "4-6 servings" to 4 // Extracts first number
        )

        testCases.forEach { (input, expected) ->
            val jsonElement = JsonPrimitive(input)
            val result = callParseServings(parser, jsonElement)
            assertEquals("Should parse servings from: $input", expected, result)
        }
    }

    @Test
    fun `parseServings returns null for invalid input`() {
        val parser = createParser()

        val result = callParseServings(parser, JsonNull)
        assertNull("Should return null for JsonNull", result)
    }

    // ========== Image Parsing Tests ==========

    @Test
    fun `parseImage handles string URL`() {
        val parser = createParser()

        val url = "https://example.com/recipe.jpg"
        val jsonElement = JsonPrimitive(url)

        val result = callParseImage(parser, jsonElement)
        assertEquals("Should extract URL from string", url, result)
    }

    @Test
    fun `parseImage handles ImageObject with url field`() {
        val parser = createParser()

        val json = buildJsonObject {
            put("@type", "ImageObject")
            put("url", "https://example.com/image.jpg")
        }

        val result = callParseImage(parser, json)
        assertEquals("Should extract URL from ImageObject", "https://example.com/image.jpg", result)
    }

    @Test
    fun `parseImage handles array of URLs`() {
        val parser = createParser()

        val json = buildJsonArray {
            add("https://example.com/image1.jpg")
            add("https://example.com/image2.jpg")
        }

        val result = callParseImage(parser, json)
        assertEquals("Should extract first URL from array", "https://example.com/image1.jpg", result)
    }

    @Test
    fun `parseImage returns null for invalid input`() {
        val parser = createParser()

        val result = callParseImage(parser, JsonNull)
        assertNull("Should return null for JsonNull", result)
    }

    // ========== Instructions Parsing Tests ==========

    @Test
    fun `parseInstructions handles array of strings`() {
        val parser = createParser()

        val json = buildJsonArray {
            add("Preheat oven to 350°F")
            add("Mix ingredients")
            add("Bake for 30 minutes")
        }

        val result = callParseInstructions(parser, json)

        assertEquals("Should extract 3 instructions", 3, result.size)
        assertEquals("Preheat oven to 350°F", result[0])
        assertEquals("Mix ingredients", result[1])
        assertEquals("Bake for 30 minutes", result[2])
    }

    @Test
    fun `parseInstructions handles HowToStep objects`() {
        val parser = createParser()

        val json = buildJsonArray {
            addJsonObject {
                put("@type", "HowToStep")
                put("text", "Preheat oven to 350°F")
            }
            addJsonObject {
                put("@type", "HowToStep")
                put("text", "Mix ingredients well")
            }
        }

        val result = callParseInstructions(parser, json)

        assertEquals("Should extract 2 HowToStep instructions", 2, result.size)
        assertEquals("Preheat oven to 350°F", result[0])
        assertEquals("Mix ingredients well", result[1])
    }

    @Test
    fun `parseInstructions handles HowToStep with name field`() {
        val parser = createParser()

        val json = buildJsonArray {
            addJsonObject {
                put("@type", "HowToStep")
                put("name", "Preheat")
            }
        }

        val result = callParseInstructions(parser, json)

        assertEquals("Should use name field as fallback", 1, result.size)
        assertEquals("Preheat", result[0])
    }

    @Test
    fun `parseInstructions handles HowToSection with steps`() {
        val parser = createParser()

        val json = buildJsonArray {
            addJsonObject {
                put("@type", "HowToSection")
                put("name", "For the dough")
                putJsonArray("itemListElement") {
                    addJsonObject {
                        put("@type", "HowToStep")
                        put("text", "Mix flour and water")
                    }
                    addJsonObject {
                        put("@type", "HowToStep")
                        put("text", "Knead for 10 minutes")
                    }
                }
            }
            addJsonObject {
                put("@type", "HowToSection")
                put("name", "For the topping")
                putJsonArray("itemListElement") {
                    addJsonObject {
                        put("@type", "HowToStep")
                        put("text", "Spread sauce")
                    }
                }
            }
        }

        val result = callParseInstructions(parser, json)

        assertEquals("Should extract section headers and steps", 5, result.size)
        assertEquals("For the dough:", result[0])
        assertEquals("Mix flour and water", result[1])
        assertEquals("Knead for 10 minutes", result[2])
        assertEquals("For the topping:", result[3])
        assertEquals("Spread sauce", result[4])
    }

    @Test
    fun `parseInstructions handles single string`() {
        val parser = createParser()

        val json = JsonPrimitive("Mix everything and bake")

        val result = callParseInstructions(parser, json)

        assertEquals("Should handle single string", 1, result.size)
        assertEquals("Mix everything and bake", result[0])
    }

    @Test
    fun `parseInstructions filters blank instructions`() {
        val parser = createParser()

        val json = buildJsonArray {
            add("Valid instruction")
            add("")
            add("   ")
            add("Another valid instruction")
        }

        val result = callParseInstructions(parser, json)

        assertEquals("Should filter blank instructions", 2, result.size)
        assertEquals("Valid instruction", result[0])
        assertEquals("Another valid instruction", result[1])
    }

    // ========== JSON Array to Strings Tests ==========

    @Test
    fun `parseJsonArrayToStrings handles array of strings`() {
        val parser = createParser()

        val json = buildJsonArray {
            add("dessert")
            add("cookies")
            add("chocolate")
        }

        val result = callParseJsonArrayToStrings(parser, json)

        assertEquals("Should extract 3 tags", 3, result.size)
        assertTrue("Should contain dessert", result.contains("dessert"))
        assertTrue("Should contain cookies", result.contains("cookies"))
        assertTrue("Should contain chocolate", result.contains("chocolate"))
    }

    @Test
    fun `parseJsonArrayToStrings handles comma-separated string`() {
        val parser = createParser()

        val json = JsonPrimitive("dessert, cookies, chocolate")

        val result = callParseJsonArrayToStrings(parser, json)

        assertEquals("Should split and extract 3 tags", 3, result.size)
        assertEquals("dessert", result[0])
        assertEquals("cookies", result[1])
        assertEquals("chocolate", result[2])
    }

    @Test
    fun `parseJsonArrayToStrings filters blank values`() {
        val parser = createParser()

        val json = buildJsonArray {
            add("valid")
            add("")
            add("   ")
            add("another")
        }

        val result = callParseJsonArrayToStrings(parser, json)

        assertEquals("Should filter blanks", 2, result.size)
        assertEquals("valid", result[0])
        assertEquals("another", result[1])
    }

    // ========== Schema.org Parsing Integration Tests ==========

    @Test
    fun `parse extracts recipe from Schema org JSON-LD`() = runTest {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@context": "https://schema.org",
                    "@type": "Recipe",
                    "name": "Chocolate Chip Cookies",
                    "description": "Delicious homemade cookies",
                    "recipeIngredient": ["2 cups flour", "1 cup sugar", "2 eggs"],
                    "recipeInstructions": [
                        {
                            "@type": "HowToStep",
                            "text": "Preheat oven to 350°F"
                        },
                        {
                            "@type": "HowToStep",
                            "text": "Mix ingredients"
                        }
                    ],
                    "recipeYield": "24 cookies",
                    "prepTime": "PT15M",
                    "cookTime": "PT12M",
                    "recipeCategory": "dessert",
                    "recipeCuisine": "American",
                    "image": "https://example.com/cookies.jpg"
                }
                </script>
            </head>
            <body>Recipe content</body>
            </html>
        """.trimIndent()

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(html),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/html")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val parser = SchemaOrgRecipeParser(httpClient)
        val result = parser.parse("https://example.com/recipe")

        assertTrue("Should succeed parsing", result.isSuccess)

        val recipe = result.getOrNull()!!
        assertEquals("Should extract title", "Chocolate Chip Cookies", recipe.title)
        assertEquals("Should extract 3 ingredients", 3, recipe.ingredients.size)
        assertEquals("Should extract 2 instructions", 2, recipe.instructions.size)
        assertEquals("Should extract servings", 24, recipe.servings)
        assertEquals("Should extract prep time", 15, recipe.prepTimeMinutes)
        assertEquals("Should extract cook time", 12, recipe.cookTimeMinutes)
        assertTrue("Should have dessert tag", recipe.tags.contains("dessert"))
        assertTrue("Should have American tag", recipe.tags.contains("American"))
        assertEquals("Should extract image", "https://example.com/cookies.jpg", recipe.photoPath)
        assertEquals("Should extract description", "Delicious homemade cookies", recipe.notes)
    }

    @Test
    fun `parse handles @graph array with Recipe`() = runTest {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@context": "https://schema.org",
                    "@graph": [
                        {
                            "@type": "WebSite",
                            "name": "Recipe Site"
                        },
                        {
                            "@type": "Recipe",
                            "name": "Test Recipe",
                            "recipeIngredient": ["1 cup flour"],
                            "recipeInstructions": "Mix and bake"
                        }
                    ]
                }
                </script>
            </head>
            <body>Content</body>
            </html>
        """.trimIndent()

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(html),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/html")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val parser = SchemaOrgRecipeParser(httpClient)
        val result = parser.parse("https://example.com/recipe")

        assertTrue("Should find Recipe in @graph", result.isSuccess)
        assertEquals("Test Recipe", result.getOrNull()?.title)
    }

    @Test
    fun `parse falls back to Open Graph when no Schema org`() = runTest {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta property="og:title" content="Open Graph Recipe" />
                <meta property="og:description" content="A test recipe" />
                <meta property="og:image" content="https://example.com/og-image.jpg" />
            </head>
            <body>Content</body>
            </html>
        """.trimIndent()

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(html),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/html")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val parser = SchemaOrgRecipeParser(httpClient)
        val result = parser.parse("https://example.com/recipe")

        assertTrue("Should succeed with Open Graph fallback", result.isSuccess)

        val recipe = result.getOrNull()!!
        assertEquals("Should use og:title", "Open Graph Recipe", recipe.title)
        assertEquals("Should use og:description as notes", "A test recipe", recipe.notes)
        assertEquals("Should use og:image", "https://example.com/og-image.jpg", recipe.photoPath)
    }

    @Test
    fun `parse fails when no recipe data found`() = runTest {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Not a recipe</title>
            </head>
            <body>Just a regular page</body>
            </html>
        """.trimIndent()

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(html),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/html")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val parser = SchemaOrgRecipeParser(httpClient)
        val result = parser.parse("https://example.com/not-recipe")

        assertTrue("Should fail when no recipe data", result.isFailure)
        assertTrue(
            "Should have meaningful error",
            result.exceptionOrNull()?.message?.contains("No recipe data found") == true
        )
    }

    @Test
    fun `parse defaults to 4 servings when not specified`() = runTest {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <script type="application/ld+json">
                {
                    "@type": "Recipe",
                    "name": "Test Recipe",
                    "recipeIngredient": ["1 cup flour"],
                    "recipeInstructions": "Mix"
                }
                </script>
            </head>
            </html>
        """.trimIndent()

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(html),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/html")
            )
        }

        val httpClient = HttpClient(mockEngine)
        val parser = SchemaOrgRecipeParser(httpClient)
        val result = parser.parse("https://example.com/recipe")

        assertEquals("Should default to 4 servings", 4, result.getOrNull()?.servings)
    }

    // ========== Helper Methods ==========

    private fun createParser(): SchemaOrgRecipeParser {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.OK
            )
        }
        val httpClient = HttpClient(mockEngine)
        return SchemaOrgRecipeParser(httpClient)
    }

    private fun callParseIsoDuration(parser: SchemaOrgRecipeParser, duration: String?): Int? {
        val method = parser.javaClass.getDeclaredMethod(
            "parseIsoDuration",
            String::class.java
        )
        method.isAccessible = true
        return method.invoke(parser, duration) as Int?
    }

    private fun callParseServings(parser: SchemaOrgRecipeParser, element: JsonElement?): Int? {
        val method = parser.javaClass.getDeclaredMethod(
            "parseServings",
            JsonElement::class.java
        )
        method.isAccessible = true
        return method.invoke(parser, element) as Int?
    }

    private fun callParseImage(parser: SchemaOrgRecipeParser, element: JsonElement?): String? {
        val method = parser.javaClass.getDeclaredMethod(
            "parseImage",
            JsonElement::class.java
        )
        method.isAccessible = true
        return method.invoke(parser, element) as String?
    }

    @Suppress("UNCHECKED_CAST")
    private fun callParseInstructions(parser: SchemaOrgRecipeParser, element: JsonElement?): List<String> {
        val method = parser.javaClass.getDeclaredMethod(
            "parseInstructions",
            JsonElement::class.java
        )
        method.isAccessible = true
        return method.invoke(parser, element) as List<String>
    }

    @Suppress("UNCHECKED_CAST")
    private fun callParseJsonArrayToStrings(parser: SchemaOrgRecipeParser, element: JsonElement?): List<String> {
        val method = parser.javaClass.getDeclaredMethod(
            "parseJsonArrayToStrings",
            JsonElement::class.java
        )
        method.isAccessible = true
        return method.invoke(parser, element) as List<String>
    }
}
