package com.recipeindex.app.data.managers

import com.recipeindex.app.data.dao.RecipeDao
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeSource
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RecipeManager
 *
 * Focus: CRUD operations, validation, search, favorite toggle
 */
class RecipeManagerTest {

    private lateinit var recipeDao: RecipeDao
    private lateinit var manager: RecipeManager

    @Before
    fun setup() {
        recipeDao = mockk(relaxed = true)
        manager = RecipeManager(recipeDao)
    }

    // ========== CRUD Operation Tests ==========

    @Test
    fun `createRecipe succeeds with valid recipe`() = runTest {
        val recipe = Recipe(
            title = "Test Recipe",
            ingredients = listOf("flour", "sugar"),
            instructions = listOf("Mix well"),
            servings = 4
        )

        coEvery { recipeDao.insertRecipe(any()) } returns 1L

        val result = manager.createRecipe(recipe)

        assertTrue("Creating recipe should succeed", result.isSuccess)
        assertEquals("Should return recipe ID", 1L, result.getOrNull())

        // Verify timestamps were set
        coVerify {
            recipeDao.insertRecipe(match {
                it.createdAt > 0 && it.updatedAt > 0
            })
        }
    }

    @Test
    fun `createRecipe fails with blank title`() = runTest {
        val recipe = Recipe(
            title = "",
            ingredients = listOf("flour"),
            instructions = listOf("Mix"),
            servings = 4
        )

        val result = manager.createRecipe(recipe)

        assertTrue("Should fail with blank title", result.isFailure)
        result.exceptionOrNull()?.let { exception ->
            assertTrue(
                "Should have meaningful error message",
                exception.message?.contains("title") == true
            )
        }

        // Verify DAO was never called
        coVerify(exactly = 0) { recipeDao.insertRecipe(any()) }
    }

    @Test
    fun `createRecipe fails with empty ingredients`() = runTest {
        val recipe = Recipe(
            title = "Test Recipe",
            ingredients = emptyList(),
            instructions = listOf("Mix"),
            servings = 4
        )

        val result = manager.createRecipe(recipe)

        assertTrue("Should fail with empty ingredients", result.isFailure)
        result.exceptionOrNull()?.let { exception ->
            assertTrue(
                "Should have meaningful error message",
                exception.message?.contains("ingredient") == true
            )
        }
    }

    @Test
    fun `createRecipe fails with empty instructions`() = runTest {
        val recipe = Recipe(
            title = "Test Recipe",
            ingredients = listOf("flour"),
            instructions = emptyList(),
            servings = 4
        )

        val result = manager.createRecipe(recipe)

        assertTrue("Should fail with empty instructions", result.isFailure)
        result.exceptionOrNull()?.let { exception ->
            assertTrue(
                "Should have meaningful error message",
                exception.message?.contains("instruction") == true
            )
        }
    }

    @Test
    fun `createRecipe fails with zero servings`() = runTest {
        val recipe = Recipe(
            title = "Test Recipe",
            ingredients = listOf("flour"),
            instructions = listOf("Mix"),
            servings = 0
        )

        val result = manager.createRecipe(recipe)

        assertTrue("Should fail with zero servings", result.isFailure)
        result.exceptionOrNull()?.let { exception ->
            assertTrue(
                "Should have meaningful error message",
                exception.message?.contains("Servings") == true
            )
        }
    }

    @Test
    fun `updateRecipe succeeds with valid recipe`() = runTest {
        val recipe = Recipe(
            id = 1,
            title = "Updated Recipe",
            ingredients = listOf("flour", "eggs"),
            instructions = listOf("Mix", "Bake"),
            servings = 6,
            createdAt = 1000L
        )

        coEvery { recipeDao.updateRecipe(any()) } returns Unit

        val result = manager.updateRecipe(recipe)

        assertTrue("Updating recipe should succeed", result.isSuccess)

        // Verify updatedAt was set (but createdAt preserved)
        coVerify {
            recipeDao.updateRecipe(match {
                it.id == 1L &&
                it.updatedAt > it.createdAt &&
                it.createdAt == 1000L
            })
        }
    }

