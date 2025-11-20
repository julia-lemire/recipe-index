package com.recipeindex.app.utils.filtersort.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

/**
 * Generic manager for filtering, sorting, and grouping lists of items
 *
 * Combines search, filters, sorting, and grouping into a single reactive pipeline.
 * All state is exposed via StateFlow for Compose integration.
 *
 * @param T The type of items to filter/sort/group
 * @param sourceItems Flow of source items from database/repository
 * @param searchPredicate Function that determines if an item matches a search query
 * @param scope CoroutineScope for Flow operations
 */
class FilterSortGroupManager<T>(
    sourceItems: Flow<List<T>>,
    private val searchPredicate: (item: T, query: String) -> Boolean,
    scope: CoroutineScope
) {
    // Input state
    private val _searchQuery = MutableStateFlow("")
    private val _activeFilters = MutableStateFlow<Map<String, Filter<T>>>(emptyMap())
    private val _currentSort = MutableStateFlow<Sort<T>?>(null)
    private val _currentGroupBy = MutableStateFlow<GroupBy<T, *>?>(null)

    // Exposed read-only state
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val activeFilters: StateFlow<Map<String, Filter<T>>> = _activeFilters.asStateFlow()
    val currentSort: StateFlow<Sort<T>?> = _currentSort.asStateFlow()
    val currentGroupBy: StateFlow<GroupBy<T, *>?> = _currentGroupBy.asStateFlow()

    /**
     * Output: Filtered and sorted items (when no grouping is active)
     */
    val filteredItems: StateFlow<List<T>> = combine(
        sourceItems,
        _searchQuery,
        _activeFilters,
        _currentSort,
        _currentGroupBy
    ) { items, query, filters, sort, groupBy ->
        // Skip processing if grouping is active (use groupedItems instead)
        if (groupBy != null) {
            return@combine emptyList()
        }

        var result = items

        // Apply search
        if (query.isNotBlank()) {
            result = result.filter { searchPredicate(it, query) }
        }

        // Apply filters
        if (filters.isNotEmpty()) {
            result = result.filter { item ->
                filters.values.all { filter -> filter.matches(item) }
            }
        }

        // Apply sort
        if (sort != null) {
            result = result.sortedWith(sort.comparator)
        }

        result
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Output: Grouped items (when grouping is active)
     * Returns Map<K, List<T>> where K is the group key type
     */
    val groupedItems: StateFlow<Map<*, List<T>>?> = combine(
        sourceItems,
        _searchQuery,
        _activeFilters,
        _currentSort,
        _currentGroupBy
    ) { items, query, filters, sort, groupBy ->
        // Skip if no grouping active
        if (groupBy == null) {
            return@combine null
        }

        var result = items

        // Apply search
        if (query.isNotBlank()) {
            result = result.filter { searchPredicate(it, query) }
        }

        // Apply filters
        if (filters.isNotEmpty()) {
            result = result.filter { item ->
                filters.values.all { filter -> filter.matches(item) }
            }
        }

        // Group items
        @Suppress("UNCHECKED_CAST")
        val grouped = result.groupBy { (groupBy as GroupBy<T, Any?>).extractKey(it) }

        // Sort groups by key
        val sortedGroups = grouped.toSortedMap { key1, key2 ->
            (groupBy as GroupBy<T, Any?>).compareKeys(key1, key2)
        }

        // Sort items within each group if sort is specified
        if (sort != null) {
            sortedGroups.mapValues { (_, groupItems) ->
                groupItems.sortedWith(sort.comparator)
            }
        } else {
            sortedGroups
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Set the search query
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Add or update a filter
     */
    fun addFilter(filter: Filter<T>) {
        _activeFilters.value = _activeFilters.value + (filter.id to filter)
    }

    /**
     * Remove a filter by ID
     */
    fun removeFilter(filterId: String) {
        _activeFilters.value = _activeFilters.value - filterId
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        _activeFilters.value = emptyMap()
    }

    /**
     * Set the current sort (null to clear)
     */
    fun setSort(sort: Sort<T>?) {
        _currentSort.value = sort
    }

    /**
     * Toggle sort direction (keeps same sort, reverses direction)
     */
    fun toggleSortDirection() {
        _currentSort.value = _currentSort.value?.reversed()
    }

    /**
     * Set the current grouping (null to clear)
     */
    fun setGroupBy(groupBy: GroupBy<T, *>?) {
        _currentGroupBy.value = groupBy
    }

    /**
     * Check if any filters are active
     */
    fun hasActiveFilters(): Boolean = _activeFilters.value.isNotEmpty()

    /**
     * Check if a specific filter is active
     */
    fun isFilterActive(filterId: String): Boolean = _activeFilters.value.containsKey(filterId)
}
