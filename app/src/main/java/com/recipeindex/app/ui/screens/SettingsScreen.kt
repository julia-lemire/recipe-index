package com.recipeindex.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.recipeindex.app.data.TemperatureUnit
import com.recipeindex.app.data.UnitSystem
import com.recipeindex.app.ui.MainActivity
import com.recipeindex.app.ui.viewmodels.SettingsViewModel
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.RecipeTemplateHelper
import kotlinx.coroutines.launch

/**
 * Settings Screen - App preferences and configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onMenuClick: () -> Unit = {},
    onNavigateToPantryStaples: () -> Unit = {}
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

            // Dry Volume Units Section
            SettingsSection(title = "Dry Volume Units") {
                Text(
                    text = "Choose how dry ingredients are displayed (flour, sugar, spices)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dry Volume Radio Buttons
                UnitSystem.values().forEach { unit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.dryVolumePreference == unit,
                            onClick = { viewModel.setDryVolumePreference(unit) }
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
                                    UnitSystem.IMPERIAL -> "Cups for dry ingredients"
                                    UnitSystem.METRIC -> "Grams for dry ingredients"
                                    UnitSystem.BOTH -> "Show both units (e.g., \"1 cup (120g)\")"
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

            // Import/Export Section
            SettingsSection(title = "Import / Export") {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                var importResult by remember { mutableStateOf<String?>(null) }

                // File picker for import
                val filePicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri ->
                    uri?.let {
                        scope.launch {
                            try {
                                val json = context.contentResolver.openInputStream(it)
                                    ?.bufferedReader()
                                    ?.use { reader -> reader.readText() }

                                if (json != null) {
                                    // Store in MainActivity for import dialog handling
                                    MainActivity.pendingImportJson = json
                                    importResult = "File loaded. Navigate to Recipes screen to complete import."
                                    DebugConfig.debugLog(DebugConfig.Category.UI, "Import file loaded from settings")
                                } else {
                                    importResult = "Failed to read file"
                                }
                            } catch (e: Exception) {
                                DebugConfig.error(DebugConfig.Category.UI, "Import file error", e)
                                importResult = "Error: ${e.message}"
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Import recipes, meal plans, or grocery lists shared from other devices",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = {
                            filePicker.launch(arrayOf("application/json", "text/plain"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Import from File")
                    }

                    importResult?.let { result ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (result.startsWith("Error") || result.startsWith("Failed")) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                }
                            )
                        ) {
                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Create recipes manually using a text template you can edit on your computer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = {
                            RecipeTemplateHelper.shareTemplate(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Get Recipe Template")
                    }
                }
            }

            Divider()

            // Grocery List Section
            SettingsSection(title = "Grocery Lists") {
                Text(
                    text = "Configure grocery list behavior",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onNavigateToPantryStaples,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Manage Pantry Staples Filter", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Configure which items are filtered from grocery lists",
                            style = MaterialTheme.typography.bodySmall
                        )
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