    @Test
    fun `updateRecipe preserves database ID`() = runTest {
        val recipeId = 42L
        val recipe = Recipe(
            id = recipeId,
            title = "Test Recipe",
            ingredients = listOf("flour"),
            instructions = listOf("Mix"),
            servings = 4
        )

        coEvery { recipeDao.updateRecipe(any()) } returns Unit

        manager.updateRecipe(recipe)

        coVerify {
            recipeDao.updateRecipe(match { it.id == recipeId })
        }
    }

    @Test
    fun `updateRecipe validates same as create`() = runTest {
        val invalidRecipe = Recipe(
            id = 1,
            title = "", // Invalid
            ingredients = listOf("flour"),
            instructions = listOf("Mix"),
            servings = 4
        )

        val result = manager.updateRecipe(invalidRecipe)

        assertTrue("Should fail validation", result.isFailure)
        coVerify(exactly = 0) { recipeDao.updateRecipe(any()) }
    }

    @Test
    fun `deleteRecipe removes recipe from database`() = runTest {
        val recipeId = 1L

        coEvery { recipeDao.deleteRecipeById(recipeId) } returns Unit

        val result = manager.deleteRecipe(recipeId)

        assertTrue("Deleting recipe should succeed", result.isSuccess)
        coVerify { recipeDao.deleteRecipeById(recipeId) }
    }

    @Test
    fun `deleteRecipe handles DAO errors`() = runTest {
        val recipeId = 1L
        val exception = Exception("Database error")

        coEvery { recipeDao.deleteRecipeById(recipeId) } throws exception

        val result = manager.deleteRecipe(recipeId)

        assertTrue("Should fail when DAO throws", result.isFailure)
        assertEquals("Should preserve exception", exception, result.exceptionOrNull())
    }

    // ========== Favorite Toggle Tests ==========

    @Test
    fun `toggleFavorite sets favorite to true`() = runTest {
        val recipeId = 1L

        coEvery { recipeDao.updateFavoriteStatus(recipeId, true) } returns Unit

        val result = manager.toggleFavorite(recipeId, true)

        assertTrue("Toggle should succeed", result.isSuccess)
        coVerify { recipeDao.updateFavoriteStatus(recipeId, true) }
    }

    @Test
    fun `toggleFavorite sets favorite to false`() = runTest {
        val recipeId = 1L

        coEvery { recipeDao.updateFavoriteStatus(recipeId, false) } returns Unit

        val result = manager.toggleFavorite(recipeId, false)

        assertTrue("Toggle should succeed", result.isSuccess)
        coVerify { recipeDao.updateFavoriteStatus(recipeId, false) }
    }

    @Test
    fun `toggleFavorite handles DAO errors`() = runTest {
        val recipeId = 1L
        val exception = Exception("Database error")

        coEvery { recipeDao.updateFavoriteStatus(any(), any()) } throws exception

        val result = manager.toggleFavorite(recipeId, true)

        assertTrue("Should fail when DAO throws", result.isFailure)
    }

    // ========== Query Tests (Flow-based) ==========

    @Test
    fun `getAllRecipes returns Flow from DAO`() = runTest {
        val recipes = listOf(
            Recipe(id = 1, title = "Recipe 1", ingredients = listOf("a"), instructions = listOf("1"), servings = 4),
            Recipe(id = 2, title = "Recipe 2", ingredients = listOf("b"), instructions = listOf("2"), servings = 6)
        )

        every { recipeDao.getAllRecipes() } returns flowOf(recipes)

        val flow = manager.getAllRecipes()

        verify { recipeDao.getAllRecipes() }
        // Flow is returned, actual collection would happen in caller
    }

    @Test
    fun `getRecipeById returns Flow from DAO`() = runTest {
        val recipe = Recipe(
            id = 1,
            title = "Test Recipe",
            ingredients = listOf("flour"),
            instructions = listOf("Mix"),
            servings = 4
        )

        every { recipeDao.getRecipeById(1L) } returns flowOf(recipe)

        val flow = manager.getRecipeById(1L)

        verify { recipeDao.getRecipeById(1L) }
    }

