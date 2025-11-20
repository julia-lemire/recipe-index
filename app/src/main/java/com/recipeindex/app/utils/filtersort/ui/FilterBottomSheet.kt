package com.recipeindex.app.utils.filtersort.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeindex.app.utils.filtersort.core.Filter

/**
 * Bottom sheet for filter selection
 *
 * @param availableFilters List of all available filters
 * @param activeFilterIds Set of currently active filter IDs
 * @param onFilterToggle Callback when filter is toggled
 * @param onClearAll Callback to clear all filters
 * @param onDismiss Callback when bottom sheet is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FilterBottomSheet(
    availableFilters: List<Filter<T>>,
    activeFilterIds: Set<String>,
    onFilterToggle: (Filter<T>) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleLarge
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clear All button (only show if filters are active)
                    if (activeFilterIds.isNotEmpty()) {
                        TextButton(onClick = {
                            onClearAll()
                            onDismiss()
                        }) {
                            Text("Clear All")
                        }
                    }

                    // Close button
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            }

            if (availableFilters.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No filters available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Filter list
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableFilters) { filter ->
                        FilterCheckboxItem(
                            filter = filter,
                            isChecked = filter.id in activeFilterIds,
                            onToggle = { onFilterToggle(filter) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> FilterCheckboxItem(
    filter: Filter<T>,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isChecked) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = filter.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isChecked) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Checkbox(
                checked = isChecked,
                onCheckedChange = null  // null because the whole surface is clickable
            )
        }
    }
}
