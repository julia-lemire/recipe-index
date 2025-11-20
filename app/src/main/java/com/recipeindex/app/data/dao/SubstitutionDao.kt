package com.recipeindex.app.data.dao

import androidx.room.*
import com.recipeindex.app.data.entities.IngredientSubstitution
import kotlinx.coroutines.flow.Flow

/**
 * SubstitutionDao - Database access for ingredient substitutions
 */
@Dao
interface SubstitutionDao {

    /**
     * Get all substitutions as Flow
     */
    @Query("SELECT * FROM ingredient_substitutions ORDER BY ingredient ASC")
    fun getAllSubstitutions(): Flow<List<IngredientSubstitution>>

    /**
     * Search substitutions by ingredient name (case-insensitive, partial match)
     */
    @Query("SELECT * FROM ingredient_substitutions WHERE LOWER(ingredient) LIKE '%' || LOWER(:query) || '%' ORDER BY ingredient ASC")
    fun searchSubstitutions(query: String): Flow<List<IngredientSubstitution>>

    /**
     * Get substitutions by category
     */
    @Query("SELECT * FROM ingredient_substitutions WHERE category = :category ORDER BY ingredient ASC")
    fun getSubstitutionsByCategory(category: String): Flow<List<IngredientSubstitution>>

    /**
     * Get substitution by exact ingredient name (normalized lowercase)
     */
    @Query("SELECT * FROM ingredient_substitutions WHERE LOWER(ingredient) = LOWER(:ingredient) LIMIT 1")
    suspend fun getSubstitutionByIngredient(ingredient: String): IngredientSubstitution?

    /**
     * Get substitution by ID
     */
    @Query("SELECT * FROM ingredient_substitutions WHERE id = :id LIMIT 1")
    suspend fun getSubstitutionById(id: Long): IngredientSubstitution?

    /**
     * Insert substitution
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubstitution(substitution: IngredientSubstitution): Long

    /**
     * Insert multiple substitutions (for initial data population)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(substitutions: List<IngredientSubstitution>)

    /**
     * Update substitution
     */
    @Update
    suspend fun updateSubstitution(substitution: IngredientSubstitution)

    /**
     * Delete substitution
     */
    @Delete
    suspend fun deleteSubstitution(substitution: IngredientSubstitution)

    /**
     * Delete substitution by ID
     */
    @Query("DELETE FROM ingredient_substitutions WHERE id = :id")
    suspend fun deleteSubstitutionById(id: Long)

    /**
     * Get count of all substitutions (for checking if DB is populated)
     */
    @Query("SELECT COUNT(*) FROM ingredient_substitutions")
    suspend fun getSubstitutionCount(): Int

    /**
     * Get all categories (distinct)
     */
    @Query("SELECT DISTINCT category FROM ingredient_substitutions ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
}
