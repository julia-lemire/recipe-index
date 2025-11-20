package com.recipeindex.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.recipeindex.app.data.Substitute

/**
 * IngredientSubstitution - Ingredient substitution database entity
 *
 * Stores base ingredient and its possible substitutes (encoded as JSON via TypeConverter)
 * Can be pre-populated or user-added
 */
@Entity(tableName = "ingredient_substitutions")
data class IngredientSubstitution(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Base ingredient name (e.g., "butter", "eggs", "milk") - normalized to lowercase */
    val ingredient: String,

    /** Category for filtering (e.g., "Dairy", "Baking", "Spices", "Sweeteners") */
    val category: String,

    /** List of possible substitutes - automatically converted to/from JSON by Room TypeConverter */
    val substitutes: List<Substitute>,

    /** Whether this was added by the user (true) or pre-populated (false) */
    val isUserAdded: Boolean = false,

    /** Timestamp when created/last modified */
    val lastModified: Long = System.currentTimeMillis()
)
