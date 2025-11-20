package com.recipeindex.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.recipeindex.app.data.dao.GroceryItemDao
import com.recipeindex.app.data.dao.GroceryListDao
import com.recipeindex.app.data.dao.MealPlanDao
import com.recipeindex.app.data.dao.RecipeDao
import com.recipeindex.app.data.dao.RecipeLogDao
import com.recipeindex.app.data.dao.SubstitutionDao
import com.recipeindex.app.data.entities.GroceryItem
import com.recipeindex.app.data.entities.GroceryList
import com.recipeindex.app.data.entities.IngredientSubstitution
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeLog

/**
 * AppDatabase - Single source of truth for offline-first storage
 *
 * Room database with Recipe, MealPlan, GroceryList, GroceryItem, IngredientSubstitution, and RecipeLog tables
 * No cloud sync - fully local with Samsung Quick Share for sharing
 */
@Database(
    entities = [Recipe::class, MealPlan::class, GroceryList::class, GroceryItem::class, IngredientSubstitution::class, RecipeLog::class],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    abstract fun mealPlanDao(): MealPlanDao

    abstract fun groceryListDao(): GroceryListDao

    abstract fun groceryItemDao(): GroceryItemDao

    abstract fun substitutionDao(): SubstitutionDao

    abstract fun recipeLogDao(): RecipeLogDao

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
