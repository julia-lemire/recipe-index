package com.recipeindex.app.utils.filtersort.grocerylist

import com.recipeindex.app.data.entities.GroceryList
import com.recipeindex.app.utils.filtersort.core.Filter
import java.util.concurrent.TimeUnit

/**
 * Filter grocery lists created in the last N days
 */
class CreatedRecentlyFilter(private val daysAgo: Int) : Filter<GroceryList> {
    override val id: String = "created_recently_$daysAgo"
    override val label: String = when (daysAgo) {
        1 -> "Today"
        7 -> "This Week"
        30 -> "This Month"
        else -> "Last $daysAgo days"
    }

    override fun matches(item: GroceryList): Boolean {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysAgo.toLong())
        return item.createdAt >= cutoff
    }
}

/**
 * Filter grocery lists modified in the last N days
 */
class ModifiedRecentlyFilter(private val daysAgo: Int) : Filter<GroceryList> {
    override val id: String = "modified_recently_$daysAgo"
    override val label: String = when (daysAgo) {
        1 -> "Modified Today"
        7 -> "Modified This Week"
        30 -> "Modified This Month"
        else -> "Modified in Last $daysAgo days"
    }

    override fun matches(item: GroceryList): Boolean {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysAgo.toLong())
        return item.updatedAt >= cutoff
    }
}

/**
 * Note: Filters for item-based properties (e.g., "has unchecked items", "item count")
 * would require a composite data type that includes both the GroceryList and its items.
 * Since grocery lists are short-lived (per user feedback), these simple filters should suffice.
 */
