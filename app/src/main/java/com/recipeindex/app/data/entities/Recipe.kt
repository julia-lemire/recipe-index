package com.recipeindex.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.recipeindex.app.data.Converters
import kotlinx.serialization.Serializable

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

    /** Serving/portion size (e.g., "1 ½ cups", "200g") */
    val servingSize: String? = null,

    val prepTimeMinutes: Int? = null,

    val cookTimeMinutes: Int? = null,

    val tags: List<String> = emptyList(),

    val cuisine: String? = null,

    val source: RecipeSource,

    val sourceUrl: String? = null,

    val photoPath: String? = null, // Deprecated: use mediaPaths for new recipes

    val mediaPaths: List<MediaItem> = emptyList(),

    val notes: String? = null,

    /** Tips, substitutions, and notes from the original source (website/PDF) */
    val sourceTips: String? = null,

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

/**
 * Media item for recipe photos and videos
 */
@Serializable
data class MediaItem(
    val type: MediaType,
    val path: String, // Local file path or URL
    val thumbnailPath: String? = null // For videos, path to generated thumbnail
)

/**
 * Type of media item
 */
@Serializable
enum class MediaType {
    IMAGE,
    VIDEO
}
