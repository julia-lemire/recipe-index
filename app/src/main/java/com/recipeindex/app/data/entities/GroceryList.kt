package com.recipeindex.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.recipeindex.app.data.Converters

/**
 * GroceryList entity - Container for grocery shopping lists
 *
 * Supports multiple lists (e.g., "Weekly Shopping", "Thanksgiving", "Pantry Restock")
 */
@Entity(tableName = "grocery_lists")
@TypeConverters(Converters::class)
data class GroceryList(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** User-defined name (e.g., "Week of Nov 18", "Thanksgiving Dinner") */
    val name: String,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)
