package com.recipeindex.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import android.view.WindowManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.platform.LocalView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.ui.components.SubstitutionDialog
import com.recipeindex.app.ui.viewmodels.SettingsViewModel
import com.recipeindex.app.ui.viewmodels.SubstitutionViewModel
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.IngredientScaler
import com.recipeindex.app.utils.IngredientUnitConverter
import com.recipeindex.app.utils.TextFormatUtils
import kotlinx.coroutines.delay

/**
 * RecipeDetailScreen - View recipe details
 *
 * Layout: Title, Servings/Prep/Cook time, Ingredients, Instructions, Tags, Notes
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    settingsViewModel: SettingsViewModel,
    substitutionViewModel: SubstitutionViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    onAddToGroceryList: () -> Unit = {},
    onAddToMealPlan: () -> Unit = {}
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "RecipeDetailScreen - ${recipe.title}")

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var selectedServings by remember { mutableStateOf(recipe.servings) }
    var showServingsMenu by remember { mutableStateOf(false) }

    // Cook mode state (persists while on this screen, resets on navigation away)
    var cookModeEnabled by remember { mutableStateOf(false) }
    var checkedIngredients by remember { mutableStateOf(setOf<Int>()) }
    var checkedInstructions by remember { mutableStateOf(setOf<Int>()) }
    var timerSecondsRemaining by remember { mutableStateOf(0) }
    var timerRunning by remember { mutableStateOf(false) }
    var timerInitialMinutes by remember { mutableStateOf(10) }
    var showTimerMenu by remember { mutableStateOf(false) }

    // Substitution dialog state
    var showSubstitutionDialog by remember { mutableStateOf(false) }
    var selectedIngredientForSub by remember { mutableStateOf<Triple<String, Double?, String>?>(null) } // ingredient, quantity, unit

    // Get user's unit preferences from settings
    val settings by settingsViewModel.settings.collectAsState()

    // Calculate scaling factor for ingredients
    val scaleFactor = selectedServings.toDouble() / recipe.servings.toDouble()

    // Parse instructions into steps for cook mode
    val instructionSteps = remember(recipe.instructions) {
        TextFormatUtils.parseInstructionsIntoSteps(recipe.instructions.joinToString("\n"))
    }

    // Handle system back button
    BackHandler {
        onBack()
    }

    // Keep screen awake when in cook mode
    val view = LocalView.current
    DisposableEffect(cookModeEnabled) {
        if (cookModeEnabled) {
            view.keepScreenOn = true
        }
        onDispose {
            view.keepScreenOn = false
        }
    }

    // Timer countdown logic
    LaunchedEffect(timerRunning, timerSecondsRemaining) {
        if (timerRunning && timerSecondsRemaining > 0) {
            delay(1000L)
            timerSecondsRemaining--
        } else if (timerSecondsRemaining == 0 && timerRunning) {
            // Timer finished - stop it
            timerRunning = false
            // TODO: Could add notification/sound here
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onToggleFavorite(!recipe.isFavorite) }) {
                        Icon(
                            if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Favorite"
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    // Inline buttons for Add to Grocery List and Add to Meal Plan
                    IconButton(onClick = onAddToGroceryList) {
                        Icon(Icons.Default.ShoppingCart, "Add to Grocery List")
                    }
                    IconButton(onClick = onAddToMealPlan) {
                        Icon(Icons.Default.CalendarMonth, "Add to Meal Plan")
                    }
                    // Cook mode toggle
                    IconButton(onClick = { cookModeEnabled = !cookModeEnabled }) {
                        Icon(
                            Icons.Default.RestaurantMenu,
                            "Cook Mode",
                            tint = if (cookModeEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    // Overflow menu with delete option only
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete recipe") },
                                onClick = {
                                    showOverflowMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
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
            // Recipe Photo
            recipe.photoPath?.let { photoUrl ->
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Recipe photo for ${recipe.title}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Crop
                )
            }

            // Servings and Time Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Servings dropdown
                        Column {
                            Text(
                                text = "Servings",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box {
                                OutlinedButton(
                                    onClick = { showServingsMenu = true },
                                    modifier = Modifier.height(40.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = selectedServings.toString(),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showServingsMenu,
                                    onDismissRequest = { showServingsMenu = false }
                                ) {
                                    // Servings options: half, original, double, custom multiples
                                    listOf(
                                        recipe.servings / 2,
                                        recipe.servings,
                                        recipe.servings * 2,
                                        recipe.servings * 3,
                                        recipe.servings * 4
                                    ).distinct().filter { it > 0 }.forEach { servings ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(servings.toString())
                                                    if (servings == recipe.servings) {
                                                        Text(
                                                            "(original)",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedServings = servings
                                                showServingsMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        recipe.prepTimeMinutes?.let {
                            InfoItem(label = "Prep", value = "$it min")
                        }
                        recipe.cookTimeMinutes?.let {
                            InfoItem(label = "Cook", value = "$it min")
                        }
                    }
                    recipe.prepTimeMinutes?.let { prep ->
                        recipe.cookTimeMinutes?.let { cook ->
                            InfoItem(label = "Total", value = "${prep + cook} min")
                        }
                    }
                }
            }

            // Cook Mode: Timer and Controls
            if (cookModeEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cook Mode",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            // Deselect all button
                            TextButton(
                                onClick = {
                                    checkedIngredients = setOf()
                                    checkedInstructions = setOf()
                                }
                            ) {
                                Text("Deselect All")
                            }
                        }

                        // Timer section
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Timer:",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                // Time dropdown
                                Box {
                                    OutlinedButton(
                                        onClick = {
                                            if (!timerRunning) {
                                                showTimerMenu = true
                                            }
                                        },
                                        modifier = Modifier.height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        enabled = !timerRunning
                                    ) {
                                        Text("${timerInitialMinutes} min")
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showTimerMenu,
                                        onDismissRequest = { showTimerMenu = false }
                                    ) {
                                        listOf(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60).forEach { minutes ->
                                            DropdownMenuItem(
                                                text = { Text("$minutes min") },
                                                onClick = {
                                                    timerInitialMinutes = minutes
                                                    showTimerMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Timer display
                                Text(
                                    text = String.format(
                                        "%02d:%02d",
                                        timerSecondsRemaining / 60,
                                        timerSecondsRemaining % 60
                                    ),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.weight(1f)
                                )

                                // Start/Pause button
                                FilledTonalIconButton(
                                    onClick = {
                                        if (timerSecondsRemaining == 0) {
                                            // Start new timer
                                            timerSecondsRemaining = timerInitialMinutes * 60
                                            timerRunning = true
                                        } else {
                                            // Toggle pause/resume
                                            timerRunning = !timerRunning
                                        }
                                    }
                                ) {
                                    Icon(
                                        if (timerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (timerRunning) "Pause" else "Start"
                                    )
                                }

                                // Reset button
                                FilledTonalIconButton(
                                    onClick = {
                                        timerSecondsRemaining = 0
                                        timerRunning = false
                                    },
                                    enabled = timerSecondsRemaining > 0
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                                }
                            }
                        }
                    }
                }
            }

            // Ingredients - scaled based on selected servings
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (selectedServings != recipe.servings) {
                        Text(
                            text = "Scaled for $selectedServings servings",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                recipe.ingredients.forEachIndexed { index, ingredient ->
                    val isChecked = index in checkedIngredients

                    // Parse ingredient for substitution lookup
                    val parsed = parseIngredientForSubstitution(ingredient)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = {
                                    if (cookModeEnabled) {
                                        checkedIngredients = if (isChecked) {
                                            checkedIngredients - index
                                        } else {
                                            checkedIngredients + index
                                        }
                                    }
                                },
                                onLongClick = {
                                    // Show substitution dialog
                                    selectedIngredientForSub = parsed
                                    showSubstitutionDialog = true
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Show checkbox in cook mode
                        if (cookModeEnabled) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    checkedIngredients = if (it) {
                                        checkedIngredients + index
                                    } else {
                                        checkedIngredients - index
                                    }
                                }
                            )
                        }

                        // Scale ingredient based on servings
                        var processedIngredient = if (scaleFactor != 1.0) {
                            IngredientScaler.scaleIngredient(ingredient, scaleFactor)
                        } else {
                            ingredient
                        }

                        // Format ingredient according to user's granular unit preferences
                        processedIngredient = IngredientUnitConverter.formatIngredient(
                            processedIngredient,
                            liquidPreference = settings.liquidVolumePreference,
                            weightPreference = settings.weightPreference
                        )

                        Text(
                            text = if (!cookModeEnabled) "• $processedIngredient" else processedIngredient,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontStyle = if (isChecked && cookModeEnabled) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                            ),
                            modifier = Modifier.alpha(if (isChecked && cookModeEnabled) 0.5f else 1f)
                        )
                    }
                }
            }

            // Instructions (with sections if present)
            InstructionsSection(
                instructions = recipe.instructions,
                cookModeEnabled = cookModeEnabled,
                instructionSteps = instructionSteps,
                checkedInstructions = checkedInstructions,
                onInstructionToggle = { stepIndex ->
                    checkedInstructions = if (stepIndex in checkedInstructions) {
                        checkedInstructions - stepIndex
                    } else {
                        checkedInstructions + stepIndex
                    }
                }
            )

            // Tags
            if (recipe.tags.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.titleMedium
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp
                    ) {
                        recipe.tags.forEach { tag ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
            }

            // Notes
            recipe.notes?.let { notes ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete \"${recipe.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Substitution dialog
    if (showSubstitutionDialog && selectedIngredientForSub != null) {
        SubstitutionDialog(
            ingredient = selectedIngredientForSub!!.first,
            quantity = selectedIngredientForSub!!.second,
            unit = selectedIngredientForSub!!.third,
            viewModel = substitutionViewModel,
            onDismiss = { showSubstitutionDialog = false }
        )
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Parse instructions into sections
 * Sections are detected by lines ending with ":"
 */
