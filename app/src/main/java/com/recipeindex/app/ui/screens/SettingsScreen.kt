package com.recipeindex.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeindex.app.data.TemperatureUnit
import com.recipeindex.app.data.UnitSystem
import com.recipeindex.app.ui.viewmodels.SettingsViewModel
import com.recipeindex.app.utils.DebugConfig

/**
 * Settings Screen - App preferences and configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onMenuClick: () -> Unit = {}
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "SettingsScreen composed")

    val settings by viewModel.settings.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Are you sure you want to reset all settings to their default values?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Liquid Volume Units Section
            SettingsSection(title = "Liquid Volume Units") {
                Text(
                    text = "Choose how liquid measurements are displayed (cups, tbsp, tsp, fl oz)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Liquid Volume Radio Buttons
                UnitSystem.values().forEach { unit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.liquidVolumePreference == unit,
                            onClick = { viewModel.setLiquidVolumePreference(unit) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when (unit) {
                                    UnitSystem.IMPERIAL -> "Imperial"
                                    UnitSystem.METRIC -> "Metric"
                                    UnitSystem.BOTH -> "Show Both"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = when (unit) {
                                    UnitSystem.IMPERIAL -> "Cups, tablespoons, teaspoons, fluid ounces"
                                    UnitSystem.METRIC -> "Milliliters, liters"
                                    UnitSystem.BOTH -> "Show both units (e.g., \"1 cup (237 ml)\")"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Divider()

            // Weight Units Section
            SettingsSection(title = "Weight Units") {
                Text(
                    text = "Choose how weight measurements are displayed (oz, lbs)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Weight Radio Buttons
                UnitSystem.values().forEach { unit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.weightPreference == unit,
                            onClick = { viewModel.setWeightPreference(unit) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when (unit) {
                                    UnitSystem.IMPERIAL -> "Imperial"
                                    UnitSystem.METRIC -> "Metric"
                                    UnitSystem.BOTH -> "Show Both"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = when (unit) {
                                    UnitSystem.IMPERIAL -> "Ounces, pounds"
                                    UnitSystem.METRIC -> "Grams, kilograms"
                                    UnitSystem.BOTH -> "Show both units (e.g., \"1 lb (454 g)\")"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Divider()

            // Temperature Unit Section
            SettingsSection(title = "Temperature") {
                Text(
                    text = "Choose temperature display preference",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Temperature Radio Buttons
                TemperatureUnit.values().forEach { tempUnit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.temperatureUnit == tempUnit,
                            onClick = { viewModel.setTemperatureUnit(tempUnit) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (tempUnit) {
                                TemperatureUnit.FAHRENHEIT -> "Fahrenheit (°F)"
                                TemperatureUnit.CELSIUS -> "Celsius (°C)"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Divider()

            // Display Preferences Section
            SettingsSection(title = "Display") {
                // Show Photos in List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Show recipe photos in list",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Display recipe images in the recipe list view",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.showPhotosInList,
                        onCheckedChange = { viewModel.setShowPhotosInList(it) }
                    )
                }
            }

            Divider()

            // Recipe Defaults Section
            SettingsSection(title = "Recipe Defaults") {
                Column {
                    Text(
                        text = "Default servings for new recipes",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "The default number of servings when creating a new recipe",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Servings selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(1, 2, 4, 6, 8).forEach { servings ->
                            FilterChip(
                                selected = settings.defaultServings == servings,
                                onClick = { viewModel.setDefaultServings(servings) },
                                label = { Text(servings.toString()) }
                            )
                        }
                    }
                }
            }

            Divider()

            // Reset Section
            SettingsSection(title = "Reset") {
                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset All Settings to Defaults")
                }
            }
        }
    }
}

/**
 * Section container for settings
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}
