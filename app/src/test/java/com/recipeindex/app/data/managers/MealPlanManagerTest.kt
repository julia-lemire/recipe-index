package com.recipeindex.app.data.managers

import com.recipeindex.app.data.dao.MealPlanDao
import com.recipeindex.app.data.dao.RecipeDao
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.entities.Recipe
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MealPlanManager
 *
 * Focus: Auto-tag aggregation, special event detection, date range validation
 */
class MealPlanManagerTest {

    private lateinit var mealPlanDao: MealPlanDao
    private lateinit var recipeDao: RecipeDao
    private lateinit var manager: MealPlanManager

    @Before
    fun setup() {
        mealPlanDao = mockk(relaxed = true)
        recipeDao = mockk(relaxed = true)

        manager = MealPlanManager(mealPlanDao, recipeDao)
    }

    // ========== Auto-Tag Aggregation Tests ==========

    @Test
    fun `getAutoTags aggregates ingredient tags from recipes`() = runTest {
        val recipe1 = Recipe(
            id = 1,
            title = "Chicken Soup",
            ingredients = listOf("chicken", "carrots"),
            instructions = listOf("Cook"),
            servings = 4,
            tags = listOf("Chicken", "Soup", "Winter")
        )

        val recipe2 = Recipe(
            id = 2,
            title = "Beef Stew",
            ingredients = listOf("beef"),
            instructions = listOf("Simmer"),
            servings = 4,
            tags = listOf("Beef", "Stew", "Winter")
        )

        coEvery { recipeDao.getRecipeById(1) } returns recipe1
        coEvery { recipeDao.getRecipeById(2) } returns recipe2

        // Create meal plan with these recipes
        val plan = MealPlan(
            name = "Winter Meals",
            recipeIds = listOf(1, 2)
        )

        // Use reflection to access private getAutoTags method
        val method = manager.javaClass.getDeclaredMethod(
            "getAutoTags",
            MealPlan::class.java
        )
        method.isAccessible = true

        val tags = method.invoke(manager, plan) as List<*>

        // Should contain ingredient tags: Chicken, Beef
        assertTrue("Should contain Chicken tag", tags.contains("Chicken"))
        assertTrue("Should contain Beef tag", tags.contains("Beef"))
    }

    @Test
    fun `getAutoTags removes duplicate tags`() = runTest {
        val recipe1 = Recipe(
            id = 1,
            title = "Recipe 1",
            ingredients = listOf("chicken"),
            instructions = listOf("Cook"),
            servings = 4,
            tags = listOf("Chicken", "Winter", "Comfort Food")
        )

        val recipe2 = Recipe(
            id = 2,
            title = "Recipe 2",
            ingredients = listOf("chicken"),
            instructions = listOf("Bake"),
            servings = 4,
            tags = listOf("Chicken", "Winter", "Easy")
        )

        coEvery { recipeDao.getRecipeById(1) } returns recipe1
        coEvery { recipeDao.getRecipeById(2) } returns recipe2

        val plan = MealPlan(
            name = "Test Plan",
            recipeIds = listOf(1, 2)
        )

        val method = manager.javaClass.getDeclaredMethod(
            "getAutoTags",
            MealPlan::class.java
        )
        method.isAccessible = true

        val tags = method.invoke(manager, plan) as List<*>

        // Should not have duplicate "Chicken" or "Winter" tags
        val chickenCount = tags.count { it == "Chicken" }
        val winterCount = tags.count { it == "Winter" }

        assertEquals("Should have only one Chicken tag", 1, chickenCount)
        assertEquals("Should have only one Winter tag", 1, winterCount)
    }

    // ========== Special Event Detection Tests ==========

    @Test
    fun `detectSpecialEventFromName detects Thanksgiving`() = runTest {
        val method = manager.javaClass.getDeclaredMethod(
            "detectSpecialEventFromName",
            String::class.java
        )
        method.isAccessible = true

        val testCases = listOf(
            "Thanksgiving Dinner",
            "thanksgiving feast",
            "THANKSGIVING MENU"
        )

        testCases.forEach { name ->
            val tags = method.invoke(manager, name) as List<*>
            assertTrue(
                "Should detect Thanksgiving in: $name",
                tags.contains("Thanksgiving")
            )
        }
    }

    @Test
    fun `detectSpecialEventFromName detects Christmas`() = runTest {
        val method = manager.javaClass.getDeclaredMethod(
            "detectSpecialEventFromName",
            String::class.java
        )
        method.isAccessible = true

        val testCases = listOf(
            "Christmas Dinner",
            "xmas party",
            "CHRISTMAS EVE"
        )

        testCases.forEach { name ->
            val tags = method.invoke(manager, name) as List<*>
            val hasChristmas = tags.contains("Christmas")
            assertTrue(
                "Should detect Christmas in: $name",
                hasChristmas
            )
        }
    }

