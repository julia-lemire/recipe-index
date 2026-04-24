package com.recipeindex.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeindex.app.data.entities.MealPlan
import com.recipeindex.app.utils.DateFormatting

/**
 * Meal plan picker — select an existing plan or create a new one (date-first flow).
 * onCreateNew receives (name, startDate, endDate).
 */
@Composable
fun MealPlanPickerDialog(
    availablePlans: List<MealPlan>,
    onDismiss: () -> Unit,
    onPlanSelected: (Long) -> Unit,
    onCreateNew: (name: String, startDate: Long?, endDate: Long?) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateMealPlanDialog(
            onConfirm = { name, start, end ->
                onCreateNew(name, start, end)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Meal Plan") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select a plan or create a new one:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create New Plan")
                }

                if (availablePlans.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(availablePlans, key = { it.id }) { plan ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onPlanSelected(plan.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = plan.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        val dateLabel = DateFormatting.formatDateRange(plan.startDate, plan.endDate)
                                        if (dateLabel.isNotBlank()) {
                                            Text(
                                                text = dateLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No plans yet. Create one to get started!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
