package com.recipeindex.app.data.dao

import androidx.room.*
import com.recipeindex.app.data.entities.GroceryItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for GroceryItem entity
 */
@Dao
interface GroceryItemDao {

    /**
     * Get all items for a grocery list
     */
    @Query("SELECT * FROM grocery_items WHERE listId = :listId ORDER BY isChecked ASC, createdAt ASC")
    fun getItemsForList(listId: Long): Flow<List<GroceryItem>>

    /**
     * Get unchecked items for a grocery list
     */
    @Query("SELECT * FROM grocery_items WHERE listId = :listId AND isChecked = 0 ORDER BY createdAt ASC")
    fun getUncheckedItems(listId: Long): Flow<List<GroceryItem>>

    /**
     * Get checked items for a grocery list
     */
    @Query("SELECT * FROM grocery_items WHERE listId = :listId AND isChecked = 1 ORDER BY createdAt ASC")
    fun getCheckedItems(listId: Long): Flow<List<GroceryItem>>

    /**
     * Get item by ID
     */
    @Query("SELECT * FROM grocery_items WHERE id = :id")
    suspend fun getById(id: Long): GroceryItem?

    /**
     * Get item count for a list
     */
    @Query("SELECT COUNT(*) FROM grocery_items WHERE listId = :listId")
    fun getItemCount(listId: Long): Flow<Int>

    /**
     * Get checked item count for a list
     */
    @Query("SELECT COUNT(*) FROM grocery_items WHERE listId = :listId AND isChecked = 1")
    fun getCheckedCount(listId: Long): Flow<Int>

    /**
     * Insert new grocery item
     * @return ID of inserted item
     */
    @Insert
    suspend fun insert(item: GroceryItem): Long

    /**
     * Insert multiple items
     */
    @Insert
    suspend fun insertAll(items: List<GroceryItem>)

    /**
     * Update existing grocery item
     */
    @Update
    suspend fun update(item: GroceryItem)

    /**
     * Delete grocery item
     */
    @Delete
    suspend fun delete(item: GroceryItem)

    /**
     * Update checked status of an item
     */
    @Query("UPDATE grocery_items SET isChecked = :checked, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun updateCheckedStatus(itemId: Long, checked: Boolean, updatedAt: Long = System.currentTimeMillis())

    /**
     * Delete item by ID
     */
    @Query("DELETE FROM grocery_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Long)

    /**
     * Delete all checked items from a list
     */
    @Query("DELETE FROM grocery_items WHERE listId = :listId AND isChecked = 1")
    suspend fun deleteCheckedItems(listId: Long)

    /**
     * Delete all items from a list
     */
    @Query("DELETE FROM grocery_items WHERE listId = :listId")
    suspend fun deleteAllItemsForList(listId: Long)
}
