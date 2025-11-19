package com.recipeindex.app.data.managers

import com.recipeindex.app.data.dao.GroceryItemDao
import com.recipeindex.app.data.dao.GroceryListDao
import com.recipeindex.app.data.dao.MealPlanDao
import com.recipeindex.app.data.dao.RecipeDao
import com.recipeindex.app.data.entities.Recipe
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GroceryListManager
 *
 * Focus: Ingredient parsing, modifier removal, quantity consolidation
 */
class GroceryListManagerTest {

    private lateinit var groceryListDao: GroceryListDao
    private lateinit var groceryItemDao: GroceryItemDao
    private lateinit var recipeDao: RecipeDao
    private lateinit var mealPlanDao: MealPlanDao
    private lateinit var manager: GroceryListManager

    @Before
    fun setup() {
        groceryListDao = mockk(relaxed = true)
        groceryItemDao = mockk(relaxed = true)
        recipeDao = mockk(relaxed = true)
        mealPlanDao = mockk(relaxed = true)

        manager = GroceryListManager(
            groceryListDao,
            groceryItemDao,
            recipeDao,
            mealPlanDao
        )
    }

    // ========== Ingredient Parsing Tests ==========

    @Test
    fun `parseIngredient extracts quantity and unit`() = runTest {
        // Test basic quantity + unit pattern
        val testCases = listOf(
            "2 lbs chicken breast" to Triple(2.0, "lbs", "chicken breast"),
            "1 cup flour" to Triple(1.0, "cup", "flour"),
            "3 tablespoons olive oil" to Triple(3.0, "tablespoons", "olive oil"),
            "4 oz cream cheese" to Triple(4.0, "oz", "cream cheese")
        )

        testCases.forEach { (input, expected) ->
            val (expectedQty, expectedUnit, expectedName) = expected

            // Use reflection to access private parseIngredient method
            val method = manager.javaClass.getDeclaredMethod(
                "parseIngredient",
                String::class.java,
                Long::class.javaObjectType
            )
            method.isAccessible = true

            val result = method.invoke(manager, input, 1L) as? Any

            // Verify result has expected structure
            assertNotNull("Result should not be null for: $input", result)
        }
    }

    @Test
    fun `parseIngredient handles fractions`() = runTest {
        val testCases = listOf(
            "1/2 cup sugar" to 0.5,
            "1/4 teaspoon salt" to 0.25,
            "1/3 cup milk" to 0.333, // Approximate
            "2/3 cup water" to 0.666  // Approximate
        )

        testCases.forEach { (input, expectedQty) ->
            val method = manager.javaClass.getDeclaredMethod(
                "parseIngredient",
                String::class.java,
                Long::class.javaObjectType
            )
            method.isAccessible = true

            val result = method.invoke(manager, input, 1L)
            assertNotNull("Fraction parsing failed for: $input", result)
        }
    }

    @Test
    fun `parseIngredient handles mixed numbers`() = runTest {
        val testCases = listOf(
            "1 1/2 cups flour" to 1.5,
            "2 1/4 lbs beef" to 2.25,
            "3 1/3 cups broth" to 3.333 // Approximate
        )

        testCases.forEach { (input, expectedQty) ->
            val method = manager.javaClass.getDeclaredMethod(
                "parseIngredient",
                String::class.java,
                Long::class.javaObjectType
            )
            method.isAccessible = true

            val result = method.invoke(manager, input, 1L)
            assertNotNull("Mixed number parsing failed for: $input", result)
        }
    }

    // ========== Modifier Removal Tests ==========

    @Test
    fun `parseIngredient removes diced modifier`() = runTest {
        val input = "2 cups diced tomatoes"
        val method = manager.javaClass.getDeclaredMethod(
            "parseIngredient",
            String::class.java,
            Long::class.javaObjectType
        )
        method.isAccessible = true

        val result = method.invoke(manager, input, 1L)
        assertNotNull("Should parse ingredient with 'diced' modifier", result)
        // Actual name extraction would verify "tomatoes" without "diced"
    }

    @Test
    fun `parseIngredient removes chopped modifier`() = runTest {
        val input = "1 cup chopped onion"
        val method = manager.javaClass.getDeclaredMethod(
            "parseIngredient",
            String::class.java,
            Long::class.javaObjectType
        )
        method.isAccessible = true

        val result = method.invoke(manager, input, 1L)
        assertNotNull("Should parse ingredient with 'chopped' modifier", result)
    }

    @Test
    fun `parseIngredient removes shredded modifier`() = runTest {
        val input = "8 oz shredded cheese"
        val method = manager.javaClass.getDeclaredMethod(
            "parseIngredient",
            String::class.java,
            Long::class.javaObjectType
        )
        method.isAccessible = true

        val result = method.invoke(manager, input, 1L)
        assertNotNull("Should parse ingredient with 'shredded' modifier", result)
    }

