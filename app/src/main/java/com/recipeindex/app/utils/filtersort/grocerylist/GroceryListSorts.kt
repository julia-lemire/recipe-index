package com.recipeindex.app.utils.filtersort.grocerylist

import com.recipeindex.app.data.entities.GroceryList
import com.recipeindex.app.utils.filtersort.core.BaseSort
import com.recipeindex.app.utils.filtersort.core.Sort
import com.recipeindex.app.utils.filtersort.core.SortDirection

/**
 * Sort grocery lists by name (alphabetically)
 */
class NameSort(
    direction: SortDirection = SortDirection.ASC
) : BaseSort<GroceryList>("name", "Name", direction) {

    override fun getBaseComparator(): Comparator<GroceryList> {
        return compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
    }

    override fun reversed(): Sort<GroceryList> {
        return NameSort(direction.reversed())
    }
}

/**
 * Sort grocery lists by creation date (newest first by default)
 */
class DateCreatedSort(
    direction: SortDirection = SortDirection.DESC
) : BaseSort<GroceryList>("date_created", "Date Created", direction) {

    override fun getBaseComparator(): Comparator<GroceryList> {
        return compareBy { it.createdAt }
    }

    override fun reversed(): Sort<GroceryList> {
        return DateCreatedSort(direction.reversed())
    }
}

/**
 * Sort grocery lists by last modified date (most recently modified first by default)
 */
class DateModifiedSort(
    direction: SortDirection = SortDirection.DESC
) : BaseSort<GroceryList>("date_modified", "Last Modified", direction) {

    override fun getBaseComparator(): Comparator<GroceryList> {
        return compareBy { it.updatedAt }
    }

    override fun reversed(): Sort<GroceryList> {
        return DateModifiedSort(direction.reversed())
    }
}

/**
 * Note: Sorts based on item count or completion percentage would require a composite
 * data type that includes both the GroceryList and its items. Since grocery lists are
 * short-lived (per user feedback), these simple sorts should suffice.
 */
