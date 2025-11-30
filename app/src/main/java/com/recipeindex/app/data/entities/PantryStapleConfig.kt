package com.recipeindex.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * PantryStapleConfig - Configuration for pantry staple filtering in grocery lists
 *
 * Defines ingredients that should be filtered from grocery lists when their
 * quantity is below a specified threshold, but shown when the quantity exceeds
 * the threshold.
 */
@Entity(tableName = "pantry_staple_configs")
data class PantryStapleConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Name/pattern to match (e.g., "salt", "black pepper", "flour") */
    val itemName: String,

    /** Threshold quantity - show item if quantity exceeds this value */
    val thresholdQuantity: Double,

    /** Threshold unit (e.g., "tbsp", "cup", "oz") */
    val thresholdUnit: String,

    /** Category for organization (e.g., "Spices", "Baking", "Oils") */
    val category: String,

    /** Whether this item is always filtered (never shown, regardless of quantity) */
    val alwaysFilter: Boolean = false,

    /** Whether this config is enabled */
    val enabled: Boolean = true,

    /** Whether this is a user-created custom config (vs default) */
    val isCustom: Boolean = false,

    /** Alternative names/patterns to match (comma-separated) */
    val alternativeNames: String? = null
)
