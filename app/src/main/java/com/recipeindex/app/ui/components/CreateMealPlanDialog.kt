package com.recipeindex.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.recipeindex.app.utils.DateFormatting

/**
 * Date-first meal plan creation dialog.
 *
 * Flow:
 *   1. Date range picker (start → end). "Skip" bypasses dates.
 *   2. Name field pre-filled from date range; user can edit.
 *   3. Confirm creates the plan.
 *
 * onConfirm receives (name, startDate, endDate).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMealPlanDialog(
    onConfirm: (name: String, startDate: Long?, endDate: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(Step.DATES) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var name by remember { mutableStateOf("") }
    var userEditedName by remember { mutableStateOf(false) }

    // Picking start or end
    var pickingStart by remember { mutableStateOf(true) }
    var showPicker by remember { mutableStateOf(false) }

    // Auto-fill name whenever dates change and user hasn't overridden it
    LaunchedEffect(startDate, endDate) {
        if (!userEditedName) {
            name = DateFormatting.autoNameFromDateRange(startDate, endDate)
        }
    }

    if (showPicker) {
        AppDatePickerDialog(
            initialDate = if (pickingStart) startDate else endDate,
            onDateSelected = { millis ->
                if (pickingStart) {
                    startDate = millis
                    // Move to end date picker
                    pickingStart = false
                } else {
                    endDate = millis
                    showPicker = false
                    step = Step.NAME
                }
            },
            onDismiss = {
                showPicker = false
                // If user dismissed mid-flow, jump to name step
                if (!pickingStart) step = Step.NAME
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (step == Step.DATES) "When is this plan?" else "Name your plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            when (step) {
                Step.DATES -> DateStep(
                    startDate = startDate,
                    endDate = endDate,
                    onPickStart = { pickingStart = true; showPicker = true },
                    onPickEnd = { pickingStart = false; showPicker = true },
                    onClearDates = { startDate = null; endDate = null }
                )
                Step.NAME -> NameStep(
                    name = name,
                    startDate = startDate,
                    endDate = endDate,
                    onNameChange = { name = it; userEditedName = true }
                )
            }
        },
        confirmButton = {
            when (step) {
                Step.DATES -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            // Skip dates — go straight to name
                            step = Step.NAME
                        }) { Text("Skip dates") }
                        Button(
                            onClick = { step = Step.NAME },
                            enabled = startDate != null
                        ) { Text("Next") }
                    }
                }
                Step.NAME -> {
                    Button(
                        onClick = { onConfirm(name.trim(), startDate, endDate) },
                        enabled = name.isNotBlank()
                    ) { Text("Create") }
                }
            }
        },
        dismissButton = {
            when (step) {
                Step.DATES -> TextButton(onClick = onDismiss) { Text("Cancel") }
                Step.NAME -> TextButton(onClick = { step = Step.DATES }) { Text("Back") }
            }
        }
    )
}

@Composable
private fun DateStep(
    startDate: Long?,
    endDate: Long?,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit,
    onClearDates: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Pick a start and end date to auto-name the plan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPickStart, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (startDate != null) DateFormatting.formatDateShort(startDate) else "Start")
            }
            OutlinedButton(onClick = onPickEnd, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (endDate != null) DateFormatting.formatDateShort(endDate) else "End")
            }
        }

        if (startDate != null || endDate != null) {
            TextButton(
                onClick = onClearDates,
                modifier = Modifier.align(Alignment.End)
            ) { Text("Clear") }
        }
    }
}

@Composable
private fun NameStep(
    name: String,
    startDate: Long?,
    endDate: Long?,
    onNameChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (startDate != null || endDate != null) {
            Text(
                text = DateFormatting.formatDateRange(startDate, endDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Plan name") },
            placeholder = { Text("e.g., Week of Apr 21") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private enum class Step { DATES, NAME }
