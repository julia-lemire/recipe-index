package com.recipeindex.app.utils.filtersort.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeindex.app.utils.filtersort.core.Sort
import com.recipeindex.app.utils.filtersort.core.SortDirection

/**
 * Icon button that opens a dropdown menu for sort options
 *
 * @param availableSorts List of all available sort options
 * @param currentSort Currently active sort (null if none)
 * @param onSortSelected Callback when a sort is selected
 * @param onSortDirectionToggle Callback when the direction toggle is clicked for current sort
 * @param modifier Modifier for the icon button
 */
@Composable
fun <T> SortMenu(
    availableSorts: List<Sort<T>>,
    currentSort: Sort<T>?,
    onSortSelected: (Sort<T>?) -> Unit,
    onSortDirectionToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { expanded = true },
        modifier = modifier
    ) {
        Icon(
            Icons.Default.Sort,
            contentDescription = "Sort options"
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Clear sort option
            if (currentSort != null) {
                DropdownMenuItem(
                    text = { Text("Clear Sort") },
                    onClick = {
                        onSortSelected(null)
                        expanded = false
                    }
                )
                HorizontalDivider()
            }

            // Sort options
            availableSorts.forEach { sort ->
                val isActive = currentSort?.id == sort.id

                DropdownMenuItem(
                    text = { Text(sort.label) },
                    onClick = {
                        if (isActive) {
                            // If already active, toggle direction
                            onSortDirectionToggle()
                        } else {
                            // Otherwise, select this sort
                            onSortSelected(sort)
                        }
                        expanded = false
                    },
                    leadingIcon = if (isActive) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active sort",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    trailingIcon = if (isActive) {
                        {
                            Icon(
                                imageVector = if (currentSort.direction == SortDirection.ASC) {
                                    Icons.Default.ArrowUpward
                                } else {
                                    Icons.Default.ArrowDownward
                                },
                                contentDescription = "Sort direction",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}
