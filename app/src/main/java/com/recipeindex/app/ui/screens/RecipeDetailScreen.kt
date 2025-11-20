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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.ui.components.SubstitutionDialog
import com.recipeindex.app.ui.viewmodels.RecipeViewModel
import com.recipeindex.app.ui.viewmodels.SettingsViewModel
import com.recipeindex.app.ui.viewmodels.SubstitutionViewModel
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.IngredientScaler
import com.recipeindex.app.utils.IngredientUnitConverter
import com.recipeindex.app.utils.ShareHelper
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
    recipeViewModel: RecipeViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    onAddToGroceryList: () -> Unit = {},
    onAddToMealPlan: () -> Unit = {}
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "RecipeDetailScreen - ${recipe.title}")

    val context = LocalContext.current
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
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Row with Edit/Cook/Context buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Edit button
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    // Cook mode toggle
                    IconButton(
                        onClick = { cookModeEnabled = !cookModeEnabled },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (cookModeEnabled) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.RestaurantMenu, contentDescription = "Cook mode")
                    }
                    // Context menu
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
            }

            // Compact info line (Servings | Prep | Cook | Total)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Servings with dropdown
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🍽️", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(4.dp))
                    Box {
                        TextButton(
                            onClick = { showServingsMenu = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                "$selectedServings",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(
                            expanded = showServingsMenu,
                            onDismissRequest = { showServingsMenu = false }
                        ) {
                            listOf(
                                recipe.servings / 2,
                                recipe.servings,
                                recipe.servings * 2,
                                recipe.servings * 3,
                                recipe.servings * 4
                            ).distinct().filter { it > 0 }.forEach { servings ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (servings == recipe.servings) "$servings (original)" else "$servings",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
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
                    Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("⏱️ ${it}m prep", style = MaterialTheme.typography.bodyMedium)
                }
                recipe.cookTimeMinutes?.let {
                    Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("🔥 ${it}m cook", style = MaterialTheme.typography.bodyMedium)
                }
                recipe.prepTimeMinutes?.let { prep ->
                    recipe.cookTimeMinutes?.let { cook ->
                        Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("⏰ ${prep + cook}m total", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddToGroceryList,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("Grocery", style = MaterialTheme.typography.labelSmall)
                    }
                }
                OutlinedButton(
                    onClick = onAddToMealPlan,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("Meal Plan", style = MaterialTheme.typography.labelSmall)
                    }
                }
                OutlinedButton(
                    onClick = {
                        ShareHelper.shareRecipe(context, recipe, recipe.photoPath)
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("Share", style = MaterialTheme.typography.labelSmall)
                    }
                }
                OutlinedButton(
                    onClick = { onToggleFavorite(!recipe.isFavorite) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (recipe.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Favorite", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

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

            // Cook Mode: Timer and Controls
            if (cookModeEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
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
                                color = Color.Black
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
                            .padding(vertical = if (cookModeEnabled) 2.dp else 4.dp)
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

            // Recipe Log (only show when not in cook mode)
            if (!cookModeEnabled) {
                RecipeLogSection(
                    recipeId = recipe.id,
                    recipeViewModel = recipeViewModel
                )
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
                        .padding(vertical = 4.dp)
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

/**
 * RecipeLogSection - Shows when the recipe was last made and history
 */
@Composable
private fun RecipeLogSection(
    recipeId: Long,
    recipeViewModel: RecipeViewModel
) {
    val logs by recipeViewModel.getLogsForRecipe(recipeId).collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }
    var showMarkAsMadeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header with expand/collapse and "Mark as Made" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cooking Log",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mark as Made button
                FilledTonalButton(onClick = { showMarkAsMadeDialog = true }) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Mark as Made",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Mark as Made")
                }

                // Expand/Collapse if there are logs
                if (logs.isNotEmpty()) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowBack,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }
            }
        }

        // Summary info
        if (logs.isNotEmpty()) {
            val lastLog = logs.first() // Already sorted by timestamp DESC
            val timeAgo = formatTimeAgo(lastLog.timestamp)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Last made $timeAgo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Made ${logs.size} time${if (logs.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = "Not made yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Expanded log history
        if (expanded && logs.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleSmall
                    )

                    logs.forEach { log ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = formatDate(log.timestamp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                log.notes?.let { notes ->
                                    Text(
                                        text = notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                log.rating?.let { rating ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        repeat(rating) {
                                            Icon(
                                                Icons.Default.Favorite,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    recipeViewModel.deleteLog(log.id)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete log",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        if (log != logs.last()) {
                            Divider()
                        }
                    }
                }
            }
        }
    }

    // Mark as Made Dialog
    if (showMarkAsMadeDialog) {
        var notes by remember { mutableStateOf("") }
        var rating by remember { mutableIntStateOf(0) }

        AlertDialog(
            onDismissRequest = { showMarkAsMadeDialog = false },
            title = { Text("Mark as Made") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Rating
                    Text(
                        text = "Rating (optional)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { rating = if (rating == star) 0 else star }
                            ) {
                                Icon(
                                    if (star <= rating) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "$star stars",
                                    tint = if (star <= rating)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        placeholder = { Text("e.g., Added extra garlic") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        recipeViewModel.markRecipeAsMade(
                            recipeId = recipeId,
                            notes = notes.ifBlank { null },
                            rating = if (rating > 0) rating else null
                        )
                        showMarkAsMadeDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAsMadeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Format timestamp as relative time (e.g., "2 days ago")
 */
private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30

    return when {
        months > 0 -> "${months} month${if (months == 1L) "" else "s"} ago"
        weeks > 0 -> "${weeks} week${if (weeks == 1L) "" else "s"} ago"
        days > 0 -> "${days} day${if (days == 1L) "" else "s"} ago"
        hours > 0 -> "${hours} hour${if (hours == 1L) "" else "s"} ago"
        minutes > 0 -> "${minutes} minute${if (minutes == 1L) "" else "s"} ago"
        else -> "just now"
    }
}

/**
 * Format timestamp as readable date
 */
private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault())
    return format.format(date)
}
