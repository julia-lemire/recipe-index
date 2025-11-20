package com.recipeindex.app.utils.filtersort.grocerylist

import com.recipeindex.app.data.entities.GroceryList
import com.recipeindex.app.utils.filtersort.core.GroupBy
import java.text.SimpleDateFormat
import java.util.*

/**
 * Month key for grouping grocery lists by month
 */
data class GroceryListMonthKey(val year: Int, val month: Int) : Comparable<GroceryListMonthKey> {
    override fun compareTo(other: GroceryListMonthKey): Int {
        return if (year != other.year) {
            year.compareTo(other.year)
        } else {
            month.compareTo(other.month)
        }
    }
}

/**
 * Age categories for grouping grocery lists
 */
enum class GroceryListAgeCategory {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    OLDER
}

/**
 * Group grocery lists by creation month
 */
class MonthGrouping : GroupBy<GroceryList, GroceryListMonthKey> {
    override val id: String = "month"
    override val label: String = "Month"

    override fun extractKey(item: GroceryList): GroceryListMonthKey {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = item.createdAt
        }
        return GroceryListMonthKey(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH)
        )
    }

    override fun formatKeyLabel(key: GroceryListMonthKey): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, key.year)
            set(Calendar.MONTH, key.month)
        }
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    override fun compareKeys(key1: GroceryListMonthKey, key2: GroceryListMonthKey): Int {
        return key2.compareTo(key1)  // Reverse order (most recent first)
    }
}

/**
 * Group grocery lists by age category
 */
class AgeGrouping : GroupBy<GroceryList, GroceryListAgeCategory> {
    override val id: String = "age"
    override val label: String = "Age"

    override fun extractKey(item: GroceryList): GroceryListAgeCategory {
        val now = System.currentTimeMillis()
        val age = now - item.createdAt

        val oneDayMs = 24 * 60 * 60 * 1000L
        val oneWeekMs = 7 * oneDayMs
        val oneMonthMs = 30 * oneDayMs

        return when {
            age < oneDayMs -> GroceryListAgeCategory.TODAY
            age < oneWeekMs -> GroceryListAgeCategory.THIS_WEEK
            age < oneMonthMs -> GroceryListAgeCategory.THIS_MONTH
            else -> GroceryListAgeCategory.OLDER
        }
    }

    override fun formatKeyLabel(key: GroceryListAgeCategory): String {
        return when (key) {
            GroceryListAgeCategory.TODAY -> "Today"
            GroceryListAgeCategory.THIS_WEEK -> "This Week"
            GroceryListAgeCategory.THIS_MONTH -> "This Month"
            GroceryListAgeCategory.OLDER -> "Older"
        }
    }

    override fun compareKeys(key1: GroceryListAgeCategory, key2: GroceryListAgeCategory): Int {
        val order = listOf(
            GroceryListAgeCategory.TODAY,
            GroceryListAgeCategory.THIS_WEEK,
            GroceryListAgeCategory.THIS_MONTH,
            GroceryListAgeCategory.OLDER
        )
        return order.indexOf(key1).compareTo(order.indexOf(key2))
    }
}

/**
 * Note: Groupings based on completion status or item count would require a composite
 * data type that includes both the GroceryList and its items. Since grocery lists are
 * short-lived (per user feedback), these simple groupings should suffice.
 */
