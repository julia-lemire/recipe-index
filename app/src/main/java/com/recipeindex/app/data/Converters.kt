package com.recipeindex.app.data

import androidx.room.TypeConverter
import com.recipeindex.app.data.entities.RecipeSource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

/**
 * Room TypeConverters for complex types
 *
 * Converts between Kotlin types and database-compatible types
 */
class Converters {

    private val json = Json { ignoreUnknownKeys = true }

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
    fun fromLongList(value: List<Long>): String {
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(",").map { it.toLong() }
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

    @TypeConverter
    fun fromSubstituteList(value: List<Substitute>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toSubstituteList(value: String): List<Substitute> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            try {
                json.decodeFromString(ListSerializer(Substitute.serializer()), value)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
