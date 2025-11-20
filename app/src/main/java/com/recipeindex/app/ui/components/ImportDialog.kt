package com.recipeindex.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.utils.DuplicateAction
import com.recipeindex.app.utils.ShareRecipe

/**
 * ImportDialog - Shows import preview and handles duplicate detection
 *
 * Supports:
 * - Single recipe import with duplicate options
 * - Meal plan import with multiple recipe duplicates
 * - Grocery list import (no duplicates)
 */

/**
 * Single recipe duplicate detection dialog
 */
@Composable
fun RecipeDuplicateDialog(
    existingRecipe: Recipe,
    newRecipe: ShareRecipe,
    onAction: (DuplicateAction) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Recipe Already Exists") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    "\"${newRecipe.title}\" already exists in your recipes.",
                    style = MaterialTheme.typography.bodyLarge
                )

                Divider()

                Text("Existing Recipe:", style = MaterialTheme.typography.labelMedium)
                Text(
                    "• ${existingRecipe.ingredients.size} ingredients\n" +
                    "• ${existingRecipe.instructions.size} steps\n" +
                    "• ${existingRecipe.servings} servings",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                Text("New Recipe:", style = MaterialTheme.typography.labelMedium)
                Text(
                    "• ${newRecipe.ingredients.size} ingredients\n" +
                    "• ${newRecipe.instructions.size} steps\n" +
                    "• ${newRecipe.servings} servings",
                    style = MaterialTheme.typography.bodyMedium
                )

                Divider()

                Text(
                    "What would you like to do?",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(DuplicateAction.REPLACE) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Replace Existing")
                }
                OutlinedButton(
                    onClick = { onAction(DuplicateAction.KEEP_BOTH) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Keep Both")
                }
                TextButton(
                    onClick = { onAction(DuplicateAction.SKIP) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

/**
 * Meal plan import dialog with multiple recipe duplicate handling
 */
@Composable
fun MealPlanImportDialog(
    mealPlanName: String,
    recipes: List<ShareRecipe>,
    duplicateRecipes: Map<String, Recipe>, // recipeTitle -> existing recipe
    onImport: (Map<String, DuplicateAction>) -> Unit, // recipeTitle -> action
    onDismiss: () -> Unit
) {
    var selectedActions by remember {
        mutableStateOf(
            recipes.associate { recipe ->
                recipe.title to if (duplicateRecipes.containsKey(recipe.title)) {
                    DuplicateAction.SKIP
                } else {
                    DuplicateAction.SKIP // Will import as new
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Meal Plan") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 400.dp)
            ) {
                Text(
                    "Meal Plan: $mealPlanName",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${recipes.size} recipes • ${duplicateRecipes.size} duplicates found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (duplicateRecipes.isNotEmpty()) {
                    Divider()
                    Text(
                        "Duplicate Recipes:",
                        style = MaterialTheme.typography.labelLarge
                    )

                    duplicateRecipes.forEach { (title, _) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(Modifier.height(8.dp))

                                // Action selector
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Action:", style = MaterialTheme.typography.labelSmall)

                                    FilterChip(
                                        selected = selectedActions[title] == DuplicateAction.SKIP,
                                        onClick = {
                                            selectedActions = selectedActions + (title to DuplicateAction.SKIP)
                                        },
                                        label = { Text("Skip", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    FilterChip(
                                        selected = selectedActions[title] == DuplicateAction.REPLACE,
                                        onClick = {
                                            selectedActions = selectedActions + (title to DuplicateAction.REPLACE)
                                        },
                                        label = { Text("Replace", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    FilterChip(
                                        selected = selectedActions[title] == DuplicateAction.KEEP_BOTH,
                                        onClick = {
                                            selectedActions = selectedActions + (title to DuplicateAction.KEEP_BOTH)
                                        },
                                        label = { Text("Keep Both", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (recipes.size > duplicateRecipes.size) {
                    Divider()
                    Text(
                        "New Recipes (${recipes.size - duplicateRecipes.size}):",
                        style = MaterialTheme.typography.labelMedium
                    )
                    recipes.filter { !duplicateRecipes.containsKey(it.title) }.forEach { recipe ->
                        Text(
                            "• ${recipe.title}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(selectedActions) }
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Grocery list import confirmation dialog
 */
@Composable
fun GroceryListImportDialog(
    listName: String,
    itemCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Grocery List") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "List: $listName",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "$itemCount items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "A new list will be created with all items.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Generic import success/error snackbar
 */
@Composable
fun ImportResultSnackbar(
    message: String,
    isError: Boolean = false,
    onDismiss: () -> Unit
) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        containerColor = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        contentColor = if (isError) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        action = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    ) {
        Text(message)
    }
}