    @Test
    fun `getFavoriteRecipes returns Flow from DAO`() = runTest {
        val favorites = listOf(
            Recipe(id = 1, title = "Fav 1", ingredients = listOf("a"), instructions = listOf("1"), servings = 4, isFavorite = true),
            Recipe(id = 2, title = "Fav 2", ingredients = listOf("b"), instructions = listOf("2"), servings = 4, isFavorite = true)
        )

        every { recipeDao.getFavoriteRecipes() } returns flowOf(favorites)

        val flow = manager.getFavoriteRecipes()

        verify { recipeDao.getFavoriteRecipes() }
    }

    @Test
    fun `searchRecipes passes query to DAO`() = runTest {
        val query = "chicken"
        val results = listOf(
            Recipe(id = 1, title = "Chicken Soup", ingredients = listOf("chicken"), instructions = listOf("Cook"), servings = 4)
        )

        every { recipeDao.searchRecipes(query) } returns flowOf(results)

        val flow = manager.searchRecipes(query)

        verify { recipeDao.searchRecipes(query) }
    }

    // ========== Scaling Tests ==========

    @Test
    fun `scaleRecipe updates servings`() {
        val recipe = Recipe(
            id = 1,
            title = "Test Recipe",
            ingredients = listOf("2 cups flour", "1 cup sugar"),
            instructions = listOf("Mix"),
            servings = 4
        )

        val scaled = manager.scaleRecipe(recipe, 8)

        assertEquals("Should update servings", 8, scaled.servings)
        assertEquals("Should preserve title", recipe.title, scaled.title)
        assertEquals("Should preserve ID", recipe.id, scaled.id)
    }

    @Test
    fun `scaleRecipe down preserves recipe structure`() {
        val recipe = Recipe(
            id = 1,
            title = "Test Recipe",
            ingredients = listOf("4 cups flour"),
            instructions = listOf("Mix", "Bake"),
            servings = 8
        )

        val scaled = manager.scaleRecipe(recipe, 4)

        assertEquals("Should halve servings", 4, scaled.servings)
        assertEquals("Should preserve ingredients list", recipe.ingredients, scaled.ingredients)
        assertEquals("Should preserve instructions", recipe.instructions, scaled.instructions)
    }

    // ========== Edge Cases ==========

    @Test
    fun `createRecipe handles all optional fields`() = runTest {
        val recipe = Recipe(
            title = "Full Recipe",
            ingredients = listOf("flour"),
            instructions = listOf("Mix"),
            servings = 4,
            prepTimeMinutes = 15,
            cookTimeMinutes = 30,
            tags = listOf("easy", "quick"),
            notes = "Test notes",
            source = RecipeSource.MANUAL,
            sourceUrl = "https://example.com",
            photoPath = "/path/to/photo.jpg",
            isFavorite = true
        )

        coEvery { recipeDao.insertRecipe(any()) } returns 1L

        val result = manager.createRecipe(recipe)

        assertTrue("Should handle all optional fields", result.isSuccess)

        // Verify all fields were preserved
        coVerify {
            recipeDao.insertRecipe(match {
                it.prepTimeMinutes == 15 &&
                it.cookTimeMinutes == 30 &&
                it.tags.contains("easy") &&
                it.notes == "Test notes" &&
                it.isFavorite == true
            })
        }
    }

    @Test
    fun `createRecipe with negative servings fails`() = runTest {
        val recipe = Recipe(
            title = "Test",
            ingredients = listOf("flour"),
            instructions = listOf("Mix"),
            servings = -1
        )

        val result = manager.createRecipe(recipe)

        assertTrue("Should fail with negative servings", result.isFailure)
    }

    @Test
    fun `validateRecipe allows single ingredient and instruction`() = runTest {
        val recipe = Recipe(
            title = "Minimal Recipe",
            ingredients = listOf("salt"),
            instructions = listOf("Sprinkle"),
            servings = 1
        )

        coEvery { recipeDao.insertRecipe(any()) } returns 1L

        val result = manager.createRecipe(recipe)

        assertTrue("Should allow minimal valid recipe", result.isSuccess)
    }
}
