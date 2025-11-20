package com.recipeindex.app.utils.filtersort.core

/**
 * Generic filter interface for filtering lists of items
 *
 * @param T The type of item to filter
 */
interface Filter<T> {
    /** Unique identifier for this filter */
    val id: String

    /** Human-readable label for UI display */
    val label: String

    /**
     * Tests whether an item matches this filter's criteria
     *
     * @param item The item to test
     * @return true if the item matches and should be included in results
     */
    fun matches(item: T): Boolean
}

/**
 * Composite filter that combines multiple filters with AND logic
 */
class AndFilter<T>(
    private val filters: List<Filter<T>>
) : Filter<T> {
    override val id: String = "and_${filters.joinToString("_") { it.id }}"
    override val label: String = filters.joinToString(" AND ") { it.label }

    override fun matches(item: T): Boolean {
        return filters.all { it.matches(item) }
    }
}

/**
 * Composite filter that combines multiple filters with OR logic
 */
class OrFilter<T>(
    private val filters: List<Filter<T>>
) : Filter<T> {
    override val id: String = "or_${filters.joinToString("_") { it.id }}"
    override val label: String = filters.joinToString(" OR ") { it.label }

    override fun matches(item: T): Boolean {
        return filters.any { it.matches(item) }
    }
}
