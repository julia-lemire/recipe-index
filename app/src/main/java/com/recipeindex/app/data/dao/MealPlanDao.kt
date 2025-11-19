package com.recipeindex.app.data.dao

import androidx.room.*
import com.recipeindex.app.data.entities.MealPlan
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MealPlan entity
 *
 * Provides queries for meal plan CRUD operations, search, and date filtering
 */
@Dao
interface MealPlanDao {

    /**
     * Get all meal plans, sorted by creation date (newest first)
     */
    @Query("SELECT * FROM meal_plans ORDER BY createdAt DESC")
    fun getAll(): Flow<List<MealPlan>>

    /**
     * Get meal plan by ID
     */
    @Query("SELECT * FROM meal_plans WHERE id = :id")
    suspend fun getById(id: Long): MealPlan?

    /**
     * Search meal plans by name (case-insensitive)
     */
    @Query("SELECT * FROM meal_plans WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchByName(query: String): Flow<List<MealPlan>>

    /**
     * Get meal plans that overlap with a date range
     * A plan overlaps if: plan.startDate <= endDate AND plan.endDate >= startDate
     * Also includes plans with no dates set
     */
    @Query("""
        SELECT * FROM meal_plans
        WHERE (startDate IS NULL OR startDate <= :endDate)
          AND (endDate IS NULL OR endDate >= :startDate)
        ORDER BY createdAt DESC
    """)
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<MealPlan>>

    /**
     * Search meal plans by tag (case-insensitive)
     * Note: Room doesn't support searching in List<String> directly,
     * so we search in the serialized tags string
     */
    @Query("SELECT * FROM meal_plans WHERE tags LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    fun searchByTag(tag: String): Flow<List<MealPlan>>

    /**
     * Insert new meal plan
     * @return ID of inserted plan
     */
    @Insert
    suspend fun insert(mealPlan: MealPlan): Long

    /**
     * Update existing meal plan
     */
    @Update
    suspend fun update(mealPlan: MealPlan)

    /**
     * Delete meal plan
     */
    @Delete
    suspend fun delete(mealPlan: MealPlan)

    /**
     * Delete meal plan by ID
     */
    @Query("DELETE FROM meal_plans WHERE id = :id")
    suspend fun deleteById(id: Long)
}
