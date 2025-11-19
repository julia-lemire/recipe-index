package com.recipeindex.app.data.dao

import androidx.room.*
import com.recipeindex.app.data.entities.GroceryList
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for GroceryList entity
 */
@Dao
interface GroceryListDao {

    /**
     * Get all grocery lists, sorted by creation date (newest first)
     */
    @Query("SELECT * FROM grocery_lists ORDER BY createdAt DESC")
    fun getAll(): Flow<List<GroceryList>>

    /**
     * Get grocery list by ID
     */
    @Query("SELECT * FROM grocery_lists WHERE id = :id")
    suspend fun getById(id: Long): GroceryList?

    /**
     * Get grocery list by ID as Flow
     */
    @Query("SELECT * FROM grocery_lists WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<GroceryList?>

    /**
     * Search grocery lists by name
     */
    @Query("SELECT * FROM grocery_lists WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchByName(query: String): Flow<List<GroceryList>>

    /**
     * Insert new grocery list
     * @return ID of inserted list
     */
    @Insert
    suspend fun insert(list: GroceryList): Long

    /**
     * Update existing grocery list
     */
    @Update
    suspend fun update(list: GroceryList)

    /**
     * Delete grocery list
     */
    @Delete
    suspend fun delete(list: GroceryList)

    /**
     * Delete grocery list by ID
     */
    @Query("DELETE FROM grocery_lists WHERE id = :id")
    suspend fun deleteById(id: Long)
}