private data class InstructionSection(
    val name: String,
    val steps: List<String>
)

private fun parseInstructionSections(instructions: List<String>): List<InstructionSection> {
    val sections = mutableListOf<InstructionSection>()
    var currentSection: String? = null
    var currentSteps = mutableListOf<String>()

    instructions.forEach { instruction ->
        if (instruction.endsWith(":")) {
            // Save previous section if exists
            if (currentSection != null && currentSteps.isNotEmpty()) {
                sections.add(InstructionSection(currentSection, currentSteps.toList()))
                currentSteps.clear()
            }
            // Start new section
            currentSection = instruction.removeSuffix(":")
        } else {
            // Add to current section
            currentSteps.add(instruction)
        }
    }

    // Add final section
    if (currentSection != null && currentSteps.isNotEmpty()) {
        sections.add(InstructionSection(currentSection, currentSteps.toList()))
    } else if (sections.isEmpty() && currentSteps.isNotEmpty()) {
        // No sections found, treat all as default section
        sections.add(InstructionSection("Instructions", currentSteps.toList()))
    }

    // If no sections detected and we still have instructions, use all instructions
    if (sections.isEmpty() && instructions.isNotEmpty()) {
        sections.add(InstructionSection("Instructions", instructions))
    }

    return sections
}

@Composable
private fun InstructionsSection(
    instructions: List<String>,
    cookModeEnabled: Boolean = false,
    instructionSteps: List<String> = emptyList(),
    checkedInstructions: Set<Int> = emptySet(),
    onInstructionToggle: (Int) -> Unit = {}
) {
    val sections = remember(instructions) { parseInstructionSections(instructions) }

    if (sections.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Instructions",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        // Cook mode: simple checkable list with bold numbers
        if (cookModeEnabled) {
            instructionSteps.forEachIndexed { index, step ->
                val isChecked = index in checkedInstructions

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            onInstructionToggle(index)
                        },
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = {
                            onInstructionToggle(index)
                        }
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Step ${index + 1}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontStyle = if (isChecked) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                            ),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.alpha(if (isChecked) 0.5f else 1f)
                        )
                        Text(
                            text = TextFormatUtils.highlightNumbersInText(step),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontStyle = if (isChecked) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                            ),
                            modifier = Modifier.alpha(if (isChecked) 0.5f else 1f)
                        )
                    }
                }
            }
        } else if (sections.size > 1) {
            // Multiple sections - use tabs
            var selectedTabIndex by remember { mutableIntStateOf(0) }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Tab Row
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        sections.forEachIndexed { index, section ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(section.name) }
                            )
                        }
                    }

                    // Tab Content
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        sections[selectedTabIndex].steps.forEachIndexed { index, step ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Step ${index + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Single section - simple list
            sections.first().steps.forEachIndexed { index, step ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Step ${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

/**
 * Custom FlowRow implementation for wrapping tags
 * Tags are only as wide as their content, wrapping to next line when needed
 */
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hSpacing = horizontalSpacing.roundToPx()
        val vSpacing = verticalSpacing.roundToPx()

        // Measure each child with no minimum width constraint - only as wide as needed
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0))
        }

        // Build rows
        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        placeables.forEach { placeable ->
            val itemWidth = placeable.width + hSpacing

            // Check if adding this item would exceed the max width
            if (currentRowWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }

            currentRow.add(placeable)
            currentRowWidth += itemWidth
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        // Calculate total height
        val height = rows.mapIndexed { index, row ->
            row.maxOf { it.height } + if (index < rows.size - 1) vSpacing else 0
        }.sum()

        layout(constraints.maxWidth, height) {
            var yPosition = 0
            rows.forEach { row ->
                var xPosition = 0
                val rowHeight = row.maxOf { it.height }

                row.forEach { placeable ->
                    placeable.placeRelative(xPosition, yPosition)
                    xPosition += placeable.width + hSpacing
                }

                yPosition += rowHeight + vSpacing
            }
        }
    }
}

