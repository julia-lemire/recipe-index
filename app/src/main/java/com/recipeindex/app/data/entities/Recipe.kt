package com.recipeindex.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.recipeindex.app.data.Converters

/**
 * Recipe entity - Unified entity for all recipe types
 *
 * Uses behavioral flags approach (isTemplate, isFavorite)
 * Single source of truth for recipe data
 */
@Entity(tableName = "recipes")
@TypeConverters(Converters::class)
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    val ingredients: List<String>,

    val instructions: List<String>,

    val servings: Int,

    val prepTimeMinutes: Int? = null,

    val cookTimeMinutes: Int? = null,

    val tags: List<String> = emptyList(),

    val source: RecipeSource,

    val sourceUrl: String? = null,

    val photoPath: String? = null,

    val notes: String? = null,

    val isFavorite: Boolean = false,

    val isTemplate: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Source type for recipe origin tracking
 */
enum class RecipeSource {
    MANUAL,
    URL,
    PDF,
    PHOTO
}