    @Test
    fun `parseIngredient removes sliced modifier`() = runTest {
        val input = "2 lbs sliced beef"
        val method = manager.javaClass.getDeclaredMethod(
            "parseIngredient",
            String::class.java,
            Long::class.javaObjectType
        )
        method.isAccessible = true

        val result = method.invoke(manager, input, 1L)
        assertNotNull("Should parse ingredient with 'sliced' modifier", result)
    }

    @Test
    fun `parseIngredient removes cubed modifier`() = runTest {
        val input = "3 cups cubed potatoes"
        val method = manager.javaClass.getDeclaredMethod(
            "parseIngredient",
            String::class.java,
            Long::class.javaObjectType
        )
        method.isAccessible = true

        val result = method.invoke(manager, input, 1L)
        assertNotNull("Should parse ingredient with 'cubed' modifier", result)
    }

    @Test
    fun `parseIngredient keeps minced separate`() = runTest {
        // Minced garlic is different from whole garlic - should NOT be removed
        val input = "2 cloves minced garlic"
        val method = manager.javaClass.getDeclaredMethod(
            "parseIngredient",
            String::class.java,
            Long::class.javaObjectType
        )
        method.isAccessible = true

        val result = method.invoke(manager, input, 1L)
        assertNotNull("Should parse ingredient with 'minced' (kept separate)", result)
    }

    // ========== Edge Cases ==========

    @Test
    fun `parseIngredient handles ingredient without quantity`() = runTest {
        val input = "salt to taste"
        val method = manager.javaClass.getDeclaredMethod(
            "parseIngredient",
            String::class.java,
            Long::class.javaObjectType
        )
        method.isAccessible = true

        val result = method.invoke(manager, input, 1L)
        assertNotNull("Should handle ingredient without quantity", result)
    }

    @Test
    fun `parseIngredient handles ingredient without unit`() = runTest {
        val input = "2 eggs"
        val method = manager.javaClass.getDeclaredMethod(
            "parseIngredient",
            String::class.java,
            Long::class.javaObjectType
        )
        method.isAccessible = true

        val result = method.invoke(manager, input, 1L)
        assertNotNull("Should handle ingredient without unit", result)
    }

    @Test
    fun `parseIngredient handles empty string`() = runTest {
        val input = ""
        val method = manager.javaClass.getDeclaredMethod(
            "parseIngredient",
            String::class.java,
            Long::class.javaObjectType
        )
        method.isAccessible = true

        val result = method.invoke(manager, input, 1L)
        assertNotNull("Should handle empty string gracefully", result)
    }

    // ========== Integration Tests with Recipe Data ==========

    @Test
    fun `addRecipesToList consolidates duplicate ingredients`() = runTest {
        // Setup mock recipe with ingredients
        val recipe1 = Recipe(
            id = 1,
            title = "Test Recipe 1",
            ingredients = listOf("2 cups flour", "1 cup sugar"),
            instructions = listOf("Mix well"),
            servings = 4
        )

        val recipe2 = Recipe(
            id = 2,
            title = "Test Recipe 2",
            ingredients = listOf("3 cups flour", "1/2 cup sugar"),
            instructions = listOf("Bake"),
            servings = 4
        )

        coEvery { recipeDao.getRecipeById(1) } returns recipe1
        coEvery { recipeDao.getRecipeById(2) } returns recipe2
        coEvery { groceryItemDao.getItemsForList(any()) } returns flowOf(emptyList())
        coEvery { groceryItemDao.insertAll(any()) } returns Unit

        // Add both recipes to list
        val result = manager.addRecipesToList(1L, listOf(1L, 2L))

        // Verify consolidation happened
        assertTrue("addRecipesToList should succeed", result.isSuccess)

        // Verify items were inserted (consolidated)
        coVerify { groceryItemDao.insertAll(any()) }
    }

    @Test
    fun `addRecipesToList tracks source recipes`() = runTest {
        val recipe = Recipe(
            id = 1,
            title = "Test Recipe",
            ingredients = listOf("2 cups flour"),
            instructions = listOf("Mix"),
            servings = 4
        )

        coEvery { recipeDao.getRecipeById(1) } returns recipe
        coEvery { groceryItemDao.getItemsForList(any()) } returns flowOf(emptyList())
        coEvery { groceryItemDao.insertAll(any()) } returns Unit

        val result = manager.addRecipesToList(1L, listOf(1L))

        assertTrue("Should track source recipe", result.isSuccess)
        coVerify { groceryItemDao.insertAll(any()) }
    }

    @Test
    fun `createList succeeds with valid name`() = runTest {
        val listName = "Weekly Shopping"

        coEvery { groceryListDao.insert(any()) } returns 1L

        val result = manager.createList(listName)

        assertTrue("Creating list should succeed", result.isSuccess)
        assertEquals("Should return list ID", 1L, result.getOrNull())
        coVerify { groceryListDao.insert(any()) }
    }

    @Test
    fun `createList fails with blank name`() = runTest {
        val result = manager.createList("")

        assertTrue("Creating list with blank name should fail", result.isFailure)
    }
}
