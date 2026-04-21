package com.recipeindex.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.TagStandardizer

/**
 * TagModificationDialog - Shows which tags were modified during standardization
 *
 * Allows users to review and edit each tag modification before accepting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagModificationDialog(
    modifications: List<TagStandardizer.TagModification>,
    onAccept: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    // Log dialog composition
    LaunchedEffect(modifications) {
        DebugConfig.debugLog(
            DebugConfig.Category.UI,
            "[TAG_DIALOG] Dialog composed/recomposed with ${modifications.size} modifications"
        )
        DebugConfig.debugLog(
            DebugConfig.Category.UI,
            "[TAG_DIALOG] Modifications: ${modifications.map { it.original to it.standardized }}"
        )
    }

    // Track which tags the user has chosen to keep/modify
    var editedTags by remember {
        mutableStateOf(modifications.map { it.standardized }.toMutableList())
    }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            DebugConfig.debugLog(
                DebugConfig.Category.UI,
                "[TAG_DIALOG] onDismissRequest called (back button or outside tap)"
            )
            onDismiss()
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Text(
                    text = "Review Tag Changes",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Some tags were modified during import. Review the changes below:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // List of modifications
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(modifications.indices.toList()) { index ->
                        val mod = modifications[index]
                        val currentTag = editedTags[index]
                        val isEditing = editingIndex == index
                        val isMarkedForDeletion = currentTag.isBlank()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isMarkedForDeletion -> MaterialTheme.colorScheme.errorContainer
                                    mod.wasModified -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                if (isEditing) {
                                    // Edit mode
                                    OutlinedTextField(
                                        value = editText,
                                        onValueChange = { editText = it },
                                        label = { Text("Edit tag") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = {
                                            editingIndex = null
                                            editText = ""
                                        }) {
                                            Text("Cancel")
                                        }
                                        TextButton(onClick = {
                                            if (editText.isNotBlank()) {
                                                editedTags = editedTags.toMutableList().apply {
                                                    this[index] = editText.trim().lowercase()
                                                }
                                            }
                                            editingIndex = null
                                            editText = ""
                                        }) {
                                            Text("Save")
                                        }
                                    }
                                } else {
                                    // Display mode
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (isMarkedForDeletion) {
                                                Text(
                                                    text = mod.original,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.error,
                                                    textDecoration = TextDecoration.LineThrough
                                                )
                                                Text(
                                                    text = "Marked for removal",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            } else if (mod.wasModified) {
                                                // BEFORE line
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "BEFORE",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                                                    )
                                                    Text(
                                                        text = mod.original,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
                                                        textDecoration = TextDecoration.LineThrough
                                                    )
                                                }
                                                // AFTER line
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "AFTER",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                                                    )
                                                    Text(
                                                        text = currentTag,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                                    )
                                                }
                                                if (mod.original != mod.standardized) {
                                                    Text(
                                                        text = getRejectionReason(mod.original, mod.standardized),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
                                                    )
                                                }
                                            } else {
                                                // No change
                                                Text(
                                                    text = currentTag,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "No changes needed",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                        }

                                        if (isMarkedForDeletion) {
                                            TextButton(
                                                onClick = {
                                                    editedTags = editedTags.toMutableList().apply {
                                                        this[index] = mod.standardized
                                                    }
                                                }
                                            ) {
                                                Text("Restore")
                                            }
                                        } else {
                                            // Edit + Remove icons stacked vertically
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            editingIndex = index
                                                            editText = currentTag
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Edit,
                                                            contentDescription = "Edit tag",
                                                            modifier = Modifier.size(16.dp),
                                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    }
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.errorContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            editedTags = editedTags.toMutableList().apply {
                                                                this[index] = ""
                                                            }
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Close,
                                                            contentDescription = "Remove tag",
                                                            modifier = Modifier.size(16.dp),
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            DebugConfig.debugLog(
                                DebugConfig.Category.UI,
                                "[TAG_DIALOG] Cancel button clicked"
                            )
                            onDismiss()
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            val finalTags = editedTags.filter { it.isNotBlank() }
                            DebugConfig.debugLog(
                                DebugConfig.Category.UI,
                                "[TAG_DIALOG] Accept clicked with ${finalTags.size} tags: $finalTags"
                            )
                            onAccept(finalTags)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Accept all changes")
                    }
                }
            }
        }
    }
}

/**
 * Get a human-readable reason for why a tag was modified
 */
private fun getRejectionReason(original: String, standardized: String): String {
    val originalLower = original.lowercase()
    val standardizedLower = standardized.lowercase()

    return when {
        originalLower.contains("recipe") || originalLower.contains("meal") || originalLower.contains("dish") ->
            "Removed noise words (recipe, meal, dish)"
        originalLower.contains("food") || originalLower.contains("cuisine") ->
            "Removed noise words (food, cuisine)"
        originalLower.contains("dinner") || originalLower.contains("lunch") || originalLower.contains("breakfast") ->
            "Removed meal type suffix"
        standardizedLower != originalLower && !originalLower.contains(standardizedLower) ->
            "Standardized to common form"
        else ->
            "Cleaned and simplified"
    }
}
