package com.recipeindex.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.recipeindex.app.data.Converters

/**
 * GroceryItem entity - Individual item in a grocery list
 *
 * Tracks source recipes for traceability and supports manual items.
 * Quantities are consolidated from multiple recipes.
 */
@Entity(
    tableName = "grocery_items",
    foreignKeys = [
        ForeignKey(
            entity = GroceryList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId")]
)
@TypeConverters(Converters::class)
data class GroceryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Foreign key to GroceryList */
    val listId: Long,

    /** Item name (e.g., "Chicken breast", "Garlic") */
    val name: String,

    /** Quantity as number (e.g., 6.0) */
    val quantity: Double?,

    /** Unit (e.g., "lbs", "cups", "tbsp") - null for count items */
    val unit: String?,

    /** Whether item is checked off */
    val isChecked: Boolean = false,

    /** Recipe IDs this item came from (for traceability) */
    val sourceRecipeIds: List<Long> = emptyList(),

    /** Optional user notes */
    val notes: String? = null,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)
