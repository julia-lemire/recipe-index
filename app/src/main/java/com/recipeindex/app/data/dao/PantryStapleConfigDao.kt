package com.recipeindex.app.data.dao

import androidx.room.*
import com.recipeindex.app.data.entities.PantryStapleConfig
import kotlinx.coroutines.flow.Flow

/**
 * PantryStapleConfigDao - Data access for pantry staple configurations
 */
@Dao
interface PantryStapleConfigDao {

    @Query("SELECT * FROM pantry_staple_configs WHERE enabled = 1 ORDER BY category, itemName")
    fun getAllEnabled(): Flow<List<PantryStapleConfig>>

    @Query("SELECT * FROM pantry_staple_configs ORDER BY category, itemName")
    fun getAll(): Flow<List<PantryStapleConfig>>

    @Query("SELECT * FROM pantry_staple_configs WHERE id = :id")
    suspend fun getById(id: Long): PantryStapleConfig?

    @Query("SELECT * FROM pantry_staple_configs WHERE category = :category AND enabled = 1 ORDER BY itemName")
    fun getByCategory(category: String): Flow<List<PantryStapleConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: PantryStapleConfig): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<PantryStapleConfig>)

    @Update
    suspend fun update(config: PantryStapleConfig)

    @Delete
    suspend fun delete(config: PantryStapleConfig)

    @Query("DELETE FROM pantry_staple_configs WHERE isCustom = 0")
    suspend fun deleteAllDefaults()

    @Query("DELETE FROM pantry_staple_configs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM pantry_staple_configs")
    suspend fun getCount(): Int
}
