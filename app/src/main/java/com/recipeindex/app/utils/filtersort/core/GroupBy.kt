package com.recipeindex.app.utils.filtersort.core

/**
 * Generic grouping interface for grouping lists of items
 *
 * @param T The type of item to group
 * @param K The type of the grouping key
 */
interface GroupBy<T, K> {
    /** Unique identifier for this grouping */
    val id: String

    /** Human-readable label for UI display */
    val label: String

    /**
     * Extracts the grouping key from an item
     *
     * @param item The item to extract the key from
     * @return The key to group this item by
     */
    fun extractKey(item: T): K

    /**
     * Optional: Provide a label for a group key
     * Defaults to toString() of the key
     */
    fun formatKeyLabel(key: K): String = key.toString()

    /**
     * Optional: Provide custom sort order for group keys
     * Defaults to natural order if K is Comparable
     */
    fun compareKeys(key1: K, key2: K): Int {
        @Suppress("UNCHECKED_CAST")
        return when {
            key1 is Comparable<*> && key2 is Comparable<*> -> {
                (key1 as Comparable<K>).compareTo(key2)
            }
            else -> 0
        }
    }
}
