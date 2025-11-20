package com.recipeindex.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * RecipeLog - Track when recipes were made
 *
 * Stores history of when user made a recipe, with optional notes and rating
 */
@Entity(
    tableName = "recipe_logs",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
data class RecipeLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val recipeId: Long,

    val timestamp: Long = System.currentTimeMillis(),

    val notes: String? = null,

    val rating: Int? = null  // 1-5 stars, optional
)
