package com.recipeindex.app.utils.filtersort.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeindex.app.utils.filtersort.core.Filter

/**
 * Horizontal scrollable row of filter chips
 *
 * @param availableFilters List of all available filters to choose from
 * @param activeFilterIds Set of IDs for currently active filters
 * @param onFilterToggle Callback when a filter is toggled on/off
 * @param onClearAll Callback when "Clear All" is clicked (optional)
 * @param modifier Modifier for the row
 */
@Composable
fun <T> FilterChipRow(
    availableFilters: List<Filter<T>>,
    activeFilterIds: Set<String>,
    onFilterToggle: (Filter<T>) -> Unit,
    onClearAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Clear all button (only show if filters are active and callback provided)
        if (activeFilterIds.isNotEmpty() && onClearAll != null) {
            AssistChip(
                onClick = onClearAll,
                label = { Text("Clear All") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        // Filter chips
        availableFilters.forEach { filter ->
            val isActive = activeFilterIds.contains(filter.id)

            FilterChip(
                selected = isActive,
                onClick = { onFilterToggle(filter) },
                label = { Text(filter.label) },
                leadingIcon = if (isActive) {
                    {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove filter",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
        }
    }
}
