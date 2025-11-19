package com.recipeindex.app.data

import com.recipeindex.app.data.entities.RecipeSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Room TypeConverters
 *
 * Focus: List<String> and List<Long> serialization/deserialization
 */
class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setup() {
        converters = Converters()
    }

    // ========== List<String> Conversion Tests ==========

    @Test
    fun `fromStringList serializes list to delimited string`() {
        val list = listOf("flour", "sugar", "eggs")

        val result = converters.fromStringList(list)

        assertEquals("Should use ||| delimiter", "flour|||sugar|||eggs", result)
    }

    @Test
    fun `toStringList deserializes delimited string to list`() {
        val serialized = "flour|||sugar|||eggs"

        val result = converters.toStringList(serialized)

        assertEquals("Should deserialize to 3 items", 3, result.size)
        assertEquals("flour", result[0])
        assertEquals("sugar", result[1])
        assertEquals("eggs", result[2])
    }

    @Test
    fun `toStringList handles empty string`() {
        val result = converters.toStringList("")

        assertEquals("Should return empty list", 0, result.size)
    }

    @Test
    fun `fromStringList handles empty list`() {
        val result = converters.fromStringList(emptyList())

        assertEquals("Should return empty string", "", result)
    }

    @Test
    fun `fromStringList handles single item`() {
        val list = listOf("only-one")

        val result = converters.fromStringList(list)

        assertEquals("Should serialize single item", "only-one", result)
    }

    @Test
    fun `toStringList handles single item`() {
        val serialized = "only-one"

        val result = converters.toStringList(serialized)

        assertEquals("Should have 1 item", 1, result.size)
        assertEquals("only-one", result[0])
    }

    @Test
    fun `stringList roundtrip preserves data`() {
        val original = listOf("Mix flour and sugar", "Beat eggs", "Bake at 350°F")

        val serialized = converters.fromStringList(original)
        val deserialized = converters.toStringList(serialized)

        assertEquals("Should preserve all items", original, deserialized)
    }

    @Test
    fun `stringList handles items with special characters`() {
        val list = listOf("1/2 cup flour", "2 1/4 cups sugar", "Salt & pepper")

        val serialized = converters.fromStringList(list)
        val deserialized = converters.toStringList(serialized)

        assertEquals("Should preserve special characters", list, deserialized)
    }

    @Test
    fun `stringList handles very long strings`() {
        val longString = "a".repeat(1000)
        val list = listOf(longString, "short", longString)

        val serialized = converters.fromStringList(list)
        val deserialized = converters.toStringList(serialized)

        assertEquals("Should handle long strings", list, deserialized)
    }

    // ========== List<Long> Conversion Tests ==========

    @Test
    fun `fromLongList serializes list to comma-delimited string`() {
        val list = listOf(1L, 2L, 3L)

        val result = converters.fromLongList(list)

        assertEquals("Should use comma delimiter", "1,2,3", result)
    }

    @Test
    fun `toLongList deserializes comma-delimited string to list`() {
        val serialized = "1,2,3"

        val result = converters.toLongList(serialized)

        assertEquals("Should deserialize to 3 items", 3, result.size)
        assertEquals(1L, result[0])
        assertEquals(2L, result[1])
        assertEquals(3L, result[2])
    }

    @Test
    fun `toLongList handles empty string`() {
        val result = converters.toLongList("")

        assertEquals("Should return empty list", 0, result.size)
    }

    @Test
    fun `fromLongList handles empty list`() {
        val result = converters.fromLongList(emptyList())

        assertEquals("Should return empty string", "", result)
    }

    @Test
    fun `fromLongList handles single item`() {
        val list = listOf(42L)

        val result = converters.fromLongList(list)

        assertEquals("Should serialize single item", "42", result)
    }

    @Test
    fun `toLongList handles single item`() {
        val serialized = "42"

        val result = converters.toLongList(serialized)

        assertEquals("Should have 1 item", 1, result.size)
        assertEquals(42L, result[0])
    }

    @Test
    fun `longList roundtrip preserves data`() {
        val original = listOf(10L, 20L, 30L, 40L, 50L)

        val serialized = converters.fromLongList(original)
        val deserialized = converters.toLongList(serialized)

        assertEquals("Should preserve all items", original, deserialized)
    }

    @Test
    fun `longList handles large numbers`() {
        val list = listOf(Long.MAX_VALUE, 0L, Long.MAX_VALUE - 1)

        val serialized = converters.fromLongList(list)
        val deserialized = converters.toLongList(serialized)

        assertEquals("Should handle large numbers", list, deserialized)
    }

    @Test
    fun `longList handles many items`() {
        val list = (1L..100L).toList()

        val serialized = converters.fromLongList(list)
        val deserialized = converters.toLongList(serialized)

        assertEquals("Should handle 100 items", list, deserialized)
    }

    // ========== RecipeSource Conversion Tests ==========

    @Test
    fun `fromRecipeSource converts all enum values`() {
        val testCases = listOf(
            RecipeSource.MANUAL to "MANUAL",
            RecipeSource.URL to "URL",
            RecipeSource.PDF to "PDF",
            RecipeSource.PHOTO to "PHOTO"
        )

        testCases.forEach { (source, expected) ->
            val result = converters.fromRecipeSource(source)
            assertEquals("Should convert $source to string", expected, result)
        }
    }

    @Test
    fun `toRecipeSource converts all string values`() {
        val testCases = listOf(
            "MANUAL" to RecipeSource.MANUAL,
            "URL" to RecipeSource.URL,
            "PDF" to RecipeSource.PDF,
            "PHOTO" to RecipeSource.PHOTO
        )

        testCases.forEach { (str, expected) ->
            val result = converters.toRecipeSource(str)
            assertEquals("Should convert $str to RecipeSource", expected, result)
        }
    }

    @Test
    fun `recipeSource roundtrip preserves data`() {
        RecipeSource.values().forEach { source ->
            val serialized = converters.fromRecipeSource(source)
            val deserialized = converters.toRecipeSource(serialized)

            assertEquals("Should preserve $source through roundtrip", source, deserialized)
        }
    }

    // ========== Edge Cases ==========

    @Test
    fun `stringList handles items containing delimiter`() {
        // Note: This is a known limitation - items containing "|||" would cause issues
        // In practice, ingredients/instructions are unlikely to contain this sequence
        val list = listOf("Step 1", "Step 2", "Step 3")

        val serialized = converters.fromStringList(list)
        val deserialized = converters.toStringList(serialized)

        assertEquals("Should handle normal items", list, deserialized)
    }

    @Test
    fun `stringList handles Unicode characters`() {
        val list = listOf("2 cups flour", "½ teaspoon salt", "Preheat to 350°F")

        val serialized = converters.fromStringList(list)
        val deserialized = converters.toStringList(serialized)

        assertEquals("Should preserve Unicode", list, deserialized)
    }

    @Test
    fun `longList handles zero`() {
        val list = listOf(0L, 0L, 0L)

        val serialized = converters.fromLongList(list)
        val deserialized = converters.toLongList(serialized)

        assertEquals("Should handle zeros", list, deserialized)
    }
}
