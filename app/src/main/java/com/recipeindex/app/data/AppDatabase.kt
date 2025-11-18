package com.recipeindex.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.recipeindex.app.data.dao.MealPlanDao
import com.recipeindex.app.data.dao.RecipeDao
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.entities.Recipe

/**
 * AppDatabase - Single source of truth for offline-first storage
 *
 * Room database with Recipe and MealPlan tables
 * No cloud sync - fully local with Samsung Quick Share for sharing
 */
@Database(
    entities = [Recipe::class, MealPlan::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    abstract fun mealPlanDao(): MealPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recipe_index_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
