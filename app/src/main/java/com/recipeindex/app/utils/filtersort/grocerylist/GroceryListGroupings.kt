package com.recipeindex.app.utils.filtersort.grocerylist

import com.recipeindex.app.data.entities.GroceryList
import com.recipeindex.app.utils.filtersort.core.GroupBy
import java.text.SimpleDateFormat
import java.util.*

/**
 * Group grocery lists by creation month
 */
class MonthGrouping : GroupBy<GroceryList, MonthKey> {
    override val id: String = "month"
    override val label: String = "Month"

    data class MonthKey(val year: Int, val month: Int) : Comparable<MonthKey> {
        override fun compareTo(other: MonthKey): Int {
            return if (year != other.year) {
                year.compareTo(other.year)
            } else {
                month.compareTo(other.month)
            }
        }
    }

    override fun extractKey(item: GroceryList): MonthKey {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = item.createdAt
        }
        return MonthKey(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH)
        )
    }

    override fun formatKeyLabel(key: MonthKey): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, key.year)
            set(Calendar.MONTH, key.month)
        }
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    override fun compareKeys(key1: MonthKey, key2: MonthKey): Int {
        return key2.compareTo(key1)  // Reverse order (most recent first)
    }
}

/**
 * Group grocery lists by age category
 */
class AgeGrouping : GroupBy<GroceryList, AgeCategory> {
    override val id: String = "age"
    override val label: String = "Age"

    enum class AgeCategory {
        TODAY,
        THIS_WEEK,
        THIS_MONTH,
        OLDER
    }

    override fun extractKey(item: GroceryList): AgeCategory {
        val now = System.currentTimeMillis()
        val age = now - item.createdAt

        val oneDayMs = 24 * 60 * 60 * 1000L
        val oneWeekMs = 7 * oneDayMs
        val oneMonthMs = 30 * oneDayMs

        return when {
            age < oneDayMs -> AgeCategory.TODAY
            age < oneWeekMs -> AgeCategory.THIS_WEEK
            age < oneMonthMs -> AgeCategory.THIS_MONTH
            else -> AgeCategory.OLDER
        }
    }

    override fun formatKeyLabel(key: AgeCategory): String {
        return when (key) {
            AgeCategory.TODAY -> "Today"
            AgeCategory.THIS_WEEK -> "This Week"
            AgeCategory.THIS_MONTH -> "This Month"
            AgeCategory.OLDER -> "Older"
        }
    }

    override fun compareKeys(key1: AgeCategory, key2: AgeCategory): Int {
        val order = listOf(
            AgeCategory.TODAY,
            AgeCategory.THIS_WEEK,
            AgeCategory.THIS_MONTH,
            AgeCategory.OLDER
        )
        return order.indexOf(key1).compareTo(order.indexOf(key2))
    }
}

/**
 * Note: Groupings based on completion status or item count would require a composite
 * data type that includes both the GroceryList and its items. Since grocery lists are
 * short-lived (per user feedback), these simple groupings should suffice.
 */