/**
 * Parse ingredient string to extract name, quantity, and unit for substitution lookup
 * Returns Triple<ingredient, quantity, unit>
 * Example: "2 cups butter" -> Triple("butter", 2.0, "cups")
 */
private fun parseIngredientForSubstitution(ingredient: String): Triple<String, Double?, String> {
    val trimmed = ingredient.trim()

    // Common units (singular forms for matching)
    val knownUnits = listOf(
        "cup", "tbsp", "tsp", "oz", "lb", "g", "kg", "ml", "l",
        "can", "pack", "bottle", "jar", "tablespoon", "teaspoon",
        "ounce", "pound", "gram", "kilogram", "liter", "milliliter"
    )

    // Try to parse quantity at the beginning
    val quantityPattern = Regex("^([\\d./\\s-]+)\\s+(.+)")
    val match = quantityPattern.find(trimmed)

    if (match != null) {
        val quantityStr = match.groupValues[1]
        val remainder = match.groupValues[2]

        // Parse quantity
        val quantity = parseQuantity(quantityStr)

        // Try to extract unit from remainder
        val words = remainder.split(" ", limit = 3)
        if (words.isNotEmpty()) {
            val possibleUnit = words[0].lowercase().removeSuffix("s") // Handle plurals
            val matchedUnit = knownUnits.find { it == possibleUnit }

            if (matchedUnit != null) {
                // Unit found, rest is ingredient
                val ingredientName = if (words.size > 1) words.drop(1).joinToString(" ") else remainder
                return Triple(ingredientName.trim(), quantity, words[0]) // Return original unit with plural
            }
        }

        // No unit found, entire remainder is ingredient
        return Triple(remainder.trim(), quantity, "")
    }

    // No quantity found, entire string is ingredient
    return Triple(trimmed, null, "")
}

