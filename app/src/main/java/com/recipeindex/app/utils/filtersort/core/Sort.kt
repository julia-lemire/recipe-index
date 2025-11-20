package com.recipeindex.app.utils.filtersort.core

/**
 * Sort direction enum
 */
enum class SortDirection {
    ASC,
    DESC;

    fun reversed(): SortDirection = when (this) {
        ASC -> DESC
        DESC -> ASC
    }
}

/**
 * Generic sort interface for sorting lists of items
 *
 * @param T The type of item to sort
 */
interface Sort<T> {
    /** Unique identifier for this sort */
    val id: String

    /** Human-readable label for UI display */
    val label: String

    /** Sort direction (ascending or descending) */
    val direction: SortDirection

    /** The comparator that defines the sort order */
    val comparator: Comparator<T>

    /**
     * Returns a copy of this sort with reversed direction
     */
    fun reversed(): Sort<T>
}

/**
 * Base implementation of Sort interface
 */
abstract class BaseSort<T>(
    override val id: String,
    override val label: String,
    override val direction: SortDirection = SortDirection.ASC
) : Sort<T> {

    /**
     * Implement this to provide the base comparator (assumes ASC direction)
     */
    protected abstract fun getBaseComparator(): Comparator<T>

    override val comparator: Comparator<T>
        get() = if (direction == SortDirection.ASC) {
            getBaseComparator()
        } else {
            getBaseComparator().reversed()
        }
}
