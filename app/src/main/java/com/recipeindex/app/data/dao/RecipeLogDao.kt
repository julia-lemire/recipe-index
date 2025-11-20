package com.recipeindex.app.data.dao

import androidx.room.*
import com.recipeindex.app.data.entities.RecipeLog
import kotlinx.coroutines.flow.Flow

/**
 * RecipeLogDao - Data access for recipe logs
 */
@Dao
interface RecipeLogDao {
    /**
     * Get all logs for a specific recipe (newest first)
     */
    @Query("SELECT * FROM recipe_logs WHERE recipeId = :recipeId ORDER BY timestamp DESC")
    fun getLogsForRecipe(recipeId: Long): Flow<List<RecipeLog>>

    /**
     * Get the most recent log for a recipe
     */
    @Query("SELECT * FROM recipe_logs WHERE recipeId = :recipeId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLogForRecipe(recipeId: Long): RecipeLog?

    /**
     * Get total count of times a recipe was made
     */
    @Query("SELECT COUNT(*) FROM recipe_logs WHERE recipeId = :recipeId")
    suspend fun getLogCountForRecipe(recipeId: Long): Int

    /**
     * Insert a new log entry
     */
    @Insert
    suspend fun insertLog(log: RecipeLog): Long

    /**
     * Update an existing log
     */
    @Update
    suspend fun updateLog(log: RecipeLog)

    /**
     * Delete a log entry
     */
    @Delete
    suspend fun deleteLog(log: RecipeLog)

    /**
     * Delete a log by ID
     */
    @Query("DELETE FROM recipe_logs WHERE id = :logId")
    suspend fun deleteLogById(logId: Long)

    /**
     * Get all logs across all recipes (for stats/analytics)
     */
    @Query("SELECT * FROM recipe_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<RecipeLog>>
}