/**
 * Parse a quantity string to a Double
 * Handles: "1/2", "1 1/2", "0.5", "2", "2-3" (takes first number)
 */
private fun parseQuantity(quantityStr: String): Double? {
    val trimmed = quantityStr.trim()

    // Handle range (2-3) - take the first number
    if (trimmed.contains('-')) {
        val parts = trimmed.split('-')
        if (parts.size == 2) {
            return parseQuantity(parts[0].trim())
        }
    }

    // Handle mixed numbers (1 1/2)
    val mixedPattern = Regex("^(\\d+)\\s+(\\d+)/(\\d+)$")
    val mixedMatch = mixedPattern.find(trimmed)
    if (mixedMatch != null) {
        val whole = mixedMatch.groupValues[1].toDoubleOrNull() ?: return null
        val numerator = mixedMatch.groupValues[2].toDoubleOrNull() ?: return null
        val denominator = mixedMatch.groupValues[3].toDoubleOrNull() ?: return null
        if (denominator == 0.0) return null
        return whole + (numerator / denominator)
    }

    // Handle fractions (1/2)
    val fractionPattern = Regex("^(\\d+)/(\\d+)$")
    val fractionMatch = fractionPattern.find(trimmed)
    if (fractionMatch != null) {
        val numerator = fractionMatch.groupValues[1].toDoubleOrNull() ?: return null
        val denominator = fractionMatch.groupValues[2].toDoubleOrNull() ?: return null
        if (denominator == 0.0) return null
        return numerator / denominator
    }

    // Handle decimals and whole numbers
    return trimmed.toDoubleOrNull()
}
