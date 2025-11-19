package com.recipeindex.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.recipeindex.app.data.Converters

/**
 * MealPlan entity - Flexible meal planning for weeks, special events, or custom periods
 *
 * Supports both date-based plans (e.g., "Nov 18-22") and standalone event plans
 * (e.g., "Thanksgiving Dinner"). Tags are auto-aggregated from recipe ingredients.
 */
@Entity(tableName = "meal_plans")
@TypeConverters(Converters::class)
data class MealPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** User-defined name (e.g., "Thanksgiving Dinner", "Nov 18-22") */
    val name: String,

    /** Optional start date (epoch milliseconds) */
    val startDate: Long? = null,

    /** Optional end date (epoch milliseconds) */
    val endDate: Long? = null,

    /** List of recipe IDs in this plan */
    val recipeIds: List<Long> = emptyList(),

    /** Tags aggregated from recipes (ingredient and special event tags) */
    val tags: List<String> = emptyList(),

    /** Optional user notes */
    val notes: String? = null,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)
