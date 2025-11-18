package com.recipeindex.app.data

import androidx.room.TypeConverter
import com.recipeindex.app.data.entities.RecipeSource

/**
 * Room TypeConverters for complex types
 *
 * Converts between Kotlin types and database-compatible types
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(separator = "|||")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            value.split("|||")
        }
    }

    @TypeConverter
    fun fromRecipeSource(value: RecipeSource): String {
        return value.name
    }

    @TypeConverter
    fun toRecipeSource(value: String): RecipeSource {
        return RecipeSource.valueOf(value)
    }
}