    @Test
    fun `detectSpecialEventFromName detects Easter`() = runTest {
        val method = manager.javaClass.getDeclaredMethod(
            "detectSpecialEventFromName",
            String::class.java
        )
        method.isAccessible = true

        val tags = method.invoke(manager, "Easter Brunch") as List<*>
        assertTrue("Should detect Easter", tags.contains("Easter"))
    }

    @Test
    fun `detectSpecialEventFromName detects Fourth of July`() = runTest {
        val method = manager.javaClass.getDeclaredMethod(
            "detectSpecialEventFromName",
            String::class.java
        )
        method.isAccessible = true

        val testCases = listOf(
            "Fourth of July BBQ",
            "4th of july party",
            "independence day celebration"
        )

        testCases.forEach { name ->
            val tags = method.invoke(manager, name) as List<*>
            val hasFourth = tags.contains("Fourth of July") || tags.contains("Independence Day")
            assertTrue(
                "Should detect Fourth of July in: $name",
                hasFourth
            )
        }
    }

    @Test
    fun `detectSpecialEventFromName returns empty for regular names`() = runTest {
        val method = manager.javaClass.getDeclaredMethod(
            "detectSpecialEventFromName",
            String::class.java
        )
        method.isAccessible = true

        val tags = method.invoke(manager, "Weekly Meal Plan") as List<*>
        assertTrue("Should return empty list for regular names", tags.isEmpty())
    }

    // ========== CRUD Operation Tests ==========

    @Test
    fun `createMealPlan succeeds with valid plan`() = runTest {
        val plan = MealPlan(
            name = "Test Plan",
            recipeIds = listOf(1, 2, 3)
        )

        coEvery { mealPlanDao.insert(any()) } returns 1L

        val result = manager.createMealPlan(plan)

        assertTrue("Creating meal plan should succeed", result.isSuccess)
        assertEquals("Should return plan ID", 1L, result.getOrNull())
        coVerify { mealPlanDao.insert(any()) }
    }

    @Test
    fun `createMealPlan fails with blank name`() = runTest {
        val plan = MealPlan(
            name = "",
            recipeIds = listOf(1)
        )

        val result = manager.createMealPlan(plan)

        assertTrue("Creating plan with blank name should fail", result.isFailure)
        coVerify(exactly = 0) { mealPlanDao.insert(any()) }
    }

    @Test
    fun `updateMealPlan succeeds with valid plan`() = runTest {
        val plan = MealPlan(
            id = 1,
            name = "Updated Plan",
            recipeIds = listOf(1, 2)
        )

        coEvery { mealPlanDao.update(any()) } returns Unit

        val result = manager.updateMealPlan(plan)

        assertTrue("Updating meal plan should succeed", result.isSuccess)
        coVerify { mealPlanDao.update(any()) }
    }

    @Test
    fun `deleteMealPlan preserves recipes`() = runTest {
        val plan = MealPlan(
            id = 1,
            name = "Plan to Delete",
            recipeIds = listOf(1, 2, 3)
        )

        coEvery { mealPlanDao.delete(any()) } returns Unit

        val result = manager.deleteMealPlan(plan)

        assertTrue("Deleting meal plan should succeed", result.isSuccess)
        coVerify { mealPlanDao.delete(plan) }

        // Verify recipes are not deleted
        coVerify(exactly = 0) { recipeDao.delete(any()) }
    }

    // ========== Date Range Tests ==========

    @Test
    fun `createMealPlan accepts null dates`() = runTest {
        val plan = MealPlan(
            name = "Indefinite Plan",
            startDate = null,
            endDate = null,
            recipeIds = listOf(1)
        )

        coEvery { mealPlanDao.insert(any()) } returns 1L

        val result = manager.createMealPlan(plan)

        assertTrue("Should accept null dates", result.isSuccess)
    }

    @Test
    fun `createMealPlan accepts flexible date range`() = runTest {
        val sundayTimestamp = 1700380800000L // Example Sunday
        val thursdayTimestamp = 1700726400000L // Example Thursday

        val plan = MealPlan(
            name = "Sun-Thu Plan",
            startDate = sundayTimestamp,
            endDate = thursdayTimestamp,
            recipeIds = listOf(1, 2)
        )

        coEvery { mealPlanDao.insert(any()) } returns 1L

        val result = manager.createMealPlan(plan)

        assertTrue("Should accept flexible date range", result.isSuccess)
    }

    @Test
    fun `createMealPlan accepts single day`() = runTest {
        val timestamp = 1700380800000L // Single day

        val plan = MealPlan(
            name = "Single Day Event",
            startDate = timestamp,
            endDate = timestamp, // Same day
            recipeIds = listOf(1)
        )

        coEvery { mealPlanDao.insert(any()) } returns 1L

        val result = manager.createMealPlan(plan)

        assertTrue("Should accept single day plan", result.isSuccess)
    }
}
